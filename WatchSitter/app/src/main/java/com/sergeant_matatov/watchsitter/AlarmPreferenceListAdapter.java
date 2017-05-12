package com.sergeant_matatov.watchsitter;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yurka on 29.08.2016.
 */
public class AlarmPreferenceListAdapter extends BaseAdapter implements Serializable {

    private Context context;
    private Alarm alarm;
    private List<AlarmPreference> preferences = new ArrayList<AlarmPreference>();
    //   private final String[] repeatDays = {context.getString(R.string.daySunday), context.getString(R.string.dayMonday), context.getString(R.string.dayTuesday), context.getString(R.string.dayWednesday), context.getString(R.string.dayThursday), context.getString(R.string.dayFriday), context.getString(R.string.daySaturday)};
    private String[] repeatDays;

    private String[] alarmDifficulties;

    private String[] alarmTones;
    private String[] alarmTonePaths;

    public AlarmPreferenceListAdapter(Context context, Alarm alarm) {
        setContext(context);


//		(new Runnable(){
//
//			@Override
//			public void run() {
        //      Log.d("AlarmPreferenceListAdapter", "Loading Ringtones...");

        repeatDays = new String[]{context.getString(R.string.daySunday), context.getString(R.string.dayMonday), context.getString(R.string.dayTuesday), context.getString(R.string.dayWednesday), context.getString(R.string.dayThursday), context.getString(R.string.dayFriday), context.getString(R.string.daySaturday)};
 //       alarmDifficulties = new String[]{context.getString(R.string.levelEasy), context.getString(R.string.levelMedium), context.getString(R.string.levelHard)};

        RingtoneManager ringtoneMgr = new RingtoneManager(getContext());

        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);

        Cursor alarmsCursor = ringtoneMgr.getCursor();

        alarmTones = new String[alarmsCursor.getCount() + 1];
        alarmTones[0] = "Silent";
        alarmTonePaths = new String[alarmsCursor.getCount() + 1];
        alarmTonePaths[0] = "";

        if (alarmsCursor.moveToFirst()) {
            do {
                alarmTones[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(getContext());
                alarmTonePaths[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString();
            } while (alarmsCursor.moveToNext());
        }
        //     Log.d("AlarmPreferenceListAdapter", "Finished Loading " + alarmTones.length + " Ringtones.");
        alarmsCursor.close();
//
//			}
//
//		}).run();
//
        setMathAlarm(alarm);
    }

    @Override
    public int getCount() {
        return preferences.size();
    }

    @Override
    public Object getItem(int position) {
        return preferences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlarmPreference alarmPreference = (AlarmPreference) getItem(position);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        switch (alarmPreference.getType()) {
            case BOOLEAN:
                if (null == convertView || convertView.getId() != android.R.layout.simple_list_item_checked)
                    convertView = layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);

                CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                checkedTextView.setText(alarmPreference.getTitle());
                checkedTextView.setChecked((Boolean) alarmPreference.getValue());
                break;
            case INTEGER:
            case STRING:
            case LIST:
            case MULTIPLE_LIST:
            case TIME:
            default:
                if (null == convertView || convertView.getId() != android.R.layout.simple_list_item_2)
                    convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);

                TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                text1.setTextSize(18);
                text1.setText(alarmPreference.getTitle());

                TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                text2.setText(alarmPreference.getSummary());
                break;
        }

        return convertView;
    }

    public Alarm getMathAlarm() {
        for (AlarmPreference preference : preferences) {
            switch (preference.getKey()) {
                case ALARM_ACTIVE:
                    alarm.setAlarmActive((Boolean) preference.getValue());
                    break;
       //         case ALARM_NAME:
       //             alarm.setAlarmName((String) preference.getValue());
        //            break;
                case ALARM_TIME:
                    alarm.setAlarmTime((String) preference.getValue());
                    break;
        //        case ALARM_DIFFICULTY:
         //           alarm.setDifficulty(Alarm.Difficulty.valueOf((String) preference.getValue()));
          //          break;
                case ALARM_TONE:
                    alarm.setAlarmTonePath((String) preference.getValue());
                    break;
                case ALARM_VIBRATE:
                    alarm.setVibrate((Boolean) preference.getValue());
                    break;
                case ALARM_REPEAT:
                    alarm.setDays((Alarm.Day[]) preference.getValue());
                    break;
            }
        }

        return alarm;
    }

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
        preferences.clear();
  //      preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACTIVE, "Active", null, null, alarm.getAlarmActive(), AlarmPreference.Type.BOOLEAN));
  //      preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_NAME, "Label", alarm.getAlarmName(), null, alarm.getAlarmName(), AlarmPreference.Type.STRING));
        preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TIME, context.getString(R.string.setTimeAlarm), alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), AlarmPreference.Type.TIME));
        preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_REPEAT, context.getString(R.string.setRepeat), alarm.getRepeatDaysString(), repeatDays, alarm.getDays(), AlarmPreference.Type.MULTIPLE_LIST));
   //     preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_DIFFICULTY, context.getString(R.string.setDifficulty), alarm.getDifficulty().toString(), alarmDifficulties, alarm.getDifficulty(), AlarmPreference.Type.LIST));

        Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
        Ringtone alarmTone = RingtoneManager.getRingtone(getContext(), alarmToneUri);

        if (alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")) {
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, context.getString(R.string.setRingtone), alarmTone.getTitle(getContext()), alarmTones, alarm.getAlarmTonePath(), AlarmPreference.Type.LIST));
        } else {
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, context.getString(R.string.setRingtone), getAlarmTones()[0], alarmTones, null, AlarmPreference.Type.LIST));
        }

        preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_VIBRATE, "Vibrate", null, null, alarm.getVibrate(), AlarmPreference.Type.BOOLEAN));
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String[] getRepeatDays() {
        return repeatDays;
    }

    public String[] getAlarmDifficulties() {
        return alarmDifficulties;
    }

    public String[] getAlarmTones() {
        return alarmTones;
    }

    public String[] getAlarmTonePaths() {
        return alarmTonePaths;
    }

}