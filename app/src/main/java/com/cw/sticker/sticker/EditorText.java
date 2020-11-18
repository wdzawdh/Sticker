package com.cw.sticker.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

import static com.cw.sticker.sticker.EditorFrame.EDITOR_FRAME_PADDING;


public class EditorText implements ISticker {
    private static final float DEFAULT_TEXT_SIZE = 32;

    private EditorFrame mEditorFrame;
    private Paint mHelperFramePaint;
    private TextPaint mTextPaint;

    private RectF mClipRect;
    private Rect mTextRect;
    private RectF mFrameRect;

    private Rect mDeleteHandleSrcRect;
    private Rect mFrontHandleSrcRect;
    private Rect mResizeAndScaleHandleSrcRect;

    private RectF mDeleteHandleDstRect;
    private RectF mFrontHandleDstRect;
    private RectF mResizeAndScaleHandleDstRect;

    private String mText;
    private int mColor = Color.BLACK;
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
        mResizeAndScaleHandleSrcRect = new Rect(0, 0, mEditorFrame.getResizeHandleBitmap().getWidth(),
                mEditorFrame.getResizeHandleBitmap().getHeight());
        mFrontHandleSrcRect = new Rect(0, 0, mEditorFrame.getFrontHandleBitmap().getWidth(),
                mEditorFrame.getFrontHandleBitmap().getHeight());

        int handleHalfSize = mEditorFrame.getDeleteHandleBitmap().getWidth() / 2;

        mDeleteHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mResizeAndScaleHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
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
    public float getScale(Rect standardRect) {
        //有基准尺寸时，计算和默认字体大小的缩放比
        Rect textRect = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), textRect);
        return mTextRect.width() / mClipRect.width() * standardRect.width() / textRect.width();
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
        mResizeAndScaleHandleDstRect.offsetTo(mFrameRect.right - offsetValue, mFrameRect.bottom - offsetValue);
        mFrontHandleDstRect.offsetTo(mFrameRect.left - offsetValue, mFrameRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mResizeAndScaleHandleDstRect, mFrameRect.centerX(),
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
                mResizeAndScaleHandleSrcRect, mResizeAndScaleHandleDstRect, null);
        canvas.drawBitmap(mEditorFrame.getFrontHandleBitmap(),
                mFrontHandleSrcRect, mFrontHandleDstRect, null);
    }

    private void updateFrameRect() {
        mFrameRect.left -= EDITOR_FRAME_PADDING;
        mFrameRect.right += EDITOR_FRAME_PADDING;
        mFrameRect.top -= EDITOR_FRAME_PADDING;
        mFrameRect.bottom += EDITOR_FRAME_PADDING;
    }

    @Override
    public void actionMove(float dx, float dy) {
        mX += dx;
        mY += dy;
    }

    @Override
    public void updateRotateAndScale(float distanceX, float distanceY) {
        float frameCenterX = mFrameRect.centerX();
        float frameCenterY = mFrameRect.centerY();

        float handleCenterX = mResizeAndScaleHandleDstRect.centerX();
        float handleCenterY = mResizeAndScaleHandleDstRect.centerY();

        float newX = handleCenterX + distanceX;
        float newY = handleCenterY + distanceY;

        float xa = handleCenterX - frameCenterX;
        float ya = handleCenterY - frameCenterY;

        float xb = newX - frameCenterX;
        float yb = newY - frameCenterY;

        float sourceLength = (float) Math.sqrt(Math.pow(xa, 2) + Math.pow(ya, 2));
        float currentLength = (float) Math.sqrt(Math.pow(xb, 2) + Math.pow(yb, 2));

        float scale = currentLength / sourceLength;

        mScale *= scale;

        float newWidth = mFrameRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        //cosθ = (a·b) / (|a|*|b|)
        double cos = (xa * xb + ya * yb) / (sourceLength * currentLength);
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

    private Point mTouchPoint = new Point();

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
    public boolean isInResizeAndScaleHandleButton(MotionEvent event) {
        return mResizeAndScaleHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public void setHelpFrameEnabled(boolean enabled) {
        mIsDrawHelperFrame = enabled;
    }
}