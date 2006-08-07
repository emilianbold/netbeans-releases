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
package org.openide.modules;


// THIS CLASS OUGHT NOT USE NbBundle NOR org.openide CLASSES
// OUTSIDE OF openide-util.jar! UI AND FILESYSTEM/DATASYSTEM
// INTERACTIONS SHOULD GO ELSEWHERE.
import java.util.*;


/** Utility class representing a specification version.
 * @author Jesse Glick
 * @since 1.24
 */
public final class SpecificationVersion implements Comparable {
    // Might be a bit wasteful of memory, but many SV's are created during
    // startup, so best to not have to reparse them each time!
    // In fact sharing the int arrays might save a bit of memory overall,
    // since it is unusual for a module to be deleted.
    private static final Map<String,int[]> parseCache = new HashMap<String,int[]>(200);
    private final int[] digits;

    /** Parse from string. Must be Dewey-decimal. */
    public SpecificationVersion(String version) throws NumberFormatException {
        synchronized (parseCache) {
            int[] d = (int[]) parseCache.get(version);

            if (d == null) {
                d = parse(version);
                parseCache.put(version.intern(), d);
            }

            digits = d;
        }
    }

    private static int[] parse(String version) throws NumberFormatException {
        StringTokenizer tok = new StringTokenizer(version, ".", true); // NOI18N
        
        int len = tok.countTokens();
        if ((len % 2) == 0) {
            throw new NumberFormatException("Even number of pieces in a spec version: `" + version + "'"); // NOI18N
        }
        int[] digits = new int[len / 2 + 1];
        int i = 0;

        boolean expectingNumber = true;

        while (tok.hasMoreTokens()) {
            if (expectingNumber) {
                expectingNumber = false;

                int piece = Integer.parseInt(tok.nextToken());

                if (piece < 0) {
                    throw new NumberFormatException("Spec version component <0: " + piece); // NOI18N
                }

                digits[i++] = piece;
            } else {
                if (!".".equals(tok.nextToken())) { // NOI18N
                    throw new NumberFormatException("Expected dot in spec version: `" + version + "'"); // NOI18N
                }

                expectingNumber = true;
            }
        }
        return digits;
    }

    /** Perform a Dewey-decimal comparison. */
    public int compareTo(Object o) {
        int[] od = ((SpecificationVersion) o).digits;
        int len1 = digits.length;
        int len2 = od.length;
        int max = Math.max(len1, len2);

        for (int i = 0; i < max; i++) {
            int d1 = ((i < len1) ? digits[i] : 0);
            int d2 = ((i < len2) ? od[i] : 0);

            if (d1 != d2) {
                return d1 - d2;
            }
        }

        return 0;
    }

    /** Overridden to compare contents. */
    public boolean equals(Object o) {
        if (!(o instanceof SpecificationVersion)) {
            return false;
        }

        return Arrays.equals(digits, ((SpecificationVersion) o).digits);
    }

    /** Overridden to hash by contents. */
    public int hashCode() {
        int hash = 925295;
        int len = digits.length;

        for (int i = 0; i < len; i++) {
            hash ^= (digits[i] << i);
        }

        return hash;
    }

    /** String representation (Dewey-decimal). */
    public String toString() {
        StringBuilder buf = new StringBuilder((digits.length * 3) + 1);

        for (int i = 0; i < digits.length; i++) {
            if (i > 0) {
                buf.append('.'); // NOI18N
            }

            buf.append(digits[i]);
        }

        return buf.toString();
    }
}
