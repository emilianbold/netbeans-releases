/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.ruby.railsprojects.server.RailsServer;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rubyproject.RakeSupport;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.rubyproject.GotoTest;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.TestNotifier;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** 
 * Action provider of the Ruby project. This is the place where to do
 * strange things to Ruby actions. E.g. compile-single.
 */
public class RailsActionProvider implements ActionProvider {
    
    /**
     * Standard command for running rdoc on a project.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_RDOC = "rdoc"; // NOI18N
    
    /**
     * Command for running auto test on this project (if installed)
     */
    public static final String COMMAND_AUTOTEST = "autotest"; // NOI18N
    
    /**
     * Standard command for running rake migrate on a project
     */
    public static final String COMMAND_RAILS_CONSOLE = "rails-console"; // NOI18N
    
    // Commands available from Ruby project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD,
        COMMAND_AUTOTEST,
        COMMAND_RDOC,
        COMMAND_RAILS_CONSOLE,
        COMMAND_RUN, 
        COMMAND_RUN_SINGLE, 
        COMMAND_DEBUG, 
        COMMAND_DEBUG_SINGLE,
        COMMAND_TEST, 
        COMMAND_TEST_SINGLE, 
        COMMAND_DEBUG_TEST_SINGLE, 
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    
    
    // Project
    RailsProject project;
    
    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
        
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    public RailsActionProvider( RailsProject project, UpdateHelper updateHelper ) {
        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(new String[] {
            COMMAND_RUN, 
            COMMAND_RUN_SINGLE, 
            COMMAND_DEBUG, 
            COMMAND_DEBUG_SINGLE,
        }));
            
        this.updateHelper = updateHelper;
        this.project = project;
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
        
        // 001_whatever.rb
        return file.getName().matches("\\d\\d\\d_.*"); // NOI18N
    }
    
    private FileObject getCurrentFile(Lookup context) {
        FileObject file = null;
        FileObject[] files = findSources(context);
        if (files != null && files.length > 0) {
            file = files[0];
        } else {
            for (DataObject d : context.lookupAll(DataObject.class)) {
                FileObject fo = d.getPrimaryFile();
                if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                    file = fo;
                    break;
                }
            }
        }
        
        return file;
    }
    
    private void saveFile(FileObject file) {
        // Save the file
        try {
            DataObject dobj = DataObject.find(file);
            if (dobj != null) {
                SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    saveCookie.save();
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(donfe);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        // TODO Check for valid installation of Ruby and Rake
        boolean debugCommand = COMMAND_DEBUG.equals(command);
        boolean debugSingleCommand = COMMAND_DEBUG_SINGLE.equals(command);
        if (COMMAND_RUN.equals(command) || debugCommand) {
            // Save all files first
            LifecycleManager.getDefault().saveAll();
            runServer("", debugCommand);
            return;
        } else if (COMMAND_TEST.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }
            // Save all files first
            LifecycleManager.getDefault().saveAll();
            RakeSupport rake = new RakeSupport(project);
            rake.setTest(true);
            File pwd = FileUtil.toFile(project.getProjectDirectory());
            String displayName = NbBundle.getMessage(RailsActionProvider.class, "Tests");
            rake.runRake(pwd, null, displayName, new RubyFileLocator(context), true, "test"); // NOI18N
            return;
        } else if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRuby(true)) {
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
 
            RSpecSupport rspec = new RSpecSupport(project.getProjectDirectory(),
                    project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
            if (rspec.isRSpecInstalled() && rspec.isSpecFile(file)) {
                rspec.setClassPath(project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH));
                rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context), true, isDebug);
                return;
            }
            
            runRubyScript(FileUtil.toFile(file).getAbsolutePath(), file.getNameExt(), context, 
                    isDebug, new OutputRecognizer[] { new TestNotifier() });
            
            return;

        } else if (COMMAND_RUN_SINGLE.equals(command) || debugSingleCommand) {
            if (!RubyInstallation.getInstance().isValidRuby(true)) {
                return;
            }

            FileObject file = getCurrentFile(context);

            if (file == null) {
                return;
            }
            
            if (RakeSupport.isRakeFile(file)) {
                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                RakeSupport rake = new RakeSupport(project);
                rake.runRake(null, file, file.getName(), new RubyFileLocator(context), true);
                return;
            }
            
            RSpecSupport rspec = new RSpecSupport(project.getProjectDirectory(),
                    project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
            if (rspec.isRSpecInstalled() && rspec.isSpecFile(file)) {
                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                rspec.setClassPath(project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH));
                rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context), true, debugSingleCommand);
                return;
            }
            
            saveFile(file);
            
            if (isMigrationFile(file)) {
                String name = file.getName();
                String version = Integer.toString(Integer.parseInt(name.substring(0, 3)));
                RakeSupport rake = new RakeSupport(project);
                rake.runRake(null, file, file.getName(), new RubyFileLocator(context), true, "db:migrate", "VERSION=" + version); // NOI18N
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
                JEditorPane pane = NbUtilities.getOpenPane();

                if (app != null && pane != null && pane.getCaret() != null) {
                    FileObject fo = NbUtilities.findFileObject(pane);

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
                runRubyScript(FileUtil.toFile(file).getAbsolutePath(), file.getNameExt(), context, debugSingleCommand,
                        new OutputRecognizer[] { new TestNotifier() });
                return;
            }
            
            if (path.length() == 0) {
                // No corresponding URL - some other file we should just try to execute
                runRubyScript(FileUtil.toFile(file).getAbsolutePath(), file.getNameExt(), context, debugSingleCommand, null);
                return;
            }

            runServer(path, debugCommand || debugSingleCommand);
            return;
        } else if (COMMAND_BUILD.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }
            
            // Save all files first
            LifecycleManager.getDefault().saveAll();

            // TODO - use RakeSupport

            RubyFileLocator fileLocator = new RubyFileLocator(context);
            String displayName = NbBundle.getMessage(RailsActionProvider.class, "Rake");

            ProjectInformation info = ProjectUtils.getInformation(project);
            if (info != null) {
                displayName = info.getDisplayName();
            }
            
            File pwd = FileUtil.toFile(project.getProjectDirectory());

            String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
  
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    fileLocator(fileLocator).
                    classPath(classPath).
                    addStandardRecognizers().
                    addOutputRecognizer(RubyExecution.RUBY_TEST_OUTPUT),
                    project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            return;
