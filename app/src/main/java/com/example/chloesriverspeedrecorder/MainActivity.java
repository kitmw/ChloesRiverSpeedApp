package com.example.chloesriverspeedrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chloesriverspeedrecorder.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer timer;
    private TimerTask timerTask;
    private long timerStart;
    private Snackbar timerSnack;
    private TextView timerSnackText;
    final Handler handler = new Handler();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will export saved data points", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                draftEmail();
            }
        });
        setRiverScraperListener();
        setTideScraperListener();


    }

    public void setRiverScraperListener(){
        FloatingActionButton updateRiverHeightButton = findViewById(R.id.updateRiverHeight);
        updateRiverHeightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                    scrapeRiverData riverScraper = new scrapeRiverData();
                    riverScraper.execute();
            }
        });
    }

    public void setTideScraperListener(){
        FloatingActionButton updateTideHeightButton = findViewById(R.id.updateTideHeight);
        updateTideHeightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                scrapeTideData tideScraper = new scrapeTideData();
                tideScraper.execute();
            }
        });
    }

    public void startTimer(View view) {
        timer = new Timer();
        initializeTimerTask();
        timerSnack = Snackbar.make(view, "0s", Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null);
        timerSnackText = timerSnack.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        timerSnack.show();
        timerStart = System.currentTimeMillis();

        //schedule the timer, after the first 0ms the TimerTask will run every 10ms
        timer.schedule(timerTask, 0, 10); //
    }

    public void stopTimerTask(View view) {
        if (timer != null) {
            timer.cancel();
            TextView timeTo50Text = view.getRootView().findViewById(R.id.editTextTimeTo50);
            timeTo50Text.setText(timerSnackText.getText());
            String timeTo50String = (String)timerSnackText.getText();
            String [] strArr = timeTo50String.split("\\D+");
            double timeTo50Double;
            if(strArr.length==1){
                timeTo50Double = Double.parseDouble(strArr[0]);
            } else{
                timeTo50Double = Double.parseDouble(strArr[0] + "." + strArr[1]);
            }
            double speed = 50/timeTo50Double;
            TextView speedText = view.getRootView().findViewById(R.id.editTextRiverSpeed);
            speedText.setText(String.format("%,.1f",speed) + "m/s");
            timerSnack.dismiss();
            timer = null;
        }
    }

    public boolean checkTimerRunning(){
        if(timer != null){
            return true;
        }
        return false;
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        long timerDuration = System.currentTimeMillis() - timerStart;
                        double timerDurationSeconds = (double)timerDuration/1000.0;
                        String timerDurationString = String.format("%,.1f",timerDurationSeconds) + "s";
                        if (timerSnackText != null) {
                            timerSnackText.setText(timerDurationString);
                        }
                    }
                });
            }
        };
    }

    public void draftEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        SharedPreferences sharedPrefs =getPreferences(Context.MODE_PRIVATE);
        Map<String,?> allSharedPrefs = sharedPrefs.getAll();
        String allDataString = "";
        for(Map.Entry<String,?> entry : allSharedPrefs.entrySet()){
            allDataString = allDataString + entry + "\n";
        }
        emailIntent.putExtra(Intent.EXTRA_TEXT, allDataString);
        startActivity(Intent.createChooser(emailIntent, "Share"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class scrapeRiverData extends AsyncTask<Void, Void, Void> {
        private String riverHeightString;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect("https://rivers-and-seas.naturalresources.wales/Station/4193").get();
                Element riverHeightHtmlElement = document.getElementsByClass("latest-reading-bar__reading-value").get(0);
                riverHeightString = riverHeightHtmlElement.text();
            } catch(IOException e)   {
                riverHeightString = "Error";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EditText riverHeightEditText = (EditText)findViewById(R.id.editTextUpRiverHeight);
            riverHeightEditText.setText(riverHeightString);
            super.onPostExecute(aVoid);
        }
    }

    private class scrapeTideData extends AsyncTask<Void, Void, Void> {
        private String tideHeightAndTimeString;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect("https://surf-reports.com/United-Kingdom/Wales/Carmarthen-Bay/Tides/").get();
                Element tideHeightTableHtmlElement = document.getElementsByClass("table-sm table-striped w-100 mb-4 tide-table").get(0);
                Elements tideHeightTodayElements = tideHeightTableHtmlElement.children().get(0).children();
                String heightOfNextTide = null;
                for(Element tide : tideHeightTodayElements){
                    String tideStateString = tide.children().get(0).toString();
                    String tideTimeString = tide.children().get(1).toString();
                    String tideHeightString = tide.children().get(2).toString();
                    tideHeightString = tideHeightString.split(">")[1].split("<")[0];
                    tideHeightString = tideHeightString.split(" ")[0]+"m";
                    if (Objects.equals(tideStateString,"<th scope=\"row\">High tide</th>")){
                        LocalTime nowTime = LocalTime.now();
                        int nowHour = nowTime.getHour();
                        int nowMinute = nowTime.getMinute();
                        int nowSecond = nowTime.getSecond();
                        int nowTimeSeconds = nowSecond + 60*(nowMinute+60*nowHour);
                        String[] tideTimeArray = tideTimeString.split("\\D+");
                        int tideHour12 = Integer.parseInt(tideTimeArray[1]);
                        int tideMinute = Integer.parseInt(tideTimeArray[2]);
                        String[] amOrPmArray = tideTimeString.split("\\d+");
                        String amOrPm = amOrPmArray[2];
                        int hoursToAdd = 0;
                        if(Objects.equals(amOrPm,"pm</td>")&& !(Objects.equals(tideHour12,12))) {
                            // add 12 hours to get 24 hour clock if pm (unless 12pm)
                            hoursToAdd = 12;
                        }
                        int tideHour24 = tideHour12+hoursToAdd;
                        int tideTimeSeconds = 60*(tideMinute+60*(tideHour24));
                        if(nowTimeSeconds<tideTimeSeconds){
                            String tideHour24Str=""+tideHour24;
                            String tideMinuteStr = ""+tideMinute;
                            if(tideHour24<10){
                                tideHour24Str = "0"+tideHour24;
                            }
                            if(tideMinute<10){
                                tideMinuteStr = "0"+tideMinute;
                            }
                            tideHeightAndTimeString = (tideHeightString + " @ " + tideHour24Str+":"+tideMinuteStr);
                        }
                    }
                }
                if(tideHeightAndTimeString == null){
                    tideHeightAndTimeString = "Now past high";
                }

            } catch(IOException e)   {
                tideHeightAndTimeString = "Error";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EditText tideHeightEditText = (EditText)findViewById(R.id.editTextTideHeight);
            tideHeightEditText.setText(tideHeightAndTimeString);
            super.onPostExecute(aVoid);
        }
    }


}