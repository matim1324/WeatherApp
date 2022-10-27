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

public class ForecastFragment extends Fragment {
    private static final long REFRESH_TIME = 1000 * 60;

    private RequestQueue requestQueue;
    private static final Handler handler = new Handler();

    private SharedViewModel viewModel;

    TextView cityName;
    TextView date1, date2, date3, date4, date5;
    TextView temp1, temp2, temp3, temp4, temp5;
    ImageView image1, image2, image3, image4, image5;

    final String urlWeather = "https://api.openweathermap.org/data/2.5/forecast?";
    final String apiID = "65be2d5623022f64488000d8e9fa6a95";

    public final Runnable updateData = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            forecastAPIRequest();

            handler.postDelayed(this, REFRESH_TIME);
        }
    };

    public void forecastAPIRequest() {
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
                        setForecastInfo(response, viewModel.getCityName().getValue(), temperatureType);
                        //saveForecastDataInfo(viewModel.getCityName().getValue(), response.toString());
                        ((MainActivity)getActivity()).saveForecastDataInfo(viewModel.getCityName().getValue(), response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Cannot obtain data from the server.", Toast.LENGTH_SHORT).show();
                    try {
                        //ArrayList<String> response = loadForecastDataInfo();
                        ArrayList<String> response = ((MainActivity)getActivity()).loadForecastDataInfo();
                        JSONObject object = new JSONObject(response.get(1));

                        setForecastInfo(object, response.get(0), temperatureType);
                    } catch (Exception e) {
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
            forecastAPIRequest();
        });

        handler.post(updateData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateData);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    private void setupViews(View view){
        if (!getResources().getBoolean(R.bool.isTablet)){
            cityName = view.findViewById(R.id.cityName);
        }
        //cityName = view.findViewById(R.id.cityName);

        date1 = view.findViewById(R.id.dateWeather1);
        date2 = view.findViewById(R.id.dateWeather2);
        date3 = view.findViewById(R.id.dateWeather3);
        date4 = view.findViewById(R.id.dateWeather4);
        date5 = view.findViewById(R.id.dateWeather5);

        temp1 = view.findViewById(R.id.weatherTemperature1);
        temp2 = view.findViewById(R.id.weatherTemperature2);
        temp3 = view.findViewById(R.id.weatherTemperature3);
        temp4 = view.findViewById(R.id.weatherTemperature4);
        temp5 = view.findViewById(R.id.weatherTemperature5);

        image1 = view.findViewById(R.id.weatherImage1);
        image2 = view.findViewById(R.id.weatherImage2);
        image3 = view.findViewById(R.id.weatherImage3);
        image4 = view.findViewById(R.id.weatherImage4);
        image5 = view.findViewById(R.id.weatherImage5);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    public void setForecastInfo(JSONObject jsonObjectData, String city, String temperatureTypeValue) throws JSONException {
        String temperatureType = temperatureTypeValue.equals("metric") ? "\u2103" : "\u2109";
        if (!getResources().getBoolean(R.bool.isTablet)){
            cityName.setText(toUpperCase(city));
        }

        //set dates in forecast
        date1.setText(getDate(jsonObjectData, 0));
        date2.setText(getDate(jsonObjectData, 8));
        date3.setText(getDate(jsonObjectData, 16));
        date4.setText(getDate(jsonObjectData, 24));
        date5.setText(getDate(jsonObjectData, 32));

        //set temperatures in forecast
        temp1.setText(getTemperature(jsonObjectData, 0) + temperatureType);
        temp2.setText(getTemperature(jsonObjectData, 8) + temperatureType);
        temp3.setText(getTemperature(jsonObjectData, 16) + temperatureType);
        temp4.setText(getTemperature(jsonObjectData, 24) + temperatureType);
        temp5.setText(getTemperature(jsonObjectData, 32) + temperatureType);

        //set images in forecast
        image1.setImageResource(getImageResource(jsonObjectData, 0));
        image2.setImageResource(getImageResource(jsonObjectData, 8));
        image3.setImageResource(getImageResource(jsonObjectData, 16));
        image4.setImageResource(getImageResource(jsonObjectData, 24));
        image5.setImageResource(getImageResource(jsonObjectData, 32));
    }

    private String getDate(JSONObject jsonObjectData, int index) throws JSONException {
        String date = jsonObjectData.getJSONArray("list").getJSONObject(index).getString("dt_txt");
        String dateView =  date.substring(0, 10);

        return dateView;
    }

    private String getTemperature(JSONObject jsonObjectData, int index) throws JSONException {
        double temperatureDoubleValue = jsonObjectData.getJSONArray("list").getJSONObject(index).getJSONObject("main").getDouble("temp");
        int temperatureIntValue = (int) Math.round(temperatureDoubleValue);

        return String.valueOf(temperatureIntValue);
    }

    private int getImageResource(JSONObject jsonObjectData, int index) throws JSONException {
        String resourceImageId = jsonObjectData.getJSONArray("list").getJSONObject(index).getJSONArray("weather").getJSONObject(0).getString("icon");

        switch(resourceImageId){
            case "01d":
            case "01n":
                return R.drawable.clear_sky_day;
            case "02d":
            case "02n":
                return R.drawable.few_clouds_day;
            case "03d":
            case "03n":
                return R.drawable.scattered_clouds_day;
            case "04d":
            case "04n":
                return R.drawable.broken_clouds;
            case "09d":
            case "09n":
                return R.drawable.shower_rain;
            case "10d":
            case "10n":
                return R.drawable.rain_day;
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