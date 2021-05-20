package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import ehu.das.myconnect.MainActivity;
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
//        WebView webView = loadingLayout.findViewById(R.id.webView);
//        webView.loadDataWithBaseURL("file:///android_res/drawable/", "<style>img{display: inline;height: auto;width: auto;}</style><img src='loading_computer.' />", "text/html", "utf-8", null);
//        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        builder.setView(loadingLayout);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(800, 700);
    }
}
