package jp.yuruga.kuseki.utils;

import android.util.Log;

/**
 * Created by maedanaohito on 2015/06/27.
 */
public class Util {
    public static void log(String message){
        Log.d(getCallerClassName(), message);
    }
    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Util.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste.getClassName();
            }
        }
        return null;
    }
}
