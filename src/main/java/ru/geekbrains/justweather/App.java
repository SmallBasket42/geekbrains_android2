package ru.geekbrains.justweather;

import android.app.Application;
import androidx.room.Room;
import ru.geekbrains.justweather.database.CitiesListDao;
import ru.geekbrains.justweather.database.CitiesListDatabase;

public class App extends Application {

    private static App instance;
    private CitiesListDatabase db;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        db = Room.databaseBuilder(
                getApplicationContext(),
                CitiesListDatabase.class,
                "cities_list_database")
                .build();
    }

    public CitiesListDao getCitiesListDao() {
        return db.getCitiesListDao();
    }
}
