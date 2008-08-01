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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test {@link Evaluator} generally (but also see {@link ClassPathProviderImplTest}).
 * @author Jesse Glick
 */
public class EvaluatorTest extends TestBase {

    public EvaluatorTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    private File userPropertiesFile;
    
    protected void setUp() throws Exception {
        super.setUp();
        userPropertiesFile = TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        FileObject dir = nbRoot().getFileObject("java.project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
        dir = nbRoot().getFileObject("openide.loaders");
        assertNotNull("have openide/loaders checked out", dir);
        p = ProjectManager.getDefault().findProject(dir);
        loadersProject = (NbModuleProject)p;
    }
    
    public void testEvaluator() throws Exception {
        PropertyEvaluator eval = javaProjectProject.evaluator();
        assertEquals("right basedir", file("java.project"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("basedir")));
        assertEquals("right nb_all", nbRootFile(),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("nb_all")));
        assertEquals("right code.name.base.dashes", "org-netbeans-modules-java-project", eval.getProperty("code.name.base.dashes"));
        assertEquals("right is.autoload", "true", eval.getProperty("is.autoload"));
        assertEquals("right manifest.mf", "manifest.mf", eval.getProperty("manifest.mf"));
        assertEquals("right module JAR", file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar"),
            javaProjectProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
        eval = loadersProject.evaluator();
        assertEquals("right module JAR", file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"),
            loadersProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
    }

    /** @see "#63541" */
    public void testJdkProperties() throws Exception {
        File testjdk = new File(getWorkDir(), "testjdk");
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(userPropertiesFile));
        ep.setProperty("platforms.testjdk.home", testjdk.getAbsolutePath());
        Util.storeProperties(FileUtil.toFileObject(userPropertiesFile), ep);
        NbModuleProject p = generateStandaloneModule("module");
        PropertyEvaluator eval = p.evaluator();
        TestBase.TestPCL l = new TestBase.TestPCL();
        eval.addPropertyChangeListener(l);
        String bootcp = eval.getProperty("nbjdk.bootclasspath");
        String origbootcp = bootcp;
        assertNotNull(bootcp); // who knows what actual value will be inside a unit test - probably empty
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty("nbjdk.active", "testjdk");
        p.getHelper().putProperties("nbproject/platform.properties", ep);
        assertTrue("got a change in bootcp", l.changed.contains("nbjdk.bootclasspath"));
        l.reset();
        bootcp = eval.getProperty("nbjdk.bootclasspath");
        assertEquals("correct bootcp", new File(testjdk, "jre/lib/rt.jar".replace('/', File.separatorChar)).getAbsolutePath(), bootcp);
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty("nbjdk.active", "default");
        p.getHelper().putProperties("nbproject/platform.properties", ep);
        assertTrue("got a change in bootcp", l.changed.contains("nbjdk.bootclasspath"));
        l.reset();
        bootcp = eval.getProperty("nbjdk.bootclasspath");
        assertEquals(origbootcp, bootcp);
    }
    
}
