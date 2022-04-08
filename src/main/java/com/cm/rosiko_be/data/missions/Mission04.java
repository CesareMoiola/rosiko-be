package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Continent;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;

import java.util.List;

public class Mission04 extends Mission{

    public Mission04(){
        super();
        description = "Capture Asia and South America.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {

        boolean asia = false;
        boolean southAmerica = false;

        //Lista dei continenti posseduti dal giocatore
        List<Continent> continents = match.getContinentsOwned(player);

        for (Continent continent : continents) {
            //Controlla che abbia preso l'Asia
            if(continent.getId().equals("asia")) asia = true;
            //Controlla che abbia preso il Sud America
            if(continent.getId().equals("south_america")) southAmerica = true;
        }

        return asia && southAmerica;
    }
}
