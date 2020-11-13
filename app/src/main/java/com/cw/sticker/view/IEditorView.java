package com.cw.sticker.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.cw.sticker.sticker.ISticker;

import java.util.List;


interface IEditorView {
    void setupImage(Bitmap bitmap, Matrix imageMatrix);

    void setClipRect(RectF clipRect);

    void onStickerAdded(List<ISticker> texts);

    void updateView();
}