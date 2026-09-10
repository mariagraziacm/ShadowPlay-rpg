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
    private List<String> failedArrestLocations = new ArrayList<>();
    private List<Clue> fakeClues = new ArrayList<>();

    private Set<String> visitedByKiller = new LinkedHashSet<>();
    private Set<String> visitedByPolice = new LinkedHashSet<>();

    private boolean finished;
    private RoleType winner;
    private String endReason;

    // ---- Campagna Best of 3 / bilanciamento ----
    private MatchDifficulty difficulty = MatchDifficulty.MATCH_1;

    // ---- Tratti RPG ----
    private List<Trait> killerTraits = new ArrayList<>();
    private List<Trait> policeTraits = new ArrayList<>();

    // ---- Inventario Killer aggiuntivo ----
    private int killerSmokeBombsRemaining;
    private int killerTrapKitsRemaining;
    private boolean killerShortcutMapUsed;
    private boolean killerSmokeBombActive;

    // ---- Inventario Poliziotto aggiuntivo ----
    private int policeRoadblocksRemaining;
    private int policeCheckpointTokensRemaining;
    private int policeScannerRemaining;

    // ---- Trap Zone attiva (Killer) ----
    private String activeTrapZoneLocationId;
    private boolean policeStunnedNextTurn;

    // ---- Roadblock attivo (Poliziotto) ----
    private String activeRoadblockLocationId;

    // ---- Checkpoint Token attivo: blocca un singolo collegamento (Poliziotto) ----
    private String activeCheckpointFromId;
    private String activeCheckpointToId;

    // ---- Ultimo esito Scanner (per la UI) ----
    private String lastScannerCenterId;
    private boolean lastScannerFoundKiller;

    // ---- Punteggio informativo: cala se la Polizia sbaglia un arresto ----
    private int policeScore = 100;

    public GameState() {
    }

    public GameState(Player killer, Player police, RoleType humanRole) {
        this.killer = killer;
        this.police = police;
        this.humanRole = humanRole;
        this.visitedByPolice.add(police.getCurrentLocationId());
    }

    public GameState(Player killer, Player police, RoleType humanRole, MatchDifficulty difficulty) {
        this(killer, police, humanRole);
        applyDifficulty(difficulty);
    }

    // applica il bilanciamento di un match della campagna, azzerando gli oggetti "una tantum"
    public void applyDifficulty(MatchDifficulty difficulty) {
        this.difficulty = difficulty;
        this.killerFakeCluesRemaining = difficulty.getKillerFakeClues();
        this.killerSmokeBombsRemaining = difficulty.getKillerSmokeBombs();
        this.killerTrapKitsRemaining = difficulty.getKillerTrapKits();
        this.killerShortcutMapUsed = false;
        this.policeCluesRemaining = difficulty.getPoliceClues();
        this.policeRoadblocksRemaining = difficulty.getPoliceRoadblocks();
        this.policeCheckpointTokensRemaining = difficulty.getPoliceCheckpoints();
        this.policeScannerRemaining = 1;
    }

    public MatchDifficulty getDifficulty() {
        return difficulty;
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

    public void chooseHome(String locationId) {
        this.killerHomeLocationId = locationId;
        this.homeChosen = true;
    }

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

    public Player playerOf(RoleType role) {
        return role == RoleType.KILLER ? killer : police;
    }

    // ---------------- Tratti ----------------

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

    // ---------------- Inventario Killer aggiuntivo ----------------

    public int getKillerSmokeBombsRemaining() {
        return killerSmokeBombsRemaining;
    }

    public void useKillerSmokeBomb() {
        killerSmokeBombsRemaining--;
    }

    // bonus concesso dal tratto Sangue Freddo quando la Polizia sbaglia un arresto
    public void grantKillerBonusSmokeBomb() {
        killerSmokeBombsRemaining++;
    }

    public int getKillerTrapKitsRemaining() {
        return killerTrapKitsRemaining;
    }

    public void useKillerTrapKit() {
        killerTrapKitsRemaining--;
    }

    public boolean isKillerShortcutMapUsed() {
        return killerShortcutMapUsed;
    }

    public void markKillerShortcutMapUsed() {
        killerShortcutMapUsed = true;
    }

    public boolean isKillerSmokeBombActive() {
        return killerSmokeBombActive;
    }

    public void activateKillerSmokeBomb() {
        killerSmokeBombActive = true;
    }

    public void clearKillerSmokeBombActive() {
        killerSmokeBombActive = false;
    }

    // guadagno base del Killer quando la Polizia sbaglia un arresto
    public void grantKillerArrestFailureBonus() {
        killerFakeCluesRemaining++;
    }

    // ---------------- Inventario Poliziotto aggiuntivo ----------------

    public int getPoliceRoadblocksRemaining() {
        return policeRoadblocksRemaining;
    }

    public void useRoadblock() {
        policeRoadblocksRemaining--;
    }

    public int getPoliceCheckpointTokensRemaining() {
        return policeCheckpointTokensRemaining;
    }

    public void useCheckpointToken() {
        policeCheckpointTokensRemaining--;
    }

    public int getPoliceScannerRemaining() {
        return policeScannerRemaining;
    }

    public void useScanner() {
        policeScannerRemaining--;
    }

    // ---------------- Trap Zone (Killer) ----------------

    public String getActiveTrapZoneLocationId() {
        return activeTrapZoneLocationId;
    }

    public void setActiveTrapZone(String locationId) {
        this.activeTrapZoneLocationId = locationId;
    }

    public void clearActiveTrapZone() {
        this.activeTrapZoneLocationId = null;
    }

    public boolean isPoliceStunnedNextTurn() {
        return policeStunnedNextTurn;
    }

    public void setPoliceStunnedNextTurn(boolean value) {
        this.policeStunnedNextTurn = value;
    }

    // ---------------- Roadblock (Poliziotto) ----------------

    public String getActiveRoadblockLocationId() {
        return activeRoadblockLocationId;
    }

    public void setActiveRoadblock(String locationId) {
        this.activeRoadblockLocationId = locationId;
    }

    public void clearActiveRoadblock() {
        this.activeRoadblockLocationId = null;
    }

    // ---------------- Checkpoint Token / Checkpoint Mobile (Poliziotto) ----------------

    public String getActiveCheckpointFromId() {
        return activeCheckpointFromId;
    }

    public String getActiveCheckpointToId() {
        return activeCheckpointToId;
    }

    public void setActiveCheckpoint(String fromId, String toId) {
        this.activeCheckpointFromId = fromId;
        this.activeCheckpointToId = toId;
    }

    public void clearActiveCheckpoint() {
        this.activeCheckpointFromId = null;
        this.activeCheckpointToId = null;
    }

    public boolean isCheckpointEdge(String a, String b) {
        if (activeCheckpointFromId == null || activeCheckpointToId == null) {
            return false;
        }
        return (activeCheckpointFromId.equals(a) && activeCheckpointToId.equals(b))
                || (activeCheckpointFromId.equals(b) && activeCheckpointToId.equals(a));
    }

    // ---------------- Scanner ----------------

    public void setLastScannerResult(String centerId, boolean found) {
        this.lastScannerCenterId = centerId;
        this.lastScannerFoundKiller = found;
    }

    public String getLastScannerCenterId() {
        return lastScannerCenterId;
    }

    public boolean isLastScannerFoundKiller() {
        return lastScannerFoundKiller;
    }

    // ---------------- Punteggio ----------------

    public int getPoliceScore() {
        return policeScore;
    }

    public void adjustPoliceScore(int delta) {
        policeScore += delta;
    }
}