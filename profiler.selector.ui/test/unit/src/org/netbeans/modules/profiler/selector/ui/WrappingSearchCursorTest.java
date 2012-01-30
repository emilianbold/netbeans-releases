/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.selector.ui;

import java.util.Arrays;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Bachorik
 */
public class WrappingSearchCursorTest {
    private static class Empty extends WrappingSearchCursor<String> {
        public Empty() {
            super("Empty");
        }
        
        @Override
        protected String getItem(int slotIndex, int itemIndex) {
            return null;
        }

        @Override
        protected int getSlotSize(int slotIndex) {
            return 0;
        }

        @Override
        protected int getSlotsNumber() {
            return 0;
        }
    }
    
    private static class SingleSlot extends WrappingSearchCursor<Object> {
        private List items = null;
        private int itemCnt;
        
        public SingleSlot(int items) {
            super("SingleSlot");
            itemCnt = items;
        }
        
        @Override
        protected Object getItem(int slotIndex, int itemIndex) {
            assertEquals(0, slotIndex);
            return items.get(itemIndex);
        }

        @Override
        protected int getSlotSize(int slotIndex) {
            assertEquals(0, slotIndex);
            if (items == null) {
                Object[] a = new Object[itemCnt];
                for(int i=1;i<=itemCnt;i++) {
                    a[i-1] = i;
                }
                items = Arrays.asList(a);
            }
            return items.size();
        }

        @Override
        protected int getSlotsNumber() {
            return 1;
        }
    }
    
    private static class MultiSlot extends WrappingSearchCursor<Object> {
        private int slots;
        private int[] slotsizes;
        private Object[][] items;
        
        public MultiSlot(int slots, int[] slotsizes) {
            super("MultiSlot");
            this.slots = slots;
            this.slotsizes = slotsizes;
            this.items = new Object[slots][];
        }

        @Override
        protected Object getItem(int slotIndex, int itemIndex) {
            initSlot(slotIndex);
            
            return items[slotIndex][itemIndex];
        }

        @Override
        protected int getSlotSize(int slotIndex) {
            initSlot(slotIndex);
            
            return items[slotIndex].length;
        }

        @Override
        protected int getSlotsNumber() {
            return slots;
        }
        
        private void initSlot(int slotIndex) {
            Object[] slot = items[slotIndex];
            if (slot == null) {
                slot = new Object[slotsizes[slotIndex]];
                for(int i=0;i<slot.length;i++) {
                    slot[i] = "s" + slotIndex + "i" + i;
                }
                items[slotIndex] = slot;
            }
        }
    }
    
    public WrappingSearchCursorTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of forward method on empty results
     */
    @Test
    public void testForwardEmpty() {
        System.out.println("forward (empty)");
        WrappingSearchCursor instance = new Empty();
        Object expResult = null;
        Object result = instance.forward();
        assertEquals(expResult, result);
    }

    /**
     * Test of back method on empty results
     */
    @Test
    public void testBackEmpty() {
        System.out.println("back (empty)");
        WrappingSearchCursor instance = new Empty();
        Object expResult = null;
        Object result = instance.back();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of forward method on single slot results
     */
    @Test
    public void testForwardSingleSlot() {
        System.out.println("forward (single slot)");
        int cnt = 5;
        WrappingSearchCursor instance = new SingleSlot(cnt);
        for(int i=0;i<cnt * 2;i++) { // iterating twice over the slot to make sure wrapping works correctly
            assertEquals((i % cnt) + 1, instance.forward());
        }
    }
    
    /**
     * Test of back method on single slot results
     */
    @Test
    public void testBackSingleSlot() {
        System.out.println("back (single slot)");
        int cnt = 5;
        WrappingSearchCursor instance = new SingleSlot(cnt);
        for(int i=0;i<cnt * 2;i++) { // iterating twice over the slot to make sure wrapping works correctly
            assertEquals(cnt - (i % cnt), instance.back());
        }
    }
    
    /**
     * Test of forward method on multi slot results
     */
    @Test
    public void testForwardMultiSlot() {
        System.out.println("forward (multi slot)");
        int slotcnt = 3;
        int[] itemcnts = new int[]{3,1,6};
        int cnt = 10;
        
        WrappingSearchCursor instance = new MultiSlot(slotcnt, itemcnts);
        String rslt = "s0i0s0i1s0i2s1i0s2i0s2i1s2i2s2i3s2i4s2i5s0i0s0i1s0i2s1i0s2i0s2i1s2i2s2i3s2i4s2i5";
        
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<cnt * 2;i++) { // iterating twice over the slot to make sure wrapping works correctly
            sb.append(instance.forward());
        }
        
        assertEquals(rslt, sb.toString());
    }
    
    /**
     * Test of back method on multi slot results
     */
    @Test
    public void testBackMultiSlot() {
        System.out.println("back (multi slot)");
        int slotcnt = 3;
        int[] itemcnts = new int[]{3,1,6};
        int cnt = 10;
        
        WrappingSearchCursor instance = new MultiSlot(slotcnt, itemcnts);
        String rslt = "s0i0s0i1s0i2s1i0s2i0s2i1s2i2s2i3s2i4s2i5s0i0s0i1s0i2s1i0s2i0s2i1s2i2s2i3s2i4s2i5";
        
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<cnt * 2;i++) { // iterating twice over the slot to make sure wrapping works correctly
            sb.insert(0, instance.back());
        }
        
        assertEquals(rslt, sb.toString());
    }
    
    /**
     * Test of forward/back method mix on multi slot results
     */
    @Test
    public void testForwBackMultiSlot() {
        System.out.println("forward/backward (multi slot)");
        int slotcnt = 3;
        int[] itemcnts = new int[]{2,1,6};
        
        WrappingSearchCursor instance = new MultiSlot(slotcnt, itemcnts);
        String rslt = "s0i0s0i1s1i0s2i0s1i0s0i1s0i0s2i5s2i4";
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(instance.forward());
        sb.append(instance.forward());
        sb.append(instance.forward());
        sb.append(instance.forward());
        sb.append(instance.back());
        sb.append(instance.back());
        sb.append(instance.back());
        sb.append(instance.back());
        sb.append(instance.back());
        
        assertEquals(rslt, sb.toString());
    }
}
