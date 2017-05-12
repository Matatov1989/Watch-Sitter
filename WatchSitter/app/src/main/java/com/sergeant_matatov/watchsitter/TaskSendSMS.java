package com.sergeant_matatov.watchsitter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Yurka on 30.08.2016.
 */
public class TaskSendSMS extends AsyncTask<Void, Void, Void>
{
    final String LOG_TAG = "myLogs";
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    Context context;
    String phone;
    int size;
    int i;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Log.d(LOG_TAG, "task");
        String msg = context.getString(R.string.textSendSMS);

        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> al_message = new ArrayList<String>();
        al_message = sms.divideMessage(msg);

        ArrayList<PendingIntent> al_piSent = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> al_piDelivered = new ArrayList<PendingIntent>();

        for (int i = 0; i < al_message.size(); i++) {
            Intent sentIntent = new Intent(SENT);
            PendingIntent pi_sent = PendingIntent.getBroadcast(context, i, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piSent.add(pi_sent);
            Intent deliveredIntent = new Intent(DELIVERED);
            PendingIntent pi_delivered = PendingIntent.getBroadcast(context, i, deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piDelivered.add(pi_delivered);
        }
        Log.d(LOG_TAG, "task phone "  + phone + " " + al_message);
        sms.sendMultipartTextMessage(phone, null, al_message, al_piSent, al_piDelivered);

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        Log.d(LOG_TAG, "if size == i " + size+ " == " + i);
        if (size == i)
            context.stopService(new Intent(context, ServiceSendSMS.class));
    }
}
