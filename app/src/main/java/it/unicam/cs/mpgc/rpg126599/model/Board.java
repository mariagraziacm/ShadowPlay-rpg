package it.unicam.cs.mpgc.rpg126599.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Rappresenta la mappa di gioco unicamente come grafo topologico puro.
 * Ottimizzata con il precalcolo delle distanze all'avvio per garantire efficienza O(1).
 */
public class Board {

    private final Map<String, Location> locations = new LinkedHashMap<>();
    // Mappa a due livelli: allPairsDistances.get(fromId).get(toId) -> distanza
    private final Map<String, Map<String, Integer>> allPairsDistances = new HashMap<>();

    /**
     * Costruisce la Board e precalcola istantaneamente tutte le distanze minime.
     */
    public Board(Collection<Location> initialLocations) {
        if (initialLocations == null || initialLocations.isEmpty()) {
            throw new IllegalArgumentException("La collezione di nodi non può essere vuota.");
        }
        for (Location location : initialLocations) {
            this.locations.put(location.getId(), location);
        }
        
        // Efficienza: esegue la BFS una volta sola per ogni nodo al caricamento
        precomputeDistances();
    }

    /**
     * Precalcola le distanze tra tutte le coppie di nodi possibili sul grafo.
     */
    private void precomputeDistances() {
        for (String fromId : locations.keySet()) {
            allPairsDistances.put(fromId, computeBfsDistancesFrom(fromId));
        }
    }

    /**
     * Algoritmo BFS interno che mappa le distanze da un nodo di partenza verso tutti gli altri.
     */
    private Map<String, Integer> computeBfsDistancesFrom(String startId) {
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();

        queue.add(startId);
        visited.add(startId);
        distances.put(startId, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distances.get(current);

            for (String neighborId : get(current).getConnections()) {
                if (visited.add(neighborId)) {
                    distances.put(neighborId, currentDistance + 1);
                    queue.add(neighborId);
                }
            }
        }
        return distances;
    }

    /**
     * Restituisce la distanza minima tra due nodi in tempo costante O(1).
     */
    public int distance(String fromId, String toId) {
        if (fromId.equals(toId)) {
            return 0;
        }
        Map<String, Integer> row = allPairsDistances.get(fromId);
        if (row == null) {
            return Integer.MAX_VALUE;
        }
        return row.getOrDefault(toId, Integer.MAX_VALUE);
    }

    public Location get(String id) {
        Location location = locations.get(id);
        if (location == null) {
            throw new NoSuchElementException("Nodo mappa inesistente: " + id);
        }
        return location;
    }

    public Collection<Location> all() {
        return Collections.unmodifiableCollection(locations.values());
    }

    public List<Location> neighborsOf(String locationId) {
        List<Location> result = new ArrayList<>();
        for (String neighborId : get(locationId).getConnections()) {
            result.add(get(neighborId));
        }
        return result;
    }

    public boolean isNeighbor(String fromId, String toId) {
        return get(fromId).getConnections().contains(toId);
    }
}