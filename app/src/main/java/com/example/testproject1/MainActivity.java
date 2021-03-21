package com.example.testproject1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    SignInButton signin;
    TextView tvname,tvemail;
    ImageView imgv;

    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;

//    private LoginButton facebooksignin;
//    private CallbackManager

    private LoginButton facebooksignin;
    private CallbackManager c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


      tvname = findViewById(R.id.tvname);
        tvemail = findViewById(R.id.tvemail);
       imgv = findViewById(R.id.imgv);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signin = findViewById(R.id.sign_in_button);
        signin.setSize(SignInButton.SIZE_STANDARD);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }

            }
        });

        facebooksignin = findViewById(R.id.facebooksignin);
        facebooksignin.setLoginBehavior( LoginBehavior.WEB_ONLY );

        c = CallbackManager.Factory.create();
        facebooksignin.setPermissions(Arrays.asList("email","public_profile"));
        facebooksignin.registerCallback(c, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("SocialMediaIntegration","Login successfull");
                Toast.makeText(MainActivity.this, "Logged in successfully!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Log.d("SocialMediaIntegration","Login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("SocialMediaIntegration","Login Error");
            }
        });

    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//    }
private void signIn() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
}
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        c.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken == null){
                LoginManager.getInstance().logOut();
                tvname.setText("");
                tvemail.setText("");
                imgv.setImageResource(0);
                Toast.makeText(MainActivity.this,"Logged out successfully!!", Toast.LENGTH_LONG).show();
                signin.setVisibility(View.VISIBLE);
                tvname.setVisibility(View.INVISIBLE);
                tvemail.setVisibility(View.INVISIBLE);
            }
            else {
                loaduserprofile(currentAccessToken);
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tokenTracker.stopTracking();
    }

    private void loaduserprofile(AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback(){
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {
            try {
                signin.setVisibility(View.INVISIBLE);
                String fname = object.getString("first_name");
                String lname = object.getString("last_name");
                String email = object.getString("email");
                String id = object.getString("id");
                String pic = object.getJSONObject("picture").getJSONObject("data").getString("url");

                tvname.setText(fname+" "+lname);
                tvname.setVisibility(View.VISIBLE);
                tvemail.setText(email);
                tvemail.setVisibility(View.VISIBLE);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.dontAnimate();

                Glide.with(MainActivity.this).load(pic).into(imgv);


            }catch (JSONException e){
                e.printStackTrace();

            }
        }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email,id,picture.width(150).height(150)");
        request.setParameters(parameters);
        request.executeAsync();
    }




    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                Toast.makeText(this,"user email : "+personEmail, Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(MainActivity.this,MainActivity2.class));
        } catch (ApiException e) {
            Log.d( "Login failed " , e.toString());
        }
    }
}
