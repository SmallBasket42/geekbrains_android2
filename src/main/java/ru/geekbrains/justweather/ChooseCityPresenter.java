package ru.geekbrains.justweather;

import android.content.res.Resources;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import ru.geekbrains.justweather.model.WeatherRequest;

public final class ChooseCityPresenter {
    public static final int FORECAST_DAYS = 5;
    final String myLog = "myLog";
    private static final String TAG = "WEATHER";
    String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private ArrayList<WeatherData> weekWeatherData;
    private ArrayList<HourlyWeatherData> hourlyWeatherData;
    public static int responseCode;

    private static ChooseCityPresenter instance = null;

    private static final Object syncObj = new Object();

    private ChooseCityPresenter(){}

    public static ChooseCityPresenter getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new ChooseCityPresenter();
            }
            return instance;
        }
    }
    public ArrayList<WeatherData> getWeekWeatherData(){return weekWeatherData;}
    public ArrayList<HourlyWeatherData> getHourlyWeatherData(){return hourlyWeatherData;}

       public void getFiveDaysWeatherFromServer(String currentCity, Resources resources){
           try {
               final URL uri = getWeatherUrl(currentCity);
               Thread t1 = new Thread(() -> {
                   HttpsURLConnection urlConnection = null;
                   try {
                       urlConnection = (HttpsURLConnection) uri.openConnection();
                       urlConnection.setRequestMethod("GET");
                       urlConnection.setReadTimeout(10000);
                       responseCode = urlConnection.getResponseCode();
                       Log.d(myLog, "###getFiveDaysWeatherFromServer responseCod = " + responseCode);
                       Log.d(myLog, "###getFiveDaysWeatherFromServer currentCity = " + currentCity);
                       BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                       String result = getLines(in);
                       Gson gson = new Gson();
                       final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);
                       getWeatherData(weatherRequest, resources);
                       getHourlyWeatherData(weatherRequest);
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
           } catch (MalformedURLException e) {
               Log.e(TAG, "Fail URI", e);
               e.printStackTrace();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }

    private URL getWeatherUrl(String cityName) throws MalformedURLException {
        return new URL(BASE_URL + "forecast?q=" + cityName + "&units=metric&appid=" + "849b888638d27a2b5c80b65cfc590f12");
    }

    private String getLines(BufferedReader reader) {
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

    public void getWeatherData(WeatherRequest weatherRequest, Resources resources){
        weekWeatherData = new ArrayList<>();
        for (int i = 0; i < weatherRequest.getList().size(); i += 8) {
            Log.d("WEATHER", "List Weather forecast size = " + weatherRequest.getList().size());
            String degrees = String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(i).getMain().getTemp()));
            String windInfo = String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(i).getWind().getSpeed()));
            String pressure = String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i).getMain().getPressure());
            String weatherStateInfo = String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i).getWeather().get(0).getDescription());
            String feelLike = String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i).getMain().getFeelsLike());
            int weatherIcon = weatherRequest.getList().get(i).getWeather().get(0).getId();
            WeatherData weatherData = new WeatherData(resources, degrees, windInfo, pressure, weatherStateInfo, feelLike, weatherIcon);
            weekWeatherData.add(weatherData);
            Log.d(myLog, i + weatherData.toString());
        }
    }

    private void getHourlyWeatherData(WeatherRequest weatherRequest) {
        hourlyWeatherData = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String temperature = Math.round(Float.parseFloat(String.format(Locale.getDefault(),
                    "%.2f", weatherRequest.getList().get(i).getMain().getTemp()))) + "°";
            int weatherId = weatherRequest.getList().get(i).getWeather().get(0).getId();
            String time = String.format(Locale.getDefault(),
                    "%s", weatherRequest.getList().get(i).getDtTxt()).substring(11, 16);
            HourlyWeatherData hourlyForecastData = new HourlyWeatherData(time, weatherId, temperature);
            hourlyWeatherData.add(hourlyForecastData);
        }
    }
}



