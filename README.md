# SHADOWPLAY

## Progetto per il corso di Modellazione e Gestione della Conoscenza

**Shadowplay** è un GDR investigativo digitale a turni ispirato al gioco da tavolo *Lettere da Whitechapel*.

All'inizio della partita il giocatore sceglie quale ruolo interpretare tra **Killer** e **Poliziotto**. Il ruolo non selezionato viene gestito automaticamente dalla logica interna del gioco.

L'obiettivo del **Killer** è riuscire a tornare nel proprio nascondiglio senza essere trovato, mentre il **Poliziotto** deve indagare sulla mappa e attraverso strategie e indizi e arrestare il Killer prima dello scadere dei turni.

La partita si svolge su un tabellone (mappa) costituito da nodi interattivi, attraverso i quali vengono eseguite le varie azioni di gioco.

Oltre alla componente investigativa, Shadowplay presenta elementi come **tratti**, **strumenti** ed **esperienza**, che si sviluppano nel corso di una campagna di più partite.

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

## Meccaniche e Regole di gioco

## Fase Iniziale

All'avvio della partita il **Killer** seleziona segretamente il proprio **nascondiglio** sulla mappa. Il punto di partenza deve essere obbligatoriamente diverso dal nascondiglio e, in più, deve trovarsi ad **almeno 3 caselle di distanza** da esso: in questo modo il Killer non può assicurarsi subito la vittoria scegliendo un punto di partenza troppo vicino a casa.

Successivamente sceglie il luogo del **primo omicidio**, che rappresenta il punto di partenza della partita. Il punto di partenza deve essere obbligatoriamente diverso dal nascondiglio.

---
## Fasi Intermedie 

### Movimento del Killer

Durante il proprio turno il Killer può spostarsi di **una o due caselle adiacenti** scegliendo il percorso migliore per depistare il Poliziotto e cercare di raggiungere il proprio nascondiglio senza essere catturato.

Se il Poliziotto si trova fisicamente sulla casella del nascondiglio, il Killer non può rientrarvi in quel momento: deve aspettare che la Polizia si sposti,  oppure rischiare altre mosse sulla mappa.

---

### Indizi falsi del Killer

Il Killer dispone di **indizi falsi** per ogni partita (il numero esatto varia in base al match della campagna).

Ogni indizio può essere collocato su una casella diversa dalla propria posizione attuale, con l'obiettivo di confondere il Poliziotto e rallentarne le indagini.

---

### Movimento del Poliziotto

Durante il proprio turno il Poliziotto può muoversi di **una sola casella adiacente**.

---

### Indizi del Poliziotto

Il Poliziotto dispone di alcuni **indizi** per partita (il numero esatto varia in base al match della campagna).

Gli indizi permettono di escludere e rendere visibili sulla mappa alcuni nodi nei quali il nascondiglio del Killer sicuramente non può trovarsi, restringendo l'area di ricerca.


---

### Tentativo di arresto

Durante il proprio turno il Poliziotto può tentare l'arresto scegliendo una casella entro **3 caselle di distanza** dalla propria posizione, nella quale pensa possa trovarsi il Killer.

- Se il Killer si trova effettivamente in quella posizione, la partita termina immediatamente con la vittoria del Poliziotto.
- In caso contrario, il tentativo fallisce, la casella viene contrassegnata come **perlustrata** e il Poliziotto subisce una penalità di punteggio/punti esperienza, mentre il Killer ne trae vantaggio.

---

### Strumenti speciali

Oltre alle azioni base, ogni ruolo ha a disposizione degli strumenti tattici nel proprio inventario da poter utilizzare, in quantità limitata e diversa per ogni match della campagna:

**Killer**
- **Smoke Bomb** - durata per un turno: neutralizza il prossimo tentativo di Scanner della Polizia.
- **Trap Kit** - piazza una Trapola: se la Polizia ci entra nel turno successivo viene rallentata.
- **Shortcut Map** - utilizzabile una sola volta a match, ignora distanza massima, Roadblock e Checkpoint (ma non aiuta a rientrare a casa se il Poliziotto la presidia).

**Poliziotto**
- **Roadblock** - blocca un intero nodo per il turno successivo del Killer.
- **Checkpoint** - blocca un singolo collegamento tra due caselle adiacenti alla propria posizione.
- **Scanner** - rivela se il Killer si trova entro 2 caselle dal punto scelto.

---

