package lab.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

   private Context mContext;
   private TextView mTextView;
   private ImageView mImageView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mContext = this;
      mTextView = (TextView) findViewById(R.id.textView);
      mImageView = (ImageView) findViewById(R.id.imageView);

      new RunWork().start();
   }

   class RunWork extends Thread {

      String strXML;
      OkHttpClient client = new OkHttpClient();

      String run(String url) throws IOException {
         Request request = new Request.Builder().url(url).build();
         Response response = client.newCall(request).execute();
         return response.body().string();
      }

      Runnable r = new Runnable() {
         @Override
         public void run() {
            try {
               /** 以下為制式的寫法 ************************************************************************/
               InputStream inputStream = new ByteArrayInputStream(strXML.getBytes(StandardCharsets.UTF_8));
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document document = builder.parse(inputStream);
               Element root = document.getDocumentElement(); // 用Toast顯示root.getTagName()會得到根元素CwbOpenData
               // 零代表最新的一筆
               String reportContent = root.getElementsByTagName("reportContent").item(0).getTextContent();
               String magnitudeValue = root.getElementsByTagName("magnitudeValue").item(0).getTextContent();
               NodeList shakingAreas = root.getElementsByTagName("shakingArea");
               /** 以上為制式的寫法 ************************************************************************/

               /** 好用的招式 *****************************************************************************/
               StringBuilder sb = new StringBuilder();
               for (int i = 0; i < shakingAreas.getLength(); i++) {
                  Element element = (Element) shakingAreas.item(i);
                  String areaName = element.getElementsByTagName("areaName").item(0).getTextContent();
                  String areaIntensity = element.getElementsByTagName("areaIntensity").item(0).getTextContent();
                  String unit = element.getElementsByTagName("areaIntensity").item(0).getAttributes()
                     .getNamedItem("unit").getTextContent();
                  sb.append(areaName + " " + areaIntensity + unit).append("\n");
               } //getElementsByTagName是Element才有的方法
               /** 好用的招式 *****************************************************************************/

               mTextView.setText(reportContent + "\n\n" + magnitudeValue + "\n\n" + sb);
               String shakemapImageURI = root.getElementsByTagName("shakemapImageURI").item(0).getTextContent();
               Picasso.with(mContext).load(shakemapImageURI).into(mImageView);
            } catch (Exception e) {

            }
         }
      };

      @Override
      public void run() {

         try {
            strXML = run("http://opendata.cwb.gov.tw/govdownload?" +
               "dataid=E-A0015-001R&authorizationkey=rdec-key-123-45678-011121314");
            runOnUiThread(r);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

}
