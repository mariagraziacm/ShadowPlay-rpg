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
        primaryStage.setMinWidth(1145);
        primaryStage.setMinHeight(705);
        primaryStage.setWidth(1325);
        primaryStage.setHeight(775);
        primaryStage.setScene(new Scene(root, 1325, 775));
        primaryStage.setTitle("SHADOW PLAY — inzia a giocaare!");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
