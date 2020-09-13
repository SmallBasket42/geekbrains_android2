package ru.geekbrains.justweather;

public class SettingsActivityPresenter {
    private static SettingsActivityPresenter instance = null;
    private static final Object syncObj = new Object();
    private boolean isNightModeSwitchOn;
    private boolean isPressureSwitchOn;
    private boolean isFeelsLikeSwitchOn;
    private boolean[] settingsArray;

    private SettingsActivityPresenter(){}

    public boolean[] getSettingsArray(){return settingsArray;}

    public void changeFeelsLikeSwitchStatus(){
       isFeelsLikeSwitchOn = !isFeelsLikeSwitchOn;
    }

    public boolean getIsNightModeSwitchOn(){return isNightModeSwitchOn;}

    public void changeNightModeSwitchStatus(){
        isNightModeSwitchOn = !isNightModeSwitchOn;
    }

    public void changePressureSwitchStatus(){
        isPressureSwitchOn = !isPressureSwitchOn;
    }

    public boolean[] createSettingsSwitchArray(){
        settingsArray =  new boolean[]{isNightModeSwitchOn, isFeelsLikeSwitchOn, isPressureSwitchOn};
        return  settingsArray;
    }

    public static SettingsActivityPresenter getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new SettingsActivityPresenter();
            }
            return instance;
        }
    }
}
