package ru.geekbrains.justweather.placeDetailsRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.geekbrains.justweather.model.placeDetails.FetchPlaceRequest;
import ru.geekbrains.justweather.model.weather.WeatherRequest;

public interface PlaceDetailsInterface {
    @GET("maps/api/place/details/json")
    Call<FetchPlaceRequest> loadPlaceDetails (@Query("place_id") String id,
                                        @Query("key") String keyApi);
}
