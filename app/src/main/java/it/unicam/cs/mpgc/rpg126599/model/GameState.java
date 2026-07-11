package it.unicam.cs.mpgc.rpg126599.model;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class GameState {

    private Player killer;
    private Player police;
    private RoleType humanRole;
    private Turn phase = Turn.AWAITING_HOME_CHOICE;

    private String killerHomeLocationId;
    private boolean homeChosen;
    private boolean killerHasLeftHome;

    private int roundsElapsed;
    private int maxRounds = 8;

    private int policeCluesRemaining = 3;
    private int killerFakeCluesRemaining = 2;

    private List<String> eliminatedHomeCandidates = new ArrayList<>();
    private List<Clue> fakeClues = new ArrayList<>();

    private Set<String> visitedByKiller = new LinkedHashSet<>();
    private Set<String> visitedByPolice = new LinkedHashSet<>();

    private boolean finished;
    private RoleType winner;
    private String endReason;

    
    public GameState() {
    }

    public GameState(Player killer, Player police, RoleType humanRole) {
        this.killer = killer;
        this.police = police;
        this.humanRole = humanRole;
        this.visitedByPolice.add(police.getCurrentLocationId());
    }

    public Player getKiller() {
        return killer;
    }

    public Player getPolice() {
        return police;
    }

    public RoleType getHumanRole() {
        return humanRole;
    }

    public Turn getPhase() {
        return phase;
    }

    public void setPhase(Turn phase) {
        this.phase = phase;
    }

    public boolean isHomeChosen() {
        return homeChosen;
    }

    public String getKillerHomeLocationId() {
        return killerHomeLocationId;
    }

    /** Fissa la casa segreta del killer (punto di ritorno). Non sposta ancora il killer. */
    public void chooseHome(String locationId) {
        this.killerHomeLocationId = locationId;
        this.homeChosen = true;
    }

    /** Fissa il luogo del primo omicidio: è la posizione di partenza del killer, diversa da casa. */
    public void setKillerStartLocation(String locationId) {
        this.killer.moveTo(locationId);
        this.visitedByKiller.add(locationId);
    }

    public boolean hasLeftHome() {
        return killerHasLeftHome;
    }

    public void markLeftHome() {
        this.killerHasLeftHome = true;
    }

    public int getRoundsElapsed() {
        return roundsElapsed;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void incrementRound() {
        roundsElapsed++;
    }

    public int getPoliceCluesRemaining() {
        return policeCluesRemaining;
    }

    public void usePoliceClue() {
        policeCluesRemaining--;
    }

    public int getKillerFakeCluesRemaining() {
        return killerFakeCluesRemaining;
    }

    public void useKillerFakeClue() {
        killerFakeCluesRemaining--;
    }

    public List<String> getEliminatedHomeCandidates() {
        return eliminatedHomeCandidates;
    }

    public void eliminateHomeCandidate(String locationId) {
        eliminatedHomeCandidates.add(locationId);
    }

    public List<Clue> getFakeClues() {
        return fakeClues;
    }

    public void addFakeClue(String locationId) {
        fakeClues.add(new Clue(locationId, roundsElapsed));
    }

    public Set<String> getVisitedByKiller() {
        return visitedByKiller;
    }

    public Set<String> getVisitedByPolice() {
        return visitedByPolice;
    }

    public void markKillerVisited(String locationId) {
        visitedByKiller.add(locationId);
    }

    public void markPoliceVisited(String locationId) {
        visitedByPolice.add(locationId);
    }

    public boolean isFinished() {
        return finished;
    }

    public RoleType getWinner() {
        return winner;
    }

    public String getEndReason() {
        return endReason;
    }

    public void finish(RoleType winnerRole, String reason) {
        this.finished = true;
        this.winner = winnerRole;
        this.endReason = reason;
    }

    public Player playerOf(RoleType role) {
        return role == RoleType.KILLER ? killer : police;
    }
}
