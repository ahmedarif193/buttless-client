package com.buttless.client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
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
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Objects;

public class ActivityWithdraw extends AppCompatActivity {

    public static final String PREFS_NAME = "FB_DTLHS";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_POINTS = "points";
    private static final String KEY_MAIL = "mail";
    private static final String KEY_TITLE = "title";

    private String apiUrl, storageUser;
    private TextView txtHomePoints, txtEstimated;
    private ShimmerFrameLayout mShimmerViewWithdraw;
    private EditText edtPayPalMail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //get api url
        apiUrl = getString(R.string.url_api);
        //get all settings from local storage
        SharedPreferences shaPrefHome = Objects.requireNonNull(ActivityWithdraw.this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        //define the home points textview
        txtHomePoints = findViewById(R.id.home_points);

        txtEstimated = findViewById(R.id.txtEstimated);
        //pick the facebook id from local storage logged user
        storageUser = shaPrefHome.getString("fb_id", "");
        edtPayPalMail = findViewById(R.id.edtPayPalMail);
        mShimmerViewWithdraw = findViewById(R.id.shimmer_view_withdraw);
        mShimmerViewWithdraw.startShimmerAnimation();

        txtHomePoints.setVisibility(View.GONE);

        getPoints();
    }

    private void getPoints() {
        //define a json request
        JSONObject request = new JSONObject();
        try {
            //set the parameter
            request.put(KEY_USERNAME, storageUser);
        } catch (JSONException e) {
            //print a error, or do something
            e.printStackTrace();
        }
        //apiurl + php points file, to get user points
        String requestUserPoints = apiUrl + "points.php";
        //define a array
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, requestUserPoints, request, response -> {
                    //show a dialog
                    try {
                        if (response.getInt(KEY_STATUS) == 0) {
                            txtHomePoints.setText(response.getString(KEY_POINTS));
                            mShimmerViewWithdraw.stopShimmerAnimation();
                            mShimmerViewWithdraw.setVisibility(View.GONE);
                            txtHomePoints.setVisibility(View.VISIBLE);

                            double value = Double.parseDouble(response.getString(KEY_POINTS));
                            Double valueTwo = value / 1000;
                            String dc = new DecimalFormat("##.00").format(valueTwo);

                            txtEstimated.setText(getString(R.string.estimated, dc));
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(ActivityWithdraw.this).create();
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
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError){
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
        finish();
    }

    public void closeWithdrawActivity(View view) {
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
        finish();
    }

    boolean isEmailValid(CharSequence email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void withdraw(View view){
        if (edtPayPalMail.getText().toString().trim().length() < 1){
            Toast.makeText(this, this.getResources().getString(R.string.error_empty_mail), Toast.LENGTH_LONG).show();
        } else if (!isEmailValid(edtPayPalMail.getText().toString())){
            Toast.makeText(this, this.getResources().getString(R.string.error_invalid_mail), Toast.LENGTH_LONG).show();
        } else if (txtHomePoints.getText().toString().equals("0")){
            Toast.makeText(this, this.getResources().getString(R.string.error_no_balance), Toast.LENGTH_LONG).show();
        } else {
            JSONObject request = new JSONObject();
            try {
                request.put(KEY_USERNAME, storageUser);
                request.put(KEY_POINTS, txtHomePoints.getText().toString());
                request.put(KEY_MAIL, edtPayPalMail.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String withdraw_url = apiUrl + "withdraw.php";
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                    (Request.Method.POST, withdraw_url, request, response -> {
                        try {
                            if (response.getInt(KEY_STATUS) == 0) {
                                AlertDialog alertDialog = new AlertDialog.Builder(ActivityWithdraw.this).create();
                                alertDialog.setTitle(response.getString(KEY_TITLE));
                                alertDialog.setMessage(response.getString(KEY_MESSAGE));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, this.getResources().getString(R.string.ok),
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();
                                        });
                                alertDialog.show();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(ActivityWithdraw.this).create();
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
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                            } else if(error instanceof NoConnectionError){
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                            } else if (error instanceof AuthFailureError) {
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                            } else if (error instanceof ServerError) {
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            } else if (error instanceof ParseError) {
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ActivityWithdraw.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ignored) {

                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
        }
    }
}