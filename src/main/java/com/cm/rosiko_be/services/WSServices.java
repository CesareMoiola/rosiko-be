package com.cm.rosiko_be.services;

import com.cm.rosiko_be.controller.MatchesController;
import com.cm.rosiko_be.controller.RestController;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/*Questa classe serve per mandare messaggi tramite websocket a i client iscritti*/
@Service
public class WSServices {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WSServices(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate=messagingTemplate;
    }

    //Manda come messaggio la lista di joinableMatches
    public void notifyJoinableMatches (){
        messagingTemplate.convertAndSend("/queue/joinableMatches", RestController.getJoinableMatches());
    }

    //Manda il match aggiornato ai players iscritti escluso quello passato come parametro
    public void notifyMatch (long matchId, String playerId){
        Match match = MatchesController.getMatch(matchId);

        for(Player player : match.getPlayers())
        {
            if(!player.getId().equals(playerId)){
                messagingTemplate.convertAndSendToUser(player.getId(),"/queue/match", match);
            }
        }
    }

    //Manda il match aggiornato ai players iscritti
    public void notifyMatch (long matchId){
        Match match = MatchesController.getMatch(matchId);

        for(Player player : match.getPlayers())
        {
            messagingTemplate.convertAndSendToUser(player.getId(),"/queue/match", match);
        }
    }
}
