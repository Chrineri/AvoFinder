package com.example.testmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Conferma extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conferma);

        Button ToServerLog = findViewById(R.id.bottReg);
        ToServerLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent openPage1 = new Intent(Conferma.this,Login.class);
                startActivity(openPage1);
            }
        });



    }
}
