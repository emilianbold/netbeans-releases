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

package org.openide.util;

import org.netbeans.junit.NbTestCase;

/**
 * @author Jesse Glick
 */
public class Union2Test extends NbTestCase {

    public Union2Test(String name) {
        super(name);
    }

    public void testUnions() throws Exception {
        Union2<Integer,String> union = Union2.createFirst(3);
        assertEquals(3, union.first().intValue());
        try {
            union.second();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertTrue(union.hasFirst());
        assertFalse(union.hasSecond());
        assertEquals("3", union.toString());
        assertTrue(union.equals(Union2.createFirst(3)));
        assertFalse(union.equals(Union2.createFirst(4)));
        assertEquals(union.hashCode(), Union2.createFirst(3).hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        int i = union.clone().first();
        assertEquals(3, i);
        // Second type now.
        union = Union2.createSecond("hello");
        try {
            union.first();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertEquals("hello", union.second());
        assertFalse(union.hasFirst());
        assertTrue(union.hasSecond());
        assertEquals("hello", union.toString());
        assertTrue(union.equals(Union2.createSecond("hello")));
        assertFalse(union.equals(Union2.createSecond("there")));
        assertEquals(union.hashCode(), Union2.createSecond("hello").hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        String s = union.clone().second();
        assertEquals("hello", s);
    }

}
