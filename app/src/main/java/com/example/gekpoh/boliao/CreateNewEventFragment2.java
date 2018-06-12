package com.example.gekpoh.boliao;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class CreateNewEventFragment2 extends Fragment {

    private static final int RC_PHOTO_PICKER = 1;

    private ImageView imageViewActivityPic;
    private EditText editTextNumPeople;
    private EditText editTextDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_event_fragment_layout2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        imageViewActivityPic = getView().findViewById(R.id.imageViewActivityPic);
        editTextNumPeople = getView().findViewById(R.id.editTextNumPeople);
        editTextDescription = getView().findViewById(R.id.editTextDescription);

        imageViewActivityPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_editprofilepic, popupMenu.getMenu());



                popupMenu.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public String sendNumPeople() {return editTextNumPeople.getText().toString(); }
    public String sendDescription() { return editTextDescription.getText().toString(); }
}
