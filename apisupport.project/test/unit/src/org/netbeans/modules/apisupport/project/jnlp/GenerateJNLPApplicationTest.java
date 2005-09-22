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
package org.netbeans.modules.apisupport.project.jnlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGeneratorTest;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Checks JNLP support behaviour.
 * @author Jaroslav Tulach
 */
public class GenerateJNLPApplicationTest extends TestBase {
    
    private SuiteProject suite;
    
    public GenerateJNLPApplicationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        SuiteProjectGeneratorTest.openProject(suite);
        proj.open();
    }
    
    public void testBuildTheJNLPAppWhenAppNamePropIsNotSet() {
        ActionProvider p = (ActionProvider)suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here");
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        p.invokeAction("build-jnlp", suite.getLookup());
        
        org.openide.filesystems.FileObject[] arr = suite.getProjectDirectory().getChildren();
        List subobj = new ArrayList (Arrays.asList(arr));
        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
    }
    
    public void testBuildTheJNLPAppWhenAppNamePropIsSet() throws Exception {
        FileObject x = suite.getProjectDirectory().getFileObject("nbproject/project.properties");
        EditableProperties ep = org.netbeans.modules.apisupport.project.Util.loadProperties(x);
        ep.setProperty("app.name", "fakeapp");
        
        File someJar = createNewJarFile("fake-jnlp-servlet");
        
        StringBuffer exclude = new StringBuffer();
        String sep = "";
        String[] possibleClusters = destDirF.list();
        for (int i = 0; i < possibleClusters.length; i++) {
            if (possibleClusters[i].startsWith("platform")) {
                continue;
            }
            exclude.append(sep);
            exclude.append(possibleClusters[i]);
            sep = ",";
        }
        ep.setProperty("disabled.clusters", exclude.toString());
        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
            "org.openide.compat," +
            "org.netbeans.api.progress," +
            "org.netbeans.core.multiview," +
            "org.openide.util.enumerations" +
            "");
        ep.setProperty("jnlp.servlet.jar", someJar.toString());
        org.netbeans.modules.apisupport.project.Util.storeProperties(x, ep);
        
        ActionProvider p = (ActionProvider)suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here");
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        p.invokeAction("build-jnlp", suite.getLookup());
        
        org.openide.filesystems.FileObject[] arr = suite.getProjectDirectory().getChildren();
        List subobj = new ArrayList (Arrays.asList(arr));
        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
        FileObject master = suite.getProjectDirectory().getFileObject("master.jnlp");
        assertNotNull("Master must be created", master);
        subobj.remove(master);
        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
        assertNotNull("dist created", dist);
        subobj.remove(dist);
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
        
        FileObject war = dist.getFileObject("fakeapp.war");
        assertNotNull("War file created: " + war, war);
        
        File warF = org.openide.filesystems.FileUtil.toFile(war);
        JarFile warJ = new JarFile(warF);
        Enumeration en = warJ.entries();
        int cntJnlp = 0;
        while (en.hasMoreElements()) {
            JarEntry entry = (JarEntry)en.nextElement();
            if (!entry.getName().endsWith(".jnlp")) {
                continue;
            }
            cntJnlp++;

            byte[] data = new byte[(int)entry.getSize()];
            int len = 0;
            InputStream is = warJ.getInputStream(entry);
            for(int pos = 0; pos < data.length; ) {
                int r = is.read(data, pos, data.length - pos);
                pos += r;
                len += r;
            }
            is.close();
            assertEquals("Correct data read: " + entry, data.length, len);
            
            String s = new String(data);
            if (s.indexOf(getWorkDir().getName()) >= 0) {
                fail("Name of work dir in a file, means that there is very likely local reference to a file: " + entry + "\n" + s);
            }
        }
        
        if (cntJnlp == 0) {
            fail("There should be at least one jnlp entry");
        }
    }
    
    private File createNewJarFile (String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) {
                f.createNewFile();
                return f;
            }
        }
    }
}
