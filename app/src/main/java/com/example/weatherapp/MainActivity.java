package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weatherapp.fragments.DetailsFragment;
import com.example.weatherapp.fragments.ForecastFragment;
import com.example.weatherapp.fragments.WeatherFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String DEFAULT_CITY = "Lodz";
    private static final double DEFAULT_LATITUDE = 51.7833;
    private static final double DEFAULT_LONGITUDE = 19.4667;

    private SharedViewModel viewModel;

    ViewPager2 viewPager;
    FragmentStateAdapter fragmentsAdapter;
    TabLayout tabLayout;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();

        ViewModelFactory sharedViewModelFactory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, sharedViewModelFactory).get(SharedViewModel.class);
        if (viewModel.getRefreshData().getValue() == null) {
            viewModel.setCityName(DEFAULT_CITY);
            viewModel.setLatitude(DEFAULT_LATITUDE);
            viewModel.setLongitude(DEFAULT_LONGITUDE);
        }

        Intent intent = getIntent();
        String cityNameIntent = intent.getStringExtra("cityName");
        double latitudeIntent = intent.getDoubleExtra("latitude", 200);
        double longitudeIntent = intent.getDoubleExtra("longitude", 200);
        boolean temperatureTypeIntent = intent.getBooleanExtra("temperatureType", false);
        if (latitudeIntent != 200) {
            viewModel.setLatitude(latitudeIntent);
            viewModel.setLongitude(longitudeIntent);
            viewModel.setTemperatureType(temperatureTypeIntent);
            viewModel.setCityName(cityNameIntent);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (getResources().getBoolean(R.bool.isTablet)) {
                fragmentsAdapter = new TabletViewAdapter(this);
            } else {
                fragmentsAdapter = new MobileViewAdapter(this);
            }
        } else {
            fragmentsAdapter = new MobileViewAdapter(this);
        }

        viewPager.setAdapter(fragmentsAdapter);
        if (!getResources().getBoolean(R.bool.isTablet)){
            new TabLayoutMediator(
                    tabLayout,
                    viewPager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            if (position == 0)
                                tab.setText("Weather");
                            else if (position == 1)
                                tab.setText("Details");
                            else
                                tab.setText("Forecast");
                        }
                    }
            ).attach();
        }
    }

    private void setupViews(){
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    public void saveWeatherDataInfo(String cityName, String weatherData){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CityName", cityName);
        editor.putString("WeatherData", weatherData);
        editor.apply();
    }

    public ArrayList<String> loadWeatherDataInfo(){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        ArrayList<String> weatherData = new ArrayList<>();
        weatherData.add(sharedPreferences.getString("CityName", ""));
        weatherData.add(sharedPreferences.getString("WeatherData", ""));
        return weatherData;
    }

    public void saveForecastDataInfo(String cityName, String forecastData){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CityName", cityName);
        editor.putString("ForecastData", forecastData);
        editor.apply();
    }

    public ArrayList<String> loadForecastDataInfo(){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        ArrayList<String> forecastData = new ArrayList<>();
        forecastData.add(sharedPreferences.getString("CityName", ""));
        forecastData.add(sharedPreferences.getString("ForecastData", ""));
        return forecastData;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int action = item.getItemId();
        if (action == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("latitude", viewModel.getLatitude().getValue().toString());
            intent.putExtra("longitude", viewModel.getLongitude().getValue().toString());
            intent.putExtra("cityName", viewModel.getCityName().getValue());
            intent.putExtra("temperatureType", viewModel.getTemperatureType().getValue());
            startActivity(intent);
            return true;
        } else if (action == R.id.refresh) {
            viewModel.setRefreshData(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    private static class MobileViewAdapter extends FragmentStateAdapter {
        public MobileViewAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new WeatherFragment();
                case 1:
                    return new DetailsFragment();
                default:
                    return new ForecastFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private static class TabletViewAdapter extends FragmentStateAdapter {
        public TabletViewAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new TabletView();
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}