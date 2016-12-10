package com.gamevenez.a21barth.a21barth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gamevenez.a21barth.a21barth.adapter.CardAdapter;
import com.gamevenez.a21barth.a21barth.adapter.ItemDecorator;
import com.gamevenez.a21barth.a21barth.model.Game;

public class OnlineActivity extends AppCompatActivity {

    private Game game;

    private static int MINIMUM_BET = 10;
    private int currentBet = MINIMUM_BET;

    private RecyclerView rvCardsDealer, rvCards1, rvCards2;
    private CardAdapter dealerCardAdapter, playerCardAdapter1, playerCardAdapter2;
    private TextView dealerScoreText, scoreText1, scoreText2,
            currentBet1, currentBet2,
            balanceText1, balanceText2,
            betText, resultText;
    private Button hitButton, standButton, minusButton, plusButton, dealButton;
    private FrameLayout dealerScoreLayout, scoreLayout1, scoreLayout2;
    private LinearLayout resultLayout, gameOverLayout;

    public OnlineActivity() {
        game = new Game();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGui();
        initListeners();
    }

    private void initGui() {
        // Recycler views (hands)
        rvCardsDealer = (RecyclerView) findViewById(R.id.rvCardsDealer);
        rvCards1 = (RecyclerView) findViewById(R.id.rvCards1);
        rvCards2 = (RecyclerView) findViewById(R.id.rvCards2);
        setupRecyclerView(rvCardsDealer);
        setupRecyclerView(rvCards1);
        setupRecyclerView(rvCards2);

        // Score texts
        dealerScoreText = (TextView)findViewById(R.id.txtScoreDealer);
        scoreText1 = (TextView)findViewById(R.id.txtScore1);
        scoreText2 = (TextView)findViewById(R.id.txtScore2);

        // Bet in the current round
        currentBet1 = (TextView)findViewById(R.id.txtCurrentBet1);
        currentBet1.setVisibility(View.GONE);
        currentBet2 = (TextView)findViewById(R.id.txtCurrentBet2);
        currentBet2.setVisibility(View.GONE);

        // Balance texts
        balanceText1 = (TextView)findViewById(R.id.txtBalance1);
        balanceText1.setText(String.valueOf(game.getPlayerBalance()));
        balanceText2 = (TextView)findViewById(R.id.txtBalance2);
        balanceText2.setText(String.valueOf(game.getPlayerBalance()));

        // Bet text in options
        betText = (TextView)findViewById(R.id.txtBetOptions);
        betText.setText(String.valueOf(currentBet));
        // Result messages
        resultText = (TextView)findViewById(R.id.txtResult);

        // Option buttons
        hitButton = (Button)findViewById(R.id.btnHit);
        standButton = (Button)findViewById(R.id.btnStand);
        minusButton = (Button)findViewById(R.id.btnMinus);
        minusButton.setEnabled(false);
        plusButton = (Button)findViewById(R.id.btnPlus);
        dealButton = (Button)findViewById(R.id.btnDeal);

        dealerScoreLayout = (FrameLayout)findViewById(R.id.layoutDealerScore);
        dealerScoreLayout.setVisibility(View.GONE);
        scoreLayout1 = (FrameLayout)findViewById(R.id.layoutScore1);
        scoreLayout1.setVisibility(View.GONE);
        scoreLayout2 = (FrameLayout)findViewById(R.id.layoutScore2);
        scoreLayout2.setVisibility(View.GONE);

        // Layouts to show messages
        resultLayout = (LinearLayout)findViewById(R.id.layoutResult);
        gameOverLayout = (LinearLayout)findViewById(R.id.layoutGameOver);
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
        playerCardAdapter1.notifyDataSetChanged();

        currentBet1.setVisibility(View.INVISIBLE);
        balanceText1.setText(String.valueOf(game.getPlayerBalance()));
        betText.setText(String.valueOf(currentBet));

        enableDealButtons(true);

        dealerScoreLayout.setVisibility(View.INVISIBLE);
        scoreLayout1.setVisibility(View.INVISIBLE);
        gameOverLayout.setVisibility(View.INVISIBLE);
    }

    private void dealCards() {
        game.dealAgain();
        game.playerBet(currentBet);

        dealerCardAdapter = new CardAdapter(game.getDealerHand());
        playerCardAdapter1 = new CardAdapter(game.getPlayerHand());

        rvCardsDealer.setAdapter(dealerCardAdapter);
        rvCards1.setAdapter(playerCardAdapter1);

        dealerScoreText.setText("?");
        scoreText1.setText(String.valueOf(game.getPlayerScore()));
        balanceText1.setText(String.valueOf(game.getPlayerBalance()));
        currentBet1.setText(String.valueOf(currentBet));
        currentBet1.setVisibility(View.VISIBLE);

        enableActionButtons(true);
        enableDealButtons(false);

        dealerScoreLayout.setVisibility(View.VISIBLE);
        scoreLayout1.setVisibility(View.VISIBLE);

        if (game.playerHasBlackjack()) {
            enableActionButtons(false);
            playerBlackjack();
        }
    }

    private void newRound() {
        game.resetPlayersHands();
        currentBet = MINIMUM_BET;

        dealerCardAdapter.notifyDataSetChanged();
        playerCardAdapter1.notifyDataSetChanged();

        dealerScoreText.setText("?");
        scoreText1.setText("");
        currentBet1.setVisibility(View.GONE);
        betText.setText(String.valueOf(currentBet));

        enableActionButtons(false);
        enableDealButtons(true);

        dealerScoreLayout.setVisibility(View.INVISIBLE);
        scoreLayout1.setVisibility(View.INVISIBLE);
        resultLayout.setVisibility(View.INVISIBLE);
    }

    private void playerHits() {
        game.dealPlayerCard();

        playerCardAdapter1.notifyDataSetChanged();
        scoreText1.setText(String.valueOf(game.getPlayerScore()));

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
        balanceText1.setText(String.valueOf(game.getPlayerBalance()));
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
