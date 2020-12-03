package com.cw.sticker.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.MotionEvent;

import com.cw.sticker.utils.RectUtil;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;


public class EditorText implements ISticker {
    private static final float DEFAULT_TEXT_SIZE = 32f;

    private final EditorFrame mEditorFrame;
    private final Paint mHelperFramePaint;
    private final int mColor;
    private final RectF mClipRect;
    private final Point mTouchPoint = new Point();

    private Rect mTextRect;
    private RectF mFrameRect;

    private Rect mDeleteHandleSrcRect;
    private Rect mScaleHandleSrcRect;
    private Rect mRotateHandleSrcRect;
    private Rect mFrontHandleSrcRect;

    private RectF mDeleteHandleDstRect;
    private RectF mScaleHandleDstRect;
    private RectF mRotateHandleDstRect;
    private RectF mFrontHandleDstRect;

    private TextPaint mTextPaint;
    private String mText;
    private float mX;
    private float mY;
    private float mScale = 1;
    private float mRotateAngle = 0;
    private boolean mIsDrawHelperFrame = true;

    public EditorText(Context context, String text, @ColorInt int color, RectF clipRect) {
        mEditorFrame = new EditorFrame(context);
        mText = text;
        mColor = color;
        mClipRect = clipRect;
        mX = clipRect.centerX();
        mY = clipRect.centerY();
        mHelperFramePaint = new Paint(mEditorFrame.getFramePaint());
        initTextPaint();
        initEditorText();
    }

    private void initEditorText() {
        mTextRect = new Rect();
        mFrameRect = new RectF();

        mDeleteHandleSrcRect = new Rect(0, 0, mEditorFrame.getDeleteHandleBitmap().getWidth(),
                mEditorFrame.getDeleteHandleBitmap().getHeight());
        mScaleHandleSrcRect = new Rect(0, 0, mEditorFrame.getResizeHandleBitmap().getWidth(),
                mEditorFrame.getResizeHandleBitmap().getHeight());
        mRotateHandleSrcRect = new Rect(0, 0, mEditorFrame.getRotateHandleBitmap().getWidth(),
                mEditorFrame.getRotateHandleBitmap().getHeight());
        mFrontHandleSrcRect = new Rect(0, 0, mEditorFrame.getFrontHandleBitmap().getWidth(),
                mEditorFrame.getFrontHandleBitmap().getHeight());

        int handleHalfSize = mEditorFrame.getDeleteHandleBitmap().getWidth() >> 1;

        mDeleteHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mScaleHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mRotateHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mFrontHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
    }

