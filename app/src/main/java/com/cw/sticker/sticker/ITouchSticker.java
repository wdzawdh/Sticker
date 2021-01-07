package com.cw.sticker.sticker;

import android.view.MotionEvent;

public interface ITouchSticker extends ISticker{
    void setEditorTouched(boolean isTouched);

    void setHelpFrameEnabled(boolean enabled);

    boolean getHelpFrameEnabled();

    boolean isInside(MotionEvent event);

    boolean isInDeleteHandleButton(MotionEvent event);

    boolean isInEditHandleButton(MotionEvent event);

    boolean isInScaleHandleButton(MotionEvent event);

    boolean isInRotateHandleButton(MotionEvent event);

    void updateMove(float dx, float dy);

    void updateScale(final float dx, final float dy);

    void updateRotate(final float dx, final float dy);
}
