package lab.kevin23;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private GoogleMap mMap;

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
      mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
      mMap.setMyLocationEnabled(true);
      mMap.setBuildingsEnabled(true);
      UiSettings ui = mMap.getUiSettings();
      ui.setZoomControlsEnabled(true);

      // Add a marker in Sydney and move the camera
      LatLng pcschool = new LatLng(25.041708, 121.550422);
      mMap.addMarker(new MarkerOptions().position(pcschool).title("Hello"));
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pcschool, 17));
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(0, 0, 0, "紐約世貿");
      menu.add(0, 1, 0, "日月潭");
      menu.add(0, 2, 0, "語音查詢");

      return super.onCreateOptionsMenu(menu);
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case 0:
         case 1:
            String addr = item.getTitle().toString();
            double[] p = GeoUtil.getPos(addr);
            LatLng addr_position = new LatLng(p[0], p[1]);
            mMap.addMarker(new MarkerOptions().position(addr_position).title(addr));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addr_position, 17));
//            CameraPosition cp = new CameraPosition.Builder().target(addr_position).tilt(67).bearing(300).zoom(17).zoom(17).build();
//            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
//            mMap.animateCamera(cu, 60000, null);

            break;
         case 2:
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            startActivityForResult(intent, 101);
            break;

      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 101) {
         ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
         String addr = results.get(0);
         double[] p = GeoUtil.getPos(addr);
         LatLng addr_position = new LatLng(p[0], p[1]);
         mMap.addMarker(new MarkerOptions().position(addr_position).title(addr));
         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addr_position, 17));
      }

   }

}
