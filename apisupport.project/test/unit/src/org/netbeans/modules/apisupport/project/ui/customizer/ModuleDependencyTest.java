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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.text.Collator;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ModuleDependencyTest extends TestBase {
    
    public ModuleDependencyTest(String testName) {
        super(testName);
    }
    
    public void testCompareTo() throws Exception {
        NbModuleProject module = TestBase.generateStandaloneModule(getWorkDir(), "module");
        ModuleList ml = ModuleList.getModuleList(FileUtil.toFile(module.getProjectDirectory()));
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
