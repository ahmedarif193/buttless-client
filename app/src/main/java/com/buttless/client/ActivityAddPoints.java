package com.buttless.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


public class ActivityAddPoints extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    public static final String PREF_FILE= "MyPref";

    public static final String PREFS_NAME = "FB_DTLHS";

    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_POINTS = "score";
    private static final String KEY_FBID = "fb_id";
    private static final String KEY_UUID = "uuid";

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    public String apiUrl, storageUser;
    private ShimmerFrameLayout mShimmerViewContainerItem;

    private ProgressDialog pDialog;

    
    //private List<String> sku = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_points);

        //ask for camera permission
        requestCamera();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //get api url
        apiUrl = getString(R.string.url_api);
        //get all settings from local storage
        SharedPreferences shaPrefHome = Objects.requireNonNull(ActivityAddPoints.this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        //pick the facebook id from local storage logged user
        storageUser = shaPrefHome.getString(KEY_FBID, "");

        new AsyncFetch().execute();

        barcodeView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
//        mShimmerViewContainerItem = findViewById(R.id.shimmer_view_container_item);
//        mShimmerViewContainerItem.startShimmerAnimation();
    }

//
//    private SharedPreferences getPreferenceObject() {
//        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
//    }
//
//    private SharedPreferences.Editor getPreferenceEditObject() {
//        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
//        return pref.edit();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(billingClient!=null){
//            billingClient.endConnection();
//        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                                  R.anim.slide_out_right);
        finish();
    }

    public void closeAddPointsActivity(View view) {
        finish();
    }

    public class AsyncFetch extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL(apiUrl + "add.php?username=" + storageUser);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }

            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.toString();
            }

            try {
                int response_code = conn.getResponseCode();
                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return (result.toString());
                } else {
                    return ("unsuccessful");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(ActivityAddPoints.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    boolean resultValue = false;
    Context context;
    public void addPointsToUser(String uuidQrcode){
        context = ActivityAddPoints.this;
        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityAddPoints.this);
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setPositiveButton("Return True", new
                DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        ((Activity) context).finish();

                        resultValue = true;
                    }
                });

        JSONObject request = new JSONObject();
        try {
            request.put(KEY_FBID, storageUser);
            request.put(KEY_UUID, uuidQrcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("API123 request : ", String.valueOf(request));

        String login_url = apiUrl + "qrcode/assing";
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, login_url, request, response -> {
                    try {
                        Log.v("API123 request : ", String.valueOf(response));

                        displayLoader();
                        AlertDialog alertDialog = new AlertDialog.Builder(ActivityAddPoints.this).create();
                        alertDialog.setTitle(this.getResources().getString(R.string.ops));
                        alertDialog.setMessage(response.getString(KEY_MESSAGE));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, this.getResources().getString(R.string.ok),
                                (dialog, which) -> ((Activity) context).finish());

                        alertDialog.show();
                        pDialog.dismiss();

                        if (response.getInt(KEY_STATUS) == 0){
                            

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    try {
                        if (error instanceof TimeoutError) {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError){
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivityAddPoints.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            barcodeView.setStatusText(result.getText());

            beepManager.playBeepSoundAndVibrate();
            addPointsToUser(lastText);
            //Added preview of scanned barcode 
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };
    private AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
    private void displayLoader() {
        pDialog = new ProgressDialog(ActivityAddPoints.this);
        pDialog.setMessage(this.getResources().getString(R.string.autenticate));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}