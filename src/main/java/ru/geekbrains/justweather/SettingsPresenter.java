package ru.geekbrains.justweather;

public class SettingsPresenter {

    private static SettingsPresenter instance = null;
    private static final Object syncObj = new Object();
    private boolean isNightModeSwitchOn;
    private boolean isPressureSwitchOn;
    private boolean isFeelsLikeSwitchOn;

    private SettingsPresenter(){}
    public void changeFeelsLikeSwitchStatus(){
       isFeelsLikeSwitchOn = !isFeelsLikeSwitchOn;
    }
    public boolean getIsNightModeSwitchOn(){return isNightModeSwitchOn;}
    public boolean getIsPressureSwitchOn(){return isPressureSwitchOn;}
    public boolean getIsFeelsLikeSwitchOn(){return isFeelsLikeSwitchOn;}
    public void changeNightModeSwitchStatus(){
        isNightModeSwitchOn = !isNightModeSwitchOn;
    }
    public void changePressureSwitchStatus(){
        isPressureSwitchOn = !isPressureSwitchOn;
    }

    public static SettingsPresenter getInstance(){

        synchronized (syncObj) {
            if (instance == null) {
                instance = new SettingsPresenter();
            }
            return instance;
        }
    }
}
