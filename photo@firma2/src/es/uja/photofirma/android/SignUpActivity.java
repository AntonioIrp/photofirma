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
import java.net.MalformedURLException;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controla la actividad y navegación durante el funcionamiento por SignUpActivity.
 * Permite registrar un nuevo usuario en una base de datos
 * @author Antonio Isaac Roldán Peña
 *
 */
public class SignUpActivity extends Activity {

	//Dirección de conexion para la operación de Registro
	private static String URL_DO_SIGNUP=""; //= AppConfigurationParams.URL_DO_SIGNUP;
	
	//Credenciales del usuario
	private String userEmail;
	private String userName;
	private String userPassword;
	private String userVPassword;
	private int userPasswordLenght,userVPasswordLenght;
	
	//Elementos gráficos 
	LinearLayout spb,signUpButton,signUpButtonError;
	TextView signUpButtonErrorText,tConnectingText,tSignupText;
	AlertDialog alertDialog;
	CheckBox checkBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up_activity);
		
		//Preparación de elementos gráficos
		spb=(LinearLayout)findViewById(R.id.signUpActivityProgressBar);
		signUpButton=(LinearLayout)findViewById(R.id.signUpDoSignUpButton);
		signUpButtonError=(LinearLayout)findViewById(R.id.signUpActivityDoSignupError);
		signUpButtonErrorText=(TextView)findViewById(R.id.signUpActivityDoSignupErrorText);
		tConnectingText=(TextView)findViewById(R.id.signUpActivityConectingText);
		tSignupText=(TextView)findViewById(R.id.signUpActivitySignUpTextView);
		checkBox=(CheckBox)findViewById(R.id.signUpActivityIAccept);
		alertDialog = new AlertDialog.Builder(this).create();
		
		SharedPreferences prefs = getSharedPreferences("prefsfile",Context.MODE_PRIVATE);	 
		String myPrefServerIp = prefs.getString("prefIpAddress", "10.0.3.2");
		URL_DO_SIGNUP="https://"+ myPrefServerIp + "/photo@firmaServices/doSignup.php";
	}

	/**
	 * Se ejecuta cuando el usuario pulsa el botón 'Registrar' de la interfaz gráfica, permite verficar los 
	 * datos del formulario para la posterior conexión con el servidor
	 */
	public void onDoSignup(View view){
		signUpButton.setVisibility(LinearLayout.VISIBLE);
		tSignupText.setVisibility(TextView.INVISIBLE);
		tConnectingText.setVisibility(TextView.VISIBLE);
		spb.setVisibility(LinearLayout.VISIBLE);
		
		EditText userPasswordTextEdit = (EditText) findViewById(R.id.signUpActivityPassword);
		EditText userVPasswordTextEdit = (EditText) findViewById(R.id.signUpActivityVPassword);
		EditText userNameTextEdit = (EditText) findViewById(R.id.signUpActivityUsername);
		EditText userEmailTextEdit = (EditText) findViewById(R.id.signUpActivityEmail);

		userEmail = userEmailTextEdit.getText().toString();
		userName = userNameTextEdit.getText().toString();
		//longitud contraseña, para comprobación de la fortaleza
		userPasswordLenght=userPasswordTextEdit.getText().toString().length();
		userVPasswordLenght=userVPasswordTextEdit.getText().toString().length();
		userPassword=userPasswordTextEdit.getText().toString();
		userVPassword=userVPasswordTextEdit.getText().toString();

		if(checkBox.isChecked()==false){
			signUpButton.setVisibility(LinearLayout.INVISIBLE);
			signUpButtonError.setVisibility(LinearLayout.VISIBLE);
			signUpButtonErrorText.setText(R.string.sign_up_activity_accept_terms_of_use);
		}else{
			//Comprobación de la longitud de la contraseña
		if(userEmail.length()==0){
			signUpButton.setVisibility(LinearLayout.INVISIBLE);
			signUpButtonError.setVisibility(LinearLayout.VISIBLE);
			signUpButtonErrorText.setText(R.string.sign_up_activity_email_required);
		}else if(userName.length()==0){
			signUpButton.setVisibility(LinearLayout.INVISIBLE);
			signUpButtonError.setVisibility(LinearLayout.VISIBLE);
			signUpButtonErrorText.setText(R.string.sign_up_activity_username_required);
		}else if(userPasswordLenght<8 && userVPasswordLenght<8){
			//Si la contraseña no es lo suficientemente fuerte notifica al usuario
			signUpButton.setVisibility(LinearLayout.INVISIBLE);
			signUpButtonError.setVisibility(LinearLayout.VISIBLE);
			signUpButtonErrorText.setText(R.string.signup_activity_password_required);
		}else if(!userPassword.equals(userVPassword)){
				signUpButton.setVisibility(LinearLayout.INVISIBLE);
				signUpButtonError.setVisibility(LinearLayout.VISIBLE);
				signUpButtonErrorText.setText(R.string.sign_up_activity_password_mismatch);
		}else{
				userPassword = StringMD.getStringMessageDigest(userPassword, StringMD.SHA512);
				userVPassword = StringMD.getStringMessageDigest(userVPassword, StringMD.SHA512);
				new DoSignup().execute(URL_DO_SIGNUP);
		}	
		}
		
	}

	/**
	 * Permite adjuntar los datos necesarios del formulario rellenado al mensaje que se enviará al servidor
	 *
	 */
	private class DoSignup extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DoConnection().getNewHttpClient();
			HttpPost httppost = new HttpPost(urls[0]);
			String res = null;
			try {
				// Añade las variables a enviar por post
				List<NameValuePair> postValues = new ArrayList<NameValuePair>(4);
				postValues.add(new BasicNameValuePair("password", userPassword));
				postValues.add(new BasicNameValuePair("v_password",userVPassword));
				postValues.add(new BasicNameValuePair("username", userName));
				postValues.add(new BasicNameValuePair("email", userEmail));
				postValues.add(new BasicNameValuePair("password_strength", String.valueOf(userPasswordLenght)));
				postValues.add(new BasicNameValuePair("v_password_strength", String.valueOf(userVPasswordLenght)));

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
				signUpButton.setVisibility(LinearLayout.INVISIBLE);
				signUpButtonError.setVisibility(LinearLayout.VISIBLE);
				signUpButtonErrorText.setText(R.string.login_activity_error_conn_text_view);
				
			}else{
				//Si no, procesa la respuesta
				JsonParse jresult=new JsonParse();
				jresult.getResponseFromServer(result);
				if(jresult.getStatus()==true){
					//Si la operación tuvo exito notifica al usuario
//					Toast.makeText(getApplicationContext(), jresult.getDescription(), Toast.LENGTH_LONG).show();
					
					
			        alertDialog.setTitle("Ya puede acceder");
			        alertDialog.setCancelable(false);
			        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
							overridePendingTransition(R.anim.right_in, R.anim.right_out);
						}
					});
			        
			        alertDialog.setMessage(getString(R.string.sign_up_activity_signup_success_completed));
			        alertDialog.show();
					      
					
				}else if(jresult.getStatus()==false){
					//Si la operación fracasó notifica al usuario
					signUpButton.setVisibility(LinearLayout.INVISIBLE);
					signUpButtonError.setVisibility(LinearLayout.VISIBLE);
					signUpButtonErrorText.setText(jresult.getDescription());
				}
			}
		}
	}

	public void onBackToMain(View view) {
		finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	/**
	 * Advertencia a cerca de la privacidad del usuario, el usuario será notificado
	 * 
	 */
	public void onTermsOfUse(View view){
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Terminos de uso");
        alertDialog.setMessage(getString(R.string.terms_of_use));
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
        alertDialog.show(); 

		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.right_in, R.anim.right_out);
		}
		return false;// super.onKeyDown(keyCode, event);
	}
}
