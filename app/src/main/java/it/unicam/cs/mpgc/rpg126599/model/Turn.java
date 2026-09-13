package it.unicam.cs.mpgc.rpg126599.model;
// enum per le  fasi del gioco

public enum Turn {
    // FASE INIZIALE: il killer deve scegliere il nascondiglio
    AWAITING_HOME_CHOICE,
    // FASE DI PARTENZA: il killer deve scegliere il luogo del primo omicidio  da cui partire
    AWAITING_MURDER_LOCATION_CHOICE,
    // FASE DI AZIONE DEL KILLER: il killer deve  eseguire azioni
    AWAITING_KILLER_ACTION,
    // FASE DI AZIONE DEL POLIZIOTTO: il poliziotto deve eseguire azioni
    AWAITING_POLICE_ACTION,
    // FASE FINALE: la partita è finita
    GAME_OVER
}
