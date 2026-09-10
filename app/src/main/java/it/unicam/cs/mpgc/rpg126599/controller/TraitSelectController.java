package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;

public class TraitSelectController {

    @FXML private Label titleLabel;
    @FXML private Button confirmButton;
    @FXML private ToggleButton traitButton1;
    @FXML private ToggleButton traitButton2;
    @FXML private ToggleGroup traitGroup;

    private CampaignManager campaign;

    public void init(CampaignManager campaign) {
        this.campaign = campaign;

        // Disabilita il pulsante di conferma finché non viene selezionato un tratto
        confirmButton.setDisable(true);
        traitGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            confirmButton.setDisable(newVal == null);
        });

        // Utilizza getHumanRole() della classe CampaignManager
        if (campaign.getHumanRole() == RoleType.KILLER) {
            setupKillerTraits();
        } else {
            setupPoliceTraits();
        }
    }

    private void setupKillerTraits() {
        traitButton1.setText("MANIPOLATORE\n\nEnfatizza il depistaggio: i tuoi indizi falsi sono più efficaci nel confondere la Polizia.");
        traitButton2.setText("CALCOLATORE\n\nPremia il timing: agire nei momenti chiave della partita ti dà un vantaggio.");
    }

    private void setupPoliceTraits() {
        traitButton1.setText("CONTINUITÀ INVESTIGATIVA\n\nPremia la continuità investigativa: i tuoi indizi restringono il campo con più efficacia.");
        traitButton2.setText("PRESSIONE TATTICA\n\nPremia gli arresti corretti: la sicurezza nel colpire nel segno è la tua forza.");
    }

    @FXML
    private void onConfirm() {
        ToggleButton selected = (ToggleButton) traitGroup.getSelectedToggle();
        if (selected == null) {
            return;
        }

        boolean isFirstTrait = (selected == traitButton1);
        campaign.applyTraitSelection(isFirstTrait);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameview.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) confirmButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY - Match");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile avviare la partita", e);
        }
    }
}
