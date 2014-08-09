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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



//import com.example.cxv.Login;
//import com.example.cxv.MainActivity;
//import com.example.cxv.R;
//import com.example.cxv.Login.MySSLSocketFactory;
//import com.example.cxv.Login.doLogin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controla la actividad y navegación durante el funcionamiento por LoginActivity.
 * Realiza la operación de acceso al sistema
 * @author Antonio Isaac Roldán Peña
 *
 */
public class LoginActivity extends Activity {
	
	//Dirección de conexión para la operación de Login
	private static String URL_DO_LOGIN=""; 
//	private final static String URL_DO_LOGIN = AppConfigurationParams.URL_DO_LOGIN;
	
	private String userEmail;
	private String userPassword;
	private int passLength;
	
	LinearLayout lpb,loginButton,loginButtonError;
	TextView lButtonErrorText,tConnectingText,tLoginText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		//Elementos gráficos
		lpb=(LinearLayout)findViewById(R.id.loginProgressBar);
		loginButton=(LinearLayout)findViewById(R.id.loginActivityDoLoginButton);
		loginButtonError=(LinearLayout)findViewById(R.id.loginActivityDoLoginError);
		lButtonErrorText=(TextView)findViewById(R.id.loginActivityDoLoginErrorText);	
		tConnectingText=(TextView)findViewById(R.id.loginActivityConnectingTextView);
		tLoginText=(TextView)findViewById(R.id.loginActivityLoginTextView);
		
		SharedPreferences prefs = getSharedPreferences("prefsfile",Context.MODE_PRIVATE);	 
		String myPrefServerIp = prefs.getString("prefIpAddress", "10.0.3.2");
		URL_DO_LOGIN="https://"+ myPrefServerIp + "/photo@firmaServices/doLogin.php";
	}

	/**
	 * Se ejecuta al pulsar el boton de 'Acceder' en la interfaz gráfica, inicia el proceso de acceso al sistema
	 *
	 */
	public void onDoLogin(View view) {
		loginButton.setVisibility(LinearLayout.VISIBLE);
		tLoginText.setVisibility(TextView.INVISIBLE);
		tConnectingText.setVisibility(TextView.VISIBLE);
		lpb.setVisibility(LinearLayout.VISIBLE);
		
		TextView user_text_view = (TextView) findViewById(R.id.loginActivityEmailTextEdit);
		TextView password_text_view = (TextView) findViewById(R.id.loginActivityPasswordTextEdit);

		userEmail = user_text_view.getText().toString();
		passLength = password_text_view.getText().toString().length();
		userPassword = StringMD.getStringMessageDigest(password_text_view.getText().toString(), StringMD.SHA512);
		
		if(userEmail.length()==0){
			loginButton.setVisibility(LinearLayout.INVISIBLE);
			loginButtonError.setVisibility(LinearLayout.VISIBLE);
			lButtonErrorText.setText(R.string.sign_up_activity_email_required); 
		}else if(passLength<8){
			loginButton.setVisibility(LinearLayout.INVISIBLE);
			loginButtonError.setVisibility(LinearLayout.VISIBLE);
			lButtonErrorText.setText(R.string.signup_activity_password_required); 
		}else{
			new DoLogin().execute(URL_DO_LOGIN);
		}
		
	}

	/**
	 * Crea una nueva conexión al servidor y adjunta los datos necesarios para la operación de login
	 * 
	 */
	private class DoLogin extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DoConnection().getNewHttpClient();
			HttpPost httppost = new HttpPost(urls[0]);
			
			String res = null;
			try {
				// Añade las variables a enviar por post
				List<NameValuePair> postValues = new ArrayList<NameValuePair>(3);
				postValues.add(new BasicNameValuePair("email", userEmail)); 
				postValues.add(new BasicNameValuePair("length", String.valueOf(passLength))); 
				postValues.add(new BasicNameValuePair("password", userPassword)); 

				httppost.setEntity(new UrlEncodedFormEntity(postValues));

				// Hace la petición
				HttpResponse response = httpclient.execute(httppost);
				res = EntityUtils.toString(response.getEntity());
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return res;
		}

		
		@Override
		protected void onPostExecute(String result) {
			
			if(result==null){
				//Si no se recibe respuesta del servidor notifica al usuario
				loginButton.setVisibility(LinearLayout.INVISIBLE);
				loginButtonError.setVisibility(LinearLayout.VISIBLE);
				lButtonErrorText.setText(R.string.login_activity_error_conn_text_view);
				
				
			}else{
				//Procesa el mensaje recibido 
				JsonParse jresult=new JsonParse();
				jresult.getResponseFromServer(result);
				if(jresult.getStatus()==true){	
					//Si la operación resultó exitosa
					Activity parentActivity; 
					parentActivity=(Activity)MainActivity.cntxofParent;
					parentActivity.finish(); //Elimina el contexto de MainActivity
					
					Intent intent = new Intent(LoginActivity.this,CameraActivity.class);
					intent.putExtra("userName", jresult.getUsername());
					intent.putExtra("userEmail", jresult.getUseremail());
					intent.putExtra("userId", jresult.getUserid());
					startActivity(intent);
					overridePendingTransition(R.anim.left_in, R.anim.left_out);
					finish();
				
				}else if(jresult.getStatus()==false){
					//Si la operación falló notifica al usuario
					loginButton.setVisibility(LinearLayout.INVISIBLE);
					loginButtonError.setVisibility(LinearLayout.VISIBLE);
					lButtonErrorText.setText(jresult.getDescription());

				}
			}
		}
	}

	public void onBackToMain(View view) {
		finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	/**
	 * Acción ejecutada si el usuario pulsa sobre la opción que indica que no recuerda su contraseña,
	 * abre una instancia de la aplicación de email del dispositivo para contactar con el administrador
	 * 
	 */
	public void onForgotPassword(View view){
		Intent it = new Intent(Intent.ACTION_SEND);   
		it.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
		it.putExtra(Intent.EXTRA_SUBJECT, "Olvidé mi contraseña...");
		it.setType("message/rfc822");  
		startActivity(Intent.createChooser(it, null));  
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.right_in, R.anim.right_out);
		}
		return false;
	}

}
