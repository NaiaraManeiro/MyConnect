 package ehu.das.myconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ehu.das.myconnect.dialog.ConnectionLostDialog;
import ehu.das.myconnect.dialog.DeleteUserDialog;

 public class MainActivity extends AppCompatActivity  {

     private ConnectionLostDialog connectionLostDialog;
     // Clase anonima de receiver para cuando se cambia el estado de la conexion
     private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (!isConnectedToInternet(context)) {
                 Log.i("internet", "lost");
                 // muestra dialog para indicar este caso
                 connectionLostDialog = new ConnectionLostDialog();
                 connectionLostDialog.setCancelable(false);
                 connectionLostDialog.show(getSupportFragmentManager(), "delete_user");
             } else {
                 if (connectionLostDialog != null) {
                     connectionLostDialog.dismiss();
                     recreate();
                     connectionLostDialog = null;
                 }
                 Log.i("internet", "connected");
             }
         }

         // Comprueba si el dispositivo tiene conexion a internet
         public boolean isConnectedToInternet(Context context){
             ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
             if (connectivity != null)
             {
                 NetworkInfo[] info = connectivity.getAllNetworkInfo();
                 if (info != null)
                     for (int i = 0; i < info.length; i++)
                         if (info[i].getState() == NetworkInfo.State.CONNECTED)
                         {
                             return true;
                         }

             }
             return false;
         }
     };

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Pide los permisos, a futuro ponerlos cuando se necesiten
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    01);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    02);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    03);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    04);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    05);
        }
    }
        // getSupportActionBar().hide();


     @Override
     protected void onResume() {
         super.onResume();
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         registerReceiver(networkChangeReceiver, intentFilter);
     }

     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(networkChangeReceiver);
     }


 }