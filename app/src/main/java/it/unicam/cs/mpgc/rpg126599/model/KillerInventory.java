package it.unicam.cs.mpgc.rpg126599.model;

// tiwnw il conteggio degli oggetti del Killer e le regole

public class KillerInventory {

    private int fakeCluesRemaining;
    private int smokeBombsRemaining;
    private int trapKitsRemaining;
    private boolean shortcutMapUsed;
    private boolean smokeBombActive;

    public void reset(MatchDifficulty difficulty) {
        this.fakeCluesRemaining = difficulty.getKillerFakeClues();
        this.smokeBombsRemaining = difficulty.getKillerSmokeBombs();
        this.trapKitsRemaining = difficulty.getKillerTrapKits();
        this.shortcutMapUsed = false;
    }

    public int getFakeCluesRemaining() {
        return fakeCluesRemaining;
    }

    public void useFakeClue() {
        fakeCluesRemaining--;
    }

    // bonus concesso dal tratto della Polizia
    public void grantFakeClueBonus() {
        fakeCluesRemaining++;
    }

    public int getSmokeBombsRemaining() {
        return smokeBombsRemaining;
    }

    public void useSmokeBomb() {
        smokeBombsRemaining--;
    }

    public void grantBonusSmokeBomb() {
        smokeBombsRemaining++;
    }

    public int getTrapKitsRemaining() {
        return trapKitsRemaining;
    }

    public void useTrapKit() {
        trapKitsRemaining--;
    }

    public boolean isShortcutMapUsed() {
        return shortcutMapUsed;
    }

    public void markShortcutMapUsed() {
        shortcutMapUsed = true;
    }

    public boolean isSmokeBombActive() {
        return smokeBombActive;
    }

    public void activateSmokeBomb() {
        smokeBombActive = true;
    }

    public void clearSmokeBombActive() {
        smokeBombActive = false;
    }
}