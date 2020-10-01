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

        public CitiesList(){}

        public CitiesList(String cityName){this.name = cityName; this.created = System.currentTimeMillis()/1000L;}
    }


