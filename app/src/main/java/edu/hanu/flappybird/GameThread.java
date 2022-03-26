package edu.hanu.flappybird;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.LinkedList;

public class GameThread implements Runnable {
    public static final int DEFAULT_FLAPPING_SPEED = 5000000;
    RelativeLayout layoutParent;
    Context context;
    BirdObject bird;
    Thread gameThread;
    Rect birdRect, pipeUpRect, pipeDownRect, baseRect, powerRect;
    float birdFallSpeed,flyCount,minY,maxY,pipeHeight;
    boolean isRunnable, soundOn;
    long globalCount;
    int screenHeight, screenWidth;
    LinkedList<PipeObject[]> pipeList;
    LinkedList<SuperPowerObject> powerList;
    LocalBroadcastManager broadcastManager;
    Intent actionBirdGoesDown, actionBirdDead, actionPowerEffect, actionAddScore ,actionRemovePower;
    MediaPlayer soundHit, soundPoint;
    Handler handler;
    LinearInterpolator linearInterpolator;
    DecelerateInterpolator decelerateInterpolator;


    public GameThread(Context context, RelativeLayout layoutParent, BirdObject bird) {
        this.context = context;
        this.layoutParent = layoutParent;
        this.bird = bird;
        initialize();
    }

    /**
     * deal with different situation on resume
     */
    public void onResume() {
        if (bird.getStatus().equals(BirdObject.PAUSE)) {
            bird.setStatus(BirdObject.PAUSE);
            gameThread = new Thread(this);
            isRunnable = false;
        } else if (bird.getStatus().equals(BirdObject.WAITING)) {
            gameThread = new Thread(this);
            isRunnable = true;
            gameThread.start();
        } else if (bird.getStatus().equals(BirdObject.PLAYING)) {
            gameThread = new Thread(this);
            isRunnable = true;
        }
    }

    /**
     * stop the thread, avoid memory leaking
     * @throws InterruptedException
     */
    public void onPause() throws InterruptedException {
        isRunnable = false;
        gameThread.join();
    }

    /**
     * init necessary things
     */
    private void initialize() {
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        flyCount = 0;
        birdFallSpeed = (screenHeight / 4f) / 60;

        // rect for checking intersection
        birdRect = new Rect();
        pipeUpRect = new Rect();
        pipeDownRect = new Rect();
        powerRect = new Rect();

        // intent request to main act
        broadcastManager = LocalBroadcastManager.getInstance(context);
        actionBirdGoesDown = new Intent("actionBirdGoesDown");
        actionBirdDead = new Intent("actionBirdDead");
        actionPowerEffect = new Intent("actionPowerEffect");
        actionAddScore = new Intent("actionAddScore");
        actionRemovePower = new Intent("actionRemovePower");

        // init sound fx
        soundHit = MediaPlayer.create(context, R.raw.hit);
        soundPoint = MediaPlayer.create(context, R.raw.point);


        //init handler
        handler = new Handler(Looper.getMainLooper());

        linearInterpolator = new LinearInterpolator();
        decelerateInterpolator = new DecelerateInterpolator();
    }

    /**
     * get the pipe height from the main act, for setting up pivot point
     * @param pipeHeight
     */
    public void setPipeHeight(float pipeHeight) {
        this.pipeHeight = pipeHeight;
    }

    /**
     * get base rect from main act to check intersection
     * @param baseRect
     */
    public void setBaseRect(Rect baseRect) {
        this.baseRect = baseRect;
    }

