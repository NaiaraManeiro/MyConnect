package ehu.das.myconnect.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

import ehu.das.myconnect.R;

public class Preferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_config);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // https://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
        // Si se cambia el idioma reiniciar la aplicación con el idioma cambiado
        Log.i("Preferencias", "Se ha cambiado a " + key);
        if (key.equals("list_preference_1")) {
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
}