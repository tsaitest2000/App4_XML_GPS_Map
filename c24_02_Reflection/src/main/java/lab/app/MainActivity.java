package lab.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView textView;
    private ImageView imageView;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        textView = $(R.id.textView);
        imageView = $(R.id.imageView);
        new RunWork().start();
    }

    private <T extends View>T $(int resId) {
        return (T)super.findViewById(resId);
    }

    class RunWork extends Thread {

        String xml;

        OkHttpClient client = new OkHttpClient();

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder=factory.newDocumentBuilder();
                    Document document=builder.parse(is);
                    Element root = document.getDocumentElement();

                    String reportContent = root.getElementsByTagName("reportContent").item(0).getTextContent();
                    String magnitudeValue = root.getElementsByTagName("magnitudeValue").item(0).getTextContent();

                    NodeList shakingAreas = root.getElementsByTagName("shakingArea");

                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<shakingAreas.getLength();i++) {
                        Element element = (Element)shakingAreas.item(i);
                        String areaName = element.getElementsByTagName("areaName").item(0).getTextContent();
                        String areaIntensity = element.getElementsByTagName("areaIntensity").item(0).getTextContent();
                        String unit = element.getElementsByTagName("areaIntensity").item(0).getAttributes().getNamedItem("unit").getTextContent();
                        sb.append(areaName + " " + areaIntensity + unit).append("\n");
                    }

                    textView.setText(reportContent + "\n\n" + magnitudeValue + "\n\n" + sb);

                    String shakemapImageURI = root.getElementsByTagName("shakemapImageURI").item(0).getTextContent();
                    Picasso.with(context).load(shakemapImageURI).into(imageView);

                } catch(Exception e) {

                }
            }
        };

        @Override
        public void run() {

            try {
                xml = run("http://opendata.cwb.gov.tw/govdownload?dataid=E-A0015-001R&authorizationkey=rdec-key-123-45678-011121314");
                runOnUiThread(r);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
