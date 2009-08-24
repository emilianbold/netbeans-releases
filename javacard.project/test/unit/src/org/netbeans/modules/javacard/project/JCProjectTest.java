/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.javacard.project;

import com.sun.javacard.AID;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.*;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.constants.ProjectTemplateWizardKeys;
import org.netbeans.modules.javacard.constants.ProjectWizardKeys;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.wizard.ProjectXmlCreator;
import org.netbeans.modules.projecttemplates.ProjectCreator;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.test.MockLookup;
import org.xml.sax.SAXException;

/**
 *
 * @author Tim Boudreau
 */
public class JCProjectTest extends NbTestCase {
    public JCProjectTest() {
        super ("Test");
    }

    FileObject dir;
    JCProject project;
    FileObject projDir;
    @Before
    @Override
    public void setUp() throws Exception {
        System.out.println("setUpClass");
        System.setProperty("JCProjectTest", Boolean.TRUE.toString());
        clearWorkDir();
        //if we don't do this, we get an endless loop inside
        //recognizeInstanceFiles in lookup
        MockLookup.setLayersAndInstances(getClass().getClassLoader());
        FileObject fo = FileUtil.getConfigFile("Templates/Project/javacard/capproject.properties");
        assertNotNull (fo);
        DataObject dob = DataObject.find (fo);
        assertTrue (dob.isTemplate());
        dir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertTrue (dir != null);
        ProgressHandle h = ProgressHandleFactory.createHandle("x");
        assertNotNull (dir);
        FileObject capDir = dir.createFolder ("CapProject");
        ProjectKind kind = ProjectKind.CLASSIC_APPLET;
        String webContextPath = "/foo";
        ProjectCreator gen = new ProjectCreator(capDir);
        Map<String, String> templateProperties = new HashMap<String, String>();
        String pkg = "com.foo.bar.baz";
        String name = "CapProject";
        gen.add (new ProjectXmlCreator(name, ProjectKind.CLASSIC_APPLET));
        String nameSpaces = "Cap Project";
        String mainClassName = "Bob";
        String appletAid = Utils.generateAppletAID(pkg, "Bob").toString();
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_CLASSIC_PACKAGE_AID,
                Utils.generatePackageAid(pkg).toString());
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_APPLET_AID,
                appletAid);
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROJECT_NAME_SPACES, nameSpaces);
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE, pkg);
        String pkgSlashes = pkg.replace('.', '/'); //NOI18N
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE_PATH, pkgSlashes);
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME, mainClassName);
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME_LOWERCASE, mainClassName.toLowerCase());
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PROJECT_NAME, name);
        templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSPATH, ""); //NOI18N
        templateProperties.put (ProjectWizardKeys.WIZARD_PROP_APPLET_AID, appletAid);
        if (appletAid != null) {
            String aidAsHex = Utils.getAIDStringForScript(appletAid);
            templateProperties.put (ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_AID_HEX, aidAsHex);
        }
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_FILE_SEPARATOR, File.separator);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_MANIFEST_TYPE, kind.getManifestApplicationType());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_KIND, kind.name());

        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_DEVICE, "Default Device");
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_PLATFORM, JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME);

        if (kind == ProjectKind.CLASSIC_APPLET) {
            AID packageAid = Utils.generatePackageAid(pkg);
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_CLASSIC_PACKAGE_AID,
                    packageAid.toString());
        }

        if (appletAid != null) {
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_APPLET_AID, appletAid);
            AID instanceAid = AID.parse(appletAid).increment();
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_INSTANCE_AID, instanceAid.toString());
        }
        if (webContextPath != null) {
            templateProperties.put (ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH,
                    webContextPath);
        }
        String servletMapping = "/bar";
        if (servletMapping != null) {
            templateProperties.put(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING, servletMapping);
        }

        projDir = gen.createProject(h, "CapProject", fo, templateProperties).projectDir;
        synchronized (gen) {
            gen.wait(3000);
        }
        
        project = (JCProject) ProjectManager.getDefault().findProject(projDir);
        assertNotNull (project);
        System.err.println("Opening project");
        OpenProjects.getDefault().open(new Project[] { project }, false);
    }

    @After
    @Override
    public void tearDown() throws Exception {
    }

    @Test
    public void testPropertyEvaluator() {
        assertSame (ProjectKind.CLASSIC_APPLET, project.getLookup().lookup(ProjectKind.class));
        assertTrue (project.isBadPlatformOrCard());

        JCLogicalViewProvider prov = project.getLookup().lookup (JCLogicalViewProvider.class);
        assertNotNull (prov);
        assertTrue (prov.createLogicalView().getHtmlDisplayName() != null);

        EditableProperties props = 
                project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        PropertyEvaluator eval = project.evaluator();

        String defaultName = JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME;

        assertEquals (defaultName,
                props.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM));

        String activePlatformFromProps = props.getProperty (ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String fromEvaluator = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        assertEquals (activePlatformFromProps, fromEvaluator);

        assertEquals (props.getProperty (ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE),
                eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE));
    }

    public void testPropertyEvaluatorSanity() {
        FileObject fo = projDir.getFileObject("nbproject/project.properties");
        File f = FileUtil.toFile (fo);
        PropertyProvider prov = PropertyUtils.propertiesFilePropertyProvider(f);
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(
                project.getAntProjectHelper().getStockPropertyPreprovider(),
                PropertyUtils.globalPropertyProvider(), prov);        
        String ap = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        assertEquals ("javacard_default", ap);
    }

    public void testDependencies() throws SAXException, IOException, InterruptedException {
        System.out.println("testDependencies");
        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);
        assertEquals (1, prov.findClassPath(projDir.getFileObject("src"), ClassPath.COMPILE).getRoots().length);

        ResolvedDependencies deps = project.syncGetResolvedDependencies();
        assertTrue (deps.all().isEmpty());
        File tmp = new File (System.getProperty("java.io.tmpdir"));
        final File fakeLib = new File (tmp, "fakelib.jar");
        fakeLib.deleteOnExit();
        if (!fakeLib.exists()) {
            assertTrue (fakeLib.createNewFile());
        }

        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(fakeLib));
        JarOutputStream jo = new JarOutputStream(bo);

        String act = "org/netbeans/modules/javacard/project/Nothing.class";
        BufferedInputStream bi = new BufferedInputStream(JCProjectTest.class.getResourceAsStream("Nothing.class"));
        JarEntry je = new JarEntry(act);
        jo.putNextEntry(je);
        byte[] buf = new byte[1024];
        int anz;
        while ((anz = bi.read(buf)) != -1) {
          jo.write(buf, 0, anz);
        }
        bi.close();
        jo.close();
        bo.close();

        Map<ArtifactKind,String> m = new HashMap <ArtifactKind, String>();
        Dependency dep = new Dependency ("dep", DependencyKind.RAW_JAR, DeploymentStrategy.ALREADY_ON_CARD);
        m.put (ArtifactKind.ORIGIN, fakeLib.getAbsolutePath());
        deps.add(dep, m);
        assertEquals (1, deps.all().size());
        deps.save();

        //Now force a re-read of the data

        DependenciesProvider.Receiver r = new DependenciesProvider.Receiver() {

            public void receive(ResolvedDependencies deps) {
                try {
                    assertNotNull(deps);
                    assertEquals(1, deps.all().size());
                    assertEquals("dep", deps.all().get(0).getDependency().getID());
                    assertEquals(new File(fakeLib.getAbsolutePath()).getCanonicalFile(), new File(deps.all().get(0).getPath(ArtifactKind.ORIGIN)).getCanonicalFile());
                } catch (IOException ex) {
                    throw new IllegalStateException (ex);
                }
            }

            public boolean failed(Throwable failure) {
                throw new IllegalStateException(failure);
            }
        };
        DependenciesProvider p = project.getLookup().lookup(DependenciesProvider.class);
        assertNotNull (p);
        Cancellable c = p.requestDependencies(r);
        synchronized (c) {
            c.wait(10000);
        }

        assertNotNull (prov);

        FileObject srcDir = projDir.getFileObject("src");
        ClassPath path = prov.findClassPath(srcDir, ClassPath.COMPILE);

        FileObject[] roots = path.getRoots();
        assertEquals (2, roots.length);

        FileObject clazz = path.findResource(act);
        assertNotNull (clazz);
    }

    public void testAntArtifactProvider() throws Exception {
        System.out.println("testAntArtifactProvider");
        AntArtifactProvider prov = project.getLookup().lookup(AntArtifactProvider.class);
        assertNotNull (prov);
        boolean jarFound = false;
        for (AntArtifact a : prov.getBuildArtifacts()) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                jarFound = true;
                URI[] uris = a.getArtifactLocations();
                assertNotNull (uris);
                assertEquals (1, uris.length);
                System.err.println("URI:  " + uris[0]);
                File f = new File (uris[0]);
                File f2 = new File (FileUtil.toFile (project.getProjectDirectory()), "dist" + File.separatorChar + "CapProject.cap");
                assertEquals (f, f2);
            }
        }
        assertTrue (jarFound);
    }
}