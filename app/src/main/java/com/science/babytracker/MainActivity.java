package com.science.babytracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    MediaRecorder mRecorder = null;

    ToggleButton rhymeButton;
    int clickCount = 0;
    Runnable rhymeButtonRunnable;
    Handler rhymeButtonHandler;
    boolean childAwakeAsleep = true;
    boolean isRecording = true;
    String toggleButtonGroupStrings[];
    View toggleButtonGroups[];
    File audioFile;
    int seconds = 0;
    int[] amplitudes;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    int programCount = 0;
    public static TextView audioTextView;
    public static TextView audioTextViewDb;
    private static String fileName = "/dev/null";
    private static String dirName = "";

    Context currentContext = null;
    private int group1 = R.raw.group1;
    private int group2 = R.raw.group2;
    private int currentGroup;

    Button finishButton;

    HashMap csvEntries = new HashMap<String, Long>();
    CSVHandler csv;

    AudioPlayer audioPlayer;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionToRecordAccepted) {
            try {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");

                mRecorder.prepare();
                mRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Data.userGroup.equals("1") ) {
            currentGroup = group1;
        } else {
            currentGroup = group2;
        }
        setContentView(R.layout.activity_main);
        audioPlayer = new AudioPlayer();


        currentContext = getApplicationContext();
        dirName = currentContext.getExternalFilesDir(null).getAbsolutePath();

        csv = new CSVHandler(dirName);
        fileName = csv.FILE_NAME;
        audioFile = new File(dirName + "/" + "audio_" + fileName);
        audioTextView = (TextView) findViewById(R.id.textViewAudioSignalDb);
        amplitudes = new int[10];

        mRecorder = new MediaRecorder();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        rhymeButton = (ToggleButton) findViewById(R.id.toggleButtonRhyme);

        finishButton = (Button) findViewById(R.id.finishButton);

        createAudioThread();
        createToggleButtonGroups();
        toggleButtonHandler();
        buttonHandler();
    }

    private void createToggleButtonGroups() {
        toggleButtonGroupStrings = new String[]{"stress_na", "happy_na", "complaints_na", "position_na", "child_activity_na"};
        toggleButtonGroups = new View[]{null, null, null, null, null};
    }

    private void buttonHandler() {

        finishButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.GONE);

                if (e.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.parseColor("#FF0000"));
                } else if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    programCount++;
                    writeRadioButtonValuesToFile();
                    v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                if (programCount == 1) {
                    System.exit(0);
                }
                return false;
            }
        });
    }

    private void toggleButtonHandler() {
        rhymeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleButtonBehaviourHandler(v);
                boolean on = ((ToggleButton) v).isChecked();
                clickCount += 1;
                if (on && clickCount < 2) {
                    //((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.VISIBLE);
                    if(Data.userGroup.equals("1") ) {
                        audioPlayer.startLoop(currentContext, group1);
                    } else if (Data.userGroup.equals("2") ){
                        audioPlayer.startLoop(currentContext, group2);
                    }
                }
//                else {
//                    ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.VISIBLE);
//                    //audioPlayer.stopLoop();
//                    //stopBlinkingBehaviour();
//                }
                if(clickCount >= 1) {
                    ((ToggleButton) v).setText("");
                    ((ToggleButton) v).setBackgroundColor(0);
                }
            }
        });
    }


    public void buttonToCsvHandler(View v) {
        String name = getResources().getResourceEntryName(v.getId());
        Long ts = System.currentTimeMillis();

        Long timeStamp = ts / 1000;

        String dateTime = getDateTimeFromTimeStamp(new Date(ts));
        csv.writeToFile(name, timeStamp + "", dateTime);
    }

    public void buttonBehaviourHandler(View v) {
        ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.GONE);

        writeRadioButtonValuesToFile();
    }

    private void resetButton(View v) {
        if (v != null) {
            Context ctx = v.getContext();
            Drawable bg_button = ContextCompat.getDrawable(ctx, R.drawable.bg_button);
            v.setBackgroundDrawable(bg_button);
        }
    }

    private void setButton(View v) {
        v.setBackgroundColor(Color.parseColor("#FF0000"));
    }

    private void changeButtonColoring(View oldButton, View newButton) {
        if (oldButton != null) {
            resetButton(oldButton);
        }
        setButton(newButton);
    }

    public void buttonBehaviourHandlerStress(View v) {
        changeButtonColoring(toggleButtonGroups[0], v);
        toggleButtonGroupStrings[0] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[0] = v;
    }

    public void buttonBehaviourHandlerHappy(View v) {
        changeButtonColoring(toggleButtonGroups[1], v);
        toggleButtonGroupStrings[1] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[1] = v;
    }

    public void buttonBehaviourHandlerComplaints(View v) {
        changeButtonColoring(toggleButtonGroups[2], v);
        toggleButtonGroupStrings[2] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[2] = v;
    }

    public void buttonBehaviourHandlerPosition(View v) {
        changeButtonColoring(toggleButtonGroups[3], v);
        toggleButtonGroupStrings[3] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[3] = v;
    }

    public void buttonBehaviourHandlerChildActivity(View v) {
        changeButtonColoring(toggleButtonGroups[4], v);
        toggleButtonGroupStrings[4] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[4] = v;
    }

    public void writeRadioButtonValuesToFile() {
        Long ts = System.currentTimeMillis();
        Long timeStamp = ts / 1000;
        String dateTime = getDateTimeFromTimeStamp(new Date(ts));

        for (int i = 0; i < toggleButtonGroupStrings.length; i++) {
            String name = toggleButtonGroupStrings[i];
            resetButton(toggleButtonGroups[i]);
            csv.writeToFile(name, timeStamp + "", "", dateTime, "");
        }
    }

    public void buttonTouchBehaviourHandler(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            v.setBackgroundColor(Color.parseColor("#FF0000"));
        } else if (e.getAction() == MotionEvent.ACTION_DOWN) {
            buttonToCsvHandler(v);
            v.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }

    public void toggleButtonBehaviourHandler(View v) {
        boolean on = ((ToggleButton) v).isChecked();
        String name = getResources().getResourceEntryName(v.getId()).replace("toggleButton", "");

        Long ts = System.currentTimeMillis();
        Long timeStamp = ts / 1000;
        String dateTime = getDateTimeFromTimeStamp(new Date(ts));

        if (on) {
            //started
            v.setBackgroundColor(0xFFFF0000);
            csvEntries.put(name, ts);
        } else {
            //stopped
            v.setBackgroundColor(0xFF00FF00);
            Long startTimeStamp = (Long) csvEntries.get(name);

            String startDateTime = getDateTimeFromTimeStamp(new Date(startTimeStamp));
            String stopDateTime = getDateTimeFromTimeStamp(new Date(ts));

            csv.writeToFile(name, startTimeStamp.toString(), timeStamp + "", startDateTime, stopDateTime);
        }
    }

    private int getMinutes(int m) {
        return (60 * 1000) * m;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_BACK:
                showMessage("Aktion nicht erlaubt!");
                return true;
        }
        return super.onKeyDown(keycode, e);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getDateTimeFromTimeStamp(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    private int getAmplitude() {
        if (mRecorder != null) {
            return mRecorder.getMaxAmplitude();
        } else {
            Log.d("AudioActivity", "no MediaRecorder found!!");
            return 0;
        }
    }

    private void createAudioThread() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(audioPlayer.isPlaying(currentContext, currentGroup )) {
                                rhymeButton.setEnabled(false);
                                rhymeButton.setFocusable(false);
                            }
                            if(!audioPlayer.isPlaying(currentContext, currentGroup) && clickCount >= 1) {
                                ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.VISIBLE);
                            }
                            Long ts = System.currentTimeMillis();
                            String dateTime = getDateTimeFromTimeStamp(new Date(ts));

                            int max = 17500;
                            int min = 0;
                            int cAmplitude = Math.abs(getAmplitude());

                            double clampAmplitude = Math.max(min, Math.min(max, cAmplitude));
                            String currentAmplitude = String.format("%.2f",(clampAmplitude/max)*100);

                            writeToFile(dateTime, currentAmplitude);
                            if (cAmplitude != 0) {
                                audioTextView.setText(currentAmplitude);
                                double db = 20 * Math.log10((double)Math.abs(cAmplitude));
//                                double db = 20 * Math.log10((double)Math.abs(cAmplitude) / 32768);
//                                double db = 20 * Math.log((double)Math.abs(cAmplitude) / 2700.0);
                                //audioTextViewDb.setText(String.format("%.2f", db));
                                setAudioBackgroundColor(cAmplitude);
                            }
                        }
                    });
                }
            }
        };

        new Thread(runnable).start();
    }

    public void writeToFile(String timeStamp, String amplitude) {
        if (new File(dirName).exists()) {
            try {
                FileWriter fw = new FileWriter(audioFile, true);
                String entry = timeStamp + "," + amplitude + "\n";
                fw.append(entry);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAudioBackgroundColor(int amplitude) {
        int max = 17500;
        amplitudes[seconds] = amplitude;
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += amplitudes[i];
        }
        int avg = sum / 10;

        if (avg > max) {
            //audioTextView.setBackgroundColor(Color.parseColor("#FF0000"));
        } else {
            //audioTextView.setBackgroundColor(Color.parseColor("#99FF00"));
        }
        seconds = (seconds >= 9) ? 0 : seconds + 1;
    }
}