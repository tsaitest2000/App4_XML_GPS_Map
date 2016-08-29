package lab.app_gps2;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GeoUtil {

   // 地址轉經緯度
   public static String getAddress(final double latitude, final double longitude) {

      String address = "";

      class RunAddress implements Callable<String> { // ★★★★★ 同步執行緒 ★★★★★

         String mPath = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=zh_tw";
         String json;

         RunAddress(double latitude, double longitude) {
            mPath = String.format(mPath, latitude, longitude);
         }

         OkHttpClient client = new OkHttpClient();

         String run(String url) throws IOException {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
         }

         @Override
         public String call() {
            String address = "";
            try {
               String json = run(mPath);
               address = new JSONObject(json).getJSONArray("results").getJSONObject(0).getString("formatted_address");
            } catch (Exception e) {
               e.printStackTrace();
            }
            return address;
         }
      }

      FutureTask<String> task = new FutureTask<>(new RunAddress(latitude, longitude));
      new Thread(task).start();
      try {
         address = task.get();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return address;
   }

   // 經緯度轉地址
   public static double[] getLatLng(final String address) {

      double[] pos = null;
      class RunLatLng implements Callable<double[]> {
         String address;
         String json;
         String path = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&language=zh_tw";
         double[] pos = new double[2];

         RunLatLng(String address) {
            path = String.format(path, URLEncoder.encode(address));
         }

         OkHttpClient client = new OkHttpClient();

         String run(String url) throws IOException {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
         }

         @Override
         public double[] call() {
            try {
               String json = run(path);
               try {
                  String lat = new JSONObject(json).getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
                  String lng = new JSONObject(json).getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");
                  pos[0] = Double.parseDouble(lat);
                  pos[1] = Double.parseDouble(lng);
               } catch (JSONException e) {
                  e.printStackTrace();
               }
            } catch (IOException e) {
               e.printStackTrace();
            }
            return pos;
         }
      }

      FutureTask<double[]> task = new FutureTask<>(new RunLatLng(address));
      new Thread(task).start();
      try {
         pos = task.get();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return pos;
   }

}
