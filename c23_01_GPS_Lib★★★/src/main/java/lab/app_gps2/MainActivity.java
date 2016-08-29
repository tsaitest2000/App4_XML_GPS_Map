package lab.app_gps2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

   private TextView mTextView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mTextView = (TextView) findViewById(R.id.textView);
   }

   public void onClick(View view) {
      switch (view.getId()) {
         case R.id.button1:
            String address = GeoUtil.getAddress(25.041708, 121.550422);
            mTextView.setText(address);
            break;
         case R.id.button2:
            double[] latLng = GeoUtil.getLatLng("日本名古屋");
            mTextView.setText("緯度:" + latLng[0] + "\n經度:" + latLng[1]);
            break;
      }
   }

}