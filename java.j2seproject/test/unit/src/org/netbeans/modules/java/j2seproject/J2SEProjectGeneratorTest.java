/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.util.ArrayList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

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
        "src",
        "test",
    };
    
    private static final String[] createdFilesExtSources = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
//        "nbproject/private/private.properties",       no private.properties are created when project and source roots are collocated
    };

    private static final String[] createdProperties = {
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "disable.compile.on.save",
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
        "source.encoding",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        "src.dir",
        "test.src.dir",
    };

    private static final String[] createdPropertiesExtSources = {
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "disable.compile.on.save",
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
        "source.encoding",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        "src.dir",
        "test.src.dir",
    };

    public void testCreateProject() throws Exception {
        File proj = getWorkDir();
        clearWorkDir();
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "test-project", null, "manifest.mf", null);
        J2SEProjectGenerator.setDefaultSourceLevel(null);
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
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        AntProjectHelper helper = J2SEProjectGenerator.createProject(proj, "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot}, "manifest.mf", null, null);
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        assertNotNull (helper);
        FileObject fo = FileUtil.toFileObject(proj);
        for (int i=0; i<createdFilesExtSources.length; i++) {
            assertNotNull(createdFilesExtSources[i]+" file/folder cannot be found", fo.getFileObject(createdFilesExtSources[i]));
        } 
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ArrayList l = new ArrayList(props.keySet());
        int extFileRefCount = 0;
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
                //Remove the file.reference to the source.dir, it is implementation detail
                //depending on the presence of the AlwaysRelativeCollocationQuery
                assertTrue("Value of the external source dir should be file reference",propValue.startsWith("${file.reference."));
                if (l.remove (propValue.subSequence(2,propValue.length()-1))) {
                    extFileRefCount++;
                }
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of src.dir property.", srcRoot, file);
            }
            else if ("test.src.dir".equals(propName)) {
                PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
                //Remove the file.reference to the source.dir, it is implementation detail
                //depending on the presence of the AlwaysRelativeCollocationQuery
                assertTrue("Value of the external test dir should be file reference",propValue.startsWith("${file.reference."));
                if (l.remove (propValue.subSequence(2,propValue.length()-1))) {
                    extFileRefCount++;
                }
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of test.src.dir property.", testRoot, file);
            }
        }
        assertEquals("Found unexpected property: "+l,createdPropertiesExtSources.length, props.keySet().size() - extFileRefCount);
    }
    
    //Tests issue: #147128:J2SESources does not register new external roots immediately
    public void testProjectFromExtSourcesOwnsTheSources () throws Exception {
        File root = getWorkDir();
        clearWorkDir();
        File proj = new File (root, "ProjectDir");
        proj.mkdir();
        File srcRoot = new File (root, "src");
        srcRoot.mkdir ();
        File testRoot = new File (root, "test");
        testRoot.mkdir ();
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        AntProjectHelper helper = J2SEProjectGenerator.createProject(proj, "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot}, "manifest.mf", null, null);
        final Project expected = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        assertNotNull(expected);
        assertEquals(expected, FileOwnerQuery.getOwner(srcRoot.toURI()));
        assertEquals(expected, FileOwnerQuery.getOwner(testRoot.toURI()));
    }
    
}
