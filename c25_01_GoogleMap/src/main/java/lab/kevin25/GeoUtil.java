package lab.kevin25;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GeoUtil {

   // 經緯度轉地址 ==================================================================================
   public static String getAddress(final double lat, final double lng) {
      String address = "";
      class RunAddress implements Callable<String> {
         String path = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=zh_tw";
         String json;
         OkHttpClient client = new OkHttpClient();

         RunAddress(double lat, double lng) {
            path = String.format(path, lat, lng);
         }

         String run(String url) throws IOException {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
         }

         @Override
         public String call() {
            String address = "";
            try {
               String json = run(path);
               address = new JSONObject(json).getJSONArray("results").getJSONObject(0).getString("formatted_address");
            } catch (Exception e) { e.printStackTrace(); }
            return address;
         }
      }

      FutureTask<String> task = new FutureTask<>(new RunAddress(lat, lng));
      new Thread(task).start();
      try {
         address = task.get();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return address;
   }

   // 地址轉經緯度 ==================================================================================
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
                  String lat = new JSONObject(json).getJSONArray("results").getJSONObject(0)
                     .getJSONObject("geometry").getJSONObject("location").getString("lat");
                  String lng = new JSONObject(json).getJSONArray("results").getJSONObject(0)
                     .getJSONObject("geometry").getJSONObject("location").getString("lng");
                  pos[0] = Double.parseDouble(lat);
                  pos[1] = Double.parseDouble(lng);
               } catch (JSONException e) { e.printStackTrace(); }
            } catch (IOException e) { e.printStackTrace(); }
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

   // 取得導航 JSON Path ============================================================================
   /*PPT 91頁中：範例網址為URL導航請求範例為 https://maps.googleapis.com/maps/api/directions/json?
   origin=24.989617221084362,121.31199035793541&destination=24.98272512844633,121.30355447530745&sensor=false*/
   /*Json結構：JsonObject / JsonArray(routes) / JsonObject(0) / JsonObject(overview_polyline) / JsonString(points)*/
   public static String getPoints(final LatLng latLng1, final LatLng latLng2) {

      String points = null;
      class GetPoints implements Callable<String> {

         String path = "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f";

         GetPoints() {
            path = String.format(path, latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude);
         }

         OkHttpClient client = new OkHttpClient();

         String run(String url) throws IOException {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
         }

         @Override
         public String call() throws Exception {
            String json = run(path);
            return new JSONObject(json).getJSONArray("routes").getJSONObject(0)
               .getJSONObject("overview_polyline").getString("points");
         }
      }

      FutureTask<String> task = new FutureTask<>(new GetPoints());
      new Thread(task).start();
      try {
         points = task.get();
      } catch (Exception e) {

      }
      return points;
   }

   // 解析路徑 ======================================================================================
   public static List<LatLng> decodePoly(String encoded) {

      List<LatLng> poly = new ArrayList<LatLng>();
      int index = 0, len = encoded.length();
      int lat = 0, lng = 0;

      while (index < len) {
         int b, shift = 0, result = 0;
         do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
         } while (b >= 0x20);
         int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
         lat += dlat;

         shift = 0;
         result = 0;
         do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
         } while (b >= 0x20);
         int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
         lng += dlng;
         LatLng p = new LatLng((((double) lat / 1E5)),
            (((double) lng / 1E5)));
         poly.add(p);
      }
      return poly;
   }

}
