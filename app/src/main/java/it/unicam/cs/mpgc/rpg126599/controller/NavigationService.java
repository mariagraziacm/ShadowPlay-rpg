package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Responsabilità unica: caricare un FXML e mostrarlo nella finestra corrente.
 *
 * Prima questo blocco era copiato in 5 controller diversi. Centralizzarlo qui
 * evita la duplicazione, ma soprattutto permette di tenere in UN SOLO posto
 * la logica di dimensionamento che nella versione funzionante del progetto
 * (prima del refactoring) era: NON richiedere a JavaFX di ricalcolare la
 * dimensione della scena (sizeToScene() calcolato troppo presto è ciò che
 * causava le finestre "schiacciate"), ma mantenere la dimensione attuale
 * della finestra, con un default sensato solo per la primissima apertura.
 */
public final class NavigationService {

    private static final String APP_TITLE = "SHADOW PLAY";
    private static final double DEFAULT_WIDTH = 1024;
    private static final double DEFAULT_HEIGHT = 741;

    private NavigationService() {
    }

    /** Risultato del caricamento: la radice della scena e il suo controller, già pronti per essere inizializzati. */
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

    /**
     * Cambio scena "normale": mantiene la larghezza/altezza che la finestra ha
     * già in quel momento (così passare da una schermata all'altra non la fa
     * mai saltare a una dimensione diversa), usando un default 1024x741 solo
     * se la finestra non ha ancora una dimensione valida (praticamente mai,
     * tranne al primissimo avvio).
     */
    public static void show(Node ownerNode, Parent root) {
        Stage stage = (Stage) ownerNode.getScene().getWindow();
        double width = stage.getWidth() > 0 ? stage.getWidth() : DEFAULT_WIDTH;
        double height = stage.getHeight() > 0 ? stage.getHeight() : DEFAULT_HEIGHT;
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(APP_TITLE);
    }

    /**
     * Cambio scena verso la schermata di gioco: in più rispetto a show(),
     * garantisce che la finestra non sia mai più piccola di 1100x650 e che si
     * apra almeno a 1280x720 la prima volta che si entra in partita (dove la
     * mappa e il pannello comandi hanno bisogno di spazio reale).
     */
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