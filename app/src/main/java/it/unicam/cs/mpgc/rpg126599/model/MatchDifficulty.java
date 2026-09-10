package it.unicam.cs.mpgc.rpg126599.model;

// Bilanciamento dei 3 match della campagna Best of 3.
// Vincolo di design: per ciascun ruolo la somma degli oggetti "a scorta" (esclusi Shortcut Map
// e Scanner, che sono abilità singole da 1 uso a match) non supera mai 4 — il cap richiesto.
public enum MatchDifficulty {

    // killerFakeClues, killerSmokeBombs, killerTrapKits, policeClues, policeRoadblocks, policeCheckpoints, penalitàArrestoSbagliato
    MATCH_1(2, 1, 1, 2, 1, 1, 1.0),
    MATCH_2(1, 1, 1, 3, 1, 0, 1.25),
    MATCH_3(1, 0, 1, 2, 2, 0, 1.6);

    private final int killerFakeClues;
    private final int killerSmokeBombs;
    private final int killerTrapKits;
    private final int policeClues;
    private final int policeRoadblocks;
    private final int policeCheckpoints;
    private final double arrestFailurePenaltyMultiplier;

    MatchDifficulty(int killerFakeClues, int killerSmokeBombs, int killerTrapKits,
                     int policeClues, int policeRoadblocks, int policeCheckpoints,
                     double arrestFailurePenaltyMultiplier) {
        this.killerFakeClues = killerFakeClues;
        this.killerSmokeBombs = killerSmokeBombs;
        this.killerTrapKits = killerTrapKits;
        this.policeClues = policeClues;
        this.policeRoadblocks = policeRoadblocks;
        this.policeCheckpoints = policeCheckpoints;
        this.arrestFailurePenaltyMultiplier = arrestFailurePenaltyMultiplier;
    }

    public int getKillerFakeClues() { return killerFakeClues; }
    public int getKillerSmokeBombs() { return killerSmokeBombs; }
    public int getKillerTrapKits() { return killerTrapKits; }
    public int getPoliceClues() { return policeClues; }
    public int getPoliceRoadblocks() { return policeRoadblocks; }
    public int getPoliceCheckpoints() { return policeCheckpoints; }
    public double getArrestFailurePenaltyMultiplier() { return arrestFailurePenaltyMultiplier; }

    public static MatchDifficulty forMatchNumber(int matchNumber) {
        return switch (matchNumber) {
            case 1 -> MATCH_1;
            case 2 -> MATCH_2;
            default -> MATCH_3;
        };
    }
}