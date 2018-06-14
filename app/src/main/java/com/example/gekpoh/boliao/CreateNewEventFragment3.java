package com.example.gekpoh.boliao;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CreateNewEventFragment3 extends Fragment {

    private Button buttonLocationSelect;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_event_fragment_layout3, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        buttonLocationSelect = getView().findViewById(R.id.buttonLocationSelect);
        buttonLocationSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startLocationSelectActivityIntent = new Intent(getActivity(), CreateNewEventMapActivity.class);
                startActivity(startLocationSelectActivityIntent);
            }
        });
    }
}
