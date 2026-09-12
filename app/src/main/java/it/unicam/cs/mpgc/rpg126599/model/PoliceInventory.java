package it.unicam.cs.mpgc.rpg126599.model;

// Simmetrico a KillerInventory: stessa responsabilità, lato Polizia.
public class PoliceInventory {

    private int cluesRemaining;
    private int roadblocksRemaining;
    private int checkpointTokensRemaining;
    private int scannerRemaining;

    public void reset(MatchDifficulty difficulty) {
        this.cluesRemaining = difficulty.getPoliceClues();
        this.roadblocksRemaining = difficulty.getPoliceRoadblocks();
        this.checkpointTokensRemaining = difficulty.getPoliceCheckpoints();
        this.scannerRemaining = 1;
    }

    public int getCluesRemaining() {
        return cluesRemaining;
    }

    public void useClue() {
        cluesRemaining--;
    }

    public int getRoadblocksRemaining() {
        return roadblocksRemaining;
    }

    public void useRoadblock() {
        roadblocksRemaining--;
    }

    public int getCheckpointTokensRemaining() {
        return checkpointTokensRemaining;
    }

    public void useCheckpointToken() {
        checkpointTokensRemaining--;
    }

    public int getScannerRemaining() {
        return scannerRemaining;
    }

    public void useScanner() {
        scannerRemaining--;
    }
}