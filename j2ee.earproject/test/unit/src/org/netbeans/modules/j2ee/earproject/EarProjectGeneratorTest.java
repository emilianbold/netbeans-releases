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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author vkraemer
 */
public class EarProjectGeneratorTest extends NbTestCase {
    
    private String serverID;
    
    private static final String[] CREATED_FILES = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
        "src/conf/application.xml"
    };
    
    private static final String[] CREATED_FILES_EXT_SOURCES = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
    };
    
    private static final String[] CREATED_PROPERTIES = {
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "client.module.uri",
        "client.urlPart",
        "debug.classpath",
        "display.browser",
        "dist.dir",
        "dist.jar",
        "j2ee.appclient.mainclass.args",
        "j2ee.platform",
        "j2ee.server.type",
        "jar.compress",
        "jar.content.additional",
        "jar.name",
        "javac.debug",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "meta.inf",
        "no.dependencies",
        "platform.active",
        "resource.dir",
        "source.root",
    };
    
    private static final String[] CREATED_PROPERTIES_EXT_SOURCES = {
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "client.module.uri",
        "client.urlPart",
        "debug.classpath",
        "display.browser",
        "dist.dir",
        "dist.jar",
        "j2ee.appclient.mainclass.args",
        "j2ee.platform",
        "j2ee.server.type",
        "jar.compress",
        "jar.content.additional",
        "jar.name",
        "javac.debug",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "meta.inf",
        "no.dependencies",
        "platform.active",
        //"resource.dir",  -XXX- this is not found in project.props
        //        when the project is created from ex. sources. Bug or not???
        "source.root",
    };
    
    public EarProjectGeneratorTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testCreateProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(prjDirF, "test-project",
                J2eeModule.JAVA_EE_5, serverID, "1.5");
        assertNotNull(aph);
        FileObject prjDirFO = aph.getProjectDirectory();
        for (String file : CREATED_FILES) {
            assertNotNull(file + " file/folder cannot be found", prjDirFO.getFileObject(file));
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List createdProperties = new ArrayList(props.keySet());
        for (String property : CREATED_PROPERTIES) {
            assertNotNull(property + " property cannot be found in project.properties", props.getProperty(property));
            createdProperties.remove(property);
        }
        assertEquals("Found unexpected property: " + createdProperties,
                CREATED_PROPERTIES.length, props.keySet().size());
    }
    
    public void testImportProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EARProject");
        AntProjectHelper helper = EarProjectGenerator.importProject(prjDirF, prjDirF,
                "test-project-ext-src", J2eeModule.JAVA_EE_5, serverID, null,
                "1.5", Collections.<FileObject, ModuleType>emptyMap());
        assertNotNull(helper);
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        for (String createdFile : CREATED_FILES_EXT_SOURCES) {
            assertNotNull(createdFile + " file/folder cannot be found", prjDirFO.getFileObject(createdFile));
        }
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List createdProperties = new ArrayList(props.keySet());
        int extFileRefCount = 0;
        for (String propName : CREATED_PROPERTIES_EXT_SOURCES) {
            String propValue = props.getProperty(propName);
            assertNotNull(propName+" property cannot be found in project.properties", propValue);
            createdProperties.remove(propName);
            if ("manifest.file".equals(propName)) {
                assertEquals("Invalid value of manifest.file property.", "manifest.mf", propValue);
            }
        }
        assertEquals("Found unexpected property: " + createdProperties,
                CREATED_PROPERTIES_EXT_SOURCES.length, props.keySet().size() - extFileRefCount);
    }
    
    public void testProjectNameIsSet() throws Exception { // #73930
        File prjDirF = new File(getWorkDir(), "EARProject");
        EarProjectGenerator.createProject(prjDirF, "test-project",
                J2eeModule.JAVA_EE_5, serverID, "1.5");
        // test also build
        final File buildXML = new File(prjDirF, "build.xml");
        String projectName = (String) ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(buildXML.toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is set in the build.xml", "test-project", projectName);
    }
    
    public void testProjectNameIsEscaped() throws Exception {
        final File prjDirF = new File(getWorkDir(), "EARProject");
        EarProjectGenerator.createProject(prjDirF, "test project",
                J2eeModule.JAVA_EE_5, serverID, "1.5");
        // test build.xml
        String buildXmlProjectName = (String) ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(new File(prjDirF, "build.xml").toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is escaped in build.xml", "test_project", buildXmlProjectName);
        // test build-impl.xml
        String buildImplXmlProjectName = (String) ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(new File(prjDirF, "nbproject/build-impl.xml").toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is escaped in build-impl.xml", "test_project-impl", buildImplXmlProjectName);
    }
    
}
