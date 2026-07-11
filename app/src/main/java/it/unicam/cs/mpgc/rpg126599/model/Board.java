package it.unicam.cs.mpgc.rpg126599.model;


import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Rappresenta la mappa come grafo e offre solo funzioni: nessuna classe di
 * supporto dedicata al caricamento, nessuna interfaccia intermedia.
 * Tutta la logica di calcolo (vicini, distanza) vive qui perché riguarda
 * esclusivamente la topologia della mappa.
 */
public class Board {

    private final Map<String, Location> locations = new LinkedHashMap<>();

    private Board() {
    }

    /** Carica la mappa da un file JSON nelle risorse (es. "/maps/map.json"). */
    public static Board loadFromResource(String resourcePath) {
        Board board = new Board();
        Gson gson = new Gson();

        try (Reader reader = new InputStreamReader(
                Board.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8)) {
            MapFile mapFile = gson.fromJson(reader, MapFile.class);
            for (Location location : mapFile.locations) {
                board.locations.put(location.getId(), location);
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Impossibile caricare la mappa: " + resourcePath, e);
        }
        return board;
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

    /** Distanza minima (numero di archi) tra due nodi, calcolata con una visita in ampiezza. */
    public int distance(String fromId, String toId) {
        if (fromId.equals(toId)) {
            return 0;
        }
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        Map<String, Integer> distances = new HashMap<>();

        queue.add(fromId);
        visited.add(fromId);
        distances.put(fromId, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distances.get(current);

            if (current.equals(toId)) {
                return currentDistance;
            }
            for (String neighborId : get(current).getConnections()) {
                if (visited.add(neighborId)) {
                    distances.put(neighborId, currentDistance + 1);
                    queue.add(neighborId);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    /** Tra i vicini di fromId, quello con distanza minima verso targetId. */
    public Location neighborClosestTo(String fromId, String targetId) {
        return neighborsOf(fromId).stream()
                .min(Comparator.comparingInt(candidate -> distance(candidate.getId(), targetId)))
                .orElse(get(fromId));
    }

    /** Tra i vicini di fromId, quello con distanza massima da avoidId (preferendo nodi mai visitati). */
    public Location neighborFarthestFrom(String fromId, String avoidId, Set<String> alreadyVisited) {
        List<Location> neighbors = neighborsOf(fromId);
        List<Location> notVisited = new ArrayList<>();
        for (Location candidate : neighbors) {
            if (!alreadyVisited.contains(candidate.getId())) {
                notVisited.add(candidate);
            }
        }
        List<Location> candidates = notVisited.isEmpty() ? neighbors : notVisited;

        return candidates.stream()
                .max(Comparator.comparingInt(candidate -> distance(candidate.getId(), avoidId)))
                .orElse(get(fromId));
    }

    public Optional<Location> firstUnvisitedNeighbor(String fromId, Set<String> visited) {
        return neighborsOf(fromId).stream()
                .filter(location -> !visited.contains(location.getId()))
                .findFirst();
    }

    public Location randomNeighbor(String fromId) {
        List<Location> neighbors = neighborsOf(fromId);
        return neighbors.get(new Random().nextInt(neighbors.size()));
    }

    /** Struttura di appoggio solo per la deserializzazione del file map.json. */
    private static class MapFile {
        List<Location> locations;
    }
}

