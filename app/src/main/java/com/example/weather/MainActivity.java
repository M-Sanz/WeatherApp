package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.weather.WeatherRVAdapter;
import com.example.weather.WeatherRVModal;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRl;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV, humidityTV, forecastTitle;
    private ImageView backIV;
    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private SharedPreferences.Editor setData;
    private SharedPreferences getData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRl = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        backIV = findViewById(R.id.idIVBack);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        forecastTitle = findViewById(R.id.idTVForecastTitle);
        humidityTV = findViewById(R.id.idTVHumidity);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setData = getPreferences(MODE_PRIVATE).edit();
        getData = getPreferences(MODE_PRIVATE);


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        if(getData.contains("temp")){
            temperatureTV.setText(getData.getString("temp", null));
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        cityName = getCityName(location.getLongitude(),location.getLatitude());
        cityNameTV.setText(cityName);

        getWeatherInfo(location.getLatitude(), location.getLongitude());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please provide the permissions..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude, 10);
            for(Address adr : addresses){
                if (adr != null){
                    String city = adr.getLocality();
                    if(city != null && !city.equals("")) {
                        cityName = city;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return cityName;
    }

    private void getWeatherInfo(Double lat, Double lon){
        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&appid=16e06464bd309193a3c00ae33d5bd21a";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRl.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try{
                    String temperature = response.getJSONObject("current").getString("temp");
                    double temperature_d = Double.parseDouble(temperature) - 273.15;
                    NumberFormat formatter = new DecimalFormat("#0.0");
                    temperatureTV.setText( formatter.format(temperature_d) + "°C");
                    setData.putString("temp", formatter.format(temperature_d) + "°C");
//                    setData.apply();

                    String condition = response.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
                    conditionTV.setText(toTitleCase(condition));
                    setData.putString("condition", condition);
//                    setData.apply();

                    String humidity = response.getJSONObject("current").getString("humidity");
                    humidityTV.setText("Humidity: " + humidity + "%" );
                    setData.putString("humidity", humidity);
                    setData.apply();


                    JSONArray hourArray = response.getJSONArray("hourly");

                    for(int i=1; i<hourArray.length(); i++ ){
                        JSONObject hourObj = hourArray.getJSONObject(i);

                        String epoch = hourObj.getString("dt");
                        Date expiry = new Date(Long.parseLong(epoch) * 1000);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String strTime = dateFormat.format(expiry);
                        String temper = hourObj.getString("temp");
                        double temper_d = Double.parseDouble(temper) - 273.15;
                        NumberFormat temper_formatter = new DecimalFormat("#0.0");
                        String wind = hourObj.getString("wind_speed");

                        weatherRVModalArrayList.add(new WeatherRVModal(strTime, temper_formatter.format(temper_d), wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();


                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Connection Error...", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public static String toTitleCase(String string) {

        // Check if String is null
        if (string == null) {

            return null;
        }

        boolean whiteSpace = true;

        StringBuilder builder = new StringBuilder(string); // String builder to store string
        final int builderLength = builder.length();

        // Loop through builder
        for (int i = 0; i < builderLength; ++i) {

            char c = builder.charAt(i); // Get character at builders position

            if (whiteSpace) {

                // Check if character is not white space
                if (!Character.isWhitespace(c)) {

                    // Convert to title case and leave whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    whiteSpace = false;
                }
            } else if (Character.isWhitespace(c)) {

                whiteSpace = true; // Set character is white space

            } else {

                builder.setCharAt(i, Character.toLowerCase(c)); // Set character to lowercase
            }
        }

        return builder.toString(); // Return builders text
    }
}