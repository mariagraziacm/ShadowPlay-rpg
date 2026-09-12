package it.unicam.cs.mpgc.rpg126599.core;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;

// Responsabilità unica: decidere le mosse della Polizia quando è il ruolo automatico.
// Prima era il metodo privato autoPlayPoliceTurn di GameEngine.
public class PoliceAI {

    private final Board board;
    private final GameState state;
    private final MatchActions actions;
    private final NavigationHelper nav;
    private final Random random;

    public PoliceAI(Board board, GameState state, MatchActions actions, NavigationHelper nav, Random random) {
        this.board = board;
        this.state = state;
        this.actions = actions;
        this.nav = nav;
        this.random = random;
    }

    public void autoPlayPoliceTurn() {
        String current = state.getPolice().getCurrentLocationId();
        Optional<Clue> activeLead = state.getFakeClues().stream()
                .filter(clue -> !clue.isInvestigated())
                .filter(clue -> !state.isAlreadySearched(clue.getLocationId()))
                .reduce((first, second) -> second);

        if (activeLead.isPresent() && board.isNeighbor(current, activeLead.get().getLocationId())) {
            Clue clue = activeLead.get();
            actions.applyPoliceArrestAttempt(clue.getLocationId());
            clue.setInvestigated(true);
        } else if (activeLead.isPresent()) {
            String next = nav.findClosestNeighbor(current, activeLead.get().getLocationId());
            actions.applyPoliceMove(next);
        } else if (state.getPoliceCluesRemaining() > 0 && state.getRoundsElapsed() % 2 == 0) {
            actions.applyPoliceUseClue();
        } else {
            Optional<Location> unvisited = board.neighborsOf(current).stream()
                    .filter(location -> !state.getVisitedByPolice().contains(location.getId()))
                    .findFirst();

            String next = unvisited.map(Location::getId).orElseGet(() -> {
                List<Location> neighbors = board.neighborsOf(current);
                return neighbors.get(random.nextInt(neighbors.size())).getId();
            });
            actions.applyPoliceMove(next);
        }
    }
}