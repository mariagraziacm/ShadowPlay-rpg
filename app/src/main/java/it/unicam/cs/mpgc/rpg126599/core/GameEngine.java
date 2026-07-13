package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;
import it.unicam.cs.mpgc.rpg126599.model.Player;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Turn;
// motore del gioco che applica le regole, controlla le azioni del giocatore umano e gestisce il turno automatico del ruolo che non viene selezionato dall'utente
public class GameEngine {

    private final Board board;
    private final GameState state;
    private final Random random = new Random();

    private GameEngine(Board board, GameState state) {
        this.board = board;
        this.state = state;
    }
// inizia nuova partita
    public static GameEngine newGame(Board board, RoleType humanRole) {
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole);
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }
// riprende partita salvata
    public static GameEngine resume(Board board, GameState state) {
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    public GameState getState() { return state; }
    public Board getBoard() { return board; }

    // LOGICA PER UTENTE: azioni di gioco che possono essere fatte dal giocatore umano
// quando il giocatore umano sceglie di giocare come killer deve scegliere il proprio nascondiglio e il luogo dell'omicidio
    public void chooseHome(String locationId) {
        requirePhase(Turn.AWAITING_HOME_CHOICE);
        requireHumanRole(RoleType.KILLER);
        requireExistingLocation(locationId);
        applyChooseHome(locationId);
        resolveAutomaticPhases();
    }

    public void chooseMurderLocation(String locationId) {
        requirePhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
        requireHumanRole(RoleType.KILLER);
        requireExistingLocation(locationId);
        if (locationId.equals(state.getKillerHomeLocationId())) {
            throw new IllegalArgumentException("Il luogo dell'omicidio deve essere diverso da casa.");
        }
        applyChooseMurderLocation(locationId);
        resolveAutomaticPhases();
    }
// il killer può scegliere di spostarsi di una o due casella alla volta per turno
    public void killerMove(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);

        String current = state.getKiller().getCurrentLocationId();
        int distance = board.distance(current, targetLocationId);

        if (distance != 1 && distance != 2) {
            throw new IllegalArgumentException("Puoi spostarti solo di 1 o 2 caselle.");
        }

        if (distance == 2) {
            board.neighborsOf(current).stream()
                    .filter(n -> board.isNeighbor(n.getId(), targetLocationId))
                    .findFirst()
                    .ifPresent(inter -> state.markKillerVisited(inter.getId()));
        }

        applyKillerMove(targetLocationId);
        resolveAutomaticPhases(); // gestione automatica del turno del poliziotto
    }

    public void killerLeaveFakeClue(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        if (state.getKillerFakeCluesRemaining() <= 0) {
            throw new IllegalStateException("Non hai più indizi falsi disponibili.");
        }
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) {
            throw new IllegalArgumentException("Non puoi lasciare l'indizio sulla tua stessa posizione.");
        }

        applyKillerFakeClue(targetLocationId);
        resolveAutomaticPhases();
    }

    public void policeUseClue() {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        if (state.getPoliceCluesRemaining() <= 0) {
            throw new IllegalStateException("Non hai più indizi disponibili.");
        }
        applyPoliceUseClue();
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    public void policeMoveTo(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);
        requireNotAlreadySearched(targetLocationId);

        applyPoliceMove(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    public void policeAttemptArrest(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);
        requireNotAlreadySearched(targetLocationId);

        applyPoliceArrestAttempt(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    // regole di gioco vengono applicate modificando così anche lo stato del gioco in base alle sclete fatte durante i turni

    private void applyChooseHome(String locationId) {
        state.chooseHome(locationId);
        state.setPhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
    }

    private void applyChooseMurderLocation(String locationId) {
        state.setKillerStartLocation(locationId);
        state.markLeftHome();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyKillerMove(String targetLocationId) {
        boolean wasAlreadyAwayFromHome = state.hasLeftHome();

        state.getKiller().moveTo(targetLocationId);
        state.markKillerVisited(targetLocationId);

        boolean isAtHomeNow = targetLocationId.equals(state.getKillerHomeLocationId());
        if (!isAtHomeNow) {
            state.markLeftHome();
        } else if (wasAlreadyAwayFromHome) {
            state.finish(RoleType.KILLER, "Il killer è rientrato a casa senza essere scoperto.");
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
        List<String> candidates = board.all().stream()
                .map(Location::getId)
                .filter(id -> !id.equals(state.getKillerHomeLocationId()))
                .filter(id -> !state.getEliminatedHomeCandidates().contains(id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Non ci sono più caselle da escludere.");
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private void applyPoliceMove(String targetLocationId) {
        state.getPolice().moveTo(targetLocationId);
        state.markPoliceVisited(targetLocationId);
    }

    private void applyPoliceArrestAttempt(String targetLocationId) {
        boolean killerIsThere = targetLocationId.equals(state.getKiller().getCurrentLocationId());
        if (killerIsThere) {
            state.finish(RoleType.POLICE, "Il poliziotto ha arrestato il killer.");
        } else {
            state.recordFailedArrest(targetLocationId);
        }
    }

    private void endPoliceTurn() {
        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.incrementRound();
        if (state.getRoundsElapsed() >= state.getMaxRounds()) {
            state.finish(RoleType.KILLER, "Tempo scaduto: il killer sfugge alla cattura.");
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.setPhase(Turn.AWAITING_KILLER_ACTION);
    }

    // Logica di gioco automatica del ruolo non selezionato dal giocatore umano
     // quando il turno del giocatore umano termina fa muovere il ruolo automatico
    private void resolveAutomaticPhases() {
        while (!state.isFinished() && phaseBelongsToAutomaticRole()) {
            switch (state.getPhase()) {
                case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE -> autoSetupKiller();
                case AWAITING_KILLER_ACTION -> autoPlayKillerTurn();
                case AWAITING_POLICE_ACTION -> autoPlayPoliceTurn();
                case GAME_OVER -> { }
            }
        }
        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
        }
    }
//controllo se la fase appartiene al ruolo automatico
    private boolean phaseBelongsToAutomaticRole() {
        RoleType automaticRole = state.getHumanRole() == RoleType.KILLER ? RoleType.POLICE : RoleType.KILLER;
        return switch (state.getPhase()) {
            case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE, AWAITING_KILLER_ACTION ->
                    automaticRole == RoleType.KILLER;
            case AWAITING_POLICE_ACTION -> automaticRole == RoleType.POLICE;
            case GAME_OVER -> false;
        };
    }
// se il ruolo automatico deve giocare come killer setta nascondiglio e primo luogo dell'omicidio
    private void autoSetupKiller() {
        if (!state.isHomeChosen()) {
            String policeStart = state.getPolice().getCurrentLocationId();
            String home = board.all().stream()
                    .max(Comparator.comparingInt(location -> board.distance(location.getId(), policeStart)))
                    .map(Location::getId)
                    .orElseThrow();
            applyChooseHome(home);
        }
        String murderLocation = board.neighborsOf(state.getKillerHomeLocationId()).get(0).getId();
        applyChooseMurderLocation(murderLocation);
    }
// movimento del killer automatico e strategie appliacte dal killer automatico
    private void autoPlayKillerTurn() {
        boolean stillHasFakeClues = state.getKillerFakeCluesRemaining() > 0;
        boolean isMidGame = state.getRoundsElapsed() == state.getMaxRounds() / 2;

        String current = state.getKiller().getCurrentLocationId();

        if (stillHasFakeClues && isMidGame) {
            String decoyLocation = findFarthestNeighbor(current, state.getKillerHomeLocationId(), state.getVisitedByKiller());
            applyKillerFakeClue(decoyLocation);
            return;
        }

        boolean shouldHeadHome = state.getRoundsElapsed() >= state.getMaxRounds() - 2;
        String target = shouldHeadHome
                ? findClosestNeighbor(current, state.getKillerHomeLocationId())
                : findFarthestNeighbor(current, state.getKillerHomeLocationId(), state.getVisitedByKiller());

        applyKillerMove(target);
    }
//movimento del poliziotto automatico e strategie usate dal poliziotto automatico
    private void autoPlayPoliceTurn() {
        String current = state.getPolice().getCurrentLocationId();
        Optional<Clue> activeLead = state.getFakeClues().stream()
                .filter(clue -> !clue.isInvestigated())
                .filter(clue -> !state.isAlreadySearched(clue.getLocationId()))
                .reduce((first, second) -> second);

        if (activeLead.isPresent() && board.isNeighbor(current, activeLead.get().getLocationId())) {
            Clue clue = activeLead.get();
            applyPoliceArrestAttempt(clue.getLocationId());
            clue.setInvestigated(true);
        } else if (activeLead.isPresent()) {
            String next = findClosestNeighbor(current, activeLead.get().getLocationId());
            applyPoliceMove(next);
        } else if (state.getPoliceCluesRemaining() > 0 && state.getRoundsElapsed() % 2 == 0) {
            applyPoliceUseClue();
        } else {
            Optional<Location> unvisited = board.neighborsOf(current).stream()
                    .filter(location -> !state.getVisitedByPolice().contains(location.getId()))
                    .findFirst();

            String next = unvisited.map(Location::getId).orElseGet(() -> {
                List<Location> neighbors = board.neighborsOf(current);
                return neighbors.get(random.nextInt(neighbors.size())).getId();
            });
            applyPoliceMove(next);
        }
        endPoliceTurn();
    }

    private String findClosestNeighbor(String fromId, String targetId) {
        return board.neighborsOf(fromId).stream()
                .min(Comparator.comparingInt(candidate -> board.distance(candidate.getId(), targetId)))
                .map(Location::getId)
                .orElse(fromId);
    }

    private String findFarthestNeighbor(String fromId, String avoidId, java.util.Set<String> alreadyVisited) {
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


    private void requirePhase(Turn expected) {
        if (state.getPhase() != expected) {
            throw new IllegalStateException("Non è il momento per questa azione. Fase attuale: " + state.getPhase());
        }
    }

    private void requireHumanRole(RoleType expected) {
        if (state.getHumanRole() != expected) {
            throw new IllegalStateException("Questa azione non ti spetta.");
        }
    }

    private void requireNeighbor(String fromId, String toId) {
        if (!board.isNeighbor(fromId, toId)) {
            throw new IllegalArgumentException("Puoi agire solo su una casella collegata alla tua.");
        }
    }

    private void requireNotAlreadySearched(String locationId) {
        if (state.isAlreadySearched(locationId)) {
            throw new IllegalArgumentException("Hai già cercato in questa casella: scegline un'altra.");
        }
    }

    private void requireExistingLocation(String locationId) {
        try {
            board.get(locationId);
        } catch (java.util.NoSuchElementException e) {
            throw new IllegalArgumentException("Casella inesistente sulla mappa: " + locationId);
        }
    }
}