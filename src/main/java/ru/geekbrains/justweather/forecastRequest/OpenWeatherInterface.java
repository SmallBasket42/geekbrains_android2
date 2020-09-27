package ru.geekbrains.justweather.forecastRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public interface OpenWeatherInterface {
    @GET("data/2.5/forecast")
    Call<WeatherRequest> loadWeather(@Query("q") String city,
                                     @Query("units") String units,
                                     @Query("appid") String keyApi);
}