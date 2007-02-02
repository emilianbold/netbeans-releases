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

package org.netbeans.lib.lexer;

/**
 * Cache of java.lang.Integer instances.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class IntegerCache {

    // Should cache the same what Integer.valueOf(int) does on 1.5
    private static final int MAX_CACHED_INTEGER = 127;

    private static final Integer[] cache = new Integer[MAX_CACHED_INTEGER + 1];

    public static Integer integer(int i) {
        Integer integer;
        if (i <= MAX_CACHED_INTEGER) {
            integer = cache[i];
            if (integer == null) {
                integer = Integer.valueOf(i); // possibly delegate to global cache
                cache[i] = integer; // may lead to multiple instances but no problem with that
            }

        } else { // cannot cache
            integer = new Integer(i);
        }
        return integer;
    }
    
}
