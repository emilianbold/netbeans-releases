/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.ruby.platform.execution.RubyLineConvertorFactory;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunner;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.MainClassWarning;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;
import org.openide.util.EditableProperties;


/**
 * Action provider of the Ruby project.
 */
public final class RubyActionProvider extends RubyBaseActionProvider {
    
    /**
     * Standard command for running the IRB console on a project
     */
    public static final String COMMAND_IRB_CONSOLE = "irb-console"; // NOI18N
    
    // Commands available from Ruby project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_AUTOTEST,
        COMMAND_AUTOSPEC,
        COMMAND_RDOC,
        COMMAND_IRB_CONSOLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_TEST,
        COMMAND_RSPEC,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };

    private final static String[] MIME_TYPES = new String[] {
        RubyInstallation.RUBY_MIME_TYPE
    };
    
    final RubyProject project;
    
    public RubyActionProvider(RubyProject project, UpdateHelper updateHelper) {
        super(project, updateHelper);
        this.project = project;
    }

    @Override
    protected FileObject[] getSourceRoots() {
        return project.getSourceRoots().getRoots();
    }

    @Override
    protected FileObject[] getTestSourceRoots() {
        return project.getTestSourceRoots().getRoots();
    }

    @Override
    protected String[] getMimeTypes() {
        return MIME_TYPES;
    }

    public String[] getSupportedActions() {
        return supportedActions;
    }

    public RubyExecutionDescriptor getScriptDescriptor(File pwd, FileObject fileObject, String target,
            String displayName, final Lookup context, final boolean debug,
            LineConvertor... extraConvertors) {
    
        String rubyOptions = SharedRubyProjectProperties.getRubyOptions(project);

        String includePath = RubyProjectUtil.getLoadPath(project);
        if (rubyOptions != null) {
            rubyOptions = includePath + " " + rubyOptions; // NOI18N
        } else {
            rubyOptions = includePath;
        }
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        FileObject[] testPath = project.getTestSourceRoots().getRoots();
        
        target = locate(target, srcPath, testPath);
        
        if (pwd == null) {
            String runDir = project.evaluator().getProperty(RubyProjectProperties.RUN_WORK_DIR);
            pwd = getSourceFolder();
            if (runDir != null && runDir.length() > 0) {
                File dir = new File(runDir);                
                if (!dir.exists()) {
                    // Is it relative to the project directory?
                    dir = new File(FileUtil.toFile(project.getProjectDirectory()), runDir);
                    if (!dir.exists()) {
                        // Could it be relative to one of the source folders?
                        if (srcPath != null && srcPath.length > 0) {
                            for (FileObject root : srcPath) {
                                dir = new File(FileUtil.toFile(root), runDir);
                                if (dir.exists()) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (dir.exists()) {
                    pwd = dir;
                }
            }
        }
        
        String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
        String jvmArgs = project.evaluator().getProperty(RubyProjectProperties.JVM_ARGS);

        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(getPlatform(), displayName, pwd, target);
        desc.debug(debug);
        desc.showSuspended(true);
        desc.allowInput();
        desc.fileObject(fileObject);
        desc.jvmArguments(jvmArgs);
        desc.initialArgs(rubyOptions);
        desc.classPath(classPath);
        desc.additionalArgs(getApplicationArguments());
        desc.fileLocator(new RubyFileLocator(context, project));
        desc.addStandardRecognizers();
        desc.addOutConvertor(LineConvertors.filePattern(desc.getFileLocator(),
                RubyLineConvertorFactory.RUBY_TEST_OUTPUT,
                RubyLineConvertorFactory.EXT_RE
                ,1,2));
        desc.addErrConvertor(LineConvertors.filePattern(desc.getFileLocator(),
                RubyLineConvertorFactory.RUBY_TEST_OUTPUT,
                RubyLineConvertorFactory.EXT_RE
                ,1,2));
        desc.setEncoding(getSourceEncoding());
        
        if (extraConvertors != null) {
            for (LineConvertor extra : extraConvertors) {
                desc.addOutConvertor(extra);
            }
        }

        RubyCoverageProvider coverageProvider = RubyCoverageProvider.get(project);
        if (coverageProvider != null && coverageProvider.isEnabled()) {
            desc = coverageProvider.wrapWithCoverage(desc, false, null);
        }

        return desc;
    }
    
    private String locate(String target, final FileObject[] srcPath, final FileObject[] testPath) {
        // Locate the target and specify it by full path. This is necessary
        // because JRuby and Ruby don't locate the script from the load path it
        // seems.
        if (!new File(target).exists() && srcPath != null && srcPath.length > 0) {
            boolean found = false; // Prefer the first match
            for (FileObject root : srcPath) {
                FileObject fo = root.getFileObject(target);
                if (fo != null) {
                    target = FileUtil.toFile(fo).getAbsolutePath();
                    found = true;
                    break;
                }
            }
            if (!found && testPath != null) {
                for (FileObject root : testPath) {
                    FileObject fo = root.getFileObject(target);
                    if (fo != null) {
                        target = FileUtil.toFile(fo).getAbsolutePath();
                        break;
                    }
                }
            }
        }
        return target;
    }

    private void openIrbConsole(Lookup context) {
        RubyPlatform platform = getPlatform();
        String irbPath = platform.findExecutable("irb"); // NOI18N
        if (irbPath == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String displayName = NbBundle.getMessage(RubyActionProvider.class, "CTL_IrbTopComponent");
        File pwd = FileUtil.toFile(project.getProjectDirectory());
        String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);

        RubyExecutionDescriptor desc =
        new RubyExecutionDescriptor(platform, displayName, pwd, irbPath).
                showSuspended(false).
                showProgress(false).
                classPath(classPath).
                allowInput().
                additionalArgs("--simple-prompt", "--noreadline"). // NOI18N
                //additionalArgs(getApplicationArguments()).
                fileLocator(new RubyFileLocator(context, project)).
                addStandardRecognizers();

        RubyProcessCreator rpc = new RubyProcessCreator(desc, getSourceEncoding());
        ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName).run();
    }
    
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        // Initialize the configuration: find a way to pass this to the launched child process!
        //RubyConfigurationProvider.Config c = context.lookup(RubyConfigurationProvider.Config.class);
        //if (c != null) {
        //    String config;
        //    if (c.name != null) {
        //        config = c.name;
        //    } else {
        //        // Invalid but overrides any valid setting in config.properties.
        //        config = "";
        //    }
        //    Properties p = new Properties();
        //    p.setProperty(RubyConfigurationProvider.PROP_CONFIG, config);
        //    TODO: Somehow pass the properties to the launched process, and have it digest it
        //}

        RubyPlatform platform = RubyPlatform.platformFor(project);
        assert platform != null : "Action '" + command + "' should be disabled when platform is invalid";
        
        // TODO Check for valid installation of Ruby and Rake
        if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command)) {
            if (!platform.isValid(true)) {
                return;
            }

            String config = project.evaluator().getProperty(RubyConfigurationProvider.PROP_CONFIG);
            String path;
            if (config == null || config.length() == 0) {
                path = RakeProjectHelper.PROJECT_PROPERTIES_PATH;
            } else {
                // Set main class for a particular config only.
                path = "nbproject/configs/" + config + ".properties"; // NOI18N
            }
            EditableProperties ep = getUpdateHelper().getProperties(path);

            // check project's main class
            // Check whether main class is defined in this config. Note that we use the evaluator,
            // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
            // to define a main class - in this case an active config need not override it.
            String mainClass = project.evaluator().getProperty(RubyProjectProperties.MAIN_CLASS);
            MainClassStatus result = isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
            if (context.lookup(RubyConfigurationProvider.Config.class) != null) {
                // If a specific config was selected, just skip this check for now.
                // XXX would ideally check that that config in fact had a main class.
                // But then evaluator.getProperty(MAIN_CLASS) would be inaccurate.
                // Solvable but punt on it for now.
                result = MainClassStatus.SET_AND_VALID;
            }
            if (result != MainClassStatus.SET_AND_VALID) {
                do {
                    // show warning, if cancel then return
                    if (showMainClassWarning (mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep,result)) {
                        return;
                    }
                    // No longer use the evaluator: have not called putProperties yet so it would not work.
                    mainClass = ep.get(RubyProjectProperties.MAIN_CLASS);
                    result=isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
                } while (result != MainClassStatus.SET_AND_VALID);
                try {
                    if (getUpdateHelper().requestSave()) {
                        getUpdateHelper().putProperties(path, ep);
                        ProjectManager.getDefault().saveProject(project);
                    }
                    else {
                        return;
                    }
                } catch (IOException ioe) {           
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe); // NOI18N
                }
            }

            // Save all files first
            LifecycleManager.getDefault().saveAll();
            
            String displayName = (mainClass != null) ? 
                NbBundle.getMessage(RubyActionProvider.class, "Ruby") :
                NbBundle.getMessage(RubyActionProvider.class, "Rake");

            ProjectInformation info = ProjectUtils.getInformation(project);
            if (info != null) {
                displayName = info.getDisplayName();
            }

            if (mainClass != null) {
                // TODO - compute mainclass
                FileObject fileObject = null;
                runRubyScript(fileObject, mainClass, displayName, context, COMMAND_DEBUG.equals(command));
                return;
            }

            // Default to running rake
            if (!platform.hasRubyGemsInstalled(true) || !platform.hasValidRake(true)) {
                return;
            }
            
            RubyFileLocator fileLocator = new RubyFileLocator(context, project);
            File pwd = getSourceFolder(); // Or project directory?
            String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
            RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform, displayName, pwd, platform.getRake()).
                    fileLocator(fileLocator).
                    allowInput().
                    classPath(classPath).
                    appendJdkToPath(platform.isJRuby()).
                    addStandardRecognizers().
                    addErrConvertor(LineConvertors.filePattern(fileLocator,
                        RubyLineConvertorFactory.RUBY_TEST_OUTPUT,
                        RubyLineConvertorFactory.EXT_RE, 1, 2)).
                    addOutConvertor(LineConvertors.filePattern(fileLocator,
                        RubyLineConvertorFactory.RUBY_TEST_OUTPUT,
                        RubyLineConvertorFactory.EXT_RE, 1, 2));

            RubyCoverageProvider coverageProvider = RubyCoverageProvider.get(project);
            if (coverageProvider != null && coverageProvider.isEnabled()) {
                desc = coverageProvider.wrapWithCoverage(desc, false, null);
            }

            RubyProcessCreator rpc = new RubyProcessCreator(desc, getSourceEncoding());
            ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName);

            return;
        } else if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command)) {
            if (!platform.isValid(true)) {
                return;
            }

            FileObject file = getCurrentFile(context);

            if (RakeSupport.isRakeFile(file)) {
                if (!platform.hasValidRake(true)) {
                    return;
                }

                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                RakeRunner runner = new RakeRunner(project);
                runner.setRakeFile(file);
                runner.setFileLocator(new RubyFileLocator(context, project));
                runner.showWarnings(true);
                runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
                runner.run();
                return;
            }
            
            RSpecSupport rspec = new RSpecSupport(project);
            if (rspec.isRSpecInstalled() && RSpecSupport.isSpecFile(file)) {
                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                TestRunner rspecRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
                if (rspecRunner != null) {
                    rspecRunner.runTest(file, COMMAND_DEBUG_SINGLE.equals(command));
                } else {
                    rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context, project), true,
                            COMMAND_DEBUG_SINGLE.equals(command));
                }
                return;
            }
            
            saveFile(file);
            
            //String target = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
            if (file.getName().endsWith("_test")) { // NOI18N
                // Run test normally - don't pop up browser
                TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.TEST_UNIT);
                if (testRunner != null) {
                    testRunner.getInstance().runTest(file, COMMAND_DEBUG_SINGLE.equals(command));
                    return;
                }
            }
            runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(),
                    file.getNameExt(), context, COMMAND_DEBUG_SINGLE.equals(command), (LineConvertor) null);
            return;
        } else if (COMMAND_REBUILD.equals(command) || COMMAND_BUILD.equals(command) || COMMAND_CLEAN.equals(command)) {
            RakeRunner runner = new RakeRunner(project);
            runner.showWarnings(true);
            if (COMMAND_REBUILD.equals(command)) {
                runner.run("clean", "gem"); // NOI18N
            } else if (COMMAND_BUILD.equals(command)) {
                runner.run("gem"); // NOI18N
            } else { // if(COMMAND_CLEAN.equals(command)) {
                runner.run("clean"); // NOI18N
            }
            return;
        }
        
        if (COMMAND_RDOC.equals(command)) {
            LifecycleManager.getDefault().saveAll();
            File pwd = FileUtil.toFile(project.getProjectDirectory());

            Runnable showBrowser = new Runnable() {
                public void run() {
                    // TODO - wait for the file to be created
                    // Open brower on the doc directory
                    FileObject doc = project.getProjectDirectory().getFileObject("doc"); // NOI18N
                    if (doc != null) {
                        FileObject index = doc.getFileObject("index.html"); // NOI18N
                        if (index != null) {
                            try {
                                URL url = FileUtil.toFile(index).toURI().toURL();

                                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                            }
                            catch (MalformedURLException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    }
                }
            };
            
            RubyFileLocator fileLocator = new RubyFileLocator(context, project);
            String displayName = NbBundle.getMessage(RubyActionProvider.class, "RubyDocumentation");

            RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform, displayName, pwd).
                    additionalArgs("-r", "rdoc/rdoc", "-e", "begin; r = RDoc::RDoc.new; r.document(ARGV); end"). // NOI18N
                    fileLocator(fileLocator).
                    postBuild(showBrowser).
                    addStandardRecognizers();

            RubyProcessCreator rpc = new RubyProcessCreator(desc, getSourceEncoding());
            ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName).run();
            return;
        }
        
        if (COMMAND_AUTOTEST.equals(command)) {
            if (AutoTestSupport.isInstalled(project, TestType.AUTOTEST)) {
                AutoTestSupport support = new AutoTestSupport(context, project, getSourceEncoding());
                support.setClassPath(project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH));
                support.start(TestType.AUTOTEST);
            }
            
            return;
        }

        if (COMMAND_AUTOSPEC.equals(command)) {
            if (AutoTestSupport.isInstalled(project, TestType.AUTOSPEC)) {
                AutoTestSupport support = new AutoTestSupport(context, project, getSourceEncoding());
                support.setClassPath(project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH));
                support.start(TestType.AUTOSPEC);
            }

            return;
        }
        
        if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
            if (!platform.isValid(true)) {
                return;
            }

            // Run test normally - don't pop up browser
            FileObject file = getCurrentFile(context);
            
            if (file == null) {
                return;
            }

            saveFile(file);

            // If we try to "test" a file that has a corresponding test file,
            // run/debug the test file instead
            DeclarationLocation location = new GotoTest().findTest(file, -1);
            if (location != DeclarationLocation.NONE) {
                file = location.getFileObject();
                // Save the test file too
                saveFile(file);
            }
            
            boolean isDebug = COMMAND_DEBUG_TEST_SINGLE.equals(command);

            RSpecSupport rspec = new RSpecSupport(project);
            if (rspec.isRSpecInstalled() && RSpecSupport.isSpecFile(file)) {
                TestRunner rspecRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
                if (rspecRunner != null) {
                    rspecRunner.runTest(file, isDebug);
                } else {
                    rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context, project), true,
                            isDebug);
                }
                return;
            }
            
            TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.TEST_UNIT);
            if (testRunner != null) {
                testRunner.getInstance().runTest(file, isDebug);
            } else {
                runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(),
                        file.getNameExt(), context, isDebug, new TestNotifierLineConvertor(true, true));
            }
        }

        if (COMMAND_TEST.equals(command)) {
            TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.TEST_UNIT);
            boolean testTaskExist = RakeSupport.getRakeTask(project, TEST_TASK_NAME) != null;
            if (testTaskExist) {
                File pwd = FileUtil.toFile(project.getProjectDirectory());
                RakeRunner runner = new RakeRunner(project);
                runner.setPWD(pwd);
                runner.setFileLocator(new RubyFileLocator(context, project));
                runner.showWarnings(true);
                runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
                runner.run(TEST_TASK_NAME);
            } else if (testRunner != null) {
                testRunner.getInstance().runAllTests(project, false);
            }
            return;
        }
        
        if (COMMAND_RSPEC.equals(command)) {
            boolean rspecTaskExists = RakeSupport.getRakeTask(project, RSPEC_TASK_NAME) != null;
            TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
            if (rspecTaskExists) {
                File pwd = FileUtil.toFile(project.getProjectDirectory());
                RakeRunner runner = new RakeRunner(project);
                runner.setPWD(pwd);
                runner.setFileLocator(new RubyFileLocator(context, project));
                runner.showWarnings(true);
                runner.run(RSPEC_TASK_NAME); // NOI18N
            } else if (testRunner != null) {
                testRunner.getInstance().runAllTests(project, false);
            }
            return;
            
        }

        if (COMMAND_RSPEC_ALL.equals(command)) {
            TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
            if (testRunner != null) {
                testRunner.getInstance().runAllTests(project, false);
            }
            return;
        }

        if (COMMAND_IRB_CONSOLE.equals(command)) {
            openIrbConsole(context);
            return;
        }
        
        
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        }
    }    
    
    public boolean isActionEnabled( String command, Lookup context ) {
        if (getPlatform() == null) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        } else if (command.equals(COMMAND_RUN_SINGLE) ||
                command.equals(COMMAND_DEBUG_SINGLE)) {
            if (RakeSupport.isRakeFileSelected(context)) {
                return true;
            }

            FileObject fos[] = findSources(context);
            if (fos != null && fos.length == 1) {
                return true;
            }
            fos = findTestSources(context);
            return fos != null && fos.length == 1;
        } else {
            // other actions are global
            return true;
        }
    }

    protected FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
        if (srcDir != null) {
            FileObject[] files = findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages or Ruby files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && files[i].getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                        return null;
                    }
                }
            }
            return files;
        } else {
            return null;
        }
    }
    
    private FileObject[] findSourcesAndPackages (Lookup context, FileObject[] srcRoots) {
        for (int i=0; i<srcRoots.length; i++) {
            FileObject[] result = findSourcesAndPackages(context, srcRoots[i]);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    private FileObject getRoot (FileObject[] roots, FileObject file) {
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            assert roots[i] != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }
    
    private static enum MainClassStatus {
        SET_AND_VALID,
        SET_BUT_INVALID,
        UNSET
    }

    /**
     * Tests if the main class is set.
     *
     * @param sourcesRoots source roots
     * @param mainClass main class name
     * @return status code
     */
    private MainClassStatus isSetMainClass(FileObject[] sourcesRoots, String mainClass) {

        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult ? MainClassStatus.SET_AND_VALID : MainClassStatus.SET_BUT_INVALID;
        }

        if (mainClass == null || mainClass.length () == 0) {
            return MainClassStatus.UNSET;
        }
        
        //ClassPath classPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);  //Single compilation unit
        if (RubyProjectUtil.isMainClass (mainClass, sourcesRoots)) {
            return MainClassStatus.SET_AND_VALID;
        }
        return MainClassStatus.SET_BUT_INVALID;
    }
    
    /**
     * Asks user for name of main class.
     *
     * @param mainClass current main class
     * @param projectName the name of project
     * @param ep project.properties to possibly edit
     * @param messgeType type of dialog
     * @return true if user selected main class
     */
    private boolean showMainClassWarning(String mainClass, String projectName, EditableProperties ep, MainClassStatus messageType) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (RubyActionProvider.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (RubyActionProvider.class, "AD_MainClassWarning_ChooseMainClass_OK"));
        
        // main class goes wrong => warning
        String message;
        switch (messageType) {
            case UNSET:
                message = MessageFormat.format (NbBundle.getMessage(RubyActionProvider.class,"LBL_MainClassNotFound"), new Object[] {
                    projectName
                });
                break;
            case SET_BUT_INVALID:
                message = MessageFormat.format (NbBundle.getMessage(RubyActionProvider.class,"LBL_MainClassWrong"), new Object[] {
                    mainClass,
                    projectName
                });
                break;
            default:
                throw new IllegalArgumentException ();
        }
        final MainClassWarning panel = new MainClassWarning (message,project.getSourceRoots().getRoots());
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        
        panel.addChangeListener (new ChangeListener () {
           public void stateChanged (ChangeEvent e) {
               if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                   // click button and the finish dialog with selected class
                   okButton.doClick ();
               } else {
                   okButton.setEnabled (panel.getSelectedMainClass () != null);
               }
           }
        });
        
        okButton.setEnabled (false);
        DialogDescriptor desc = new DialogDescriptor (panel,
            NbBundle.getMessage (RubyActionProvider.class, "CTL_MainClassWarning_Title", ProjectUtils.getInformation(project).getDisplayName()), // NOI18N
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            mainClass = panel.getSelectedMainClass ();
            canceled = false;
            ep.put(RubyProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass);
        }
        dlg.dispose();            

        return canceled;
    }    
    
    // From the ant module - ActionUtils.
    // However, I've modified it to do its search based on mime type rather than
    // file suffixes (since some Ruby files do not use a .rb extension and are
    // discovered based on the initial shebang line)
    
    private File getSourceFolder() {
        // Default to using the project source directory
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        if (srcPath != null && srcPath.length > 0) {
            return FileUtil.toFile(srcPath[0]);
        } else {
            return FileUtil.toFile(project.getProjectDirectory());
        }
    }
    
}
