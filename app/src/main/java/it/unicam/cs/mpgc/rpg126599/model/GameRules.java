package it.unicam.cs.mpgc.rpg126599.model;

// regole del gioco che non dipendono dal bilanciamento del match 
public final class GameRules {

    private GameRules() {
    }

    
    public static final int MAX_ROUNDS = 10;
    public static final int INITIAL_POLICE_SCORE = 100;

    public static final int ARREST_MAX_DISTANCE = 3;

    public static final int SCANNER_DETECTION_RADIUS = 2;
    public static final int KILLER_MIN_MOVE_DISTANCE = 1;
    public static final int KILLER_MAX_MOVE_DISTANCE = 2;

    public static final int SERIES_WIN_XP_BONUS = 200;


    public static final int MIN_MURDER_LOCATION_DISTANCE_FROM_HOME = 3;
}