package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

public class Covid19Symptoms extends AppCompatActivity  {

    private LinearLayout lnLyVMild;
    private LinearLayout lnLyVSevere;
    private String [] strArrSymptoms = null;
    private String sympUpdated = "";
    private int checkedCounter = 0;
    private int currentOptionsChecked = 0;
    private Button btnUpdated;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        setContentView(R.layout.activity_covid19_symptoms);
        lnLyVMild = findViewById(R.id.lnLyVFacts);
        lnLyVSevere = findViewById(R.id.lnLyVSevere);
        btnUpdated = findViewById(R.id.btnUpdated);

        strArrSymptoms = getIntent().getStringArrayExtra("symptomsList");
        currentOptionsChecked = 0;
        createSymptonsBoxes();
        checkedCounter = 0;

        btnUpdated.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createSymptonsBoxes(){

        if(strArrSymptoms == null)
            return;
        int doCount = 0;
        int dontCount = 0;
        int backgColor = getColor(R.color.colorFactVsMythDef);
        for (int i = 0; i < strArrSymptoms.length; ++i)
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

            //fielValue(Position)--> classification(0);symptom(1);checked(2)
            String[] strLst = strArrSymptoms[i].split(";");
            chxBox.setText(strLst[1]);
            boolean state = Integer.parseInt(strLst[2]) == 1 ? true : false;
            currentOptionsChecked = state ? currentOptionsChecked + 1 : currentOptionsChecked;
            chxBox.setChecked(state);
            chxBox.setTextSize(16);
            if(strLst[0].compareTo("Mild") == 0){
                lnLyVMild.addView(chxBox);
                if(doCount % 2 == 0)
                    chxBox.setBackgroundColor(backgColor);
                ++doCount;
            }
            else{
                lnLyVSevere.addView(chxBox);
                if(dontCount % 2 == 0 )
                    chxBox.setBackgroundColor(backgColor);
                ++dontCount;
            }
        }
    }

    private void updateCheckBoxState(String str, int checked){

        if(strArrSymptoms == null)
            return;

        for (int i = 0; i < strArrSymptoms.length; ++i){
            String[] strLst = strArrSymptoms[i].split(";");
            if(strLst[1].compareTo(str) == 0)
            {
                strLst[2] = String.valueOf(checked);
                String newString = strLst[1] + ";" + strLst[2];
                sympUpdated = sympUpdated.isEmpty() ? newString : sympUpdated + "@" + newString;
                ++checkedCounter;
                if(checkedCounter != 0 && currentOptionsChecked != checkedCounter)
                    btnUpdated.setEnabled(true);

                return;
            }
        }

    }

    public void updateSymtopms(View vw){
        Intent intMainAct = new Intent();
        intMainAct.putExtra("sympUpdated", sympUpdated);
        setResult(Activity.RESULT_OK,intMainAct);
        finish();
    }
}
