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
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Environment;

/**
 * PhotoMaker ayuda a encapsular y automatizar todo el proceso de captura y almacenado de una fotografia capturada,
 * además permite añadir a la fotografia datos exif a cerca del modelo de cámara, exposicion, asi como geoTags.
 * 
 * @author Antonio Isaac Roldán Peña
 * @version 1.0
 * 
 */
public class PhotoMaker extends Activity {

	private String photourl = null;

	/**
	 * Automatiza el almacenaje de la fotografia, comprueba que puede ser guardada en el directorio de la aplicación y en caso negativo
	 * se almacena en el directorio de fotos predeterminado del sistema, además renombra el archivo con un formato de fecha
	 * @return Se devuelve el archivo generado
	 */
	public File savePhoto() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String currentDateandTime = sdf.format(new Date()).replace(" ", "");

		if (checkMyAppFolder() == false) {
			photourl = Environment.DIRECTORY_PICTURES;
		} else {
			photourl = Environment.getExternalStorageDirectory().getAbsolutePath() + CameraActivity.APP_FOLDER;
		}
		return new File(photourl, currentDateandTime + ".jpg");
	}
	
	/**
	 * Crea la carpeta de la app, en caso de que no exista se crea una con el nombre oficial de la aplicación que será
	 * usada para el almacenaje de las capturas 
	 * @return
	 */
	private boolean checkMyAppFolder() {
		String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + CameraActivity.APP_FOLDER;
		File f = new File(folder);
		if (f == null || (!f.exists() && !f.mkdir())) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * Automatiza el proceso de añadir datos exif a una fotografia, permite una amplia selección de parámetros, 
	 * a destacar los datos de localizacion: latitud, longitud y altura, así como modelo de cámara.
	 * @param path Ruta a la fotografia que se pretende añadir tags
	 * @param location	Información a cerca de la localización del terminal
	 */
	public void addExifData(String path, Location location) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(path);
			exif.setAttribute(ExifInterface.TAG_APERTURE, String.valueOf(exif
					.getAttribute(ExifInterface.TAG_APERTURE)));
			exif.setAttribute(ExifInterface.TAG_MODEL,
					String.valueOf(exif.getAttribute(ExifInterface.TAG_MODEL)));
			exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME,
					String.valueOf(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)));
			exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, String
					.valueOf(exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)));
			exif.setAttribute(ExifInterface.TAG_ISO,
					String.valueOf(exif.getAttribute(ExifInterface.TAG_ISO)));
			exif.setAttribute(ExifInterface.TAG_MAKE,
					String.valueOf(exif.getAttribute(ExifInterface.TAG_MAKE)));
			exif.setAttribute(ExifInterface.TAG_FLASH,
					String.valueOf(exif.getAttribute(ExifInterface.TAG_FLASH)));

			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
					String.valueOf(location.getLatitude()));
			exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE,
					String.valueOf(location.getAltitude()));
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
					String.valueOf(location.getLongitude()));

			exif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
