package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    private static final String DEFAULT_CITY = "Lodz";
    private static final double DEFAULT_LATITUDE = 51.7833;
    private static final double DEFAULT_LONGITUDE = 19.4667;

    private EditText cityName;
    private EditText latitudeValue;
    private EditText longitudeValue;

    private Spinner favouritesCitiesSpinner;
    private ArrayList<String> favouritesCitiesList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private Button saveData;
    private Button addCityToFavourites;
    private Button delCityFromFavourites;
    private ToggleButton setTemperatureType;

    private RequestQueue requestQueue;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupViews();
        requestQueue = Volley.newRequestQueue(this);

        loadCitiesList();
        if (favouritesCitiesList.size() == 0){
            favouritesCitiesList.add(DEFAULT_CITY);
            saveCitiesList(favouritesCitiesList);
        }

        arrayAdapter = new ArrayAdapter<>(this, R.menu.selected_item, favouritesCitiesList);
        arrayAdapter.setDropDownViewResource(R.menu.dropdown_item);
        favouritesCitiesSpinner.setAdapter(arrayAdapter);

        Intent intent = getIntent();
        String cityNameIntent = intent.getStringExtra("cityName");
        String latitudeIntent = intent.getStringExtra("latitude");
        String longitudeIntent = intent.getStringExtra("longitude");
        boolean temperatureTypeIntent = intent.getBooleanExtra("temperatureType", false);
        if (latitudeIntent != null){
            latitudeValue.setText(latitudeIntent);
            longitudeValue.setText(longitudeIntent);
            cityName.setText(cityNameIntent);
            setTemperatureType.setChecked(temperatureTypeIntent);
        }

        saveData.setOnClickListener(v -> {
            try {
                String latitudeString = latitudeValue.getText().toString();
                String longitudeString = longitudeValue.getText().toString();
                String cityNameString = cityName.getText().toString();

                double latitudeTemp = Double.parseDouble(latitudeString);
                double longitudeTemp = Double.parseDouble(longitudeString);

                Intent intentSettings = new Intent(SettingsActivity.this, MainActivity.class);
                intentSettings.putExtra("cityName", cityNameString);
                intentSettings.putExtra("latitude", latitudeTemp);
                intentSettings.putExtra("longitude", longitudeTemp);
                intentSettings.putExtra("temperatureType", setTemperatureType.isChecked());

                saveCitiesList(favouritesCitiesList);
                startActivity(intentSettings);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid city data input", Toast.LENGTH_SHORT).show();
            }
        });

        favouritesCitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    cityName.setText(favouritesCitiesSpinner.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        addCityToFavourites.setOnClickListener(v -> {
            String city = cityName.getText().toString();
            if (!city.equals("") && !favouritesCitiesList.contains(city)) {
                favouritesCitiesList.add(city);
                favouritesCitiesSpinner.setAdapter(arrayAdapter);
                favouritesCitiesSpinner.setSelection(favouritesCitiesList.size() - 1);
                saveCitiesList(favouritesCitiesList);
            }
        });

        delCityFromFavourites.setOnClickListener(v -> {
            Object city = favouritesCitiesSpinner.getSelectedItem();
            if (city != null) {
                favouritesCitiesList.remove(city.toString());
                favouritesCitiesSpinner.setAdapter(arrayAdapter);
                saveCitiesList(favouritesCitiesList);
            }
        });

        cityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCity(cityName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupViews(){
        cityName = findViewById(R.id.editText_cityName);
        latitudeValue = findViewById(R.id.latitude);
        longitudeValue = findViewById(R.id.longitude);

        favouritesCitiesSpinner = findViewById(R.id.spinnerFavouritesCities);
        saveData = findViewById(R.id.saveDataButton);
        addCityToFavourites = findViewById(R.id.addCityButton);
        delCityFromFavourites = findViewById(R.id.delCityButton);
        setTemperatureType = findViewById(R.id.setTemperatureType);
    }

    public void saveCitiesList(ArrayList<String> citiesList) {
        Gson gson = new Gson();
        String json = gson.toJson(citiesList);

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("citiesList", json);
        editor.apply();
    }

    public void loadCitiesList() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String jsonStrings = sharedPreferences.getString("citiesList", "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> sharedPreferencesList = gson.fromJson(jsonStrings, type);
        if (sharedPreferencesList == null) return;
        favouritesCitiesList = sharedPreferencesList;
    }

    private void searchCity(String cityName){
        String urlAPI = "https://api.openweathermap.org/data/2.5/weather?";
        String apiID = "65be2d5623022f64488000d8e9fa6a95";
        String url = urlAPI + "q=" + cityName + "&appid=" + apiID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        latitudeValue.setText(response.getJSONObject("coord").getString("lat"));
                        longitudeValue.setText(response.getJSONObject("coord").getString("lon"));
                        saveData.setEnabled(true);
                        addCityToFavourites.setEnabled(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    saveData.setEnabled(false);
                    addCityToFavourites.setEnabled(false);
                });

        requestQueue.add(jsonObjectRequest);
    }
}
