package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FactsVsMyths extends AppCompatActivity {

    private LinearLayout lnLyVMild;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facts_vs_myths);
//Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        lnLyVMild = (LinearLayout) findViewById(R.id.lnLyVFacts);

        String [] strArrFactsVsMyths = getIntent().getStringArrayExtra("factsVsMythsList");
        createFactsTextViews(strArrFactsVsMyths);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createFactsTextViews(String [] facts){
        String classfMyth = "Myth";
        String classfFact = "Fact";
        String classfMythTrans = getString(R.string.txtMyth);
        String classfFactTrans = getString(R.string.txtFact);
        int mythColor = getColor(R.color.colorMyth);
        int factColor = getColor(R.color.colorFact);
        int defColor  = getColor(R.color.colorFactVsMythDef);
        //int colorMythId = getApplicationContext().getResources(). colorMyth;
        for (int i = 0; i < facts.length; ++i)
        {
            TextView texVwClassf = new TextView(getApplicationContext());
            TextView texVwDefin = new TextView(getApplicationContext());

            //fielValue(Position)--> classification(0);Definition(1)
            String[] strLst = facts[i].split(";");

            texVwClassf.setTypeface(null, Typeface.BOLD);
            texVwClassf.setGravity(Gravity.CENTER);
            texVwClassf.setTextSize(16);
            if(strLst[0].compareTo(classfMyth) == 0){
                texVwClassf.setText(classfMythTrans);
                texVwClassf.setBackgroundColor(mythColor);
            }else if(strLst[0].compareTo(classfFact) == 0){
                texVwClassf.setText(classfFactTrans);
                texVwClassf.setBackgroundColor(factColor);
            }

            lnLyVMild.addView(texVwClassf);
            texVwDefin.setText(strLst[1]);
            texVwDefin.setBackgroundColor(defColor);
            texVwDefin.setTextSize(16);
            texVwDefin.setPadding(40,5,5,40);
            lnLyVMild.addView(texVwDefin);

        }
    }
}
