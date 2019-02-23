package com.sergeant_matatov.watchsitter;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.sergeant_matatov.watchsitter.Constants.NAME_PREF_FILE;
import static com.sergeant_matatov.watchsitter.Constants.USER_NAME;
import static com.sergeant_matatov.watchsitter.Constants.USER_NUMBER;

public class ServiceSendSMS extends Service {
    final String LOG_TAG = "myLogs";

    private static final String ACCOUNT_SID = "AC27b47ac07c55093e9a763eebb61106f2";
    private static final String AUTH_TOKEN = "2404272220431950d85a6ff2cf33eb25";
    private static final String PHONE_NUMBER_SENDER = "+14253812308";

//    private static final String ACCOUNT_SID = "ACb7a775cff360238a63b8cdeacc0faeb9";
//    private static final String AUTH_TOKEN = "4c118e5634ea1efbb86b098123f7b8e3";

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
            sendMessage(pd.getPhone());
        }
        //      stopSelf();
        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy service");
    }

    //отправляем SMS
    private void sendMessage(String receiverNumber) {
        String body = getString(R.string.textSendSMS, getUserName(), getUserPhone());

        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP
        );

        Map<String, String> data = new HashMap<>();
        data.put("From", PHONE_NUMBER_SENDER);
        data.put("To", "+972549759346");
        data.put("Body", "Hello");

        Log.d(LOG_TAG, "osms " + PHONE_NUMBER_SENDER);
        Log.d(LOG_TAG, "osms " + receiverNumber);
        Log.d(LOG_TAG, "osms " + body);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twilio.com/2010-04-01/")
                .build();
        TwilioApi api = retrofit.create(TwilioApi.class);

        api.sendMessage(ACCOUNT_SID, base64EncodedCredentials, data).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful())
                    Log.d(LOG_TAG, "onResponse->success ");
                else
                    Log.d(LOG_TAG, "onResponse->failure " + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(LOG_TAG, "onFailure");
            }
        });
    }

    interface TwilioApi {
        @FormUrlEncoded
        @POST("Accounts/{ACCOUNT_SID}/SMS/Messages")
        Call<ResponseBody> sendMessage(
                @Path("ACCOUNT_SID") String accountSId,
                @Header("Authorization") String signature,
                @FieldMap Map<String, String> metadata
        );
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
}