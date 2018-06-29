package com.example.gekpoh.boliao;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditEventFragment3 extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoDataClient mGeoDataClient;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(1.3521, 103.8198);   // LatLng of Singapore
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private Marker marker;
    private String placeIdToSave;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String[] FINE_LOCATION_ARRAY = {FINE_LOCATION};

    private AutoCompleteTextView searchBar;
    private ImageView imageViewGps;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapter;
    private EditEventFragment3.fragment3CallBack mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (EditEventFragment3.fragment3CallBack)context;
        }catch(ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_event_fragment_layout3, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        placeIdToSave = args.getString("placeId");

        getLocationPermission();

        imageViewGps = getView().findViewById(R.id.imageViewGps);
        searchBar = getView().findViewById(R.id.searchBar);

        mGeoDataClient = Places.getGeoDataClient(getActivity());

        searchBar.setOnItemClickListener(mAutoCompleteClickListener);
        mPlaceAutoCompleteAdapter = new PlaceAutoCompleteAdapter(getActivity(), mGeoDataClient, LAT_LNG_BOUNDS, null);
        searchBar.setAdapter(mPlaceAutoCompleteAdapter);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    searchLocation();
                }
                return false;
            }
        });

        imageViewGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        initMap();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionsGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), FINE_LOCATION_ARRAY, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            mGeoDataClient.getPlaceById(placeIdToSave).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        moveCamera(myPlace.getLatLng(), DEFAULT_ZOOM, myPlace.getName().toString());
                        mCallback.setLatLng(myPlace.getLatLng());
                        Toast.makeText(getActivity(), "Activity location marked at: " + myPlace.getName().toString(), Toast.LENGTH_LONG).show();
                        places.release();
                    } else {
                        Log.e("EditEventMapAct", "Place not found.");
                    }
                }
            });

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {  // this part doesn't work on emulator
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                    }
                    else moveCamera(mDefaultLocation, DEFAULT_ZOOM, "My Location");
                }
            });
        }
        catch (SecurityException e) {
            Log.e("EditEventMap", "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            if (marker != null) marker.remove();
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            marker = mMap.addMarker(options);
        }
    }

    private void searchLocation() {
        String searchString = searchBar.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e) {
            Log.e("EditEventMap", "geoLocate: geolocating");
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            Toast.makeText(getActivity(), "Click on the marker to show directions", Toast.LENGTH_LONG).show();
        }
    }

    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AutocompletePrediction item = mPlaceAutoCompleteAdapter.getItem(position);
            placeIdToSave = item.getPlaceId();

            mGeoDataClient.getPlaceById(placeIdToSave).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        moveCamera(myPlace.getLatLng(), DEFAULT_ZOOM, myPlace.getName().toString());
                        mCallback.setLatLng(myPlace.getLatLng());
                        Toast.makeText(getActivity(), "Activity location marked at: " + myPlace.getName().toString(), Toast.LENGTH_LONG).show();
                        places.release();
                    } else {
                        Log.e("EditEventMapAct", "Place not found.");
                    }
                }
            });
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public String sendPlaceId() {return placeIdToSave; }

    public interface fragment3CallBack{
        void setLatLng(LatLng ll);
    }
}
