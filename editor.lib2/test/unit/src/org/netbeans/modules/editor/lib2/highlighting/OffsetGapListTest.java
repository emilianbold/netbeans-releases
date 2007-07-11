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
package org.netbeans.modules.editor.lib2.highlighting;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class OffsetGapListTest extends NbTestCase {

    public OffsetGapListTest(String name) {
        super(name);
    }

    public void testMovingZeroOffset() {
        OffsetGapList<OffsetGapList.Offset> ogl = new OffsetGapList<OffsetGapList.Offset>();
        OffsetGapList.Offset offsetA = new OffsetGapList.Offset(0);
        OffsetGapList.Offset offsetB = new OffsetGapList.Offset(100);
        
        ogl.add(offsetA);
        ogl.add(offsetB);
        assertEquals("Wrong initial offset A", 0, offsetA.getOffset());
        assertEquals("Wrong initial offset B", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 10); // simulate insert at the zero offset
        assertEquals("Offset A should have been moved", 10, offsetA.getOffset());
        assertEquals("Offset B should have been moved", 110, offsetB.getOffset());
    }
    
    public void testFixedZeroOffset() {
        OffsetGapList<OffsetGapList.Offset> ogl = new OffsetGapList<OffsetGapList.Offset>(true);
        OffsetGapList.Offset offsetA = new OffsetGapList.Offset(0);
        OffsetGapList.Offset offsetB = new OffsetGapList.Offset(100);
        
        ogl.add(offsetA);
        ogl.add(offsetB);
        assertEquals("Wrong initial offset A", 0, offsetA.getOffset());
        assertEquals("Wrong initial offset B", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 10); // simulate insert at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved", 110, offsetB.getOffset());
        
        ogl.defaultRemoveUpdate(0, 10); // simulate remove at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved back", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 3); // simulate insert at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved again", 103, offsetB.getOffset());
    }
    
}
