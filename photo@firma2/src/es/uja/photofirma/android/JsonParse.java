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

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Esta clase se encarga de recibir una respuesta del servidor, analiza y extrae todos los valores 
 * adjuntos del mensaje para almacenarlos o ser usados en posteriores operaciones con el servidor
 * 
 * 
 * @author Antonio Isaac Roldán Peña
 * @version 1.0
 *
 */
public class JsonParse {

	private int opCode;
	private Boolean status;
	private int userId;
	private String userName;
	private String userEmail;
	private String description=null;

	/**
	 * 
	 * @param response contiene la respuesta del servidor, se trata de un tipo strin cuyo formato sea el de un JSON
	 * 
	 */
	public void getResponseFromServer(String response){
		try {
			JSONObject jsonResponse=new JSONObject(response);
			
			//Esto solo se usa cuando el login es satisfactorio, para conocer las credenciales del usuario en operaciones posteriores
			if(jsonResponse.has("user_id") && jsonResponse.has("user_email") && jsonResponse.has("user_name")){
				setUserid(Integer.valueOf(jsonResponse.getString("user_id")));
				setUseremail(jsonResponse.getString("user_email"));
				setUsername(jsonResponse.getString("user_name"));
			}
			
			setOpcode(0);//no implementados codigos de operacion en el servidor
			setStatus(jsonResponse.getBoolean("status"));
			setDescription(jsonResponse.getString("description"));
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
	//Getters and Setters
	public String getUsername() {
		return userName;
	}


	private void setUsername(String username) {
		this.userName = username;
	}


	public String getUseremail() {
		return userEmail;
	}


	private void setUseremail(String useremail) {
		this.userEmail = useremail;
	}
	
	public int getUserid() {
		return userId;
	}

	private void setUserid(int userid) {
		this.userId = userid;
	}
	
	public int getOpcode() {
		return opCode;
	}

	private void setOpcode(int opcode) {
		this.opCode = opcode;
	}

	public Boolean getStatus() {
		return status;
	}

	private void setStatus(Boolean status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	private void setDescription(String description) {
		this.description = description;
	}
	
	
	
	

}
