package com.example.myapplication;

import android.os.Parcel;

public class User {
    private String fullname;
    private String email;
    private String password;

    public User(String fullname, String email, String password) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;

    }
    public User(){}

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    protected User(Parcel in){
        fullname = in.readString();
        email = in.readString();
        password = in.readString();
    }

}