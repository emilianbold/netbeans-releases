/*
 * DateUtils.java
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Kirill Sorokin
 */
public class DateUtils {
    private static DateFormat timestampFormatter = 
            new SimpleDateFormat("yyyyMMddHHmmss");
    
    private static DateFormat formattedTimestampFormatter = 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static String getTimestamp() {
        return timestampFormatter.format(new Date());
    }
    
    public static String getFormattedTimestamp() {
        return formattedTimestampFormatter.format(new Date());
    }
}