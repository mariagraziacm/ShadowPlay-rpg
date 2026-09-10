package it.unicam.cs.mpgc.rpg126599.core;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.Clue;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;
import it.unicam.cs.mpgc.rpg126599.model.MatchDifficulty;
import it.unicam.cs.mpgc.rpg126599.model.Player;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

public class GameEngine {

    private final Board board;
    private final GameState state;
    private final Random random = new Random();

    private GameEngine(Board board, GameState state) {
        this.board = board;
        this.state = state;
    }

    // partita singola "storica" (retrocompatibile): usa comunque il bilanciamento del match 1
    public static GameEngine newGame(Board board, RoleType humanRole) {
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole, MatchDifficulty.MATCH_1);
        GameEngine engine = new GameEngine(board, state);
        engine.resolveAutomaticPhases();
        return engine;
    }

    // avvia un match già preparato dal CampaignManager (bilanciamento e tratti già impostati)
    public static GameEngine newCampaignMatch(Board board, GameState state) {
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

    // ================= AZIONI DEL GIOCATORE UMANO =================

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

    public void killerMove(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);

        String current = state.getKiller().getCurrentLocationId();
        int distance = board.distance(current, targetLocationId);

        if (distance != 1 && distance != 2) {
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

        checkMovementNotBlocked(current, targetLocationId, intermediate);

        if (intermediate != null) {
            state.markKillerVisited(intermediate);
        }

        applyKillerMove(targetLocationId);
        resolveAutomaticPhases();
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

    // Smoke Bomb: copertura per un turno, neutralizza il prossimo Scanner della Polizia
    public void killerUseSmokeBomb() {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        if (state.getKillerSmokeBombsRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Smoke Bomb disponibili.");
        }
        applyKillerSmokeBomb();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
        resolveAutomaticPhases();
    }

    // Trap Kit: piazza una Trap Zone che rallenta la Polizia se ci entra nel turno immediatamente successivo
    public void killerPlaceTrap(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        if (state.getKillerTrapKitsRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Trap Kit disponibili.");
        }
        requireExistingLocation(targetLocationId);
        if (targetLocationId.equals(state.getKiller().getCurrentLocationId())) {
            throw new IllegalArgumentException("Non puoi piazzare la trappola sulla tua stessa posizione.");
        }
        applyKillerPlaceTrap(targetLocationId);
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
        resolveAutomaticPhases();
    }

    // Shortcut Map: una sola volta a match, ignora distanza massima, Roadblock e Checkpoint
    public void killerUseShortcutMap(String targetLocationId) {
        requirePhase(Turn.AWAITING_KILLER_ACTION);
        requireHumanRole(RoleType.KILLER);
        if (state.isKillerShortcutMapUsed()) {
            throw new IllegalStateException("Hai già usato la Shortcut Map in questo match.");
        }
        requireExistingLocation(targetLocationId);
        applyKillerShortcutMap(targetLocationId);
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

    // Roadblock: blocca un intero nodo per il prossimo turno del Killer
    public void policePlaceRoadblock(String targetLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        if (state.getPoliceRoadblocksRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Roadblock disponibili.");
        }
        requireExistingLocation(targetLocationId);
        state.useRoadblock();
        state.setActiveRoadblock(targetLocationId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    // Checkpoint Token (Checkpoint Mobile): blocca un singolo collegamento adiacente alla propria posizione
    public void policeUseCheckpoint(String fromId, String toId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        if (state.getPoliceCheckpointTokensRemaining() <= 0) {
            throw new IllegalStateException("Non hai più Checkpoint Token disponibili.");
        }
        if (!board.isNeighbor(fromId, toId)) {
            throw new IllegalArgumentException("Il Checkpoint Token blocca solo un collegamento realmente esistente.");
        }
        state.useCheckpointToken();
        state.setActiveCheckpoint(fromId, toId);
        endPoliceTurn();
        resolveAutomaticPhases();
    }

    // Scanner: lettura rapida dell'area, rivela se il Killer è entro 2 caselle dal punto scelto
    public void policeUseScanner(String centerLocationId) {
        requirePhase(Turn.AWAITING_POLICE_ACTION);
        requireHumanRole(RoleType.POLICE);
        if (state.getPoliceScannerRemaining() <= 0) {
            throw new IllegalStateException("Non hai più letture Scanner disponibili in questo match.");
        }
        requireExistingLocation(centerLocationId);
        state.useScanner();

        boolean found;
        if (state.isKillerSmokeBombActive()) {
            found = false;
            state.clearKillerSmokeBombActive();
        } else {
            found = board.distance(centerLocationId, state.getKiller().getCurrentLocationId()) <= 2;
        }
        state.setLastScannerResult(centerLocationId, found);

        endPoliceTurn();
        resolveAutomaticPhases();
    }

    // ================= APPLICAZIONE REGOLE =================

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
        afterKillerAction();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyKillerFakeClue(String targetLocationId) {
        state.addFakeClue(targetLocationId);
        state.useKillerFakeClue();
        afterKillerAction();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    private void applyKillerSmokeBomb() {
        state.useKillerSmokeBomb();
        state.activateKillerSmokeBomb();
        afterKillerAction();
    }

    private void applyKillerPlaceTrap(String targetLocationId) {
        state.useKillerTrapKit();
        state.setActiveTrapZone(targetLocationId);
        afterKillerAction();
    }

    private void applyKillerShortcutMap(String targetLocationId) {
        boolean wasAlreadyAwayFromHome = state.hasLeftHome();

        state.getKiller().moveTo(targetLocationId);
        state.markKillerVisited(targetLocationId);

        boolean isAtHomeNow = targetLocationId.equals(state.getKillerHomeLocationId());
        if (!isAtHomeNow) {
            state.markLeftHome();
        } else if (wasAlreadyAwayFromHome) {
            state.finish(RoleType.KILLER, "Il killer è rientrato a casa senza essere scoperto (Shortcut Map).");
        }
        state.markKillerShortcutMapUsed();
        afterKillerAction();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    // Roadblock e Checkpoint durano un solo turno del Killer: si esauriscono sempre qui,
    // che l'azione del Killer li abbia dovuti aggirare oppure no.
    private void afterKillerAction() {
        state.clearActiveRoadblock();
        state.clearActiveCheckpoint();
    }

    private void checkMovementNotBlocked(String current, String target, String intermediate) {
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
        if (targetLocationId.equals(state.getActiveTrapZoneLocationId())) {
            // il Killer aveva armato una Trap Zone qui: la Polizia viene rallentata al prossimo turno
            state.setPoliceStunnedNextTurn(true);
        }
        state.getPolice().moveTo(targetLocationId);
        state.markPoliceVisited(targetLocationId);
    }

    private void applyPoliceArrestAttempt(String targetLocationId) {
        boolean killerIsThere = targetLocationId.equals(state.getKiller().getCurrentLocationId());
        if (killerIsThere) {
            state.finish(RoleType.POLICE, "Il poliziotto ha arrestato il killer.");
        } else {
            // L'arresto non è un tentativo casuale ma una decisione ad alta responsabilità:
            // se sbagliato, il Killer guadagna e la Polizia perde punti.
            state.recordFailedArrest(targetLocationId);
            int penalty = (int) Math.round(15 * state.getDifficulty().getArrestFailurePenaltyMultiplier());
            state.adjustPoliceScore(-penalty);
            state.grantKillerArrestFailureBonus();
            if (state.getKillerTraits().contains(Trait.SANGUE_FREDDO)) {
                state.grantKillerBonusSmokeBomb();
            }
        }
    }

    private void endPoliceTurn() {
        // la Trap Zone e la copertura Smoke Bomb durano al massimo un turno di Polizia:
        // si esauriscono qui, indipendentemente dall'esito dell'azione appena compiuta.
        state.clearActiveTrapZone();
        state.clearKillerSmokeBombActive();

        if (state.isFinished()) {
            state.setPhase(Turn.GAME_OVER);
            return;
        }
        state.incrementRound();
        if (state.isPoliceStunnedNextTurn()) {
            // penalità della Trap Zone: il rallentamento costa alla Polizia un round extra dell'orologio di partita
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

    // ================= LOGICA AUTOMATICA DEL RUOLO NON UMANO =================

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

    private boolean phaseBelongsToAutomaticRole() {
        RoleType automaticRole = state.getHumanRole() == RoleType.KILLER ? RoleType.POLICE : RoleType.KILLER;
        return switch (state.getPhase()) {
            case AWAITING_HOME_CHOICE, AWAITING_MURDER_LOCATION_CHOICE, AWAITING_KILLER_ACTION ->
                    automaticRole == RoleType.KILLER;
            case AWAITING_POLICE_ACTION -> automaticRole == RoleType.POLICE;
            case GAME_OVER -> false;
        };
    }

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