package com.gamevenez.a21barth.a21barth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamevenez.a21barth.a21barth.adapter.CardAdapter;
import com.gamevenez.a21barth.a21barth.adapter.ItemDecorator;
import com.gamevenez.a21barth.a21barth.model.Game;
import com.gamevenez.a21barth.a21barth.model.GameOnline;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnlineActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, RoomUpdateListener, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener, OnInvitationReceivedListener {

    /*
    * LOCAL GAME VARIABLES
    */
    private GameOnline game;

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
        game = new GameOnline();
    }

    /*
    * REAL TIME VARIABLES
    */
    final static String TAG = "OnlineActivityLog";

    // Request codes
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    // Request code used to invoke sign in user
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // Room ID; null if we're not playing
    String mRoomId = null;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupGoogleApiClient();
        initGui();
        initListeners();
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it
        leaveRoom();

        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        // switchToScreen(R.id.screen_wait);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG, "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
        // mSecondsLeft = 0;
        stopKeepingScreenOn();

        if (mRoomId != null) {
            Log.d(TAG, "Leaving room");
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    private void setupGoogleApiClient()
    {
        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
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
        balanceText1.setText(String.valueOf(game.getPlayer1Balance()));
        balanceText2 = (TextView)findViewById(R.id.txtBalance2);
        balanceText2.setText(String.valueOf(game.getPlayer2Balance()));

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

        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void initListeners() {
        // Local actions

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
        /*int balance = game.getPlayerBalance();
        if (currentBet <= balance - 10) {
            currentBet += 10;
            betText.setText(String.valueOf(currentBet));

            minusButton.setEnabled(true);
            if (currentBet == balance) {
                plusButton.setEnabled(false);
            }
        }*/
    }

    private void startNewGame() {
        Log.d(TAG, "startNewGame, my id => " + mMyId);

        game.resetPlayersBalance();
        game.resetPlayersHands();
        currentBet = MINIMUM_BET;
/*
        dealerCardAdapter.notifyDataSetChanged();
        playerCardAdapter1.notifyDataSetChanged();

        currentBet1.setVisibility(View.INVISIBLE);
        balanceText1.setText(String.valueOf(game.getPlayerBalance()));
        betText.setText(String.valueOf(currentBet));

        enableDealButtons(true);

        dealerScoreLayout.setVisibility(View.INVISIBLE);
        scoreLayout1.setVisibility(View.INVISIBLE);
        gameOverLayout.setVisibility(View.INVISIBLE);
        */
    }

    private void dealCards() {
        game.dealAgain();
        // game.playerBet(currentBet);

        dealerCardAdapter = new CardAdapter(game.getDealerHand());
        // playerCardAdapter1 = new CardAdapter(game.getPlayerHand());

        rvCardsDealer.setAdapter(dealerCardAdapter);
        rvCards1.setAdapter(playerCardAdapter1);

        dealerScoreText.setText("?");
        // scoreText1.setText(String.valueOf(game.getPlayerScore()));
        // balanceText1.setText(String.valueOf(game.getPlayerBalance()));
        currentBet1.setText(String.valueOf(currentBet));
        currentBet1.setVisibility(View.VISIBLE);

        enableActionButtons(true);
        enableDealButtons(false);

        dealerScoreLayout.setVisibility(View.VISIBLE);
        scoreLayout1.setVisibility(View.VISIBLE);
/*
        if (game.playerHasBlackjack()) {
            enableActionButtons(false);
            playerBlackjack();
        }*/
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
        /*game.dealPlayerCard();

        playerCardAdapter1.notifyDataSetChanged();
        scoreText1.setText(String.valueOf(game.getPlayerScore()));

        if (game.playerHasBlackjack()) {
            playerStands();
            enableActionButtons(false);
        } else if (game.playerHasBusted()) {
            playerBusts();
            enableActionButtons(false);
        }*/
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

        /* Game.Outcome outcome = game.getOutcome();
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
        }*/
    }

    private void playerBlackjack() {
        // game.playerWinBlackjack();
        resultText.setText(R.string.you_got_blackjack);
        showResult();
    }

    private void playerWins() {
        // game.playerWin();
        resultText.setText(R.string.you_won);
        showResult();
    }

    private void dealerBusts() {
        // game.playerWin();
        resultText.setText(R.string.dealer_busted);
        showResult();
    }

    private void draw() {
        // game.draw();
        resultText.setText(R.string.draw);
        showResult();
    }

    private void playerBusts() {
        /*if (game.isGameOver()) {
            gameOver();
            return;
        }
        resultText.setText(R.string.you_busted);
        showResult();*/
    }

    private void playerLoses() {
        /*if (game.isGameOver()) {
            gameOver();
            return;
        }
        resultText.setText(R.string.dealer_won);
        showResult();*/
    }

    private void showResult() {
        resultLayout.setVisibility(View.VISIBLE);
        // balanceText1.setText(String.valueOf(game.getPlayerBalance()));
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

    /*
    * USER OPTIONS
    */
    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.button_sign_in:
                // start the sign-in flow
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // Log.d(TAG, "sign out clicked");
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                // the last two params: MIN players and MAX players to invite
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        // resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    /*
    * API INTEGRATION SECTION
    */
    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        // resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    /*
    * CALLBACKS SECTION: GoogleApiClient.ConnectionCallbacks
    */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");

            Invitation inv = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }

        switchToMainScreen();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox)
                // we are ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startNewGame();
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            // Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        // Log.d(TAG, "Invitee count: " + invitees.size());

        // get the auto match criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        // resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI (the player can pick an invitation to accept)
    // We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            // Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        // Invitation inbox UI succeeded
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        final String invitationId = inv.getInvitationId();
        acceptInviteToRoom(invitationId);
    }

    /*
    * CALLBACKS SECTION: RoomUpdateListener
    */

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }


    /*
    * CALLBACKS SECTION: GoogleApiClient.OnConnectionFailedListener
    */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.screen_sign_in);
    }


    /*
    * CALLBACKS SECTION: OnInvitationReceivedListener
    */
    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(mCurScreen); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {

        if (mIncomingInvitationId.equals(invitationId)&&mIncomingInvitationId!=null) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen); // This will hide the invitation popup
        }

    }

    /*
    * CALLBACKS SECTION: RoomStatusUpdateListener
    */
    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        updateRoom(room);
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if (mRoomId == null)
            mRoomId = room.getRoomId();

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {
    }

    @Override
    public void onP2PDisconnected(String s) {
    }

    /*
    * COMMUNICATIONS SECTION
    */

    // Score of other participants. We update this as we receive their scores from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // Called when we receive a real-time message from the network.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);
        /*
        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            int existingScore = mParticipantScore.containsKey(sender) ?
                    mParticipantScore.get(sender) : 0;
            int thisScore = (int) buf[1];
            if (thisScore > existingScore) {
                // this check is necessary because packets may arrive out of
                // order, so we
                // should only ever consider the highest score we received, as
                // we know in our
                // game there is no way to lose points. If there was a way to
                // lose points,
                // we'd have to add a "serial number" to the packet.
                mParticipantScore.put(sender, thisScore);
            }

            // update the scores on the screen
            updatePeerScoresDisplay();

            // if it's a final score, mark this participant as having finished
            // the game
            if ((char) buf[0] == 'F') {
                mFinishedParticipants.add(rtm.getSenderParticipantId());
            }
        }*/
    }



    /*
    * UI SECTION
    */

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out
    };
    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in, R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_main);
        }
        else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else {
            // only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            updatePeerScoresDisplay();
        }
    }
    // updates the screen with the scores from our peers
    void updatePeerScoresDisplay() {
        Toast.makeText(this, "updates the screen with the scores; myId is " + mMyId, Toast.LENGTH_SHORT).show();

        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();
                if (pid.equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                Toast.makeText(this, "pid => " + pid, Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
    * MISCELLANEOUS SECTION
    */

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the  handshake when setting up a game, because if the screen turns off,
    // the game will be cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

}
