package com.sergeant_matatov.watchsitter;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ServiceSendSMS extends Service {
    final String LOG_TAG = "myLogs";

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";


    private Vibrator vibrator;

    private MediaPlayer mediaPlayer;

    AlarmAlertActivity a;

    public ServiceSendSMS() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "create onStartCommand");
        //загрузка контактов
        PersonSQL personSQL = new PersonSQL(getApplicationContext(), "db1", null, 1);
        List<PersonData> listPD;
        listPD = personSQL.getAllPerson();
        int i = 1;
        for (PersonData pd : listPD) {
            //      str += "name :" + pd.getPerson() + "\n";
            Log.d(LOG_TAG, "create onStartCommand " + pd.getPhone());
            TaskSendSMS tsSms = new TaskSendSMS();
            tsSms.context = getBaseContext();
            tsSms.phone = pd.getPhone();
            tsSms.execute();
            tsSms.size = listPD.size();
            tsSms.i = i++;

            //         personArrayList.add(pd.getPerson());
            //           sendSMS(pd.getPhone());
        }

  //      stopSelf();


        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy service");

        System.exit(0);
   //     a.finish();
    }

    //отправляем SMS

    public void sendSMS(String phoneNum) {
        //     String phoneNum = loadContact();
        String msg = "watch sitter..что-то случидлсь";
        Log.d(LOG_TAG, "send  " + phoneNum);
        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> al_message = new ArrayList<String>();
        al_message = sms.divideMessage(msg);

        ArrayList<PendingIntent> al_piSent = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> al_piDelivered = new ArrayList<PendingIntent>();

        for (int i = 0; i < al_message.size(); i++) {
            Intent sentIntent = new Intent(SENT);
            PendingIntent pi_sent = PendingIntent.getBroadcast(this, i, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piSent.add(pi_sent);
            Intent deliveredIntent = new Intent(DELIVERED);
            PendingIntent pi_delivered = PendingIntent.getBroadcast(this, i, deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piDelivered.add(pi_delivered);
        }
        sms.sendMultipartTextMessage(phoneNum, null, al_message, al_piSent, al_piDelivered);
    }


}
