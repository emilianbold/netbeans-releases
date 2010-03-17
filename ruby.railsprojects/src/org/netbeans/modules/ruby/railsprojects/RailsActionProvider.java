/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyLineConvertorFactory;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.rubyproject.Migrations;
import org.netbeans.modules.ruby.railsprojects.server.RailsServerManager;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.GotoTest;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.RubyBaseActionProvider;
import org.netbeans.modules.ruby.rubyproject.RubyFileLocator;
import org.netbeans.modules.ruby.rubyproject.RubyProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.TestNotifierLineConvertor;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunner;
import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** 
 * Action provider of the Ruby project.
 */
public final class RailsActionProvider extends RubyBaseActionProvider {

    private static final Logger LOGGER = Logger.getLogger(RailsActionProvider.class.getName());
    
    /**
     * Standard command for running the Rails console for a project
     */
    public static final String COMMAND_RAILS_CONSOLE = "rails-console"; // NOI18N

    // Commands available from Ruby project
    private static final String[] supportedActions = {
        COMMAND_AUTOTEST,
        COMMAND_AUTOSPEC,
        COMMAND_RDOC,
        COMMAND_RAILS_CONSOLE,
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
        RubyInstallation.RUBY_MIME_TYPE,
        RhtmlTokenId.MIME_TYPE
    };

    
    final RailsProject project;
    
