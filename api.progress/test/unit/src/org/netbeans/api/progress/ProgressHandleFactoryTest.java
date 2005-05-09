/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.api.progress;

import junit.framework.*;
import org.netbeans.progress.module.InternalHandle;
import org.openide.util.Cancellable;


/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class ProgressHandleFactoryTest extends TestCase {
    
    public ProgressHandleFactoryTest(String testName) {
        super(testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(ProgressHandleFactoryTest.class);
        
        return suite;
    }

    /**
     * Test of createHandle method, of class org.netbeans.progress.api.ProgressHandleFactory.
     */
    public void testCreateHandle() {
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("task 1");
        InternalHandle internal = handle.getInternalHandle();
        assertEquals("task 1", internal.getDisplayName());
        assertFalse(internal.isAllowCancel());
        assertFalse(internal.isCustomPlaced());
        assertEquals(InternalHandle.STATE_INITIALIZED, internal.getState());
        
        handle = ProgressHandleFactory.createHandle("task 2", new TestCancel());
        internal = handle.getInternalHandle();
        assertEquals("task 2", internal.getDisplayName());
        assertTrue(internal.isAllowCancel());
        assertFalse(internal.isCustomPlaced());
        assertEquals(InternalHandle.STATE_INITIALIZED, internal.getState());
        
    }

    
    private static class TestCancel implements Cancellable {
        public boolean cancel() {
            return true;
        }
        
    }
    
}
