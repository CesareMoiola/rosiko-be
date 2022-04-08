package com.cm.rosiko_be.data;

import java.util.List;

public class Continent {
    private String id;
    private String name;
    private int bonusArmies;
    private List<String> territories;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBonusArmies() {
        return bonusArmies;
    }

    public List<String> getTerritories(){return territories; }
}
