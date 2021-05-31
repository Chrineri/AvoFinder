package com.app.avofinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Orario extends AppCompatActivity {
    TimetableView timetable; //Tabella dell'orario
    ImageButton back;   //Tasto indietro
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


            ArrayList<Schedule> schedules = new ArrayList<Schedule>(); //ArrayList di schedule contenenti parametri dell'orario
            String[] lineo = orario.split("--"); //Splitto le linee degli orari
            for(int i=0;i<lineo.length;i++){
                String[] oraClasse = lineo[i].split(";"); //Splitto ogni linea e prendo i parametri necessari all'orario
                Schedule schedule = new Schedule(); //Creo una schedule
                schedule.setClassTitle(oraClasse[3]); //Imposta la materia
                schedule.setClassPlace(oraClasse[2]); //Imposta l'aula
                schedule.setProfessorName(oraClasse[5]);// contiene il piano a cui si trova la classe
                schedule.setStartTime(new Time(Integer.parseInt(oraClasse[1]),0)); //Imposto l'ora di inizio
                schedule.setEndTime(new Time(Integer.parseInt(oraClasse[1])+Integer.parseInt(oraClasse[4]),0)); //Imposto l'ora di fine
                String result = StringUtils.substring(oraClasse[0], 0, oraClasse[0].length() - 1); //Elimino l'ultimo carattere (ì) per evitare conflitti
                //Imposto il giorno
                switch(result){
                    case "luned":
                        schedule.setDay(0);
                        break;
                    case "marted":
                        schedule.setDay(1);
                        break;
                    case "mercoled":
                        schedule.setDay(2);
                        break;
                    case "gioved":
                        schedule.setDay(3);
                        break;
                    case "venerd":
                        schedule.setDay(4);
                        break;
                    case "sabat":
                        schedule.setDay(5);
                        break;
                }
                //Aggiungo la singola schedule all'array di schedules
                schedules.add(schedule);
            }


            //Aggiungo un listener per il click dello sticker
            timetable.setOnStickerSelectEventListener((idx, schedules1) -> {
                controllo = true;       //Se controllo è true indica che l'utente ha premuto una classe per tornare al mainActivity
                Intent i = new Intent(Orario.this, MainActivity.class);
                i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("class", schedules1.get(idx).getClassPlace()+ ";" + schedules1.get(idx).getProfessorName());
                startActivity(i);


            });
            //Aggiungo le schedules alla tabella dell'orario
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


