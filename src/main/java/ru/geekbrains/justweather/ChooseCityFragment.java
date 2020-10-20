package ru.geekbrains.justweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import ru.geekbrains.justweather.placeDetailsRequest.GooglePlaceDetails;
import ru.geekbrains.justweather.placeDetailsRequest.PlaceDetailsRequest;
import ru.geekbrains.justweather.rvDataAdapters.CitiesRecyclerDataAdapter;
import ru.geekbrains.justweather.rvDataAdapters.GooglePlacesRecyclerDataAdapter;
import ru.geekbrains.justweather.rvDataAdapters.PlacesRVOnItemClick;
import ru.geekbrains.justweather.rvDataAdapters.RVOnItemClick;

import static android.content.Context.MODE_PRIVATE;

public class ChooseCityFragment extends Fragment implements RVOnItemClick, PlacesRVOnItemClick {

    private TextInputEditText enterCity;
    static String currentCity = "";
    private RecyclerView recyclerView, placesRV;
    private CitiesRecyclerDataAdapter adapter;
    private GooglePlacesRecyclerDataAdapter placesAdapter;
    private ArrayList<WeatherData> weekWeatherData = new ArrayList<>();
    private ArrayList<HourlyWeatherData> hourlyWeatherList = new ArrayList<>();
    final String myLog = "myLog";
    OpenWeatherMap openWeatherMap = OpenWeatherMap.getInstance();
    private boolean isErrorShown;
    Pattern checkEnterCity = Pattern.compile("^[а-яА-ЯЁa-zA-Z]+(?:[\\s-][а-яА-ЯЁa-zA-Z]+)*$");
    private CitiesListSource citiesListSource;
    PlacesClient placesClient;
    private boolean isCityChosen;
    private ConstraintLayout constraintLayout;
    private ArrayList<String> placesIds;
    private boolean coordinatesByIdGot;


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
        String apiKey = "AIzaSyCsdcjzPbl1ShsdyDF3ssaAEomQAOkC7L0";
        Places.initialize(requireActivity().getApplicationContext(), apiKey);
        placesClient = Places.createClient(requireContext());
        setupPlacesRecyclerView(null);
        addTextChangedListenerToEnterCityEditText();
        setOnEnterCityEnterKeyListener();
    }

    private void initViews(View view) {
        enterCity = view.findViewById(R.id.enterCity);
        recyclerView = view.findViewById(R.id.cities);
        placesRV = view.findViewById(R.id.places);
        constraintLayout = view.findViewById(R.id.choose_city_constraint);

    }

    private void getPlacesList(String query){
        Log.d("places", "setOnEnterCityClickListener");

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.CITIES)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            ArrayList<String> placesFullText = new ArrayList<>();
            placesIds = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.d("places", prediction.getPrimaryText(null).toString());
                Log.d("places", prediction.getPlaceTypes().toString());
                Log.d("places", prediction.getPlaceId());
                placesFullText.add(prediction.getFullText(null).toString());
                placesIds.add(prediction.getPlaceId());
            }
            showPlacesLIst(placesFullText);
            placesAdapter.updatePlacesList(placesFullText);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.d("places", "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    private void addTextChangedListenerToEnterCityEditText(){
        enterCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String userQuery = Objects.requireNonNull(enterCity.getText()).toString();
                if(userQuery.length() >= 3 && !isCityChosen) {
                    getPlacesList(userQuery);
                }
                if(userQuery.length() >= 3 && isCityChosen) {
                    recyclerView.setVisibility(View.VISIBLE);
                    placesRV.setVisibility(View.GONE);
                }
                if(userQuery.length() < 3) {
                    Log.d("places", "CATCH");
                    placesAdapter.updatePlacesList(new ArrayList<>());
                    recyclerView.setVisibility(View.VISIBLE);
                    placesRV.setVisibility(View.GONE);
                    isCityChosen = false;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showPlacesLIst(ArrayList<String> places) {
        if(places.size() > 0) {
            Log.d("places", "showPlacesLIst - move places to place of cities");
            placesRV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.places, ConstraintSet.TOP, R.id.enterCityLayout, ConstraintSet.BOTTOM, 0);
        } else {
            placesRV.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void setupPlacesRecyclerView(ArrayList<String> places){
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity().getBaseContext());

        if (placesRV.getItemDecorationCount() <= 0){
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity().getBaseContext(), LinearLayoutManager.VERTICAL);
            placesRV.addItemDecoration(itemDecoration);
        }
        placesAdapter = new GooglePlacesRecyclerDataAdapter(places, this);
        placesRV.setLayoutManager(layoutManager);
        placesRV.setAdapter(placesAdapter);

    }

    @Override
    public void onPlaceItemClicked(View view, String itemText, int position) {
        isCityChosen = true;
        placesRV.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        String place = itemText.split(",", 2)[0];
        enterCity.setText(place);
        String cityId = placesIds.get(position);
        findCoordinatesById(cityId);
    }

    private void findCoordinatesById(String placeId) {
        PlaceDetailsRequest.getPlaceDetails(placeId);
        new Thread(()->{
            try {
                PlaceDetailsRequest.detailsResponseReceived.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            GooglePlaceDetails googlePlaceDetails = GooglePlaceDetails.getInstance();
            googlePlaceDetails.getCityCoordinates();
            requireActivity().runOnUiThread(()-> {
                if(googlePlaceDetails.getCityLatitude() != null) {
                    CurrentDataContainer.cityLongitude = GooglePlaceDetails.getInstance().getCityLongitude();
                    coordinatesByIdGot = true;
                } else {
                    CurrentDataContainer.cityLongitude = null;
                    coordinatesByIdGot = false;
                }
                if(googlePlaceDetails.getCityLongitude() != null) {
                    CurrentDataContainer.cityLatitude = GooglePlaceDetails.getInstance().getCityLatitude();
                    coordinatesByIdGot = true;
                } else {
                    CurrentDataContainer.cityLatitude = null;
                    coordinatesByIdGot = false;
                }
                Log.d("places", "coordinates: " + CurrentDataContainer.cityLatitude + " " + CurrentDataContainer.cityLongitude);

            });
        }).start();
    }

    private void findCoordinatesByCityName(String cityName){
        final Geocoder geo = new Geocoder(getContext());
        List<Address> list = null;

        try {
            list = geo.getFromLocationName(cityName, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list != null && !list.isEmpty()) {
            Address address = list.get(0);
            CurrentDataContainer.cityLatitude = address.getLatitude();
            CurrentDataContainer.cityLongitude = address.getLongitude();
        } else {
            CurrentDataContainer.cityLatitude = null;
            CurrentDataContainer.cityLongitude = null;
        }
        coordinatesByIdGot = false;
        Log.d("geocoder", "GEOCODER: latitude = "+ CurrentDataContainer.cityLatitude + " logitude = "+CurrentDataContainer.cityLongitude );
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

                                if (ForecastRequest.responseCode == 404 || ForecastRequest.responseCode == 400) {
                                    Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);
                                    handler.post(()->{
                                        showAlertDialog(R.string.city_not_found);
                                        currentCity = previousCity;
                                    });
                                }
                                if (ForecastRequest.responseCode == 200) {
                                    CurrentDataContainer.isFirstEnter = false;
                                    CurrentDataContainer.isFirstCityInSession = false;

                                    citiesListSource.addCity(new CitiesList(currentCity, CurrentDataContainer.cityLatitude, CurrentDataContainer.cityLongitude));
                                    if(CurrentDataContainer.isCitiesListSortedByName) adapter.sortByName();
                                    else adapter.sortByCreatedTime();
                                    saveCurrentCityToPreference(requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE), currentCity);

                                    saveIsFirstEnterToPreference(requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE), CurrentDataContainer.isFirstEnter);

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

    private void saveCurrentCityToPreference(SharedPreferences preferences, String currentCity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current city", currentCity);
        editor.apply();
    }

    private void saveIsFirstEnterToPreference(SharedPreferences preferences, boolean isFirstEnter) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstEnter", isFirstEnter);
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
        String previousCity = sharedPreferences.getString("current city", null);
        saveCurrentCityToPreference(sharedPreferences, currentCity);

        Thread thread = new Thread(()->{
            CitiesListDao citiesListDao = App
                    .getInstance()
                    .getCitiesListDao();
            citiesListSource = new CitiesListSource(citiesListDao);
            List<Double> coord = citiesListSource.getCoordinatesFromDB(currentCity);
            CurrentDataContainer.cityLatitude = coord.get(0);
            CurrentDataContainer.cityLongitude = coord.get(1);
            Log.d("coord", "cityLatitude and cityLongitude from db = "+ CurrentDataContainer.cityLatitude + " " + CurrentDataContainer.cityLongitude);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ForecastRequest.getForecastFromServer(CurrentDataContainer.cityLatitude, CurrentDataContainer.cityLongitude);


        Handler handler = new Handler();
        new Thread(() -> {
            try {
                ForecastRequest.getForecastResponseReceived().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(ForecastRequest.responseCode == 200) {
                CurrentDataContainer.isFirstEnter = false;
                CurrentDataContainer.isFirstCityInSession = false;
                saveIsFirstEnterToPreference(requireActivity().getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE), CurrentDataContainer.isFirstEnter);

                Log.d(myLog, "RESPONSE COD = " + ForecastRequest.responseCode + " CURR CITY = " + currentCity);

                weekWeatherData = openWeatherMap.getWeekWeatherData(getResources());
                hourlyWeatherList = openWeatherMap.getHourlyWeatherData();
                requireActivity().runOnUiThread(() -> {
                    CurrentDataContainer.getInstance().weekWeatherData = weekWeatherData;
                    CurrentDataContainer.getInstance().hourlyWeatherList = hourlyWeatherList;
                    updateWeatherData();
                });
            } else {
                saveCurrentCityToPreference(sharedPreferences, previousCity);

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
        if (!coordinatesByIdGot) findCoordinatesByCityName(currentCity);
        coordinatesByIdGot = false;
        ForecastRequest.getForecastFromServer(CurrentDataContainer.cityLatitude, CurrentDataContainer.cityLongitude);
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