package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import it.unicam.cs.mpgc.rpg126599.core.CampaignManager;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;

// Schermata di fine match: mostra l'immagine del vincitore (killer.png / police.png),
// gli XP accumulati e propone di proseguire la campagna oppure tornare al menu
public class MatchResultController {

    @FXML private ImageView backgroundImage;
    @FXML private Label resultLabel;
    @FXML private Label xpLabel;
    @FXML private Button continueButton;

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

        xpLabel.setText(String.format("⭐ XP Killer: %d    ⭐ XP Poliziotto: %d",
                campaign.getKillerXp(), campaign.getPoliceXp()));
    }

    // Carica l'immagine corretta cercandola prima nel classpath e poi sul filesystem
    private Image loadWinnerImage(RoleType winner) {
        String fileName = winner == RoleType.KILLER ? "killer.png" : "police.png";

        // 1. Prova dal Classpath (cartella /images/ dentro resources)
        String resourcePath = "/images/" + fileName;
        var stream = getClass().getResourceAsStream(resourcePath);
        if (stream != null) {
            System.out.println("Immagine trovata nel CLASSPATH: " + resourcePath);
            return new Image(stream);
        }

        // 2. Fallback su FileSystem per ambiente di sviluppo (Gradle/Maven/IDE)
        List<String> fileCandidates = List.of(
                "app/src/main/resources/images/" + fileName,
                "src/main/resources/images/" + fileName,
                "images/" + fileName
        );

        for (String path : fileCandidates) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Immagine trovata su FILESYSTEM: " + file.getAbsolutePath());
                return new Image(file.toURI().toString());
            }
        }

        System.err.println("ATTENZIONE: Immagine " + fileName + " non trovata nei percorsi previsti.");
        return null;
    }

    @FXML
    private void onContinue() {
        try {
            if (campaign.isSeriesOver()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/roleselect.fxml"));
                Parent root = loader.load();
                setScene(root);
                return;
            }
            if (campaign.isSecondTraitUnlockPending()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/traitselect.fxml"));
                Parent root = loader.load();
                TraitSelectController controller = loader.getController();
                controller.init(campaign);
                setScene(root);
            } else {
                campaign.startCurrentMatch();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameview.fxml"));
                Parent root = loader.load();
                GameController controller = loader.getController();
                controller.init(campaign);
                setScene(root);
            }
        } catch (IOException e) {
            resultLabel.setText("Errore nel caricamento della schermata successiva.");
        }
    }

    private void setScene(Parent root) {
        Stage stage = (Stage) continueButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.setTitle("SHADOW PLAY");
    }
}