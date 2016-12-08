package com.gamevenez.a21barth.a21barth.model;

public class Player extends Participant {

    private int startBalance;
    private int balance;
    private int currentBet;

    public Player(Hand hand, int balance) {
        super(hand);
        this.balance = balance;
        this.startBalance = balance;
        showAllCards();
    }

    @Override
    public void setHand(Hand hand) {
        super.setHand(hand);
        showAllCards();
    }

    public int getBalance() {
        return balance;
    }

    public void resetBalance() {
        this.balance = startBalance;
    }

    public void bet(int bet) {
        currentBet = bet;
        balance -= bet;
    }

    public void win() {
        balance += 2 * currentBet;
    }

    public void winBlackjack() {
        balance += 2.5 * currentBet;
    }

    public void draw() {
        balance += currentBet;
    }

    public boolean isGameOver() {
        return balance < 10;
    }

    private void showAllCards() {
        for (Card card : getHand().getCards()) {
            card.showCard();
        }
    }
}
