package com.cw.sticker.sticker;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;

public interface ISticker {
    void draw(@NonNull Canvas canvas);

    void setStandardRect(Rect standardRect);

    float getRotate();

    PointF getPosition();

    float getScale();

    void setColor(int color);

    int getColor();
}
