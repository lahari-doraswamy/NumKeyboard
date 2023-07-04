package com.example.numkeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.numpad.Numpad;
import com.example.numpad.NumpadListener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.editText);
        Numpad numpad = findViewById(R.id.numberKeyboardView);
        numpad.setTargetEditText(editText);
        numpad.setButtonColor(Color.BLACK,Color.WHITE);

        numpad.setNumpadListener(value -> Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show());


    }
}