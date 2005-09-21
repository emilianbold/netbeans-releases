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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
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
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(getWorkDir(), "suite");
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
        ep.setProperty("app.name", "Jarouskova aplikace");
        FileLock lock = x.lock();
        OutputStream os = x.getOutputStream(lock);
        ep.store(os);
        os.close();
        lock.releaseLock();
        
        
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
        assertTrue("Master jnlp file created", subobj.remove(suite.getProjectDirectory().getFileObject("master.jnlp")));
        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
    }
}
