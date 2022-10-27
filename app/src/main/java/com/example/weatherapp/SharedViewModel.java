package com.example.weatherapp;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> cityName = new MutableLiveData<>();
    private final MutableLiveData<Double> longitude = new MutableLiveData<>();
    private final MutableLiveData<Double> latitude = new MutableLiveData<>();
    private final MutableLiveData<Boolean> temperatureType = new MutableLiveData<>();
    private final MutableLiveData<Boolean> refreshData = new MutableLiveData<>();

    public MutableLiveData<String> getCityName() {
        return cityName;
    }

    public void setCityName(String city) {
        cityName.setValue(city);
    }

    public MutableLiveData<Double> getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        longitude.setValue(lon);
    }

    public MutableLiveData<Double> getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        latitude.setValue(lat);
    }

    public MutableLiveData<Boolean> getTemperatureType() {
        return temperatureType;
    }

    public void setTemperatureType(Boolean type) {
        temperatureType.setValue(type);
    }

    public MutableLiveData<Boolean> getRefreshData() {
        return refreshData;
    }

    public void setRefreshData(Boolean refresh) { refreshData.setValue(refresh); }
}
