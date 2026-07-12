# SHADOWPLAY

## Progetto per il corso di Modellazione e Gestione della Conoscenza

**Shadowplay** è un gioco investigativo digitale a turni ispirato al gioco da tavolo *Lettere da Whitechapel*.

All'inizio della partita il giocatore sceglie quale ruolo interpretare tra **Killer** e **Poliziotto**. Il ruolo non selezionato viene gestito automaticamente dalla logica interna del gioco.

L'obiettivo del **Killer** è riuscire a tornare nel proprio nascondiglio senza essere trovato, mentre il **Poliziotto** deve indagare sulla mappa e attraverso strategie e indizi e arrestare il Killer prima dello scadere dei turni.

La partita si svolge su un tabellone (mappa) costituito da nodi interattivi, attraverso i quali vengono eseguite le varie azioni di gioco.

L'intera architettura del progetto è stata progettata seguendo un approccio modulare, così da facilitare l'estensione futura del gioco mediante l'aggiunta di nuove mappe, ruoli, azioni e regole senza modificare il nucleo dell'applicazione.

---

# Come eseguire il progetto

## Prerequisiti

- Java 25 (LTS)
- Gradle

## Build del progetto

```bash
./gradlew build
```

## Avvio del progetto

```bash
./gradlew run
```

---

# Meccaniche e regole di gioco

## Fase iniziale

All'avvio della partita il **Killer** seleziona segretamente il proprio **nascondiglio** sulla mappa.

Successivamente sceglie il luogo del **primo omicidio**, che rappresenta il punto di partenza della partita. Il punto di partenza deve essere obbligatoriamente diverso dal nascondiglio.

---
## Fasi Intermedie 

## Movimento del Killer

Durante il proprio turno il Killer può spostarsi di **una o due caselle adiacenti** scegliendo il percorso migliore per depistare il Poliziotto e cercare di raggiungere il proprio nascondiglio 
senza essere catturato.

---

## Indizi falsi del Killer

Il Killer dispone di **2 indizi falsi** per ogni partita.

Ogni indizio può essere collocato su una casella diversa dalla propria posizione attuale, con l'obiettivo di confondere il Poliziotto e rallentarne le indagini.

---

## Movimento del Poliziotto

Durante il proprio turno il Poliziotto può muoversi di **una sola casella adiacente**.

---

## Indizi del Poliziotto

Il Poliziotto dispone di **3 indizi** per partita.

Gli indizi permettono di escludere e rendere visibili sulla mappa alcuni nodi nei quali il nascondiglio del Killer sicuramente non può trovarsi, restringendo l'area di ricerca.

---

## Tentativo di arresto

Durante il proprio turno il Poliziotto può tentare l'arresto scegliendo una casella adiacente nella quale pensa possa trovarsi il Killer.

- Se il Killer si trova effettivamente in quella posizione, la partita termina immediatamente con la vittoria del Poliziotto.
- In caso contrario, il tentativo fallisce e la casella viene contrassegnata come **perlustrata**.

---

# Condizioni di vittoria

## Vittoria del Killer

Il Killer vince se:

- riesce a rientrare nel proprio nascondiglio segreto senza essere arrestato;
- sopravvive fino allo scadere del numero massimo di turni previsto.

## Vittoria del Poliziotto

Il Poliziotto vince se riesce a individuare e arrestare il Killer prima della fine della partita.

---

# Persistenza dei dati

Il gioco include un sistema di salvataggio dello stato del tabellone in un file `Persistence.json`, che consente di riprendere le sessioni interrotte direttamente dal menù principale.

```
Persistence.json
```

Alla riapertura dell'applicazione è possibile riprendere automaticamente la partita salvata direttamente dal menu principale.

---

# Modalità Single Player

Shadowplay è progettato come esperienza **single player**.

Il giocatore controlla uno dei due ruoli (**Killer** oppure **Poliziotto**), mentre il ruolo avversario viene gestito automaticamente dal motore di gioco (**GameEngine**) attraverso logiche decisionali dedicate.

---

# Utilizzo di strumenti di Intelligenza Artificiale

Il progetto è stato sviluppato con il supporto di strumenti di intelligenza artificiale, utilizzati come assistenti alla programmazione per individuare e correggere i bug 
(in particolare con la sincronizzazione tra la mappa grafica e il modello logico) e  la progettazione di alcune logiche algoritmiche più complesse.
Ogni suggerimento è stato comunque valutato, verificato, compreso e  corretto manualmente prima dell'integrazione nel progetto.
In particolare:

- **ChatGPT** e **Gemini** sono stati utilizzati per la generazione delle immagini e delle grafiche della schermata iniziale e della mappa di gioco.
- **Gemini** ha fornito supporto nella rifinitura dei fogli di stile CSS dell'interfaccia JavaFX, migliorandone la coerenza estetica.
- **Claude Code** è stato utilizzato come supporto nella progettazione della logica di generazione e disposizione dei nodi sulla mappa.

Per una descrizione più dettagliata dell'utilizzo degli strumenti di Intelligenza Artificiale è disponibile la **Wiki** del repository.
