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

/*
 * SimpleDiff.java
 *
 * Created on February 8, 2001, 2:59 PM
 */

package org.netbeans.junit.diff;

import java.io.*;

/**
 *
 * @author  vstejskal, Jan Becicka
 * @version 2.0
 */
public class SimpleDiff extends Object implements Diff {
    
    private static final int BUFSIZE = 1024;
    private final LineDiff lineDiff;
    
    /** Creates new SimpleDiff */
    public SimpleDiff() {
        lineDiff = new LineDiff(false);
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final java.io.File first, final java.io.File second, java.io.File diff)  throws java.io.IOException {
        if (isBinaryFile(first) || isBinaryFile(second)) {
            return binaryCompare(first, second, diff);
        }
        else {
            return textualCompare(first, second, diff);
        }
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final String first, final String second, String diff)  throws java.io.IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    protected boolean isBinaryFile(File file) throws java.io.IOException {
        FileInputStream is = null;
        byte[] buffer = new byte[BUFSIZE];
        is = new FileInputStream(file);
        int bytesRead = is.read(buffer, 0, BUFSIZE);
        if (bytesRead == -1)
            return false;
        for (int i = 0; i != bytesRead; i++) {
            if (buffer[i] < 0)
                return true;
        }
        is.close();
        return false;
    }
    
    protected boolean binaryCompare(final File first, final File second, File diff) throws java.io.IOException {
        if (first.length() != second.length())
            return true;
        
        InputStream fs1 = new BufferedInputStream(new FileInputStream(first));
        InputStream fs2 = new BufferedInputStream(new FileInputStream(second));
        byte[] b1 = new byte[BUFSIZE];
        byte[] b2 = new byte[BUFSIZE];
        
        while (true) {
            int l1 = fs1.read(b1);
            int l2 = fs2.read(b2);
            if (l1 == -1) {
                if (l2 == -1)
                    return false;    // files equal
                else
                    return true;   // files differ in length
            }
            if (l1 != l2)
                return true;    // files differ in length
            if (!java.util.Arrays.equals(b1,b2))
                return true;  // files differ in content
        }
    }
    
    protected boolean textualCompare(final File first, final File second, File diff) throws java.io.IOException {
        return lineDiff.diff(first, second, diff);
    }
}
