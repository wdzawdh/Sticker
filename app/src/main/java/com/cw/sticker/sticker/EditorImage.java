package com.cw.sticker.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

import static com.cw.sticker.sticker.EditorFrame.EDITOR_FRAME_PADDING;


public class EditorImage implements ISticker {
    private static final float MIN_SCALE = 0.15f;
    private static final int BUTTON_WIDTH = 30;

    private EditorFrame mEditorFrame;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private Paint mPaint;
    private Paint mHelperFramePaint;

    private RectF mClipRect;
    private RectF mDstRect;
    private RectF mFrameRect;

    private Rect mDeleteHandleSrcRect;
    private Rect mScaleAndRotateHandleSrcRect;

    private RectF mDeleteHandleDstRect;
    private RectF mResizeAndScaleHandleDstRect;

    private float mRotateAngle;
    private float mInitWidth;
    private int mColor = -1;
    private boolean mIsDrawHelperFrame = true;

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

        float left = mClipRect.centerX() - (stickerWidth / 2);
        float top = mClipRect.centerY() - (stickerHeight / 2);

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
        mScaleAndRotateHandleSrcRect = new Rect(0, 0, mEditorFrame.getResizeHandleBitmap().getWidth(),
                mEditorFrame.getResizeHandleBitmap().getHeight());

        int handleHalfSize = mEditorFrame.getDeleteHandleBitmap().getWidth() / 2;

        mDeleteHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mResizeAndScaleHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
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
        return mDstRect.width() / mInitWidth;
    }

    @Override
    public float getScale(Rect standardRect) {
        //有基准尺寸时，计算和原图的缩放比
        return mDstRect.width() / mClipRect.width() * standardRect.width() / mBitmap.getWidth();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        if (mIsDrawHelperFrame) drawHelperFrame(canvas);
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

        mResizeAndScaleHandleDstRect.offsetTo(
                mFrameRect.right - offsetValue,
                mFrameRect.bottom - offsetValue
        );

        RectUtil.rotateRect(mDeleteHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mResizeAndScaleHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        canvas.drawBitmap(mEditorFrame.getDeleteHandleBitmap(),
                mDeleteHandleSrcRect,
                mDeleteHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getResizeHandleBitmap(),
                mScaleAndRotateHandleSrcRect,
                mResizeAndScaleHandleDstRect,
                null);
    }

    private void updateFrameRect() {
        mFrameRect.left -= EDITOR_FRAME_PADDING;
        mFrameRect.right += EDITOR_FRAME_PADDING;
        mFrameRect.top -= EDITOR_FRAME_PADDING;
        mFrameRect.bottom += EDITOR_FRAME_PADDING;
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
    public void updateRotateAndScale(final float dx, final float dy) {
        float stickerCenterX = mDstRect.centerX();
        float stickerCenterY = mDstRect.centerY();

        float handleCenterX = mResizeAndScaleHandleDstRect.centerX();
        float handleCenterY = mResizeAndScaleHandleDstRect.centerY();

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

        mResizeAndScaleHandleDstRect.offsetTo(mFrameRect.right - BUTTON_WIDTH, mFrameRect.bottom
                - BUTTON_WIDTH);
        mDeleteHandleDstRect.offsetTo(mFrameRect.left - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH);

        //cosθ = (a·b) / (|a|*|b|)
        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1) return;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        //a x b 叉乘判断向量旋转方向
        float calMatrix = xa * yb - xb * ya;
        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
        mMatrix.postRotate(angle, mDstRect.centerX(), mDstRect.centerY());

        RectUtil.rotateRect(mResizeAndScaleHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mDeleteHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);

        Log.i("Sticker", "Scale = " + MatrixUtil.getScale(mMatrix) + "\n" +
                "Angle = " + MatrixUtil.getAngle(mMatrix));
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
        return mDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInDeleteHandleButton(MotionEvent event) {
        return mDeleteHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInResizeAndScaleHandleButton(MotionEvent event) {
        return mResizeAndScaleHandleDstRect.contains(event.getX(), event.getY());
    }

    @Override
    public boolean isInEditHandleButton(MotionEvent event) {
        return false;
    }

    @Override
    public void setHelpFrameEnabled(boolean enabled) {
        mIsDrawHelperFrame = enabled;
    }
}