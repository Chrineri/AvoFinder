package com.example.testmap;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
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
import org.osmdroid.views.overlay.gestures.RotationGestureDetector;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private List l;
    private List<Polygon> p = null;
    private int piano = 0;
    List<Marker> markers = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        map = (MapView) findViewById(R.id.map);  //Creazione dell'oggetto mapView
        map.setTileSource(TileSourceFactory.MAPNIK);  //Imposto il tipo di mappa
        map.setTilesScaledToDpi(true);
        RotationGestureOverlay rg = new RotationGestureOverlay(map);
        rg.setEnabled(true);
        map.setMultiTouchControls(true);  //Abilito lo zoom con le dita
        map.getOverlays().add(rg);


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
        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, Color.parseColor("#104281"), 5f, Color.parseColor("#f1c047"));
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);

        l = geoJsonOverlay.getItems(); //Prendo tutti gli oggetti che compongono la piantina scolastica
        CheckBox c = (CheckBox) findViewById(R.id.check);

        //Metto ogni oggetto in un arraylist di polygoni
        p = (List<Polygon>) l;


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


        //Imposto il piano 0 inizialmente
        for(int i=0;i<p.size();i++) {
            if(p.get(i).getSubDescription().contains("level=0"))
                map.getOverlays().add(p.get(i));
        }


        Button p0 = (Button) findViewById(R.id.l0);
        Button p1 = (Button) findViewById(R.id.l1);
        Button p2 = (Button) findViewById(R.id.l2);
        Button p3 = (Button) findViewById(R.id.l3);
        Button p4 = (Button) findViewById(R.id.l4);

        p0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
        p0.setTextColor(Color.WHITE);

        


        p0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(p0,p1,p2,p3,p4);
                p0.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                p0.setTextColor(Color.WHITE);
                piano = 0;



                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=0")) {
                        map.getOverlays().add(p.get(i));
                        map.invalidate();
                    }else {
                        map.getOverlays().remove(p.get(i));
                        map.invalidate();
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }


                if(c.isChecked()){
                    setMarker();
                }else{
                    InfoWindow.closeAllInfoWindowsOn(map);
                }
            }
        });


        p1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                setDefaultColor(p0,p1,p2,p3,p4);
                p1.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                p1.setTextColor(Color.WHITE);
                piano = 1;


                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=1")) {
                        map.getOverlays().add(p.get(i));
                        map.invalidate();
                    }else {
                        map.getOverlays().remove(p.get(i));
                        map.invalidate();
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }

                if(c.isChecked()){
                    setMarker();
                }else{
                    InfoWindow.closeAllInfoWindowsOn(map);
                }
            }
        });

        p2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(p0,p1,p2,p3,p4);
                p2.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                p2.setTextColor(Color.WHITE);
                piano = 2;

                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=2")) {
                        map.getOverlays().add(p.get(i));
                        map.invalidate();
                    }else {
                        map.getOverlays().remove(p.get(i));
                        map.invalidate();
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }


                if(c.isChecked()){
                    setMarker();
                }else{
                    InfoWindow.closeAllInfoWindowsOn(map);
                }
            }
        });

        p3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(p0,p1,p2,p3,p4);
                p3.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                p3.setTextColor(Color.WHITE);
                piano = 3;


                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=3")) {
                        map.getOverlays().add(p.get(i));
                        map.invalidate();
                    }else {
                        map.getOverlays().remove(p.get(i));
                        map.invalidate();
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }

                if(c.isChecked()){
                    setMarker();
                }else{
                    InfoWindow.closeAllInfoWindowsOn(map);
                }
            }
        });

        p4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setDefaultColor(p0,p1,p2,p3,p4);
                p4.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.gray));
                p4.setTextColor(Color.WHITE);
                piano = 4;



                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=4")) {
                        map.getOverlays().add(p.get(i));
                        map.invalidate();
                    }else {
                        map.getOverlays().remove(p.get(i));
                        map.invalidate();
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }

                if(c.isChecked()){
                    setMarker();
                }else{
                    InfoWindow.closeAllInfoWindowsOn(map);
                }
            }
        });


        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

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
                                map.invalidate();
                            }
                        }else {
                            map.getOverlays().remove(p.get(i));
                            map.invalidate();
                        }
                    }
                }else{
                    for(int i=0;i<markers.size();i++)
                        map.getOverlayManager().remove(markers.get(i));
                        map.invalidate();
                }

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
        requestPermissionsIfNecessary(new String[] {

                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
        map.invalidate();

    }

    public void setMarker() {
        for(int i=0;i<markers.size();i++)
            map.getOverlayManager().remove(markers.get(i));
            map.invalidate();
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
                    map.invalidate();
                }
            } else {
                map.getOverlays().remove(p.get(i));
                map.invalidate();
            }
        }
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
}