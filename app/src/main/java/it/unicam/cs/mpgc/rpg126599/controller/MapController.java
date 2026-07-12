package it.unicam.cs.mpgc.rpg126599.controller;


import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.shape.Circle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


public class MapController {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass POLICE = PseudoClass.getPseudoClass("police");
    private static final PseudoClass KILLER = PseudoClass.getPseudoClass("killer");
    private static final PseudoClass ELIMINATED = PseudoClass.getPseudoClass("eliminated");
    private static final PseudoClass CLUE = PseudoClass.getPseudoClass("clue");
    private static final PseudoClass SEARCHED = PseudoClass.getPseudoClass("searched");
    private static final PseudoClass HOME = PseudoClass.getPseudoClass("home");

    @FXML private Circle n1;
    @FXML private Circle n2;
    @FXML private Circle n3;
    @FXML private Circle n4;
    @FXML private Circle n5;
    @FXML private Circle n6;
    @FXML private Circle n7;
    @FXML private Circle n8;
    @FXML private Circle n9;
    @FXML private Circle n10;
    @FXML private Circle n11;
    @FXML private Circle n12;
    @FXML private Circle n13;
    @FXML private Circle n14;
    @FXML private Circle n15;
    @FXML private Circle n16;
    @FXML private Circle n17;
    @FXML private Circle n18;
    @FXML private Circle n19;
    @FXML private Circle n20;
    
    private final Map<String, Circle> nodesById = new LinkedHashMap<>();
    private Consumer<String> onNodeClicked = locationId -> { };

    @FXML
    private void initialize() {
        register("n1", n1);
        register("n2", n2);
        register("n3", n3);
        register("n4", n4);
        register("n5", n5);
        register("n6", n6);
        register("n7", n7);
        register("n8", n8);
        register("n9", n9);
        register("n10", n10);
        register("n11", n11);
        register("n12", n12);
        register("n13", n13);
        register("n14", n14);
        register("n15", n15);
        register("n16", n16);
        register("n17", n17);
        register("n18", n18);
        register("n19", n19);
        register("n20", n20);


        nodesById.forEach((locationId, circle) ->
                circle.setOnMouseClicked(event -> onNodeClicked.accept(locationId)));
    }

    private void register(String locationId, Circle circle) {
        nodesById.put(locationId, circle);
    }


    public Set<String> nodeIds() {
        return nodesById.keySet();
    }

    public void setOnNodeClicked(Consumer<String> handler) {
        this.onNodeClicked = handler;
    }
    public void clearAllStates() {
        nodesById.values().forEach(circle -> {
            circle.pseudoClassStateChanged(POLICE, false);
            circle.pseudoClassStateChanged(KILLER, false);
            circle.pseudoClassStateChanged(ELIMINATED, false);
            circle.pseudoClassStateChanged(CLUE, false);
            circle.pseudoClassStateChanged(SEARCHED, false);
            circle.pseudoClassStateChanged(HOME, false);
        });
    }

    public void markPolice(String locationId) {
        setState(locationId, POLICE);
    }

    public void markKiller(String locationId) {
        setState(locationId, KILLER);
    }

    public void markEliminated(String locationId) {
        setState(locationId, ELIMINATED);
    }

    public void markFakeClue(String locationId) {
        setState(locationId, CLUE);
    }

    public void markSearched(String locationId) {
        setState(locationId, SEARCHED);
    }

    public void markHome(String locationId) {
        setState(locationId, HOME);
    }

    private void setState(String locationId, PseudoClass pseudoClass) {
        Circle circle = nodesById.get(locationId);
        if (circle != null) {
            circle.pseudoClassStateChanged(pseudoClass, true);
        }
    }

    /** Riabilita il click su tutti i nodi: da chiamare ad ogni refresh, prima di ridisabilitare quelli esclusi.????????? */
    public void resetInteractable() {
        nodesById.values().forEach(circle -> circle.setMouseTransparent(false));
    }    

 /** Evidenzia un solo nodo come "selezionato" (es. durante una scelta in corso). */
    public void setSelected(String locationId) {
        nodesById.forEach((id, circle) -> circle.pseudoClassStateChanged(SELECTED, id.equals(locationId)));
    }

    public void clearSelection() {
        nodesById.values().forEach(circle -> circle.pseudoClassStateChanged(SELECTED, false));
    }

    /** Rende un nodo non cliccabile (es. una casella dove l'arresto è già fallito). */
    public void setInteractable(String locationId, boolean interactable) {
        Circle circle = nodesById.get(locationId);
        if (circle != null) {
            circle.setMouseTransparent(!interactable);
        }
    }

   
}



