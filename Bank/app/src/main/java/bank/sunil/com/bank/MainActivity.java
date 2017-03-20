package bank.sunil.com.bank;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.DialogInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    SupportMapFragment fragment;
    LocationManager locationManager;
    Double latitude, longitude;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGooglePlayServices();
        fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
             if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                 buildAlertMessageNoGps();

             }
  else {

                 if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                     // TODO: Consider calling
                     //    ActivityCompat#requestPermissions
                     // here to request the missing permissions, and then overriding
                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                     //                                          int[] grantResults)
                     // to handle the case where the user grants the permission. See the documentation
                     // for ActivityCompat#requestPermissions for more details.
                     return;
                 }
                 dialog = new ProgressDialog(this);
                 dialog.setMessage("Fetching Location");
                 dialog.setCancelable(false);
                 dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                 dialog.show();
                 locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                 locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 5000, new LocationListener() {
                     @Override
                     public void onLocationChanged(final Location location) {

                         fragment.getMapAsync(new OnMapReadyCallback() {
                             @Override
                             public void onMapReady(GoogleMap googleMap) {
                                 dialog.dismiss();
                                 latitude = location.getLatitude();
                                 longitude = location.getLongitude();
                                 googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                 MarkerOptions options = new MarkerOptions();
                                 options.position(new LatLng(location.getLatitude(), location.getLongitude()));
                                 googleMap.addMarker(options);
                                 googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));

                             }
                         });
                         locationManager.removeUpdates(this);
                     }

                     @Override
                     public void onStatusChanged(String provider, int status, Bundle extras) {

                     }

                     @Override
                     public void onProviderEnabled(String provider) {

                     }

                     @Override
                     public void onProviderDisabled(String provider) {

                     }
                 });

             }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable Your GPS To Use Map")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        finish();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    public void getBanks(View v) {
        dialog.show();
       build_retrofit_and_get_response("bank");
    }

    public void getAtm(View v){
        dialog.show();
        build_retrofit_and_get_response("atm");
    }

    public void getHospitals(View v){
        dialog.show();
        build_retrofit_and_get_response("hospital");
    }


    private void build_retrofit_and_get_response(String type) {

        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface service = retrofit.create(RetrofitInterface.class);

        Call<Example> call = service.getNearbyPlaces(type, latitude + "," + longitude, 5000);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> resp) {
               final Response<Example> response=resp;
                fragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.clear();
                        dialog.dismiss();
                       for(int i=0;i<response.body().getResults().size();i++) {
                           Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                           Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                           String placeName = response.body().getResults().get(i).getName();
                           String vicinity = response.body().getResults().get(i).getVicinity();
                           MarkerOptions options=new MarkerOptions();
                           options.position(new LatLng(latitude,longitude));
                           options.title("Your Location");
                           googleMap.addMarker(options);
                           MarkerOptions markerOptions = new MarkerOptions();
                           LatLng latLng = new LatLng(lat, lng);
                           // Position of Marker on Map
                           markerOptions.position(latLng);
                           // Adding Title to the Marker
                           markerOptions.title(placeName + " : " + vicinity);
                           // Adding Marker to the Camera.
                           markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));
                           googleMap.addMarker(markerOptions);
                           googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),14));
                       }

                    }
                });
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

            }
        });
    }


}
