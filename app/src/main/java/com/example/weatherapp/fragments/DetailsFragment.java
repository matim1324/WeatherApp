package com.example.weatherapp.fragments;

import static android.icu.lang.UCharacter.toUpperCase;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DetailsFragment extends Fragment {
    private static final long REFRESH_TIME = 1000 * 60;

    private RequestQueue requestQueue;
    private static final Handler handler = new Handler();

    private SharedViewModel viewModel;

    TextView cityName;
    TextView latitude;
    TextView longitude;
    TextView windSpeed;
    TextView windDirection;
    TextView visibility;
    TextView sunrise;
    TextView sunset;

    final String urlWeather = "https://api.openweathermap.org/data/2.5/weather?";
    final String apiID = "65be2d5623022f64488000d8e9fa6a95";

    public final Runnable updateDetails = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            detailsAPIRequest();

            handler.postDelayed(this, REFRESH_TIME);
        }
    };

    public void detailsAPIRequest() {
        String temperatureType;
        if (viewModel.getTemperatureType().getValue() == null) {
            temperatureType = "metric";
        } else {
            temperatureType = viewModel.getTemperatureType().getValue() ? "imperial" : "metric";
        }

        String url = urlWeather + "q=" + viewModel.getCityName().getValue() + "&units=" + temperatureType + "&appid=" + apiID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    setDetailsInfo(response, viewModel.getCityName().getValue());
                    ((MainActivity)getActivity()).saveWeatherDataInfo(viewModel.getCityName().getValue(), response.toString());
                },
                error -> {
                    Toast.makeText(getActivity(), "Cannot obtain data from the server.", Toast.LENGTH_SHORT).show();

                    try {
                        ArrayList<String> response = ((MainActivity)getActivity()).loadWeatherDataInfo();
                        JSONObject object = new JSONObject(response.get(1));

                        setDetailsInfo(object, response.get(0));
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
            detailsAPIRequest();
        });

        handler.post(updateDetails);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateDetails);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    private void setupViews(View view){
        if (!getResources().getBoolean(R.bool.isTablet)) {
            cityName = view.findViewById(R.id.cityName);
            latitude = view.findViewById(R.id.latitude);
            longitude = view.findViewById(R.id.longitude);
        }
        windSpeed = view.findViewById(R.id.windSpeedView);
        windDirection = view.findViewById(R.id.windDirectionView);
        visibility = view.findViewById(R.id.visibilityView);
        sunrise = view.findViewById(R.id.sunriseView);
        sunset = view.findViewById(R.id.sunsetView);
    }

    @SuppressLint("SetTextI18n")
    public void setDetailsInfo(JSONObject jsonObjectData, String city) {
        try {
            //set city, lat, lon
            if (!getResources().getBoolean(R.bool.isTablet)){
                cityName.setText(toUpperCase(city));
                latitude.setText(jsonObjectData.getJSONObject("coord").getString("lat"));
                longitude.setText(jsonObjectData.getJSONObject("coord").getString("lon"));
            }

            //set wind view data
            windSpeed.setText(jsonObjectData.getJSONObject("wind").getInt("speed") + " km/h");
            windDirection.setText(jsonObjectData.getJSONObject("wind").getInt("deg") + "\u00B0");

            //set visibility data
            visibility.setText(jsonObjectData.getInt("visibility") / 1000.0 + " km");

            //set sunrise view
            sunrise.setText(getTime(jsonObjectData, "sunrise"));

            //set sunset view
            sunset.setText(getTime(jsonObjectData, "sunset"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getTime(JSONObject jsonObjectData, String type) throws JSONException {
        int time = jsonObjectData.getJSONObject("sys").getInt(type);
        Date date = new Date(time * 1000L);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int hour = 2 + calendar.get(Calendar.HOUR_OF_DAY);
        String timeResult = hour + ":" + calendar.get(Calendar.MINUTE);

        return timeResult;
    }
}