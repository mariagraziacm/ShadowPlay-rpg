package it.unicam.cs.mpgc.rpg126599.model;

// Responsabilità unica: contabilità dei punti esperienza del match corrente
// e del punteggio informativo della Polizia.
public class XpLedger {

    private int killerXpThisMatch;
    private int policeXpThisMatch;
    private int lastDeltaForKiller;
    private int lastDeltaForPolice;
    private int policeScore = 100;

    public int getKillerXpThisMatch() {
        return killerXpThisMatch;
    }

    public int getPoliceXpThisMatch() {
        return policeXpThisMatch;
    }

    public void addKillerXp(int delta) {
        killerXpThisMatch += delta;
        lastDeltaForKiller = delta;
    }

    public void addPoliceXp(int delta) {
        policeXpThisMatch += delta;
        lastDeltaForPolice = delta;
    }

    public int getLastDelta(RoleType role) {
        return role == RoleType.KILLER ? lastDeltaForKiller : lastDeltaForPolice;
    }

    public void clearLastDelta() {
        lastDeltaForKiller = 0;
        lastDeltaForPolice = 0;
    }

    public void reset() {
        killerXpThisMatch = 0;
        policeXpThisMatch = 0;
        lastDeltaForKiller = 0;
        lastDeltaForPolice = 0;
    }

    public int getPoliceScore() {
        return policeScore;
    }

    public void adjustPoliceScore(int delta) {
        policeScore += delta;
    }
}