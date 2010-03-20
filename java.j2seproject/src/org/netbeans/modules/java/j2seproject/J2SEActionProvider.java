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

package org.netbeans.modules.java.j2seproject;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.applet.AppletSupport;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassWarning;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
class J2SEActionProvider implements ActionProvider {
    public static final String AUTOMATIC_BUILD_TAG = ".netbeans_automatic_build";

    private static final Logger LOG = Logger.getLogger(J2SEActionProvider.class.getName());

    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };


    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };

    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    // Project
    final J2SEProject project;

    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
    //Property evaluator
    private final PropertyEvaluator evaluator;

    /** Map from commands to ant targets */
    Map<String,String[]> commands;

    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    final Set<String> needJavaModelActions;

    /** Set of Java source files (as relative path from source root) known to have been modified. See issue #104508. */
    private Set<String> dirty = null;

    private Sources src;
    private List<FileObject> roots;

    // Used only from unit tests to suppress detection of top level classes. If value
    // is different from null it will be returned instead.
    String unitTestingSupport_fixClasses;
    
    private volatile Boolean allowsFileTracking;

    public J2SEActionProvider(J2SEProject project, UpdateHelper updateHelper) {

        commands = new HashMap<String,String[]>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO
        ));

        this.needJavaModelActions = new HashSet<String>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));

        this.updateHelper = updateHelper;
        this.project = project;
        this.evaluator = project.evaluator();
        this.evaluator.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                synchronized (J2SEActionProvider.class) {
                    final String propName = evt.getPropertyName();
                    if (propName == null || J2SEProjectProperties.TRACK_FILE_CHANGES.equals(propName)) {
                        allowsFileTracking = null;
                        dirty = null;
                    }
                }
            }
        });
    }


    private boolean allowsFileChangesTracking () {
        //allowsFileTracking is volatile primitive, fine to do double checking
        synchronized (this) {
            if (allowsFileTracking != null) {
                return allowsFileTracking.booleanValue();
            }
        }
        final String val = evaluator.getProperty(J2SEProjectProperties.TRACK_FILE_CHANGES);
        synchronized (this) {
            if (allowsFileTracking == null) {
                allowsFileTracking = "true".equals(val) ? Boolean.TRUE : Boolean.FALSE;  //NOI18N
            }
            return allowsFileTracking.booleanValue();
        }
    }

    private final FileChangeListener modificationListener = new FileChangeAdapter() {
        public @Override void fileChanged(FileEvent fe) {
            modification(fe.getFile());
        }
        public @Override void fileDataCreated(FileEvent fe) {
            modification(fe.getFile());
        }
    };

    private final ChangeListener sourcesChangeListener = new ChangeListener() {

        public void stateChanged(ChangeEvent e) {
            synchronized (J2SEActionProvider.this) {
                J2SEActionProvider.this.roots = null;
            }
        }
    };


    void startFSListener () {
        //Listener has to be started when the project's lookup is initialized
        try {
            FileSystem fs = project.getProjectDirectory().getFileSystem();
            // XXX would be more efficient to only listen while TRACK_FILE_CHANGES is set,
            // but it needs adding and removing of listeners depending on PropertyEvaluator events,
            // the file event handling is cheap when TRACK_FILE_CHANGES is disabled.
            fs.addFileChangeListener(FileUtil.weakFileChangeListener(modificationListener, fs));
        } catch (FileStateInvalidException x) {
            Exceptions.printStackTrace(x);
        }
    }

    private void modification(FileObject f) {
        if (!allowsFileChangesTracking()) {
            return;
        }
        final Iterable <? extends FileObject> _roots = getRoots();
        assert _roots != null;
        for (FileObject root : _roots) {
            String path = FileUtil.getRelativePath(root, f);
            if (path != null) {
                synchronized (this) {
                    if (dirty != null) {
                        dirty.add(path);
                    }
                }
                break;
            }
        }
    }

    private Iterable <? extends FileObject> getRoots () {
        Sources _src = null;
        synchronized (this) {
            if (this.roots != null) {
                return this.roots;
            }
            if (this.src == null) {
                this.src = this.project.getLookup().lookup(Sources.class);
                this.src.addChangeListener (sourcesChangeListener);
            }
            _src = this.src;
        }
        assert _src != null;
        final SourceGroup[] sgs = _src.getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
        final List<FileObject> _roots = new ArrayList<FileObject>(sgs.length);
        for (SourceGroup sg : sgs) {
            final FileObject root = sg.getRootFolder();
            if (UnitTestForSourceQuery.findSources(root).length == 0) {
                _roots.add (root);
            }
        }
        synchronized (this) {
            if (this.roots == null) {
                this.roots = _roots;
            }
            return this.roots;
        }
    }

    private FileObject findBuildXml() {
        return J2SEProjectUtil.getBuildXml(project);
    }

    public String[] getSupportedActions() {
        return supportedActions;
    }

    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
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

        final boolean isCompileOnSaveEnabled = J2SEProjectUtil.isCompileOnSaveEnabled(project);
        final AtomicReference<Thread> caller = new AtomicReference<Thread>(Thread.currentThread());
        final AtomicBoolean called = new AtomicBoolean(false);

        class  Action implements Runnable {

            /**
             * True when the action always requires access to java model
             */
            private boolean needsJavaModel = true;
            /**
             * When true getTargetNames accesses java model, when false
             * the default walues (possibly incorrect) are used.
             */
            private boolean doJavaChecks = true;

            public void run () {
                if (!needsJavaModel && caller.get() != Thread.currentThread()) {
                    return;
                }
                called.set(true);
                Properties p = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(command, context, p, doJavaChecks);
                if (targetNames == null) {
                    return;
                }
                if (isCompileOnSaveEnabled) {
                    if (COMMAND_BUILD.equals(command) && !allowAntBuild()) {
                        showBuildActionWarning(context);
                        return ;
                    }
                    Map<String, Object> execProperties = new HashMap<String, Object>();

                    copyMultiValue(J2SEProjectProperties.RUN_JVM_ARGS, execProperties);
                    prepareWorkDir(execProperties);

                    execProperties.put(JavaRunner.PROP_PLATFORM, J2SEProjectUtil.getActivePlatform(evaluator.getProperty("platform.active")));
                    execProperties.put(JavaRunner.PROP_PROJECT_NAME, ProjectUtils.getInformation(project).getDisplayName());
                    String runtimeEnc = evaluator.getProperty(J2SEProjectProperties.RUNTIME_ENCODING);
                    if (runtimeEnc != null) {
                        try {
                            Charset runtimeChs = Charset.forName(runtimeEnc);
                            execProperties.put(JavaRunner.PROP_RUNTIME_ENCODING, runtimeChs); //NOI18N
                        } catch (IllegalCharsetNameException ichsn) {
                            LOG.warning("Illegal charset name: " + runtimeEnc); //NOI18N
                        } catch (UnsupportedCharsetException uchs) {
                            LOG.warning("Unsupported charset : " + runtimeEnc); //NOI18N
                        }
                    }

                    if (targetNames.length == 1 && ("run-applet".equals(targetNames[0]) || "debug-applet".equals(targetNames[0]))) {
                        try {
                            FileObject file = findSources(context)[0];
                            String url = p.getProperty("applet.url");
                            execProperties.put("applet.url", url);
                            execProperties.put(JavaRunner.PROP_EXECUTE_FILE, file);
                            prepareSystemProperties(execProperties, false);
                            JavaRunner.execute(targetNames[0], execProperties);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                    if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command)) {
                        prepareSystemProperties(execProperties, false);
                        bypassAntBuildScript(command, context, execProperties);

                        return ;
                    }
                    if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command)) {
                        prepareSystemProperties(execProperties, false);
                        if (COMMAND_RUN_SINGLE.equals(command)) {
                            execProperties.put(JavaRunner.PROP_CLASSNAME, p.getProperty("run.class"));
                        } else {
                            execProperties.put(JavaRunner.PROP_CLASSNAME, p.getProperty("debug.class"));
                        }
                        bypassAntBuildScript(command, context, execProperties);
                        return;
                    }
                    if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command)) {
                        FileObject[] files = findTestSources(context, true);
                        try {
                            prepareSystemProperties(execProperties, true);
                            execProperties.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
                            execProperties.put("tmp.dir", updateHelper.getAntProjectHelper().resolvePath(evaluator.getProperty(J2SEProjectProperties.BUILD_DIR)));   //NOI18N
                            JavaRunner.execute(COMMAND_TEST_SINGLE.equals(command) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG, execProperties);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                    if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(command)) {
                        SingleMethod methodSpec = findTestMethods(context)[0];
                        try {
                            execProperties.put("methodname", methodSpec.getMethodName());//NOI18N
                            execProperties.put(JavaRunner.PROP_EXECUTE_FILE, methodSpec.getFile());
                            execProperties.put("tmp.dir",updateHelper.getAntProjectHelper().resolvePath(evaluator.getProperty(J2SEProjectProperties.BUILD_DIR)));   //NOI18N
                            JavaRunner.execute(command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG,
                                                  execProperties);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (p.keySet().size() == 0) {
                    p = null;
                }
                try {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(J2SEActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                    else {
                        ActionUtils.runTarget(buildFo, targetNames, p).addTaskListener(new TaskListener() {
                            public void taskFinished(Task task) {
                                if (((ExecutorTask) task).result() != 0) {
                                    synchronized (J2SEActionProvider.this) {
                                        // #120843: if a build fails, disable dirty-list optimization.
                                        dirty = null;
                                    }
                                }
                            }
                        });
                    }
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        final Action action = new Action();

        if (this.needJavaModelActions.contains(command) || (isCompileOnSaveEnabled && this.bkgScanSensitiveActions.contains(command))) {
            //Always have to run with java model
            ScanDialog.runWhenScanFinished(action, NbBundle.getMessage (J2SEActionProvider.class,"ACTION_"+command));   //NOI18N
        }
        else if (this.bkgScanSensitiveActions.contains(command)) {
            //Run without model if not yet ready
            try {
                action.needsJavaModel = false;
                invokeByJavaSource(action);
                if (!called.get()) {
                    action.doJavaChecks = false;
                    action.run();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        else {
            //Does not need java model
            action.run();
        }
    }
    //where
    private static void invokeByJavaSource (final Runnable runnable) throws IOException {
        Parameters.notNull("runnable", runnable);   //NOI18N
        final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]));
        final JavaSource js = JavaSource.create(info);
        js.runWhenScanFinished(new org.netbeans.api.java.source.Task<CompilationController>() {
            public void run(final CompilationController controller) throws Exception {
                runnable.run();
            }
        }, true);
    }

    /**
     * Compatibility
     *
     */
    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        return getTargetNames(command, context, p, true);
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        if (Arrays.asList(platformSensitiveActions).contains(command)) {
            final String activePlatformId = this.project.evaluator().getProperty("platform.active");  //NOI18N
            if (J2SEProjectUtil.getActivePlatform (activePlatformId) == null) {
                showPlatformWarning ();
                return null;
            }
        }
        String[] targetNames = new String[0];
        Map<String,String[]> targetsFromConfig = loadTargetsFromConfig();
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
                String[] targets = targetsFromConfig.get(command);
                targetNames = (targets != null) ? targets : commands.get(command);
            }
            else {
                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
                files = findSourcesAndPackages(context, testRoots);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-test-single"}; // NOI18N
            }
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSources(context, true);
            targetNames = setupTestSingle(p, files);
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSources(context, true);
            targetNames = setupDebugTestSingle(p, files);
        }
        else if ( command.equals( SingleMethod.COMMAND_RUN_SINGLE_METHOD ) ) {
            SingleMethod[] methodSpecs = findTestMethods(context);
            if ((methodSpecs == null) || (methodSpecs.length != 1)) {
                return new String[0];
            }
            targetNames = setupRunSingleTestMethod(p, methodSpecs[0]);
        }
        else if ( command.equals( SingleMethod.COMMAND_DEBUG_SINGLE_METHOD ) ) {
            SingleMethod[] methodSpecs = findTestMethods(context);
            if ((methodSpecs == null) || (methodSpecs.length != 1)) {
                return new String[0];
            }
            targetNames = setupDebugSingleTestMethod(p, methodSpecs[0]);
        }
        else if ( command.equals( JavaProjectConstants.COMMAND_DEBUG_FIX ) ) {
            FileObject[] files = findSources( context );
            String path = null;
            String classes = "";    //NOI18N
            if (files != null) {
                path = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
                classes = getTopLevelClasses(files[0]);
            } else {
                files = findTestSources(context, false);
                assert files != null : "findTestSources () can't be null: " + project.getTestSourceRoots().getRoots();   //NOI18N
                path = FileUtil.getRelativePath(getRoot(project.getTestSourceRoots().getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix-test"}; // NOI18N
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
            p.setProperty("fix.classes", classes); // NOI18N
        }
        else if (command.equals (COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            String config = project.evaluator().getProperty(J2SEConfigurationProvider.PROP_CONFIG);
            String path;
            if (config == null || config.length() == 0) {
                path = AntProjectHelper.PROJECT_PROPERTIES_PATH;
            } else {
                // Set main class for a particular config only.
                path = "nbproject/configs/" + config + ".properties"; // NOI18N
            }
            EditableProperties ep = updateHelper.getProperties(path);

            // check project's main class
            // Check whether main class is defined in this config. Note that we use the evaluator,
            // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
            // to define a main class - in this case an active config need not override it.
            String mainClass = project.evaluator().getProperty(J2SEProjectProperties.MAIN_CLASS);
            MainClassStatus result;
            if (doJavaChecks) {
                result = isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
            }
            else {
                result = MainClassStatus.SET_AND_VALID;
            }
            if (context.lookup(J2SEConfigurationProvider.Config.class) != null) {
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
                        return null;
                    }
                    // No longer use the evaluator: have not called putProperties yet so it would not work.
                    mainClass = ep.get(J2SEProjectProperties.MAIN_CLASS);
                    result=isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
                } while (result != MainClassStatus.SET_AND_VALID);
                try {
                    if (updateHelper.requestUpdate()) {
                        updateHelper.putProperties(path, ep);
                        ProjectManager.getDefault().saveProject(project);
                    }
                    else {
                        return null;
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe);
                }
            }
            if (!command.equals(COMMAND_RUN) && /* XXX should ideally look up proper mainClass in evaluator x config */ mainClass != null) {
                p.setProperty("debug.class", mainClass); // NOI18N
            }
            String[] targets = targetsFromConfig.get(command);
            targetNames = (targets != null) ? targets : commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
            prepareDirtyList(p, false);
        } else if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            FileObject[] rootz = project.getTestSourceRoots().getRoots();
            boolean isTest = true;
            if (files == null) {
                isTest = false;
                files = findSources(context);
                rootz = project.getSourceRoots().getRoots();
            }
            if (files == null) {
                //The file was not found under the source roots
                return null;
            }
            FileObject file = files[0];
            String clazz = FileUtil.getRelativePath(getRoot(rootz, file), file);
            p.setProperty("javac.includes", clazz); // NOI18N
            // Convert foo/FooTest.java -> foo.FooTest
            if (clazz.endsWith(".java")) { // NOI18N
                clazz = clazz.substring(0, clazz.length() - 5);
            }
            clazz = clazz.replace('/','.');
            final boolean hasMainClassFromTest = MainClassChooser.unitTestingSupport_hasMainMethodResult == null ? false :
                MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue();
            if (doJavaChecks) {
                final Collection<ElementHandle<TypeElement>> mainClasses = J2SEProjectUtil.getMainMethods (file);
                if (!hasMainClassFromTest && mainClasses.isEmpty()) {
                    if (!isTest && AppletSupport.isApplet(file)) {

                        EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String jvmargs = ep.getProperty(J2SEProjectProperties.RUN_JVM_ARGS);

                        URL url = null;

                        // do this only when security policy is not set manually
                        if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") > 0)) {  //NOI18N
                            AppletSupport.generateSecurityPolicy(project.getProjectDirectory());
                            if ((jvmargs == null) || (jvmargs.length() == 0)) {
                                ep.setProperty(J2SEProjectProperties.RUN_JVM_ARGS, "-Djava.security.policy=applet.policy"); //NOI18N
                            } else {
                                ep.setProperty(J2SEProjectProperties.RUN_JVM_ARGS, jvmargs + " -Djava.security.policy=applet.policy"); //NOI18N
                            }
                            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (Exception e) {
                                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + e);
                            }
                        }

                        if (file.existsExt("html") || file.existsExt("HTML")) { //NOI18N
                            url = copyAppletHTML(file, "html"); //NOI18N
                        } else {
                            url = generateAppletHTML(file);
                        }
                        if (url == null) {
                            return null;
                        }
                        p.setProperty("applet.url", url.toString()); // NOI18N
                        if (command.equals (COMMAND_RUN_SINGLE)) {
                            targetNames = new String[] {"run-applet"}; // NOI18N
                        } else {
                            p.setProperty("debug.class", clazz); // NOI18N
                            targetNames = new String[] {"debug-applet"}; // NOI18N
                        }
                    } else {
                        if (isTest) {
                            //Fallback to normal (non-main-method-based) unit test run
                            if (command.equals(COMMAND_RUN_SINGLE)) {
                                targetNames = setupTestSingle(p, files);
                            } else {
                                targetNames = setupDebugTestSingle(p, files);
                            }
                        } else {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(J2SEActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                            return null;
                        }
                    }
                } else {
                    if (!hasMainClassFromTest) {
                        if (mainClasses.size() == 1) {
                            //Just one main class
                            clazz = mainClasses.iterator().next().getBinaryName();
                        }
                        else {
                            //Several main classes, let the user choose
                            clazz = showMainClassWarning(file, mainClasses);
                            if (clazz == null) {
                                return null;
                            }
                        }
                    }
                    if (command.equals (COMMAND_RUN_SINGLE)) {
                        p.setProperty("run.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] { "run-test-with-main" } : commands.get(COMMAND_RUN_SINGLE));
                    } else {
                        p.setProperty("debug.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] {"debug-test-with-main"} : commands.get(COMMAND_DEBUG_SINGLE));
                    }
                }
            }
            else {
                //The Java model is not ready, we cannot determine if the file is applet or main class or unit test
                //Acts like everything is main class, maybe for test folder junit is better default?
                if (clazz == null) {
                    return null;
                }
                if (command.equals (COMMAND_RUN_SINGLE)) {
                        p.setProperty("run.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] { "run-test-with-main" } : commands.get(COMMAND_RUN_SINGLE));    //NOI18N
                } else {
                    p.setProperty("debug.class", clazz); // NOI18N
                    String[] targets = targetsFromConfig.get(command);
                    targetNames = (targets != null) ? targets : (isTest ? new String[] {"debug-test-with-main"} : commands.get(COMMAND_DEBUG_SINGLE));      //NOI18N
                }
            }
        } else {
            String[] targets = targetsFromConfig.get(command);
            targetNames = (targets != null) ? targets : commands.get(command);
            if (targetNames == null) {
                String buildTarget = "false".equalsIgnoreCase(project.evaluator().getProperty(J2SEProjectProperties.DO_JAR)) ? "compile" : "jar"; // NOI18N
                if (command.equals(COMMAND_BUILD)) {
                    targetNames = new String[] {buildTarget};
                    prepareDirtyList(p, true);
                } else if (command.equals(COMMAND_REBUILD)) {
                    targetNames = new String[] {"clean", buildTarget}; // NOI18N
                } else {
                    throw new IllegalArgumentException(command);
                }
            }
            if (COMMAND_CLEAN.equals(command)) {
                //After clean, rebuild all
                dirty = null;
            }
        }
        J2SEConfigurationProvider.Config c = context.lookup(J2SEConfigurationProvider.Config.class);
        if (c != null) {
            String config;
            if (c.name != null) {
                config = c.name;
            } else {
                // Invalid but overrides any valid setting in config.properties.
                config = "";
            }
            p.setProperty(J2SEConfigurationProvider.PROP_CONFIG, config);
        }
        return targetNames;
    }
    private void prepareDirtyList(Properties p, boolean isExplicitBuildTarget) {
        String doDepend = project.evaluator().getProperty(J2SEProjectProperties.DO_DEPEND);
        String buildClassesDirValue = project.evaluator().getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClassesDirValue == null) {            
            //Log
            StringBuilder logRecord = new StringBuilder();
            logRecord.append("EVALUATOR: "+evaluator.getProperties().toString()+";");       //NOI18N
            logRecord.append("PROJECT_PROPS: "+updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).entrySet()+";");    //NOI18N
            logRecord.append("PRIVATE_PROPS: "+updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).entrySet()+";");    //NOI18N
            LOG.warning("No build.classes.dir property: " + logRecord.toString());
            return;
        }
        File buildClassesDir = project.getAntProjectHelper().resolveFile(buildClassesDirValue);
        synchronized (this) {
            if (dirty == null) {
                if (allowsFileChangesTracking()) {
                    // #119777: the first time, build everything.
                    dirty = new TreeSet<String>();
                }
                return;
            }
            for (DataObject d : DataObject.getRegistry().getModified()) {
                // Treat files modified in memory as dirty as well.
                // (If you make an edit and press F11, the save event happens *after* Ant is launched.)
                modification(d.getPrimaryFile());
            }
            boolean wasBuiltAutomatically = new File(buildClassesDir,AUTOMATIC_BUILD_TAG).canRead(); //NOI18N
            if (!"true".equalsIgnoreCase(doDepend) && !(isExplicitBuildTarget && dirty.isEmpty()) && !wasBuiltAutomatically) { // NOI18N
                // #104508: if not using <depend>, try to compile just those files known to have been touched since the last build.
                // (In case there are none such, yet the user invoked build anyway, probably they know what they are doing.)
                if (dirty.isEmpty()) {
                    // includes="" apparently is ignored.
                    dirty.add("nothing whatsoever"); // NOI18N
                }
                StringBuilder dirtyList = new StringBuilder();
                for (String f : dirty) {
                    if (dirtyList.length() > 0) {
                        dirtyList.append(',');
                    }
                    dirtyList.append(f);
                }
                p.setProperty(ProjectProperties.INCLUDES, dirtyList.toString());
            }
            dirty.clear();
        }
    }

    // loads targets for specific commands from shared config property file
    // returns map; key=command name; value=array of targets for given command
    private HashMap<String,String[]> loadTargetsFromConfig() {
        HashMap<String,String[]> targets = new HashMap<String,String[]>(6);
        String config = project.evaluator().getProperty(J2SEConfigurationProvider.PROP_CONFIG);
        // load targets from shared config
        FileObject propFO = project.getProjectDirectory().getFileObject("nbproject/configs/" + config + ".properties");
        if (propFO == null) {
            return targets;
        }
        Properties props = new Properties();
        try {
            InputStream is = propFO.getInputStream();
            try {
                props.load(is);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return targets;
        }
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            if (propName.startsWith("$target.")) {
                String tNameVal = props.getProperty(propName);
                String cmdNameKey = null;
                if (tNameVal != null && !tNameVal.equals("")) {
                    cmdNameKey = propName.substring("$target.".length());
                    StringTokenizer stok = new StringTokenizer(tNameVal.trim(), " ");
                    List<String> targetNames = new ArrayList<String>(3);
                    while (stok.hasMoreTokens()) {
                        targetNames.add(stok.nextToken());
                    }
                    targets.put(cmdNameKey, targetNames.toArray(new String[targetNames.size()]));
                }
            }
        }
        return targets;
    }

    private String[] setupTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        String path = FileUtil.getRelativePath(root, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"debug-test"}; // NOI18N
    }

    private String[] setupRunSingleTestMethod(Properties p, SingleMethod methodSpec) {
        return setupTestSingle(p, new FileObject[] {methodSpec.getFile()});

        //FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        //FileObject testFile = methodSpec.getFile();
        //FileObject root = getRoot(testSrcPath, testFile);
        //String relPath = FileUtil.getRelativePath(root, testFile);
        //String className = getClassName(relPath);
        //p.setProperty("javac.includes", relPath); // NOI18N
        //p.setProperty("test.class", className); // NOI18N
        //p.setProperty("test.method", methodSpec.getMethodName()); // NOI18N
        //return new String[] {"test-single-method"}; // NOI18N
    }

    private String[] setupDebugSingleTestMethod(Properties p, SingleMethod methodSpec) {
        return setupDebugTestSingle(p, new FileObject[] {methodSpec.getFile()});

        //FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        //FileObject testFile = methodSpec.getFile();
        //FileObject root = getRoot(testSrcPath, testFile);
        //String relPath = FileUtil.getRelativePath(root, testFile);
        //String className = getClassName(relPath);
        //p.setProperty("javac.includes", relPath); // NOI18N
        //p.setProperty("test.class", className); // NOI18N
        //p.setProperty("test.method", methodSpec.getMethodName()); // NOI18N
        //return new String[] {"debug-test-method"}; // NOI18N
    }

    private static String getClassName(String relPath) {
        // Convert foo/FooTest.java -> foo.FooTest
        return relPath.substring(0, relPath.length() - 5).replace('/', '.');
    }

    private boolean allowAntBuild() {
        String buildClasses = project.evaluator().getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClasses == null) return false;
        File buildClassesFile = this.updateHelper.getAntProjectHelper().resolveFile(buildClasses);

        return !new File(buildClassesFile, AUTOMATIC_BUILD_TAG).exists();
    }

    public boolean isActionEnabled( String command, Lookup context ) {
        FileObject buildXml = findBuildXml();
        if (  buildXml == null || !buildXml.isValid()) {
            return false;
        }
        if (   Arrays.asList(actionsDisabledForQuickRun).contains(command)
            && J2SEProjectUtil.isCompileOnSaveEnabled(project)
            && !allowAntBuild()) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] fos = findTestSources(context, true);
            return fos != null && fos.length == 1;
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] fos = findTestSources(context, true);
            return fos != null && fos.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE) ||
                        command.equals(COMMAND_DEBUG_SINGLE) ||
                        command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject fos[] = findSources(context);
            if (fos != null && fos.length == 1) {
                return true;
            }
            fos = findTestSources(context, false);
            return fos != null && fos.length == 1;
        } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD)
                || command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            if (J2SEProjectUtil.isCompileOnSaveEnabled(project)) {
                SingleMethod[] methodSpecs = findTestMethods(context);
                return (methodSpecs != null) && (methodSpecs.length == 1);
            } else {
                return false;
            }
        } else {
            // other actions are global
            return true;
        }
    }



    // Private methods -----------------------------------------------------


    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N


    /**
     * Lists all top level classes in a String, classes are separated by space (" ")
     * Used by debuger fix and continue (list of files to fix)
     * @param file for which the top level classes should be found
     * @return list of top levels
     */
    private String getTopLevelClasses (final FileObject file) {
        assert file != null;
        if (unitTestingSupport_fixClasses != null) {
            return unitTestingSupport_fixClasses;
        }
        final String[] classes = new String[] {""}; //NOI18N
        JavaSource js = JavaSource.forFileObject(file);
        if (js != null) {
            try {
                js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                    public void run(CompilationController ci) throws Exception {
                        if (ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) < 0) {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,
                                    "Unable to resolve "+ci.getFileObject()+" to phase "+JavaSource.Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                    "\nDiagnostics = "+ci.getDiagnostics()+
                                    "\nFree memory = "+Runtime.getRuntime().freeMemory());
                            return;
                        }
                        List<? extends TypeElement> types = ci.getTopLevelElements();
                        if (types.size() > 0) {
                            for (TypeElement type : types) {
                                if (classes[0].length() > 0) {
                                    classes[0] = classes[0] + " ";            // NOI18N
                                }
                                classes[0] = classes[0] + type.getQualifiedName().toString().replace('.', '/') + "*.class";  // NOI18N
                            }
                        }
                    }
                }, true);
            } catch (java.io.IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }
        return classes[0];
    }

    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     */
    private FileObject[] findSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    private FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) {
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
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        if (checkInSrcDir && testSrcPath.length>0) {
            FileObject[] files = findSources (context);
            if (files != null) {
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(project.getSourceRoots().getRoots(),files[0]);
                for (int i=0; i<testSrcPath.length; i++) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files,srcRoot, SRCDIRJAVA, testSrcPath[i], SUBST, true);
                    if (files2 != null) {
                        return files2;
                    }
                }
            }
        }
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
        for (int i=0; i<testSrcPath.length; i++) {
            FileObject[] files2 = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcPath[i], SUBST, true);
            if (files2 != null) {
                return files2;
            }
        }
        return null;
    }

    /**
     * Finds single method specification objects corresponding to JUnit test
     * methods in unit test roots.
     */
    private SingleMethod[] findTestMethods(Lookup context) {
        Collection<? extends SingleMethod> methodSpecs
                                           = context.lookupAll(SingleMethod.class);
        if (methodSpecs.isEmpty()) {
            return null;
        }

        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        if ((testSrcPath == null) || (testSrcPath.length == 0)) {
            return null;
        }

        Collection<SingleMethod> specs = new LinkedHashSet<SingleMethod>(); //#50644: remove dupes
        for (FileObject testRoot : testSrcPath) {
            for (SingleMethod spec : methodSpecs) {
                FileObject f = spec.getFile();
                if (FileUtil.toFile(f) == null) {
                    continue;
                }
                if ((f != testRoot) && !FileUtil.isParentOf(testRoot, f)) {
                    continue;
                }
                if (!f.getNameExt().endsWith(".java")) {                //NOI18N
                    continue;
                }
                specs.add(spec);
            }
        }
        if (specs.isEmpty()) {
            return null;
        }
        return specs.toArray(new SingleMethod[specs.size()]);
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

    private void bypassAntBuildScript(String command, Lookup context, Map<String, Object> p) throws IllegalArgumentException {
        boolean run = true;
        boolean hasMainMethod = true;

        if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command)) {
            final String mainClass = project.evaluator().getProperty(J2SEProjectProperties.MAIN_CLASS);
            ClassPathProviderImpl cpProvider = project.getClassPathProvider();

            assert cpProvider != null;

            p.put(JavaRunner.PROP_CLASSNAME, mainClass);
            p.put(JavaRunner.PROP_EXECUTE_CLASSPATH, cpProvider.getProjectSourcesClassPath(ClassPath.EXECUTE));
            
            if (COMMAND_DEBUG_STEP_INTO.equals(command)) {
                p.put("stopclassname", mainClass);
            }
        } else {
            //run single:
            FileObject[] files = findSources(context);

            if (files == null || files.length != 1) {
                files = findTestSources(context, false);
                hasMainMethod = J2SEProjectUtil.hasMainMethod(files[0]);
                run = false;
            }

            if (files == null || files.length != 1) {
                return ;//warn the user
            }

            p.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
        }
        boolean debug = COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command);
        try {
            if (run) {
                copyMultiValue(J2SEProjectProperties.APPLICATION_ARGS, p);
                JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : JavaRunner.QUICK_RUN, p);
            } else {
                if (hasMainMethod) {
                    JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : JavaRunner.QUICK_RUN, p);
                } else {
                    JavaRunner.execute(debug ? JavaRunner.QUICK_TEST_DEBUG : JavaRunner.QUICK_TEST, p);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void prepareWorkDir(Map<String, Object> properties) {
        String val = evaluator.getProperty(J2SEProjectProperties.RUN_WORK_DIR);

        if (val == null) {
            val = ".";
        }

        File file = this.updateHelper.getAntProjectHelper().resolveFile(val);
        
        properties.put(JavaRunner.PROP_WORK_DIR, file);
    }

    private void copyMultiValue(String propertyName, Map<String, Object> properties) {
        String val = evaluator.getProperty(propertyName);
        if (val != null) {

            putMultiValue(properties,propertyName, val);
        }
    }

    private void putMultiValue(Map<String, Object> properties, String propertyName, String val) {
        @SuppressWarnings(value = "unchecked")
        Collection<String> it = (Collection<String>) properties.get(propertyName);
        if (it == null) {
            properties.put(propertyName, it = new LinkedList<String>());
        }
        it.add(val);
    }

    private void prepareSystemProperties(Map<String, Object> properties, boolean test) {
        String prefix = test ? J2SEProjectProperties.SYSTEM_PROPERTIES_TEST_PREFIX : J2SEProjectProperties.SYSTEM_PROPERTIES_RUN_PREFIX;
        Map<String, String> evaluated = evaluator.getProperties();

        if (evaluated == null) {
            return ;
        }
        
        for (Entry<String, String> e : evaluated.entrySet()) {
            if (e.getKey().startsWith(prefix) && e.getValue() != null) {
                putMultiValue(properties, JavaRunner.PROP_RUN_JVMARGS, "-D" + e.getKey().substring(prefix.length()) + "=" + e.getValue());
            }
        }
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
        if (sourcesRoots.length > 0) {
            ClassPath bootPath = null, compilePath = null;
            try {
                bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);        //Single compilation unit
                assert bootPath != null : assertPath (sourcesRoots[0], sourcesRoots, ClassPath.BOOT);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            try {
                compilePath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);
                assert compilePath != null : assertPath (sourcesRoots[0], sourcesRoots, ClassPath.EXECUTE);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            //todo: The J2SEActionProvider does not require the sourceRoots, it can take the classpath
            //from ClassPathProvider everytime. But the assertions above are important, it seems that
            //the SimpleFileOwnerQueryImplementation is broken in some cases. When assertions are enabled
            //log the data.
            if (bootPath == null) {
                bootPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.BOOT);
            }
            if (compilePath == null) {
                compilePath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.EXECUTE);
            }

            ClassPath sourcePath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
            if (J2SEProjectUtil.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                return MainClassStatus.SET_AND_VALID;
            }
        }
        else {
            ClassPathProviderImpl cpProvider = project.getClassPathProvider();
            if (cpProvider != null) {
                ClassPath bootPath = cpProvider.getProjectSourcesClassPath(ClassPath.BOOT);
                ClassPath compilePath = cpProvider.getProjectSourcesClassPath(ClassPath.EXECUTE);
                ClassPath sourcePath = cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE);   //Empty ClassPath
                if (J2SEProjectUtil.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                    return MainClassStatus.SET_AND_VALID;
                }
            }
        }
        return MainClassStatus.SET_BUT_INVALID;
    }

    private String assertPath (
        FileObject          fileObject,
        FileObject[]          expectedRoots,
        String              pathType
    ) {
        StringBuilder sb = new StringBuilder ();
        sb.append ("File: ").append (fileObject);                                                                       //NOI18N
        sb.append ("\nPath Type: ").append (pathType);                                                                  //NOI18N
        final Project owner = FileOwnerQuery.getOwner(fileObject);
        sb.append ("\nOwner: ").append (owner == null ? "" : ProjectUtils.getInformation(owner).getDisplayName());      //NOI18N
        sb.append ("\nClassPathProviders: ");                                                                           //NOI18N
        for (ClassPathProvider impl  : Lookup.getDefault ().lookupResult (ClassPathProvider.class).allInstances ())
            sb.append ("\n  ").append (impl);                                                                           //NOI18N
        sb.append ("\nProject SourceGroups:");                                                                          //NOI18N
        final SourceGroup[] sgs =  ProjectUtils.getSources(this.project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            sb.append("\n  ").append(FileUtil.getFileDisplayName(sg.getRootFolder()));                                  //NOI18N
        }
        sb.append ("\nProject Source Roots:");                                                                          //NOI18N
        for (FileObject expectedRoot : expectedRoots) {
            sb.append("\n  ").append(FileUtil.getFileDisplayName(expectedRoot));                                        //NOI18N
        }
        return sb.toString ();
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
        final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK"));

        // main class goes wrong => warning
        String message;
        switch (messageType) {
            case UNSET:
                message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassNotFound"), new Object[] {
                    projectName
                });
                break;
            case SET_BUT_INVALID:
                message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassWrong"), new Object[] {
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
            NbBundle.getMessage (MainClassWarning.class, "CTL_MainClassWarning_Title", ProjectUtils.getInformation(project).getDisplayName()), // NOI18N
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            mainClass = panel.getSelectedMainClass ();
            canceled = false;
            ep.put(J2SEProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass);
        }
        dlg.dispose();

        return canceled;
    }

    private String showMainClassWarning (final FileObject file, final Collection<ElementHandle<TypeElement>> mainClasses) {
        assert mainClasses != null;
        String mainClass = null;
        final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK"));

        final MainClassWarning panel = new MainClassWarning (NbBundle.getMessage(MainClassWarning.class, "CTL_FileMultipleMain", file.getNameExt()),mainClasses);
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
        DialogDescriptor desc = new DialogDescriptor (panel,
            NbBundle.getMessage (MainClassWarning.class, "CTL_FileMainClass_Title"), // NOI18N
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() == options[0]) {
            mainClass = panel.getSelectedMainClass ();
        }
        dlg.dispose();
        return mainClass;
    }

    private void showPlatformWarning () {
        final JButton closeOption = new JButton (NbBundle.getMessage(J2SEActionProvider.class, "CTL_BrokenPlatform_Close"));
        closeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEActionProvider.class, "AD_BrokenPlatform_Close"));
        final ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
        final String projectDisplayName = pi == null ?
            NbBundle.getMessage (J2SEActionProvider.class,"TEXT_BrokenPlatform_UnknownProjectName")
            : pi.getDisplayName();
        final DialogDescriptor dd = new DialogDescriptor(
            NbBundle.getMessage(J2SEActionProvider.class, "TEXT_BrokenPlatform", projectDisplayName),
            NbBundle.getMessage(J2SEActionProvider.class, "MSG_BrokenPlatform_Title"),
            true,
            new Object[] {closeOption},
            closeOption,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
    }

    private URL generateAppletHTML(FileObject file) {
        URL url = null;
        try {
            String buildDirProp = project.evaluator().getProperty("build.dir"); //NOI18N
            String classesDirProp = project.evaluator().getProperty("build.classes.dir"); //NOI18N
            FileObject buildDir = this.updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp);
            FileObject classesDir = this.updateHelper.getAntProjectHelper().resolveFileObject(classesDirProp);

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            if (classesDir == null) {
                classesDir = FileUtil.createFolder(project.getProjectDirectory(), classesDirProp);
            }
            String activePlatformName = project.evaluator().getProperty("platform.active"); //NOI18N
            url = AppletSupport.generateHtmlFileURL(file, buildDir, classesDir, activePlatformName);
        } catch (FileStateInvalidException fe) {
            //ingore
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }

    private URL copyAppletHTML(FileObject file, String ext) {
        URL url = null;
        try {
            String buildDirProp = project.evaluator().getProperty("build.dir"); //NOI18N
            FileObject buildDir = updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp);

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            FileObject htmlFile = null;
            htmlFile = file.getParent().getFileObject(file.getName(), "html"); //NOI18N
            if (htmlFile == null) {
                htmlFile = file.getParent().getFileObject(file.getName(), "HTML"); //NOI18N
            }
            if (htmlFile == null) {
                return null;
            }

            FileObject existingFile = buildDir.getFileObject(htmlFile.getName(), htmlFile.getExt());
            if (existingFile != null) {
                existingFile.delete();
            }

            FileObject targetHtml = htmlFile.copy(buildDir, file.getName(), ext);

            if (targetHtml != null) {
                String activePlatformName = project.evaluator().getProperty("platform.active"); //NOI18N
                url = AppletSupport.getHTMLPageURL(targetHtml, activePlatformName);
            }
        } catch (FileStateInvalidException fe) {
            //ingore
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }

    private void showBuildActionWarning(Lookup context) {
        String text = NbBundle.getMessage(J2SEActionProvider.class, "LBL_ProjectBuiltAutomatically");
        String projectProperties = NbBundle.getMessage(J2SEActionProvider.class, "BTN_ProjectProperties");
        String cleanAndBuild = NbBundle.getMessage(J2SEActionProvider.class, "BTN_CleanAndBuild");
        String ok = NbBundle.getMessage(J2SEActionProvider.class, "BTN_OK");
        String titleFormat = NbBundle.getMessage(J2SEActionProvider.class, "TITLE_BuildProjectWarning");
        String title = MessageFormat.format(titleFormat, ProjectUtils.getInformation(project).getDisplayName());
        DialogDescriptor dd = new DialogDescriptor(text,
                                                   title,
                                                   true,
                                                   new Object[] {projectProperties, cleanAndBuild, ok},
                                                   ok,
                                                   DialogDescriptor.DEFAULT_ALIGN,
                                                   null,
                                                   null);

        dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        
        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == projectProperties) {
            CustomizerProviderImpl p = project.getLookup().lookup(CustomizerProviderImpl.class);

            p.showCustomizer("Build"); //NOI18N
            return ;
        }

        if (result == cleanAndBuild) {
            invokeAction(COMMAND_REBUILD, context);
            return ;
        }

        //otherwise dd.getValue() == ok
    }

}
