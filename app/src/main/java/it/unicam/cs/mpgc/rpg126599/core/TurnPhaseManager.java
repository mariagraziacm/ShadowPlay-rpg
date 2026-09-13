package it.unicam.cs.mpgc.rpg126599.core;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;


public class TurnPhaseManager {

    private final GameState state;
    private final KillerAI killerAI;
    private final PoliceAI policeAI;

    public TurnPhaseManager(Board board, GameState state, MatchActions actions, java.util.Random random) {
        this.state = state;
        NavigationHelper nav = new NavigationHelper(board);
        this.killerAI = new KillerAI(board, state, actions, nav);
        this.policeAI = new PoliceAI(board, state, actions, nav, random);
    }

    
    public void endPoliceTurn() {
        state.clearActiveTrapZone();
        state.clearKillerSmokeBombActive();

        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.incrementRound();
        if (state.isPoliceStunnedNextTurn()) {
           
            state.incrementRound();
            state.setPoliceStunnedNextTurn(false);
        }
        if (state.getRoundsElapsed() >= state.getMaxRounds()) {
            state.finish(RoleType.KILLER, "Tempo scaduto: il killer sfugge alla cattura.");
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.setPhase(Turn.AWAITING_KILLER_ACTION);
    }

    public void resolveAutomaticPhases() {
        while (!state.isFinished() && phaseBelongsToAutomaticRole()) {
            switch (state.getPhase()) {
                case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE -> killerAI.autoSetupKiller();
                case AWAITING_KILLER_ACTION -> killerAI.autoPlayKillerTurn();
                case AWAITING_POLICE_ACTION -> {
                    policeAI.autoPlayPoliceTurn();
                    endPoliceTurn();
                }
                case GAME_OVER -> { }
            }
        }
        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
        }
    }

    private boolean phaseBelongsToAutomaticRole() {
        RoleType automaticRole = state.getHumanRole() == RoleType.KILLER ? RoleType.POLICE : RoleType.KILLER;
        return switch (state.getPhase()) {
            case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE, AWAITING_KILLER_ACTION ->
                    automaticRole == RoleType.KILLER;
            case AWAITING_POLICE_ACTION -> automaticRole == RoleType.POLICE;
            case GAME_OVER -> false;
        };
    }
}