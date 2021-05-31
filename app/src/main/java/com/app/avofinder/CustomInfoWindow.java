package com.app.avofinder;

import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfoWindow extends MarkerInfoWindow {
    //Infowindow personalizzata
    Polygon p;

    public CustomInfoWindow(MapView mapView, Polygon p) {
        super(R.layout.my_bubble, mapView);
        this.p = p;
    }


    @Override
    public void onOpen(Object item) {


        TextView title = (TextView) mView.findViewById(R.id.bubble_title);
        title.setText(p.getTitle());





    }
}
