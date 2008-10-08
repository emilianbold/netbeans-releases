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
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.modules.maven.classpath.RuntimeClassPathImpl;
import org.netbeans.modules.maven.classpath.TestRuntimeClassPathImpl;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class CosChecker implements PrerequisitesChecker {

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
                    ActionProvider.COMMAND_DEBUG_SINGLE.equals(actionName))) {
                //TODO check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.

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
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (UnsupportedOperationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return false;
                    } else {
                    }
                } else {
                    //TODO what to do now? skip?
                }
            }
        }

        //TODO identify testng tests and not allow CoS for them.
        if (RunUtils.hasTestCompileOnSaveEnabled(config) &&
                   (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName))) {
                //TODO check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.

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
                        JavaRunner.execute(action2Quick, params);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (UnsupportedOperationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return false;
                } else {
                }

        }
        return true;
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

}
