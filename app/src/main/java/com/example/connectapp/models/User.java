package com.example.connectapp.models;

import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable {
    private String device_token;
    private String name;
    private String image;
    private String online;
    private String status;
    private String thumb_image;

    public User() {
    }

    public User(String device_token, String name) {
        this.device_token = device_token;
        this.name = name;
        this.image = "";
        this.online = "true";
        this.status = "Available";
        this.thumb_image = "";
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    private User(Parcel in) {
        device_token = in.readString();
        name = in.readString();
        image = in.readString();
        online = in.readString();
        status = in.readString();
        thumb_image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(device_token);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(online);
        dest.writeString(status);
        dest.writeString(thumb_image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}