package view.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import services.APIService;
import view.general.ExtendedActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

import com.bukalapakmobile.R;

public class MainActivity extends ExtendedActivity {
	ImageView iv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	      StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		iv = (ImageView) findViewById(R.id.imageView1);
	}
	
	@Override
	public void onServiceReady(APIService api) {
		// TODO Auto-generated method stub
		super.onServiceReady(api);
		final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request
        final HttpGet getRequest = new HttpGet("http://pplcs.ui.ac.id/product_img/9.png");
            HttpResponse response = null;
			try {
				response = client.execute(getRequest);
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + "http://pplcs.ui.ac.id/product_img/9.png");
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null)
				try {
					{
					    InputStream inputStream = null;
					        // getting contents from the stream
					        try {
								inputStream = entity.getContent();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					        // decoding stream data back into image Bitmap that android understands
					 final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					 iv.setImageBitmap(bitmap);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
