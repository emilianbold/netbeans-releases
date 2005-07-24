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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.jar.JarFile;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
        assertNull(info);
    }
    
    public void testFindLocalizedBundleInfoFromBinaryModule() throws Exception {
        File apisupportF = file("nbbuild/netbeans/ide6/modules/org-netbeans-modules-apisupport-project.jar");
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
    
    public void testLoadProperties() throws Exception {
        File props = file(getWorkDir(), "testing.properties");
        OutputStream propsOS = new FileOutputStream(props);
        PrintWriter pw = new PrintWriter(propsOS);
        try {
            pw.println("property1=some value");
            pw.println("property2=other value");
        } finally {
            pw.close();
        }
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(props));
        assertEquals("property1", "some value", ep.getProperty("property1"));
        assertEquals("property2", "other value", ep.getProperty("property2"));
        try {
            File notFile = file(getWorkDir(), "i_am_not_file");
            notFile.mkdir();
            Util.loadProperties(FileUtil.toFileObject(notFile));
            fail("FileNotFoundException should be thrown");
        } catch (FileNotFoundException fnfe) {
            // fine expected exception has been thrown
        }
    }
    
    public void testStoreProperties() throws Exception {
        FileObject propsFO = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "testing.properties");
        EditableProperties props = Util.loadProperties(propsFO);
        assertTrue("empty props", props.isEmpty());
        props.setProperty("property1", "some value");
        Util.storeProperties(propsFO, props);
        
        BufferedReader reader = new BufferedReader(
                new FileReader(file(getWorkDir(), "testing.properties")));
        try {
            assertEquals("stored property", "property1=some value", reader.readLine());
        } finally {
            reader.close();
        }
    }
    
    // XXX testLoadManifest()
    // XXX testStoreManifest()
}
