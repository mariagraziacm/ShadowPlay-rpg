package it.unicam.cs.mpgc.rpg126599;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/role_select.fxml"));
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setTitle("Whitechapel Lite — scegli il tuo ruolo");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
