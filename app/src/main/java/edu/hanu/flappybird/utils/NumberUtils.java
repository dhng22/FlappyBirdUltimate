package edu.hanu.flappybird.utils;
import android.widget.ImageView;
import java.util.HashMap;

import edu.hanu.flappybird.R;

public class NumberUtils {
    public static void setGameScore(int defaultScoreNumber, int currentScore, ImageView imgLastNumber,
                                    ImageView imgSecondNumber, ImageView imgFirstNumber,
                                    HashMap<Integer, Integer> numberMap) {
        int scoreCount = defaultScoreNumber;
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
}
