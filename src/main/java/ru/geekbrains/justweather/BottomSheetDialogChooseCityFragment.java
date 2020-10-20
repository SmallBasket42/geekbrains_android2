package ru.geekbrains.justweather;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.IOException;
import java.util.List;
import ru.geekbrains.justweather.database.CitiesList;
import ru.geekbrains.justweather.database.CitiesListDao;
import ru.geekbrains.justweather.database.CitiesListSource;
import ru.geekbrains.justweather.events.OpenWeatherMainFragmentEvent;
import ru.geekbrains.justweather.forecastRequest.ForecastRequest;
import ru.geekbrains.justweather.forecastRequest.OpenWeatherMap;

import static android.content.Context.MODE_PRIVATE;

public class BottomSheetDialogChooseCityFragment extends BottomSheetDialogFragment {
    private EditText enterCityEditText;
    private TextView chooseCityTextView;

    static BottomSheetDialogChooseCityFragment newInstance() {
        return new BottomSheetDialogChooseCityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_dialog, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(true);

        chooseCityTextView = view.findViewById(R.id.choose_city_textView);
        enterCityEditText = view.findViewById(R.id.enter_city_editText);
        enterCityEditText.setOnKeyListener((view1, keyCode, keyEvent) -> {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (enterCityEditText.getText().toString().length() != 0) {
                    checkIsShowingWeatherPossible(enterCityEditText.getText().toString().trim());
                }
                return true;
            }
            return false;
        });
    }

    @SuppressLint("ResourceAsColor")
    private void checkIsShowingWeatherPossible(String cityName){

        OpenWeatherMap openWeatherMap = OpenWeatherMap.getInstance();
        findCoordinatesByCityName(cityName);
        ForecastRequest.getForecastFromServer(CurrentDataContainer.cityLatitude,  CurrentDataContainer.cityLongitude);
        Log.d("retrofit", "countDownLatch = " + ForecastRequest.getForecastResponseReceived().getCount());
        Handler handler = new Handler();
        new Thread(() -> {
            try {
                ForecastRequest.getForecastResponseReceived().await();

                if(ForecastRequest.responseCode == 200) {
                    String newCityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);

                    CurrentDataContainer.isFirstEnter = false;
                    CurrentDataContainer.isFirstCityInSession = false;
                    CurrentDataContainer.getInstance().weekWeatherData = openWeatherMap.getWeekWeatherData(getResources());
                    CurrentDataContainer.getInstance().hourlyWeatherList = openWeatherMap.getHourlyWeatherData();

                    CitiesListDao citiesListDao = App
                            .getInstance()
                            .getCitiesListDao();
                    CitiesListSource citiesListSource = new CitiesListSource(citiesListDao);
                    citiesListSource.addCity(new CitiesList(newCityName, CurrentDataContainer.cityLatitude, CurrentDataContainer.cityLongitude));

                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE);
                    saveCityToPreference(sharedPreferences, newCityName);

                    saveIsFirstEnterToPreference(sharedPreferences, CurrentDataContainer.isFirstEnter);

                    requireActivity().runOnUiThread(() -> {
                        dismiss();
                        EventBus.getBus().post(new OpenWeatherMainFragmentEvent());
                    });
                }
                if(ForecastRequest.responseCode == 400 || ForecastRequest.responseCode == 404){
                    handler.post(()->{
                        enterCityEditText.setText("");
                        chooseCityTextView.setText(R.string.city_not_found);
                        chooseCityTextView.setTextColor(R.color.colorPrimary);
                    });
                }
                if(ForecastRequest.responseCode != 400 && ForecastRequest.responseCode != 200 && ForecastRequest.responseCode != 404){
                    Log.d("response", "bottomSheetFragment responseCode = "+ ForecastRequest.responseCode);
                    handler.post(()->{
                        enterCityEditText.setText("");
                        chooseCityTextView.setText(R.string.connection_failed);
                        chooseCityTextView.setTextColor(R.color.colorPrimary);
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void findCoordinatesByCityName(String cityName){

        final Geocoder geo = new Geocoder(getContext());
        List<Address> list = null;

        try {
            list = geo.getFromLocationName(cityName, 1);
        } catch (IOException e) {
            e.printStackTrace();
            //                return e.getLocalizedMessage();
        }

        if (list != null && !list.isEmpty()) {

            Address address = list.get(0);
            CurrentDataContainer.cityLatitude = address.getLatitude();
            CurrentDataContainer.cityLongitude = address.getLongitude();
        } else {
            CurrentDataContainer.cityLatitude = null;
            CurrentDataContainer.cityLongitude = null;
        }
    }


    private void saveCityToPreference(SharedPreferences preferences, String currentCity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current city", currentCity);
        editor.apply();
    }
    private void saveIsFirstEnterToPreference(SharedPreferences preferences, boolean isFirstEnter) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstEnter", isFirstEnter);
        editor.apply();
    }
}