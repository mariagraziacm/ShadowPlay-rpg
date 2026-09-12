package it.unicam.cs.mpgc.rpg126599.model;

// Responsabilità unica: gli effetti "attivi per un turno" piazzati da Killer o Polizia
// (Trap Zone, Roadblock, Checkpoint) e l'ultimo esito dello Scanner per la UI.
public class TacticalEffects {

    private String activeTrapZoneLocationId;
    private boolean policeStunnedNextTurn;

    private String activeRoadblockLocationId;

    private String activeCheckpointFromId;
    private String activeCheckpointToId;

    private String lastScannerCenterId;
    private boolean lastScannerFoundKiller;

    // ---- Trap Zone (Killer) ----

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

    // ---- Roadblock (Polizia) ----

    public String getActiveRoadblockLocationId() {
        return activeRoadblockLocationId;
    }

    public void setActiveRoadblock(String locationId) {
        this.activeRoadblockLocationId = locationId;
    }

    public void clearActiveRoadblock() {
        this.activeRoadblockLocationId = null;
    }

    // ---- Checkpoint Token (Polizia) ----

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

    // ---- Scanner ----

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
}