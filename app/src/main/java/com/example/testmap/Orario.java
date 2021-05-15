package com.example.testmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Orario extends AppCompatActivity {
    TextView classe;

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
            classe =findViewById(R.id.textView3);
            classe.append(getSpClass());
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

    public String getSpClass(){
        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        String classe = sp.getString("classe","");
        return classe;
    }

    public Boolean loadSp2(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        boolean text1 = sp.getBoolean("log2",false);
        return text1;
    }

    public void changeSp(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();        //Per editarlo
        ed.putBoolean("log1",false);         //imposto a logged e in base al suo valore imposta true\false
        ed.commit();
    }

    @Override
    protected void onStop() {
        if(!loadSp2()){         //Contiene se l'utente vuole rimanere loggato
            changeSp();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }
}


