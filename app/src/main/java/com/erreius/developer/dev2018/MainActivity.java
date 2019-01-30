package com.erreius.developer.dev2018;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {

    JSONParser jParser = new JSONParser();
    private static String url_Servicio = "https://appcodigosservicios.erreius.com/api/Login/";
    private static final String TAG_SUCCESS = "StatusCode";
    private static final String TAG_USER = "UserName";
    private ProgressDialog pDialog;
    private UserLoginTask mAuthTask = null;
    private String IDuser;
    // UI references.
    private static MainActivity ins;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    //FaceBoook
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Button fbbutton;
    //Google Plus
    private SignInButton signInButton;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private int SIGN_IN = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Erreius");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //FaceBook Login
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        fbbutton = (Button) findViewById(R.id.login_button);

        fbbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFblogin();
            }
        });

        //Google Plus Login

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Iniciar con Google");
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, SIGN_IN);
            }
        });

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setRawInputType(Configuration.KEYBOARD_12KEY);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setRawInputType(Configuration.KEYBOARD_12KEY);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("Email", null);
        if (restoredText != null) {
            String name = prefs.getString("Email", "No name defined");//"No name defined" is the default value.
            String password = prefs.getString("Password", "None"); //0 is the default value.
            mEmailView.setText(name);
            mPasswordView.setText(password);
            attemptLogin();
        }
        else {
            String usuariored = prefs.getString("tipored", null);
            if (usuariored != null)
            {
                Intent myIntent = new Intent(MainActivity.this, WebViewApp.class);
                String id = prefs.getString("id", "No name defined");//"No name defined" is the default value.
                String name = prefs.getString("name", "None"); //0 is the default value.
                String fullname = prefs.getString("fullname", "No name defined");//"No name defined" is the default value.
                String tipored = prefs.getString("tipored", "None"); //0 is the default value.
                myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                myIntent.putExtra("id", id);
                myIntent.putExtra("name", name);
                myIntent.putExtra("fullname", fullname);
                myIntent.putExtra("tipored", tipored);
                MainActivity.this.startActivity(myIntent);
            }
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void onFblogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    public static final String TAG_ERROR = "Error";
                    public static final String TAG_CANCEL = "a";

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        System.out.println("Success");
                        GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject json, GraphResponse response) {
                                        if (response.getError() != null) {
                                            // handle error
                                            System.out.println("ERROR");
                                        } else {
                                            System.out.println("Success");
                                            try {
                                                String jsonresult = String.valueOf(json);
                                                System.out.println("JSON Result"+jsonresult);
                                                String fullname = json.getString("name");
                                                String iduser = json.getString("id");
                                                String name = json.getString("name");

                                                Intent myIntent = new Intent(MainActivity.this, WebViewApp.class);
                                                myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                myIntent.putExtra("id", iduser);
                                                myIntent.putExtra("name", name);
                                                myIntent.putExtra("fullname", fullname);
                                                myIntent.putExtra("tipored", "F");
                                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                                editor.putString("id", iduser);
                                                editor.putString("name", name);
                                                editor.putString("fullname", fullname);
                                                editor.putString("tipored", "F");
                                                editor.apply();
                                                LoginManager.getInstance().logOut();
                                                MainActivity.this.startActivity(myIntent);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                }).executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG_CANCEL,"On cancel");
                        Toast.makeText(MainActivity.this,"On cancel: ",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG_ERROR,error.toString());
                        Toast.makeText(MainActivity.this,"Error: " + error.toString(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            final GoogleSignInAccount acct = result.getSignInAccount();

            String iduser = acct.getId();
            String name = acct.getGivenName();
            String fullname = acct.getDisplayName();
            //Toast.makeText(this, "id = " + acct.getId(), Toast.LENGTH_LONG).show();
            // String photourl = acct.getPhotoUrl().toString();
            //final String givenname="",familyname="",displayname="",birthday="";
            Intent myIntent = new Intent(MainActivity.this, WebViewApp.class);
            myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            myIntent.putExtra("id", iduser);
            myIntent.putExtra("name", name);
            myIntent.putExtra("fullname", fullname);
            myIntent.putExtra("tipored", "G");

            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("id", iduser);
            editor.putString("name", name);
            editor.putString("fullname", fullname);
            editor.putString("tipored", "G");
            editor.apply();

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }
            MainActivity.this.startActivity(myIntent);

        } else {
            //If login fails

            Toast.makeText(this, "Login Failed: " + result.getStatus(), Toast.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Email", email);
        editor.putString("Password", password);
        editor.apply();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
            new UserLoginTask(email, password).execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private ProgressDialog progressDialog;   // class variable

    private void showProgressDialog(String title, String message){
        progressDialog = new ProgressDialog(this);

        progressDialog.setTitle(title); //title

        progressDialog.setMessage(message); // message

        progressDialog.setCancelable(false);

        progressDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Por favor espere...", "Validando..");
        }

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean flag = false;
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("UserName", mEmail));
            nameValuePairs.add(new BasicNameValuePair("Password", mPassword));

            String Resultado="";
            JSONObject json = jParser.makeHttpRequest(url_Servicio + "AuthenticateUser", "POST", nameValuePairs);

            if (json != null){
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 200){
                    //IDuser = json.getString(TAG_USER);
                    flag = true;}
            } catch (JSONException e) {
                e.printStackTrace();
                Resultado = e.getMessage();
            }
            }
            return flag;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(progressDialog != null && progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            mAuthTask = null;
            if (success) {
                Intent myIntent = new Intent(MainActivity.this, WebViewApp.class);
                myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                //myIntent.putExtra("key", IDuser);
                myIntent.putExtra("email", mEmail);
                myIntent.putExtra("password", mPassword);
                MainActivity.this.startActivity(myIntent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

            }
        }

       // @Override
       // protected void onCancelled() {
         //   mAuthTask = null;
       // }
    }

    public void postData(){
        HashMap data = new HashMap();
        data.put("UserName","91490");
        data.put("Password","23072");

        RequestQueue requstQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, "https://appcodigosservicios.erreius.com/api/Login/AuthenticateUser",new JSONObject(data),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int jsonResponse = response.getInt(TAG_SUCCESS);
                            Intent myIntent = new Intent(MainActivity.this, WebViewApp.class);
                            myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            myIntent.putExtra("email", "91490");
                            myIntent.putExtra("password", "23072");
                            MainActivity.this.startActivity(myIntent);
                            Toast.makeText(getApplicationContext(), "Toast: " + jsonResponse, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROE " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //here I want to post data to sever
        };
        requstQueue.add(jsonobj);

    }
}

