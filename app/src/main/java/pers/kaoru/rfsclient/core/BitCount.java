package pers.kaoru.rfsclient.core;

import java.text.DecimalFormat;

public class BitCount {

    private static final long KB = 1024;
    private static final long MB = 1024 * 1024;
    private static final long GB = 1024 * 1024 * 1024;

    public static String ToString(long count) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (count / KB < 1) {
            return count + "B";
        } else if (count / MB < 1) {
            return decimalFormat.format((float)count / KB) + "KB";
        } else if (count / GB < 1) {
            return decimalFormat.format((float)count / MB) + "MB";
        } else {
            return decimalFormat.format((float)count / GB) + "GB";
        }
    }
}
