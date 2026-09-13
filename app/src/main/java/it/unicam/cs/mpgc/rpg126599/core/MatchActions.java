package it.unicam.cs.mpgc.rpg126599.core;

import java.util.List;
import java.util.Random;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameRules;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.Location;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;
import it.unicam.cs.mpgc.rpg126599.model.Turn;

// applica l'effetto di ogni azione di gioco sullo stato

public class MatchActions {

    private final Board board;
    private final GameState state;
    private final XpCalculator xp;
    private final Random random;

    public MatchActions(Board board, GameState state, XpCalculator xp, Random random) {
        this.board = board;
        this.state = state;
        this.xp = xp;
        this.random = random;
    }

    public void applyChooseHome(String locationId) {
        state.chooseHome(locationId);
        state.setPhase(Turn.AWAITING_MURDER_LOCATION_CHOICE);
    }

    public void applyChooseMurderLocation(String locationId) {
        state.setKillerStartLocation(locationId);
        state.markLeftHome();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    public void applyKillerMove(String targetLocationId) {
        boolean wasAlreadyAwayFromHome = state.hasLeftHome();

        state.registerKillerMove();
        state.addKillerXp(xp.tacticalMoveXp(state, RoleType.KILLER, targetLocationId));
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

    public void applyKillerFakeClue(String targetLocationId) {
        state.addFakeClue(targetLocationId);
        state.useKillerFakeClue();
        state.addKillerXp(xp.fakeClueXp());
        afterKillerAction();
        state.setPhase(Turn.AWAITING_POLICE_ACTION);
    }

    public void applyKillerSmokeBomb() {
        state.useKillerSmokeBomb();
        state.activateKillerSmokeBomb();
        afterKillerAction();
    }

    public void applyKillerPlaceTrap(String targetLocationId) {
        state.useKillerTrapKit();
        state.setActiveTrapZone(targetLocationId);
        afterKillerAction();
    }

    public void applyKillerShortcutMap(String targetLocationId) {
        boolean wasAlreadyAwayFromHome = state.hasLeftHome();

        state.registerKillerMove();
        state.addKillerXp(xp.shortcutMoveXp(state, targetLocationId));
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

    private void afterKillerAction() {
        state.clearActiveRoadblock();
        state.clearActiveCheckpoint();
    }

    public void applyPoliceUseClue() {
        String eliminated = pickHomeCandidateToEliminate();
        state.eliminateHomeCandidate(eliminated);
        state.usePoliceClue();
        state.addPoliceXp(xp.policeClueXp());
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

    public void applyPoliceMove(String targetLocationId) {
        if (targetLocationId.equals(state.getActiveTrapZoneLocationId())) {
            
            state.setPoliceStunnedNextTurn(true);
        }
        state.registerPoliceMove();
        state.addPoliceXp(xp.tacticalMoveXp(state, RoleType.POLICE, targetLocationId));
        state.getPolice().moveTo(targetLocationId);
        state.markPoliceVisited(targetLocationId);
    }

    public void applyPoliceArrestAttempt(String targetLocationId) {
        boolean killerIsThere = targetLocationId.equals(state.getKiller().getCurrentLocationId());
        if (killerIsThere) {
            state.finish(RoleType.POLICE, "Il poliziotto ha arrestato il killer.");
            state.addPoliceXp(xp.arrestSuccessXp(state));
        } else {
            // aarresto se sbagliato, il Killer guadagna e la Polizia perde punti.
            state.recordFailedArrest(targetLocationId);
            int penalty = xp.arrestFailurePolicePenalty(state);
            state.adjustPoliceScore(-penalty);
            state.addPoliceXp(-penalty);
            state.addKillerXp(xp.arrestFailureKillerXp(state));
            state.grantKillerArrestFailureBonus();
            if (state.getKillerTraits().contains(Trait.SANGUE_FREDDO)) {
                state.grantKillerBonusSmokeBomb();
            }
        }
    }

    public void applyPoliceRoadblock(String targetLocationId) {
        state.useRoadblock();
        state.setActiveRoadblock(targetLocationId);
    }

    public void applyPoliceCheckpoint(String fromId, String toId) {
        state.useCheckpointToken();
        state.setActiveCheckpoint(fromId, toId);
    }

    public void applyPoliceScanner(String centerLocationId) {
        state.useScanner();

        boolean found;
        int distance;
        if (state.isKillerSmokeBombActive()) {
            found = false;
            distance = 3;
            state.clearKillerSmokeBombActive();
        } else {
            distance = board.distance(centerLocationId, state.getKiller().getCurrentLocationId());
            found = distance <= GameRules.SCANNER_DETECTION_RADIUS;
        }
        state.setLastScannerResult(centerLocationId, found);
        state.addPoliceXp(xp.scannerXp(state, found, distance));
    }
}