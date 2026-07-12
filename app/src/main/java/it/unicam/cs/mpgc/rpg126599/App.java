package it.unicam.cs.mpgc.rpg126599;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/roleselect.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("SHADOW PLAY — inzia a giocaare!");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
