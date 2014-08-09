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

import android.app.Activity;

public class AppConfigurationParams extends Activity{

	//10.0.3.2
	
//Conjunto de direcciones Locales
	public static String SERVER_IP_ADDRESS="10.0.3.2"; //por defecto se usa esta, NAT virtualbox
//	public static String SERVER_IP_ADDRESS="192.168.1.128";
	public final static String URL_DO_LOGIN = "https://"+ AppConfigurationParams.SERVER_IP_ADDRESS + "/photo@firmaServices/doLogin.php";
	public final static String URL_CONTENT_UPLOAD="https://"+ AppConfigurationParams.SERVER_IP_ADDRESS + "/photo@firmaServices/content_upload.php";
	public final static String URL_DO_SIGNUP = "https://"+ AppConfigurationParams.SERVER_IP_ADDRESS + "/photo@firmaServices/doSignup.php";

public void setIp(String ip){
	AppConfigurationParams.SERVER_IP_ADDRESS=ip;
}

//Conjunto de direcciones a mi dominio
//	public final static String URL_DO_SIGNUP="http://yoantonioroldan.info/doSignup.php";
//	public final static String URL_CONTENT_UPLOAD="http://yoantonioroldan.info/content_upload.php";
//	public final static String URL_DO_LOGIN = "http://yoantonioroldan.info/doLogin.php";
	
}
