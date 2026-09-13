package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;

public class TraitSelectController {

    @FXML private Label titleLabel;
    @FXML private Button confirmButton;
    @FXML private ToggleButton traitButton1;
    @FXML private ToggleButton traitButton2;
    @FXML private ToggleGroup traitGroup;

    private CampaignManager campaign;
    private Trait traitForButton1;
    private Trait traitForButton2;

    public void init(CampaignManager campaign) {
        this.campaign = campaign;

        traitGroup.selectToggle(null); // reset di eventuale selezione residua
        confirmButton.setDisable(true);
        traitGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) ->
                confirmButton.setDisable(newVal == null));

        int level = campaign.getCurrentLevel();
        titleLabel.setText(level == 1
                ? "Scegli il tuo tratto iniziale"
                : "Nuovo tratto sbloccato al livello 3: scegline uno");

        if (campaign.getHumanRole() == RoleType.KILLER) {
            traitForButton1 = level == 1 ? Trait.MANIPOLATORE : Trait.SANGUE_FREDDO;
            traitForButton2 = level == 1 ? Trait.CALCOLATORE : Trait.OMBRA_URBANA;
        } else {
            traitForButton1 = level == 1 ? Trait.DEDUTTIVO : Trait.METODICO;
            traitForButton2 = level == 1 ? Trait.PRESSIONE_TATTICA : Trait.COMANDO_OPERATIVO;
        }

        traitButton1.setText(traitForButton1.getDisplayName() + "\n\n" + traitForButton1.getDescription());
        traitButton2.setText(traitForButton2.getDisplayName() + "\n\n" + traitForButton2.getDescription());
    }

    @FXML
    private void onConfirm() {
        ToggleButton selected = (ToggleButton) traitGroup.getSelectedToggle();
        if (selected == null) {
            return;
        }

        Trait chosenTrait = (selected == traitButton1) ? traitForButton1 : traitForButton2;
        campaign.addHumanTrait(chosenTrait);
        campaign.startCurrentMatch(); // <-- fondamentale: crea davvero l'engine del match

        try {
            NavigationService.LoadedScreen<GameController> screen =
                    NavigationService.load(getClass(), "/fxml/gameview.fxml");
            screen.controller.init(campaign); // <-- fondamentale: senza questa riga la mappa resta morta
            NavigationService.show(confirmButton, screen.root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile avviare la partita", e);
        }
    }
}