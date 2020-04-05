package com.r2bd.covid19Helper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

public class Media extends AppCompatActivity {

    private LinearLayout lnLyLeft;
    private LinearLayout lnLyRight;
    private VideoView vdVwr;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        lnLyLeft = findViewById(R.id.lnLyLeft);
        lnLyRight = findViewById(R.id.lnLyRight);
        vdVwr = findViewById(R.id.vdVwer);
        createVideoViewers();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVideoViewers(){
        final String path = "android.resource://" + getPackageName() + "/" + R.raw.test;
        for(int i = 0; i < 4; ++i){
            final ImageView imgVwBackground = new ImageView(getApplicationContext());
            imgVwBackground.setBackground(getDrawable(R.drawable.cvd19_main_view));

            if(i % 2 == 0)
            {
                lnLyLeft.addView(imgVwBackground);
            }
            else {
                lnLyRight.addView(imgVwBackground);
            }
            imgVwBackground.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onClick(View v) {
                    vdVwr.setMediaController((new MediaController(getApplicationContext())));
                    vdVwr.setVideoURI(Uri.parse(path));
                    vdVwr.requestFocus();
                    vdVwr.start();
                }
            });
        }
    }
}
