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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for J2SEProjectGenerator
 *
 * @author David Konecny
 */
public class J2SEProjectGeneratorTest extends NbTestCase {
    
    public J2SEProjectGeneratorTest(String testName) {
        super(testName);
    }

    private static final String[] createdFiles = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
        "src",
        "test",
    };
    
    private static final String[] createdFilesExtSources = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
    };

    private static final String[] createdProperties = {
        "application.args",
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "dist.dir",
        "dist.jar",
        "dist.javadoc.dir",
        "jar.compress",
        "javac.classpath",
        "javac.compilerargs",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "javac.test.classpath",
        "javadoc.author",
        "javadoc.encoding",
        "javadoc.noindex",
        "javadoc.nonavbar",
        "javadoc.notree",
        "javadoc.private",
        "javadoc.splitindex",
        "javadoc.use",
        "javadoc.version",
        "javadoc.windowtitle",
        "javadoc.additionalparam",
        "main.class",
        "manifest.file",
        "meta.inf.dir",
        "platform.active",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        "src.dir",
        "test.src.dir",
    };

    private static final String[] createdPropertiesExtSources = {
        "application.args",
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "dist.dir",
        "dist.jar",
        "dist.javadoc.dir",
        "jar.compress",
        "javac.classpath",
        "javac.compilerargs",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "javac.test.classpath",
        "javadoc.author",
        "javadoc.encoding",
        "javadoc.noindex",
        "javadoc.nonavbar",
        "javadoc.notree",
        "javadoc.private",
        "javadoc.splitindex",
        "javadoc.use",
        "javadoc.version",
        "javadoc.windowtitle",
        "javadoc.additionalparam",
        "main.class",
        "manifest.file",
        "meta.inf.dir",
        "platform.active",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        "src.dir",
        "test.test.dir",
    };

    public void testCreateProject() throws Exception {
        File proj = getWorkDir();
        clearWorkDir();
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "test-project", null, "manifest.mf");
        assertNotNull(aph);
        FileObject fo = aph.getProjectDirectory();
        for (int i=0; i<createdFiles.length; i++) {
            assertNotNull(createdFiles[i]+" file/folder cannot be found", fo.getFileObject(createdFiles[i]));
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ArrayList l = new ArrayList(props.keySet());
        for (int i=0; i<createdProperties.length; i++) {
            assertNotNull(createdProperties[i]+" property cannot be found in project.properties", props.getProperty(createdProperties[i]));
            l.remove(createdProperties[i]);
        }
        assertEquals("Found unexpected property: "+l,createdProperties.length, props.keySet().size());
    } 
    
    public void testCreateProjectFromExtSources () throws Exception {
        File root = getWorkDir();
        clearWorkDir();
        File proj = new File (root, "ProjectDir");
        proj.mkdir();
        File srcRoot = new File (root, "src");
        srcRoot.mkdir ();
        File testRoot = new File (root, "test");
        testRoot.mkdir ();
        AntProjectHelper helper = J2SEProjectGenerator.createProject(proj, "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot}, "manifest.mf");
        assertNotNull (helper);
        FileObject fo = FileUtil.toFileObject(proj);
        for (int i=0; i<createdFilesExtSources.length; i++) {
            assertNotNull(createdFilesExtSources[i]+" file/folder cannot be found", fo.getFileObject(createdFilesExtSources[i]));
        } 
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ArrayList l = new ArrayList(props.keySet());
        for (int i=0; i<createdPropertiesExtSources.length; i++) {
            String propName = createdPropertiesExtSources[i];
            String propValue = props.getProperty(propName);
            assertNotNull(propName+" property cannot be found in project.properties", propValue);
            l.remove(propName);
            if ("manifest.file".equals (propName)) {
                assertEquals("Invalid value of manifest.file property.", "manifest.mf", propValue);
            }
            else if ("src.dir".equals (propName)) {
                PropertyEvaluator eval = helper.getStandardPropertyEvaluator();                
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of src.dir property.", srcRoot, file);
            }
            else if ("test.test.dir".equals(propName)) {
                PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of test.src.dir property.", testRoot, file);
            }
        }
        assertEquals("Found unexpected property: "+l,createdPropertiesExtSources.length, props.keySet().size());
    }
    
}
