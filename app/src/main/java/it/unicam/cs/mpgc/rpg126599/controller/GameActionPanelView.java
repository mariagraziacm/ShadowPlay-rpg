package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

/**
 * Responsabilità unica: riflettere lo stato di gioco (GameState) sui controlli
 * del pannello comandi/inventario.
 *
 * Prima questa logica — circa 90 righe fatte quasi solo di setVisible/setManaged
 * /setText/setDisable ripetuti — viveva dentro GameController, che nello stesso
 * momento doveva occuparsi anche di leggere l'input, navigare tra le schermate
 * e gestire i messaggi temporanei: troppe ragioni per cambiare in un'unica
 * classe. Qui diventa una classe a parte, e in più testabile senza aprire una
 * finestra JavaFX vera (basta passare dei Button/Label costruiti a mano).
 *
 * I riferimenti @FXML restano dentro GameController (è l'unico modo in cui
 * JavaFX può iniettarli): questa classe li riceve già pronti nel costruttore.
 */
public class GameActionPanelView {

    private final Label commandsHeaderLabel;
    private final Label inventoryHeaderLabel;
    private final Button useClueButton;
    private final Button moveButton;
    private final Button fakeClueButton;
    private final Button arrestButton;
    private final HBox killerInventoryRow;
    private final HBox policeInventoryRow;
    private final Button smokeBombButton;
    private final Button trapKitButton;
    private final Button shortcutMapButton;
    private final Button roadblockButton;
    private final Button checkpointButton;
    private final Button scannerButton;

    public GameActionPanelView(Label commandsHeaderLabel, Label inventoryHeaderLabel,
                                Button useClueButton, Button moveButton, Button fakeClueButton, Button arrestButton,
                                HBox killerInventoryRow, HBox policeInventoryRow,
                                Button smokeBombButton, Button trapKitButton, Button shortcutMapButton,
                                Button roadblockButton, Button checkpointButton, Button scannerButton) {
        this.commandsHeaderLabel = commandsHeaderLabel;
        this.inventoryHeaderLabel = inventoryHeaderLabel;
        this.useClueButton = useClueButton;
        this.moveButton = moveButton;
        this.fakeClueButton = fakeClueButton;
        this.arrestButton = arrestButton;
        this.killerInventoryRow = killerInventoryRow;
        this.policeInventoryRow = policeInventoryRow;
        this.smokeBombButton = smokeBombButton;
        this.trapKitButton = trapKitButton;
        this.shortcutMapButton = shortcutMapButton;
        this.roadblockButton = roadblockButton;
        this.checkpointButton = checkpointButton;
        this.scannerButton = scannerButton;
    }

    public void refresh(GameState state) {
        if (state.isFinished()) {
            hideAll();
            return;
        }

        boolean isPoliceTurn = state.getPhase() == Turn.AWAITING_POLICE_ACTION
                && state.getHumanRole() == RoleType.POLICE;
        boolean isKillerTurn = state.getPhase() == Turn.AWAITING_KILLER_ACTION
                && state.getHumanRole() == RoleType.KILLER;
        boolean anyCommandsAvailable = isPoliceTurn || isKillerTurn;

        setVisibleAndManaged(commandsHeaderLabel, anyCommandsAvailable);
        setVisibleAndManaged(inventoryHeaderLabel, anyCommandsAvailable);

        setVisibleAndManaged(useClueButton, isPoliceTurn);
        useClueButton.setDisable(state.getPoliceCluesRemaining() <= 0);
        useClueButton.setText("Usa indizio (" + state.getPoliceCluesRemaining() + ")");

        setVisibleAndManaged(arrestButton, isPoliceTurn);
        setVisibleAndManaged(moveButton, isPoliceTurn || isKillerTurn);

        setVisibleAndManaged(fakeClueButton, isKillerTurn);
        fakeClueButton.setDisable(state.getKillerFakeCluesRemaining() <= 0);
        fakeClueButton.setText("Indizio falso (" + state.getKillerFakeCluesRemaining() + ")");

        setVisibleAndManaged(killerInventoryRow, isKillerTurn);
        refreshCountedButton(smokeBombButton, "💨 Smoke Bomb", state.getKillerSmokeBombsRemaining());
        refreshCountedButton(trapKitButton, "🪤 Trap Kit", state.getKillerTrapKitsRemaining());
        shortcutMapButton.setDisable(state.isKillerShortcutMapUsed());
        shortcutMapButton.setText(state.isKillerShortcutMapUsed()
                ? "🗺️ Shortcut Map\n(usata)"
                : "🗺️ Shortcut Map\n(1)");

        setVisibleAndManaged(policeInventoryRow, isPoliceTurn);
        refreshCountedButton(roadblockButton, "🚧 Roadblock", state.getPoliceRoadblocksRemaining());
        refreshCountedButton(checkpointButton, "⛔ Checkpoint", state.getPoliceCheckpointTokensRemaining());
        refreshCountedButton(scannerButton, "📡 Scanner", state.getPoliceScannerRemaining());
    }

    // fattorizza lo schema comune a smoke bomb / trap kit / roadblock / checkpoint / scanner:
    // "disabilita se esaurito, mostra l'etichetta con il conteggio residuo"
    private void refreshCountedButton(Button button, String label, int remaining) {
        button.setDisable(remaining <= 0);
        button.setText(label + "\n(" + remaining + ")");
    }

    private void hideAll() {
        setVisibleAndManaged(commandsHeaderLabel, false);
        setVisibleAndManaged(inventoryHeaderLabel, false);
        setVisibleAndManaged(useClueButton, false);
        setVisibleAndManaged(arrestButton, false);
        setVisibleAndManaged(moveButton, false);
        setVisibleAndManaged(fakeClueButton, false);
        setVisibleAndManaged(killerInventoryRow, false);
        setVisibleAndManaged(policeInventoryRow, false);
    }

    private void setVisibleAndManaged(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }
}