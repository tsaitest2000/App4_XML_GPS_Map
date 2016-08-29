package com.example.testgps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

   private Context mContext;
   private LocationManager mLocationMgr;
   private TextView mTextView1, mTextView2;
   private ServiceConnection mConnection;
   private ServiceGPS.MyBinder mMyBinder;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      mContext = MainActivity.this;
      mLocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      mTextView1 = (TextView) this.findViewById(R.id.textView1);
      mTextView2 = (TextView) this.findViewById(R.id.textView2);

      dealWithService();
      mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationLnr());
      mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new MyLocationLnr());
   }

   private void dealWithService() {
      mConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (ServiceGPS.MyBinder) service;
            mTextView2.setText(mMyBinder.toString());
         }

         @Override
         public void onServiceDisconnected(ComponentName name) {

         }
      };
      Intent intent = new Intent(mContext, ServiceGPS.class);
      MainActivity.this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   }

   private class MyLocationLnr implements LocationListener {
      @Override
      public void onLocationChanged(Location location) {
         double latitude = location.getLatitude();
         double longitude = location.getLongitude();
         double altitude = location.getAltitude();
         float accuracy = location.getAccuracy();
         String format = String.format("緯度:%f\n經度:%f\n高度:%f\n精度:%f", latitude, longitude, altitude, accuracy);
         mTextView1.setText(format);
         View container = new View(mContext) {
            @Override
            public void setTag(Object tag) {
               final String address = (String) tag;
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     mTextView1.setText(mTextView1.getText().toString() + "\n" + address);
                  }
               });
            }
         };
         if (mMyBinder != null) {
            mMyBinder.getAddress(location, container);
         }
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {

      }

      @Override
      public void onProviderEnabled(String provider) {

      }

      @Override
      public void onProviderDisabled(String provider) {

      }
   }


   public void onClick(View view) {
      String address = null;
      switch (view.getId()) {
         case R.id.button1:
            address = ((Button) view).getText().toString();
            break;
         case R.id.button2:
            address = ((Button) view).getText().toString();
            break;
         case R.id.button3:
            address = ((Button) view).getText().toString();
            break;
      }

      View container = new View(mContext) {
         @Override
         public void setTag(Object tag) {
            final String[] latLngArray = (String[]) tag;
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  mTextView2.setText(String.format("緯度:%s\n經度:%s", latLngArray[0], latLngArray[1]));
               }
            });
         }
      };
      mMyBinder.getLatLng(address, container);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      MainActivity.this.unbindService(mConnection);
   }

}
