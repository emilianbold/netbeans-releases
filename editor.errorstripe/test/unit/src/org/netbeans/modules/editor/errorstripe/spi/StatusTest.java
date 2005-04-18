/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe.spi;

import junit.framework.*;
import java.awt.Color;
import java.text.MessageFormat;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class StatusTest extends TestCase {
    
    public StatusTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StatusTest.class);
        
        return suite;
    }

    /**
     * Test of getStatus method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testGetStatus() {
    }

    /**
     * Test of compareTo method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testCompareTo() {
    }

    /**
     * Test of equals method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testEquals() {
    }

    /**
     * Test of hashCode method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testHashCode() {
    }

    /**
     * Test of getCompoundStatus method, of class org.netbeans.modules.editor.errorstripe.spi.Status.
     */
    public void testGetCompoundStatus() {
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_WARNING));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_ERROR));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_ERROR));
        
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_OK));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_OK));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_WARNING));
        
        assertEquals(Status.STATUS_OK, Status.getCompoundStatus(Status.STATUS_OK, Status.STATUS_OK));
        assertEquals(Status.STATUS_WARNING, Status.getCompoundStatus(Status.STATUS_WARNING, Status.STATUS_WARNING));
        assertEquals(Status.STATUS_ERROR, Status.getCompoundStatus(Status.STATUS_ERROR, Status.STATUS_ERROR));
    }

}
