package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Continent;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import java.util.List;

public class Mission02 extends Mission{

    public Mission02(){
        super();
        description = "Capture Europe, South America and one other continent.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {

        boolean southAmerica = false;
        boolean europe = false;
        boolean thirdContinent = false;

        //Lista dei continenti posseduti dal giocatore
        List<Continent> continents = match.getContinentsOwned(player);

        for (Continent continent : continents) {
            //Controlla che abbia preso il Sud America
            if(continent.getId().equals("south_america")) southAmerica = true;
            //Controlla che abbia preso l'Europa
            if(continent.getId().equals("europe")) europe = true;
            //Controlla che abbia preso un terzo continente
            if(!continent.getId().equals("south_america") && !continent.getId().equals("europe")) thirdContinent = true;
        }

        return southAmerica && europe && thirdContinent;
    }
}
