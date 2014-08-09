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

//COMPRUEBA LOS INTENTOS DE LOGIN DEL USUARIO
function checkbrute($user_id, $mysqli) 
{
  // Get timestamp of current time
  $now = time();
   
  $valid_attempts = $now - (2 * 60 * 60); //SE CONTABILIZAN LOS INTENTOS EN LAS ULTIMAS 2 HORAS 
  $mysql_query=("SELECT time FROM login_attempts WHERE user_id = ? AND time > ?");
  
  	if ($stmt = $mysqli->prepare($mysql_query))
  	{ 
	  	$stmt->bind_param('ii', $user_id,$valid_attempts); 
	  	$stmt->execute();
	  	$stmt->store_result();
	 	if($stmt->num_rows > 5)
		{
			 return true;
	  	} 
	  	else 
	  	{
		 	return false;
	  	}
	}
}



//COMPROBACION DEL LOGIN DEL USUARIO
function user_check($mysqli,$username,$useremail,$userid) {
 
   if($stmt=$mysqli->prepare("SELECT * FROM members WHERE username = ? and email = ? and id = ? limit 1")){
		$stmt->bind_param('ssi', $username,$useremail,$userid);
		$stmt->execute();
		$stmt->store_result();
		
		if($stmt->num_rows==1){
			return true;
		}else{
			return false;
		}
   }else{
		return false;
   }
}
?>
