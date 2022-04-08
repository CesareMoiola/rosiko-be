package com.cm.rosiko_be.data;

import com.cm.rosiko_be.dao.DAOContinents;
import com.cm.rosiko_be.dao.DAOTerritories;
import java.util.List;

//Mappa di gioco
public class GameMap {
    private List<Continent> continents;
    private List<Territory> territories;

    public GameMap(){
        continents = DAOContinents.getContinents();
        territories = DAOTerritories.getTerritories();
    }

    public List<Continent> getContinents() {
        return continents;
    }

    public void setContinents(List<Continent> continents) {
        this.continents = continents;
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    public void setTerritories(List<Territory> territories) {
        this.territories = territories;
    }

    public Territory getTerritory(String id){
        Territory targetTerritory = null;

        for (Territory territory: territories) {
            if(territory.getId().equals(id)){
                targetTerritory = territory;
                break;
            }
        }

        return targetTerritory;
    }
}
