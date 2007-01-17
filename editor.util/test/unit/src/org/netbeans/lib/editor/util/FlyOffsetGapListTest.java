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

import org.netbeans.junit.NbTestCase;

/**
 * Test of FlyOffsetGapList correctness.
 *
 * @author mmetelka
 */
public class FlyOffsetGapListTest extends NbTestCase {

    public FlyOffsetGapListTest(java.lang.String testName) {
        super(testName);
    }

    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        FOGL fogl = new FOGL(3); // start offset is 3
        assertEquals(3, fogl.elementOrEndOffset(0));

        try {
            fogl.elementOffset(0);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            fogl.elementOrEndOffset(1);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            fogl.elementOrEndOffset(-1);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        fogl.add(new Element(-1, 5));
        fogl.defaultInsertUpdate(3, 5);
        assertEquals(3, fogl.elementOrEndOffset(0));
        assertEquals(8, fogl.elementOrEndOffset(1));
    }
    
    private static final class FOGL extends FlyOffsetGapList<Element> {
        
        private int startOffset;
        
        FOGL(int startOffset) {
            this.startOffset = startOffset;
        }
        
        @Override
        protected int startOffset() {
            return startOffset;
        }

        protected boolean isElementFlyweight(Element elem) {
            return elem.isFlyweight();
        }
        
        protected int elementLength(Element elem) {
            return elem.length();
        }

        protected int elementRawOffset(Element elem) {
            return elem.rawOffset();
        }
        
        protected void setElementRawOffset(Element elem, int rawOffset) {
            elem.setRawOffset(rawOffset);
        }

    }
    
    private static final class Element {
        
        private int rawOffset;
        
        private final int length;
        
        public Element(int rawOffset, int length) {
            this.rawOffset = rawOffset;
            this.length = length;
        }
        
        public int rawOffset() {
            return rawOffset;
        }
        
        public void setRawOffset(int rawOffset) {
            this.rawOffset = rawOffset;
        }
        
        public int length() {
            return length;
        }
        
        public boolean isFlyweight() {
            return (rawOffset == -1);
        }

    }
    
}
