package com.cm.rosiko_be.controller;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.services.WSServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;
import java.util.Map;


/*Questa classe si occupa di recepire i messaggi arrivati tramite websocket e rispondere*/
@Controller
public class SocketController {

    private final int RESPONSE_DELAY = 0; //Secondi di attesa prima di inviare la risposta


    @Autowired
    public MatchController matchController;

    @Autowired
    public MatchesController matchesController;

    @Autowired
    public WSServices wsService;

    /*Manda il match aggiornato ai player partecipanti*/
    @MessageMapping("/match")
    public void getMatch(@Payload Map<String, String> json) {
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Crea un nuovo match in stato waiting con iscritto il player che ha creato la partita.
     * Il match viene aggiunto alla lista matchList e notifica gli utenti dell'aggiornamento*/
    @MessageMapping("/new_match")
    @SendToUser("/queue/new_match")
    public Match newMatch(Map<String, String> json, Principal principal) throws InterruptedException{
        Match match = matchesController.newMatch(json.get("matchName"), json.get("password"));
        try {
            match.addNewPlayer(json.get("playerName"), principal.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        wsService.notifyJoinableMatches();
        return match;
    }

    @MessageMapping("/join_match")
    public void joinMatch(@RequestBody Map<String, String> json, Principal principal){
        Match match = matchesController.getMatch(Long.parseLong(json.get("matchId")));
        try {
            match.addNewPlayer(json.get("playerName"), principal.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        wsService.notifyMatch(match.getId(), principal.getName());
        wsService.notifyJoinableMatches();
    }

    //Viene dato il via alla partita
    @MessageMapping("/start_match")
    public void startMatch(@RequestBody Map<String, String> json){
        Match match = matchesController.getMatch(Long.parseLong(json.get("matchId")));
        matchController.setMatch(match);
        matchController.startMatch();

        wsService.notifyMatch(match.getId());
        wsService.notifyJoinableMatches();
    }

    /*Piazza un armata*/
    @MessageMapping("/placeArmy")
    public void placeArmy(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.placeArmy(json.get("territoryId"));

        try {
            Thread.sleep(RESPONSE_DELAY * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Seleziona il territorio dal quale attaccare*/
    @MessageMapping("/select_attacker")
    public void selectAttacker(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.selectAttacker(json.get("territoryId"));
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Seleziona il territorio da attaccare*/
    @MessageMapping("/select_defender")
    public void selectDefender(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.selectDefender(json.get("territoryId"));
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Deseleziona il territorio*/
    @MessageMapping("/deselect_territory")
    public void deselectTerritory(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.deselectTerritory(json.get("territoryId"));
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Attack*/
    @MessageMapping("/attack")
    public void attack(@Payload Map<String, String> json) {
        int numberOfAttackerDice = 0;
        try{numberOfAttackerDice = Integer.parseInt(json.get("numberOfAttackerDice"));}
        catch (NumberFormatException e){
            e.printStackTrace();
        }

        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.attack(numberOfAttackerDice);

        try {
            Thread.sleep(RESPONSE_DELAY * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Move armies from territory to another territory*/
    @MessageMapping("/move_armies")
    public void moveArmies(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.moveArmies(Integer.parseInt(json.get("armies")));

        try {
            Thread.sleep(RESPONSE_DELAY * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Change stage to displacement*/
    @MessageMapping("/displacement_stage")
    public void displacementStage(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.displacementStage();
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Select territory from which to move armies*/
    @MessageMapping("/select_territory_from")
    public void selectTerritoryFrom(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.selectTerritoryFrom(json.get("territoryId"));
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Select territory to which to move armies*/
    @MessageMapping("/select_territory_to")
    public void selectTerritoryTo(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.selectTerritoryTo(json.get("territoryId"));
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Select territory to which to move armies*/
    @MessageMapping("/ends_turn")
    public void endsTurn(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        matchController.endsTurn();
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }

    /*Play a cards set*/
    @MessageMapping("/play_cards")
    public void playCards(@Payload Map<String, String> json) {
        matchController.setMatch(matchesController.getMatch(Long.parseLong(json.get("matchId"))));
        Integer[] cardsId = {
                Integer.parseInt(json.get("card_1")),
                Integer.parseInt(json.get("card_2")),
                Integer.parseInt(json.get("card_3"))
        };
        String playerId = json.get("playerId");

        matchController.playCards(playerId, cardsId);
        wsService.notifyMatch(Long.parseLong(json.get("matchId")));
    }
}
