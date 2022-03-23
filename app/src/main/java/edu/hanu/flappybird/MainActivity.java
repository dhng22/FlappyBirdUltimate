package edu.hanu.flappybird;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *  Game created by duy hung
 *  @dhng.22
 */
public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_SCORE_NUMBER = 3;
    public static final int DEFAULT_PIPE_GENERATION = 2100;
    public static final int DEFAULT_PIPE_SPEED = 3000;
    public static final int DEFAULT_POWER_GENERATION = 23100;
    public static final int DEFAULT_DAY_NIGHT_CYCLE = 60000;
    BirdObject mainBird;
    Context context;
    GameThread gameThread;
    ImageButton btnPause, btnPlay, btnReplay, btnRestart, btnSound, btnMusic;
    RelativeLayout layoutParent, waitingScreen, pauseScreen, deathScreen, gameControlPanel;
    ImageView imgMessage, imgLastHighestNumber, imgSecondHighestNumber, imgFirstHighestNumber,imgDoubleSrc,
            imgLastNumber, imgSecondNumber, imgFirstNumber, imgBase,imgBaseNight, backgroundNight,cloudScreen,cloudScreen2;
    LinearLayout.LayoutParams params, powerParams;
    Animation animMessageBlink, animBirdBlinkStart, animBirdBlinkEnd, animPowerUp;
    ObjectAnimator animBirdIdling, animBirdGoingUp, animBirdGoingDown, movingCloud,movingCloud2;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    HashMap<Integer, Integer> numberMap;
    View.OnClickListener controlBirdListener, waitingScreenListener;
    LinkedList<PipeObject[]> pipeList;
    LinkedList<SuperPowerObject> powerList;
    Handler addPipeHandler, addPowerHandler, normalHandler, powerExecHandler;
    BroadcastReceiver receiveBirdGoesDown, receiveOnDead, receiveOnSuperPower, receiveScore, receiverRemovePower;
    LocalBroadcastManager broadcastManager;
    Runnable execPipe, execPower, onPowerExhaust;
    LinearInterpolator linearInterpolator;
    IntentFilter actionBirdGoesDownFilter, actionBirdDeadFilter, actionBirdPowerEffect, actionAddScore, actionRemovePower;
    int screenHeight, screenWidth, baseHeight, highScore,powerSpeed, pipeGenSpeed, pipeCount, pipeSpeed, pipeWidth,pipeHeight;
    float minY, maxY;
    boolean isDay, soundOn, musicOn;
    Bitmap[] giantBirdSource, speedBirdSource, originalSource, poisonBirdSource, goldenBirdSource;
    Bitmap pipeGreen,pipeGreenUp,pipeRed,pipeRedUp,pipePurple,pipePurpleUp,pipeSourceDown, pipeSourceUp,pipeGolden,pipeGoldenUp;
    MediaPlayer soundDead, soundWing, powerUp, soundSwoosh, backgrMusic;
    DecelerateInterpolator decelerateInterpolator;
    AccelerateInterpolator accelerateInterpolator;
    AlarmManager alarmManager;
    Intent reminderIntent;
    PendingIntent reminderPending;
    Calendar calendar,calendar2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initialize();
    }

    private void initialize() {
        mapping();
        initParams();
        initSharedPreferences();
        prepareForHighScore();
        prepareForThread();
        initReceiver();
    }

    /**
     * Just mapping View
     */
    private void mapping() {
        // executing button
        btnPause = findViewById(R.id.btn_pause);
        btnPlay = findViewById(R.id.btn_play);
        btnReplay = findViewById(R.id.btn_replay);
        btnRestart = findViewById(R.id.btn_restart);
        btnSound = findViewById(R.id.btn_Sound);
        btnMusic = findViewById(R.id.btn_Music);

        // main screen components
        cloudScreen = findViewById(R.id.imgCloudScreen);
        cloudScreen2 = findViewById(R.id.imgCloudScreen2);

        backgroundNight = findViewById(R.id.imgBackGrNight);
        layoutParent = findViewById(R.id.layoutParent);
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

        // resource to change bird bitmap depends on its super power
        giantBirdSource = new Bitmap[3];
        speedBirdSource = new Bitmap[3];
        originalSource = new Bitmap[3];
        poisonBirdSource = new Bitmap[3];
        goldenBirdSource = new Bitmap[3];
        giantBirdSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_upflap);
        giantBirdSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_midflap);
        giantBirdSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_downflap);
        speedBirdSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_upflap);
        speedBirdSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_midflap);
        speedBirdSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_downflap);
        originalSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap);
        originalSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap);
        originalSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_downflap);
        poisonBirdSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.purplebird_upflap);
        poisonBirdSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.purplebird_midflap);
        poisonBirdSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.purplebird_downflap);
        goldenBirdSource[0] = BitmapFactory.decodeResource(getResources(), R.drawable.goldenbird_upflap);
        goldenBirdSource[1] = BitmapFactory.decodeResource(getResources(), R.drawable.goldenbird_midflap);
        goldenBirdSource[2] = BitmapFactory.decodeResource(getResources(), R.drawable.goldenbird_downflap);
        //resource to change pipe depends on bird power
        pipeGreen = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_green);
        pipeGreenUp = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_green_up);
        pipeRed = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_red);
        pipeRedUp = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_red_up);
        pipePurple = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_purple);
        pipePurpleUp = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_purple_up);
        pipeGolden = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_golden);
        pipeGoldenUp = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_golden_up);


        // asc variables
        backgrMusic = MediaPlayer.create(this, R.raw.background);
        isDay = true;
        cloudScreen.setX(screenWidth);
        cloudScreen2.setX(0);
        pipeSourceDown = pipeGreen;
        pipeSourceUp = pipeGreenUp;
        pipeCount = 0;
        pipeSpeed = DEFAULT_PIPE_SPEED;
        pipeGenSpeed = DEFAULT_PIPE_GENERATION;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        powerParams = new LinearLayout.LayoutParams(screenWidth / 14, screenHeight / 18);
        mainBird = new BirdObject(this);
        mainBird.setLayoutParams(params);
        layoutParent.addView(mainBird);
        mainBird.setZ(0.75f);
        minY = screenHeight / 4f;

        gameThread = new GameThread(this, layoutParent, mainBird);
        imgBase.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgBase.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                baseHeight = imgBase.getHeight();
                maxY = screenHeight - baseHeight - (screenHeight / 15f);
                Rect baseRect = new Rect();
                imgBase.getHitRect(baseRect);
                gameThread.setBaseRect(baseRect);
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
                layoutParent.removeView(pipeObject);
                gameThread.setPipeHeight(pipeHeight);

                powerSpeed = (pipeSpeed / (screenWidth + pipeWidth)) * ((pipeWidth * 8) + screenWidth);
            }
        });

        // interpolator animating moving object except bird
        linearInterpolator = new LinearInterpolator();

        // loading animation
        decelerateInterpolator = new DecelerateInterpolator();
        accelerateInterpolator = new AccelerateInterpolator();
        animMessageBlink = AnimationUtils.loadAnimation(this, R.anim.anim_blink);
        animBirdBlinkStart = AnimationUtils.loadAnimation(this, R.anim.anim_bird_blink_start);
        animBirdBlinkEnd = AnimationUtils.loadAnimation(this, R.anim.anim_bird_blink_end);
        animPowerUp = AnimationUtils.loadAnimation(this, R.anim.anim_power_up);
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
                if (mainBird.getStatus().equals(BirdObject.PLAYING)) {
                    addPowerHandler.postDelayed(execPower, DEFAULT_POWER_GENERATION);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animBirdIdling = ObjectAnimator.ofFloat(mainBird, "translationY", screenHeight / 2f, screenHeight / 2f + 60)
                .setDuration(500);
        animBirdIdling.setRepeatCount(ValueAnimator.INFINITE);
        animBirdIdling.setRepeatMode(ValueAnimator.REVERSE);

        // two main listener control game and control waiting screen
        waitingScreenListener = v -> {
            gameThread.globalCount = 0;
            mainBird.setStatus(BirdObject.PLAYING);
            layoutParent.setOnClickListener(controlBirdListener);
            hideWaitingScreen();
            layoutParent.performClick();
            startAddingObject();
        };
        //TODO
        controlBirdListener = v -> {
            runOnUiThread(() -> {
                animBirdGoingDown.cancel();
                animBirdGoingUp.setFloatValues(mainBird.getY(), mainBird.getY() - screenHeight / 7.75f);
                animBirdGoingUp.start();
                birdGoesUp();
                if (soundOn) {
                    soundWing.start();
                }
                mainBird.userTouch = true;
                gameThread.flyCount = 0;
            });
        };

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
                pipeCount++;
                if (pipeCount % 10 == 0 && mainBird.getSuperPower().equals(SuperPower.NONE)) {
                    pipeSourceDown = pipeRed;
                    pipeSourceUp = pipeRedUp;
                } else if (mainBird.getSuperPower().equals(SuperPower.NONE)) {
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
                    layoutParent.addView(pipeUp);
                    layoutParent.addView(pipeDown);

                    pipeDown.setPivotX(0);
                    pipeDown.setPivotY(pipeHeight*2);
                    pipeDown.setX(screenWidth);
                    pipeDown.setY(getRandomY());

                    pipeUp.setPivotX(0);
                    pipeUp.setPivotY(0);
                    pipeUp.setX(pipeDown.getX());
                    pipeUp.setY(pipeDown.getY() - pipeHeight - screenHeight / 5.25f);
                    pipeList.offer(pipePair);
                    startObjectAnimation(pipeUp, pipeDown);
                });
                addPipeHandler.postDelayed(this, pipeGenSpeed);
            }
        };
        execPower = new Runnable() {
            @Override
            public void run() {
                SuperPowerObject object = new SuperPowerObject(context, getRandomSuperPower());
                object.setLayoutParams(powerParams);
                runOnUiThread(() -> {
                    layoutParent.addView(object);
                    object.setX(screenWidth + (4 * pipeWidth));
                    object.setY(getRandomY() - minY);
                    powerList.offer(object);
                    startObjectAnimation(object);
                });

                addPowerHandler.postDelayed(this, DEFAULT_POWER_GENERATION);
            }
        };

        onPowerExhaust = () -> mainBird.startAnimation(animBirdBlinkStart);


        //creating sound
        soundDead = MediaPlayer.create(this, R.raw.die);
        soundWing = MediaPlayer.create(this, R.raw.wing_1);
        powerUp = MediaPlayer.create(this, R.raw.power_up);
        soundSwoosh = MediaPlayer.create(context, R.raw.swoosh_1);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        reminderIntent = new Intent(this, AlarmReminderReceiver.class);
        reminderIntent.setAction(AlarmReminderReceiver.actionRemind);
        reminderIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        reminderPending = PendingIntent.getBroadcast(this, AlarmReminderReceiver.reminderId, reminderIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        calendar = Calendar.getInstance();
        calendar2 = Calendar.getInstance();
    }

    /**
     * Initialize receiver that will receive broadcast from game_thread
     */
    private void initReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);

        // request when bird head down
        receiveBirdGoesDown = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("actionBirdGoesDown")) {
                    birdGoesDown();
                }
            }
        };
        // request when bird intersect with obstacle and die
        receiveOnDead = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("actionBirdDead")) {
                    setToDeadStatus();
                }
            }
        };

        //remove off_screen power
        receiverRemovePower = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("actionRemovePower")) {
                    SuperPowerObject power = (SuperPowerObject) intent.getSerializableExtra("power");
                    power.animate().cancel();
                    layoutParent.removeView(power);
                    powerList.remove(power);
                    power = null;
                }
            }
        };
        // request when bird obtain a super power
        receiveOnSuperPower = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("actionPowerEffect")) {
                    mainBird.startAnimation(animPowerUp);
                    addPowerHandler.removeCallbacks(execPower);
                    if (soundOn) {
                        powerUp.start();
                    }
                    if (mainBird.getSuperPower().equals(SuperPower.GIANT)) {
                        mainBird.setImageSource(giantBirdSource);
                        powerExecHandler.postDelayed(onPowerExhaust, 12000);
                    } else if (mainBird.getSuperPower().equals(SuperPower.SPEED)) {
                        mainBird.setImageSource(speedBirdSource);
                        gameSpeedUp(6f);
                        resumeObjectAnimation();
                        powerExecHandler.postDelayed(onPowerExhaust, 2000);
                    } else if (mainBird.getSuperPower().equals(SuperPower.INVULNERABLE)) {
                        mainBird.setAlpha(0.5f);
                        powerExecHandler.postDelayed(onPowerExhaust, 12000);
                    } else if (mainBird.getSuperPower().equals(SuperPower.POISON)) {
                        mainBird.setImageSource(poisonBirdSource);
                        changePipeColor();
                        powerExecHandler.postDelayed(onPowerExhaust, 12000);
                    } else if (mainBird.getSuperPower().equals(SuperPower.GOLDEN)) {
                        imgDoubleSrc.setAlpha(1f);
                        mainBird.setImageSource(goldenBirdSource);
                        changePipeColor();
                        powerExecHandler.postDelayed(onPowerExhaust, 6300);
                    }
                }
            }
        };
        // request to add score when bird pass haft of a pipe without dying
        receiveScore = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("actionAddScore")) {
                    setGameScore(mainBird.score);
                }
            }
        };

        //init intent filter
        actionBirdGoesDownFilter = new IntentFilter("actionBirdGoesDown");
        actionBirdDeadFilter = new IntentFilter("actionBirdDead");
        actionBirdPowerEffect = new IntentFilter("actionPowerEffect");
        actionAddScore = new IntentFilter("actionAddScore");
        actionRemovePower = new IntentFilter("actionRemovePower");

    }

    /**
     * register receiver to local broadcast manager on onResume
     */
    private void registerReceiver() {
        broadcastManager.registerReceiver(receiveBirdGoesDown, actionBirdGoesDownFilter);
        broadcastManager.registerReceiver(receiveOnDead, actionBirdDeadFilter);
        broadcastManager.registerReceiver(receiveOnSuperPower, actionBirdPowerEffect);
        broadcastManager.registerReceiver(receiveScore, actionAddScore);
        broadcastManager.registerReceiver(receiverRemovePower,actionRemovePower);
    }

    /**
     * unregister receiver on onPause to save resources
     */
    private void unregisterReceiver() {
        broadcastManager.unregisterReceiver(receiveBirdGoesDown);
        broadcastManager.unregisterReceiver(receiveOnDead);
        broadcastManager.unregisterReceiver(receiveOnSuperPower);
        broadcastManager.unregisterReceiver(receiveScore);
        broadcastManager.unregisterReceiver(receiverRemovePower);
    }

    /**
     * intit game thread and some secondary variables
     */
    private void prepareForThread() {

        gameThread.setPipeList(pipeList);
        gameThread.setPowerList(powerList);
        gameThread.setFloor(minY, maxY);
    }

    /**
     * init shared pref and its editor
     * get the stored high-score in shared preference
     */
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("highScoreData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        highScore = sharedPreferences.getInt("highScore", 0);

        soundOn = sharedPreferences.getBoolean("soundOn", true);
        musicOn = sharedPreferences.getBoolean("musicOn", true);
        if (!soundOn) {
            btnSound.setImageResource(R.drawable.sound_off);
        }
        if (!musicOn) {
            btnMusic.setImageResource(R.drawable.music_off);
        }
    }

    private void setSoundForThread(boolean soundOn) {
        gameThread.setSoundOn(soundOn);
    }
    /**
     * mapping high-score in waiting screen
     */
    private void prepareForHighScore() {
        numberMap = new HashMap<>();
        numberMap.put(0, R.drawable.zero);
        numberMap.put(1, R.drawable.one);
        numberMap.put(2, R.drawable.two);
        numberMap.put(3, R.drawable.three);
        numberMap.put(4, R.drawable.four);
        numberMap.put(5, R.drawable.five);
        numberMap.put(6, R.drawable.six);
        numberMap.put(7, R.drawable.seven);
        numberMap.put(8, R.drawable.eight);
        numberMap.put(9, R.drawable.nine);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSoundForThread(soundOn);
        initAnimation();
        registerReceiver();
        gameThread.onResume();
        if (mainBird.getStatus().equals(BirdObject.WAITING)) {
            setToWaitingStatus();
        }
    }

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
                animBirdGoingDown.setFloatValues(mainBird.getY(), screenHeight);
                animBirdGoingDown.start();
                birdGoesDown();
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

    /**
     * reset every thing to the waiting screen state
     */
    private void onGameRestart() {
        mainBird.setStatus(BirdObject.WAITING);
        btnPause.setEnabled(true);
        layoutParent.setOnClickListener(waitingScreenListener);
        hidePauseScreen();
        gameThread.onResume();
        setToWaitingStatus();
    }

    /**
     * setting the waiting screen and others related component
     */
    private void setToWaitingStatus() {
        movingCloud.start();
        movingCloud2.start();
        mainBird.setStatus(BirdObject.WAITING);
        showWaitingScreen();
        birdIdlingAnimationExec();
        setHighScore();
        hideGamePanel();
        clearSuperPower();
        clearRemainingPipe();
    }

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

    private void hideWaitingScreen() {
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
    private void startAddingObject() {
        startAddingPipe();
        startAddingPower();
    }

    private void startAddingPipe() {
        addPipeHandler.post(execPipe);
    }

    private void startAddingPower() {
        addPowerHandler.postDelayed(execPower, DEFAULT_POWER_GENERATION);
    }

    /**
     * game speed up and reset for speed superpower
     */
    private void resetGameSpeed() {
        this.pipeSpeed = DEFAULT_PIPE_SPEED;
        this.pipeGenSpeed = DEFAULT_PIPE_GENERATION;
    }

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
            if (musicOn) {
                if (backgrMusic == null) {
                    backgrMusic = MediaPlayer.create(this, R.raw.background);
                }
                backgrMusic.start();
            }
            addPipeHandler.postDelayed(execPipe, pipeGenSpeed);
            addPowerHandler.postDelayed(execPower, DEFAULT_POWER_GENERATION);
            resumeObjectAnimation();
            mainBird.setStatus(BirdObject.WAITING);
            gameThread.onResume();
            btnPause.setEnabled(true);
            layoutParent.setOnClickListener(controlBirdListener);
            gameThread.onManuallyResume();
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
            } else {
                musicOn = true;
                if (backgrMusic == null) {
                    backgrMusic = MediaPlayer.create(this, R.raw.background);
                }
                backgrMusic.start();
                btnMusic.setImageResource(R.drawable.music_on);
            }
            editor.putBoolean("musicOn", musicOn);
            editor.commit();
        });
        btnSound.setOnClickListener(v -> {
            if (soundOn) {
                soundOn = false;
                btnSound.setImageResource(R.drawable.sound_off);
            } else {
                soundOn = true;
                btnSound.setImageResource(R.drawable.sound_on);
            }
            gameThread.setSoundOn(soundOn);
            editor.putBoolean("soundOn", soundOn);
            editor.commit();
        });
    }

    private void hidePauseScreen() {
        pauseScreen.setVisibility(View.GONE);
    }

    private void pauseGameManually() {
        if (musicOn && backgrMusic != null) {
            backgrMusic.pause();
        }
        animBirdGoingDown.cancel();
        animBirdGoingUp.cancel();
        clearObjectAnimation();
        mainBird.setStatus(BirdObject.PAUSE);
        btnPause.setEnabled(false);
        layoutParent.setOnClickListener(null);
        showPauseScreen();
        mainBird.animate().cancel();
        mainBird.clearAnimation();
        animBirdBlinkStart.cancel();
        animBirdBlinkEnd.cancel();
        addPipeHandler.removeCallbacks(execPipe);
        addPowerHandler.removeCallbacks(execPower);
    }

    /**
     * set everything stop ( clear objects animation)
     * show death screen
     * store high score
     */
    private void setToDeadStatus() {
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
        mainBird.setAlpha(1f);
        mainBird.setStatus(BirdObject.DEAD);
        mainBird.animate().cancel();
        animBirdGoingUp.cancel();
        animBirdGoingDown.cancel();
        imgDoubleSrc.setAlpha(0f);

        movingCloud.cancel();
        movingCloud2.cancel();
        clearObjectAnimation();
        int highScore = Math.max(sharedPreferences.getInt("highScore", 0), mainBird.score);
        editor.putInt("highScore", highScore);
        editor.commit();
        addPipeHandler.removeCallbacks(execPipe);
        addPowerHandler.removeCallbacks(execPower);
        layoutParent.setOnClickListener(null);
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

    public void birdGoesUp() {
        mainBird.animate().cancel();
        mainBird.flyUp();
        mainBird.animate().rotation(-20f).setDuration(300).start();
    }

    public void birdGoesDown() {
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
        int scoreCount = DEFAULT_SCORE_NUMBER;
        if (currentScore == 0) {
            imgLastNumber.setImageResource(R.drawable.zero);
            imgSecondNumber.setImageResource(R.drawable.zero);
            imgFirstNumber.setImageResource(R.drawable.zero);
        }
        while (currentScore > 0) {
            int last = currentScore % 10;
            currentScore /= 10;
            if (scoreCount == 3) {
                int source = numberMap.get(last);
                imgLastNumber.setImageResource(source);
            } else if (scoreCount == 2) {
                int source = numberMap.get(last);
                imgSecondNumber.setImageResource(source);
            } else if (scoreCount == 1) {
                int source = numberMap.get(last);
                imgFirstNumber.setImageResource(source);
                return;
            }
            scoreCount--;
        }
    }

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
                    pipePair1 = null;
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
                    superPowerObject1 = null;
                })
                .start();
    }

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

    private void resetPipeColor() {
        pipeSourceUp = pipeGreenUp;
        pipeSourceDown = pipeGreen;
        if (pipeList.size() > 0) {
            for (int i = 0; i < pipeList.size(); i++) {
                PipeObject[] pipePair = pipeList.get(i);
                if (!(pipePair[0].getPipeBitmap()==(pipeRedUp))) {
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

    private void resumePipeAnimation() {
        if (pipeList.size() > 0) {
            normalHandler.postDelayed(() -> runOnUiThread(() -> {
                for (int i = 0; i < pipeList.size(); i++) {
                    PipeObject[] pipePair = pipeList.get(i);
                    PipeObject pipeUp = pipePair[0];
                    PipeObject pipeDown = pipePair[1];
                    long dur = pipeSpeed - (int) getCurrentTime(pipeUp.getX());
                    pipeUp.animate().translationX(-(pipeWidth))
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(dur<0?pipeSpeed:dur).start();
                    pipeDown.animate().translationX(-(pipeWidth))
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(dur<0?pipeSpeed:dur)
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
            }), 0);
        }
    }

    private void resumePowerAnimation() {
        if (powerList.size() > 0) {
            for (int i = 0; i < powerList.size(); i++) {
                SuperPowerObject object = powerList.get(i);
                long dur = powerSpeed - (int) getCurrentTime(object.getX());
                object.animate().translationX(-pipeWidth)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(dur < 0 ? pipeSpeed : dur)
                        .withEndAction(() -> {
                            SuperPowerObject object1 = powerList.poll();
                            assert object1 != null;
                            object1.animate().cancel();
                            layoutParent.removeView(object1);
                            object1 = null;
                        })
                        .start();
            }
        }
    }

    /**
     * clear object animation on death and on pause
     */
    private void clearObjectAnimation() {
        clearPipeAnimation();
        clearPowerAnimation();
    }

    private void clearPipeAnimation() {
        if (pipeList.size() > 0) {
            for (PipeObject[] pipePair :
                    pipeList) {
                PipeObject pipeUp = pipePair[0];
                PipeObject pipeDown = pipePair[1];

                pipeUp.animate().cancel();
                pipeDown.animate().cancel();
            }
        }
    }

    private void clearPowerAnimation() {
        if (powerList.size() > 0) {
            for (SuperPowerObject superPower :
                    powerList) {
                superPower.animate().cancel();
            }
        }
    }

    /**
     * clear the remaining object when bird is died
     */
    private void clearSuperPower() {
        if (powerList.size() > 0) {
            for (SuperPowerObject superPower :
                    powerList) {
                layoutParent.removeView(superPower);
                superPower = null;
            }
            powerList.clear();
        }
    }

    private void clearRemainingPipe() {
        if (pipeList.size() > 0) {
            for (PipeObject[] pipePair :
                    pipeList) {
                PipeObject pipeUp = pipePair[0];
                PipeObject pipeDown = pipePair[1];

                layoutParent.removeView(pipeUp);
                layoutParent.removeView(pipeDown);

                pipeUp = null;
                pipeDown = null;
            }
            pipeCount = 0;
            pipeList.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        long time = calendar.getTimeInMillis();
        if (time < System.currentTimeMillis()) {
            calendar2.set(Calendar.HOUR_OF_DAY, 24);
            calendar2.set(Calendar.MINUTE, 0);

            long minus = calendar2.getTimeInMillis() - calendar.getTimeInMillis();
            time += minus;
        }
        // set alarm notification
       alarmManager.setExact(AlarmManager.RTC_WAKEUP,time,reminderPending);
        if (backgrMusic != null) {
            backgrMusic.pause();
        }
        if (mainBird.getStatus().equals(BirdObject.PLAYING)) {
            unregisterReceiver();
            pauseGameManually();
        }
        try {
            gameThread.onPause();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * some random stuff
     *
     * @param currentPos
     * @return
     */
    public float getCurrentTime(float currentPos) {
        float n = (pipeSpeed / (float) (screenWidth + pipeWidth) * ((screenWidth + pipeWidth) - currentPos));
        return n >= 0 ? n : 0;
    }

    public int getRandomY() {
        return (int) (Math.random() * (maxY - minY + 1) + minY);
    }

    private SuperPower getRandomSuperPower() {
        int random = (int) ((Math.random() * 5) + 1);
        if (random == 1) {
            return SuperPower.GIANT;
        } else if (random == 2) {
            return SuperPower.SPEED;
        } else if (random == 3) {
            return SuperPower.INVULNERABLE;
        } else if (random == 4) {
            return SuperPower.POISON;
        } else {
            return SuperPower.GOLDEN;
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