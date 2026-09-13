package it.unicam.cs.mpgc.rpg126599.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Responsabilità unica: la fase di gioco corrente, i round trascorsi, la casa del Killer
// e le tracce di ciò che è già stato visitato/tentato nel match. Prima era mischiata
// dentro GameState insieme a inventari, effetti tattici e progressione di campagna.
public class TurnState {

    private Turn phase = Turn.AWAITING_HOME_CHOICE;

    private String killerHomeLocationId;
    private boolean homeChosen;
    private boolean killerHasLeftHome;

    private int roundsElapsed;
    private int maxRounds = GameRules.MAX_ROUNDS;

    private int killerMovesMade;
    private int policeMovesMade;

    private List<String> eliminatedHomeCandidates = new ArrayList<>();
    private List<String> failedArrestLocations = new ArrayList<>();
    private List<Clue> fakeClues = new ArrayList<>();

    private Set<String> visitedByKiller = new LinkedHashSet<>();
    private Set<String> visitedByPolice = new LinkedHashSet<>();

    private boolean finished;
    private RoleType winner;
    private String endReason;

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

    public void chooseHome(String locationId) {
        this.killerHomeLocationId = locationId;
        this.homeChosen = true;
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

    public int getKillerMovesMade() {
        return killerMovesMade;
    }

    public void registerKillerMove() {
        killerMovesMade++;
    }

    public int getPoliceMovesMade() {
        return policeMovesMade;
    }

    public void registerPoliceMove() {
        policeMovesMade++;
    }

    public List<String> getEliminatedHomeCandidates() {
        return eliminatedHomeCandidates;
    }

    public void eliminateHomeCandidate(String locationId) {
        eliminatedHomeCandidates.add(locationId);
    }

    public List<String> getFailedArrestLocations() {
        return failedArrestLocations;
    }

    public void recordFailedArrest(String locationId) {
        failedArrestLocations.add(locationId);
    }

    public boolean isAlreadySearched(String locationId) {
        return failedArrestLocations.contains(locationId);
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
}