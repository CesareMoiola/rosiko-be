package com.cm.rosiko_be.data;

import com.cm.rosiko_be.data.missions.Mission;
import com.cm.rosiko_be.enums.Color;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String name ;
    private Color color = null;
    private int availableArmies = 0;                //Armate che ha a disposizione il giocatore per essere posizionate sui territori
    private Mission mission;                        //Obiettivo da raggiungere per vincere la partita
    private int armiesPlacedThisTurn = 0;           //Armate che il giocatore ha piazzato durante il turno
    private boolean isActive = true;
    private List<Player> defeatedPlayers = new ArrayList<>();
    private boolean mustDrawACard = false;          //Se a fine turno il giocatore deve pescare una carta
    private List<Card> cards = new ArrayList<>();   //Lista di carte a disposizione del giocatore

    public Player(String id, String name){
        this.id = id;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getAvailableArmies() {
        return availableArmies;
    }

    public void setAvailableArmies(int availableArmies) {
        this.availableArmies = availableArmies;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public int getArmiesPlacedThisTurn() {
        return armiesPlacedThisTurn;
    }

    public void setArmiesPlacedThisTurn(int armiesPlacedThisTurn) {
        this.armiesPlacedThisTurn = armiesPlacedThisTurn;
    }

    public void increaseArmiesPlacedThisTurn(int increase) {
        this.armiesPlacedThisTurn += increase;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Player> getDefeatedPlayers() {
        return defeatedPlayers;
    }

    public void addDefeatedPlayer(Player player){
        defeatedPlayers.add(player);
    }

    public void setDefeatedPlayers(List<Player> defeatedPlayers) {
        this.defeatedPlayers = defeatedPlayers;
    }

    public boolean isMustDrawACard() {
        return mustDrawACard;
    }

    public void setMustDrawACard(boolean mustDrawACard) {
        this.mustDrawACard = mustDrawACard;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void addCards(List<Card> cards) {
        for(Card card : cards){
            this.cards.add(card);
        }
    }

    public void removeCard(Card card) {this.cards.remove(card);};

    public boolean hasCard(Card card) {
        boolean hasCard = false;
        for (Card currentCard : this.cards){
            if(currentCard.getId() == card.getId()){
                hasCard = true; break;
            }
        }
        return hasCard;
    }

    public Card getCard(int cardId){
        Card target = null;
        for(Card card : cards){
            if(card.getId() == cardId) {
                target = card; break;
            }
        }
        return target;
    }

    public List<Card> takeCards(){
        List<Card> cards = this.cards;
        this.cards = new ArrayList<>();
        return cards;
    }
}
