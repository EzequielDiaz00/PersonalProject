package com.arashimikamidev.personalproject;

import java.io.Serializable;

public class ClassFriends implements Serializable {

    String userName;
    String userEmail;
    String userFoto;
    String userUrlFoto;

    public ClassFriends(String userName, String userEmail, String userFoto, String userUrlFoto) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userFoto = userFoto;
        this.userUrlFoto = userUrlFoto;
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

    public String getUserUrlFoto() {
        return userUrlFoto;
    }
}
