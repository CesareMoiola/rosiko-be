package com.cm.rosiko_be.controller;

import com.cm.rosiko_be.dao.DAOTerritories;
import com.cm.rosiko_be.data.Continent;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Territory;
import com.cm.rosiko_be.enums.MatchState;
import com.cm.rosiko_be.dao.DAOContinents;
import java.util.ArrayList;
import java.util.List;
import static com.cm.rosiko_be.controller.MatchController.MAX_PLAYERS;

//Gestisce tutte le partite aperte
public class MatchesController {

    //Lista di tutte le partite aperte
    private static List<Match> matchList = new ArrayList<Match>();

    public static List<Match> getJoinableMatches(){

        //PROVA
        List<Continent> continents = DAOContinents.getContinents();
        List<Territory> territories = DAOTerritories.getTerritories();

        List<Match> joinableMatches = new ArrayList<Match>();
        for(Match match : matchList){
            if(
                    (match.getState().equals(MatchState.WAITING)
                            || match.getState().equals(MatchState.READY))
                            && match.getPlayers().size() < MAX_PLAYERS
            ) joinableMatches.add(match);
        }
        return joinableMatches;
    }

    public static void updateMatch(Match match){
        int index = matchList.indexOf(getMatch(match.getId()));
        matchList.set(index,match);
    }

    public static Match getMatch(long id){
        for (Match currentMatch : matchList) {
            if(currentMatch.getId() == id) return currentMatch;
        }
        return null;
    }

    public static Match newMatch(String name, String password){
        Match match = new Match(generateMatchId(),name);
        if(password!=null) match.setPassword(password);
        matchList.add(match);
        return match;
    }

    public static long generateMatchId(){
        long id = 1;

        for (int i=0; i<matchList.size(); i++) {
            if(matchList.get(i).getId() == id){
                id++;
                i=0;
            }
        }
        return id;
    }
}
