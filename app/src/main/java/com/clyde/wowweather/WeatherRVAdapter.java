package com.clyde.wowweather;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.awareness.state.Weather;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<WeatherRVModal> weatherRVModalArray;
//    private final String celcius = "\u2103";
//    private final String speedUnit = "Km/h";
    private final String TAG = WeatherRVAdapter.class.getSimpleName();


    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModal> weatherArraylist) {
        this.mContext = context;
        weatherRVModalArray = weatherArraylist;
    }

    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.weather_rv_item, parent ,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {

        WeatherRVModal modal = weatherRVModalArray.get(position);
        holder.tempTV.setText(modal.getTemperature().concat(mContext.getString(R.string.celcius)));
        Picasso.get().load("http:".concat(modal.getIcon())).into(holder.conditionIV);
        holder.windTV.setText(modal.getWindspeed().concat(mContext.getString(R.string.speed_unit)));
        SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat dateFormatout = new SimpleDateFormat("hh:mm aa");

        try {
            Date date = dateFormatIn.parse(modal.getTime());
            holder.timeTV.setText(dateFormatout.format(date));

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public int getItemCount() {
        return weatherRVModalArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView windTV, tempTV, timeTV;
        private ImageView conditionIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTV = itemView.findViewById(R.id.idTVWindSpeed);
            tempTV = itemView.findViewById(R.id.idTVTemperature);
            timeTV = itemView.findViewById(R.id.idTVTime);
            conditionIV = itemView.findViewById(R.id.idIVCondition);
        }
    }
}
