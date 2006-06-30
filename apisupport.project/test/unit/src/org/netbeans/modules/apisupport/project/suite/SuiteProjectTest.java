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

package org.netbeans.modules.apisupport.project.suite;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.BasicBrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

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
    
    public void testSuiteEvaluatorReturnsUpToDateValues_67314() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        PropertyEvaluator eval = suite1.getEvaluator();
        TestBase.generateSuiteComponent(suite1, "module1");
        
        EditableProperties suiteEP = Util.loadProperties(suite1.getProjectDirectory().getFileObject("nbproject/project.properties"));
        assertEquals("modules property", "${project.org.example.module1}", suiteEP.getProperty("modules"));
        assertEquals("project.org.example.module1 property", "module1", suiteEP.getProperty("project.org.example.module1"));
        
        assertEquals("up-to-date 'modules' property from suite evaluator", "module1", eval.getProperty("modules"));
    }
    
    public void testPlatformPropertiesFromEvaluatorAreUpToDate() throws Exception {
        final SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite1", "custom");
        PropertyEvaluator eval = suite.getEvaluator();
        assertEquals("custom", eval.getProperty("nbplatform.active"));
        assertEquals(NbPlatform.getPlatformByID("custom").getDestDir(), suite.getHelper().resolveFile(eval.getProperty("netbeans.dest.dir")));
        
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                // simulate change (e.g. through suite properties)
                FileObject plafProps = suite.getProjectDirectory().getFileObject("nbproject/platform.properties");
                EditableProperties ep = Util.loadProperties(plafProps);
                ep.setProperty("nbplatform.active", "default");
                Util.storeProperties(plafProps, ep);
                return null;
            }
        });
        
        assertEquals("nbplatform.active change took effect", "default", eval.getProperty("nbplatform.active"));
        assertEquals("#67628: netbeans.dest.dir change did as well", NbPlatform.getDefaultPlatform().getDestDir(), suite.getHelper().resolveFile(eval.getProperty("netbeans.dest.dir")));
    }
    
    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openSuite(final Project p) {
        SuiteProject.OpenedHook hook = (SuiteProject.OpenedHook) p.getLookup().lookup(SuiteProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
    }
    
}
