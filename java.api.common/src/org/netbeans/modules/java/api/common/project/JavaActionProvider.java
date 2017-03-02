/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.project;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class JavaActionProvider implements ActionProvider {
    private static final Logger LOG = Logger.getLogger(JavaActionProvider.class.getName());
    private static final FileObject[] EMPTY = new FileObject[0];

    private final Project prj;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator eval;
    private final Function<String,ClassPath> classpaths;
    private final Map<String,Action> supportedActions;
    private final List<AntTargetInvocationListener> listeners;
    private final Supplier<? extends JavaPlatform> jpp;
    private final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
    private final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
    private final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;
    private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;

    private JavaActionProvider(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Function<String,ClassPath> classpaths,
            @NonNull final Collection<? extends Action> actions,
            @NonNull Supplier<? extends JavaPlatform> javaPlatformProvider,
            @NullAllowed final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider,
            @NullAllowed final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider,
            @NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("updateHelper", updateHelper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("classpaths", classpaths);   //NOI18N
        Parameters.notNull("actions", actions); //NOI18N
        Parameters.notNull("javaPlatformProvider", javaPlatformProvider);   //NOI18N
        Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
        Parameters.notNull("mfs", mfs);
        this.prj = project;
        this.updateHelper = updateHelper;
        this.eval = evaluator;
        this.classpaths = classpaths;
        final Map<String,Action> abn = new HashMap<>();
        for (Action action : actions) {
            abn.put(action.getCommand(), action);
        }
        this.supportedActions = Collections.unmodifiableMap(abn);
        this.listeners = new CopyOnWriteArrayList<>();
        this.listeners.add(new AntTargetInvocationListener() {
            @Override
            public void antTargetInvocationStarted(String command, Lookup context) {
            }
            @Override
            public void antTargetInvocationFinished(String command, Lookup context, int result) {
                if (result != 0 || COMMAND_CLEAN.equals(command)) {
                    mfs.resetDirtyList();
                }
            }
            @Override
            public void antTargetInvocationFailed(String command, Lookup context) {
            }
        });
        this.jpp = javaPlatformProvider;
        this.additionalPropertiesProvider = additionalPropertiesProvider;
        this.concealedPropertiesProvider = concealedPropertiesProvider;
        this.cosOpsProvider = cosOpsProvider;
    }

    public static enum CompileOnSaveOperation {
        UPDATE,
        EXECUTE
    }

    public static interface AntTargetInvocationListener extends EventListener {
        void antTargetInvocationStarted(final String command, final Lookup context);
        void antTargetInvocationFinished(final String command, final Lookup context, int result);
        void antTargetInvocationFailed(final String command, final Lookup context);
    }

    public static interface Action {
        String getCommand();
        boolean isEnabled(@NonNull final Context context);
        void invoke(@NonNull final Context context);
    }

    public static final class Context {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator eval;
        private final Function<String,ClassPath> classpaths;
        private final String command;
        private final Lookup lkp;
        private boolean doJavaChecks;
        private Set<? extends CompileOnSaveOperation> cosOpsCache;
        private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;
        private final Supplier<? extends JavaPlatform> jpp;
        private final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
        private final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
        private final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;
        private final Collection<? extends AntTargetInvocationListener> listeners;
        private final Properties properties;
        private final Set<String> concealedProperties;

        Context(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator eval,
                @NonNull final Function<String,ClassPath> classpaths,
                @NonNull final String command,
                @NonNull final Lookup lookup,
                @NullAllowed final ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy,
                @NullAllowed final Supplier<? extends JavaPlatform> jpp,
                @NullAllowed final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider,
                @NullAllowed final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider,
                @NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider,
                @NonNull final Collection<? extends AntTargetInvocationListener> listeners) {
            this.project = project;
            this.updateHelper = updateHelper;
            this.eval = eval;
            this.classpaths = classpaths;
            this.command = command;
            this.lkp = lookup;
            this.doJavaChecks = true;
            this.userPropertiesPolicy = userPropertiesPolicy;
            this.jpp = jpp;
            this.additionalPropertiesProvider = additionalPropertiesProvider;
            this.concealedPropertiesProvider = concealedPropertiesProvider;
            this.cosOpsProvider = cosOpsProvider;
            this.listeners = listeners;
            this.properties = new Properties();
            this.concealedProperties = new HashSet<>();
        }

        @NonNull
        public String getCommand() {
            return command;
        }

        @NonNull
        public Lookup getActiveLookup() {
            return lkp;
        }

        @NonNull
        public Project getProject() {
            return project;
        }

        @NonNull
        public UpdateHelper getUpdateHelper() {
            return updateHelper;
        }

        @NonNull
        public PropertyEvaluator getPropertyEvaluator() {
            return eval;
        }

        @CheckForNull
        public JavaPlatform getActiveJavaPlatform() {
            return Optional.ofNullable(jpp)
                    .map((p) -> p.get())
                    .orElse(null);
        }

        public boolean doJavaChecks () {
            return doJavaChecks;
        }

        @NonNull
        public Set<? extends CompileOnSaveOperation> getCompileOnSaveOperations() {
            Set<? extends CompileOnSaveOperation> res = cosOpsCache;
            if (res == null) {
                res = cosOpsCache = cosOpsProvider.get();
            }
            return res;
        }

        @CheckForNull
        public ClassPath getProjectClassPath(@NonNull final String classPathId) {
            return classpaths.apply(classPathId);
        }

        @CheckForNull
        String getProperty(@NonNull final String propName) {
            return properties.getProperty(propName);
        }

        void setProperty(
                @NonNull final String propName,
                @NonNull final String propValue) {
            Parameters.notNull("propName", propName);   //NOI18N
            Parameters.notNull("propValue", propValue);   //NOI18N
            properties.put(propName, propValue);
        }

        void removeProperty(
                @NonNull final String propName) {
            properties.remove(propName);
        }

        void addConcealedProperty(@NonNull final String propName) {
            Parameters.notNull("propName", propName);   //NOI18N
            concealedProperties.add(propName);
        }

        void setJavaChecks(final boolean doJavaChecks) {
            this.doJavaChecks = doJavaChecks;
        }

        @CheckForNull
        ActionProviderSupport.UserPropertiesPolicy getUserPropertiesPolicy() {
            return userPropertiesPolicy;
        }

        void setUserPropertiesPolicy(@NullAllowed final ActionProviderSupport.UserPropertiesPolicy p) {
            userPropertiesPolicy = p;
        }

        void fireAntTargetInvocationListener(final int state, final int res) {
            for (AntTargetInvocationListener l : listeners) {
                switch (state) {
                    case 0:
                        l.antTargetInvocationStarted(command, lkp);
                        break;
                    case 1:
                        l.antTargetInvocationFinished(command, lkp, res);
                        break;
                    case 2:
                        l.antTargetInvocationFailed(command, lkp);
                        break;
                    default:
                        throw new IllegalArgumentException(Integer.toString(state));
                }
            }
        }

        @CheckForNull
        Properties getProperties() {
            Optional.ofNullable(additionalPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(properties::putAll);
            return properties.keySet().isEmpty() ?
                    null :
                    properties;
        }

        @CheckForNull
        Set<String> getConcealedProperties() {
            Optional.ofNullable(concealedPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(concealedProperties::addAll);
            return concealedProperties.isEmpty() ?
                    null :
                    concealedProperties;
        }
    }


    public static abstract class ScriptAction implements Action {
        private final String command;
        private final String displayName;
        private final Set<ActionProviderSupport.ActionFlag> actionFlags;
        private volatile BiFunction<Context,Map<String,Object>,Boolean> cosInterceptor;

        public static final class Result {
            private static final Result ABORT = new Result(null);
            private static final Result FOLLOW = new Result(null);

            private final ExecutorTask task;

            private Result(@NullAllowed final ExecutorTask task) {
                this.task = task;
            }

            ExecutorTask getTask() {
                return task;
            }

            @NonNull
            public static Result success(@NonNull final ExecutorTask task) {
                Parameters.notNull("task", task);   //NOI18N
                return new Result(task);
            }

            @NonNull
            public static Result abort() {
                return ABORT;
            }

            @NonNull
            public static Result follow() {
                return FOLLOW;
            }
        }

        protected ScriptAction (
                @NonNull final String command,
                @NullAllowed final String dispalyName,
                final boolean platformSensitive,
                final boolean javaModelSensitive,
                final boolean scanSensitive) {
            Parameters.notNull("command", command); //NOI18N
            this.command = command;
            this.displayName = dispalyName;
            this.actionFlags = EnumSet.noneOf(ActionProviderSupport.ActionFlag.class);
            if (platformSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE);
            }
            if (javaModelSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE);
            }
            if (scanSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE);
            }
        }

        @CheckForNull
        public abstract String[] getTargetNames(@NonNull final Context context);

        @NonNull
        public Result performCompileOnSave(@NonNull final Context context, @NonNull final String[] targetNames) {
            return Result.follow();
        }

        @Override
        public final String getCommand() {
            return command;
        }

        @Override
        public final void invoke(@NonNull final Context context) {
            if (actionFlags.contains(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE) && context.getActiveJavaPlatform() == null) {
                ActionProviderSupport.showPlatformWarning(context.getProject());
                return;
            }
            ActionProviderSupport.invokeTarget(
                    this::getTargetNames,
                    this::performCompileOnSave,
                    context,
                    actionFlags,
                    getDisplayName());
        }

        @NonNull
        final String getDisplayName() {
            String res = displayName;
            if (res == null) {
                res = ActionProviderSupport.getCommandDisplayName(command);
            }
            return res;
        }

        @NonNull
        final Set<ActionProviderSupport.ActionFlag> getActionFlags() {
            return actionFlags;
        }

        @CheckForNull
        final BiFunction<Context,Map<String,Object>,Boolean> getCoSInterceptor() {
            return cosInterceptor;
        }

        final void setCoSInterceptor(@NullAllowed final BiFunction<Context,Map<String,Object>,Boolean> cosInterceptor) {
            this.cosInterceptor = cosInterceptor;
        }
    }

    public static final class Builder {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;
        private final List<Action> actions;
        private final ActionProviderSupport.ModifiedFilesSupport mfs;
        private final Function<String,ClassPath> classpaths;
        private volatile Object[] mainClassServices;
        private Supplier<? extends JavaPlatform> jpp;
        private BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
        private BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
        private Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;

        private Builder(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final SourceRoots sourceRoots,
                @NonNull final SourceRoots testSourceRoots,
                @NonNull final Function<String,ClassPath> projectClassPaths) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("updateHelper", updateHelper); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
            Parameters.notNull("testSourceRoots", testSourceRoots); //NOI18N
            Parameters.notNull("projectClassPaths", projectClassPaths); //NOI18N
            this.project = project;
            this.updateHelper = updateHelper;
            this.evaluator = evaluator;
            this.sourceRoots = sourceRoots;
            this.testRoots = testSourceRoots;
            this.actions = new ArrayList<>();
            this.jpp = createJavaPlatformProvider(ProjectProperties.PLATFORM_ACTIVE);
            this.mfs = ActionProviderSupport.ModifiedFilesSupport.newInstance(project, updateHelper, evaluator);
            this.classpaths = projectClassPaths;
            final Function<Boolean,String> pmcp = (validate) -> ActionProviderSupport.getProjectMainClass(project, evaluator, sourceRoots, classpaths, validate);
            final Supplier<Boolean> mcc = () -> ActionProviderSupport.showCustomizer(project, updateHelper, evaluator, sourceRoots, classpaths);
            this.mainClassServices = new Object[] {pmcp, mcc};
        }

        @NonNull
        public Builder addAction(@NonNull final Action action) {
            Parameters.notNull("action", action);   //NOI18N
            actions.add(action);
            return this;
        }

        @NonNull
        public Action createProjectOperation(String command) {
            final Consumer<? super Context> performer;
            switch (command) {
                case COMMAND_DELETE:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultDeleteOperation(ctx.getProject());
                    break;
                case COMMAND_MOVE:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultMoveOperation(ctx.getProject());
                    break;
                case COMMAND_COPY:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultCopyOperation(ctx.getProject());
                    break;
                case COMMAND_RENAME:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultRenameOperation(ctx.getProject(), null);
                    break;
                default:
                    throw new IllegalArgumentException(command);
            }
            return new SimpleAction(command, performer);
        }

        @NonNull
        public ScriptAction createScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                @NonNull final String... targets) {
            Parameters.notNull("targets", targets);     //NOI18N
            return createScriptAction(command, javaModelSensitive, scanSensitive, () -> targets, null);
        }

        @NonNull
        public ScriptAction createScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                @NonNull final Supplier<? extends String[]> targets) {
            return createScriptAction(command, javaModelSensitive, scanSensitive, targets, null);
        }

        @NonNull
        public ScriptAction createScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                @NonNull final Supplier<? extends String[]> targets,
                @NullAllowed final BiFunction<FileObject, Context, String[]> customFileTarget) {
            Parameters.notNull("command", command);         //NOI18N
            Parameters.notNull("targets", targets);         //NOI18N
            switch (command) {
                case ActionProvider.COMMAND_CLEAN:
                    return createNonCosAction(command, false, javaModelSensitive, scanSensitive, targets, Collections.emptyMap());
                case ActionProvider.COMMAND_REBUILD:
                    return createNonCosAction(command, true, javaModelSensitive, scanSensitive, targets, Collections.emptyMap());
                case ActionProvider.COMMAND_BUILD:
                    return createBuildAction(javaModelSensitive, scanSensitive, targets, mfs);
                case ActionProvider.COMMAND_RUN:
                case ActionProvider.COMMAND_DEBUG:
                case ActionProvider.COMMAND_DEBUG_STEP_INTO:
                case ActionProvider.COMMAND_PROFILE:
                    return createRunAction(command, javaModelSensitive, scanSensitive, targets, mfs, mainClassServices);
                case ActionProvider.COMMAND_TEST:
                    return createNonCosAction(command, true, javaModelSensitive, scanSensitive, targets, Collections.singletonMap("ignore.failing.tests", "true"));  //NOI18N);
                case ActionProvider.COMMAND_COMPILE_SINGLE:
                    return createCompileSingleAction(javaModelSensitive, scanSensitive, sourceRoots, testRoots, targets);
                case ActionProvider.COMMAND_RUN_SINGLE:
                case ActionProvider.COMMAND_DEBUG_SINGLE:
                case ActionProvider.COMMAND_PROFILE_SINGLE:
                    return createRunSingleAction(
                            command, javaModelSensitive,scanSensitive,
                            sourceRoots, testRoots, targets, customFileTarget);
                case ActionProvider.COMMAND_TEST_SINGLE:
                    return createTestSingleAction(javaModelSensitive, scanSensitive, sourceRoots, testRoots, targets);
                case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
                case ActionProvider.COMMAND_PROFILE_TEST_SINGLE:
                    return createDebugTestSingleAction(command, javaModelSensitive, scanSensitive, sourceRoots, testRoots, targets);
                default:
                    throw new UnsupportedOperationException(String.format("Unsupported command: %s", command)); //NOI18N
            }
        }

        @NonNull
        public Builder setActivePlatformProperty(@NonNull final String activePlatformProperty) {
            Parameters.notNull("activePlatformProperty", activePlatformProperty);   //NOI18N
            this.jpp = createJavaPlatformProvider(activePlatformProperty);
            return this;
        }

        @NonNull
        public Builder setActivePlatformProvider(@NonNull final Supplier<? extends JavaPlatform> javaPlatformProvider) {
            Parameters.notNull("javaPlatformProvider", javaPlatformProvider);   //NOI18N
            this.jpp = javaPlatformProvider;
            return this;
        }

        @NonNull
        public Builder setCompileOnSaveOperationsProvider(@NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
            Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
            this.cosOpsProvider = cosOpsProvider;
            return this;
        }

        @NonNull
        public Builder setProjectMainClassProvider(@NonNull final Function<Boolean,String> mainClassProvider) {
            Parameters.notNull("mainClassProvider", mainClassProvider);
            mainClassServices[0] = mainClassProvider;
            mainClassServices = mainClassServices;
            return this;
        }

        @NonNull
        public Builder setProjectMainClassSelector(@NonNull final Supplier<Boolean> selectMainClassAction) {
            Parameters.notNull("selectMainClassAction", selectMainClassAction);
            mainClassServices[1] = selectMainClassAction;
            mainClassServices = mainClassServices;
            return this;
        }

        @NonNull
        public Builder setAdditionalPropertiesProvider(@NonNull final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider) {
            Parameters.notNull("additionalPropertiesProvider", additionalPropertiesProvider);   //NOI18N
            this.additionalPropertiesProvider = additionalPropertiesProvider;
            return this;
        }

        @NonNull
        public Builder setConcealedPropertiesProvider(@NonNull final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider) {
            Parameters.notNull("concealedPropertiesProvider", concealedPropertiesProvider);   //NOI18N
            this.concealedPropertiesProvider = concealedPropertiesProvider;
            return this;
        }

        @NonNull
        public JavaActionProvider build() {
            final JavaActionProvider ap = new JavaActionProvider(
                    project,
                    updateHelper,
                    evaluator,
                    classpaths,
                    actions,
                    jpp,
                    additionalPropertiesProvider,
                    concealedPropertiesProvider,
                    cosOpsProvider,
                    mfs);
            mfs.start();
            return ap;
        }

        @NonNull
        private Supplier<? extends JavaPlatform> createJavaPlatformProvider(@NonNull final String activePlatformProperty) {
            return () -> {
                return ActionProviderSupport.getActivePlatform(project, evaluator, activePlatformProperty);
            };
        }

        @NonNull
        public static Builder newInstance(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final SourceRoots sourceRoots,
                @NonNull final SourceRoots testSourceRoots,
                @NonNull final Function<String,ClassPath> projectClassPaths) {
            return new Builder(project, updateHelper, evaluator, sourceRoots, testSourceRoots, projectClassPaths);
        }
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.keySet().toArray(new String[supportedActions.size()]);
    }

    @Override
    public boolean isActionEnabled(
            @NonNull final String command,
            @NonNull final Lookup context) throws IllegalArgumentException {
        return Optional.ofNullable(supportedActions.get(command))
                .map((act) -> act.isEnabled(new Context(
                        prj, updateHelper, eval, classpaths,
                        command, context, userPropertiesPolicy,
                        jpp, additionalPropertiesProvider, concealedPropertiesProvider, cosOpsProvider,
                        listeners)))
                .orElse(Boolean.FALSE);
    }

    @Override
    public void invokeAction(
            @NonNull final String command,
            @NonNull final Lookup context) throws IllegalArgumentException {
        assert SwingUtilities.isEventDispatchThread();
        Optional.ofNullable(supportedActions.get(command))
                .ifPresent((act) -> {
                    final Context ctx = new Context(
                            prj, updateHelper, eval, classpaths,
                            command, context, userPropertiesPolicy,
                            jpp, additionalPropertiesProvider, concealedPropertiesProvider, cosOpsProvider,
                            listeners);
                    try {
                        act.invoke(ctx);
                    } finally {
                        userPropertiesPolicy = ctx.getUserPropertiesPolicy();
                    }
                });
    }

    public void addAntTargetInvocationListener(@NonNull final AntTargetInvocationListener listener) {
        Parameters.notNull("listener", listener);
        listeners.add(listener);
    }

    public void removeAntTargetInvocationListener(@NonNull final AntTargetInvocationListener listener) {
        Parameters.notNull("listener", listener);
        listeners.remove(listener);
    }

    @CheckForNull
    Action getAction(@NonNull final String command) {
        return supportedActions.get(command);
    }

    @NonNull
    @Messages({
        "LBL_ProjectBuiltAutomatically=<html><b>This project's source files are compiled automatically when you save them.</b><br>You do not need to build the project to run or debug the project in the IDE.<br><br>If you need to build or rebuild the project's JAR file, use Clean and Build.<br>To disable the automatic compiling feature and activate the Build command,<br>go to Project Properties and disable Compile on Save.",
        "BTN_ProjectProperties=Project Properties...",
        "BTN_CleanAndBuild=Clean and Build",
        "BTN_OK=OK",
        "# {0} - project name", "TITLE_BuildProjectWarning=Build Project ({0})"
    })
    private static ScriptAction createBuildAction (
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final Supplier<? extends String[]> targets,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs) {
        return new BaseScriptAction(COMMAND_BUILD, true, javaModelSensitive, scanSensitive, targets) {

            @Override
            public String[] getTargetNames(Context context) {
                String[] targets = super.getTargetNames(context);
                if (targets != null) {
                    final String includes = mfs.prepareDirtyList(true);
                    if (includes != null) {
                        context.setProperty(ProjectProperties.INCLUDES, includes);
                    }
                }
                return targets;
            }

            @Override
            public ScriptAction.Result performCompileOnSave(Context context, String[] targetNames) {
                if (!ActionProviderSupport.allowAntBuild(context.getPropertyEvaluator(), context.getUpdateHelper())) {
                    showBuildActionWarning(context);
                    return JavaActionProvider.ScriptAction.Result.abort();
                }
                return JavaActionProvider.ScriptAction.Result.follow();
            }

            @org.netbeans.api.annotations.common.SuppressWarnings("ES_COMPARING_STRINGS_WITH_EQ")
            private void showBuildActionWarning(Context context) {
                String projectProperties = Bundle.BTN_ProjectProperties();
                String cleanAndBuild = Bundle.BTN_CleanAndBuild();
                String ok = Bundle.BTN_OK();
                DialogDescriptor dd = new DialogDescriptor(Bundle.LBL_ProjectBuiltAutomatically(),
                       Bundle.TITLE_BuildProjectWarning(ProjectUtils.getInformation(context.getProject()).getDisplayName()),
                       true,
                       new Object[] {projectProperties, cleanAndBuild, ok},
                       ok,
                       DialogDescriptor.DEFAULT_ALIGN,
                       null,
                       null);

                dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                Object result = DialogDisplayer.getDefault().notify(dd);
                if (result == projectProperties) {
                    CustomizerProvider2 p = context.getProject().getLookup().lookup(CustomizerProvider2.class);
                    p.showCustomizer("Build", null); //NOI18N
                    return ;
                }
                if (result == cleanAndBuild) {
                    final ActionProvider ap = context.getProject().getLookup().lookup(ActionProvider.class);
                    if (ap != null) {
                        ap.invokeAction(COMMAND_REBUILD, context.getActiveLookup());
                    }
                }
            }
        };
    }

    @NonNull
    private static ScriptAction createCompileSingleAction(
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoots,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseScriptAction(COMMAND_COMPILE_SINGLE, true, javaModelSensitive, scanSensitive, targets) {

            @Override
            public boolean isEnabled(Context context) {
                return ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), sourceRoots.getRoots()) != null
                    || ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), testRoots.getRoots()) != null;
            }

            @Override
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    final Lookup lkp = context.getActiveLookup();
                    FileObject[] srcRoots = sourceRoots.getRoots();
                    FileObject[] files = ActionProviderSupport.findSourcesAndPackages(lkp, srcRoots);
                    final boolean recursive = lkp.lookup(NonRecursiveFolder.class) == null;
                    if (files != null) {
                        context.setProperty(
                                "javac.includes",    // NOI18N
                                ActionUtils.antIncludesList(files, ActionProviderSupport.getRoot(srcRoots,files[0]), recursive));
                        final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                            context.getProject(),
                            context.getPropertyEvaluator())
                            .get(context.getCommand());
                        if (cfgTargets != null) {
                            res = cfgTargets;
                        }
                    } else {
                        srcRoots = testRoots.getRoots();
                        files = ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), srcRoots);
                        if (files != null) {
                            context.setProperty(
                                "javac.includes",    // NOI18N
                                ActionUtils.antIncludesList(files, ActionProviderSupport.getRoot(srcRoots,files[0]), recursive));
                            res = new String[] {"compile-test-single"}; // NOI18N
                        } else {
                            res = null;
                        }
                    }
                }
                return res;
            }
        };
    }

    @NonNull
    @Messages({
        "# {0} - file name", "CTL_FileMultipleMain=The file {0} has more main classes.",
        "CTL_FileMainClass_Title=Run File"
    })
    private static ScriptAction createRunSingleAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final SourceRoots sr,
            final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets,
            @NullAllowed final BiFunction<FileObject,Context,String[]> customFileExecutor) {
        return new BaseRunSingleAction(command, true, javaModelSensitive, scanSensitive, sr, tr, targets) {

            @Override
            public boolean isEnabled(Context context) {
                FileObject fos[] = ActionProviderSupport.findSources(getSourceRoots().getRoots(), context.getActiveLookup());
                if (fos != null && fos.length == 1) {
                    return true;
                }
                fos = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), false);
                if (fos != null && fos.length == 1) {
                    return true;
                }
                logNoFiles(getSourceRoots(), getTestRoots(), context);
                return false;
            }

            @Override
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    FileObject[] files = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), false);
                    FileObject[] rootz = getTestRoots().getRoots();
                    boolean isTest = true;
                    if (files == null) {
                        isTest = false;
                        files = ActionProviderSupport.findSources(getSourceRoots().getRoots(), context.getActiveLookup());
                        rootz = getSourceRoots().getRoots();
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Is test: {0} Files: {1} Roots: {2}",    //NOI18N
                                new Object[] {
                                    isTest,
                                    asPath(files),
                                    asPath(rootz)
                        });
                    }
                    if (files == null) {
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
                    String[] pathFqn = ActionProviderSupport.pathAndFqn(file, rootz);
                    if (pathFqn == null) {
                        return null;
                    }
                    context.setProperty("javac.includes", pathFqn[0]); // NOI18N
                    String clazz = pathFqn[1];
                    LOG.log(Level.FINE, "Class to run: {0}", clazz);    //NOI18N
                    final boolean hasMainClassFromTest = MainClassChooser.unitTestingSupport_hasMainMethodResult == null ?
                            false :
                            MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue();
                    if (context.doJavaChecks()) {
                        final Collection<ElementHandle<TypeElement>> mainClasses = CommonProjectUtils.getMainMethods (file);
                        LOG.log(Level.FINE, "Main classes: {0} ", mainClasses);
                        if (!hasMainClassFromTest && mainClasses.isEmpty()) {
                            final String[] customTargets = customFileExecutor == null ?
                                    null :
                                    customFileExecutor.apply(file, context);
                            if (customTargets != null) {
                                res = customTargets;
                            } else {
                                if (isTest) {
                                    res = ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                                } else {
                                    final ActionProviderSupport.JavaMainAction javaMainAction = ActionProviderSupport.getJavaMainAction(context.getPropertyEvaluator());
                                    if (javaMainAction == null) {
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.LBL_No_Main_Class_Found(clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                                        DialogDisplayer.getDefault().notify(nd);
                                        return null;
                                    } else if (javaMainAction == ActionProviderSupport.JavaMainAction.RUN) {
                                        res = updateTargets(res, clazz, false, context);
                                    } else if (javaMainAction == ActionProviderSupport.JavaMainAction.TEST) {
                                        res = ActionProviderSupport.setupTestSingle(context, files, getSourceRoots().getRoots());
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
                            res = updateTargets(res, clazz, isTest, context);
                        }
                    } else {
                        res = updateTargets(res, clazz, isTest, context);
                    }
                }
                return res;
            }

            private String[] updateTargets(
                    @NonNull String[] targets,
                    @NonNull final String clazz,
                    final boolean isTest,
                    @NonNull final Context context) {
                //The Java model is not ready, we cannot determine if the file is applet or main class or unit test
                //Acts like everything is main class, maybe for test folder junit is better default?
                switch (context.getCommand()) {
                    case COMMAND_RUN_SINGLE:
                        context.setProperty("run.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"run-test-with-main"};
                        }
                        break;
                    case COMMAND_DEBUG_SINGLE:
                        context.setProperty("debug.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"debug-test-with-main"};
                        }
                        break;
                    default:
                        context.setProperty("run.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"profile-test-with-main"};
                        }
                        break;
                }
                final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                    context.getProject(),
                    context.getPropertyEvaluator())
                    .get(command);
                if (cfgTargets != null) {
                    targets = cfgTargets;
                }
                return targets;
            }

            private String showMainClassWarning (final FileObject file, final Collection<ElementHandle<TypeElement>> mainClasses) {
                assert mainClasses != null;
                String mainClass = null;
                final JButton okButton = new JButton(Bundle.LBL_MainClassWarning_ChooseMainClass_OK());
                okButton.getAccessibleContext().setAccessibleDescription(Bundle.AD_MainClassWarning_ChooseMainClass_OK());
                final MainClassWarning panel = new MainClassWarning(Bundle.CTL_FileMultipleMain(file.getNameExt()), mainClasses);
                Object[] options = new Object[] {
                    okButton,
                    DialogDescriptor.CANCEL_OPTION
                };
                panel.addChangeListener ((e) -> {
                   if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                       // click button and the finish dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
                });
                DialogDescriptor desc = new DialogDescriptor (panel,
                    Bundle.CTL_FileMainClass_Title(),
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
        };
    }

    @NonNull
    private static ScriptAction createTestSingleAction(
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final SourceRoots sr,
            @NonNull final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseRunSingleAction(COMMAND_TEST_SINGLE, true, javaModelSensitive, scanSensitive, sr, tr, targets) {

            @Override
            public boolean isEnabled(Context context) {
                return ActionProviderSupport.findTestSourcesForFiles(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup()) != null;
            }

            @Override
            public String[] getTargetNames(Context context) {
                context.setProperty("ignore.failing.tests", "true");  //NOI18N
                final FileObject[] files = ActionProviderSupport.findTestSourcesForFiles(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup());
                if (files == null) {
                    return null;
                }
                if(files.length == 1 && files[0].isData()) {
                    //one file or a package containing one file selected
                    return ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                } else {
                    //multiple files or package(s) selected
                    if (files != null) {
                        FileObject root = ActionProviderSupport.getRoot(getTestRoots().getRoots(), files[0]);
                        // the replace part is so that we can test everything under a package recusively
                        context.setProperty("includes", ActionUtils.antIncludesList(files, root).replace("**", "**/*Test.java")); // NOI18N
                    }
                    return new String[]{"test"}; // NOI18N
                }
            }
        };
    }

    @NonNull
    private static ScriptAction createDebugTestSingleAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final SourceRoots sr,
            @NonNull final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseRunSingleAction(command, true, javaModelSensitive, scanSensitive, sr, tr, targets) {

            @Override
            public boolean isEnabled(Context context) {
                FileObject[] fos = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), true);
                return fos != null && fos.length == 1;
            }

            @Override
            public String[] getTargetNames(Context context) {
                final FileObject[] files = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), true);
                if (files != null) {
                    return ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                }
                return null;
            }
        };
    }

    @NonNull
    private static ScriptAction createRunAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final Supplier<? extends String[]> targets,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs,
            @NonNull final Object[] mainClassServices) {
        return new BaseScriptAction(command, true, javaModelSensitive, scanSensitive, targets) {
            @Override
            public String[] getTargetNames(Context context) {
                String[] targets = super.getTargetNames(context);
                if (targets != null) {
                    // check project's main class
                    // Check whether main class is defined in this config. Note that we use the evaluator,
                    // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
                    // to define a main class - in this case an active config need not override it.

                    // If a specific config was selected, just skip this check for now.
                    // XXX would ideally check that that config in fact had a main class.
                    // But then evaluator.getProperty(MAIN_CLASS) would be inaccurate.
                    // Solvable but punt on it for now.
                    final boolean hasCfg = context.getActiveLookup().lookup(ProjectConfiguration.class) != null;
                    final boolean verifyMain = context.doJavaChecks() && !hasCfg && ActionProviderSupport.getJavaMainAction(context.getPropertyEvaluator()) == null;
                    String mainClass = ((Function<Boolean,String>)mainClassServices[0]).apply(verifyMain);
                    if (mainClass == null) {
                        do {
                            // show warning, if cancel then return
                            if (!((Supplier<Boolean>)mainClassServices[1]).get()) {
                                return null;
                            }
                            // No longer use the evaluator: have not called putProperties yet so it would not work.
                            mainClass = context.getPropertyEvaluator().getProperty(ProjectProperties.MAIN_CLASS);
                            mainClass = ((Function<Boolean,String>)mainClassServices[0]).apply(verifyMain);
                        } while (mainClass == null);
                    }
                    if (mainClass != null) {
                        switch (command) {
                            case COMMAND_PROFILE:
                                context.setProperty("run.class", mainClass); // NOI18N
                                break;
                            case COMMAND_DEBUG:
                            case COMMAND_DEBUG_STEP_INTO:
                                context.setProperty("debug.class", mainClass); // NOI18N
                                break;
                        }
                    }
                    final String includes = mfs.prepareDirtyList(false);
                    if (includes != null) {
                        context.setProperty(ProjectProperties.INCLUDES, includes);
                    }
                    final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                            context.getProject(),
                            context.getPropertyEvaluator())
                            .get(command);
                    if (cfgTargets != null) {
                        targets = cfgTargets;
                    }
                }
                return targets;
            }

            @Override
            public ScriptAction.Result performCompileOnSave(Context context, String[] targetNames) {
                final Map<String,Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
                ActionProviderSupport.prepareSystemProperties(
                        context.getPropertyEvaluator(),
                        execProperties,
                        context.getCommand(),
                        context.getActiveLookup(),
                        false);
                AtomicReference<ExecutorTask> _task = new AtomicReference<>();
                ActionProviderSupport.bypassAntBuildScript(
                        context,
                        execProperties,
                        EMPTY,  //not needed
                        EMPTY,  //not needed
                        _task,
                        getCoSInterceptor());
                final ExecutorTask t = _task.get();
                return t == null ?
                        JavaActionProvider.ScriptAction.Result.abort() :
                        JavaActionProvider.ScriptAction.Result.success(t);
            }
        };
    }

    @NonNull
    private static ScriptAction createNonCosAction (
            final String command,
            final boolean platformSensitive,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            @NonNull final Supplier<? extends String[]> targets,
            @NullAllowed final Map<String,String> props) {
        return new BaseScriptAction(command, platformSensitive, javaModelSensitive, scanSensitive, targets);
    }

    private static void logNoFiles(
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoots,
            @NonNull final Context context) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Source Roots: {0} Test Roots: {1} Lookup Content: {2}",    //NOI18N
                    new Object[]{
                        asPath(sourceRoots.getRoots()),
                        asPath(testRoots.getRoots()),
                        asPath(context.getActiveLookup())
                    });
        }
    }

    @NonNull
    private static CharSequence asPath(final @NonNull Lookup context) {
        final Set<FileObject> fos = new HashSet<>();
        context.lookupAll(DataObject.class).stream()
                .map(DataObject::getPrimaryFile)
                .forEach(fos::add);
        context.lookupAll(FileObject.class).stream()
                .forEach(fos::add);
        return asPath(fos.toArray(new FileObject[fos.size()]));
    }

    @NonNull
    private static CharSequence asPath(@NonNull final FileObject[] fos) {
        if (fos == null) {
            return null;
        }
        return Arrays.stream(fos)
                .map(FileUtil::getFileDisplayName)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static final class SimpleAction implements Action {
        private final String name;
        private final Consumer<? super Context> performer;

        SimpleAction(
                @NonNull final String name,
                final Consumer<? super Context> performer) {
            Parameters.notNull("name", name);           //NOI18N
            Parameters.notNull("performer", performer); //NOI18N
            this.name = name;
            this.performer = performer;
        }

        @Override
        @NonNull
        public final String getCommand() {
            return this.name;
        }

        @Override
        public boolean isEnabled(@NonNull final Context context) {
            return true;
        }

        @Override
        public void invoke(@NonNull final Context context) {
            performer.accept(context);
        }
    }

    private static class BaseScriptAction extends ScriptAction {
        private final Supplier<? extends String[]> targetNames;
        private final Map<String,String> initialProps;

        BaseScriptAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                Supplier<? extends String[]> targetNames) {
            this(command, ps, jms, sc, targetNames, Collections.emptyMap());
        }

        BaseScriptAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                @NonNull Supplier<? extends String[]> targetNames,
                @NonNull Map<String,String> initialProps) {
            super(command, null, ps, jms, sc);
            this.targetNames = targetNames;
            this.initialProps = initialProps;
        }

        @Override
        public boolean isEnabled(JavaActionProvider.Context context) {
            return true;
        }

        @Override
        public String[] getTargetNames(JavaActionProvider.Context context) {
            for (Map.Entry<String,String> e : initialProps.entrySet()) {
                context.setProperty(e.getKey(), e.getValue());
            }
            return targetNames.get();
        }
    }

    private static class BaseRunSingleAction extends BaseScriptAction {
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;

        BaseRunSingleAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                final SourceRoots sourceRoots,
                final SourceRoots testRoots,
                final Supplier<? extends String[]> targetNames) {
            super(command, ps, jms, sc, targetNames);
            this.sourceRoots = sourceRoots;
            this.testRoots = testRoots;
        }

        @Override
        public Result performCompileOnSave(Context context, String[] targetNames) {
            if (Arrays.equals(targetNames, new String[]{"test"})) {
                return Result.follow();
            }
            final String command = context.getCommand();
            final Map<String,Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
            if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command)) {
                ActionProviderSupport.prepareSystemProperties(
                        context.getPropertyEvaluator(),
                        execProperties,
                        command,
                        context.getActiveLookup(),
                        false);
                switch (command) {
                    case COMMAND_RUN_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("run.class"));    //NOI18N
                        break;
                    case COMMAND_DEBUG_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("debug.class"));  //NOI18N
                        break;
                    case COMMAND_PROFILE_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("profile.class"));
                        break;
                    default:
                        throw new IllegalStateException(command);
                }
                final AtomicReference<ExecutorTask> _task = new AtomicReference<>();
                ActionProviderSupport.bypassAntBuildScript(
                        context,
                        execProperties,
                        sourceRoots.getRoots(),
                        testRoots.getRoots(),
                        _task,
                        getCoSInterceptor());
                final ExecutorTask t = _task.get();
                return t == null ?
                        JavaActionProvider.ScriptAction.Result.abort() :
                        JavaActionProvider.ScriptAction.Result.success(t);
            } else if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command) || COMMAND_PROFILE_TEST_SINGLE.equals(command)) {
                final FileObject[] files = ActionProviderSupport.findTestSources(sourceRoots.getRoots(), testRoots.getRoots(), context.getActiveLookup(), true);
                try {
                    ActionProviderSupport.prepareSystemProperties(context.getPropertyEvaluator(), execProperties, command, context.getActiveLookup(), true);
                    execProperties.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
                    String buildDir = context.getPropertyEvaluator().getProperty(ProjectProperties.BUILD_DIR);
                    if (buildDir != null) { // #211543
                        context.setProperty("tmp.dir", context.getUpdateHelper().getAntProjectHelper().resolvePath(buildDir));  //NOI18N
                    }
                    boolean vote = true;
                    final BiFunction<Context, Map<String, Object>, Boolean> cosi = getCoSInterceptor();
                    if (cosi != null) {
                        vote = cosi.apply(context, execProperties);
                    }
                    if (vote) {
                        return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(
                                command.equals(COMMAND_TEST_SINGLE) ? JavaRunner.QUICK_TEST : (COMMAND_DEBUG_TEST_SINGLE.equals(command) ? JavaRunner.QUICK_TEST_DEBUG :JavaRunner.QUICK_TEST_PROFILE),
                                           execProperties));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            } else {
                assert false : "Unhandled command:" + command;
            }
            return Result.follow();
        }

        @NonNull
        final SourceRoots getSourceRoots() {
            return sourceRoots;
        }

        @NonNull
        final SourceRoots getTestRoots() {
            return testRoots;
        }
    }
}
