/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.util;

/** Utility class to woth with file names. */
public class FileNameUtil {
    
    /** Return absolute path computed from home path and relative path.
     * @param home home directory
     * @param relative the relative part of the file path
     * @return computed absolute path
     */
    public static String computeAbsolutePath(String home, String relative) {
        String path;
        String separator = System.getProperty("file.separator");
        if (home.endsWith("\\") || home.endsWith("/"))
            home = home.substring(0, home.length() - 1);
        if (relative.startsWith("\\") || relative.startsWith("/"))
            relative = relative.substring(1);
        
        path = home + separator + relative;
        if (separator.equals("/"))
            path = path.replace('\\', separator.charAt(0));
        else if (separator.equals("\\"))
            path = path.replace('/', separator.charAt(0));
        
        return path;
    }
}
