package it.unicam.cs.mpgc.rpg126599.model;

import java.util.List;

/**
 * Un singolo nodo della mappa. Le coordinate x/y servono solo a disegnare
 * il nodo nella view: la logica di gioco usa solo id e connections.
 */
public class Location {

    private String id;
    private String name;
    private double x;
    private double y;
    private List<String> connections;

    // Costruttore vuoto richiesto da Gson per la deserializzazione da JSON
    public Location() {
    }

    public Location(String id, String name, double x, double y, List<String> connections) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.connections = connections;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public List<String> getConnections() {
        return connections;
    }
}

