package it.unicam.cs.mpgc.rpg126599.model;
import java.util.List;

//serve per rappresentare un nodo della mappa con coordinate e connessionni
public class Location {

    private String id;
    private String name;
    private double x;
    private double y;
    private List<String> connections;

   
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

