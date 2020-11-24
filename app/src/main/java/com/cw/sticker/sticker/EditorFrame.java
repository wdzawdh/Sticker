package com.cw.sticker.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;

import com.cw.sticker.R;
import com.cw.sticker.utils.BitmapUtil;


public class EditorFrame {
    static final int EDITOR_FRAME_PADDING = 25;

    private Paint mFramePaint;

    private Bitmap mDeleteHandleBitmap;
    private Bitmap mResizeHandleBitmap;
    private Bitmap mRotateHandleBitmap;
    private Bitmap mFrontHandleBitmap;

    public EditorFrame(Context context) {
        initializeFramePaint();
        initializeHandlesBitmap(context);
    }

    private void initializeFramePaint() {
        mFramePaint = new Paint();
        mFramePaint.setColor(0xFF6194e3);
        mFramePaint.setAntiAlias(true);
        mFramePaint.setDither(true);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(5);
        mFramePaint.setAlpha(100);
        //mFramePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
    }

    private void initializeHandlesBitmap(Context context) {
        mDeleteHandleBitmap = BitmapUtil.drawable2Bitmap(context, R.mipmap.ic_handle_delete);
        mResizeHandleBitmap = BitmapUtil.drawable2Bitmap(context, R.mipmap.ic_handle_scale);
        mRotateHandleBitmap = BitmapUtil.drawable2Bitmap(context, R.mipmap.ic_handle_rotate);
        mFrontHandleBitmap = BitmapUtil.drawable2Bitmap(context, R.mipmap.ic_handle_front);
    }

    public Paint getFramePaint() {
        return mFramePaint;
    }

    public Bitmap getDeleteHandleBitmap() {
        return mDeleteHandleBitmap;
    }

    public Bitmap getResizeHandleBitmap() {
        return mResizeHandleBitmap;
    }

    public Bitmap getRotateHandleBitmap() {
        return mRotateHandleBitmap;
    }

    public Bitmap getFrontHandleBitmap() {
        return mFrontHandleBitmap;
    }
}
