package com.example.testmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Orario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Boolean log = loadSp();
        //Nel caso non sia loggato
        if(!loadSp()) {
            Intent openPage1 = new Intent(Orario.this,Login.class);
            startActivity(openPage1);
        }else{
            System.out.println("ciaooo " + log);
            setContentView(R.layout.orario);
        }



    }

    /*
    *** Metodo per leggere i valori per controllo se l'utente è già loggato
     */

    public Boolean loadSp(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        boolean text = sp.getBoolean("log1",false);
        return text;
    }
}


