package com.example.testmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Sticker;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Orario extends AppCompatActivity {
    TimetableView timetable;
    ImageButton back;
    String classe, orario;      //orario contiene l'orario della classe
    boolean controllo = false;  //controllo per onStop()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Boolean log = loadSp();
        //Nel caso non sia loggato
        if(!loadSp()) {
            Intent openPage1 = new Intent(Orario.this,Login.class);
            openPage1.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(openPage1);
        }else{
            classe = getSpClass();       //Prendo la classe dell'utente
            classe = classe.replace(" ",";");       //inserisco al posto degli spazi i ;
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
                        bw.write("POST http://avofinder.altervista.org/index.php?email=c&psw=c&contr=4&classe="+classe+" HTTP/1.1\r\n");
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
                                orario = lineD;
                            }
                        }
                        //System.out.println(orario);
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

            //////////////////Fine thread///////////////////////////////
            setContentView(R.layout.orario);
            timetable = findViewById(R.id.tablet);
            back =  findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    controllo = true;       //Se controllo è true indica che l'utente ha premuto il tasto per tornare al mainActivity
                    Intent openPage1 = new Intent(Orario.this, MainActivity.class);
                    openPage1.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(openPage1);
                }
            });


            ArrayList<Schedule> schedules = new ArrayList<Schedule>();
            String[] lineo = orario.split("--");
            Sticker s = new Sticker();
            for(int i=0;i<lineo.length;i++){
                String[] oraClasse = lineo[i].split(";");
                Schedule schedule = new Schedule();
                schedule.setClassTitle(oraClasse[3]); // sets subject
                schedule.setClassPlace(oraClasse[2]); // sets place
                schedule.setProfessorName(oraClasse[5]);// contiene il piano a cui si trova la classe
                schedule.setStartTime(new Time(Integer.parseInt(oraClasse[1]),0)); // sets the beginning of class time (hour,minute)
                schedule.setEndTime(new Time(Integer.parseInt(oraClasse[1])+Integer.parseInt(oraClasse[4]),0)); // sets the end of class time (hour,minute)
                switch(oraClasse[0]){
                    case "lunedì":
                        schedule.setDay(0);
                        break;
                    case "martedì":
                        schedule.setDay(1);
                        break;
                    case "mercoledì":
                        schedule.setDay(2);
                        break;
                    case "giovedì":
                        schedule.setDay(3);
                        break;
                    case "venerdì":
                        schedule.setDay(4);
                        break;
                    case "sabato":
                        schedule.setDay(5);
                        break;
                }

                schedules.add(schedule);
            }



            timetable.setOnStickerSelectEventListener((idx, schedules1) -> {
                controllo = true;       //Se controllo è true indica che l'utente ha premuto una classe per tornare al mainActivity
                Intent i = new Intent(Orario.this, MainActivity.class);
                i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("class", schedules1.get(idx).getClassPlace()+ ";" + schedules1.get(idx).getProfessorName());
                startActivity(i);


            });
            timetable.add(schedules);
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
        System.out.println("CAZZOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO: "+controllo);
        if(!controllo) { //Se controllo è false indica che l'utente non ha premuto il tasto per uscire e quindi bisogna modificare le variabili di sessione
            if (!loadSp2()) {         //Contiene se l'utente vuole rimanere loggato
                changeSp();
            }
        }
        controllo = false;  //Riazzero Variabile
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }
}


