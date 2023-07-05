package com.example.numpad;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.FrameLayout;
import android.widget.GridLayout;

import com.example.numpad.NumpadListener;

public class Numpad extends GridLayout implements View.OnClickListener {

    private EditText targetEditText;
    private NumpadListener numpadListener;
    private InputMethodManager inputMethodManager;
    private EditText activeEditText;
    private View cursorView;
    private boolean cursorVisible = false;
    private Handler cursorHandler = new Handler();
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

    private void init(Context context) {
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setupButtonListeners();
        setupCursorView(context);
    }

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
                hideCursor();
            }
            return false;
        });
    }

    private void setActiveEditText(EditText editText) {
        activeEditText = editText;
        editText.requestFocus();

        if (editText.getText() != null) {
            String text = editText.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                int selection = editText.getSelectionStart();
                editText.setSelection(selection);
                showCursor();
            } else {
                editText.setSelection(0); // Set the cursor at the beginning of the text
                editText.setPadding(30, editText.getPaddingTop(), editText.getPaddingRight(), editText.getPaddingBottom()); // Add left padding
                showCursor();
            }
        }

        editText.setCursorVisible(true); // Show the cursor

        // Register a TextWatcher to detect text changes
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Ensure the cursor stays at the end of the text
                editText.setSelection(s.length());
            }
        });
    }


    private void setupCursorView(Context context) {
        cursorView = new View(context);
        int cursorColor = getResources().getColor(R.color.black);
        cursorView.setBackgroundColor(cursorColor);
        int cursorWidth = 1; // Change the width here
        int cursorHeight = 40; // Change the height here
        cursorView.setLayoutParams(new FrameLayout.LayoutParams(cursorWidth, cursorHeight));
    }

    private void showCursor() {
        if (activeEditText != null && !cursorVisible) {
            cursorVisible = true;
            FrameLayout rootView = activeEditText.getRootView().findViewById(android.R.id.content);
            rootView.addView(cursorView);
            updateCursorPosition();
            cursorHandler.postDelayed(cursorRunnable, 500);
            cursorView.setVisibility(View.VISIBLE); // Make the cursor view visible
            cursorView.removeCallbacks(cursorBlinkRunnable); // Remove any existing callbacks
            cursorView.postDelayed(cursorBlinkRunnable, 500); // Start the cursor blinking
        }
    }

    private void hideCursor() {
        if (cursorVisible) {
            cursorVisible = false;
            FrameLayout rootView = activeEditText.getRootView().findViewById(android.R.id.content);
            rootView.removeView(cursorView);
            cursorHandler.removeCallbacks(cursorRunnable);
            cursorView.setVisibility(View.GONE); // Hide the cursor view
            cursorView.removeCallbacks(cursorBlinkRunnable); // Remove the cursor blinking callback
        }
    }


    private void updateCursorPosition() {
        if (activeEditText != null && cursorVisible) {
            int selectionStart = activeEditText.getSelectionStart();
            int selectionEnd = activeEditText.getSelectionEnd();

            Layout layout = activeEditText.getLayout();
            if (layout != null) {
                int line = layout.getLineForOffset(selectionStart);

                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);
                int descent = layout.getLineDescent(line);

                int paddingTop = activeEditText.getPaddingTop();
                int paddingBottom = activeEditText.getPaddingBottom();
                int textHeight = descent - ascent;
                int cursorHeight = textHeight; // Set the cursor height equal to the text height
                int cursorWidth = 3; // Adjust the width as desired

                int cursorX;
                int cursorY = activeEditText.getTop() + baseline + (textHeight / 2) - paddingTop;

                if (selectionStart > 0) {
                    cursorX = (int) (layout.getPrimaryHorizontal(selectionStart - 1) + activeEditText.getX() - activeEditText.getPaddingLeft());
                } else {
                    cursorX = (int) (layout.getPrimaryHorizontal(selectionStart) + activeEditText.getX() - activeEditText.getPaddingLeft());
                }

                cursorView.setX(cursorX);
                cursorView.setY(cursorY);
                cursorView.setLayoutParams(new FrameLayout.LayoutParams(cursorWidth, cursorHeight));
            }
        }
    }








    private Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            updateCursorPosition();
            cursorHandler.postDelayed(this, 500);
        }
    };
    private Runnable cursorBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (cursorVisible) {
                int visibility = cursorView.getVisibility();
                cursorView.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                cursorView.postDelayed(this, 500); // Toggle visibility every 500ms
            }
        }
    };

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
            hideCursor();
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
