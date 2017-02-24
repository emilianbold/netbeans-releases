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

import java.util.Set;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.CompileOnSaveOperation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleActionProviderBuilder {

        private final JavaActionProvider.Builder builder;

        private MultiModuleActionProviderBuilder(
                @NonNull final JavaActionProvider.Builder builder) {
            Parameters.notNull("builder", builder); //NOI18N
            this.builder = builder;
        }

    @NonNull
    private MultiModuleActionProviderBuilder addProjectSensitiveActions() {
        builder.addAction(new DummyAction(ActionProvider.COMMAND_CLEAN, false, false, "clean"));
        builder.addAction(new DummyAction(ActionProvider.COMMAND_BUILD, false, false,  "build"));
        builder.addAction(new DummyAction(ActionProvider.COMMAND_REBUILD, false, false,  "clean", "build"));
        builder.addAction(new DummyAction(ActionProvider.COMMAND_RUN, false, true, "run"));
        builder.addAction(new DummyAction(ActionProvider.COMMAND_DEBUG, false, true, "debug"));
        return this;
    }

    @NonNull
    private MultiModuleActionProviderBuilder addProjectOperationsActions() {
        builder.addProjectOperations(
                ActionProvider.COMMAND_DELETE,
                ActionProvider.COMMAND_MOVE,
                ActionProvider.COMMAND_COPY,
                ActionProvider.COMMAND_RENAME);
        return this;
    }

    @NonNull
    public MultiModuleActionProviderBuilder setCompileOnSaveOperationsProvider(@NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
        builder.setCompileOnSaveOperationsProvider(cosOpsProvider);
        return this;
    }

    @NonNull
    public JavaActionProvider build() {
        addProjectOperationsActions();
        addProjectSensitiveActions();
        return builder.build();
    }

    @NonNull
    public static MultiModuleActionProviderBuilder newInstance(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator) {
        return new MultiModuleActionProviderBuilder(JavaActionProvider.Builder.newInstance(project, updateHelper, evaluator));
    }

    //Temporary - should be replaced by actions shared with BaseActionProvider
    private static class DummyAction extends JavaActionProvider.ScriptAction {
        private final String[] targetNames;

        DummyAction(
                @NonNull final String command,
                final boolean jms,
                @NonNull final boolean sc, String... targetNames) {
            super(command, null, true, true);
            this.targetNames = targetNames;
        }

        @Override
        public boolean isEnabled(JavaActionProvider.Context context) {
            return true;
        }

        @Override
        protected String[] getTargerNames(JavaActionProvider.Context context) {
            return targetNames;
        }
    }
}
