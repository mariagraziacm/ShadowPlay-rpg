package it.unicam.cs.mpgc.rpg126599.model;

// Regole del gioco che NON dipendono dal bilanciamento del match (quelle restano in
// MatchDifficulty, perché variano da match a match): valgono sempre, in ogni partita.
// Centralizzarle qui evita numeri "magici" ripetuti tra GameEngine, ActionValidator,
// MatchActions, TurnState, XpLedger, CampaignManager e KillerAI.
public final class GameRules {

    private GameRules() {
    }

    // Numero massimo di round di un match: superato senza cattura, vince il Killer.
    public static final int MAX_ROUNDS = 10;

    // Punteggio informativo iniziale della Polizia (penalizzato dagli arresti sbagliati).
    public static final int INITIAL_POLICE_SCORE = 100;

    // Distanza massima entro cui la Polizia può tentare un arresto.
    public static final int ARREST_MAX_DISTANCE = 3;

    // Raggio entro cui lo Scanner della Polizia rileva il Killer.
    public static final int SCANNER_DETECTION_RADIUS = 2;

    // Caselle minime/massime percorribili dal Killer in una mossa normale.
    public static final int KILLER_MIN_MOVE_DISTANCE = 1;
    public static final int KILLER_MAX_MOVE_DISTANCE = 2;

    // Bonus Xp per chi vince la serie Best of 3 della campagna.
    public static final int SERIES_WIN_XP_BONUS = 200;

    // Distanza minima richiesta tra il nascondiglio del Killer e il luogo del primo omicidio.
    public static final int MIN_MURDER_LOCATION_DISTANCE_FROM_HOME = 3;
}