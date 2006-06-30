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
        if (home.endsWith("\\") || home.endsWith("/")) // NOI18N
            home = home.substring(0, home.length() - 1);
        if (relative.startsWith("\\") || relative.startsWith("/")) // NOI18N
            relative = relative.substring(1);
        
        path = home + separator + relative;
        if (separator.equals("/")) // NOI18N
            path = path.replace('\\', separator.charAt(0));
        else if (separator.equals("\\")) // NOI18N
            path = path.replace('/', separator.charAt(0));
        
        return path;
    }
}