    private void initTextPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mColor);
        mTextPaint.setAlpha(255);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    @Override
    public void setStandardRect(Rect standardRect) {
        if (standardRect != null) {
            mTextPaint.setTextSize(DEFAULT_TEXT_SIZE * mClipRect.width() / standardRect.width());
        }
    }

    @Override
    public void setColor(int color) {
        mTextPaint.setColor(color);
    }

    @Override
    public int getColor() {
        return mTextPaint.getColor();
    }

    @Override
    public float getRotate() {
        return mRotateAngle;
    }

    @Override
    public PointF getPosition() {
        return new PointF(mTextRect.centerX(), mTextRect.centerY());
    }

    @Override
    public float getScale() {
        return mScale;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int textLength = mText.length();
        mTextPaint.getTextBounds(mText, 0, textLength, mTextRect);
        mTextRect.offset((int) mX - (mTextRect.width() >> 1), (int) mY);

        mFrameRect = new RectF(mTextRect);
        updateFrameRect();
        RectUtil.scaleRect(mFrameRect, mScale);

        canvas.save();
        canvas.scale(mScale, mScale, mFrameRect.centerX(), mFrameRect.centerY());
        canvas.rotate(mRotateAngle, mFrameRect.centerX(), mFrameRect.centerY());
        canvas.drawText(mText, mX, mY, mTextPaint);
        canvas.restore();

        if (mIsDrawHelperFrame) {
            drawHelperFrame(canvas);
        }
    }

    private void drawHelperFrame(Canvas canvas) {
        int offsetValue = ((int) mDeleteHandleDstRect.width()) >> 1;

        mDeleteHandleDstRect.offsetTo(mFrameRect.left - offsetValue, mFrameRect.top - offsetValue);
        mScaleHandleDstRect.offsetTo(mFrameRect.right - offsetValue, mFrameRect.bottom - offsetValue);
        mRotateHandleDstRect.offsetTo(mFrameRect.right - offsetValue, mFrameRect.top - offsetValue);
        mFrontHandleDstRect.offsetTo(mFrameRect.left - offsetValue, mFrameRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mScaleHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mRotateHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mFrontHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        canvas.save();
        canvas.rotate(mRotateAngle, mFrameRect.centerX(), mFrameRect.centerY());
        canvas.drawRect(mFrameRect, mHelperFramePaint);
        canvas.restore();

        canvas.drawBitmap(mEditorFrame.getDeleteHandleBitmap(),
                mDeleteHandleSrcRect, mDeleteHandleDstRect, null);
        canvas.drawBitmap(mEditorFrame.getResizeHandleBitmap(),
                mScaleHandleSrcRect, mScaleHandleDstRect, null);
        canvas.drawBitmap(mEditorFrame.getRotateHandleBitmap(),
                mRotateHandleSrcRect, mRotateHandleDstRect, null);
        canvas.drawBitmap(mEditorFrame.getFrontHandleBitmap(),
                mFrontHandleSrcRect, mFrontHandleDstRect, null);
    }

    private void updateFrameRect() {
        mFrameRect.left -= EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.right += EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.top -= EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.bottom += EditorFrame.EDITOR_FRAME_PADDING;
    }

    @Override
    public void actionMove(float dx, float dy) {
        mX += dx;
        mY += dy;
    }

    @Override
    public void updateScale(float distanceX, float distanceY) {
        float frameCenterX = mFrameRect.centerX();
        float frameCenterY = mFrameRect.centerY();

        float handleCenterX = mScaleHandleDstRect.centerX();
        float handleCenterY = mScaleHandleDstRect.centerY();

        float newX = handleCenterX + distanceX;
        float newY = handleCenterY + distanceY;

        float xa = handleCenterX - frameCenterX;
        float ya = handleCenterY - frameCenterY;

        float xb = newX - frameCenterX;
        float yb = newY - frameCenterY;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;

        mScale *= scale;

        float newWidth = mFrameRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
        }
    }

    @Override
    public void updateRotate(float newX, float newY) {
        float frameCenterX = mFrameRect.centerX();
        float frameCenterY = mFrameRect.centerY();

        float handleCenterX = mRotateHandleDstRect.centerX();
        float handleCenterY = mRotateHandleDstRect.centerY();

        float xa = handleCenterX - frameCenterX;
        float ya = handleCenterY - frameCenterY;

        float xb = newX - frameCenterX;
        float yb = newY - frameCenterY;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        //cosθ = (a·b) / (|a|*|b|)
        float dot = xa * xb + ya * yb;
        double cos = dot / (srcLen * curLen);
        if (cos > 1 || cos < -1) return;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        //a x b 叉乘判断向量旋转方向
        float calMatrix = xa * yb - xb * ya;
        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    @Override
    public void setEditorTouched(boolean isTouched) {
        if (isTouched) {
            mHelperFramePaint.setAlpha(255);
        } else {
            mHelperFramePaint.set(mEditorFrame.getFramePaint());
        }
    }

    @Override
    public boolean isInside(MotionEvent event) {
        mTouchPoint.set((int) event.getX(), (int) event.getY());
        RectUtil.rotatePoint(mTouchPoint, mFrameRect.centerX(), mFrameRect.centerY(), -mRotateAngle);
        return mFrameRect.contains(mTouchPoint.x, mTouchPoint.y);
    }

    @Override
    public boolean isInDeleteHandleButton(MotionEvent event) {
        return mDeleteHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInEditHandleButton(MotionEvent event) {
        return mFrontHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInScaleHandleButton(MotionEvent event) {
        return mScaleHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInRotateHandleButton(MotionEvent event) {
        return mRotateHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public void setHelpFrameEnabled(boolean enabled) {
        mIsDrawHelperFrame = enabled;
    }
}