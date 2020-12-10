package com.cw.sticker.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.cw.sticker.utils.BitmapUtil;
import com.cw.sticker.utils.MatrixUtil;
import com.cw.sticker.utils.RectUtil;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;


public class EditorImage implements ISticker {
    private static final float MIN_SCALE = 0.15f;
    private static final int BUTTON_WIDTH = 30;

    private final EditorFrame mEditorFrame;
    private final Point mTouchPoint = new Point();
    private final Paint mPaint;
    private final Paint mHelperFramePaint;
    private final RectF mClipRect;

    private RectF mDstRect;
    private RectF mFrameRect;
    private Rect mStandardRect;

    private Rect mDeleteHandleSrcRect;
    private Rect mScaleHandleSrcRect;
    private Rect mRotateHandleSrcRect;

    private RectF mDeleteHandleDstRect;
    private RectF mScaleHandleDstRect;
    private RectF mRotateHandleDstRect;

    private Bitmap mBitmap;
    private Matrix mMatrix;
    private float mRotateAngle;
    private float mInitWidth;
    private int mColor = -1;
    private boolean mIsDrawHelperFrame = true;
    private String mAlias;

    public EditorImage(Context context, Bitmap bitmap, RectF clipRect) {
        mEditorFrame = new EditorFrame(context);
        mBitmap = bitmap;
        mClipRect = clipRect;
        mPaint = new Paint();
        mHelperFramePaint = new Paint(mEditorFrame.getFramePaint());
        initialize();
    }

    public EditorImage(Context context, Bitmap bitmap, @ColorInt int color, RectF clipRect) {
        this(context, BitmapUtil.makeTintBitmap(bitmap, color), clipRect);
        mColor = color;
    }

    private void initialize() {
        //根据背景RectF设置大小
        int stickerWidth = Math.min(mBitmap.getWidth(), (int) mClipRect.width() >> 1);
        int stickerHeight = stickerWidth * mBitmap.getHeight() / mBitmap.getWidth();

        float left = mClipRect.centerX() - (stickerWidth >> 1);
        float top = mClipRect.centerY() - (stickerHeight >> 1);

        mDstRect = new RectF(left, top, left + stickerWidth, top + stickerHeight);

        mMatrix = new Matrix();
        mMatrix.postTranslate(mDstRect.left, mDstRect.top);
        mMatrix.postScale(
                (float) stickerWidth / mBitmap.getWidth(),
                (float) stickerHeight / mBitmap.getHeight(),
                mDstRect.left, mDstRect.top
        );

        mInitWidth = mDstRect.width();

        mFrameRect = new RectF(mDstRect);
        updateFrameRect();

        mDeleteHandleSrcRect = new Rect(0, 0, mEditorFrame.getDeleteHandleBitmap().getWidth(),
                mEditorFrame.getDeleteHandleBitmap().getHeight());
        mScaleHandleSrcRect = new Rect(0, 0, mEditorFrame.getResizeHandleBitmap().getWidth(),
                mEditorFrame.getResizeHandleBitmap().getHeight());
        mRotateHandleSrcRect = new Rect(0, 0, mEditorFrame.getRotateHandleBitmap().getWidth(),
                mEditorFrame.getRotateHandleBitmap().getHeight());

        int handleHalfSize = mEditorFrame.getDeleteHandleBitmap().getWidth() >> 1;

        mDeleteHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mScaleHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mRotateHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
    }

    @Override
    public void setStandardRect(Rect standardRect) {
        mStandardRect = standardRect;
    }

