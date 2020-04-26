package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.r2bd.covid19Helper.Adapters.VideoListAdapter;
import com.r2bd.covid19Helper.Models.VideoModel;

import java.util.ArrayList;
import java.util.List;

public class Media extends AppCompatActivity {

    private LinearLayout lnLyMediaControler;
    private VideoView vdVwr;
    private String [] strVideoData = null;
    private String path = null;

    private ListView lstVwIMGs;
    private List<VideoModel> videoList = new ArrayList<>();
    private VideoListAdapter videoListAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        vdVwr = findViewById(R.id.vdVwer);
        strVideoData = getIntent().getStringArrayExtra("videoList");
        createVideoViewers();
        lnLyMediaControler = findViewById(R.id.lnLyMediaControler);

        lstVwIMGs = findViewById(R.id.lstVwIMGs);
        lstVwIMGs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                path = "android.resource://" + getPackageName() + "/" + videoList.get(position).getVideoName();
                MediaController mController = new MediaController(Media.this);
                vdVwr.setMediaController(mController);
                vdVwr.setVideoURI(Uri.parse(path));
                vdVwr.requestFocus();
                vdVwr.start();
                mController.setAnchorView(vdVwr);
            }
        });

        videoListAdapter = new VideoListAdapter(this,R.layout.item_row_video, videoList);
        lstVwIMGs.setAdapter(videoListAdapter);

    }

    private void createVideoViewers(){

        for(int i = 0; i < strVideoData.length; ++i){
            String[] currVideo = strVideoData[i].split(";");

            int idVideoName = getResources().getIdentifier(currVideo[1], "raw",getPackageName());
            int idSnapshot = getResources().getIdentifier(currVideo[2], "drawable",getPackageName());
            String description = currVideo[3];
            String location = currVideo[4];
            videoList.add(new VideoModel(idVideoName, idSnapshot, description, location));
        }
    }
}
