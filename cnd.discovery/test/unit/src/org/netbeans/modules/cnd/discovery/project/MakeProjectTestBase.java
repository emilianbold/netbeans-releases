/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.WizardDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public abstract class MakeProjectTestBase extends CndBaseTestCase { //extends NbTestCase
    private static final boolean OPTIMIZE_NATIVE_EXECUTIONS =false;
    private static final boolean TRACE = true;

    public MakeProjectTestBase(String name) {
        super(name);
        if (TRACE) {
            System.setProperty("cnd.discovery.trace.projectimport", "true"); // NOI18N
        }
//        System.setProperty("org.netbeans.modules.cnd.makeproject.api.runprofiles", "true"); // NOI18N
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
//        MockServices.setServices(MakeProjectType.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //MockServices.setServices(MakeProjectType.class);
        startupModel();
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(MakeProjectType.class);
        list.addAll(super.getServices());
        return list;
    }
 
    @Override
    protected void setUpMime() {
        // setting up MIME breaks other services
        super.setUpMime();
    }

    private void startupModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.startup();
        RepositoryUtils.cleanCashes();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        shutdownModel();
    }

    private void shutdownModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        waitModelTasks(model);
        model.shutdown();
        waitModelTasks(model);
        RepositoryUtils.cleanCashes();
        RepositoryUtils.debugClear();
    }

    private void waitModelTasks(ModelImpl model) {
        Cancellable task = model.enqueueModelTask(new Runnable() {
            @Override
            public void run() {
            }
        }, "wait finished other tasks"); //NOI18N
        if (task instanceof Task) {
            ((Task) task).waitFinished();
        }
    }

    private File detectConfigure(String path){
        File configure = new File(path, "configure");
        if (configure.exists()) {
            return configure;
        }
        configure = new File(path, "CMakeLists.txt");
        if (configure.exists()) {
            return configure;
        }
        File base = new File(path);
        File[] files = base.listFiles();
        if (files != null){
            for(File file : files) {
                if (file.getAbsolutePath().endsWith(".pro")){
                    return file;
                }
            }
        }
        return new File(path, "configure");
    }

    public void performTestProject(String URL, List<String> additionalScripts, boolean useSunCompilers, final String subFolder){
        Map<String, String> tools = findTools();
        CompilerSet def = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        if (useSunCompilers) {
            if (def != null && def.getCompilerFlavor().isGnuCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()){
                    if (set.getCompilerFlavor().isSunStudioCompiler()) {
                        CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).setDefault(set);
                        break;
                    }
                }
            }
        } else {
            if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()){
                    if (set.getCompilerFlavor().isGnuCompiler()) {
                        CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).setDefault(set);
                        break;
                    }
                }
            }
        }
        def = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        final boolean isSUN = def != null ? def.getCompilerFlavor().isSunStudioCompiler() : false;
        if (tools == null) {
            assertTrue("Please install required tools.", false);
            System.err.println("Test did not run because required tools do not found");
            return;
        }
        try {
            final String path = download(URL, additionalScripts, tools)+subFolder;

            final File configure = detectConfigure(path);
            final File makeFile = new File(path, "Makefile");
            if (!configure.exists()) {
                if (!makeFile.exists()){
                    assertTrue("Cannot find configure or Makefile in folder "+path, false);
                }
            }
            if (Utilities.isWindows()){
                // cygwin does not allow test discovery in real time, so disable tests on windows
                return;
            }

            WizardDescriptor wizard = new WizardDescriptor() {
                @Override
                public synchronized Object getProperty(String name) {
                    if ("simpleMode".equals(name)) {
                        return Boolean.TRUE;
                    } else if ("path".equals(name)) {
                        return path;
                    } else if ("configureName".equals(name)) {
                        if (OPTIMIZE_NATIVE_EXECUTIONS && makeFile.exists()){// && !configure.getAbsolutePath().endsWith("CMakeLists.txt")) {
                            // optimization on developer computer:
                            // run configure only once
                            return null;
                        } else {
                            return configure.getAbsolutePath();
                        }
                    } else if ("realFlags".equals(name)) {
                        if (path.indexOf("cmake-")>0 && subFolder.isEmpty()) {
                            if (isSUN) {
                                return "CMAKE_C_COMPILER=cc CMAKE_CXX_COMPILER=CC CFLAGS=-g CXXFLAGS=-g CMAKE_BUILD_TYPE=Debug CMAKE_CXX_FLAGS_DEBUG=-g CMAKE_C_FLAGS_DEBUG=-g";
                            } else {
                                return "CFLAGS=\"-g3 -gdwarf-2\" CXXFLAGS=\"-g3 -gdwarf-2\" CMAKE_BUILD_TYPE=Debug CMAKE_CXX_FLAGS_DEBUG=\"-g3 -gdwarf-2\" CMAKE_C_FLAGS_DEBUG=\"-g3 -gdwarf-2\"";
                            }
                        } else {
                            if (configure.getAbsolutePath().endsWith("configure")) {
                                if (isSUN) {
                                    return "CC=cc CXX=CC CFLAGS=-g CXXFLAGS=-g";
                                } else {
                                    return "CFLAGS=\"-g3 -gdwarf-2\" CXXFLAGS=\"-g3 -gdwarf-2\"";
                                }
                            } else if (configure.getAbsolutePath().endsWith("CMakeLists.txt")) {
                                if (isSUN) {
                                    return "-G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug -DCMAKE_C_COMPILER=cc -DCMAKE_CXX_COMPILER=CC -DCMAKE_CXX_FLAGS_DEBUG=-g -DCMAKE_C_FLAGS_DEBUG=-g";
                                } else {
                                    return "-G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug -DCMAKE_CXX_FLAGS_DEBUG=\"-g3 -gdwarf-2\" -DCMAKE_C_FLAGS_DEBUG=\"-g3 -gdwarf-2\"";
                                }
                            } else if (configure.getAbsolutePath().endsWith(".pro")) {
                                if (isSUN) {
                                    return "-spec solaris-cc QMAKE_CC=cc QMAKE_CXX=CC QMAKE_CFLAGS=-g QMAKE_CXXFLAGS=-g";
                                } else {
                                    if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                                        return "-spec macx-g++ QMAKE_CFLAGS=\"-g3 -gdwarf-2\" QMAKE_CXXFLAGS=\"-g3 -gdwarf-2\"";
                                    } else {
                                        if (Utilities.isWindows()) {
                                            for (CompilerSet set : CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()){
                                                if (set.getCompilerFlavor().getToolchainDescriptor().getName().startsWith("MinGW")) {
                                                    CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).setDefault(set);
                                                    break;
                                                }
                                            }
                                        }
                                        return "QMAKE_CFLAGS=\"-g3 -gdwarf-2\" QMAKE_CXXFLAGS=\"-g3 -gdwarf-2\"";
                                    }
                                }
                            }
                        }
                    } else if ("buildProject".equals(name)) {
                        if (OPTIMIZE_NATIVE_EXECUTIONS && makeFile.exists() && findObjectFiles(path)) {
                            // optimization on developer computer:
                            // make only once
                            return Boolean.FALSE;
                        } else {
                            return Boolean.TRUE;
                        }
                    }
                    return null;
                }
            };

            ImportProject importer = new ImportProject(wizard);
            importer.setUILessMode();
            importer.create();
            OpenProjects.getDefault().open(new Project[]{importer.getProject()}, false);
            int i = 0;
            while(!importer.isFinished()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (i > 10 && !OpenProjects.getDefault().isProjectOpen(importer.getProject())){
                    break;
                }
                i++;
            }
            assertEquals("Failed configure", ImportProject.State.Successful, importer.getState().get(ImportProject.Step.Configure));
            assertEquals("Failed build", ImportProject.State.Successful, importer.getState().get(ImportProject.Step.Make));
            CsmModel model = CsmModelAccessor.getModel();
            Project makeProject = importer.getProject();
            assertTrue("Not found model", model != null);
            assertTrue("Not found make project", makeProject != null);
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            assertTrue("Not found native project", np != null);
            CsmProject csmProject = model.getProject(np);
            assertTrue("Not found model project", csmProject != null);
            csmProject.waitParse();
            perform(csmProject);
            OpenProjects.getDefault().close(new Project[]{makeProject});
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            assertTrue(ex.getMessage(), false);
        }
    }

    private boolean findObjectFiles(String path){
        return findObjectFiles(new File(path));
    }

    private boolean findObjectFiles(File file){
        if (file.isDirectory()) {
            for(File f : file.listFiles()){
                if (f.isDirectory()) {
                    boolean b = findObjectFiles(f);
                    if (b) {
                        return true;
                    }
                } else if (f.isFile() && f.getName().endsWith(".o")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected List<String> requiredTools(){
        List<String> list = new ArrayList<String>();
        list.add("wget");
        list.add("gzip");
        list.add("tar");
        list.add("rm");
        return list;
    }

    private Map<String, String> findTools(){
        Map<String, String> map = new HashMap<String, String>();
        for(String t: requiredTools()){
            map.put(t, null);
        }
        if (findTools(map)){
            return map;
        }
        return null;
    }

    private boolean findTools(Map<String, String> map){
        if (map.isEmpty()) {
            return true;
        }
        for (String path : Path.getPath()) {
            for(Map.Entry<String, String> entry : map.entrySet()){
                if (entry.getValue() == null) {
                    String task = path+File.separatorChar+entry.getKey();
                    File tool = new File(task);
                    if (tool.exists() && tool.isFile()) {
                        entry.setValue(task);
                    } else if (Utilities.isWindows()) {
                        task = task+".exe";
                        tool = new File(task);
                        if (tool.exists() && tool.isFile()) {
                            entry.setValue(task);
                        }   
                    }
                }
            }
        }
        boolean res = true;
        for(Map.Entry<String, String> entry : map.entrySet()){
           if (entry.getValue() == null) {
               System.err.println("Not found required tool: "+entry.getKey());
               res =false;
           } else {
               System.err.println("Found required tool: "+entry.getKey()+"="+entry.getValue());
           }
        }
        return res;
    }

    protected void perform(CsmProject csmProject) {
        csmProject.waitParse();
        Collection<CsmFile> col = csmProject.getAllFiles();
        if (TRACE) {
            System.err.println("Model has "+col.size()+" files");
        }
        for (CsmFile file : col) {
            if (TRACE) {
                //System.err.println("\t"+file.getAbsolutePath());
            }
            for(CsmInclude include : file.getIncludes()){
                assertTrue("Not resolved include directive "+include.getIncludeName()+" in file "+file.getAbsolutePath(), include.getIncludeFile() != null);
            }
        }
    }

    private String download(String urlName, List<String> additionalScripts, Map<String, String> tools) throws IOException {
        String zipName = urlName.substring(urlName.lastIndexOf(File.separator)+1);
        String tarName = zipName.substring(0, zipName.lastIndexOf('.'));
        String packageName = tarName.substring(0, tarName.lastIndexOf('.'));
        File fileDataPath = CndCoreTestUtils.getDownloadBase();
        String dataPath = fileDataPath.getAbsolutePath();

        File fileCreatedFolder = new File(fileDataPath, packageName);
        String createdFolder = fileCreatedFolder.getAbsolutePath();
        if (!fileCreatedFolder.exists()){
            fileCreatedFolder.mkdirs();
        } else {
            if (!OPTIMIZE_NATIVE_EXECUTIONS) {
                execute(tools, "rm", dataPath, "-rf", packageName);
                fileCreatedFolder.mkdirs();
            }
        }
        if (fileCreatedFolder.list().length == 0){
            if (!new File(fileDataPath, tarName).exists()) {
                execute(tools, "wget", dataPath, urlName);
                execute(tools, "gzip", dataPath, "-d", zipName);
            }
            execute(tools, "tar", dataPath, "xf", tarName);
            execAdditionalScripts(createdFolder, additionalScripts, tools);
        } else {
            final File configure = new File(fileCreatedFolder, "configure");
            final File makeFile = detectConfigure(createdFolder);
            if (!configure.exists()) {
                if (!makeFile.exists()){
                    execAdditionalScripts(createdFolder, additionalScripts, tools);
                }
            }
        }
        execute(tools, "rm", createdFolder, "-rf", "nbproject");
        return createdFolder;
    }

    private void execute(Map<String, String> tools, String cmd, String folder, String ... arguments){
        String command = tools.get(cmd);
        StringBuilder buf = new StringBuilder();
        for(String arg : arguments) {
            buf.append(' ');
            buf.append(arg);
        }
        System.err.println(folder+"#"+command+buf.toString());
        NativeProcessBuilder ne = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal())
        .setWorkingDirectory(folder)
        .setExecutable(command)
        .setArguments(arguments);
        ne.redirectError();
        waitExecution(ne);
    }

    private void execAdditionalScripts(String createdFolder, List<String> additionalScripts, Map<String, String> tools) throws IOException {
        if (additionalScripts != null) {
            for(String s: additionalScripts){
                int i = s.indexOf(' ');
                String command = s.substring(0,i);
                String arguments = s.substring(i+1);
                command = tools.get(command);
                System.err.println(createdFolder+"#"+command+" "+arguments);
                //NativeExecutor ne = new NativeExecutor(createdFolder, tools.get(command), arguments, new String[0], command, "run", false, false);
                NativeProcessBuilder ne = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal())
                .setWorkingDirectory(createdFolder)
                .setCommandLine(command+" "+arguments);
                waitExecution(ne);
            }
        }
    }

    private void waitExecution(NativeProcessBuilder ne){
        try {
            NativeProcess process = ne.call();
            int rc = process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else {
                        System.out.println(line);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                reader.close();
            }
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else {
                        System.out.println(line);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                reader.close();
            }

        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
