package com.example.shauryamanocha.mapmarkers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class googleSignIn extends AppCompatActivity  {
    Button sign;
    GoogleSignInClient signInClient;
    MapsActivity maps;
    boolean schoolSelected = false;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    public static User user = new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("244075201852-s4t9s0680jetsejvpnhlqa3bf0n5v3e2.apps.googleusercontent.com")
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this,gso);
        sign = findViewById(R.id.googleButton);
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        maps = new MapsActivity();
    }
    @Override
    public void onStart(){
        super.onStart();

    }
    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authorizeWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }


    private void authorizeWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    user.name = auth.getCurrentUser().getDisplayName();
                    user.id = auth.getUid();
                    user.school = schoolSelect.school;
                    //MapsActivity.test(MapsActivity.auth.getCurrentUser());
                    Toast.makeText(googleSignIn.this,"Signed in ", Toast.LENGTH_SHORT).show();
                    Log.d("googleAuth","Signed in");
                    startActivity(new Intent(googleSignIn.this,schoolSelect.class));
                    MapsActivity.user = user;
                    MapsActivity.signedIn = true;
                }else{
                    Toast.makeText(googleSignIn.this,"Authentication Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
