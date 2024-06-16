package com.arashimikamidev.personalproject;

import android.util.Log;

import java.util.Calendar;

public class ClassDate {

    public String horaAndroid;
    public String fechaAndroid;

    public void obtenerHora() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String currentTimeCalendar = String.format("%02d:%02d:%02d", hour, minute, second);
        horaAndroid = currentTimeCalendar;
        Log.d("CurrentTime", "Hora actual (Calendar): " + currentTimeCalendar);
    }

    public void obtenerFecha() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        String currentDate = String.format("%02d/%02d/%04d", day, month, year);
        fechaAndroid = currentDate;
        Log.d("CurrentTime", "Fecha Actual (Calendar): " + currentDate);
    }
}
