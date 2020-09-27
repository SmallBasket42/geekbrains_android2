package ru.geekbrains.justweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImageTranscoderType;
import com.facebook.imagepipeline.core.MemoryChunkType;
import com.google.android.material.navigation.NavigationView;
import com.squareup.otto.Subscribe;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ru.geekbrains.justweather.events.OpenWeatherMainFragmentEvent;

public class MainActivity extends AppCompatActivity {

    public NavigationView navigationView;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        setHomeFragment();
        setOnClickForSideMenuItems();
        Fresco.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            CurrentDataContainer.backStack.pop();
            String currFragmentName = CurrentDataContainer.backStack.peek();
            if(currFragmentName.equals("WeatherMainFragment")) navigationView.setCheckedItem(R.id.nav_home);
            if(currFragmentName.equals("ChooseCityFragment")) navigationView.setCheckedItem(R.id.nav_choose_city);
            if(currFragmentName.equals("AboutFragment")) navigationView.setCheckedItem(R.id.nav_about);
            if(currFragmentName.equals("SettingsFragment")) {
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getBus().unregister(this);
        super.onStop();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onOpenFragmentEvent(OpenWeatherMainFragmentEvent event) {
        setHomeFragment();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    private void setOnClickForSideMenuItems() {
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home: {
                    setHomeFragment();
                    drawer.close();
                    break;
                }
                case R.id.nav_choose_city: {
                    setChooseCityFragment();
                    drawer.close();
                    break;
                }
                case R.id.nav_about: {
                    setAboutFragment();
                    drawer.close();
                    break;
                }
            }
            return true;
        });
    }

    public void setHomeFragment() {
        setFragment(WeatherMainFragment.create(CurrentDataContainer.getInstance()), WeatherMainFragment.class.getSimpleName());
    }

    private void setChooseCityFragment() {
        setFragment(ChooseCityFragment.create(CurrentDataContainer.getInstance()), ChooseCityFragment.class.getSimpleName());
    }

    private void setAboutFragment() {
        setFragment(AboutFragment.create(CurrentDataContainer.getInstance()), AboutFragment.class.getSimpleName());
        Log.d("BACKSTACK", AboutFragment.class.getSimpleName());
    }

    private void setSettingsFragment(){
        setFragment(SettingsFragment.create(CurrentDataContainer.getInstance()), SettingsFragment.class.getSimpleName());
    }

    public void setFragment(Fragment fragment, String fragmentName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.addToBackStack(fragmentName);
        CurrentDataContainer.backStack.addElement(fragmentName);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            setSettingsFragment();
        }
        if (item.getItemId() == R.id.action_read_more){
            String wiki = "https://ru.wikipedia.org/wiki/" + CurrentDataContainer.getInstance().currCityName;
            Uri uri = Uri.parse(wiki);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return false;
    }
}