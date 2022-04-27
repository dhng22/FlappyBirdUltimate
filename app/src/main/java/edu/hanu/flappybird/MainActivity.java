package edu.hanu.flappybird;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.LinkedList;

import edu.hanu.flappybird.service.AlarmReminder;
import edu.hanu.flappybird.model.BirdObject;
import edu.hanu.flappybird.model.PipeObject;
import edu.hanu.flappybird.model.SuperPowerObject;
import edu.hanu.flappybird.utils.DateUtils;
import edu.hanu.flappybird.utils.GameUtils;
import edu.hanu.flappybird.utils.NumberUtils;

/**
 * Game created by duy hung
 *
 * @author dhng.22
 */
public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_SCORE_NUMBER = 3;
    public static final int DEFAULT_PIPE_GENERATION = 2100;
    public static final int DEFAULT_PIPE_SPEED = 3000;
    public static final int DEFAULT_POWER_GENERATION = 11;
    public static final int DEFAULT_DAY_NIGHT_CYCLE = 60000;
    public static final int DEFAULT_FLAPPING_SPEED = 100;
    public static final int JOB_ID_SEVEN = 7;
    BirdObject mainBird;
    Context context;
    ImageButton btnPause, btnPlay, btnReplay, btnRestart, btnSound, btnMusic;
    RelativeLayout layoutParent, waitingScreen, pauseScreen, deathScreen, gameControlPanel;
    ImageView imgMessage, imgLastHighestNumber, imgSecondHighestNumber, imgFirstHighestNumber, imgDoubleSrc,
            imgLastNumber, imgSecondNumber, imgFirstNumber, imgBase, imgCoin, imgBaseNight, imgBaseDay, backgroundNight, cloudScreen, cloudScreen2,
            imgCart, imgSetting, imgShopBluePot, imgShopRedPot, imgShopPurplePot, imgShopGoldenPot, imgShopYellowPot, btnSoundMain, btnMusicMain;
    LinearLayout.LayoutParams params, powerParams;
    Animation animMessageBlink, animBirdBlinkStart, animBirdBlinkEnd, animPowerUp, animMovingBase,
            animTouchCart, animTouchSetting, animErrorCoin, animTouchBlue, animTouchRed, animTouchPurple, animTouchGolden,
            animTouchYellow;
    ObjectAnimator animBirdIdling, animBirdGoingUp, animBirdGoingDown, movingCloud, movingCloud2,
            animObserver;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    HashMap<Integer, Integer> numberMap;
    View.OnClickListener controlBirdListener, waitingScreenListener;
    ValueAnimator.AnimatorUpdateListener updateListener;
    LinkedList<PipeObject[]> pipeList;
    LinkedList<SuperPowerObject> powerList;
    Handler addPipeHandler, addPowerHandler, normalHandler, powerExecHandler;
    Runnable execPipe, execPower, onPowerExhaust;
    LinearInterpolator linearInterpolator;
    int screenHeight, screenWidth, baseHeight, highScore, pipeCount, pipeCountForPower, powerSpeed, pipeGenSpeed, pipeSpeed, pipeWidth, pipeHeight;
    float minY, maxY;
    boolean isDay, soundOn, musicOn;
    Bitmap[] giantBirdSource, speedBirdSource, originalSource, poisonBirdSource, goldenBirdSource;
    Bitmap pipeGreen, pipeGreenUp, pipeRed, pipeRedUp, pipePurple, pipePurpleUp, pipeSourceDown, pipeSourceUp, pipeGolden, pipeGoldenUp;
    MediaPlayer soundDead, soundWing, powerUp, soundSwoosh, backgrMusic, soundHit, soundPoint;
    DecelerateInterpolator decelerateInterpolator;
    AccelerateInterpolator accelerateInterpolator;
    AnimationDrawable animFlappingBird, animFlippingCoin;
    AlarmManager alarmManager;
    Rect powerRect, pipeUpRect, pipeDownRect, birdRect, baseRect;
    HorizontalScrollView shoppingPlace;
    TextView txtCoin;
    ConstraintLayout splashScreen, settingPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        doSplash();
        initialize();

    }

    private void doSplash() {
        splashScreen = findViewById(R.id.splashScreen);
        new Handler().postDelayed(() -> splashScreen.animate().alpha(0).setDuration(300)
                        .withEndAction(() -> runOnUiThread(() -> splashScreen.setVisibility(View.GONE))),
                1400);
    }

    private void initialize() {
        mapping();
        initParams();
        initSharedPreferences();
        prepareForHighScore();
        initAnimation();
    }

    /**
     * Just mapping View
     */
    private void mapping() {
        layoutParent = findViewById(R.id.layoutParent);
        // executing button
        btnPause = findViewById(R.id.btn_pause);
        btnPlay = findViewById(R.id.btn_play);
        btnReplay = findViewById(R.id.btn_replay);
        btnRestart = findViewById(R.id.btn_restart);
        btnSound = findViewById(R.id.btn_Sound);
        btnMusic = findViewById(R.id.btn_Music);
        btnSoundMain = findViewById(R.id.btn_SoundMain);
        btnMusicMain = findViewById(R.id.btn_MusicMain);

        // main screen components
        cloudScreen = findViewById(R.id.imgCloudScreen);
        cloudScreen2 = findViewById(R.id.imgCloudScreen2);

        backgroundNight = findViewById(R.id.imgBackGrNight);
        gameControlPanel = findViewById(R.id.gameControlPanel);
        gameControlPanel.setZ(0.95f);
        waitingScreen = findViewById(R.id.waitingScreen);
        waitingScreen.setZ(1f);
        deathScreen = findViewById(R.id.deathScreen);
        deathScreen.setZ(1f);
        pauseScreen = findViewById(R.id.pauseScreen);
        pauseScreen.setZ(1f);

        // components inside main components
        imgMessage = findViewById(R.id.img_message);
        imgLastHighestNumber = findViewById(R.id.lastHighestNumber);
        imgSecondHighestNumber = findViewById(R.id.secondHighestNumber);
        imgFirstHighestNumber = findViewById(R.id.firstHighestNumber);
        imgLastNumber = findViewById(R.id.lastNumber);
        imgSecondNumber = findViewById(R.id.secondNumber);
        imgFirstNumber = findViewById(R.id.firstNumber);
        imgBase = findViewById(R.id.imgBase);
        imgBaseNight = findViewById(R.id.imgBaseNight);
        imgBaseDay = findViewById(R.id.imgBaseDay);
        imgCart = findViewById(R.id.imgCart);
        imgSetting = findViewById(R.id.imgSetting);
        shoppingPlace = findViewById(R.id.shoppingPlace);
        settingPlace = findViewById(R.id.mainSetting);
        imgShopBluePot = findViewById(R.id.shopBluePot);
        imgShopRedPot = findViewById(R.id.shopRedPot);
        imgShopPurplePot = findViewById(R.id.shopPurplePot);
        imgShopGoldenPot = findViewById(R.id.shopGoldenPot);
        imgShopYellowPot = findViewById(R.id.shopYellowPot);
        imgCoin = findViewById(R.id.imgShowCoin);
        txtCoin = findViewById(R.id.txtCoin);
        imgBase.setZ(0.5f);
        imgBaseNight.setZ(0.55f);

        imgDoubleSrc = findViewById(R.id.doubleScr);
    }

    /**
     * Initialize necessary variables
     */
    private void initParams() {
        // init basic variables
        context = this;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        GameUtils.Initializer.resources = getResources();
        GameUtils.PipeNPower.screenHeight = screenHeight;
        GameUtils.PipeNPower.screenWidth = screenWidth;

        // resource to change bird bitmap depends on its super power
        originalSource = new Bitmap[3];
        giantBirdSource = new Bitmap[3];
        speedBirdSource = new Bitmap[3];
        poisonBirdSource = new Bitmap[3];
        goldenBirdSource = new Bitmap[3];
        Bitmap[][] birdSourceArray = {originalSource, giantBirdSource, speedBirdSource,
                poisonBirdSource, goldenBirdSource};
        GameUtils.Initializer.initBirdSource(birdSourceArray);


        //resource to change pipe depends on bird power
        Bitmap[] pipeSourceArray = GameUtils.Initializer.initPipeSource(new Bitmap[8]);
        pipeGreen = pipeSourceArray[0];
        pipeGreenUp = pipeSourceArray[1];
        pipeRed = pipeSourceArray[2];
        pipeRedUp = pipeSourceArray[3];
        pipePurple = pipeSourceArray[4];
        pipePurpleUp = pipeSourceArray[5];
        pipeGolden = pipeSourceArray[6];
        pipeGoldenUp = pipeSourceArray[7];

        // rect for checking collision
        powerRect = new Rect();
        pipeUpRect = new Rect();
        pipeDownRect = new Rect();
        birdRect = new Rect();
        baseRect = new Rect();

        // asc variables
        isDay = true;
        pipeSourceDown = pipeGreen;
        pipeSourceUp = pipeGreenUp;
        pipeSpeed = DEFAULT_PIPE_SPEED;
        pipeGenSpeed = DEFAULT_PIPE_GENERATION;
        powerSpeed = DEFAULT_PIPE_SPEED;
        minY = screenHeight / 4f;
        GameUtils.minY = minY;

        //creating sound
        soundDead = MediaPlayer.create(this, R.raw.die);
        soundWing = MediaPlayer.create(this, R.raw.wing_1);
        powerUp = MediaPlayer.create(this, R.raw.power_up);
        soundSwoosh = MediaPlayer.create(this, R.raw.swoosh_1);
        soundHit = MediaPlayer.create(this, R.raw.hit);
        soundPoint = MediaPlayer.create(this, R.raw.point);
        backgrMusic = MediaPlayer.create(this, R.raw.background);


        // interpolator animating moving object except bird and params
        linearInterpolator = new LinearInterpolator();
        decelerateInterpolator = new DecelerateInterpolator();
        accelerateInterpolator = new AccelerateInterpolator();
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        powerParams = new LinearLayout.LayoutParams(screenWidth / 14, screenHeight / 18);

        // init game's object
        mainBird = new BirdObject(this);
        GameUtils.Bird.birdObject = mainBird;
        GameUtils.Bird.initBirdObject(layoutParent, params);

        cloudScreen.setX(screenWidth);
        cloudScreen2.setX(0);


        imgBase.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgBase.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                baseHeight = imgBase.getHeight();
                maxY = screenHeight - baseHeight - (screenHeight / 15f);
                GameUtils.maxY = maxY;
                imgBase.getHitRect(baseRect);
            }
        });
        // rendering a temporary pipe object to get its view's height
        PipeObject pipeObject = new PipeObject(context, pipeGreen);
        pipeObject.setLayoutParams(params);
        layoutParent.addView(pipeObject);
        pipeObject.setX(screenWidth);
        pipeObject.setY(0);
        pipeObject.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pipeObject.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                pipeHeight = pipeObject.getHeight();
                pipeWidth = pipeObject.getWidth();
                GameUtils.PipeNPower.pipeHeight = pipeHeight;
                layoutParent.removeView(pipeObject);
            }
        });


        // two main listener control game and control waiting screen
        waitingScreenListener = v -> {
            animFlappingBird.setVisible(false, true);
            mainBird.setBackgroundDrawable(null);
            mainBird.setStatus(BirdObject.PLAYING);
            layoutParent.setOnClickListener(controlBirdListener);
            hideWaitingScreen();
            layoutParent.performClick();
            startAddingPipe();
        };
        controlBirdListener = v -> runOnUiThread(() -> {
            animBirdGoingDown.cancel();
            animBirdGoingUp.setFloatValues(mainBird.getY(), mainBird.getY() - screenHeight / 7.75f);
            animBirdGoingUp.start();
            birdGoesUp();
            if (soundOn) {
                soundWing.start();
            }
            mainBird.userTouch = true;
        });
        updateListener = animation -> eventHandling();

        // init list that store game object
        pipeList = new LinkedList<>();
        powerList = new LinkedList<>();

        // handler to add pipe on a non-fixed pace
        addPipeHandler = new Handler();
        addPowerHandler = new Handler();
        normalHandler = new Handler();
        powerExecHandler = new Handler();
        // creating pipe for the use of handler
        execPipe = new Runnable() {
            @Override
            public void run() {
                countPipe();
                if (pipeCountForPower % DEFAULT_POWER_GENERATION == 0 && pipeCountForPower != 0) {
                    startAddingPower();
                }
                if (pipeCount % 10 == 0 && pipeCount != 0) {
                    pipeSourceDown = pipeRed;
                    pipeSourceUp = pipeRedUp;
                } else if (mainBird.getSuperPower().equals(SuperPower.POISON)) {
                    pipeSourceDown = pipePurple;
                    pipeSourceUp = pipePurpleUp;
                } else if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                    pipeSourceDown = pipeGolden;
                    pipeSourceUp = pipeGoldenUp;
                } else {
                    pipeSourceDown = pipeGreen;
                    pipeSourceUp = pipeGreenUp;
                }
                PipeObject[] pipePair = new PipeObject[2];

                PipeObject pipeUp = new PipeObject(context, pipeSourceUp);
                pipeUp.setLayoutParams(params);
                PipeObject pipeDown = new PipeObject(context, pipeSourceDown);
                pipeDown.setLayoutParams(params);

                pipePair[0] = pipeUp;
                pipePair[1] = pipeDown;
                runOnUiThread(() -> {
                    GameUtils.PipeNPower.addPipe(pipeUp, pipeDown,
                            layoutParent, pipeList, pipePair);
                    startObjectAnimation(pipeUp, pipeDown);
                });
                addPipeHandler.postDelayed(this, pipeGenSpeed);


            }
        };
        execPower = () -> {
            SuperPowerObject object = new SuperPowerObject(context, getRandomSuperPower());
            object.setLayoutParams(powerParams);
            runOnUiThread(() -> {
                GameUtils.PipeNPower.addPower(object, layoutParent, powerList);
                startObjectAnimation(object);
            });
        };

        onPowerExhaust = () -> mainBird.startAnimation(animBirdBlinkStart);

        // listener
        imgCart.setOnClickListener(v -> {
            toggleShop();
            imgCart.startAnimation(animTouchCart);
        });

        imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSetting();
                imgSetting.startAnimation(animTouchSetting);
            }
        });
        imgShopBluePot.setOnClickListener(v -> {
            imgShopBluePot.startAnimation(animTouchBlue);
            if (mainBird.coin >= 1) {
                mainBird.coin--;
                setGameCoin();
                layoutParent.performClick();
                mainBird.setSuperPower(SuperPower.INVULNERABLE);
                actionPowerEffect();
                if (soundOn) {
                    powerUp.start();
                }
            } else {
                coinColor(Color.RED);
                imgCoin.startAnimation(animErrorCoin);
            }
        });
        imgShopRedPot.setOnClickListener(v -> {
            imgShopRedPot.startAnimation(animTouchRed);
            if (mainBird.coin >= 2) {
                mainBird.coin -= 2;
                setGameCoin();
                layoutParent.performClick();
                mainBird.setSuperPower(SuperPower.GIANT);
                actionPowerEffect();
                if (soundOn) {
                    powerUp.start();
                }
            } else {
                coinColor(Color.RED);
                imgCoin.startAnimation(animErrorCoin);
            }

        });

        imgShopPurplePot.setOnClickListener(v -> {
            imgShopPurplePot.startAnimation(animTouchPurple);
            if (mainBird.coin >= 2) {
                mainBird.coin -= 2;
                setGameCoin();
                layoutParent.performClick();
                mainBird.setSuperPower(SuperPower.POISON);
                actionPowerEffect();
                if (soundOn) {
                    powerUp.start();
                }
            } else {
                coinColor(Color.RED);
                imgCoin.startAnimation(animErrorCoin);
            }

        });
        imgShopGoldenPot.setOnClickListener(v -> {
            imgShopGoldenPot.startAnimation(animTouchGolden);
            if (mainBird.coin >= 3) {
                mainBird.coin -= 3;
                setGameCoin();
                layoutParent.performClick();
                mainBird.setSuperPower(SuperPower.GOLDEN);
                actionPowerEffect();
                if (soundOn) {
                    powerUp.start();
                }
            } else {
                coinColor(Color.RED);
                imgCoin.startAnimation(animErrorCoin);
            }
        });
        imgShopYellowPot.setOnClickListener(v -> {
            imgShopYellowPot.startAnimation(animTouchYellow);
            if (mainBird.coin >= 4) {
                mainBird.coin -= 4;
                setGameCoin();
                layoutParent.performClick();
                mainBird.setSuperPower(SuperPower.SPEED);
                actionPowerEffect();
                if (soundOn) {
                    powerUp.start();
                }
            } else {
                coinColor(Color.RED);
                imgCoin.startAnimation(animErrorCoin);
            }
        });

        btnSoundMain.setOnClickListener(v -> {
            if (soundOn) {
                soundOn = false;
                btnSoundMain.setImageResource(R.drawable.sound_off);
                btnSound.setImageResource(R.drawable.sound_off);
            } else {
                soundOn = true;
                btnSoundMain.setImageResource(R.drawable.sound_on);
                btnSound.setImageResource(R.drawable.sound_on);
            }
            editor.putBoolean("soundOn", soundOn);
            editor.commit();

        });
        btnMusicMain.setOnClickListener(v -> {
            if (musicOn) {
                musicOn = false;
                if (backgrMusic != null) {
                    backgrMusic.pause();
                }
                btnMusicMain.setImageResource(R.drawable.music_off);
                btnMusic.setImageResource(R.drawable.music_off);
            } else {
                musicOn = true;
                if (backgrMusic == null) {
                    backgrMusic = MediaPlayer.create(this, R.raw.background);
                }
                backgrMusic.start();
                btnMusicMain.setImageResource(R.drawable.music_on);
                btnMusic.setImageResource(R.drawable.music_on);
            }
            editor.putBoolean("musicOn", musicOn);
            editor.commit();
        });
        imgBaseNight.setOnClickListener(v -> layoutParent.performClick());
        // service
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

    /**
     * init shared pref and its editor
     * get the stored high-score in shared preference
     */
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("highScoreData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        highScore = sharedPreferences.getInt("highScore", 0);
        mainBird.coin = sharedPreferences.getInt("coin", 0);
        setGameCoin();

        soundOn = sharedPreferences.getBoolean("soundOn", true);
        musicOn = sharedPreferences.getBoolean("musicOn", true);
        if (!soundOn) {
            btnSound.setImageResource(R.drawable.sound_off);
            btnSoundMain.setImageResource(R.drawable.sound_off);
        }
        if (!musicOn) {
            btnMusic.setImageResource(R.drawable.music_off);
            btnMusicMain.setImageResource(R.drawable.music_off);
        }
    }

    /**
     * mapping high-score in waiting screen
     */
    private void prepareForHighScore() {
        numberMap = new HashMap<>();
        GameUtils.Initializer.initScoreSrc(numberMap);
    }

    /**
     * initialize animation
     */
    private void initAnimation() {
        animBirdGoingUp = ObjectAnimator.ofFloat(mainBird, "translationY", mainBird.getY(), mainBird.getY() - screenHeight / 8.5f);
        animBirdGoingUp.setDuration(500);
        animBirdGoingUp.setInterpolator(decelerateInterpolator);
        animBirdGoingDown = ObjectAnimator.ofFloat(mainBird, "translationY", mainBird.getY(), screenHeight);
        animBirdGoingDown.setDuration(800);
        animBirdGoingDown.setInterpolator(accelerateInterpolator);
        animBirdGoingUp.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (soundOn) {
                    soundSwoosh.start();
                }
                if (mainBird.getStatus().equals(BirdObject.PLAYING)) {
                    animBirdGoingDown.setFloatValues(mainBird.getY(), screenHeight);
                    animBirdGoingDown.start();
                    birdGoesDown();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        movingCloud = ObjectAnimator.ofFloat(cloudScreen, "translationX", screenWidth, 0);
        movingCloud.setDuration(DEFAULT_DAY_NIGHT_CYCLE);
        movingCloud.setRepeatMode(ValueAnimator.RESTART);
        movingCloud.setInterpolator(linearInterpolator);
        movingCloud.setRepeatCount(ValueAnimator.INFINITE);

        movingCloud2 = ObjectAnimator.ofFloat(cloudScreen2, "translationX", 0, -screenWidth);
        movingCloud2.setDuration(DEFAULT_DAY_NIGHT_CYCLE);
        movingCloud2.setInterpolator(linearInterpolator);
        movingCloud2.setRepeatMode(ValueAnimator.RESTART);
        movingCloud2.setRepeatCount(ValueAnimator.INFINITE);
        animMessageBlink = AnimationUtils.loadAnimation(this, R.anim.anim_blink);
        animBirdBlinkStart = AnimationUtils.loadAnimation(this, R.anim.anim_bird_blink_start);
        animBirdBlinkEnd = AnimationUtils.loadAnimation(this, R.anim.anim_bird_blink_end);
        animPowerUp = AnimationUtils.loadAnimation(this, R.anim.anim_power_up);
        animMovingBase = AnimationUtils.loadAnimation(this, R.anim.anim_moving_base);
        animTouchCart = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animTouchSetting = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animErrorCoin = AnimationUtils.loadAnimation(this, R.anim.anim_coin);
        animTouchRed = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animTouchBlue = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animTouchPurple = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animTouchGolden = AnimationUtils.loadAnimation(this, R.anim.anim_touch);
        animTouchYellow = AnimationUtils.loadAnimation(this, R.anim.anim_touch);


        animFlappingBird = new AnimationDrawable();
        animFlappingBird.addFrame(new BitmapDrawable(getResources(), originalSource[0]), DEFAULT_FLAPPING_SPEED);
        animFlappingBird.addFrame(new BitmapDrawable(getResources(), originalSource[1]), DEFAULT_FLAPPING_SPEED);
        animFlappingBird.addFrame(new BitmapDrawable(getResources(), originalSource[2]), DEFAULT_FLAPPING_SPEED);
        animFlappingBird.addFrame(new BitmapDrawable(getResources(), originalSource[1]), DEFAULT_FLAPPING_SPEED);
        animFlappingBird.setOneShot(false);
        mainBird.setBackgroundDrawable(animFlappingBird);
        animFlippingCoin = new AnimationDrawable();
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin1), 90);
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin2), 90);
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin3), 90);
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin4), 90);
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin5), 90);
        animFlippingCoin.addFrame(AppCompatResources.getDrawable(this, R.drawable.coin6), 90);
        animFlippingCoin.setOneShot(false);
        imgCoin.setBackground(animFlippingCoin);

        animBirdBlinkStart.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainBird.startAnimation(animBirdBlinkEnd);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animBirdBlinkEnd.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imgDoubleSrc.setAlpha(0f);
                if (mainBird.getSuperPower().equals(SuperPower.SPEED)) {
                    resetGameSpeed();
                }
                mainBird.setImageSource(originalSource);
                if (mainBird.getSuperPower().equals(SuperPower.POISON) || mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                    resetPipeColor();
                }
                mainBird.setSuperPower(SuperPower.INVULNERABLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainBird.setSuperPower(SuperPower.NONE);
                mainBird.setAlpha(1f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animBirdIdling = ObjectAnimator.ofFloat(mainBird, "translationY", screenHeight / 2f, screenHeight / 2f + 60)
                .setDuration(500);
        animBirdIdling.setRepeatCount(ValueAnimator.INFINITE);
        animBirdIdling.setRepeatMode(ValueAnimator.REVERSE);

        animObserver = ObjectAnimator.ofFloat(imgBase, "alpha", 1, 1)
                .setDuration(Long.MAX_VALUE);


        movingCloud.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (isDay) {
                    isDay = false;
                    backgroundNight.animate().alpha(1).setDuration(3000).start();
                    imgBaseNight.animate().alpha(1).setDuration(3000).start();
                } else {
                    isDay = true;
                    backgroundNight.animate().alpha(0).setDuration(3000).start();
                    imgBaseNight.animate().alpha(0).setDuration(3000).start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        animFlippingCoin.start();
        if (mainBird.getStatus().equals(BirdObject.WAITING)) {
            setToWaitingStatus();
        }

        if (mainBird.getStatus().equals(BirdObject.PAUSE)) {
            mainBird.setStatus(BirdObject.PAUSE);
            pauseGameManually();
        }

    }

    /**
     * handling event on observing listener
     */
    private void eventHandling() {
        handleFloorEvent();
        handlePipeEvent();
        handlePowerEvent();
    }

    /**
     * handle event of power(intersect, remove)
     */
    private void handlePowerEvent() {
        if (powerList.size() > 0) {
            SuperPowerObject superPowerObject = powerList.peek();
            assert superPowerObject != null;
            //check here
            if (superPowerObject.getX() <= screenWidth / 2f) {
                if (superPowerObject.getX() <= -(screenWidth / 4f)) {
                    actionRemovePower(superPowerObject);
                }
                superPowerObject.getHitRect(powerRect);
                mainBird.getHitRect(birdRect);
                // set power to the bird and remove power from screen
                if (birdRect.intersect(powerRect)) {
                    mainBird.setSuperPower(superPowerObject.getSuperPower());
                    actionPowerEffect();
                    actionRemovePower(superPowerObject);
                }
            }
        }
    }

    /**
     * handle pipe event(intersect, remove)
     */
    private void handlePipeEvent() {
        if (pipeList.size() > 0) {
            for (int i = 0; i < pipeList.size(); i++) {
                PipeObject[] pipePair = pipeList.get(i);
                PipeObject pipeUp = pipePair[0];
                PipeObject pipeDown = pipePair[1];
                // check bird collision with pipe
                if (pipeUp.getX() <= screenWidth / 2f) {

                    //check if the bird pass through pipe and has taken the score
                    handleScoreEvent(pipeUp);

                    if (mainBird.getSuperPower().equals(SuperPower.POISON)) {
                        withDrawPipe(pipeUp, pipeDown);
                    }
                    mainBird.getHitRect(birdRect);
                    pipeUp.getHitRect(pipeUpRect);
                    pipeDown.getHitRect(pipeDownRect);
                    //if intersect, check if bird has super power
                    if (birdRect.intersect(pipeUpRect) || birdRect.intersect(pipeDownRect)) {
                        if (!mainBird.getSuperPower().equals(SuperPower.INVULNERABLE)) {
                            if (soundOn) {
                                soundHit.start();
                            }
                        }
                        // if no => die
                        if (mainBird.getSuperPower().equals(SuperPower.NONE) || mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                            setToDeadStatus();
                            //if yes, send broadcast and let main act take care
                        } else if (mainBird.getSuperPower().equals(SuperPower.GIANT)
                                || mainBird.getSuperPower().equals(SuperPower.SPEED)) {
                            if (birdRect.intersect(pipeUpRect)) {
                                rotatePipe(pipeUp);
                            } else {
                                rotatePipe(pipeDown);
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * add score when passed a pipe
     * and add coin when pass a red pipe
     *
     * @param pipeUp the pipe to check
     */
    private void handleScoreEvent(PipeObject pipeUp) {
        if (!pipeUp.isScored && mainBird.getX() >= pipeUp.getX()) {
            pipeUp.isScored = true;
            if (pipeUp.getPipeBitmap().sameAs(pipeRedUp) && !mainBird.hasTakenCoin) {
                if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                    mainBird.coin += 2;
                } else {
                    mainBird.coin++;
                }
                imgCoin.startAnimation(animErrorCoin);
                setGameCoin();
                coinColor(Color.YELLOW);
            }
            if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                mainBird.score += 2;
            } else {
                mainBird.score++;
            }
            if (mainBird.score >= 999) {
                setToDeadStatus();
                Toast.makeText(context, "Damn! you're good", Toast.LENGTH_SHORT).show();
            }
            if (soundOn) {
                soundPoint.start();
            }
            actionAddScore();
        }
    }

    /**
     * handle bird intersection with floor and ceil
     */
    private void handleFloorEvent() {
        if (mainBird.getY() >= maxY || mainBird.getY() <= minY) {
            mainBird.getHitRect(birdRect);
            if (birdRect.intersect(baseRect) || mainBird.getY() <= 0) {
                mainBird.setStatus(BirdObject.DEAD);
                setToDeadStatus();
                if (soundOn) {
                    soundHit.start();
                }
            }
        }
    }

    /**
     * reset every thing to the waiting screen state
     */
    private void onGameRestart() {
        mainBird.setStatus(BirdObject.WAITING);
        btnPause.setEnabled(true);
        layoutParent.setOnClickListener(waitingScreenListener);
        hidePauseScreen();
        setToWaitingStatus();
    }

    /**
     * setting the waiting screen and others related component
     */
    private void setToWaitingStatus() {
        normalHandler.postDelayed(() -> animObserver.addUpdateListener(updateListener), 200);
        animObserver.start();
        mainBird.setImageBitmap(null);
        mainBird.setRotation(0);
        mainBird.setBackgroundDrawable(animFlappingBird);
        animFlappingBird.setVisible(true, true);
        animFlappingBird.start();
        movingCloud.start();
        movingCloud2.start();
        imgBaseDay.startAnimation(animMovingBase);
        imgBaseNight.startAnimation(animMovingBase);
        mainBird.setStatus(BirdObject.WAITING);
        showWaitingScreen();
        birdIdlingAnimationExec();
        setHighScore();
        hideGamePanel();
        clearRemainingObject();

        pipeCount = 0;
        pipeCountForPower = 0;
    }

    /**
     * toggle shop visibility
     */
    private void toggleShop() {
        if (settingPlace.getVisibility() == View.VISIBLE) {
            settingPlace.setAlpha(1);
            settingPlace.animate().alpha(0).setDuration(200).withEndAction(() -> settingPlace.setVisibility(View.GONE)).start();
        }
        if (shoppingPlace.getVisibility() == View.GONE) {
            shoppingPlace.setVisibility(View.VISIBLE);
            shoppingPlace.setAlpha(0);
            shoppingPlace.animate().alpha(1).setDuration(200).start();
        } else {
            shoppingPlace.setAlpha(1);
            shoppingPlace.animate().alpha(0).setDuration(200).withEndAction(() -> shoppingPlace.setVisibility(View.GONE)).start();
        }
    }

    /**
     * toggle setting visibility
     */
    private void toggleSetting() {
        if (shoppingPlace.getVisibility() == View.VISIBLE) {
            shoppingPlace.setAlpha(1);
            shoppingPlace.animate().alpha(0).setDuration(200).withEndAction(() -> shoppingPlace.setVisibility(View.GONE)).start();
        }
        if (settingPlace.getVisibility() == View.GONE) {
            settingPlace.setVisibility(View.VISIBLE);
            settingPlace.setAlpha(0);
            settingPlace.animate().alpha(1).setDuration(200).start();
        } else {
            settingPlace.setAlpha(1);
            settingPlace.animate().alpha(0).setDuration(200).withEndAction(() -> settingPlace.setVisibility(View.GONE)).start();
        }
    }

    /**
     * show waiting screen
     */
    private void showWaitingScreen() {
        if (musicOn) {
            if (backgrMusic == null) {
                backgrMusic = MediaPlayer.create(this, R.raw.background);
            }
            backgrMusic.start();
        }
        waitingScreen.setVisibility(View.VISIBLE);
        waitingScreen.setAlpha(1);
        imgMessage.startAnimation(animMessageBlink);
        layoutParent.setOnClickListener(waitingScreenListener);
    }

    /**
     * hide waiting screen
     */
    private void hideWaitingScreen() {
        shoppingPlace.setVisibility(View.GONE);
        settingPlace.setVisibility(View.GONE);
        waitingScreen.setVisibility(View.GONE);
        showGamePanel();
        setGameScore(0);
        animBirdIdling.cancel();
    }

    /**
     * show the game panel (current score and pause button)
     */
    private void showGamePanel() {
        gameControlPanel.setVisibility(View.VISIBLE);
        gameControlPanel.setAlpha(1f);
        btnPause.setOnClickListener(v -> pauseGameManually());
    }

    private void hideGamePanel() {
        gameControlPanel.setVisibility(View.GONE);
    }

    /**
     * adding object continuously (power and pipe)
     */

    private void startAddingPipe() {
        addPipeHandler.post(execPipe);
    }

    private void startAddingPower() {
        addPowerHandler.postDelayed(execPower, 1300);
    }

    /**
     * game speed up and reset for speed superpower
     */
    private void resetGameSpeed() {
        this.pipeSpeed = DEFAULT_PIPE_SPEED;
        this.pipeGenSpeed = DEFAULT_PIPE_GENERATION;
    }

    /**
     * speed up pipe's generation and pipe's speed
     *
     * @param by how many time?
     */
    private void gameSpeedUp(float by) {
        this.pipeSpeed = (int) (DEFAULT_PIPE_SPEED / by);
        this.pipeGenSpeed = (int) (DEFAULT_PIPE_GENERATION / by);
    }

    /**
     * pause screen and its component
     */
    private void showPauseScreen() {
        pauseScreen.setVisibility(View.VISIBLE);
        pauseScreen.setAlpha(0);
        pauseScreen.animate().alpha(1).setDuration(100).start();
        btnPlay.setOnClickListener(v -> {
            imgBaseDay.startAnimation(animMovingBase);
            imgBaseNight.startAnimation(animMovingBase);
            if (musicOn) {
                if (backgrMusic == null) {
                    backgrMusic = MediaPlayer.create(this, R.raw.background);
                }
                backgrMusic.start();
            }
            addPipeHandler.postDelayed(execPipe, pipeGenSpeed);
            resumeObjectAnimation();
            mainBird.setStatus(BirdObject.WAITING);
            btnPause.setEnabled(true);
            layoutParent.setOnClickListener(controlBirdListener);
            mainBird.setStatus(BirdObject.PLAYING);
            layoutParent.performClick();
            layoutParent.performClick();
            hidePauseScreen();
        });
        btnReplay.setOnClickListener(v -> onGameRestart());

        btnMusic.setOnClickListener(v -> {
            if (musicOn) {
                musicOn = false;
                if (backgrMusic != null) {
                    backgrMusic.pause();
                }
                btnMusic.setImageResource(R.drawable.music_off);
                btnMusicMain.setImageResource(R.drawable.music_off);
            } else {
                musicOn = true;
                if (backgrMusic == null) {
                    backgrMusic = MediaPlayer.create(this, R.raw.background);
                }
                backgrMusic.start();
                btnMusic.setImageResource(R.drawable.music_on);
                btnMusicMain.setImageResource(R.drawable.music_on);
            }
            editor.putBoolean("musicOn", musicOn);
            editor.commit();
        });
        btnSound.setOnClickListener(v -> {
            if (soundOn) {
                soundOn = false;
                btnSound.setImageResource(R.drawable.sound_off);
                btnSoundMain.setImageResource(R.drawable.sound_off);
            } else {
                soundOn = true;
                btnSound.setImageResource(R.drawable.sound_on);
                btnSoundMain.setImageResource(R.drawable.sound_on);
            }
            editor.putBoolean("soundOn", soundOn);
            editor.commit();
        });
    }

    private void hidePauseScreen() {
        pauseScreen.setVisibility(View.GONE);
    }

    private void pauseGameManually() {
        mainBird.setStatus(BirdObject.PAUSE);
        imgBaseNight.clearAnimation();
        imgBaseDay.clearAnimation();
        if (musicOn && backgrMusic != null) {
            backgrMusic.pause();
        }
        layoutParent.setOnClickListener(null);
        animBirdGoingDown.cancel();
        animBirdGoingUp.cancel();
        clearObjectAnimation();
        btnPause.setEnabled(false);
        showPauseScreen();
        mainBird.animate().cancel();
        mainBird.clearAnimation();
        animBirdBlinkStart.cancel();
        animBirdBlinkEnd.cancel();
        addPipeHandler.removeCallbacks(execPipe);
    }

    /**
     * set everything stop ( clear objects animation)
     * show death screen
     * store high score
     */
    private void setToDeadStatus() {
        mainBird.setStatus(BirdObject.DEAD);
        animObserver.removeUpdateListener(updateListener);
        animObserver.cancel();
        layoutParent.setOnClickListener(null);
        mainBird.setAlpha(1f);
        mainBird.animate().cancel();
        animBirdGoingUp.cancel();
        animBirdGoingDown.cancel();
        imgDoubleSrc.setAlpha(0f);
        imgBaseNight.clearAnimation();
        imgBaseDay.clearAnimation();
        if (musicOn && backgrMusic != null) {
            backgrMusic.pause();
        }
        powerExecHandler.removeCallbacks(onPowerExhaust);
        imgDoubleSrc.setAlpha(0f);
        if (mainBird.getSuperPower().equals(SuperPower.SPEED)) {
            resetGameSpeed();
        }
        mainBird.setImageSource(originalSource);
        if (mainBird.getSuperPower().equals(SuperPower.POISON) || mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
            resetPipeColor();
        }
        movingCloud.cancel();
        movingCloud2.cancel();
        clearObjectAnimation();
        int highScore = Math.max(sharedPreferences.getInt("highScore", 0), mainBird.score);
        editor.putInt("highScore", highScore);
        editor.commit();
        addPipeHandler.removeCallbacks(execPipe);
        btnPause.setEnabled(false);
        animBirdBlinkStart.cancel();
        animBirdBlinkEnd.cancel();
        showDeathScreen();
        if (soundOn) {
            soundDead.start();
        }
        resetGameSpeed();
        mainBird.setSuperPower(SuperPower.NONE);
        mainBird.clearAnimation();
        mainBird.setImageSource(originalSource);

        mainBird.score = 0;
    }

    /**
     * show death screen when bird dies
     */
    private void showDeathScreen() {
        deathScreen.setVisibility(View.VISIBLE);
        deathScreen.setAlpha(0);
        deathScreen.animate().alpha(1f).setDuration(100).start();
        btnRestart.setOnClickListener(v -> {
            hideDeathScreen();
            onGameRestart();
        });
    }

    private void hideDeathScreen() {
        deathScreen.setVisibility(View.GONE);
    }

    /**
     * idling animation use for bird at waiting status
     * goes up animation on user_click
     * fall down animation when bird falling
     */
    private void birdIdlingAnimationExec() {
        mainBird.setX(screenWidth / 7f);
        mainBird.setY(screenHeight / 2f);
        mainBird.setRotation(0);
        animBirdIdling.start();
    }

    private void birdGoesUp() {
        mainBird.animate().cancel();
        mainBird.flyUp();
        mainBird.animate().rotation(-20f).setDuration(300).start();
    }

    private void birdGoesDown() {
        mainBird.animate().cancel();
        mainBird.fallDown();
        mainBird.animate().rotation(60f).setDuration(800).start();
    }

    /**
     * sett game score
     *
     * @param currentScore the current bird score
     *                     set high score at waiting screen
     */
    private void setGameScore(int currentScore) {
        NumberUtils.setGameScore(DEFAULT_SCORE_NUMBER, currentScore,
                imgLastNumber, imgSecondNumber,
                imgFirstNumber, numberMap);
    }

    /**
     * synchronize bird's coin with the text view
     */
    private void setGameCoin() {
        String text = " x " + mainBird.coin;
        txtCoin.setText(text);
    }

    /**
     * set effect to the coin text whenever an event happen
     *
     * @param toColor color to change to
     */
    private void coinColor(int toColor) {
        txtCoin.setTextColor(toColor);
        imgCoin.animate().alpha(1).setDuration(400).withEndAction(() -> txtCoin.setTextColor(Color.WHITE));
    }

    /**
     * synchronize bird's score with high score
     */
    private void setHighScore() {
        int scoreCount = DEFAULT_SCORE_NUMBER;
        highScore = sharedPreferences.getInt("highScore", 0);
        while (highScore > 0) {
            int last = highScore % 10;
            highScore /= 10;
            if (scoreCount == 3) {
                int source = numberMap.get(last);
                imgLastHighestNumber.setImageResource(source);
            } else if (scoreCount == 2) {
                int source = numberMap.get(last);
                imgSecondHighestNumber.setImageResource(source);
            } else if (scoreCount == 1) {
                int source = numberMap.get(last);
                imgFirstHighestNumber.setImageResource(source);
                return;
            }
            scoreCount--;
        }
    }

    /**
     * start the pipe animation
     *
     * @param pipeUp   upper pipe
     * @param pipeDown downward pipe
     */
    private void startObjectAnimation(PipeObject pipeUp, PipeObject pipeDown) {
        pipeUp.animate().translationX(-(pipeWidth))
                .setInterpolator(linearInterpolator)
                .setDuration(pipeSpeed).start();
        pipeDown.animate().translationX(-(pipeWidth))
                .setInterpolator(linearInterpolator)
                .setDuration(pipeSpeed)
                .withEndAction(() -> {
                    PipeObject[] pipePair1 = pipeList.poll();
                    assert pipePair1 != null;
                    pipePair1[0].animate().cancel();
                    pipePair1[1].animate().cancel();
                    layoutParent.removeView(pipePair1[0]);
                    layoutParent.removeView(pipePair1[1]);
                    pipePair1[0] = null;
                    pipePair1[1] = null;
                })
                .start();
    }

    /**
     * start the super power animation
     *
     * @param superPowerObject the power needed to animate
     */
    private void startObjectAnimation(SuperPowerObject superPowerObject) {
        superPowerObject.animate().translationX(-pipeWidth)
                .setInterpolator(linearInterpolator)
                .setDuration(powerSpeed)
                .withEndAction(() -> {
                    SuperPowerObject superPowerObject1 = powerList.poll();
                    assert superPowerObject1 != null;
                    superPowerObject1.animate().cancel();
                    layoutParent.removeView(superPowerObject1);
                })
                .start();
    }

    private void countPipe() {
        pipeCount++;
        if (mainBird.getSuperPower().equals(SuperPower.NONE)) {
            pipeCountForPower++;
        }
    }

    /**
     * change the remaining pipe regard to the corresponding power
     */
    private void changePipeColor() {
        if (mainBird.getSuperPower().equals(SuperPower.POISON)) {
            pipeSourceUp = pipePurpleUp;
            pipeSourceDown = pipePurple;
        } else if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
            pipeSourceDown = pipeGolden;
            pipeSourceUp = pipeGoldenUp;
        }
        if (pipeList.size() > 0) {
            for (int i = 0; i < pipeList.size(); i++) {
                PipeObject[] pipePair = pipeList.get(i);
                PipeObject pipeUp = pipePair[0];
                PipeObject pipeDown = pipePair[1];
                pipeUp.setImageBitmap(pipeSourceUp);
                pipeDown.setImageBitmap(pipeSourceDown);
            }
        }
    }

    /**
     * reset to green pipe whenever a power is exhausted
     */
    private void resetPipeColor() {
        pipeSourceUp = pipeGreenUp;
        pipeSourceDown = pipeGreen;
        if (pipeList.size() > 0) {
            for (int i = 0; i < pipeList.size(); i++) {
                PipeObject[] pipePair = pipeList.get(i);
                if (!(pipePair[0].getPipeBitmap() == (pipeRedUp))) {
                    pipePair[0].setImageBitmap(pipeSourceUp);
                    pipePair[1].setImageBitmap(pipeSourceDown);
                }
            }
        }
    }

    /**
     * resume animating object on what ever the pause reason is
     * is used to deal with synchronization of super speed
     */
    private void resumeObjectAnimation() {
        resumePipeAnimation();
        resumePowerAnimation();
    }

    /**
     * for speed power only
     * make the fast pipe back to normal speed
     */
    private void resumePipeAnimation() {
        if (pipeList.size() > 0) {
            for (int i = 0; i < pipeList.size(); i++) {
                PipeObject[] pipePair = pipeList.get(i);
                PipeObject pipeUp = pipePair[0];
                PipeObject pipeDown = pipePair[1];
                long dur = getTimeToGo(pipeUp.getX());
                pipeUp.animate().translationX(-(pipeWidth))
                        .setInterpolator(linearInterpolator)
                        .setDuration(dur).start();
                pipeDown.animate().translationX(-(pipeWidth))
                        .setInterpolator(linearInterpolator)
                        .setDuration(dur)
                        .withEndAction(() -> {
                            PipeObject[] pipePair1 = pipeList.poll();
                            assert pipePair1 != null;
                            pipePair1[0].animate().cancel();
                            pipePair1[1].animate().cancel();
                            layoutParent.removeView(pipePair1[0]);
                            layoutParent.removeView(pipePair1[1]);
                            pipePair1[0] = null;
                            pipePair1[1] = null;
                        })
                        .start();
            }
        }
    }

    /**
     * for speed power only
     * make the fast power back to normal speed
     */
    private void resumePowerAnimation() {
        if (powerList.size() > 0) {
            for (int i = 0; i < powerList.size(); i++) {
                SuperPowerObject object = powerList.get(i);
                long dur = powerSpeed - (int) getTimeToGo(object.getX());
                object.animate().translationX(-pipeWidth)
                        .setInterpolator(linearInterpolator)
                        .setDuration(dur)
                        .withEndAction(() -> {
                            SuperPowerObject object1 = powerList.poll();
                            assert object1 != null;
                            object1.animate().cancel();
                            layoutParent.removeView(object1);
                        })
                        .start();
            }
        }
    }

    /**
     * clear object animation on death and on pause
     */
    private void clearObjectAnimation() {
        GameUtils.PipeNPower.clearAnimation(pipeList, powerList);
    }

    /**
     * clear the remaining object when bird is died
     */
    private void clearRemainingObject() {
        GameUtils.PipeNPower.clearObject(pipeList, powerList, layoutParent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        animFlippingCoin.stop();
        setReminder();
        editor.putInt("coin", mainBird.coin);
        editor.commit();
        if (backgrMusic != null) {
            backgrMusic.pause();
        }
        if (mainBird.getStatus().equals(BirdObject.PLAYING)) {
            pauseGameManually();
        }
    }

    /**
     * set the reminder at 7.am the next morning
     */
    private void setReminder() {
        if (!sharedPreferences.getBoolean("startedService", false)) {
            editor.putBoolean("startedService", true);
            editor.commit();
            Intent intent = new Intent(this, AlarmReminder.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, JOB_ID_SEVEN, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DateUtils.getTimeToGo(), pendingIntent);
        }
    }

    /**
     * some random stuff
     *
     * @param currentPos current pos of object on-screen
     * @return time to go
     */
    private long getTimeToGo(float currentPos) {
        float n = pipeSpeed - (pipeSpeed / (float)(screenWidth + pipeWidth) * (screenWidth - currentPos));
        return n > 0 ? (long) n : pipeSpeed;
    }

    /**
     * call whenever bird acquires super power and trigger it
     */
    public void actionPowerEffect() {
        mainBird.startAnimation(animPowerUp);

        if (soundOn) {
            powerUp.start();
        }
        if (mainBird.getSuperPower().equals(SuperPower.GIANT)) {
            turnGiant();
        } else if (mainBird.getSuperPower().equals(SuperPower.SPEED)) {
            turnSpeed();
        } else if (mainBird.getSuperPower().equals(SuperPower.INVULNERABLE)) {
            turnInvulnerable();
        } else if (mainBird.getSuperPower().equals(SuperPower.POISON)) {
            turnPoison();
        } else if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
            turnGolden();
        }
    }

    /**
     * bird receive golden power
     * <p>
     * blue 4 point
     * purple 7 point
     * red 7 point
     * golden 10 point
     * yellow 13 point
     */
    private void turnGolden() {
        mainBird.setImageSource(goldenBirdSource);
        imgDoubleSrc.setAlpha(1f);
        changePipeColor();
        powerExecHandler.postDelayed(onPowerExhaust, (long) (DEFAULT_PIPE_GENERATION * 4.5f));
    }

    /**
     * bird receive poison power
     */
    private void turnPoison() {
        mainBird.setImageSource(poisonBirdSource);
        changePipeColor();
        powerExecHandler.postDelayed(onPowerExhaust, DEFAULT_PIPE_GENERATION * 6);
    }

    /**
     * bird receive invulnerable power
     */
    private void turnInvulnerable() {
        mainBird.setAlpha(0.5f);
        powerExecHandler.postDelayed(onPowerExhaust, DEFAULT_PIPE_GENERATION * 3);
    }

    /**
     * bird receive speed power
     */
    private void turnSpeed() {
        mainBird.setImageSource(speedBirdSource);
        gameSpeedUp(9f);
        resumeObjectAnimation();
        powerExecHandler.postDelayed(onPowerExhaust, DEFAULT_PIPE_GENERATION);
    }

    /**
     * bird receive giant power
     */
    private void turnGiant() {
        mainBird.setImageSource(giantBirdSource);
        powerExecHandler.postDelayed(onPowerExhaust, DEFAULT_PIPE_GENERATION * 6);
    }

    /**
     * get a random super power to release
     * the rarity is different between super powers
     * <p>
     * rarity chance: Invulnerable > Poison, Giant > Golden > Speed
     *
     * @return random power
     */
    private SuperPower getRandomSuperPower() {
        int random = (int) ((Math.random() * 10) + 1);
        if (random == 1) {
            return SuperPower.SPEED;
        } else if (random >= 2 && random <= 3) {
            return SuperPower.GOLDEN;
        } else if (random >= 4 && random <= 7) {
            return SuperPower.INVULNERABLE;
        } else {
            return ((int) ((Math.random() * 2) + 1)) == 1 ? SuperPower.GIANT : SuperPower.POISON;
        }
    }


    /**
     * set the game score
     */
    public void actionAddScore() {
        setGameScore(mainBird.score);
    }

    /**
     * remove power from off-screen event or bird's intersection
     *
     * @param superPowerObject power to remove
     */
    public void actionRemovePower(SuperPowerObject superPowerObject) {
        superPowerObject.animate().cancel();
        layoutParent.removeView(superPowerObject);
        powerList.remove(superPowerObject);
    }

    /**
     * for Giant and Speed super power only
     * rotate the pipe on intersection
     *
     * @param pipe the pipe to rotate
     */
    public void rotatePipe(PipeObject pipe) {
        if (!pipe.playedAnimation) {
            pipe.playedAnimation = true;
            if (pipe.getPivotY() == 0) {
                ObjectAnimator animRotateUp = ObjectAnimator.ofFloat(pipe, "rotation", 0, -90)
                        .setDuration(300);
                animRotateUp.setInterpolator(linearInterpolator);
                animRotateUp.start();
            } else {
                ObjectAnimator animRotateDown = ObjectAnimator.ofFloat(pipe, "rotation", 0, 90)
                        .setDuration(300);
                animRotateDown.setInterpolator(linearInterpolator);
                animRotateDown.start();
            }
        }
    }

    /**
     * for Poison super power only
     * withdraw the pipe on half-screen passed
     *
     * @param pipeU pipe to withdraw
     * @param pipeD pipe to withdraw
     */
    public void withDrawPipe(PipeObject pipeU, PipeObject pipeD) {
        if (!pipeU.playedAnimation) {
            pipeU.playedAnimation = true;
            ObjectAnimator animWidthDrawUp = ObjectAnimator
                    .ofFloat(pipeU, "translationY", pipeU.getY(), -pipeHeight + (int) (baseHeight / 4.0))
                    .setDuration(500);
            animWidthDrawUp.setInterpolator(decelerateInterpolator);
            ObjectAnimator animWidthDrawDown = ObjectAnimator
                    .ofFloat(pipeD, "translationY", pipeD.getY(), screenHeight - baseHeight - (int) (baseHeight / 8.0))
                    .setDuration(500);
            animWidthDrawDown.setInterpolator(decelerateInterpolator);
            animWidthDrawUp.start();
            animWidthDrawDown.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgrMusic != null) {
            backgrMusic.release();
        }
    }
}