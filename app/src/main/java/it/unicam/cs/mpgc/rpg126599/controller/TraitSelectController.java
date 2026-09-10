package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class TraitSelectController {

    @FXML private Label titleLabel;
    @FXML private Button confirmButton;
    
    @FXML private ToggleButton traitButton1;
    @FXML private ToggleButton traitButton2;
    @FXML private ToggleGroup traitGroup;

    public void init() {
        // Imposta i testi o le descrizioni direttamente sui due bottoni grandi
        traitButton1.setText("Primo Tratto\n\nDescrizione o effetto...");
        traitButton2.setText("Secondo Tratto\n\nDescrizione o effetto...");
    }

    @FXML
    private void onConfirm() {
        ToggleButton selected = (ToggleButton) traitGroup.getSelectedToggle();
        if (selected != null) {
            System.out.println("Tratto selezionato: " + selected.getText());
            // Prosegui con l'avvio del match...
        } else {
            System.out.println("Nessun tratto selezionato!");
        }
    }
}