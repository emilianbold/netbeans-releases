/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.modules.testng.spi.XMLSuiteSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service=TestNGSupportImplementation.class)
public class MavenTestNGSupport extends TestNGSupportImplementation {

    private static final Logger LOGGER = Logger.getLogger(MavenTestNGSupport.class.getName());
    private static final Set<Action> SUPPORTED_ACTIONS;

    static {
        Set<Action> s = new HashSet<Action>();
//        s.add(Action.CREATE_TEST);
//        s.add(Action.RUN_FAILED);
//        s.add(Action.RUN_TESTMETHOD);
        s.add(Action.RUN_TESTSUITE);
        s.add(Action.DEBUG_TESTSUITE);
        SUPPORTED_ACTIONS = Collections.unmodifiableSet(s);
    }

    public boolean isActionSupported(Action action,Project p) {
        return p != null && p.getLookup().lookup(NbMavenProject.class) != null && SUPPORTED_ACTIONS.contains(action);
    }

    public void configureProject(FileObject createdFile) {
        ClassPath cp = ClassPath.getClassPath(createdFile, ClassPath.COMPILE);
        FileObject ng = cp.findResource("org.testng.annotations.Test"); //NOI18N
        if (ng == null) {
            final Project p = FileOwnerQuery.getOwner(createdFile);
            FileObject pom = p.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                public @Override
                void performOperation(POMModel model) {
                    String groupID = "org.testng"; //NOI18N
                    String artifactID = "testng"; //NOI18N
                    if (!ModelUtils.hasModelDependency(model, groupID, artifactID)) {
                        Dependency dep = ModelUtils.checkModelDependency(model, groupID, artifactID, true);
                        dep.setVersion("6.5.2"); //NOI18N
                        dep.setScope("test"); //NOI18N
                    }
                }
            };
            Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
            RequestProcessor RP = new RequestProcessor("Configure TestNG project task", 1, true); //NOI18N
            RP.post(new Runnable() {

                public void run() {
                    p.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(true);
                }
            });
        }
    }

    public TestExecutor createExecutor(Project p) {
        return new MavenExecutor(p);
    }

    private static class MavenExecutor implements TestExecutor {

        private static final String failedConfPath = "target/surefire-reports/testng-failed.xml"; //NOI18N
        private static final String failedConfPath2 = "target/surefire-reports/testng-native-results/testng-failed.xml"; //NOI18N
        private static final String resultsPath = "target/surefire-reports/testng-native-results/testng-results.xml"; //NOI18N
        private Project p;

        public MavenExecutor(Project p) {
            this.p = p;
        }

        public boolean hasFailedTests() {
            return getFailedConfig() != null;
        }

        public void execute(Action action, TestConfig config) throws IOException {
            RunConfig rc;
            if (Action.DEBUG_TESTSUITE.equals(action)
                    || Action.DEBUG_TEST.equals(action)
                    || Action.DEBUG_TESTMETHOD.equals(action)) {
                rc = new TestNGActionsProvider().createConfigForDefaultAction("testng.debug", p, Lookups.singleton(config.getTest()));
            } else {
                rc = new TestNGActionsProvider().createConfigForDefaultAction("testng.test", p, Lookups.singleton(config.getTest()));
            }
//            MavenProject mp = rc.getMavenProject();
            rc.setProperty("netbeans.testng.action", "true"); //NOI18N
            if (config.doRerun()) {
                copy(getFailedConfig());
//                mp.addPlugin(createPluginDef(failedConfPath));
            } else {
                File f = null;
                if (Action.RUN_TESTSUITE.equals(action) || Action.DEBUG_TESTSUITE.equals(action)) {
                    f = FileUtil.toFile(config.getTest());
                } else {
                   f = XMLSuiteSupport.createSuiteforMethod(
                        new File(System.getProperty("java.io.tmpdir")), //NOI18N
                        ProjectUtils.getInformation(p).getDisplayName(),
                        config.getPackageName(),
                        config.getClassName(),
                        config.getMethodName());
                }
                f = FileUtil.normalizeFile(f);
                copy(FileUtil.toFileObject(f));
//                mp.addPlugin(createPluginDef(FileUtil.getRelativePath(p.getProjectDirectory(), FileUtil.toFileObject(f))));
            }
            ExecutorTask task = RunUtils.executeMaven(rc);

        }

        private FileObject getFailedConfig() {
            FileObject fo = p.getProjectDirectory();
            //XXX - should rather listen on a fileobject??
            FileUtil.refreshFor(FileUtil.toFile(fo));
            FileObject cfg = fo.getFileObject(failedConfPath);
            if (cfg == null || !cfg.isValid()) {
               cfg = fo.getFileObject(failedConfPath2);
            }
            return cfg;
        }

        private FileObject copy(FileObject source) throws IOException {
            FileObject fo = p.getProjectDirectory();
            //target/nb-private/tesng-suite.xml
            FileObject folder = FileUtil.createFolder(fo, "target/nb-private"); //NOI18N
            FileObject cfg = folder.getFileObject("testng-suite", "xml"); //NOI18N
            if (cfg != null) {
                cfg.delete();
            }
            return FileUtil.copyFile(source, folder, "testng-suite"); //NOI18N
        }

//        private Plugin createPluginDef(String testDesc) {
//            Plugin plugin = new Plugin();
//            plugin.setGroupId("org.apache.maven.plugins");
//            plugin.setArtifactId("maven-surefire-plugin");
//            plugin.setVersion("2.4.2");
//
//            Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
//            if (dom == null) {
//                dom = new Xpp3Dom("configuration");
//                plugin.setConfiguration(dom);
//            }
//
//            Xpp3Dom dom2 = dom.getChild("suiteXmlFiles");
//            if (dom2 == null) {
//                dom2 = new Xpp3Dom("suiteXmlFiles");
//                dom.addChild(dom2);
//            }
//            Xpp3Dom dom3 = dom2.getChild("suiteXmlFile");
//            if (dom3 == null) {
//                dom3 = new Xpp3Dom("suiteXmlFile");
//                dom3.setValue(testDesc);
//                dom2.addChild(dom3);
//            }
//            return plugin;
//        }
    }
}
