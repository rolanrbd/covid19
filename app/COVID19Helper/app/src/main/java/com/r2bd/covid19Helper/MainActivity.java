package com.r2bd.covid19Helper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_PHONE_STATE = 1 ;
    private static final int RETURN_CODE_SYMPTOMS = 1001 ;
    private static final int RETURN_CODE_WHAT_TO_DO = 1002 ;
    private static final int RETURN_CODE_HOW_TO_STOP = 1003 ;
    private static String    DATE_CURRENT = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private static int       DAILY_RECORD_COUNTER = 0;
    private long timeStartRecord = 0;
    private  static String EMERGENCY_NUMBER = "";
    private  static String FOOD_BANK_NUMBER = "";

    private ImageView imVwMap;
    private ImageView imgVwChart;
    private ImageView imgBtnFoodBank;


    private MediaRecorder audioRecorder = null;
    private String audioOutput = null;
    private ImageButton imgBtnRecord;
    private boolean permissionToRecordGranted = false;

    private ImageButton imgBtnSymptoms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        //Setting the phone numbers
        Button btnNotifyCOVID19 = findViewById(R.id.btnNotifyCOVID19);
        btnNotifyCOVID19.setText(btnNotifyCOVID19.getText().toString() + " COVID19+");

        //Check emergency phone number
        Button btnEmergency = (Button) findViewById(R.id.btnEmergency);
        if(EMERGENCY_NUMBER.length() != 0)
            btnEmergency.setText(btnEmergency.getText().toString() + " " + EMERGENCY_NUMBER);

        //Check food bank numbers
        imgBtnFoodBank = findViewById(R.id.imgBtnFoodBank);
        imgBtnFoodBank.setOnClickListener(new View.OnClickListener() { public void onClick(View vw) {
            callFoodBank(vw);
        } });

        checkAudioRecordPermission();
        createRequiredDirectory();
        computeLasNumbOfRecord();
        imgBtnRecord = findViewById(R.id.imgBtnRecord);
        imgBtnRecord.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View vw, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            try {
                                startAudioRecorder(vw);
                            }catch (IOException e){}
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // touch move code
                            break;
                        case MotionEvent.ACTION_UP:
                            try {
                                startAudioRecorder(vw);
                            }catch (IOException e){}
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

            int rowsDeleted = db.delete("covid19_state","id=0",null);

            ContentValues rowValues = new ContentValues();
            //empty, dateLastUpdate
            rowValues.put("dateLastUpdate", "2020-03-28");
            rowValues.put("empty", 0);
            db.insert("covid19_state", null, rowValues);

            db.close();
        }

        //Setting an onClickListener to map imagen in the home page
        imVwMap = findViewById(R.id.imVwMap);
        imVwMap.setOnClickListener(new View.OnClickListener() { public void onClick(View vw) {
            actionNoImpemented(vw);
        } });

        //Setting an onClickListener to chart imagen in the home page
        imgVwChart = findViewById(R.id.imgVwChart);
        imgVwChart.setOnClickListener(new View.OnClickListener() { public void onClick(View vw) {
            actionNoImpemented(vw);
        } });

    }

    public void goToActSettings(View vw) {
        actionNoImpemented(vw);
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
        actionNoImpemented(vw);
        /*
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

        Intent intMedia = new Intent(this, AudioHistory.class);
        startActivity(intMedia);
        //*/
    }

    public void goToActShare(View vw) {
        actionNoImpemented(vw);
        /*
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND); //# change the type of data you need to share, # for image use "image/*"
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, URL_TO_SHARE);
        startActivity(Intent.createChooser(intent, "Share"));
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
        actionNoImpemented(vw);
        /*
        Intent intNews = new Intent(this, News.class);
        startActivity(intNews);
         */
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

    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            } else {
                requestPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
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
        actionNoImpemented(vw);

        //TOFIX
        /*
        Intent i = new Intent(Intent.ACTION_CALL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            showPhoneStatePermission();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //return;
        }
        i.setData(Uri.parse("tel:+5353282915"));
        startActivity(i);
        */
    }

    public void callFoodBank(View vw) {

        if(FOOD_BANK_NUMBER.length() == 0) {
            Toast toast = Toast.makeText(this, this.getString(R.string.txtActionCallFoodBank), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void notifyCovid19Positive(View vw) {
        actionNoImpemented(vw);
    }

    public void showNextAdvice(View vw) {
        actionNoImpemented(vw);
    }

    public void showPreviousAdvice(View vw) {
        actionNoImpemented(vw);
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

    public void playAudio(View vw) throws IOException {
        MediaPlayer playAudio = new MediaPlayer();
        try {
            playAudio.setDataSource(audioOutput);
            playAudio.prepare();
        }catch (IOException e){}

        playAudio.start();
        Toast.makeText(this, this.getString(R.string.txtPlayAudioHomePage),Toast.LENGTH_SHORT).show();
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
}
