package com.r2bd.covid19Helper;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.r2bd.covid19Helper.Alarms.Utils;

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


public class MainActivity extends AppCompatActivity {

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

    //***********----------Counter----------------******************
    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle;
    EditText textSearchBox;
    Handler handler;
    String url = "https://www.worldometers.info/coronavirus/";
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, germanResults, tmpNewCases, tmpNewDeaths;
    Document doc, germanDoc;
    Element countriesTable, row, germanTable;
    Elements countriesRows, cols, germanRows;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    double tmpNumber;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    ArrayList<CountryLine> allCountriesResults, FilteredArrList;
    Intent sharingIntent;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths, colNumActive, colNumNewCases, colNumNewDeaths;
    SwipeRefreshLayout mySwipeRefreshLayout;
    InputMethodManager inputMethodManager;
    Iterator<Element> rowIterator;
    ProgressBar countryProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

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
        createRequiredDirectory();
        computeLasNumbOfRecord();
        imgBtnRecord = findViewById(R.id.imgBtnRecord);
        imgBtnRecord.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View vw, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            startAudioRecorder(vw);
                        } catch (IOException e) {
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // touch move code
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            startAudioRecorder(vw);
                        } catch (IOException e) {
                        }
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
            rowValues.put("dateLastUpdate", "2020-03-28");
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

        //**************--------Counter-----------------***********************
        // All initial definitions
        textViewCases = findViewById(R.id.textViewCases);
        textViewRecovered = findViewById(R.id.textViewRecovered);
        textViewDeaths = findViewById(R.id.textViewDeaths);
        textViewDate = findViewById(R.id.textViewDate);
        textViewRecoveredTitle = findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = findViewById(R.id.textViewDeathsTitle);
        textViewActiveTitle = findViewById(R.id.textViewActiveTitle);
        textViewActive = findViewById(R.id.textViewActive);
        textViewNewDeaths = findViewById(R.id.textViewNewDeaths);
        textViewNewCases = findViewById(R.id.textViewNewCases);
        textViewNewCasesTitle = findViewById(R.id.textViewNewCasesTitle);
        textViewNewDeathsTitle = findViewById(R.id.textViewNewDeathsTitle);
        listViewCountries = findViewById(R.id.listViewCountries);
        textSearchBox = findViewById(R.id.textSearchBox);
        textSearchBox.setVisibility(View.INVISIBLE);
        countryProgressBar = findViewById(R.id.countryProgressBar);
        colNumCountry = 0;
        colNumCases = 1;
        colNumRecovered = 0;
        colNumDeaths = 0;
        colNumNewCases = 0;
        colNumNewDeaths = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        handler = new Handler();
        generalDecimalFormat = new DecimalFormat("0.00", symbols);
        allCountriesResults = new ArrayList<CountryLine>();

        // Implement Swipe to Refresh
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.coronaMainSwipeRefresh);
        mySwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                }
        );

        // fix interference between scrolling in listView & parent SwipeRefreshLayout
        listViewCountries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) mySwipeRefreshLayout.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mySwipeRefreshLayout.setEnabled(true);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }

            private boolean listIsAtTop() {
                if (listViewCountries.getChildCount() == 0) return true;
                return listViewCountries.getChildAt(0).getTop() == 0;
            }
        });

        listViewCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("CLICKED", allCountriesResults.get(position).getCountryName());
                if (allCountriesResults.get(position).getCountryName().contains("Germany")) {
                    countryProgressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                germanDoc = null; // Fetches the HTML document
                                germanResults = "";
                                germanDoc = Jsoup.connect("https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html").timeout(10000).get();
                                germanTable = germanDoc.select("table").get(0);
                                germanRows = germanTable.select("tbody").select("tr");
                                rowIterator = germanRows.iterator();
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        while (rowIterator.hasNext()) {
                                            row = rowIterator.next();
                                            cols = row.select("td");
                                            if (cols.get(0).text().contains("Gesamt")) {
                                                break;
                                            }
                                            germanResults = germanResults + cols.get(0).text() + " : " + cols.get(1).text().split("\\s")[0] + "\n";
                                            //Log.e("TABLE: ", cols.get(0).text() + " : " + cols.get(1).text().split("\\s")[0]);
                                        }
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Confirmed Cases in Germany")
                                                .setCancelable(true)
                                                .setMessage("Robert Koch Institut www.rki.de\n\n" +
                                                        germanResults)
                                                .setPositiveButton("Close", null)
                                                .setIcon(R.drawable.ic_info)
                                                .show();
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Network Connection Error!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } finally {
                                doc = null;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    countryProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        // fetch previously saved data in SharedPreferences, if any
        if (preferences.getString("textViewCases", null) != null) {
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));
            textViewActive.setText(preferences.getString("textViewActive", null));
            //calculate_percentages();
        }

        // Add Text Change Listener to textSearchBox to filter by Country
        textSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence searchSequence, int start, int before, int count) {
                FilteredArrList = new ArrayList<CountryLine>();
                if (searchSequence == null || searchSequence.length() == 0) {
                    // back to original
                    setListViewCountries(allCountriesResults);
                } else {
                    searchSequence = searchSequence.toString().toLowerCase();
                    for (int i = 0; i < allCountriesResults.size(); i++) {
                        String data = allCountriesResults.get(i).countryName;
                        if (data.toLowerCase().startsWith(searchSequence.toString())) {
                            FilteredArrList.add(new CountryLine(
                                    allCountriesResults.get(i).countryName,
                                    allCountriesResults.get(i).cases,
                                    allCountriesResults.get(i).newCases,
                                    allCountriesResults.get(i).recovered,
                                    allCountriesResults.get(i).deaths,
                                    allCountriesResults.get(i).newDeaths));
                        }
                    }
                    // set the Filtered result to return
                    setListViewCountries(FilteredArrList);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Hide keyboard after hitting done button
        textSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    textSearchBox.clearFocus();
                    return true;
                }
                return false;
            }
        });

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String filtered = "";
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isWhitespace(character)) {
                        filtered += character;
                    }
                }

                return filtered;
            }

        };

        textSearchBox.setFilters(new InputFilter[]{filter});
        textSearchBox.clearFocus();
        // Call refreshData once the app is opened only one time, then user can request updates
        refreshData();
    }

    public void goToActSettings(View vw) {
        //actionNoImpemented(vw);
        Intent intSettings = new Intent(this, Settings.class);
        startActivityForResult(intSettings, RETURN_CODE_SETTINGS);
    }

    public void goToActCovid19Symptom(View vw) {
        Intent intCovid19Symptom = new Intent(this, Covid19Symptoms.class);
        String[] strArrSymptoms = loadSymptoms();
        intCovid19Symptom.putExtra("symptomsList", strArrSymptoms);
        startActivityForResult(intCovid19Symptom, RETURN_CODE_SYMPTOMS);
        //startActivity(intCovid19Symptom);
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
        //startActivity(intHowToStop);
        //*/
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
        //startActivity(intWhatToDo);
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

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
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
            File ruta_sd = Environment.getExternalStorageDirectory();
            File localDir = new File(ruta_sd, "COVID19Helper");
            boolean rslt = localDir.mkdirs();
            localDir = new File(ruta_sd + "/COVID19Helper", "MyDailyReports");
            rslt = localDir.mkdirs();
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
            audioOutput = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/COVID19Helper/MyDailyReports/" + audioFileName;
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
        String audioFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/COVID19Helper/MyDailyReports/";
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

    private boolean createRequiredDirectory(){
        File ruta_sd = Environment.getExternalStorageDirectory();
        File localDir = new File(ruta_sd, "COVID19Helper");
        boolean existCOVID19Dir = false;
        boolean existDailyDir = false;
        if(!localDir.exists())
            existCOVID19Dir = localDir.mkdirs();
        else existCOVID19Dir = true;
        localDir = new File(ruta_sd + "/COVID19Helper", "MyDailyReports");
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

    //******************-----------Counter---------------*********************
    @Override
    protected void onResume() {
        super.onResume();
        textSearchBox.clearFocus();
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
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("COVID19 Helper + COVID-19 Tracker")
                        .setCancelable(true)
                        .setMessage(getString(R.string.textViewHEADSource) +
                                "\n\n" +
                                "COVID-19 Tracker's  " + R.string.txtDeveloper + ": Sherif Mousa (Shatrix)" +
                                "\n" +
                                "COVID19 Helper's " + R.string.txtDeveloper + ": Rolan R. Bullain")
                        .setPositiveButton(getString(R.string.txtBtnClosInf), null)
                        .setIcon(R.drawable.ic_info)
                        .show();
                return true;
            case R.id.action_refresh:
                refreshData();
                return true;
            case R.id.action_share:
                sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.txtShareBody) + " " +"\n" +
                        "https://drive.google.com/drive/folders/16UYlBvUQ-Aln-IjJpE55e1VlM5EWO-2X?usp=sharing";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody );
                startActivity(Intent.createChooser(sharingIntent, R.string.txtShare + " COVID19 Helper Link"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    void calculate_percentages () {
        tmpNumber = Double.parseDouble(textViewRecovered.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewRecoveredTitle.setText(getResources().getString(R.string.textViewRecoveredTitle)  + "  " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewDeaths.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewDeathsTitle.setText(getResources().getString(R.string.textViewDeathsTitle) + "  " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewActive.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewActiveTitle.setText(getResources().getString(R.string.textViewActive) + "  " + generalDecimalFormat.format(tmpNumber) + "%");
    }

    void refreshData() {
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    // table id main_table_countries
                    countriesTable = doc.select("table").get(0);
                    countriesRows = countriesTable.select("tr");
                    //Log.e("TITLE", elementCases.text());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            rowIterator = countriesRows.iterator();
                            allCountriesResults = new ArrayList<CountryLine>();

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

                                if (cols.get(0).text().contains("Total")) {
                                    textViewCases.setText(cols.get(colNumCases).text());
                                    textViewRecovered.setText(cols.get(colNumRecovered).text());
                                    textViewDeaths.setText(cols.get(colNumDeaths).text());

                                    if (cols.get(colNumActive).hasText()) {textViewActive.setText(cols.get(colNumActive).text());}
                                    else {textViewActive.setText("0");}
                                    if (cols.get(colNumNewCases).hasText()) {textViewNewCases.setText(cols.get(colNumNewCases).text());}
                                    else {textViewNewCases.setText("0");}
                                    if (cols.get(colNumNewDeaths).hasText()) {textViewNewDeaths.setText(cols.get(colNumNewDeaths).text());}
                                    else {textViewNewDeaths.setText("0");}
                                    break;
                                }

                                if (cols.get(colNumCountry).hasText()) {tmpCountry = cols.get(0).text();}
                                else {tmpCountry = "NA";}

                                if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
                                else {tmpCases = "0";}

                                if (cols.get(colNumRecovered).hasText()){
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

                            setListViewCountries(allCountriesResults);
                            textSearchBox.setText(null);
                            textSearchBox.clearFocus();

                            // save results
                            editor.putString("textViewCases", textViewCases.getText().toString());
                            editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                            editor.putString("textViewActive", textViewActive.getText().toString());
                            editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                            editor.putString("textViewDate", textViewDate.getText().toString());
                            editor.apply();

                            calculate_percentages();

                            myCalender = Calendar.getInstance();
                            textViewDate.setText("Last updated: " + myFormat.format(myCalender.getTime()));
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

    private void settingsUpdated() throws ParseException {
        //Setting up daily Record
        if(SettingsAppValues.isAlarmDailyRecordChanged())
        {
            if(!SettingsAppValues.dailyRecord.equals(R.string.txtNone)){
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
        if(SettingsAppValues.isAlarmTeaChanged()){
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
        if(SettingsAppValues.isAlarmGargleChanged()){
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
}
