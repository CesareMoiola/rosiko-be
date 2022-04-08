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
@CrossOrigin
@RequestMapping(path = "match")
public class RestController {

    @Autowired
    WSServices wsService;

    @PostMapping("/update")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateMatch(@RequestBody Match match){
        MatchesController.updateMatch(match);
    }

    @PostMapping("/joinable_matches")
    public static List<Match> getJoinableMatches(){

        return MatchesController.getJoinableMatches();
    }

    @PostMapping("/get_match")
    public Match getMatch(@RequestBody Map<String, String> json){
        Match targetMatch = MatchesController.getMatch(Long.parseLong(json.get("matchId")));
        return targetMatch;
    }

    @PostMapping("/get_player")
    public Player getPlayer(@RequestBody Map<String, String> json){
        Match match = MatchesController.getMatch(Long.parseLong(json.get("matchId")));
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

    @PostMapping("/get_players")
    public List<Player> getPlayers(@RequestBody Map<String, String> json){
        Match match = MatchesController.getMatch(Long.parseLong(json.get("matchId")));
        return match.getPlayers();
    }
}
