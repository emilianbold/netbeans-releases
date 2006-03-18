/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.text.Collator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ModuleDependencyTest extends TestBase {
    
    public ModuleDependencyTest(String testName) {
        super(testName);
    }
    
    public void testHashCodeAndEqualsAndCompareTo() throws Exception {
        NbModuleProject module = generateStandaloneModule("module");
        ModuleList ml = module.getModuleList();
        ModuleEntry antME = ml.getEntry("org.apache.tools.ant.module");
        ModuleDependency d1 = new ModuleDependency(antME);
        ModuleDependency sameAsD1 = new ModuleDependency(antME);
        ModuleDependency alsoSameAsD1 = new ModuleDependency(antME, antME.getReleaseVersion(), antME.getSpecificationVersion(), true, false);
        ModuleDependency d2 = new ModuleDependency(antME, "0-1", null, true, false);
        ModuleDependency d3 = new ModuleDependency(antME, null, null, true, false);
        ModuleDependency d4 = new ModuleDependency(antME, antME.getReleaseVersion(), null, true, true);
        ModuleDependency d5 = new ModuleDependency(antME, antME.getReleaseVersion(), null, true, false);
        
        // test hash code and equals
        Set/*<ModuleDependency>*/ set = new HashSet();
        Set/*<ModuleDependency>*/ sorted = new TreeSet();
        set.add(d1);
        sorted.add(d1);
        assertFalse("already there", set.add(sameAsD1));
        assertFalse("already there", sorted.add(sameAsD1));
        assertFalse("already there", set.add(alsoSameAsD1));
        assertFalse("already there", sorted.add(alsoSameAsD1));
        assertTrue("is not there yet", set.add(d2));
        assertTrue("is not there yet", sorted.add(d2));
        assertTrue("is not there yet", set.add(d3));
        assertTrue("is not there yet", sorted.add(d3));
        assertTrue("is not there yet", set.add(d4));
        assertTrue("is not there yet", sorted.add(d4));
        assertTrue("is not there yet", set.add(d5));
        assertTrue("is not there yet", sorted.add(d5));
        
        ModuleDependency[] expectedOrder = new ModuleDependency[] {
            d3, d2, d5, d4, d1
        };
        Iterator it = sorted.iterator();
        for (int i = 0; i < expectedOrder.length; i++) {
            assertSame("expected order", expectedOrder[i], it.next());
        }
        assertFalse("sanity check", it.hasNext());
    }
    
    public void testLocalizedNameComparator() throws Exception {
        NbModuleProject module = generateStandaloneModule("module");
        ModuleList ml = module.getModuleList();
        ModuleDependency[] deps = new ModuleDependency[] {
            new ModuleDependency(ml.getEntry("org.apache.tools.ant.module")),
            new ModuleDependency(ml.getEntry("org.openide.loaders")),
            new ModuleDependency(ml.getEntry("org.apache.tools.ant.module")),
            new ModuleDependency(ml.getEntry("org.openide.io")),
            new ModuleDependency(ml.getEntry("org.jdesktop.layout")),
            new ModuleDependency(ml.getEntry("org.openide.filesystems")),
            new ModuleDependency(ml.getEntry("org.openide.execution")),
        };
        
        for (int i = 0; i < deps.length; i++) {
            for (int j = 0; j < deps.length; j++) {
                int locNameResult = Collator.getInstance().compare(
                        deps[i].getModuleEntry().getLocalizedName(),
                        deps[j].getModuleEntry().getLocalizedName());
                int realResult = ModuleDependency.LOCALIZED_NAME_COMPARATOR.compare(deps[i], deps[j]);
                assertTrue("ordering works: " + deps[i] + " <--> " + deps[j],
                        locNameResult > 0 ? realResult > 0 :
                            (locNameResult == 0 ? realResult == 0 : realResult < 0));
//                (int) Math.signum(locNameResult), (int) Math.signum(realResult));
            }
        }
    }
    
    public void testSpecVersionBaseSourceEntries() throws Exception { // #72463
        SuiteProject suite = generateSuite("suite");
        NbModuleProject p = TestBase.generateSuiteComponent(suite, "module");
        ModuleList ml = ModuleList.getModuleList(FileUtil.toFile(p.getProjectDirectory()));
        ModuleEntry e = ml.getEntry("org.example.module");
        assertNotNull("have entry", e);
        ModuleDependency dep = new ModuleDependency(e);
        assertEquals("right initial spec vers from manifest", "1.0", dep.getSpecificationVersion());
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.1.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        Util.storeManifest(p.getManifestFile(), em);
        ProjectManager.getDefault().saveProject(p);
        dep = new ModuleDependency(e);
        assertEquals("right spec.version.base", "1.1", dep.getSpecificationVersion());
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.2.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        dep = new ModuleDependency(e);
        assertEquals("right modified spec.version.base", "1.2", dep.getSpecificationVersion());
        dep = new ModuleDependency(e, null, "1.0", true, false);
        assertEquals("right explicit spec vers", "1.0", dep.getSpecificationVersion());
    }
    
    public void testAppropriateDefaultCompileDependency() throws Exception { // #73666
        NbModuleProject p = generateStandaloneModule("module");
        ModuleList ml = ModuleList.getModuleList(FileUtil.toFile(p.getProjectDirectory()));
        ModuleDependency d = new ModuleDependency(ml.getEntry("org.example.module"));
        assertFalse("no public packages -> no compile dependency by default", d.hasCompileDependency());
    }
    
}
