package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;

// mostra la descrizione del personaggio scelto (aspetto fisico, profilo mentale, abilità)
// prima di procedere alla scelta del tratto e all'inizio della campagna
public class CharacterDescriptionController {

    @FXML private Label nameLabel;
    @FXML private Label physicalLabel;
    @FXML private Label mentalLabel;
    @FXML private Label abilityLabel;
    @FXML private Button continueButton;

    private CampaignManager campaign;

    public void init(CampaignManager campaign) {
        this.campaign = campaign;
        RoleType role = campaign.getHumanRole();

        if (role == RoleType.KILLER) {
            nameLabel.setText("IL KILLER");
            physicalLabel.setText("Aspetto Fisico: figura silenziosa e sfuggente, capace di confondersi "
                    + "tra la folla e sparire nell'ombra in pochi istanti, senza lasciare tracce evidenti.");
            mentalLabel.setText("Profilo Mentale: lucido e manipolatore, pianifica ogni mossa in anticipo, "
                    + "resta calmo sotto pressione e sa trarre vantaggio dagli errori altrui.");
            abilityLabel.setText("Abilità: depistaggio con indizi falsi, coperture rapide, capacità "
                    + "di creare tranelli sul territorio e di sfuggire ai controlli quando serve.");
        } else {
            nameLabel.setText("IL POLIZIOTTO");
            physicalLabel.setText("Aspetto Fisico: portamento metodico e attento, occhio allenato "
                    + "a cogliere ogni dettaglio fuori posto sulla scena.");
            mentalLabel.setText("Profilo Mentale: analitico e paziente, costruisce le proprie ipotesi "
                    + "passo dopo passo: per lui ogni arresto è una decisione ad alta responsabilità.");
            abilityLabel.setText("Abilità: analisi degli indizi, controllo del territorio tramite "
                    + "blocchi stradali e checkpoint mobili, letture rapide dell'area con lo Scanner.");
        }
    }

    @FXML
    private void onContinue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/traitselect.fxml"));
            Parent root = loader.load();

            TraitSelectController controller = loader.getController();
            controller.init();

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata dei tratti", e);
        }
    }
}