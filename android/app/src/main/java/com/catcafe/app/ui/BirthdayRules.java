package com.catcafe.app.ui;

import android.app.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

final class BirthdayRules {
    static final String ERROR_MESSAGE = "生日需在 1920-01-01 到今天之间";

    private BirthdayRules() {
    }

    static void applyTo(DatePickerDialog dialog) {
        dialog.getDatePicker().setMinDate(minDateMillis());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    static boolean isBlankOrValid(String value) {
        return value == null || value.trim().isEmpty() || isValid(value);
    }

    static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            long millis = format.parse(value).getTime();
            return millis >= minDateMillis() && millis <= todayEndMillis();
        } catch (ParseException e) {
            return false;
        }
    }

    private static long minDateMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(1920, Calendar.JANUARY, 1);
        return calendar.getTimeInMillis();
    }

    private static long todayEndMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
