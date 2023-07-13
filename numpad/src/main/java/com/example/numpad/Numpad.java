package com.example.numpad;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioMetadata;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

import androidx.core.content.ContextCompat;

import java.security.Key;

//Custom numpad view that can be attached to an EditText.
public class Numpad extends GridLayout implements View.OnClickListener {

    private EditText targetEditText;
    private NumpadListener numpadListener;
    private InputMethodManager inputMethodManager;
    private EditText activeEditText;

    private Handler backspaceHandler = new Handler();
    private boolean backspaceLongPressed = false;

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

    //Initialize the numpad by setting up the input method manager and button listeners.


    private void init(Context context) {
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setupButtonListeners();
    }

     // Set up the button listeners for the numpad.


    private void setupButtonListeners() {
        getContext().getTheme().applyStyle(R.style.MyLibTheme, true);
        LayoutInflater.from(getContext()).inflate(R.layout.keypad_layout, this, true);

        for (int buttonId : getButtonIds()) {
            Button key = findViewById(buttonId);
            key.setOnClickListener(this);
            key.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (v.getId() == R.id.button_backspace) {
                        backspaceLongPressed = true;
                        backspaceHandler.postDelayed(backspaceRunnable, 300);
                        return true;
                    }
                    return false;
                }
            });
            key.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.getId() == R.id.button_backspace && event.getAction() == MotionEvent.ACTION_UP) {
                        backspaceLongPressed = false;
                        backspaceHandler.removeCallbacks(backspaceRunnable);
                    }
                    return false;
                }
            });
        }
    }

     //Get the array of button IDs.
     //@return An array of button IDs.

    private int[] getButtonIds() {
        return new int[]{R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_backspace, R.id.button_submit};
    }

     // Set the color of the numpad buttons.


    public void setButtonColor(Integer textColor, Integer backgroundColor) {
        for (int buttonId : getButtonIds()) {
            Button key = findViewById(buttonId);
            key.setTextColor(textColor);
            key.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        }
    }


    //Set the target EditText for the numpad.
    public void setTargetEditText(EditText editText) {
        targetEditText = editText;
        targetEditText.setInputType(InputType.TYPE_NULL);
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setVisibility(View.VISIBLE);
                setActiveEditText(editText);
                Log.d("Numpad", "Numpad setTargetEditText - MotionEvent.ACTION_UP");
                return true;
            }
            return false;
        });
        View rootView = editText.getRootView();
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setVisibility(View.GONE);
            }
            return false;
        });
    }

     //Set the active EditText.
    private void setActiveEditText(EditText editText) {
        activeEditText = editText;
        editText.requestFocus();
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        if (editText.getText() != null) {
            String text = editText.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                int selection = editText.getSelectionStart();
                editText.setSelection(selection);

            } else {
                editText.setSelection(0);
                editText.setPadding(30, editText.getPaddingTop(), editText.getPaddingRight(), editText.getPaddingBottom());

            }
        }

    }
    private Runnable backspaceRunnable = new Runnable() {
        @Override
        public void run() {
            if (activeEditText != null && backspaceLongPressed) {
                Editable editable = activeEditText.getText();
                int selectionStart = activeEditText.getSelectionStart();
                int selectionEnd = activeEditText.getSelectionEnd();

                if (selectionStart > 0) {
                    if (selectionStart == selectionEnd) {
                        editable.delete(selectionStart - 1, selectionStart);
                        activeEditText.setSelection(selectionStart - 1);
                    } else {
                        editable.delete(selectionStart, selectionEnd);
                        activeEditText.setSelection(selectionStart);
                    }
                    backspaceHandler.postDelayed(this, 100);
                }
            }
        }
    };

    public void setNumpadListener(NumpadListener listener) {
        numpadListener = listener;
    }

     //Handle button clicks on the numpad.

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

        Editable editable = activeEditText.getText();
        int selectionStart = activeEditText.getSelectionStart();
        int selectionEnd = activeEditText.getSelectionEnd();

        if (view.getId() == R.id.button_backspace) {
            if (selectionStart > 0) {
                if (selectionStart == selectionEnd) {
                    editable.delete(selectionStart - 1, selectionStart);
                    activeEditText.setSelection(selectionStart - 1);
                } else {
                    editable.delete(selectionStart, selectionEnd);
                    activeEditText.setSelection(selectionStart);
                }
            }
        } else if (view.getId() == R.id.button_submit) {
            String enteredValue = activeEditText.getText().toString();
            if (numpadListener != null) {
                numpadListener.onNumpadSubmit(enteredValue);
            }
            activeEditText.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(activeEditText.getWindowToken(), 0);
        } else {
            Button button = (Button) view;
            String pressedKey = button.getText().toString();

            if (selectionStart == selectionEnd) {
                ((Editable) editable).insert(selectionStart, pressedKey);
                activeEditText.setSelection(selectionStart + 1);
            } else {
                editable.replace(selectionStart, selectionEnd, pressedKey);
                activeEditText.setSelection(selectionStart + pressedKey.length());
            }
        }
    }
}
