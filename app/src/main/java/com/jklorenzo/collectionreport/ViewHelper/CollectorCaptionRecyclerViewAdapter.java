package com.jklorenzo.collectionreport.ViewHelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.jklorenzo.collectionreport.MainActivity;
import com.jklorenzo.collectionreport.Objects.CollectorCaption;
import com.jklorenzo.collectionreport.R;

import java.util.ArrayList;

public class CollectorCaptionRecyclerViewAdapter extends RecyclerView.Adapter<CollectorCaptionRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<CollectorCaption> collectorCaptions;
    private Context context;

    public CollectorCaptionRecyclerViewAdapter(Context context, ArrayList<CollectorCaption> collectorCaptions){
        this.context = context;
        this.collectorCaptions = collectorCaptions;
    }

    @NonNull
    @Override
    public CollectorCaptionRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_row_collectorcaption, parent, false);
        return new CollectorCaptionRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectorCaptionRecyclerViewAdapter.MyViewHolder holder, final int position) {
        holder.imageViewImage.setImageResource(collectorCaptions.get(position).getImage());
        holder.textViewCollectorName.setText(collectorCaptions.get(position).getName());
        String rank = collectorCaptions.get(position).getRanking();
        if (rank.contains("1"))
            holder.ImageViewRank.setImageResource(R.drawable.rank1);
        else if (rank.contains("2"))
            holder.ImageViewRank.setImageResource(R.drawable.rank2);
        else if (rank.contains("3"))
            holder.ImageViewRank.setImageResource(R.drawable.rank3);
        else
            holder.ImageViewRank.setImageResource(android.R.color.transparent);
        holder.textViewCaption1.setText(collectorCaptions.get(position).getCaption1());
        holder.textViewCaption2.setText(collectorCaptions.get(position).getCaption2());
        holder.textViewAmount1.setText(collectorCaptions.get(position).getAmount1());
        holder.textViewAmount2.setText(collectorCaptions.get(position).getAmount2());

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MainActivity ma = (MainActivity)context;

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);

                @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.alertdialog_buttons_monthlydaily, null);
                Button buttonMonthly = view.findViewById(R.id.buttonMonthly);
                Button buttonDaily = view.findViewById(R.id.buttonDaily);
                builder.setView(view);

                final AlertDialog dialog = builder.create();

                if (position == 0) {
                    buttonMonthly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ma.selectCollector(position, 0);
                            dialog.cancel();
                        }
                    });
                    buttonDaily.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ma.selectCollector(position, 1);
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                } else if (position == 1){
                    buttonMonthly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ma.selectCollector(position, 1);
                            dialog.cancel();
                        }
                    });
                    buttonDaily.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ma.selectCollector(position, 2);
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                } else{
                    ma.selectCollector(position, 2);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return collectorCaptions.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewImage, ImageViewRank;
        TextView textViewCollectorName, textViewCaption1, textViewCaption2, textViewAmount1, textViewAmount2;
        ConstraintLayout mainLayout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
            textViewCollectorName = itemView.findViewById(R.id.textViewCollectorName);
            ImageViewRank = itemView.findViewById(R.id.ImageViewRank);
            textViewCaption1 = itemView.findViewById(R.id.textViewCaption1);
            textViewCaption2 = itemView.findViewById(R.id.textViewCaption2);
            textViewAmount1 = itemView.findViewById(R.id.textViewAmount1);
            textViewAmount2 = itemView.findViewById(R.id.textViewAmount2);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
