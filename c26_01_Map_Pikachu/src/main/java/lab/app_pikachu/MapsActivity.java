package lab.app_pikachu;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Circle circle;
    private LatLng pikachu;
    private Marker pikachuMarker;
    private boolean pikachuFind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                drawCircle(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                pikachuFind = true;
                setTitle("pikachu find !");
                return false;
            }
        });

        LatLng latLng = new LatLng(25.041767, 121.550417);
        double lat = (25040000 + new Random().nextInt(9999)) / 1000000.0;
        double lng = (121550000 + new Random().nextInt(9999)) / 1000000.0;
        pikachu = new LatLng(lat, lng);
        //setTitle(pikachu.latitude +", "+pikachu.longitude);
        drawCircle(latLng);
    }

    private void drawCircle(LatLng latLng) {
        if(circle != null) circle.remove();
        CircleOptions options = new CircleOptions();
        options.center(latLng);
        options.strokeWidth(1f);
        options.radius(50);
        options.fillColor(Color.argb(150, 255, 0, 0));
        circle = mMap.addCircle(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        if(!pikachuFind) {
            float[] result = new float[1];
            Location.distanceBetween(latLng.latitude, latLng.longitude, pikachu.latitude, pikachu.longitude, result);
            setTitle(result[0] + " m");

            if (pikachuMarker != null) pikachuMarker.remove();
            if (result[0] < 200) {
                pikachuMarker = mMap.addMarker(new MarkerOptions().position(pikachu).icon(BitmapDescriptorFactory.fromResource(R.drawable.pikachu)));
            }
        }
    }

}
