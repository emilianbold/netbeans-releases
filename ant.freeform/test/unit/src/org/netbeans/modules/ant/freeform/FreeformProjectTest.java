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

import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

// XXX testAuxiliaryConfiguration
// XXX testCacheProvider
// XXX testAntArtifact

/**
 * Test functionality of FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class FreeformProjectTest extends TestBase {
    
    public FreeformProjectTest(String name) {
        super(name);
    }
    
    public void testProjectInformation() throws Exception {
        ProjectInformation info = ProjectUtils.getInformation(simple);
        assertEquals("correct name", "Simple_Freeform_Project", info.getName());
        assertEquals("same display name", "Simple Freeform Project", info.getDisplayName());
        // XXX test changes
    }
    
}
