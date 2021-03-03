package com.hasan.firebase_chatapp.Model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String search;
    private String isWriting;
    private String numara;
    private boolean hesap;

    public User(String id, String username, String imageURL, String status, String search) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
    }

    public User(String isWriting) {
        this.isWriting = isWriting;
    }

    public User() {

    }

    public boolean getHesap() {
        return this.hesap;
    }
    public void setHesap(Boolean hesap){
        this.hesap=hesap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getIsWriting() {
        return isWriting;
    }

    public void setIsWriting(String isWriting) {
        this.isWriting = isWriting;
    }

    public void setNumara(String numara) {
        this.numara = numara;
    }

    public String getNumara() {
        return this.numara;
    }
}
