package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
                "creazione di tranelli strategici ed evasione rapida dai controlli.\n\n" +
                "Inventario: Indizio Falso, Smoke Bomb, Trap Kit e Shortcut Map.\n\n" +
                "Tratti iniziali: Manipolatore o Calcolatore; al livello 3 sblocca Sangue Freddo o Ombra Urbana."
            );
        } else {
            descriptionArea.setText(
                "IL POLIZIOTTO\n\n" +
                "Aspetto Fisico: postura eccentrica, sguardo fisso e occhio cinico, " +
                "nota immediatamente ogni minimo dettaglio fuori posto sulla scena.\n\n" +
                "Profilo Mentale: mente logica e spietatamente deduttiva, analizza " +
                "ogni indizio senza farsi ingannare e anticipa le mosse della preda.\n\n" +
                "Abilità: analisi degli indizi, controllo del territorio tramite " +
                "blocchi stradali e checkpoint mobili, letture rapide dell'area con lo Scanner.\n\n" +
                "Inventario: Indizio, Roadblock, Checkpoint e Scanner.\n\n" +
                "Tratti iniziali: Deduttivo o Pressione Tattica; al livello 3 sblocca Metodico o Comando Operativo."
            );
        }
    }

    @FXML
    private void onContinue() {
        try {
            NavigationService.LoadedScreen<TraitSelectController> screen =
                    NavigationService.load(getClass(), "/fxml/traitselect.fxml");
            screen.controller.init(campaign);
            NavigationService.show(continueButton, screen.root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile aprire la schermata dei tratti", e);
        }
    }

    @FXML
    private void onBack() {
        try {
            NavigationService.LoadedScreen<Object> screen =
                    NavigationService.load(getClass(), "/fxml/roleselect.fxml");
            NavigationService.show(backButton, screen.root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile tornare alla selezione del ruolo", e);
        }
    }
}