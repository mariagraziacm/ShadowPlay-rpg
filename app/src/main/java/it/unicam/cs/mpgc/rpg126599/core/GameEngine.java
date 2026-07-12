package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Comparator;
import java.util.Optional;
import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;
import it.unicam.cs.mpgc.rpg126599.model.Player;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

public class GameEngine {

    private final Board board;
    private final GameState state;

    private GameEngine(Board board, GameState state) {
        this.board = board;
        this.state = state;
    }

    public static GameEngine newGame(Board board, RoleType humanRole) {
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole);
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    public static GameEngine resume(Board board, GameState state) {
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    public GameState getState() { return state; }
    public Board getBoard() { return board; }

    public void chooseHome(String locationId) {
        requirePhase(Turn.AWAITING_HOME_CHOICE);
        requireHumanRole(RoleType.KILLER);
        applyChooseHome(locationId);
        resolveAutomaticPhases();
    }

    public void chooseMurderLocation(String locationId) {
        requirePhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
        requireHumanRole(RoleType.KILLER);
        if (locationId.equals(state.getKillerHomeLocationId())) {
            throw new IllegalArgumentException("Il luogo dell'omicidio deve essere diverso da casa.");
        }
        applyChooseMurderLocation(locationId);
        resolveAutomaticPhases();
    }

    /** MOVIMENTO KILLER UMANO (Supporta 1 e 2 passi) */
    public void killerMove(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        
        String current = state.getKiller().getCurrentLocationId();
        int distance = board.distance(current, targetLocationId);
        
        if (distance != 1 && distance != 2) {
            throw new IllegalArgumentException("Puoi spostarti solo di 1 o 2 caselle.");
        }

        // Se fa 2 passi, cerchiamo e segnamo la casella di mezzo
        if (distance == 2) {
            board.neighborsOf(current).stream()
                    .filter(n -> board.isNeighbor(n.getId(), targetLocationId))
                    .findFirst()
                    .ifPresent(inter -> state.markKillerVisited(inter.getId()));
        }

        applyKillerMove(targetLocationId);
        resolveAutomaticPhases();
    }

    public void killerLeaveFakeClue(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        if (state.getKillerFakeCluesRemaining() <= 0) throw new IllegalStateException("Finiti indizi falsi.");
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) throw new IllegalArgumentException("Non sulla tua posizione.");
        applyKillerFakeClue(targetLocationId);
        resolveAutomaticPhases();
    }

