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
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import java.util.*;


/**
 * general util class
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class Util {
    /**
     * get sorted list in Descending Order
     *
     * @param unsortedLines
     * @return sorted list in descending order
     */
    public static List sortDescendingOrder(List unsortedLines) {
        List sortedList = new ArrayList();
        int[] numberArray = new int[unsortedLines.size()];

        for (int i = 0; i < unsortedLines.size(); i++) {
            String line = (String) unsortedLines.get(i);
            numberArray[i] = (new Integer(line.substring(0, line.indexOf('>')))).intValue();
        }

        for (int i = numberArray.length - 1; i >= 0; i--) {
            int highest = numberArray[i];
            int highestIndex = i;

            for (int j = 0; j < i; j++) {
                if (numberArray[j] > highest) {
                    highest = numberArray[j];
                    highestIndex = j;
                }
            }

            sortedList.add(unsortedLines.get(highestIndex));
            numberArray[highestIndex] = 0;
        }

        return sortedList;
    }

    /**
     *
     * @param fullPath
     * @return filename only (no path, eg: if fullpath is c:\tmp1\abc.java returns abc.java)
     */
    public static String getNormalizedFileName(String fullPath) {
        if ((fullPath == null) || (fullPath.trim().length() == 0)) {
            throw new IllegalArgumentException("No file name specified");
        }

        String tmpFileName = fullPath;
        int index = fullPath.lastIndexOf('/');

        if (index == -1) {
            index = fullPath.lastIndexOf('\\');
        }

        if (index != -1) {
            tmpFileName = fullPath.substring(index + 1);
        }

        return tmpFileName;
    }

    /**
     *
     * @param range
     * @return a random count (long type) in the given range
     */
    public synchronized static long getRandomCount(long range) {
        Random rand = new Random();
        long randomCount = rand.nextInt((int) range);

        return randomCount;
    }
}
