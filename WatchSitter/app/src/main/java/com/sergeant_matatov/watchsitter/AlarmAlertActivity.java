package com.sergeant_matatov.watchsitter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("ALL")
public class AlarmAlertActivity extends Activity {

    final String LOG_TAG = "myLogs";

    private Alarm alarm;
    private MediaPlayer mediaPlayer;

    private StringBuilder answerBuilder = new StringBuilder();

 //   private MathProblem mathProblem;
    private Vibrator vibrator;

    private boolean alarmActive;

    private TextView textTimerNums;
    private TextView answerView;
    private String answerString;

    long time;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarm_alert);

        textTimerNums = (TextView) findViewById(R.id.textTimerNums);



        Log.d(LOG_TAG, "creat AlarmAlertActivity");

   //     Bundle bundle = this.getIntent().getExtras();
   //     alarm = (Alarm) bundle.getSerializable("alarm");

        alarm = new Alarm();


  //      this.setTitle(alarm.getAlarmName());
/*
        switch (alarm.getDifficulty()) {
            case EASY:
                mathProblem = new MathProblem(3);
                break;
            case MEDIUM:
                mathProblem = new MathProblem(4);
                break;
            case HARD:
                mathProblem = new MathProblem(5);
                break;

        }
*/

 /*       answerString = String.valueOf(mathProblem.getAnswer());
        if (answerString.endsWith(".0")) {
            answerString = answerString.substring(0, answerString.length() - 2);
        }
*/
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(getClass().getSimpleName(), "Incoming call: "
                                + incomingNumber);
                        try {
                            mediaPlayer.pause();
                        } catch (IllegalStateException e) {
                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(getClass().getSimpleName(), "Call State Idle");
                        try {
                            mediaPlayer.start();
                        } catch (IllegalStateException e) {
                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        // Toast.makeText(this, answerString, Toast.LENGTH_LONG).show();

        startAlarm();
//        10000 1 min 10000
        countDownTimer = new CountDownTimer(300000, 1000) {

            //Здесь обновляем текст счетчика обратного отсчета с каждой секундой
            public void onTick(long millisUntilFinished) {

                String strTime = (new SimpleDateFormat("mm:ss")).format(new Date(millisUntilFinished));
                Log.d(LOG_TAG, "temer "+strTime);
                textTimerNums.setText(strTime);
            }
            //Задаем действия после завершения отсчета (высвечиваем надпись "Бабах!"):
            public void onFinish() {
                Log.d(LOG_TAG, "fin");
                textTimerNums.setText("00:00");
                startService(new Intent(AlarmAlertActivity.this, ServiceSendSMS.class));
            }
        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmActive = true;
    }

    private void startAlarm() {

        if (alarm.getAlarmTonePath() != "") {
            mediaPlayer = new MediaPlayer();
            if (alarm.getVibrate()) {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = { 1000, 200, 200, 200 };
                vibrator.vibrate(pattern, 0);
            }
            try {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setDataSource(this,
                        Uri.parse(alarm.getAlarmTonePath()));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                mediaPlayer.release();
                alarmActive = false;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if (!alarmActive)
            super.onBackPressed();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(this);
    }

    @Override
    protected void onDestroy() {
        try {
            if (vibrator != null)
                vibrator.cancel();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
        }
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
        }
        try {
            mediaPlayer.release();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
        }
        super.onDestroy();
    }

 //   @Override
    public void onClickStopAlarm(View v) {
        if (!alarmActive)
            return;

        stopAlarm();

    }

    public void stopAlarm()
    {
        this.finish();
        countDownTimer.cancel();
    }

/*
    @Override
    public void onClick(View v) {
        if (!alarmActive)
            return;
        String button = (String) v.getTag();
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (button.equalsIgnoreCase("clear")) {
            if (answerBuilder.length() > 0) {
                answerBuilder.setLength(answerBuilder.length() - 1);
                answerView.setText(answerBuilder.toString());
            }
        } else if (button.equalsIgnoreCase(".")) {
            if (!answerBuilder.toString().contains(button)) {
                if (answerBuilder.length() == 0)
                    answerBuilder.append(0);
                answerBuilder.append(button);
                answerView.setText(answerBuilder.toString());
            }
        } else if (button.equalsIgnoreCase("-")) {
            if (answerBuilder.length() == 0) {
                answerBuilder.append(button);
                answerView.setText(answerBuilder.toString());
            }
        } else {
            answerBuilder.append(button);
            answerView.setText(answerBuilder.toString());
            if (isAnswerCorrect()) {
                alarmActive = false;
                if (vibrator != null)
                    vibrator.cancel();
                try {
                    mediaPlayer.stop();
                } catch (IllegalStateException ise) {

                }
                try {
                    mediaPlayer.release();
                } catch (Exception e) {

                }
                this.finish();
            }
        }
        if (answerView.getText().length() >= answerString.length()
                && !isAnswerCorrect()) {
            answerView.setTextColor(Color.RED);
        } else {
            answerView.setTextColor(Color.BLACK);
        }
    }
    */


}
