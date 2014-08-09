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
include '/config/db_connect.php';
include '/lib/form_validator.php';
include '/lib/functions.php';

//sec_session_start(); // Our custom secure way of starting a php session. 
//session_start();

$email=trim($_POST["email"]);
$password=trim($_POST["password"]);
$length=intval(trim($_POST['length']));
$login_status=("");

//clase para almacenar la estructura de la respuesta
$response = new StdClass();

if(db_connection()){
	//echo("FALLO AL CONECTAR");
	$response->code_operation="login";
	$response->status="false";
	$response->description="fallo al conectar con db";
	echo json_encode($response);
	
}else if($validation_status = loginValidator($email, $password, $length)){
	//echo $validation_status;
	$response->code_operation="login";
	$response->status="false";
	$response->description=$validation_status;
	echo json_encode($response);
	
}else{ 
	if($response=login($email,$password,$mysqli))
	{
		echo $response;
		exit;
	}
	else
	{
		//echo("Error general");
		$response->code_operation="login";
		$response->status="false";
		$response->description="error";
		echo json_encode($response);
		exit;
	}
}

//FUNCION DE LOGIN
function login($email, $password, $mysqli) 
{	
	if ($stmt = $mysqli->prepare("SELECT id, username, password, salt FROM members WHERE email = ? LIMIT 1")) 
	{ 
		$stmt->bind_param('s', $email); 
		$stmt->execute();
		$stmt->store_result();
		$stmt->bind_result($user_id, $username, $db_password, $salt); 
		$stmt->fetch();
		
		//ALMACENAR LA CONTRASEÑA JUNTO CON UN SALT ALEATORIO ES MAS SEGURO QUE EL HASH DE LA CONTRASEÑA UNICAMENTE
		//comprobar que la contraseña enviada junto con el salt recuperado es correcto
		//$password = hash('sha512', $password.$salt); 
		
		if($stmt->num_rows == 1)
		{         
			if(checkbrute($user_id, $mysqli)==true)
			{
				$response->code_operation="login";
				$response->status="false";
				$response->description="Su cuenta ha sido bloqueada";
				echo json_encode($response);
				return 1;
			}	
			else
			{
				if(password_verify($password,$db_password))//$db_password == $password
				{ 
					//$user_browser = $_SERVER['HTTP_USER_AGENT'];
					//PREVENIR ATAQUES XSS, SOLO SE PERMITEN LOS CARACTERES INDICADOS, LOS DEMAS SE ELIMINAN
					// XSS protection as we might print this value 
					//$user_id = preg_replace("/[^0-9]+/", "", $user_id); 
					//$_SESSION['user_id'] = $user_id; 
					// XSS protection as we might print this value
					//$username = preg_replace("/[^a-zA-Z0-9_\-]+/", "", $username); 
					//$_SESSION['username'] = $username;
					//IDENTIFICADOR DE SESION, EL HASH DE PASS.USER_BROW ES UNA CLAVE DIFICIL DE REPETIR POR LO QUE SIRVE COMO IDENTIFICADOR DE SESION
					//$_SESSION['login_string'] = hash('sha512', $password.$user_browser);
					// Login successful.
					
					$response->code_operation="login";
					$response->status="true";
					$response->user_id=$user_id;
					$response->user_email=$email;
					$response->user_name=$username;
					$response->description="login OK";
					echo json_encode($response);
					return 1;	
				}
				else
				{
					//SI EL LOGIN NO ES CORRECTO SE ENVIA COMO INTENTO DE LOGIN FALLIDO
					$now = time();
					$mysqli->query("INSERT INTO login_attempts (user_id, time) VALUES ('$user_id', '$now')");
					
					$response->code_operation="login";
					$response->status="false";
					$response->description="Credenciales incorrectas";
					echo json_encode($response);
					return 1;
				}
			}
		}
		else
		{
			$response->code_operation="login";
			$response->status="false";
			$response->description="El usuario no existe";
			echo json_encode($response);
			return 1;
		}
	}
	else
	{	
		$response->code_operation="login";
		$response->status="false";
		$response->description="Credenciales incorrectas";
		echo json_encode($response);
		return 1;
	}
}


?>
