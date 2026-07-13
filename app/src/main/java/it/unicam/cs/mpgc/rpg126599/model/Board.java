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

// mappa del gioco con nodi e connessioni, calcola le distanze tra tutti i nodi
public class Board {

    private final Map<String, Location> locations = new LinkedHashMap<>();
    
    private final Map<String, Map<String, Integer>> allPairsDistances = new HashMap<>();


    public Board(Collection<Location> initialLocations) {
        if (initialLocations == null || initialLocations.isEmpty()) {
            throw new IllegalArgumentException("La collezione di nodi non può essere vuota.");
        }
        for (Location location : initialLocations) {
            this.locations.put(location.getId(), location);
        }
        
        
        precomputeDistances(); // calcol distanze minime tra i nodi della mappa per ottimizzare successive letture
    }

// algoritmo per calcolare distanze tra i nodi della mappa (utilizzata BFS)
    // ho fatto questa scelta perché mi serviva un modo valido per gestire e verificare gli spostamenti del killer che può spostarsi anche di due caselle alla volta, 
    // quindi avevo bisogno di sapere la distanza minima tra due nodi 
 
    private void precomputeDistances() {
        for (String fromId : locations.keySet()) {
            allPairsDistances.put(fromId, computeBfsDistancesFrom(fromId));
        }
    }

    
    private Map<String, Integer> computeBfsDistancesFrom(String startId) {
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
// nodo iniziale
        queue.add(startId);
        visited.add(startId);
        distances.put(startId, 0);
// algoritmo BFS
        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distances.get(current);
//indagine dei vicini del nodo corrente
            for (String neighborId : get(current).getConnections()) {
                if (visited.add(neighborId)) {
                    distances.put(neighborId, currentDistance + 1);
                    queue.add(neighborId);
                }
            }
        }
        return distances;
    }

   
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
// lista nodi vicini a un nodo specifico
    public List<Location> neighborsOf(String locationId) {
        List<Location> result = new ArrayList<>();
        for (String neighborId : get(locationId).getConnections()) {
            result.add(get(neighborId));
        }
        return result;
    }
// controlla se due nodi sono collegati direttamente
    public boolean isNeighbor(String fromId, String toId) {
        return get(fromId).getConnections().contains(toId);
    }
}