package it.unicam.cs.mpgc.rpg126599.model;

// Cataloga gli oggetti dell'inventario Killer (manipolazione e fuga)
public enum KillerItemType {
    FAKE_CLUE("Fake Clue", "Lascia un indizio falso per depistare il Poliziotto."),
    SMOKE_BOMB("Smoke Bomb", "Copertura per un turno: neutralizza il prossimo tentativo di Scanner."),
    TRAP_KIT("Trap Kit", "Crea una Trap Zone: se la Polizia vi entra nel turno successivo viene rallentata."),
    SHORTCUT_MAP("Shortcut Map", "Bypassa un vincolo di movimento (Roadblock, Checkpoint o distanza) una volta per match.");

    private final String displayName;
    private final String description;

    KillerItemType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}