package lab.kevin25;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private Context mContext;
   private GoogleMap mGoogleMap;
   // ★★商業邏輯A01：每點擊一個InfoWindow就產生一個縮略圖 並且被加入到List型別物件中，方更一次性刪除★★
   private List<GroundOverlay> mListOverlay = new ArrayList<>();
   // ★★商業邏輯A02：每點擊一次GoogleMap 就會產生一個Marker 並且被加入到List型別物件中★★
   private List<Marker> mListMarker = new ArrayList<>();
   private int mCount = 2;

   @Override // 新增模組時選擇"Google Maps Activity"後 onCreate方法中會自動處理Fragment的程式語法
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);
      SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
      mapFragment.getMapAsync(this);
      mContext = this;
   }

   @Override // 當GoogleMap載入完成所要執行的工作 ====================================================
   public void onMapReady(GoogleMap googleMap) {
      mGoogleMap = googleMap;
      mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
      mGoogleMap.setMyLocationEnabled(true);
      mGoogleMap.setBuildingsEnabled(true);
      UiSettings uiSettings = mGoogleMap.getUiSettings();
      uiSettings.setZoomControlsEnabled(true);

      LatLng latLng = new LatLng(25.041708, 121.550422);
      //mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Hello"));
      mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

      // 進階第05堂，議題：GoogleMap的onClick
      mGoogleMap.setOnMapClickListener(new MyMapOnClickLnr());
      mGoogleMap.setOnInfoWindowClickListener(new MyInfoWindowClickLnr());
   }

   @Override // 創建OptionsMenu ====================================================================
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(0, 0, 0, "台南車站");
      menu.add(0, 1, 0, "日月潭");
      menu.add(0, 2, 0, "語音查詢");
      menu.add(0, 3, 0, "YouBike");
      menu.add(0, 4, 0, "清除縮略圖");
      return super.onCreateOptionsMenu(menu);
   }

   @Override // 當OptionsMenu的item被按下時所要執行的工作 ============================================
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case 0:
         case 1:
            String address = item.getTitle().toString();
            double[] p = GeoUtil.getLatLng(address);
            LatLng latLng = new LatLng(p[0], p[1]);

            // 傳統作法：移往OptionsItem文字所代表的地點 1.在該地點加上Marker物件 2.相機鏡頭移往該處
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(address));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            // 動畫做法：移往該地點時顯示動畫效果
            /*
            CameraPosition cp = new CameraPosition.Builder().target(latLng).tilt(67).bearing(300).zoom(17).build();
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
            mGoogleMap.animateCamera(cu, 1000, null);
            */
            break;
         case 2: // 語音地點查詢
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            startActivityForResult(intent, 101);
            break;
         case 3: // 顯示雙北市的YouBike地點
            new RunWork_YouBike().start();
            break;
         case 4: // ★★ 商業邏輯A01：一次性刪除所有產生的縮略圖 ★★
            for (GroundOverlay overlay : mListOverlay) {
               overlay.remove();
            }
            break;
      }
      return super.onOptionsItemSelected(item);
   }


   @Override // 語音查詢完成時，所要執行的工作 ========================================================
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
         // ★★ 容易忘記的程式語法 ★★
         ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
         String address = results.get(0);
         double[] p = GeoUtil.getLatLng(address);
         LatLng latLng = new LatLng(p[0], p[1]);
         mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(address));
         mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
      }
   }

   // YouBike專用的執行緒 ===========================================================================
   class RunWork_YouBike extends Thread {

      String json;
      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      public void run() {
         try {
            json = run("http://data.taipei/youbike");
            runOnUiThread(runnable);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.041708, 121.550422), 13));
            mGoogleMap.clear();

            try {
               JSONObject jsonObject = new JSONObject(json).getJSONObject("retVal");
               int jsonObjectLength = jsonObject.length();

               for (int i = 1; i <= jsonObjectLength; i++) { // http://data.taipei/youbike的資料筆數
                  String index = String.format("%04d", i); // ★★ 格式如：0001 0002 0003 ...

                  JSONObject detailObject = jsonObject.getJSONObject(index);
                  int tot=detailObject.getInt("tot"); // 場站總停車格
                  int sbi = detailObject.getInt("sbi"); // 場站目前車輛數量
                  double lat = detailObject.getDouble("lat"); // 經度
                  double lng = detailObject.getDouble("lng"); // 緯度
                  String ar = detailObject.getString("ar"); // 地址(中文)
                  double available = (double) sbi / (double) tot; // 腳踏車可用比率值

                  // ★★ 設定預設的MarkerDescriptor的顏色為紅色
                  BitmapDescriptor flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

                  if (available >= 0.75) {
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                  } else if (available >= 0.5) {
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                  } else if (available >= 0.25) {
                     flag = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                  }

                  LatLng latLng = new LatLng(lat, lng);
                  String message = String.format("%.1f", available * 100) + "%, " + sbi + "輛";
                  mGoogleMap.addMarker(new MarkerOptions().icon(flag).position(latLng).title(message)); //★ icon語法
                  // 所有腳踏車的Marker物件被點擊時會出現InfoWindow → 設定被點擊後要執行的操作
                  // mGoogleMap.setOnInfoWindowClickListener(new MyInfoWindowClickLnr()); // 原本在這 我搬到onMapReady()
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }
         }
      };
   }

   // GoogleMap被按下時所要執行的工作 ================================================================
   private class MyMapOnClickLnr implements GoogleMap.OnMapClickListener {
      @Override
      public void onMapClick(LatLng latLng) {
         //★規劃邏輯：若在地圖上點擊第三個點，則會清空地圖上所有的Marker。每點擊二點就自動繪製直線或導航線★
         if (mListMarker.size() >= mCount) {
            mGoogleMap.clear();
            mListMarker.clear();
            MapsActivity.this.setTitle("Map");
         }
         Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("test"));
         mListMarker.add(marker);
         if (mListMarker.size() == mCount) {
            drawLine(); // ★★ 議題：GoogleMap上兩點間畫線
            navigate(); // ★★ 議題：GoogleMap上繪製導航線
         }
      }
   }

   // 進階第05堂，議題：地圖上兩點間畫線 ==============================================================
   private void drawLine() {
      PolylineOptions options = new PolylineOptions();
      for (int i = 0; i < mCount; i++) {
         options.add(mListMarker.get(i).getPosition());
      }
      options.width(5);
      options.color(Color.RED);
      options.zIndex(1);
      mGoogleMap.addPolyline(options);

      float[] results = new float[1]; //計算兩點間距，講義3.3節，API通則為float[] → 結果可能不止一項
      Location.distanceBetween(
         mListMarker.get(0).getPosition().latitude,
         mListMarker.get(0).getPosition().longitude,
         mListMarker.get(1).getPosition().latitude,
         mListMarker.get(1).getPosition().longitude,
         results
      );
      MapsActivity.this.setTitle(String.valueOf((int) results[0]) + "公尺");
   }

   // 進階第05堂，議題：地圖上繪製導航線 ==============================================================
   private void navigate() {
      if (mListMarker.size() == mCount) {
         // ★★★ GeoUtil.getPoints() "points"：URL請求範例網址中jsonString("points")：所有點的字串編碼
         String points = GeoUtil.getPoints(mListMarker.get(0).getPosition(), mListMarker.get(1).getPosition());
         List<LatLng> list = GeoUtil.decodePoly(points);
         setTitle(list.size() + ", " + points);
         PolylineOptions options = new PolylineOptions();
         for (LatLng latLng : list) {
            options.add(latLng);
         }
         options.width(5);
         options.color(Color.MAGENTA);
         options.zIndex(1);
         mGoogleMap.addPolyline(options);
      }
   }

   // GoogleMap中InfoWindow被按下時所要執行的工作 ====================================================
   private class MyInfoWindowClickLnr implements GoogleMap.OnInfoWindowClickListener {
      @Override
      public void onInfoWindowClick(Marker marker) {
         // ★ 方案一：在新Activity中顯示全景視圖 ★
         String data1 = "google.streetview:cbll=%f,%f&mz=21"; // ★★
         String path1 = String.format(data1, marker.getPosition().latitude, marker.getPosition().longitude);
         MapsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(path1)));

         // ★ 方案二：在原Activity中顯示小縮略圖 ★
         String data2 = "http://maps.googleapis.com/maps/api/streetview?size=640x480&location=%f,%f"; // ★★
         String path2 = String.format(data2, marker.getPosition().latitude, marker.getPosition().longitude);
         Picasso.with(mContext).load(path2).into(new MyTarget(marker.getPosition())); // ★★★
      }
   }

   // ★Picasso的into方法的參數★ ===================================================================
   private class MyTarget implements Target {

      private LatLng mLatLng;

      public MyTarget(LatLng latLng) {
         this.mLatLng = latLng;
      }

      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
         // 縮略圖參數
         GroundOverlayOptions options = new GroundOverlayOptions();
         options.image(BitmapDescriptorFactory.fromBitmap(bitmap));
         options.anchor(0, 1);
         options.position(mLatLng, 400f, 200f);

         // 縮略圖
         GroundOverlay overlay = mGoogleMap.addGroundOverlay(options);
         // 縮略圖透明度
         overlay.setTransparency(0.3f);
         // ★★商業邏輯A01：存入縮略圖，方便一次性刪除所有產生的縮略圖★★
         mListOverlay.add(overlay);
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
   }

}
