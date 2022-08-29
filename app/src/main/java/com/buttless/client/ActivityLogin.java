package com.buttless.client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ActivityLogin extends AppCompatActivity {

    public static final String PREFS_NAME = "FB_DTLHS";

    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static final String KEY_FBID = "fb_id";

    TextView txtUsername, txtEmail;
    private ProgressDialog pDialog;
    private String api_url;

    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        api_url = getString(R.string.url_api);

        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        SharedPreferences shaPrefHome = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
  

        loginButton = findViewById(R.id.login_button);
        loginButton.setPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());
                                try {
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday"); // 01/31/1980 format
                                    String name = object.getString("name"); // 01/31/1980 format
                                    String id = object.getString("id"); // 01/31/1980 format

                                    Log.v("API123", birthday);
                                    Log.v("API123", email);
                                    Log.v("API123", name);
                                    Log.v("API123", id);

                                    registerUser(id,name,email);
                                    
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void registerUser(final String fb_id, final String full_name, final String email){

        displayLoader();
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_FBID, fb_id);
            request.put(KEY_FULL_NAME, full_name);
            request.put(KEY_EMAIL, email);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("API123 request : ", String.valueOf(request));
        String login_url = api_url + "fb/usr/register";

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, login_url, request, response -> {
                    pDialog.dismiss();
                    try {
                        Log.v("API123 before if", "------------------");
                        Log.v("API123 response : ", String.valueOf(response));

                        if (response.getInt(KEY_STATUS) == 0) {
                            Log.v("API123 after if", "------------------");
                            SharedPreferences.Editor edt = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            edt.putString(KEY_FBID, fb_id);
                            edt.putString(KEY_FULL_NAME, full_name);
                            edt.apply();
                            loadDashboard();
                            bruteForce();
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(ActivityLogin.this).create();
                            alertDialog.setTitle(this.getResources().getString(R.string.ops));
                            alertDialog.setMessage(response.getString(KEY_MESSAGE));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, this.getResources().getString(R.string.ok),
                                    (dialog, which) -> dialog.dismiss());
                            alertDialog.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    try {
                        if (error instanceof TimeoutError) {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError){
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        } else {
                            pDialog.dismiss();
                            Toast.makeText(ActivityLogin.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    private void bruteForce(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String fb_id = settings.getString(KEY_FBID, "");

                if (!fb_id.equals("")) {
                    loadDashboard();
                    handler.removeCallbacks(this);
                } else {
                    handler.postDelayed(this, 250);
                }
            }
        }, 250);
    }

    @Override
    public void onResume(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String fb_id = settings.getString(KEY_FBID, "");
                if (!fb_id.equals("")) {
                    loadDashboard();
                    handler.removeCallbacks(this);
                } else {
                    handler.postDelayed(this, 250);
                }
            }
        }, 250);
        super.onResume();
    }


    //TODO : to be used to gather the email with the rest 
    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, (object, response) -> {
                    Log.d("TAG", object.toString());
                    try {
                        String first_name = object.getString("first_name");
                        String last_name = object.getString("last_name");
                        String id = object.getString("id");
                        String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";
                        String full_nm = first_name + " " + last_name;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void loadDashboard() {
        Intent i = new Intent(this, ActivityHome.class);
        startActivity(i);
        finish();
    }

    private void displayLoader() {
        pDialog = new ProgressDialog(ActivityLogin.this);
        pDialog.setMessage(this.getResources().getString(R.string.autenticate));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}