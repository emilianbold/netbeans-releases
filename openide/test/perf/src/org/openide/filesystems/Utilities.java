/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * A set of utility methods.
 */
public abstract class Utilities {
    
    /** Counts padding size from given size, i.e. 1 for less than 10, 3 for 100 - 999, etc. */
    public static int expPaddingSize(int size) {
        int ret = 0;
        
        while (size > 0) {
            size /= 10;
            ret++;
        }
        return ret;
    }
    
    /** Appends paddingSize number of digits e.g. 00digit */
    public static void appendNDigits(int digit, int paddingSize, StringBuffer buffer) {
        int localLength = paddingSize - 1;
        int exp[] = new int[] { 0, 10, 100, 1000, 10000, 100000, 1000000 };

        while (digit < exp[localLength--]) {
            buffer.append('0');
        }

        buffer.append(String.valueOf(digit));
    }
    
    /** Creates jar file 
     * @param srcdir which folder to be zipped
     * @param <tt>name</tt> name of the jar
     */
    public static File createJar(File srcdir, String name) throws Exception {
        Process proc = Runtime.getRuntime().exec("jar cf " + name + " .", null, srcdir);
        proc.waitFor();
        copyIS(proc.getErrorStream(), System.out);
        
        return new File(srcdir, name);
    }
    
    /** Copy content of a stream to a PrintStream */
    public static void copyIS(InputStream is, PrintStream out) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String str;
        while ((str = reader.readLine()) != null) {
            out.println(str);
        }
    }
}
