package com.gamevenez.a21barth.a21barth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gamevenez.a21barth.a21barth.adapter.CardAdapter;
import com.gamevenez.a21barth.a21barth.adapter.ItemDecorator;
import com.gamevenez.a21barth.a21barth.model.Game;

public class Juego extends AppCompatActivity {

    private Game game;

    private static int MINIMUM_BET = 10;
    private int currentBet = MINIMUM_BET;

    private RecyclerView dealerRecyclerView, playerRecyclerView;
    private CardAdapter dealerCardAdapter, playerCardAdapter;
    private TextView dealerScoreText, playerScoreText, currentBetText, balanceText, betText, resultText;
    private Button hitButton, standButton, minusButton, plusButton, dealButton;
    private LinearLayout dealerScoreLayout, playerScoreLayout, resultLayout, gameOverLayout;

    public Juego() {
        game = new Game();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGui();
        initListeners();
    }

    private void initGui() {
        dealerRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_dealer);
        playerRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_player);
        setupRecyclerView(dealerRecyclerView);
        setupRecyclerView(playerRecyclerView);

        dealerScoreText = (TextView)findViewById(R.id.txt_dealer_score);
        playerScoreText = (TextView)findViewById(R.id.txt_player_score);
        currentBetText = (TextView)findViewById(R.id.txt_currentBet);
        currentBetText.setVisibility(View.GONE);
        balanceText = (TextView)findViewById(R.id.txt_balance);
        balanceText.setText(String.valueOf(game.getPlayerBalance()));
        betText = (TextView)findViewById(R.id.txt_bet);
        betText.setText(String.valueOf(currentBet));
        resultText = (TextView)findViewById(R.id.txt_result);

        hitButton = (Button)findViewById(R.id.btn_hit);
        standButton = (Button)findViewById(R.id.btn_stand);
        minusButton = (Button)findViewById(R.id.btn_minus);
        minusButton.setEnabled(false);
        plusButton = (Button)findViewById(R.id.btn_plus);
        dealButton = (Button)findViewById(R.id.btn_deal);

        dealerScoreLayout = (LinearLayout)findViewById(R.id.layout_dealer_score);
        dealerScoreLayout.setVisibility(View.GONE);
        playerScoreLayout = (LinearLayout)findViewById(R.id.layout_player_score);
        playerScoreLayout.setVisibility(View.GONE);
        resultLayout = (LinearLayout)findViewById(R.id.layout_result);
        gameOverLayout = (LinearLayout)findViewById(R.id.layout_gameOver);
    }

    private void initListeners() {
        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerHits();
            }
        });

        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerStands();
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseBet();
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseBet();
            }
        });

        dealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealCards();
            }
        });

        resultLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRound();
            }
        });

        gameOverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });
    }

    private void decreaseBet() {
        if (currentBet > MINIMUM_BET) {
            currentBet -= 10;
            betText.setText(String.valueOf(currentBet));

            plusButton.setEnabled(true);
            if (currentBet == MINIMUM_BET) {
                minusButton.setEnabled(false);
            }
        }
    }

    private void increaseBet() {
        int balance = game.getPlayerBalance();
        if (currentBet <= balance - 10) {
            currentBet += 10;
            betText.setText(String.valueOf(currentBet));

            minusButton.setEnabled(true);
            if (currentBet == balance) {
                plusButton.setEnabled(false);
            }
        }
    }

    private void startNewGame() {
        game.resetPlayerBalance();
        game.resetPlayersHands();
        currentBet = MINIMUM_BET;

        dealerCardAdapter.notifyDataSetChanged();
        playerCardAdapter.notifyDataSetChanged();

        currentBetText.setVisibility(View.INVISIBLE);
        balanceText.setText(String.valueOf(game.getPlayerBalance()));
        betText.setText(String.valueOf(currentBet));

        enableDealButtons(true);

        dealerScoreLayout.setVisibility(View.INVISIBLE);
        playerScoreLayout.setVisibility(View.INVISIBLE);
        gameOverLayout.setVisibility(View.INVISIBLE);
    }

    private void dealCards() {
        game.dealAgain();
        game.playerBet(currentBet);

        dealerCardAdapter = new CardAdapter(game.getDealerHand());
        playerCardAdapter = new CardAdapter(game.getPlayerHand());

        dealerRecyclerView.setAdapter(dealerCardAdapter);
        playerRecyclerView.setAdapter(playerCardAdapter);

        dealerScoreText.setText("?");
        playerScoreText.setText(String.valueOf(game.getPlayerScore()));
        balanceText.setText(String.valueOf(game.getPlayerBalance()));
        currentBetText.setText(String.valueOf(currentBet));
        currentBetText.setVisibility(View.VISIBLE);

        enableActionButtons(true);
        enableDealButtons(false);

        dealerScoreLayout.setVisibility(View.VISIBLE);
        playerScoreLayout.setVisibility(View.VISIBLE);

        if (game.playerHasBlackjack()) {
            enableActionButtons(false);
            playerBlackjack();
        }
    }

    private void newRound() {
        game.resetPlayersHands();
        currentBet = MINIMUM_BET;

        dealerCardAdapter.notifyDataSetChanged();
        playerCardAdapter.notifyDataSetChanged();

        dealerScoreText.setText("?");
        playerScoreText.setText("");
        currentBetText.setVisibility(View.GONE);
        betText.setText(String.valueOf(currentBet));

        enableActionButtons(false);
        enableDealButtons(true);

        dealerScoreLayout.setVisibility(View.INVISIBLE);
        playerScoreLayout.setVisibility(View.INVISIBLE);
        resultLayout.setVisibility(View.INVISIBLE);
    }

    private void playerHits() {
        game.dealPlayerCard();

        playerCardAdapter.notifyDataSetChanged();
        playerScoreText.setText(String.valueOf(game.getPlayerScore()));

        if (game.playerHasBlackjack()) {
            playerStands();
            enableActionButtons(false);
        } else if (game.playerHasBusted()) {
            playerBusts();
            enableActionButtons(false);
        }
    }

    private void playerStands() {
        game.dealerShowHoleCard();

        dealerCardAdapter.notifyDataSetChanged();
        dealerScoreText.setText(String.valueOf(game.getDealerScore()));

        while (game.dealerShouldDrawCard()) {
            game.dealDealerCard();

            dealerCardAdapter.notifyDataSetChanged();
            dealerScoreText.setText(String.valueOf(game.getDealerScore()));

            if (game.dealerHasBusted()) {
                dealerBusts();
                enableActionButtons(false);
                return;
            }
        }

        Game.Outcome outcome = game.getOutcome();
        enableActionButtons(false);

        switch (outcome) {
            case DEALER:
                playerLoses();
                enableActionButtons(false);
                break;
            case PLAYER:
                playerWins();
                enableActionButtons(false);
                break;
            default:
                draw();
                enableActionButtons(false);
        }
    }

    private void playerBlackjack() {
        game.playerWinBlackjack();
        resultText.setText(R.string.you_got_blackjack);
        showResult();
    }

    private void playerWins() {
        game.playerWin();
        resultText.setText(R.string.you_won);
        showResult();
    }

    private void dealerBusts() {
        game.playerWin();
        resultText.setText(R.string.dealer_busted);
        showResult();
    }

    private void draw() {
        game.draw();
        resultText.setText(R.string.draw);
        showResult();
    }

    private void playerBusts() {
        if (game.isGameOver()) {
            gameOver();
            return;
        }
        resultText.setText(R.string.you_busted);
        showResult();
    }

    private void playerLoses() {
        if (game.isGameOver()) {
            gameOver();
            return;
        }
        resultText.setText(R.string.dealer_won);
        showResult();
    }

    private void showResult() {
        resultLayout.setVisibility(View.VISIBLE);
        balanceText.setText(String.valueOf(game.getPlayerBalance()));
    }

    private void gameOver() {
        gameOverLayout.setVisibility(View.VISIBLE);
        enableActionButtons(false);
        enableDealButtons(false);
    }

    private void enableDealButtons(boolean enabled) {
        dealButton.setEnabled(enabled);
        minusButton.setEnabled(enabled);
        plusButton.setEnabled(enabled);
    }

    private void enableActionButtons(boolean enable) {
        hitButton.setEnabled(enable);
        standButton.setEnabled(enable);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(llm);
        int overlap;
        if (getResources().getDisplayMetrics().density < 3) {
            overlap = -100;
        } else {
            overlap = -150;
        }
        recyclerView.addItemDecoration(new ItemDecorator(overlap));
    }

}
