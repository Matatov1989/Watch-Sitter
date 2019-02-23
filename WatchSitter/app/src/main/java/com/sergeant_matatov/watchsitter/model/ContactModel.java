package com.sergeant_matatov.watchsitter.model;

public class ContactModel {
    String nameContact;
    String phoneContact;
    boolean isSelected = false;

    public ContactModel() {
    }

    public ContactModel(String nameContact, String phoneContact) {
        this.nameContact = nameContact;
        this.phoneContact = phoneContact;
    }

    public String getNameContact() {
        return nameContact;
    }

    public String getPhoneContact() {
        return phoneContact;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
