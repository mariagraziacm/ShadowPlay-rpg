package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.nio.file.Path;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.core.GameEngine;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;
import it.unicam.cs.mpgc.rpg126599.model.Turn;
import it.unicam.cs.mpgc.rpg126599.persistence.GameJsonStorage;

// gestisce schermata di gioco, interazioni con la mappa e bottoni, la schermata si aggiorna in base a turno, ruolo e scelte
public class GameController {

    private enum PendingAction {
        NONE, MOVE, FAKE_CLUE, ARREST, TRAP_KIT, SHORTCUT_MOVE, ROADBLOCK, CHECKPOINT, SCANNER
    }

    @FXML
    private MapController mapController;

    @FXML
    private Label statusLabel;
    @FXML
    private Label inventoryLabel;
    @FXML
    private Label commandsHeaderLabel;
    @FXML
    private Label inventoryHeaderLabel;
    @FXML
    private Button useClueButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button fakeClueButton;
    @FXML
    private Button arrestButton;

    @FXML private HBox killerInventoryRow;
    @FXML private HBox policeInventoryRow;
    @FXML private Button smokeBombButton;
    @FXML private Button trapKitButton;
    @FXML private Button shortcutMapButton;
    @FXML private Button roadblockButton;
    @FXML private Button checkpointButton;
    @FXML private Button scannerButton;

    private final GameJsonStorage storage = new GameJsonStorage();
    private GameEngine engine;
    private CampaignManager campaign;
    private boolean matchResultRecorded;
    private PendingAction pendingAction = PendingAction.NONE;
    private String temporaryInfoMessage;
    private PauseTransition temporaryMessageTimer;

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
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
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
                showTemporaryFeedback("⭐ +" + calculateMoveXp(RoleType.KILLER) + " XP", Duration.seconds(4.5));
            }
            case FAKE_CLUE -> engine.killerLeaveFakeClue(locationId);
            case TRAP_KIT -> engine.killerPlaceTrap(locationId);
            case SHORTCUT_MOVE -> {
                engine.killerUseShortcutMap(locationId);
                showTemporaryFeedback("⭐ +" + calculateMoveXp(RoleType.KILLER) + " XP", Duration.seconds(4.5));
            }
            default -> throw new IllegalStateException("Scegli prima un'azione dal pannello comandi.");
        }
    }

    private void handlePoliceNodeClick(String locationId) {
        switch (pendingAction) {
            case MOVE -> {
                engine.policeMoveTo(locationId);
                showTemporaryFeedback("⭐ +" + calculateMoveXp(RoleType.POLICE) + " XP", Duration.seconds(4.5));
            }
            case ARREST -> engine.policeAttemptArrest(locationId);
            case ROADBLOCK -> engine.policePlaceRoadblock(locationId);
            case CHECKPOINT -> engine.policeUseCheckpoint(engine.getState().getPolice().getCurrentLocationId(), locationId);
            case SCANNER -> {
                engine.policeUseScanner(locationId);
                int xpDelta = engine.getState().getLastXpDelta(RoleType.POLICE);
                String scannerMessage = engine.getState().isLastScannerFoundKiller()
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
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        try {
            engine.policeUseClue();
        } catch (IllegalStateException e) {
            statusLabel.setText(e.getMessage());
            return;
        }
        refreshView();
    }

    @FXML
    private void onSelectMove() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        pendingAction = PendingAction.MOVE;
        boolean isKiller = engine.getState().getHumanRole() == RoleType.KILLER;
        statusLabel.setText(isKiller
                ? "Seleziona sulla mappa una casella: puoi muoverti di uno o due passi"
                : "Seleziona sulla mappa una casella collegata alla tua per spostarti");
    }

    @FXML
    private void onSelectFakeClue() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        if (engine.getState().getKillerFakeCluesRemaining() <= 0) {
            statusLabel.setText("Non hai più indizi falsi disponibili.");
            return;
        }
        pendingAction = PendingAction.FAKE_CLUE;
        statusLabel.setText("Seleziona una casella (diversa dalla tua) dove lasciare l'indizio falso");
    }

