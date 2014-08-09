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

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.webkit.WebView;

/**
 * Establece un espacio para mostrar en un WebView el manual de usuario.
 * Permite al usuario visualizar el manual de funcionamiento de la aplicación
 * @author Antonio Isaac Roldán Peña
 *
 */
public class WebViewUserGuide extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view_user_guide);
		
		//Manejo del elemento WebView para la visualización del manual de usuario
		WebView wb=(WebView)findViewById(R.id.webUserGuide);
		
		//JavaScript y caché activados
		wb.getSettings().setJavaScriptEnabled(true);
		wb.getSettings().setAppCacheEnabled(true);
		
		//Elemento file que se cargará en el WebView
		String htmlUserGuide="file:///android_asset/helptext/index.html";
		wb.loadUrl(htmlUserGuide);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.left_in, R.anim.left_out);
		}
		return false;
	}
}
