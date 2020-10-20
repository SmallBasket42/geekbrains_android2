package ru.geekbrains.justweather.placeDetailsRequest;

import android.content.res.Resources;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import ru.geekbrains.justweather.model.HourlyWeatherData;
import ru.geekbrains.justweather.model.WeatherData;
import ru.geekbrains.justweather.model.placeDetails.FetchPlaceRequest;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public final class GooglePlaceDetails {
    private FetchPlaceRequest fetchPlaceRequest = new FetchPlaceRequest();
    private Double cityLatitude;
    private Double cityLongitude;

    public Double getCityLongitude(){return cityLongitude;}
    public Double getCityLatitude(){return cityLatitude;}

    private static GooglePlaceDetails instance = null;

    private static final Object syncObj = new Object();

    private GooglePlaceDetails() {
    }

    public static GooglePlaceDetails getInstance() {

        synchronized (syncObj) {
            if (instance == null) {
                instance = new GooglePlaceDetails();
            }
            return instance;
        }
    }

    public  void getCityCoordinates() {
        fetchPlaceRequest = PlaceDetailsRequest.getFetchPlaceRequest();
        if (fetchPlaceRequest != null) {
            Log.d("Threads", "getCityCoordinates -> fetchPlaceRequest != null");
            Thread coorditatesGetter = new Thread(() -> {
                cityLatitude = fetchPlaceRequest.getResult().getGeometry().getLocation().getLat();
                cityLongitude = fetchPlaceRequest.getResult().getGeometry().getLocation().getLng();
            });
            coorditatesGetter.start();
            Log.d("Threads", "coorditatesGetter start");
            try {
                coorditatesGetter.join();
                Log.d("Threads", "coorditatesGetter joined");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}