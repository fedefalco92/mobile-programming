package it.unibs.appwow;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;

import it.unibs.appwow.fragments.PaymentsFragment;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.MyPlace;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.DateUtils;
import it.unibs.appwow.utils.IdEncodingUtils;
import it.unibs.appwow.utils.PositionUtils;

public class PaymentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    // TODO: 06/07/2016  AGGIUNGERE NEI DETTAGLI DELLA SPESA I SINGOLI AMOUNT

    private final String TAG_LOG = PaymentDetailsActivity.class.getSimpleName();

    private Payment mCost;
    private View mRootLayout;
    private TextView mCostName;
    private TextView mFullName;
    private TextView mEmail;
    private TextView mAmount;
    private TextView mDate;
    private TextView mNotes;
    private TextView mNotesLabel;
    private TextView mPositionText;
    private TextView mPositionLabel;
    //private Place mPlace;
    private MyPlace mPlace;
    private GoogleMap mMap;
    private MapFragment mMapFragment;

    private ImageButton mMapButton;
    private LinearLayout mAmountDetailContainer;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRootLayout = findViewById(R.id.activity_payment_details_container);
        mCost = getIntent().getParcelableExtra(PaymentsFragment.PASSING_PAYMENT_TAG);
        setTitle(mCost.getName());


        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mCostName = (TextView) findViewById(R.id.cost_detail_name);
        mCostName.setText(mCost.getName());

        mFullName = (TextView) findViewById(R.id.cost_detail_user_fullName);
        mFullName.setText(mCost.getFullName());
        mEmail = (TextView) findViewById(R.id.cost_detail_email);
        mEmail.setText("(" + mCost.getEmail() + ")");

        mAmount = (TextView) findViewById(R.id.cost_detail_amount);
        mAmount.setText("EUR " + mCost.getAmount());

        mNotes = (TextView) findViewById(R.id.cost_detail_notes_text);
        if(mCost.getNotes() == null || mCost.getNotes().isEmpty()){
            mNotesLabel = (TextView) findViewById(R.id.cost_detail_notes_label);
            mNotesLabel.setVisibility(View.GONE);
            mNotes.setVisibility(View.GONE);
        } else {
            mNotes.setText(mCost.getNotes());
        }

        mDate = (TextView) findViewById(R.id.cost_detail_date);
        mDate.setText(DateUtils.dateLongToString(mCost.getUpdatedAt()));

        mPositionText = (TextView) findViewById(R.id.cost_detail_position_text);
        mPositionLabel = (TextView) findViewById(R.id.cost_detail_position_label);
        String stringaPosizione = mCost.getPosition();
        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.cost_detail_position_map));
        mMapButton = (ImageButton) findViewById(R.id.payment_detail_map_button);

        if(stringaPosizione!=null && !stringaPosizione.isEmpty()){
           mPositionText.setText(stringaPosizione);
        } else {
            mPositionText.setVisibility(View.GONE);
            mPositionLabel.setVisibility(View.GONE);
        }

        final String position_id = mCost.getPositionId();
        if(position_id!= null && !position_id.isEmpty()){
            /*
            Places.GeoDataApi.getPlaceById(mClient, position_id).setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(PlaceBuffer places) {
                    Log.d("RESUL CALLBACK", "sono entrato nel result callback di getPlaceById");
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        final Place myPlace = places.get(0);
                        mPlace = myPlace.freeze();
                        Log.i(TAG_LOG, "Place found: " + mPlace.getName());
                    } else {
                        Log.e(TAG_LOG, "Place not found");
                    }
                    mPositionText.setText(mPlace.getName());
                    mMapFragment.getMapAsync(PaymentDetailsActivity.this);
                    places.release();
                }
            });*/
            sendPlaceDetailRequest(position_id);
        } else {
            mMapButton.setVisibility(View.INVISIBLE);
            mMapFragment.getView().setVisibility(View.GONE);
        }


        //details
        mAmountDetailContainer = (LinearLayout) findViewById(R.id.payment_detail_amount_details_container);

        String ad = mCost.getAmountDetails();
        List<Amount> amounts = IdEncodingUtils.decodeAmountDetails(ad, mCost.getIdUser(), mCost.getAmount());
        for(Amount a: amounts){
            TextView tv = new TextView(this, null);
            tv.setText(a.getFormattedString());
            mAmountDetailContainer.addView(tv);
        }
    }

    private void sendPlaceDetailRequest(final String place_id) {
        StringRequest req = new StringRequest(WebServiceUri.getPlaceDetailsUri(this, place_id),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String place_url = "";
                        String place_name = "";
                        String place_address = "";
                        LatLng latLng;
                        try {
                            JSONObject placejs = new JSONObject(response).getJSONObject("result");
                            place_url = placejs.getString("url");
                            place_name = placejs.getString("name");
                            place_address = placejs.getString("formatted_address");
                            JSONObject location = placejs.getJSONObject("geometry").getJSONObject("location");
                            String lat = location.getString("lat");
                            String lng = location.getString("lng");
                            latLng = new LatLng(new Double(lat), new Double (lng));
                            mPlace = new MyPlace(place_name, place_address, place_url, latLng);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                        Log.d(TAG_LOG,"Setting map button on details");
                        //setto il bottone di gmaps
                        mMapButton.setVisibility(View.VISIBLE);
                        mMapButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(mPlace.getUrl());
                                Intent gmaps = new Intent(Intent.ACTION_VIEW, uri);
                                gmaps.setPackage("com.google.android.apps.maps");
                                startActivity(gmaps);
                            }
                        });

                        //setto il fragment con la mappa
                        mPositionText.setText(mPlace.getName());
                        mMapFragment.getMapAsync(PaymentDetailsActivity.this);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MyApplication.getAppContext(),error.toString(),Toast.LENGTH_SHORT);
                    }
                });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_payment_details_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.payment_delete_title));
                builder.setMessage(String.format(getString(R.string.payment_delete_message), mCost.getName()));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        //progress dialog
                        showProgressDialog();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_payment_details_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.payment_delete_title));
            builder.setMessage(String.format(getString(R.string.payment_delete_message), mCost.getName()));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                   //progress dialog
                    showProgressDialog();
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);*/
    }

    private void showProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.payment_deleting));
        progressDialog.setCancelable(false);
        progressDialog.show();
        sendDeleteRequest(progressDialog);

    }

    private void sendDeleteRequest(final ProgressDialog dialog) {
        URL url = WebServiceUri.uriToUrl(WebServiceUri.getDeletePaymentUri(mCost.getId()));
        StringRequest req = WebServiceRequest.stringRequest(Request.Method.DELETE, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean result = false;
                try {
                    JSONObject obj = new JSONObject(response);
                    String stringresult = obj.getString("success");
                    result = Boolean.parseBoolean(stringresult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(result){
                    dialog.dismiss();
                    finish();
                } else {
                    dialog.dismiss();
                    showUnableToRemoveSnackbar(WebServiceUri.SERVER_ERROR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_LOG, "VOLLEY ERROR: " + error);
                dialog.dismiss();
                showUnableToRemoveSnackbar(WebServiceUri.NETWORK_ERROR);
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private void showUnableToRemoveSnackbar(int errorType){
        String msg = "";
        switch (errorType){
            case WebServiceUri.SERVER_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_server_error), mCost.getName());
                break;
            case WebServiceUri.NETWORK_ERROR:
                msg = String.format(getResources().getString(R.string.payment_delete_unsuccess_network_error), mCost.getName());
        }
        final Snackbar snackbar = Snackbar.make(mRootLayout, msg , Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.retry, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showProgressDialog();
            }
        });
        snackbar.show();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
        LatLng position = mPlace.getLatLng();
        String name = mPlace.getName();
        String snippet = mPlace.getAddress();

        Marker marker = mMap.addMarker(
                new MarkerOptions().position(position).title(name).snippet(snippet)
        );
        poitToPosition();
    }

    private void poitToPosition() {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mPlace.getLatLng())
                .zoom(14).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        /*
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CostDetails Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.start(mClient, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();
/*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CostDetails Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://it.unibs.appwow/http/host/path")
        );
        AppIndex.AppIndexApi.end(mClient, viewAction);*/
        mClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(PaymentDetailsActivity.this, "Connection to google maps failed", Toast.LENGTH_SHORT).show();
    }
}
