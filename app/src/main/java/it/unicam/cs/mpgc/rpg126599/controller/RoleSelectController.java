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

    // scegliendo un ruolo si avvia una nuova campagna 
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

    // il caricamento di un salvataggio resta una partita singola, fuori dalla campagna
    @FXML
    private void onLoadGame() {
        try {
            Board board = BoardLoader.loadFromResource("/rounds/maps.json");
            GameState savedState = storage.load(Path.of("Persistence.json"));
            if (savedState.isCampaignInProgress()) {
                CampaignManager campaign = CampaignManager.resume(board, savedState);
                openGameScreen(campaign);
            } else {
                GameEngine engine = GameEngine.resume(board, savedState);
                openGameScreen(engine);
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        CharacterDescriptionController controller = loader.getController();
        controller.init(campaign);

        Stage stage = (Stage) killerButton.getScene().getWindow();
        double width = stage.getWidth() > 0 ? stage.getWidth() : 1024;
        double height = stage.getHeight() > 0 ? stage.getHeight() : 741;
        stage.setScene(new Scene(root, width, height));
        stage.setTitle("SHADOW PLAY");
    } catch (IOException e) {
        throw new IllegalStateException("Impossibile aprire la schermata del personaggio", e);
    }
}

    private void openGameScreen(GameEngine engine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameview.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.init(engine);

            Stage stage = (Stage) killerButton.getScene().getWindow();
            double width = Math.max(1280, stage.getWidth() > 0 ? stage.getWidth() : 1320);
            double height = Math.max(720, stage.getHeight() > 0 ? stage.getHeight() : 732);
            stage.setMinWidth(1100);
            stage.setMinHeight(650);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setScene(new Scene(root, width, height));
            stage.setTitle("SHADOWPLAY");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata di gioco", e);
        }
    }

    private void openGameScreen(CampaignManager campaign) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameview.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.init(campaign);

            Stage stage = (Stage) killerButton.getScene().getWindow();
            double width = Math.max(1280, stage.getWidth() > 0 ? stage.getWidth() : 1280);
            double height = Math.max(720, stage.getHeight() > 0 ? stage.getHeight() : 720);
            stage.setMinWidth(1100);
            stage.setMinHeight(650);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setScene(new Scene(root, width, height));
            stage.setTitle("SHADOWPLAY");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata di gioco", e);
        }
    }
}