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
import java.util.Collections;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import ru.geekbrains.justweather.model.weather.HourlyWeatherData;
import ru.geekbrains.justweather.model.weather.WeatherData;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

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
        String tempMax;
        String tempMin;
        ArrayList<String> fiveDaysTempMax = new ArrayList<>();
        ArrayList<String> fourDayTempMin = new ArrayList<>();
        ArrayList<String> weatherStateInfoArrayList = new ArrayList<>();
        ArrayList<Integer> weatherIconsArrayList  = new ArrayList<>();
        ArrayList<String> degreesArrayList  = new ArrayList<>();
        ArrayList<String> windInfoArrayList  = new ArrayList<>();
        ArrayList<String> pressureArrayList  = new ArrayList<>();
        ArrayList<String> feelLikeArrayList  = new ArrayList<>();
        fiveDaysTempMax.add("0");
        fourDayTempMin.add("0");
        weatherStateInfoArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(0).getWeather().get(0).getDescription()));
        weatherIconsArrayList.add(weatherRequest.getList().get(0).getWeather().get(0).getId());
        degreesArrayList.add(String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(0).getMain().getTemp())));
        windInfoArrayList.add(String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(0).getWind().getSpeed())));
        pressureArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(0).getMain().getPressure()));
        feelLikeArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(0).getMain().getFeelsLike()));
        int days = 0;
        for (int i = 0; i < weatherRequest.getList().size(); i++) {
            String time = String.format(Locale.getDefault(),
                    "%s", weatherRequest.getList().get(i).getDtTxt()).substring(11, 16);
            if (time.equals("00:00") && days <= ChooseCityPresenter.FORECAST_DAYS - 2){
                ArrayList<Float> dayMaxTemp = new ArrayList<>();
                ArrayList<Float> dayMinTemp = new ArrayList<>();
                for (int j = i; j < i + 8 ; j++) {
                    dayMaxTemp.add(weatherRequest.getList().get(j).getMain().getTempMax());
                    dayMinTemp.add(weatherRequest.getList().get(j).getMain().getTempMin());
                }
                float currDayMaxTemp = Collections.max(dayMaxTemp);
                float currDayMinTemp = Collections.min(dayMinTemp);
                days++;
                fiveDaysTempMax.add(String.format(Locale.getDefault(), "%s", currDayMaxTemp));
                fourDayTempMin.add(String.format(Locale.getDefault(), "%s", currDayMinTemp));
                weatherStateInfoArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i+4).getWeather().get(0).getDescription()));
                weatherIconsArrayList.add(weatherRequest.getList().get(i+4).getWeather().get(0).getId());
                degreesArrayList.add(String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(i+4).getMain().getTemp())));
                windInfoArrayList.add(String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(i+4).getWind().getSpeed())));
                pressureArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i+4).getMain().getPressure()));
                feelLikeArrayList.add(String.format(Locale.getDefault(), "%s", weatherRequest.getList().get(i+4).getMain().getFeelsLike()));
            }
        }
        for (int i = 0; i < ChooseCityPresenter.FORECAST_DAYS; i++) {
            Log.d("WEATHER", "List Weather forcast size = " + weatherRequest.getList().size());
            String degrees = degreesArrayList.get(i);
            String windInfo = windInfoArrayList.get(i);
            String pressure = pressureArrayList.get(i);
            String weatherStateInfo = weatherStateInfoArrayList.get(i);
            String feelLike = feelLikeArrayList.get(i);
            int weatherIcon = weatherIconsArrayList.get(i);
            tempMax = fiveDaysTempMax.get(i);
            tempMin = fourDayTempMin.get(i);
            WeatherData weatherData = new WeatherData(resources, degrees, windInfo, pressure, weatherStateInfo, feelLike, weatherIcon, tempMax, tempMin);
            weekWeatherData.add(weatherData);
            Log.d("tempMaxMin from int= ", tempMax + " " + tempMin);
        }
    }
    private void getHourlyWeatherData(WeatherRequest weatherRequest) {
        hourlyWeatherData = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            String temperature = String.format(Locale.getDefault(), "%s", Math.round(weatherRequest.getList().get(i).getMain().getTemp()));
            int weatherId = weatherRequest.getList().get(i).getWeather().get(0).getId();
            String time = String.format(Locale.getDefault(),
                    "%s", weatherRequest.getList().get(i).getDtTxt()).substring(11, 16);
            HourlyWeatherData hourlyForecastData = new HourlyWeatherData(time, weatherId, temperature);
            hourlyWeatherData.add(hourlyForecastData);
        }
    }
}