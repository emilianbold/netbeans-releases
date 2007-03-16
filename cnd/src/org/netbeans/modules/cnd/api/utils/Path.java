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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.openide.util.Utilities;

/**
 * Get/Set path. Remembers additions to the path.
 *
 * @author gordonp
 */
public final class Path {
    
    private static ArrayList list = new ArrayList();
    
    static {
        String path = System.getenv("PATH"); // NOI18N
        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, File.pathSeparator); // NOI18N

            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                list.add(dir);
            }
        } else {
            if (Utilities.isUnix()) {
                list.add("/bin"); // NOI18N
                list.add("/usr/bin"); // NOI18N
                list.add("/sbin"); // NOI18N
                list.add("/usr/sbin"); // NOI18N
            } else if (Utilities.isWindows()) {
                list.add("C:/WINDOWS/System32"); // NOI18N
                list.add("C:/WINDOWS"); // NOI18N
                list.add("C:/WINDOWS/System32/WBem"); // NOI18N
            }
        }
        
    }
    
    /**
     * Read the PATH from the environment and make an array from it.
     * 
     * @return A list of all path directories
     */
    public static ArrayList getPathAsList() {
        return list;
    }
    
    /**
     * Return the path with the correct path separator character.
     * This would be named toString() if it weren't a static method.
     * 
     * @return Path as a string (with OS specific directory separators)
     */
    public static String getPathAsString() {
        Iterator<String> iter = list.iterator();
        StringBuffer buf = new StringBuffer();
        
        while (iter.hasNext()) {
            buf.append(File.separatorChar);
            buf.append(iter.next());
        }
        return buf.toString();
    }
    
    /**
     * Replace the current path with this new one. We should validate but currently aren't.
     * 
     * @param newPath A list of directories to use as a replacement path
     */
    public static void setPath(ArrayList newPath) {
        list = newPath;
    }
    
    /**
     * Add a directory to the path.
     * 
     * @param pos Position where dir should be added
     * @param dir New directory to add to path
     * @throws IndexOutOfBoundsException
     */
    public static void add(int pos, String dir) throws IndexOutOfBoundsException {
        list.add(pos, dir);
    }
    
    /**
     * Remove a directory (by index) from the path.
     * 
     * @param pos Position where dir should be added
     * @throws IndexOutOfBoundsException
     */
    public static void remove(int pos) throws IndexOutOfBoundsException {
        list.remove(pos);
    }
}
