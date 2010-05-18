/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
