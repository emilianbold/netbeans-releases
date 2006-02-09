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
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;

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
    
}
