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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectTest;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * @author Martin Krauskopf
 */
public class ImportBlueprintEarWizardIteratorTest extends NbTestCase {
    
    private static final String CUSTOM_CONTEXT_ROOT = "/my-context-root";
    
    /* Default values. */
    private String name = "Test EnterpriseApplication";
    private String j2eeLevel = "1.5";
    private String warName = "testEA-war";
    private String jarName = "testEA-ejb";
    private String carName = "testEA-app-client";
    private String mainClass = "testEA.app.client.Main";
    private String platformName = null;
    private String sourceLevel = "1.5";
    
    private String serverInstanceID;
    private File prjDirF;
    
    public ImportBlueprintEarWizardIteratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        serverInstanceID = TestUtil.registerSunAppServer(
                this, new Object[] { new SilentDialogDisplayer(), new SimplePlatformProvider() });
        assertTrue("wrong dialog displayer", DialogDisplayer.getDefault() instanceof SilentDialogDisplayer);
        // default project dir
        prjDirF = new File(getWorkDir(), "testEA");
    }
    
    public void testTestableInstantiateBasics() throws Exception {
        generateJ2EEApplication(false);
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeLevel, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null);
        
        FileObject fo = FileUtil.toFileObject(importedDir);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(fo);
        EditableProperties props = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("j2ee.platform was set to 1.5", "1.5", props.getProperty("j2ee.platform")); // #76874
    }
    
    public void testTestableInstantiateWitoutDD() throws Exception {
        FileObject prjDirFO = generateJ2EEApplication(true);
        
        // and Enterprise Application's deployment descriptor
        prjDirFO.getFileObject("src/conf/application.xml").delete();
        
        Map<FileObject, ModuleType> userModules = new HashMap<FileObject, ModuleType>();
        userModules.put(prjDirFO.getFileObject(warName), ModuleType.WEB);
        userModules.put(prjDirFO.getFileObject(jarName), ModuleType.EJB);
        userModules.put(prjDirFO.getFileObject(carName), ModuleType.CLIENT);
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeLevel, importedDir, prjDirF, serverInstanceID, name, userModules, null);
        
        FileObject importedDirFO = FileUtil.toFileObject(importedDir);
        FileObject ddFO = prjDirFO.getFileObject("src/conf/application.xml");
        assertNotNull("deployment descriptor was created", ddFO);
        EarProjectTest.validate(ddFO);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(importedDirFO);
        EarProjectTest.openProject(project);
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        assertSame("three modules", 3, app.getModule().length);
    }
    
    public void testTestableInstantiateWithWebAndEJBAndAC() throws Exception {
        this.j2eeLevel = "1.4";
        FileObject prjDirFO = generateJ2EEApplication(true);
        
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeLevel, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null);
        
        assertNotNull("have a backup copy of application.xml", prjDirFO.getFileObject("src/conf/original_application.xml"));
        assertNotNull("have a backup copy of manifest", prjDirFO.getFileObject("src/conf/original_MANIFEST.MF"));
        FileObject importedDirFO = FileUtil.toFileObject(importedDir);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(importedDirFO);
        EarProjectTest.openProject(project);
        
        FileObject ddFO = project.getAppModule().getDeploymentDescriptor();
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        EarProjectTest.validate(ddFO);
        assertSame("three modules", 3, app.getModule().length);
        NewEarProjectWizardIteratorTest.doTestThatEJBWasAddedToWebAndAC( // #66546 and #74123
                importedDirFO.getFileObject("testEA-war"),
                importedDirFO.getFileObject("testEA-app-client"));
    }
    
    // temporarily(?) turned off
    public void off_testWebContextRootIsSet() throws Exception {
        this.j2eeLevel = "1.4";
        generateJ2EEApplicationWithWeb();
        
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeLevel, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null);
        
        String importedContextRoot = null;
        FileObject ddFO = FileUtil.toFileObject(prjDirF).getFileObject("src/conf/application.xml");
        assertNotNull(ddFO);
        EarProjectTest.validate(ddFO);
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        assertNotNull(app);
        for (Module module : app.getModule()) {
            Web web = module.getWeb();
            if (web != null) {
                importedContextRoot = web.getContextRoot();
                break;
            }
        }
        
        assertNotNull("context-root set", importedContextRoot);
        assertEquals("context-root successfully imported", CUSTOM_CONTEXT_ROOT, importedContextRoot);
    }
    
    private FileObject generateJ2EEApplication() throws Exception {
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(
                prjDirF, name, j2eeLevel, serverInstanceID,
                warName, jarName, carName, mainClass, platformName, sourceLevel);
        
        // Workaround. Set the context root which should be set automatically.
        // Do not know how to do it. Probably by getting somehow "Sun J2EE DD GUI"
        // loader into the game.
        FileObject ddFO = FileUtil.toFileObject(prjDirF).getFileObject("src/conf/application.xml");
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        for (Module module : app.getModule()) {
            Web web = module.getWeb();
            if (web != null) {
                web.setContextRoot("/my-context-root");
                app.write(ddFO);
                break;
            }
        }
        
        // clean-up NB specific metadata
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        prjDirFO.getFileObject("nbproject").delete();
        if (warName != null) {
            prjDirFO.getFileObject("testEA-war/nbproject").delete();
        }
        if (jarName != null) {
            prjDirFO.getFileObject("testEA-ejb/nbproject").delete();
        }
        if (carName != null) {
            prjDirFO.getFileObject("testEA-app-client/nbproject").delete();
        }
        return prjDirFO;
    }
    
    private FileObject generateJ2EEApplication(boolean withSubModules) throws Exception {
        if (!withSubModules) {
            this.warName = null;
            this.jarName = null;
            this.carName = null;
            this.mainClass = null;
        }
        return generateJ2EEApplication();
    }
    
    private FileObject generateJ2EEApplicationWithWeb() throws Exception {
        this.jarName = null;
        this.carName = null;
        this.mainClass = null;
        return generateJ2EEApplication();
    }
    
    // This could be probably removed as soon as #66988 is fixed since the
    // dialog will not be displayed any more.
    private static final class SilentDialogDisplayer extends DialogDisplayer {
        
        public Object notify(NotifyDescriptor descriptor) {
            return null;
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            return null;
        }
        
    }
    
    private static class SimplePlatformProvider implements JavaPlatformProvider {
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public JavaPlatform[] getInstalledPlatforms() {
            return new JavaPlatform[] {
                getDefaultPlatform()
            };
        }
        
        public JavaPlatform getDefaultPlatform() {
            return new TestDefaultPlatform();
        }
        
    }
    
    private static class TestDefaultPlatform extends JavaPlatform {
        
        public FileObject findTool(String toolName) {
            return null;
        }
        
        public String getDisplayName() {
            return "Default Platform";
        }
        
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }
        
        public Collection getInstallFolders() {
            return null;
        }
        
        public ClassPath getStandardLibraries() {
            return null;
        }
        
        public String getVendor() {
            return null;
        }
        
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("1.5"));
        }
        
        public ClassPath getSourceFolders() {
            return null;
        }
        
        public List getJavadocFolders() {
            return null;
        }
        
        public Map getProperties() {
            return Collections.singletonMap("platform.ant.name","default_platform");
        }
        
    }
    
}
