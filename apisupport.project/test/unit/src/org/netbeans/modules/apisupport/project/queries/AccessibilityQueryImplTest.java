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

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test {@link AccessibilityQueryImpl}.
 * @author Jesse Glick
 */
public class AccessibilityQueryImplTest extends TestBase {
    
    public AccessibilityQueryImplTest(String name) {
        super(name);
    }
    
    public void testPublicPackages() throws Exception {
        assertEquals(Boolean.TRUE, AccessibilityQuery.isPubliclyAccessible(nbroot.getFileObject("ant/project/src/org/netbeans/spi/project/support/ant")));
        assertEquals(Boolean.FALSE, AccessibilityQuery.isPubliclyAccessible(nbroot.getFileObject("ant/project/src/org/netbeans/modules/project/ant")));
    }
    
    public void testFriendPackages() throws Exception {
        assertEquals(Boolean.TRUE, AccessibilityQuery.isPubliclyAccessible(nbroot.getFileObject("ant/freeform/src/org/netbeans/modules/ant/freeform/spi")));
        assertEquals(Boolean.FALSE, AccessibilityQuery.isPubliclyAccessible(nbroot.getFileObject("ant/freeform/src/org/netbeans/modules/ant/freeform")));
    }
    
    // XXX testSubpackages - would need to generate a new module to test
    
    public void testOtherSourceRoots() throws Exception {
        assertEquals(null, AccessibilityQuery.isPubliclyAccessible(nbroot.getFileObject("ant/src-bridge/org/apache/tools/ant/module/bridge/impl")));
    }
    
}
