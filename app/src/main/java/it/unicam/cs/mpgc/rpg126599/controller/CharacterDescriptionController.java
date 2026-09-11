package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;

public class CharacterDescriptionController {

    @FXML private TextArea descriptionArea;
    @FXML private Button continueButton;
    @FXML private Button backButton;

    private CampaignManager campaign;

    public void init(CampaignManager campaign) {
        this.campaign = campaign;

        if (descriptionArea == null) {
            throw new IllegalStateException("L'elemento 'descriptionArea' non è stato iniettato da FXML. Verifica il file .fxml.");
        }

        // Impedisce all'utente di modificare il testo descrittivo a schermo
        descriptionArea.setEditable(false);

        RoleType role = campaign.getHumanRole();
if (role == RoleType.KILLER) {
            descriptionArea.setText(
                "IL KILLER\n\n" +
                "Aspetto Fisico: presenza distinta e curata, portamento composto " +
                "e sguardo acuto: l'immagine del cittadino modello che si mimetizza alla perfezione.\n\n" +
                "Profilo Mentale: intelletto superiore, lucido narcisista e stratega " +
                "impeccabile, anticipa le mosse altrui mantenendo un controllo totale.\n\n" +
                "Abilità: depistaggio con falsi indizi, coperture e alibi di ferro, " +
                "creazione di tranelli strategici ed evasione rapida dai controlli."
            );
        } else {
            descriptionArea.setText(
                "IL POLIZIOTTO\n\n" +
                "Aspetto Fisico: postura eccentrica, sguardo fisso e occhio cinico, " +
                "nota immediatamente ogni minimo dettaglio fuori posto sulla scena.\n\n" +
                "Profilo Mentale: mente logica e spietatamente deduttiva, analizza " +
                "ogni indizio senza farsi ingannare e anticipa le mosse della preda.\n\n" +
                "Abilità: analisi degli indizi, controllo del territorio tramite " +
                "blocchi stradali e checkpoint mobili, letture rapide dell'area con lo Scanner."
            );
        }
    }

    @FXML
    private void onContinue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/traitselect.fxml"));
            Parent root = loader.load();

            TraitSelectController controller = loader.getController();
            controller.init(campaign);

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata dei tratti", e);
        }
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/roleselect.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("SHADOW PLAY");
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile tornare alla selezione del ruolo", e);
        }
    }
}