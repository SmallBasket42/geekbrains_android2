package ru.geekbrains.justweather.database;


import java.util.List;

public class CitiesListSource {

    private final CitiesListDao citiesListDao;
    private List<CitiesList> citiesList;

    public CitiesListSource(CitiesListDao citiesListDao){
        this.citiesListDao = citiesListDao;
    }

    public List<CitiesList> getCitiesList(){

        if (citiesList == null){
            loadCitiesListSortedByCreated();
        }
        return citiesList;
    }

    public void loadCitiesListSortedByCreated(){
        citiesList = citiesListDao.getAllCities();
    }

    public long getCountCities(){
        return citiesListDao.getCountCities();
    }

    public void addCity(CitiesList city){
        CitiesList cityFromDB = citiesListDao.getCityByName(city.name);
        if (cityFromDB != null){
            updateCityCreatedTime(cityFromDB.name);
        } else citiesListDao.insertCity(city);

    }

    public void updateCity(CitiesList city){
        citiesListDao.updateCity(city);
        loadCitiesListSortedByCreated();
    }

    public void removeCity(long id){
        citiesListDao.deteleCityById(id);
        loadCitiesListSortedByCreated();
    }

    public void updateCityCreatedTime(String cityName) {
        long currentTime = System.currentTimeMillis() / 1000L;
        citiesListDao.updateCreatedTime(cityName, currentTime);
    }

    public void loadCitiesListSortedByName(){
        citiesList = citiesListDao.sortByName();
    }
}

