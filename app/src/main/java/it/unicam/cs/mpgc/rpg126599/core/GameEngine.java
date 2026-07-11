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

    /** Crea una nuova partita. Se il killer è automatico, casa e luogo del delitto vengono scelti subito. */
    public static GameEngine newGame(Board board, RoleType humanRole) {
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole);

        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    /** Ricostruisce il motore da una partita caricata da JSON: la fase è già salvata nello stato. */
    public static GameEngine resume(Board board, GameState state) {
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    public GameState getState() {
        return state;
    }

    public Board getBoard() {
        return board;
    }

    // ---------------------------------------------------------------
    // Azioni del killer umano
    // ---------------------------------------------------------------

    /** Prima scelta del killer: la casa segreta, il punto in cui dovrà rientrare per vincere. */
    public void chooseHome(String locationId) {
        requirePhase(Turn.AWAITING_HOME_CHOICE);
        requireHumanRole(RoleType.KILLER);

        applyChooseHome(locationId);
        resolveAutomaticPhases();
    }

    /** Seconda scelta del killer: il luogo del primo omicidio, la sua posizione di partenza (diversa da casa). */
    public void chooseMurderLocation(String locationId) {
        requirePhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
        requireHumanRole(RoleType.KILLER);
        if (locationId.equals(state.getKillerHomeLocationId())) {
            throw new IllegalArgumentException("Il luogo del primo omicidio deve essere diverso dalla tua casa.");
        }

        applyChooseMurderLocation(locationId);
        resolveAutomaticPhases();
    }

    /** Il killer si sposta su una casella collegata direttamente (1 passo) o tramite una intermedia (2 passi). */
    public void killerMove(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        int distance = board.distance(state.getKiller().getCurrentLocationId(), targetLocationId);
        if (distance != 1 && distance != 2) {
            throw new IllegalArgumentException("Puoi spostarti solo su una casella a 1 o 2 passi di distanza.");
        }

        applyKillerMove(targetLocationId);
        resolveAutomaticPhases();
    }

    /** Il killer lascia un indizio falso su una casella diversa dalla propria (al massimo 2 volte a partita). */
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

    // ---------------------------------------------------------------
    // Azioni del poliziotto umano
    // ---------------------------------------------------------------

    /** Usa uno dei 3 indizi per escludere definitivamente una casella come casa del killer. */
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

    /** Il poliziotto si sposta su una casella collegata, senza tentare l'arresto. */
    public void policeMoveTo(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);

        applyPoliceMove(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    /** Il poliziotto tenta l'arresto su una casella collegata: se il killer è lì, vince subito. */
    public void policeAttemptArrest(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        requireNeighbor(state.getPolice().getCurrentLocationId(), targetLocationId);

        applyPoliceArrestAttempt(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    // ---------------------------------------------------------------
    // Applicazione delle azioni (usata sia dai metodi umani sia dalla logica automatica)
    // ---------------------------------------------------------------

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
        return board.all().stream()
                .map(Location::getId)
                .filter(id -> !id.equals(state.getKillerHomeLocationId()))
                .filter(id -> !state.getEliminatedHomeCandidates().contains(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Non ci sono più caselle da escludere."));
    }

    private void applyPoliceMove(String targetLocationId) {
        state.getPolice().moveTo(targetLocationId);
        state.markPoliceVisited(targetLocationId);
    }

    private void applyPoliceArrestAttempt(String targetLocationId) {
        boolean killerIsThere = targetLocationId.equals(state.getKiller().getCurrentLocationId());
        if (killerIsThere) {
            state.finish(RoleType.POLICE, "Il poliziotto ha arrestato il killer.");
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

    // ---------------------------------------------------------------
    // Logica automatica del ruolo non scelto dall'utente: sempre funzioni
    // deterministiche, mai un modello o un'euristica "intelligente".
    // ---------------------------------------------------------------

    private void resolveAutomaticPhases() {
        while (!state.isFinished() && phaseBelongsToAutomaticRole()) {
            switch (state.getPhase()) {
                case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE -> autoSetupKiller();
                case AWAITING_KILLER_ACTION -> autoPlayKillerTurn();
                case AWAITING_POLICE_ACTION -> autoPlayPoliceTurn();
                case GAME_OVER -> {
                    // nulla da fare
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
            case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE, AWAITING_KILLER_ACTION ->
                    automaticRole == RoleType.KILLER;
            case AWAITING_POLICE_ACTION -> automaticRole == RoleType.POLICE;
            case GAME_OVER -> false;
        };
    }

    /**
     * Casa e luogo del primo omicidio scelti a tavolino: la casa è il nodo
     * più lontano dal punto di partenza del poliziotto, il luogo del delitto
     * è un suo vicino (quindi sempre diverso dalla casa, per costruzione).
     * Il metodo è idempotente: se la casa è già stata scelta in una chiamata
     * precedente, sceglie solo il luogo dell'omicidio.
     */
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

    /**
     * Turno del killer automatico: a metà partita piazza un indizio falso
     * (se ne ha ancora), nella prima parte della partita si allontana dalla
     * propria casa, nell'ultima parte torna verso casa.
     */
    private void autoPlayKillerTurn() {
        boolean stillHasFakeClues = state.getKillerFakeCluesRemaining() > 0;
        boolean isMidGame = state.getRoundsElapsed() == state.getMaxRounds() / 2;

        if (stillHasFakeClues && isMidGame) {
            String decoyLocation = board.neighborFarthestFrom(
                    state.getKiller().getCurrentLocationId(),
                    state.getKillerHomeLocationId(),
                    state.getVisitedByKiller()).getId();
            applyKillerFakeClue(decoyLocation);
            return;
        }

        boolean shouldHeadHome = state.getRoundsElapsed() >= state.getMaxRounds() - 2;
        String current = state.getKiller().getCurrentLocationId();
        String target = shouldHeadHome
                ? board.neighborClosestTo(current, state.getKillerHomeLocationId()).getId()
                : board.neighborFarthestFrom(current, state.getKillerHomeLocationId(), state.getVisitedByKiller()).getId();

        applyKillerMove(target);
    }

    /**
     * Turno del poliziotto automatico: segue l'indizio falso più recente non
     * ancora verificato; se non ha piste, ogni due turni usa un indizio per
     * escludere una casa, altrimenti esplora sistematicamente la mappa.
     */
    private void autoPlayPoliceTurn() {
        String current = state.getPolice().getCurrentLocationId();
        Optional<Clue> activeLead = state.getFakeClues().stream()
                .filter(clue -> !clue.isInvestigated())
                .reduce((first, second) -> second);

        if (activeLead.isPresent() && board.isNeighbor(current, activeLead.get().getLocationId())) {
            Clue clue = activeLead.get();
            applyPoliceArrestAttempt(clue.getLocationId());
            clue.setInvestigated(true);
        } else if (activeLead.isPresent()) {
            String next = board.neighborClosestTo(current, activeLead.get().getLocationId()).getId();
            applyPoliceMove(next);
        } else if (state.getPoliceCluesRemaining() > 0 && state.getRoundsElapsed() % 2 == 0) {
            applyPoliceUseClue();
        } else {
            Optional<Location> unvisited = board.firstUnvisitedNeighbor(current, state.getVisitedByPolice());
            String next = unvisited.map(Location::getId).orElseGet(() -> board.randomNeighbor(current).getId());
            applyPoliceMove(next);
        }
        endPoliceTurn();
    }

    // ---------------------------------------------------------------
    // Validazioni
    // ---------------------------------------------------------------

    private void requirePhase(Turn expected) {
        if (state.getPhase() != expected) {
            throw new IllegalStateException("Non è il momento per questa azione.");
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
}

