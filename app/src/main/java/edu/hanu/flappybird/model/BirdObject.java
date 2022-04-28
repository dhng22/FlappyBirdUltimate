package edu.hanu.flappybird.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import edu.hanu.flappybird.R;
import edu.hanu.flappybird.SuperPower;

public class BirdObject extends androidx.appcompat.widget.AppCompatImageView {
    public static final String DEAD = "isDead";
    public static final String PAUSE = "isPaused";
    public static final String WAITING = "isWaiting";
    public static final String PLAYING = "isPlaying";
    Bitmap[] imgSource;
    SuperPower superPower;
    private String status;
    Bitmap birdUp, birdDown, birdMid;
    public int score;
    public boolean userTouch;
    boolean isGoingUp;
    boolean isGoingDown;
    public int coin;
    public boolean hasTakenCoin;


    public BirdObject(Context context) {
        super(context);
        initialize();
    }


    public BirdObject(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public BirdObject(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void initialize() {
        isGoingUp = false;
        isGoingDown = false;
        hasTakenCoin = false;
        userTouch = false;

        score = 0;
        coin = 0;
        status = WAITING;

        imgSource = new Bitmap[3];
        imgSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap);
        imgSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap);
        imgSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_downflap);
        setImageSource(imgSource);

        superPower = SuperPower.NONE;
    }

    public void setImageSource(Bitmap[] imgSource) {
        birdDown = imgSource[0];
        birdMid = imgSource[1];
        birdUp = imgSource[2];
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSuperPower(SuperPower superPower) {
        this.superPower = superPower;
    }

    public SuperPower getSuperPower() {
        return superPower;
    }

    public void flyUp() {
        setImageBitmap(birdUp);
    }

    public void stayMid() {
        setImageBitmap(birdMid);
    }

    public void fallDown() {
        setImageBitmap(birdDown);
    }
}
