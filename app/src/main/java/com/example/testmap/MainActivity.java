package com.example.testmap;
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

import android.os.StrictMode;
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

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


public class MainActivity extends AppCompatActivity{
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private List l;
    private List<Polygon> p = null;
    private List<Polygon> p0 = new ArrayList<>();
    private List<Polygon> p1 = new ArrayList<>();
    private List<Polygon> p2 = new ArrayList<>();
    private List<Polygon> p3 = new ArrayList<>();
    private List<Polygon> p4 = new ArrayList<>();
    private int piano = 0;
    List<Marker> markers = new ArrayList<>();
    Spinner s;
    String getClass="";
    CheckBox c;
    String[] pol;
    Polygon[] stu;
    String[] passed;
    Button b0;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    TextView tf;
    boolean controllo = false;  //controllo per onStop()
    TextView floor1;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onCreate(Bundle savedInstanceState) {
         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
         super.onCreate(savedInstanceState);

         Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

        }



        setContentView(R.layout.activity_main);



        System.out.println("Appena entrato in mappa: "+loadSp());
        s = findViewById(R.id.Spinner);
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





        map = (MapView) findViewById(R.id.map);  //Creazione dell'oggetto mapView
        map.setTileSource(TileSourceFactory.MAPNIK);  //Imposto il tipo di mappa
        map.setTilesScaledToDpi(true);
        RotationGestureOverlay rg = new RotationGestureOverlay(map);
        rg.setEnabled(true);
        map.setMultiTouchControls(true);  //Abilito lo zoom con le dita







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
        Style defaultStyle = new Style(defaultBitmap, Color.parseColor("#104281"), 5f, Color.parseColor("#f1c047"));
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);

        l = geoJsonOverlay.getItems(); //Prendo tutti gli oggetti che compongono la piantina scolastica
        c = (CheckBox) findViewById(R.id.check);

        //Metto ogni oggetto in un arraylist di polygoni
        p = (List<Polygon>) l;
        pol  = new String[p.size()];


        myThread t = new myThread();
        t.start();



        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                    switch (item.toString()) {
                        case "\uD83D\uDC64 Login    ":
                            s.setSelection(listsize);
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
                            i.putExtra("polygons", pol);
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


        setInfoWindow();

        getPassedValue();
        if(getIntent().hasExtra("class")){
        }else {
            setInitialFloor();
        }



        b0 = (Button) findViewById(R.id.l0);
        b1 = (Button) findViewById(R.id.l1);
        b2 = (Button) findViewById(R.id.l2);
        b3 = (Button) findViewById(R.id.l3);
        b4 = (Button) findViewById(R.id.l4);

        b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
        b0.setTextColor(Color.WHITE);

        tf = (TextView) findViewById(R.id.floor1);
        tf.setText(String.valueOf("P: "+piano));
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





        b0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(b0,b1,b2,b3,b4);
                resetColor();
                b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                b0.setTextColor(Color.WHITE);
                piano = 0;
                setFloor();
                rg.setEnabled(true);
                map.getOverlays().add(rg);

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
                resetColor();
                checkCheckbox();
            }
        });

        //map.getOverlays().add(geoJsonOverlay);

        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        IMapController mapController = map.getController();
        mapController.setZoom((double)18);
        GeoPoint startPoint = new GeoPoint(45.07122657916548,7.692658552021214);
        mapController.setCenter(startPoint);
        map.setMinZoomLevel((double) 18);
        map.setScrollableAreaLimitLatitude(45.07146656030419, 45.06952171642693,10);
        map.setScrollableAreaLimitLongitude(7.69138,7.693766902971969,10);
        map.getOverlays().add(rg);
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
            map.getOverlayManager().remove(markers.get(i));
        for (int i = 0; i < p.size(); i++) {
            if (p.get(i).getSubDescription().contains("level=" + piano)) {
                if (p.get(i).getTitle() != null) {
                    map.getOverlays().add(p.get(i));

                    Marker m = new Marker(map);
                    m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                    m.setTextLabelForegroundColor(Color.BLACK);
                    m.setTextIcon(p.get(i).getTitle());
                    m.setPosition(p.get(i).getInfoWindowLocation());
                    m.setInfoWindow(null);
                    map.getOverlayManager().add(m);
                    markers.add(m);
                }
            } else {
                map.getOverlays().remove(p.get(i));
            }
        }
        map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();

        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("class")){
        getClass = intent.getExtras().getString("class");
        c.setChecked(false);
        resetColor();
        getPassedValue();

        }
        else{
            c.setChecked(false);
            resetColor();
        }

    }


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
        map.getOverlays().clear();
        tf.setText(String.valueOf("P: "+piano));
        switch(piano){
            case 0: map.getOverlays().addAll(p0);break;
            case 1: map.getOverlays().addAll(p1);break;
            case 2: map.getOverlays().addAll(p2);break;
            case 3: map.getOverlays().addAll(p3);break;
            case 4: map.getOverlays().addAll(p4);break;
        }

        if(c.isChecked()){
            setMarker();
        }else{
            if(!InfoWindow.getOpenedInfoWindowsOn(map).isEmpty())
            InfoWindow.closeAllInfoWindowsOn(map);
        }
        map.invalidate();
    }

    public void checkCheckbox(){
            if(c.isChecked()){
                    InfoWindow.closeAllInfoWindowsOn(map);
                        for(int i=0;i<p.size();i++) {
                        if(p.get(i).getSubDescription().contains("level="+piano)) {
                            if(p.get(i).getTitle()!=null) {
                                map.getOverlays().add(p.get(i));

                                Marker m = new Marker(map);
                                m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                                m.setTextLabelForegroundColor(Color.BLACK);
                                m.setTextIcon(p.get(i).getTitle());
                                m.setPosition(p.get(i).getInfoWindowLocation());
                                m.setInfoWindow(null);
                                map.getOverlayManager().add(m);
                                markers.add(m);
                            }
                        }else {
                            map.getOverlays().remove(p.get(i));
                        }
                    }
                }else{
                    for(int i=0;i<markers.size();i++)
                        map.getOverlayManager().remove(markers.get(i));
                }

                map.invalidate();
        }




    public void getPassedValue(){

        if(getIntent().hasExtra("class")){
            getClass = getIntent().getExtras().getString("class");
        }

        //cambio il colore dei poligoni selezionati e passati dall'activity aule
        if(!getClass.equals("")){
            setDefaultColor(b0,b1,b2,b3,b4);
            map.getOverlays().clear();
            passed = getClass.split(";");
            piano = Integer.parseInt(passed[1]);
            tf.setText("P: "+piano);
            switch(Integer.parseInt(passed[1])){
                case 0:

                    map.getOverlays().addAll(p0);
                    for(int i=0;i<p0.size();i++){
                        if(passed[0].equals(p0.get(i).getTitle())){
                            p0.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambiare i colori ai singoli poligoni
                            p0.get(i).showInfoWindow();
                            b0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b0.setTextColor(Color.WHITE);
                        }
                    }
                    break;
                case 1:
                    map.getOverlays().addAll(p1);
                    for(int i=0;i<p1.size();i++){
                        if(passed[0].equals(p1.get(i).getTitle())){
                            p1.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambiare i colori ai singoli poligoni
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
                            p2.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambiare i colori ai singoli poligoni
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
                            p3.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambiare i colori ai singoli poligoni
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
                            p4.get(i).getFillPaint().setColor(Color.parseColor("#f17f46"));//Cambiare i colori ai singoli poligoni
                            p4.get(i).showInfoWindow();
                            b4.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                            b4.setTextColor(Color.WHITE);
                        }
                    }
                    break;
            }
            RotationGestureOverlay rg = new RotationGestureOverlay(map);
            rg.setEnabled(true);
            map.setMultiTouchControls(true);
            map.getOverlays().add(rg);
        }
        map.invalidate();
    }

    public void setInitialFloor(){
        //Imposto il piano 0 inizialmente
        map.getOverlays().addAll(p0);
        map.invalidate();
    }

    public void setInfoWindow(){
        for(int i=0;i<l.size();i++){

            CustomInfoWindow ci = new CustomInfoWindow(map,p.get(i));
            p.get(i).setInfoWindow(ci);
            int counter = i;//counter che serve al metodo sottostante
            p.get(i).setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    InfoWindow.closeAllInfoWindowsOn(map);//se viene cliccato un poligono diverso da quello attualmente attivo vengono chiuse tutte le infowindow attive
                    p.get(counter).showInfoWindow();//attiva l'infowindow del poligono selezionato
                    return true;
                }
            });

        }
    }


    public void addInArray(){
        //Aggiungo i nomi delle aule in un array di strighe da passare alle altre activity

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

        if(passed!=null) {
            InfoWindow.closeAllInfoWindowsOn(map);


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
            addInArray();
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