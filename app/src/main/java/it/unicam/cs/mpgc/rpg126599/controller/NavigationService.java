package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public final class NavigationService {

    private static final String APP_TITLE = "SHADOW PLAY";
    private static final double DEFAULT_WIDTH = 1024;
    private static final double DEFAULT_HEIGHT = 741;

    private NavigationService() {
    }

    
    public static final class LoadedScreen<T> {
        public final Parent root;
        public final T controller;

        private LoadedScreen(Parent root, T controller) {
            this.root = root;
            this.controller = controller;
        }
    }

    public static <T> LoadedScreen<T> load(Class<?> caller, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(caller.getResource(fxmlPath));
        Parent root = loader.load();
        T controller = loader.getController();
        return new LoadedScreen<>(root, controller);
    }


    public static void show(Node ownerNode, Parent root) {
        Stage stage = (Stage) ownerNode.getScene().getWindow();
        double width = stage.getWidth() > 0 ? stage.getWidth() : DEFAULT_WIDTH;
        double height = stage.getHeight() > 0 ? stage.getHeight() : DEFAULT_HEIGHT;
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(APP_TITLE);
    }

  
    public static void showGameScreen(Node ownerNode, Parent root) {
        Stage stage = (Stage) ownerNode.getScene().getWindow();
        double width = Math.max(1320, stage.getWidth() > 0 ? stage.getWidth() : 1320);
        double height = Math.max(775, stage.getHeight() > 0 ? stage.getHeight() : 775);
        stage.setMinWidth(1320);
        stage.setMinHeight(775);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(APP_TITLE);
    }
}