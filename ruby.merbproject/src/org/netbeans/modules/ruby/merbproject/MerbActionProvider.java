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

package org.netbeans.modules.ruby.merbproject;

import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.ruby.merbproject.ui.customizer.MerbProjectProperties;
import org.netbeans.modules.ruby.platform.execution.RubyLineConvertorFactory;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
//import org.netbeans.modules.ruby.rubyproject.GotoTest;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.RubyBaseActionProvider;
import org.netbeans.modules.ruby.rubyproject.RubyFileLocator;
import org.netbeans.modules.ruby.rubyproject.RubyProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.TestNotifierLineConvertor;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunner;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action provider of the Ruby project.
 */
public final class MerbActionProvider extends RubyBaseActionProvider {
    
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
    
    final MerbProject project;
    
    public MerbActionProvider(MerbProject project, UpdateHelper updateHelper) {
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
        // Locate the target and specify it by full path.
        // This is necessary because JRuby and Ruby don't locate the script from the load
        // path it seems.
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

        // For Rails, the execution directory should be the RAILS_ROOT directory
        if (pwd == null) {
            pwd = FileUtil.toFile(project.getProjectDirectory());
        }

        String classPath = project.evaluator().getProperty(SharedRubyProjectProperties.JAVAC_CLASSPATH);
        String jvmArgs = project.evaluator().getProperty(SharedRubyProjectProperties.JVM_ARGS);

        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(getPlatform(), displayName, pwd, target);
        desc.debug(debug);
        desc.showSuspended(true);
        desc.allowInput();
        desc.initialArgs(rubyOptions);
        desc.jvmArguments(jvmArgs);
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
        if (extraConvertors != null) {
            for (LineConvertor extra : extraConvertors) {
                desc.addOutConvertor(extra);
            }
        }

        return desc;
    }

    private void openIrbConsole(Lookup context) {
        RubyPlatform platform = getPlatform();
        String irbPath = platform.findExecutable("irb"); // NOI18N
        if (irbPath == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String displayName = NbBundle.getMessage(MerbActionProvider.class, "CTL_IrbTopComponent");
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

            // Save all files first
            LifecycleManager.getDefault().saveAll();
            runApp(COMMAND_DEBUG.equals(command));
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
                TestRunner rspecRunner = getTestRunner(TestRunner.TestType.RSPEC);
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
                TestRunner testRunner = getTestRunner(TestRunner.TestType.TEST_UNIT);
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
            String displayName = NbBundle.getMessage(MerbActionProvider.class, "RubyDocumentation");

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
            if (AutoTestSupport.isInstalled(project)) {
                AutoTestSupport support = new AutoTestSupport(context, project, getSourceEncoding());
                support.setClassPath(project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH));
                support.start();
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
//            DeclarationLocation location = new GotoTest().findTest(file, -1);
            DeclarationLocation location = DeclarationLocation.NONE;
            if (location != DeclarationLocation.NONE) {
                file = location.getFileObject();
                // Save the test file too
                saveFile(file);
            }
            
            boolean isDebug = COMMAND_DEBUG_TEST_SINGLE.equals(command);

            RSpecSupport rspec = new RSpecSupport(project);
            if (rspec.isRSpecInstalled() && RSpecSupport.isSpecFile(file)) {
                TestRunner rspecRunner = getTestRunner(TestRunner.TestType.RSPEC);
                if (rspecRunner != null) {
                    rspecRunner.runTest(file, isDebug);
                } else {
                    rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context, project), true,
                            isDebug);
                }
                return;
            }
            
            TestRunner testRunner = getTestRunner(TestRunner.TestType.TEST_UNIT);
            if (testRunner != null) {
                testRunner.getInstance().runTest(file, isDebug);
            } else {
                runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(),
                        file.getNameExt(), context, isDebug, new TestNotifierLineConvertor(true, true));
            }
        }

        if (COMMAND_TEST.equals(command)) {
            TestRunner testRunner = getTestRunner(TestRunner.TestType.TEST_UNIT);
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
            TestRunner testRunner = getTestRunner(TestRunner.TestType.RSPEC);
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
    
    private TestRunner getTestRunner(TestRunner.TestType testType) {
        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
        for (TestRunner each : testRunners) {
            if (each.supports(testType)) {
                return each;
            }
        }
        return null;
    }

    private void runApp(boolean debug) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        final String merb = platform.findExecutable("merb"); //NOI18N
        String msgKey = debug ? "DebugMerbApp" : "RunMerbApp"; //NOI18N
        String displayName = NbBundle.getMessage(MerbProjectGenerator.class, msgKey, ProjectUtils.getInformation(project).getDisplayName());
        File pwd = FileUtil.toFile(project.getProjectDirectory());
        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform, displayName, pwd, merb);
        String args = project.evaluator().getProperty(MerbProjectProperties.APPLICATION_ARGS);
        if (args != null) {
            desc.additionalArgs(args);
        }
        desc.debug(debug);
        RubyProcessCreator rpc = new RubyProcessCreator(desc);
        ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName).run();
    }
}
