package com.sergeant_matatov.watchsitter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Yurka on 29.08.2016.
 */
public class Book extends Activity {

    final String LOG_TAG = "myLogs";

    ListView listView;
    String pers;
    String flagTab;
    String tempPhone, lastPhone;        //корзины (temps)

    String old = " ";             //для проверки на дубляж

    ArrayAdapter myArrayAdapter;
    private ArrayList<String> personList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_book);

        Intent intent = getIntent();
        if (intent.hasExtra("flagTab"))
            flagTab = intent.getStringExtra("flagTab");

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);       // устанавливаем режим выбора пунктов списка

        getContacts();      //функция загружающая контакты с телефоной книги

        myArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, personList);

        listView.setAdapter(myArrayAdapter);
    }

    //выбранный номер отправляем в textPhone
    public void onClickOK(View v)
    {
        SparseBooleanArray sbArray = listView.getCheckedItemPositions();
        for (int i = 0; i < sbArray.size(); i++)
        {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key))
            {
                Log.d(LOG_TAG, "book person: " + personList.get(key));
                PersonSQL personSQL = new PersonSQL(getApplicationContext(), "db1", null, 1);
                PersonData pd;

                String strTemp = personList.get(key).toString();
                String [] arrTempStr = strTemp.split(" ");
                String name = arrTempStr[0];
                String phone = arrTempStr[1];
                Log.d(LOG_TAG, "book person: " + name + " " + phone);
       //         pd = new PersonData(personList.get(key).toString());
                pd = new PersonData(name, phone);
                personSQL.addNewPerson(pd);
            }
        }
        startActivity(new Intent(Book.this, SysManagerActivity.class));
    }

    //загружаем контакты с телефонной книги в arraylist
    public void getContacts()
    {
        String phoneNumber = null;

        //Связываемся с контактными данными и берем с них значения id контакта, имени контакта и его номера:
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        pers="";
        ContentResolver contentResolver = getContentResolver();
        //тут прописывается сортировка по алфавиту
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
        //Запускаем цикл обработчик для каждого контакта:
        if (cursor.getCount() > 0)
        {
            //Если значение имени и номера контакта больше 0 (то есть они существуют) выбираем
            //их значения в приложение привязываем с соответствующие поля "Имя" и "Номер":
            while (cursor.moveToNext())
            {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                //Получаем имя:
                if (hasPhoneNumber > 0)
                {
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    //и соответствующий ему номер:
                    while (phoneCursor.moveToNext())
                    {
                        pers=name;
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));

                        int flag = 0;

                        if(phoneNumber.indexOf("+") != -1 || phoneNumber.indexOf("05") != -1)
                            flag = 0;

                        else if(phoneNumber.indexOf("05") == -1 || phoneNumber.indexOf("+") == -1)
                            flag=1;

                        if(flag == 0)
                        {
                            tempPhone = phoneNumber.replaceAll("-", "");    //убираем "-"
                            lastPhone = tempPhone.replaceAll(" ", "");    //убираем пробелы
                            pers += " " + lastPhone + " ";

                            if(!old.equals(pers))   //проверка на дубляж
                                personList.add(pers);

                            old = pers;     //для проверки на дубляж
                        }
                    }
                }
            }
        }

        /*проверка на дубляж в основном из-за WhatsApp*/
        for(int i = 0; i < personList.size(); i++)
        {
            pers = personList.get(i);

            for(int j = i + 1; j < personList.size(); j++)
                if(pers.equals(personList.get(j)))
                    personList.remove(j);   //удаляем повторяющийся контакт
        }

        //удаляем повторяющийся контакт
        for(int i = 0; i < personList.size(); i++)
        {
            pers = personList.get(i);

            for(int j = i + 1; j < personList.size(); j++)
                if(pers.equals(personList.get(j)))
                    personList.remove(j);   //удаляем повторяющийся контакт

        }
    }

}
