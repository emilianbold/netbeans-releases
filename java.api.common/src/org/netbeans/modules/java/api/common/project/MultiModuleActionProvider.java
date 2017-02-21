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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class MultiModuleActionProvider implements ActionProvider {

    private final Project prj;
    private final PropertyEvaluator eval;
    private final Map<String,Action> supportedActions;

    private MultiModuleActionProvider(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Collection<? extends Action> actions) {
        this.prj = project;
        this.eval = evaluator;
        final Map<String,Action> abn = new HashMap<>();
        for (Action action : actions) {
            abn.put(action.getName(), action);
        }
        this.supportedActions = Collections.unmodifiableMap(abn);
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
                .map((act) -> act.isEnabled(new Context(prj, eval, context)))
                .orElse(Boolean.FALSE);
    }

    @Override
    public void invokeAction(
            @NonNull final String command,
            @NonNull final Lookup context) throws IllegalArgumentException {
        assert SwingUtilities.isEventDispatchThread();
        Optional.ofNullable(supportedActions.get(command))
                .ifPresent((act) -> act.invoke(new Context(prj, eval, context)));
    }

    public static final class Context {
        private final Project project;
        private final PropertyEvaluator eval;
        private final Lookup lkp;

        private Context(
                @NonNull final Project project,
                @NonNull final PropertyEvaluator eval,
                @NonNull final Lookup lookup) {
            this.lkp = lookup;
            this.project = project;
            this.eval = eval;
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
        public PropertyEvaluator getPropertyEvaluator() {
            return eval;
        }
    }

    public static interface Action {
        String getName();
        boolean isEnabled(@NonNull final Context context);
        void invoke(@NonNull final Context context);
    }

    public static final class Builder {
        private final Project project;
        private final PropertyEvaluator evaluator;
        private final List<Action> actions;

        private Builder(
                @NonNull final Project project,
                @NonNull final PropertyEvaluator evaluator) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            this.project = project;
            this.evaluator = evaluator;
            this.actions = new ArrayList<>();
        }

        @NonNull
        public Builder addProjectOperationsActions() {
            addAction(new SimpleAction(COMMAND_DELETE, (ctx) -> DefaultProjectOperations.performDefaultDeleteOperation(ctx.getProject())));
            addAction(new SimpleAction(COMMAND_MOVE, (ctx) -> DefaultProjectOperations.performDefaultMoveOperation(ctx.getProject())));
            addAction(new SimpleAction(COMMAND_COPY, (ctx) -> DefaultProjectOperations.performDefaultCopyOperation(ctx.getProject())));
            addAction(new SimpleAction(COMMAND_RENAME, (ctx) -> DefaultProjectOperations.performDefaultRenameOperation(ctx.getProject(), null)));
            return this;
        }

        @NonNull
        public Builder addCleanAction(@NonNull final String cleanTarget) {
            return addAction(new ScriptAction(COMMAND_CLEAN, cleanTarget));
        }

        @NonNull
        public Builder addBuildAction(@NonNull final String buildTarget) {
            return addAction(new ScriptAction(COMMAND_BUILD, buildTarget));
        }

        @NonNull
        public Builder addAction(@NonNull final Action action) {
            Parameters.notNull("action", action);   //NOI18N
            actions.add(action);
            return this;
        }

        @NonNull
        public MultiModuleActionProvider build() {
            return new MultiModuleActionProvider(project, evaluator, actions);
        }

        @NonNull
        public static Builder newInstance(
                @NonNull final Project project,
                @NonNull final PropertyEvaluator evaluator) {
            return new Builder(project, evaluator);
        }
    }

    private static abstract class BaseAction implements Action {
        private final String name;

        BaseAction(@NonNull final String name) {
            Parameters.notNull("name", name);           //NOI18N
            this.name = name;
        }

        @Override
        @NonNull
        public final String getName() {
            return this.name;
        }
    }

    private static final class SimpleAction extends BaseAction {

        private final Consumer<? super Context> performer;

        SimpleAction(
                @NonNull final String name,
                final Consumer<? super Context> performer) {
            super(name);
            Parameters.notNull("performer", performer); //NOI18N
            this.performer = performer;
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

    private static final class ScriptAction extends BaseAction {

        private Predicate<Lookup> enabled;
        private Function<Lookup,Collection<? extends String>> targetProvider;

        ScriptAction(
                @NonNull final String name,
                @NonNull final String... targets) {
            this(name, (l)->true, (l)->Arrays.asList(targets));
        }

        ScriptAction(
                @NonNull final String name,
                @NonNull final Predicate<Lookup> enabled,
                @NonNull final Function<Lookup,Collection<? extends String>> targetProvider) {
            super(name);
            Parameters.notNull("enabled", enabled);     //NOI18N
            this.enabled = enabled;
            this.targetProvider = targetProvider;
        }

        @Override
        public boolean isEnabled(
                @NonNull final Context context) {
            return this.enabled.test(context.getActiveLookup());
        }

        @Override
        public void invoke(@NonNull final Context context) {
            final Collection<? extends String> targets = this.targetProvider.apply(context.getActiveLookup());
            if (!targets.isEmpty()) {
                final FileObject buildScript = getBuildScript(context);
                if (buildScript == null || !buildScript.isValid()) {
                    //The build.xml was deleted after the isActionEnabled was called
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.LBL_No_Build_XML_Found(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    try {
                        ActionUtils.runTarget(
                                buildScript,
                                targets.toArray(new String[targets.size()]),
                                new Properties(),
                                Collections.emptySet());
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }

        @CheckForNull
        private FileObject getBuildScript(@NonNull final Context context) {
            final String path =  CommonProjectUtils.getBuildXmlName(context.getPropertyEvaluator(), null);
            return context.getProject().getProjectDirectory().getFileObject(path);
        }
    }
}
