package com.example.weatherapp.fragments;

import static android.icu.lang.UCharacter.toUpperCase;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.MainActivity;
import com.example.weatherapp.R;
import com.example.weatherapp.SharedViewModel;
import com.example.weatherapp.ViewModelFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherFragment extends Fragment {
    private static final long REFRESH_TIME = 1000 * 60;

    private RequestQueue requestQueue;
    private static final Handler handler = new Handler();

    private SharedViewModel viewModel;

    TextView cityName;
    TextView latitude;
    TextView longitude;
    ImageView imageView;
    TextView temperature;
    TextView description;
    TextView feelsTemperature;
    TextView pressure;
    TextView humidity;
    TextView cloudiness;

    final String urlWeather = "https://api.openweathermap.org/data/2.5/weather?";
    final String apiID = "65be2d5623022f64488000d8e9fa6a95";

    public final Runnable updateWeather = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            weatherAPIRequest();

            handler.postDelayed(this, REFRESH_TIME);
        }
    };

    private void updateViewModel(JSONObject jsonObject, String cityName, String units) throws JSONException {
        viewModel.setLatitude(jsonObject.getJSONObject("coord").getDouble("lat"));
        viewModel.setLongitude(jsonObject.getJSONObject("coord").getDouble("lon"));
        viewModel.setCityName(cityName);
        viewModel.setTemperatureType(units.equals("imperial"));
    }

    public void weatherAPIRequest() {
        String temperatureType;
        if (viewModel.getTemperatureType().getValue() == null) {
            temperatureType = "metric";
        } else {
            temperatureType = viewModel.getTemperatureType().getValue() ? "imperial" : "metric";
        }

        String url = urlWeather + "q=" + viewModel.getCityName().getValue() + "&units=" + temperatureType + "&appid=" + apiID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        setWeatherInfo(response, viewModel.getCityName().getValue(), temperatureType);
                        ((MainActivity)getActivity()).saveWeatherDataInfo(viewModel.getCityName().getValue(), response.toString());
                        updateViewModel(response, viewModel.getCityName().getValue(), temperatureType);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Cannot obtain data from the server.", Toast.LENGTH_SHORT).show();
                    try {
                        ArrayList<String> weatherData = ((MainActivity)getActivity()).loadWeatherDataInfo();
                        JSONObject object = new JSONObject(weatherData.get(1));

                        setWeatherInfo(object, weatherData.get(0), temperatureType);
                        updateViewModel(object, weatherData.get(0), temperatureType);
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "App needs internet connection", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    @SuppressLint("FragmentLiveDataObserve")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        requestQueue = Volley.newRequestQueue(requireActivity());

        ViewModelFactory sharedViewModelFactory = new ViewModelFactory();
        viewModel = new ViewModelProvider(requireActivity(), sharedViewModelFactory).get(SharedViewModel.class);
        viewModel.getRefreshData().observe(this, (value) -> {
            weatherAPIRequest();
            Toast.makeText(getActivity(), "Refresh data", Toast.LENGTH_SHORT).show();
        });

        handler.post(updateWeather);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateWeather);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    private void setupViews(View view){
        cityName = view.findViewById(R.id.cityName);
        latitude = view.findViewById(R.id.latitude);
        longitude = view.findViewById(R.id.longitude);
        imageView = view.findViewById(R.id.weatherImage);
        temperature = view.findViewById(R.id.temperature);
        description = view.findViewById(R.id.weatherDescription);
        feelsTemperature = view.findViewById(R.id.feelsTempView);
        pressure = view.findViewById(R.id.pressureView);
        humidity = view.findViewById(R.id.humidityView);
        cloudiness = view.findViewById(R.id.cloudinessView);
    }

    @SuppressLint("SetTextI18n")
    public void setWeatherInfo(JSONObject jsonObjectData, String city, String temperatureTypeValue) {
        String temperatureType = temperatureTypeValue.equals("metric") ? "\u2103" : "\u2109";

        try {
            //set city name
            cityName.setText(toUpperCase(city));
            latitude.setText(jsonObjectData.getJSONObject("coord").getString("lat"));
            longitude.setText(jsonObjectData.getJSONObject("coord").getString("lon"));

            //set image view in fragment
            imageView.setImageResource(getImageResource(jsonObjectData));

            //set temperature View in fragment
            double temperatureDoubleValue = jsonObjectData.getJSONObject("main").getDouble("temp");
            int temperatureIntValue = (int) Math.round(temperatureDoubleValue);
            temperature.setText(temperatureIntValue + temperatureType);

            //set description View in fragment
            description.setText(jsonObjectData.getJSONArray("weather").getJSONObject(0).getString("description"));

            //set wind value in fragment
            double feelsTemperatureDoubleValue = jsonObjectData.getJSONObject("main").getDouble("feels_like");
            int feelsTemperatureIntValue = (int) Math.round(feelsTemperatureDoubleValue);
            feelsTemperature.setText(feelsTemperatureIntValue + temperatureType);

            //set pressure value in fragment
            pressure.setText(jsonObjectData.getJSONObject("main").getInt("pressure") + " hPa");

            //set humidity value in fragment
            humidity.setText(jsonObjectData.getJSONObject("main").getInt("humidity") + " %");

            //set cloudiness percentage value in fragment
            cloudiness.setText(jsonObjectData.getJSONObject("clouds").getInt("all") + " %");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getImageResource(JSONObject jsonObjectData) throws JSONException {
        String resourceImageId = jsonObjectData.getJSONArray("weather").getJSONObject(0).getString("icon");

        switch(resourceImageId){
            case "01d":
                return R.drawable.clear_sky_day;
            case "01n":
                return R.drawable.clear_sky_night;
            case "02d":
                return R.drawable.few_clouds_day;
            case "02n":
                return R.drawable.few_clouds_night;
            case "03d":
                return R.drawable.scattered_clouds_day;
            case "03n":
                return R.drawable.scattered_clouds_night;
            case "04d":
            case "04n":
                return R.drawable.broken_clouds;
            case "09d":
            case "09n":
                return R.drawable.shower_rain;
            case "10d":
                return R.drawable.rain_day;
            case "10n":
                return R.drawable.rain_night;
            case "11d":
            case "11n":
                return R.drawable.thunderstorm;
            case "13d":
            case "13n":
                return R.drawable.snow;
            case "50d":
            case "50n":
                return R.drawable.mist;
            default:
                return R.drawable.ic_01d;
        }
    }
}