package com.cm.rosiko_be.data;

import com.cm.rosiko_be.enums.CardType;

public class Card {
    private int id;
    private String territoryId = null;
    private String territoryName = null;
    private CardType cardType;
    private boolean selected;

    public Card(int id, Territory territory, CardType cardType) {
        this.id = id;
        this.territoryId = territory.getId();
        this.territoryName = territory.getName();
        this.cardType = cardType;
    }

    public Card(int id, CardType cardType) {
        this.id = id;
        this.cardType = cardType;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    public String getTerritoryName() {
        return territoryName;
    }

    public void setTerritoryName(String territoryName) {
        this.territoryName = territoryName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
