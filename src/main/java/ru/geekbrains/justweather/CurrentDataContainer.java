package ru.geekbrains.justweather;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import ru.geekbrains.justweather.model.weather.HourlyWeatherData;
import ru.geekbrains.justweather.model.weather.WeatherData;

public class CurrentDataContainer implements Serializable {

    private static CurrentDataContainer instance = null;

    private static final Object syncObj = new Object();

    private CurrentDataContainer(){}

    public static CurrentDataContainer getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new CurrentDataContainer();
            }
            return instance;
        }
    }
    String currCityName = "Sochi";
    boolean[] switchSettingsArray;
    ArrayList<WeatherData> weekWeatherData = new ArrayList<>();
    ArrayList<HourlyWeatherData> hourlyWeatherList;
    ArrayList<String> citiesList = new ArrayList<>();
    static boolean isFirstEnter = true;
    static boolean isNightModeOn;
    static boolean NightIsAlreadySettedInMain;
    static Stack<String> backStack = new Stack<>();
}