    public void policeUseClue() {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        if (state.getPoliceCluesRemaining() <= 0) throw new IllegalStateException("Finiti indizi.");
        applyPoliceUseClue();
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    public void policeMoveTo(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);
        applyPoliceMove(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    public void policeAttemptArrest(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);
        applyPoliceArrestAttempt(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    private void applyChooseHome(String locationId) {
        state.chooseHome(locationId);
        state.setPhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
    }

    private void applyChooseMurderLocation(String locationId) {
        state.setKillerStartLocation(locationId);
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyKillerMove(String targetLocationId) {
        boolean wasAlreadyAwayFromHome = state.hasLeftHome();
        state.getKiller().moveTo(targetLocationId);
        state.markKillerVisited(targetLocationId);

        if (!targetLocationId.equals(state.getKillerHomeLocationId())) {
            state.markLeftHome();
        } else if (wasAlreadyAwayFromHome) {
            state.finish(RoleType.KILLER, "Il killer è rientrato a casa.");
        }
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyKillerFakeClue(String targetLocationId) {
        state.addFakeClue(targetLocationId);
        state.useKillerFakeClue();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyPoliceUseClue() {
        String eliminated = pickHomeCandidateToEliminate();
        state.eliminateHomeCandidate(eliminated);
        state.usePoliceClue();
    }

    private String pickHomeCandidateToEliminate() {
        return board.all().stream()
                .map(Location::getId)
                .filter(id -> !id.equals(state.getKillerHomeLocationId()))
                .filter(id -> !state.getEliminatedHomeCandidates().contains(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nessuna casella da escludere."));
    }

    private void applyPoliceMove(String targetLocationId) {
        state.getPolice().moveTo(targetLocationId);
        state.markPoliceVisited(targetLocationId);
    }

    private void applyPoliceArrestAttempt(String targetLocationId) {
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) {
            state.finish(RoleType.POLICE, "Killer arrestato.");
        }
    }

    private void endPoliceTurn() {
        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.incrementRound();
        if (state.getRoundsElapsed() >= state.getMaxRounds()) {
            state.finish(RoleType.KILLER, "Tempo scaduto.");
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.setPhase(Turn.AWAITING_KILLER_ACTION);
    }

    // ---------------------------------------------------------------
    // GESTIONE TURNI AUTOMATICI (SEMPLIFICATA E ANTI-BLOCCO)
    // ---------------------------------------------------------------

    private void resolveAutomaticPhases() {
        int safety = 0;
        // Massimo 5 passaggi automatici di fila per evitare loop infiniti
        while (!state.isFinished() && phaseBelongsToAutomaticRole() && safety < 5) {
            safety++;
            try {
                switch (state.getPhase()) {
                    case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE -> autoSetupKiller();
                    case AWAITING_KILLER_ACTION -> autoPlayKillerTurn();
                    case AWAITING_POLICE_ACTION -> autoPlayPoliceTurn();
                    case GAME_OVER -> {}
                }
            } catch (Exception e) {
                // Se il bot fallisce o va in errore, sblocchiamo forzatamente il turno
                if (state.getPhase() == Turn.AWAITING_POLICE_ACTION) {
                    endPoliceTurn(); 
                } else {
                    state.setPhase(Turn.AWAITING_POLICE_ACTION);
                }
            }
        }
        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
        }
    }

    private boolean phaseBelongsToAutomaticRole() {
        RoleType automaticRole = state.getHumanRole() == RoleType.KILLER ? RoleType.POLICE : RoleType.KILLER;
        return switch (state.getPhase()) {
            case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE, AWAITING_KILLER_ACTION -> automaticRole == RoleType.KILLER;
            case AWAITING_POLICE_ACTION -> automaticRole == RoleType.POLICE;
            case GAME_OVER -> false;
        };
    }

    private void autoSetupKiller() {
        if (!state.isHomeChosen()) {
            String policeStart = state.getPolice().getCurrentLocationId();
            String home = board.all().stream()
                    .max(Comparator.comparingInt(loc -> board.distance(loc.getId(), policeStart)))
                    .map(Location::getId).orElse("n1");
            applyChooseHome(home);
        }
        String murderLocation = board.neighborsOf(state.getKillerHomeLocationId()).get(0).getId();
        applyChooseMurderLocation(murderLocation);
    }

    /** BOT KILLER AUTOMATICO (Fa sempre 1 o 2 passi in base alla distanza) */
    private void autoPlayKillerTurn() {
        String current = state.getKiller().getCurrentLocationId();
        boolean shouldHeadHome = state.getRoundsElapsed() >= state.getMaxRounds() - 2;
        String targetHome = state.getKillerHomeLocationId();

        String target = board.all().stream()
                .filter(loc -> {
                    int d = board.distance(current, loc.getId());
                    return d == 1 || d == 2;
                })
                .min(Comparator.comparingInt(loc -> {
                    int d = board.distance(loc.getId(), targetHome);
                    return shouldHeadHome ? d : -d; // Se scappa massimizza la distanza, se rientra la minimizza
                }))
                .map(Location::getId).orElse(current);

        // Applica il passo intermedio se si muove di 2
        if (board.distance(current, target) == 2) {
            board.neighborsOf(current).stream()
                    .filter(n -> board.isNeighbor(n.getId(), target))
                    .findFirst()
                    .ifPresent(inter -> state.markKillerVisited(inter.getId()));
        }

        applyKillerMove(target);
    }

    /** BOT POLIZIA AUTOMATICA (Si muove solo sui vicini liberi) */
    private void autoPlayPoliceTurn() {
        String current = state.getPolice().getCurrentLocationId();
        Optional<Location> unvisited = board.firstUnvisitedNeighbor(current, state.getVisitedByPolice());
        String next = unvisited.map(Location::getId).orElseGet(() -> board.randomNeighbor(current).getId());
        
        applyPoliceMove(next);
        endPoliceTurn();
    }

    private void requirePhase(Turn expected) {
        if (state.getPhase() != expected) throw new IllegalStateException("Fase errata.");
    }

    private void requireHumanRole(RoleType expected) {
        if (state.getHumanRole() != expected) throw new IllegalStateException("Azione non tua.");
    }

    private void requireNeighbor(String fromId, String toId) {
        if (!board.isNeighbor(fromId, toId)) throw new IllegalArgumentException("Non adiacente.");
    }
}