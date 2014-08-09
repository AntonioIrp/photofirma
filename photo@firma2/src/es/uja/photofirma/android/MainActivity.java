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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
/**
 * Controla la actividad y navegación durante el funcionamiento por MainActivity.
 * Permite llevar al usuario hacia las operaciones de acceso y registro
 * @author Antonio Isaac Roldán Peña
 *
 */
public class MainActivity extends Activity {
	static Context cntxofParent;
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_app);
		cntxofParent=MainActivity.this;
	}

	/**
	 * Accede a la activity LoginActivity cuando el usuario pulsa el botón correspondiente
	 * 
	 */
	public void onLoginPage(View view) {
		Intent login = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(login);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);	
	}

	/**
	 * Accede a la activity SignUpActivity cuando el usuario pulsa el botón correspondiente
	 * 
	 */
	public void onSignUpPage(View view) {
		Intent signup = new Intent(MainActivity.this, SignUpActivity.class);
		startActivity(signup);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	/**
	 * Accede a la activity AboutActivity cuando el usuario pulsa el botón correspondiente
	 * 
	 */
	public void onAboutPage(View view) {
		Intent about = new Intent(MainActivity.this, AboutActivity.class);
		startActivity(about);
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
//		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			super.finish();
		}
		return false;// super.onKeyDown(keyCode, event);
	}
}
