package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import com.cm.rosiko_be.data.Territory;
import java.util.List;

public class Mission06 extends Mission{

    public Mission06(){
        super();
        description = "Capture 24 territories.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {

        int territoryCounter = 0;

        List<Territory> territories = match.getMap().getTerritories();

        for (Territory territory : territories) {
            if(territory.getOwner().equals(player)) territoryCounter++;
        }

        return territoryCounter >= 24;
    }
}
