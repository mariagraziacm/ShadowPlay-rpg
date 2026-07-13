package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Path;


import it.unicam.cs.mpgc.rpg126599.core.GameEngine;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.persistence.GameJsonStorage;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

// gestisce schermata di gioco, interazioni con la mappa e bottoni, la schermata si aggiorna in base a turno, ruolo e scelte
public class GameController {

    private enum PendingAction {
        NONE, MOVE, FAKE_CLUE, ARREST
    }

    @FXML
    private MapController mapController;

    @FXML
    private Label statusLabel;
    @FXML
    private Button useClueButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button fakeClueButton;
    @FXML
    private Button arrestButton;

    private final GameJsonStorage storage = new GameJsonStorage(); 
    private GameEngine engine;
    private PendingAction pendingAction = PendingAction.NONE;

    public void init(GameEngine engine) {
        this.engine = engine;
        mapController.setOnNodeClicked(this::onNodeClicked);
        refreshView();
    }

    private void onNodeClicked(String locationId) {
        if (engine.getState().isFinished()) {
            return;
        }
        Turn phase = engine.getState().getPhase(); // controlla turno e ruolo
        RoleType humanRole = engine.getState().getHumanRole();
// comportamenti avviati al click sui nodi in base a turno e ruolo
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
// azioni per killer con click su un nodo
    private void handleKillerNodeClick(String locationId) {
        switch (pendingAction) {
            case MOVE -> engine.killerMove(locationId);
            case FAKE_CLUE -> engine.killerLeaveFakeClue(locationId);
            default -> throw new IllegalStateException("Scegli prima 'Sposta' oppure 'Lascia indizio falso'.");
        }
    }
// azioni per poliziotto con click su un nodo
    private void handlePoliceNodeClick(String locationId) {
        switch (pendingAction) {
            case MOVE -> engine.policeMoveTo(locationId);
            case ARREST -> engine.policeAttemptArrest(locationId);
            default -> throw new IllegalStateException("Scegli prima 'Sposta' oppure 'Tenta l'arresto'.");
        }
    }
// poliziotto usa indizio
    @FXML
    private void onUseClue() {
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
        pendingAction = PendingAction.MOVE;
        boolean isKiller = engine.getState().getHumanRole() == RoleType.KILLER;
        statusLabel.setText(isKiller
                ? "Seleziona sulla mappa una casella: puoi muoverti di uno o due passi"
                : "Seleziona sulla mappa una casella collegata alla tua per spostarti");
    }
// killer lascia indizio falso
    @FXML
    private void onSelectFakeClue() {
        if (engine.getState().getKillerFakeCluesRemaining() <= 0) {
            statusLabel.setText("Non hai più indizi falsi disponibili.");
            return;
        }
        pendingAction = PendingAction.FAKE_CLUE;
        statusLabel.setText("Seleziona una casella (diversa dalla tua) dove lasciare l'indizio falso per ingannare il poliziotto");
    }
// poliziotto tentaa arresto
    @FXML
    private void onSelectArrest() {
        pendingAction = PendingAction.ARREST;
        statusLabel.setText("Seleziona la casella su cui tentare l'arresto");
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

    private void resetPendingAction() {
        pendingAction = PendingAction.NONE;
        mapController.clearSelection();
    }

    private void refreshView() {
        mapController.clearAllStates();
        mapController.resetInteractable();

        var state = engine.getState();
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
// mostra nascondiglio e posizione killer solo se l'utente è il killer o se la partita è finita
        boolean revealKillerSecrets = state.getHumanRole() == RoleType.KILLER || state.isFinished();
        if (revealKillerSecrets && state.isHomeChosen()) {
            mapController.markHome(state.getKillerHomeLocationId());
        }
        if (revealKillerSecrets && state.getKiller().getCurrentLocationId() != null) {
            mapController.markKiller(state.getKiller().getCurrentLocationId());
        }
      // poliziotto sempre visibile a tutti  
        if (state.getPolice().getCurrentLocationId() != null) {
            mapController.markPolice(state.getPolice().getCurrentLocationId());
        }

        updateActionButtons();
        updateStatusLabel();
    }

    private void updateActionButtons() {
        Turn phase = engine.getState().getPhase();
        RoleType humanRole = engine.getState().getHumanRole();

        boolean isPoliceHumanTurn = phase == Turn.AWAITING_POLICE_ACTION && humanRole == RoleType.POLICE;
        boolean isKillerHumanTurn = phase == Turn.AWAITING_KILLER_ACTION && humanRole == RoleType.KILLER;

        useClueButton.setVisible(isPoliceHumanTurn);
        useClueButton.setManaged(isPoliceHumanTurn);
        useClueButton.setDisable(engine.getState().getPoliceCluesRemaining() <= 0);

        arrestButton.setVisible(isPoliceHumanTurn);
        arrestButton.setManaged(isPoliceHumanTurn);

        moveButton.setVisible(isPoliceHumanTurn || isKillerHumanTurn);
        moveButton.setManaged(isPoliceHumanTurn || isKillerHumanTurn);

        fakeClueButton.setVisible(isKillerHumanTurn);
        fakeClueButton.setManaged(isKillerHumanTurn);
        fakeClueButton.setDisable(engine.getState().getKillerFakeCluesRemaining() <= 0);
    }
// aggiorna label in base a turno, ruolo e scelte
    private void updateStatusLabel() {
        var state = engine.getState();

        if (state.isFinished()) {
            String vincitore = state.getWinner() == RoleType.KILLER ? "il Killer" : "il Poliziotto";
            statusLabel.setText("Partita finita: vince " + vincitore + ". " + state.getEndReason());
            return;
        }
        if (state.getPhase() == Turn.AWAITING_HOME_CHOICE) {
            statusLabel.setText("Scegli sulla mappa il tuo nascondiglio: il punto in cui dovrai rientrare per vincere.");
            return;
        }
        if (state.getPhase() == Turn.AWAITING_MURDER_LOCATION_CHOICE) {
            statusLabel.setText("Scegli sulla mappa il luogo del primo omicidio: la tua posizione di partenza (ATTENZIONE: deve essere diversa dal nascondiglio)");
            return;
        }

        String ruolo = state.getHumanRole() == RoleType.KILLER ? "Killer" : "Poliziotto";

        String indiziInfo = state.getHumanRole() == RoleType.KILLER
                ? "indizi falsi rimasti: " + state.getKillerFakeCluesRemaining()
                : "indizi rimasti: " + state.getPoliceCluesRemaining();

        statusLabel.setText(String.format("Turno %d/%d — sei il %s — %s",
                state.getRoundsElapsed() + 1, state.getMaxRounds(), ruolo, indiziInfo));
    }
}