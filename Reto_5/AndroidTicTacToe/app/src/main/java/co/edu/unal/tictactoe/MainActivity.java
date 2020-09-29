package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.BreakIterator;

import edu.harding.tictactoe.BoardView;
import edu.harding.tictactoe.TicTacToeGame;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    private TicTacToeGame mGame;
    private BoardView mBoardView;

    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;

    // Buttons making up the board
    private Button mBoardButtons[];
    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTieScoreTextView;
    private SharedPreferences mPrefs;


    private boolean mGameOver;
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    private TicTacToeGame.DifficultyLevel selectedLevel;
    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
//        mBoardButtons[0] = (Button) findViewById(R.id.one);
//        mBoardButtons[1] = (Button) findViewById(R.id.two);
//        mBoardButtons[2] = (Button) findViewById(R.id.three);
//        mBoardButtons[3] = (Button) findViewById(R.id.four);
//        mBoardButtons[4] = (Button) findViewById(R.id.five);
//        mBoardButtons[5] = (Button) findViewById(R.id.six);
//        mBoardButtons[6] = (Button) findViewById(R.id.seven);
//        mBoardButtons[7] = (Button) findViewById(R.id.eight);
//        mBoardButtons[8] = (Button) findViewById(R.id.nine);

        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mHumanScoreTextView = (TextView) findViewById(R.id.player_score);
        mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
        mTieScoreTextView = (TextView) findViewById(R.id.tie_score);

        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);
        mGame.setPlayer(TicTacToeGame.Player.Human);
        mGame.clearBoard();
        clearButtons();
        showFragmentDialog(DIALOG_DIFFICULTY_ID);
    }



    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.new_game:
                mGame.clearBoard();
                clearButtons();
                Toast.makeText(MainActivity.this, "New Game", Toast.LENGTH_SHORT).show();
                if(mGame.getPlayer() == TicTacToeGame.Player.Human){
                    mGame.setPlayer(TicTacToeGame.Player.Computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    mInfoTextView.setText(R.string.turn_human);
                }
                else {
                    mGame.setPlayer(TicTacToeGame.Player.Human);
                }
                startNewGame(mGame.getDifficultyLevel());
                return true;
            case R.id.ai_difficulty:
                showFragmentDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showFragmentDialog(DIALOG_QUIT_ID);
                return true;
            case R.id.about:
                showAboutDialog();
                return true;
        }
        return false;


    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Context context = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.about_dialog, null);
        builder.setView(layout);
        builder.setPositiveButton("OK", null);
        builder.create().show();

    }

    private void showFragmentDialog(int id){
        final String[] levels = getResources().getStringArray(R.array.difficulty_levels);
        final String[] selected = {null};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        switch (id){
            case DIALOG_QUIT_ID:
                builder.setTitle("");
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                break;
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle("Select a level");
                builder.setSingleChoiceItems(levels, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (levels[which]) {
                            case ("Easy"):
                                selectedLevel = TicTacToeGame.DifficultyLevel.Easy;
                                break;
                            case ("Harder"):
                                selectedLevel = TicTacToeGame.DifficultyLevel.Harder;
                                break;
                            case ("Expert"):
                                selectedLevel = TicTacToeGame.DifficultyLevel.Expert;
                                break;
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selectedLevel == null || mGame.getDifficultyLevel() == null){
                            selectedLevel = TicTacToeGame.DifficultyLevel.Expert;
                            clearButtons();
                            mGame.clearBoard();
                            startNewGame(selectedLevel);
                        }
                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selectedLevel == null){
                            selectedLevel = TicTacToeGame.DifficultyLevel.Expert;
                        }
                        Toast.makeText(MainActivity.this, "" + selectedLevel.toString() + " Level", Toast.LENGTH_SHORT).show();
                        clearButtons();
                        mGame.clearBoard();
                        startNewGame(selectedLevel);
                    }
                });
        }
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(Boolean.FALSE);
        dialog.show();

    }

    public void onClickMenu(View view) {
        if (view.getId() == R.id.new_game){
            mGame.clearBoard();
            clearButtons();
            Toast.makeText(MainActivity.this, "New game", Toast.LENGTH_SHORT).show();
            if(mGame.getPlayer() == TicTacToeGame.Player.Human){
                mGame.setPlayer(TicTacToeGame.Player.Computer);
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                mInfoTextView.setText(R.string.turn_human);
            }
            else {
                mGame.setPlayer(TicTacToeGame.Player.Human);
            }
            startNewGame(mGame.getDifficultyLevel());
        }
        if (view.getId() == R.id.difficulty_level){
            showFragmentDialog(DIALOG_DIFFICULTY_ID);
        }
        if (view.getId() == R.id.quit_game){
            showFragmentDialog(DIALOG_QUIT_ID);
        }
    }

    private void startNewGame(TicTacToeGame.DifficultyLevel level) {
        mGame.setDifficultyLevel(level);
        mBoardView.invalidate();
        mInfoTextView.setTextColor(Color.rgb(10,10,10));
        if(mGame.getPlayer() == TicTacToeGame.Player.Human){
            mInfoTextView.setText(R.string.first_human);
        }

        mGameOver = Boolean.FALSE;
    }    // End of startNewGame

    private void clearButtons() {
        mBoardView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sword);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.swish);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        public void onClick(View view) {
            if(!mGameOver) {
                if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);

                    // If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_computer);
                        int move = mGame.getComputerMove();
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                        winner = mGame.checkForWinner();
                    }
                    if(mGame.getPlayer() == TicTacToeGame.Player.Human){
                        if (winner == 0)
                            mInfoTextView.setText(R.string.turn_human);
                        else if (winner == 1){
                            mInfoTextView.setText(R.string.result_tie);
                            mGameOver = Boolean.TRUE;
                        }
                        else if (winner == 2){
                            mInfoTextView.setText(R.string.result_human_wins);
                            mGameOver = Boolean.TRUE;
                        } else {
                            mInfoTextView.setText(R.string.result_computer_wins);
                            mGameOver = Boolean.TRUE;
                        }
                    }
                }
            }

        }
    }


    private boolean setMove(char player, int location) {
        if(player == mGame.HUMAN_PLAYER)
            mHumanMediaPlayer.start();
        else
            mComputerMediaPlayer.start();

        if (mGame.setMove(player, location)) {
            mBoardView.invalidate();   // Redraw the board
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

            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{
                    // If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_computer);
                        turnComputer();
                    } else {
                        endGame(winner);
                    }
                }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private void turnComputer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                mBoardView.invalidate();

                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                } else
                    endGame(winner);
            }
        }, 0);
    }


    private void endGame(int winner) {
        switch (winner) {
            case 0:
                return;
            case 1:
                mInfoTextView.setTextColor(Color.rgb(0,0,255));
                mInfoTextView.setText(R.string.result_tie);
                mTies++;
                mTieScoreTextView.setText(Integer.toString(mTies));
                break;
            case 2:
                mInfoTextView.setTextColor(Color.rgb(0,255,0));
                String defaultMessage = getResources().getString(R.string.result_human_wins);
                mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
                mHumanWins++;
                mHumanScoreTextView.setText(Integer.toString(mHumanWins));
                break;
            default:
                mInfoTextView.setTextColor(Color.rgb(255,0,0));
                mInfoTextView.setText(R.string.result_computer_wins);
                mComputerWins++;
                mComputerScoreTextView.setText(Integer.toString(mComputerWins));
                break;
        }
        mGameOver = true;
    }

}