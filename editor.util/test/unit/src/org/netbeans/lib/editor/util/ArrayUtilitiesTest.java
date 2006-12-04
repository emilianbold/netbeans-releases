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

package org.netbeans.lib.editor.util;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class ArrayUtilitiesTest extends NbTestCase {

    public ArrayUtilitiesTest(String testName) {
        super(testName);
    }

    public void testUnmodifiableList() throws Exception {
        String[] arr = new String[] { "haf", "cau", "test" };
        List<String> l = ArrayUtilities.unmodifiableList(arr);
        assertEquals("haf", l.get(0));
        assertEquals("cau", l.get(1));
        assertEquals("test", l.get(2));
        try {
            l.add("no");
            fail("Modifiable!");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        assertEquals(3, l.size());
        Object a[] = l.toArray();
        assertTrue(Arrays.equals(arr, a));
        a = l.toArray(new String[2]);
        assertTrue(Arrays.equals(arr, a));
        
    }
    
}
