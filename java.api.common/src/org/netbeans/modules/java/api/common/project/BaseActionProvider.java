/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.api.common.project;

import java.awt.Dialog;
import java.awt.EventQueue;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.applet.AppletSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import static org.netbeans.modules.java.api.common.project.Bundle.*;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
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
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

/** Action provider which was originally written for J2SE project and later
 * refactored here so that other EE project types requiring handling of Java
 * related actions can reuse and extend it.
 *
 * @since org.netbeans.modules.java.api.common/1 1.20
 */
public abstract class BaseActionProvider implements ActionProvider {
    public static final String AUTOMATIC_BUILD_TAG = ".netbeans_automatic_build";

    private static final String PROP_JAVA_MAIN_ACTION = "java.main.action"; //NOI18N
    private static final Logger LOG = Logger.getLogger(BaseActionProvider.class.getName());

    public static final String PROPERTY_RUN_SINGLE_ON_SERVER = "run.single.on.server";

    private static final Set<String> NO_SYNC_COMMANDS = Collections.unmodifiableSet(
        new HashSet<String>(
            Arrays.asList(new String[]{
                COMMAND_BUILD,
                COMMAND_CLEAN,
                COMMAND_REBUILD,
                COMMAND_COMPILE_SINGLE,
                JavaProjectConstants.COMMAND_JAVADOC
            })));

    // Project
    private final Project project;

    private final AntProjectHelper antProjectHelper;

    private final Callback callback;

    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
    //Property evaluator
    private final PropertyEvaluator evaluator;

    /** Set of Java source files (as relative path from source root) known to have been modified. See issue #104508. */
    private Set<String> dirty = null;

    private Sources src;
    private List<FileObject> roots;

    // Used only from unit tests to suppress detection of top level classes. If value
    // is different from null it will be returned instead.
    public String unitTestingSupport_fixClasses;
    
    private volatile Boolean allowsFileTracking;
    private volatile String buildXMLName;

    private SourceRoots projectSourceRoots;
    private SourceRoots projectTestRoots;

    private boolean serverExecution = false;
    private UserPropertiesPolicy userPropertiesPolicy;

