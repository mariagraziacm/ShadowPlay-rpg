package it.unicam.cs.mpgc.rpg126599.model;
// rappresenta un indizi reali e falsi
public class Clue {

    private String locationId;
    private int roundFound;
    private boolean investigated;


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