## Fase Finale

### Vittoria del Killer

Il Killer vince se:

- riesce a rientrare nel proprio nascondiglio segreto senza essere arrestato;
- sopravvive fino allo scadere del numero massimo di turni previsto.

### Vittoria del Poliziotto

Il Poliziotto vince se riesce a individuare e arrestare il Killer prima della fine della partita.

---

## MIGLIORAMENTI NUOVA VERSIONE
## Sistema RPG

### Tratti

All'inizio della campagna il giocatore sceglie un **tratto** per il proprio ruolo, che ne caratterizza lo stile di gioco (il Killer può puntare sul depistaggio o sul timing, il Poliziotto sulla continuità investigativa o sulla sicurezza negli arresti).

### Campagna Best of 3

Le partite non sono isolate: fanno parte di una **campagna al meglio di 3 match**. Ogni match ha un proprio bilanciamento (numero di indizi, strumenti disponibili, penalità sugli arresti sbagliati), che diventa progressivamente più impegnativo man mano che la campagna prosegue.

### Punti Esperienza (XP)

Ogni azione compiuta durante il match assegna un totatle di punti esperienza in base al tratto del ruolo che l'ha eseguita (muoversi in modo tattico, usare un indizio, arrestare correttamente o sbagliare un arresto). L'esperienza si accumula match dopo match e chi vince la campagna riceve un bonus finale. In particolare, il poliziotto scegliendo di spostarsi in caselle che lo avvicinano al killer guadagna gradualmente più punti XP, al contrario, allontanandosi dalla posizione del killer ne guadagna di meno; il killer, invece, guadagna un numero crescente di XP quando con le sue azioni riesci ad allontanare il poliziotto dalla propria posizione.

## FIX per rispettare i principi SOLID e le regole di CLEAN CODE

Il motore del gioco (`GameEngine`) non contiene più tutta la logica al suo interno, ma delega a classi con una responsabilità ben precisa ciascuna, sono state introdotte infatti: **`ActionValidator`** , **`MatchActions`**, **`KillerAI`** / **`PoliceAI`**, **`TurnPhaseManager`**, **`XpCalculator`**, **`CampaignManager`**.

Separazione delle regole fisse di gioco dal suo stato, i valori costanti sono inseriti nell'apposita classe **`GameRules`**, quelli che invece variano tra i vari match della partiita sono nella classe **`MatchDifficulty`**. 

Tutte le scelte sono state fatte con lo scopo di rendere il progetto più facilmente estendibile a nuove mappe, ruoli, strumenti o regole, oltre che riusabile su altre piattaforme.


# Persistenza dei dati

Il gioco include un sistema di salvataggio dello stato del tabellone in un file `Persistence.json`, che consente di riprendere le sessioni interrotte direttamente dal menù principale.

```
Persistence.json
```
Anche alla riapertura dell'applicazione è possibile riprendere la partita salvata in qualsiasi momento, selezionando l'apposita opzione dal menù principale.


---

# Modalità Single Player

Shadowplay è progettato come esperienza single player. Il giocatore controlla uno dei due ruoli (**Killer** oppure **Poliziotto**), mentre il ruolo avversario viene gestito automaticamente dal motore di gioco (**GameEngine**) attraverso logiche decisionali dedicate.

---

# Utilizzo di strumenti di Intelligenza Artificiale

Il progetto è stato sviluppato con il supporto di strumenti di intelligenza artificiale, utilizzati come assistenti alla programmazione per individuare e correggere i bug (in particolare con la sincronizzazione tra la mappa grafica e il modello logico) e per la progettazione di alcune logiche algoritmiche più complesse.
Ogni suggerimento è stato comunque valutato, verificato, compreso e corretto manualmente prima dell'integrazione nel progetto.
In particolare:

- **ChatGPT** e **Gemini** sono stati utilizzati per la generazione delle immagini e delle grafiche della schermata iniziale e della mappa di gioco.
- **Gemini** ha fornito supporto nella rifinitura dei fogli di stile CSS dell'interfaccia JavaFX, migliorandone la coerenza estetica.
- **Claude Code** è stato utilizzato come supporto nella progettazione e riorganizzazione della logica e nella generazione e disposizione dei nodi sulla mappa.

# Wiki
Per una descrizione più dettagliata del progetto e dell'utilizzo degli strumenti di Intelligenza Artificiale consultare la **Wiki** del repository.
