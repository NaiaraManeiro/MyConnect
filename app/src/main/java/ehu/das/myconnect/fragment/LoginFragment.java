package ehu.das.myconnect.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.service.ServerWorker;

import static android.content.ContentValues.TAG;


public class LoginFragment extends Fragment {

    public static FirebaseUser user = null;
    private LoadingDialog loadingDialog;
    public static String username = "";

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        // Si ha iniciado sesión lleva a serveList
        if (currentUser != null) {
            loadingDialog = new LoadingDialog();
            loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
            user = currentUser;
            try {
                getUsername(currentUser.getEmail());
            } catch (IllegalArgumentException e) {
                loadingDialog.dismiss();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Permite hacer login al usuario
        super.onActivityCreated(savedInstanceState);
        ImageView conf = getActivity().findViewById(R.id.confLogin);
        conf.setColorFilter(Color.WHITE);
        conf.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_preferences);
        });
        Button login = getActivity().findViewById(R.id.login_login);
        login.setOnClickListener(v -> {
            // Se hace una petición para comprobar si los usuarios son correctos
            loadingDialog = new LoadingDialog();
            loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
            EditText emailUserField = getActivity().findViewById(R.id.emailLogin);
            EditText passwordField = getActivity().findViewById(R.id.passwordLogin);
            if (!(emailUserField.getText().toString().length() > 0)) {
                Toast.makeText(getContext(), getResources().getString(R.string.insert_email_username), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            } else if (passwordField.getText().toString().length() < 8) {
                Toast.makeText(getContext(), getResources().getString(R.string.password_validity), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            } else if (!emailUserField.getText().toString().contains("@") || !emailUserField.getText().toString().contains(".")) {
                    Data data = new Data.Builder()
                            .putString("action", "login")
                            .putString("script", "login.php")
                            .putString("user", emailUserField.getText().toString())
                            .putString("password", passwordField.getText().toString())
                            .build();
                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    Log.i("login", "Results:" +  result);
                                    if (result.equals("0")) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                    else if (result.equals("1") || result.trim().equals("")) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                    else {
                                        Log.i("login", "Results2:" +  result);
                                        signInFirebase(result, passwordField.getText().toString());
                                    }
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
                else {
                    signInFirebase(emailUserField.getText().toString(), passwordField.getText().toString());
                }

        });
        Button register = getActivity().findViewById(R.id.signup_login);
        register.setOnClickListener(new View.OnClickListener() {
            // Lleva a la pantalla de registro
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });
        TextView reset = getActivity().findViewById(R.id.forgotPassword);
        reset.setOnClickListener(v -> {
            EditText emailField = getActivity().findViewById(R.id.emailLogin);
            if (emailField.getText().toString().length() == 0) {
                Toast.makeText(getContext(), getResources().getString(R.string.reset_message),Toast.LENGTH_SHORT).show();
            } else {
                LoadingDialog ld = new LoadingDialog();
                ld.show(getActivity().getSupportFragmentManager(), "loading");
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                ld.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), getResources().getString(R.string.error_sending_email), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            });

    }

    public void signInFirebase(String email, String password) {
        // Hace login en firebase
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            getUsername(email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            loadingDialog.dismiss();
                        }
                    }
                });
    }

    private void getUsername(String email) {
        // Obtiene el nombre de usuario asociado a un email
        Data data = new Data.Builder()
                .putString("action", "user")
                .putString("script", "user.php")
                .putString("email", email)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        Log.i("login2", result);
                        username = result;
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                        try {
                            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_serverListFragment);
                        } catch (IllegalStateException e) {}
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}