<?php

$DB_SERVER="localhost"; #la dirección del servidor
$DB_USER="Xnmaneiro001"; #el usuario para esa base de datos
$DB_PASS="3IFwPosJy"; #la clave para ese usuario
$DB_DATABASE="Xnmaneiro001_MyConnect"; #la base de datos a la que hay que conectarse

# Se establece la conexión:
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

#Comprobamos conexión
if (mysqli_connect_errno($con)) {
    echo 'Error de conexion: ' . mysqli_connect_error();
    exit();
} else {
    $parametros = json_decode( file_get_contents( 'php://input' ), true );

    $accion = $parametros["funcion"]; 

    if ($accion == "addServer") {
        $usuario = $parametros["usuario"]; 
        $host = $parametros["host"]; 
        $puerto = $parametros["puerto"];
        $contrasena = $parametros["contrasena"]; 
        $nombreServidor = $parametros["nombreServidor"]; 
        $nombreUsuario = $parametros["nombreUsuario"]; 

        $resultado = mysqli_query($con, "SELECT Nombre FROM Servidor WHERE NombreServidor = '$nombreServidor'");
        if (!$resultado) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            if ($fila = mysqli_fetch_array($resultado)) { //Comprobamos si el servidor existe
                echo 'Error';
            } else {
                $contrasena_encriptada = password_hash($contrasena, PASSWORD_DEFAULT);
            
                $resultado2 = mysqli_query($con, "INSERT INTO Servidor (NombreServidor,Usuario,Host,Puerto,Contrasena,NombreUsuario) VALUES ('$nombreServidor','$usuario','$host','$puerto','$contrasena_encriptada', '$nombreUsuario')");

                if (!$resultado2) {
                    echo 'Ha ocurrido algún error: ' . mysqli_error($con);
                }
            }
        }
    }
}

//Cerrar conexión
$con->close();

?>