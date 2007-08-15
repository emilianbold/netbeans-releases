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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.junit;

import java.util.Comparator;
import org.netbeans.api.project.libraries.Library;

/**
 * Comparator of JUnit libraries - compares versions of JUnit libraries.
 *
 * @author  Marian Petras
 */
final class JUnitLibraryComparator implements Comparator<Library> {

    public int compare(Library l1, Library l2) {
        String name1 = l1.getName().toLowerCase();
        String name2 = l2.getName().toLowerCase();

        if (name1.equals(name2)) {
            return 0;
        } else if (name1.equals("junit")) {                             //NOI18N
            return -1;
        } else if (name2.equals("junit")) {                             //NOI18N
            return 1;
        }

        final String[] parts1 = name1.substring(5).split("_|\\W");      //NOI18N
        final String[] parts2 = name2.substring(5).split("_|\\W");      //NOI18N
        final int min = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < min; i++) {
            int partCmp = parts1[i].compareTo(parts2[i]);
            if (partCmp != 0) {
                return partCmp;
            }
        }
        return parts2.length - parts1.length;
    }

}