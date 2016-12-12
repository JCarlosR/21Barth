package com.gamevenez.a21barth.a21barth.model;

public class GameOnline {

    private Dealer dealer;
    private Player player1;
    private Player player2;
    private Deck deck;
    private static final int INITIAL_MONEY = 100;

    public enum Outcome {
        PLAYER, DEALER, DRAW
    }

    public GameOnline() {
        deck = new Deck();
        dealer = new Dealer(deck.createStartHand());
        player1 = new Player(deck.createStartHand(), INITIAL_MONEY);
        player2 = new Player(deck.createStartHand(), INITIAL_MONEY);
    }

    public void dealAgain() {
        deck = new Deck();
        dealer.setHand(deck.createStartHand());
        player1.setHand(deck.createStartHand());
        player2.setHand(deck.createStartHand());
    }

    public Hand getDealerHand() {
        return dealer.getHand();
    }
    public void setDealerHand(Hand hand) {
        dealer.setHand(hand);
    }

    public Hand getPlayer1Hand() {
        return player1.getHand();
    }
    public void setPlayer1Hand(Hand hand) {
        player1.setHand(hand);
    }

    public Hand getPlayer2Hand() {
        return player2.getHand();
    }
    public void setPlayer2Hand(Hand hand) {
        player2.setHand(hand);
    }

    public int getDealerScore() {
        return dealer.getHand().getScore();
    }

    public int getPlayer1Score() {
        return player1.getHand().getScore();
    }

    public int getPlayer2Score() {
        return player2.getHand().getScore();
    }

    public int getPlayer1Balance() {
        return player1.getBalance();
    }

    public int getPlayer2Balance() {
        return player2.getBalance();
    }

    public boolean player1HasBlackjack() {
        return player1.hasBlackjack();
    }

    public boolean player2HasBlackjack() {
        return player2.hasBlackjack();
    }

    public boolean dealerHasBusted() {
        return dealer.hasBusted();
    }

    public boolean player1HasBusted() {
        return player1.hasBusted();
    }

    public boolean player2HasBusted() {
        return player2.hasBusted();
    }

    public boolean isPlayer1GameOver() {
        return player1.isGameOver();
    }

    public boolean isPlayer2GameOver() {
        return player2.isGameOver();
    }

    public boolean dealerShouldDrawCard() {
        return dealer.shouldDrawCard();
    }

    public void dealerShowHoleCard() {
        dealer.showHoleCard();
    }

    public void player1Bet(int bet) {
        player1.bet(bet);
    }

    public void player2Bet(int bet) {
        player2.bet(bet);
    }

    public void player1WinBlackjack() {
        player1.winBlackjack();
    }

    public void player2WinBlackjack() {
        player2.winBlackjack();
    }

    public void player1Win() {
        player1.win();
    }

    public void player2Win() {
        player2.win();
    }

    public void player1Draw() {
        player1.draw();
    }

    public void player2Draw() {
        player2.draw();
    }

    public void dealDealerCard() {
        dealer.dealCard(deck.getRandomCard());
    }

    public Card dealPlayer1Card() {
        Card randomCard = deck.getRandomCard();
        player1.dealCard(randomCard);
        return randomCard;
    }
    public void setPlayer1CardDealt(Card cardDealt) {
        player1.dealCard(cardDealt);
    }
    public Card dealPlayer2Card() {
        Card randomCard = deck.getRandomCard();
        player2.dealCard(randomCard);
        return randomCard;
    }
    public void setPlayer2CardDealt(Card cardDealt) {
        player2.dealCard(cardDealt);
    }

    public void resetPlayersHands() {
        dealer.resetHand();
        player1.resetHand();
        player2.resetHand();
    }

    public void resetPlayersBalance() {
        player1.resetBalance();
        player2.resetBalance();
    }

    public Outcome getOutcome1() {
        int playerScore = player1.getHand().getScore();
        int dealerScore = dealer.getHand().getScore();

        if (playerScore == dealerScore) {
            return Outcome.DRAW;
        }

        return (playerScore > dealerScore) ? Outcome.PLAYER : Outcome.DEALER;
    }

    public Outcome getOutcome2() {
        int playerScore = player2.getHand().getScore();
        int dealerScore = dealer.getHand().getScore();

        if (playerScore == dealerScore) {
            return Outcome.DRAW;
        }

        return (playerScore > dealerScore) ? Outcome.PLAYER : Outcome.DEALER;
    }
}
