package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import com.cm.rosiko_be.data.Territory;
import java.util.List;

public class Mission07 extends Mission{

    public Mission07(){
        super();
        description = "Capture 18 territories and occupy each with two troops.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {

        int territoryCounter = 0;

        List<Territory> territories = match.getMap().getTerritories();

        for (Territory territory : territories) {
            if(
                    territory.getOwner().equals(player)
                    && territory.getArmies() >= 2
            ) territoryCounter++;
        }

        return territoryCounter >= 18;
    }
}
