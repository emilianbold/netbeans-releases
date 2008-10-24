/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.cos;

import hidden.org.codehaus.plexus.util.cli.CommandLineUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl;
import org.netbeans.modules.maven.classpath.RuntimeClassPathImpl;
import org.netbeans.modules.maven.classpath.TestRuntimeClassPathImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class CosChecker implements PrerequisitesChecker {

    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N

    private static final String MAVEN_MAIN_COS = ".netbeans_CoS_timestamp_main"; //NOI18N
    private static final String MAVEN_TEST_COS = ".netbeans_CoS_timestamp_test"; //NOI18N

    public static ExecutionResultChecker createResultChecker() {
        return new COSExChecker();
    }

    public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();
        if (config.getProject() == null) {
            return true;
        }
        //compile on save stuff
        if (NbMavenProject.TYPE_JAR.equals(
                config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType())) {

            if (RunUtils.hasApplicationCompileOnSaveEnabled(config) &&
                   (ActionProvider.COMMAND_RUN.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                    ActionProvider.COMMAND_RUN_SINGLE.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG_SINGLE.equals(actionName)))
            {
                long stamp = getLastCoSLastTouch(config, true);
                System.out.println("stamp=" + stamp);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (checkImportantFiles(stamp,config)) {
                    return true;
                }

                //TODO check the COS timestamp against resources etc.
                //if changed, perform part of the maven build. (or skip COS)

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName());
                params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
                params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, createRuntimeClassPath(config.getMavenProject(), false));
                //exec:exec property
                String exargs = config.getProperties().getProperty("exec.args"); //NOI18N
                if (exargs != null) {
                    String[] args = RunJarPanel.splitAll(exargs);
                    System.out.println("jvmargs=" + args[0]);
                    System.out.println("clazz=" + args[1]);
                    System.out.println("args=" + args[2]);
                    params.put(JavaRunner.PROP_CLASSNAME, args[1]);
                    String[] appargs = args[2].split(" ");
                    params.put(JavaRunner.PROP_APPLICATION_ARGS, Arrays.asList(appargs));
                    //TODO jvm args, add and for debugging, remove the debugging ones..
//                    params.put(JavaRunner.PROP_RUN_JVMARGS, args[2]);
                    String action2Quick = action2Quick(actionName);
                    boolean supported = JavaRunner.isSupported(action2Quick, params);
                    if (supported) {
                        try {
                            JavaRunner.execute(action2Quick, params);
                            touchCoSTimeStamp(config, false);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (UnsupportedOperationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            touchCoSTimeStamp(config, false);
                        }
                        return false;
                    } else {
                    }
                } else {
                    //TODO what to do now? skip?
                }
            }
        }

        if (RunUtils.hasTestCompileOnSaveEnabled(config) &&
                   (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName))) {
            String testng = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "testNGArtifactName", "test"); //NOI18N
            if (testng == null) {
                testng = "org.testng:testng"; //NOI18N
            }
            @SuppressWarnings("unchecked")
            List<Dependency> deps = config.getMavenProject().getTestDependencies();
            for (Dependency d : deps) {
                if (testng.equals(d.getManagementKey())) {
                    //skip tests that are invoked by testng, no support for it yet.
                    //#149464
                    return true;
                }
            }
            long stamp = getLastCoSLastTouch(config, true);
            System.out.println("stamp=" + stamp);
            //check the COS timestamp against critical files (pom.xml)
            // if changed, don't do COS.
            if (checkImportantFiles(stamp, config)) {
                return true;
            }

            //TODO check the COS timestamp against resources etc.
            //if changed, perform part of the maven build. (or skip COS)


            Map<String, Object> params = new HashMap<String, Object>();
            String test = config.getProperties().getProperty("test"); //NOI18N
            if (test == null) {
                //user somehow configured mapping in unknown way.
                return true;
            }
            params.put(JavaRunner.PROP_EXECUTE_FILE, config.getSelectedFileObject());

            List<String> jvmProps = new ArrayList<String>();
            Set<String> jvmPropNames = new HashSet<String>();
            params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName());
            String dir = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "basedir", "test"); //NOI18N
            //TODO there's another property named workingDirectory that overrides  basedir.
            // basedir is also assumed to end up as system property for tests..
            jvmPropNames.add("basedir"); //NOI18N
            if (dir != null) {
                params.put(JavaRunner.PROP_WORK_DIR, dir);
                jvmProps.add("-Dbasedir=" + dir); //NOI18N
            } else {
                params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
                jvmProps.add("-Dbasedir=" + config.getExecutionDirectory().getAbsolutePath()); //NOI18N
            }
            //add properties defined in surefire plugin
            Properties sysProps = PluginPropertyUtils.getPluginPropertyParameter(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "systemProperties", "test"); //NOI18N
            if (sysProps != null) {
                for (Map.Entry key : sysProps.entrySet()) {
                    jvmProps.add("-D" + key.getKey() + "=" + key.getValue()); //NOI18N
                    jvmPropNames.add((String)key.getKey());
                }
            }
            //add properties from action config,
            if (config.getProperties() != null) {
               for (Map.Entry entry : config.getProperties().entrySet()) {
                    //TODO do these have preference to ones defined in surefire plugin?
                   if (!jvmPropNames.contains((String)entry.getKey())) {
                       jvmProps.add("-D" + entry.getKey() + "=" + entry.getValue());
                       jvmPropNames.add((String)entry.getKey());
                   }
               }
            }

            String argLine = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "argLine", "test"); //NOI18N
            if (argLine != null) {
                try {
                    String[] arr = CommandLineUtils.translateCommandline(argLine);
                    jvmProps.addAll(Arrays.asList(arr));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                //TODO jvm args from the argLine exec property,
                //add and for debugging, remove the debugging ones..
            }

            //add additionalClasspathElements parameter in surefire plugin..
            String[] additionals = PluginPropertyUtils.getPluginPropertyList(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "additionalClasspathElements", "additionalClasspathElement", "test"); //NOI18N
            ClassPath cp = createRuntimeClassPath(config.getMavenProject(), true);
            if (additionals != null) {
                List<URL> roots = new ArrayList<URL>();
                File base = FileUtil.toFile(config.getProject().getProjectDirectory());
                for (String add : additionals) {
                    File root = FileUtilities.resolveFilePath(base, add);
                    if (root != null) {
                        try {
                            roots.add(root.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(CosChecker.class.getName()).info("Cannot convert '" + add + "' to URL");
                        }
                    } else {
                        Logger.getLogger(CosChecker.class.getName()).info("Cannot convert '" + add + "' to URL.");
                    }
                }
                ClassPath addCp = ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
                cp = ClassPathSupport.createProxyClassPath(cp, addCp);
            }
            params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, cp);

            params.put(JavaRunner.PROP_RUN_JVMARGS, jvmProps);
            String action2Quick = action2Quick(actionName);
            boolean supported = JavaRunner.isSupported(action2Quick, params);
            if (supported) {
                try {
                    ExecutorTask tsk = JavaRunner.execute(action2Quick, params);
                    //TODO listen on result of execution
                    //if failed, tweak the timestamps to force a non-CoS build
                    //next time around.
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (UnsupportedOperationException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    touchCoSTimeStamp(config, true);
                    touchCoSTimeStamp(config, false);
                }
                return false;
            } else {
            }
        }
        return true;
    }

    private boolean checkImportantFiles(long stamp, RunConfig rc) {
        assert rc.getProject() != null;
        FileObject prjDir = rc.getProject().getProjectDirectory();
        if (isNewer(stamp, prjDir.getFileObject("pom.xml"))) { //NOI18N
            return true;
        }
        if (isNewer(stamp, prjDir.getFileObject("profiles.xml"))) { //NOI18N
            return true;
        }
        if (isNewer(stamp, prjDir.getFileObject("nbactions.xml"))) { //NOI18N
            return true;
        }
        // the nbactions.xml file belonging to active configuration?
        M2ConfigProvider prov = rc.getProject().getLookup().lookup(M2ConfigProvider.class);
        if (prov != null) {
            M2Configuration m2c = prov.getActiveConfiguration();
            if (m2c != null) {
                String name = M2Configuration.getFileNameExt(m2c.getId());
                if (isNewer(stamp, prjDir.getFileObject(name))) {
                    return true;
                }
            }
        }
        //TODO what other files/folders to check?
        return false;
    }

    private boolean isNewer(long stamp, FileObject fo) {
        if (fo != null) {
            File fl = FileUtil.toFile(fo);
            if (fl.lastModified() >= stamp) {
                return true;
            }
        }
        return false;
    }

    //create a special runtime classpath here as the resolved mavenproject in execution
    // can be different from the one in loaded project
    private ClassPath createRuntimeClassPath(MavenProject prj, boolean test) {
        List<URI> roots;
        if (test) {
            roots = TestRuntimeClassPathImpl.createPath(prj);
        }
        else {
            roots = RuntimeClassPathImpl.createPath(prj);
        }
        return ClassPathSupport.createClassPath(AbstractProjectClassPathImpl.getPath(roots.toArray(new URI[0])));
    }

    /**
     * returns the
     * @param rc
     * @param test
     * @return
     */
    private long getLastCoSLastTouch(RunConfig rc, boolean test) {
        if (rc.getProject() == null) {
            return 0;
        }
        Build build = rc.getMavenProject().getBuild();
        if (build == null || build.getDirectory() == null) {
            return 0;
        }
        File fl = new File(build.getDirectory());
        fl = FileUtil.normalizeFile(fl);
        if (!fl.exists()) {
            //the project was not built
            return 0;
        }
        File check = new File(fl, test ? MAVEN_TEST_COS : MAVEN_MAIN_COS);
        if (!check.exists()) {
            //wasn't yet run with CoS, assume it's been built correctly.
            // if CoS fails, we probably want to remove the file to trigger
            // rebuilding by maven
            return Long.MAX_VALUE;
        }
        return check.lastModified();
    }

    private boolean touchCoSTimeStamp(RunConfig rc, boolean test) {
        if (rc.getProject() == null) {
            return false;
        }
        Build build = rc.getMavenProject().getBuild();
        if (build == null || build.getDirectory() == null) {
            return false;
        }
        File fl = new File(build.getDirectory());
        fl = FileUtil.normalizeFile(fl);
        if (!fl.exists()) {
            //the project was not built
            return false;
        }
        File check = new File(fl, test ? MAVEN_TEST_COS : MAVEN_MAIN_COS);
        if (!check.exists()) {
            try {
                return check.createNewFile();
            } catch (IOException ex) {
                return false;
            }
        } else {
            return check.setLastModified(System.currentTimeMillis());
        }
    }

    private static void deleteCoSTimeStamp(RunConfig rc, boolean test) {
        if (rc.getProject() == null) {
            return;
        }
        Build build = rc.getMavenProject().getBuild();
        if (build == null || build.getDirectory() == null) {
            return;
        }
        File fl = new File(build.getDirectory());
        fl = FileUtil.normalizeFile(fl);
        if (!fl.exists()) {
            //the project was not built
            return;
        }
        File check = new File(fl, test ? MAVEN_TEST_COS : MAVEN_MAIN_COS);
        if (check.exists()) {
            check.delete();
        }
    }




    private String action2Quick(String actionName) {
        if (ActionProvider.COMMAND_CLEAN.equals(actionName)) {
            return JavaRunner.QUICK_CLEAN;
        } else if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProvider.COMMAND_RUN_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_RUN;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProvider.COMMAND_DEBUG_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_DEBUG;
        } else if (ActionProvider.COMMAND_TEST.equals(actionName) || ActionProvider.COMMAND_TEST_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_TEST;
        } else if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_TEST_DEBUG;
        }
        assert false: "Cannot convert " + actionName + " to quick actions.";
        return null;
    }

    private static class COSExChecker implements ExecutionResultChecker {

        public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
            if (resultCode == 0) {
                // in all those cases, delete the CoS timestamp to allow CoS on the next iteration.
                CosChecker.deleteCoSTimeStamp(config, false);
                CosChecker.deleteCoSTimeStamp(config, true);
            }
        }
    }

}
