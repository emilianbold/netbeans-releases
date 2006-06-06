/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