    public BaseActionProvider(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, 
            SourceRoots sourceRoots, SourceRoots testRoots, AntProjectHelper antProjectHelper, Callback callback) {
        this.antProjectHelper = antProjectHelper;
        this.callback = callback;
        this.updateHelper = updateHelper;
        this.project = project;
        this.evaluator = evaluator;
        this.projectSourceRoots = sourceRoots;
        this.projectTestRoots = testRoots;
        this.evaluator.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                synchronized (BaseActionProvider.class) {
                    final String propName = evt.getPropertyName();
                    if (propName == null || ProjectProperties.TRACK_FILE_CHANGES.equals(propName)) {
                        allowsFileTracking = null;
                        dirty = null;
                    }
                    if (propName == null || BUILD_SCRIPT.equals(propName)) {
                        buildXMLName = null;
                    }
                }
            }
        });
    }

    abstract protected String[] getPlatformSensitiveActions();

    abstract protected String[] getActionsDisabledForQuickRun();

    /** Return map from commands to ant targets */
    abstract public Map<String,String[]> getCommands();

    /**Return set of commands which are affected by background scanning*/
    abstract protected Set<String> getScanSensitiveActions();

    /**Return set of commands which need java model up to date*/
    abstract protected Set<String> getJavaModelActions();

    abstract protected boolean isCompileOnSaveEnabled();

    protected void setServerExecution(boolean serverExecution) {
        this.serverExecution = serverExecution;
    }

    protected boolean isServerExecution() {
        return serverExecution;
    }

    protected PropertyEvaluator getEvaluator() {
        return evaluator;
    }

    protected UpdateHelper getUpdateHelper() {
        return updateHelper;
    }

    protected AntProjectHelper getAntProjectHelper() {
        return antProjectHelper;
    }

    /**
     * Callback for project private data.
     *
     * @return
     * @see Callback
     * @see Callback2
     */
    protected Callback getCallback() {
        return callback;
    }

    private boolean allowsFileChangesTracking () {
        //allowsFileTracking is volatile primitive, fine to do double checking
        synchronized (this) {
            if (allowsFileTracking != null) {
                return allowsFileTracking.booleanValue();
            }
        }
        final String val = evaluator.getProperty(ProjectProperties.TRACK_FILE_CHANGES);
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

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized (BaseActionProvider.this) {
                BaseActionProvider.this.roots = null;
            }
        }
    };


    public void startFSListener () {
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
        Sources _src;
        synchronized (this) {
            if (this.roots != null) {
                return this.roots;
            }
            if (this.src == null) {
                this.src = ProjectUtils.getSources(this.project);
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

    // Main build.xml location
    public static final String BUILD_SCRIPT = ProjectProperties.BUILD_SCRIPT;

    @NonNull
    public static String getBuildXmlName (final Project project, PropertyEvaluator evaluator) {
        return CommonProjectUtils.getBuildXmlName(evaluator, null);
    }

    public static FileObject getBuildXml (final Project project, PropertyEvaluator evaluator) {
        return getBuildXml(project, getBuildXmlName(project, evaluator));
    }
    
    private static FileObject getBuildXml(
            @NonNull final Project project,
            @NonNull final String buildXmlName) {
        return project.getProjectDirectory().getFileObject (buildXmlName);
    }

    @CheckForNull
    private FileObject findBuildXml() {
        String name = buildXMLName;
        if (name == null) {
            buildXMLName = name = getBuildXmlName(project, evaluator);
        }
        assert name != null;
        return getBuildXml(project, name);
    }

    protected final Project getProject() {
        return project;
    }

    @Messages("LBL_No_Build_XML_Found=The project does not have a build script.")
    @Override
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        assert EventQueue.isDispatchThread();
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
        final String[] userPropertiesFile = new String[]{verifyUserPropertiesFile()};

        final boolean isCompileOnSaveEnabled = isCompileOnSaveEnabled();
        final AtomicReference<Thread> caller = new AtomicReference<Thread>(Thread.currentThread());
        final AtomicBoolean called = new AtomicBoolean(false);
        // XXX prefer to call just if and when actually starting target, but that is hard to calculate here
        final ActionProgress listener = ActionProgress.start(context);

        class  Action implements Runnable {

            /**
             * True when the action always requires access to java model
             */
            private boolean needsJavaModel = true;
            /**
             * When true getTargetNames accesses java model, when false
             * the default values (possibly incorrect) are used.
             */
            private boolean doJavaChecks = true;
            ExecutorTask task;

            @Override
            public void run () {
                if (!needsJavaModel && caller.get() != Thread.currentThread()) {
                    return;
                }
                called.set(true);
                try {
                    doRun();
                } finally {
                    if (task != null) {
                        task.addTaskListener(new TaskListener() {
                            @org.netbeans.api.annotations.common.SuppressWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
                            @Override public void taskFinished(Task _) {
                                listener.finished(task.result() == 0);
                            }
                        });
                    } else {
                        listener.finished(false);
                    }
                }
            }

            void doRun() {
                Properties p = new Properties();
                p.put("nb.internal.action.name", command);                  //NOI18N
                if (userPropertiesFile[0] != null) {
                    p.put("user.properties.file", userPropertiesFile[0]);   //NOI18N
                }
                String[] targetNames;

                targetNames = getTargetNames(command, context, p, doJavaChecks);
                if (targetNames == null) {
                    return;
                }
                final String command2execute;
                if(COMMAND_TEST_SINGLE.equals(command) && targetNames.length == 1 && targetNames[0].equals(COMMAND_TEST)) {
                    //multiple files or package(s) selected so we need to call test target instead of test-single
                    command2execute = COMMAND_TEST;
                    p.put("nb.internal.action.name", command2execute);
                } else {
                    command2execute = command;
                }
                if (isCompileOnSaveEnabled) {
                    if (COMMAND_BUILD.equals(command2execute) && !allowAntBuild()) {
                        showBuildActionWarning(context);
                        return ;
                    }
                    Map<String, Object> execProperties = new HashMap<String, Object>();
                    execProperties.put("nb.internal.action.name", command2execute);

                    copyMultiValue(ProjectProperties.RUN_JVM_ARGS, execProperties);
                    prepareWorkDir(execProperties);

                    execProperties.put(JavaRunner.PROP_PLATFORM, getProjectPlatform());
                    execProperties.put(JavaRunner.PROP_PROJECT_NAME, ProjectUtils.getInformation(project).getDisplayName());
                    String runtimeEnc = evaluator.getProperty(ProjectProperties.RUNTIME_ENCODING);
                    if (runtimeEnc != null) {
                        try {
                            Charset runtimeChs = Charset.forName(runtimeEnc);
                            execProperties.put(JavaRunner.PROP_RUNTIME_ENCODING, runtimeChs); //NOI18N
                        } catch (IllegalCharsetNameException ichsn) {
                            LOG.log(Level.WARNING, "Illegal charset name: {0}", runtimeEnc); //NOI18N
                        } catch (UnsupportedCharsetException uchs) {
                            LOG.log(Level.WARNING, "Unsupported charset : {0}", runtimeEnc); //NOI18N
                        }
                    }

                    if (targetNames.length == 1 && (JavaRunner.QUICK_RUN_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_DEBUG_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_PROFILE_APPLET.equals(targetNames[0]))) {
                        try {
                            final FileObject[] selectedFiles = findSources(context);
                            if (selectedFiles != null) {
                                FileObject file = selectedFiles[0];
                                String url = p.getProperty("applet.url");
                                execProperties.put("applet.url", url);
                                execProperties.put(JavaRunner.PROP_EXECUTE_FILE, file);
                                prepareSystemProperties(execProperties, command2execute, context, false);
                                task =
                                JavaRunner.execute(targetNames[0], execProperties);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                    if (!isServerExecution() && (COMMAND_RUN.equals(command2execute) || COMMAND_DEBUG.equals(command2execute) || COMMAND_DEBUG_STEP_INTO.equals(command2execute) || COMMAND_PROFILE.equals(command2execute))) {
                        prepareSystemProperties(execProperties, command2execute, context, false);
                        AtomicReference<ExecutorTask> _task = new AtomicReference<ExecutorTask>();
                        bypassAntBuildScript(command2execute, context, execProperties, _task);
                        task = _task.get();
                        return ;
                    }
                    // for example RUN_SINGLE Java file with Servlet must be run on server and not locally
                    boolean serverExecution = p.getProperty(PROPERTY_RUN_SINGLE_ON_SERVER) != null;
                    p.remove(PROPERTY_RUN_SINGLE_ON_SERVER);
                    if (!serverExecution && (COMMAND_RUN_SINGLE.equals(command2execute) || COMMAND_DEBUG_SINGLE.equals(command2execute) || COMMAND_PROFILE_SINGLE.equals(command2execute))) {
                        prepareSystemProperties(execProperties, command2execute, context, false);
                        if (COMMAND_RUN_SINGLE.equals(command2execute)) {
                            execProperties.put(JavaRunner.PROP_CLASSNAME, p.getProperty("run.class"));
                        } else if (COMMAND_DEBUG_SINGLE.equals(command2execute)) {
                            execProperties.put(JavaRunner.PROP_CLASSNAME, p.getProperty("debug.class")); 
                        } else {
                            execProperties.put(JavaRunner.PROP_CLASSNAME, p.getProperty("profile.class"));
                        }
                        AtomicReference<ExecutorTask> _task = new AtomicReference<ExecutorTask>();
                        bypassAntBuildScript(command2execute, context, execProperties, _task);
                        task = _task.get();
                        return;
                    }
                    String buildDir = evaluator.getProperty(ProjectProperties.BUILD_DIR);
                    if (COMMAND_TEST_SINGLE.equals(command2execute) || COMMAND_DEBUG_TEST_SINGLE.equals(command2execute) || COMMAND_PROFILE_TEST_SINGLE.equals(command2execute)) {
                        @SuppressWarnings("MismatchedReadAndWriteOfArray")
                        FileObject[] files = findTestSources(context, true);
                        try {
                            prepareSystemProperties(execProperties, command2execute, context, true);
                            execProperties.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
                            if (buildDir != null) { // #211543
                                execProperties.put("tmp.dir", updateHelper.getAntProjectHelper().resolvePath(buildDir));
                            }
                            updateJavaRunnerClasspath(command2execute, execProperties);
                            task =
                            JavaRunner.execute(command2execute.equals(COMMAND_TEST_SINGLE) ? JavaRunner.QUICK_TEST : (COMMAND_DEBUG_TEST_SINGLE.equals(command2execute) ? JavaRunner.QUICK_TEST_DEBUG :JavaRunner.QUICK_TEST_PROFILE),
                                               execProperties);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                    if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command2execute) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(command2execute)) {
                        SingleMethod methodSpec = findTestMethods(context)[0];
                        try {
                            execProperties.put("methodname", methodSpec.getMethodName());//NOI18N
                            execProperties.put(JavaRunner.PROP_EXECUTE_FILE, methodSpec.getFile());
                            if (buildDir != null) {
                                execProperties.put("tmp.dir",updateHelper.getAntProjectHelper().resolvePath(buildDir));
                            }
                            updateJavaRunnerClasspath(command2execute, execProperties);
                            task =
                            JavaRunner.execute(command2execute.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG,
                                                  execProperties);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }
                }
                collectStartupExtenderArgs(p, command2execute);
                Set<String> concealedProperties = collectAdditionalProperties(p, command2execute, context);
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (isCompileOnSaveEnabled && !NO_SYNC_COMMANDS.contains(command2execute)) {
                    p.put("nb.wait.for.caches", "true");
                }
                final Callback cb = getCallback();
                final Callback2 cb2 = (cb instanceof Callback2) ? (Callback2) cb : null;
                if (p.keySet().isEmpty()) {
                    p = null;
                }
                try {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(LBL_No_Build_XML_Found(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        if (cb2 != null) {
                            cb2.antTargetInvocationStarted(command2execute, context);
                        }
                        try {
                            task = ActionUtils.runTarget(buildFo, targetNames, p, concealedProperties);
                            task.addTaskListener(new TaskListener() {
                                @Override
                                public void taskFinished(Task _) {
                                    try {
                                        if (task.result() != 0) {
                                            synchronized (BaseActionProvider.this) {
                                                // #120843: if a build fails, disable dirty-list optimization.
                                                dirty = null;
                                            }
                                        }
                                    } finally {
                                        if (cb2 != null) {
                                            cb2.antTargetInvocationFinished(command2execute, context, task.result());
                                        }
                                    }
                                }
                            });
                        } catch (IOException ex) {
                            if (cb2 != null) {
                                cb2.antTargetInvocationFailed(command2execute, context);
                            }
                            throw ex;
                        } catch (RuntimeException ex) {
                            if (cb2 != null) {
                                cb2.antTargetInvocationFailed(command2execute, context);
                            }
                            throw ex;
                        }
                    }
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        final Action action = new Action();

        if (getJavaModelActions().contains(command) || (isCompileOnSaveEnabled && getScanSensitiveActions().contains(command))) {
            //Always have to run with java model
            ScanDialog.runWhenScanFinished(action, commandName(command));
        }
        else if (getScanSensitiveActions().contains(command)) {
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
        } else {
            //Does not need java model
            action.run();
        }
    }

    @Messages({
        "ACTION_run=Run Project",
        "ACTION_run.single=Run File",
        "ACTION_run.single.method=Run File",
        "ACTION_debug=Debug Project",
        "ACTION_debug.single=Debug File",
        "ACTION_debug.single.method=Debug File",
        "ACTION_debug.stepinto=Debug Project",
        "ACTION_debug.fix=Apply Code Changes",
        "ACTION_debug.test.single=Debug Test",
        "ACTION_profile=Profile Project",
        "ACTION_profile.single=Profile File",
        "ACTION_profile.test.single=Profile Test",
        "ACTION_rebuild=Rebuild Project",
        "ACTION_build=Build Project",
        "ACTION_clean=Clean Project",
        "ACTION_compile.single=Compile File",
        "ACTION_javadoc=Generate JavaDoc",
        "ACTION_test=Test Project",
        "ACTION_test.single=Test File"
    })
    private String commandName(String command) throws MissingResourceException {
        if (command.equals("run")) {
            return ACTION_run();
        } else if (command.equals("run.single")) {
            return ACTION_run_single();
        } else if (command.equals("run.single.method")) {
            return ACTION_run_single_method();
        } else if (command.equals("debug")) {
            return ACTION_debug();
        } else if (command.equals("debug.single")) {
            return ACTION_debug_single();
        } else if (command.equals("debug.single.method")) {
            return ACTION_debug_single_method();
        } else if (command.equals("debug.stepinto")) {
            return ACTION_debug_stepinto();
        } else if (command.equals("debug.fix")) {
            return ACTION_debug_fix();
        } else if (command.equals("debug.test.single")) {
            return ACTION_debug_test_single();
        } else if (command.equals("profile")) {
            return ACTION_profile();
        } else if (command.equals("profile.single")) {
            return ACTION_profile_single();
        } else if (command.equals("profile.test.single")) {
            return ACTION_profile_test_single();
        } else if (command.equals("rebuild")) {
            return ACTION_rebuild();
        } else if (command.equals("build")) {
            return ACTION_build();
        } else if (command.equals("clean")) {
            return ACTION_clean();
        } else if (command.equals("compile.single")) {
            return ACTION_compile_single();
        } else if (command.equals("javadoc")) {
            return ACTION_javadoc();
        } else if (command.equals("test")) {
            return ACTION_test();
        } else if (command.equals("test.single")) {
            return ACTION_test_single();
        } else {
            return command;
        }
    }

    protected void updateJavaRunnerClasspath(String command, Map<String, Object> execProperties) {
    }

    //where
    private static void invokeByJavaSource (final Runnable runnable) throws IOException {
        Parameters.notNull("runnable", runnable);   //NOI18N
        final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]));
        final JavaSource js = JavaSource.create(info);
        js.runWhenScanFinished(new org.netbeans.api.java.source.Task<CompilationController>() {
            @Override
            public void run(final CompilationController controller) throws Exception {
                runnable.run();
            }
        }, true);
    }

    /**
     * Compatibility
     *
     */
    public String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        return getTargetNames(command, context, p, true);
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    @Messages({"# {0} - class name", "LBL_No_Main_Class_Found=Class \"{0}\" does not have a main method."})
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public @CheckForNull String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        if (Arrays.asList(getPlatformSensitiveActions()).contains(command)) {
            if (getProjectPlatform() == null) {
                showPlatformWarning ();
                return null;
            }
        }
        LOG.log(Level.FINE, "COMMAND: {0}", command);       //NOI18N
        String[] targetNames = new String[0];
        Map<String,String[]> targetsFromConfig = loadTargetsFromConfig();
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = projectSourceRoots.getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
                String[] targets = targetsFromConfig.get(command);
                targetNames = (targets != null) ? targets : getCommands().get(command);
            } else {
                FileObject[] testRoots = projectTestRoots.getRoots();
                files = findSourcesAndPackages(context, testRoots);
                if (files != null) {
                    p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
                    targetNames = new String[] {"compile-test-single"}; // NOI18N
                } else {
                    return null;
                }
            }
        } else if ( command.equals( COMMAND_TEST ) ){
            p.setProperty("ignore.failing.tests", "true");  //NOI18N
            targetNames = getCommands().get(command);
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            p.setProperty("ignore.failing.tests", "true");  //NOI18N
            final FileObject[] files = findTestSourcesForFiles(context);
            if (files == null) {
                return null;
            }
            if(files.length == 1 && files[0].isData()) {
                //one file or a package containing one file selected
            targetNames = setupTestSingle(p, files, projectTestRoots);
            } else {
                //multiple files or package(s) selected
                targetNames = setupTestFilesOrPackages(p, files);
            }
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            final FileObject[] files = findTestSources(context, true);
            if (files == null) {
                return null;
            }
            targetNames = setupDebugTestSingle(p, files, projectTestRoots);
        } else if ( command.equals( COMMAND_PROFILE_TEST_SINGLE ) ) {
            final FileObject[] files = findTestSources(context, true);
            if (files == null) {
                return null;
            }
            targetNames = setupProfileTestSingle(p, files, projectTestRoots);
        } else if ( command.equals( SingleMethod.COMMAND_RUN_SINGLE_METHOD ) ) {
            SingleMethod[] methodSpecs = findTestMethods(context);
            if ((methodSpecs == null) || (methodSpecs.length != 1)) {
                return new String[0];
            }
            targetNames = setupRunSingleTestMethod(p, methodSpecs[0]);
        } else if ( command.equals( SingleMethod.COMMAND_DEBUG_SINGLE_METHOD ) ) {
            SingleMethod[] methodSpecs = findTestMethods(context);
            if ((methodSpecs == null) || (methodSpecs.length != 1)) {
                return new String[0];
            }
            targetNames = setupDebugSingleTestMethod(p, methodSpecs[0]);
        } else if ( command.equals( JavaProjectConstants.COMMAND_DEBUG_FIX ) ) {
            FileObject[] files = findSources( context );
            String path;
            String classes = "";    //NOI18N
            if (files != null) {
                path = FileUtil.getRelativePath(getRoot(projectSourceRoots.getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
                classes = getTopLevelClasses(files[0]);
            } else {
                files = findTestSources(context, false);
                assert files != null : "findTestSources () can't be null: " + Arrays.toString(projectTestRoots.getRoots());   //NOI18N
                path = FileUtil.getRelativePath(getRoot(projectTestRoots.getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix-test"}; // NOI18N
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
            p.setProperty("fix.classes", classes); // NOI18N
        } else if (!isServerExecution() && (command.equals (COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO) || command.equals(COMMAND_PROFILE))) {
            // check project's main class
            // Check whether main class is defined in this config. Note that we use the evaluator,
            // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
            // to define a main class - in this case an active config need not override it.

            // If a specific config was selected, just skip this check for now.
            // XXX would ideally check that that config in fact had a main class.
            // But then evaluator.getProperty(MAIN_CLASS) would be inaccurate.
            // Solvable but punt on it for now.
            final boolean hasCfg = context.lookup(ProjectConfiguration.class) != null;
            final boolean verifyMain = doJavaChecks && !hasCfg && getJavaMainAction() == null;
            String mainClass = getProjectMainClass(verifyMain);
            if (mainClass == null) {
                do {
                    // show warning, if cancel then return
                    if (!showMainClassSelector()) {
                        return null;
                    }
                    // No longer use the evaluator: have not called putProperties yet so it would not work.
                    mainClass = evaluator.getProperty(ProjectProperties.MAIN_CLASS);
                    mainClass = getProjectMainClass(verifyMain);
                } while (mainClass == null);
            }
            if (!command.equals(COMMAND_RUN) && /* XXX should ideally look up proper mainClass in evaluator x config */ mainClass != null) {
                if (command.equals(COMMAND_PROFILE)) {
                    p.setProperty("run.class", mainClass); // NOI18N
                } else {
                    p.setProperty("debug.class", mainClass); // NOI18N
                }
            }
            String[] targets = targetsFromConfig.get(command);
            targetNames = (targets != null) ? targets : getCommands().get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
            prepareDirtyList(p, false);
        } else if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            FileObject[] rootz = projectTestRoots.getRoots();
            boolean isTest = true;
            if (files == null) {
                isTest = false;
                files = findSources(context);
                rootz = projectSourceRoots.getRoots();
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Is test: {0} Files: {1} Roots: {2}",    //NOI18N
                        new Object[]{
                            isTest,
                            asPaths(files),
                            asPaths(rootz)
                });
            }
            if (files == null) {
                //The file was not found under the source roots
                return null;
            }
            final FileObject file = files[0];
            assert file != null;
            if (!file.isValid()) {
                LOG.log(Level.WARNING,
                        "FileObject to execute: {0} is not valid.",
                        FileUtil.getFileDisplayName(file));   //NOI18N
                return null;
            }
            String clazz = FileUtil.getRelativePath(getRoot(rootz, file), file);
            if (clazz == null) {
                return null;
            }
            p.setProperty("javac.includes", clazz); // NOI18N
            // Convert foo/FooTest.java -> foo.FooTest
            if (clazz.endsWith(".java")) { // NOI18N
                clazz = clazz.substring(0, clazz.length() - 5);
            }
            clazz = clazz.replace('/','.');
            LOG.log(Level.FINE, "Class to run: {0}", clazz);    //NOI18N
            final boolean hasMainClassFromTest = MainClassChooser.unitTestingSupport_hasMainMethodResult == null ? false :
                MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue();
            if (doJavaChecks) {
                final Collection<ElementHandle<TypeElement>> mainClasses = CommonProjectUtils.getMainMethods (file);
                LOG.log(Level.FINE, "Main classes: {0} ", mainClasses);
                if (!hasMainClassFromTest && mainClasses.isEmpty()) {
                    if (!isTest && AppletSupport.isApplet(file)) {

                        EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String jvmargs = ep.getProperty(ProjectProperties.RUN_JVM_ARGS);

                        // do this only when security policy is not set manually
                        if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") != -1)) {  //NOI18N
                            AppletSupport.generateSecurityPolicy(project.getProjectDirectory());
                            if ((jvmargs == null) || (jvmargs.length() == 0)) {
                                ep.setProperty(ProjectProperties.RUN_JVM_ARGS, "-Djava.security.policy=applet.policy"); //NOI18N
                            } else {
                                ep.setProperty(ProjectProperties.RUN_JVM_ARGS, jvmargs + " -Djava.security.policy=applet.policy"); //NOI18N
                            }
                            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException e) {
                                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + e);
                            }
                        }
                        URL url;
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
                        } else if (COMMAND_DEBUG_SINGLE.equals(command)) {
                            p.setProperty("debug.class", clazz); // NOI18N
                            targetNames = new String[] {"debug-applet"}; // NOI18N
                        } else if (COMMAND_PROFILE_SINGLE.equals(command)) {
                            p.setProperty("run.class", clazz); // NOI18N
                            targetNames = new String[]{"profile-applet"}; // NOI18N
                        }
                    } else {
                        List<String> alternativeTargetNames = new ArrayList<>();
                        if (isTest) {
                            //Fallback to normal (non-main-method-based) unit test run
                            if (command.equals(COMMAND_RUN_SINGLE)) {
                                targetNames = setupTestSingle(p, files, projectTestRoots);
                            } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
                                targetNames = setupDebugTestSingle(p, files, projectTestRoots);
                            } else {
                                targetNames = setupProfileTestSingle(p, files, projectTestRoots);
                            }
                        } else if (handleJavaClass(p, file, command, alternativeTargetNames)) {
                            if (alternativeTargetNames.size() > 0) {
                                targetNames = alternativeTargetNames.toArray(new String[alternativeTargetNames.size()]);
                            } else {
                                return null;
                            }
                        } else {
                            final JavaMainAction javaMainAction = getJavaMainAction();
                            if (javaMainAction == null) {
                                NotifyDescriptor nd = new NotifyDescriptor.Message(LBL_No_Main_Class_Found(clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(nd);
                                return null;
                            } else if (javaMainAction == JavaMainAction.RUN) {
                                String prop;
                                switch (command) {
                                    case COMMAND_RUN_SINGLE:
                                        prop = "run.class"; //NOI18N
                                        break;
                                    case COMMAND_DEBUG_SINGLE:
                                        prop = "debug.class";   //NOI18N
                                        break;
                                    default:
                                        prop = "run.class"; //NOI18N
                                }
                                p.setProperty(prop, clazz);
                                final String[] targets = targetsFromConfig.get(command);
                                targetNames = targets != null ? targets : getCommands().get(command);
                            } else if (javaMainAction == JavaMainAction.TEST) {
                                switch (command) {
                                    case COMMAND_RUN_SINGLE:
                                        targetNames = setupTestSingle(p, files, projectSourceRoots);
                                        break;
                                    case COMMAND_DEBUG_SINGLE:
                                        targetNames = setupDebugTestSingle(p, files, projectSourceRoots);
                                        break;
                                    default:
                                        targetNames = setupProfileTestSingle(p, files, projectSourceRoots);
                                        break;
                                }
                            }
                        }
                    }
                } else {
                    if (!hasMainClassFromTest) {
                        if (mainClasses.size() == 1) {
                            //Just one main class
                            clazz = mainClasses.iterator().next().getBinaryName();
                        } else {
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
                        targetNames = (targets != null) ? targets : (isTest ? new String[] { "run-test-with-main" } : getCommands().get(COMMAND_RUN_SINGLE));
                    } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
                        p.setProperty("debug.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] {"debug-test-with-main"} : getCommands().get(COMMAND_DEBUG_SINGLE));
                    } else {
                        p.setProperty("run.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] {"profile-test-with-main"} : getCommands().get(COMMAND_PROFILE_SINGLE));
                    }
                }
            } else {
                //The Java model is not ready, we cannot determine if the file is applet or main class or unit test
                //Acts like everything is main class, maybe for test folder junit is better default?
                if (command.equals (COMMAND_RUN_SINGLE)) {
                        p.setProperty("run.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : (isTest ? new String[] { "run-test-with-main" } : getCommands().get(COMMAND_RUN_SINGLE));    //NOI18N
                } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
                    p.setProperty("debug.class", clazz); // NOI18N
                    String[] targets = targetsFromConfig.get(command);
                    targetNames = (targets != null) ? targets : (isTest ? new String[] {"debug-test-with-main"} : getCommands().get(COMMAND_DEBUG_SINGLE));      //NOI18N
                } else {
                    p.setProperty("run.class", clazz); // NOI18N
                    String[] targets = targetsFromConfig.get(command);
                    targetNames = (targets != null) ? targets : (isTest ? new String[] {"profile-test-with-main"} : getCommands().get(COMMAND_PROFILE_SINGLE));      //NOI18N
                }
            }
        } else {
            String[] targets = targetsFromConfig.get(command);
            targetNames = (targets != null) ? targets : getCommands().get(command);
            if (targetNames == null) {
                String buildTarget = "false".equalsIgnoreCase(evaluator.getProperty(ProjectProperties.DO_JAR)) ? "compile" : "jar"; // NOI18N
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
        
        return targetNames;
    }

    /**
     * Returns the project's {@link JavaPlatform}.
     * @return the project's {@link JavaPlatform} or null when project's
     * {@link JavaPlatform} is broken.
     * @since 1.66
     */
    @CheckForNull
    protected JavaPlatform getProjectPlatform() {
        return CommonProjectUtils.getActivePlatform(evaluator.getProperty(ProjectProperties.PLATFORM_ACTIVE));
    }

    /**
     * @param targetNames caller of this method must set this parameter to empty 
     *  modifiable array; implementor of this method can return alternative target
     *  names to be used to handle this Java class
     */
    protected boolean handleJavaClass(Properties p, FileObject javaFile, String command, List<String> targetNames) {
        return false;
    }

    /**
     * Gets the project main class to be executed.
     * @param verify if true the java checks should be performed
     * and the main class should be returned only if it's valid
     * @return the main class
     * @since 1.66
     */
    @CheckForNull
    protected String getProjectMainClass(final boolean verify) {
        final String mainClass = evaluator.getProperty(ProjectProperties.MAIN_CLASS);
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult ?
                mainClass :
                null;
        }
        if (mainClass == null || mainClass.length () == 0) {
            LOG.fine("Main class is not set");    //NOI18N
            return null;
        }
        if (!verify) {
            return mainClass;
        }
        final FileObject[] sourcesRoots = projectSourceRoots.getRoots();
        if (sourcesRoots.length > 0) {
            LOG.log(Level.FINE, "Searching main class {0} for root: {1}",   //NOI18N
                    new Object[] {
                        mainClass,
                        FileUtil.getFileDisplayName(sourcesRoots[0])
            });
            ClassPath bootPath = null, compilePath = null;
            try {
                bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);        //Single compilation unit
                assert bootPath != null : assertPath (
                        sourcesRoots[0],
                        sourcesRoots,
                        projectSourceRoots,
                        ClassPath.BOOT);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            try {
                compilePath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);
                assert compilePath != null : assertPath (
                        sourcesRoots[0],
                        sourcesRoots,
                        projectSourceRoots,
                        ClassPath.EXECUTE);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            //todo: The J2SEActionProvider does not require the sourceRoots, it can take the classpath
            //from ClassPathProvider everytime. But the assertions above are important, it seems that
            //the SimpleFileOwnerQueryImplementation is broken in some cases. When assertions are enabled
            //log the data.
            if (bootPath == null) {
                LOG.fine("Source root has no boot classpath, using project boot classpath.");   //NOI18N
                bootPath = callback.getProjectSourcesClassPath(ClassPath.BOOT);
            }
            if (compilePath == null) {
                LOG.fine("Source root has no execute classpath, using project execute classpath.");   //NOI18N
                compilePath = callback.getProjectSourcesClassPath(ClassPath.EXECUTE);
            }

            ClassPath sourcePath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
            LOG.log(Level.FINE, "Classpaths used to resolve main boot: {0}, exec: {1}, src: {2}",   //NOI18N
                    new Object[]{
                        bootPath,
                        compilePath,
                        sourcePath
            });
            if (CommonProjectUtils.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                return mainClass;
            }
        } else {
            LOG.log(Level.FINE, "Searching main class {0} without source root", mainClass);  //NOI18N
            ClassPath bootPath = callback.getProjectSourcesClassPath(ClassPath.BOOT);
            ClassPath compilePath = callback.getProjectSourcesClassPath(ClassPath.EXECUTE);
            ClassPath sourcePath = callback.getProjectSourcesClassPath(ClassPath.SOURCE);   //Empty ClassPath
            LOG.log(Level.FINE, "Classpaths used to resolve main boot: {0}, exec: {1}, src: {2}",   //NOI18N
                    new Object[]{
                        bootPath,
                        compilePath,
                        sourcePath
            });
            if (CommonProjectUtils.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                return mainClass;
            }
        }
        LOG.log(Level.FINE, "Main class {0} is invalid.", mainClass);   //NOI18N
        return null;
    }

    private String assertPath (
            FileObject          fileObject,
            FileObject[]        expectedRoots,
            SourceRoots         roots,
            String              pathType) {
        final StringBuilder sb = new StringBuilder ();
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
        sb.append ("\nProject Source Roots(");                                                                          //NOI18N
        sb.append(System.identityHashCode(roots));
        sb.append("):");                                                                                                //NOI18N
        for (FileObject expectedRoot : expectedRoots) {
            sb.append("\n  ").append(FileUtil.getFileDisplayName(expectedRoot));                                        //NOI18N
        }
        return sb.toString ();
    }

    /**
     * Shows a selector of project main class.
     * @return true if main class was selected, false when project execution was canceled.
     * @since 1.66
     */
    @Messages({
        "LBL_MainClassWarning_ChooseMainClass_OK=OK",
        "AD_MainClassWarning_ChooseMainClass_OK=N/A",
        "# {0} - project name", "LBL_MainClassNotFound=Project {0} does not have a main class set.",
        "# {0} - name of class", "# {1} - project name", "LBL_MainClassWrong={0} class wasn''t found in {1} project.",
        "CTL_MainClassWarning_Title=Run Project"
        })
    protected boolean showMainClassSelector() {
        boolean result = false;
        final JButton okButton = new JButton(LBL_MainClassWarning_ChooseMainClass_OK());
        okButton.getAccessibleContext().setAccessibleDescription(AD_MainClassWarning_ChooseMainClass_OK());        
        // main class goes wrong => warning
        String mainClass = getProjectMainClass(false);
        String message;
        if (mainClass == null) {
            message = LBL_MainClassNotFound(ProjectUtils.getInformation(project).getDisplayName());
        } else {
            message = LBL_MainClassWrong(
                mainClass,
                ProjectUtils.getInformation(project).getDisplayName());
        }
        final MainClassWarning panel = new MainClassWarning (message, projectSourceRoots.getRoots());
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        panel.addChangeListener (new ChangeListener () {
            @Override
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
            CTL_MainClassWarning_Title(),
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() == options[0]) {
            mainClass = panel.getSelectedMainClass ();
            String config = evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            String path;
            if (config == null || config.length() == 0) {
                path = AntProjectHelper.PROJECT_PROPERTIES_PATH;
            } else {
                // Set main class for a particular config only.
                path = "nbproject/configs/" + config + ".properties"; // NOI18N
            }
            final EditableProperties ep = updateHelper.getProperties(path);
            ep.put(ProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass);
            try {
                if (updateHelper.requestUpdate()) {
                    updateHelper.putProperties(path, ep);
                    ProjectManager.getDefault().saveProject(project);
                    result = true;
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe);
            }
        }
        dlg.dispose();
        return result;
    }

    private void prepareDirtyList(Properties p, boolean isExplicitBuildTarget) {
        String doDepend = evaluator.getProperty(ProjectProperties.DO_DEPEND);
        String buildClassesDirValue = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClassesDirValue == null) {            
            //Log
            StringBuilder logRecord = new StringBuilder();
            logRecord.append("EVALUATOR: ").append(evaluator.getProperties()).append(";"); // NOI18N
            logRecord.append("PROJECT_PROPS: ").append(updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).entrySet()).append(";"); // NOI18N
            logRecord.append("PRIVATE_PROPS: ").append(updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).entrySet()).append(";"); // NOI18N
            LOG.log(Level.WARNING, "No build.classes.dir property: {0}", logRecord.toString());
            return;
        }
        File buildClassesDir = antProjectHelper.resolveFile(buildClassesDirValue);
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
        String config = evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
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
        Enumeration<?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            if (propName.startsWith("$target.")) {
                String tNameVal = props.getProperty(propName);
                if (tNameVal != null && !tNameVal.equals("")) {
                    String cmdNameKey = propName.substring("$target.".length());
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

    private String[] setupTestFilesOrPackages(Properties p, FileObject[] files) {
        if (files != null) {
            FileObject root = getRoot(projectTestRoots.getRoots(), files[0]);
            // the replace part is so that we can test everything under a package recusively
            p.setProperty("includes", ActionUtils.antIncludesList(files, root).replace("**", "**/*Test.java")); // NOI18N
        }
        return new String[]{"test"}; // NOI18N
    }

    private void setupTestSingleCommon(Properties p, FileObject[] files, SourceRoots sourceRoots) {
        final FileObject[] srcPath = sourceRoots.getRoots();
        final FileObject root = getRoot(srcPath, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        final String path = FileUtil.getRelativePath(root, files[0]);
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
    }

    private String[] setupTestSingle(Properties p, FileObject[] files, SourceRoots sourceRoots) {
        setupTestSingleCommon(p, files, sourceRoots);
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, FileObject[] files, SourceRoots sourceRoots) {
        setupTestSingleCommon(p, files, sourceRoots);
        return new String[] {"debug-test"}; // NOI18N
    }

    private String[] setupProfileTestSingle(Properties p, FileObject[] files, SourceRoots sourceRoots) {
        setupTestSingleCommon(p, files, sourceRoots);
        return new String[] {"profile-test"}; // NOI18N
    }

    private String[] setupRunSingleTestMethod(Properties p, SingleMethod methodSpec) {
//        return setupTestSingle(p, new FileObject[] {methodSpec.getFile()});

        FileObject[] testSrcPath = projectTestRoots.getRoots();
        FileObject testFile = methodSpec.getFile();
        FileObject root = getRoot(testSrcPath, testFile);
        String relPath = FileUtil.getRelativePath(root, testFile);
        String className = getClassName(relPath);
        p.setProperty("javac.includes", relPath); // NOI18N
        p.setProperty("test.class", className); // NOI18N
        p.setProperty("test.method", methodSpec.getMethodName()); // NOI18N
        return new String[] {"test-single-method"}; // NOI18N
    }

    private String[] setupDebugSingleTestMethod(Properties p, SingleMethod methodSpec) {
//        return setupDebugTestSingle(p, new FileObject[] {methodSpec.getFile()});

        FileObject[] testSrcPath = projectTestRoots.getRoots();
        FileObject testFile = methodSpec.getFile();
        FileObject root = getRoot(testSrcPath, testFile);
        String relPath = FileUtil.getRelativePath(root, testFile);
        String className = getClassName(relPath);
        p.setProperty("javac.includes", relPath); // NOI18N
        p.setProperty("test.class", className); // NOI18N
        p.setProperty("test.method", methodSpec.getMethodName()); // NOI18N
        return new String[] {"debug-test-method"}; // NOI18N
    }

    private static String getClassName(String relPath) {
        // Convert foo/FooTest.java -> foo.FooTest
        return relPath.substring(0, relPath.length() - 5).replace('/', '.');
    }

    private boolean allowAntBuild() {
        String buildClasses = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClasses == null) return false;
        File buildClassesFile = this.updateHelper.getAntProjectHelper().resolveFile(buildClasses);

        return !new File(buildClassesFile, AUTOMATIC_BUILD_TAG).exists();
    }

    @Override
    public boolean isActionEnabled( String command, Lookup context ) {
        if (COMMAND_DELETE.equals(command) 
            || COMMAND_MOVE.equals(command)
            || COMMAND_COPY.equals(command)
            || COMMAND_RENAME.equals(command)) {
            return true;
        }   
        if (   Arrays.asList(getActionsDisabledForQuickRun()).contains(command)
            && isCompileOnSaveEnabled()
            && !allowAntBuild()) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, projectSourceRoots.getRoots()) != null
                    || findSourcesAndPackages( context, projectTestRoots.getRoots()) != null;
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] fos = findTestSourcesForFiles(context);
            return fos != null;
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] fos = findTestSources(context, true);
            return fos != null && fos.length == 1;
        } else if ( command.equals( COMMAND_PROFILE_TEST_SINGLE ) ) {
            FileObject[] fos = findTestSources(context, true);
            return fos != null && fos.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE) ||
                        command.equals(COMMAND_DEBUG_SINGLE) ||
                        command.equals(COMMAND_PROFILE_SINGLE) ||
                        command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject fos[] = findSources(context);
            if (fos != null && fos.length == 1) {
                return true;
            }
            fos = findTestSources(context, false);
            if (fos != null && fos.length == 1) {
                return true;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Source Roots: {0} Test Roots: {1} Lookup Content: {2}",    //NOI18N
                        new Object[]{
                            asPaths(projectSourceRoots.getRoots()),
                            asPaths(projectTestRoots.getRoots()),
                            asPaths(context)
                        });
            }
            return false;
        } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD)
                || command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
//            if (isCompileOnSaveEnabled()) {
                SingleMethod[] methodSpecs = findTestMethods(context);
                return (methodSpecs != null) && (methodSpecs.length == 1);
//            } else {
//                return false;
//            }
        } else {
            // other actions are global
            return true;
        }
    }



    // Private methods -----------------------------------------------------


    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    private static final String SUBSTNG = "NGTest.java"; // NOI18N


    /**
     * Lists all top level classes in a String, classes are separated by space (" ")
     * Used by debugger fix and continue (list of files to fix)
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
                    @Override
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
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findSources(Lookup context) {
        return findSources(context, true, false);
    }
    
    /**
     * Find selected source files
     *
     * @param context the lookup in which files should be found
     * @param strict if true, all files in the selection have to be accepted
     * @param findInPackages if true, all files under a selected package in the selection will also be checked
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findSources(Lookup context, boolean strict, boolean findInPackages) {
        FileObject[] srcPath = projectSourceRoots.getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcPath[i], findInPackages ? null : ".java", strict); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
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

    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findSourcesAndPackages (Lookup context, FileObject[] srcRoots) {
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
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        return findTestSources(context, checkInSrcDir, true, false);
    }
    
    /**
     * Find selected tests and/or tests which belong to selected source files
     *
     * @param context the lookup in which files should be found
     * @param checkInSrcDir if true, tests which belong to selected source files will be searched for
     * @param strict if true, all files in the selection have to be accepted
     * @param findInPackages if true, all files under a selected package in the selection will also be checked
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull
    FileObject[] findTestSources(Lookup context, boolean checkInSrcDir, boolean strict, boolean findInPackages) {
        //XXX: Ugly, should be rewritten
        FileObject[] testSrcPaths = projectTestRoots.getRoots();
        for (FileObject testSrcPath : testSrcPaths) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcPath, findInPackages ? null : ".java", strict); // NOI18N
            ArrayList<FileObject> testFOs = new ArrayList<>();
            if (files != null) {
                for (FileObject file : files) {
                    if ((file.hasExt("java") || findInPackages && file.isFolder())) {
                        testFOs.add(file);
                    }
                }
                return testFOs.isEmpty() ?
                    null:
                    testFOs.toArray(new FileObject[testFOs.size()]);
            }
        }
        if (checkInSrcDir && testSrcPaths.length > 0) {
            FileObject[] files = findSources(context, strict, findInPackages);
            if (files != null) {
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(projectSourceRoots.getRoots(), files[0]);
                for (FileObject testSrcPath : testSrcPaths) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files, srcRoot, SRCDIRJAVA, testSrcPath, SUBST, strict);
                    if (files2 != null && files2.length != 0) {
                        return files2;
                    }
                    FileObject[] files2NG = ActionUtils.regexpMapFiles(files, srcRoot, SRCDIRJAVA, testSrcPath, SUBSTNG, strict);
                    if (files2NG != null && files2NG.length != 0) {
                        return files2NG;
                    }
                }
                // no test files found. The selected FOs might be folders under source packages
                files = ActionUtils.findSelectedFiles(context, srcRoot, findInPackages ? null : ".java", strict); // NOI18N
                ArrayList<FileObject> testFOs = new ArrayList<>();
                if (files != null) {
                    for (FileObject file : files) {
                        if (findInPackages && file.isFolder()) {
                            String relativePath = FileUtil.getRelativePath(srcRoot, file);
                            if (relativePath != null) {
                                for (FileObject testSrcPath : testSrcPaths) {
                                    FileObject testFO = FileUtil.toFileObject(new File(FileUtil.toFile(testSrcPath).getPath().concat(File.separator).concat(relativePath)));
                                    if (testFO != null) {
                                        testFOs.add(testFO);
                                    }
                                }
                            }
                        }
                    }
                    return testFOs.isEmpty() ?
                        null :
                        testFOs.toArray(new FileObject[testFOs.size()]);
                }
            }
        }
        return null;
    }

    /**
     * Find selected tests and tests which belong to selected source files
     * when package(s) or multiple files are selected.
     *
     * @param context the lookup in which files should be found
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull FileObject[] findTestSourcesForFiles(Lookup context) {
        FileObject[] sourcesFOs = findSources(context, false, true);
        FileObject[] testSourcesFOs = findTestSources(context, false, false, true);
        HashSet<FileObject> testFiles = new HashSet<>();
        if(testSourcesFOs == null) { // no test files were selected
            return findTestSources(context, true, false, true); // return tests which belong to selected source files, if any
        } else {
            if(sourcesFOs == null) { // only test files were selected
                return testSourcesFOs;
            } else { // both test and source files were selected, do not return any dublicates
                testFiles.addAll(Arrays.asList(testSourcesFOs));
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(projectSourceRoots.getRoots(),sourcesFOs[0]);
                for (FileObject testRoot : projectTestRoots.getRoots()) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBST, true);
                    if (files2 != null) {
                        for (FileObject fo : files2) {
                            if(!testFiles.contains(fo)) {
                                testFiles.add(fo);
                            }
                        }
                    }
                    FileObject[] files2NG = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBSTNG, true);
                    if (files2NG != null) {
                        for (FileObject fo : files2NG) {
                            if(!testFiles.contains(fo)) {
                                testFiles.add(fo);
                            }
                        }
                    }
                }
            }
        }
        return testFiles.isEmpty() ? null : testFiles.toArray(new FileObject[testFiles.size()]);
    }

    /**
     * Finds single method specification objects corresponding to JUnit test
     * methods in unit test roots.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private @CheckForNull SingleMethod[] findTestMethods(Lookup context) {
        Collection<? extends SingleMethod> methodSpecs
                                           = context.lookupAll(SingleMethod.class);
        if (methodSpecs.isEmpty()) {
            return null;
        }

        FileObject[] testSrcPath = projectTestRoots.getRoots();
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

    private void bypassAntBuildScript(String command, Lookup context, Map<String, Object> p, AtomicReference<ExecutorTask> task) throws IllegalArgumentException {
        final JavaMainAction javaMainAction = getJavaMainAction();
        boolean run = javaMainAction != JavaMainAction.TEST;
        boolean hasMainMethod = run;

        if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command) || COMMAND_PROFILE.equals(command)) {
            final String mainClass = evaluator.getProperty(ProjectProperties.MAIN_CLASS);

            p.put(JavaRunner.PROP_CLASSNAME, mainClass);
            p.put(JavaRunner.PROP_EXECUTE_CLASSPATH, callback.getProjectSourcesClassPath(ClassPath.EXECUTE));
            
            if (COMMAND_DEBUG_STEP_INTO.equals(command)) {
                p.put("stopclassname", mainClass);
            }
        } else {
            //run single:
            FileObject[] files = findSources(context);

            if (files == null || files.length != 1) {
                files = findTestSources(context, false);
                if (files != null && files.length == 1) {
                    hasMainMethod = CommonProjectUtils.hasMainMethod(files[0]);
                    run = false;
                }
            } else if (!hasMainMethod) {
                hasMainMethod = CommonProjectUtils.hasMainMethod(files[0]);
            }

            if (files == null || files.length != 1) {
                return ;//warn the user
            }

            p.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
        }
        boolean debug = COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command);
        boolean profile = COMMAND_PROFILE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command);
        try {
            updateJavaRunnerClasspath(command, p);
            if (run) {
                copyMultiValue(ProjectProperties.APPLICATION_ARGS, p);
                task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : (profile ? JavaRunner.QUICK_PROFILE : JavaRunner.QUICK_RUN), p));
            } else {
                if (hasMainMethod) {
                    task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : (profile ? JavaRunner.QUICK_PROFILE : JavaRunner.QUICK_RUN), p));
                } else {
                    task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_TEST_DEBUG : (profile ? JavaRunner.QUICK_TEST_PROFILE : JavaRunner.QUICK_TEST), p));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void prepareWorkDir(Map<String, Object> properties) {
        String val = evaluator.getProperty(ProjectProperties.RUN_WORK_DIR);

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

    private List<String> runJvmargsIde(String command) {
        StartupExtender.StartMode mode;
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            return Collections.emptyList();
        }
        List<String> args = new ArrayList<String>();
        JavaPlatform p = getProjectPlatform();
        for (StartupExtender group : StartupExtender.getExtenders(Lookups.fixed(project, p != null ? p : JavaPlatformManager.getDefault().getDefaultPlatform()), mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }
    
    private void collectStartupExtenderArgs(Map<? super String,? super String> p, String command) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(command)) {
            b.append(' ').append(arg);
        }
        if (b.length() > 0) {
            p.put(ProjectProperties.RUN_JVM_ARGS_IDE, b.toString());
        }
    }

    @CheckForNull
    private Set<String> collectAdditionalProperties(Map<? super String,? super String> p, String command, Lookup context) {
        final Callback cb = getCallback();
        if (cb instanceof Callback3) {
            final Map<String,String> additionalProperties = ((Callback3)cb).createAdditionalProperties(command, context);
            assert additionalProperties != null;
            p.putAll(additionalProperties);
            return ((Callback3)cb).createConcealedProperties(command, context);
        }
        return null;
    }

    @CheckForNull
    private Set<String> prepareSystemProperties(Map<String, Object> properties, String command, Lookup context, boolean test) {
        String prefix = test ? ProjectProperties.SYSTEM_PROPERTIES_TEST_PREFIX : ProjectProperties.SYSTEM_PROPERTIES_RUN_PREFIX;
        Map<String, String> evaluated = evaluator.getProperties();

        if (evaluated == null) {
            return null;
        }
        
        for (Entry<String, String> e : evaluated.entrySet()) {
            if (e.getKey().startsWith(prefix) && e.getValue() != null) {
                putMultiValue(properties, JavaRunner.PROP_RUN_JVMARGS, "-D" + e.getKey().substring(prefix.length()) + "=" + e.getValue());
            }
        }        
        collectStartupExtenderArgs(properties, command);
        return collectAdditionalProperties(properties, command, context);
    }

    @Messages({
        "# {0} - file name", "CTL_FileMultipleMain=The file {0} has more main classes.",
        "CTL_FileMainClass_Title=Run File"
    })
    private String showMainClassWarning (final FileObject file, final Collection<ElementHandle<TypeElement>> mainClasses) {
        assert mainClasses != null;
        String mainClass = null;
        final JButton okButton = new JButton(LBL_MainClassWarning_ChooseMainClass_OK());
        okButton.getAccessibleContext().setAccessibleDescription(AD_MainClassWarning_ChooseMainClass_OK());

        final MainClassWarning panel = new MainClassWarning(CTL_FileMultipleMain(file.getNameExt()), mainClasses);
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };

        panel.addChangeListener (new ChangeListener () {
            @Override
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
            CTL_FileMainClass_Title(),
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

    @Messages({
        "CTL_BrokenPlatform_Close=Close",
        "AD_BrokenPlatform_Close=N/A",
        "# {0} - project name", "TEXT_BrokenPlatform=<html><p><strong>The project {0} has a broken platform reference.</strong></p><br><p> You have to fix the broken reference and invoke the action again.</p>",
        "MSG_BrokenPlatform_Title=Broken Platform Reference"
    })
    private void showPlatformWarning () {
        final JButton closeOption = new JButton(CTL_BrokenPlatform_Close());
        closeOption.getAccessibleContext().setAccessibleDescription(AD_BrokenPlatform_Close());
        final String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        final DialogDescriptor dd = new DialogDescriptor(
            TEXT_BrokenPlatform(projectDisplayName),
            MSG_BrokenPlatform_Title(),
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
        try {
            String buildDirProp = evaluator.getProperty("build.dir"); //NOI18N
            String classesDirProp = evaluator.getProperty("build.classes.dir"); //NOI18N
            FileObject buildDir = buildDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp) : null;
            FileObject classesDir = classesDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(classesDirProp) : null;

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            if (classesDir == null) {
                classesDir = FileUtil.createFolder(project.getProjectDirectory(), classesDirProp);
            }
            String activePlatformName = evaluator.getProperty("platform.active"); //NOI18N
            return AppletSupport.generateHtmlFileURL(file, buildDir, classesDir, activePlatformName);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
    }

    private URL copyAppletHTML(FileObject file, String ext) {
        URL url = null;
        try {
            String buildDirProp = evaluator.getProperty("build.dir"); //NOI18N
            FileObject buildDir = buildDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp) : null;

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            FileObject htmlFile = file.getParent().getFileObject(file.getName(), "html"); //NOI18N
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
                String activePlatformName = evaluator.getProperty("platform.active"); //NOI18N
                url = AppletSupport.getHTMLPageURL(targetHtml, activePlatformName);
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }

    @Messages({
        "LBL_ProjectBuiltAutomatically=<html><b>This project's source files are compiled automatically when you save them.</b><br>You do not need to build the project to run or debug the project in the IDE.<br><br>If you need to build or rebuild the project's JAR file, use Clean and Build.<br>To disable the automatic compiling feature and activate the Build command,<br>go to Project Properties and disable Compile on Save.",
        "BTN_ProjectProperties=Project Properties...",
        "BTN_CleanAndBuild=Clean and Build",
        "BTN_OK=OK",
        "# {0} - project name", "TITLE_BuildProjectWarning=Build Project ({0})"
    })
    @org.netbeans.api.annotations.common.SuppressWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    private void showBuildActionWarning(Lookup context) {
        String projectProperties = BTN_ProjectProperties();
        String cleanAndBuild = BTN_CleanAndBuild();
        String ok = BTN_OK();
        DialogDescriptor dd = new DialogDescriptor(LBL_ProjectBuiltAutomatically(),
                                                   TITLE_BuildProjectWarning(ProjectUtils.getInformation(project).getDisplayName()),
                                                   true,
                                                   new Object[] {projectProperties, cleanAndBuild, ok},
                                                   ok,
                                                   DialogDescriptor.DEFAULT_ALIGN,
                                                   null,
                                                   null);

        dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        
        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == projectProperties) {
            CustomizerProvider2 p = project.getLookup().lookup(CustomizerProvider2.class);

            p.showCustomizer("Build", null); //NOI18N
            return ;
        }

        if (result == cleanAndBuild) {
            invokeAction(COMMAND_REBUILD, context);
        }

        //otherwise dd.getValue() == ok
    }

    /**
     * Callback for accessing project private data.
     */
    public static interface Callback {
        ClassPath getProjectSourcesClassPath(String type);
        ClassPath findClassPath(FileObject file, String type);
    }

    /**
     * Callback for accessing project private data and supporting
     * ant invocation hooks.
     * 
     * @since 1.29
     */
    public static interface Callback2 extends Callback {

        /**
         * Called before an <i>ant</i> target is invoked. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         */
        void antTargetInvocationStarted(final String command, final Lookup context);

        /**
         * Called after the <i>ant</i> target invocation. This does not reflect
         * whether the ant target returned error or not, just successful invocation.
         * Note that call to {@link #invokeAction(java.lang.String, org.openide.util.Lookup)}
         * does not necessarily means call to ant.
         *
         * @param command executed command
         * @param context the invocation context
         */
        void antTargetInvocationFinished(final String command, final Lookup context, int result);

        /**
         * Called when the <i>ant</i> target invocation failed. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command failed command
         * @param context the invocation context
         */
        void antTargetInvocationFailed(final String command, final Lookup context);

    }

    /**
     * Callback for accessing project private data and supporting
     * ant invocation hooks.
     *
     * @since 1.58
     */
    public static interface Callback3 extends Callback2 {
        /**
         * Creates additional properties passed to the <i>ant</t>.
         * Called before an <i>ant</i> target is invoked. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         * @return the {@link Map} of additional properties.
         */
        @NonNull
        Map<String,String> createAdditionalProperties(@NonNull String command, @NonNull Lookup context);


        /**
         * Returns names of concealed properties.
         * Values of such properties are not printed into UI.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         * @return the {@link Set} of property names.
         */
        @NonNull
        Set<String> createConcealedProperties(@NonNull String command, @NonNull Lookup context);
    }

    public static final class CallbackImpl implements Callback {

        private ClassPathProviderImpl cp;

        public CallbackImpl(ClassPathProviderImpl cp) {
            this.cp = cp;
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return cp.getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return cp.findClassPath(file, type);
        }

    }

    private static @CheckForNull Collection<? extends String> asPaths(final @NullAllowed FileObject[] fos) {
        if (fos == null) {
            return null;
        }
        final Collection<String> result = new ArrayList<String>(fos.length);
        for (FileObject fo : fos) {
            result.add(FileUtil.getFileDisplayName(fo));
        }
        return result;
    }

    private static @NonNull Collection<? extends String> asPaths(final @NonNull Lookup context) {
        final Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
        final Collection<String> result = new ArrayList<String>(dobjs.size());
        for (DataObject dobj : dobjs) {
            result.add(FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
        }
        return result;
    }

    private static enum JavaMainAction {
        RUN("run"),     //NOI18N
        TEST("test");   //NOI18N

        private final String name;
        JavaMainAction(@NonNull final String name) {
            assert name != null;
            this.name = name;
        }
        @CheckForNull
        static JavaMainAction forName(@NullAllowed final String name) {
            if (RUN.name.equals(name)) {
                return RUN;
            } else if (TEST.name.equals(name)) {
                return TEST;
            }
            return null;
        }
    }

    private JavaMainAction getJavaMainAction() {
        return JavaMainAction.forName(evaluator.getProperty(PROP_JAVA_MAIN_ACTION));
    }

    private static enum UserPropertiesPolicy {
        RUN_ANYWAY(NbBundle.getMessage(BaseActionProvider.class, "OPTION_Run_Anyway")),
        RUN_WITH(NbBundle.getMessage(BaseActionProvider.class, "OPTION_Run_With")),
        RUN_UPDATE(NbBundle.getMessage(BaseActionProvider.class, "OPTION_Run_Update"));

        private final String displayName;

        UserPropertiesPolicy(@NonNull final String displayName) {
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    @CheckForNull
    private String verifyUserPropertiesFile() {
        final String currentPath = evaluator.getProperty("user.properties.file");      //NOI18N
        final File current = currentPath == null ? null : FileUtil.normalizeFile(antProjectHelper.resolveFile(currentPath));
        final File expected = FileUtil.normalizeFile(new File(System.getProperty("netbeans.user"), "build.properties")); // NOI18N
        if (!expected.equals(current)) {
            if (userPropertiesPolicy == null) {
                final Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        NbBundle.getMessage(BaseActionProvider.class, "MSG_InvalidBuildPropertiesPath", ProjectUtils.getInformation(project).getDisplayName()),
                        NbBundle.getMessage(BaseActionProvider.class, "TITLE_InvalidBuildPropertiesPath"),
                        0,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        UserPropertiesPolicy.values(),
                        UserPropertiesPolicy.RUN_ANYWAY));
                userPropertiesPolicy = option instanceof UserPropertiesPolicy ?
                        (UserPropertiesPolicy) option :
                        null;
            }
            if (null != userPropertiesPolicy) {
                switch (userPropertiesPolicy) {
                    case RUN_ANYWAY:
                        return null;
                    case RUN_WITH:
                        return expected.getAbsolutePath();
                    case RUN_UPDATE:
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            @Override
                            public void run() {
                                final EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                ep.setProperty("user.properties.file", expected.getAbsolutePath()); //NOI18N
                                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                                try {
                                    ProjectManager.getDefault().saveProject(project);
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }
                        });
                        return null;
                    default:
                }
            }
        }
        return null;
    }
}
