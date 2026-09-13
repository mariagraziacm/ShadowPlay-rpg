package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Comparator;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameRules;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;

// Responsabilità unica: decidere le mosse del Killer quando è il ruolo automatico.
// Prima erano i metodi privati autoSetupKiller/autoPlayKillerTurn di GameEngine,
// mescolati con la validazione delle azioni umane e il calcolo dell'Xp.
public class KillerAI {

    private final Board board;
    private final GameState state;
    private final MatchActions actions;
    private final NavigationHelper nav;

    public KillerAI(Board board, GameState state, MatchActions actions, NavigationHelper nav) {
        this.board = board;
        this.state = state;
        this.actions = actions;
        this.nav = nav;
    }

    public void autoSetupKiller() {
        if (!state.isHomeChosen()) {
            String policeStart = state.getPolice().getCurrentLocationId();
            String home = board.all().stream()
                    .max(Comparator.comparingInt(location -> board.distance(location.getId(), policeStart)))
                    .map(Location::getId)
                    .orElseThrow();
            actions.applyChooseHome(home);
        }
        String home = state.getKillerHomeLocationId();
        // rispetta la stessa regola richiesta al giocatore umano: il luogo dell'omicidio
        // deve stare ad almeno MIN_MURDER_LOCATION_DISTANCE_FROM_HOME caselle da casa.
        // Tra i nodi validi si sceglie il più vicino, per non allontanare l'IA più del necessario.
        String murderLocation = board.all().stream()
                .filter(location -> board.distance(home, location.getId()) >= GameRules.MIN_MURDER_LOCATION_DISTANCE_FROM_HOME)
                .min(Comparator.comparingInt(location -> board.distance(home, location.getId())))
                .map(Location::getId)
                .orElseGet(() -> board.neighborsOf(home).get(0).getId());
        actions.applyChooseMurderLocation(murderLocation);
    }

    public void autoPlayKillerTurn() {
        boolean stillHasFakeClues = state.getKillerFakeCluesRemaining() > 0;
        boolean isMidGame = state.getRoundsElapsed() == state.getMaxRounds() / 2;

        String current = state.getKiller().getCurrentLocationId();

        if (stillHasFakeClues && isMidGame) {
            String decoyLocation = nav.findFarthestNeighbor(current, state.getKillerHomeLocationId(), state.getVisitedByKiller());
            actions.applyKillerFakeClue(decoyLocation);
            return;
        }

        boolean shouldHeadHome = state.getRoundsElapsed() >= state.getMaxRounds() - 2;
        String target = shouldHeadHome
                ? nav.findClosestNeighbor(current, state.getKillerHomeLocationId())
                : nav.findFarthestNeighbor(current, state.getKillerHomeLocationId(), state.getVisitedByKiller());

        actions.applyKillerMove(target);
    }
}