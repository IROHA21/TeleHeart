package com.moon.TeleHeart.contacts;

public class contactList {
    String name;
    int typeOfContact;
    long id;

    public contactList(String name, int typeOfContact, long id) {
        this.name = name;
        this.typeOfContact = typeOfContact;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTypeOfContact() {
        return typeOfContact;
    }

    public void setTypeOfContact(int typeOfContact) {
        this.typeOfContact = typeOfContact;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}





