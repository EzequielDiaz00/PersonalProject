package com.arashimikamidev.personalproject;

import java.io.Serializable;
public class ClassFriends implements Serializable {

    String userName;
    String userEmail;
    String userFoto;

    public ClassFriends(String userName, String userEmail, String userFoto) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userFoto = userFoto;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserFoto() {
        return userFoto;
    }
}
