package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

public class Covid19Symptoms extends AppCompatActivity  {

    private LinearLayout lnLyVMild;
    private LinearLayout lnLyVSevere;
    private String [] strArrSymptoms = null;
    private String sympUpdated = "";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        setContentView(R.layout.activity_covid19_symptoms);
        lnLyVMild = (LinearLayout) findViewById(R.id.lnLyVFacts);
        lnLyVSevere = (LinearLayout) findViewById(R.id.lnLyVSevere);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels / 2;

        lnLyVMild.getLayoutParams().width = width;
        lnLyVSevere.getLayoutParams().width = width;

        strArrSymptoms = getIntent().getStringArrayExtra("symptomsList");
        createSymptonsBoxes();
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
            chxBox.setChecked(Integer.parseInt(strLst[2]) == 1 ? true : false);
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
