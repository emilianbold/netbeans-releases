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

package org.netbeans.modules.java.freeform;

import org.netbeans.api.java.queries.SourceLevelQuery;

import org.netbeans.modules.ant.freeform.TestBase;

/**
 * Test functionality of source level definitions in FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class SourceLevelQueryImplTest extends TestBase {

    public SourceLevelQueryImplTest(String name) {
        super(name);
    }
    
    public void testSourceLevel() throws Exception {
        assertEquals("correct source level for MyApp.java", "1.4", SourceLevelQuery.getSourceLevel(myAppJava));
        assertEquals("correct source level for SpecialTask.java", "1.4", SourceLevelQuery.getSourceLevel(specialTaskJava));
        assertEquals("no source level for build.properties", null, SourceLevelQuery.getSourceLevel(buildProperties));
    }
    
}
