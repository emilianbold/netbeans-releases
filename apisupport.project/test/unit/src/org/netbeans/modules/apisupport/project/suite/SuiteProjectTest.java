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

package org.netbeans.modules.apisupport.project.suite;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.BasicBrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * Test basic {@link SuiteProject} stuff.
 * @author Jesse Glick
 */
public class SuiteProjectTest extends NbTestCase {
    
    public SuiteProjectTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testProjectInformation() throws Exception {
        SuiteProject p = TestBase.generateSuite(getWorkDir(), "Sweet Stuff");
        ProjectInformation i = ProjectUtils.getInformation(p);
        assertEquals("Sweet_Stuff", i.getName());
        assertEquals("Sweet Stuff", i.getDisplayName());
        BasicBrandingModel model = new BasicBrandingModel(new SuiteProperties(p, p.getHelper(), p.getEvaluator(), Collections.EMPTY_SET));
        assertEquals("sweet_stuff", model.getName());
        assertEquals("Sweet Stuff", model.getTitle());
        TestBase.TestPCL l = new TestBase.TestPCL();
        i.addPropertyChangeListener(l);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "sweetness");
        ep.setProperty("app.title", "Sweetness is Now!");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertEquals(new HashSet(Arrays.asList(new String[] {ProjectInformation.PROP_NAME, ProjectInformation.PROP_DISPLAY_NAME})), l.changed);
        assertEquals("Sweet_Stuff", i.getName());
        assertEquals("Sweetness is Now!", i.getDisplayName());
        model = new BasicBrandingModel(new SuiteProperties(p, p.getHelper(), p.getEvaluator(), Collections.EMPTY_SET));
        assertEquals("sweetness", model.getName());
        assertEquals("Sweetness is Now!", model.getTitle());
    }
    
}
