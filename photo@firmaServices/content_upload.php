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
include '/lib/functions.php';
session_start();
$username=trim($_POST["user_name"]);
$useremail=trim($_POST["user_email"]);
$userid=trim($_POST["user_id"]);
$events=trim($_POST["events"]);
$imei=trim($_POST["imei"]);


if(db_connection()){
	//echo("FALLO AL CONECTAR");
	$response->code_operation="dataupload";
	$response->status="false";
	$response->description="fallo al conectar con db";
	echo json_encode($response);
	
}else if(user_check($mysqli,$username,$useremail,$userid)==false){
	//echo "login incorrecto";
	$response->code_operation="dataupload";
	$response->status="false";
	$response->description="credenciales incorrectas";
	echo json_encode($response);
	
}else{
 	$permitidos = array("image/jpg", "image/jpeg", "image/gif", "image/png", "application/octet-stream");
	$limite_kb = 4000;

	if (($t=!in_array($_FILES['imagen']['type'], $permitidos)) || ($_FILES['imagen']['size'] > $limite_kb * 1024) ){
		$response->code_operation="dataupload";
		$response->status="false";
		if(t==false){
			$response->description="Formato de imagen incorrecto";
		}else{
			$response->description="La imagen excede el tamaño permitido";
		}
		echo json_encode($response);
		
	}else if($_FILES['firma']==null){
		$response->code_operation="dataupload";
		$response->status="false";
		$response->description="No se adjuntó firma digital";
		echo json_encode($response);
		
	}else if ($username==null || $useremail==null || $userid == null || $events==null){
		//echo "falla el POST de datos";
		$response->code_operation="dataupload";
		$response->status="false";
		$response->description="Falta información del proceso";
		echo json_encode($response);
			
	}else{
		$photoName=basename($_FILES['imagen']['name']);
		$signName=basename($_FILES['firma']['name']);
		$photoHash=hash_file('md5', $_FILES['imagen']['tmp_name']);
		$signHash=hash_file('md5', $_FILES['firma']['tmp_name']);	
		$clientip=get_client_ip();
		
		if($insert_stmt = $mysqli->prepare("INSERT INTO transacctions (id_user, photo_name, sign_name, device_imei, client_ip, photo_hash, sign_hash) VALUES (?, ?, ?, ?, ?, ?, ?)")){
			$insert_stmt->bind_param('sssssss', $userid, $photoName, $signName, $imei, $clientip, $photoHash, $signHash); 
			$insert_stmt->execute();
			
			$transactId=$mysqli->insert_id; 
			
			$jdec=json_decode($events,true);
			
 			foreach($jdec as $key){
				$event_number=$key['EVENT_NUMBER'];
				$code=$key['CODE'];
				$description=$key['DESCRIPTION'];
				$dev_time=$key['DEVICE_TIME'];
				
				if($inserta = $mysqli->prepare("INSERT INTO operations (id_transac, code, description, device_time, event) VALUES ('$transactId', '$code', '$description', '$dev_time', '$event_number')")){
					$inserta->execute();
				}else{
					$response->code_operation="dataupload";
					$response->status="false";
					$response->description="Error en la base de datos";
					echo json_encode($response);
				}
			}	 
			
			$t=time();
			$db_date=date("Y-m-d",$t);
 
			$storage_dir='db_storage/'.$userid.'/'.$useremail.'/'.$db_date.'/'.$transactId.'/';
			mkdir($storage_dir,0777,true);
		
			if(!is_dir($storage_dir)){
				$response->code_operation="dataupload";
				$response->status="false";
				$response->description="error de almacenamiento";
				echo json_encode($response);
			
			}else{
				$ruta_imagen = $storage_dir . basename($_FILES['imagen']['name']);
				$ruta_firma = $storage_dir  . basename($_FILES['firma']['name']);
				
				move_uploaded_file($_FILES['imagen']['tmp_name'], $ruta_imagen);
				move_uploaded_file($_FILES['firma']['tmp_name'], $ruta_firma);
							
				sleep(2);
				
				$response->code_operation="dataupload";
				$response->status="true";
				$response->description="Almacenado correctamente";
				echo json_encode($response);
			}
		
		}else{
			$response->code_operation="dataupload";
			$response->status="false";
			$response->description="Error en la base de datos";
			echo json_encode($response);
		}
	}	
}

function get_client_ip() {
     $ipaddress = '';
     if ($_SERVER['HTTP_CLIENT_IP'])
         $ipaddress = $_SERVER['HTTP_CLIENT_IP'];
     else if($_SERVER['HTTP_X_FORWARDED_FOR'])
         $ipaddress = $_SERVER['HTTP_X_FORWARDED_FOR'];
     else if($_SERVER['HTTP_X_FORWARDED'])
         $ipaddress = $_SERVER['HTTP_X_FORWARDED'];
     else if($_SERVER['HTTP_FORWARDED_FOR'])
         $ipaddress = $_SERVER['HTTP_FORWARDED_FOR'];
     else if($_SERVER['HTTP_FORWARDED'])
         $ipaddress = $_SERVER['HTTP_FORWARDED'];
     else if($_SERVER['REMOTE_ADDR'])
         $ipaddress = $_SERVER['REMOTE_ADDR'];
     else
         $ipaddress = 'UNKNOWN';

     return $ipaddress; 
}
?>
