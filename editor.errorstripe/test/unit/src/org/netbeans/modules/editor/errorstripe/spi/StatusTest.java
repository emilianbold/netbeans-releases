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

package org.netbeans.modules.editor.errorstripe.spi;

import junit.framework.TestCase;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class StatusTest extends TestCase {

    public StatusTest(String testName) {
        super(testName);
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
