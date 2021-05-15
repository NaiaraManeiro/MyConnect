package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.LoadingDialog;

import static android.content.ContentValues.TAG;


public class LoginFragment extends Fragment {

    public static FirebaseUser user = null;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            user = currentUser;
            try {
                Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_serverListFragment);
            } catch (IllegalArgumentException e) {

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
        super.onActivityCreated(savedInstanceState);
        Button login = getActivity().findViewById(R.id.login_login);
        login.setOnClickListener(v -> {
            EditText emailField = getActivity().findViewById(R.id.emailLogin);
            EditText passwordField = getActivity().findViewById(R.id.passwordLogin);
            if (!emailField.getText().toString().contains("@") || !(emailField.getText().toString().length() > 0) || !emailField.getText().toString().contains(".")) {
                Toast.makeText(getContext(), "Insert a valid email", Toast.LENGTH_SHORT).show();
            } else if (passwordField.getText().toString().length() < 8) {
                Toast.makeText(getContext(), "Password must have more than 8 characters", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    user = mAuth.getCurrentUser();
                                    Navigation.findNavController(getActivity().getCurrentFocus()).navigate(R.id.action_loginFragment_to_serverListFragment);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
            }
        });
        Button register = getActivity().findViewById(R.id.signup_login);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });
        TextView reset = getActivity().findViewById(R.id.forgotPassword);
        reset.setOnClickListener(v -> {
            EditText emailField = getActivity().findViewById(R.id.emailLogin);
            if (emailField.getText().toString().length() == 0) {
                Toast.makeText(getContext(), "Insert the email where you want to receive the reset email",Toast.LENGTH_SHORT).show();
            } else {
                LoadingDialog ld = new LoadingDialog();
                ld.show(getActivity().getSupportFragmentManager(), "loading");
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                ld.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Email sent!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "There was an error sending the email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            });

    }
}