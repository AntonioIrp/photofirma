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


function db_connection(){
  define("HOST", "localhost"); // The host you want to connect to.
  define("USER", "root"); // The database username.
  define("PASSWORD", ""); // The database password. 
  define("DATABASE", "photo@firma"); // The database name.
  error_reporting(0);
  global $mysqli; 
  
  $mysqli= new mysqli(HOST, USER, PASSWORD, DATABASE);
  if($mysqli->connect_error){
		return TRUE;  
  }else{
	  	//$acentos = $mysqli->query("SET NAMES 'utf8'");
		$mysqli->set_charset('utf8');
	  	return FALSE;
		//return $mysqli
  }
}
?>
