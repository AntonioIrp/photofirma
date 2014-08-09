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

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controla la actividad y navegación durante el funcionamiento por CameraActivity:
 * Contiene las operaciones básicas para la captura, generación de firma, comprobación y subida al servidor
 * de los archivos involucrados
 * @author Antonio Isaac Roldán Peña
 *
 */
public class CameraActivity extends Activity {

	//Parámetros de referencia a los archivos involucrados en el proceso
	private String photoLocation = null;
	private String signedFileLocation = null;
	PhotoMaker PhotoCapture;

	//Nombre del directorio oficial de la aplicación
	public static final String APP_FOLDER = "/Photo@Firma/";
	public static final int REQUEST_IMAGE_CAPTURE = 1;

	//Conjunto de codigos para la coordinacion con @firma
	public static final int APP_FIRMA_REQUEST_CODE = 8; // codigo de llamada a @firma 
	public static final int RESULT_ERROR = 0X00000010; // codigo de operacion con @firma fallida
	
	//Intent Filter @firma ACTION *.OPEN, acción de apertura de @firma
	public static final String APP_FIRMA_OPEN_ACTION = "es.gob.afirma.android.OPEN"; // accion de apertura de @firma
	
	//Parametros de entrada a @firma
	public static final String APP_FIRMA_EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
	
	//Direccion de conexión para la subida de contenido
	private static String URL_CONTENT_UPLOAD=""; //=AppConfigurationParams.URL_CONTENT_UPLOAD;

	//Registo de eventos
	private Logger logger = new Logger();

	//Elementos visuales de informacion
	private LinearLayout lyerr, lysuc, lyinf, lycan,lyupl,lyudone,lyuerr;
	private ImageView lycpt;
	private TextView errorText;
	
	//Informacion de usuario
	private String userName=null;
	private int userId=-1;
	private String userEmail=null;
	private String imeiNumber=null;
	private TelephonyManager telephonyManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		logger.appendLog(100, "login usuario satisfactorio");
		
		//Definición de elementos visuales involucrados
		lycpt =	(ImageView) findViewById(R.id.cameraActivityCameraPhotoTap);
		lyerr = (LinearLayout) findViewById(R.id.cameraActivityErrorHeader);
		lysuc = (LinearLayout) findViewById(R.id.cameraActivitySuccessHeaderTextView);
		lycan = (LinearLayout) findViewById(R.id.cameraActivityCancelHeader);
		lyinf = (LinearLayout) findViewById(R.id.cameraActivityInfoHeader);
		lyupl = (LinearLayout) findViewById(R.id.cameraActivityUploadingHeader);
		lyudone= (LinearLayout) findViewById(R.id.cameraActivitySuccesUploadHeader);
		lyuerr= (LinearLayout) findViewById(R.id.cameraActivityErrorUploadHeader);
		errorText=(TextView)findViewById(R.id.cameraActivityErrorTextView);
		
		//gestor de servicios de telefonia, para la obtención del IMEI del terminal
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		if(telephonyManager==null){
			imeiNumber=getString(R.string.no_imei_available);
		}else{
			imeiNumber=telephonyManager.getDeviceId();	
		}
		
		//Detección de los parametros necesarios tras un login exitoso
		if(getIntent().hasExtra("userName")&&getIntent().hasExtra("userId")&&getIntent().hasExtra("userEmail")){
				userName = getIntent().getExtras().getString("userName");
				userId = getIntent().getExtras().getInt("userId");
				userEmail = getIntent().getExtras().getString("userEmail");		
		}else{
			Toast.makeText(getApplicationContext(), getString(R.string.no_user_data_found), Toast.LENGTH_LONG).show();
		}
		
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Aviso de privacidad");
        alertDialog.setMessage(getString(R.string.privacy_alert));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
        alertDialog.show(); 
        
