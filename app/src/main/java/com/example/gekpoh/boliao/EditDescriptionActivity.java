package com.example.gekpoh.boliao;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditDescriptionActivity extends AppCompatActivity {

    private EditText editTextDescription;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_description);

        editTextDescription = findViewById(R.id.editTextDescription);
        saveBtn = findViewById(R.id.saveBtn);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            String currentDescription = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
            editTextDescription.setText(currentDescription);
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(Intent.EXTRA_TEXT, editTextDescription.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
