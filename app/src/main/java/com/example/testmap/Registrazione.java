package com.example.testmap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Registrazione extends AppCompatActivity {

    Editable email, pass, passControllo;
    TextInputEditText eMail;
    TextInputEditText psw;
    TextInputEditText pswControllo;
    Thread clientThread;
    Handler ClientHandler;
    String sceltaSpinner = "";
    List < String > list = new ArrayList < String > ();
    String e="";
    String p="";
    TextView scritta1,scritta2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);
        scritta1 =findViewById(R.id.scritta1);

        scritta1.setAutoSizeTextTypeUniformWithConfiguration(
                1, 40, 1, TypedValue.COMPLEX_UNIT_DIP);

        scritta2 =findViewById(R.id.scritta2);

        scritta2.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);


        /*
         ***Lettura dei dati dalla pagina php che si collega al database
         */
        Thread t1 = new Thread() {
            public void run() {

                try {
                    //Connessione al webServer con SSL
                    SSLSocketFactory factory =
                            (SSLSocketFactory) SSLSocketFactory.getDefault();
                    SSLSocket socket =
                            (SSLSocket) factory.createSocket("avofinder.altervista.org", 443);

                    socket.startHandshake();

                    BufferedWriter bw = null;

                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                    bw.write("POST http://avofinder.altervista.org/index.php?email=c&psw=c&contr=3&classe=c HTTP/1.1\r\n");
                    bw.write("Host: altervista\r\n");
                    bw.write("Cache-Control: no-cache\r\n");
                    bw.write("\r\n");
                    bw.flush();

                    /* leggo risposta */
                    String lineD;
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //Le prime otto righe dovrebbero essere sempre inutili
                    int c = 0;
                    while ((lineD = br.readLine()) != null) {
                        c++;
                        //c indica la riga da leggere, se è uguale a 9 indica che contiene i parametri che ci interessano
                        if (c == 9) {
                            System.out.println(lineD);
                            String[] split = lineD.split(";");
                            list.addAll(Arrays.asList(split));
                        }
                    }
                    br.close();
                    bw.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        };
        t1.start();
        try {
            //il thread sta riconvergendo nel thread principale
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t1.interrupt();

        /*
         ***Spinner
         */
        Spinner spinner = findViewById(R.id.spinner1);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView < ? > parent, View view, int position, long id) {
                sceltaSpinner = parent.getItemAtPosition(position).toString();
                System.out.println(sceltaSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView < ? > parent) {

            }
        });

        /*
         *** Bottone per tornare al Login
         */
        ImageButton ToLog = findViewById(R.id.bottMap);
        ToLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent openPage1 = new Intent(Registrazione.this, Login.class);
                startActivity(openPage1);
            }
        });

        /*
         *** Bottone per registrazione
         */
        Button Reg = findViewById(R.id.bottReg);
        Reg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean contrE = false; //Controllo per email
                boolean contrE1 = false; //Controllo per dominio
                boolean contrP = false; //Controllo per password
                boolean contrPC = false; //Controllo per pswControllo
                //////////////////////////////////////////////////////////////////
                /*
                 *** Salvo email
                 */
                eMail = findViewById(R.id.email);
                email = eMail.getText();

                /*
                 *** Salvo password
                 */
                psw = findViewById(R.id.psw);
                pass = psw.getText();
                /*
                 *** Salvo pswControllo
                 */
                pswControllo = findViewById(R.id.pswC);
                passControllo = pswControllo.getText();
                //////////////////////////////////////////////////////////////////
                //Editable ---> String
                 e = email.toString();
                 e = e.replace(" ",""); //Rimuovo gli spazi dall'email
                 p = pass.toString();
                 p = p.replace(" ",""); //Rimuovo gli spazi dalla password
                String pC = passControllo.toString();

                System.out.println("email:" + e + " psw:" + p + " pswC:" + pC + sceltaSpinner);

                /*
                 ***Controllo se email è vuoto
                 */
                if (e.isEmpty()) {
                    eMail.setError("Immetti una mail corretta");
                } else {
                    contrE = true; //Se è corretto contrE diventa true
                }
                /*
                 *** Controllo se le due psw combaciano
                 */
                if (p.equals(pC)) {
                    contrPC = true;
                } else {
                    psw.setError("le due password non combaciano");
                }

                /*
                 ***Controllo se psw è vuoto, se è troppo lunga
                 */

                if (p.isEmpty()) {
                    psw.setError("Immetti una psw corretta");

                } else if (p.length() > 16) {
                    psw.setError("Immetti una psw più corta");
                } else {
                    contrP = true; //Se è corretto contrP diventa true
                }

                /*
                 *** Controllo dominio
                 */

                try {
                    String[] controllo = e.split("@");

                    if (controllo[1].equals("studenti.itisavogadro.it") || controllo[1].equals("itisavogadro.it")) {
                        contrE1 = true;
                    } else {
                        eMail.setError("Immetti una mail corretta");
                    }
                } catch (Exception error) {
                    eMail.setError("Immetti una mail corretta");
                }

                if (contrP && contrE && contrPC && contrE1) {
                    System.out.println("Invio al server");
                    clientThread = new Thread(new Registrazione.clientThread());
                    ClientHandler = new Handler();
                    clientThread.start();
                }

            }
        });
    }

    /*
     *** Thread per socket
     */
    class clientThread implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try {
                            //Connessione al webServer con SSL
                            SSLSocketFactory factory =
                                    (SSLSocketFactory) SSLSocketFactory.getDefault();
                            SSLSocket socket =
                                    (SSLSocket) factory.createSocket("avofinder.altervista.org", 443);

                            socket.startHandshake();

                            BufferedWriter bw = null;
                            String classeFinale = sceltaSpinner.replace(" ","_");   //Cambio spazzi con _ perchè se no da errore sui parametri

                            /*
                            ***Applico Sha256 alla password (p)
                             */
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            byte[] hash = digest.digest(p.getBytes(StandardCharsets.UTF_8));
                            String pswCript = String.format("%064x", new BigInteger(1, hash));  //Viene inviata la password criptata


                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                            bw.write("POST http://avofinder.altervista.org/index.php?email="+e+"&psw="+pswCript+"&contr=2&classe="+classeFinale+" HTTP/1.1\r\n");
                            bw.write("Host: altervista\r\n");
                            bw.write("Cache-Control: no-cache\r\n");
                            bw.write("\r\n");
                            bw.flush();

                            /* leggo risposta */
                            String lineD;
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            //Le prime otto righe dovrebbero essere sempre inutili
                            int c = 0;
                            while ((lineD = br.readLine()) != null) {
                                c++;
                                //c indica la riga da leggere, se è uguale a 9 indica che contiene i parametri che ci interessano
                                if (c == 9) {
                                System.out.println(lineD);
                                ClientHandler.post(new Registrazione.ClientHandler(lineD));
                                }
                            }
                            br.close();
                            bw.close();
                            socket.close();
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }
    }

    class ClientHandler implements Runnable {
        String ricevo;

        public ClientHandler(String ricevo) {
            this.ricevo = ricevo;
        }

        @Override
        public void run() {

            switch (ricevo) {
                case "1":
                    System.out.println("Registrazione effettuata con successo");
                    Intent openPage2 = new Intent(Registrazione.this, Conferma.class);
                    startActivity(openPage2);
                    break;
                case "2":
                    eMail.setError("Errore nella registrazione");
                    break;

            }
        }
    }
}