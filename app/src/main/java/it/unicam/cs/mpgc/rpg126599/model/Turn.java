package it.unicam.cs.mpgc.rpg126599.model;
// enum per le  fasi del gioco

public enum Turn {
    // FASE INIZIALE: il killer deve scegliere il nascondiglio
    AWAITING_HOME_CHOICE,
    // FASE DI PARTENZA: il killer deve scegliere il luogo del primo omicidio, da cui poi partirà con le prime mosse
    AWAITING_MURDER_LOCATION_CHOICE,
    // FASE DI AZIONE DEL KILLER: il killer deve muoversi o lasciare un indizio falso
    AWAITING_KILLER_ACTION,
    // FASE DI AZIONE DEL POLIZIOTTO: il poliziotto deve usare un indizio, muoversi o tentare l'arresto
    AWAITING_POLICE_ACTION,
    // FASE FINALE: la partita è finita
    GAME_OVER
}
