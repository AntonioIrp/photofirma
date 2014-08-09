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
header("Content-Type: text/html; charset=utf-8");
include '/config/db_connect.php';
include '/lib/form_validator.php';

// Datos recibidos del formulario
$password_form = trim($_POST['password']);
$v_password_form = trim($_POST['v_password']);
$v_password_form_strength=trim($_POST['v_password_strength']);
$password_form_strength=trim($_POST['password_strength']);
$nombre_usuario=trim($_POST['username']);
$email=trim($_POST['email']);

//crear un salt aleatorio
//$random_salt = hash('sha512', uniqid(mt_rand(1, mt_getrandmax()), true));
$random_salt=mcrypt_create_iv(22, MCRYPT_DEV_URANDOM);

$opciones = [
    'cost' => 12,
    'salt' => $random_salt
];
$sec_pass=password_hash($password_form, PASSWORD_BCRYPT, $opciones);

//crear una contreseña con salt
$password_for_store = $sec_pass; //hash('sha512', $password_form.$random_salt);

$response = new StdClass();
//comprobaciones conexion y trabajo con bd
if(db_connection()){
	//echo("ERROR AL CONECTAR");
	$response->code_operation="signup";
	$response->status="false";
	$response->description="Error en la bd";
	echo json_encode($response);
	
}else if($validation_status = registerValidator($nombre_usuario, $email, $password_form, $v_password_form,$password_form_strength,$v_password_form_strength)){
	//echo $validation_status;
	$response->code_operation="signup";
	$response->status="false";
	$response->description=$validation_status;
	echo json_encode($response);
	
}else if($check_stmt = $mysqli->prepare("SELECT username, email FROM members WHERE username= ? OR email= ? ")){
	
	$check_stmt->bind_param('ss', $nombre_usuario, $email);
	$check_stmt->execute();
	$check_stmt->store_result();
	$check_stmt->bind_result($db_username, $db_email);
    $check_stmt->fetch();
	
	if($check_stmt->num_rows >= 1){
		if($db_username == $nombre_usuario){
			$response->code_operation="signup";
			$response->status="false";
			$response->description="El usuario ya existe";
			echo json_encode($response);
			
		}else if($db_email == $email){
			$response->code_operation="signup";
			$response->status="false";
			$response->description="El e-mail ya está registrado";
			echo json_encode($response);
		}
		
	}else if($insert_stmt = $mysqli->prepare("INSERT INTO members (username, email, password, salt) VALUES (?, ?, ?, ?)")){
		
		$insert_stmt->bind_param('ssss', $nombre_usuario, $email, $password_for_store, $random_salt); 
		$insert_stmt->execute();
		
		$response->code_operation="signup";
		$response->status="true";
		$response->description="!Registrado correctamente!";
		echo json_encode($response);
	}
}
?>
