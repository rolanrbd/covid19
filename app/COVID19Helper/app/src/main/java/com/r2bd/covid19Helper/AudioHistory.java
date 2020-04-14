package com.r2bd.covid19Helper;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioHistory extends AppCompatActivity {

    private  File[] audioFiles = null;

    private LinearLayout lnLyVFacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_history);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        lnLyVFacts = findViewById(R.id.lnLyVFacts);
        try {
            removeOldFiles();
        } catch (ParseException e) {}

        searchAudioFiles();
        createView();
    }

    private void searchAudioFiles(){
        File pathToMySongs = new File(getExternalFilesDir(null).getAbsolutePath() + "/COVID19Helper/MyDailyReports/");
        if(pathToMySongs.exists())
        {
            audioFiles = pathToMySongs.listFiles();
        }
    }

    private void createView(){
        for(File file : audioFiles){
            String name = file.getName();
            final ImageButton imgBtnPlay = new ImageButton(getApplicationContext(),null,R.attr.buttonBarButtonStyle);
            imgBtnPlay.setImageResource(R.mipmap.cvd19_play_audio);
            imgBtnPlay.setPadding(40,10,10,10);
            final ImageButton imtBtnRemove = new ImageButton(getApplicationContext(),null,R.attr.buttonBarButtonStyle);
            imtBtnRemove.setImageResource(R.mipmap.cvd19_remove_audio);
            imtBtnRemove.setPadding(15,10,5,10);
            final TextView txtVwFileName = new TextView(getApplicationContext());
            txtVwFileName.setText(name);
            txtVwFileName.setGravity(Gravity.CENTER);
            txtVwFileName.setPadding(15,10,5,10);
            txtVwFileName.setTextSize(16);
            final LinearLayout lnLyH = new LinearLayout(getApplicationContext());
            lnLyH.setOrientation(LinearLayout.HORIZONTAL);
            lnLyH.addView(imgBtnPlay);
            lnLyH.addView(imtBtnRemove);
            lnLyH.addView(txtVwFileName);
            lnLyVFacts.addView(lnLyH);

            imgBtnPlay.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View vw) {
                    try {
                        playAudio(vw, txtVwFileName.getText().toString());
                    }catch (IOException e){}

                }
            });

            imtBtnRemove.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View vw) {
                    removeAudio(vw, txtVwFileName.getText().toString(), lnLyH);
                }
            });
        }
    }

    public void playAudio(View vw, String audioFile) throws IOException {
        MediaPlayer playAudio = new MediaPlayer();
        String filePath = getExternalFilesDir(null).getAbsolutePath() + "/COVID19Helper/MyDailyReports/";
        filePath += audioFile;
        try {
            playAudio.setDataSource(filePath);
            playAudio.prepare();
        }catch (IOException e){}

        playAudio.start();
        Toast.makeText(this, getString(R.string.txtPlayAudioHomePage),Toast.LENGTH_SHORT).show();
    }

    public void removeAudio(View vw, String audioFile, LinearLayout ly){
        String filePath = getExternalFilesDir(null).getAbsolutePath() + "/COVID19Helper/MyDailyReports/";
        filePath += audioFile;

        File mFile = new File(filePath);
        mFile.delete();
        lnLyVFacts.removeView(ly);
    }

    private void removeOldFiles() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date actualDate = new Date();

        String audioFilesPath = getExternalFilesDir(null).getAbsolutePath()+ "/COVID19Helper/MyDailyReports/";
        File  f = new File(audioFilesPath);
        File [] audioFiles = f.listFiles();

        for(File file : audioFiles){

            String fileDate = file.getName();
            fileDate = fileDate.substring(0, fileDate.lastIndexOf("-"));
            Date oldDate = dateFormat.parse(fileDate);

            int days = (int) Math.abs(actualDate.getTime() - oldDate.getTime())/86400000;
            if(days > 15){
                file.delete();
            }

        }

    }
}
