package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.Location;

// Responsabilità unica: scegliere un vicino "buono" secondo un criterio di distanza.
// Prima erano due metodi privati di GameEngine, usati sia dalla logica automatica
// del Killer che da quella della Polizia: qui diventano un servizio condiviso.
public class NavigationHelper {

    private final Board board;

    public NavigationHelper(Board board) {
        this.board = board;
    }

    public String findClosestNeighbor(String fromId, String targetId) {
        return board.neighborsOf(fromId).stream()
                .min(Comparator.comparingInt(candidate -> board.distance(candidate.getId(), targetId)))
                .map(Location::getId)
                .orElse(fromId);
    }

    public String findFarthestNeighbor(String fromId, String avoidId, Set<String> alreadyVisited) {
        List<Location> neighbors = board.neighborsOf(fromId);
        List<Location> notVisited = neighbors.stream()
                .filter(candidate -> !alreadyVisited.contains(candidate.getId()))
                .toList();

        List<Location> candidates = notVisited.isEmpty() ? neighbors : notVisited;

        return candidates.stream()
                .max(Comparator.comparingInt(candidate -> board.distance(candidate.getId(), avoidId)))
                .map(Location::getId)
                .orElse(fromId);
    }
}