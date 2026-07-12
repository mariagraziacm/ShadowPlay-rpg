package it.unicam.cs.mpgc.rpg126599.model;



public class Player {

    private RoleType role;
    private String currentLocationId;

    public Player() {
    }

    public Player(RoleType role, String startingLocationId) {
        this.role = role;
        this.currentLocationId = startingLocationId;
    }

    public RoleType getRole() {
        return role;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public void moveTo(String locationId) {
        this.currentLocationId = locationId;
    }
}

