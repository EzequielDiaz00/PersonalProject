package com.arashimikamidev.personalproject;

import java.io.Serializable;

public class ClassChat implements Serializable {

    String mReceptor;
    String mEmisor;

    String mfReceptor;
    String mfEmisor;

    public ClassChat(String mReceptor, String mEmisor, String mfReceptor, String mfEmisor) {
        this.mReceptor = mReceptor;
        this.mEmisor = mEmisor;
        this.mfReceptor = mfReceptor;
        this.mfEmisor = mfEmisor;
    }

    public String getmEmisor() {
        return mEmisor;
    }

    public String getmReceptor() {
        return mReceptor;
    }

    public String getMfEmisor() {
        return mfEmisor;
    }

    public String getMfReceptor() {
        return mfReceptor;
    }
}
