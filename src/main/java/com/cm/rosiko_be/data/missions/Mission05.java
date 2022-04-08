package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Continent;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;

import java.util.List;

public class Mission05 extends Mission{

    public Mission05(){
        super();
        description = "Capture North America and Oceania.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {

        boolean northAmerica = false;
        boolean oceania = false;

        //Lista dei continenti posseduti dal giocatore
        List<Continent> continents = match.getContinentsOwned(player);

        for (Continent continent : continents) {
            //Controlla che abbia preso il Nord America
            if(continent.getId().equals("north_america")) northAmerica = true;
            //Controlla che abbia preso l'Oceania
            if(continent.getId().equals("oceania")) oceania = true;
        }

        return northAmerica && oceania;
    }
}
