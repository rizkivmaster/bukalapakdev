package view.home;



import services.APIService;
import view.general.ExtendedActivity;
import view.product.list.ListProductActivity;
import view.product.upload.UploadProductActivity;
import view.user.login.LoginActivity;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bukalapakmobile.R;

public class Dashboard extends ExtendedActivity{
	APIService api;
    // Dashboard News feed button
    Button btn_upload ;    
    // Dashboard Friends button
    Button btn_mylapak;
    // Dashboard Photos button
    Button btn_logout;
    
    TextView UserFooter;

     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);
        
        // Dashboard News feed button
        btn_upload = (Button) findViewById(R.id.btn_upload);
        
        // Dashboard Friends button
        btn_mylapak = (Button) findViewById(R.id.btn_mylapak);        
        // Dashboard Photos button
        btn_logout = (Button) findViewById(R.id.footer_logout);
        
        UserFooter = (TextView) findViewById(R.id.footer_text);

        
        
        /**
         * Creating all buttons instances
         * */
        

			String name = "halo";
	      
        
        /**
         * Handling all button click events
         * */
        
        UserFooter.setText("Hi " + name);
        
        // Listening to News Feed button click
        btn_logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				//Intent i = new Intent(getApplicationContext(),
	            //        AddRestaurant.class);
	            //startActivity(i);
	        	Button button = (Button)view;
	        	button.setTextColor(Color.rgb(0, 10, 55));
	        	//button.setBackgroundColor(Color.rgb(255, 255, 255));
	        	Toast.makeText(Dashboard.this, "Log Out is selected", Toast.LENGTH_SHORT).show();
	        	// Clear the session data
                // This will clear all session data and 
                // redirect user to MainActivity
	        	api.removeRecentAccess();
	        	startActivity(new Intent(Dashboard.this,LoginActivity.class));
                finish();
	            //PenggunaController.logoutUser(getApplicationContext());
	            //Intent login = new Intent(getApplicationContext(), MainActivity.class);
				//login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(login);
				//finish();
			}
		});
        
        // Listening to News Feed button click
        btn_upload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				startActivity(new Intent(Dashboard.this,UploadProductActivity.class));
			}
		});
        
       // Listening Friends button click
        btn_mylapak.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				startActivity(new Intent(Dashboard.this,
						ListProductActivity.class));
	        	
			}
		});
        
    }
    
    @Override
    public void onServiceReady(APIService api) {
    	// TODO Auto-generated method stub
    	super.onServiceReady(api);
    	this.api  = api;
    }
}
