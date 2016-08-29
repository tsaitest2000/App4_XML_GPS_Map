package lab.app_gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

   private Context mContext;
   private LocationManager mLocationMgr;
   private Button mButton1;
   private Button mButton2;
   private TextView mTextView1;
   private TextView mTextView2;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mContext = MainActivity.this;
      mLocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      mTextView1 = (TextView) this.findViewById(R.id.textView1);
      mButton1 = (Button) this.findViewById(R.id.button1);
      mButton2 = (Button) this.findViewById(R.id.button2);
      mTextView2 = (TextView) this.findViewById(R.id.textView2);

      // ★★ 室外或可以接收衛星訊號的地方 LocationManager.GPS_PROVIDER ★★
      mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationLnr());
      // ★★ 室內或無法接收衛星訊號的地方 LocationManager.NETWORK_PROVIDER ★★
      mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new MyLocationLnr());

      mButton1.setOnClickListener(new btnOnClickLnr());
      mButton2.setOnClickListener(new btnOnClickLnr());
   }

   class MyLocationLnr implements LocationListener {
      @Override
      public void onLocationChanged(Location loc) {
         String data = "緯度=%f\n經度=%f\n標高=%f\n精確度=%f\n";
         data = String.format(data, loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getAccuracy());
         mTextView1.setText(data);
         // ★ 監聽器監聽使用者在地圖上的點按 當位置發生變化時將該地點的Location物件做為參數傳給執行緒 ★
         new RunWork_Address(loc).start();
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

   // ★★ 接到由監聽器LocationListener傳送過來的Location物件 拆解為Latitude與Longitude → 到網頁中取得地址 並顯示 ★★
   class RunWork_Address extends Thread {

      Location location;
      String path = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=zh_tw"; // ★★
      String json;

      RunWork_Address(Location location) {
         this.location = location;
         path = String.format(path, location.getLatitude(), location.getLongitude());
      }

      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      Runnable mRunnable = new Runnable() {
         @Override
         public void run() {
            try {
               String address = new JSONObject(json).getJSONArray("results").getJSONObject(0).getString("formatted_address");
               mTextView1.setText(mTextView1.getText() + address + "\n");
            } catch (JSONException e) {
               e.printStackTrace();
            }
         }
      };

      @Override
      public void run() {
         try {
            json = run(path);
            runOnUiThread(mRunnable);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private class btnOnClickLnr implements View.OnClickListener {
      @Override
      public void onClick(View view) {
         String address = null;
         switch (view.getId()) {
            case R.id.button1:
               address = mButton1.getText().toString();
               break;
            case R.id.button2:
               address = mButton2.getText().toString();
               break;
         }
         // Button被按下時取得它的文字(如：台北車站, 日本名古屋) 做為參數並傳給執行緒
         new RunWork_LatLng(address).start();
      }
   }

   // ★★ 接到Button元件傳送過來的地址文字 → 到網頁中取得經度與緯度 並顯示 ★★
   class RunWork_LatLng extends Thread {

      String address;
      String json;
      String path = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&language=zh_tw"; // ★★

      RunWork_LatLng(String address) {
         this.address = address;
         path = String.format(path, URLEncoder.encode(address)); // ★★ 編碼地址以避免發生錯誤
      }

      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      Runnable mRunnable = new Runnable() {
         @Override
         public void run() {
            try {
               String lat = new JSONObject(json).getJSONArray("results").getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location").getString("lat");
               String lng = new JSONObject(json).getJSONArray("results").getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location").getString("lng");
               mTextView2.setText(String.format("經度：%s\n緯度：%s", lat, lng));
            } catch (JSONException e) {
               e.printStackTrace();
            }
         }
      };

      @Override
      public void run() {
         try {
            json = run(path);
            runOnUiThread(mRunnable);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

}
