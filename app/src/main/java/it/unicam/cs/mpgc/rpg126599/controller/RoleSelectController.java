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
import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.core.GameEngine;
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
        Board board = Board.loadFromResource("/maps/map.json");
        GameEngine engine = GameEngine.newGame(board, RoleType.KILLER);
        openGameScreen(engine);
    }

    @FXML
    private void onChoosePolice() {
        Board board = Board.loadFromResource("/maps/map.json");
        GameEngine engine = GameEngine.newGame(board, RoleType.POLICE);
        openGameScreen(engine);
    }

    @FXML
    private void onLoadGame() {
        try {
            Board board = Board.loadFromResource("/maps/map.json");
            GameState savedState = storage.load(Path.of("save.json"));
            GameEngine engine = GameEngine.resume(board, savedState);
            openGameScreen(engine);
        } catch (IOException e) {
            messageLabel.setText("Nessun salvataggio valido trovato (save.json).");
        }
    }

    private void openGameScreen(GameEngine engine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_view.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.init(engine);

            Stage stage = (Stage) killerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1040, 900));
            stage.setTitle("Whitechapel Lite");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata di gioco", e);
        }
    }
}
