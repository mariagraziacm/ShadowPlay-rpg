package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.file.Path;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.core.GameEngine;
import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.BoardLoader;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.persistence.GameJsonStorage;

public class RoleSelectController {

    @FXML
    private Button killerButton;

    @FXML
    private Label messageLabel;

    private final GameJsonStorage storage = new GameJsonStorage();

    @FXML
    private void onChooseKiller() {
        Board board = BoardLoader.loadFromResource("/rounds/maps.json");
        CampaignManager campaign = CampaignManager.start(board, RoleType.KILLER);
        openCharacterDescription(campaign);
    }

    @FXML
    private void onChoosePolice() {
        Board board = BoardLoader.loadFromResource("/rounds/maps.json");
        CampaignManager campaign = CampaignManager.start(board, RoleType.POLICE);
        openCharacterDescription(campaign);
    }

    @FXML
    private void onLoadGame() {
        try {
            Board board = BoardLoader.loadFromResource("/rounds/maps.json");
            GameState savedState = storage.load(Path.of("Persistence.json"));
            if (savedState.isCampaignInProgress()) {
                CampaignManager campaign = CampaignManager.resume(board, savedState);
                openGameScreen(controller -> controller.init(campaign));
            } else {
                GameEngine engine = GameEngine.resume(board, savedState);
                openGameScreen(controller -> controller.init(engine));
            }
        } catch (IOException e) {
            messageLabel.setText("Nessun salvataggio valido trovato (Persistence.json).");
        }
    }

    private void openCharacterDescription(CampaignManager campaign) {
        try {
            String fxmlPath = campaign.getHumanRole() == RoleType.KILLER
                    ? "/fxml/killer.fxml"
                    : "/fxml/police.fxml";

            NavigationService.LoadedScreen<CharacterDescriptionController> screen =
                    NavigationService.load(getClass(), fxmlPath);
            screen.controller.init(campaign);
            NavigationService.show(killerButton, screen.root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata del personaggio", e);
        }
    }

    private void openGameScreen(java.util.function.Consumer<GameController> initializer) {
        try {
            NavigationService.LoadedScreen<GameController> screen =
                    NavigationService.load(getClass(), "/fxml/gameview.fxml");
            initializer.accept(screen.controller);
            NavigationService.showGameScreen(killerButton, screen.root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata di gioco", e);
        }
    }
}