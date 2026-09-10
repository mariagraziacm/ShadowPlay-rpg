package it.unicam.cs.mpgc.rpg126599.model;

import java.util.ArrayList;
import java.util.List;

// Tratti RPG: caratterizzano lo stile di interpretazione del ruolo.
// Ogni ruolo ha 2 tratti scelti alla creazione (livello 1) e 2 sbloccati al terzo match (livello 3).
public enum Trait {

    MANIPOLATORE(RoleType.KILLER, "Manipolatore",
            "Enfatizza il depistaggio: i tuoi indizi falsi sono più efficaci nel confondere la Polizia.", 1),
    CALCOLATORE(RoleType.KILLER, "Calcolatore",
            "Premia il timing: agire nei momenti chiave della partita ti dà un vantaggio.", 1),
    SANGUE_FREDDO(RoleType.KILLER, "Sangue Freddo",
            "Valorizza l'errore avversario: ogni arresto sbagliato della Polizia ti frutta una Smoke Bomb extra.", 3),
    OMBRA_URBANA(RoleType.KILLER, "Ombra Urbana",
            "Offre elasticità contro il controllo mappa: sei più difficile da bloccare.", 3),

    DEDUTTIVO(RoleType.POLICE, "Deduttivo",
            "Premia la continuità investigativa: i tuoi indizi restringono il campo con più efficacia.", 1),
    PRESSIONE_TATTICA(RoleType.POLICE, "Pressione Tattica",
            "Premia gli arresti corretti: la sicurezza nel colpire nel segno è la tua forza.", 1),
    METODICO(RoleType.POLICE, "Metodico",
            "Rafforza la preparazione iniziale: ti muovi sulla mappa con più metodo fin dal primo turno.", 3),
    COMANDO_OPERATIVO(RoleType.POLICE, "Comando Operativo",
            "Potenzia il controllo territoriale nei momenti chiave della partita.", 3);

    private final RoleType role;
    private final String displayName;
    private final String description;
    private final int requiredLevel;

    Trait(RoleType role, String displayName, String description, int requiredLevel) {
        this.role = role;
        this.displayName = displayName;
        this.description = description;
        this.requiredLevel = requiredLevel;
    }

    public RoleType getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getRequiredLevel() { return requiredLevel; }

    // tratti disponibili per un ruolo al livello di campagna raggiunto
    public static List<Trait> availableFor(RoleType role, int level) {
        List<Trait> result = new ArrayList<>();
        for (Trait t : values()) {
            if (t.role == role && t.requiredLevel <= level) {
                result.add(t);
            }
        }
        return result;
    }
}