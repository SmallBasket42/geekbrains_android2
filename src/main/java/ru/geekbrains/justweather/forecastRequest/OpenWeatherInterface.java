package ru.geekbrains.justweather.forecastRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public interface OpenWeatherInterface {
    @GET("data/2.5/forecast")
    Call<WeatherRequest> loadWeather(@Query("lat") Double latitude,
                                     @Query("lon") Double longitude,
                                     @Query("units") String units,
                                     @Query("lang") String lang,
                                     @Query("appid") String keyApi);
}