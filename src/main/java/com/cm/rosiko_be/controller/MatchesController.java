package com.cm.rosiko_be.controller;

import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.enums.MatchState;
import com.cm.rosiko_be.enums.Stage;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.cm.rosiko_be.controller.MatchController.MAX_PLAYERS;
import static com.cm.rosiko_be.enums.MatchState.*;

//Gestisce tutte le partite aperte
@Component
public class MatchesController {

    final static int HOURS_BEFORE_REMOVE_MATCH = 1; //Ore da attendere prima di rimuovere il match da quando va in game over
    final static int MAX_MATCH_DURATION = 48;       //Massima durata di una partita in ore dopo la quale il match verrà rimosso

    //Lista di tutte le partite aperte
    private static List<Match> matchList = new ArrayList<>();

    public List<Match> getJoinableMatches(){
        List<Match> joinableMatches = new ArrayList<Match>();
        for(Match match : matchList){
            if(
                    (match.getState().equals(WAITING)
                            || match.getState().equals(MatchState.READY))
                            && match.getPlayers().size() < MAX_PLAYERS
            ) joinableMatches.add(match);
        }
        return joinableMatches;
    }

    public void updateMatch(Match match){
        int index = matchList.indexOf(getMatch(match.getId()));
        matchList.set(index,match);
    }

    public Match getMatch(long id){
        for (Match currentMatch : matchList) {
            if(currentMatch.getId() == id) return currentMatch;
        }
        return null;
    }

    public Match newMatch(String name, String password){
        Match match = new Match(generateMatchId(),name);
        if(password!=null) match.setPassword(password);
        matchList.add(match);
        removeMatches();
        return match;
    }

    public long generateMatchId(){
        long id = 1;

        for (int i=0; i<matchList.size(); i++) {
            if(matchList.get(i).getId() == id){
                id++;
                i=0;
            }
        }
        return id;
    }

    //Rimuove tutti i match inattivi o terminati
    public static void removeMatches(){
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        int counter = 0;
        List<Match> newMatchList = new ArrayList<>(matchList);

        for (Match match : matchList){
            long diffInMillies = Math.abs(today.getTime() - match.getDate().getTime());
            long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if(match.getStage().equals(Stage.GAME_OVER)){
                if(diff >= HOURS_BEFORE_REMOVE_MATCH) newMatchList.remove(match);
            }
            else{
                if(diff >= MAX_MATCH_DURATION) newMatchList.remove(match);
            }
        }

        counter = matchList.size() - newMatchList.size();
        matchList = newMatchList;

        if(counter > 0){
            System.out.println("Matches removed: " + counter + ", matches active: " + matchList.size());
        }
    }

    //Rimuove un giocatore dalla waiting room di un match
    public void leavesMatch(long matchId, String playerId){
        Match match = getMatch(matchId);
        if(match.getState().equals(WAITING) || match.getState().equals(READY)){
            match.removePlayer(playerId);
        }
    }
}
