package com.sergeant_matatov.watchsitter.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeant_matatov.watchsitter.PersonData;
import com.sergeant_matatov.watchsitter.PersonSQL;
import com.sergeant_matatov.watchsitter.R;
import com.sergeant_matatov.watchsitter.model.ContactModel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ContactHolder> {

    final String LOG_TAG = "myLogs";
    Context context;
    ArrayList<ContactModel> arrayListContact;

    private final boolean[] mCheckedStateA;

    int cntCheckedContacts = 0;

    public ContactRecyclerAdapter(Context context) {
        this.context = context;
        this.arrayListContact = getContacts();
        mCheckedStateA = new boolean[arrayListContact.size()];
    }

    @Override
    public ContactRecyclerAdapter.ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater myInflator = LayoutInflater.from(context);
        View view = myInflator.inflate(R.layout.element_list_contact, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactRecyclerAdapter.ContactHolder holder, final int position) {
        holder.textNameContact.setText(arrayListContact.get(position).getNameContact());
        holder.textPhoneContact.setText(arrayListContact.get(position).getPhoneContact());


    }


    @Override
    public int getItemCount() {
        return arrayListContact.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textNameContact;
        TextView textPhoneContact;
        CheckBox checkBoxContact;

        public ContactHolder(View view) {
            super(view);
            textNameContact = (TextView) view.findViewById(R.id.textNameContact);
            textPhoneContact = (TextView) view.findViewById(R.id.textPhoneContact);
            checkBoxContact = (CheckBox) view.findViewById(R.id.checkBoxContact);

            view.setOnClickListener(this);

            checkBoxContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        if (cntCheckedContacts < 3) {
                      //      mCheckedStateA[position] = false;
                            ++cntCheckedContacts;

                            PersonSQL personSQL = new PersonSQL(context, "db1", null, 1);
                            PersonData pd = new PersonData(arrayListContact.get(getAdapterPosition()).getNameContact(), arrayListContact.get(getAdapterPosition()).getPhoneContact());
                            personSQL.addNewPerson(pd);

                            Log.d(LOG_TAG, "if onCheckedChanged : " + isChecked + " - " + cntCheckedContacts);
                        }
                        else{
                            Log.d(LOG_TAG, "max 3 " );
                            checkBoxContact.setChecked(false);
                            Toast.makeText(context, R.string.toastLimitSelected, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        --cntCheckedContacts;

                        PersonSQL personSQL = new PersonSQL(context, "db1", null, 1);
                        personSQL.delPerson(arrayListContact.get(getAdapterPosition()).getPhoneContact());

                        Log.d(LOG_TAG, "else onCheckedChanged : "+ isChecked+" - "+cntCheckedContacts);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
    //        saveContact(arrayListContact.get(getAdapterPosition()).getNameContact(), arrayListContact.get(getAdapterPosition()).getPhoneContact());
       //     Log.d(LOG_TAG, "onCheckedChanged : "+ arrayListContact.get(getAdapterPosition()).getNameContact());
            PersonSQL personSQL = new PersonSQL(context, "db1", null, 1);
            List<PersonData> listPD;
            listPD = personSQL.getAllPerson();
            int i = 1;
            for (PersonData pd : listPD) {
                Log.d(LOG_TAG, "contactts  : "+ pd.getPerson()+" "+pd.getPhone());
            }

        }
    }

    //get contacts from a phone book
    public ArrayList<ContactModel> getContacts() {
        ArrayList<ContactModel> contactListData = new ArrayList<ContactModel>();
        //connect with contacts data, get id, name and number
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = context.getContentResolver();
        //sorting
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
        //start searchig for every contact
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                String phoneNumber = "";
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                //get name
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    //get his numbers
                    while (phoneCursor.moveToNext()) {
                        //           pers = name;
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        phoneNumber = phoneNumber.replace("-", "");
                        phoneNumber = phoneNumber.replace(" ", "");
                        int len = phoneNumber.length();

                        if (len >= 7)
                            contactListData.add(new ContactModel(name, phoneNumber));
                    }
                }
            }
        }

        //dubbing check in due WhatsApp
        for (int i = 0; i < contactListData.size(); i++) {
            String contactName = contactListData.get(i).getNameContact();
            String contactPhone = contactListData.get(i).getPhoneContact();
            for (int j = i + 1; j < contactListData.size(); j++)
                if (contactName.equals(contactListData.get(j).getNameContact()) && contactPhone.equals(contactListData.get(j).getPhoneContact()))
                    contactListData.remove(j);     //remove
        }

        for (int i = 0; i < contactListData.size(); i++) {
            String contactName = contactListData.get(i).getNameContact();
            String contactPhone = contactListData.get(i).getPhoneContact();
            for (int j = i + 1; j < contactListData.size(); j++)
                if (contactName.equals(contactListData.get(j).getNameContact()) && contactPhone.equals(contactListData.get(j).getPhoneContact()))
                    contactListData.remove(j);     //remove
        }
        return contactListData;
    }
}