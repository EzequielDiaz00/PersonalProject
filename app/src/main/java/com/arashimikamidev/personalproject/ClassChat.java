package com.arashimikamidev.personalproject;

import java.io.Serializable;

public class ClassChat implements Serializable {

    String mReceptor;
    String mEmisor;

    public ClassChat(String mReceptor, String mEmisor) {
        this.mReceptor = mReceptor;
        this.mEmisor = mEmisor;
    }

    public String getmEmisor() {
        return mEmisor;
    }

    public String getmReceptor() {
        return mReceptor;
    }
}
