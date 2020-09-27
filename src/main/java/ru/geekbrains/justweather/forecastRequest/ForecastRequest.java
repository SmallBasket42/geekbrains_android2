package ru.geekbrains.justweather.forecastRequest;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.concurrent.CountDownLatch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public class ForecastRequest {

    public static int responseCode;
    private static WeatherRequest weatherRequest;
    private static CountDownLatch forecastResponseReceived;

    public static WeatherRequest getWeatherRequest(){return weatherRequest;}

    public static CountDownLatch getForecastResponseReceived(){return forecastResponseReceived;}

    public static void getForecastFromServer(String currentCity) {
        forecastResponseReceived = new CountDownLatch(1);
        Log.d("retrofit", "countDounLatch = " + forecastResponseReceived.getCount());
        OpenWeatherRepo.getInstance().getAPI().loadWeather(currentCity,"metric",
                "849b888638d27a2b5c80b65cfc590f12").enqueue(new Callback<WeatherRequest>() {
            @Override
            public void onResponse(@NonNull Call<WeatherRequest> call,
                                   @NonNull Response<WeatherRequest> response) {
                if (response.body() != null && response.isSuccessful()) {
                    responseCode = 200;
                    weatherRequest = response.body();
                    forecastResponseReceived.countDown();
                    Log.d("retrofit", "countDown");

                } else {
                    if (response.code() == 404) {
                        responseCode = 404;
                        forecastResponseReceived.countDown();
                        return;
                    }
                    responseCode = 0;
                    forecastResponseReceived.countDown();
                }
                Log.d("retrofit", "response.code = " + responseCode);
                Log.d("retrofit", "weatherRequest is null: " + (weatherRequest == null));
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public void onFailure(Call<WeatherRequest> call, Throwable t) {
                forecastResponseReceived.countDown();
                responseCode = 0;
            }
        });
    }
}