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

    public Card(byte suitByte, byte valueByte) {
        switch (suitByte) {
            case 'H': this.suit = Suit.HEARTS; break;
            case 'D': this.suit = Suit.DIAMONDS; break;
            case 'C': this.suit = Suit.CLUBS; break;
            case 'S': this.suit = Suit.SPADES; break;
        }
        switch (valueByte) {
            case 1: this.value = Value.ACE; break;
            case 2: this.value = Value.TWO; break;
            case 3: this.value = Value.THREE; break;
            case 4: this.value = Value.FOUR; break;
            case 5: this.value = Value.FIVE; break;
            case 6: this.value = Value.SIX; break;
            case 7: this.value = Value.SEVEN; break;
            case 8: this.value = Value.EIGHT; break;
            case 9: this.value = Value.NINE; break;
            case 10: this.value = Value.TEN; break;
            case 11: this.value = Value.JACK; break;
            case 12: this.value = Value.QUEEN; break;
            case 13: this.value = Value.KING; break;
        }
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

    public byte getSuitByte() {
        switch (suit) {
            case HEARTS:
                return (byte) 'H';
            case DIAMONDS:
                return (byte) 'D';
            case CLUBS:
                return (byte) 'C';
            default:
                return (byte) 'S';
        }
    }

    public byte getValueByte() {
        return (byte) value.getValue();
    }
}
