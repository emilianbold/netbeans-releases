/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.lookup;

import java.util.Comparator;


/** Implementation of comparator for AbstractLookup.Pair
 *
 * @author  Jaroslav Tulach
 */
final class ALPairComparator implements Comparator {
    public static final Comparator DEFAULT = new ALPairComparator();

    /** Creates a new instance of ALPairComparator */
    private ALPairComparator() {
    }

    /** Compares two items.
    */
    public int compare(Object obj, Object obj1) {
        AbstractLookup.Pair i1 = (AbstractLookup.Pair) obj;
        AbstractLookup.Pair i2 = (AbstractLookup.Pair) obj1;

        int result = i1.getIndex() - i2.getIndex();

        if (result == 0) {
            if (i1 != i2) {
                java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(bs);

                ps.println(
                    "Duplicate pair in tree" + // NOI18N
                    "Pair1: " + i1 + " pair2: " + i2 + " index1: " + i1.getIndex() + " index2: " +
                    i2.getIndex() // NOI18N
                     +" item1: " + i1.getInstance() + " item2: " + i2.getInstance() // NOI18N
                     +" id1: " + Integer.toHexString(System.identityHashCode(i1)) // NOI18N
                     +" id2: " + Integer.toHexString(System.identityHashCode(i2)) // NOI18N
                );

                //                print (ps, false);
                ps.close();

                throw new IllegalStateException(bs.toString());
            }

            return 0;
        }

        return result;
    }
}
