package com.cm.rosiko_be.controller;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import com.cm.rosiko_be.services.WSServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    MatchesController matchesController;
    int getMatchCounter = 0;


    @GetMapping("/update")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateMatch(@RequestBody Match match){
        matchesController.updateMatch(match);
    }

    @GetMapping("/joinable_matches")
    public List<Match> getJoinableMatches(){
        return matchesController.getJoinableMatches();
    }

    @GetMapping("/get_match")
    public Match getMatch(@RequestParam String matchId){
        Match targetMatch = matchesController.getMatch(Long.parseLong(matchId));
        System.out.println("GET MATCH: " + getMatchCounter);
        getMatchCounter++;
        return targetMatch;
    }

    @GetMapping("/get_player")
    public Player getPlayer(@RequestBody Map<String, String> json){
        Match match = matchesController.getMatch(Long.parseLong(json.get("matchId")));
        String playerId = json.get("playerId");
        Player targetPlayer = null;

        for (Player player : match.getPlayers()) {
            if(player.getId().equals(playerId)) {
                targetPlayer = player;
                break;
            }
        }
        return targetPlayer;
    }

    @GetMapping("/get_players")
    public List<Player> getPlayers(@RequestBody Map<String, String> json){
        Match match = matchesController.getMatch(Long.parseLong(json.get("matchId")));
        return match.getPlayers();
    }
}
