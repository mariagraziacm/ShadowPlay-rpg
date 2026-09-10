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

public class TraitSelectController {

    @FXML private Label titleLabel;
    @FXML private Button confirmButton;
    @FXML private ToggleButton traitButton1;
    @FXML private ToggleButton traitButton2;
    @FXML private ToggleGroup traitGroup;

    private CampaignManager campaign;

    public void init(CampaignManager campaign) {
        this.campaign = campaign;
        
        // Esempio: imposta i testi dinamici in base al ruolo scelto
        traitButton1.setText("Primo Tratto\n\nDescrizione o effetto...");
        traitButton2.setText("Secondo Tratto\n\nDescrizione o effetto...");
    }

    @FXML
    private void onConfirm() {
        ToggleButton selected = (ToggleButton) traitGroup.getSelectedToggle();
        if (selected == null) {
            System.out.println("Seleziona prima un tratto!");
            return;
        }

        // Salva la scelta nel CampaignManager (es. quale tratto è stato scelto)
        boolean isFirstTrait = selected == traitButton1;
        campaign.applyTraitSelection(isFirstTrait);

        try {
            // Carica la schermata della partita (sostituisci il percorso con il tuo file FXML)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameview.fxml"));
            Parent root = loader.load();

            // Se il controller della partita ha un metodo init(), passagli il campaign
            // MatchController matchController = loader.getController();
            // matchController.init(campaign);

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