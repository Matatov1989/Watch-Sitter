package com.sergeant_matatov.watchsitter;

/**
 * Created by Yurka on 29.08.2016.
 */
public class PersonData {
    String person;
    String phone;

    public PersonData(String person, String phone) {
        this.person = person;
        this.phone = phone;
    }
    public String getPerson() {
        return person;
    }
    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone()
    {
        return phone;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

}