package com.example.smartgardenassistent;

import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable {
    private String fullName;
    private String email;


    private String password;



    public User() {}

    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;

        this.password = password;
    }
    protected User(Parcel in) {
        fullName = in.readString();
        email = in.readString();

        password = in.readString();

    }





    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(email);

        dest.writeString(password);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
