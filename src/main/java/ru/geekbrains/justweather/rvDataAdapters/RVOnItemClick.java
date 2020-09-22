package ru.geekbrains.justweather.rvDataAdapters;

import android.view.View;

public interface RVOnItemClick {
    void onItemClicked(View view, String itemText);
    void onItemLongPressed(View itemText);
}
