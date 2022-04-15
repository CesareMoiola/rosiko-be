package com.cm.rosiko_be.data;

import java.util.Map;

public class ArmiesToPlace {
    private Long matchId;
    private Map<String, Integer> armies;

    public ArmiesToPlace(){super();}

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Map<String, Integer> getArmies() {
        return armies;
    }

    public void setArmies(Map<String, Integer> armies) {
        this.armies = armies;
    }
}
