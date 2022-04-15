package com.cm.rosiko_be.services;

import com.cm.rosiko_be.controller.MatchController;
import com.cm.rosiko_be.data.Match;
import com.cm.rosiko_be.data.Player;
import com.cm.rosiko_be.enums.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class TimerService {

    private Timer timer = null;
    private long delay = MatchController.MAX_INACTIVITY_PERIOD * 60000;

    @Autowired
    public WSServices wsServices;


    public void startTimer(MatchController matchController){
        Match match = matchController.getMatch();

        if(timer != null) timer.cancel();
        if(match.getStage().equals(Stage.GAME_OVER)) return;

        timer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                Match match = matchController.getMatch();
                Player player = match.getTurnPlayer();

                player.setActive(false);
                matchController.nextStage();
                if(match.getWinner() == null) wsServices.notifyMatch(match.getId());
                stopTimer();
            }
        };

        timer.schedule(task,delay);
    }

    public void stopTimer(){if(timer != null) timer.cancel();}
}
