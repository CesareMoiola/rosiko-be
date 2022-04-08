package com.cm.rosiko_be.data;

import com.cm.rosiko_be.enums.CardType;
import com.cm.rosiko_be.enums.Color;

import java.util.List;

//Rappresenta una singola nazione della mappa di gioco.
public class Territory {
    private String id;                      //Id del territorio rappresentato nella mappa svg in front end
    private String name;                    //Nome del territorio
    private String continentId;             //Id del continente a cui appartiene il territorio
    private CardType cardType;              //Tipo di carta per i tris
    private List<String> neighboring;       //Lista degli id dei territori confinanti
    private Player owner;                   //Giocatore che possiede il territorio
    private Color color;                    //Colore
    private int armies = 0;                 //Numero di armate presenti nel territorio
    private boolean clickable = false;      //Indica se il territorio è cliccabile


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getNeighboring() {
        return neighboring;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.color = owner.getColor();
    }

    public int getArmies() {
        return armies;
    }

    public void setArmies(int armies) {
        this.armies = armies;
    }

    public void removeArmies(int armies) { this.armies -= armies; }

    public void addArmies(int armies) {
        this.armies += armies;
    }

    public Color getColor() {
        return color;
    }

    public String getContinentId() {
        return continentId;
    }

    public void setContinentId(String continentId) {
        this.continentId = continentId;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public boolean isBordering(Territory territory){
        boolean isBordering = false;
        for(String id : neighboring){
            if(territory.getId().equals(id)){
                isBordering = true;
                break;
            }
        }
        return isBordering;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }
}
