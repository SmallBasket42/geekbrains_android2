package ru.geekbrains.justweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import ru.geekbrains.justweather.database.CitiesList;
import ru.geekbrains.justweather.database.CitiesListDao;
import ru.geekbrains.justweather.database.CitiesListSource;
import ru.geekbrains.justweather.events.OpenWeatherMainFragmentEvent;
import ru.geekbrains.justweather.forecastRequest.ForecastRequest;
import ru.geekbrains.justweather.forecastRequest.OpenWeatherMap;
import ru.geekbrains.justweather.model.HourlyWeatherData;
import ru.geekbrains.justweather.model.WeatherData;
import ru.geekbrains.justweather.rvDataAdapters.CitiesRecyclerDataAdapter;
import ru.geekbrains.justweather.rvDataAdapters.RVOnItemClick;
import static android.content.Context.MODE_PRIVATE;

public class ChooseCityFragment extends Fragment implements RVOnItemClick {

    private TextInputEditText enterCity;
    static String currentCity = "";
    private RecyclerView recyclerView;
    private CitiesRecyclerDataAdapter adapter;
    private ArrayList<WeatherData> weekWeatherData = new ArrayList<>();
    private ArrayList<HourlyWeatherData> hourlyWeatherList = new ArrayList<>();
    final String myLog = "myLog";
    OpenWeatherMap openWeatherMap = OpenWeatherMap.getInstance();
    private boolean isErrorShown;
    Pattern checkEnterCity = Pattern.compile("^[а-яА-ЯЁa-zA-Z]+(?:[\\s-][а-яА-ЯЁa-zA-Z]+)*$");
    private CitiesListSource citiesListSource;

