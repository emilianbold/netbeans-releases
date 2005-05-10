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

package org.netbeans.modules.ant.freeform;

import java.util.Collections;
import java.util.Set;
import org.netbeans.spi.project.SubprojectProvider;

// XXX testChanges

/**
 * Test {@link Subprojects}.
 * @author Jesse Glick
 */
public class SubprojectsTest extends TestBase {
    
    public SubprojectsTest(String name) {
        super(name);
    }
    
    private SubprojectProvider simpleSubprojects, extsrcrootSubprojects, simple2Subprojects;

    protected void setUp() throws Exception {
        super.setUp();
        simpleSubprojects = (SubprojectProvider) simple.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for simple", simpleSubprojects);
        extsrcrootSubprojects = (SubprojectProvider) extsrcroot.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for extsrcroot", extsrcrootSubprojects);
        simple2Subprojects = (SubprojectProvider) simple2.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have a SubprojectProvider for simple2", simple2Subprojects);
    }
    
    public void testBasicSubprojects() throws Exception {
        Set subprojects = simpleSubprojects.getSubprojects();
        
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        assertEquals("no subprojects for simple", Collections.EMPTY_SET, subprojects);
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        
        subprojects = extsrcrootSubprojects.getSubprojects();
        assertFalse("extsrcroot has simple as a subproject", subprojects.isEmpty());
        assertEquals("extsrcroot has simple as a subproject", Collections.singleton(simple), subprojects);
        assertFalse("extsrcroot has simple as a subproject", subprojects.isEmpty());
        
        subprojects = simple2Subprojects.getSubprojects();
        
        assertTrue("no subprojects for simple", subprojects.isEmpty());
        assertEquals("no subprojects for simple", Collections.EMPTY_SET, subprojects);
        assertTrue("no subprojects for simple", subprojects.isEmpty());
    }
    
}
