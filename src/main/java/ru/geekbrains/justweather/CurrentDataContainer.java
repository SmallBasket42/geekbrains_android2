package ru.geekbrains.justweather;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import ru.geekbrains.justweather.model.HourlyWeatherData;
import ru.geekbrains.justweather.model.WeatherData;

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

    ArrayList<WeatherData> weekWeatherData;
    ArrayList<HourlyWeatherData> hourlyWeatherList;
    static boolean isFirstEnter = true;
    static Stack<String> backStack = new Stack<>();
    static boolean isCitiesListSortedByName;
}
