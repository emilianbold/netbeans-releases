/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.project;

import com.sun.javacard.AID;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.constants.ProjectTemplateWizardKeys;
import org.netbeans.modules.javacard.constants.ProjectWizardKeys;
import org.netbeans.modules.javacard.wizard.ProjectXmlCreator;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.projecttemplates.ProjectCreator;
import org.netbeans.modules.propdos.PropertiesBasedDataObject;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Tim Boudreau
 */
public class AbstractJCProjectTest extends NbTestCase {

    public AbstractJCProjectTest(String name) {
        super(name);
    }
    private ProjectManager mgr;
    static final Logger P_LOGGER = Logger.getLogger(PropertiesBasedDataObject.class.
            getPackage().getName());

    public static void setUpClass() {
        P_LOGGER.setLevel(Level.FINEST);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        System.out.println("setUp");
        clearWorkDir();
        //if we don't do this, we get an endless loop inside
        //recognizeInstanceFiles in lookup
//        MockLookup.setLayersAndInstances(AbstractProjectDependenciesTest.class.getClassLoader(), new F(), new TP());
        MockLookup.setLayersAndInstances(AbstractJCProjectTest.class.getClassLoader());
        mgr = ProjectManager.getDefault();
        System.setProperty("JCProjectTest", Boolean.TRUE.toString());
    }

    protected File createJar(String contents, String name, String resName, String classpath) throws Exception {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        final File fakeLib = new File(tmp, name);
        fakeLib.deleteOnExit();
        if (!fakeLib.exists()) {
            assertTrue(fakeLib.createNewFile());
        }
        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(fakeLib));
        JarOutputStream jo;
        Manifest m = new Manifest();
        if (classpath != null) {
            Attributes a = m.getMainAttributes();
            a.putValue("Manifest-Version", "1.0");
            a.putValue("Class-Path", classpath);
            System.err.println("Writing jar " + name + " with classpath " + classpath + " main attrs size " + m.getMainAttributes().size());
            //sanity check
            assertEquals(classpath, m.getMainAttributes().getValue("Class-Path"));
        }
        jo = new JarOutputStream(bo, m);
        String act = contents;
        BufferedInputStream bi = new BufferedInputStream(ProjectDependenciesTest.class.getResourceAsStream(resName));
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

        if (classpath != null) {
            JarFile jf = new JarFile(fakeLib);
            jf.getManifest();
            try {
                assertEquals("Manifest entries size wrong", 2, jf.getManifest().getMainAttributes().size());
                assertEquals("Class-Path not correct in manifest", classpath, jf.getManifest().getMainAttributes().getValue("Class-Path"));
            } finally {
                jf.close();
            }
        }

