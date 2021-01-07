package com.cw.sticker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cw.sticker.sticker.ISticker;

import java.util.List;

import androidx.annotation.NonNull;


public class ImageEditorView extends View implements IEditorView {

    private final ImageEditorControl mControl;
    private Bitmap mImageBitmap;
    private Bitmap mAlteredImageBitmap;
    private Matrix mImageMatrix;
    private RectF mClipRect;
    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint();
    private final PathEffect mBorderEffects = new DashPathEffect(new float[]{10, 10}, 0);

    private List<ISticker> mStickerList;
    private boolean mApply = true;
    private boolean mClipBorderEnable = true;
    private boolean mIsOriginalImageDisplayed;

    public ImageEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mControl = new ImageEditorControl(getContext(), this);
        initBorderPaint();
    }

    private void initBorderPaint() {
        mBorderPaint.setColor(0xFFec6841);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(5);
        mBorderPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClipRect != null) {
            canvas.clipRect(mClipRect);
            if (mApply&&mClipBorderEnable) {
                mBorderPaint.setPathEffect(mBorderEffects);
                canvas.drawRoundRect(mClipRect, 0, 0, mBorderPaint);
            }
        }
        if (mIsOriginalImageDisplayed) {
            if (mImageBitmap != null && mImageMatrix != null) {
                canvas.drawBitmap(mImageBitmap, mImageMatrix, mBitmapPaint);
            }
        } else {
            Bitmap bitmap = getAlteredImageBitmap();
            if (bitmap != null && mImageMatrix != null) {
                canvas.drawBitmap(bitmap, mImageMatrix, mBitmapPaint);
            }
            drawSticker(canvas);
        }
    }

    @Override
    public void setupImage(Bitmap bitmap, Matrix imageMatrix) {
        if (mImageBitmap == null) {
            mImageBitmap = bitmap;
        } else {
            mAlteredImageBitmap = bitmap;
        }
        mImageMatrix = imageMatrix;
        invalidate();
    }

    @Override
    public void setClipRect(RectF clipRect) {
        mClipRect = clipRect;
        invalidate();
    }

    @Override
    public void onStickerAdded(List<ISticker> stickers) {
        mStickerList = stickers;
        invalidate();
    }

    @Override
    public void updateView() {
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mApply && mControl.dispatchTouchEvent(event)) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mApply) {
            mControl.onTouchEvent(event);
        }
        return true;
    }

    public void setClipBorderEnable(boolean enable) {
        this.mClipBorderEnable = enable;
    }

    public void switchOriginal() {
        mIsOriginalImageDisplayed = !mIsOriginalImageDisplayed;
        invalidate();
    }

    public void onApplyChanges() {
        mApply = false;
        mControl.clearSelectState();
        invalidate();
    }

    public void setImageBitmap(@NonNull Bitmap bitmap) {
        mImageBitmap = bitmap;
        mControl.setImageBitmap(bitmap, getWidth(), getHeight());
    }

    public void setColor(int color) {
        mControl.setColor(color);
        invalidate();
    }

    public Bitmap getAlteredImageBitmap() {
        if (mAlteredImageBitmap != null) {
            return mAlteredImageBitmap;
        }
        return mImageBitmap;
    }

    public void addText(String text) {
        mControl.addText(text, Color.BLACK);
    }

    public void addText(String text, int color) {
        mControl.addText(text, color);
    }

    public void addImage(Bitmap bitmap) {
        mControl.addImage(bitmap);
    }

    public void addImage(Bitmap bitmap, int color) {
        mControl.addImage(bitmap, color);
    }

    public void clearSelectState() {
        mControl.clearSelectState();
    }

    public List<ISticker> getSticks() {
        return mStickerList;
    }

    public RectF getClipRect(){
        return mClipRect;
    }

    private void drawSticker(Canvas canvas) {
        if (mStickerList != null) {
            for (ISticker text : mStickerList) {
                text.draw(canvas);
            }
        }
    }
}