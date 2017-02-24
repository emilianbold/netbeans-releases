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

import java.util.ArrayList;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.execution.ExecutorTask;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class JavaActionProvider implements ActionProvider {
    private final Project prj;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator eval;
    private final Map<String,Action> supportedActions;
    private final List<AntTargetInvocationListener> listeners;
    private final Supplier<? extends JavaPlatform> jpp;
    private final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;
    private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;

    private JavaActionProvider(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Collection<? extends Action> actions,
            @NonNull final String javaPlatformProperty,
            @NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("updateHelper", updateHelper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("actions", actions); //NOI18N
        Parameters.notNull("javaPlatformProperty", javaPlatformProperty);   //NOI18N
        Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
        this.prj = project;
        this.updateHelper = updateHelper;
        this.eval = evaluator;
        final Map<String,Action> abn = new HashMap<>();
        for (Action action : actions) {
            abn.put(action.getCommand(), action);
        }
        this.supportedActions = Collections.unmodifiableMap(abn);
        this.listeners = new CopyOnWriteArrayList<>();
        this.jpp = () -> {
            return CommonProjectUtils.getActivePlatform(eval.getProperty(javaPlatformProperty));
        };
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
                final boolean javaModelSensitive,
                final boolean scanSensitive) {
            Parameters.notNull("command", command); //NOI18N
            this.command = command;
            this.displayName = dispalyName;
            this.actionFlags = EnumSet.noneOf(ActionProviderSupport.ActionFlag.class);
            if (javaModelSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE);
            }
            if (scanSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.SCAN_SENITIVE);
            }
        }

        @NonNull
        protected abstract String[] getTargerNames(@NonNull final Context context);

        @NonNull
        protected Result performCompileOnSave(@NonNull final Context context, @NonNull final String[] targetNames) {
            return Result.follow();
        }

        @Override
        public final String getCommand() {
            return command;
        }

        @Override
        public final void invoke(@NonNull final Context context) {
            ActionProviderSupport.invokeTarget(
                    this::getTargerNames,
                    this::performCompileOnSave,
                    context,
                    actionFlags,
                    getDisplayName());
        }

        @NonNull
        String getDisplayName() {
            String res = displayName;
            if (res == null) {
                res = ActionProviderSupport.getCommandDisplayName(command);
            }
            return res;
        }
    }

    public static final class Builder {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator evaluator;
        private final List<Action> actions;
        private String javaPlatformProperty = ProjectProperties.PLATFORM_ACTIVE;
        private Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;

        private Builder(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("updateHelper", updateHelper); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            this.project = project;
            this.updateHelper = updateHelper;
            this.evaluator = evaluator;
            this.actions = new ArrayList<>();
        }

        @NonNull
        public Builder addAction(@NonNull final Action action) {
            Parameters.notNull("action", action);   //NOI18N
            actions.add(action);
            return this;
        }

        @NonNull
        public Builder addProjectOperations(String... requiredOperations) {
            for (String requiredOperation : requiredOperations) {
                switch (requiredOperation) {
                    case COMMAND_DELETE:
                        addAction(new SimpleAction(COMMAND_DELETE, (ctx) -> DefaultProjectOperations.performDefaultDeleteOperation(ctx.getProject())));
                        break;
                    case COMMAND_MOVE:
                        addAction(new SimpleAction(COMMAND_MOVE, (ctx) -> DefaultProjectOperations.performDefaultMoveOperation(ctx.getProject())));
                        break;
                    case COMMAND_COPY:
                        addAction(new SimpleAction(COMMAND_COPY, (ctx) -> DefaultProjectOperations.performDefaultCopyOperation(ctx.getProject())));
                        break;
                    case COMMAND_RENAME:
                        addAction(new SimpleAction(COMMAND_RENAME, (ctx) -> DefaultProjectOperations.performDefaultRenameOperation(ctx.getProject(), null)));
                        break;
                    default:
                        throw new IllegalArgumentException(requiredOperation);
                }
            }
            return this;
        }

        @NonNull
        public Builder setPlatformProperty(@NonNull final String activePlatformProperty) {
            Parameters.notNull("activePlatformProperty", activePlatformProperty);   //NOI18N
            this.javaPlatformProperty = activePlatformProperty;
            return this;
        }

        @NonNull
        public Builder setCompileOnSaveOperationsProvider(@NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
            Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
            this.cosOpsProvider = cosOpsProvider;
            return this;
        }

        @NonNull
        public JavaActionProvider build() {
            return new JavaActionProvider(
                    project,
                    updateHelper,
                    evaluator,
                    actions,
                    javaPlatformProperty,
                    cosOpsProvider);
        }

        @NonNull
        public static Builder newInstance(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator) {
            return new Builder(project, updateHelper, evaluator);
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
                        prj, updateHelper, eval,
                        command, context, userPropertiesPolicy,
                        jpp, null, null, cosOpsProvider,
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
                            prj, updateHelper, eval,
                            command, context, userPropertiesPolicy,
                            jpp, null, null, cosOpsProvider,
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
}
