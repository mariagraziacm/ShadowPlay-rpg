package it.unicam.cs.mpgc.rpg126599.model;

import java.util.List;
import java.util.Set;

// Prima era una God Class da ~530 righe con oltre 30 campi che mescolava:
// stato del turno, inventari di Killer e Polizia, effetti tattici attivi,
// progressione della campagna e contabilità Xp.
//
// Ora GameState è una facciata: mantiene l'identica API pubblica (nessun'altra
// classe del progetto deve cambiare), ma la responsabilità reale è distribuita
// su oggetti dedicati e testabili singolarmente: TurnState, KillerInventory,
// PoliceInventory, TacticalEffects, CampaignProgress, XpLedger.
public class GameState {

    private Player killer;
    private Player police;
    private RoleType humanRole;

    private final TurnState turn = new TurnState();
    private final KillerInventory killerInventory = new KillerInventory();
    private final PoliceInventory policeInventory = new PoliceInventory();
    private final TacticalEffects effects = new TacticalEffects();
    private final CampaignProgress campaign = new CampaignProgress();
    private final XpLedger xp = new XpLedger();

    public GameState() {
    }

    public GameState(Player killer, Player police, RoleType humanRole) {
        this.killer = killer;
        this.police = police;
        this.humanRole = humanRole;
        this.turn.markPoliceVisited(police.getCurrentLocationId());
    }

    public GameState(Player killer, Player police, RoleType humanRole, MatchDifficulty difficulty) {
        this(killer, police, humanRole);
        applyDifficulty(difficulty);
    }

    // applica il bilanciamento di un match della campagna, azzerando gli oggetti "una tantum"
    public void applyDifficulty(MatchDifficulty difficulty) {
        campaign.setDifficulty(difficulty);
        killerInventory.reset(difficulty);
        policeInventory.reset(difficulty);
    }

    public MatchDifficulty getDifficulty() {
        return campaign.getDifficulty();
    }

    // ---------------- Campagna ----------------

    public boolean isCampaignInProgress() {
        return campaign.isInProgress();
    }

    public int getCampaignCurrentMatchNumber() {
        return campaign.getCurrentMatchNumber();
    }

    public int getCampaignKillerWins() {
        return campaign.getKillerWins();
    }

    public int getCampaignPoliceWins() {
        return campaign.getPoliceWins();
    }

    public int getCampaignKillerXp() {
        return campaign.getKillerXp();
    }

    public int getCampaignPoliceXp() {
        return campaign.getPoliceXp();
    }

    public void setCampaignProgress(int currentMatchNumber, int killerWins, int policeWins,
                                     int killerXp, int policeXp) {
        campaign.setProgress(currentMatchNumber, killerWins, policeWins, killerXp, policeXp);
    }

    public void clearCampaignProgress() {
        campaign.clear();
        xp.reset();
    }

    // ---------------- Xp del match ----------------

    public int getKillerXpThisMatch() {
        return xp.getKillerXpThisMatch();
    }

    public int getPoliceXpThisMatch() {
        return xp.getPoliceXpThisMatch();
    }

    public void addKillerXp(int delta) {
        xp.addKillerXp(delta);
    }

    public void addPoliceXp(int delta) {
        xp.addPoliceXp(delta);
    }

    public int getLastXpDelta(RoleType role) {
        return xp.getLastDelta(role);
    }

    public void clearLastXpDelta() {
        xp.clearLastDelta();
    }

    public void resetMatchXp() {
        xp.reset();
    }

    // ---------------- Giocatori e ruolo ----------------

    public Player getKiller() {
        return killer;
    }

    public Player getPolice() {
        return police;
    }

    public RoleType getHumanRole() {
        return humanRole;
    }

    public Player playerOf(RoleType role) {
        return role == RoleType.KILLER ? killer : police;
    }

    // ---------------- Turno e fase ----------------

    public Turn getPhase() {
        return turn.getPhase();
    }

    public void setPhase(Turn phase) {
        turn.setPhase(phase);
    }

    public boolean isHomeChosen() {
        return turn.isHomeChosen();
    }

    public String getKillerHomeLocationId() {
        return turn.getKillerHomeLocationId();
    }

    public void chooseHome(String locationId) {
        turn.chooseHome(locationId);
    }

    public void setKillerStartLocation(String locationId) {
        this.killer.moveTo(locationId);
        turn.markKillerVisited(locationId);
    }

    public boolean hasLeftHome() {
        return turn.hasLeftHome();
    }

    public void markLeftHome() {
        turn.markLeftHome();
    }

    public int getRoundsElapsed() {
        return turn.getRoundsElapsed();
    }

    public int getMaxRounds() {
        return turn.getMaxRounds();
    }

    public void incrementRound() {
        turn.incrementRound();
    }

    public int getKillerMovesMade() {
        return turn.getKillerMovesMade();
    }

    public int getPoliceMovesMade() {
        return turn.getPoliceMovesMade();
    }

    public void registerKillerMove() {
        turn.registerKillerMove();
    }

    public void registerPoliceMove() {
        turn.registerPoliceMove();
    }

    public List<String> getEliminatedHomeCandidates() {
        return turn.getEliminatedHomeCandidates();
    }

    public void eliminateHomeCandidate(String locationId) {
        turn.eliminateHomeCandidate(locationId);
    }

    public List<String> getFailedArrestLocations() {
        return turn.getFailedArrestLocations();
    }

    public void recordFailedArrest(String locationId) {
        turn.recordFailedArrest(locationId);
    }

