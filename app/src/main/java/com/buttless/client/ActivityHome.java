package com.buttless.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.buttless.client.Adapter.AdapterHomeHistory;
import com.buttless.client.Controllers.Fragments.MainFragment;
import com.buttless.client.models.DataPublic;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityHome extends AppCompatActivity {

    public static final String PREFS_NAME = "FB_DTLHS";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_POINTS = "score";
    private static final String KEY_FBID = "score";

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    private String apiUrl, storageUser;
    private TextView txtHomePoints;
    private ShimmerFrameLayout mShimmerViewContainer, mShimmerViewContainerHistory;
    private RecyclerView recHistory;
    private CoordinatorLayout corNoLonger;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MainFragment mainFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the contentview from xml layout
        setContentView(R.layout.activity_home);
        //get api url
        apiUrl = getString(R.string.url_api);
        //get all settings from local storage
        SharedPreferences shaPrefHome = Objects.requireNonNull(ActivityHome.this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        //define the home points textview
        txtHomePoints = findViewById(R.id.home_points);
        //pick the facebook id from local storage logged user
        storageUser = shaPrefHome.getString(KEY_FBID, "");

        recHistory = findViewById(R.id.userActivityList);
        corNoLonger = findViewById(R.id.coordinator_lyt);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                onItemsLoadComplete();

                mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
                mShimmerViewContainerHistory = findViewById(R.id.shimmer_view_container_history);

                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainerHistory.setVisibility(View.VISIBLE);

                mShimmerViewContainer.startShimmerAnimation();
                mShimmerViewContainerHistory.startShimmerAnimation();

                txtHomePoints.setVisibility(View.GONE);
                recHistory.setVisibility(View.GONE);
                corNoLonger.setVisibility(View.GONE);

                new AsyncFetch().execute();
                getPoints();
            } else {
                Toast.makeText(this, this.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                onItemsLoadComplete();
            }
        });

        new AsyncFetch().execute();

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainerHistory = findViewById(R.id.shimmer_view_container_history);

        mShimmerViewContainer.startShimmerAnimation();
        mShimmerViewContainerHistory.startShimmerAnimation();

        txtHomePoints.setVisibility(View.GONE);
        recHistory.setVisibility(View.GONE);
        corNoLonger.setVisibility(View.GONE);
        this.configureAndShowMainFragment();

        getPoints();
    }
    private void configureAndShowMainFragment(){

        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);

//        if (mainFragment == null) {
//            mainFragment = new MainFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.activity_main_frame_layout, mainFragment)
//                    .commit();
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainerHistory = findViewById(R.id.shimmer_view_container_history);

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainerHistory.setVisibility(View.VISIBLE);

        txtHomePoints.setVisibility(View.GONE);
        recHistory.setVisibility(View.GONE);
        corNoLonger.setVisibility(View.GONE);

        mShimmerViewContainer.startShimmerAnimation();
        mShimmerViewContainerHistory.startShimmerAnimation();

        new AsyncFetch().execute();
        getPoints();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getPoints() {
        //define a json request
        JSONObject request = new JSONObject();
        try {
            //set the parameter
            Log.d("API123 KEY_USERNAME - ", storageUser);
            request.put(KEY_USERNAME, storageUser);
        } catch (JSONException e) {
            //print a error, or do something
            e.printStackTrace();
        }
        //api/fb/[id]
        String requestUserPoints = apiUrl + "/fb/" + storageUser;
        //define a array
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, requestUserPoints, request, response -> {
                    //show a dialog
                    try {
                        Log.d("API123", String.valueOf(response.getInt(KEY_STATUS)));
                        if (response.getString(KEY_STATUS) == "0") {
                            txtHomePoints.setText(response.getString(KEY_POINTS));
                            mShimmerViewContainer.stopShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            txtHomePoints.setVisibility(View.VISIBLE);
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(ActivityHome.this).create();
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
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError){
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivityHome.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    @SuppressLint("CutPasteId")
    public void onItemsLoadComplete(){
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void openAddFundsActivity(View view) {
        Intent i = new Intent(this, ActivityAddPoints.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,
                                  R.anim.slide_out_left);
    }

    public void openWithdrawActivity(View view) {
        Intent i = new Intent(this, ActivityWithdraw.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
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
                url = new URL(apiUrl + "activity.php?username=" + storageUser);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
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

        @Override
        protected void onPostExecute(String result) {

            List<DataPublic> data=new ArrayList<>();
            try {
                JSONArray jArray = new JSONArray(result);
                for(int i=0;i<jArray.length();i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    DataPublic publicData = new DataPublic();
                    publicData.historyType = json_data.getString("type");
                    publicData.historyDate = json_data.getString("date");
                    publicData.historyStatus = json_data.getString("status");
                    publicData.historyValue = json_data.getString("value");
                    data.add(publicData);
                }
                onItemsLoadComplete();
                RecyclerView mRVUserActivity = findViewById(R.id.userActivityList);
                AdapterHomeHistory mAdapter = new AdapterHomeHistory(ActivityHome.this, data);
                mRVUserActivity.setAdapter(mAdapter);
                mRVUserActivity.setLayoutManager(new LinearLayoutManager(ActivityHome.this));
                mRVUserActivity.setVisibility(View.VISIBLE);
                mShimmerViewContainerHistory.setVisibility(View.GONE);
            } catch (JSONException e) {
                mShimmerViewContainerHistory.setVisibility(View.GONE);
                corNoLonger.setVisibility(View.VISIBLE);
                onItemsLoadComplete();
            }
        }
    }
}