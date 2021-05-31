package com.app.avofinder;


import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


public class aule extends AppCompatActivity {

    String[] pass; //Array contenente l'aula e il piano ricevuti dallo split del valore ricevuto
    String[] s; //Array contenenti i valori ricevuti dall'acticity
    LinearLayout l; //Variabile LinearLayout
    List<Button> buttons = new ArrayList<>(); //Array contenente i bottoni
    boolean controllo = false;  //controllo per onStop()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aule);

        l = findViewById(R.id.table);
        SearchView se = findViewById(R.id.search); //Inizializzo il searchview che permette la ricerca delle aule
        ImageButton b1 = findViewById(R.id.back); //Bottone per tornare indietro
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controllo = true;       //Se controllo è true indica che l'utente ha premuto il tasto per tornare al mainActivity
                Intent i = new Intent(aule.this, MainActivity.class);
                i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        s = getIntent().getExtras().getStringArray("polygons"); //Variabile contenente il nome dei poligoni e il relativo piano
        for(int i = 0; i<s.length; i++) {
            //Controllo se il nome è nullo
            if(s[i]!=null) {
                pass = s[i].split(";"); //Splitto la variabile ricevuta
                Button b = new Button(aule.this); //Creo un oggetto bottone
                String s1 = "Aula: "+pass[0]; //Imposto la variabile rappresentante l'aula
                String s2 = "Piano: "+pass[1]; //Imposto la variabile rappresentante il piano
                int n = s1.length();
                int m = s2.length();
                //Imposto il layout del testo del bottone
                Spannable span = new SpannableString(s1 + "\n" +  s2);
                span.setSpan(new RelativeSizeSpan(0.8f), n, (n+m+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                b.setText(span); //Aggiungo il layout del testo al bottone
                b.setBackgroundResource(R.drawable.custom_button); //Imposto il background personalizzato del bottone
                buttons.add(b); //Aggiungo i bottoni ad un array di bottoni
                int count = i;
                //Aggiungo un listener al bottone che permette di visualizzare l'aula sulla mappa
                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        controllo = true;       //Se controllo è true indica che l'utente ha premuto il tasto per tornare al mainActivity
                        Intent i = new Intent(aule.this, MainActivity.class);
                        i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        i.putExtra("class", s[count] + ";" + pass[1]); //Passo all'activity il nome dell'aula e del piano
                        startActivity(i);
                    }
                });
                l.addView(b); //Aggiungo i bottoni al LinearLayout (alla pagina)
            }
        }

        //Parametri per l'icona di ricerca
        int id = se.getContext().getResources().getIdentifier("android:id/search_src_text", null,
                null);
        TextView textView = (TextView) se.findViewById(id);
        textView.setTextColor(Color.parseColor("#f1c047"));

        //Metodo che permette la ricerca
        se.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            //Metodo che permette la ricerca dinamica in tempo reale delle aule
            @Override
            public boolean onQueryTextChange(String newText) {

                //Per ogni bottone se contiene la parola cercata viene visualizzato altrimenti rimane invisibile
                for (Button value : buttons) {
                    if(newText.equals(""))
                        value.setVisibility(View.VISIBLE);
                    if(value.getText().toString().toLowerCase().contains(newText)){
                        value.setVisibility(View.VISIBLE);
                    }else{
                        value.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
    }

    public Boolean loadSp(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        boolean text = sp.getBoolean("log1",false);
        return text;
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
        if(!controllo) { //Se controllo è false indica che l'utente non ha premuto il tasto per uscire e quindi bisogna modificare le variabili di sessione
            if (!loadSp2()) {         //Contiene se l'utente vuole rimanere loggato
                changeSp();
            }
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }
}