    /**
     * init pipe list
     * @param pipeList
     */
    public void setPipeList(LinkedList<PipeObject[]> pipeList) {
        this.pipeList = pipeList;

    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    /**
     * init power list
     * @param powerList
     */
    public void setPowerList(LinkedList<SuperPowerObject> powerList) {
        this.powerList = powerList;

    }

    public void setFloor(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }
    @Override
    public void run() {
        while (isRunnable) {
            // i dont know how to control thread exec speed so yea,
            // here is a global count
            globalCount++;
            if (globalCount == 30000000000L) {
                globalCount = -1;
            }
            // control bird status flow
            switch (bird.getStatus()) {
                case BirdObject.WAITING:
                    onWaiting();
                    break;
                case BirdObject.PLAYING:
                    onPlaying();
                    break;
                case BirdObject.PAUSE:
                    onManuallyPaused();
                    break;
                case BirdObject.DEAD:
                    onDead();
                    break;
            }
        }
    }

    /**
     * just do the flapping animation when the bird
     * is idling
     */
    private void onWaiting() {
        animateBirdIdleFlapping();
    }
    private void animateBirdIdleFlapping() {
        if (globalCount % DEFAULT_FLAPPING_SPEED == 0) {
            flyCount++;
            if (flyCount == 1) {
                handler.post(() -> {
                    bird.flyUp();
                });
            } else if (flyCount == 2) {
                handler.post(() -> {
                    bird.stayMid();
                });
            } else if (flyCount >= 3) {
                handler.post(() -> {
                    bird.fallDown();
                });
                flyCount = 0;
            }
        }
    }

    /**
     * control bird move up and down on user touch
     * checking intersection with pipe and power
     */
    private void onPlaying() {
        execPlayingBird();
        movingPipe();
        movingPower();
    }
    private void execPlayingBird() {
        if (globalCount % 20000 == 0) {
            if (bird.getY() >= maxY || bird.getY() <= minY) {
                bird.getHitRect(birdRect);
                if (birdRect.intersect(baseRect) || bird.getY() <= 0) {
                    bird.setStatus(BirdObject.DEAD);
                    if (soundOn) {
                        soundHit.start();
                    }
                }
            }
        }
    }

    private void movingPipe() {
        if (globalCount % 20000 == 0){
            if (pipeList.size() > 0) {
                for (int i = 0; i < pipeList.size(); i++) {
                    PipeObject[] pipePair = pipeList.get(i);
                    PipeObject pipeUp = pipePair[0];
                    PipeObject pipeDown = pipePair[1];
                    //check if the bird pass through pipe and has taken the score
                    if (bird.getX() >= pipeUp.getX() && !pipeUp.isScored) {
                        pipeUp.isScored = true;
                        if (bird.getSuperPower().equals(SuperPower.GOLDEN)) {
                            bird.score += 2;
                        }else {
                            bird.score++;
                        }
                        if (bird.score >= 999) {
                            broadcastManager.sendBroadcast(actionBirdDead);
                        }
                        if (soundOn) {
                            soundPoint.start();
                        }
                        broadcastManager.sendBroadcast(actionAddScore);
                    }
                    // check bird collision with pipe
                    if (pipeUp.getX() <= screenWidth / 2f) {
                        if (bird.getSuperPower().equals(SuperPower.POISON)) {
                            withDrawPipe(pipeUp,pipeDown);
                        }
                        bird.getHitRect(birdRect);
                        pipeUp.getHitRect(pipeUpRect);
                        pipeDown.getHitRect(pipeDownRect);
                        //if intersect, check if bird has super power
                        if (birdRect.intersect(pipeUpRect) || birdRect.intersect(pipeDownRect)) {
                            if (!bird.getSuperPower().equals(SuperPower.INVULNERABLE)) {
                                if (soundOn) {
                                    soundHit.start();
                                }
                            }
                            // if no => die
                            if (bird.getSuperPower().equals(SuperPower.NONE) || bird.getSuperPower().equals(SuperPower.GOLDEN)) {
                                broadcastManager.sendBroadcast(actionBirdDead);
                                //if yes, send broadcast and let main act take care
                            } else if (bird.getSuperPower().equals(SuperPower.GIANT)
                                    || bird.getSuperPower().equals(SuperPower.SPEED)) {
                                handler.post(() -> {
                                    if (birdRect.intersect(pipeUpRect)) {
                                        rotatePipe(pipeUp);
                                    } else {
                                        rotatePipe(pipeDown);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * rotate on bird collision but with super power
     * @param pipeObject the pipe that collide
     */
    private void rotatePipe(PipeObject pipeObject) {
        if (pipeObject.getPivotY() == 0) {
            handler.post(() -> {
                pipeObject.animate().setInterpolator(linearInterpolator).rotationBy(-90).setDuration(300).start();
            });
        } else  {
            handler.post(() -> {
                pipeObject.animate().setInterpolator(linearInterpolator).rotationBy(90).setDuration(300).start();
            });
        }
    }

    /**
     * withdraw pipe for poison power
     * @param pipeUp
     * @param pipeDown
     */
    private void withDrawPipe(PipeObject pipeUp, PipeObject pipeDown) {
        handler.post(() -> {
            pipeUp.animate().translationY(-pipeHeight).setDuration(500).setInterpolator(decelerateInterpolator)
                    .start();
            pipeDown.animate().translationY(screenHeight).setDuration(500).setInterpolator(decelerateInterpolator)
                    .start();
        });
    }
    /**
     * checking bird intersect with power
     */
    private void movingPower() {
        if (globalCount % 26000 == 0) {
            if (powerList.size() > 0) {
                SuperPowerObject superPowerObject = powerList.peek();
                assert superPowerObject != null;
                //check here
                if (superPowerObject.getX() <= screenWidth / 2f) {
                    if (superPowerObject.getX()<=-(screenWidth/4f)) {
                        actionRemovePower.putExtra("power", superPowerObject);
                        broadcastManager.sendBroadcast(actionRemovePower);
                    }
                    superPowerObject.getHitRect(powerRect);
                    bird.getHitRect(birdRect);
                    // set power to the bird and remove power from screen
                    if (birdRect.intersect(powerRect)) {
                        actionRemovePower.putExtra("power", superPowerObject);
                        bird.setSuperPower(superPowerObject.getSuperPower());
                        broadcastManager.sendBroadcast(actionPowerEffect);
                        broadcastManager.sendBroadcast(actionRemovePower);
                    }
                }
            }
        }
    }

    public void onManuallyResume() {
        isRunnable = true;
    }
    private void onManuallyPaused() {
        isRunnable = false;
    }
    private void onDead() {
        broadcastManager.sendBroadcast(actionBirdDead);
        isRunnable = false;
    }
}
