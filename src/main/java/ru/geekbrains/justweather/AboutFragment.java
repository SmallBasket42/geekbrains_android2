package ru.geekbrains.justweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    static AboutFragment create(CurrentDataContainer container) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putSerializable("currCity", container);
        fragment.setArguments(args);
        Log.d("myLog", "AboutFragment CREATE");
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("myLog", "onCreate - fragment AboutFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("myLog", "onCreateView - fragment SettingsFragment");
        return getView() != null ? getView() :
                inflater.inflate(R.layout.fagment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton sendMail = view.findViewById(R.id.writeToUs);
        sendMail.setOnClickListener(view1 -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","alexander.mukha23@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Just Weather");
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });
        super.onViewCreated(view, savedInstanceState);
    }

}