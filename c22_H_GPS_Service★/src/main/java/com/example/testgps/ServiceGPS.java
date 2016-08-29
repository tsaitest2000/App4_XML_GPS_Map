package com.example.testgps;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ServiceGPS extends Service {

   public ServiceGPS() {

   }

   @Override
   public void onCreate() {
      super.onCreate();
   }

   @Override
   public IBinder onBind(Intent intent) {
      return new MyBinder();
   }

   public class MyBinder extends Binder {

      public void getAddress(Location location, View container) {
         new RunWork_GetAddress(location, container).start();
      }

      public void getLatLng(String address, View container) {
         new RunWork_GetLatLng(address, container).start();
      }
   }

   @Override
   public boolean onUnbind(Intent intent) {
      return super.onUnbind(intent);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
   }

   private class RunWork_GetAddress extends Thread {

      private Location mLocation;
      private View mContainer;
      private String mStrJson;

      public RunWork_GetAddress(Location location, View container) {
         this.mLocation = location;
         this.mContainer = container;
      }

      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      @Override
      public void run() {
         String data = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=zh_tw";
         String path = String.format(data, mLocation.getLatitude(), mLocation.getLongitude());
         try {
            mStrJson = run(path);
            String add = new JSONObject(mStrJson).getJSONArray("results").getJSONObject(0).getString("formatted_address");
            mContainer.setTag(add);
         } catch (IOException e) {
            e.printStackTrace();
         } catch (JSONException e) {
            e.printStackTrace();
         }
      }
   }

   public class RunWork_GetLatLng extends Thread {

      private String mAddress;
      private View mContainer;
      private String strJson;

      public RunWork_GetLatLng(String address, View container) {
         this.mAddress = address;
         this.mContainer = container;
      }

      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      @Override
      public void run() {
         String data = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&language=zh_tw";
         String path = String.format(data, mAddress);
         try {
            strJson = run(path);
            String[] mLatLng = new String[2];
            mLatLng[0] = new JSONObject(strJson).getJSONArray("results").getJSONObject(0)
               .getJSONObject("geometry").getJSONObject("location").getString("lat");
            mLatLng[1] = new JSONObject(strJson).getJSONArray("results").getJSONObject(0)
               .getJSONObject("geometry").getJSONObject("location").getString("lng");
            mContainer.setTag(mLatLng);
         } catch (IOException e) {
            e.printStackTrace();
         } catch (JSONException e) {
            e.printStackTrace();
         }
      }
   }

}

