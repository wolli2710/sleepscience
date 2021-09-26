package com.science.babytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class StartActivity extends AppCompatActivity {

    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        createButtonHandler();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createButtonHandler(){
        startButton = (Button)findViewById(R.id.buttonStart);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userId = (EditText)findViewById(R.id.editTextId);
                EditText userAge = (EditText)findViewById(R.id.editTextAge);
                EditText userGroup = (EditText)findViewById(R.id.editTextGroup);


                Data.userId = userId.getText().toString();
                Data.userAge = userAge.getText().toString();
                Data.userGroup = userGroup.getText().toString();

                if(valid()) {
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(StartActivity.this, "Input is not valid!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean valid(){
        if(Data.userId.length() != 0 && Data.userAge.length() != 0 && Data.userGroup.length() != 0){
            if(isNumerical(Data.userAge) ){
                if(isValidGroup(Data.userGroup)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidGroup(String num){
        return (num.equals("1") || num.equals("2"));
    }
    private boolean isNumerical(String num){
        return num.matches("[0-9]+");
    }
}