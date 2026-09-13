package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.core.GameEngine;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;
import it.unicam.cs.mpgc.rpg126599.persistence.GameJsonStorage;

/**
 * Responsabilità unica: tradurre l'input dell'utente in chiamate a GameEngine
 * e richiedere il refresh della vista dopo ogni azione.
 *
 * Prima questa classe (549 righe) era una God Class che faceva anche da:
 *  - gestore di visibilità/testo di ogni singolo pulsante del pannello
 *    (spostato in GameActionPanelView);
 *  - gestore di navigazione tra schermate, con FXMLLoader/Stage/Scene
 *    duplicati (spostato in NavigationService);
 *  - un controllo "il gioco è finito?" ripetuto identico in nove metodi
 *    diversi (ora centralizzato in ensureGameOngoing()/runGuardedAction()).
 *
 * Quello che resta qui è davvero solo controller-logic: leggere il click,
 * decidere quale azione dell'engine invocare, e chiedere alla vista di
 * aggiornarsi.
 */
public class GameController {

    private enum PendingAction {
        NONE, MOVE, FAKE_CLUE, ARREST, TRAP_KIT, SHORTCUT_MOVE, ROADBLOCK, CHECKPOINT, SCANNER
    }

    @FXML private MapController mapController;

    @FXML private Label statusLabel;
    @FXML private Label inventoryLabel;
    @FXML private Label commandsHeaderLabel;
    @FXML private Label inventoryHeaderLabel;
    @FXML private Button useClueButton;
    @FXML private Button moveButton;
    @FXML private Button fakeClueButton;
    @FXML private Button arrestButton;

    @FXML private HBox killerInventoryRow;
    @FXML private HBox policeInventoryRow;
    @FXML private Button smokeBombButton;
    @FXML private Button trapKitButton;
    @FXML private Button shortcutMapButton;
    @FXML private Button roadblockButton;
    @FXML private Button checkpointButton;
    @FXML private Button scannerButton;

    private final GameJsonStorage storage = new GameJsonStorage();
    private GameActionPanelView actionPanel;
    private GameEngine engine;
    private CampaignManager campaign;
    private boolean matchResultRecorded;
    private PendingAction pendingAction = PendingAction.NONE;
    private String temporaryInfoMessage;
    private PauseTransition temporaryMessageTimer;

    @FXML
    private void initialize() {
        actionPanel = new GameActionPanelView(commandsHeaderLabel, inventoryHeaderLabel,
                useClueButton, moveButton, fakeClueButton, arrestButton,
                killerInventoryRow, policeInventoryRow,
                smokeBombButton, trapKitButton, shortcutMapButton,
                roadblockButton, checkpointButton, scannerButton);
    }

    // retrocompatibilità: partita singola senza campagna (es. "Carica partita salvata")
    public void init(GameEngine engine) {
        this.engine = engine;
        this.campaign = null;
        this.matchResultRecorded = false;
        mapController.setOnNodeClicked(this::onNodeClicked);
        refreshView();
    }

    // avvio di un match all'interno della campagna Best of 3
    public void init(CampaignManager campaign) {
        this.campaign = campaign;
        this.engine = campaign.getCurrentEngine();
        this.matchResultRecorded = false;
        mapController.setOnNodeClicked(this::onNodeClicked);
        refreshView();
    }

    private void onNodeClicked(String locationId) {
        if (!ensureGameOngoing()) return;

        Turn phase = engine.getState().getPhase();
        RoleType humanRole = engine.getState().getHumanRole();

        try {
            if (phase == Turn.AWAITING_HOME_CHOICE && humanRole == RoleType.KILLER) {
                engine.chooseHome(locationId);
            } else if (phase == Turn.AWAITING_MURDER_LOCATION_CHOICE && humanRole == RoleType.KILLER) {
                engine.chooseMurderLocation(locationId);
            } else if (phase == Turn.AWAITING_KILLER_ACTION && humanRole == RoleType.KILLER) {
                handleKillerNodeClick(locationId);
            } else if (phase == Turn.AWAITING_POLICE_ACTION && humanRole == RoleType.POLICE) {
                handlePoliceNodeClick(locationId);
            } else {
                return;
            }
        } catch (IllegalArgumentException | IllegalStateException invalidAction) {
            statusLabel.setText(invalidAction.getMessage());
            return;
        }

        resetPendingAction();
        refreshView();
    }

