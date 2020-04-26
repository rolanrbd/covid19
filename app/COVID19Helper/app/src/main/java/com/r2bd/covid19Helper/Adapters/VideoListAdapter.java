package com.r2bd.covid19Helper.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.r2bd.covid19Helper.R;
import com.r2bd.covid19Helper.Models.VideoModel;

import java.util.List;

public class VideoListAdapter extends ArrayAdapter<VideoModel>{

    private List <VideoModel> videoList;
    private Context lContext;
    private int rescLayout;

    public VideoListAdapter(@NonNull Context context, int resource, @NonNull List <VideoModel> objects) {
        super(context, resource, objects);
        this.videoList = objects;
        this.lContext = context;
        this.rescLayout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View vw = convertView;
        if(vw == null)
            vw = LayoutInflater.from(lContext).inflate(rescLayout, null);

        LinearLayout lnLy = vw.findViewById(R.id.lnLyBackground);

        int colorId = position%2 == 0 ? lContext.getResources().getColor(R.color.colorFactVsMythDef) :
                lContext.getResources().getColor(R.color.colorGreenLight);
        lnLy.setBackgroundColor( colorId);
        VideoModel model = videoList.get(position);

        ImageView img = vw.findViewById(R.id.imageView);
        img.setImageResource(model.getSnapshot());

        TextView txtDesciption = vw.findViewById(R.id.txtDescription);
        txtDesciption.setText(model.getDescription());

        TextView txtLocation = vw.findViewById(R.id.txtLocation);
        txtLocation.setText(R.string.txtLocation);

        ImageView imgIco = vw.findViewById(R.id.imgLocationIco);
        boolean local = model.getLocation().compareTo("local") == 0 ? true : false;
        imgIco.setImageResource(local ? R.drawable.cvd19_phone : R.drawable.cvd19_internet);

        return vw;
    }
}
