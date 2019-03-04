package com.sergeant_matatov.watchsitter;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;

import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sergeant_matatov.watchsitter.Constants.NAME_PREF_FILE;
import static com.sergeant_matatov.watchsitter.Constants.USER_NAME;
import static com.sergeant_matatov.watchsitter.Constants.USER_NUMBER;

public class SysManagerActivity extends BaseActivity {

    final String LOG_TAG = "myLogs";

    ListView listAlarmView;
    ListView listPersonView;

    AlarmListAdapter alarmListAdapter;
    ArrayAdapter arrayAdapterPerson;

    private ArrayList<String> personArrayList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sys_manager_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //    dialogWriteAuth();

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
             //   startActivity(new Intent(SysManagerActivity.this, Book.class));
    //            sendMessage("");
                //       Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //круглая кнопка добавить будильник
        FloatingActionButton addAlarm = (FloatingActionButton) findViewById(R.id.addAlarm);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getUserName().isEmpty() && getUserPhone().isEmpty())
                    dialogAuth();
                else
                    startActivity(new Intent(SysManagerActivity.this, AlarmPreferencesActivity.class));
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

    //save user name and phone number
    private void saveUser(String name, String phone) {
        SharedPreferences personPref = getSharedPreferences(NAME_PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor edit = personPref.edit();
        edit.putString(USER_NAME, name);
        edit.putString(USER_NUMBER, phone);
        edit.commit();
    }

    //get user name
    private String getUserName() {
        SharedPreferences personPref = getSharedPreferences(NAME_PREF_FILE, MODE_PRIVATE);
        return personPref.getString(USER_NAME, "");
    }

    //get user phone number
    private String getUserPhone() {
        SharedPreferences personPref = getSharedPreferences(NAME_PREF_FILE, MODE_PRIVATE);
        return personPref.getString(USER_NUMBER, "");
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

    public void dialogAuth() {
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View v = adbInflater.inflate(R.layout.element_dialog_auth, null);

        TextInputLayout tilEditName = (TextInputLayout) v.findViewById(R.id.tilEditName);
        final EditText editTextName = (EditText) tilEditName.findViewById(R.id.editTextName);

        TextInputLayout tilEditPhone = (TextInputLayout) v.findViewById(R.id.tilEditPhone);
        final EditText editTextPhone = (EditText) tilEditPhone.findViewById(R.id.editTextPhone);

        TextView textLinkPolicy = (TextView) v.findViewById(R.id.textLinkPolicy);
        textLinkPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.textTitleImportant))
                .setCancelable(false)
                .setView(v)
                .setPositiveButton(R.string.btnOK, null)
                .setNegativeButton(R.string.btnCancel, null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextName.getText().toString().isEmpty() && editTextPhone.getText().toString().isEmpty()) {
                    editTextName.setError(getString(R.string.strErrorName));
                    editTextPhone.setError(getString(R.string.strErrorPhone));
                } else if (editTextName.getText().toString().isEmpty() && !editTextPhone.getText().toString().isEmpty())
                    editTextName.setError(getString(R.string.strErrorName));
                else if (!editTextName.getText().toString().isEmpty() && editTextPhone.getText().toString().isEmpty())
                    editTextPhone.setError(getString(R.string.strErrorPhone));
                else {
                    saveUser(editTextName.getText().toString(), editTextPhone.getText().toString());
                    dialog.dismiss();
                }
            }
        });
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

    //exit
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        System.exit(0);
    }

    @Override
    protected void onPause() {
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
}