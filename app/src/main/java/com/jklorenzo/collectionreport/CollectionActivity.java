package com.jklorenzo.collectionreport;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jklorenzo.collectionreport.Database.DatabaseHelper;
import com.jklorenzo.collectionreport.Objects.CollectorData;
import com.jklorenzo.collectionreport.ViewHelper.CollectorDataRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class CollectionActivity extends AppCompatActivity {
    TextView tvCollectorName, tvCurrentDate;
    RecyclerView rv;

    String CurrentDate, MonthYear, Table_Name, CollectorName;
    int CollectorIndex;
    ArrayList<CollectorData> collectorDatas;
    Cursor res;

    DatabaseHelper db;
    public CollectorDataRecyclerViewAdapter CollectorDataRVA;

    boolean databaseChanged = false;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        tvCollectorName = findViewById(R.id.textViewCollectorName);
        tvCurrentDate = findViewById(R.id.textViewCurrentDate);
        rv = findViewById(R.id.recyclerView);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        CurrentDate = String.format(String.valueOf(calendar.get(Calendar.MONTH)+1), "00")
                + "/" + String.format(String.valueOf(calendar.get(Calendar.DATE)), "00")
                + "/" + calendar.get(Calendar.YEAR);
        MonthYear = getIntent().getStringExtra("MonthYear");
        assert MonthYear != null;
        Table_Name = MonthYear.replace(" ", "");
        CollectorIndex = getIntent().getIntExtra("CollectorIndex", 0);
        CollectorName = DatabaseHelper.NAMES[CollectorIndex];

        db = new DatabaseHelper(super.getBaseContext(), Table_Name);
        res = db.getCollectorData(CollectorName);
        res.moveToNext();

        tvCollectorName.setText(res.getString(0));
        tvCurrentDate.setText(CurrentDate);

        collectorDatas = new ArrayList<>();
        for (int i = 1; i <= 31; i++){
            collectorDatas.add(new CollectorData(String.valueOf(i), String.format("%.2f", res.getDouble(i))));
        }
        collectorDatas.add(new CollectorData("+", String.format("%.2f", res.getDouble(32))));
        collectorDatas.add(new CollectorData("-", String.format("%.2f", res.getDouble(33))));

        CollectorDataRVA = new CollectorDataRecyclerViewAdapter(this, collectorDatas, new CollectorDataRecyclerViewAdapter.OnItemClickListener(){
            final CollectorDataRecyclerViewAdapter.OnItemClickListener oicl = this;
            final Context context = CollectionActivity.this;
            @Override
            public void onItemClick(final View view, final int position) {
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                input.requestFocus();

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setView(input)
                        .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String in = input.getText().toString();
                                if (in.length() == 0)
                                    in = "0";
                                CollectionActivity ca = (CollectionActivity) context;
                                ca.UpdateDatabase(position+1, Double.parseDouble(in));
                                ((InputMethodManager) Objects.requireNonNull(context.getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((InputMethodManager) Objects.requireNonNull(context.getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        })
                        .setCancelable(false);
                if (position < 32){
                    builder.setNeutralButton("Next", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String in = input.getText().toString();
                            if (in.length() == 0)
                                in = "0";
                            CollectionActivity ca = (CollectionActivity) context;
                            ca.UpdateDatabase(position+1, Double.parseDouble(in));
                            oicl.onItemClick(view, position+1);
                        }
                    });
                }
                if (position == 31){
                    builder.setTitle("Addition");
                } else if (position == 32){
                    builder.setTitle("Subtraction");
                } else {
                    builder.setTitle("Day " + (position+1));
                }
                builder.show();
                ((InputMethodManager) Objects.requireNonNull(context.getSystemService(Context.INPUT_METHOD_SERVICE))).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        rv.setAdapter(CollectorDataRVA);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @SuppressLint("DefaultLocale")
    public void UpdateDatabase(int position, Double input){
        if (db.updateData(CollectorName, "DATA" + position, input)){
            collectorDatas.get(position - 1).setAmount(String.format("%.2f", input));
            CollectorDataRVA.notifyItemChanged(position-1);
            databaseChanged = true;
        } else{
            Toast.makeText(this, "Failed to update data. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent output = new Intent();
        output.putExtra("UpdateDatabaseRequired", databaseChanged);
        if(databaseChanged)
            output.putExtra("CollectorIndex", CollectorIndex);
        setResult(RESULT_OK, output);
        finish();
    }
}
