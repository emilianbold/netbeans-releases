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