    public boolean isAlreadySearched(String locationId) {
        return turn.isAlreadySearched(locationId);
    }

    public List<Clue> getFakeClues() {
        return turn.getFakeClues();
    }

    public void addFakeClue(String locationId) {
        turn.addFakeClue(locationId);
    }

    public Set<String> getVisitedByKiller() {
        return turn.getVisitedByKiller();
    }

    public Set<String> getVisitedByPolice() {
        return turn.getVisitedByPolice();
    }

    public void markKillerVisited(String locationId) {
        turn.markKillerVisited(locationId);
    }

    public void markPoliceVisited(String locationId) {
        turn.markPoliceVisited(locationId);
    }

    public boolean isFinished() {
        return turn.isFinished();
    }

    public RoleType getWinner() {
        return turn.getWinner();
    }

    public String getEndReason() {
        return turn.getEndReason();
    }

    public void finish(RoleType winnerRole, String reason) {
        turn.finish(winnerRole, reason);
    }

    // ---------------- Tratti ----------------

    public List<Trait> getKillerTraits() {
        return campaign.getKillerTraits();
    }

    public void setKillerTraits(List<Trait> traits) {
        campaign.setKillerTraits(traits);
    }

    public List<Trait> getPoliceTraits() {
        return campaign.getPoliceTraits();
    }

    public void setPoliceTraits(List<Trait> traits) {
        campaign.setPoliceTraits(traits);
    }

    // ---------------- Inventario Killer ----------------

    public int getKillerSmokeBombsRemaining() {
        return killerInventory.getSmokeBombsRemaining();
    }

    public void useKillerSmokeBomb() {
        killerInventory.useSmokeBomb();
    }

    public void grantKillerBonusSmokeBomb() {
        killerInventory.grantBonusSmokeBomb();
    }

    public int getKillerFakeCluesRemaining() {
        return killerInventory.getFakeCluesRemaining();
    }

    public void useKillerFakeClue() {
        killerInventory.useFakeClue();
    }

    public void grantKillerArrestFailureBonus() {
        killerInventory.grantFakeClueBonus();
    }

    public int getKillerTrapKitsRemaining() {
        return killerInventory.getTrapKitsRemaining();
    }

    public void useKillerTrapKit() {
        killerInventory.useTrapKit();
    }

    public boolean isKillerShortcutMapUsed() {
        return killerInventory.isShortcutMapUsed();
    }

    public void markKillerShortcutMapUsed() {
        killerInventory.markShortcutMapUsed();
    }

    public boolean isKillerSmokeBombActive() {
        return killerInventory.isSmokeBombActive();
    }

    public void activateKillerSmokeBomb() {
        killerInventory.activateSmokeBomb();
    }

    public void clearKillerSmokeBombActive() {
        killerInventory.clearSmokeBombActive();
    }

    // ---------------- Inventario Poliziotto ----------------

    public int getPoliceCluesRemaining() {
        return policeInventory.getCluesRemaining();
    }

    public void usePoliceClue() {
        policeInventory.useClue();
    }

    public int getPoliceRoadblocksRemaining() {
        return policeInventory.getRoadblocksRemaining();
    }

    public void useRoadblock() {
        policeInventory.useRoadblock();
    }

    public int getPoliceCheckpointTokensRemaining() {
        return policeInventory.getCheckpointTokensRemaining();
    }

    public void useCheckpointToken() {
        policeInventory.useCheckpointToken();
    }

    public int getPoliceScannerRemaining() {
        return policeInventory.getScannerRemaining();
    }

    public void useScanner() {
        policeInventory.useScanner();
    }

    // ---------------- Effetti tattici ----------------

    public String getActiveTrapZoneLocationId() {
        return effects.getActiveTrapZoneLocationId();
    }

    public void setActiveTrapZone(String locationId) {
        effects.setActiveTrapZone(locationId);
    }

    public void clearActiveTrapZone() {
        effects.clearActiveTrapZone();
    }

    public boolean isPoliceStunnedNextTurn() {
        return effects.isPoliceStunnedNextTurn();
    }

    public void setPoliceStunnedNextTurn(boolean value) {
        effects.setPoliceStunnedNextTurn(value);
    }

    public String getActiveRoadblockLocationId() {
        return effects.getActiveRoadblockLocationId();
    }

    public void setActiveRoadblock(String locationId) {
        effects.setActiveRoadblock(locationId);
    }

    public void clearActiveRoadblock() {
        effects.clearActiveRoadblock();
    }

    public String getActiveCheckpointFromId() {
        return effects.getActiveCheckpointFromId();
    }

    public String getActiveCheckpointToId() {
        return effects.getActiveCheckpointToId();
    }

    public void setActiveCheckpoint(String fromId, String toId) {
        effects.setActiveCheckpoint(fromId, toId);
    }

    public void clearActiveCheckpoint() {
        effects.clearActiveCheckpoint();
    }

    public boolean isCheckpointEdge(String a, String b) {
        return effects.isCheckpointEdge(a, b);
    }

    public void setLastScannerResult(String centerId, boolean found) {
        effects.setLastScannerResult(centerId, found);
    }

    public String getLastScannerCenterId() {
        return effects.getLastScannerCenterId();
    }

    public boolean isLastScannerFoundKiller() {
        return effects.isLastScannerFoundKiller();
    }

    // ---------------- Punteggio ----------------

    public int getPoliceScore() {
        return xp.getPoliceScore();
    }

    public void adjustPoliceScore(int delta) {
        xp.adjustPoliceScore(delta);
    }
}