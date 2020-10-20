package ru.geekbrains.justweather.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"city"})})
public class CitiesList {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "city")
    public String name;

    @ColumnInfo(name = "created")
    public long created;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    public CitiesList(){}

    public CitiesList(String cityName, Double latitude, Double longitude){
        this.name = cityName;
        this.created = System.currentTimeMillis()/1000L;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