        SharedPreferences prefs = getSharedPreferences("prefsfile",Context.MODE_PRIVATE);	 
		String myPrefServerIp = prefs.getString("prefIpAddress", "10.0.3.2");
		URL_CONTENT_UPLOAD="https://"+ myPrefServerIp + "/photo@firmaServices/content_upload.php";
	}

	/**
	 * Muestra la cabecera de error en la subida de datos
	 */
	public void showUpErrorHeader(){
		hideAllHeaders();
		lyuerr.setVisibility(LinearLayout.VISIBLE);
		lycpt.setImageResource(R.drawable.camera512r);	
	}
	
	/**
	 * Muestra la cabecera de exito en la subida de datos
	 */
	public void showUpCompletedHeader(){
		hideAllHeaders();
		lyudone.setVisibility(LinearLayout.VISIBLE);
	}
	
	/**
	 * Muestra la cabecera de subida de datos en progreso
	 */
	public void showUploadingHeader(){
		hideAllHeaders();
		lyupl.setVisibility(LinearLayout.VISIBLE);
	}
	
	/**
	 * Muestra la cabecera de exito en la creación de una firma
	 */
	public void showSuccessHeader() {
		hideAllHeaders();
		lysuc.setVisibility(LinearLayout.VISIBLE);
		lycan.setVisibility(LinearLayout.VISIBLE);
		lycpt.setImageResource(R.drawable.camera512g);
	}

	/**
	 * Muestra la cabecera de información 
	 */
	public void showInfoHeader() {
		hideAllHeaders();
		lyinf.setVisibility(LinearLayout.VISIBLE);
		lycpt.setImageResource(R.drawable.camera512b);
	}

	/**
	 * Muestra la cabecera de error o cancelación del proceso
	 */
	public void showErrorHeader() {
		hideAllHeaders();
		lyerr.setVisibility(LinearLayout.VISIBLE);
		lycpt.setImageResource(R.drawable.camera512r);
		errorText.setText(getString(R.string.camera_activity_error_header_text_view));
	}

	/**
	 * Oculta todas las cabeceras visibles
	 */
	public void hideAllHeaders() {
		lyuerr.setVisibility(LinearLayout.INVISIBLE);
		lyudone.setVisibility(LinearLayout.INVISIBLE);
		lyupl.setVisibility(LinearLayout.INVISIBLE);
		lyerr.setVisibility(LinearLayout.INVISIBLE);
		lysuc.setVisibility(LinearLayout.INVISIBLE);
		lycan.setVisibility(LinearLayout.INVISIBLE);
		lyinf.setVisibility(LinearLayout.INVISIBLE);
	}


	/**
	 * Se ejecuta al pulsar sobre el boton para abrir la captura de fotos, inicia la captura y almacena la fotografía realizada
	 */
	public void onTakeAPhoto(View view) {
		logger.appendLog(300, "el usuario abre la aplicacion de fotos");
		
		//Se crea nueva instancia de la clase PhotoMaker() para automatizar el proceso de trabajo con fotografías
		PhotoCapture = new PhotoMaker();
		File imagen = PhotoCapture.savePhoto();
		photoLocation = imagen.getAbsolutePath();

		//Se redirige al usuario a la app de captura de fotografías
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,Uri.fromFile(imagen));
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//			Toast.makeText(getApplicationContext(), getString(R.string.privacy_alert), Toast.LENGTH_LONG).show();
			startActivityForResult(takePictureIntent,CameraActivity.REQUEST_IMAGE_CAPTURE);
		}
	}

	/**
	 * Controla los accesos y retornos a las aplicaciones de caputura de fotografías y '@firma', se establece
	 * para cada caso tanto la situacion de exito como la de fracaso. 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Vuelta de @firma, el resultCode indica exito o fracaso, en cada caso se toman unas medidas diferentes
		if (requestCode == CameraActivity.APP_FIRMA_REQUEST_CODE && resultCode == CameraActivity.RESULT_ERROR) {
			logger.appendLog(402, "el usuario deniega el uso del certificado");
			showErrorHeader();
		}
		
		//Vuelta de @firma, la firma fue realizada con exito, se almacena la ruta al archivo
		if (requestCode == CameraActivity.APP_FIRMA_REQUEST_CODE && resultCode == RESULT_OK) {
			logger.appendLog(200, "@firma realizó la firma adecuadamente");
			signedFileLocation = data.getExtras().getString("signedfilelocation");			
			showSuccessHeader();
		}

		//Si la captura de la fotografía tuvo exito
		if (requestCode == CameraActivity.REQUEST_IMAGE_CAPTURE & resultCode == RESULT_OK) {
			logger.appendLog(101, "el usuario captura foto");
			//Se obtienen los datos del servicio de localización geografica
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			// Añade los geoTags a la foto realizada anteriormente
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && location != null) {
				PhotoCapture.addExifData(photoLocation, location);
				logger.appendLog(102, "el usuario añade exif");
				logger.appendLog(103, "gps data: " + location);
			}
			
			logger.appendLog(104, "buscando @firma");

			//Se comprueba si está la app @firma
			Intent intent = getPackageManager().getLaunchIntentForPackage("es.gob.afirma");
			if (intent == null) {
				//Si no se encuentra instalada se notifica al usuario
				logger.appendLog(400, "@firma no instalada");
				showErrorHeader();
				errorText.setText(getString(R.string.camera_activity_no_afirma_present));
			}else {
				//Si se encuentra, entonces se abre para dar comienzo al proceso de firma
				logger.appendLog(201, "abriendo @firma");
				Intent i = new Intent(APP_FIRMA_OPEN_ACTION);
				i.putExtra(CameraActivity.APP_FIRMA_EXTRA_FILE_PATH,photoLocation);
				startActivityForResult(i, APP_FIRMA_REQUEST_CODE);
			}
		}

		//Si la captura de fotos fue cancelada
		if (requestCode == CameraActivity.REQUEST_IMAGE_CAPTURE && resultCode == CameraActivity.RESULT_CANCELED) {
			showInfoHeader();
			logger.appendLog(105, "cierra la app de photo sin captura");
		}
		//Si la captura de fotos fue cancelada
		if (requestCode == CameraActivity.REQUEST_IMAGE_CAPTURE && resultCode == CameraActivity.RESULT_ERROR) {
			showInfoHeader();
			logger.appendLog(403, "Se produjo un error en la app de Cámara");
		}
	}

	/**
	 * Se ejecuta cuando el usario confirma la fotografia realizada y permite el uso del certificado seleccionado para generar
	 * la firma del archivo.
	 * 
	 */
	public void onSubmitPhoto(View view) {
		logger.appendLog(302, "el usuario acepta la captura y pretende enviar");
		
		//Se verifica que exiten todos los datos necesarios antes de comenzar el proceso de subida
		if(new File(photoLocation).exists() && new File(signedFileLocation).exists() && userName!=null && userId!=-1 && userEmail!=null && logger!=null && imeiNumber!=null){
			logger.appendLog(106, "enviando archvios al servidor");
			showUploadingHeader();
			new ContentUpload().execute(URL_CONTENT_UPLOAD,photoLocation,signedFileLocation,userName,String.valueOf(userId),userEmail,String.valueOf(logger.getJson()),imeiNumber);	
		}else{
			logger.appendLog(401, "faltan datos en el proceso de subida");
			showErrorHeader();
			errorText.setText(getString(R.string.no_files_found));
		}	
	}
	
	/**
	 * Realiza la operación de subida de datos al servidor, negocia la conexión http o https y adjunta
	 * los datos generados durante todo el proceso
	 *
	 */
	private class ContentUpload extends AsyncTask <String, Void, String> {
		
		@Override
		protected String doInBackground(String... data ) {
				String res = null;
				try {
					HttpClient httpclient = new DoConnection().getNewHttpClient();
					HttpPost httppost = new HttpPost(data[0]);
					
					//Mensaje multiparte, permite añadir datos de diferente naturaleza en un mismo mensaje
					MultipartEntity mpEntity = new MultipartEntity();
					File foto =new File(data[1]);
					File firma= new File(data[2]);
					
//					ContentBody fotoContent= new FileBody(foto,"image/jpg");
					ContentBody fotoContent= new FileBody(foto);
					ContentBody firmaContent= new FileBody(firma);
//					ContentBody firmaContent= new FileBody(firma, "signedfile/csig");
				
					//Se adjuntan los datos generados
					mpEntity.addPart("imagen", fotoContent);
					mpEntity.addPart("firma", firmaContent);
					mpEntity.addPart("user_name", new StringBody(data[3]));
					mpEntity.addPart("user_id", new StringBody(data[4]));
					mpEntity.addPart("user_email", new StringBody(data[5]));
					mpEntity.addPart("events", new StringBody(data[6]));
					mpEntity.addPart("imei", new StringBody(data[7]));
			
					httppost.setEntity(mpEntity);
					
					HttpResponse response = httpclient.execute(httppost);
					res = EntityUtils.toString(response.getEntity());

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return res;
		}

		
		@Override
		protected  void onPostExecute(String result){
			
			//se verifica que haya una respuesta válida
			if(result==null){
				logger.appendLog(500, "sin respuesta del servidor");
				showUpErrorHeader();
			}else{
				//Se procesa la respuesta del servidor y se extraen datos
				logger.appendLog(202, "respuesta del servidor recibida");
				JsonParse jresult=new JsonParse();
				jresult.getResponseFromServer(result);
				
				if(jresult.getStatus()==true){
					//Si la operacion ha sido correcta se notifica al usuario del exito del proceso de subida
					logger.appendLog(203, "subida completada satisfactoriamente");
					showUpCompletedHeader();
					
					File f=new File(photoLocation);
					String photoName=f.getName().toString();
					String logUrl=Environment.getExternalStorageDirectory() + CameraActivity.APP_FOLDER+photoName+".log";
					
//					String g=String.valueOf(logger.getJson());
//					Log.d("log", logger.getJson().toString());
//					Log.d("json", jresult.getDescription());

					logger.logWrite(logUrl);
					logger.logStop();
					
				}else if(jresult.getStatus()==false){
					//Si la operación no es válida, se ha producido un error y se muestra la información al usuario
					logger.appendLog(501, "se produjo un error en la subida de datos");
					showUpErrorHeader();
				}	
			}	
		}
	}
	
	
/**
 * Se ejecuta al presionar el boton de cancelar, permite vaciar las variables de los elementos a subir, y eliminar
 * los archivos relacionados
 * 
 */
	public void onCancel(View view) {
		logger.appendLog(303, "el usuario cancela el proceso");
		
		File p=new File(photoLocation);
		File f=new File(signedFileLocation);
		p.delete();
		f.delete();
		
		photoLocation=null;
		signedFileLocation=null;
		
		Toast.makeText(getApplicationContext(), getString(R.string.camera_activity_on_cancel_text), Toast.LENGTH_LONG).show();
		showInfoHeader();
	}

	/**
	 * El botón back se configura para cerrar sesión
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent mainActivity = new Intent(CameraActivity.this, MainActivity.class);
			startActivity(mainActivity);
			overridePendingTransition(R.anim.right_in, R.anim.right_out);
			super.finish();
			Toast.makeText(getApplicationContext(), getString(R.string.close_session_message), Toast.LENGTH_LONG).show();
		}
		return false;// super.onKeyDown(keyCode, event);
	}

}
