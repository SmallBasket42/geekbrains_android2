package ru.geekbrains.justweather.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

    @Dao
    public interface CitiesListDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertCity(CitiesList city);

        @Update
        void updateCity(CitiesList city);

        @Delete
        void deleteCity(CitiesList city);

        @Query("DELETE FROM citieslist WHERE id = :id")
        void deteleCityById(long id);

        @Query("SELECT * FROM citieslist ORDER BY created DESC")
        List<CitiesList> getAllCities();

        @Query("SELECT * FROM citieslist WHERE id = :id")
        CitiesList getCityById(long id);

        @Query("SELECT * FROM citieslist WHERE city = :name")
        CitiesList getCityByName(String name);

        @Query("SELECT COUNT() FROM citieslist")
        long getCountCities();

        @Query("update citieslist set created = :currentTime where city = :cityName")
        void updateCreatedTime(String cityName, long currentTime);

        @Query("SELECT * FROM citieslist ORDER BY city ASC")
        List<CitiesList> sortByName();

    }


