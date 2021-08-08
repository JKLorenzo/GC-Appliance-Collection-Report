package com.jklorenzo.collectionreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jklorenzo.collectionreport.Database.DatabaseHelper;
import com.jklorenzo.collectionreport.Objects.CollectorCaption;
import com.jklorenzo.collectionreport.Objects.GenericPrintDocumentAdapter;
import com.jklorenzo.collectionreport.Objects.Rank;
import com.jklorenzo.collectionreport.ViewHelper.CollectorCaptionRecyclerViewAdapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.TimeZone;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    int month, year;
    double[] totals;
    double grandTotal;
    String[] monthText = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    String[] NAMES = {"Edmund", "Johnny", "Ricky", "Romy", "Allan", "Delfin"};
    String[] ranking;

    TextView textViewMonth, textViewGT;
    RecyclerView recyclerViewCollectorCaption;

    DatabaseHelper db;
    SharedPreferences sessionPreference;
    ArrayList<CollectorCaption> collectorCaptions;
    CollectorCaptionRecyclerViewAdapter collectorCaptionRVA;
    Cursor res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        sessionPreference = getSharedPreferences("Session", Context.MODE_PRIVATE);
        month = sessionPreference.getInt("Month", calendar.get((Calendar.MONTH)));
        year = sessionPreference.getInt("Year", calendar.get(Calendar.YEAR));

        db = new DatabaseHelper(this);

        textViewMonth = findViewById(R.id.textViewMonth);
        textViewGT = findViewById(R.id.textViewGT);
        recyclerViewCollectorCaption = findViewById(R.id.recyclerViewCollectorCaption);

        reloadDatabase();
        reloadDisplay(true);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    public void selectCollector(int CollectorIndex, int correction){
        CollectorIndex += correction;
        Intent i = new Intent(this, CollectionActivity.class);
        i.putExtra("MonthYear", monthText[month] + " " + year);
        i.putExtra("CollectorIndex", CollectorIndex);
        startActivityForResult(i, 2121);
    }

    public void gotoNextMonth(View v){
        month++;
        if(month >= 12){
            month = 0;
            year++;
        }
        db.openTable(monthText[month] +  year);
        reloadDatabase();
        reloadDisplay(false);
    }

    public void gotoPreviousMonth(View v){
        month--;
        if (month <= -1){
            month = 11;
            year--;
        }
        db.openTable(monthText[month] +  year);
        reloadDatabase();
        reloadDisplay(false);
    }

    public void reloadDatabase(){
        try{
            db.openTable(monthText[month] + year);
            res = db.getAllData();
            if(res.getCount() == 0) {
                db.setDefaultData();
                do{
                    try{
                        Thread.sleep(250);
                    } catch (Exception ignored){}
                    res = db.getAllData();
                } while (res.getCount() == 0);
                Toast.makeText(this, "Database Created for " + monthText[month] + " " + year, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            SharedPreferences.Editor sessioneditor = sessionPreference.edit();
            sessioneditor.putInt("Month", month);
            sessioneditor.putInt("Year", year);
            sessioneditor.apply();
        }
    }

    ArrayList<Rank> ranks;
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void reloadDisplay(boolean initialize){
        textViewMonth.setText(monthText[month] + " " + year);
        db.openTable(monthText[month] + year);
        res = db.getAllData();
        totals = new double[res.getCount()];
        ranks = new ArrayList<>();
        for (int i = 0; i < res.getCount(); i++){
            res.moveToNext();
            double current_total = 0;
            for (int j = 1; j <= 32; j++)
                current_total += res.getDouble(j);
            current_total -= res.getDouble(33);
            totals[i] = current_total;
        }
        if (totals[0] + totals[1] >= 170000)
            ranks.add(new Rank(0, totals[0] + totals[1]));
        if (totals[2] + totals[3] >= 170000)
            ranks.add(new Rank(1, totals[2] + totals[3]));
        if (totals[4] >= 170000)
            ranks.add(new Rank(2, totals[4]));
        if (totals[5] >= 170000)
            ranks.add(new Rank(3, totals[5]));
        if (totals[6] >= 170000)
            ranks.add(new Rank(4, totals[6]));
        if (totals[7] >= 170000)
            ranks.add(new Rank(5, totals[7]));

        String[] rankCaption = {"1st", "2nd", "3rd"};
        int rankSize = ranks.size();
        ranking = new String[]{"", "", "", "", "", ""};
        for (int i = 0; i < rankSize; i++){
            if (i == 3)
                break;
            Rank buffer = ranks.get(0);
            for (Rank r : ranks){
                if (r.getTotal() > buffer.getTotal())
                    buffer = r;
            }
            ranking[buffer.getCollectorIndex()] = rankCaption[i];
            ranks.remove(buffer);
        }

        if(initialize){
            collectorCaptions = new ArrayList<>();
            collectorCaptions.add(new CollectorCaption(NAMES[0], ranking[0], "Daily", String.format("%.2f", totals[1]), "Monthly", String.format("%.2f", totals[0]), R.drawable.icon));
            collectorCaptions.add(new CollectorCaption(NAMES[1], ranking[1],"Daily", String.format("%.2f", totals[3]), "Monthly", String.format("%.2f", totals[2]), R.drawable.icon1));
            collectorCaptions.add(new CollectorCaption(NAMES[2], ranking[2],"", "", "Monthly", String.format("%.2f", totals[4]), R.drawable.icon2));
            collectorCaptions.add(new CollectorCaption(NAMES[3], ranking[3],"", "", "Monthly", String.format("%.2f", totals[5]), R.drawable.icon3));
            collectorCaptions.add(new CollectorCaption(NAMES[4], ranking[4],"", "", "Monthly", String.format("%.2f", totals[6]), R.drawable.icon4));
            collectorCaptions.add(new CollectorCaption(NAMES[5], ranking[5],"", "", "Monthly", String.format("%.2f", totals[7]), R.drawable.icon5));
            collectorCaptionRVA = new CollectorCaptionRecyclerViewAdapter(this, collectorCaptions);
            recyclerViewCollectorCaption.setAdapter(collectorCaptionRVA);
            recyclerViewCollectorCaption.setLayoutManager(new LinearLayoutManager(this));
        } else{
            collectorCaptions.set(0, new CollectorCaption(NAMES[0], ranking[0],"Daily", String.format("%.2f", totals[1]), "Monthly", String.format("%.2f", totals[0]), R.drawable.icon));
            collectorCaptions.set(1, new CollectorCaption(NAMES[1], ranking[1],"Daily", String.format("%.2f", totals[3]), "Monthly", String.format("%.2f", totals[2]), R.drawable.icon1));
            collectorCaptions.set(2, new CollectorCaption(NAMES[2], ranking[2],"", "", "Monthly", String.format("%.2f", totals[4]), R.drawable.icon2));
            collectorCaptions.set(3, new CollectorCaption(NAMES[3], ranking[3],"", "", "Monthly", String.format("%.2f", totals[5]), R.drawable.icon3));
            collectorCaptions.set(4, new CollectorCaption(NAMES[4], ranking[4],"", "", "Monthly", String.format("%.2f", totals[6]), R.drawable.icon4));
            collectorCaptions.set(5, new CollectorCaption(NAMES[5], ranking[5],"", "", "Monthly", String.format("%.2f", totals[7]), R.drawable.icon5));
            collectorCaptionRVA.notifyDataSetChanged();
        }

        grandTotal = 0;
        for(double x : totals)
            grandTotal += x;
        textViewGT.setText(String.format("%.2f", grandTotal));
    }

    public void Print(View v){
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, 200);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            if (requestCode == 2121) {
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("UpdateDatabaseRequired", false)) {
                        reloadDatabase();
                        reloadDisplay(false);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200) {// Print Process
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new GeneratePDFFile(this, month, year).execute();
            } else {
                makeText(this, "Printing denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GeneratePDFFile extends AsyncTask<String, Integer, String> {
        DatabaseHelper db;
        BaseFont timesroman, timesbold;
        int month, year;
        GeneratePDFFile(Context context, int month, int year){
            db = new DatabaseHelper(context);
            db.openTable(monthText[month] + year);
            this.month = month;
            this.year = year;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            makeText(MainActivity.this, "Generating PDF File", Toast.LENGTH_SHORT).show();

            try{
                timesroman = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED);
                timesbold = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED);
            } catch (Exception e){
                makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        @SuppressLint("DefaultLocale")
        @Override
        protected String doInBackground(String... params) {
            try{
                Document document = new Document(PageSize.LETTER);

                PdfWriter.getInstance(document, new FileOutputStream(new File(Environment.getExternalStorageDirectory(),"excel.pdf")));
                document.open();

                PdfPTable table = new PdfPTable(9);
                table.setWidths(new float[]{3f, 9.2f, 10f, 9.2f, 9.2f, 9.2f, 9.2f, 9.2f, 9.2f});
                table.setWidthPercentage(100f);
                PdfPCell cell;
                String[] x;
                String temp;

                // HEADERS
                Font font = new Font(timesbold, 14);
                x = new String[]{"GC APPLIANCE CORPORATION", "COLLECTION REPORT", "FOR " + monthText[month].toUpperCase() + " " + String.valueOf(year).toUpperCase()};
                for(String y : x){
                    cell = new PdfPCell(new Phrase(y, font));
                    cell.setColspan(9);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 9; i++)
                    table.addCell(cell);

                // COLLECTORS
                font = new Font(timesroman, 14);
                x = new String[]{"", "RICKY", "EDMUND", "DAILY", "JOHNNY", "DAILY", "ROMY", "ALLAN", "DELFIN"};
                for(String y : x){
                    cell = new PdfPCell(new Phrase(y, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // DATA 1 to 32
                font = new Font(timesroman, 12);
                int[] order = {4, 0, 1, 2, 3, 5, 6, 7};
                double[] initialtotal = new double[]{0, 0, 0, 0, 0, 0, 0, 0};
                for (int index = 1; index <= 32; index++){
                    if (index <= 31){
                        cell = new PdfPCell(new Phrase(String.valueOf(index), font));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                    } else{
                        cell = new PdfPCell(new Phrase(" ", font));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                    }
                    for (int collectorIndex : order){
                        res = db.getCollectorData(DatabaseHelper.NAMES[collectorIndex]);
                        res.moveToNext();
                        initialtotal[collectorIndex] += res.getDouble(index);
                        temp = String.valueOf(res.getDouble(index));
                        if (temp.contains(".0"))
                            temp = temp.replace(".0", "");
                        if (temp.equals("0"))
                            temp = " ";
                        cell = new PdfPCell(new Phrase(temp, font));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                    }
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 2; i++)
                    table.addCell(cell);

                // INITIAL TOTAL SET A1
                for (int collectorIndex = 0; collectorIndex <= 3; collectorIndex++){
                    temp = String.format("%.2f", initialtotal[collectorIndex]);
                    if (temp.contains(".00"))
                        temp = temp.replace(".00", "");
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 4; i++)
                    table.addCell(cell);

                // INITIAL TOTAL SET B1
                temp = String.format("%.2f", initialtotal[4]);
                if (temp.contains(".00"))
                    temp = temp.replace(".00", "");
                cell = new PdfPCell(new Phrase(temp, font));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                // INITIAL TOTAL SET A2
                for (int collectorIndex = 0; collectorIndex <= 3; collectorIndex+=2){
                    temp = String.format("%.2f", initialtotal[collectorIndex] + initialtotal[collectorIndex+1]);
                    if (temp.contains(".00"))
                        temp = temp.replace(".00", "");
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setColspan(2);
                    table.addCell(cell);
                }

                // INITIAL TOTAL SET B2
                for (int collectorIndex = 5; collectorIndex <= 7; collectorIndex++){
                    temp = String.format("%.2f", initialtotal[collectorIndex]);
                    if (temp.contains(".00"))
                        temp = temp.replace(".00", "");
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 1; i++)
                    table.addCell(cell);

                // Subtract
                for(int collectorIndex : order){
                    res = db.getCollectorData(DatabaseHelper.NAMES[collectorIndex]);
                    res.moveToNext();
                    temp = String.valueOf(res.getDouble(33));
                    if (temp.contains(".0"))
                        temp = temp.replace(".0", "");
                    if (temp.equals("0"))
                        temp = " ";
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 1; i++)
                    table.addCell(cell);

                // TOTAL
                temp = String.format("%.2f", totals[4]);
                if (temp.contains(".00"))
                    temp = temp.replace(".00", "");
                cell = new PdfPCell(new Phrase(temp, font));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                for (int collectorIndex = 0; collectorIndex < 4; collectorIndex+=2){
                    temp = String.format("%.2f", totals[collectorIndex] + totals[collectorIndex+1]);
                    if (temp.contains(".00"))
                        temp = temp.replace(".00", "");
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setColspan(2);
                    table.addCell(cell);
                }

                for (int collectorIndex = 5; collectorIndex <= 7; collectorIndex++){
                    temp = String.format("%.2f", totals[collectorIndex]);
                    if (temp.contains(".00"))
                        temp = temp.replace(".00", "");
                    cell = new PdfPCell(new Phrase(temp, font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 1; i++)
                    table.addCell(cell);

                // Rankings
                cell = new PdfPCell(new Phrase(ranking[2], font));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                for (int i = 0; i <= 1; i++){
                    cell = new PdfPCell(new Phrase(ranking[i], font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setColspan(2);
                    table.addCell(cell);
                }

                for (int i = 3; i <= 5; i++){
                    cell = new PdfPCell(new Phrase(ranking[i], font));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(cell);
                }

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 10; i++)
                    table.addCell(cell);

                // GRAND TOTAL
                font = new Font(timesbold, 14);
                cell = new PdfPCell(new Phrase("GRAND TOTAL", font));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setColspan(2);
                table.addCell(cell);

                temp = String.format("%.2f", grandTotal);
                if (temp.contains(".00"))
                    temp = temp.replace(".00", "");
                cell = new PdfPCell(new Phrase(temp, font));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setColspan(3);
                table.addCell(cell);

                // SPACER
                cell = new PdfPCell(new Phrase(" ", font));
                cell.setBorder(Rectangle.NO_BORDER);
                for (int i = 1; i <= 1; i++)
                    table.addCell(cell);

                // TRADEMARK
                font = new Font(timesroman, 12);
                cell = new PdfPCell(new Phrase("| Prepared by N.Lorenzo |", font));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setColspan(2);
                table.addCell(cell);

                // Save Table
                table.setHorizontalAlignment(Element.ALIGN_CENTER);
                document.add(table);
                document.close();
            } catch (Exception e){
                Log.e("********ERROR********", Objects.requireNonNull(e.getMessage()));
            }
            return "Finished";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            File file = new File(Environment.getExternalStorageDirectory(),"excel.pdf");
            PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
            try{
                GenericPrintDocumentAdapter printDocumentAdapter = new GenericPrintDocumentAdapter("Doc", file);
                assert printManager != null;
                printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());
            }catch (Exception e){
                makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
