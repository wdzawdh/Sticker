package com.cw.sticker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.cw.sticker.utils.BitmapUtil;
import com.cw.sticker.view.EditTextDialog;
import com.cw.sticker.view.ImageEditorView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
                imageEditor.addImage(BitmapUtil.makeTintBitmap(bitmap, currentColor));
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