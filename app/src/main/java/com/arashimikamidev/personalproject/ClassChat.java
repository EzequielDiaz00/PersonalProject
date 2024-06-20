package com.arashimikamidev.personalproject;

import java.io.Serializable;

public class ClassChat implements Serializable {

    String mReceptor;
    String mEmisor;

    String mfReceptor;
    String mfEmisor;

    String mFoto;

    public ClassChat(String mReceptor, String mEmisor, String mfReceptor, String mfEmisor, String mFoto) {
        this.mReceptor = mReceptor;
        this.mEmisor = mEmisor;
        this.mfReceptor = mfReceptor;
        this.mfEmisor = mfEmisor;
        this.mFoto = mFoto;
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

    public String getmFoto() {
        return mFoto;
    }
}
