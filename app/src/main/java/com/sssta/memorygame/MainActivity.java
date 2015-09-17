package com.sssta.memorygame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    private static final String SP_NAME = "memory_game";
    private static final String SP_MAX_SCORE = "max_score";

    private static final int AUTO_DRAW_FINISH = 0;
    private static final int ON_AUTO_DRAW = 1;
    private static final int GESTURE_DRAW_FINISH_TRUE = 2;
    private static final int GESTURE_DRAW_FINISH_FALSE = 3;

    private static final int START = 4;

    private AutoDrawView autoDrawView;
    private GestureLockView gestureLockView;
    private Button btnStart;
    private TextView mTextScoreBoard;
    private TextView mTextMaxScoreBoard;

    private int score = 0;
    private int maxScore = 0;

    private ArrayList<Integer> passLists = new ArrayList<>();

    private SharedPreferences mSharedPreferences;

    private Handler handler;

    private int time = 5;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AUTO_DRAW_FINISH:
                        autoDrawView.setVisibility(View.GONE);
                        gestureLockView.setVisibility(View.VISIBLE);
                        break;
                    case GESTURE_DRAW_FINISH_TRUE:
                        mTextScoreBoard.setText(score + "");
                        autoDrawView.setVisibility(View.VISIBLE);
                        gestureLockView.setVisibility(View.GONE);
                        autoDrawView.startAutoDrawPoints(5);

                        break;
                    case START:
                        autoDrawView.setVisibility(View.VISIBLE);
                        gestureLockView.resetPoints();
                        gestureLockView.setVisibility(View.GONE);
                        autoDrawView.startAutoDrawPoints(5);
                        break;
                    case GESTURE_DRAW_FINISH_FALSE:
                        if(maxScore < score) {
                            maxScore = score;
                            //set maxScore on max_score_board
                            mTextMaxScoreBoard.setText(maxScore + "");
                        }
                        score = 0;
                        mTextScoreBoard.setText(score + "");
                        break;
                    default:
                        break;
                }
            }
        };

        findViews();
        init();
        setClicks();

    }

    private void init() {
        mSharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        maxScore = mSharedPreferences.getInt(SP_MAX_SCORE, 0);
        mTextMaxScoreBoard.setText(maxScore + "");
    }

    private void findViews() {
        btnStart = (Button) findViewById(R.id.btn_start);
        autoDrawView = (AutoDrawView) findViewById(R.id.gesture_auto_draw_view);
        gestureLockView = (GestureLockView) findViewById(R.id.gesture_lock_view);
        mTextScoreBoard = (TextView) findViewById(R.id.score_board);
        mTextMaxScoreBoard = (TextView) findViewById(R.id.max_score_board);
    }

    private void setClicks() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("is started", autoDrawView.isStarted() + "");
                Log.e("is drawed=", gestureLockView.isDraw() + "");
                if(!(autoDrawView.isStarted()) && !(gestureLockView.isDraw())) {
                    Log.e("is inter if", "enter if");
                    Message msg = new Message();
                    msg.what = START;
                    handler.sendMessage(msg);
                }
            }
        });

        autoDrawView.setOnDrawFinishedListener(new AutoDrawView.onAutoDrawFinishedListener() {
            @Override
            public void onAutoDrawFinished(List<Integer> passList) {
                passLists = (ArrayList) passList;
                Message msg = new Message();
                msg.what = AUTO_DRAW_FINISH;
                handler.sendMessage(msg);
            }
        });

        gestureLockView.setOnDrawFinishedListener(new GestureLockView.onDrawFinishedListener() {
            @Override
            public boolean onDrawFinished(List<Integer> passList) {
                Log.e("passLists is same", passLists.toString());
                Log.e("passList is same", passList.toString());
                if(passLists.toString().equals(passList.toString())) {
                    score++;
                    Message msg = new Message();
                    msg.what = GESTURE_DRAW_FINISH_TRUE;
                    handler.sendMessage(msg);
                    return true;
                } else {
                    Message msg = new Message();
                    msg.what = GESTURE_DRAW_FINISH_FALSE;
                    handler.sendMessage(msg);
                    return false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("max_score", maxScore + "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPreferences
                .edit()
                .putInt(SP_MAX_SCORE, maxScore)
                .apply();

    }
}
