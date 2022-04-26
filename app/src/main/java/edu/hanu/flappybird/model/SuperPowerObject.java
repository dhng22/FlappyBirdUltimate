package edu.hanu.flappybird.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.io.Serializable;

import edu.hanu.flappybird.R;
import edu.hanu.flappybird.SuperPower;

public class SuperPowerObject extends AppCompatImageView implements Serializable {
    SuperPower superPower;
    Bitmap superBitmap;

    public SuperPowerObject(@NonNull Context context, @NonNull SuperPower superPower) {
        super(context);
        this.superPower = superPower;
        initialize();
    }
    public SuperPowerObject(@NonNull Context context) {
        super(context);
        initialize();
    }

    public SuperPowerObject(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SuperPowerObject(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        if (superPower.equals(SuperPower.INVULNERABLE)) {
            superBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potion_blue);
        } else if (superPower.equals(SuperPower.GIANT)) {
            superBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potion_red);
        } else if (superPower.equals(SuperPower.SPEED)) {
            superBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potion_yellow);
        } else if (superPower.equals(SuperPower.POISON)) {
            superBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potion_purple);
        } else {
            superBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potion_golden);
        }
        setImageBitmap(superBitmap);
    }
    public SuperPower getSuperPower() {
        return superPower;
    }

}
