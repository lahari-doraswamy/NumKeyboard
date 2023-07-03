package com.example.numpad;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

public class Numpad  extends GridLayout implements View.OnClickListener {

    private EditText targetEditText;
    private Numpad numPad;

    public Numpad(Context context) {
        super(context);
        numPad = this;
        setupButtonListeners();
    }

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        numPad = this;
        setupButtonListeners();
    }

    public Numpad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        numPad = this;
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_main, this, true);
        int[] buttonIds = new int[]{R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_backspace, R.id.button_submit};

        for (int buttonId : buttonIds) {
            Button key = findViewById(buttonId);
            key.setOnClickListener(this);
        }

    }

    public void setTargetEditText(EditText editText) {
        targetEditText = editText;
        targetEditText.setInputType(InputType.TYPE_NULL);
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                numPad.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onClick(View view) {
        if (targetEditText == null) {
            return;
        }
        if (view.getId() == R.id.button_backspace) {
            String currentText = targetEditText.getText().toString();
            if (!TextUtils.isEmpty(currentText)) {
                String updatedText = currentText.substring(0, currentText.length() - 1);
                targetEditText.setText(updatedText);
            }
        } else if (view.getId() == R.id.button_submit) {
            targetEditText.clearFocus();
        } else {
            Button button = (Button) view;
            String currentText = targetEditText.getText().toString();
            String pressedKey = button.getText().toString();
            String updatedText = currentText + pressedKey;
            targetEditText.setText(updatedText);
        }
    }

}