    private void handleKillerNodeClick(String locationId) {
        switch (pendingAction) {
            case MOVE -> {
                engine.killerMove(locationId);
                showTemporaryFeedback("⭐ +" + lastXp(RoleType.KILLER) + " XP", Duration.seconds(4.5));
            }
            case FAKE_CLUE -> engine.killerLeaveFakeClue(locationId);
            case TRAP_KIT -> engine.killerPlaceTrap(locationId);
            case SHORTCUT_MOVE -> {
                engine.killerUseShortcutMap(locationId);
                showTemporaryFeedback("⭐ +" + lastXp(RoleType.KILLER) + " XP", Duration.seconds(4.5));
            }
            default -> throw new IllegalStateException("Scegli prima un'azione dal pannello comandi.");
        }
    }

    private void handlePoliceNodeClick(String locationId) {
        switch (pendingAction) {
            case MOVE -> {
                engine.policeMoveTo(locationId);
                showTemporaryFeedback("⭐ +" + lastXp(RoleType.POLICE) + " XP", Duration.seconds(4.5));
            }
            case ARREST -> engine.policeAttemptArrest(locationId);
            case ROADBLOCK -> engine.policePlaceRoadblock(locationId);
            case CHECKPOINT ->
                    engine.policeUseCheckpoint(engine.getState().getPolice().getCurrentLocationId(), locationId);
            case SCANNER -> {
                engine.policeUseScanner(locationId);
                GameState state = engine.getState();
                int xpDelta = state.getLastXpDelta(RoleType.POLICE);
                String scannerMessage = state.isLastScannerFoundKiller()
                        ? "📡 Killer rilevato nell'area! +" + xpDelta + " XP"
                        : "📡 Nessun killer rilevato nell'area. +" + xpDelta + " XP";
                showTemporaryFeedback(scannerMessage, Duration.seconds(5.5));
                scheduleScannerClear();
            }
            default -> throw new IllegalStateException("Scegli prima un'azione dal pannello comandi.");
        }
    }

    @FXML
    private void onUseClue() {
        runGuardedAction(() -> engine.policeUseClue());
    }

    @FXML
    private void onSelectMove() {
        if (!ensureGameOngoing()) return;
        pendingAction = PendingAction.MOVE;
        boolean isKiller = engine.getState().getHumanRole() == RoleType.KILLER;
        statusLabel.setText(isKiller
                ? "Seleziona sulla mappa una casella: puoi muoverti di uno o due passi"
                : "Seleziona sulla mappa una casella collegata alla tua per spostarti");
    }

