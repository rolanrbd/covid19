package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HowToStop extends AppCompatActivity {

    private LinearLayout lnLyVFacts;
    private String [] howToStop = null;
    private String howToStopDoUpdated = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_stop);
//Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        lnLyVFacts = (LinearLayout) findViewById(R.id.lnLyVFacts);

        howToStop = getIntent().getStringArrayExtra("howToStopList");
        createCheckboxes();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createCheckboxes(){

        int backgroundColor = getColor(R.color.colorFactVsMythDef);
        for (int i = 0; i < howToStop.length; ++i)
        {
            CheckBox chxBox = new CheckBox(getApplicationContext());

            chxBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String btnTxt = buttonView.getText().toString();
                    int checked = isChecked ? 1 : 0;
                    updateCheckBoxState(btnTxt, checked);
                }
            });

            //fielValue(Position)--> id(0), description(1);checked(2)
            String[] strLst = howToStop[i].split(";");
            chxBox.setText(strLst[1]);
            chxBox.setChecked(Integer.parseInt(strLst[2]) == 1 ? true : false);
            chxBox.setTextSize(16);
            chxBox.setPadding(0,10,10,10);
            if(i%2 != 0)
                chxBox.setBackgroundColor(backgroundColor);
            lnLyVFacts.addView(chxBox);
        }
    }

    private void updateCheckBoxState(String str, int checked){

        if(howToStop == null)
            return;

        //classification string, description string, why string, checked int
        for (int i = 0; i < howToStop.length; ++i){
            String[] strLst = howToStop[i].split(";");
            if(strLst[1].compareTo(str) == 0)
            {
                strLst[2] = String.valueOf(checked);
                String newString = strLst[0] + ";" + strLst[2];
                howToStopDoUpdated = howToStopDoUpdated.isEmpty() ? newString : howToStopDoUpdated + "@" + newString;

                return;
            }
        }

    }

    public void updateMyActions(View vw){
        Intent intMainAct = new Intent();
        intMainAct.putExtra("howToStopUpdated", howToStopDoUpdated);
        setResult(Activity.RESULT_OK,intMainAct);
        finish();
    }
}
