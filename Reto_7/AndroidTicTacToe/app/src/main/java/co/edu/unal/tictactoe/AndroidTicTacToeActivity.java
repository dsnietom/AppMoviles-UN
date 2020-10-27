package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    private Button mBoardButtons[];

    // Various text displayed
    private TextView mInfoTextView;

    // Disabled grid
    private boolean mGameOver;
    private boolean mWaiting;
    private boolean mSoundOn;

    // Sound
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    // Preferences
    private SharedPreferences mPrefs;

    //static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

    private BoardView mBoardView;


    // Variables modo multiplayer
    private boolean mGameMultiplayer = false;
    private boolean mTurn;

    //Button button;

    String playerName = "";
    String roomName = "";
    String role = "";
    String message = "";
    int opponent = -1;

    FirebaseDatabase database;
    DatabaseReference messageRef;
    DatabaseReference opponentRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_tic_tac_toe);

        //button = findViewById(R.id.button);
        //button.setEnabled(false);

        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            roomName = extras.getString("roomName");
            mGameMultiplayer = Boolean.TRUE;
            mGameOver = Boolean.TRUE;
            if (roomName.equals(playerName)){
                role = "host";
            } else {
                role = "guest";
            }
        }

        /*
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                message = role + ":Poked!";
                messageRef.setValue(message);
            }
        });*/

        // Escucha el mensaje entrante en modo multiplayer
        if (mGameMultiplayer){
            messageRef = database.getReference("rooms/" + roomName + "/message");
            opponentRef = database.getReference("rooms/" + roomName + "/move");
            message = role + ":Poked!";
            messageRef.setValue(message);
            opponentRef.setValue(opponent);
            addRoomEventListener();
        }




        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];

        mInfoTextView = (TextView) findViewById(R.id.information);

        startNewGame();

        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);

        // Restore the scores from the persistent preference data source
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level",
                getResources().getString(R.string.difficulty_harder));
        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

    }

    public void onClickMenu(View view) {
        if (view.getId() == R.id.new_game){
            startNewGame();
        }
        if (view.getId() == R.id.settings_game){
            //showDialog(DIALOG_DIFFICULTY_ID);
            startActivityForResult(new Intent(this, Settings.class), 0);
        }
        if (view.getId() == R.id.quit_game){
            showDialog(DIALOG_QUIT_ID);
        }
    }

    // Set up the game board.
    private void startNewGame() {

        mGame.clearBoard();

        mBoardView.invalidate();   // Redraw the board


        // Human goes first
        mInfoTextView.setTextColor(Color.rgb(10,10,10));
        mInfoTextView.setElegantTextHeight(true);
        mInfoTextView.setText("You go first.");

        mGameOver = Boolean.FALSE;

        if (mGameMultiplayer){
            opponent = -1;
            opponentRef.setValue(opponent);
        }

    }    // End of startNewGame


    // Sounds
    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sword);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.swish);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }


    // Handles clicks on the game board buttons
    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        // Este ya no se usa
        public void onClick(View view) {
            if (!mGameOver) {
                if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);

                    // If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText("It's Android's turn.");
                        int move = mGame.getComputerMove();
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                        winner = mGame.checkForWinner();
                    }

                    if (winner == 0) {
                        mInfoTextView.setText("It's your turn.");
                    }
                    else if (winner == 1) {
                        mInfoTextView.setText("It's a tie!");
                        mGameOver = Boolean.TRUE;
                        mInfoTextView.setTextColor(Color.rgb(0,0,255));
                    }
                    else if (winner == 2) {
                        mInfoTextView.setText("You won!");
                        mGameOver = Boolean.TRUE;
                        mInfoTextView.setTextColor(Color.rgb(0,255,0));
                    } else {
                        mInfoTextView.setText("Android won!");
                        mGameOver = Boolean.TRUE;
                        mInfoTextView.setTextColor(Color.rgb(255,0,0));
                    }
                }
            }
        }
    }

    private boolean setMoveOld(char player, int location) {

        if (mGame.setMove(player,location)) {
            mBoardView.invalidate();   // Redraw the board
            return true;
        }
        return false;
    }

    private boolean setMove(char player, int location) {
        System.out.println("Trying to set the move.");

        if(player == TicTacToeGame.HUMAN_PLAYER){
            if(mSoundOn)
                mHumanMediaPlayer.start();    // Play the sound effect
        }
        if(player == TicTacToeGame.COMPUTER_PLAYER){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mWaiting = true;
                }
            }, 4000);
            if(mSoundOn)
                mComputerMediaPlayer.start();    // Play the sound effect
        }

        mWaiting = false;

        if (mGame.setMove(player, location)) {
            System.out.println("Trying to invalidate the mBoardView.");
            mBoardView.invalidate();   // Redraw the board
            System.out.println("Board redrawn, allegedly.");
            return true;
        }
        return false;
    }

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameMultiplayer && !mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{
                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText("It's Android's turn.");
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }if (winner == 0) {
                    mInfoTextView.setText("It's your turn!");
                }
                else if (winner == 1) {
                    mInfoTextView.setText("It's a tie!");
                    mGameOver = Boolean.TRUE;
                    mInfoTextView.setTextColor(Color.rgb(0,0,255));
                }
                else if (winner == 2) {
                    //mInfoTextView.setText("You won!");
                    String defaultMessage = getResources().getString(R.string.result_human_wins);
                    mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
                    mGameOver = Boolean.TRUE;
                    mInfoTextView.setTextColor(Color.rgb(0,255,0));
                } else {
                    mInfoTextView.setText("Android won!");
                    mGameOver = Boolean.TRUE;
                    mInfoTextView.setTextColor(Color.rgb(255,0,0));
                }
            }


            if (mGameMultiplayer && !mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{
                mGameOver = Boolean.TRUE;
                mTurn = Boolean.FALSE;
                opponent = pos;
                opponentRef.setValue(opponent);
                // If no winner yet, let the computer make a move
                checkWinner();
            }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    //@Override
    public boolean onCreateOptionsMenuOld(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add("New Game");
        return true;
    }
    //@Override
    public boolean onOptionsItemSelectedOld(MenuItem item) {
        startNewGame();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;

            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CANCELED) {
            // Apply potentially new settings

            mSoundOn = mPrefs.getBoolean("sound", true);

            String difficultyLevel = mPrefs.getString("difficulty_level",
                    getResources().getString(R.string.difficulty_harder));

            if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
            else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
            else
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
        }
    }



    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            /*case DIALOG_DIFFICULTY_ID:

                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};

                // TODO: Set selected, an integer (0 to n-1), for the Difficulty dialog.
                // selected is the radio button that should be selected.
                int selected = 2;

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();   // Close dialog

                                // TODO: Set the diff level of mGame based on which item was selected.
                                if (item==0)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                if (item==1)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                if (item==2)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

                                startNewGame();
                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();

                break;*/

            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();

                break;
        }
        return dialog;
    }



    // Funciones multiplayer
    private void addRoomEventListener() {
        opponentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue(Integer.class) != -1){
                    int move = snapshot.getValue(Integer.class);
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    if (mTurn){
                        mGameOver = Boolean.FALSE;
                        checkWinner();
                    }
                    mTurn = Boolean.TRUE;
                } else {
                    startNewGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Mensaje recibido
                if (role.equals("host")){
                    if(dataSnapshot.getValue(String.class).contains("guest:")){
                        //button.setEnabled(true);
                        Toast.makeText(AndroidTicTacToeActivity.this, "" +
                                dataSnapshot.getValue(String.class).replace("guest:",""), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(dataSnapshot.getValue(String.class).contains("host:")){
                        //button.setEnabled(true);
                        Toast.makeText(AndroidTicTacToeActivity.this, "" +
                                dataSnapshot.getValue(String.class).replace("host:",""), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error, volver a intentar
                messageRef.setValue(message);
            }
        });
    }

    private void checkWinner(){
        int winner = mGame.checkForWinner();
        if (winner == 0) {
            if (!mTurn){
                mInfoTextView.setText("It's opponent turn.");
            } else {
                mInfoTextView.setText("It's your turn.");
            }
        }
        else if (winner == 1) {
            mInfoTextView.setText("It's a tie!");
            mGameOver = Boolean.TRUE;
            mInfoTextView.setTextColor(Color.rgb(0,0,255));
        }
        else if (winner == 2) {
            //mInfoTextView.setText("You won!");
            String defaultMessage = getResources().getString(R.string.result_human_wins);
            mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
            mGameOver = Boolean.TRUE;
            mInfoTextView.setTextColor(Color.rgb(0,255,0));
        } else {
            mInfoTextView.setText("Opponent won!");
            mGameOver = Boolean.TRUE;
            mInfoTextView.setTextColor(Color.rgb(255,0,0));
        }
    }
}