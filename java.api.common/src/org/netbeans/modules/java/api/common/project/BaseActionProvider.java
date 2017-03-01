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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
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
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.applet.AppletSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import static org.netbeans.modules.java.api.common.project.Bundle.*;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/** Action provider which was originally written for J2SE project and later
 * refactored here so that other EE project types requiring handling of Java
 * related actions can reuse and extend it.
 *
 * @since org.netbeans.modules.java.api.common/1 1.20
 */
public abstract class BaseActionProvider implements ActionProvider {
    public static final String AUTOMATIC_BUILD_TAG = ".netbeans_automatic_build";

    private static final Logger LOG = Logger.getLogger(BaseActionProvider.class.getName());

    public static final String PROPERTY_RUN_SINGLE_ON_SERVER = "run.single.on.server";

    // Project
    private final Project project;

    private final AntProjectHelper antProjectHelper;

    private final Callback callback;
    private final Function<String,ClassPath> classpaths;

    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
    //Property evaluator
    private final PropertyEvaluator evaluator;

    private Sources src;

    // Used only from unit tests to suppress detection of top level classes. If value
    // is different from null it will be returned instead.
    public String unitTestingSupport_fixClasses;
    
    private volatile String buildXMLName;

    private SourceRoots projectSourceRoots;
    private SourceRoots projectTestRoots;

    private boolean serverExecution = false;
    private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;
    private final List<? extends JavaActionProvider.AntTargetInvocationListener> listeners;
    private final AtomicReference<JavaActionProvider> delegate;

