package it.unicam.cs.mpgc.rpg126599.core;

import java.util.ArrayList;
import java.util.List;

import it.unicam.cs.mpgc.rpg126599.model.Board;
import it.unicam.cs.mpgc.rpg126599.model.GameRules;
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

    public static CampaignManager resume(Board board, GameState savedState) {
        CampaignManager campaign = new CampaignManager(board, savedState.getHumanRole());
        if (savedState.isCampaignInProgress()) {
            campaign.currentMatchNumber = savedState.getCampaignCurrentMatchNumber();
            campaign.killerWins = savedState.getCampaignKillerWins();
            campaign.policeWins = savedState.getCampaignPoliceWins();
            campaign.killerXp = savedState.getCampaignKillerXp();
            campaign.policeXp = savedState.getCampaignPoliceXp();
        }
        if (savedState.getHumanRole() == RoleType.KILLER) {
            campaign.humanTraits.addAll(savedState.getKillerTraits());
        } else {
            campaign.humanTraits.addAll(savedState.getPoliceTraits());
        }
        campaign.currentEngine = GameEngine.resume(board, savedState);
        return campaign;
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

    // sblocco del secondo tratto
   
    public boolean isSecondTraitUnlockPending() {
        return currentMatchNumber == 2 && humanTraits.size() < 2;
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

        state.setCampaignProgress(currentMatchNumber, killerWins, policeWins, killerXp, policeXp);

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

    // registra l'esito del match e accumula i punti  guadagnatii9
    
    public void recordMatchResult(RoleType matchWinner) {
        GameState state = currentEngine != null ? currentEngine.getState() : null;
        int killerMatchXp = state != null ? state.getKillerXpThisMatch() : 0;
        int policeMatchXp = state != null ? state.getPoliceXpThisMatch() : 0;

        if (matchWinner == RoleType.KILLER) {
            killerWins++;
            killerXp += killerMatchXp;
            policeXp += policeMatchXp;
        } else {
            policeWins++;
            policeXp += policeMatchXp;
            killerXp += killerMatchXp;
        }
        currentMatchNumber++;

        if (isSeriesOver()) {
            if (matchWinner == RoleType.KILLER) {
                killerXp += GameRules.SERIES_WIN_XP_BONUS;
            } else {
                policeXp += GameRules.SERIES_WIN_XP_BONUS;
            }
        }

        if (currentEngine != null) {
            currentEngine.getState().setCampaignProgress(currentMatchNumber, killerWins, policeWins, killerXp, policeXp);
        }
    }
}