    @Override
    public void setColor(int color) {
        mColor = color;
        mBitmap = BitmapUtil.makeTintBitmap(mBitmap, color);
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public float getRotate() {
        return mRotateAngle;
    }

    @Override
    public PointF getPosition() {
        return new PointF(mDstRect.centerX(), mDstRect.centerY());
    }

    @Override
    public float getScale() {
        if (mStandardRect != null) {
            return mDstRect.width() / mClipRect.width() * mStandardRect.width() / mBitmap.getWidth();
        }
        return mDstRect.width() / mInitWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        if (mIsDrawHelperFrame) drawHelperFrame(canvas);
    }

    public String getBitmapAlias() {
        return mAlias;
    }

    public void setBitmapAlias(String alias) {
        this.mAlias = alias;
    }

    private void drawHelperFrame(Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotateAngle, mFrameRect.centerX(), mFrameRect.centerY());
        canvas.drawRect(mFrameRect, mHelperFramePaint);
        canvas.restore();

        int offsetValue = ((int) mDeleteHandleDstRect.width()) >> 1;

        mDeleteHandleDstRect.offsetTo(
                mFrameRect.left - offsetValue,
                mFrameRect.top - offsetValue
        );

        mScaleHandleDstRect.offsetTo(
                mFrameRect.right - offsetValue,
                mFrameRect.bottom - offsetValue
        );

        mRotateHandleDstRect.offsetTo(
                mFrameRect.right - offsetValue,
                mFrameRect.top - offsetValue
        );

        RectUtil.rotateRect(mDeleteHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mScaleHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mRotateHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        canvas.drawBitmap(mEditorFrame.getDeleteHandleBitmap(),
                mDeleteHandleSrcRect,
                mDeleteHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getResizeHandleBitmap(),
                mScaleHandleSrcRect,
                mScaleHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getRotateHandleBitmap(),
                mRotateHandleSrcRect,
                mRotateHandleDstRect,
                null);
    }

    private void updateFrameRect() {
        mFrameRect.left -= EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.right += EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.top -= EditorFrame.EDITOR_FRAME_PADDING;
        mFrameRect.bottom += EditorFrame.EDITOR_FRAME_PADDING;
    }

    @Override
    public void actionMove(float dx, float dy) {
        mMatrix.postTranslate(dx, dy);

        mDstRect.offset(dx, dy);

        mFrameRect.offset(dx, dy);

        Log.i("Sticker", "Move: " + "\n" + "X = " + MatrixUtil.getMatrixX(mMatrix) +
                "\n" + "Y = " + MatrixUtil.getMatrixY(mMatrix));
    }

    @Override
    public void updateScale(final float dx, final float dy) {
        float stickerCenterX = mDstRect.centerX();
        float stickerCenterY = mDstRect.centerY();

        float handleCenterX = mScaleHandleDstRect.centerX();
        float handleCenterY = mScaleHandleDstRect.centerY();

        float n_x = handleCenterX + dx;
        float n_y = handleCenterY + dy;

        float xa = handleCenterX - stickerCenterX;
        float ya = handleCenterY - stickerCenterY;

        float xb = n_x - stickerCenterX;
        float yb = n_y - stickerCenterY;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;

        float newWidth = mDstRect.width() * scale;
        if (newWidth / mInitWidth < MIN_SCALE) {
            return;
        }

        mMatrix.postScale(scale, scale, mDstRect.centerX(),
                mDstRect.centerY());

        RectUtil.scaleRect(mDstRect, scale);

        mFrameRect.set(mDstRect);
        updateFrameRect();

        mDeleteHandleDstRect.offsetTo(mFrameRect.left - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH);
        mScaleHandleDstRect.offsetTo(mFrameRect.right - BUTTON_WIDTH, mFrameRect.bottom
                - BUTTON_WIDTH);
        mRotateHandleDstRect.offsetTo(mFrameRect.right - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH);
    }

    @Override
    public void updateRotate(float newX, float newY) {
        float stickerCenterX = mDstRect.centerX();
        float stickerCenterY = mDstRect.centerY();

        float handleCenterX = mRotateHandleDstRect.centerX();
        float handleCenterY = mRotateHandleDstRect.centerY();

        float xa = handleCenterX - stickerCenterX;
        float ya = handleCenterY - stickerCenterY;

        float xb = newX - stickerCenterX;
        float yb = newY - stickerCenterY;

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
        mMatrix.postRotate(angle, mDstRect.centerX(), mDstRect.centerY());

        RectUtil.rotateRect(mDeleteHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mScaleHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);
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
    public boolean isInScaleHandleButton(MotionEvent event) {
        return mScaleHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInRotateHandleButton(MotionEvent event) {
        return mRotateHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInEditHandleButton(MotionEvent event) {
        return false;
    }

    @Override
    public void setHelpFrameEnabled(boolean enabled) {
        mIsDrawHelperFrame = enabled;
    }

    @Override
    public boolean getHelpFrameEnabled() {
        return mIsDrawHelperFrame;
    }
}