    public BaseActionProvider(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, 
            SourceRoots sourceRoots, SourceRoots testRoots, AntProjectHelper antProjectHelper, Callback callback) {
        this.antProjectHelper = antProjectHelper;
        this.callback = callback;
        this.classpaths = (id) -> getCallback().getProjectSourcesClassPath(id);
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
                    if (propName == null || BUILD_SCRIPT.equals(propName)) {
                        buildXMLName = null;
                    }
                }
            }
        });
        this.listeners = Collections.singletonList(new EventAdaptor());
        this.delegate = new AtomicReference<>();
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
    
    /**
     * Returns CoS update status.
     * @return true if CoS update is enabled
     * @since 1.82
     */
    protected boolean isCompileOnSaveUpdate() {
        return isCompileOnSaveEnabled();
    }

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

    public void startFSListener () {
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
    protected final FileObject findBuildXml() {
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
        if (isSupportedByDelegate(command)) {
            getDelegate().invokeAction(command, context);
            return;
        }

        final JavaActionProvider.Context ctx = new JavaActionProvider.Context(
                project,
                updateHelper,
                evaluator,
                classpaths,
                command,
                context,
                userPropertiesPolicy,
                this::getProjectPlatform,
                this::getAdditionalProperties,
                this::getConcealedProperties,
                this::getCompileOnSaveOperations,
                listeners);
        try {
            ActionProviderSupport.invokeTarget(
                    getTargetProvider(command),
                    getCoSProvider(command),
                    ctx,
                    getActionFlags(command),
                    ActionProviderSupport.getCommandDisplayName(command));
        } finally {
            userPropertiesPolicy = ctx.getUserPropertiesPolicy();
        }
    }

    protected void updateJavaRunnerClasspath(String command, Map<String, Object> execProperties) {
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
        final JavaActionProvider.Action action = getDelegate().getAction(command);
        if (action != null) {
            //Handled by delegate
            return Optional.of(action)
                .map((a) -> a instanceof JavaActionProvider.ScriptAction ?
                        ((JavaActionProvider.ScriptAction) a) :
                        null)
                .map((sa) -> {
                    final JavaActionProvider.Context ctx = new JavaActionProvider.Context(
                            project,
                            updateHelper,
                            evaluator,
                            classpaths,
                            command,
                            context,
                            null,
                            null,
                            null,
                            null,
                            this::getCompileOnSaveOperations,
                            Collections.emptyList());
                    final String[] targetNames = sa.getTargetNames(ctx);
                    if (targetNames != null) {
                        Optional.ofNullable(ctx.getProperties())
                                .ifPresent(p::putAll);
                    }
                    return targetNames;
                })
                .orElse(null);
        }
        if (Arrays.asList(getPlatformSensitiveActions()).contains(command)) {
            if (getProjectPlatform() == null) {
                showPlatformWarning ();
                return null;
            }
        }
        LOG.log(Level.FINE, "COMMAND: {0}", command);       //NOI18N
        String[] targetNames = new String[0];
        Map<String,String[]> targetsFromConfig = ActionProviderSupport.loadTargetsFromConfig(project, evaluator);
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
                            final ActionProviderSupport.JavaMainAction javaMainAction = ActionProviderSupport.getJavaMainAction(evaluator);
                            if (javaMainAction == null) {
                                NotifyDescriptor nd = new NotifyDescriptor.Message(LBL_No_Main_Class_Found(clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(nd);
                                return null;
                            } else if (javaMainAction == ActionProviderSupport.JavaMainAction.RUN) {
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
                            } else if (javaMainAction == ActionProviderSupport.JavaMainAction.TEST) {
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
                throw new IllegalArgumentException(command);
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
        return ActionProviderSupport.getActivePlatform(project, evaluator, ProjectProperties.PLATFORM_ACTIVE);
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
        return ActionProviderSupport.getProjectMainClass(
                project,
                evaluator,
                projectSourceRoots,
                classpaths,
                verify);
    }

    private boolean isSupportedByDelegate(final String command) {
        for (String action : getDelegate().getSupportedActions()) {
            if (action.equals(command)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private JavaActionProvider getDelegate() {
        JavaActionProvider jap = delegate.get();
        if (jap == null) {
            jap = createDelegate();
            if (!delegate.compareAndSet(null, jap)) {
                jap = delegate.get();
                assert jap != null : "Transition non-null -> null for delegate.";   //NOI18N
            }
        }
        return jap;
    }

    @NonNull
    private JavaActionProvider createDelegate() {
        final JavaActionProvider.Builder builder = JavaActionProvider.Builder.newInstance(project, updateHelper, evaluator, projectSourceRoots, projectTestRoots, classpaths)
                .setCompileOnSaveOperationsProvider(this::getCompileOnSaveOperations)
                .setActivePlatformProvider(this::getProjectPlatform)
                .setProjectMainClassProvider(this::getProjectMainClass)
                .setProjectMainClassSelector(this::showMainClassSelector);
        final Set<? extends String> supported = new HashSet<>(Arrays.asList(getSupportedActions()));
        for (String op : new String[] {COMMAND_DELETE, COMMAND_RENAME, COMMAND_MOVE, COMMAND_COPY}) {
            if (supported.contains(op)) {
                builder.addAction(builder.createProjectOperation(op));
            }
        }
        final Map<String,String[]> cmds = getCommands();
        final Set<String> scanSensitive = getScanSensitiveActions();
        final Set<String> modelSensitive = getJavaModelActions();
        final Set<String> disabledByServerExecuion = new HashSet<>(Arrays.asList(COMMAND_RUN, COMMAND_DEBUG, COMMAND_PROFILE, COMMAND_DEBUG_STEP_INTO));
        for (String cmd : new String[] {COMMAND_CLEAN, COMMAND_BUILD, COMMAND_REBUILD, COMMAND_RUN, COMMAND_DEBUG, COMMAND_PROFILE, COMMAND_DEBUG_STEP_INTO}) {
            if (supported.contains(cmd)) {
                String[] targets = cmds.get(cmd);
                JavaActionProvider.ScriptAction action;
                if (targets != null) {
                    action = builder.createScriptAction(
                        cmd,
                        modelSensitive.contains(cmd),
                        scanSensitive.contains(cmd),
                        targets);
                    action.setCoSInterceptor((c,m) -> {
                        updateJavaRunnerClasspath(c.getCommand(), m);
                        return true;
                    });
                } else {
                    String[] jarEnabledTargets, jarDisabledTargets;
                    switch (cmd) {
                        case COMMAND_BUILD:
                            jarEnabledTargets = new String[] {"jar"};   //NOI18N
                            jarDisabledTargets = new String[] {"compile"};  //NOI18N
                            break;
                        case COMMAND_REBUILD:
                            jarEnabledTargets = new String[] {"clean","jar"};   //NOI18N
                            jarDisabledTargets = new String[] {"clean","compile"};  //NOI18N
                            break;
                        default:
                            jarEnabledTargets = jarDisabledTargets = null;
                    }
                    action = jarEnabledTargets != null ? builder.createScriptAction(
                            cmd,
                            modelSensitive.contains(cmd),
                            scanSensitive.contains(cmd),
                            ActionProviderSupport.createConditionalTarget(
                                    evaluator,
                                    ActionProviderSupport.createJarEnabledPredicate(),
                                    jarEnabledTargets,
                                    jarDisabledTargets
                            )) : null;
                }
                if (action != null) {
                    if (disabledByServerExecuion.contains(cmd)) {
                        action = new ServerExecutionAwareAction(action);
                    }
                    builder.addAction(action);
                }
            }
        }
        return builder.build();
    }

    @NonNull
    private Set<String> getConcealedProperties(
            @NonNull final String command,
            @NonNull final Lookup context) {
        final Callback clb = getCallback();
        if (clb instanceof Callback3) {
            return ((Callback3)clb).createConcealedProperties(command, context);
        }
        return Collections.emptySet();
    }

    @NonNull
    private Map<String,String> getAdditionalProperties(
            @NonNull final String command,
            @NonNull final Lookup context) {
        final Callback clb = getCallback();
        if (clb instanceof Callback3) {
            return ((Callback3)clb).createAdditionalProperties(command, context);
        }
        return Collections.emptyMap();
    }

    @NonNull
    private Set<? extends JavaActionProvider.CompileOnSaveOperation> getCompileOnSaveOperations() {
        final Set<JavaActionProvider.CompileOnSaveOperation> ops = EnumSet.noneOf(JavaActionProvider.CompileOnSaveOperation.class);
        if (isCompileOnSaveEnabled()) {
            ops.add(JavaActionProvider.CompileOnSaveOperation.EXECUTE);
        }
        if (isCompileOnSaveUpdate()) {
            ops.add(JavaActionProvider.CompileOnSaveOperation.UPDATE);
        }
        return Collections.unmodifiableSet(ops);
    }

    @NonNull
    private Set<? extends ActionProviderSupport.ActionFlag> getActionFlags(@NonNull final String forCommand) {
        final Set<ActionProviderSupport.ActionFlag> flgs = EnumSet.noneOf(ActionProviderSupport.ActionFlag.class);
        if (getScanSensitiveActions().contains(forCommand)) {
            flgs.add(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE);
        }
        if (getJavaModelActions().contains(forCommand)) {
            flgs.add(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE);
        }
        return flgs;
    }

    @NonNull
    private Function<JavaActionProvider.Context,String[]> getTargetProvider(
            @NonNull final String forCommand) {
        return (ctx) -> {
            final Properties p = new Properties();
            final String[] result = getTargetNames(
                    ctx.getCommand(),
                    ctx.getActiveLookup(),
                    p,
                    ctx.doJavaChecks());
            for (Map.Entry<Object,Object> e : p.entrySet()) {
                ctx.setProperty((String)e.getKey(), (String)e.getValue());
            }
            return result;
        };
    }

    @NonNull
    private BiFunction<JavaActionProvider.Context,String[],JavaActionProvider.ScriptAction.Result> getCoSProvider(
            @NonNull final String forCommand) {
        return (ctx, targetNames) -> {
            String command = ctx.getCommand();
            if(COMMAND_TEST_SINGLE.equals(ctx.getCommand()) && Arrays.equals(targetNames, new String[]{COMMAND_TEST})) {
                //multiple files or package(s) selected so we need to call test target instead of test-single
                command = COMMAND_TEST;
            }
            final Map<String, Object> execProperties = ActionProviderSupport.createBaseCoSProperties(ctx);
            if (targetNames.length == 1 && (JavaRunner.QUICK_RUN_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_DEBUG_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_PROFILE_APPLET.equals(targetNames[0]))) {
                try {
                    final FileObject[] selectedFiles = findSources(ctx.getActiveLookup());
                    if (selectedFiles != null) {
                        FileObject file = selectedFiles[0];
                        String url = ctx.getProperty("applet.url");
                        execProperties.put("applet.url", url);
                        execProperties.put(JavaRunner.PROP_EXECUTE_FILE, file);
                        ActionProviderSupport.prepareSystemProperties(evaluator, execProperties, command, ctx.getActiveLookup(), false);
                        return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(targetNames[0], execProperties));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            }
            // for example RUN_SINGLE Java file with Servlet must be run on server and not locally
            boolean serverExecution = ctx.getProperty(PROPERTY_RUN_SINGLE_ON_SERVER) != null;
            ctx.removeProperty(PROPERTY_RUN_SINGLE_ON_SERVER);
            if (!serverExecution && (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command))) {
                ActionProviderSupport.prepareSystemProperties(evaluator,execProperties, command, ctx.getActiveLookup(), false);
                if (COMMAND_RUN_SINGLE.equals(command)) {
                    execProperties.put(JavaRunner.PROP_CLASSNAME, ctx.getProperty("run.class"));
                } else if (COMMAND_DEBUG_SINGLE.equals(command)) {
                    execProperties.put(JavaRunner.PROP_CLASSNAME, ctx.getProperty("debug.class")); 
                } else {
                    execProperties.put(JavaRunner.PROP_CLASSNAME, ctx.getProperty("profile.class"));
                }
                AtomicReference<ExecutorTask> _task = new AtomicReference<>();
                ActionProviderSupport.bypassAntBuildScript(
                        ctx,
                        execProperties,
                        _task,
                        (c,p) -> {
                            updateJavaRunnerClasspath(c.getCommand(), p);
                            return true;
                        });
                final ExecutorTask t = _task.get();
                return t == null ?
                        JavaActionProvider.ScriptAction.Result.abort() :
                        JavaActionProvider.ScriptAction.Result.success(t);
            }
            String buildDir = evaluator.getProperty(ProjectProperties.BUILD_DIR);
            if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command) || COMMAND_PROFILE_TEST_SINGLE.equals(command)) {
                @SuppressWarnings("MismatchedReadAndWriteOfArray")
                FileObject[] files = findTestSources(ctx.getActiveLookup(), true);
                try {
                    ActionProviderSupport.prepareSystemProperties(evaluator,execProperties, command, ctx.getActiveLookup(), true);
                    execProperties.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
                    if (buildDir != null) { // #211543
                        execProperties.put("tmp.dir", updateHelper.getAntProjectHelper().resolvePath(buildDir));
                    }
                    updateJavaRunnerClasspath(command, execProperties);
                    return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(
                            command.equals(COMMAND_TEST_SINGLE) ? JavaRunner.QUICK_TEST : (COMMAND_DEBUG_TEST_SINGLE.equals(command) ? JavaRunner.QUICK_TEST_DEBUG :JavaRunner.QUICK_TEST_PROFILE),
                                       execProperties));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            }
            if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(command)) {
                SingleMethod methodSpec = findTestMethods(ctx.getActiveLookup())[0];
                try {
                    execProperties.put("methodname", methodSpec.getMethodName());//NOI18N
                    execProperties.put(JavaRunner.PROP_EXECUTE_FILE, methodSpec.getFile());
                    if (buildDir != null) {
                        execProperties.put("tmp.dir",updateHelper.getAntProjectHelper().resolvePath(buildDir));
                    }
                    updateJavaRunnerClasspath(command, execProperties);
                    return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(
                            command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG,
                                          execProperties));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            }
            return JavaActionProvider.ScriptAction.Result.follow();
        };
    }

    /**
     * Shows a selector of project main class.
     * @return true if main class was selected, false when project execution was canceled.
     * @since 1.66
     */
    protected boolean showMainClassSelector() {
        return ActionProviderSupport.showCustomizer(
            project,
            updateHelper,
            evaluator,
            projectSourceRoots,
            classpaths);
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

    @Override
    public boolean isActionEnabled( String command, Lookup context ) {
        if (isSupportedByDelegate(command)) {
            return getDelegate().isActionEnabled(command, context);
        }
        if (   Arrays.asList(getActionsDisabledForQuickRun()).contains(command)
            && isCompileOnSaveUpdate()
            && !ActionProviderSupport.allowAntBuild(evaluator, updateHelper)) {
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

    private class EventAdaptor implements JavaActionProvider.AntTargetInvocationListener {

        @Override
        public void antTargetInvocationStarted(String command, Lookup context) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationStarted(command, context));
        }

        @Override
        public void antTargetInvocationFinished(String command, Lookup context, int result) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationFinished(command, context, result));
        }

        @Override
        public void antTargetInvocationFailed(String command, Lookup context) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationFailed(command, context));
        }
    }

    private final class ServerExecutionAwareAction extends JavaActionProvider.ScriptAction {
        private final JavaActionProvider.ScriptAction delegate;

        ServerExecutionAwareAction(
                @NonNull final JavaActionProvider.ScriptAction delegate) {
            super(
                    delegate.getCommand(),
                    null,
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE));
            this.delegate = delegate;
        }

        @Override
        public String[] getTargetNames(JavaActionProvider.Context context) {
            if (!isServerExecution()) {
                return delegate.getTargetNames(context);
            } else {
                final Map<String,String[]> targetsFromConfig = ActionProviderSupport.loadTargetsFromConfig(project, evaluator);
                String[] targets = targetsFromConfig.get(this.getCommand());
                if (targets == null) {
                    targets = getCommands().get(this.getCommand());
                }
                if (targets == null) {
                    throw new IllegalArgumentException(this.getCommand());
                }
                return targets;
            }
        }

        @Override
        public Result performCompileOnSave(JavaActionProvider.Context context, String[] targetNames) {
            if (!isServerExecution()) {
                return delegate.performCompileOnSave(context, targetNames);
            } else {
                return JavaActionProvider.ScriptAction.Result.follow();
            }
        }

        @Override
        public boolean isEnabled(JavaActionProvider.Context context) {
            return delegate.isEnabled(context);
        }
    }
}
