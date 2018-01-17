package cn.ght.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    public static void print(String msg) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss/SSS");
        System.out.println(simpleDateFormat.format(new Date()) + msg);
    }

}
