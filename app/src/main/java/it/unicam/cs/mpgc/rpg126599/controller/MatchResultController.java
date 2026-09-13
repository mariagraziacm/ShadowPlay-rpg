package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.util.List;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;

// fine match: mostra l'immagine del vincitore (killer.png / police.png), gli XP accumulati è possibile   proseguire la campagna o tornare al menu
public class MatchResultController {

    @FXML private ImageView backgroundImage;
    @FXML private Label resultLabel;
    @FXML private Label xpLabel;
    @FXML private Button continueButton;
@FXML private AnchorPane rootPane;
    private CampaignManager campaign;

    public void init(CampaignManager campaign, RoleType matchWinner, String endReason) {
        this.campaign = campaign;

        Image winnerImage = loadWinnerImage(matchWinner);
        String titolo = matchWinner == RoleType.KILLER ? "IL KILLER VINCE IL MATCH" : "IL POLIZIOTTO VINCE IL MATCH";

        if (winnerImage != null) {
            backgroundImage.setImage(winnerImage);
        }
        resultLabel.setText(titolo + "\n" + endReason);

        if (campaign.isSeriesOver()) {
            RoleType seriesWinner = campaign.getSeriesWinner();
            resultLabel.setText((seriesWinner == RoleType.KILLER
                    ? "IL KILLER HA VINTO LA PARTITA"
                    : "IL POLIZIOTTO HA VINTO LA PARTITA")
                    + "\n" + endReason
                    + "\n\nCAMPAGNA CONCLUSA: ha vinto "
                    + (seriesWinner == RoleType.KILLER ? "il Killer" : "il Poliziotto") + "!");
            continueButton.setText("Torna al menu");
        } else {
            continueButton.setText("Prossimo match");
        }

        xpLabel.setText(String.format("★ +%d XP Killer: %d    ★ +%d XP Poliziotto: %d",
                campaign.getKillerXp(), campaign.getKillerXp(), campaign.getPoliceXp(), campaign.getPoliceXp()));
    }


    private Image loadWinnerImage(RoleType winner) {
        String fileName = winner == RoleType.KILLER ? "killer.png" : "police.png";

        var classpathStream = getClass().getResourceAsStream("/images/" + fileName);
        if (classpathStream != null) {
            return new Image(classpathStream);
        }

        List<String> fileCandidates = List.of(
                "app/src/main/resources/images/" + fileName,
                "src/main/resources/images/" + fileName,
                "images/" + fileName
        );

        return fileCandidates.stream()
                .map(File::new)
                .filter(File::exists)
                .findFirst()
                .map(file -> new Image(file.toURI().toString()))
                .orElse(null);
    }

    @FXML
    private void onContinue() {
        try {
            if (campaign.isSeriesOver()) {
                NavigationService.LoadedScreen<Object> screen =
                        NavigationService.load(getClass(), "/fxml/roleselect.fxml");
                NavigationService.show(continueButton, screen.root);
                return;
            }
            if (campaign.isSecondTraitUnlockPending()) {
                NavigationService.LoadedScreen<TraitSelectController> screen =
                        NavigationService.load(getClass(), "/fxml/traitselect.fxml");
                screen.controller.init(campaign);
                NavigationService.show(continueButton, screen.root);
            } else {
                campaign.startCurrentMatch();
                NavigationService.LoadedScreen<GameController> screen =
                        NavigationService.load(getClass(), "/fxml/gameview.fxml");
                screen.controller.init(campaign);
                NavigationService.show(continueButton, screen.root);
            }
        } catch (IOException e) {
            resultLabel.setText("Errore nel caricamento della schermata successiva.");
        }
        
    }
    @FXML
    public void initialize() {
        backgroundImage.setPreserveRatio(false);
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
    }
    
}