package com.cw.sticker.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cw.sticker.R;

public class EditTextDialog extends Dialog {

    private OnEditListener listener;
    private EditText etText;
    private String text;

    public EditTextDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @SuppressLint("PrivateResource")
    public EditTextDialog(Context context, String text) {
        this(context, R.style.Base_Theme_AppCompat_Dialog);
        this.text = text;
        init();
    }

    private void init() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        //设置dialog宽度充满屏幕
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        setContentView();
        this.show();
    }

    private void setContentView() {
        setContentView(R.layout.dialog_edit_text);
        final TextView tvText = findViewById(R.id.tvText);
        etText = findViewById(R.id.etText);
        TextView tvCommit = findViewById(R.id.tvCommit);
        showSoftInputFromWindow(etText);
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvText.setText(s.toString());
            }
        });
        tvCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditText(etText.getText().toString());
                    dismiss();
                }
            }
        });
        etText.setText(text);
        etText.setSelection(text.length());
    }

    public void showSoftInputFromWindow(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * 设置点击回调
     */
    public void setOnEditListener(OnEditListener listener) {
        this.listener = listener;
    }

    public interface OnEditListener {
        void onEditText(String value);
    }

}
