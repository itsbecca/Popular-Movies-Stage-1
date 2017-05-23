package com.example.android.popularmoviesstage1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private String sortPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.support_simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sortPreference = (String) parent.getItemAtPosition(position);
        Toast.makeText(parent.getContext(), sortPreference, Toast.LENGTH_SHORT).show();
//        Intent returnIntent = getIntent();
//        returnIntent.putExtra(Intent.EXTRA_TEXT,sortPreference);
//        setResult(Activity.RESULT_OK,returnIntent);
//        finish();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