@FXML
private void onSelectArrest() {
    if (engine.getState().isFinished()) {
        statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
        return;
    }
    pendingAction = PendingAction.ARREST;
    statusLabel.setText("Seleziona la casella su cui tentare l'arresto (entro 3 caselle dalla tua posizione)");
}

    @FXML
    private void onUseSmokeBomb() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        try {
            engine.killerUseSmokeBomb();
        } catch (IllegalStateException e) {
            statusLabel.setText(e.getMessage());
            return;
        }
        resetPendingAction();
        refreshView();
    }

    @FXML
    private void onSelectTrapKit() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        if (engine.getState().getKillerTrapKitsRemaining() <= 0) {
            statusLabel.setText("Non hai più Trap Kit disponibili.");
            return;
        }
        pendingAction = PendingAction.TRAP_KIT;
        statusLabel.setText("Seleziona la casella dove piazzare la Trap Zone");
    }

    @FXML
    private void onSelectShortcutMap() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        if (engine.getState().isKillerShortcutMapUsed()) {
            statusLabel.setText("Hai già usato la Shortcut Map in questo match.");
            return;
        }
        pendingAction = PendingAction.SHORTCUT_MOVE;
        statusLabel.setText("Shortcut Map: seleziona liberamente la destinazione, ignorando i vincoli di movimento");
    }

    @FXML
    private void onSelectRoadblock() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        if (engine.getState().getPoliceRoadblocksRemaining() <= 0) {
            statusLabel.setText("Non hai più Roadblock disponibili.");
            return;
        }
        pendingAction = PendingAction.ROADBLOCK;
        statusLabel.setText("Seleziona il nodo da bloccare per il prossimo turno del Killer");
    }

    @FXML
    private void onSelectCheckpoint() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
        if (engine.getState().getPoliceCheckpointTokensRemaining() <= 0) {
            statusLabel.setText("Non hai più Checkpoint Token disponibili.");
            return;
        }
        pendingAction = PendingAction.CHECKPOINT;
        statusLabel.setText("Seleziona una casella collegata alla tua posizione per bloccarne il collegamento");
    }

    @FXML
    private void onUseScanner() {
        if (engine.getState().isFinished()) {
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.");
            return;
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/roleselect.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY");
        } catch (IOException e) {
            statusLabel.setText("Impossibile tornare al menu.");
        }
    }

    private void showMatchResultScreen(RoleType winner, String endReason) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/matchresult.fxml"));
            Parent root = loader.load();
            MatchResultController controller = loader.getController();
            controller.init(campaign, winner, endReason);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY");
        } catch (IOException e) {
            statusLabel.setText("Impossibile mostrare la schermata di fine match.");
        }
    }

    private void resetPendingAction() {
        pendingAction = PendingAction.NONE;
        mapController.clearSelection();
    }

    private int calculateMoveXp(RoleType role) {
        var state = engine.getState();
        return state.getLastXpDelta(role);
    }

    private void showTemporaryFeedback(String message, Duration duration) {
        if (temporaryMessageTimer != null) {
            temporaryMessageTimer.stop();
        }
        temporaryInfoMessage = message;
        temporaryMessageTimer = new PauseTransition(duration);
        temporaryMessageTimer.setOnFinished(event -> {
            temporaryInfoMessage = null;
            engine.getState().clearLastXpDelta();
            refreshView();
        });
        temporaryMessageTimer.play();
    }

    private void scheduleScannerClear() {
        if (temporaryMessageTimer != null) {
            temporaryMessageTimer.stop();
        }
        temporaryMessageTimer = new PauseTransition(Duration.seconds(5.0));
        temporaryMessageTimer.setOnFinished(event -> {
            engine.getState().setLastScannerResult(null, false);
            temporaryInfoMessage = null;
            engine.getState().clearLastXpDelta();
            refreshView();
        });
        temporaryMessageTimer.play();
    }

    private void refreshView() {
        var state = engine.getState();

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

        updateActionButtons();
        updateStatusLabel();
        updateInventoryLabel();
    }

    private void updateActionButtons() {
        var state = engine.getState();

        if (state.isFinished()) {
            hideAllActionButtons();
            return;
        }

        Turn phase = state.getPhase();
        RoleType humanRole = state.getHumanRole();

        boolean isPoliceHumanTurn = phase == Turn.AWAITING_POLICE_ACTION && humanRole == RoleType.POLICE;
        boolean isKillerHumanTurn = phase == Turn.AWAITING_KILLER_ACTION && humanRole == RoleType.KILLER;

        boolean anyCommandsAvailable = isPoliceHumanTurn || isKillerHumanTurn;
        commandsHeaderLabel.setVisible(anyCommandsAvailable);
        commandsHeaderLabel.setManaged(anyCommandsAvailable);
        inventoryHeaderLabel.setVisible(anyCommandsAvailable);
        inventoryHeaderLabel.setManaged(anyCommandsAvailable);

        useClueButton.setVisible(isPoliceHumanTurn);
        useClueButton.setManaged(isPoliceHumanTurn);
        useClueButton.setDisable(state.getPoliceCluesRemaining() <= 0);
        useClueButton.setText("Usa indizio (" + state.getPoliceCluesRemaining() + ")");

        arrestButton.setVisible(isPoliceHumanTurn);
        arrestButton.setManaged(isPoliceHumanTurn);

        moveButton.setVisible(isPoliceHumanTurn || isKillerHumanTurn);
        moveButton.setManaged(isPoliceHumanTurn || isKillerHumanTurn);

        fakeClueButton.setVisible(isKillerHumanTurn);
        fakeClueButton.setManaged(isKillerHumanTurn);
        fakeClueButton.setDisable(state.getKillerFakeCluesRemaining() <= 0);
        fakeClueButton.setText("Indizio falso (" + state.getKillerFakeCluesRemaining() + ")");

        killerInventoryRow.setVisible(isKillerHumanTurn);
        killerInventoryRow.setManaged(isKillerHumanTurn);

        smokeBombButton.setDisable(state.getKillerSmokeBombsRemaining() <= 0);
        smokeBombButton.setText("💨 Smoke Bomb\n(" + state.getKillerSmokeBombsRemaining() + ")");

        trapKitButton.setDisable(state.getKillerTrapKitsRemaining() <= 0);
        trapKitButton.setText("🪤 Trap Kit\n(" + state.getKillerTrapKitsRemaining() + ")");

        shortcutMapButton.setDisable(state.isKillerShortcutMapUsed());
        shortcutMapButton.setText(state.isKillerShortcutMapUsed() ? "🗺️ Shortcut Map\n(usata)" : "🗺️ Shortcut Map\n(1)");

        policeInventoryRow.setVisible(isPoliceHumanTurn);
        policeInventoryRow.setManaged(isPoliceHumanTurn);

        roadblockButton.setDisable(state.getPoliceRoadblocksRemaining() <= 0);
        roadblockButton.setText("🚧 Roadblock\n(" + state.getPoliceRoadblocksRemaining() + ")");

        checkpointButton.setDisable(state.getPoliceCheckpointTokensRemaining() <= 0);
        checkpointButton.setText("⛔ Checkpoint\n(" + state.getPoliceCheckpointTokensRemaining() + ")");

        scannerButton.setDisable(state.getPoliceScannerRemaining() <= 0);
        scannerButton.setText("📡 Scanner\n(" + state.getPoliceScannerRemaining() + ")");
    }

    // a partita finita: nessun pulsante d'azione resta visibile/utilizzabile
    private void hideAllActionButtons() {
        commandsHeaderLabel.setVisible(false);
        commandsHeaderLabel.setManaged(false);
        inventoryHeaderLabel.setVisible(false);
        inventoryHeaderLabel.setManaged(false);

        useClueButton.setVisible(false);
        useClueButton.setManaged(false);
        arrestButton.setVisible(false);
        arrestButton.setManaged(false);
        moveButton.setVisible(false);
        moveButton.setManaged(false);
        fakeClueButton.setVisible(false);
        fakeClueButton.setManaged(false);
        killerInventoryRow.setVisible(false);
        killerInventoryRow.setManaged(false);
        policeInventoryRow.setVisible(false);
        policeInventoryRow.setManaged(false);
    }

    // mostra info extra solo quando serve (es. esito dell'ultimo Scanner): niente doppioni con le tessere
    private void updateInventoryLabel() {
        var state = engine.getState();

        if (state.isFinished()) {
            inventoryLabel.setVisible(false);
            inventoryLabel.setManaged(false);
            return;
        }

        if (temporaryInfoMessage != null) {
            inventoryLabel.setVisible(true);
            inventoryLabel.setManaged(true);
            inventoryLabel.setText(temporaryInfoMessage);
            return;
        }

        boolean showScannerInfo = state.getHumanRole() == RoleType.POLICE && state.getLastScannerCenterId() != null;
        inventoryLabel.setVisible(showScannerInfo);
        inventoryLabel.setManaged(showScannerInfo);
        if (showScannerInfo) {
            inventoryLabel.setText(state.isLastScannerFoundKiller()
                    ? "📡 Ultimo Scanner: Killer rilevato nell'area!"
                    : "📡 Ultimo Scanner: nessuna traccia del Killer");
        }
    }

    private void updateStatusLabel() {
        var state = engine.getState();

        String campaignInfo = "";
        if (campaign != null) {
            campaignInfo = String.format("Match %d/3 (Killer %d - %d Poliziotto)\n",
                    campaign.getCurrentMatchNumber(), campaign.getKillerWins(), campaign.getPoliceWins());
        }

        if (state.isFinished()) {
            String vincitore = state.getWinner() == RoleType.KILLER ? "il Killer" : "il Poliziotto";
            statusLabel.setText("AZIONE NON ESEGUIBILE — IL GIOCO È FINITO.\n"
                    + campaignInfo + "Vince " + vincitore + ". " + state.getEndReason());
            return;
        }
        if (state.getPhase() == Turn.AWAITING_HOME_CHOICE) {
            statusLabel.setText(campaignInfo + "Scegli sulla mappa il tuo nascondiglio: il punto in cui dovrai rientrare per vincere.\n\n(Nessun comando disponibile in questa fase: clicca direttamente su un nodo della mappa.)");
            return;
        }
        if (state.getPhase() == Turn.AWAITING_MURDER_LOCATION_CHOICE) {
            statusLabel.setText(campaignInfo + "Scegli sulla mappa il luogo del primo omicidio (diverso dal nascondiglio).\n\n(Nessun comando disponibile in questa fase: clicca direttamente su un nodo della mappa.)");
            return;
        }

        String ruolo = state.getHumanRole() == RoleType.KILLER ? "Killer" : "Poliziotto";
        statusLabel.setText(String.format("%sTurno %d/%d — sei il %s",
                campaignInfo, state.getRoundsElapsed() + 1, state.getMaxRounds(), ruolo));
    }
}