//        } else if (COMMAND_CLEAN.equals(command)) {
//            executeTask(new File(RubyInstallation.getInstance().getRake()),
//                        "Rake", "clean", context, null, null); // TODO - internationalize
//            return;
        }
        
        if (COMMAND_RDOC.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }

            // Run rake appdoc, then open <prj>/doc/app/index.html
            LifecycleManager.getDefault().saveAll();
            File pwd = FileUtil.toFile(project.getProjectDirectory());

            Runnable showBrowser = new Runnable() {
                public void run() {
                    // TODO - wait for the file to be created
                    // Open brower on the doc directory
                    FileObject doc = project.getProjectDirectory().getFileObject("doc/app"); // NOI18N
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
            
            RubyFileLocator fileLocator = new RubyFileLocator(context);
            String displayName = NbBundle.getMessage(RailsActionProvider.class, "RakeDoc");
 
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    additionalArgs("appdoc"). // NOI18N
                    postBuild(showBrowser).
                    fileLocator(fileLocator).
                    addStandardRecognizers(),
                    project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            
            return;
        }

        if (COMMAND_AUTOTEST.equals(command)) {
            if (AutoTestSupport.isInstalled()) {
                AutoTestSupport support = new AutoTestSupport(context, project,
                        project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
                support.setClassPath(project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH));
                support.start();
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
        String script = "script" + File.separator + "console"; // NOI18N
        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);

        new RubyExecution(new ExecutionDescriptor(displayName, pwd, script).
                showSuspended(false).
                showProgress(false).
                classPath(classPath).
                allowInput().
                //initialArgs(options).
                //additionalArgs(getApplicationArguments()).
                fileLocator(new RubyFileLocator(context)).
                addStandardRecognizers(),
                project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING)
                ).
                run();
    }

    private void runRubyScript(String target, String displayName, final Lookup context, final boolean debug,
            OutputRecognizer[] extraRecognizers) {
        String options = project.evaluator().getProperty(RailsProjectProperties.RUN_JVM_ARGS);

        if (options != null && options.trim().length() == 0) {
            options = null;
        }

        // Set the load path from the source and test folders.
        // Load paths are additive so users can add their own in the
        // options field as well.
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        FileObject[] testPath = project.getTestSourceRoots().getRoots();
        StringBuilder sb = new StringBuilder();
        if (srcPath != null && srcPath.length > 0) {
            for (FileObject root : srcPath) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("-I\""); // NOI18N
                sb.append(FileUtil.toFile(root).getAbsoluteFile());
                sb.append("\""); // NOI18N
            }
        }
        if (testPath != null && testPath.length > 0) {
            for (FileObject root : testPath) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("-I\""); // NOI18N
                sb.append(FileUtil.toFile(root).getAbsoluteFile());
                sb.append("\""); // NOI18N
            }
        }
        String includePath = sb.toString();
        if (options != null) {
            options = includePath + " " + options; // NOI18N
        } else {
            options = includePath;
        }

        // Locate the target and specify it by full path.
        // This is necessary because JRuby and Ruby don't locate the script from the load
        // path it seems.
        if (!new File(target).exists()) {
            if (srcPath != null && srcPath.length > 0) {
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
        }

        // For Rails, the execution directory should be the RAILS_ROOT directory
        File pwd = FileUtil.toFile(project.getProjectDirectory());

        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
        
        ExecutionDescriptor desc = new ExecutionDescriptor(displayName, pwd, target);
        desc.debug(debug);
        desc.showSuspended(true);
        desc.allowInput();
        desc.initialArgs(options);
        desc.classPath(classPath);
        desc.additionalArgs(getApplicationArguments());
        desc.fileLocator(new RailsFileLocator(context, project));
        desc.addStandardRecognizers();
        desc.addOutputRecognizer(RubyExecution.RUBY_TEST_OUTPUT);
        
        if (extraRecognizers != null) {
            for (OutputRecognizer recognizer : extraRecognizers) {
                desc.addOutputRecognizer(recognizer);
            }
        }

        RubyExecution service = new RubyExecution(desc,
                project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
        service.run();
        
    }
    
    
    public boolean isActionEnabled( String command, Lookup context ) {
        // We don't require files to be in the source roots to be executable/debuggable;
        // for example, in Rails you may want to switch to the Files view and execute
        // some of the files in scripts/, even though these are not considered sources
        // (and don't have a source root)
        //FileObject buildXml = findBuildXml();
        //if (  buildXml == null || !buildXml.isValid()) {
        //    return false;
        //}
        //if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
        //    return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
        //            || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        //}
        //else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
        //    return findTestSourcesForSources(context) != null;
        //}
        //else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
        //    FileObject[] files = findTestSourcesForSources(context);
        //    return files != null && files.length == 1;
        //} else if (command.equals(COMMAND_RUN_SINGLE) ||
        //        command.equals(COMMAND_DEBUG_SINGLE)) {
        //    FileObject fos[] = findSources(context);
        //    if (fos != null && fos.length == 1) {
        //        return true;
        //    }
        //    fos = findTestSources(context, false);
        //    return fos != null && fos.length == 1;
        //} else {
        //    // other actions are global
        //    return true;
        //}
        
        return true;
    }
    
    
   
    // Private methods -----------------------------------------------------------------
    
    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     */
    private FileObject[] findSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = findSelectedFiles(context, srcPath[i], RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
            files = findSelectedFiles(context, srcPath[i], RhtmlTokenId.MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    // From the ant module - ActionUtils.
    // However, I've modified it to do its search based on mime type rather than file suffixes
    // (since some Ruby files do not use a .rb extension and are discovered based on the initial shebang line)
    
    static FileObject[] findSelectedFiles(Lookup context, FileObject dir, String mimeType, boolean strict) {
        if (dir != null && !dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        Collection<FileObject> files = new LinkedHashSet<FileObject>(); // #50644: remove dupes
        // XXX this should perhaps also check for FileObject's...
        for (DataObject d : context.lookupAll(DataObject.class)) {
            FileObject f = d.getPrimaryFile();
            boolean matches = FileUtil.toFile(f) != null;
            if (dir != null) {
                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
            }
            if (mimeType != null) {
                matches &= f.getMIMEType().equals(mimeType);
            }
            // Generally only files from one project will make sense.
            // Currently the action UI infrastructure (PlaceHolderAction)
            // checks for that itself. Should there be another check here?
            if (matches) {
                files.add(f);
            } else if (strict) {
                return null;
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        return files.toArray(new FileObject[files.size()]);
    }
    

    // TODO - nuke me soon - replace by RailsFileLocator
    private class RubyFileLocator implements FileLocator {
        private Lookup context;

        RubyFileLocator(Lookup context) {
            this.context = context;
        }
        
        public FileObject find(String file) {
            FileObject[] fos = null;
            if (context != Lookup.EMPTY) {
        
                // First check roots and search by relative path.
                FileObject[] srcPath = project.getSourceRoots().getRoots();
                if (srcPath != null) {
                    for (FileObject root : srcPath) {
                        FileObject f = root.getFileObject(file);
                        if (f != null) {
                            return f;
                        }
                    }
                }

                // Next try searching the set of source files
                fos = findSources(context);
                if (fos != null) {
                    for (FileObject fo : fos) {
                        if (fo.getNameExt().equals(file)) {
                            return fo;
                        }
                    }
                }
            }

            // Manual search
            FileObject[] srcPath = project.getSourceRoots().getRoots();
            for (FileObject root : srcPath) {
                // First see if this path is relative to the root
                try {
                    File f = new File(FileUtil.toFile(root), file);
                    if (f.exists()) {
                        f = f.getCanonicalFile();
                        return FileUtil.toFileObject(f);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                
                // Search recursively for the given file below the path 
                FileObject fo = findFile(root, file);
                if (fo != null) {
                    return fo;
                }
            }
            
            return null;
        }
        
        private FileObject findFile(FileObject fo, String name) {
            if (name.equals(fo.getNameExt())) {
                return fo;
            }
            
            for (FileObject child : fo.getChildren()) {
                FileObject found = findFile(child, name);
                if (found != null) {
                    return found;
                }
            }
            
            return null;
        }
    }
    
    private String[] getApplicationArguments() {
        String applicationArgs = project.evaluator().getProperty(RailsProjectProperties.APPLICATION_ARGS);
        return (applicationArgs == null || applicationArgs.trim().length() == 0)
                ? null : Utilities.parseParameters(applicationArgs);
    }    

    private void runServer(final String path, final boolean debug) {
        RailsServer server = project.getLookup().lookup(RailsServer.class);
        if (server != null) {
            server.setDebug(debug);
            server.showUrl(path);
        }
    }

}
