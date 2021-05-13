package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.ServerManagmentFragment;

public class LoadingDialog extends DialogFragment {


    public LoadingDialog() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loadingLayout = inflater.inflate(R.layout.loading_layout, null);
        WebView webView = loadingLayout.findViewById(R.id.webView);
        webView.loadDataWithBaseURL("file:///android_res/drawable/", "<style>img{display: inline;height: auto;max-width: 100%;}</style><img src='loading.jpg' />", "text/html", "utf-8", null);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        builder.setView(loadingLayout);
        return builder.create();
    }
}
