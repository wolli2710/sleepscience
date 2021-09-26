package com.science.babytracker;

import android.Manifest;
import android.app.Instrumentation;
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

    Button errorButton;
    Button finishButton;

    Button menuButton;

    Button childAwake;
    Button childAsleep;

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
        setContentView(R.layout.activity_main);
        audioPlayer = new AudioPlayer();

        currentContext = getApplicationContext();
        dirName = currentContext.getExternalFilesDir(null).getAbsolutePath();

        csv = new CSVHandler(dirName);
        fileName = csv.FILE_NAME;
        audioFile = new File(dirName + "/" + "audio_" + fileName);

        audioTextView = (TextView) findViewById(R.id.textViewAudioSignal);
        audioTextViewDb = (TextView) findViewById(R.id.textViewAudioSignalDb);
        amplitudes = new int[10];

        mRecorder = new MediaRecorder();


        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        rhymeButton = (ToggleButton) findViewById(R.id.toggleButtonRhyme);

        errorButton = (Button) findViewById(R.id.error);
        finishButton = (Button) findViewById(R.id.finishButton);
        menuButton = (Button) findViewById(R.id.buttonMenu);
        childAwake = (Button) findViewById(R.id.childAwake);
        childAsleep = (Button) findViewById(R.id.childAsleap);


        createAudioThread();
        createToggleButtonGroups();
        toggleButtonHandler();
        buttonHandler();
    }

    private void createToggleButtonGroups() {
        toggleButtonGroupStrings = new String[]{"stress_na", "happy_na", "complaints_na", "interaction_na"};
        toggleButtonGroups = new View[]{null, null, null, null, null, null};
    }

    private void buttonHandler() {
        errorButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                buttonTouchBehaviourHandler(v, e);
                return false;
            }
        });

        childAwake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAwakeAsleepButtonColors(v, childAsleep);
            }
        });

        childAsleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAwakeAsleepButtonColors(v, childAwake);
            }
        });

        menuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                System.exit(0);
                return false;
            }
        });

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
                if (programCount == 2) {
                    System.exit(0);
                }
                return false;
            }
        });
    }

    private void setAwakeAsleepButtonColors(View v, Button b) {
        v.setBackgroundColor(0xFF0000FF);
        b.setBackgroundColor(0xFFFFFFFF);
        buttonToCsvHandler(v);
        childAwakeAsleep = false;
    }

    private void toggleButtonHandler() {
        rhymeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleButtonBehaviourHandler(v);
                boolean on = ((ToggleButton) v).isChecked();
                if (on) {
                    ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.VISIBLE);
                    if(Data.userGroup.equals("1") ) {
                        audioPlayer.startLoop(currentContext, group1);
                    } else if (Data.userGroup.equals("2") ){
                        audioPlayer.startLoop(currentContext, group2);
                    }

                    startBlinkingBehaviour(5, v);
                } else {
                    ((GridLayout) findViewById(R.id.gridLayoutButtons)).setVisibility(View.VISIBLE);
                    audioPlayer.stopLoop();
                    stopBlinkingBehaviour();
                }
            }
        });
    }

    private void startBlinkingBehaviour(int t, View v) {
        final int t1 = t;
        final View v1 = v;
        rhymeButtonHandler = new Handler();
        rhymeButtonHandler.postDelayed(
                rhymeButtonRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int delay = 300;
                        changeColourOverTime(delay, v1, 0xFF00FF00, 0xFFFFFF00);
                    }
                }, getMinutes(t1)
        );
    }

    private void stopBlinkingBehaviour() {
        rhymeButtonHandler.removeCallbacks(rhymeButtonRunnable);
    }

    private void changeColourOverTime(int t, View v, int cPrev, int cNew) {
        final int t1 = t;
        final View v1 = v;
        final int c1 = cNew;
        final int c2 = cPrev;
        rhymeButtonHandler = new Handler();
        rhymeButtonHandler.postDelayed(
                rhymeButtonRunnable = new Runnable() {
                    @Override
                    public void run() {
                        v1.setBackgroundColor(c1);
                        changeColourOverTime(t1, v1, c1, c2);
                    }
                }, t
        );
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

    public void buttonBehaviourHandlerInteraction(View v) {
        changeButtonColoring(toggleButtonGroups[3], v);
        toggleButtonGroupStrings[3] = getResources().getResourceEntryName(v.getId());
        toggleButtonGroups[3] = v;
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
                            Long ts = System.currentTimeMillis();
                            String dateTime = getDateTimeFromTimeStamp(new Date(ts));

                            int cAmplitude = getAmplitude();
                            String currentAmplitude = new Integer(cAmplitude).toString();

                            writeToFile(dateTime, currentAmplitude);
                            if (cAmplitude != 0) {
                                audioTextView.setText(currentAmplitude);
                                double db = 20 * Math.log10((double)Math.abs(cAmplitude));
//                                double db = 20 * Math.log10((double)Math.abs(cAmplitude) / 32768);
//                                double db = 20 * Math.log((double)Math.abs(cAmplitude) / 2700.0);
                                audioTextViewDb.setText(String.format("%.2f", db));
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
            audioTextView.setBackgroundColor(Color.parseColor("#FF0000"));
        } else {
            audioTextView.setBackgroundColor(Color.parseColor("#99FF00"));
        }
        seconds = (seconds >= 9) ? 0 : seconds + 1;
    }
}