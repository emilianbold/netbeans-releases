/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.cldcplatform.startup;
import java.io.*;

public class Util {
    
    private Util() {
        //To avoind instantiation
    }
    
    public static void deleteDirectory(final File dir) {
        Util.deleteDirectory(dir, null);
    }
    
    /*deletes the whole directory with the given filter*/
    public static void deleteDirectory(final File dir, final FileFilter filter) {
        if(!dir.exists()) {
            return;
        }
        if (!dir.delete()) {
            if (dir.isDirectory()) {
                java.io.File[] list;
                if (filter == null)
                    list = dir.listFiles();
                else
                    list = dir.listFiles(filter);
                for (int i=0; i < list.length ; i++) {
                    deleteDirectory(list[i]);
                }
            }
            dir.delete();
        }
    }
    
    /** returns the size of the specified file in bytes*/
    public static long getFileSize(final File filepath) {
        long size = 0;
        if (!filepath.exists()) return size;
        final File[] list = filepath.listFiles();
        if ((list == null) || (list.length == 0)) return size;
        for (int i = 0; i<list.length; i++) {
            if (list[i].isDirectory()) {
                size += getFileSize(list[i]);
            } else {
                size += list[i].length();
            }
        }
        return size;
    }
    
    /** converts the array to String separated by delimiter */
    public static String arrayToString(final Object[] array, final String delimiter ) {
        try {
            if (array == null) return null;
            
            final StringBuffer buf = new StringBuffer();
            buf.append(array[0]);
            for (int i = 1; i< array.length; i++) {
                buf.append(delimiter);
                buf.append(array[i]);
            }
            return buf.toString();
        } catch (Exception ex) {
            return array.toString();
        }
    }
    
    /** Returns a String holding the stack trace information printed by printStackTrace() */
    public static String getStackTrace(final Exception ex) {
        final StringWriter sw = new StringWriter(500);
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
    
    /** A simple method to copy files. */
    public static void copyFile(final File src, final File dest) throws Exception {
        try {
            final FileInputStream in = new FileInputStream(src);
            final FileOutputStream out = new FileOutputStream(dest);
            int c;
            
            while ((c = in.read()) != -1)
                out.write(c);
            
            in.close();
            out.close();
        } catch (FileNotFoundException notFound) {
            throw new Exception("Source or Destination file not found: " + notFound);   //NOI18N
        } catch (IOException ioerr) {
            throw new Exception("IO Error copying file " + src.getName()); //NOI18N
        }
    }   
}
