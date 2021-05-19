package ehu.das.myconnect.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DeleteUserDialog;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.service.ServerWorker;

public class Preferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_config);
        Preference button = (Preference) getPreferenceManager().findPreference("delete_account");
        Preferences preferences = this;
        if (button != null) {
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        DeleteUserDialog deleteUserDialog = new DeleteUserDialog();
                        deleteUserDialog.preferences = preferences;
                        deleteUserDialog.show(getActivity().getSupportFragmentManager(), "delete");
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // https://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
        // Si se cambia el idioma reiniciar la aplicación con el idioma cambiado
        if (key.equals("language")) {
            String idioma = "";
            if (sharedPreferences.getString(key, "español").equals("Spanish") || sharedPreferences.getString(key, "Español").equals("Español")) {
                idioma = "es";
            } else {
                idioma = "en";
            }
            Log.i("Idioma", sharedPreferences.getString(key, "abcd"));
            Locale nuevaloc = new Locale(idioma);
            Locale.setDefault(nuevaloc);
            Configuration configuration =
                    getActivity().getBaseContext().getResources().getConfiguration();
            configuration.setLocale(nuevaloc);
            configuration.setLayoutDirection(nuevaloc);
            Context context =
                    getActivity().getBaseContext().createConfigurationContext(configuration);
            getActivity().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            getActivity().finish();
            Intent i = getActivity().getIntent();
            startActivity(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void deleteUser() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
        Data data = new Data.Builder()
                .putString("user", LoginFragment.username)
                .putString("action", "deleteUser")
                .putString("script", "delete_user.php")
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        if (!status.getOutputData().getString("result").equals("0")) {
                            FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(getContext(), getResources().getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();
                                    Navigation.findNavController(getView()).navigate(R.id.action_preferences_to_loginFragment);
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.error_deleting_user), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}