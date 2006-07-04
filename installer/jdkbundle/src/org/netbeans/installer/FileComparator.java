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

package org.netbeans.installer;

import java.util.Comparator;
import java.io.*;
import java.util.Arrays;

public class FileComparator implements Comparator, FileFilter {
    
    //sorts in a way to make the most recent file as the first one in the list
    public int compare(Object a, Object b) {
        long a_mod = ((File) a).lastModified();
        long b_mod = ((File) b).lastModified();
        if (a_mod > b_mod) return -1;
        else if (a_mod == b_mod) return 0;
        else return 1;
    }
    
    public boolean equals(Object a) {
        boolean result = false;
        try {
            File f1 = File.createTempFile("comp", "txt");
            File f2 = File.createTempFile("comp2", "txt");
            Comparator c = (Comparator) a;
            if ((c.compare(f1, f2) == 1) &&
            (c.compare(f2, f1) == -1) &&
            (c.compare( f1, f1) == 0))
                result = true;
            f1.delete();
            f2.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    public File getMostRecentFile(File parent) {
        File[] list = parent.listFiles(this);
        if ((list == null) || (list.length == 0)) return parent;
        Arrays.sort(list, this);
        return getMostRecentFile(list[0]);
    }
    
    public File getLeastRecentFile(File parent) {
        File[] list = parent.listFiles(this);
        if ((list == null) || (list.length == 0)) return parent;
        Arrays.sort(list, this);
        return getLeastRecentFile(list[list.length -1]);
    }
    
    /**
     * Tests whether or not the specified abstract pathname should be
     * included in a pathname list. If lastModified() returns 0, omit the file.
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File pathname) {
        if (pathname.lastModified() == 0) return false;
        return true;
    }
    
    public static void main(String args[]) {
        String path;
        if ((args == null) || (args.length == 0)) path = "/home/movadya/temp";
        else path = args[0];
        //System.out.println("Dir -> " + path);
        FileComparator fc = new FileComparator();
        File dir = new File(path);
        File f1 = fc.getMostRecentFile(dir);
        File f2 = fc.getLeastRecentFile(dir);
        //System.out.println("Least recent--> " + f2.getAbsolutePath() + " " +f2.lastModified());
        //System.out.println("Most recent--> " + f1.getAbsolutePath() + " " +f1.lastModified());
    }
    
}
