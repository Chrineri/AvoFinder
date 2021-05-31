package com.app.avofinder;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Conferma extends AppCompatActivity {
    TextView top,top2,textView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conferma);

        top =findViewById(R.id.top);

        top.setAutoSizeTextTypeUniformWithConfiguration(
                1, 40, 1, TypedValue.COMPLEX_UNIT_DIP);

        top2 =findViewById(R.id.top2);

        top2.setAutoSizeTextTypeUniformWithConfiguration(
                1, 40, 1, TypedValue.COMPLEX_UNIT_DIP);

        textView =findViewById(R.id.textView);

        textView.setAutoSizeTextTypeUniformWithConfiguration(
                1, 40, 1, TypedValue.COMPLEX_UNIT_DIP);


        Button ToServerLog = findViewById(R.id.bottReg);
        ToServerLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openPage1 = new Intent(Conferma.this,Login.class);
                startActivity(openPage1);
            }
        });



    }

    @Override
    public void onBackPressed() {

    }
}
