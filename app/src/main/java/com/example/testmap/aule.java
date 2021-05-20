package com.example.testmap;


import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


public class aule extends AppCompatActivity {

    String[] pass;
    String[] s;
    LinearLayout l;
    List<Button> buttons = new ArrayList<>();
    boolean controllo = false;  //controllo per onStop()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aule);

        l = findViewById(R.id.table);
        SearchView se = findViewById(R.id.search);
        ImageButton b1 = findViewById(R.id.back);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controllo = true;       //Se controllo è true indica che l'utente ha premuto il tasto per tornare al mainActivity
                Intent i = new Intent(aule.this, MainActivity.class);
                i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        s = getIntent().getExtras().getStringArray("polygons");
        for(int i = 0; i<s.length; i++) {
            if(s[i]!=null) {
                pass = s[i].split(";");
                Button b = new Button(aule.this);
                String s1 = "Aula: "+pass[0];
                String s2 = "Piano: "+pass[1];
                int n = s1.length();
                int m = s2.length();
                Spannable span = new SpannableString(s1 + "\n" +  s2);
                span.setSpan(new RelativeSizeSpan(0.8f), n, (n+m+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                b.setText(span);
                //b.setText(pass[0]+"\n\n Piano:"+pass[1]);
                b.setBackgroundResource(R.drawable.custom_button);
                buttons.add(b);
                int count = i;
                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        controllo = true;       //Se controllo è true indica che l'utente ha premuto il tasto per tornare al mainActivity
                        Intent i = new Intent(aule.this, MainActivity.class);
                        i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        i.putExtra("class", s[count] + ";" + pass[1]);
                        startActivity(i);
                    }
                });
                l.addView(b);
            }
        }

        int id = se.getContext().getResources().getIdentifier("android:id/search_src_text", null,
                null);
        TextView textView = (TextView) se.findViewById(id);
        textView.setTextColor(Color.parseColor("#f1c047"));
        
        se.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

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
        System.out.println(loadSp());
        ed.commit();
    }

    @Override
    protected void onStop() {
        if(!controllo) { //Se controllo è false indica che l'utente non ha premuto il tasto per uscire e quindi bisogna modificare le variabili di sessione
            System.out.println("ciaooooooooooooooooooooo");
            if (!loadSp2()) {         //Contiene se l'utente vuole rimanere loggato
                changeSp();
                System.out.println("ciaooo: " + loadSp());
            }
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }
}
