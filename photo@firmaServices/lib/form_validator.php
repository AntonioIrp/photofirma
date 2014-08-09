/*
    Copyright [2014] [Antonio Isaac Roldán Peña]

    This file is part of [photo@firmaServices].

    [photo@firmaServices] is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    [photo@firmaServices] is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with [photo@firmaServices]. If not, see <http://www.gnu.org/licenses/>.

*/

<?php

//VALIDACION DE REGISTRO DE NUEVO USUARIO
function registerValidator($pUsername, $pEmail, $pPassword, $pVpassword,$password_form_strength,$v_password_form_strength){

//mensajes de error de validacion:
$m_empty_username=("El nombre no puede estar vacío");
$m_empty_email=("El email no puede estar vacío");
$m_error_email=("E-mail no válido");
$m_empty_password=("Debe rellenar la contraseña");
$m_empty_vpassword=("Verifique su contraseña");
$m_passwords_mismatch=("Las contraseñas no coinciden");	
$m_passwords_strength=("Contraseña muy débil");

//comienzo de la validación
if(empty($pUsername)){
	return $m_empty_username;
	
}else if(empty($pEmail)){
	return $m_empty_email;
	
}else if(ValidaMail($pEmail)==false){
	return $m_error_email;
	
}else if(empty($pPassword)){
	return $m_empty_password;
	
}else if(empty($pVpassword)){
	return $m_empty_vpassword;
	
}else if($pPassword !== $pVpassword){
	return $m_passwords_mismatch;

// }else if((strlen($pPassword)!=intval($password_form_stregth))||(strlen($pVpassword)!=intval($v_password_form_stregth))){
	// return $m_passwords_mismatch;
	
}else if((intval($password_form_strength) || intval($v_password_form_strength))<8){
	return $m_passwords_strength=("Contraseña muy débil");
}

}

//VALIDACION DEl FORMULARIO DE LOGIN
function loginValidator($pEmail, $pPassword, $length){
$m_empty_email=("El email no puede estar vacío");
$m_empty_password=("Debe rellenar la contraseña");
$m_error_email=("E-mail no válido");
if(ValidaMail($pEmail)==false){
	return $m_error_email;
}else if(empty($pEmail)){
	return $m_empty_email;
}else if(empty($pPassword)){
	return $m_empty_password;
}else if($length==0){
	return $m_empty_password;
}
}

//COMPROBACION DE EMAIL VALIDO
function ValidaMail($pMail) { 
	if (ereg("^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@+([_a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]{2,200}\.[a-zA-Z]{2,6}$", $pMail )) 
	{ 
        return true; 
    } else 
	{ 
        return false; 
     } 
 }
?>
