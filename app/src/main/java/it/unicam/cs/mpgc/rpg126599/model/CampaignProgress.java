package it.unicam.cs.mpgc.rpg126599.model;

import java.util.ArrayList;
import java.util.List;

// Responsabilità unica: lo stato della campagna Best of 3 (vittorie, Xp accumulata,
// bilanciamento del match corrente, tratti sbloccati). Prima era mischiata dentro GameState
// insieme a turno, inventari ed effetti tattici.
public class CampaignProgress {

    private MatchDifficulty difficulty = MatchDifficulty.MATCH_1;
    private boolean inProgress;
    private int currentMatchNumber = 1;
    private int killerWins;
    private int policeWins;
    private int killerXp;
    private int policeXp;

    private List<Trait> killerTraits = new ArrayList<>();
    private List<Trait> policeTraits = new ArrayList<>();

    public MatchDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(MatchDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public int getCurrentMatchNumber() {
        return currentMatchNumber;
    }

    public int getKillerWins() {
        return killerWins;
    }

    public int getPoliceWins() {
        return policeWins;
    }

    public int getKillerXp() {
        return killerXp;
    }

    public int getPoliceXp() {
        return policeXp;
    }

    public void setProgress(int currentMatchNumber, int killerWins, int policeWins, int killerXp, int policeXp) {
        this.inProgress = true;
        this.currentMatchNumber = currentMatchNumber;
        this.killerWins = killerWins;
        this.policeWins = policeWins;
        this.killerXp = killerXp;
        this.policeXp = policeXp;
    }

    public void clear() {
        this.inProgress = false;
        this.currentMatchNumber = 1;
        this.killerWins = 0;
        this.policeWins = 0;
        this.killerXp = 0;
        this.policeXp = 0;
    }

    public List<Trait> getKillerTraits() {
        return killerTraits;
    }

    public void setKillerTraits(List<Trait> traits) {
        this.killerTraits = traits;
    }

    public List<Trait> getPoliceTraits() {
        return policeTraits;
    }

    public void setPoliceTraits(List<Trait> traits) {
        this.policeTraits = traits;
    }
}