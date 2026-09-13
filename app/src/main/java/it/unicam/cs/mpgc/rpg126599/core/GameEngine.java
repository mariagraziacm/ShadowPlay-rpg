package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Random;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameRules;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;
import it.unicam.cs.mpgc.rpg126599.model.MatchDifficulty;
import it.unicam.cs.mpgc.rpg126599.model.Player;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

public class GameEngine {

    private final Board board;
    private final GameState state;
    private final ActionValidator validator;
    private final MatchActions actions;
    private final TurnPhaseManager turnPhaseManager;

    private GameEngine(Board board, GameState state) {
        this.board = board;
        this.state = state;
        Random random = new Random();
        XpCalculator xpCalculator = new XpCalculator(board);
        this.validator = new ActionValidator(board, state);
        this.actions = new MatchActions(board, state, xpCalculator, random);
        this.turnPhaseManager = new TurnPhaseManager(board, state, actions, random);
    }

    public static GameEngine newGame(Board board, RoleType humanRole) {
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole, MatchDifficulty.MATCH_1);
        GameEngine engine = new GameEngine(board, state);
        engine.turnPhaseManager.resolveAutomaticPhases();
        return engine;
    }

    public static GameEngine newCampaignMatch(Board board, GameState state) {
        GameEngine engine = new GameEngine(board, state);
        engine.turnPhaseManager.resolveAutomaticPhases();
        return engine;
    }

    public static GameEngine resume(Board board, GameState state) {
        GameEngine engine = new GameEngine(board, state);
        engine.turnPhaseManager.resolveAutomaticPhases();
        return engine;
    }

    public GameState getState() { return state; }
    public Board getBoard() { return board; }

    

    public void chooseHome(String locationId) {
        validator.requirePhase(Turn.AWAITING_HOME_CHOICE);
        validator.requireHumanRole(RoleType.KILLER);
        validator.requireExistingLocation(locationId);
        actions.applyChooseHome(locationId);
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void chooseMurderLocation(String locationId) {
        validator.requirePhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
        validator.requireHumanRole(RoleType.KILLER);
        validator.requireExistingLocation(locationId);
        validator.requireMurderLocationFarEnoughFromHome(locationId, GameRules.MIN_MURDER_LOCATION_DISTANCE_FROM_HOME);
        actions.applyChooseMurderLocation(locationId);
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void killerMove(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_KILLER_ACTION);
        validator.requireHumanRole(RoleType.KILLER);

        String current = state.getKiller().getCurrentLocationId();
        int distance = board.distance(current, targetLocationId);

        if (distance < GameRules.KILLER_MIN_MOVE_DISTANCE || distance > GameRules.KILLER_MAX_MOVE_DISTANCE) {
            throw new IllegalArgumentException("Puoi spostarti solo di 1 o 2 caselle.");
        }

        String intermediate = null;
        if (distance == 2) {
            intermediate = board.neighborsOf(current).stream()
                    .filter(n -> board.isNeighbor(n.getId(), targetLocationId))
                    .findFirst()
                    .map(Location::getId)
                    .orElse(null);
        }

        validator.checkMovementNotBlocked(current, targetLocationId, intermediate);
        validator.requireHomeNotGuardedByPolice(targetLocationId);

        if (intermediate != null) {
            state.markKillerVisited(intermediate);
        }

        actions.applyKillerMove(targetLocationId);
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void killerLeaveFakeClue(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_KILLER_ACTION);
        validator.requireHumanRole(RoleType.KILLER);
        if (state.getKillerFakeCluesRemaining() <= 0) {
            throw new IllegalStateException("Non hai più indizi falsi disponibili.");
        }
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) {
            throw new IllegalArgumentException("Non puoi lasciare l'indizio sulla tua stessa posizione.");
        }
        actions.applyKillerFakeClue(targetLocationId);
        turnPhaseManager.resolveAutomaticPhases();
    }

    // Smoke Bomb: annulla il prossimo Scanner della Polizia
    public void killerUseSmokeBomb() {
        validator.requirePhase(Turn.AWAITING_KILLER_ACTION);
        validator.requireHumanRole(RoleType.KILLER);
        if (state.getKillerSmokeBombsRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Smoke Bomb disponibili.");
        }
        actions.applyKillerSmokeBomb();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
        turnPhaseManager.resolveAutomaticPhases();
    }

    // Trap Kit: piazza una Trappola che rallenta la Polizia se ci entra nel turno immediatamente successivo
    public void killerPlaceTrap(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_KILLER_ACTION);
        validator.requireHumanRole(RoleType.KILLER);
        if (state.getKillerTrapKitsRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Trap Kit disponibili.");
        }
        validator.requireExistingLocation(targetLocationId);
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) {
            throw new IllegalArgumentException("Non puoi piazzare la trappola sulla tua stessa posizione.");
        }
        actions.applyKillerPlaceTrap(targetLocationId);
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
        turnPhaseManager.resolveAutomaticPhases();
    }

    // shortcut Map: ignora distanza massima, Roadblock e Checkpoint
    public void killerUseShortcutMap(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_KILLER_ACTION);
        validator.requireHumanRole(RoleType.KILLER);
        if (state.isKillerShortcutMapUsed()) {
            throw new IllegalStateException("Hai già usato la Shortcut Map in questo match.");
        }
        validator.requireExistingLocation(targetLocationId);
        validator.requireHomeNotGuardedByPolice(targetLocationId);
        actions.applyKillerShortcutMap(targetLocationId);
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void policeUseClue() {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        if (state.getPoliceCluesRemaining() <= 0) {
            throw new IllegalStateException("Non hai più indizi disponibili.");
        }
        actions.applyPoliceUseClue();
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void policeMoveTo(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        validator.requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);
        validator.requireNotAlreadySearched(targetLocationId);

        actions.applyPoliceMove(targetLocationId);
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }

    public void policeAttemptArrest(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        validator.requireWithinDistance(state.getPolice().getCurrentLocationId(), targetLocationId, GameRules.ARREST_MAX_DISTANCE);
        validator.requireNotAlreadySearched(targetLocationId);

        actions.applyPoliceArrestAttempt(targetLocationId);
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }

    // roaadblock: blocca un intero nodo per il prossimo turno del Killer
    public void policePlaceRoadblock(String targetLocationId) {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        if (state.getPoliceRoadblocksRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Roadblock disponibili.");
        }
        validator.requireExistingLocation(targetLocationId);
        actions.applyPoliceRoadblock(targetLocationId);
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }

    // checkpoint: blocca un singolo collegamento vicino alla propria posizione
    public void policeUseCheckpoint(String fromId, String toId) {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        if (state.getPoliceCheckpointTokensRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Checkpoint Token disponibili.");
        }
        validator.requireNeighbor(fromId, toId, "Il Checkpoint Token blocca solo un collegamento realmente esistente.");
        actions.applyPoliceCheckpoint(fromId, toId);
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }

    // scanner: rivela se il Killer è entro 2 caselle dal punto scelto
    public void policeUseScanner(String centerLocationId) {
        validator.requirePhase(Turn.AWAITING_POLICE_ACTION);
        validator.requireHumanRole(RoleType.POLICE);
        if (state.getPoliceScannerRemaining() <= 0) {
            throw new IllegalStateException("Non hai più letture Scanner disponibili in questo match.");
        }
        validator.requireExistingLocation(centerLocationId);
        actions.applyPoliceScanner(centerLocationId);
        turnPhaseManager.endPoliceTurn();
        turnPhaseManager.resolveAutomaticPhases();
    }
}