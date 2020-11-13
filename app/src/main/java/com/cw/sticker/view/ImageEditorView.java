package com.cw.sticker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    private ImageEditorControl mControl;
    private Bitmap mImageBitmap;
    private Bitmap mAlteredImageBitmap;
    private Matrix mImageMatrix;
    private RectF mClipRect;
    private Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBorderPaint = new Paint();
    private PathEffect mBorderEffects = new DashPathEffect(new float[]{10, 10}, 0);

    private List<ISticker> mStickerList;

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
            mBorderPaint.setPathEffect(mBorderEffects);
            canvas.drawRoundRect(mClipRect, 0, 0, mBorderPaint);
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
    public void showOriginalImage(boolean display) {
        mIsOriginalImageDisplayed = display;
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
    public void onApplyChanges() {
        invalidate();
        setDrawingCacheEnabled(true);
        mControl.applyChanges(getDrawingCache());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mControl.viewTouched(event);
        return true;
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

    public void addText(String text, int color) {
        mControl.addText(text, color);
    }

    public void addImage(Bitmap bitmap) {
        mControl.addImage(bitmap);
    }

    public void clearSelectState() {
        mControl.clearSelectState();
    }

    private void drawSticker(Canvas canvas) {
        if (mStickerList != null) {
            for (ISticker text : mStickerList) {
                text.draw(canvas);
            }
        }
    }
}