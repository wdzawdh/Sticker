package com.cw.sticker.sticker;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public interface ISticker {
    void draw(@NonNull Canvas canvas);

    float getRotate();

    PointF getPosition();

    float getScale();

    float getScale(Rect standardRect);

    void setColor(int color);

    int getColor();

    void setEditorTouched(boolean isTouched);

    void actionMove(float dx, float dy);

    void updateScale(final float dx, final float dy);

    void updateRotate(final float dx, final float dy);

    boolean isInside(MotionEvent event);

    boolean isInDeleteHandleButton(MotionEvent event);

    boolean isInEditHandleButton(MotionEvent event);

    boolean isInScaleHandleButton(MotionEvent event);

    boolean isInRotateHandleButton(MotionEvent event);

    void setHelpFrameEnabled(boolean enabled);
}
