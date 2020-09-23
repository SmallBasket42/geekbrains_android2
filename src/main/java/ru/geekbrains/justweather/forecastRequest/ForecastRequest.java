package ru.geekbrains.justweather.forecastRequest;

import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public class ForecastRequest {

    final static String myLog = "myLog";
    private static final String TAG = "WEATHER";
    public static int responseCode;
    private static WeatherRequest weatherRequest;

    public static WeatherRequest getWeatherRequest(){return weatherRequest;}

    public static void getForecastFromServer(String currentCity, URL forecastSourceUrl){
        try {
            final URL uri = forecastSourceUrl;
            Thread t1 = new Thread(() -> {
                HttpsURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpsURLConnection) uri.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(10000);
                    try{
                        responseCode = urlConnection.getResponseCode();
                    } catch (Exception e){
                        responseCode = 0;
                    }
                    Log.d(myLog, "###getFiveDaysWeatherFromServer responseCod = " + responseCode);
                    Log.d(myLog, "###getFiveDaysWeatherFromServer currentCity = " + currentCity);
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String result = getLines(in);
                    Gson gson = new Gson();
                    weatherRequest = gson.fromJson(result, WeatherRequest.class);
                    Log.d(myLog, "ChooseCityPresenter - getFiveDaysWeatherFromServer - getWeatherData ");
                } catch (Exception e) {
                    Log.e(TAG, "Fail connection", e);
                    e.printStackTrace();
                } finally {
                    if (null != urlConnection) {
                        urlConnection.disconnect();
                    }
                }
            });
            t1.start();
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getLines(BufferedReader reader) {
        StringBuilder rawData = new StringBuilder(1024);
        String tempVariable;

        while (true) {
            try {
                tempVariable = reader.readLine();
                if (tempVariable == null) break;
                rawData.append(tempVariable).append("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawData.toString();
    }
}