package com.example.stumanager.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataFormatUtil {
    public static String getFormatDate(Date date, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
