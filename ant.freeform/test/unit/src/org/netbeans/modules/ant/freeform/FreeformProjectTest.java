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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Lookup;

// XXX testSourceForBinaryQuery
// XXX testAuxiliaryConfiguration
// XXX testCacheProvider
// XXX testSubprojectProvider
// XXX testLogicalViewItems
// XXX testAntArtifact
// XXX testExternalSourceRoots

/**
 * Test functionality of FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class FreeformProjectTest extends TestBase {
    
    public FreeformProjectTest(String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = simple.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
    }
    
    public void testProjectInformation() throws Exception {
        ProjectInformation info = ProjectUtils.getInformation(simple);
        assertEquals("correct name", "Simple Freeform Project", info.getName());
        assertEquals("same display name", "Simple Freeform Project", info.getDisplayName());
    }
    
    public void testSources() throws Exception {
        Sources s = ProjectUtils.getSources(simple);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("one generic group", 1, groups.length);
        assertEquals("right root folder", simple.getProjectDirectory(), groups[0].getRootFolder());
        assertEquals("right display name", "Simple Freeform Project", groups[0].getDisplayName());
        groups = s.getSourceGroups("java");
        assertEquals("two Java groups", 2, groups.length);
        assertEquals("right root folder #1", simple.getProjectDirectory().getFileObject("src"), groups[0].getRootFolder());
        assertEquals("right display name #1", "Main Sources", groups[0].getDisplayName());
        assertEquals("right root folder #2", simple.getProjectDirectory().getFileObject("antsrc"), groups[1].getRootFolder());
        assertEquals("right display name #2", "Ant Task Sources", groups[1].getDisplayName());
    }
    
}
