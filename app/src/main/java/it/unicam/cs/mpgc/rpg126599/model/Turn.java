package it.unicam.cs.mpgc.rpg126599.model;


public enum Turn {
    /** Il killer deve ancora scegliere la propria casa segreta (punto di ritorno). */
    AWAITING_HOME_CHOICE,
    /** Il killer deve scegliere il luogo del primo omicidio (posizione di partenza, diversa da casa). */
    AWAITING_MURDER_LOCATION_CHOICE,
    /** Tocca al killer muoversi o lasciare un indizio falso. */
    AWAITING_KILLER_ACTION,
    /** Tocca al poliziotto usare un indizio, muoversi o tentare l'arresto. */
    AWAITING_POLICE_ACTION,
    /** La partita è finita. */
    GAME_OVER
}
