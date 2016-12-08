package com.gamevenez.a21barth.a21barth.model;

public class Card {

    private Suit suit;
    private Value value;
    private boolean isVisible;

    public Card(Suit suit, Value value) {
        this.suit = suit;
        this.value = value;
        this.isVisible = false;
    }

    public Suit getSuit() {
        return suit;
    }

    public Value getValue() {
        return value;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void showCard() {
        this.isVisible = true;
    }
}
