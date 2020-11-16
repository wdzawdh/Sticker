package com.cw.sticker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cw.sticker.model.CompoundModel;
import com.cw.sticker.sticker.EditorImage;
import com.cw.sticker.sticker.EditorText;
import com.cw.sticker.sticker.ISticker;
import com.cw.sticker.utils.BitmapUtil;
import com.cw.sticker.view.EditTextDialog;
import com.cw.sticker.view.ImageEditorView;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int PERMISSION_CODE = 10001;
    private static final int OPEN_ALBUM_CODE = 20001;

    private ImageEditorView imageEditor;
    private int currentColor = 0xFF181818;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ConstraintLayout clEditor = findViewById(R.id.clEditor);
        imageEditor = findViewById(R.id.imageEditor);
        findViewById(R.id.btSelectBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_CODE);
                        return;
                    }
                }
                openAlbum();
            }
        });
        findViewById(R.id.btSelectImageStick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapUtil.drawable2Bitmap(MainActivity.this, R.drawable.ic_heart);
                imageEditor.addImage(bitmap, currentColor);
            }
        });
        findViewById(R.id.btSelectTextStick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTextDialog editTextDialog = new EditTextDialog(MainActivity.this, "");
                editTextDialog.setOnEditListener(new EditTextDialog.OnEditListener() {
                    @Override
                    public void onEditText(String value) {
                        imageEditor.addText(value, currentColor);
                    }
                });
            }
        });
        findViewById(R.id.ivBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageEditor.clearSelectState();
            }
        });
        findViewById(R.id.btSwitchOriginal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageEditor.switchOriginal();
            }
        });
        findViewById(R.id.btSaveStick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageEditor.onApplyChanges();
                clEditor.setDrawingCacheEnabled(true);
                Bitmap bitmap = clEditor.getDrawingCache();
                String path = BitmapUtil.saveBitmap(MainActivity.this, bitmap, "test");
                if (new File(path).exists()) {
                    Toast.makeText(MainActivity.this, "save success  " + path, Toast.LENGTH_LONG).show();
                }
                clEditor.setDrawingCacheEnabled(false);

                //export json
                CompoundModel compoundModel = new CompoundModel();
                compoundModel.items = new ArrayList<>();
                RectF clipRect = imageEditor.getClipRect();
                List<ISticker> sticks = imageEditor.getSticks();
                for (ISticker stick : sticks) {
                    CompoundModel.ItemsBean itemsBean = new CompoundModel.ItemsBean();
                    float rotate = stick.getRotate();
                    int color = stick.getColor();
                    String hexColor = String.format("%06X", (0xFFFFFF & color));
                    PointF position = stick.getPosition();
                    float x = position.x / clipRect.width() * 560;
                    float y = position.y / clipRect.width() * 560;
                    itemsBean.tint = Integer.parseInt(hexColor, 16);
                    itemsBean.anchor = new CompoundModel.ItemsBean.AnchorBean(0.5f, 0.5f);
                    itemsBean.position = new CompoundModel.ItemsBean.PositionBean(x, y);
                    itemsBean.rotation = Math.toRadians(rotate);
                    itemsBean.scale = stick.getScale();
                    itemsBean.fillType = color != -1 ? 1 : 2; //是否填充颜色
                    if (stick instanceof EditorImage) {
                        itemsBean.type = 1;
                        itemsBean.texture = "ic_heart";
                    }
                    if (stick instanceof EditorText) {
                        EditorText editorText = (EditorText) stick;
                        itemsBean.type = 3;
                        itemsBean.text = editorText.getText();
                    }
                    compoundModel.items.add(itemsBean);
                }
                String json = new Gson().toJson(compoundModel);
                Log.d("Export json  ", json);
            }
        });

        setColorClickListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_CODE && resultCode == RESULT_OK) {
            openAlbum();
        }
        if (requestCode == OPEN_ALBUM_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Bitmap bitmap = BitmapUtil.getBitmapFromUri(this, uri);
            if (bitmap != null) {
                imageEditor.setImageBitmap(bitmap);
            }
        }
    }

    private void setColorClickListener() {
        RadioGroup rgColor = findViewById(R.id.rgColor);
        rgColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbColor1:
                        currentColor = 0xFF181818;
                        break;
                    case R.id.rbColor2:
                        currentColor = 0xFFffffff;
                        break;
                    case R.id.rbColor3:
                        currentColor = 0xFFa5a5a5;
                        break;
                    case R.id.rbColor4:
                        currentColor = 0xFFfdafaf;
                        break;
                    case R.id.rbColor5:
                        currentColor = 0xFF22379d;
                        break;
                    case R.id.rbColor6:
                        currentColor = 0xFFf9ba12;
                        break;
                    case R.id.rbColor7:
                        currentColor = 0xFFd4b4f9;
                        break;
                }
                imageEditor.setColor(currentColor);
            }
        });
    }

    private void openAlbum() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);  传统打开相册
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, OPEN_ALBUM_CODE);
    }
}