package it.unicam.cs.mpgc.rpg126599.core;

import java.util.List;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;

// formule di ricompensa/penalità Xp

public class XpCalculator {

    private static final int BASE_MOVE_XP = 6;
    private static final int KILLER_DISTANCE_FROM_POLICE_BONUS = 5;
    private static final int POLICE_PROXIMITY_TO_KILLER_BONUS = 6;
    private static final int SHORTCUT_MAP_BONUS = 8;

    private static final int FAKE_CLUE_XP = -4;
    private static final int POLICE_CLUE_XP = -5;

    private static final int ARREST_SUCCESS_XP = 20;
    private static final int ARREST_FAILURE_KILLER_XP = 12;
    private static final int ARREST_FAILURE_BASE_PENALTY = 15;

    private static final int SCANNER_FOUND_BASE_XP = 12;
    private static final int SCANNER_FOUND_DISTANCE_BONUS = 7;
    private static final int SCANNER_MISS_BASE_XP = 4;
    private static final int SCANNER_MISS_DISTANCE_BONUS = 4;

    private final Board board;

    public XpCalculator(Board board) {
        this.board = board;
    }

    public int traitBonus(GameState state, RoleType role) {
        List<Trait> traits = role == RoleType.KILLER ? state.getKillerTraits() : state.getPoliceTraits();
        return traits.stream()
                .filter(trait -> trait.getRole() == role)
                .mapToInt(Trait::getMatchXpBonus)
                .sum();
    }

    public int tacticalMoveXp(GameState state, RoleType role, String targetLocationId) {
        int traitBonus = traitBonus(state, role);
        if (role == RoleType.KILLER) {
            int distanceFromPolice = board.distance(targetLocationId, state.getPolice().getCurrentLocationId());
            return BASE_MOVE_XP + Math.max(0, distanceFromPolice - 1) * KILLER_DISTANCE_FROM_POLICE_BONUS + traitBonus / 2;
        }
        int distanceToKiller = board.distance(targetLocationId, state.getKiller().getCurrentLocationId());
        return BASE_MOVE_XP + Math.max(0, 4 - distanceToKiller) * POLICE_PROXIMITY_TO_KILLER_BONUS + traitBonus / 2;
    }

    public int shortcutMoveXp(GameState state, String targetLocationId) {
        return tacticalMoveXp(state, RoleType.KILLER, targetLocationId) + SHORTCUT_MAP_BONUS;
    }

    public int fakeClueXp() {
        return FAKE_CLUE_XP;
    }

    public int policeClueXp() {
        return POLICE_CLUE_XP;
    }

    public int arrestSuccessXp(GameState state) {
        return ARREST_SUCCESS_XP + traitBonus(state, RoleType.POLICE) / 2;
    }

    public int arrestFailureKillerXp(GameState state) {
        return ARREST_FAILURE_KILLER_XP + traitBonus(state, RoleType.KILLER) / 2;
    }

    public int arrestFailurePolicePenalty(GameState state) {
        return (int) Math.round(ARREST_FAILURE_BASE_PENALTY * state.getDifficulty().getArrestFailurePenaltyMultiplier());
    }

    public int scannerXp(GameState state, boolean found, int distance) {
        return found
                ? SCANNER_FOUND_BASE_XP + Math.max(0, 2 - distance) * SCANNER_FOUND_DISTANCE_BONUS + traitBonus(state, RoleType.POLICE) / 2
                : SCANNER_MISS_BASE_XP + Math.max(0, 2 - distance) * SCANNER_MISS_DISTANCE_BONUS;
    }
}