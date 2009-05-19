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

import hidden.org.codehaus.plexus.util.DirectoryScanner;
import hidden.org.codehaus.plexus.util.IOUtil;
import hidden.org.codehaus.plexus.util.cli.CommandLineUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.modules.maven.classpath.RuntimeClassPathImpl;
import org.netbeans.modules.maven.classpath.TestRuntimeClassPathImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
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
    private static final String RUN_MAIN = ActionProvider.COMMAND_RUN_SINGLE + ".main"; //NOI18N
    private static final String DEBUG_MAIN = ActionProvider.COMMAND_DEBUG_SINGLE + ".main"; //NOI18N


    public static ExecutionResultChecker createResultChecker() {
        return new COSExChecker();
    }

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

        long touch1 = getLastCoSLastTouch(config, true);
        long touch2 = getLastCoSLastTouch(config, false);
        if ((touch1 != 0 && touch1 != Long.MAX_VALUE) ||
                (touch2 != 0 && touch2 != Long.MAX_VALUE)) {
            try {
                cleanGeneratedClassfiles(config);
            } catch (IOException ex) {
                if (!"clean".equals(config.getGoals().get(0))) { //NOI18N
                    config.getGoals().add(0, "clean"); //NOI18N
                }
                Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Compile on Save Clean failed", ex);
            }
        }
        return true;
    }

    private boolean checkAndCopyResources(boolean includeTests, long stamp, RunConfig config) {
        List<Resource> toprocess = new ArrayList<Resource>();
        List<Resource> toprocessTests = new ArrayList<Resource>();
        @SuppressWarnings("unchecked")
        List<Resource> res = config.getMavenProject().getResources();
        for (Resource r : res) {
            if (r.isFiltering()) {
                if (checkResource(r, null, stamp)) {
                    return false;
                }
                // if filtering resource not changed, proceed with CoS
                continue;
            }
            toprocess.add(r);
        }
        if (includeTests) {
            res = config.getMavenProject().getTestResources();
            for (Resource r : res) {
                if (r.isFiltering()) {
                    if (!checkResource(r, null, stamp)) {
                        return false;
                    }
                    // if filtering resource not changed, proceed with CoS
                    continue;
                }
                toprocessTests.add(r);
            }
        }
        String output = config.getMavenProject().getBuild().getOutputDirectory();
        FileObject outputFO = FileUtilities.convertStringToFileObject(output);
        if (outputFO == null) {
            return false;
        }
        for (Resource r : toprocess) {
            checkResource(r, outputFO, stamp);
        }
        if (includeTests) {
            output = config.getMavenProject().getBuild().getTestOutputDirectory();
            outputFO = FileUtilities.convertStringToFileObject(output);
            if (outputFO == null) {
                return false;
            }
            for (Resource r : toprocessTests) {
                checkResource(r, outputFO, stamp);
            }
        }
        return true;
    }

    private static final String[] DEFAULT_INCLUDES = {"**/**"};

    private boolean checkResource(Resource r, FileObject outputDir, long stamp) {
        String dir = r.getDirectory();
        File dirFile = FileUtil.normalizeFile(new File(dir));
  //      System.out.println("checkresource dirfile =" + dirFile);
        if (dirFile.exists()) {
            List<File> toCopy = new ArrayList<File>();
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(dirFile);
            //includes/excludes
            @SuppressWarnings("unchecked")
            String[] incls = (String[]) r.getIncludes().toArray(new String[0]);
            if (incls != null && incls.length > 0) {
                ds.setIncludes(incls);
            } else {
                ds.setIncludes(DEFAULT_INCLUDES);
            }
            @SuppressWarnings("unchecked")
            String[] excls = (String[]) r.getExcludes().toArray(new String[0]);
            if (excls != null && excls.length > 0) {
                ds.setExcludes(excls);
            }
            ds.addDefaultExcludes();
            ds.scan();
            String[] inclds = ds.getIncludedFiles();
//            System.out.println("found=" + inclds.length);
            for (String inc : inclds) {
                File f = new File(dirFile, inc);
                if (f.lastModified() >= 0) { //XXX TODO stamp) { for some reason, the
                    // java infrastructure seems to delete the non class files on output dir, we need to copy over
                    // everytime.
//                    System.out.println("to copy-" + f);
                    toCopy.add(FileUtil.normalizeFile(f));
                }
            }
            if (toCopy.size() > 0) {
                if (outputDir != null) {
                    //copy to output dir
                    for (File file : toCopy) {
                        String relPath = FileUtilities.getRelativePath(dirFile, file);
                        if (relPath == null) {
                            return false;
                        }
                        String targetPath = r.getTargetPath();
                        if (targetPath != null && targetPath.trim().length() > 0) {
                            if (!targetPath.endsWith("/")) {
                                targetPath = targetPath + "/";
                            }
                            relPath = targetPath + relPath;
                        }
  //                      System.out.println("relpath=" + relPath);
                        FileObject fo = outputDir.getFileObject(relPath);
                        if (fo == null) {
                            File outFile = FileUtil.normalizeFile(new File(FileUtil.toFile(outputDir), relPath));
                            try {
                                fo = FileUtil.createData(outFile);
                            } catch (IOException ex) {
                                //#162180, #164748
                                //well, just skip the resource with some logging..
                                Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Cannot create file " + file + ", skipping copying of resource for Compile on Save.", ex); //NOI18N
                            }
                        }
                        if (fo == null) {
                            continue;
                        }
                        FileObject sourceFO = FileUtil.toFileObject(file);
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            in = sourceFO.getInputStream();
                            out = fo.getOutputStream();
                            FileUtil.copy(in, out);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            IOUtil.close(in);
                            IOUtil.close(out);
                        }
                    }
                } else {
                    //the case of filtering source roots, here we want to return false
                    //to skip CoS altogether.
                    return false;
                }
            }
        }
        return true;
    }


    private boolean checkRunMainClass(RunConfig config) {
        String actionName = config.getActionName();
        //compile on save stuff
        if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
            if ((NbMavenProject.TYPE_JAR.equals(
                    config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType()) &&
                    (ActionProvider.COMMAND_RUN.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG.equals(actionName)) ||
                    RUN_MAIN.equals(actionName) ||
                    DEBUG_MAIN.equals(actionName))) {
                long stamp = getLastCoSLastTouch(config, true);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (checkImportantFiles(stamp, config)) {
                    return true;
                }
                //check the COS timestamp against resources etc.
                //if changed, perform part of the maven build. (or skip COS)
                if (!checkAndCopyResources(false, stamp, config)) {
                    //we have some filtered resources modified or encountered other problem,
                    //skip CoS
                    return true;
                }

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName());
                params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
                if (RUN_MAIN.equals(actionName) ||
                    DEBUG_MAIN.equals(actionName)) {
                    params.put(JavaRunner.PROP_EXECUTE_FILE, config.getSelectedFileObject());
                } else {
                    //only for the case of running the project itself, relevant for the run/debug-project action and
                    //jar packaging
                    params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, createRuntimeClassPath(config.getMavenProject(), false));
                }
                //exec:exec property
                String exargs = config.getProperties().getProperty("exec.args"); //NOI18N
                if (exargs != null) {
                    String[] args = RunJarPanel.splitAll(exargs);
                    if (params.get(JavaRunner.PROP_EXECUTE_FILE) == null) {
                        params.put(JavaRunner.PROP_CLASSNAME, args[1]);
                    }
                    String[] appargs = args[2].split(" ");
                    params.put(JavaRunner.PROP_APPLICATION_ARGS, Arrays.asList(appargs));
                    try {
                        //jvm args, add and for debugging, remove the debugging ones..
                        params.put(JavaRunner.PROP_RUN_JVMARGS, extractDebugJVMOptions(args[2]));
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (params.get(JavaRunner.PROP_EXECUTE_FILE) != null ||
                        params.get(JavaRunner.PROP_CLASSNAME) != null) {
                    String action2Quick = action2Quick(actionName);
                    boolean supported = JavaRunner.isSupported(action2Quick, params);
                    if (supported) {
                        try {
                            JavaRunner.execute(action2Quick, params);
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

    private boolean checkRunTest(RunConfig config) {
        String actionName = config.getActionName();
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
            Map<String, Object> params = new HashMap<String, Object>();
            String test = config.getProperties().getProperty("test"); //NOI18N
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
            if (!checkAndCopyResources(true, stamp, config)) {
                //we have some filtered resources modified, skip CoS
                return true;
            }

            //#
            FileObject selected = config.getSelectedFileObject();
            ClassPathProviderImpl cpp = config.getProject().getLookup().lookup(ClassPathProviderImpl.class);
            ClassPath srcs = cpp.getProjectSourcesClassPath(ClassPath.SOURCE);
            String path = srcs.getResourceName(selected);
            if (path != null) {
                //now we have a source file, need to convert to testSource..
                String nameExt = selected.getNameExt().replace(".java", "Test.java"); //NOI18N
                path = path.replace(selected.getNameExt(), nameExt);
                ClassPath[] cps = cpp.getProjectClassPaths(ClassPath.SOURCE);
                ClassPath cp = ClassPathSupport.createProxyClassPath(cps);
                FileObject testFo = cp.findResource(path);
                if (testFo != null) {
                    selected = testFo;
                }
            }
            params.put(JavaRunner.PROP_EXECUTE_FILE, selected);
            
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
                    if ("jpda.listen".equals(entry.getKey())) {//NOI18N
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
                argLine = config.getProperties().getProperty("argLine");
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
            }
        }
        return true;
    }

    static void cleanGeneratedClassfiles(RunConfig config) throws IOException { // #145243
        //we execute normal maven build, but need to clean any
        // CoS classes present.
        deleteCoSTimeStamp(config, true);
        deleteCoSTimeStamp(config, false);
        Project p = config.getProject();
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
        } else {
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
        return touchCoSTimeStamp(rc, test, System.currentTimeMillis());
    }

    private boolean touchCoSTimeStamp(RunConfig rc, boolean test, long stamp) {
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
        } 
        return check.setLastModified(stamp);
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
        } else if (ActionProvider.COMMAND_TEST.equals(actionName) || ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST;
        } else if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST_DEBUG;
        }
        assert false : "Cannot convert " + actionName + " to quick actions.";
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
