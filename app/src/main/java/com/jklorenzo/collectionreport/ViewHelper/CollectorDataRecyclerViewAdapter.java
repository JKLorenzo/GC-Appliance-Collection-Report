package com.jklorenzo.collectionreport.ViewHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.jklorenzo.collectionreport.Objects.CollectorData;
import com.jklorenzo.collectionreport.R;

import java.util.ArrayList;

public class CollectorDataRecyclerViewAdapter extends RecyclerView.Adapter<CollectorDataRecyclerViewAdapter.MyViewHolder>{
    private ArrayList<CollectorData> collectorData;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public CollectorDataRecyclerViewAdapter(Context context, ArrayList<CollectorData> collectorData, OnItemClickListener listener){
        this.context = context;
        this.collectorData = collectorData;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CollectorDataRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_row_collectordata, parent, false);
        return new CollectorDataRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CollectorDataRecyclerViewAdapter.MyViewHolder holder, final int position) {
        holder.textViewCaption.setText(collectorData.get(position).getCaption());
        holder.textViewAmount.setText(collectorData.get(position).getAmount());
    }

    @Override
    public int getItemCount() {
        return collectorData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textViewCaption, textViewAmount;
        ConstraintLayout mainLayout;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            textViewCaption = itemView.findViewById(R.id.textViewCaption);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            mainLayout = itemView.findViewById(R.id.mainLayout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }
}
