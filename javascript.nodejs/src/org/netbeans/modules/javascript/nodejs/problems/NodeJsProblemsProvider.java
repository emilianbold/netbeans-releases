/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.problems;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.ValidationResult;
import org.netbeans.modules.javascript.nodejs.util.ValidationUtils;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
public final class NodeJsProblemsProvider implements ProjectProblemsProvider {

    private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    final Project project;
    final PreferenceChangeListener optionsListener = new OptionsListener();
    final PreferenceChangeListener preferencesListener = new PreferencesListener();

    // @GuardedBy("this")
    private NodeJsSupport nodeJsSupport;


    public NodeJsProblemsProvider(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @NbBundle.Messages({
        "# {0} - message",
        "NodeJsProblemProvider.error=Node.js: {0}",
    })
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblem> collectProblems() {
                if (!getNodeJsSupport().getPreferences().isEnabled()) {
                    return Collections.emptyList();
                }
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                checkOptions(currentProblems);
                checkPreferences(currentProblems);
                checkNode(currentProblems);
                return currentProblems;
            }
        });
    }

    synchronized NodeJsSupport getNodeJsSupport() {
        if (nodeJsSupport == null) {
            nodeJsSupport = NodeJsSupport.forProject(project);
            nodeJsSupport.getPreferences().addPreferenceChangeListener(preferencesListener);
            NodeJsOptions options = NodeJsOptions.getInstance();
            options.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, optionsListener, options));
        }
        return nodeJsSupport;
    }

    void checkOptions(Collection<ProjectProblem> currentProblems) {
        ValidationResult validationResult = new NodeJsOptionsValidator()
                .validate()
                .getResult();
        if (validationResult.isFaultless()) {
            return;
        }
        String message = validationResult.getFirstErrorMessage();
        if (message == null) {
            message = validationResult.getFirstWarningMessage();
        }
        assert message != null : "Message should be found for invalid options";
        message = Bundle.NodeJsProblemProvider_error(message);
        ProjectProblem problem = ProjectProblem.createError(
                message,
                message,
                new OptionsProblemResolver());
        currentProblems.add(problem);
    }

    void checkPreferences(Collection<ProjectProblem> currentProblems) {
        ValidationResult validationResult = new NodeJsPreferencesValidator()
                .validate(project)
                .getResult();
        if (validationResult.isFaultless()) {
            return;
        }
        String message = validationResult.getFirstErrorMessage();
        if (message == null) {
            message = validationResult.getFirstWarningMessage();
        }
        assert message != null : "Message should be found for invalid preferences: " + project.getProjectDirectory().getNameExt();
        message = Bundle.NodeJsProblemProvider_error(message);
        ProjectProblem problem = ProjectProblem.createError(
                message,
                message,
                new CustomizerProblemResolver(project, validationResult));
        currentProblems.add(problem);
    }

    @NbBundle.Messages({
        "NodeJsProblemProvider.node.version=Unknown version",
        "# {0} - version",
        "NodeJsProblemProvider.node.sources=Missing sources for version {0}",
    })
    void checkNode(Collection<ProjectProblem> currentProblems) {
        final NodeExecutable node = NodeExecutable.forProject(project, false);
        if (node == null) {
            // already handled
            return;
        }
        if (EventQueue.isDispatchThread()
                && !node.versionDetected()) {
            // avoid ui flickering
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    node.getVersion();
                    fireProblemsChanged();
                }
            });
            return;
        }
        Version version = node.getVersion();
        if (version == null) {
            String message = Bundle.NodeJsProblemProvider_error(Bundle.NodeJsProblemProvider_node_version());
            ProjectProblem problem = ProjectProblem.createError(
                    message,
                    message,
                    getNodeResolver());
            currentProblems.add(problem);
            return;
        }
        if (!FileUtils.hasNodeSources(version)) {
            String message = Bundle.NodeJsProblemProvider_error(Bundle.NodeJsProblemProvider_node_sources(version.toString()));
            ProjectProblem problem = ProjectProblem.createError(
                    message,
                    message,
                    getNodeResolver());
            currentProblems.add(problem);
        }
    }

    private ProjectProblemResolver getNodeResolver() {
        if (getNodeJsSupport().getPreferences().isDefaultNode()) {
            return new OptionsProblemResolver();
        }
        // pretend invalid node path
        ValidationResult result = new ValidationResult();
        ValidationUtils.validateNode(result, null);
        return new CustomizerProblemResolver(project, result);
    }

    void fireProblemsChanged() {
        problemsProviderSupport.fireProblemsChange();
    }

    //~ Inner classes

    private final class OptionsListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (!getNodeJsSupport().getPreferences().isEnabled()) {
                return;
            }
            fireProblemsChanged();
        }

    }

    private final class PreferencesListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (!getNodeJsSupport().getPreferences().isEnabled()) {
                return;
            }
            fireProblemsChanged();
        }

    }

}
