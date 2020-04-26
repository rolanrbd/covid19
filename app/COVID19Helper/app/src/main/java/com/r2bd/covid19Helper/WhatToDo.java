package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WhatToDo extends AppCompatActivity {

    private LinearLayout lnLyVDo;
    private LinearLayout lnLyVDoNot;
    private String [] whatToDo = null;
    private String whatToDoUpdated = "";
    private int checkedCounter = 0;
    private int currentOptionsChecked = 0;
    private Button btnUpdateWhatToDo;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_to_do);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        lnLyVDo = findViewById(R.id.lnLyVDo);
        lnLyVDoNot = findViewById(R.id.lnLyVDoNot);
        btnUpdateWhatToDo = findViewById(R.id.btnUpdateWhatToDo);
        whatToDo = getIntent().getStringArrayExtra("whatToDoList");
        currentOptionsChecked = 0;
        createWhatToDoBoxes();
        checkedCounter = 0;

        btnUpdateWhatToDo.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createWhatToDoBoxes(){

        if(whatToDo == null)
            return;

        int doCount = 0;
        int dontCount = 0;
        int backgColor = getColor(R.color.colorFactVsMythDef);
        for (int i = 0; i < whatToDo.length; ++i)
        {
            CheckBox chxBox = new CheckBox(getApplicationContext());
            chxBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

            chxBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String btnTxt = buttonView.getText().toString();
                    int checked = isChecked ? 1 : 0;
                    updateCheckBoxState(btnTxt, checked);
                }
            });
            //fielValue(Position)-->id(0), description(1), classification(2), why(3), checked(4)

            String[] strLst = whatToDo[i].split(";");
            chxBox.setText(strLst[1]);
            boolean state = Integer.parseInt(strLst[4]) == 1 ? true : false;
            chxBox.setChecked(state);
            chxBox.setTextSize(16);
            if(strLst[2].compareTo("Do") == 0){
                lnLyVDo.addView(chxBox);
                if(doCount % 2 == 0)
                    chxBox.setBackgroundColor(backgColor);
                ++doCount;
            }
            else{
                lnLyVDoNot.addView(chxBox);
                if(dontCount % 2 == 0 )
                    chxBox.setBackgroundColor(backgColor);
                ++dontCount;
            }

        }
    }

    private void updateCheckBoxState(String str, int checked){

        if(whatToDo == null)
            return;

        //classification string, description string, why string, checked int
        for (int i = 0; i < whatToDo.length; ++i){
            String[] strLst = whatToDo[i].split(";");
            if(strLst[1].compareTo(str) == 0)
            {
                strLst[4] = String.valueOf(checked);
                String newString = strLst[0] + ";" + strLst[4];
                whatToDoUpdated = whatToDoUpdated.isEmpty() ? newString : whatToDoUpdated + "@" + newString;
                ++checkedCounter;
                if(checkedCounter != 0 && currentOptionsChecked != checkedCounter)
                    btnUpdateWhatToDo.setEnabled(true);

                return;
            }
        }

    }

    public void updateWhatIamDoing(View vw){
        Intent intMainAct = new Intent();
        intMainAct.putExtra("whatToDoUpdated", whatToDoUpdated);
        setResult(Activity.RESULT_OK,intMainAct);
        finish();
    }
}
