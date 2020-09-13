package ru.geekbrains.justweather;

import com.squareup.otto.Bus;

public class EventBus {
    private static Bus bus;

    static Bus getBus() {
        if(bus == null) {
            bus = new Bus();
        }
        return bus;
    }
}