    @FXML
    private void onSelectFakeClue() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().getKillerFakeCluesRemaining() <= 0) {
            statusLabel.setText("Non hai più indizi falsi disponibili.");
            return;
        }
        pendingAction = PendingAction.FAKE_CLUE;
        statusLabel.setText("Seleziona una casella (diversa dalla tua) dove lasciare l'indizio falso");
    }

    @FXML
    private void onSelectArrest() {
        if (!ensureGameOngoing()) return;
        pendingAction = PendingAction.ARREST;
        statusLabel.setText("Seleziona la casella su cui tentare l'arresto (entro 3 caselle dalla tua posizione)");
    }

    @FXML
    private void onUseSmokeBomb() {
        runGuardedAction(() -> engine.killerUseSmokeBomb());
    }

    @FXML
    private void onSelectTrapKit() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().getKillerTrapKitsRemaining() <= 0) {
            statusLabel.setText("Non hai più Trap Kit disponibili.");
            return;
        }
        pendingAction = PendingAction.TRAP_KIT;
        statusLabel.setText("Seleziona la casella dove piazzare la Trap Zone");
    }

    @FXML
    private void onSelectShortcutMap() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().isKillerShortcutMapUsed()) {
            statusLabel.setText("Hai già usato la Shortcut Map in questo match.");
            return;
        }
        pendingAction = PendingAction.SHORTCUT_MOVE;
        statusLabel.setText("Shortcut Map: seleziona liberamente la destinazione, ignorando i vincoli di movimento");
    }

    @FXML
    private void onSelectRoadblock() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().getPoliceRoadblocksRemaining() <= 0) {
            statusLabel.setText("Non hai più Roadblock disponibili.");
            return;
        }
        pendingAction = PendingAction.ROADBLOCK;
        statusLabel.setText("Seleziona il nodo da bloccare per il prossimo turno del Killer");
    }

    @FXML
    private void onSelectCheckpoint() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().getPoliceCheckpointTokensRemaining() <= 0) {
            statusLabel.setText("Non hai più Checkpoint Token disponibili.");
            return;
        }
        pendingAction = PendingAction.CHECKPOINT;
        statusLabel.setText("Seleziona una casella collegata alla tua posizione per bloccarne il collegamento");
    }

    @FXML
    private void onUseScanner() {
        if (!ensureGameOngoing()) return;
        if (engine.getState().getPoliceScannerRemaining() <= 0) {
            statusLabel.setText("Non hai più letture Scanner disponibili in questo match.");
            return;
        }
        pendingAction = PendingAction.SCANNER;
        statusLabel.setText("Seleziona il centro dell'area su cui eseguire la lettura Scanner");
    }

    @FXML
    private void onSaveGame() {
        try {
            storage.save(engine.getState(), Path.of("Persistence.json"));
            statusLabel.setText("Partita salvata in Persistence.json");
        } catch (IOException e) {
            statusLabel.setText("Errore durante il salvataggio.");
        }
    }

    @FXML
    private void onReturnToMenu() {
        try {
            NavigationService.LoadedScreen<Object> screen =
                    NavigationService.load(getClass(), "/fxml/roleselect.fxml");
            NavigationService.show(statusLabel, screen.root);
        } catch (IOException e) {
            statusLabel.setText("Impossibile tornare al menu.");
        }
    }

    // Esegue un'azione che non richiede la selezione di una casella sulla mappa
    // (es. usare un indizio o uno Smoke Bomb): evita di ripetere in ogni handler
    // il controllo "partita finita?" e la gestione dell'eccezione.
    private void runGuardedAction(Runnable action) {
        if (!ensureGameOngoing()) return;
        try {
            action.run();
        } catch (IllegalStateException e) {
            statusLabel.setText(e.getMessage());
            return;
        }
        resetPendingAction();
        refreshView();
    }

    private boolean ensureGameOngoing() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return false;
        }
        return true;
    }

    private void showMatchResultScreen(RoleType winner, String endReason) {
        try {
            NavigationService.LoadedScreen<MatchResultController> screen =
                    NavigationService.load(getClass(), "/fxml/matchresult.fxml");
            screen.controller.init(campaign, winner, endReason);
            NavigationService.show(statusLabel, screen.root);
        } catch (IOException e) {
            statusLabel.setText("Impossibile mostrare la schermata di fine match.");
        }
    }

    private void resetPendingAction() {
        pendingAction = PendingAction.NONE;
        mapController.clearSelection();
    }

    private int lastXp(RoleType role) {
        return engine.getState().getLastXpDelta(role);
    }

    private void showTemporaryFeedback(String message, Duration duration) {
        temporaryInfoMessage = message;
        restartTimer(duration, () -> {
            temporaryInfoMessage = null;
            engine.getState().clearLastXpDelta();
            refreshView();
        });
    }

    private void scheduleScannerClear() {
        restartTimer(Duration.seconds(5.0), () -> {
            engine.getState().setLastScannerResult(null, false);
            temporaryInfoMessage = null;
            engine.getState().clearLastXpDelta();
            refreshView();
        });
    }

    // fattorizza lo start/stop del PauseTransition, comune a showTemporaryFeedback e scheduleScannerClear
    private void restartTimer(Duration duration, Runnable onFinished) {
        if (temporaryMessageTimer != null) {
            temporaryMessageTimer.stop();
        }
        temporaryMessageTimer = new PauseTransition(duration);
        temporaryMessageTimer.setOnFinished(event -> onFinished.run());
        temporaryMessageTimer.play();
    }

    private void refreshView() {
        GameState state = engine.getState();

        // se siamo dentro una campagna e il match è appena finito: registra il risultato
        // (con relativi XP) e passa subito alla schermata con l'immagine del vincitore
        if (state.isFinished() && campaign != null && !matchResultRecorded) {
            matchResultRecorded = true;
            RoleType winner = state.getWinner();
            String endReason = state.getEndReason();
            campaign.recordMatchResult(winner);
            showMatchResultScreen(winner, endReason);
            return;
        }

        refreshMap(state);
        actionPanel.refresh(state);
        updateStatusLabel(state);
        updateInventoryLabel(state);
    }

    private void refreshMap(GameState state) {
        mapController.clearAllStates();
        mapController.resetInteractable();

        state.getEliminatedHomeCandidates().forEach(mapController::markEliminated);
        state.getFailedArrestLocations().forEach(mapController::markSearched);
        for (Clue clue : state.getFakeClues()) {
            mapController.markFakeClue(clue.getLocationId());
        }

        Turn phase = state.getPhase();
        if (phase == Turn.AWAITING_HOME_CHOICE || phase == Turn.AWAITING_MURDER_LOCATION_CHOICE) {
            engine.getBoard().all().forEach(loc -> mapController.setInteractable(loc.getId(), true));
        }

        boolean isPoliceHumanTurn = phase == Turn.AWAITING_POLICE_ACTION
                && state.getHumanRole() == RoleType.POLICE;
        if (isPoliceHumanTurn) {
            state.getFailedArrestLocations().forEach(id -> mapController.setInteractable(id, false));
        }

        // a partita finita (es. partita singola senza campagna): mappa completamente non cliccabile
        if (state.isFinished()) {
            engine.getBoard().all().forEach(loc -> mapController.setInteractable(loc.getId(), false));
        }

        boolean revealKillerSecrets = state.getHumanRole() == RoleType.KILLER || state.isFinished();
        if (revealKillerSecrets && state.isHomeChosen()) {
            mapController.markHome(state.getKillerHomeLocationId());
        }
        if (revealKillerSecrets && state.getKiller().getCurrentLocationId() != null) {
            mapController.markKiller(state.getKiller().getCurrentLocationId());
        }

        if (state.getPolice().getCurrentLocationId() != null) {
            mapController.markPolice(state.getPolice().getCurrentLocationId());
        }
    }

    // mostra info extra solo quando serve (es. esito dell'ultimo Scanner): niente doppioni con le tessere
    private void updateInventoryLabel(GameState state) {
        if (state.isFinished()) {
            setVisibleAndManaged(inventoryLabel, false);
            return;
        }
        if (temporaryInfoMessage != null) {
            setVisibleAndManaged(inventoryLabel, true);
            inventoryLabel.setText(temporaryInfoMessage);
            return;
        }
        boolean showScannerInfo = state.getHumanRole() == RoleType.POLICE && state.getLastScannerCenterId() != null;
        setVisibleAndManaged(inventoryLabel, showScannerInfo);
        if (showScannerInfo) {
            inventoryLabel.setText(state.isLastScannerFoundKiller()
                    ? "📡 Ultimo Scanner: Killer rilevato nell'area!"
                    : "📡 Ultimo Scanner: nessuna traccia del Killer");
        }
    }

    private void updateStatusLabel(GameState state) {
        String campaignInfo = campaign == null
                ? ""
                : String.format("Match %d/3 (Killer %d - %d Poliziotto)\n",
                        campaign.getCurrentMatchNumber(), campaign.getKillerWins(), campaign.getPoliceWins());

        if (state.isFinished()) {
            String vincitore = state.getWinner() == RoleType.KILLER ? "il Killer" : "il Poliziotto";
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.\n"
                    + campaignInfo + "Vince " + vincitore + ". " + state.getEndReason());
            return;
        }
        if (state.getPhase() == Turn.AWAITING_HOME_CHOICE) {
            statusLabel.setText(campaignInfo
                    + "Scegli sulla mappa il tuo nascondiglio: il punto in cui dovrai rientrare per vincere.\n\n"
                    + "(Nessun comando disponibile in questa fase: clicca direttamente su un nodo della mappa.)");
            return;
        }
        if (state.getPhase() == Turn.AWAITING_MURDER_LOCATION_CHOICE) {
            statusLabel.setText(campaignInfo
                    + "Scegli sulla mappa il luogo del primo omicidio (diverso dal nascondiglio).\n\n"
                    + "(Nessun comando disponibile in questa fase: clicca direttamente su un nodo della mappa.)");
            return;
        }

        String ruolo = state.getHumanRole() == RoleType.KILLER ? "Killer" : "Poliziotto";
        statusLabel.setText(String.format("%sTurno %d/%d — sei il %s",
                campaignInfo, state.getRoundsElapsed() + 1, state.getMaxRounds(), ruolo));
    }

    private void setVisibleAndManaged(Label label, boolean value) {
        label.setVisible(value);
        label.setManaged(value);
    }
}