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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.jar.JarFile;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.openide.filesystems.FileObject;

/**
 * Tests {@link Util}.
 *
 * @author Martin Krauskopf
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    public void testNormalizeCNB() throws Exception {
        assertEquals("space test", "spacetest", Util.normalizeCNB("space test"));
        assertEquals("slash test", "slashtest", Util.normalizeCNB("slash\\test"));
        assertEquals("lowercase test", "org.capital.test", Util.normalizeCNB("org.Capital.test"));
        assertEquals("dot-space test", "org.example.package", Util.normalizeCNB("org...example   ... package..."));
        assertEquals("org.example.hmmmm.misc.test339", Util.normalizeCNB("org.example.hmMMm.misc. TEst3*3=9"));
    }
    
    public void testFindLocalizedBundleInfoFromNetBeansOrgModule() throws Exception {
        FileObject dir = nbroot.getFileObject("apisupport/project");
        assertNotNull("have apisupport/project checked out", dir);
        NbModuleProject apisupport = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(
                apisupport.getSourceDirectory(), apisupport.getManifest());
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory() throws Exception {
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(file("apisupport/project"));
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory1() throws Exception {
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(file(extexamplesF, "suite3/dummy-project"));
        assertNotNull(info);
    }
    
    public void testFindLocalizedBundleInfoFromBinaryModule() throws Exception {
        File apisupportF = file("nbbuild/netbeans/ide5/modules/org-netbeans-modules-apisupport-project.jar");
        JarFile apisupportJar = new JarFile(apisupportF);
        LocalizedBundleInfo info = Util.findLocalizedBundleInfo(apisupportJar);
        assertApiSupportInfo(info);
    }
    
    private void assertApiSupportInfo(LocalizedBundleInfo info) {
        assertNotNull("info loaded", info);
        // XXX ignore this for now, but be careful when editing the module's properties :)
        assertEquals("display name", "NetBeans Module Projects", info.getDisplayName());
        assertEquals("category", "Developing NetBeans", info.getCategory());
        assertEquals("short description", "Defines an Ant-based project type for NetBeans modules.", info.getShortDescription());
        assertEquals("long description", "Defines a project type for NetBeans " +
                "modules, useful for developing plug-in extensions to NetBeans. " +
                "Provides the logical view for modules, supplies the classpath " +
                "used for code completion, integrates with the NetBeans build " +
                "system (using Ant), etc.", info.getLongDescription());
    }
}
