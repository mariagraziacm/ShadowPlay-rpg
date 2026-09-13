package it.unicam.cs.mpgc.rpg126599.model;

// Responsabilità unica: tenere il conteggio degli oggetti del Killer e le regole
// di consumo/ricarica legate ad essi. Prima viveva come un fascio di campi dentro GameState.
public class KillerInventory {

    private int fakeCluesRemaining;
    private int smokeBombsRemaining;
    private int trapKitsRemaining;
    private boolean shortcutMapUsed;
    private boolean smokeBombActive;

    // riporta l'inventario ai valori previsti dal bilanciamento del match corrente
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

    // bonus concesso dal tratto Sangue Freddo / dagli arresti falliti della Polizia
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
    


