package ru.geekbrains.justweather;

public class SettingsPresenter {

    private static SettingsPresenter instance = null;

    private static final Object syncObj = new Object();
    private boolean isNightModeSwitchOn;
    private boolean isPressureSwitchOn;
    private boolean isFeelsLikeSwitchOn;
    private boolean[] settingsArray;

    private SettingsPresenter(){}

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

    public static SettingsPresenter getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new SettingsPresenter();
            }
            return instance;
        }
    }
}