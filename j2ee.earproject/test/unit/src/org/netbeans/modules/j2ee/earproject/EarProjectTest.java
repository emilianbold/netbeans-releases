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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject.ProjectOpenedHookImpl;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Martin Krauskopf
 */
public class EarProjectTest extends NbTestCase {
    
    private String serverID;
    
    public EarProjectTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testEarWithoutDDOpening() throws Exception { // #75586
        File prjDirF = new File(getWorkDir(), "TestEarProject_15");
        EarProjectGenerator.createProject(prjDirF, "test-project",
                J2eeModule.JAVA_EE_5, serverID, "1.5");
        File dirCopy = copyFolder(prjDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        ddF.delete(); // one of #75586 scenario
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        // tests #75586
        EarProjectTest.openProject((EarProject) project);
    }
    
    public void testThatMissingDDIsRegeneratedCorrectlyDuringOpening() throws Exception { // #81154
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        validate(ddF);
        ddF.delete(); // one of #81154 scenario
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
        assertTrue("deployment descriptor was regenerated", ddF.isFile());
        validate(ddF);
        Application app = DDProvider.getDefault().getDDRoot(FileUtil.toFileObject(ddF));
        assertSame("two modules", 2, app.getModule().length);
    }
    
    public void testOpeningWihtoutPrivateMetadataAndSrcDirectory() throws Exception { // #83507
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        TestUtil.deleteRec(new File(new File(dirCopy, "nbproject"), "private"));
        TestUtil.deleteRec(new File(dirCopy, "src"));
        TestUtil.deleteRec(new File(new File(new File(dirCopy, "testEA-ac"), "nbproject"), "private"));
        TestUtil.deleteRec(new File(new File(new File(dirCopy, "testEA-ejb"), "nbproject"), "private"));
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
    }
    
    public void testEarProjectIsGCed() throws Exception { // #83128
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = "1.4";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeLevel,
                serverID, null, null, null, null, null, null);
        Project earProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        EarProjectTest.openProject((EarProject) earProject);
        Node rootNode = ((LogicalViewProvider) earProject.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        rootNode.getChildren().getNodes(true); // ping
        Reference<Project> wr = new WeakReference<Project>(earProject);
        OpenProjects.getDefault().close(new Project[] { earProject });
        EarProjectTest.closeProject((EarProject) earProject);
        rootNode = null;
        earProject = null;
        assertGC("project cannot be garbage collected", wr);
    }
    
    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final EarProject p) {
        ProjectOpenedHookImpl hook = (ProjectOpenedHookImpl) p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
    }
    
    public static void closeProject(final EarProject p) {
        ProjectOpenedHookImpl hook = (ProjectOpenedHookImpl) p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectClosed(); // protected but can use package-private access
    }
    
    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.
     * Stolen from ant/freeform.
     */
    private File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = workdir.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }
    
    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (from.getName().equals("CVS")) {
                return;
            }
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }
    
    public static void validate(final File ddFile) throws Exception {
        SAXParserFactory f = (SAXParserFactory) Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        if (f == null) {
            System.err.println("Validation skipped because org.apache.xerces.jaxp.SAXParserFactoryImpl was not found on classpath");
            return;
        }
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        URL schemaURL_1_4 = EarProjectTest.class.getResource("/org/netbeans/modules/j2ee/dd/impl/resources/application_1_4.xsd");
        URL schemaURL_5 = EarProjectTest.class.getResource("/org/netbeans/modules/j2ee/dd/impl/resources/application_5.xsd");
        assertNotNull("have access to schema", schemaURL_1_4);
        assertNotNull("have access to schema", schemaURL_5);
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", new String[] {
            schemaURL_1_4.toExternalForm(),
            schemaURL_5.toExternalForm()
        });
        try {
            p.parse(ddFile.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            fail("Validation of XML document " + ddFile + " against schema failed. Details: " +
                    e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage());
        }
    }
    
    public static void validate(FileObject ddFO) throws Exception {
        Assert.assertNotNull(ddFO);
        File ddF = FileUtil.toFile(ddFO);
        Assert.assertNotNull(ddF);
        validate(ddF);
    }
    
    private static final class Handler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
    
}
