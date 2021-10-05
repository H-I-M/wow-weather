package com.clyde.wowweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityInput;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

       mainRL = findViewById(R.id.idRLHome);
       loadingPB = findViewById(R.id.idPBLoading);
       cityNameTV = findViewById(R.id.idTVCityName);
       temperatureTV = findViewById(R.id.idTVTemperature);
       conditionTV = findViewById(R.id.ididTVCondition);
       weatherRV = findViewById(R.id.idRVWeather);
       cityInput = findViewById(R.id.idCityEdit);
       backIV = findViewById(R.id.idBackGround);
       iconIV = findViewById(R.id.idIVIcon);
       searchIV = findViewById(R.id.idIVSearch);
       weatherRVModalArrayList = new ArrayList<>();
       weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
       weatherRV.setAdapter(weatherRVAdapter);

       if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
          ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

           ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
       }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
       cityName = getCityName(location.getLatitude(), location.getLongitude());
       getWeatherInfo(cityName);

       searchIV.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String city = cityInput.getText().toString().trim();
               if(!city.isEmpty()) {
                   cityNameTV.setText(cityName);
                   getWeatherInfo(city);
               }else {
                   Toast.makeText(MainActivity.this, "Please enter city name.", Toast.LENGTH_SHORT).show();
               }
           }
       });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permissions granted, thanks !", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Please provide permissions for best results.", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    private String getCityName(double latitude, double longitude) {
        String cityLookup = "Not found";
        Geocoder gdc = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            List<Address> addresses = gdc.getFromLocation(latitude, longitude, 10);

            for(Address address : addresses) {
                if(address != null) {
                    String city = address.getLocality();

                    if(city != null && !city.equals("")) {
                        cityLookup = city;
                    }else {
                        Log.d(TAG, "CITY NOT FOUND.");
                        Toast.makeText(MainActivity.this, "User city not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch (IOException ioe) {
            Log.e(TAG, ioe.getLocalizedMessage());
        }
        return cityLookup;
    }

    private void getWeatherInfo(String cityName) {
        String url = this.getString(R.string.api_url, cityName);
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                mainRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();  //prevent repeated entries

                try {
                    JSONObject currentObj = response.getJSONObject("current");
                    String temp = currentObj.getString("temp_c");
                    int isDay = currentObj.getInt("is_day");
                    JSONObject conditionObj = currentObj.getJSONObject("condition");
                    String condition = conditionObj.getString("text");
                    String conditionIcon = conditionObj.getString("icon");

                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    temperatureTV.setText(temp.concat(MainActivity.this.getString(R.string.celcius)));

                    if (isDay == 1) {
                        // Day
                        Picasso.get().load(MainActivity.this.getString(R.string.day_sky_url));
                    } else {
                        // Night
                        Picasso.get().load(MainActivity.this.getString(R.string.night_sky_url));
                    }

                    JSONObject forecast = response.getJSONObject("forecast");
                    JSONObject forecastObj = forecast.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourlyArray = forecastObj.getJSONArray("hour");

                    for (int i = 0; i < hourlyArray.length(); i++) {
                        JSONObject hourObj = hourlyArray.getJSONObject(i);
                        String hTime = hourObj.getString("time");
                        String hTemp = hourObj.getString("temp_c");
                        String hIcon = hourObj.getJSONObject("condition").getString("icon");
                        String hWind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(hTime, hTemp, hIcon, hWind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException je) {
                    Log.e(TAG, je.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter a valid city name.", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonRequest);
    }
}