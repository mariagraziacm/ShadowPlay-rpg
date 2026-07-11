package it.unicam.cs.mpgc.rpg126599.controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectController {

    @FXML
    private Button killerButton;

    @FXML
    private Button policeButton;

    @FXML
    private void onChooseKiller() {
        startGame(RoleType.KILLER);
    }

    @FXML
    private void onChoosePolice() {
        startGame(RoleType.POLICE);
    }

    private void startGame(RoleType humanRole) {
        Board board = Board.loadFromResource("/maps/map.json");
        GameEngine engine = GameEngine.newGame(board, humanRole, 30);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/map.fxml"));
            Parent root = loader.load();

            MapController mapController = loader.getController();
            mapController.init(engine);

            Stage stage = (Stage) killerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 760, 560));
            stage.setTitle("Whitechapel Lite");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata di gioco", e);
        }
    }
}
