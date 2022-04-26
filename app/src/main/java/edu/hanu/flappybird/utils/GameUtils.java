package edu.hanu.flappybird.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedList;

import edu.hanu.flappybird.R;
import edu.hanu.flappybird.model.BirdObject;
import edu.hanu.flappybird.model.PipeObject;
import edu.hanu.flappybird.model.SuperPowerObject;

public class GameUtils {
    public static float minY, maxY;
    public static class Initializer {
        public static Resources resources;

        public static void initBirdSource(Bitmap[][] birdSource) {
            for (int i = 0; i < birdSource.length; i++) {
                if (i == 0) {
                    birdSource[i][0] = BitmapFactory.decodeResource(resources, R.drawable.bluebird_upflap);
                    birdSource[i][1] = BitmapFactory.decodeResource(resources, R.drawable.bluebird_midflap);
                    birdSource[i][2] = BitmapFactory.decodeResource(resources, R.drawable.bluebird_downflap);
                } else if (i == 1) {
                    birdSource[i][0] = BitmapFactory.decodeResource(resources, R.drawable.redbird_upflap);
                    birdSource[i][1] = BitmapFactory.decodeResource(resources, R.drawable.redbird_midflap);
                    birdSource[i][2] = BitmapFactory.decodeResource(resources, R.drawable.redbird_downflap);
                } else if (i == 2) {
                    birdSource[i][0] = BitmapFactory.decodeResource(resources, R.drawable.yellowbird_upflap);
                    birdSource[i][1] = BitmapFactory.decodeResource(resources, R.drawable.yellowbird_midflap);
                    birdSource[i][2] = BitmapFactory.decodeResource(resources, R.drawable.yellowbird_downflap);
                } else if (i == 3) {
                    birdSource[i][0] = BitmapFactory.decodeResource(resources, R.drawable.purplebird_upflap);
                    birdSource[i][1] = BitmapFactory.decodeResource(resources, R.drawable.purplebird_midflap);
                    birdSource[i][2] = BitmapFactory.decodeResource(resources, R.drawable.purplebird_downflap);

                } else if (i == 4) {
                    birdSource[i][0] = BitmapFactory.decodeResource(resources, R.drawable.goldenbird_upflap);
                    birdSource[i][1] = BitmapFactory.decodeResource(resources, R.drawable.goldenbird_midflap);
                    birdSource[i][2] = BitmapFactory.decodeResource(resources, R.drawable.goldenbird_downflap);

                }
            }
        }

        public static Bitmap[] initPipeSource(Bitmap[] pipeSource) {
            pipeSource[0] = BitmapFactory.decodeResource(resources, R.drawable.pipe_green);
            pipeSource[1] = BitmapFactory.decodeResource(resources, R.drawable.pipe_green_up);
            pipeSource[2] = BitmapFactory.decodeResource(resources, R.drawable.pipe_red);
            pipeSource[3] = BitmapFactory.decodeResource(resources, R.drawable.pipe_red_up);
            pipeSource[4] = BitmapFactory.decodeResource(resources, R.drawable.pipe_purple);
            pipeSource[5] = BitmapFactory.decodeResource(resources, R.drawable.pipe_purple_up);
            pipeSource[6] = BitmapFactory.decodeResource(resources, R.drawable.pipe_golden);
            pipeSource[7] = BitmapFactory.decodeResource(resources, R.drawable.pipe_golden_up);
            return pipeSource;
        }

        public static void initScoreSrc(HashMap<Integer, Integer> numberMap) {
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
    }

    public static class Bird {
        public static BirdObject birdObject;

        public static void initBirdObject(RelativeLayout layoutParent,
                                          LinearLayout.LayoutParams layoutParams) {
            birdObject.setLayoutParams(layoutParams);
            layoutParent.addView(birdObject);
            birdObject.setZ(0.75f);

        }
    }

    public static class PipeNPower {
        public static int pipeHeight,screenHeight,screenWidth;

        public static void addPipe(PipeObject pipeUp, PipeObject pipeDown,
                                   RelativeLayout layoutParent,
                                   LinkedList<PipeObject[]> pipeList,
                                   PipeObject[] pipePair) {
            layoutParent.addView(pipeUp);
            layoutParent.addView(pipeDown);

            pipeDown.setPivotX(0);
            pipeDown.setPivotY(pipeHeight * 2);
            pipeDown.setX(screenWidth);
            pipeDown.setY(getRandomY());

            pipeUp.setPivotX(0);
            pipeUp.setPivotY(0);
            pipeUp.setX(pipeDown.getX());
            pipeUp.setY(pipeDown.getY() - pipeHeight - screenHeight / 5.25f);
            pipeList.offer(pipePair);
        }

        public static void addPower(SuperPowerObject object, RelativeLayout layoutParent,
                                    LinkedList<SuperPowerObject> powerList) {
            layoutParent.addView(object);
            object.setX(screenWidth);
            object.setY(GameUtils.getRandomY() - minY);
            powerList.offer(object);
        }

        public static void clearAnimation(LinkedList<PipeObject[]> pipeList,
                                          LinkedList<SuperPowerObject> powerList) {
            if (pipeList.size() > 0) {
                for (PipeObject[] pipePair :
                        pipeList) {
                    PipeObject pipeUp = pipePair[0];
                    PipeObject pipeDown = pipePair[1];

                    pipeUp.animate().cancel();
                    pipeDown.animate().cancel();
                }
            }
            if (powerList.size() > 0) {
                for (SuperPowerObject superPower :
                        powerList) {
                    superPower.animate().cancel();
                }
            }
        }

        public static void clearObject(LinkedList<PipeObject[]> pipeList,
                                       LinkedList<SuperPowerObject> powerList,
                                       RelativeLayout layoutParent) {
            if (pipeList.size() > 0) {
                for (PipeObject[] pipePair :
                        pipeList) {
                    PipeObject pipeUp = pipePair[0];
                    PipeObject pipeDown = pipePair[1];

                    layoutParent.removeView(pipeUp);
                    layoutParent.removeView(pipeDown);
                }
                pipeList.clear();
            }
            if (powerList.size() > 0) {
                for (SuperPowerObject superPower :
                        powerList) {
                    layoutParent.removeView(superPower);
                    superPower = null;
                }
                powerList.clear();
            }
        }
    }

    public static int getRandomY() {
        return (int) (Math.random() * (maxY - minY + 1) + minY);
    }
}
