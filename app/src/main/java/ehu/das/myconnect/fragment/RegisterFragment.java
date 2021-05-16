package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.list.ServerListAdapter;
import ehu.das.myconnect.service.ServerWorker;

import static android.content.ContentValues.TAG;

public class RegisterFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button login = getActivity().findViewById(R.id.login_register);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });
        Button register = getActivity().findViewById(R.id.signup_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
        EditText userField = getActivity().findViewById(R.id.registerUser);
        EditText emailField = getActivity().findViewById(R.id.registerEmail);
        EditText passwordField = getActivity().findViewById(R.id.registerPassword);
        EditText repeatPasswordField = getActivity().findViewById(R.id.registerRepeatPassword);
        if (userField.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), "Insert a username", Toast.LENGTH_SHORT).show();
        }
        if (!emailField.getText().toString().contains("@") || !(emailField.getText().toString().length() > 0) || !emailField.getText().toString().contains(".")) {
            Toast.makeText(getContext(), "Insert a valid email", Toast.LENGTH_SHORT).show();
        } else if (passwordField.getText().toString().length() < 8) {
            Toast.makeText(getContext(), "Password must have more than 8 characters", Toast.LENGTH_SHORT).show();
        } else if (!passwordField.getText().toString().equals(repeatPasswordField.getText().toString())) {
            Toast.makeText(getContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
        } else {
            Data data = new Data.Builder()
                    .putString("action", "register")
                    .putString("script", "register.php")
                    .putString("user", userField.getText().toString())
                    .putString("email", emailField.getText().toString())
                    .putString("password", passwordField.getText().toString())
                    .build();
            OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                    .observe(getActivity(), status -> {
                        if (status != null && status.getState().isFinished()) {
                            String result = status.getOutputData().getString("result");
                            Log.i("register", result);
                            if (result.equals("0")) {
                                Toast.makeText(getContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                            }
                            else if (result.equals("1")) {
                                Toast.makeText(getContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                            }
                            else if (result.equals("2")) {
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d(TAG, "createUserWithEmail:success");
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                    LoginFragment.user = user;
                                                    Toast.makeText(getContext(), "RegisterSuccesful", Toast.LENGTH_SHORT).show();
                                                    Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_loginFragment);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(getActivity().getApplicationContext(), "Register failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                loadingDialog.dismiss();
                                            }
                                        });
                            }
                            else if (result.equals("3")) {
                                Toast.makeText(getContext(), "Internal server error, try later", Toast.LENGTH_SHORT).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
            WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
        }
    }
}