/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.indicators.graph;

import org.netbeans.modules.dlight.indicators.graph.CyclicArray;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for CyclicArray
 * @author Vladimir Kvashin
 */
public class CyclicArrayTest {

    public CyclicArrayTest() {
    }

    @Test
    public void testCtor() {
        int initialCapacity = 8;
        CyclicArray<Integer>  array = new CyclicArray<Integer>(initialCapacity);
        assertEquals(0, array.size());
    }

    @Test
    public void testAddAndSize() {
        System.err.printf("\ntestAddAndSize\n");
        int initialCapacity = 8;
        CyclicArray<Integer>  array = new CyclicArray<Integer>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            array.add(i);
            System.err.printf("%s\n", array);
            assertEquals("Array: " + array, i+1, array.size());
        }
        array.add(100);
        System.err.printf("%s\n", array);
        array.add(200);
        System.err.printf("%s\n", array);
        assertEquals(initialCapacity, array.size());
    }

    @Test
    public void testGet() {
        System.err.printf("\ntestGet\n");
        int initialCapacity = 8;
        CyclicArray<Integer>  array = new CyclicArray<Integer>(initialCapacity);
        for (int i = 0; i < initialCapacity*3; i++) {
            array.add(i);
            System.err.printf("%s\n", array);
            int index = (i < initialCapacity) ? i : initialCapacity-1;
            Integer v = array.get(index);
            assertEquals(String.format("Array(%d) ", index) + array, (Object) i, v);
        }
    }

    @Test
    public void testSet() {
        System.err.printf("\ntestSet\n");
        int initialCapacity = 10;
        CyclicArray<Integer>  array = new CyclicArray<Integer>(initialCapacity);
        for (int i = 0; i < initialCapacity*3/2; i++) {
            array.add(i);
        }
        for (int i = 0; i < array.size(); i++) {
            Integer value = Integer.valueOf((int) (Math.random() * Integer.MAX_VALUE));
            array.set(i, value);
            assertEquals(String.format("Array(%d) ", i) + array, value, array.get(i));
        }
    }

    @Test
    public void testAreEqualAndEnsureCapacity() {
        System.err.printf("\ntestAreEqualAndEnsureCapacity\n");
        int initialCapacity = 10;
        CyclicArray<Integer>  a1 = new CyclicArray<Integer>(initialCapacity);
        CyclicArray<Integer>  a2 = new CyclicArray<Integer>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            a1.add(i);
            a2.add(i);
        }
        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", a1, a2), CyclicArray.<Integer>areEqual(a1, a2));

        a2.ensureCapacity(initialCapacity/2);
        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", a1, a2), CyclicArray.<Integer>areEqual(a1, a2));
        a1.add(500);
        a2.add(500);
        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", a1, a2), CyclicArray.<Integer>areEqual(a1, a2));

        a2.ensureCapacity(initialCapacity*2);
        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", a1, a2), CyclicArray.<Integer>areEqual(a1, a2));
        a1.add(1000);
        a2.add(1000);
        for (int i = 1; i < initialCapacity-2; i++) {
            Integer v1 = a1.get(a1.size()-i);
            Integer v2 = a2.get(a2.size()-i);
            assertEquals(v1, v2);
        }
    }

    @Test
    public void testSetCapacity() {
        System.err.printf("\ntestSetCapacity\n");

        int lesserCapacity = 5;
        CyclicArray<Integer>  lesserArray = new CyclicArray<Integer>(lesserCapacity);
        for (int i = 0; i < lesserCapacity; i++) {
            lesserArray.add(i);
        }

        CyclicArray<Integer>  array2compare = new CyclicArray<Integer>(lesserCapacity);

        int biggerCapacity = 10;
        CyclicArray<Integer>  biggerArray = new CyclicArray<Integer>(biggerCapacity);
        for (int i = 0; i < biggerCapacity; i++) {
            biggerArray.add(i);
            array2compare.add(i);
        }


        assertFalse(String.format("Arrays: \n\t%s \n\t%s\n", biggerArray, lesserArray), CyclicArray.<Integer>areEqual(biggerArray, lesserArray));

        biggerArray.setCapacity(lesserCapacity);

        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", biggerArray, array2compare), CyclicArray.<Integer>areEqual(biggerArray, array2compare));

        biggerArray.add(500);
        array2compare.add(500);
        assertTrue(String.format("Arrays: \n\t%s \n\t%s\n", biggerArray, array2compare), CyclicArray.<Integer>areEqual(biggerArray, array2compare));

    }
}

