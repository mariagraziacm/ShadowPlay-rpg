package it.unicam.cs.mpgc.rpg126599.model;


/**
 * Indizio falso lasciato dal killer su una casella in cui non si trova
 * realmente. "investigated" è usato solo dalla logica automatica per
 * sapere se ha già controllato questa pista.
 */
public class Clue {

    private String locationId;
    private int roundFound;
    private boolean investigated;

    // Costruttore vuoto richiesto da Gson
    public Clue() {
    }

    public Clue(String locationId, int roundFound) {
        this.locationId = locationId;
        this.roundFound = roundFound;
    }

    public String getLocationId() {
        return locationId;
    }

    public int getRoundFound() {
        return roundFound;
    }

    public boolean isInvestigated() {
        return investigated;
    }

    public void setInvestigated(boolean investigated) {
        this.investigated = investigated;
    }
}