    static ChooseCityFragment create(CurrentDataContainer container) {
        ChooseCityFragment fragment = new ChooseCityFragment();
        Bundle args = new Bundle();
        args.putSerializable("currCity", container);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("myLog", "onCreate - fragment SettingsFragment");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.choose_city_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sorting){
            if(!CurrentDataContainer.isCitiesListSortedByName) {
                adapter.sortByName();
                CurrentDataContainer.isCitiesListSortedByName = true;
                Toast.makeText(getContext(), R.string.alfabetical_sorting, Toast.LENGTH_SHORT).show();
            } else {
                adapter.sortByCreatedTime();
                CurrentDataContainer.isCitiesListSortedByName = false;
                Toast.makeText(getContext(), R.string.sorting_by_date, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("myLog", "onCreateView - fragment SettingsFragment");
        return getView() != null ? getView() :
                inflater.inflate(R.layout.fragment_choose_city, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        checkEnterCityField();
        setupRecyclerView();
        if(CurrentDataContainer.isCitiesListSortedByName) adapter.sortByName();
        setOnEnterCityEnterKeyListener();
    }

    private void initViews(View view) {
        enterCity = view.findViewById(R.id.enterCity);
        recyclerView = view.findViewById(R.id.cities);
    }

    private void setOnEnterCityEnterKeyListener() {
        enterCity.setOnKeyListener((view, keyCode, keyEvent) -> {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                enterCity.setEnabled(false);
                if (isErrorShown) {
                    enterCity.setEnabled(true);
                    Toast.makeText(requireActivity(), R.string.setOnBtnOkEnterCityToast, Toast.LENGTH_SHORT).show();
                }
                if (!isErrorShown) {
                    enterCity.setEnabled(true);
                    if (!Objects.requireNonNull(enterCity.getText()).toString().equals("")) {
                        String previousCity = requireActivity()
                                .getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE)
                                .getString("current city", "Sochi");
                        currentCity = enterCity.getText().toString();
                        currentCity = currentCity.substring(0, 1).toUpperCase() + currentCity.substring(1);
                        takeWeatherInfoForFiveDays();
                        Handler handler = new Handler();
                        new Thread(() -> {
                                try {
                                    ForecastRequest.getForecastResponseReceived().await();

                                if (ForecastRequest.responseCode == 404) {
                                    Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);
                                    handler.post(()->{
                                        showAlertDialog(R.string.city_not_found);
                                        currentCity = previousCity;
                                    });
                                }
                                if (ForecastRequest.responseCode == 200) {
                                    CurrentDataContainer.isFirstEnter = false;
                                    citiesListSource.addCity(new CitiesList(currentCity));
                                    if(CurrentDataContainer.isCitiesListSortedByName) adapter.sortByName();
                                    else adapter.sortByCreatedTime();
                                    saveToPreference(requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE), currentCity);
                                    Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);
                                    weekWeatherData = openWeatherMap.getWeekWeatherData(getResources());
                                    hourlyWeatherList = openWeatherMap.getHourlyWeatherData();
                                    requireActivity().runOnUiThread(() -> {
                                    CurrentDataContainer.getInstance().weekWeatherData = weekWeatherData;
                                    CurrentDataContainer.getInstance().hourlyWeatherList = hourlyWeatherList;
                                    Toast.makeText(requireActivity(), currentCity, Toast.LENGTH_SHORT).show();
                                    updateWeatherData();
                                    enterCity.setText("");
                                    });
                                }
                                if (ForecastRequest.responseCode != 200 && ForecastRequest.responseCode != 404) {
                                    Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);
                                    handler.post(()-> {
                                        showAlertDialog(R.string.connection_failed);
                                        currentCity = previousCity;
                                    });
                                }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                }).start();
                            }
                    Log.d(myLog, "ChooseCityFragment - setOnBtnOkEnterCityClickListener -> BEFORE flag");
                    }
                return true;
            }
            return false;
        });
    }

    private void saveToPreference(SharedPreferences preferences, String currentCity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current city", currentCity);
        editor.apply();
    }

    private void showAlertDialog(int messageId){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.sorry_alert_dialog)
                .setMessage(messageId)
                .setIcon(R.drawable.ic_baseline_sentiment_dissatisfied_24)
                .setPositiveButton(R.string.ok,
                        (dialog, id) -> enterCity.setText(""));
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateWeatherData(){
        EventBus.getBus().post(new OpenWeatherMainFragmentEvent());
    }

    @Override
    public void onItemClicked(View view, String itemText, int position) {
        currentCity = itemText;
        adapter.putChosenCityToTopInCitiesList(currentCity);
        if(CurrentDataContainer.isCitiesListSortedByName) {adapter.sortByName();}
        else {adapter.sortByCreatedTime();}
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE);
        saveToPreference(sharedPreferences, currentCity);

        takeWeatherInfoForFiveDays();
        Handler handler = new Handler();
        new Thread(() -> {
            try {
                ForecastRequest.getForecastResponseReceived().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(ForecastRequest.responseCode == 200) {
                CurrentDataContainer.isFirstEnter = false;
                Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);

                weekWeatherData = openWeatherMap.getWeekWeatherData(getResources());
                hourlyWeatherList = openWeatherMap.getHourlyWeatherData();
                requireActivity().runOnUiThread(() -> {
                    CurrentDataContainer.getInstance().weekWeatherData = weekWeatherData;
                    CurrentDataContainer.getInstance().hourlyWeatherList = hourlyWeatherList;
                    updateWeatherData();
                });
            } else {
                Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);
                handler.post(()->showAlertDialog(R.string.connection_failed));
            }
        }).start();
        Log.d(myLog, "ChooseCityFragment - setOnBtnOkEnterCityClickListener -> BEFORE flag");
    }

    @Override
    public void onItemLongPressed(View view, int position) {
        TextView textView = (TextView) view;
        deleteItem(textView, position);
    }

    public void deleteItem(final TextView view, int position) {
        Snackbar.make(view, R.string.delete_city, Snackbar.LENGTH_LONG)
                .setAction(R.string.delete, v -> {
                    Thread thread = new Thread(()-> {
                        adapter.remove(position);
                        CitiesList cityForRemove = citiesListSource
                                .getCitiesList()
                                .get(position);
                        citiesListSource.removeCity(cityForRemove.id);
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(CurrentDataContainer.isCitiesListSortedByName) adapter.sortByName();
                }).show();
    }

    private void takeWeatherInfoForFiveDays(){
        ForecastRequest.getForecastFromServer(currentCity);
        Log.d("retrofit", "ChooseCityFragment - countDownLatch = " + ForecastRequest.getForecastResponseReceived().getCount());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity().getBaseContext());

        CitiesListDao citiesListDao = App
                .getInstance()
                .getCitiesListDao();
        citiesListSource = new CitiesListSource(citiesListDao);

        if (recyclerView.getItemDecorationCount() <= 0){
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity().getBaseContext(), LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(itemDecoration);
        }

        adapter = new CitiesRecyclerDataAdapter(citiesListSource, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void checkEnterCityField() {
        final TextView[] tv = new TextView[1];
        enterCity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                tv[0] = (TextView) v;
                validate(tv[0], checkEnterCity, getString(R.string.HintTextInputEditText));
                hideSoftKeyboard(requireActivity(), enterCity);
            }
        });
    }

    public static void hideSoftKeyboard (Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private void validate(TextView tv, Pattern check, String message){
        String value = tv.getText().toString();
        if (check.matcher(value).matches()) {
            hideError(tv);
            isErrorShown = false;
        } else {
            showError(tv, message);
            isErrorShown = true;
        }
    }
    private void showError(TextView view, String message) {
        view.setError(message);
    }
    private void hideError(TextView view) {
        view.setError(null);
    }
}