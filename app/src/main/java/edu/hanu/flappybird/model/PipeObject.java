package edu.hanu.flappybird.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class PipeObject extends AppCompatImageView {
    Bitmap pipeBitmap;
    public boolean isScored;
    public boolean playedAnimation;
    public PipeObject(@NonNull Context context, Bitmap pipeBitmap) {
        super(context);
        this.pipeBitmap = pipeBitmap;
        initialize();
    }

    public PipeObject(@NonNull Context context) {
        super(context);
        initialize();
    }

    public PipeObject(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PipeObject(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setImageBitmap(pipeBitmap);
        isScored = false;
        playedAnimation = false;
    }

    public Bitmap getPipeBitmap() {
        return pipeBitmap;
    }
}
