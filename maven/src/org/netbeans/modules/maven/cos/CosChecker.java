/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.*;
import org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl;
import org.netbeans.modules.maven.classpath.RuntimeClassPathImpl;
import org.netbeans.modules.maven.classpath.TestRuntimeClassPathImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import static org.netbeans.modules.maven.cos.Bundle.*;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service={PrerequisitesChecker.class, LateBoundPrerequisitesChecker.class}, projectType="org-netbeans-modules-maven")
public class CosChecker implements PrerequisitesChecker, LateBoundPrerequisitesChecker {

    static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    private static final String STARTUP_ARGS_KEY = "run.jvmargs.ide"; // NOI18N
    private static final String RUN_MAIN = ActionProvider.COMMAND_RUN_SINGLE + ".main"; //NOI18N
    private static final String DEBUG_MAIN = ActionProvider.COMMAND_DEBUG_SINGLE + ".main"; //NOI18N
    private static final String PROFILE_MAIN = ActionProvider.COMMAND_PROFILE_SINGLE + ".main"; // NOI18N
    private static final Logger LOG = Logger.getLogger(CosChecker.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(CosChecker.class);

    @Override
    public boolean checkRunConfig(RunConfig config) {
        if (config.getProject() == null) {
            return true;
        }

        if (!checkRunMainClass(config)) {
            return false;
        }

        if (!checkRunTest(config)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        //deleting the timestamp before every action invokation means
        // we only can rely on Run via JavaRunner and via DeployOnSave
        // any other means of long term execution will not keep the CoS stamp around while running..
        // -->> ONLY DELETE FOR BUILD ACTION
        if (ActionProvider.COMMAND_BUILD.equals(config.getActionName())) {
            
//commented out because deleting the timestamp makes the createGeneratedClassfiles action a noop.
//            deleteCoSTimeStamp(config, true);
//            deleteCoSTimeStamp(config, false);
//            //do clean the generated class files everytime, that won't hurt anything
//            // unless the classes are missing then ?!? if the action doesn't perform the compilation step?
//            try {
//                cleanGeneratedClassfiles(config.getProject());
//            } catch (IOException ex) {
//                if (!"clean".equals(config.getGoals().get(0))) { //NOI18N
//                    config.getGoals().add(0, "clean"); //NOI18N
//                    }
//                Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Compile on Save Clean failed", ex);
//            }
        } else if (!ActionProvider.COMMAND_REBUILD.equals(config.getActionName())) {
            //now for all custom and non-build only related actions,
            //make sure we place the stamp files into all opened projects.
            Project[] opened = OpenProjects.getDefault().getOpenProjects();
            for (Project openprj : opened) {
                touchProject(openprj);
            }
        }
        return true;
    }

    private boolean hasChangedFilteredResources(boolean includeTests, long stamp, RunConfig config) {
        List<Resource> res = config.getMavenProject().getResources();
        for (Resource r : res) {
            if (r.isFiltering()) {
                if (hasChangedResources(r, stamp)) {
                    return true;
                }
                // if filtering resource not changed, proceed with CoS
                continue;
            }
        }
        if (includeTests) {
            res = config.getMavenProject().getTestResources();
            for (Resource r : res) {
                if (r.isFiltering()) {
                    if (hasChangedResources(r, stamp)) {
                        return true;
                    }
                    // if filtering resource not changed, proceed with CoS
                    continue;
                }
            }
        }
        return false;
    }

    static final String[] DEFAULT_INCLUDES = {"**"};

    private boolean hasChangedResources(Resource r, long stamp) {
        String dir = r.getDirectory();
        File dirFile = FileUtil.normalizeFile(new File(dir));
  //      System.out.println("checkresource dirfile =" + dirFile);
        if (dirFile.exists()) {
            List<File> toCopy = new ArrayList<File>();
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(dirFile);
            //includes/excludes
            String[] incls = r.getIncludes().toArray(new String[0]);
            if (incls.length > 0) {
                ds.setIncludes(incls);
            } else {
                ds.setIncludes(DEFAULT_INCLUDES);
            }
            String[] excls = r.getExcludes().toArray(new String[0]);
            if (excls.length > 0) {
                ds.setExcludes(excls);
            }
            ds.addDefaultExcludes();
            ds.scan();
            String[] inclds = ds.getIncludedFiles();
//            System.out.println("found=" + inclds.length);
            for (String inc : inclds) {
                File f = new File(dirFile, inc);
                if (f.lastModified() >= stamp) { 
                    toCopy.add(FileUtil.normalizeFile(f));
                }
            }
            if (toCopy.size() > 0) {
                    //the case of filtering source roots, here we want to return false
                    //to skip CoS altogether.
                return true;
            }
        }
        return false;
    }


    private boolean checkRunMainClass(final RunConfig config) {
        String actionName = config.getActionName();
        //compile on save stuff
        if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
            if ((NbMavenProject.TYPE_JAR.equals(
                    config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType()) &&
                    (ActionProvider.COMMAND_RUN.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                    ActionProvider.COMMAND_PROFILE.equals(actionName))) ||
                    RUN_MAIN.equals(actionName) ||
                    DEBUG_MAIN.equals(actionName) ||
                    PROFILE_MAIN.equals(actionName)) {
                long stamp = getLastCoSLastTouch(config, false);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (checkImportantFiles(stamp, config)) {
                    return true;
                }
                //check the COS timestamp against resources etc.
                //if changed, perform part of the maven build. (or skip COS)
                if (hasChangedFilteredResources(false, stamp, config)) {
                    //we have some filtered resources modified or encountered other problem,
                    //skip CoS
                    return true;
                }

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName() + "/CoS");
                String proppath = config.getProperties().get("exec.workingdir"); //NOI18N
                if (proppath != null) {
                    params.put(JavaRunner.PROP_WORK_DIR, FileUtil.normalizeFile(new File(proppath)));
                } else {
                    params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
                }
                if (RUN_MAIN.equals(actionName) ||
                    DEBUG_MAIN.equals(actionName) ||
                    PROFILE_MAIN.equals(actionName)) {
                    FileObject selected = config.getSelectedFileObject();
                    ClassPath srcs = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectSourcesClassPath(ClassPath.SOURCE);
                    String path = srcs.getResourceName(selected);
                    if (path == null) {
                        //#160776 only files on source classpath pass through
                        return true;
                    }
                    params.put(JavaRunner.PROP_EXECUTE_FILE, selected);
                } else {
                    //only for the case of running the project itself, relevant for the run/debug-project action and
                    //jar packaging
                    params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, createRuntimeClassPath(config.getMavenProject(), false));
                }
                //exec:exec property
                String exargs = config.getProperties().get("exec.args"); //NOI18N
                if (exargs != null) {
                    String[] args = RunJarPanel.splitAll(exargs);
                    if (params.get(JavaRunner.PROP_EXECUTE_FILE) == null) {
                        params.put(JavaRunner.PROP_CLASSNAME, args[1]);
                    }
                    String[] appargs = args[2].split(" ");
                    params.put(JavaRunner.PROP_APPLICATION_ARGS, Arrays.asList(appargs));
                    try {
                        //jvm args, add and for debugging, remove the debugging ones..
                        params.put(JavaRunner.PROP_RUN_JVMARGS, extractDebugJVMOptions(args[0]));
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                //make sure to run with the proper jdk
                params.put(JavaRunner.PROP_PLATFORM, config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform());

                //#168551
                params.put("maven.disableSources", Boolean.TRUE); //NOI18N

                if (params.get(JavaRunner.PROP_EXECUTE_FILE) != null ||
                        params.get(JavaRunner.PROP_CLASSNAME) != null) {
                    String action2Quick = action2Quick(actionName);
                    boolean supported = JavaRunner.isSupported(action2Quick, params);
                    if (supported) {
                        try {
                            collectStartupArgs(config, params);
                            ExecutorTask tsk = JavaRunner.execute(action2Quick, params);
                            warnCoSInOutput(tsk, config);
                            tsk.addTaskListener(new TaskListener() {

                                @Override
                                public void taskFinished(Task task) {
                                    warnTestCoS(config);
                                }
                            });
                            
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (UnsupportedOperationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            touchCoSTimeStamp(config, false);
                        }
                        return false;
                    }
                } else {
                    //TODO what to do now? skip?
                }
            }
        }
        return true;
    }

    private boolean checkRunTest(final RunConfig config) {
        String actionName = config.getActionName();
        if (!(ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName))) {
            return true;
        }
        if (RunUtils.hasTestCompileOnSaveEnabled(config)) {
            String testng = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "testNGArtifactName", "test"); //NOI18N
            if (testng == null) {
                testng = "org.testng:testng"; //NOI18N
            }
            List<Dependency> deps = config.getMavenProject().getTestDependencies();
            boolean haveJUnit = false, haveTestNG = false;
            String testngVersion = null;
            for (Dependency d : deps) {
                if (d.getManagementKey().startsWith(testng)) {
                    testngVersion = d.getVersion();
                    haveTestNG = true;
                } else if (d.getManagementKey().startsWith("junit:junit")) { //NOI18N
                    haveJUnit = true;
                }
            }
            if (haveJUnit && haveTestNG && new ComparableVersion("6.5.1").compareTo(new ComparableVersion(testngVersion)) >= 0) {
                //CoS requires at least TestNG 6.5.2-SNAPSHOT if JUnit is present
                return true;
            }
            Map<String, Object> params = new HashMap<String, Object>();
            String test = config.getProperties().get("test"); //NOI18N
            if (test == null) {
                //user somehow configured mapping in unknown way.
                return true;
            }

            long stamp = getLastCoSLastTouch(config, true);
            //check the COS timestamp against critical files (pom.xml)
            // if changed, don't do COS.
            if (checkImportantFiles(stamp, config)) {
                return true;
            }

            //check the COS timestamp against resources etc.
            //if changed, perform part of the maven build. (or skip COS)
            if (hasChangedFilteredResources(true, stamp, config)) {
                //we have some filtered resources modified, skip CoS
                return true;
            }

            //#
            FileObject selected = config.getSelectedFileObject();
            ProjectSourcesClassPathProvider cpp = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class);
            ClassPath srcs = cpp.getProjectSourcesClassPath(ClassPath.SOURCE);
            ClassPath[] cps = cpp.getProjectClassPaths(ClassPath.SOURCE);
            ClassPath testcp = ClassPathSupport.createProxyClassPath(cps);
            String path;
            if (selected != null) {
                path = srcs.getResourceName(selected);
                if (path != null) {
                    //now we have a source file, need to convert to testSource..
                    String nameExt = selected.getNameExt().replace(".java", "Test.java"); //NOI18N
                    path = path.replace(selected.getNameExt(), nameExt);
                    FileObject testFo = testcp.findResource(path);
                    if (testFo != null) {
                        selected = testFo;
                    } else {
                        //#160776 only files on source classpath pass through
                        return true;
                    }
                } else {
                    // we have a test source file ?
                    path = testcp.getResourceName(selected);
                    if (path == null) {
                        //#160776 only files on source classpath pass through
                        return true;
                    }
                }
            } else {
               //#173724 we have a custom action now which was not triggered on a file.
               // we need to find the FileObject/path based on "test" property.. hard and fragile.
               test = test + ".java";
               selected = testcp.findResource(test); //just in case the test pattern is full path..
               if (selected == null) {
                   List<FileObject> mainSourceRoots = Arrays.asList(srcs.getRoots());
                   TOP : for (FileObject root : testcp.getRoots()) {
                       if (mainSourceRoots.contains(root)) continue;
                       Enumeration<? extends FileObject> fos = root.getData(true);
                       while (fos.hasMoreElements()) {
                           FileObject fo = fos.nextElement();
                           if (fo.getNameExt().equals(test)) {
                               selected = fo;
                               break TOP;
                           }
                       }
                   }
               }
            }
            if (selected == null) {
                return true;
            }
            
            params.put(JavaRunner.PROP_EXECUTE_FILE, selected);

            //make sure to run with the proper jdk
            params.put(JavaRunner.PROP_PLATFORM, config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform());
            
            List<String> jvmProps = new ArrayList<String>();
            Set<String> jvmPropNames = new HashSet<String>();
            params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName() + "/CoS");
            String dir = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "basedir", "test"); //NOI18N
            //TODO there's another property named workingDirectory that overrides  basedir.
            // basedir is also assumed to end up as system property for tests..
            jvmPropNames.add("basedir"); //NOI18N
            if (dir != null) {
                params.put(JavaRunner.PROP_WORK_DIR, dir);
                jvmProps.add("-Dbasedir=\"" + dir + "\""); //NOI18N
            } else {
                    params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
                    jvmProps.add("-Dbasedir=\"" + config.getExecutionDirectory().getAbsolutePath()+ "\""); //NOI18N
            }
            //add properties defined in surefire plugin
            Properties sysProps = PluginPropertyUtils.getPluginPropertyParameter(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "systemProperties", "test"); //NOI18N
            if (sysProps != null) {
                for (Map.Entry key : sysProps.entrySet()) {
                    jvmProps.add("-D" + key.getKey() + "=" + key.getValue()); //NOI18N
                    jvmPropNames.add((String) key.getKey());
                }
            }
            //add properties from action config,
            if (config.getProperties() != null) {
                for (Map.Entry entry : config.getProperties().entrySet()) {
                    //#158039
                    if ("maven.surefire.debug".equals(entry.getKey())) { //NOI18N
                        continue;
                    }
                    if (Constants.ACTION_PROPERTY_JPDALISTEN.equals(entry.getKey())) {
                        continue;
                    }
                    if ("jpda.stopclass".equals(entry.getKey())) {//NOI18N
                        continue;
                    }
                    if (DefaultReplaceTokenProvider.METHOD_NAME.equals(entry.getKey())) {
                        params.put("methodname", entry.getValue()); // NOI18N
                        actionName = ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ? SingleMethod.COMMAND_RUN_SINGLE_METHOD : SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
                        continue;
                    }
                    //TODO do these have preference to ones defined in surefire plugin?
                    if (!jvmPropNames.contains((String) entry.getKey())) {
                        jvmProps.add("-D" + entry.getKey() + "=" + entry.getValue());
                        jvmPropNames.add((String) entry.getKey());
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
                // jvm args from the argLine exec property,
                //add and for debugging, remove the debugging ones..
                argLine = config.getProperties().get("argLine");
                if (argLine != null) {
                    try {
                        jvmProps.addAll(extractDebugJVMOptions(argLine));
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
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
                            URL url = Utilities.toURI(root).toURL();
                            if (FileUtil.isArchiveFile(url)) {
                                url = FileUtil.getArchiveRoot(url);
                            }
                            roots.add(url);
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Cannot convert ''{0}'' to URL", add);
                        }
                    } else {
                        Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Cannot convert ''{0}'' to URL.", add);
                    }
                }
                ClassPath addCp = ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
                cp = ClassPathSupport.createProxyClassPath(cp, addCp);
            }
            params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, cp);

            params.put(JavaRunner.PROP_RUN_JVMARGS, jvmProps);

            //#168551
            params.put("maven.disableSources", Boolean.TRUE);  //NOI18N

            String action2Quick = action2Quick(actionName);
            boolean supported = JavaRunner.isSupported(action2Quick, params);
            if (supported) {
                try {
                    collectStartupArgs(config, params);
                    final ExecutorTask tsk = JavaRunner.execute(action2Quick, params);
                    warnCoSInOutput(tsk, config);
                    tsk.addTaskListener(new TaskListener() {

                        @Override
                        public void taskFinished(Task task) {
                            warnTestCoS(config);
                        }
                    });
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
            }
        } else {
            warnNoTestCoS(config);
        }
        return true;
    }

    private static void cleanGeneratedClassfiles(Project p) throws IOException { // #145243
        //we execute normal maven build, but need to clean any
        // CoS classes present.
        List<ClassPath> executePaths = new ArrayList<ClassPath>();
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            FileObject root = g.getRootFolder();
            ClassPath cp = ClassPath.getClassPath(root, ClassPath.EXECUTE);
            if (cp != null) {
                executePaths.add(cp);
            }
        }
        int res = JavaRunner.execute(JavaRunner.QUICK_CLEAN, Collections.singletonMap(
                JavaRunner.PROP_EXECUTE_CLASSPATH, ClassPathSupport.createProxyClassPath(executePaths.toArray(new ClassPath[0])))).
                result();
        if (res != 0) {
            throw new IOException("Failed to clean NetBeans-generated classes");
        }
    }

    private boolean checkImportantFiles(long stamp, RunConfig rc) {
        assert rc.getProject() != null;
        FileObject prjDir = rc.getProject().getProjectDirectory();
        if (isNewer(stamp, prjDir.getFileObject("pom.xml"))) { //NOI18N
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
        } else {
            roots = RuntimeClassPathImpl.createPath(prj);
        }
        return ClassPathSupport.createClassPath(AbstractProjectClassPathImpl.getPath(roots.toArray(new URI[0]), null));
    }

    private static File getCoSFile(RunConfig rc, boolean test) {
        if (rc.getProject() == null) {
            return null;
        }
        return getCoSFile(rc.getMavenProject(), test);
    }

    private static File getCoSFile(MavenProject mp, boolean test) {
        if (mp == null) {
            return null;
        }
        Build build = mp.getBuild();
        if (build == null) {
            return null;
        }
        String path = test ? build.getTestOutputDirectory() : build.getOutputDirectory();
        if (path == null) {
            return null;
        }
        File fl = new File(path);
        fl = FileUtil.normalizeFile(fl);
        return  new File(fl, NB_COS);
    }

    /**
     * returns the
     * @param rc
     * @param test
     * @return
     */
    private static long getLastCoSLastTouch(RunConfig rc, boolean test) {
        File fl = getCoSFile(rc, test);
        if (fl == null) {
            return 0;
        }
        if (fl.getParentFile() == null || !(fl.getParentFile().exists())) {
            //the project was not built
            return 0;
        }
        if (!fl.exists()) {
            //wasn't yet run with CoS, assume it's been built correctly.
            // if CoS fails, we probably want to remove the file to trigger
            // rebuilding by maven
            return Long.MAX_VALUE;
        }
        return fl.lastModified();
    }

    private static boolean touchCoSTimeStamp(RunConfig rc, boolean test) {
        return touchCoSTimeStamp(rc, test, System.currentTimeMillis());
    }

    private static boolean touchCoSTimeStamp(RunConfig rc, boolean test, long stamp) {
        if (rc.getProject() == null) {
            return false;
        }
        return touchCoSTimeStamp(rc.getMavenProject(), test, stamp);
    }

    private static boolean touchCoSTimeStamp(MavenProject mvn, boolean test) {
        return touchCoSTimeStamp(mvn, test, System.currentTimeMillis());
    }

    private static boolean touchCoSTimeStamp(MavenProject mvn, boolean test, long stamp) {
        File fl = getCoSFile(mvn, test);
        if (fl == null) {
            return false;
        }
        if (fl.getParentFile() == null || !(fl.getParentFile().exists())) {
            //the project was not built
            return false;
        }
        if (!fl.exists()) {
            try {
                return fl.createNewFile();
            } catch (IOException ex) {
                return false;
            }
        } 
        return fl.setLastModified(stamp);
    }

    private static void deleteCoSTimeStamp(RunConfig rc, boolean test) {
        File fl = getCoSFile(rc, test);
        if (fl != null && fl.exists()) {
            fl.delete();
        }
    }

    private static void deleteCoSTimeStamp(MavenProject mp, boolean test) {
        File fl = getCoSFile(mp, test);
        if (fl != null && fl.exists()) {
            fl.delete();
        }
    }


    static List<String> extractDebugJVMOptions(String argLine) throws Exception {
        String[] split = CommandLineUtils.translateCommandline(argLine);
        List<String> toRet = new ArrayList<String>();
        for (String arg : split) {
            if ("-Xdebug".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Djava.compiler=none".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Xnoagent".equals(arg)) { //NOI18N
                continue;
            }
            if (arg.startsWith("-Xrunjdwp")) { //NOI18N
                continue;
            }
            if (arg.trim().length() == 0) {
                continue;
            }
            toRet.add(arg);
        }
        return toRet;
    }

    private String action2Quick(String actionName) {
        if (ActionProvider.COMMAND_CLEAN.equals(actionName)) {
            return JavaRunner.QUICK_CLEAN;
        } else if (ActionProvider.COMMAND_RUN.equals(actionName) || RUN_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_RUN;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || DEBUG_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || PROFILE_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_PROFILE;
        } else if (ActionProvider.COMMAND_TEST.equals(actionName) || ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST;
        } else if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST_DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_TEST_PROFILE;
        }
        assert false : "Cannot convert " + actionName + " to quick actions.";
        return null;
    }
    
    private void collectStartupArgs(RunConfig config, Map<String, Object> params) {
        String actionName = config.getActionName();
        StartupExtender.StartMode mode;
        
        if (ActionProvider.COMMAND_RUN.equals(actionName) || RUN_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || DEBUG_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProvider.COMMAND_PROFILE_SINGLE.equals(actionName) || PROFILE_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            // XXX could also set argLine for COMMAND_TEST and relatives (StartMode.TEST_*); need not be specific to TYPE_JAR
            return;
        }

        InstanceContent ic = new InstanceContent();
        Project p = config.getProject();
        if (p != null) {
            ic.add(p);
            ActiveJ2SEPlatformProvider pp = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            if (pp != null) {
                ic.add(pp.getJavaPlatform());
            }
        }
        Set<String> args = new HashSet<String>();

        for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
            args.addAll(group.getArguments());
        }
        
        if (!args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for(String arg : args) {
                sb.append(arg).append(' ');
            }
            params.put(STARTUP_ARGS_KEY, sb.toString());
        }
    }

    static void touchProject(Project project) {
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        if (prj != null) {
            MavenProject mvn = prj.getMavenProject();
            if (RunUtils.hasApplicationCompileOnSaveEnabled(project)) {
                touchCoSTimeStamp(mvn, false);
            } else {
                File f = getCoSFile(mvn, false);
                boolean doClean = f != null && f.exists();
                if (doClean) {
                    try {
                        cleanGeneratedClassfiles(project);
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, "Error cleaning up", ex);
                    }
                }
                deleteCoSTimeStamp(mvn, false);
            }
            if (RunUtils.hasTestCompileOnSaveEnabled(project)) {
                touchCoSTimeStamp(mvn, true);
            } else {
                deleteCoSTimeStamp(mvn, true);
            }
        }
    }

    @StaticResource private static final String SUGGESTION = "org/netbeans/modules/maven/resources/suggestion.png";
    private static boolean warnedNoCoS;
    @Messages({
        "CosChecker.no_test_cos.title=Not using Compile on Save",
        "CosChecker.no_test_cos.details=Compile on Save mode can speed up single test execution for many projects."
    })
    private static void warnNoTestCoS(RunConfig config) {
        if (warnedNoCoS) {
            return;
        }
        final Project project = config.getProject();
        if (project == null) {
            return;
        }
        final Notification n = NotificationDisplayer.getDefault().notify(CosChecker_no_test_cos_title(), ImageUtilities.loadImageIcon(SUGGESTION, true), CosChecker_no_test_cos_details(), new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showCompilePanel(project);
            }
        }, NotificationDisplayer.Priority.LOW);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override public void run() {
                n.clear();
            }
        }, 15 * 1000);
        warnedNoCoS = true;
    }
    
    private static boolean warnedCoS;
    @Messages({
        "CosChecker.test_cos.title=Using Compile on Save (CoS)",
        "CosChecker.test_cos.details=CoS mode is not executing tests through Maven. Disable if causing problems."
    })
    private static void warnTestCoS(RunConfig config) {
        if (warnedCoS) {
            return;
        }
        final Project project = config.getProject();
        if (project == null) {
            return;
        }
        final Notification n = NotificationDisplayer.getDefault().notify(CosChecker_test_cos_title(), ImageUtilities.loadImageIcon(SUGGESTION, true), CosChecker_test_cos_details(), new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showCompilePanel(project);
            }

        }, NotificationDisplayer.Priority.LOW);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override public void run() {
                n.clear();
            }
        }, 15 * 1000);
        warnedCoS = true;
    }
    
    private static void showCompilePanel(Project project) {
        CustomizerProviderImpl prv = project.getLookup().lookup(CustomizerProviderImpl.class);
        if (prv != null) {
            prv.showCustomizer(ModelHandle2.PANEL_COMPILE);
        }
    }

    //this method has a problem of timing. There is the executor's task running in some other thread and
    // and we try to write to it. In reality for short lived executions it either prints first or never at all.
    // I suppose that for long running tasks we cannot guarantee the position at which the warning appears.
    private void warnCoSInOutput(final ExecutorTask tsk, final RunConfig config) throws IOException {
        return;
//        if (IOColorPrint.isSupported(tsk.getInputOutput())) {
//            IOColorPrint.print(tsk.getInputOutput(), "NetBeans: Compile on Save Execution is not done through Maven.", null, false, Color.GRAY);
//            IOColorPrint.print(tsk.getInputOutput(), "Disable if it's causing problems.\n", new OutputListener() {
//
//                @Override
//                public void outputLineSelected(OutputEvent ev) {
//                }
//                
//                @Override
//                public void outputLineAction(OutputEvent ev) {
//                    showCompilePanel(config.getProject());
//                }
//
//                @Override
//                public void outputLineCleared(OutputEvent ev) {
//                }
//            }, false, Color.BLUE.darker());
//        }
    }

    
    @ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-maven")
    public static class CosPOH extends ProjectOpenedHook {

        private final Project project;
        
        private final PropertyChangeListener listener;

        public CosPOH(Project prj) {
            project = prj;
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        touchProject(project);
                    }
                }
            };
        }

        @Override
        protected void projectOpened() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    touchProject(project);
                }
            });
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                prj.addPropertyChangeListener(listener);
            }
        }

        @Override
        protected void projectClosed() {
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                prj.removePropertyChangeListener(listener);
                final MavenProject mvn = prj.getMavenProject();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //also delete the IDE generated class files now?
                            cleanGeneratedClassfiles(project);
                        } catch (IOException ex) {
                            LOG.log(Level.FINE, "Error cleaning up", ex);
                        } finally {
                            deleteCoSTimeStamp(mvn, true);
                            deleteCoSTimeStamp(mvn, false);
                        }
                    }
                });
            }
        }
    }

    @ProjectServiceProvider(service=ExecutionResultChecker.class, projectType="org-netbeans-modules-maven")
    public static class COSExChecker implements ExecutionResultChecker {

        @Override
        public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
            // after each build put the Cos stamp in the output folder to have
            // the classes really compiled on save.
            if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
                touchCoSTimeStamp(config, false);
            } else {
                deleteCoSTimeStamp(config, false);
            }
            if (RunUtils.hasTestCompileOnSaveEnabled(config)) {
                touchCoSTimeStamp(config, true);
            } else {
                deleteCoSTimeStamp(config, true);
            }
        }
    }
}
