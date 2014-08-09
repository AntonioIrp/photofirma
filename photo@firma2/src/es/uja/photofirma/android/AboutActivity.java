/*******************************************************************************
 * Copyright (c) 2014 Antonio Isaac Roldán Peña.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Antonio Isaac Roldán Peña - initial API and implementation
 ******************************************************************************/
package es.uja.photofirma.android;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controla la actividad y navegación durante el funcionamiento por AboutActivity.
 * Contiene información añadida de la aplicación y el desarrollador.
 * @author Antonio Isaac Roldán Peña
 *
 */
public class AboutActivity extends Activity {

	public static String MY_SERVER_IP_ADDRESS;
	MediaPlayer mplayer;
	int easterEgg=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		LinearLayout l = (LinearLayout) findViewById(R.id.aboutOfConfigLayout);
		l.setVisibility(LinearLayout.INVISIBLE);
		mplayer = MediaPlayer.create(getApplicationContext(), R.raw.lets_go_free);
	}

	public void onBackToMain(View view) {
		if(mplayer.isPlaying())
		{
			mplayer.stop();
		}
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	/**
	 * Redirige al usuario a la aplicación twitter si se encuentra instalada, en caso contrario
	 * se redirige al navegador web
	 */
	public void onTwitterFollow(View view) {
//		Toast.makeText(this, "onTwitterFollow", Toast.LENGTH_LONG).show();
		Boolean twitterInstalled=false;
		
		try{
            PackageManager packman = getPackageManager();
            packman.getPackageInfo("com.twitter.android", 0);
            twitterInstalled = true;
        }catch (Exception e) {
            e.printStackTrace();
            twitterInstalled = false;
        }

		if(twitterInstalled==true){
			Intent i = new Intent();
			i.setClassName("com.twitter.android", "com.twitter.android.ProfileActivity");
			i.putExtra("screen_name",getString(R.string.developer_twitter));
			try {
				startActivity(i);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}else{
			Intent intent = new Intent(Intent.ACTION_VIEW);	
			intent.setData(Uri.parse("http://twitter.com/"+getString(R.string.developer_twitter)));	
			AboutActivity.this.startActivity(intent);
		}	
	}

	/**
	 * Permite redirigir al usuario a la aplicación de email para el contacto con el desarrollador
	 * 
	 */
	public void onDeveloperEmail(View view) {
//		Toast.makeText(this, "onDeveloperEmail", Toast.LENGTH_LONG).show();
		Intent it = new Intent(Intent.ACTION_SEND);   
		it.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
		it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.developer_email_subject));
		it.setType("message/rfc822");  
		startActivity(Intent.createChooser(it, null));  
	}

	/**
	 Permite redirigir al usuario al repositorio GitHub del desarrollador
	 * 
	 */
	public void onGoToMyGitHub(View view){
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AntonioIrp"));
			startActivity(intent);
	}
	
	/**
	 * Permite mostrar u ocultar el cuadro de configuración de ip del servidor
	 * 
	 */
	public void onVersionClick(View view) {
		TextView t=(TextView) findViewById(R.id.aboutOfVersionTextView);
//		t.setText(String.valueOf(easterEgg));
//		
//		
//		if(easterEgg==3){
//			if(!mplayer.isPlaying())
//			{
//				try{
//					mplayer.start();
//					t.setText(">Stop<");
//				}catch(Exception e){
//					Log.d("huevo de pascua" , "error: " + e);
//				}
//			}else{
//				mplayer.pause();
//				easterEgg=0;
//				t.setText(R.string.about_of_version_text_view);
//			}
//		}else{
//			easterEgg++;
//		}
		
		

		
		LinearLayout l = (LinearLayout) findViewById(R.id.aboutOfConfigLayout);
		if (l.getVisibility() == View.INVISIBLE) {
			l.setVisibility(LinearLayout.VISIBLE);
			TextView ipTextView = (TextView) findViewById(R.id.aboutOfIpTextField);
			SharedPreferences prefs = getSharedPreferences("prefsfile",Context.MODE_PRIVATE);	 
			String myPrefServerIp = prefs.getString("prefIpAddress", "10.0.3.2");
			ipTextView.setText(myPrefServerIp);
			
		} else {
			l.setVisibility(LinearLayout.INVISIBLE);
		}
	}

	/**
	 * Establece los cambios para la conexión con el servidor
	 * 
	 */
	public void onSubmitConfiguration(View view) {
		TextView ipTextView = (TextView) findViewById(R.id.aboutOfIpTextField);
		
//		AppConfigurationParams a=new AppConfigurationParams();
//		a.setIp(ipTextView.getText().toString());
		
//		 AppConfigurationParams.SERVER_IP_ADDRESS=ipTextView.getText().toString();	
		 
		//al aceptar cambios se oculta la configuracion
		LinearLayout l = (LinearLayout) findViewById(R.id.aboutOfConfigLayout);
		l.setVisibility(LinearLayout.INVISIBLE);
		 
//		AppConfigurationParams.SERVER_IP_ADDRESS=ipTextView.getText().toString();
//		AboutActivity.MY_SERVER_IP_ADDRESS=ipTextView.getText().toString();
		
//		//preferencia elegida de ip del servidor
	    SharedPreferences settings = getSharedPreferences("prefsfile", 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("prefIpAddress", ipTextView.getText().toString());
	    editor.commit();
//	    

			
//		AppConfigurationParams.SERVER_IP_ADDRESS=myPrefServerIp;
//		Toast.makeText(getApplicationContext(), IP_ADDRESS, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Redirige al usuario al manual de instrucciones
	 * 
	 */
	public void onHelpMe(View view){
//		Toast.makeText(getApplicationContext(), "onHelpMe", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(getApplicationContext(), WebViewUserGuide.class);
	    startActivity(intent);
	    overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mplayer.isPlaying())
			{
				mplayer.stop();
			}
			finish();
			overridePendingTransition(R.anim.left_in, R.anim.left_out);
		}
		return false;
	}
}
