package com.madrimas.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        requestLocalization();
    }

    protected void requestLocalization() {

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                weatherRequest(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);

                return;
            }
        } else {
            weatherRequest(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        weatherRequest(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));


    }

    protected void displayWeather(JSONObject response) {
        try {
            JSONObject coords = response.getJSONObject("coord");
            TextView lon = (TextView) findViewById(R.id.lon);
            String lonString = getString(R.string.longitude) + " " + coords.getString("lon");
            lon.setText(lonString);

            TextView lat = (TextView) findViewById(R.id.lat);
            String latString = getString(R.string.latitude) + " " + coords.getString("lat");
            lat.setText(latString);

            TextView cityName = (TextView) findViewById(R.id.city);
            String cityString = getString(R.string.city) + " " + response.getString("name");
            cityName.setText(cityString);

            JSONObject main = response.getJSONObject("main");

            TextView temp = (TextView) findViewById(R.id.temp);
            String tempString = getString(R.string.temperature) + " " + Double.toString(Double.parseDouble(main.getString("temp")) - 273.15) + " °C";
            temp.setText(tempString);

            TextView pressure = (TextView) findViewById(R.id.pressure);
            String pressureString = getString(R.string.pressure) + " " + main.getString("pressure") + " hPa";
            pressure.setText(pressureString);

            JSONObject wind = response.getJSONObject("wind");

            TextView windVelo = (TextView) findViewById(R.id.windVelo);
            String windVeloString = getString(R.string.velocity) + " " + wind.getString("speed") + " m/s";
            windVelo.setText(windVeloString);

            if (!wind.getString("deg").equals("")) {
                TextView windDir = (TextView) findViewById(R.id.windDir);
                int degree = Integer.parseInt(wind.getString("deg"));
                String windDirString = getString(R.string.direction) + " " + Integer.toString(degree);
                windDir.setText(windDirString);

                ImageView imageView = (ImageView) findViewById(R.id.arrowImg);
                imageView.setRotation(degree);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void weatherRequest(Location location) {
        RequestQueue mRequestQueue;

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        Network network = new BasicNetwork(new HurlStack());

        mRequestQueue = new RequestQueue(cache, network);

        mRequestQueue.start();

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + Double.toString(latitude) + "&lon=" + Double.toString(longitude) + "&appid=76fb6aad9e1f1e9bbf0951579003b05f";

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url, (String)null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                displayWeather(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        mRequestQueue.add(jsonObjectRequest);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {

                        weatherRequest(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }

                }
        }
    }

    private void weatherFromLocation() {
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
        weatherRequest(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

    }
}