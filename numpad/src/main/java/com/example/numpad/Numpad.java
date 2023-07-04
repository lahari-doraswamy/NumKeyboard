package com.example.numpad;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

import com.example.numpad.NumpadListener;

import java.util.ArrayList;
import java.util.List;

public class Numpad extends GridLayout implements View.OnClickListener {

    private List<EditText> targetEditTexts;
    private EditText activeEditText;
    private NumpadListener numpadListener;
    private InputMethodManager inputMethodManager;
    private GestureDetector gestureDetector;

    public Numpad(Context context) {
        super(context);
        init(context);
    }

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Numpad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        gestureDetector = new GestureDetector(context, new NumpadGestureListener());
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        getContext().getTheme().applyStyle(R.style.MyLibTheme, true);
        LayoutInflater.from(getContext()).inflate(R.layout.keypad_layout, this, true);

        for (int buttonId : getButtonIds()) {
            Button key = findViewById(buttonId);
            key.setOnClickListener(this);
        }
    }

    private int[] getButtonIds() {
        return new int[]{R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_backspace, R.id.button_submit};
    }

    public void setButtonColor(Integer textColor, Integer backgroundColor) {
        for (int buttonId : getButtonIds()) {
            Button key = findViewById(buttonId);
            key.setTextColor(textColor);
            key.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        }
    }

    public void addTargetEditText(EditText editText) {
        if (targetEditTexts == null) {
            targetEditTexts = new ArrayList<>();
        }
        targetEditTexts.add(editText);
        editText.setInputType(InputType.TYPE_NULL);
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setVisibility(View.VISIBLE);
                Log.d("Numpad", "Numpad setTargetEditText - MotionEvent.ACTION_UP");
                setActiveEditText(editText);
                return true;
            }
            return false;
        });

        View rootView = editText.getRootView();
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setVisibility(View.GONE);
                setActiveEditText(null);
            }
            return false;
        });
    }

    private void setActiveEditText(EditText editText) {
        activeEditText = editText;
    }

    public void setNumpadListener(NumpadListener listener) {
        numpadListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (activeEditText == null) {
            return;
        }

        view.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        );
        Log.d("Numpad", "Haptic feedback triggered");

        if (view.getId() == R.id.button_backspace) {
            String currentText = activeEditText.getText().toString();
            if (!TextUtils.isEmpty(currentText)) {
                String updatedText = currentText.substring(0, currentText.length() - 1);
                activeEditText.setText(updatedText);
            }
        } else if (view.getId() == R.id.button_submit) {
            String enteredValue = activeEditText.getText().toString();
            if (numpadListener != null) {
                numpadListener.onNumpadSubmit(enteredValue);
            }
            activeEditText.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(activeEditText.getWindowToken(), 0);
            setActiveEditText(null); // Clear the active EditText
        } else {
            Button button = (Button) view;
            String currentText = activeEditText.getText().toString();
            String pressedKey = button.getText().toString();
            String updatedText = currentText + pressedKey;
            activeEditText.setText(updatedText);
        }
    }

    private class NumpadGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int touchX = (int) e.getRawX();
            int touchY = (int) e.getRawY();
            Rect numPadRect = new Rect();
            getGlobalVisibleRect(numPadRect);

            if (!numPadRect.contains(touchX, touchY)) {
                setVisibility(View.GONE);
                if (activeEditText != null) {
                    inputMethodManager.hideSoftInputFromWindow(activeEditText.getWindowToken(), 0);
                    setActiveEditText(null);
                }
                Log.d("Numpad", "Numpad closed");
            }

            return true;
        }
    }
}
