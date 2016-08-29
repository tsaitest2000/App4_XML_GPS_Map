package lab.kevin24;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private GoogleMap mGoogleMap;
   private GroundOverlay mGroundOverlay; // 配合商業邏輯才設定成物件變數

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);
      SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
      mapFragment.getMapAsync(this);
   }

   @Override
   public void onMapReady(GoogleMap googleMap) {
      mGoogleMap = googleMap;
      mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
      mGoogleMap.setMyLocationEnabled(true); //★★ minSdkVersion=15, targetSdkVersion=19 ★★
      mGoogleMap.setBuildingsEnabled(true);
      UiSettings uiSettings = mGoogleMap.getUiSettings();
      uiSettings.setZoomControlsEnabled(true);

      //地圖預備完成時，到達預設的地點(東區認證中心)並設立旗標
      LatLng latLng = new LatLng(25.041708, 121.550422); //東區認證中心
      mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("預設地點"));
      mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17)); //指定放大倍率為17
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(0, 0, Menu.NONE, "日本大阪");
      menu.add(0, 1, Menu.NONE, "日月潭");
      menu.add(0, 2, Menu.NONE, "語音地點查詢");
      menu.add(0, 3, Menu.NONE, "YouBike");
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case 0:
         case 1:
            String address = item.getTitle().toString();
            double[] p = GeoUtil.getLatLng(address);
            LatLng latLng = new LatLng(p[0], p[1]);
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(address));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            // 動畫區域：移動到指定位置時，顯示如飛機起飛與降落的動畫 =====================================
//            CameraPosition cp = new CameraPosition.Builder().target(addr_position).tilt(67).bearing(300).zoom(17).build();
//            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
//            mGoogleMap.animateCamera(cu, 60000, null);
            break;
         case 2:
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //★★★★
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH); //★★★★
            startActivityForResult(intent, 101); //使用者說話後會Intent會帶回其語音資料
            break;
         case 3:
            new RunWork_YouBike().start();
            break;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 101) {
         ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //★★★★
         String address = results.get(0);
         double[] p = GeoUtil.getLatLng(address);
         LatLng latLng = new LatLng(p[0], p[1]);
         mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(address));
         mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
      }
   }

   class RunWork_YouBike extends Thread {

      String json;

      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      Runnable mRunnable = new Runnable() {
         @Override
         public void run() {
            mGoogleMap.clear(); //★清空畫面

            for (int i = 1; i <= 261; i++) {
               String idx = String.format("%04d", i); //0001, 0002, 0003 .....
               try { //原始網站資料中，"0260"這一筆不存在，所以，使用try區塊避掉不存在的筆數，否則程式會出錯
                  JSONObject jo = new JSONObject(json).getJSONObject("retVal").getJSONObject(idx);
                  final String lat = jo.getString("lat"); //ex:25.0408578889
                  String lng = jo.getString("lng"); //ex:121.567904444
                  String ar = jo.getString("ar"); //ex:忠孝東路/松仁路(東南側)

                  double tot = Double.parseDouble(jo.getString("tot")); //腳踏車總台數(YouBike定義)
                  double sbi = Double.parseDouble(jo.getString("sbi")); //腳踏車可用數(YouBike定義)
                  double value = sbi / tot; //腳踏車可使用比率(自行定義)

                  LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                  //變更標記顏色(自行定義的商業邏輯)
                  BitmapDescriptor flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                  if (value >= 0.75) { //可用腳踏車的比率>=0.75
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                  } else if (value >= 0.50) { //可用腳踏車的比率>=0.50
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                  } else if (value >= 0.25) { //可用腳踏車的比率>=0.25
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                  }

                  String message = String.format("%.1f", value * 100) + "%, " + sbi + "輛";
                  mGoogleMap.addMarker(new MarkerOptions().icon(flag).position(latLng).title(message));
                  mGoogleMap.setOnInfoWindowClickListener(new MyInfoWindowOnClickLnr()); // ★★★
                  mGoogleMap.setOnInfoWindowCloseListener(new MyInfoWindowCloseLnr()); // ★★★
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      };

      public void run() {
         try {
            json = run("http://data.taipei/youbike");
            runOnUiThread(mRunnable);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   // 點擊InfoWindow時，顯示其中一種： 1.新Activity顯示全景圖 2.在原Activity顯示縮略圖
   class MyInfoWindowOnClickLnr implements GoogleMap.OnInfoWindowClickListener {
      @Override
      public void onInfoWindowClick(Marker marker) {
         // String data1 = "google.streetview:cbll=%f,%f&mz=21"; // ★★★
         // String path1 = String.format(data1, marker.getPosition().latitude, marker.getPosition().longitude);
         // MapsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(path1)));

         final LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
         String data2 = "http://maps.googleapis.com/maps/api/streetview?size=640x480&location=%f,%f"; // ★★★
         String path2 = String.format(data2, latLng.latitude, latLng.longitude);
         MyTarget myTarget = new MyTarget(latLng); // ★★★
         Picasso.with(MapsActivity.this).load(path2).into(myTarget); // ★★★
      }
   }

   // ★★★★★★★★★★
   class MyTarget implements com.squareup.picasso.Target {

      private LatLng mLatLng;

      MyTarget(LatLng latLng) {
         this.mLatLng = latLng;
      }

      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
         // 縮略圖參數
         GroundOverlayOptions options = new GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromBitmap(bitmap))
            .anchor(0, 1)
            .position(mLatLng, 400f, 320f);
         // 縮略圖
         mGroundOverlay = mGoogleMap.addGroundOverlay(options);
         // 縮略圖透明度
         mGroundOverlay.setTransparency(0.2f);
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
   }

   // 點擊其他的InfoWindow或在空白處點擊時，刪除顯示在原本InfoWindow旁邊的縮略圖 → 商業邏輯
   private class MyInfoWindowCloseLnr implements GoogleMap.OnInfoWindowCloseListener {
      @Override
      public void onInfoWindowClose(Marker marker) {
         mGroundOverlay.remove();
      }
   }

}