package com.r2bd.covid19Helper;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.r2bd.covid19Helper.Adapters.ListCountriesAdapter;
import com.r2bd.covid19Helper.Adapters.ViewPagerAdapter;
import com.r2bd.covid19Helper.Alarms.Utils;
import com.r2bd.covid19Helper.Country.Country;
import com.r2bd.covid19Helper.Country.CountryFactory;
import com.r2bd.covid19Helper.Country.Cuba.Cuba;
import com.r2bd.covid19Helper.Fragments.NowFragment;
import com.r2bd.covid19Helper.Fragments.YesterdayFragment;
import com.r2bd.covid19Helper.Fragments.YourCountryFragment;
import com.r2bd.covid19Helper.Models.CasesValues;
import com.r2bd.covid19Helper.Models.SharedViewModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements  NowFragment.OnRefreshCasesDataNow,
                                                                NowFragment.OnClickCountryListView,
                                                                YesterdayFragment.OnRefreshCasesDataYesterday,
                                                                YourCountryFragment.OnRefreshCasesDataOfYourCountry

{

    private static final int REQUEST_PERMISSION_PHONE_STATE = 1;
    private static final int RETURN_CODE_SYMPTOMS    = 1001;
    private static final int RETURN_CODE_WHAT_TO_DO  = 1002;
    private static final int RETURN_CODE_HOW_TO_STOP = 1003;
    private static final int RETURN_CODE_SETTINGS    = 1004;
    private static String DATE_CURRENT = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private static int DAILY_RECORD_COUNTER = 0;
    private long timeStartRecord = 0;

    private ImageView imgBtnFoodBank;
    private ImageView imgVwAdviceViewer;
    private ImageView imgZoom = null;
    private TextView txtVwDescription = null;

    private MediaRecorder audioRecorder = null;
    private String audioOutput = null;
    private ImageButton imgBtnRecord;

    private ArrayList<Integer> LAST_IMG_ID = new ArrayList<>();
    private ArrayList<String> LAST_IMG_NAME = new ArrayList<>();
    private String[] imgListName = null;
    private String currenIMG = "cvd19_main_view";
    String currentCountrySelected = "USA";
    TabLayout tbLyData;
    ViewPager2 vwPager;
    ViewPagerAdapter vwPagAdapter;
    SharedViewModel casesViewModel;
    NowFragment fNow;
    YesterdayFragment fYesterday;
    YourCountryFragment fYourCountry;
    CasesValues caseData;
    
    //***********----------covid19tracker----------------******************
    String url = "https://www.worldometers.info/coronavirus/";
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, tmpNewCases, tmpNewDeaths;
    Document doc;
    Element countriesTable, row;
    Elements countriesRows, cols;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListCountriesAdapter listCountriesAdapter;
    ArrayList<CountryLine> allCountriesResults; //FilteredArrList;
    Intent sharingIntent;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths, colNumActive, colNumNewCases, colNumNewDeaths;
    Iterator<Element> rowIterator;
    ProgressBar countryProgressBar;

    SwipeRefreshLayout mySwipeRefreshLayout = null;
    ListView listViewCountries = null;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        //Getting the preferences of the application
        androidx.preference.PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        SettingsAppValues.getPreferences(preferences, this, false);

        try {
            settingsUpdated();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Setting the phone numbers
        Button btnNotifyCOVID19 = findViewById(R.id.btnNotifyCOVID19);
        btnNotifyCOVID19.setText(btnNotifyCOVID19.getText().toString() + " COVID19+");

        //Check food bank numbers
        imgBtnFoodBank = findViewById(R.id.imgBtnFoodBank);
        imgBtnFoodBank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View vw) {
                callFoodBank(vw);
            }
        });

        checkAudioRecordPermission();
        try {
            createRequiredDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        computeLasNumbOfRecord();
        imgBtnRecord = findViewById(R.id.imgBtnRecord);
        imgBtnRecord.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View vw, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            startAudioRecorder(vw);
                        } catch (IOException e) {}
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // touch move code
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            startAudioRecorder(vw);
                        } catch (IOException e) {}
                        break;
                }
                return false;
            }
        });

        createDBCovid19Helper();

        if (isDBEmpty()) {
            updateTables();

            //Last update
            AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
            SQLiteDatabase db = dbAdmin.getWritableDatabase();

            int rowsDeleted = db.delete("covid19_state", "id=0", null);

            ContentValues rowValues = new ContentValues();
            //empty, dateLastUpdate
            rowValues.put("dateLastUpdate", "2020-04-11");
            rowValues.put("empty", 0);
            db.insert("covid19_state", null, rowValues);

            db.close();
        }

        populateImagenVector();

        imgVwAdviceViewer = findViewById(R.id.imgVwAdviceViewer);
        if (imgListName == null) {
            currenIMG = getDefault().getLanguage().compareTo("en") == 0 ? "cvd19_main_view;Be careful at all times" : "cvd19_main_view;¡Ten cuidado en todo momento!";
        } else {
            int limSup = imgListName == null ? 1 : imgListName.length;
            int idx = (int) Math.floor(Math.random() * (limSup));
            while (idx < 0 && idx > limSup)
                idx = (int) Math.floor(Math.random() * (limSup));
            currenIMG = imgListName[idx];
            int currId = getResources().getIdentifier(currenIMG.split(";")[0], "drawable", getPackageName());
            imgVwAdviceViewer.setImageDrawable(getDrawable(currId));
        }

        imgVwAdviceViewer.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alrDlg = new AlertDialog.Builder(MainActivity.this);

                String[] imgAndDescrip = currenIMG.split(";");
                imgZoom = new ImageView(v.getContext());
                int currId = getResources().getIdentifier(imgAndDescrip[0], "drawable", getPackageName());
                imgZoom.setImageDrawable(getDrawable(currId));

                LinearLayout lnLy = new LinearLayout(MainActivity.this);
                lnLy.setOrientation(LinearLayout.VERTICAL);

                txtVwDescription = new TextView(MainActivity.this);
                txtVwDescription.setText(imgAndDescrip[1]);
                txtVwDescription.setTextSize(16);
                txtVwDescription.setTypeface(Typeface.DEFAULT_BOLD);

                lnLy.addView(imgZoom);
                lnLy.addView(txtVwDescription);
                AlertDialog a = alrDlg.create();
                a.setView(lnLy);
                a.show();
            }
        });

        //**************--------covid19tracker-----------------***********************
        countryProgressBar = findViewById(R.id.countryProgressBarN);
        colNumCountry = 0; colNumCases = 1;colNumRecovered = 0; colNumDeaths = 0; colNumNewCases = 0; colNumNewDeaths = 0;
        preferences =  androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        generalDecimalFormat = new DecimalFormat("0.00", symbols);
        allCountriesResults = new ArrayList<>();

        //Tabs
        casesViewModel =  new ViewModelProvider(this).get(SharedViewModel.class);
        casesViewModel.getCasesData().observe(this, new Observer<CasesValues>() {
            @Override
            public void onChanged(CasesValues casesValues) {
                //Crear adapter para actualizar los datos
                caseData = casesValues;
            }
        });

        vwPager = findViewById(R.id.vwPager);
        tbLyData = findViewById(R.id.tbLyData);
        createViewPagerAdapter();
        if(preferences.getString(getString(R.string.txtFragmentSelectedTitle),null) != null)
            currentCountrySelected = preferences.getString(getString(R.string.txtFragmentSelectedTitle),null);
        else {
            editor.putString(getString(R.string.txtFragmentSelectedTitle), currentCountrySelected);
            editor.apply();
        }
    }

    private void createViewPagerAdapter() {
        vwPagAdapter = new ViewPagerAdapter(this);
        fNow = new NowFragment();
        vwPagAdapter.addFragment(fNow, getString(R.string.txtFragmentTitleNow));
        fYesterday = new YesterdayFragment();
        vwPagAdapter.addFragment(fYesterday, getString(R.string.txtFragmentTitleYesterday));
        fYourCountry = new YourCountryFragment();
        vwPagAdapter.addFragment(fYourCountry, getString(R.string.txtFragmentTitleCountry));
        vwPager.setAdapter(vwPagAdapter);

        new TabLayoutMediator(tbLyData, vwPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(vwPagAdapter.getTitleList().get(position));
                    }
                }
        ).attach();

    }

    public void goToActSettings(/*View vw*/) {
        //actionNoImpemented(vw);
        Intent intSettings = new Intent(this, Settings.class);
        startActivityForResult(intSettings, RETURN_CODE_SETTINGS);
    }

    public void goToActCovid19Symptom(View vw) {
        Intent intCovid19Symptom = new Intent(this, Covid19Symptoms.class);
        String[] strArrSymptoms = loadSymptoms();
        intCovid19Symptom.putExtra("symptomsList", strArrSymptoms);
        startActivityForResult(intCovid19Symptom, RETURN_CODE_SYMPTOMS);
    }

    public void goToActFacts(View vw) {
        //actionNoImpemented(vw);
        String[] strArrFactsVsMyths = loadFactsVsMyths();

        Intent intFacts = new Intent(this, FactsVsMyths.class);
        intFacts.putExtra("factsVsMythsList", strArrFactsVsMyths);
        startActivity(intFacts);
        //*/
    }

    public void goToActHowToStop(View vw) {
        //actionNoImpemented(vw);
        String[] strArrHowToStop = loadHowToStop();

        Intent intHowToStop = new Intent(this, HowToStop.class);
        intHowToStop.putExtra("howToStopList", strArrHowToStop);
        startActivityForResult(intHowToStop, RETURN_CODE_HOW_TO_STOP);
    }

    public void goToActRoutesList(View vw) {
        //actionNoImpemented(vw);
        Intent intRoutesList = new Intent(this, RoutesList.class);
        startActivity(intRoutesList);
        //*/
    }

    public void goToActWhatToDo(View vw) {
        Intent intWhatToDo = new Intent(this, WhatToDo.class);
        String[] strArrWhatToDo = loadWhatToDo();
        intWhatToDo.putExtra("whatToDoList", strArrWhatToDo);
        startActivityForResult(intWhatToDo, RETURN_CODE_WHAT_TO_DO);
    }

    public void goToActMedia(View vw) {
        //actionNoImpemented(vw);
        Intent intMedia = new Intent(this, Media.class);//AudioHistory.class
        String[] strVideoData = loadVideo();
        intMedia.putExtra("videoList", strVideoData);
        startActivity(intMedia);
        //*/
    }

    public void goToActNotifications(View vw) {
        actionNoImpemented(vw);
        /*
        Intent intNotifications = new Intent(this, Notifications.class);
        startActivity(intNotifications);
        //*/
    }

    public void goToActNews(View vw) {
        // actionNoImpemented(vw);

        Intent intNews = new Intent(this, News.class);
        startActivity(intNews);
        //*/
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void callEmergency(View vw) {
        //actionNoImpemented(vw);

        checkCallPermission();
        int permisition = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast t = Toast.makeText(this,"The current permission to call, it has been revoked",Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }
        if(SettingsAppValues.emergencyNumber.isEmpty()){
            Toast t = Toast.makeText(this,"It looks like you didn't set up a phone number for this action or the current phone number it's incorrect..",Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Uri callUri = Uri.parse("tel://" + SettingsAppValues.emergencyNumber);
        callIntent.setData(callUri);
        startActivity(callIntent);

    }

    public void callFoodBank(View vw) {

        checkCallPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast t = Toast.makeText(this,"The current permission to call, it has been revoked",Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }
        if(SettingsAppValues.foodBankNumber.isEmpty()){
            Toast t = Toast.makeText(this,"It looks like you didn't set up a phone number for this action or the current phone number it's incorrect..",Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Uri callUri = Uri.parse("tel://" + SettingsAppValues.foodBankNumber);
        callIntent.setData(callUri);
        startActivity(callIntent);
    }

    public void notifyCovid19Positive(View vw) {
        actionNoImpemented(vw);
    }

    public void showNextAdvice(View vw) {
        //actionNoImpemented(vw);
        Random rand = new Random(imgListName.length);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int limInf = 0;
            int limSup = imgListName == null ? 1 : imgListName.length;
            int idx = (int) Math.floor(Math.random()*(limSup));
            while (idx < 0 && idx > limSup)
                idx = (int) Math.floor(Math.random()*(limSup));

            String[] data = imgListName[idx].split(";");
            String name = data[0];

            LAST_IMG_NAME.add(currenIMG);
            int currId = getResources().getIdentifier(currenIMG.split(";")[0], "drawable",getPackageName());
            int id = getResources().getIdentifier(name, "drawable",getPackageName());
            imgVwAdviceViewer.setImageDrawable(getDrawable(id));

            LAST_IMG_ID.add(currId);
            currenIMG = imgListName[idx];

        }
    }

    public void showPreviousAdvice(View vw) {
        //actionNoImpemented(vw);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int id;
            if(LAST_IMG_ID.isEmpty()){
                id = getResources().getIdentifier("cvd19_main_view", "drawable",getPackageName());
                currenIMG =  getDefault().getLanguage().compareTo("en") == 0 ? "cvd19_main_view;Be careful at all times" : "cvd19_main_view;¡Ten cuidado en todo momento!";
            }
            else{
                id = LAST_IMG_ID.get(LAST_IMG_ID.size()-1);
                LAST_IMG_ID.remove(LAST_IMG_ID.size()-1);
                currenIMG = LAST_IMG_NAME.get(LAST_IMG_NAME.size()-1);
                LAST_IMG_NAME.remove(LAST_IMG_NAME.size()-1);
            }
            imgVwAdviceViewer.setImageDrawable(getDrawable(id));
        }
    }

    //Database methods
    private boolean isDBEmpty() {
        boolean empty = true;

        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select id,empty from covid19_state", null);

        if(row.moveToFirst())
        {
            int id = row.getInt(0);
            int value = row.getInt(1);
            empty = row.getInt(1) == 1;
        }

        db.close();
        return empty;
    }

    private String lastUpdate() {
        String date = "";
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select dateLastUpdate from covid19_state", null);

        if(row.moveToFirst())
        {
            int value = row.getInt(0);
            date = row.getString(0);
        }

        db.close();;
        return date;
    }

    private void createDBCovid19Helper(){

        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getWritableDatabase();
        db.close();
    }

    private String[] readData(){
        String lang = getDefault().getLanguage();

        InputStream inputStream = getResources().openRawResource(
                (lang.compareTo("en") == 0 ? R.raw.englis_data :
                                                  R.raw.spanish_data));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int i = inputStream.read();
            while ( i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString().split("\n");
    }

    private Locale getDefault() {
        return Locale.getDefault();
    }

    private void updateTables(){
        String [] data = readData();

        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getWritableDatabase();

        db.beginTransaction();
        int i = 0;
        while( i < data.length){

            String [] strRow = data[i].split(";");

            if(strRow[0].compareTo("dateLastUpdate") == 0)
            {
                ++i;
                continue;
            }

            if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_symptoms") == 0){

                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");

                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    rowValues.put("classification", strRow[0]);
                    rowValues.put("symptom", strRow[1]);
                    rowValues.put("checked", strRow[2]);

                    db.insert("covid19_symptoms", null, rowValues);
                }
            }
            else if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_whattodo") == 0) {
                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");
                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    //classification, description, why, checked
                    rowValues.put("id", j);
                    rowValues.put("classification", strRow[0]);
                    rowValues.put("description", strRow[1]);
                    rowValues.put("why", strRow[2]);
                    rowValues.put("checked", strRow[3]);

                    db.insert("covid19_whattodo", null, rowValues);
                }
            }
            else if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_facts") == 0) {
                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");
                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    //classification, description, why, checked
                    rowValues.put("classification", strRow[0]);
                    rowValues.put("description", strRow[1]);

                    db.insert("covid19_facts", null, rowValues);
                }
            }
            else if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_howtostop") == 0) {
                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");
                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    //classification, description, why, checked
                    rowValues.put("id", j);
                    rowValues.put("description", strRow[0]);
                    rowValues.put("checked", strRow[1]);

                    db.insert("covid19_howtostop", null, rowValues);
                }
            }
            else if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_advice") == 0) {
                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");
                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    //classification, description, why, checked
                    rowValues.put("id", j);
                    rowValues.put("name", strRow[0]);
                    rowValues.put("description", strRow[1]);

                    db.insert("covid19_advice", null, rowValues);
                }
            }
            else if(strRow[0].compareTo("table") == 0 && strRow[1].compareTo("covid19_video") == 0) {
                int nextAmount = Integer.parseInt(strRow[3]);
                strRow = data[++i].split(";");
                for(int j = 0; j < nextAmount; ++j) {
                    strRow = data[i++].split(";");
                    ContentValues rowValues = new ContentValues();

                    //classification, description, why, checked
                    rowValues.put("id", j);
                    rowValues.put("name", strRow[0]);
                    rowValues.put("snapshot", strRow[1]);
                    rowValues.put("description", strRow[2]);
                    rowValues.put("location", strRow[3]);

                    db.insert("covid19_video", null, rowValues);
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private String[] loadSymptoms(){
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select classification, symptom, checked from covid19_symptoms", null);

        String [] rslt = null;
        if(row.moveToFirst()){
            int nRow = row.getCount();
            rslt = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getString(0) + ";" + row.getString(1) + ";" +
                        row.getInt(2);
                rslt[i] = s;
                row.moveToNext();
            }
        }
        db.close();

        return rslt;
    }

    private String[] loadWhatToDo(){
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select id, classification, description, why, checked from covid19_whattodo", null);

        String [] rslt = null;
        if(row.moveToFirst()){
            int nRow = row.getCount();
            rslt = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getInt(0) + ";" + row.getString(1) + ";" + row.getString(2) + ";" + row.getString(3) + ";" + row.getInt(4);
                rslt[i] = s;
                row.moveToNext();
            }
        }
        db.close();

        return rslt;
    }

    private String[] loadFactsVsMyths(){
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select classification, description from covid19_facts", null);

        String [] rslt = null;
        if(row.moveToFirst()){
            int nRow = row.getCount();
            rslt = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getString(0) + ";" + row.getString(1);
                rslt[i] = s;
                row.moveToNext();
            }
        }
        db.close();

        return rslt;
    }

    private String[] loadHowToStop(){
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select id, description, checked from covid19_howtostop", null);

        String [] rslt = null;
        if(row.moveToFirst()){
            int nRow = row.getCount();
            rslt = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getInt(0) + ";" + row.getString(1) + ";" + row.getInt(2);
                rslt[i] = s;
                row.moveToNext();
            }
        }
        db.close();

        return rslt;
    }

    private String[] loadVideo(){
        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select id, name, snapshot, description, location from covid19_video", null);

        String [] rslt = null;
        if(row.moveToFirst()){
            int nRow = row.getCount();
            rslt = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getInt(0) + ";" + row.getString(1) + ";" + row.getString(2)
                           + ";" + row.getString(3) + ";" + row.getString(4);
                rslt[i] = s;
                row.moveToNext();
            }
        }
        db.close();

        return rslt;
    }

    private void actionNoImpemented(View vw){
        Toast toast = Toast.makeText(this, getText(R.string.txtActionNoImplemented), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void checkAudioRecordPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.RECORD_AUDIO}, 1000);
            File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
            File ruta_sd = externalStorageVolumes[0];
            File localDir = new File(ruta_sd, "COVID19Helper");
            localDir.mkdirs();
            localDir = new File(ruta_sd + "/COVID19Helper", "MyDailyReports");
            localDir.mkdirs();
        }
    }

    private void checkCallPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1000);
        }
    }

    private void startAudioRecorder(View vw) throws IOException {
        Toast toast;

        if(audioRecorder == null){
            timeStartRecord = System.currentTimeMillis();
            String audioFileName = DATE_CURRENT + "-" + (DAILY_RECORD_COUNTER + 1) + ".mp3";
            audioOutput = getExternalFilesDir(null).getAbsolutePath()+ "/COVID19Helper/MyDailyReports/" + audioFileName;
            audioRecorder = new MediaRecorder();
            audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            audioRecorder.setOutputFile(audioOutput);
            ++DAILY_RECORD_COUNTER;
            try {
                audioRecorder.prepare();
                audioRecorder.start();
            }catch (IOException e){}

            /*
              To change the button background use de following line of code
              btn.setBackgroundResource(R....id);
             */
            toast = Toast.makeText(this, this.getString(R.string.txtRecordingHomePage),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        else if(audioRecorder != null){
            double duration = (System.currentTimeMillis() - timeStartRecord)/1000.0;
            if(duration > 3){
                String audioFileName = DATE_CURRENT + "-" + DAILY_RECORD_COUNTER  + ".mp3";
                audioRecorder.stop();
                toast = Toast.makeText(this, this.getString(R.string.txtStopRecordHomePage) + audioOutput,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                audioRecorder.release();
                audioRecorder = null;
            }
            else {
                toast = Toast.makeText(this, this.getString(R.string.txtMSGKeepPress),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                File mFile = new File(audioOutput);
                mFile.delete();
                audioRecorder.release();
                audioRecorder = null;
            }

             /*
              To change the button background use de following line of code
              btn.setBackgroundResource(R....id);
             */
        }

    }

    public void keepPressToRecord(){
        Toast toast = Toast.makeText(this, getText(R.string.txtMSGKeepPress), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RETURN_CODE_SYMPTOMS)
        {
            if(resultCode == Activity.RESULT_OK){
                String sympUpdated = data.getStringExtra("sympUpdated");

                String[] sympUpdatedList = sympUpdated.split("@");
                AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
                SQLiteDatabase db = dbAdmin.getWritableDatabase();

                for(int i = 0; i < sympUpdatedList.length; ++i){
                    String[] sympData = sympUpdatedList[i].split(";");
                    String query = "UPDATE covid19_symptoms SET checked=" + sympData[1] + " WHERE symptom=\"" + sympData[0] +"\"";
                    db.execSQL(query);
                }//*/
                db.close();
            }
        }
        else if(requestCode == RETURN_CODE_WHAT_TO_DO)
        {
            if(resultCode == Activity.RESULT_OK){
                String whatToDoUpdated = data.getStringExtra("whatToDoUpdated");

                String[] whatToDoUpdatedList = whatToDoUpdated.split("@");
                AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
                SQLiteDatabase db = dbAdmin.getWritableDatabase();

                for(int i = 0; i < whatToDoUpdatedList.length; ++i){
                    String[] whatToDoData = whatToDoUpdatedList[i].split(";");
                    String query = "UPDATE covid19_whattodo SET checked=" + whatToDoData[1] + " WHERE id=" + whatToDoData[0];
                    db.execSQL(query);
                }//*/
                db.close();
            }
        }
        else if(requestCode == RETURN_CODE_HOW_TO_STOP){
            if(resultCode == Activity.RESULT_OK){
                String howToStopUpdated = data.getStringExtra("howToStopUpdated");

                String[] howToStopUpdatedList = howToStopUpdated.split("@");
                AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
                SQLiteDatabase db = dbAdmin.getWritableDatabase();

                for(int i = 0; i < howToStopUpdatedList.length; ++i){
                    String[] howToStopDoData = howToStopUpdatedList[i].split(";");
                    String query = "UPDATE covid19_howtostop SET checked=" + howToStopDoData[1] + " WHERE id=" + howToStopDoData[0];
                    db.execSQL(query);
                }//*/
                db.close();
            }
        }
        else if(requestCode == RETURN_CODE_SETTINGS){
            SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
            SettingsAppValues.getPreferences(preferences, this, true);
            try {
                settingsUpdated();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private void computeLasNumbOfRecord(){
        String audioFilesPath = getExternalFilesDir(null).getAbsolutePath()+ "/COVID19Helper/MyDailyReports/";
        File  f = new File(audioFilesPath);
        File [] audioFiles = f.listFiles();
        int lastIndex = -1;
        for(File file : audioFiles){

            String name = file.getName();
            name = name.substring(0, name.lastIndexOf("."));
            String [] fileNameSeg = name.split("-");
            int idx = Integer.parseInt(fileNameSeg[fileNameSeg.length-1]);
            if(idx > lastIndex)
                lastIndex = idx;
        }
        DAILY_RECORD_COUNTER = lastIndex == -1 ? 0 : lastIndex;
    }

    private boolean createRequiredDirectory() throws IOException {

        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        File ruta_sd = externalStorageVolumes[0];
        File localDir = new File(ruta_sd, "COVID19Helper");//*/
        String s = localDir.getAbsolutePath();
        boolean existCOVID19Dir = false;
        boolean existDailyDir = false;
        if(!localDir.exists())
            existCOVID19Dir = localDir.mkdirs();
        else existCOVID19Dir = true;

        localDir = new File(localDir.getAbsolutePath() + "/COVID19Helper", "MyDailyReports");
        if(!localDir.exists())
            existDailyDir = localDir.mkdirs();
        else existDailyDir = true;
        return existCOVID19Dir && existDailyDir;
    }

    private void populateImagenVector(){

        AdminDBCovid19Helper dbAdmin = new AdminDBCovid19Helper(this, "dbCovid19Helper", null, 1);
        SQLiteDatabase db = dbAdmin.getReadableDatabase();

        Cursor row = db.rawQuery("select name, description from covid19_advice", null);

        if(row.moveToFirst()){
            int nRow = row.getCount();
            imgListName = new String[nRow];
            for (int i = 0; i < nRow; ++i)
            {
                String s = row.getString(0) + ";" + row.getString(1);
                imgListName[i] = s;
                row.moveToNext();
            }
        }
        db.close();
    }

    private void settingsUpdated() throws ParseException {
        //dissable all alarms
        if((SettingsAppValues.isNotificationsChanged() && !SettingsAppValues.notifications) || (!SettingsAppValues.isNotificationsChanged() && !SettingsAppValues.notifications)){
            if(!SettingsAppValues.dailyRecord.equals(getString(R.string.txtNone)))
                Utils.stopAlarm(Utils.ALARM_ID_DAILY_RECORD, MainActivity.this);
            if(SettingsAppValues.hotTeaFrequency != 0)
                Utils.stopAlarm(Utils.ALARM_ID_TEA, MainActivity.this);
            if(SettingsAppValues.gargleFrequency != 0)
                Utils.stopAlarm(Utils.ALARM_ID_GARGLE, MainActivity.this);
            return;
        }//*/

        //Setting up daily Record
        if(SettingsAppValues.isAlarmDailyRecordChanged() || (!SettingsAppValues.isAlarmDailyRecordChanged() && !SettingsAppValues.dailyRecord.equals(getString(R.string.txtNone))))
        {
            if(!SettingsAppValues.dailyRecord.equals(getString(R.string.txtNone))){
                String[] time =SettingsAppValues.dailyRecord.split(":");  //{"21","58"};

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                calendar.set(Calendar.SECOND, 0);

                Utils.setDailyAlarmOnceADay(Utils.ALARM_ID_DAILY_RECORD/*alarmID*/, calendar.getTimeInMillis(), MainActivity.this);
            }
            else
                Utils.stopAlarm(Utils.ALARM_ID_DAILY_RECORD, MainActivity.this);
        }

        //Setting up repetitive alarm for Tea
        if(SettingsAppValues.isAlarmTeaChanged() || (!SettingsAppValues.isAlarmTeaChanged() && SettingsAppValues.hotTeaFrequency != 0)){
            if(SettingsAppValues.hotTeaFrequency != 0){
                Utils.setDailyRepeatingAlarmFrequently(Utils.ALARM_ID_TEA,
                        System.currentTimeMillis() + Utils.INTERVAL_ONE_HOUR * SettingsAppValues.hotTeaFrequency,
                        Utils.INTERVAL_ONE_HOUR * SettingsAppValues.hotTeaFrequency, MainActivity.this);
            }
            else if(SettingsAppValues.hotTeaFrequency == 0){
                Utils.stopAlarm(Utils.ALARM_ID_TEA, MainActivity.this);
            }
        }

        //Setting up repetitive alarm for gargle
        if(SettingsAppValues.isAlarmGargleChanged() || (!SettingsAppValues.isAlarmGargleChanged() && SettingsAppValues.gargleFrequency != 0)){
            if(SettingsAppValues.gargleFrequency != 0){
                Utils.setDailyRepeatingAlarmFrequently(Utils.ALARM_ID_GARGLE,
                        System.currentTimeMillis() + Utils.INTERVAL_ONE_HOUR * SettingsAppValues.gargleFrequency,
                        Utils.INTERVAL_ONE_HOUR * SettingsAppValues.gargleFrequency, MainActivity.this);
            }
            else if(SettingsAppValues.gargleFrequency == 0){
                Utils.stopAlarm(Utils.ALARM_ID_GARGLE, MainActivity.this);
            }
        }

    }

    //******************-----------covid19tracker---------------*********************
    @Override
    protected void onResume() {
        super.onResume();
        refreshData(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    void setListViewCountries(ArrayList<CountryLine> allCountriesResults) {
        Collections.sort(allCountriesResults);
        listCountriesAdapter = new ListCountriesAdapter(this, allCountriesResults);
        listViewCountries.setAdapter(listCountriesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String strDeveloper = getString(R.string.txtDeveloper);
        String strShare = getString(R.string.txtShare);
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("COVID19 Helper + COVID-19 Tracker")
                        .setCancelable(true)
                        .setMessage(getString(R.string.textViewHEADSource) +
                                "\n\n" +
                                "COVID-19 Tracker's  " + strDeveloper + ": Sherif Mousa (Shatrix)" +
                                "\n" +
                                "COVID19 Helper's " + strDeveloper + ": Rolan R. Bullain" +
                                "\n" + "r2bd.solutions@gmail.com")
                        .setPositiveButton(getString(R.string.txtBtnClosInf), null)
                        .setIcon(R.drawable.ic_info)
                        .show();
                return true;
            case R.id.action_refresh:
                refreshData(true);
                return true;
            case R.id.action_share:
                sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.txtShareBody) + " " +"\n" +
                        "https://drive.google.com/drive/folders/1uRv5jq4psUGRTBiMVijh3tPckVB5M58O?usp=sharing";

                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody );
                startActivity(Intent.createChooser(sharingIntent,  strShare + " COVID19 Helper Link"));
                return true;
            case R.id.action_setting:
                goToActSettings();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public  void refreshData(boolean b) {
        if(mySwipeRefreshLayout == null)
            return;
        caseData = null;
        final boolean refresAll = b;
        final CasesValues casesTmp = new CasesValues();
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {

                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();

                    for(int n = 0; refresAll && n < 2; ++n){
                        // table id main_table_countries
                        final boolean now = n == 0;
                        countriesTable = doc.select("table").get(n);

                        countriesRows = countriesTable.select("tr");
                        //Log.e("TITLE", elementCases.text());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // get countries
                                rowIterator = countriesRows.iterator();
                                ArrayList<CountryLine> allCountriesResults = new ArrayList<CountryLine>();
                                casesTmp.getAllCountriesResults().add(allCountriesResults);
                                // read table header and find correct column number for each category
                                row = rowIterator.next();
                                cols = row.select("th");
                                //Log.e("COLS: ", cols.text());
                                if (cols.get(0).text().contains("Country")) {
                                    for(int i=1; i < cols.size(); i++){
                                        if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases"))
                                        {colNumCases = i; Log.e("Cases: ", cols.get(i).text());}
                                        else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Recovered"))
                                        {colNumRecovered = i; Log.e("Recovered: ", cols.get(i).text());}
                                        else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths"))
                                        {colNumDeaths = i; Log.e("Deaths: ", cols.get(i).text());}
                                        else if (cols.get(i).text().contains("Active") && cols.get(i).text().contains("Cases"))
                                        {colNumActive = i; Log.e("Active: ", cols.get(i).text());}
                                        else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Cases"))
                                        {colNumNewCases = i; Log.e("NewCases: ", cols.get(i).text());}
                                        else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Deaths"))
                                        {colNumNewDeaths = i; Log.e("NewDeaths: ", cols.get(i).text());}
                                    }
                                }

                                while (rowIterator.hasNext()) {
                                    row = rowIterator.next();
                                    cols = row.select("td");

                                    if (cols.get(0).text().contains("World")) {
                                        String str = cols.get(colNumCases).text();
                                        //textViewCases.setText(cols.get(colNumCases).text());
                                        casesTmp.getTotalCases().add(cols.get(colNumCases).text());

                                        //textViewRecovered.setText(cols.get(colNumRecovered).text());
                                        casesTmp.getTotalRecoveredCases().add(cols.get(colNumRecovered).text());

                                        //textViewDeaths.setText(cols.get(colNumDeaths).text());
                                        casesTmp.getTotalDeath().add(cols.get(colNumDeaths).text());

                                        if (cols.get(colNumActive).hasText()) {
                                            //textViewActive.setText(cols.get(colNumActive).text());
                                            casesTmp.getTotalActiveCases().add(cols.get(colNumActive).text());
                                        } else {
                                            //textViewActive.setText("0");
                                            casesTmp.getTotalActiveCases().add("0");
                                        }

                                        if (cols.get(colNumNewCases).hasText()) {
                                            //textViewNewCases.setText(cols.get(colNumNewCases).text());
                                            casesTmp.getTotalNewCases().add(cols.get(colNumNewCases).text());
                                        } else {
                                            //textViewNewCases.setText("0");
                                            casesTmp.getTotalNewCases().add("0");
                                        }

                                        if (cols.get(colNumNewDeaths).hasText()) {
                                            //textViewNewDeaths.setText(cols.get(colNumNewDeaths).text());
                                            casesTmp.getTotalNewDeath().add(cols.get(colNumNewDeaths).text());
                                        } else {
                                            //textViewNewDeaths.setText("0");
                                            casesTmp.getTotalNewDeath().add("0");
                                        }
                                        continue;

                                    } else if (     cols.get(0).text().contains("Total") || cols.get(0).text().contains("Europe") ||
                                                    cols.get(0).text().contains("North America") || cols.get(0).text().contains("Asia") ||
                                                    cols.get(0).text().contains("South America") || cols.get(0).text().contains("Africa") ||
                                                    cols.get(0).text().contains("Oceania")
                                    ) {
                                        continue;
                                    }

                                    if (cols.get(colNumCountry).hasText()) {tmpCountry = cols.get(0).text();}
                                    else {tmpCountry = "NA";}

                                    if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
                                    else {tmpCases = "0";}

                                    if (cols.get(colNumRecovered).hasText() ){
                                        if(cols.get(colNumRecovered).text().equals("N/A") || cols.get(colNumRecovered).text().equals("NA"))
                                            tmpRecovered = "0";
                                        else
                                            tmpRecovered = cols.get(colNumRecovered).text();
                                        tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpRecovered.replaceAll(",", ""))
                                                / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                                * 100)) + "%";
                                        tmpRecovered = tmpRecovered + "\n" + tmpPercentage;
                                    }
                                    else {tmpRecovered = "0";}

                                    if(cols.get(colNumDeaths).hasText()) {
                                        tmpDeaths = cols.get(colNumDeaths).text();
                                        tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpDeaths.replaceAll(",", ""))
                                                / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                                * 100)) + "%";
                                        tmpDeaths = tmpDeaths + "\n" + tmpPercentage;
                                    }
                                    else {tmpDeaths = "0";}

                                    if (cols.get(colNumNewCases).hasText()) {tmpNewCases = cols.get(colNumNewCases).text();}
                                    else {tmpNewCases = "0";}

                                    if (cols.get(colNumNewDeaths).hasText()) {tmpNewDeaths = cols.get(colNumNewDeaths).text();}
                                    else {tmpNewDeaths = "0";}

                                    allCountriesResults.add(new CountryLine(tmpCountry, tmpCases, tmpNewCases, tmpRecovered, tmpDeaths, tmpNewDeaths));
                                }

                                //setListViewCountries(allCountriesResults);
                                //calculate_percentages();
                                //textSearchBox.setText(null);
                                //textSearchBox.clearFocus();
                                myCalender = Calendar.getInstance();
                                String lastUpdated = getString(R.string.txtLastUpdate);
                                //textViewDate.setText(lastUpdated + myFormat.format(myCalender.getTime()));
                                casesTmp.getAllDates().add(lastUpdated + myFormat.format(myCalender.getTime()));

                                // save results
                                if(now){
                                    editor.putString(getString(R.string.txtViewCasesPrefNow), casesTmp.getTotalCases(CasesValues.WORLD_CASES_NOW));
                                    editor.putString(getString(R.string.txtViewRecoveredPrefNow), casesTmp.getTotalRecoveredCases(CasesValues.WORLD_CASES_NOW));
                                    editor.putString(getString(R.string.txtViewActivePrefNow), casesTmp.getTotalActiveCases(CasesValues.WORLD_CASES_NOW));
                                    editor.putString(getString(R.string.txtViewDeathsPrefNow), casesTmp.getTotalDeath(CasesValues.WORLD_CASES_NOW));
                                    editor.putString(getString(R.string.txtViewDatePrefNow), casesTmp.getAllDates(CasesValues.WORLD_CASES_NOW));
                                    editor.apply();
                                }else {
                                    editor.putString(getString(R.string.txtViewCasesPrefY), casesTmp.getTotalCases(CasesValues.WORLD_CASES_YESTERDAY));
                                    editor.putString(getString(R.string.txtViewRecoveredPrefY), casesTmp.getTotalRecoveredCases(CasesValues.WORLD_CASES_YESTERDAY));
                                    editor.putString(getString(R.string.txtViewActivePrefY), casesTmp.getTotalActiveCases(CasesValues.WORLD_CASES_YESTERDAY));
                                    editor.putString(getString(R.string.txtViewDeathsPrefY), casesTmp.getTotalDeath(CasesValues.WORLD_CASES_YESTERDAY));
                                    editor.putString(getString(R.string.txtViewDatePrefY), casesTmp.getAllDates(CasesValues.WORLD_CASES_YESTERDAY));
                                    editor.apply();
                                }//*/

                            }
                        });
                        casesTmp.getUrlList().add(url);
                    }///end of for n

                    //Now updating your country data
                    Country ctrySelected = CountryFactory.getCountry(currentCountrySelected);
                    ctrySelected.setCasesValues(casesTmp);
                    ctrySelected.refreshData();
                    if(casesTmp.hasYourCountryData())
                    {
                        editor.putString(getString(R.string.txtViewCasesPrefYC), casesTmp.getTotalCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                        editor.putString(getString(R.string.txtViewRecoveredPrefYC), casesTmp.getTotalRecoveredCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                        editor.putString(getString(R.string.txtViewActivePrefYC), casesTmp.getTotalActiveCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                        editor.putString(getString(R.string.txtViewDeathsPrefYC), casesTmp.getTotalDeath(CasesValues.YOUR_COUNTRY_CASES_NOW));
                        editor.putString(getString(R.string.txtViewDatePrefYC), casesTmp.getAllDates(CasesValues.YOUR_COUNTRY_CASES_NOW));
                        editor.apply();
                    }

                    casesTmp.setEmpty(false);
                    caseData = casesTmp;
                    casesViewModel.setCasesData(caseData);//*/
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String msgText = getString(R.string.txtNetworkError);
                            Toast t = Toast.makeText(MainActivity.this, msgText,
                                    Toast.LENGTH_LONG);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                        }
                    });
                }
                finally {
                    doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                    }});
            }
        }).start();
    }

    @Override
    public void onRefreshCasesDataNow() {
        mySwipeRefreshLayout = fNow.getFragmentSwipeRefreshNow();
        refreshData(true);
    }

    @Override
    public void onRefreshCasesDataYesterday() {
        mySwipeRefreshLayout = fYesterday.getFragmentSwipeRefreshY();
        refreshData(true);
    }

    @Override
    public void onRefreshCasesDataOfYourCountry() {
        mySwipeRefreshLayout = fYourCountry.getFragmentSwipeRefreshYourCountry();
        refreshData(true);
    }

    @Override
    public void onClickCountryListViewItem(String itemClicked){

        final String ctryName = itemClicked;

        fYourCountry.setUpdated(false);
        countryProgressBar = fNow.getCountryProgressBar();
        countryProgressBar.setVisibility(View.VISIBLE);
        mySwipeRefreshLayout = fYourCountry.getFragmentSwipeRefreshYourCountry();

        countryProgressBar.setVisibility(View.GONE);
        final Country ctrySelected = CountryFactory.getCountry(ctryName);
        if(ctrySelected == null)
            return;
        currentCountrySelected = ctryName;
        //refreshData(false);
        //*****************************************************

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mySwipeRefreshLayout.setRefreshing(true);
        final CasesValues casesTmp = new CasesValues();
        CasesValues.copyCaseValues(caseData, casesTmp);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(ctrySelected == null){
                                    return;
                                }
                                currentCountrySelected = ctryName;

                                ctrySelected.setCasesValues(casesTmp);
                                ctrySelected.refreshData();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(caseData.hasYourCountryData())
                            {
                                editor.putString(getString(R.string.txtViewCasesPrefYC), caseData.getTotalCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                                editor.putString(getString(R.string.txtViewRecoveredPrefYC), caseData.getTotalRecoveredCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                                editor.putString(getString(R.string.txtViewActivePrefYC), caseData.getTotalActiveCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
                                editor.putString(getString(R.string.txtViewDeathsPrefYC), caseData.getTotalDeath(CasesValues.YOUR_COUNTRY_CASES_NOW));
                                editor.putString(getString(R.string.txtViewDatePrefYC), caseData.getAllDates(CasesValues.YOUR_COUNTRY_CASES_NOW));
                                editor.putString(getString(R.string.txtFragmentSelectedTitle), currentCountrySelected);
                                editor.apply();
                            }
                        }
                    });
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network Connection Error!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                finally {
                    //doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                        countryProgressBar.setVisibility(View.GONE);
                    }});
            }
        }).start();
        //*****************************************************

        casesTmp.setEmpty(false);
        caseData = casesTmp;
        casesViewModel.setCasesData(caseData);

        vwPager.setCurrentItem(2);

    }
}
