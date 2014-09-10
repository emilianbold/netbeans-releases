/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.nodejs.exec;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript2.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript2.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript2.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript2.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript2.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript2.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.javascript2.nodejs.util.ExternalExecutable;
import org.netbeans.modules.javascript2.nodejs.util.StringUtils;
import org.netbeans.modules.javascript2.nodejs.util.ValidationResult;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class NodeExecutable {

    private static final Logger LOGGER = Logger.getLogger(NodeExecutable.class.getName());

    public static final String NODE_NAME;

    private static final String DEBUG_COMMAND = "debug"; // NOI18N

    protected final Project project;
    protected final String nodePath;


    static {
        if (Utilities.isWindows()) {
            NODE_NAME = "node.exe"; // NOI18N
        } else {
            NODE_NAME = "node"; // NOI18N
        }
    }


    NodeExecutable(Project project, String nodePath) {
        assert project != null;
        assert nodePath != null;
        this.project = project;
        this.nodePath = nodePath;
    }

    @CheckForNull
    public static NodeExecutable getDefault(Project project, boolean showOptions) {
        ValidationResult result = new NodeJsOptionsValidator()
                .validate()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        String node = NodeJsOptions.getInstance().getNode();
        if (Utilities.isMac()) {
            return new MacNodeExecutable(project, node);
        }
        return new NodeExecutable(project, node);
    }

    @CheckForNull
    public static NodeExecutable forProject(Project project, boolean showCustomizer) {
        String node = NodeJsSupport.forProject(project).getPreferences().getNode();
        if (node == null) {
            return getDefault(project, showCustomizer);
        }
        ValidationResult result = new NodeJsPreferencesValidator()
                .validate(project)
                .getResult();
        if (validateResult(result) != null) {
            if (showCustomizer) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(NodeJsCustomizerProvider.CUSTOMIZER_IDENT, null);
            }
            return null;
        }
        if (Utilities.isMac()) {
            return new MacNodeExecutable(project, node);
        }
        return new NodeExecutable(project, node);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NodeExecutable.run=Node.js ({0})",
    })
    @CheckForNull
    public Future<Integer> run(File script) {
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        Future<Integer> task = getExecutable(Bundle.NodeExecutable_run(projectName), getProjectDir())
                .additionalParameters(getRunParams(script))
                .run(getDescriptor());
        assert task != null : nodePath;
        return task;
    }

    private File getProjectDir() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    String getCommand() {
        return nodePath;
    }

    private ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(getCommand())
                .workDir(workDir)
                .displayName(title)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        return new ExecutionDescriptor()
                .frontWindow(true)
                .frontWindowOnError(false)
                .controllable(true)
                .optionsPath(NodeJsOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true);
    }

    List<String> getRunParams(File script) {
        assert script != null;
        return Collections.singletonList(script.getAbsolutePath());
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    //~ Inner classes

    private static final class MacNodeExecutable extends NodeExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacNodeExecutable(Project project, String nodePath) {
            super(project, nodePath);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getRunParams(File script) {
            return getParams(super.getRunParams(script));
        }

        private List<String> getParams(List<String> originalParams) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(nodePath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(originalParams, "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

}
