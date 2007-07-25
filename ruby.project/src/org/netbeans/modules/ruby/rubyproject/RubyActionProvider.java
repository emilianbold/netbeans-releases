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

package org.netbeans.modules.ruby.rubyproject;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.MainClassWarning;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.MouseUtils;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action provider of the Ruby project. This is the place where to do
 * strange things to Ruby actions. E.g. compile-single.
 */
public class RubyActionProvider implements ActionProvider {
    
    /**
     * Standard command for running rdoc on a project.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_RDOC = "rdoc"; // NOI18N
    
    /**
     * Command for running auto test on this project (if installed)
     */
    public static final String COMMAND_AUTOTEST = "autotest"; // NOI18N

    // Commands available from Ruby project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_AUTOTEST,
        COMMAND_RDOC,
        //        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        //        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    
    
    // Project
    RubyProject project;
    
    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
        
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    public RubyActionProvider( RubyProject project, UpdateHelper updateHelper ) {
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
    
    private void runRubyScript(FileObject fileObject, String target, 
            String displayName, final Lookup context, final boolean debug) {
        String options = project.evaluator().getProperty(RubyProjectProperties.RUN_JVM_ARGS);

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
        
        target = locate(target, srcPath, testPath);
        
        String runDir = project.evaluator().getProperty(RubyProjectProperties.RUN_WORK_DIR);
        File pwd = getSourceFolder();
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
        
        String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);

        ExecutionDescriptor desc = new ExecutionDescriptor(displayName, pwd, target);
        desc.debug(debug);
        desc.showSuspended(true);
        desc.allowInput();
        desc.fileObject(fileObject);
        desc.initialArgs(options);
        desc.classPath(classPath);
        desc.additionalArgs(getApplicationArguments());
        desc.fileLocator(new RubyFileLocator(context, project));
        desc.addOutputRecognizer(RubyExecution.RUBY_COMPILER);
        desc.addOutputRecognizer(RubyExecution.RUBY_TEST_OUTPUT);
        RubyExecution service = new RubyExecution(desc,
                project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING));
        service.run();
    }
    
    private String locate(String target, final FileObject[] srcPath, final FileObject[] testPath) {
        // Locate the target and specify it by full path. This is necessary
        // because JRuby and Ruby don't locate the script from the load path it
        // seems.
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
        return target;
    }

    private FileObject getCurrentFile(Lookup context) {
        FileObject[] fos = findSources(context);
        if (fos == null) {
            fos = findTestSources(context, false);
        }
        if (fos == null || fos.length == 0) {
            for (DataObject d : context.lookupAll(DataObject.class)) {
                FileObject fo = d.getPrimaryFile();
                if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                    return fo;
                }
            }
            return null;
        }
        
        return fos[0];
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
        // TODO Check for valid installation of Ruby and Rake
        if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRuby(true)) {
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
            EditableProperties ep = updateHelper.getProperties(path);

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
                    if (updateHelper.requestSave()) {
                        updateHelper.putProperties(path, ep);
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
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }
            
            RubyFileLocator fileLocator = new RubyFileLocator(context, project);
            File pwd = getSourceFolder(); // Or project directory?
            String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    fileLocator(fileLocator).
                    allowInput().
                    classPath(classPath).
                    addOutputRecognizer(RubyExecution.RUBY_COMPILER).
                    addOutputRecognizer(RubyExecution.RUBY_TEST_OUTPUT),
                    project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            
            return;
        } else if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRuby(true)) {
                return;
            }

            FileObject file = getCurrentFile(context);

            if (RakeSupport.isRakeFile(file)) {
                if (!RubyInstallation.getInstance().isValidRake(true)) {
                    return;
                }

                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                RakeSupport rake = new RakeSupport(project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING));
                rake.runRake(null, file, file.getName(), new RubyFileLocator(context, project), true);
                return;
            }
            
            RSpecSupport rspec = new RSpecSupport(project.getProjectDirectory(), project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING));
            if (rspec.isRSpecInstalled() && rspec.isSpecFile(file)) {
                // Save all files first - this rake file could be accessing other files
                LifecycleManager.getDefault().saveAll();
                rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context, project), true,
                        COMMAND_DEBUG_SINGLE.equals(command));
                return;
            }
            
            saveFile(file);
            
            //String target = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
            runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(),
                    file.getNameExt(), context, COMMAND_DEBUG_SINGLE.equals(command));
            return;
        } else if (COMMAND_REBUILD.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }

            // Save all files first
            LifecycleManager.getDefault().saveAll();
            
            String displayName = NbBundle.getMessage(RubyActionProvider.class, "Rake");
            File pwd = getSourceFolder(); // Or project directory
            
            Runnable finishedAction =
                new Runnable() {
                    public void run() {
                        invokeAction(COMMAND_BUILD, context);
                    }
                };
            
            String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    additionalArgs("clean"). // NOI18N
                    postBuild(finishedAction).
                    allowInput().
                    classPath(classPath).
                    fileLocator(new RubyFileLocator(context, project)).
                    addOutputRecognizer(RubyExecution.RUBY_COMPILER),
                    project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            return;
            
        } else if (COMMAND_BUILD.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }

            // Save all files first
            LifecycleManager.getDefault().saveAll();
            
            String displayName = 
                NbBundle.getMessage(RubyActionProvider.class, "Rake");
            File pwd = getSourceFolder(); // Or project directory?
            String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    allowInput().
                    classPath(classPath).
                    fileLocator(new RubyFileLocator(context, project)).
                    addOutputRecognizer(RubyExecution.RUBY_COMPILER),
                    project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            return;
        } else if (COMMAND_CLEAN.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }

            //RubyFileLocator fileLocator = new RubyFileLocator(context);
            String displayName = 
                NbBundle.getMessage(RubyActionProvider.class, "Rake");

            File pwd = getSourceFolder(); // Or project directory?
            String classPath = project.evaluator().getProperty(RubyProjectProperties.JAVAC_CLASSPATH);
            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRake()).
                    additionalArgs("clean"). // NOI18N
                    allowInput().
                    classPath(classPath).
                    fileLocator(new RubyFileLocator(context, project)).
                    addOutputRecognizer(RubyExecution.RUBY_COMPILER),
                    project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            return;
        }
        
        if (COMMAND_RDOC.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRDoc(true)) {
                return;
            }

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

            new RubyExecution(new ExecutionDescriptor(displayName, pwd, RubyInstallation.getInstance().getRDoc()).
                    fileLocator(fileLocator).
                    postBuild(showBrowser).
                    addOutputRecognizer(RubyExecution.RUBY_COMPILER),
                    project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)
                    ).
                    run();
            
            return;
        }
        
        if (COMMAND_AUTOTEST.equals(command)) {
            if (AutoTestSupport.isInstalled()) {
                new AutoTestSupport(context, project, 
                        project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING)).start();
            }
            
            return;
        }
        
        if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
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

            RSpecSupport rspec = new RSpecSupport(project.getProjectDirectory(), project.evaluator().getProperty(RubyProjectProperties.SOURCE_ENCODING));
            if (rspec.isRSpecInstalled() && rspec.isSpecFile(file)) {
                rspec.runRSpec(null, file, file.getName(), new RubyFileLocator(context, project), true,
                       isDebug);
                return;
            }
            
            runRubyScript(file, FileUtil.toFile(file).getAbsolutePath(),
                    file.getNameExt(), context, isDebug);
        }

        if (COMMAND_TEST.equals(command)) {
            if (!RubyInstallation.getInstance().isValidRuby(true)) {
                return;
            }

            FileObject[] files = findTestSourcesForSources(context);

            //return;
            throw new RuntimeException("Not yet implemented");
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
    
//    /**
//     * @return array of targets or null to stop execution; can return empty array
//     */
//    private String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
//        String[] targetNames = new String[0];
//        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
//            throw new RuntimeException("Not yet implemented");
//            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
//            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
//            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
//            if (files != null) {
//                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
//                targetNames = new String[] {"compile-single"}; // NOI18N
//            } 
//            else {
//                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
//                files = findSourcesAndPackages(context, testRoots);
//                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
//                targetNames = new String[] {"compile-test-single"}; // NOI18N
//            }
//        } 
//        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
//            FileObject[] files = findTestSourcesForSources(context);
//            targetNames = setupTestSingle(p, files);
//        } 
//        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
//            FileObject[] files = findTestSourcesForSources(context);
//            targetNames = setupDebugTestSingle(p, files);
//        } else if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE)) {
//            FileObject[] files = findTestSources(context, false);
//            if (files != null) {
//                if (command.equals(COMMAND_RUN_SINGLE)) {
//                    targetNames = setupTestSingle(p, files);
//                } else {
//                    targetNames = setupDebugTestSingle(p, files);
//                }
//            } else {
//                FileObject file = findSources(context)[0];
//                String clazz = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
////                p.setProperty("javac.includes", clazz); // NOI18N
//                // Convert foo/FooTest.java -> foo.FooTest
//                // XXX What about Ruby?
//                if (clazz.endsWith(".java")) { // NOI18N
//                    clazz = clazz.substring(0, clazz.length() - 5);
//                }
//                clazz = clazz.replace('/','.');
//
//                if (!RubyProjectUtil.hasMainMethod (file)) {
//                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(RubyActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
//                        DialogDisplayer.getDefault().notify(nd);
//                        return null;
//                } else {
//                    if (command.equals (COMMAND_RUN_SINGLE)) {
//                        p.setProperty("run.class", clazz); // NOI18N
//                        targetNames = (String[])commands.get(COMMAND_RUN_SINGLE);
//                    } else {
//                        p.setProperty("debug.class", clazz); // NOI18N
//                        targetNames = (String[])commands.get(COMMAND_DEBUG_SINGLE);
//                    }
//                }
//            }
//        }
//        return targetNames;
//    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
//        FileObject buildXml = findBuildXml();
//        if (  buildXml == null || !buildXml.isValid()) {
//            return false;
//        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
//        }
//        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
//            return findTestSourcesForSources(context) != null;
//        }
//        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
//            FileObject[] files = findTestSourcesForSources(context);
//            return files != null && files.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE) || 
                        command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject fos[] = findSources(context);
            if (fos != null && fos.length == 1) {
                return true;
            }
            fos = findTestSources(context, false);
            return fos != null && fos.length == 1;
        } else {
            // other actions are global
            return true;
        }
    }
    
    
   
    // Private methods -----------------------------------------------------------------
    
    
    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     * @return The file objects in the sources folder
     */
    private FileObject[] findSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = findSelectedFiles(context, srcPath[i], RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    private FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
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
    
    /** Find either selected tests or tests which belong to selected source files
     */
    private FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        //XXX: Ugly, should be rewritten
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        for (int i=0; i< testSrcPath.length; i++) {
            FileObject[] files = findSelectedFiles(context, testSrcPath[i], RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
        }
//        if (checkInSrcDir && testSrcPath.length>0) {
//            FileObject[] files = findSources (context);
//            if (files != null) {
//                //Try to find the test under the test roots
//                FileObject srcRoot = getRoot(project.getSourceRoots().getRoots(),files[0]);
//                for (int i=0; i<testSrcPath.length; i++) {
//                    FileObject[] files2 = ActionUtils.regexpMapFiles(files,srcRoot, SRCDIRJAVA, testSrcPath[i], SUBST, true);
//                    if (files2 != null) {
//                        return files2;
//                    }
//                }
//            }
//        }
        return null;
    }
   

    /** Find tests corresponding to selected sources.
     */
    private FileObject[] findTestSourcesForSources(Lookup context) {
        FileObject[] sourceFiles = findSources(context);
        if (sourceFiles == null) {
            return null;
        }
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        if (testSrcPath.length == 0) {
            return null;
        }
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        FileObject srcDir = getRoot(srcPath, sourceFiles[0]);
//        for (int i=0; i<testSrcPath.length; i++) {
//            FileObject[] files2 = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcPath[i], SUBST, true);
//            if (files2 != null) {
//                return files2;
//            }
//        }
//        return null;
        return new FileObject[] { srcDir }; // XXX This is bogus
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
     * Tests if the main class is set
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
     * Asks user for name of main class
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
    // However, I've modified it to do its search based on mime type rather than file suffixes
    // (since some Ruby files do not use a .rb extension and are discovered based on the initial shebang line)
    
    public static FileObject[] findSelectedFiles(Lookup context, FileObject dir, String mimeType, boolean strict) {
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
    

    private File getSourceFolder() {
        // Default to using the project source directory
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        if (srcPath != null && srcPath.length > 0) {
            return FileUtil.toFile(srcPath[0]);
        } else {
            return FileUtil.toFile(project.getProjectDirectory());
        }
    }
    
    private String[] getApplicationArguments() {
        String applicationArgs = project.evaluator().getProperty(RubyProjectProperties.APPLICATION_ARGS);
        return (applicationArgs == null || applicationArgs.trim().length() == 0)
                ? null : Utilities.parseParameters(applicationArgs);
    }
    
}
