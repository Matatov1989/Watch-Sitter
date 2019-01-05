package com.sergeant_matatov.watchsitter;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SysManagerActivity extends BaseActivity {

    final String LOG_TAG = "myLogs";

    ListView listAlarmView;
    ListView listPersonView;

    AlarmListAdapter alarmListAdapter;
    ArrayAdapter arrayAdapterPerson;

    //   private ArrayList<String> alarmArrayList = new ArrayList<String>();
    private ArrayList<String> personArrayList = new ArrayList<String>();

    public static final int CODE_SEND_SMS = 1;

/*    int myHour;
    int myMinute;
    int cnt = 0;
 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sys_manager_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //если удерживать на выбранной позиции для удаления
        listAlarmView = (ListView) findViewById(android.R.id.list);
        listAlarmView.setLongClickable(true);
        listAlarmView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(SysManagerActivity.this);
                dialog.setTitle(getString(R.string.dialogTitleDel));
                dialog.setMessage(getString(R.string.dialogMsgDelAlarm));
                dialog.setPositiveButton(getString(R.string.btnYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Database.init(SysManagerActivity.this);
                        Database.deleteEntry(alarm);
                        SysManagerActivity.this.callMathAlarmScheduleService();

                        updateAlarmList();
                    }
                });
                dialog.setNegativeButton(getString(R.string.btnNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;
            }
        });

        callMathAlarmScheduleService();

        alarmListAdapter = new AlarmListAdapter(this);
        this.listAlarmView.setAdapter(alarmListAdapter);
        listAlarmView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                Intent intent = new Intent(SysManagerActivity.this, AlarmPreferencesActivity.class);
                intent.putExtra("alarm", alarm);
                startActivity(intent);
            }

        });


        //круглая кнопка добавить человека
        FloatingActionButton addPers = (FloatingActionButton) findViewById(R.id.addPers);
        addPers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SysManagerActivity.this, Book.class));

                //       Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //круглая кнопка добавить будильник
        FloatingActionButton addAlarm = (FloatingActionButton) findViewById(R.id.addAlarm);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissionSendSMS();

            }
        });


        //загрузка контактов
        PersonSQL personSQL = new PersonSQL(getApplicationContext(), "db1", null, 1);
        List<PersonData> listPD;
        listPD = personSQL.getAllPerson();
        String str = "";

        for (PersonData pd : listPD) {
            str += "name :" + pd.getPerson() + "\n";
            personArrayList.add(pd.getPerson());
        }

        //     Log.d(LOG_TAG, "show " + "\n" + str.toString());

        listPersonView = (ListView) findViewById(R.id.listPerson);
        listPersonView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);       // устанавливаем режим выбора пунктов списка

        arrayAdapterPerson = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, personArrayList);

        listPersonView.setAdapter(arrayAdapterPerson);


        //слушатель для листа с контактами
        listPersonView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                //            Toast.makeText(getApplicationContext(), ((TextView) itemClicked).getText(), Toast.LENGTH_SHORT).show();
                String pers = (String) ((TextView) itemClicked).getText();
                dialogDellPerson(pers);
            }
        });

    }


    //диалог для удаления контакта
    public void dialogDellPerson(final String pers) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(getString(R.string.dialogTitleDel));
        adb.setMessage(getString(R.string.dialogMsgDelPers));
        adb.setIcon(android.R.drawable.ic_delete);

        adb.setNegativeButton(getString(R.string.btnNo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setPositiveButton(getString(R.string.btnYes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                PersonSQL personSQL = new PersonSQL(getApplicationContext(), "db1", null, 1);
                personSQL.delPerson(pers);

                dialog.dismiss();
                startActivity(new Intent(SysManagerActivity.this, SysManagerActivity.class));
            }
        });
        adb.show();
    }

    //диалог стартовая инструкция
    public void dialogInstructionsStart() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(getString(R.string.actionBtnInstructions));
        adb.setMessage(getString(R.string.dialogMsgStartInstructions));
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton(getString(R.string.btnOK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.show();
    }

    //диалог инструкция
    public void dialogInstructions() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(getString(R.string.actionBtnInstructions));
        adb.setMessage(getString(R.string.dialogMsgInstructions));
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton(getString(R.string.btnOK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.show();
    }

    /*
        //чек возле будильников вкл/выкл
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.checkBox_alarm_active) {
                CheckBox checkBox = (CheckBox) v;
                Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
                alarm.setAlarmActive(checkBox.isChecked());
                Database.update(alarm);
                if (checkBox.isChecked()) {
                    Toast.makeText(SysManagerActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    */
    //чек возле будильников вкл/выкл
    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "чек возле будильников вкл/выкл");
        if (v.getId() == R.id.checkBox_alarm_active) {
            CheckBox checkBox = (CheckBox) v;
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
            alarm.setAlarmActive(checkBox.isChecked());
            Database.update(alarm);
            SysManagerActivity.this.callMathAlarmScheduleService();
            if (checkBox.isChecked()) {
                Toast.makeText(SysManagerActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }


    public void updateAlarmList() {
        Database.init(SysManagerActivity.this);
        Log.d(LOG_TAG, "create AlarmActivity Database.getAll()");
        final List<Alarm> alarms = Database.getAll();
        alarmListAdapter.setMathAlarms(alarms);

        runOnUiThread(new Runnable() {
            public void run() {
                // reload content
                SysManagerActivity.this.alarmListAdapter.notifyDataSetChanged();
                //если нет будильников вообще то поямтся текст
                if (alarms.size() == 0)
                    dialogInstructionsStart();

           /*
                if(alarms.size() > 0){
                    Log.d(LOG_TAG, "empty invisible");
//                    findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
                }else{
                    Log.d(LOG_TAG, "empty visible");
           //         findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }*/
            }
        });
    }

/*
    public void updateAlarmList(){
        Database.init(SysManagerActivity.this);
 //       Log.d(LOG_TAG, "create Alarm Database.getAll()");
        final List<Alarm> alarms = Database.getAll();
        alarmListAdapter.setMathAlarms(alarms);

        runOnUiThread(new Runnable() {
            public void run() {
                // reload content
                SysManagerActivity.this.alarmListAdapter.notifyDataSetChanged();
                //если нет будильников вообще то поямтся текст
                if(alarms.size() == 0)
                    dialogInstructionsStart();

            }
        });
    }
*/

    //выход из приложения
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        System.exit(0);
    }

    @Override
    protected void onPause() {
        // setListAdapter(null);
        Database.deactivate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAlarmList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sys_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_instructions:
                dialogInstructions();
                break;

            case R.id.action_of_programm:
                startActivity(new Intent(SysManagerActivity.this, AboutProgram.class));
                break;

            case R.id.action_from_developer:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Yury%20Matatov&hl"));
                startActivity(intent);
                break;

            case R.id.action_advise_friend:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.sergeant_matatov.watchsitter&hl");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //check permission on send sms
    private void checkPermissionSendSMS() {
        Log.d(LOG_TAG, "checkPermissionSendSMS");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SEND_SMS)) {
                dialogPermissionSendSMS();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, CODE_SEND_SMS);
            }
        } else {
            startActivity(new Intent(SysManagerActivity.this, AlarmPreferencesActivity.class));
       //     saveAlarm();
        }
    }

    //dialog if user do not set permission on send sms
    private void dialogPermissionSendSMS() {
        Log.d(LOG_TAG, "dialogPermissionSendSMS");
        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setMessage(R.string.dialogCheckPermissionSMS);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(SysManagerActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, CODE_SEND_SMS);
                dialog.dismiss();
            }
        });
        adb.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CODE_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
             //       saveAlarm();
                    startActivity(new Intent(SysManagerActivity.this, AlarmPreferencesActivity.class));
                } else {
                    checkPermissionSendSMS();
                }
                return;
            }
        }
    }
}