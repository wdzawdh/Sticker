package com.cw.sticker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.cw.sticker.enums.EditorMode;
import com.cw.sticker.sticker.EditorImage;
import com.cw.sticker.sticker.EditorText;
import com.cw.sticker.sticker.ISticker;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class ImageEditorControl {

    private Context mContext;
    private IEditorView mViewState;
    private ISticker mTouchedSticker;
    private EditorMode mCurrentMode = EditorMode.NONE;

    private float mLastX;
    private float mLastY;
    private RectF mClipRect;
    private Bitmap mImageBitmap;
    private Matrix mImageMatrix = new Matrix();
    private Matrix mTransformMatrix = new Matrix();

    private List<ISticker> mStickerList = new ArrayList<>();

    ImageEditorControl(@NonNull Context context, final ImageEditorView viewState) {
        mContext = context;
        mViewState = viewState;
        initClipRect(viewState);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return stickerActionDown(event);
        }
        return false;
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                stickerActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                stickerActionUp();
                break;
        }
    }

    private void initClipRect(final ImageEditorView viewState) {
        viewState.post(new Runnable() {
            @Override
            public void run() {
                mClipRect = new RectF(0, 0, viewState.getWidth(), viewState.getHeight());
                getViewState().setClipRect(mClipRect);
            }
        });
    }

    void setImageBitmap(Bitmap bitmap, int width, int height) {
        if (mImageBitmap == null) {
            mImageBitmap = bitmap;
        }
        RectF viewRect = new RectF(0, 0, width, height);

        mImageMatrix.reset();
        mClipRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        mImageMatrix.setRectToRect(mClipRect, viewRect, Matrix.ScaleToFit.CENTER);
        mImageMatrix.mapRect(mClipRect);
        mTransformMatrix.set(mImageMatrix);

        getViewState().setClipRect(mClipRect);
        getViewState().setupImage(bitmap, mImageMatrix);
    }

    void setColor(int color) {
        if (mTouchedSticker != null) {
            mTouchedSticker.setColor(color);
        }
    }

    void addText(String text, int color) {
        if (mClipRect == null) return;
        EditorText editorText = new EditorText(mContext, text, color, mClipRect);
        mStickerList.add(editorText);
        if (mTouchedSticker != null) mTouchedSticker.setHelpFrameEnabled(false);
        mTouchedSticker = mStickerList.get(mStickerList.size() - 1);
        getViewState().onStickerAdded(mStickerList);
    }

    void addImage(Bitmap bitmap) {
        if (mClipRect == null) return;
        EditorImage editorImage = new EditorImage(mContext, bitmap, mClipRect);
        //editorImage.setBitmapAlias(alias);
        mStickerList.add(editorImage);
        if (mTouchedSticker != null) mTouchedSticker.setHelpFrameEnabled(false);
        mTouchedSticker = mStickerList.get(mStickerList.size() - 1);
        getViewState().onStickerAdded(mStickerList);
    }

    void addImage(Bitmap bitmap, int color) {
        if (mClipRect == null) return;
        EditorImage editorImage = new EditorImage(mContext, bitmap, color, mClipRect);
        //editorImage.setBitmapAlias(alias);
        mStickerList.add(editorImage);
        if (mTouchedSticker != null) mTouchedSticker.setHelpFrameEnabled(false);
        mTouchedSticker = mStickerList.get(mStickerList.size() - 1);
        getViewState().onStickerAdded(mStickerList);
    }

    void clearSelectState() {
        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            ISticker sticker = mStickerList.get(i);
            sticker.setHelpFrameEnabled(false);
            getViewState().updateView();
        }
        mTouchedSticker = null;
        mCurrentMode = EditorMode.NONE;
    }

    private boolean stickerActionDown(MotionEvent event) {
        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            final ISticker sticker = mStickerList.get(i);

            if (sticker.isInside(event)) {
                mTouchedSticker = sticker;
                mCurrentMode = EditorMode.MOVE;
                mTouchedSticker.setEditorTouched(true);
                mLastX = event.getX();
                mLastY = event.getY();
                //move to first
                mStickerList.add(mStickerList.remove(i));
                sticker.setHelpFrameEnabled(true);
                getViewState().updateView();
                return true;
            } else if (sticker.isInDeleteHandleButton(event)) {
                mTouchedSticker = null;
                mCurrentMode = EditorMode.NONE;
                mStickerList.remove(i);
                getViewState().updateView();
                return true;
            } else if (sticker.isInScaleHandleButton(event)) {
                mTouchedSticker = sticker;
                mCurrentMode = EditorMode.SCALE;
                mTouchedSticker.setEditorTouched(true);
                mLastX = event.getX();
                mLastY = event.getY();
                return true;
            } else if (sticker.isInRotateHandleButton(event)) {
                mTouchedSticker = sticker;
                mCurrentMode = EditorMode.ROTATE;
                mTouchedSticker.setEditorTouched(true);
                return true;
            } else if (sticker.isInEditHandleButton(event)) {
                mTouchedSticker = null;
                mCurrentMode = EditorMode.NONE;
                if (sticker instanceof EditorText) {
                    EditorText editorText = (EditorText) sticker;
                    showEditDialog(mContext, editorText);
                }
                return true;
            } else {
                sticker.setHelpFrameEnabled(false);
                getViewState().updateView();
            }
        }
        mTouchedSticker = null;
        mCurrentMode = EditorMode.NONE;
        return false;
    }

    private void stickerActionMove(MotionEvent event) {
        if (mTouchedSticker != null) {
            switch (mCurrentMode) {
                case MOVE:
                    mTouchedSticker.actionMove(getDeltaX(event), getDeltaY(event));
                    mLastX = event.getX();
                    mLastY = event.getY();
                    break;
                case SCALE:
                    mTouchedSticker.updateScale(getDeltaX(event), getDeltaY(event));
                    mLastX = event.getX();
                    mLastY = event.getY();
                    break;
                case ROTATE:
                    mTouchedSticker.updateRotate(event.getX(), event.getY());
                    break;
            }
            getViewState().updateView();
        }
    }

    private void stickerActionUp() {
        if (mTouchedSticker != null) {
            mTouchedSticker.setEditorTouched(false);
            getViewState().updateView();
        }
    }

    private IEditorView getViewState() {
        return mViewState;
    }

    private float getDeltaX(MotionEvent event) {
        return event.getX() - mLastX;
    }

    private float getDeltaY(MotionEvent event) {
        return event.getY() - mLastY;
    }

    private void showEditDialog(Context context, final EditorText editorText) {
        EditTextDialog editTextDialog = new EditTextDialog(context, editorText.getText());
        editTextDialog.setOnEditListener(new EditTextDialog.OnEditListener() {
            @Override
            public void onEditText(String value) {
                if (!TextUtils.isEmpty(value)) {
                    editorText.setText(value);
                }
            }
        });
    }
}