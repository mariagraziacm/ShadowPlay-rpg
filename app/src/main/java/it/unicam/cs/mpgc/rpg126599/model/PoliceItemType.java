package it.unicam.cs.mpgc.rpg126599.model;

// Cataloga gli oggetti dell'inventario Poliziotto (analisi e controllo)
public enum PoliceItemType {
    CLUE("Clue", "Restringe le ipotesi eliminando una casella candidata al nascondiglio."),
    ROADBLOCK("Roadblock", "Blocca un intero nodo per il prossimo turno del Killer."),
    CHECKPOINT_TOKEN("Checkpoint Token", "Checkpoint Mobile: blocco breve di un singolo collegamento per un turno."),
    SCANNER("Scanner", "Lettura rapida dell'area: rivela se il Killer è entro 2 caselle dal punto scelto.");

    private final String displayName;
    private final String description;

    PoliceItemType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}