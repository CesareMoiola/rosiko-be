package com.cm.rosiko_be.data.missions;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import com.cm.rosiko_be.data.Territory;
import com.cm.rosiko_be.enums.Color;

import java.util.List;

public class MissionColor extends Mission{

    private final int TERRITORIES_TO_CONQUEST = 24;
    private Color enemyColor = null;

    private Player target = null;
    private boolean conquest24Territories = false;

    public MissionColor(Color color){
        super();
        enemyColor = color;
        description = "Destroy all armies of " + color.toString().toLowerCase() + " or, in the case of being the named player oneself, to capture 24 territories.";
    }

    @Override
    public boolean isMissionCompleted(Player player, Match match) {
        List<Player> players = match.getPlayers();

        boolean completedMission = false;

        //Controlla se il giocatore con quel colore è presente e non è se stesso.
        if(target == null){
            for (Player currentPlayer : players) {
                if(currentPlayer.getColor().equals(enemyColor) && !currentPlayer.equals(player)){
                    target = currentPlayer;
                }
            }
        }

        //Controlla se il target esista ed è stato sconfitto.
        if(target != null && !target.isActive()){
            for(Player currentPlayer : players){
                List<Player> defeated = currentPlayer.getDefeatedPlayers();
                for(Player defeatedPlayer : defeated){

                    //Se il giocatore ha sconfitto il target imposta l'obiettivo come completato
                    if(defeatedPlayer.equals(target) && currentPlayer.equals(player)){
                        completedMission = true;
                    }

                    //Se il target è stato sconfitto da un altro giocatore l'obiettivo diventa la conquista di 24 territori
                    if(defeatedPlayer.equals(target) && !currentPlayer.equals(player)){
                        conquest24Territories = true;
                    }
                }
            }
        }

        //Se l'obiettivo è diventato la conquista dei territori, controlla che sia stato raggiunto.
        if(target == null || conquest24Territories){
            List<Territory> territories = match.getTerritoriesOwned(player);
            completedMission = territories.size() >= TERRITORIES_TO_CONQUEST;
        }

        return completedMission;
    }
}
