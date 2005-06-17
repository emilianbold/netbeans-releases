/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.*;

import org.openide.*;

/**
 * Just a collection of static utility methods
 * 
 * @author Kirill Sorokin
 */
public class WSUtil {
    
    private static Class clazz = new WSUtil().getClass();
    
    /** 
     * Reads an entire text file into a string
     * 
     * @param file the file's handle
     * @return a string with the file's contents
     */
    public static String readFile(File file) {
       // init the buffer for storing the file's contents
       StringBuffer buffer = new StringBuffer();
       
       try {
           // init the reader
           LineNumberReader reader = new LineNumberReader(new FileReader(file));
           
           // init the temp line
           String temp = "";
           
           // read the file
           while ((temp = reader.readLine()) != null) {
               buffer.append(temp).append("\n");
           }
           
           if (WSDebug.isEnabled()) 
               WSDebug.notify(clazz, "read string: " + buffer.toString());
           
           // return the string
           return buffer.toString();
       } catch (IOException e) {
           ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
       }
       
       return null;
    }
    
    /**
     * Writes the supplied string to a file overwriting the previous contents
     * 
     * @param file the file to write to
     * @param the new file contents
     */
    public static void writeFile(File file, String contents) {
        try {
            // create a writer and write the contents
            new FileOutputStream(file).write(contents.getBytes());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }
}