        return fakeLib.getCanonicalFile();
    }

    protected JCProject createProject(FileObject projTemplate, String name, ProjectKind kind, String pkg, final String nameSpaces, String mainClassName) throws Exception {
        JCProject result;
        FileObject fo = projTemplate;
        assertNotNull(fo);
        DataObject dob = DataObject.find(fo);
        assertTrue(dob.isTemplate());
        FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertTrue(dir != null);
        ProgressHandle h = ProgressHandleFactory.createHandle("x");
        assertNotNull(dir);
        String projDirName = FileUtil.findFreeFileName(dir, name, "");
        FileObject capDir = dir.createFolder(projDirName);
        String webContextPath = "/foo";
        ProjectCreator gen = new ProjectCreator(capDir);
        Map<String, String> templateProperties = new HashMap<String, String>();
        gen.add(new ProjectXmlCreator(name, kind));
        String appletAid = Utils.generateAppletAID(pkg, "Bob").toString();
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_CLASSIC_PACKAGE_AID, Utils.generatePackageAid(pkg).toString());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_APPLET_AID, appletAid);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROJECT_NAME_SPACES, nameSpaces);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE, pkg);
        String pkgSlashes = pkg.replace('.', '/');
        //NOI18N
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE_PATH, pkgSlashes);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME, mainClassName);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME_LOWERCASE, mainClassName.toLowerCase());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PROJECT_NAME, name);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSPATH, "");
        //NOI18N
        templateProperties.put(ProjectWizardKeys.WIZARD_PROP_APPLET_AID, appletAid);
        if (appletAid != null) {
            String aidAsHex = Utils.getAIDStringForScript(appletAid);
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_AID_HEX, aidAsHex);
        }
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_FILE_SEPARATOR, File.separator);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_MANIFEST_TYPE, kind.getManifestApplicationType());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_KIND, kind.name());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_DEVICE, "Default Device");
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_PLATFORM, JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME);
        if (kind.isClassic()) {
            AID packageAid = Utils.generatePackageAid(pkg);
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_CLASSIC_PACKAGE_AID, packageAid.toString());
        }
        if (appletAid != null) {
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_APPLET_AID, appletAid);
            AID instanceAid = AID.parse(appletAid).increment();
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_INSTANCE_AID, instanceAid.toString());
        }
        if (webContextPath != null) {
            templateProperties.put(ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH, webContextPath);
        }
        String servletMapping = "/bar";
        if (servletMapping != null) {
            templateProperties.put(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING, servletMapping);
        }
        FileObject projDir = gen.createProject(h, projDirName, fo, templateProperties).projectDir;
        synchronized (gen) {
            gen.wait(3000);
        }
        Project res = mgr.findProject(projDir);
        assertNotNull("Project manager could not find a project in " + projDir.getPath(), res);
        System.err.println("Opening project " + res.getProjectDirectory().getPath());
        OpenProjects.getDefault().open(new Project[]{res}, false);
        result = res.getLookup().lookup(JCProject.class);
        assertNotNull(result);
        return result;
    }

    private static class F implements ProjectFactory {

        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject("nbproject") != null;
        }

        public Project loadProject(FileObject projectDir, ProjectState state) throws IOException {
            try {
                FileObject projectXml = projectDir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                if (projectXml == null) {
                    throw new IllegalArgumentException("No file " + AntProjectHelper.PROJECT_XML_PATH + " under " + projectDir.getPath());
                }
                ProjectKind kind = ProjectKind.kindForProject(projectXml);
                if (kind == null) {
                    throw new IllegalArgumentException("No project kind found in " + "" + AntProjectHelper.PROJECT_XML_PATH + " under " + projectDir.getPath());
                }
                InputStream inStream = projectXml.getInputStream();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringElementContentWhitespace(true);
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                try {
                    Document d = docBuilder.parse(inStream);
                    AntProjectHelper helper = AntBasedProjectFactorySingleton.HELPER_CALLBACK.createHelper(projectDir, d, state, new TP());
                    return new JCProject(kind, helper);
                } catch (SAXException ex) {
                    throw new IllegalStateException (ex);
                } finally {
                    inStream.close();
                }
            } catch (ParserConfigurationException ex) {
                throw new IllegalStateException (ex);
            }
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
            AntProjectHelper helper = ((JCProject) project).getAntProjectHelper();
            try {
                Method m = AntProjectHelper.class.getDeclaredMethod("save", (Class[]) null);
                m.setAccessible(true);
                m.invoke(helper, (Object[]) null);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException (ex);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException (ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalStateException (ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException (ex);
            } catch (SecurityException ex) {
                throw new IllegalStateException (ex);
            }
        }
    }

    private static class TP implements AntBasedProjectType {

        public String getType() {
            return JCProjectType.JC_PROJECT_TYPE;
        }

        public Project createProject(AntProjectHelper helper) throws IOException {
            return new JCProject (ProjectKind.kindForProject(helper), helper);
        }

        public String getPrimaryConfigurationDataElementName(boolean shared) {
            return JCProjectType.PROJECT_CONFIGURATION_NAME;
        }

        public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
            return JCProjectType.PROJECT_CONFIGURATION_NAMESPACE;
        }

    }

    @Test
    public void testNothing() throws Exception {
        //JUnit insists on trying to run this class, so give it something
        //to chew on, however pointless
    }
}
