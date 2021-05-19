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
        $keyPem = $parametros["keyPem"]; 

        $result = mysqli_query($con, "SELECT NombreServidor FROM Servidor WHERE NombreServidor = '$serverName' AND NombreUsuario = '$userName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            if ($row = mysqli_fetch_array($result)) { //Comprobamos si el servidor existe
                echo 'Error';
            } else {
                if ($keyPem == 0) {
                    $password = password_hash($password, PASSWORD_DEFAULT);
                }
            
                $result2 = mysqli_query($con, "INSERT INTO Servidor (NombreServidor,Usuario,Host,Puerto,Contrasena,NombreUsuario,PemFile) VALUES ('$serverName','$user','$host','$port','$password','$userName','$keyPem')");

                if (!$result2) {
                    echo 'Ha ocurrido algún error: ' . mysqli_error($con);
                }
            }
        }
    } else if ($accion == "serverData") {
        $userName = $parametros["userName"]; 
        $result = mysqli_query($con, "SELECT NombreServidor,Usuario,Host,Puerto,PemFile FROM Servidor WHERE NombreUsuario = '$userName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            $names = array();
            $users = array();
            $hosts = array();
            $ports = array();
            $pems = array();

            while($row = mysqli_fetch_array($result)){
                $names[] = $row["NombreServidor"];
                $users[] = $row["Usuario"];
                $hosts[] = $row["Host"];
                $ports[] = $row["Puerto"];
                $pems[] = $row["PemFile"];
            }                
            
            $arrayresultados = array(
                'serversNames' => $names,
                'users' => $users,
                'hosts' => $hosts,
                'ports' => $ports,
                'pems' => $pems,
                );
            echo json_encode($arrayresultados);
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
        $userName = $parametros["userName"]

        $result = mysqli_query($con, "SELECT NombreServidor FROM Servidor WHERE NombreServidor = '$serverName'");
        if (!$result) {
            echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        } else {
            if ($row = mysqli_fetch_array($result)) { //Comprobamos si el servidor existe
                echo 'Error';
            } else { 
                $result2 = mysqli_query($con, "UPDATE Servidor SET NombreServidor = '$serverName', Usuario = '$user', Host = '$host', Puerto = '$port' WHERE NombreServidor = '$oldServerName' AND NombreUsuario = '$userName'");

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