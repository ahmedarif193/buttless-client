package com.breaklabs.wala;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.breaklabs.wala.Adapter.AdapterItem;
import com.breaklabs.wala.Data.DataPublic;
import com.breaklabs.wala.Utils.Security;
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

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class ActivityAddFunds extends AppCompatActivity implements PurchasesUpdatedListener {

    public static final String PREF_FILE= "MyPref";
    public static String PURCHASE_KEY= "consumable";
    public static String PRODUCT_ID= "consumable";

    public static final String PREFS_NAME = "FB_DTLHS";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_POINTS = "points";

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    public String apiUrl, storageUser, svdPoints;
    private ShimmerFrameLayout mShimmerViewContainerItem;

    private BillingClient billingClient;

    //private List<String> sku = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_funds);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //get api url
        apiUrl = getString(R.string.url_api);
        //get all settings from local storage
        SharedPreferences shaPrefHome = Objects.requireNonNull(ActivityAddFunds.this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        //pick the facebook id from local storage logged user
        storageUser = shaPrefHome.getString("fb_id", "");

        new AsyncFetch().execute();

        mShimmerViewContainerItem = findViewById(R.id.shimmer_view_container_item);
        mShimmerViewContainerItem.startShimmerAnimation();
    }

    private void initBilling(){
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(INAPP);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(ActivityAddFunds.this, getResources().getString(R.string.error_google_play_library), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }

    private int getPurchaseCountValueFromPref(){
        return getPreferenceObject().getInt( PURCHASE_KEY,0);
    }

    private void savePurchaseCountValueToPref(int value){
        getPreferenceEditObject().putInt(PURCHASE_KEY, value).commit();
    }

    public void buyItem(String bdSkuId, String bdPoints) {
        PRODUCT_ID = bdSkuId;
        PURCHASE_KEY = bdSkuId;
        svdPoints = bdPoints;

        if (billingClient.isReady()) {
            initiatePurchase();
        } else {
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase();
                    } else {
                        Toast.makeText(getApplicationContext(),"Error " + billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    private void initiatePurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(ActivityAddFunds.this, flowParams);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_item_not_found),Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(getApplicationContext(),this.getResources().getString(R.string.error_purchase_cancelled),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Error " + billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    void handlePurchases(List<Purchase>  purchases) {

        for(Purchase purchase:purchases) {
            if (PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.error_invalid_purchase), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!purchase.isAcknowledged()) {
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.consumeAsync(consumeParams, consumeListener);
                }
            } else if( PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
                Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.error_pending_purchase), Toast.LENGTH_SHORT).show();
            } else if(PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE){
                Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.error_purchase_status_unknown), Toast.LENGTH_SHORT).show();
            }
        }
    }

    ConsumeResponseListener consumeListener = new ConsumeResponseListener() {
        @Override
        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                int consumeCountValue=getPurchaseCountValueFromPref()+1;
                savePurchaseCountValueToPref(consumeCountValue);
                addPointsToUser();
            }
        }
    };

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            //To get key go to Developer Console > Select your app > Monetize > Monetization setup
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAigndNIdMS/PVIypxJ7TTfd4q0E/oCwuMgeXB79b5M/leShj3LXJOEySEmvxOUxFTmJqrYsbG1tIFnrAMYHWK0b/o05695UoDkjYpfDFOFwX1Yz9/m4tkHMbtPIEkxYPAMAQFzo0RUMkxqfSN474n6whR7F2UJruh+SNSRh7/3dgwu7lCY+ppQKUoaBr3mY8U63CsPqQaAZdrJFW4HKLiix05Tpxe+YP1LF7XMQfVCuBBM2MfJq8AfEEnzpNAP13BfxfIKNcAP2MmSTkdnhh1rvzmW20HcN6MHLsXXD5P+eoGWQ3Bxy6QrR0iwrQg7yaLVQk1w8tsRFvrClPNiQX5JwIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                                  R.anim.slide_out_right);
        finish();
    }

    public void closeAddFundsActivity(View view) {
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
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

        @Override
        protected void onPostExecute(String result) {

            List<DataPublic> data = new ArrayList<>();
            try {
                JSONArray jArray = new JSONArray(result);
                for(int i=0;i<jArray.length();i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    DataPublic publicData = new DataPublic();
                    publicData.itemValue = json_data.getString("value");
                    publicData.itemPoints = json_data.getString("points");
                    publicData.itemSkuId = json_data.getString("sku_id");
                    data.add(publicData);
                }
                RecyclerView mRVItem = findViewById(R.id.itemsList);
                AdapterItem mAdapter = new AdapterItem(ActivityAddFunds.this, data, ActivityAddFunds.this);
                mRVItem.setAdapter(mAdapter);
                mRVItem.setLayoutManager(new LinearLayoutManager(ActivityAddFunds.this));
                mRVItem.setVisibility(View.VISIBLE);
                mShimmerViewContainerItem.setVisibility(View.GONE);
                initBilling();
            } catch (JSONException e) {
                mShimmerViewContainerItem.setVisibility(View.GONE);
            }
        }
    }

    public void addPointsToUser(){
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_USERNAME, storageUser);
            request.put(KEY_POINTS, svdPoints);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String login_url = apiUrl + "item.php";
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, login_url, request, response -> {
                    try {
                        if (response.getInt(KEY_STATUS) == 0) {
                            Toast.makeText(this, response.getInt(KEY_POINTS) + " " + this.getResources().getString(R.string.points_added), Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(ActivityAddFunds.this).create();
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
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.timeout_error), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError){
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.no_connection_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.auth_failure_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivityAddFunds.this, this.getResources().getString(R.string.default_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }
}