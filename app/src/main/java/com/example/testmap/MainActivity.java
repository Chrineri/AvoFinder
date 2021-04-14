package com.example.testmap;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

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
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private List l;
    private List<Polygon> p = null;
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

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTilesScaledToDpi(true);
        map.setMultiTouchControls(true);


        KmlDocument kmlDocument = new KmlDocument();


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



        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 5f, Color.CYAN);
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);

        l = geoJsonOverlay.getItems();


        for(int i=0;i<l.size();i++) {
            p = (List<Polygon>) l;
        }
        for(int i=0;i<p.size();i++) {

            if(p.get(i).getSubDescription().contains("level=0")) {
                p.get(i).setEnabled(true);
                map.invalidate();
            }else {
                p.get(i).setEnabled(false);
            }
        }


        Button invio = (Button) findViewById(R.id.l1);
        invio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for(int i=0;i<p.size();i++) {

                    if(p.get(i).getSubDescription().contains("level=1")) {
                        p.get(i).setEnabled(true);
                        map.invalidate();
                    }else {
                        p.get(i).setEnabled(false);
                    }
            /*if(p.get(i).getTitle().equals("4-13"))
                p.get(i).getFillPaint().setColor(Color.BLUE);//Cambiare i colori ai singoli poligoni
            else
                p.get(i).getFillPaint().setColor(Color.GREEN);*/
                }
            }
        });

        map.getOverlays().add(geoJsonOverlay);
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        IMapController mapController = map.getController();
        mapController.setZoom((double)18);
        GeoPoint startPoint = new GeoPoint(45.07122657916548,7.692658552021214);
        mapController.setCenter(startPoint);
        map.setMinZoomLevel((double) 18);
        map.setScrollableAreaLimitLatitude(45.07146656030419, 45.06952171642693,10);
        map.setScrollableAreaLimitLongitude(7.691488068154647,7.693766902971969,10);
        requestPermissionsIfNecessary(new String[] {

                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
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
}