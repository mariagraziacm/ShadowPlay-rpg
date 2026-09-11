package it.unicam.cs.mpgc.rpg126599.core;

import java.util.ArrayList;
import java.util.List;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameState;
import it.unicam.cs.mpgc.rpg126599.model.MatchDifficulty;
import it.unicam.cs.mpgc.rpg126599.model.Player;
import it.unicam.cs.mpgc.rpg126599.model.RoleType;
import it.unicam.cs.mpgc.rpg126599.model.Trait;

public class CampaignManager {

    private final Board board;
    private final RoleType humanRole;

    private int killerWins;
    private int policeWins;
    private int currentMatchNumber = 1;

    private int killerXp;
    private int policeXp;

    private final List<Trait> humanTraits = new ArrayList<>();
    private GameEngine currentEngine;

    private CampaignManager(Board board, RoleType humanRole) {
        this.board = board;
        this.humanRole = humanRole;
    }

    public static CampaignManager start(Board board, RoleType humanRole) {
        return new CampaignManager(board, humanRole);
    }

    public RoleType getHumanRole() {
        return humanRole;
    }

    public int getKillerWins() {
        return killerWins;
    }

    public int getPoliceWins() {
        return policeWins;
    }

    public int getCurrentMatchNumber() {
        return currentMatchNumber;
    }

    public GameEngine getCurrentEngine() {
        return currentEngine;
    }

    public int getCurrentLevel() {
        return currentMatchNumber;
    }

    public int getKillerXp() {
        return killerXp;
    }

    public int getPoliceXp() {
        return policeXp;
    }

    public boolean isSecondTraitUnlockPending() {
        return currentMatchNumber == 3 && humanTraits.size() < 2;
    }

    public void addHumanTrait(Trait trait) {
        humanTraits.add(trait);
    }

    public List<Trait> getHumanTraits() {
        return humanTraits;
    }

    public GameEngine startCurrentMatch() {
        MatchDifficulty difficulty = MatchDifficulty.forMatchNumber(currentMatchNumber);
        Player killer = new Player(RoleType.KILLER, null);
        Player police = new Player(RoleType.POLICE, "n20");
        GameState state = new GameState(killer, police, humanRole, difficulty);

        if (humanRole == RoleType.KILLER) {
            state.setKillerTraits(new ArrayList<>(humanTraits));
        } else {
            state.setPoliceTraits(new ArrayList<>(humanTraits));
        }

        currentEngine = GameEngine.newCampaignMatch(board, state);
        return currentEngine;
    }

    public boolean isSeriesOver() {
        return killerWins >= 2 || policeWins >= 2;
    }

    public RoleType getSeriesWinner() {
        if (killerWins >= 2) return RoleType.KILLER;
        if (policeWins >= 2) return RoleType.POLICE;
        return null;
    }

    // registra l'esito del match, avanza la campagna e assegna gli XP:
    // +100 XP a chi vince il match, +30 XP di consolazione a chi perde,
    // +200 XP di bonus extra a chi vince l'intera serie Best of 3
    public void recordMatchResult(RoleType matchWinner) {
        if (matchWinner == RoleType.KILLER) {
            killerWins++;
            killerXp += 100;
            policeXp += 30;
        } else {
            policeWins++;
            policeXp += 100;
            killerXp += 30;
        }
        currentMatchNumber++;

        if (isSeriesOver()) {
            if (matchWinner == RoleType.KILLER) {
                killerXp += 200;
            } else {
                policeXp += 200;
            }
        }
    }
}