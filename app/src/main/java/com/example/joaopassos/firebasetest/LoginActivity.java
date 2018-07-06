package com.example.joaopassos.firebasetest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mSenha;
    private Button mLoginBtn;

    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Entrar");

        mEmail = (EditText) findViewById(R.id.login_email);
        mSenha = (EditText) findViewById(R.id.login_senha);
        mLoginBtn = (Button) findViewById(R.id.login_btn);

        mLoginProgress = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String email = mEmail.getText().toString();
                String password = mSenha.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    mLoginProgress.setTitle("Entrando");
                    mLoginProgress.setMessage("Por favor aguarde enquanto verificamos dados!");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();


                    loginUser(email, password);
                }else {

                    mLoginProgress.hide();

                    Toast.makeText(LoginActivity.this, "Não foi possível acessar sua conta. Por favor tente novamente", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if(task.isSuccessful()){

                    mLoginProgress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);

                } else{

                    String error = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        error = "Email Invalido";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = "Senha Invalida!";
                    } catch (Exception e) {
                        error = "Verifique se há conexão com a internet";
                        e.printStackTrace();
                    }

                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();

                    /*mLoginProgress.hide();

                    Toast.makeText(LoginActivity.this, "Não foi possível acessar sua conta. Por favor tente novamente",Toast.LENGTH_LONG).show();*/

                }
            }
        });
    }
}