    public RailsActionProvider(RailsProject project, UpdateHelper updateHelper) {
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

    /** Return true iff the given file is a migration file */
    private boolean isMigrationFile(FileObject file) {
        if (file.getParent() == null || !file.getParent().getName().equals("migrate")) { // NOI18N
            return false;
        }
        if (file.getParent().getParent() == null || !file.getParent().getParent().getName().equals("db")) { // NOI18N
            return false;
        }
        
        if (!file.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }
        
        return Migrations.getMigrationVersion(file.getName()) != null;
    }
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        // TODO Check for valid installation of Ruby and Rake
        RubyPlatform platform = RubyPlatform.platformFor(project);
        assert platform != null : "Action '" + command + "' should be disabled when platform is invalid";

        boolean debugCommand = COMMAND_DEBUG.equals(command);
        boolean debugSingleCommand = COMMAND_DEBUG_SINGLE.equals(command);
        if (COMMAND_RUN.equals(command) || debugCommand) {
            // Save all files first
            LifecycleManager.getDefault().saveAll();
            runServer("", debugCommand);
            return;
        } else if (COMMAND_TEST.equals(command)) {
            TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.TEST_UNIT);
            boolean testTaskExist = RakeSupport.getRakeTask(project, TEST_TASK_NAME) != null;
            if (testTaskExist) {
                File pwd = FileUtil.toFile(project.getProjectDirectory());
                RakeRunner runner = new RakeRunner(project);
                runner.setPWD(pwd);
                runner.setFileLocator(new RailsFileLocator(context, project));
                runner.showWarnings(true);
                runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
                runner.run(TEST_TASK_NAME); // NOI18N
            } else if (testRunner != null) {
                testRunner.getInstance().runAllTests(project, false);
            }
            return;
        } else if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
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
            } else if (RubyUtils.isRhtmlFile(file)) {
                // Can't run RHTML files if there's no corresponding test
                return;
            }

            boolean isDebug = COMMAND_DEBUG_TEST_SINGLE.equals(command);
 
            RSpecSupport rspec = new RSpecSupport(project);
            if (rspec.isRSpecInstalled() && RSpecSupport.isSpecFile(file)) {
                TestRunner rspecRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
                if (rspecRunner != null) {
                    rspecRunner.runTest(file, isDebug);
                } else {
                    rspec.runRSpec(null, file, file.getName(), new RailsFileLocator(context, project), true, isDebug);
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
            return;

        } else if (COMMAND_RUN_SINGLE.equals(command) || debugSingleCommand) {
            if (!platform.isValid(true)) {
                return;
            }

            FileObject file = getCurrentFile(context);

            if (file == null) {
                return;
            }
            
            if (RakeSupport.isRakeFile(file)) {
                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                RakeRunner runner = new RakeRunner(project);
                runner.setRakeFile(file);
                runner.setFileLocator(new RailsFileLocator(context, project));
                runner.showWarnings(true);
                runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
                runner.run();
                return;
            }
            
            RSpecSupport rspec = new RSpecSupport(project);
            if (rspec.isRSpecInstalled() && RSpecSupport.isSpecFile(file)) {
                TestRunner rspecRunner = Util.getTestRunner(TestRunner.TestType.RSPEC);
                boolean debug = COMMAND_DEBUG_SINGLE.equals(command);
                if (rspecRunner != null) {
                    saveFile(file);
                    rspecRunner.runTest(file, debug);
                } else {
                    // Save all files first - this rake file could be accessing other files
                    LifecycleManager.getDefault().saveAll();
                    rspec.runRSpec(null, file, file.getName(), new RailsFileLocator(context, project), true, debugSingleCommand);
                }
                return;
            }
            
            saveFile(file);
            
            if (isMigrationFile(file)) {
                String name = file.getName();
                Long version = Migrations.getMigrationVersion(name);
                RakeRunner runner = new RakeRunner(project);
                runner.setPWD(FileUtil.toFile(project.getProjectDirectory()));
                runner.setFileLocator(new RailsFileLocator(context, project));
                runner.showWarnings(true);
                runner.setParameters("VERSION=" + version); // NOI18N
                runner.run("db:migrate"); // NOI18N
                return;
            }
            
            // Try to bring up the relevant URL
            String path = "";
            String fileName = file.getName();
            final String CONTROLLER_SUFFIX = "_controller"; // NOI18N
            final String HELPER_SUFFIX = "_helper"; // NOI18N

            if (file.getExt().equals("rhtml") || file.getExt().equals("erb")) { // NOI18N
                if (fileName.endsWith(".html")) { // .html.erb  // NOI18N
                    fileName = fileName.substring(0, fileName.length()-".html".length()); // NOI18N
                }
                if (!fileName.startsWith("_")) { // NOI18N
                    // For partials like "_foo", just use the surrounding view
                    path = fileName;
                }
                FileObject projDir = project.getProjectDirectory();
                FileObject curr = file.getParent();
                while (curr != null && curr != projDir) {
                    if (curr.getName().equals("views") && // NOI18N
                            (curr.getParent() == null || curr.getParent().getName().equals("app"))) { // NOI18N
                        break;
                    }
                    path = curr.getNameExt() + "/" + path; // NOI18N
                    curr = curr.getParent();
                }
            } else if (fileName.endsWith(CONTROLLER_SUFFIX)) {
                path = fileName.substring(0, fileName.length()-CONTROLLER_SUFFIX.length());
                FileObject projDir = project.getProjectDirectory();
                FileObject app = file.getParent();
                while (app != null && app != projDir) {
                    if (app.getName().equals("controllers") && // NOI18N
                            (app.getParent() == null || app.getParent().getName().equals("app"))) { // NOI18N
                        app = app.getParent();
                        break;
                    }
                    path = app.getNameExt() + "/" + path; // NOI18N
                    app = app.getParent();
                }

                // Try to find out which view we're in
                JTextComponent pane = GsfUtilities.getOpenPane();

                if (app != null && pane != null && pane.getCaret() != null) {
                    FileObject fo = GsfUtilities.findFileObject(pane);

                    if (fo != null) {
                        int offset = pane.getCaret().getDot();
                        if (offset >= 0) {
                            String methodName = AstUtilities.getMethodName(file, offset);
                            if (methodName != null) {
                                // Make sure that this corresponds to a valid view - it could
                                // be an unrelated method in the controller
                                // - but what if it's a method which does rendering??
//                                String[] exts = { ".rhtml", ".html.erb", ".erb" }; // NOI18N
//                                for (String ext : exts) {
//                                    FileObject viewFile = app.getFileObject("views/" + path + // NOI18N
//                                            "/" + methodName + ext); // NOI18N
//                                    if (viewFile != null) {
//                                        path = path + "/" + methodName; // NOI18N
//                                        break;
//                                    }
//                                }
                                
                                path = path + "/" + methodName; // NOI18N
                            }
                        }
                    }                
                }
            } else if (fileName.endsWith(HELPER_SUFFIX)) {
                path = fileName.substring(0, fileName.length()-HELPER_SUFFIX.length());
                FileObject projDir = project.getProjectDirectory();
                FileObject curr = file.getParent();
                while (curr != null && curr != projDir) {
                    if (curr.getName().equals("helpers") && // NOI18N
                            (curr.getParent() == null || curr.getParent().getName().equals("app"))) { // NOI18N
                        break;
                    }
                    path = curr.getNameExt() + "/" + path; // NOI18N
                    curr = curr.getParent();
                }
            //} else if (parentName.equals("models")) { // NOI18N
            //    // XXX What do we do here?
            } else if (fileName.endsWith("_test")) { // NOI18N
                // Run test normally - don't pop up browser
                TestRunner testRunner = Util.getTestRunner(TestRunner.TestType.TEST_UNIT);
                if (testRunner != null) {
                    testRunner.getInstance().runTest(file, COMMAND_DEBUG_SINGLE.equals(command));
                } else {
                    runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(), file.getNameExt(), context, debugSingleCommand,
                            new TestNotifierLineConvertor(true, true));
                }
                return;
            }
            
            if (path.length() == 0) {
                // No corresponding URL - some other file we should just try to execute
                runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(), file.getNameExt(), context, debugSingleCommand);
                return;
            }

            runServer(path, debugCommand || debugSingleCommand);
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

        if (COMMAND_AUTOTEST.equals(command)) {
            if (AutoTestSupport.isInstalled(project, TestType.AUTOTEST)) {
                AutoTestSupport support = new AutoTestSupport(context, project, getSourceEncoding());
                support.setClassPath(project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH));
                support.start(TestType.AUTOTEST);
            }
            
            return;
        }
        
        if (COMMAND_AUTOSPEC.equals(command)) {
            if (AutoTestSupport.isInstalled(project, TestType.AUTOSPEC)) {
                AutoTestSupport support = new AutoTestSupport(context, project, getSourceEncoding());
                support.setClassPath(project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH));
                support.start(TestType.AUTOSPEC);
            }

            return;
        }

        if (COMMAND_RAILS_CONSOLE.equals(command)) {
            //File pwd = FileUtil.toFile(project.getProjectDirectory());
            //String cmd = "Dir.chdir('" + pwd.getAbsolutePath() + "');load 'script/console'"; // NOI18N
            //IrbTopComponent component = IrbTopComponent.getProjectConsole(project, cmd);
            //
            //component.open();
            openRailsConsole(context);
            return;
        }
        
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
    }    
    
    private void openRailsConsole(Lookup context) {
        String displayName = NbBundle.getMessage(RailsActionProvider.class, "RailsConsole");
        File pwd = FileUtil.toFile(project.getProjectDirectory());
        String script = null;
        List<String> additionalArgs = new ArrayList<String>();
        boolean rails3 = RailsProjectUtil.getRailsVersion(project).isRails3OrHigher();
        if (rails3) {
             script = "script" + File.separator + "rails"; // NOI18N
             additionalArgs.add("console");
        } else {
            script = "script" + File.separator + "console"; // NOI18N
        }
        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
        // --irb not supported in rails3
        if (!rails3) {
            if (Utilities.isWindows() && !getPlatform().isJRuby()) {
                // see #133066
                additionalArgs.add("--irb=irb.bat --noreadline"); //NOI18N
            } else {
                additionalArgs.add("--irb=irb --noreadline"); //NOI18N
            }
        }
        String railsEnv = project.evaluator().getProperty(RailsProjectProperties.RAILS_ENV);
        if (railsEnv != null && !"".equals(railsEnv.trim())) {
            additionalArgs.add(railsEnv);
        } 
        
        RubyExecutionDescriptor descriptor = new RubyExecutionDescriptor(getPlatform(), displayName, pwd, script).
                showSuspended(false).
                showProgress(false).
                classPath(classPath).
                allowInput().
                // see #130264
                additionalArgs(additionalArgs.toArray(new String[additionalArgs.size()])). //NOI18N
                fileLocator(new RailsFileLocator(context, project));
        descriptor.addStandardRecognizers();
        RubyProcessCreator rpc = new RubyProcessCreator(descriptor, getSourceEncoding());
        ExecutionService.newService(rpc, descriptor.toExecutionDescriptor(), displayName).run();
                
        // request focus for the output window - see #133519
        final String outputWindowId = "output"; //NOI18N
        TopComponent outputWindow = WindowManager.getDefault().findTopComponent(outputWindowId);
        // outputWindow should not be null as the output window id is not likely to change, but 
        // checking for null anyway since we are not relying on an API.
        if (outputWindow != null) {
            outputWindow.requestActive();
        } else {
            LOGGER.info("Could not find the output window using id " + outputWindowId);
        }
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

        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
        String jvmArgs = project.evaluator().getProperty(RailsProjectProperties.JVM_ARGS);
        
        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(getPlatform(), displayName, pwd, target);
        desc.debug(debug);
        desc.showSuspended(true);
        desc.allowInput();
        desc.initialArgs(rubyOptions);
        desc.jvmArguments(jvmArgs);
        desc.classPath(classPath);
        desc.additionalArgs(getApplicationArguments());
        desc.fileLocator(new RailsFileLocator(context, project));
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

        RubyCoverageProvider coverageProvider = RubyCoverageProvider.get(project);
        if (coverageProvider != null && coverageProvider.isEnabled()) {
            desc = coverageProvider.wrapWithCoverage(desc, false, null);
        }

        return desc;
    }
    
    
    public boolean isActionEnabled( String command, Lookup context ) {
        if (getPlatform() == null) {
            return false;
        }
        // We don't require files to be in the source roots to be executable/debuggable;
        // for example, in Rails you may want to switch to the Files view and execute
        // some of the files in scripts/, even though these are not considered sources
        // (and don't have a source root)
        return true;
    }
    
   
    // From the ant module - ActionUtils.
    // However, I've modified it to do its search based on mime type rather than
    // file suffixes (since some Ruby files do not use a .rb extension and are
    // discovered based on the initial shebang line)
    
    private void runServer(final String path, final boolean debug) {
        if (!debug) {
            runServer(path, false, false);
        } else {
            boolean serverDebug;
            boolean clientDebug;
            
            if (!WebClientToolsSessionStarterService.isAvailable()) {
                // Ignore the debugging options if no Javascript debugger is present
                clientDebug = false;
                serverDebug = true;
            } else {
                // show Debug Project dialog
                boolean keepDebugging = WebClientToolsProjectUtils.showDebugDialog(project);
                if (!keepDebugging) {
                    return;
                }

                serverDebug = WebClientToolsProjectUtils.getServerDebugProperty(project);
                clientDebug = WebClientToolsProjectUtils.getClientDebugProperty(project);
            }
            assert serverDebug || clientDebug;
            
            runServer(path, serverDebug, clientDebug);
        }
    }
    
    private void runServer(String path, final boolean serverDebug, final boolean clientDebug) {
        RailsServerManager server = project.getLookup().lookup(RailsServerManager.class);
        if (server != null) {
            server.setDebug(serverDebug);
            server.setClientDebug(clientDebug);
            // use the url from project config if no path is specified
            if (path == null || "".equals(path)) { //NOI18N
                String url = project.evaluator().getProperty(RailsProjectProperties.RAILS_URL);
                if (url != null) {
                    path = url;
                }
            }
            server.showUrl(path);
        }
    }
    
}
