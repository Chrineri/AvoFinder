package com.app.avofinder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.avofinder.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Login extends AppCompatActivity {


    TextInputEditText email1;
    TextInputEditText password;
    Thread clientThread;
    Editable email, pass;
    Handler ClientHandler;
    String e = "";
    String p = "";
    TextView scritta1,scritta2;
    //Per mantenere loggato
    private CheckBox checkBox;
    private boolean logged;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        scritta1 =findViewById(R.id.scritta1);

        scritta1.setAutoSizeTextTypeUniformWithConfiguration(
                1, 40, 1, TypedValue.COMPLEX_UNIT_DIP);

        scritta2 =findViewById(R.id.scritta2);

        scritta2.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        /*
        ***Bottone per andare alla registrazione se non si è registrati
         */
        TextView ToReg = (TextView) findViewById(R.id.registr);
        ToReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // definisco l'intenzione
                Intent openPage1 = new Intent(Login.this,Registrazione.class);
                startActivity(openPage1);
            }
        });

        /*
         ***Bottone per andare alla mappa
         */
        ImageButton ToMap = findViewById(R.id.bottMap);
        ToMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent openPage1 = new Intent(Login.this,MainActivity.class);
                openPage1.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(openPage1);
            }
        });
        /*
        ***CheckBox
         */
        checkBox = findViewById(R.id.toggleButton);
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(checkBox.isChecked()){
                    logged = true;
                }else{
                    logged = false;
                }
            }
        });


        /*
        ***Bottone per Login
         */
        Button ToServerLog = findViewById(R.id.bottReg);
        ToServerLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean contrE = false;         //Controllo per email
                boolean contrP = false;         //Controllo per password
                ////////////////////////////////////////////////////////
                /*
                 *** Salvo eMail
                 */
                email1 =findViewById(R.id.email);
                email = email1.getText();

                /*
                 *** Salvo password
                 */
                password =findViewById(R.id.psw);
                pass = password.getText();
                ////////////////////////////////////////////////////////////
                //Editable ---> String
                e = email.toString();
                e = e.replace(" ","");  //Rimuovo gli spazi dall'email
                p = pass.toString();
                p = p.replace(" ",""); //Rimuovo gli spazi dalla password
                System.out.println("-------------------------------TASTO LOGIN------------------------------");
                /*
                ***Controllo se email è vuoto
                 */
                if(e.isEmpty()){
                    email1.setError("Immetti una mail corretta");
                }else{
                    contrE = true;  //Se è corretto contrE diventa true
                }
                /*
                ***Controllo se psw è vuoto, se è troppo lunga
                 */

                if(p.isEmpty()){
                        password.setError("Immetti una psw corretta");

                }else if(p.length()>16){
                    password.setError("Immetti una psw più corta");
                }else{
                    contrP = true;  //Se è corretto contrP diventa true
                }
                System.out.println(" email:"+e+" psw:"+p);

                /*
                *** Se non ci sono errori
                 */
                if(contrP && contrE){

                    clientThread = new Thread(new clientThread());
                    ClientHandler = new Handler();
                    clientThread.start();

                }
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

    /*
    ***Thread per socket
     */

    class clientThread implements Runnable{


        @Override
        public void run() {



        }
    }

    class ClientHandler implements Runnable{
        String ricevo;

        public ClientHandler(String ricevo){
            this.ricevo = ricevo;
        }

        @Override
        public void run() {


        }


    }

}
