package it.unicam.cs.mpgc.rpg126599.controller;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.shape.Circle;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
// controller che gestisce aspetto grafico della mappa del gioco
public class MapController {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass POLICE = PseudoClass.getPseudoClass("police");
    private static final PseudoClass KILLER = PseudoClass.getPseudoClass("killer");
    private static final PseudoClass ELIMINATED = PseudoClass.getPseudoClass("eliminated");
    private static final PseudoClass CLUE = PseudoClass.getPseudoClass("clue");
    private static final PseudoClass SEARCHED = PseudoClass.getPseudoClass("searched");
    private static final PseudoClass HOME = PseudoClass.getPseudoClass("home");


    @FXML private Circle n1; @FXML private Circle n2; @FXML private Circle n3; @FXML private Circle n4;
    @FXML private Circle n5; @FXML private Circle n6; @FXML private Circle n7; @FXML private Circle n8;
    @FXML private Circle n9; @FXML private Circle n10; @FXML private Circle n11; @FXML private Circle n12;
    @FXML private Circle n13; @FXML private Circle n14; @FXML private Circle n15; @FXML private Circle n16;
    @FXML private Circle n17; @FXML private Circle n18; @FXML private Circle n19; @FXML private Circle n20;
    @FXML private Circle n21; @FXML private Circle n22; @FXML private Circle n23; @FXML private Circle n24;
    @FXML private Circle n25; @FXML private Circle n26;
    
    private final Map<String, Circle> nodesById = new LinkedHashMap<>();
    private Consumer<String> onNodeClicked = locationId -> { };

    @FXML
    private void initialize() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FXML.class) && Circle.class.isAssignableFrom(field.getType())) {
                try {
                    Circle circle = (Circle) field.get(this);
                    if (circle != null) {
                        String id = field.getName(); 
                        nodesById.put(id, circle);
                        
                        circle.setPickOnBounds(true);
                        circle.setMouseTransparent(false);
                        circle.setOnMouseClicked(event -> onNodeClicked.accept(id));
                    }
                } catch (IllegalAccessException e) {
                    
                }
            }
        }
    }

    public Set<String> nodeIds() {
        return nodesById.keySet();
    }

    public void setOnNodeClicked(Consumer<String> handler) {
        this.onNodeClicked = handler;
        nodesById.forEach((id, circle) -> 
            circle.setOnMouseClicked(event -> onNodeClicked.accept(id))
        );
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
// metodi per colorare i nodi della mappa in base a ruoli e stati
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

    public void resetInteractable() {
        nodesById.values().forEach(circle -> circle.setMouseTransparent(false));
    }    

    public void setSelected(String locationId) {
        nodesById.forEach((id, circle) -> circle.pseudoClassStateChanged(SELECTED, id.equals(locationId)));
    }

    public void clearSelection() {
        nodesById.values().forEach(circle -> circle.pseudoClassStateChanged(SELECTED, false));
    }
// setta alcuni nodi come non cliccabili in base a condizioni specifiche
    public void setInteractable(String locationId, boolean interactable) {
        Circle circle = nodesById.get(locationId);
        if (circle != null) {
            circle.setMouseTransparent(!interactable);
        }
    }
}