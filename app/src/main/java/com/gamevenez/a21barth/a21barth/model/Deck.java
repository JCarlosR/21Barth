package com.gamevenez.a21barth.a21barth.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {

    private LinkedList<Card> cards = new LinkedList<>();

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Value value : Value.values()) {
                cards.add(new Card(suit, value));
            }
        }
        Collections.shuffle(cards);
    }

    public Card getRandomCard() {
        return cards.pop();
    }

    public Hand createStartHand() {
        List<Card> twoCards = new ArrayList<>(Arrays.asList(getRandomCard(), getRandomCard()));
        return new Hand(twoCards);
    }
}
