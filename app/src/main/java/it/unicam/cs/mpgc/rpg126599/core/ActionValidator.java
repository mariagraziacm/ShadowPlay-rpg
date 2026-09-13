package it.unicam.cs.mpgc.rpg126599.core;

import java.util.NoSuchElementException;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

//verifica le precondizioni di un'azione e lanciare eccezioni

public class ActionValidator {

    private final Board board;
    private final GameState state;

    public ActionValidator(Board board, GameState state) {
        this.board = board;
        this.state = state;
    }

    public void requirePhase(Turn expected) {
        if (state.getPhase() != expected) {
            throw new IllegalStateException("Non è il momento per questa azione. Fase attuale: " + state.getPhase());
        }
    }

    public void requireHumanRole(RoleType expected) {
        if (state.getHumanRole() != expected) {
            throw new IllegalStateException("Questa azione non ti spetta.");
        }
    }

    public void requireNeighbor(String fromId, String toId) {
        requireNeighbor(fromId, toId, "Puoi agire solo su una casella collegata alla tua.");
    }

    public void requireNeighbor(String fromId, String toId, String message) {
        if (!board.isNeighbor(fromId, toId)) {
            throw new IllegalArgumentException(message);
        }
    }

    public void requireNotAlreadySearched(String locationId) {
        if (state.isAlreadySearched(locationId)) {
            throw new IllegalArgumentException("Hai già cercato in questa casella: scegline un'altra.");
        }
    }

    public void requireExistingLocation(String locationId) {
        try {
            board.get(locationId);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Casella inesistente sulla mappa: " + locationId);
        }
    }

    public void requireWithinDistance(String fromId, String toId, int maxDistance) {
        requireExistingLocation(toId);
        int d = board.distance(fromId, toId);
        if (d == Integer.MAX_VALUE || d > maxDistance) {
            throw new IllegalArgumentException("Puoi tentare l'arresto solo entro " + maxDistance + " caselle di distanza.");
        }
    }

    // roadblock blocca l'intero nodo, checkpoint blocca un singolo collegamento
    
    public void checkMovementNotBlocked(String current, String target, String intermediate) {
        String roadblock = state.getActiveRoadblockLocationId();
        if (roadblock != null && (roadblock.equals(target) || roadblock.equals(intermediate))) {
            throw new IllegalArgumentException(
                    "Un Roadblock della Polizia blocca quel nodo per questo turno: scegli un altro percorso oppure usa la Shortcut Map.");
        }
        boolean checkpointBlocksDirect = intermediate == null && state.isCheckpointEdge(current, target);
        boolean checkpointBlocksHop = intermediate != null
                && (state.isCheckpointEdge(current, intermediate) || state.isCheckpointEdge(intermediate, target));
        if (checkpointBlocksDirect || checkpointBlocksHop) {
            throw new IllegalArgumentException(
                    "Un Checkpoint della Polizia blocca quel collegamento per questo turno: scegli un altro percorso oppure usa la Shortcut Map.");
        }
    }

    // il Killer non può rientrare nel proprio nascondiglio finché il Poliziotto vi si trova sopra
    public void requireHomeNotGuardedByPolice(String targetLocationId) {
        if (targetLocationId.equals(state.getKillerHomeLocationId())
                && targetLocationId.equals(state.getPolice().getCurrentLocationId())) {
            throw new IllegalArgumentException(
                    "Il Poliziotto presidia il tuo nascondiglio: non puoi rientrare finché non se ne allontana.");
        }
    }

    // luogo del primo omicidio deve trovarsi ad almeno a un tot di  caselle di distanza dal nascondiglio
    
    public void requireMurderLocationFarEnoughFromHome(String locationId, int minDistance) {
        int distanceFromHome = board.distance(state.getKillerHomeLocationId(), locationId);
        if (distanceFromHome < minDistance) {
            throw new IllegalArgumentException(
                    "Il luogo dell'omicidio deve essere ad almeno " + minDistance + " caselle dal nascondiglio.");
        }
    }
}