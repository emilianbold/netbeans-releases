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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import junit.framework.TestCase;

/**
 * Test utility method in AnnotationBar
 *
 * @author Petr Kuzel
 */
public class AnnotationBarTest extends TestCase {


    public static void testPreviousRevision() {
        assertNull(AnnotationBar.previousRevision("1.1"));
        assertEquals("1.1", AnnotationBar.previousRevision("1.2"));
        assertEquals("1.2.1.1", AnnotationBar.previousRevision("1.2.1.2"));
        assertEquals("1.199", AnnotationBar.previousRevision("1.200.0.1.1.1"));
    }

}
