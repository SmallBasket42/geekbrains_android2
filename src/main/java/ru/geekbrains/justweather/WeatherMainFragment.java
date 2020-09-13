package ru.geekbrains.justweather;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherMainFragment extends Fragment implements RVOnItemClick {
    private boolean isLandscape;
    public static String currentCity = "";
    private TextView cityTextView;
    private TextView degrees;
    private TextView feelsLikeTextView, pressureInfoTextView;
    final String myLog = "myLog";
    private RecyclerView weatherRecyclerView;
    private RecyclerView hourlyRecyclerView;
    private List<Integer> weatherIcon = new ArrayList<>();
    private List<String> days = new ArrayList<>();
    private List<String> daysTemp = new ArrayList<>();
    private List<String> hourlyTime = new ArrayList<>();
    private List<Integer> hourlyWeatherIcon = new ArrayList<>();
    private List<String> hourlyTemperature = new ArrayList<>();
    private TextView windInfoTextView;
    private TextView currTime;
    private TextView weatherStatusTextView;
    private ArrayList<String> citiesListFromRes;
    private ArrayList<String> citiesList;
    private ArrayList<WeatherData> weekWeatherData;
    private ArrayList<HourlyWeatherData> hourlyWeatherData;


    static WeatherMainFragment create(CurrentDataContainer container) {
        WeatherMainFragment fragment = new WeatherMainFragment();
        Bundle args = new Bundle();
        args.putSerializable("currCity", container);
        fragment.setArguments(args);
        Log.d("myLog", "WeatherMainFragment CREATE");
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("myLog", "onCreate - fragment WeatherMainFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("myLog", "onCreateView - fragment WeatherMainFragment");
        return getView() != null ? getView() :
                inflater.inflate(R.layout.fragment_weather_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        moveViewsIfLandscapeOrientation(view);
        takeCitiesListFromResources(getResources());
        generateDaysList();
        addDataToWeatherIconsIdFromRes(weatherIcon);
        addDefaultDataToDaysTempFromRes(getResources());
        addDefaultDataToHourlyWeatherRV(getResources());
        updateWeatherInfo(getResources());
        setupRecyclerView();
        setupHourlyWeatherRecyclerView();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(myLog, "WeatherMainFragment - savedInstanceState exists = " + (savedInstanceState != null));
        updateChosenCity(savedInstanceState);
        takeWeatherInfoForFirstEnter();
        Log.d(myLog, "WeatherMainFragment: onActivityCreated !AFTER updateChosenCity, currentCity: " + currentCity);
    }

    private void takeWeatherInfoForFirstEnter(){
        if(CurrentDataContainer.isFirstEnter){
            Log.d(myLog, "*FIRST ENTER*");
            ChooseCityPresenter chooseCityPresenter = ChooseCityPresenter.getInstance();
            chooseCityPresenter.getFiveDaysWeatherFromServer(currentCity, getResources());
            this.weekWeatherData = chooseCityPresenter.getWeekWeatherData();
            this.hourlyWeatherData = chooseCityPresenter.getHourlyWeatherData();
            updateWeatherInfo(getResources());
            Log.d(myLog, "takeWeatherInfoForFirstEnter - after updateWeatherInfo;  CITIES LIST = "+ citiesList.toString());
            setupRecyclerView();
            setupHourlyWeatherRecyclerView();
        } else {
            Log.d(myLog, "*NOT FIRST ENTER*");
        }
    }

    private void moveViewsIfLandscapeOrientation( View view){
        isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape) {
            ConstraintLayout constraintLayout = view.findViewById(R.id.full_screen_constraintlayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.setVerticalBias(R.id.center, 0.67f);
            constraintSet.connect(R.id.degrees,ConstraintSet.BOTTOM,R.id.center,ConstraintSet.TOP,0);
            constraintSet.setVisibility(R.id.weekWeatherRV, View.GONE);
            constraintSet.applyTo(constraintLayout);
        }
    }

    private void initViews(View view) {

        cityTextView = view.findViewById(R.id.city);
        degrees = view.findViewById(R.id.degrees);
        feelsLikeTextView = view.findViewById(R.id.feelsLikeTextView);
        pressureInfoTextView = view.findViewById(R.id.pressureInfoTextView);
        hourlyRecyclerView = view.findViewById(R.id.hourlyWeatherRV);
        weatherRecyclerView = view.findViewById(R.id.weekWeatherRV);
        windInfoTextView = view.findViewById(R.id.windSpeed);
        currTime = view.findViewById(R.id.currTime);
        weatherStatusTextView = view.findViewById(R.id.cloudyInfoTextView);
    }

    private String getCityName() {
        currentCity = CurrentDataContainer.getInstance().currCityName;
        return currentCity;
    }

    private void updateChosenCity(Bundle savedInstanceState) {
        if (savedInstanceState == null) cityTextView.setText(getCityName());
        cityTextView.setText(currentCity);
    }

    private  void updateWeatherInfo(Resources resources){
        boolean[] settingsSwitchArray;
        this.citiesList = citiesListFromRes;
        if(CurrentDataContainer.isFirstEnter) {
            CurrentDataContainer.getInstance().citiesList = citiesListFromRes;
            if(ChooseCityPresenter.responseCode != 200) {
                Log.d(myLog, "updateWeatherInfo from resources");

                degrees.setText("+0°");

                String windInfoFromRes = resources.getString(R.string.windInfo);
                windInfoTextView.setText(String.format(windInfoFromRes, "0"));

                Date currentDate = new Date();
                DateFormat timeFormat = new SimpleDateFormat("E, HH:mm", Locale.getDefault());
                String timeText = timeFormat.format(currentDate);
                currTime.setText(timeText);
                Log.d(myLog, "WEatherMainFragment - updateWeatherInfo - FIRSTENTER; responseCode != 200; CITIES LIST = " + citiesList.toString());
            } else {
                setNewWeatherData(weekWeatherData, hourlyWeatherData);
                settingsSwitchArray = CurrentDataContainer.getInstance().switchSettingsArray;
                isSettingsSwitchArrayTransferred(settingsSwitchArray);
                setNewWeatherData(weekWeatherData, hourlyWeatherData);
                Log.d(myLog, "WEatherMainFragment - updateWeatherInfo - FIRSTENTER; responseCode == 200; CITIES LIST = " + citiesList.toString());
            }
        }
        if(!CurrentDataContainer.isFirstEnter) {
            currentCity = CurrentDataContainer.getInstance().currCityName;
            settingsSwitchArray = CurrentDataContainer.getInstance().switchSettingsArray;
            weekWeatherData = CurrentDataContainer.getInstance().weekWeatherData;
            hourlyWeatherData = CurrentDataContainer.getInstance().hourlyWeatherList;
            citiesList = CurrentDataContainer.getInstance().citiesList;

            isSettingsSwitchArrayTransferred(settingsSwitchArray);
            setNewWeatherData(weekWeatherData, hourlyWeatherData);
        }
    }

    private void isSettingsSwitchArrayTransferred(boolean[] settingsSwitchArray){
        Log.d(myLog, "NightIsAlreadySettedInMain " + CurrentDataContainer.NightIsAlreadySettedInMain );
        Log.d(myLog, "NightMode " + CurrentDataContainer.isNightModeOn);
        if(settingsSwitchArray != null) {
            if (settingsSwitchArray[0] && !CurrentDataContainer.NightIsAlreadySettedInMain) {
                CurrentDataContainer.NightIsAlreadySettedInMain = true;
                CurrentDataContainer.isNightModeOn = true;
                requireActivity().recreate();
                Log.d(myLog, " RECREATE weather main fragment");
            }
            if (!settingsSwitchArray[0]) CurrentDataContainer.isNightModeOn = false;
            if (settingsSwitchArray[1]) feelsLikeTextView.setVisibility(View.VISIBLE);
            if (settingsSwitchArray[2]) pressureInfoTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setNewWeatherData(ArrayList<WeatherData> weekWeatherData, ArrayList<HourlyWeatherData> hourlyWeatherData) {
        if (weekWeatherData != null && weekWeatherData.size() != 0 && hourlyWeatherData != null && hourlyWeatherData.size() != 0) {
            WeatherData wd = weekWeatherData.get(0);
            degrees.setText(wd.degrees);
            windInfoTextView.setText(wd.windInfo);

            Date currentDate = new Date();
            DateFormat timeFormat = new SimpleDateFormat("E, HH:mm", Locale.getDefault());
            String timeText = timeFormat.format(currentDate);
            currTime.setText(timeText);

            weatherStatusTextView.setText(wd.weatherStateInfo);
            pressureInfoTextView.setText(wd.pressure);
            feelsLikeTextView.setText(wd.feelLike);

            for (int i = 0; i < ChooseCityPresenter.FORECAST_DAYS; i++) {
               WeatherData weatherData = weekWeatherData.get(i);
               daysTemp.set(i, weatherData.degrees);
                String imageName =weatherData.weatherIcon;
                Log.d(myLog, "ICON " + i + " " +  imageName);
                Integer resID = getResources().getIdentifier(imageName , "drawable", requireActivity().getPackageName());
                weatherIcon.set(i, resID);
            }
            for (int i = 0; i < 8 ; i++) {
                HourlyWeatherData hourlyData = hourlyWeatherData.get(i);
                hourlyTime.set(i, hourlyData.getTime());
                String iconName = hourlyData.getStateImage();
                Integer iconId =  getResources().getIdentifier(iconName , "drawable", requireActivity().getPackageName());
                hourlyWeatherIcon.set(i,iconId);
                hourlyTemperature.set(i, hourlyData.getTemperature());
            }
        }
    }

    public void takeCitiesListFromResources(android.content.res.Resources resources){
        String[] cities = resources.getStringArray(R.array.cities);
        List<String> cit = Arrays.asList(cities);
        citiesListFromRes = new ArrayList<>(cit);
    }

    public void generateDaysList(){
        Date currentDate = new Date();
        DateFormat timeFormat = new SimpleDateFormat("E", Locale.getDefault());
        String curDay = timeFormat.format(currentDate);
        Log.d(myLog, "CURDAY = " + curDay);
        ArrayList<String> daysList = new ArrayList<>();
        daysList.add(curDay);
        for (int i = 1; i <ChooseCityPresenter.FORECAST_DAYS ; i++) {
            Calendar instance = Calendar.getInstance(Locale.getDefault());
            instance.add(Calendar.DAY_OF_MONTH, i); // прибавляем 1 день к установленной дате
            Date nextDate = instance.getTime(); // получаем измененную дату
            String nextDay = timeFormat.format(nextDate);
            daysList.add(nextDay);
        }
        Log.d(myLog, "WEEK: "+ daysList.toString());
        days = daysList;
    }

    public void addDefaultDataToDaysTempFromRes(android.content.res.Resources resources){
        String[] daysTempStringArr = resources.getStringArray(R.array.daysTemp);
        daysTemp  = Arrays.asList(daysTempStringArr);
    }

    public void addDataToWeatherIconsIdFromRes(List<Integer> weatherIcon){
        weatherIcon.add(R.drawable.clear_sky_day);
        weatherIcon.add(R.drawable.few_clouds_day);
        weatherIcon.add(R.drawable.scattered_clouds);
        weatherIcon.add(R.drawable.broken_clouds);
        weatherIcon.add(R.drawable.shower_rain);
        weatherIcon.add(R.drawable.rain_day);
        weatherIcon.add(R.drawable.thunderstorm);
        weatherIcon.add(R.drawable.snow);
        weatherIcon.add(R.drawable.mist);
    }

    public void addDefaultDataToHourlyWeatherRV(android.content.res.Resources resources){
        String[] hourlyTempStringArr = resources.getStringArray(R.array.daysTemp);
        hourlyTemperature  = Arrays.asList(hourlyTempStringArr);

        String[] hoursStringArr = resources.getStringArray(R.array.hours);
        hourlyTime  = Arrays.asList(hoursStringArr);

        addDataToWeatherIconsIdFromRes(hourlyWeatherIcon);
    }

    @Override
    public void onItemClicked(View view, String itemText) {}

    @Override
    public void onItemLongPressed(View itemText) {}

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity().getBaseContext(), LinearLayoutManager.VERTICAL, false);
        WeekWeatherRecyclerDataAdapter weekWeatherAdapter = new WeekWeatherRecyclerDataAdapter(days, daysTemp, weatherIcon, this);

        weatherRecyclerView.setLayoutManager(layoutManager);
        weatherRecyclerView.setAdapter(weekWeatherAdapter);
    }

    private void setupHourlyWeatherRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity().getBaseContext(), LinearLayoutManager.HORIZONTAL, false);
        HourlyWeatherRecyclerDataAdapter hourlyWeatherRecyclerDataAdapter = new HourlyWeatherRecyclerDataAdapter(hourlyTime, hourlyWeatherIcon, hourlyTemperature, this);

        hourlyRecyclerView.setLayoutManager(layoutManager);
        hourlyRecyclerView.setAdapter(hourlyWeatherRecyclerDataAdapter);
    }
}