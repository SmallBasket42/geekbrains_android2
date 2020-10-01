package ru.geekbrains.justweather.rvDataAdapters;

import android.view.View;

public interface RVOnItemClick {
    void onItemClicked(View view, String itemText, int position);
    void onItemLongPressed(View itemText, int position);
}

