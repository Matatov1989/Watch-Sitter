package com.sergeant_matatov.watchsitter;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmPreferencesActivity extends BaseActivity {

    final String LOG_TAG = "myLogs";

    ImageButton deleteButton;
    TextView okButton;
    TextView cancelButton;
    private Alarm alarm;
    private MediaPlayer mediaPlayer;

    private ListAdapter listAdapter;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_pref);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("alarm")) {
            setMathAlarm((Alarm) bundle.getSerializable("alarm"));
        } else {
            setMathAlarm(new Alarm());
        }
        if (bundle != null && bundle.containsKey("adapter")) {
            setListAdapter((AlarmPreferenceListAdapter) bundle.getSerializable("adapter"));
        } else {
            setListAdapter(new AlarmPreferenceListAdapter(this, getMathAlarm()));
        }

        //вроде как лист настроек
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
                final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);

                AlertDialog.Builder alert;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                switch (alarmPreference.getType()) {
                    case BOOLEAN:

                        CheckedTextView checkedTextView = (CheckedTextView) v;
                        boolean checked = !checkedTextView.isChecked();
                        ((CheckedTextView) v).setChecked(checked);
                        switch (alarmPreference.getKey()) {
                            case ALARM_ACTIVE:
                                Log.d(LOG_TAG, "active");
                                alarm.setAlarmActive(checked);
                                break;
                            case ALARM_VIBRATE:
                                Log.d(LOG_TAG, "vibrate");
                                alarm.setVibrate(checked);
                                if (checked) {
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                }
                                break;
                        }
                        alarmPreference.setValue(checked);
                        break;
                    case STRING:
                        Log.d(LOG_TAG, "name label");
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        // Set an EditText view to get user input
                        final EditText input = new EditText(AlarmPreferencesActivity.this);

                        input.setText(alarmPreference.getValue().toString());

                        alert.setView(input);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                alarmPreference.setValue(input.getText().toString());

                                if (alarmPreference.getKey() == AlarmPreference.Key.ALARM_NAME) {
                                    alarm.setAlarmName(alarmPreference.getValue().toString());
                                }

                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        });
                        alert.show();
                        break;
                    case LIST:
                        Log.d(LOG_TAG, "liist");
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        CharSequence[] items = new CharSequence[alarmPreference.getOptions().length];
                        for (int i = 0; i < items.length; i++)
                            items[i] = alarmPreference.getOptions()[i];

                        alert.setItems(items, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (alarmPreference.getKey()) {
                                    case ALARM_DIFFICULTY:
                          //              Alarm.Difficulty d = Alarm.Difficulty.values()[which];
                          //              alarm.setDifficulty(d);
                                        break;
                                    case ALARM_TONE:
                                        alarm.setAlarmTonePath(alarmPreferenceListAdapter.getAlarmTonePaths()[which]);
                                        if (alarm.getAlarmTonePath() != null) {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = new MediaPlayer();
                                            } else {
                                                if (mediaPlayer.isPlaying())
                                                    mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }
                                            try {
                                                // mediaPlayer.setVolume(1.0f, 1.0f);
                                                mediaPlayer.setVolume(0.2f, 0.2f);
                                                mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath()));
                                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                                mediaPlayer.setLooping(false);
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();

                                                // Force the mediaPlayer to stop after 3
                                                // seconds...
                                                if (alarmToneTimer != null)
                                                    alarmToneTimer.cancel();
                                                alarmToneTimer = new CountDownTimer(3000, 3000) {
                                                    @Override
                                                    public void onTick(long millisUntilFinished) {

                                                    }

                                                    @Override
                                                    public void onFinish() {
                                                        try {
                                                            if (mediaPlayer.isPlaying())
                                                                mediaPlayer.stop();
                                                        } catch (Exception e) {

                                                        }
                                                    }
                                                };
                                                alarmToneTimer.start();
                                            } catch (Exception e) {
                                                try {
                                                    if (mediaPlayer.isPlaying())
                                                        mediaPlayer.stop();
                                                } catch (Exception e2) {

                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }

                        });

                        alert.show();
                        break;
                    case MULTIPLE_LIST:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        CharSequence[] multiListItems = new CharSequence[alarmPreference.getOptions().length];
                        for (int i = 0; i < multiListItems.length; i++)
                            multiListItems[i] = alarmPreference.getOptions()[i];

                        boolean[] checkedItems = new boolean[multiListItems.length];
                        for (Alarm.Day day : getMathAlarm().getDays()) {
                            checkedItems[day.ordinal()] = true;
                        }
                        alert.setMultiChoiceItems(multiListItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, int which, boolean isChecked) {

                                Alarm.Day thisDay = Alarm.Day.values()[which];

                                if (isChecked) {
                                    alarm.addDay(thisDay);
                                } else {
                                    // Only remove the day if there are more than 1
                                    // selected
                                    if (alarm.getDays().length > 1) {
                                        alarm.removeDay(thisDay);
                                    } else {
                                        // If the last day was unchecked, re-check
                                        // it
                                        ((AlertDialog) dialog).getListView().setItemChecked(which, true);
                                    }
                                }

                            }
                        });
                        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();

                            }
                        });
                        alert.show();
                        break;
                    case TIME:
                        Log.d(LOG_TAG, "time");
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmPreferencesActivity.this, new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                                Calendar newAlarmTime = Calendar.getInstance();
                                newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
                                newAlarmTime.set(Calendar.MINUTE, minutes);
                                newAlarmTime.set(Calendar.SECOND, 0);
                                alarm.setAlarmTime(newAlarmTime);
                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        }, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY), alarm.getAlarmTime().get(Calendar.MINUTE), true);
                        timePickerDialog.setTitle(alarmPreference.getTitle());
                        timePickerDialog.show();
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_alarm:

                Log.d(LOG_TAG, "btn save");
                Database.init(getApplicationContext());
                if (getMathAlarm().getId() < 1) {
                    Database.create(getMathAlarm());
                } else {
                    Database.update(getMathAlarm());
                }
                callMathAlarmScheduleService();
                Toast.makeText(AlarmPreferencesActivity.this, getMathAlarm().getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
                finish();

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private CountDownTimer alarmToneTimer;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", getMathAlarm());
        outState.putSerializable("adapter", (AlarmPreferenceListAdapter) getListAdapter());
    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mediaPlayer != null)
                mediaPlayer.release();
        } catch (Exception e) {
        }
        // setListAdapter(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onUserLeaveHint() {
   //     Toast toast = Toast.makeText(getApplicationContext(), "Нажата кнопка HOME", Toast.LENGTH_SHORT);
   //     toast.show();
        super.onUserLeaveHint();
        moveTaskToBack(true);
        System.exit(0);
    }

    public Alarm getMathAlarm() {
        return alarm;
    }

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public ListAdapter getListAdapter() {
        return listAdapter;
    }

    public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
        getListView().setAdapter(listAdapter);

    }

    public ListView getListView() {
        if (listView == null)
            listView = (ListView) findViewById(android.R.id.list);
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public void onClick(View v) {
        // super.onClick(v);

    }
}
