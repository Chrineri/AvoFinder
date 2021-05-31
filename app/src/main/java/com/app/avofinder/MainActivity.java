package com.app.avofinder;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import org.osmdroid.views.overlay.infowindow.InfoWindow;


import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null; //Oggetto mappa
    private List l; //Lista che contiene tutti gli oggetti presenti nel file GeoJSON
    private List<Polygon> p = null; //Lista che contiene tutti i poligoni presenti nel file GeoJSON
    private List<Polygon> p0 = new ArrayList<>(); //Lista che contiene i poligoni del piano 0
    private List<Polygon> p1 = new ArrayList<>(); //Lista che contiene i poligoni del piano 1
    private List<Polygon> p2 = new ArrayList<>(); //Lista che contiene i poligoni del piano 2
    private List<Polygon> p3 = new ArrayList<>(); //Lista che contiene i poligoni del piano 3
    private List<Polygon> p4 = new ArrayList<>(); //Lista che contiene i poligoni del piano 4
    private int piano = 0; //Variabile per definire il piano corrente (necessaria per lo scambio di piani)
    List<Marker> markers = new ArrayList<>(); //Lista che contiene tutti i markers contenenti i nomi delle aule che verranno visualizzati su ogni poligono
    Spinner s; //Spinner per la scelta della pagina (login o logout, aule e orario)
    String getClass=""; //Variabile utilizzata per prendere i parametri provenienti dalle diverse activty quali classe e piano necessarie per la visualizzazione sulla mappa
    CheckBox c; //Checkbox per la visualizzazione o meno dei markers sulla mappa
    String[] pol; //Array di poligoni che viene passato alla classe aule contenente tutti i poligoni presenti nella mappa
    String[] passed; //Variabile contenente lo split del valore ricevuto dalle altre activity (getClass). A posizione 0 vi è il nome dell'aula e a posizione 1 il piano
    Button b0; //Bottone per il passaggio al piano 0
    Button b1; //Bottone per il passaggio al piano 1
    Button b2; //Bottone per il passaggio al piano 2
    Button b3; //Bottone per il passaggio al piano 3
    Button b4; //Bottone per il passaggio al piano 4
    TextView tf; //TextView che mostra il piano in cui ci troviamo attualmente
    boolean controllo = false;  //controllo per onStop()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onCreate(Bundle savedInstanceState) {
         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //La modalità notte sarà uguale alla modalità giorno
         super.onCreate(savedInstanceState);


         //Configurazione preliminare per le preferenze di sistema
         Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        //Controllo dei permessi necessari al funzionamento della mappa, se non vi sono stati dati i permessi verranno richiesti all'avvio dell'applicazione
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

        }


        //Inizializzazione dell'interfaccia grafica
        setContentView(R.layout.activity_main);



        s = findViewById(R.id.Spinner); //Inizializzazione spinner
        //Aggiunta dei vari elementi all'interno dello spinner
        List<String> list = new ArrayList<String>();
        if(!loadSp())
            list.add("\uD83D\uDC64 Login    ");
        else
            list.add("\uD83D\uDC4B Logout    ");

        list.add("\uD83D\uDEAA Aule     ");
        list.add("⌚ Orario   ");
        list.add("");
        final int listsize = list.size() - 1;

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list) {
            @Override
            public int getCount() {
                return(listsize); // Truncate the list
            }
        };

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(dataAdapter);
        s.setSelection(listsize);




        //Inizializzazione della mappa
        map = (MapView) findViewById(R.id.map);  //Creazione dell'oggetto MapView
        map.setTileSource(TileSourceFactory.MAPNIK);  //Imposto il tipo di mappa
        map.setTilesScaledToDpi(true); //Imposta lo scaling della mappa ai DPI del display utilizzato in quel momento
        RotationGestureOverlay rg = new RotationGestureOverlay(map); //Oggetto necessario all'abilitazione dei gesti di rotazione all'interno della mappa
        rg.setEnabled(true); //Permetto la rotazione della mappa
        map.setMultiTouchControls(true);  //Abilito lo zoom con due dita







        KmlDocument kmlDocument = new KmlDocument();  //Creo un oggetto KmlDocument che permette la conversione dei formati geojson


        //Leggo il file geojson contenente la mappa scolastica e lo converto in formato KML
        try {
            InputStream is = getAssets().open("mappa.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            kmlDocument.parseGeoJSON(json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        //Vado a impostare i paramentri per l'inserimento della piantina formato KML nella mappa OSM
        @SuppressLint("UseCompatLoadingForDrawables") Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, Color.parseColor("#104281"), 5f, Color.parseColor("#f1c047")); //Imposto lo stile dei poligoni della mappa
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument); //Imposto l'overlay visualizzato sulla mappa contenente i poligoni

        l = geoJsonOverlay.getItems(); //Prendo tutti gli oggetti che compongono la piantina scolastica
        c = (CheckBox) findViewById(R.id.check); //Inizializzo il checkbox

        //Metto ogni oggetto in un arraylist di polygoni
        p = (List<Polygon>) l;
        pol  = new String[p.size()]; //Inizializzo l'array di poligoni utile per la classe aule

        //Creo e avvio il thread che aggiunge gli elementi nei vari array (utile per non abbassare le prestazioni del thread principale)
        myThread t = new myThread();
        t.start();


        //Imposto un item listener sullo spinner che mi permetterà il passaggio tra le varie activity
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                    switch (item.toString()) {
                        case "\uD83D\uDC64 Login    ":
                            s.setSelection(listsize); //Imposto lo spinner al valore nullo in modo tale che non si vedano scritte che coprono
                            controllo = true;       //Se controllo è true indica che l'utente ha premuto un bottone e non bisogna rimuovere le variabili di sessioni
                            Intent openPage1 = new Intent(MainActivity.this, Login.class);
                            startActivity(openPage1);
                            break;
                        case "\uD83D\uDC4B Logout    ":
                            changeSp();
                            System.out.println(loadSp());
                            Intent openPage3 = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(openPage3);
                            Toast toast = Toast.makeText(getApplicationContext(), "Logout effettuato correttamente", Toast.LENGTH_SHORT);
                            toast.show();
                            s.setSelection(listsize);
                            break;
                        case "\uD83D\uDEAA Aule     ":
                            controllo = true;       //Se controllo è true indica che l'utente ha premuto un bottone e non bisogna rimuovere le variabili di sessioni
                            Intent i = new Intent(MainActivity.this, aule.class);
                            i.putExtra("polygons", pol); //Passo l'array contenente tutte le aule e i relativi piani
                            startActivity(i);

                            s.setSelection(listsize);
                            break;
                        case "⌚ Orario   ":
                                controllo = true;       //Se controllo è true indica che l'utente ha premuto un bottone e non bisogna rimuovere le variabili di sessioni
                                Intent openPage2 = new Intent(MainActivity.this, Orario.class);
                                startActivity(openPage2);

                            s.setSelection(listsize);
                            break;
                        default:
                            break;
                    }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        setInfoWindow(); //Metodo che imposta le infowindow personalizzate

        getPassedValue(); //Richiamo il metodo che controlla e processa il valore passato dalle altre activity
        //Se la variabile contenente i valori ricevuti dalle altre activity è vuota non faccio niente altrimento imposto il piano 0 di default
        if(getIntent().hasExtra("class")){

        }else {
            setInitialFloor();
        }


        //Inizializzazione dei bottoni
        b0 = (Button) findViewById(R.id.l0);
        b1 = (Button) findViewById(R.id.l1);
        b2 = (Button) findViewById(R.id.l2);
        b3 = (Button) findViewById(R.id.l3);
        b4 = (Button) findViewById(R.id.l4);

        b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray)); //Imposto il colore del bottone di default
        b0.setTextColor(Color.WHITE); //Imposto il colore del testo del bottoness

        tf = (TextView) findViewById(R.id.floor1); //Inizializzazione del textview per la visualizzazione del piano attuale
        tf.setText(String.valueOf("P: "+piano)); //Imposto il valore del piano nel textview
        //Parametri per l'autoridimensionamento dei bottoni e dei testi
        tf.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        b0.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        b1.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        b2.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        b3.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);
        b4.setAutoSizeTextTypeUniformWithConfiguration(
                1, 20, 1, TypedValue.COMPLEX_UNIT_DIP);




        //Aggiungo i listener sul click a ogni bottone
        b0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(b0,b1,b2,b3,b4); //Imposto i colori dei bottoni a quelli di default
                resetColor(); //Metodo che reimposta il colore dei poligoni in base al piano selezionato
                b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray)); //Resetto il colore dei bottoni
                b0.setTextColor(Color.WHITE); //Imposto il colore del testo dei bottoni
                piano = 0; //Imposto il numero del piano selezionato
                setFloor(); //Imposto il piano con i relativi poligoni
                rg.setEnabled(true); //Aggiungo la possibilità di ruotare la mappa
                map.getOverlays().add(rg); //Aggiungo la rotazione alla mappa

            }
        });


        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetColor();
                setDefaultColor(b0,b1,b2,b3,b4);
                b1.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                b1.setTextColor(Color.WHITE);
                piano = 1;
                setFloor();
                rg.setEnabled(true);
                map.getOverlays().add(rg);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetColor();
                setDefaultColor(b0,b1,b2,b3,b4);
                b2.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                b2.setTextColor(Color.WHITE);
                piano = 2;
                setFloor();
                rg.setEnabled(true);
                map.getOverlays().add(rg);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetColor();
                setDefaultColor(b0,b1,b2,b3,b4);
                b3.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                b3.setTextColor(Color.WHITE);
                piano = 3;
                setFloor();
                rg.setEnabled(true);
                map.getOverlays().add(rg);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetColor();
                setDefaultColor(b0,b1,b2,b3,b4);
                b4.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                b4.setTextColor(Color.WHITE);
                piano = 4;
                setFloor();
                rg.setEnabled(true);
                map.getOverlays().add(rg);
            }
        });


        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetColor(); //Metodo che imposta i colori dei poligoni in base al piano selezionato
                checkCheckbox(); //Metodo che verifica se il checkbox è selezionato o meno per l'aggiunta dei markers
            }
        });

        //map.getOverlays().add(geoJsonOverlay); //metodo che permette di aggiungere l'overlay contenente tutti i poligoni, precedentemente creato, alla mappa

        map.setHorizontalMapRepetitionEnabled(false); //Disabilito la ripetizione della mappa orizzontalmente
        map.setVerticalMapRepetitionEnabled(false); //Disabilito la ripetizione della mappa verticalmente
        IMapController mapController = map.getController(); //Oggetto per gestire alcuni parametri della mappa
        mapController.setZoom((double)18); //Imposto il livello iniziale di zoom
        GeoPoint startPoint = new GeoPoint(45.07122657916548,7.692658552021214); //Creo un oggetto GeoPoint contenente la posizione di partenza della mappa (sulla scuola)
        mapController.setCenter(startPoint); //Imposto la posizione della mappa sul punto precedentemente creato
        map.setMinZoomLevel((double) 18); //Imposto il livello minimo di zoom
        map.setScrollableAreaLimitLatitude(45.07146656030419, 45.06952171642693,10); //Limito lo scroll in latitudine
        map.setScrollableAreaLimitLongitude(7.69138,7.693766902971969,10); //Limito lo scroll in longitudine
        map.getOverlays().add(rg); //Aggiungo l'oggetto che permette la rotazione della mappa

        //Metodo che richiede i permessi se necessario
        requestPermissionsIfNecessary(new String[] {

                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });


    }

    /*
     *** Metodo per leggere i valori per controllo se l'utente è già loggato
     */

    public Boolean loadSp(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        boolean text = sp.getBoolean("log1",false);
        return text;
    }
    /*
     *** Metodo per controllare se l'utente vuole rimanere registrato
     */

    public Boolean loadSp2(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        boolean text1 = sp.getBoolean("log2",false);
        return text1;
    }

    /*
     *** Metodo per cambiare i valori per controllo se l'utente è già loggato
     */

    public void changeSp(){

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();        //Per editarlo
        ed.putBoolean("log1",false);         //imposto a logged e in base al suo valore imposta true\false
        ed.commit();
    }





    public void setMarker() {
        for(int i=0;i<markers.size();i++)
            //Rimuovo i markers attualmente attivi
            map.getOverlayManager().remove(markers.get(i));
        for (int i = 0; i < p.size(); i++) {
            //Controllo se il piano è presente nella descrizione del poligono
            if (p.get(i).getSubDescription().contains("level=" + piano)) {
                if (p.get(i).getTitle() != null) {
                    map.getOverlays().add(p.get(i)); //Aggiungo il poligono alla mappa

                    Marker m = new Marker(map); //Creo un oggetto marker
                    m.setTextLabelBackgroundColor(Color.TRANSPARENT); //Imposto il background del marker
                    m.setTextLabelForegroundColor(Color.BLACK); //Imposto il colore del testo del marker
                    m.setTextIcon(p.get(i).getTitle()); //Imposto il testo del marker
                    m.setPosition(p.get(i).getInfoWindowLocation()); //Imposto la posizione del marker al centro del poligono
                    m.setInfoWindow(null); //Imposto l'infowindow del marker a nullo
                    map.getOverlayManager().add(m); //Aggiungo il marker sulla mappa
                    markers.add(m); //Aggiungo il marker alla lista dei markers
                }
            } else {
                map.getOverlays().remove(p.get(i)); //Rimuovo il poligono se non appartenente al piano
            }
        }
        map.invalidate(); //Aggiorno la mappa per vederne le modifiche
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume(); //Metodo che gestisce la mappa quando viene richiamato onResume
    }

    @Override
    public void onPause() {
        super.onPause();

        map.onPause();  //Metodo che gestisce la mappa quando viene richiamato onPause
    }
    /*
    *** Metodo per uscire dall'account quando chiudo l'applicazione
     */

    @Override
    protected void onStop() {
        System.out.println("ciaooooooo "+controllo);
        if(!controllo) { //Se controllo è false indica che l'utente non ha premuto il tasto per uscire e quindi bisogna modificare le variabili di sessione
            if (!loadSp2()) {         //Contiene se l'utente vuole rimanere loggato
                changeSp();
            }

        }
        controllo = false;  //Ri-imposto a false
        System.out.println("ciaooooooo1 "+controllo);
        super.onStop();
    }


    @Override
    public void onBackPressed() {

    }


    //Metodo che permette di gestire l'intent quando viene "riesumato" dal background
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Controllo se le variabili ricevute dalle altre activity hanno un contenuto
        if(intent.hasExtra("class")){
        InfoWindow.closeAllInfoWindowsOn(map); //Chiudo le finestre attive sulla mappa
        getClass = intent.getExtras().getString("class"); //Prendo il valore passato dall'activity
        c.setChecked(false); //Imposto il checkbox a false in modo da non creare conflitti
        resetColor(); //Metodo che reimposta il colore dei poligoni in base al piano selezionato
        getPassedValue(); //Richiamo il metodo che controlla e processa il valore passato dalle altre activity

        }
        else{
            //Se non vi è nessun contenuto nelle variabili ricevute
            c.setChecked(false); //Imposto il checkbox a false
            resetColor(); //Metodo che reimposta il colore dei poligoni in base al piano selezionato
        }

    }


    //Se i permessi necessari non sono stati dati li richiedo

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }




    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void setFloor(){

        //Metodo che imposta il piano in base al bottone selezionato
        map.getOverlays().clear(); //Pulisco la mappa
        tf.setText(String.valueOf("P: "+piano)); //Imposto il textview in base al piano selezionato
        //Controllo il piano e aggiugno i poligoni alla mappa relativi al piano
        switch(piano){
            case 0: map.getOverlays().addAll(p0);break;
            case 1: map.getOverlays().addAll(p1);break;
            case 2: map.getOverlays().addAll(p2);break;
            case 3: map.getOverlays().addAll(p3);break;
            case 4: map.getOverlays().addAll(p4);break;
        }
        //Se il checkbox è attivo aggiungo i markers alla mappa
        if(c.isChecked()){
            setMarker(); //Richiamo il metodo che imposta i markers
        }else{
            //Chiudo le infowindow attualmente attive
            if(!InfoWindow.getOpenedInfoWindowsOn(map).isEmpty())
                InfoWindow.closeAllInfoWindowsOn(map);
        }
        map.invalidate(); //Aggiorno la mappa per attuare le modifiche
    }

    //Metodo per il controllo dei checkbox
    public void checkCheckbox(){
            //Se il checkbox e impostato vado ad aggiungere i markers
            if(c.isChecked()){
                    InfoWindow.closeAllInfoWindowsOn(map); //Chiudo le infowindow attive
                        for(int i=0;i<p.size();i++) {
                            //Controllo se il piano è presente nella descrizione del poligono
                            if(p.get(i).getSubDescription().contains("level="+piano)) {
                                //Controllo se il titolo del poligono esiste
                                if(p.get(i).getTitle()!=null) {
                                    map.getOverlays().add(p.get(i)); //Aggiungo il poligono alla mappa

                                    Marker m = new Marker(map); //Creo un oggetto marker
                                    m.setTextLabelBackgroundColor(Color.TRANSPARENT); //Imposto il background del marker
                                    m.setTextLabelForegroundColor(Color.BLACK); //Imposto il colore del testo del marker
                                    m.setTextIcon(p.get(i).getTitle()); //Imposto il testo del marker
                                    m.setPosition(p.get(i).getInfoWindowLocation()); //Imposto la posizione del marker al centro del poligono
                                    m.setInfoWindow(null); //Imposto l'infowindow del marker a nullo
                                    map.getOverlayManager().add(m); //Aggiungo il marker sulla mappa
                                    markers.add(m); //Aggiungo il marker alla lista dei markers
                                }
                            }else {
                                map.getOverlays().remove(p.get(i)); //Rimuovo i poligoni dalla mappa
                            }
                    }
                }else{
                    //Se il checkbox non è selezionato rimuovo i markers dalla mappa
                    for(int i=0;i<markers.size();i++)
                        map.getOverlayManager().remove(markers.get(i));//Rimuovo i markers dalla mappa
                }

                map.invalidate(); //Aggiorno la mappa in modo tale da vedere le modifiche
        }




    public void getPassedValue(){
        //Controllo se la variabile getClass ha valori all'interno
        if(getIntent().hasExtra("class")){
            getClass = getIntent().getExtras().getString("class");
        }

        //cambio il colore dei poligoni selezionati e passati dall'activity aule
        if(!getClass.equals("")){
            setDefaultColor(b0,b1,b2,b3,b4); //Imposto i valori dei bottoni di default
            map.getOverlays().clear(); //Pulisco la mappa
            passed = getClass.split(";"); //Splitto la variabile getClass e ne prendo i valori quali aula e piano
            piano = Integer.parseInt(passed[1]);    //Prendo il piano della classe premuta
            tf.setText("P: "+piano); //Imposto il piano sul textview
            //Controllo a che piano si trova l'aula ricevuta
            switch(Integer.parseInt(passed[1])){
                //In base al piano vado ad aggiungere tutti i poligoni del piano relativo all'aula e imposto un colore diverso all'aula selezionata
                case 0:

                    map.getOverlays().addAll(p0);
                    for(int i=0;i<p0.size();i++){
                        if(passed[0].equals(p0.get(i).getTitle())){ //Verifico se l'aula passata è presente nel pianp
                            p0.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambio il colore dell'aula selezionata
                            p0.get(i).showInfoWindow(); //Faccio comparire l'infowindow dell'aula contenente il titolo
                            b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray)); //Imposto il colore del bottone a grigio (selezionato)
                            b0.setTextColor(Color.WHITE); //Imposto il colore del testo del bottone
                        }
                    }
                    break;
                case 1:
                    map.getOverlays().addAll(p1);
                    for(int i=0;i<p1.size();i++){
                        if(passed[0].equals(p1.get(i).getTitle())){
                            p1.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambio il colore dell'aula selezionata
                            p1.get(i).showInfoWindow();
                            b1.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b1.setTextColor(Color.WHITE);
                        }
                    }
                    break;
                case 2:
                    map.getOverlays().addAll(p2);
                    for(int i=0;i<p2.size();i++){
                        if(passed[0].equals(p2.get(i).getTitle())){
                            p2.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambio il colore dell'aula selezionata
                            p2.get(i).showInfoWindow();
                            b2.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b2.setTextColor(Color.WHITE);
                        }
                    }
                    break;
                case 3:
                    map.getOverlays().addAll(p3);
                    for(int i=0;i<p3.size();i++){
                        if(passed[0].equals(p3.get(i).getTitle())){
                            p3.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambio il colore dell'aula selezionata
                            p3.get(i).showInfoWindow();
                            b3.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b3.setTextColor(Color.WHITE);
                        }
                    }
                    break;
                case 4:
                    map.getOverlays().addAll(p4);
                    for(int i=0;i<p4.size();i++){
                        if(passed[0].equals(p4.get(i).getTitle())){
                            p4.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambio il colore dell'aula selezionata
                            p4.get(i).showInfoWindow();
                            b4.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b4.setTextColor(Color.WHITE);
                        }
                    }
                    break;
            }
            RotationGestureOverlay rg = new RotationGestureOverlay(map); //Oggetto necessario all'abilitazione dei gesti di rotazione all'interno della mappa
            rg.setEnabled(true); //Permetto la rotazione della mappa
            map.setMultiTouchControls(true); //Permetto lo zoom con le dita
            map.getOverlays().add(rg); //Aggiungo alla mappa l'oggetto che permette la rotazione
        }
        map.invalidate(); //Agggiorno la mappa in modo da vedere le modifiche
    }

    public void setInitialFloor(){
        //Imposto il piano 0 inizialmente
        map.getOverlays().addAll(p0);
        map.invalidate(); //Agggiorno la mappa in modo da vedere le modifiche
    }

    public void setInfoWindow(){
        for(int i=0;i<l.size();i++){

            CustomInfoWindow ci = new CustomInfoWindow(map,p.get(i)); //Creo un oggetto infowindow personalizzata
            p.get(i).setInfoWindow(ci); //imposto l'infowindow ad ogni poligono presente nella mappa
            int counter = i;//counter che serve al metodo sottostante
            p.get(i).setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    InfoWindow.closeAllInfoWindowsOn(map);//se viene cliccato un poligono diverso da quello attualmente attivo vengono chiuse tutte le infowindow attive
                    p.get(counter).showInfoWindow();//attiva l'infowindow del poligono selezionati
                    return true;
                }
            });

        }
    }


    public void addInArray(){
        //Aggiungo i nomi delle aule in un array di strighe da passare alle altre activity contenente l'aula e il piano

        int index = 0;
        for(int i=0;i<p.size();i++){
            if(p.get(i).getTitle() != null){
                if(p.get(i).getSubDescription().contains("level=0"))
                    pol[index] = p.get(i).getTitle()+";0";
                if(p.get(i).getSubDescription().contains("level=1"))
                    pol[index] = p.get(i).getTitle()+";1";
                if(p.get(i).getSubDescription().contains("level=2"))
                    pol[index] = p.get(i).getTitle()+";2";
                if(p.get(i).getSubDescription().contains("level=3"))
                    pol[index] = p.get(i).getTitle()+";3";
                if(p.get(i).getSubDescription().contains("level=4"))
                    pol[index] = p.get(i).getTitle()+";4";

                index++;
            }
        }
    }


    public void resetColor(){
        //Metodo che permette il reset dei colori dei poligoni a quelli di default in base al piano ricevuto
        if(passed!=null) {
            InfoWindow.closeAllInfoWindowsOn(map); //Chiudo tutte le infowindow aperte sulla mappa se il valore ricevuto dalle altre activity e nullo


            switch (Integer.parseInt(passed[1])) {
                case 0:
                    for (int i = 0; i < p0.size(); i++)
                        p0.get(i).getFillPaint().setColor(Color.parseColor("#FFF1C047"));//Cambiare i colori ai singoli poligoni
                    break;
                case 1:
                    for (int i = 0; i < p1.size(); i++)
                        p1.get(i).getFillPaint().setColor(Color.parseColor("#FFF1C047"));//Cambiare i colori ai singoli poligoni
                    break;
                case 2:
                    for (int i = 0; i < p2.size(); i++)
                        p2.get(i).getFillPaint().setColor(Color.parseColor("#FFF1C047"));//Cambiare i colori ai singoli poligoni
                    break;
                case 3:
                    for (int i = 0; i < p3.size(); i++)
                        p3.get(i).getFillPaint().setColor(Color.parseColor("#FFF1C047"));//Cambiare i colori ai singoli poligoni
                    break;
                case 4:
                    for (int i = 0; i < p4.size(); i++)
                        p4.get(i).getFillPaint().setColor(Color.parseColor("#FFF1C047"));//Cambiare i colori ai singoli poligoni
                    break;
            }
        }
    }


    //Metodo che imposta il colore dei bottoni a default
    private void setDefaultColor(Button b1,Button b2,Button b3,Button b4,Button b5){
        b1.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.yellow));
        b1.setTextColor(Color.parseColor("#104281"));
        b2.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.yellow));
        b2.setTextColor(Color.parseColor("#104281"));
        b3.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.yellow));
        b3.setTextColor(Color.parseColor("#104281"));
        b4.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.yellow));
        b4.setTextColor(Color.parseColor("#104281"));
        b5.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.yellow));
        b5.setTextColor(Color.parseColor("#104281"));

    }

    class myThread extends Thread{
        @Override
        public void run() {
            addInArray(); //Metodo che permette l'inserimento dei poligoni nell'array che verrà passato all'activity aule
            //Smisto gli elementi della lista principale, contenente tutti i poligoni, nelle liste dei vari piani
            for(int i = 0;i<p.size();i++){
                if(p.get(i).getSubDescription().contains("level=0"))
                    p0.add(p.get(i));
                if(p.get(i).getSubDescription().contains("level=1"))
                    p1.add(p.get(i));
                if(p.get(i).getSubDescription().contains("level=2"))
                    p2.add(p.get(i));
                if(p.get(i).getSubDescription().contains("level=3"))
                    p3.add(p.get(i));
                if(p.get(i).getSubDescription().contains("level=4"))
                    p4.add(p.get(i));
            }
            this.interrupt();
        }
    }







}