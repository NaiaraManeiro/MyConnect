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

    $accion = $parametros["action"]; 

    if ($accion == "addServer") {
        $user = $parametros["user"]; 
        $host = $parametros["host"]; 
        $port = $parametros["port"];
        $password = $parametros["password"]; 
        $serverName = $parametros["serverName"]; 
        $userName = $parametros["userName"]; 

        $result = mysqli_query($con, "SELECT NombreServidor FROM Servidor WHERE NombreServidor = '$serverName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            if ($row = mysqli_fetch_array($result)) { //Comprobamos si el servidor existe
                echo 'Error';
            } else {
                $password_encript = password_hash($password, PASSWORD_DEFAULT);
            
                $result2 = mysqli_query($con, "INSERT INTO Servidor (NombreServidor,Usuario,Host,Puerto,Contrasena,NombreUsuario) VALUES ('$serverName','$user','$host','$port','$password_encript', '$userName')");

                if (!$result2) {
                    echo 'Ha ocurrido algún error: ' . mysqli_error($con);
                }
            }
        }
    } else if ($accion == "serverData") {
        $userName = $parametros["userName"]; 
        $result = mysqli_query($con, "SELECT NombreServidor,Usuario,Host,Puerto FROM Servidor WHERE NombreUsuario = '$userName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            $names = array();
            $users = array();
            $hosts = array();
            $ports = array();

            while($row = mysqli_fetch_array($result)){
                $names[] = $row["NombreServidor"];
                $users[] = $row["Usuario"];
                $hosts[] = $row["Host"];
                $ports[] = $row["Puerto"];
            }                
            
            $arrayresultados = array(
                'serversNames' => $names,
                'users' => $users,
                'hosts' => $hosts,
                'ports' => $ports,
                );
            echo json_encode($arrayresultados);
        }
    } else if ($accion == "infoServer") { 
        $serverName = $parametros["serverName"];
        $result = mysqli_query($con, "SELECT Usuario,Host,Puerto FROM Servidor WHERE NombreServidor = '$serverName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {

            if($row = mysqli_fetch_assoc($result)) {
                $arrayresultados = array(
                    'user' => $row['Usuario'],
                    'host' => $row['Host'],
                    'port' => $row['Puerto'],
                    );
                echo json_encode($arrayresultados);
            }
        }
    } else if ($accion == "removeServer") { 
        $serverName = $parametros["serverName"];
        $result = mysqli_query($con, "DELETE FROM Servidor WHERE NombreServidor = '$serverName'");

        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            echo 'Remove';
        }
    } else if ($accion == "editServer") { 
        $user = $parametros["user"]; 
        $host = $parametros["host"]; 
        $port = $parametros["port"];
        $serverName = $parametros["serverName"]; 
        $oldServerName = $parametros["oldServerName"]; 

        $result = mysqli_query($con, "SELECT NombreServidor FROM Servidor WHERE NombreServidor = '$serverName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            if ($row = mysqli_fetch_array($result)) { //Comprobamos si el servidor existe
                echo 'Error';
            } else { 
                $result2 = mysqli_query($con, "UPDATE Servidor SET NombreServidor = '$serverName', Usuario = '$user', Host = '$host', Puerto = '$port' WHERE NombreServidor = '$oldServerName'");

                if (!$result2) {
                    echo 'Ha ocurrido algún error: ' . mysqli_error($con);
                }
            }
        }
    }
}

//Cerrar conexión
$con->close();

?>