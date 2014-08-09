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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

/**
 * Automatiza y facilita el registro de eventos sucedidos durante la ejecución de la aplicación, 
 * se encarga de almacenar actividad, ordenar dicha actividad en una estructura JSON y proporcionar
 * dicha información en un archivo de texto
 *  
 * @author Antonio Isaac Roldán Peña
 * @version 1.0
 *
 */
public class Logger extends Activity {

	private int step=0;	
	private JSONObject logJson = new JSONObject();
	

	/**
	 * Este método se encarga de señalizar cuando finaliza una etapa de recolección de datos y empieza otra
	 * puede ser reemplazada por logStart().
	 */
	public void logStop(){
		this.step=0;
		this.logJson=new JSONObject();
	}
	
	/**
	 * Este método se encarga de señalizar cuando finaliza una etapa de recolección de datos y empieza otra
	 * puede ser reemplazada por logStop().
	 */
	public void logStart() {
		this.step=0;
		this.logJson=new JSONObject();
	}
	
	/**
	 * Devuelve los valores de recolectados a lo largo de todo el proceso que delimita un logStop() o logStart() 
	 * @return Se devuelte un tipo JSONObject de todos los datos recolectados para su fácil acceso
	 */
	public JSONObject getJson(){
		return this.logJson;	
	}
	
	/**
	 * Almacena un archivo *.log en una URL especificada de todo el proceso recolectado hasta el momento de la ejecucion de la orden logWrite() y el último logStop() o logStart()
	 * @param logUrl Indica la url donde se debe almacenar el archivo *.log generado
	 */
	public void logWrite(String logUrl){
		File logFile=new File(logUrl);
				
		String sjson = logJson.toString();

		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,true));
			buf.write(sjson);
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Realiza una operacion de registro de un evento sucedido, se ha de ejecutar cada vez que se pretende registrar un evento,
	 * los eventos pueden acompañarse de un codigo identificador y una descripción
	 * 
	 * @param idCode Identificador de evento, personalizable por el desarrollador
	 * @param description Descripción sobre el evento sucedido
	 */
	public void appendLog(int idCode, String description) {
		step=step+1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());
		JSONObject logJsonChildren = new JSONObject();
		try {
			logJson.accumulate("event_"+String.valueOf(step), logJsonChildren);
			logJsonChildren.put("EVENT_NUMBER", String.valueOf(step)); //marca el orden en que suceden los eventos
			logJsonChildren.put("CODE", idCode);
			logJsonChildren.put("DESCRIPTION", description);
			logJsonChildren.put("DEVICE_TIME", currentDateandTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}						
	}

}
