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

package org.netbeans.modules.javascript.nodejs.exec;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.javascript.nodejs.util.ExternalExecutable;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.modules.javascript.nodejs.util.ValidationResult;
import org.netbeans.modules.javascript.nodejs.util.ValidationUtils;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class NodeExecutable {

    private static final Logger LOGGER = Logger.getLogger(NodeExecutable.class.getName());

    public static final String NODE_NAME;

    private static final String DEBUG_COMMAND = "debug"; // NOI18N
    private static final String VERSION_PARAM = "--version"; // NOI18N

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir")); // NOI18N

    // versions of node executables
    private static final ConcurrentMap<String, Version> VERSIONS = new ConcurrentHashMap<>();

    protected final Project project;
    protected final String nodePath;


    static {
        if (Utilities.isWindows()) {
            NODE_NAME = "node.exe"; // NOI18N
        } else {
            NODE_NAME = "node"; // NOI18N
        }
    }


    NodeExecutable(String nodePath, @NullAllowed Project project) {
        assert nodePath != null;
        this.nodePath = nodePath;
        this.project = project;
    }

    @CheckForNull
    public static NodeExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
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
            return new MacNodeExecutable(node, project);
        }
        return new NodeExecutable(node, project);
    }

    @CheckForNull
    public static NodeExecutable forProject(Project project, boolean showCustomizer) {
        assert project != null;
        return forProjectInternal(project, showCustomizer);
    }

    @CheckForNull
    public static NodeExecutable forPath(String path) {
        ValidationResult result = new ValidationResult();
        ValidationUtils.validateNode(result, path);
        if (validateResult(result) != null) {
            return null;
        }
        if (Utilities.isMac()) {
            return new MacNodeExecutable(path, null);
        }
        return new NodeExecutable(path, null);
    }

    @CheckForNull
    private static NodeExecutable forProjectInternal(@NullAllowed Project project, boolean showCustomizer) {
        if (project == null) {
            return getDefault(project, showCustomizer);
        }
        NodeJsPreferences preferences = NodeJsSupport.forProject(project).getPreferences();
        if (preferences.isDefaultNode()) {
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
        String node = preferences.getNode();
        assert node != null;
        if (Utilities.isMac()) {
            return new MacNodeExecutable(node, project);
        }
        return new NodeExecutable(node, project);
    }

    String getCommand() {
        return nodePath;
    }

    public void resetVersion() {
        VERSIONS.remove(nodePath);
    }

    public boolean hasVersion() {
        return VERSIONS.containsKey(nodePath);
    }

    @CheckForNull
    public Version getVersion() {
        Version version = VERSIONS.get(nodePath);
        if (version != null) {
            return version;
        }
        return getAndStoreVersion();
    }

    @NbBundle.Messages({
        "NodeExecutable.version.detecting=Detecting node version..."
    })
    @CheckForNull
    private Version getAndStoreVersion() {
        VersionOutputProcessorFactory versionOutputProcessorFactory = new VersionOutputProcessorFactory();
        try {
            getExecutable("node version") // NOI18N
                    .additionalParameters(getVersionParams())
                    .runAndWait(getSilentDescriptor(), versionOutputProcessorFactory, Bundle.NodeExecutable_version_detecting());
            String detectedVersion = versionOutputProcessorFactory.getVersion();
            if (detectedVersion != null) {
                Version version = Version.fromDottedNotationWithFallback(detectedVersion);
                VERSIONS.put(nodePath, version);
                return version;
            }
        } catch (CancellationException ex) {
            // cancelled, cannot happen
            assert false;
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NodeExecutable.run=Node.js ({0})",
    })
    @CheckForNull
    public Future<Integer> run(File script) {
        assert project != null;
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        Future<Integer> task = getExecutable(Bundle.NodeExecutable_run(projectName))
                .additionalParameters(getRunParams(script))
                .run(getDescriptor());
        assert task != null : nodePath;
        return task;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
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

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false);
    }

    private File getWorkDir() {
        if (project == null) {
            return TMP_DIR;
        }
        for (SourceGroup sourceGroup : ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            File root = FileUtil.toFile(rootFolder);
            assert root != null : rootFolder;
            return root;
        }
        File workDir = FileUtil.toFile(project.getProjectDirectory());
        assert workDir != null : project.getProjectDirectory();
        return workDir;
    }

    private List<String> getRunParams(File script) {
        assert script != null;
        return getParams(script.getAbsolutePath());
    }

    private List<String> getVersionParams() {
        return getParams(VERSION_PARAM);
    }

    List<String> getParams(String... params) {
        assert params != null;
        return Arrays.asList(params);
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


        MacNodeExecutable(String nodePath, Project project) {
            super(nodePath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(String... params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(nodePath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    static class VersionOutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private static final Pattern VERSION_PATTERN = Pattern.compile("^v([\\d\\.]+)$"); // NOI18N

        volatile String version;


        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    assert version == null : version + " :: " + line;
                    version = parseVersion(line);
                }

                @Override
                public void reset() {
                }

                @Override
                public void close() {
                }

            });
        }

        @CheckForNull
        public String getVersion() {
            return version;
        }

        public String parseVersion(String line) {
            Matcher matcher = VERSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            LOGGER.log(Level.INFO, "Unexpected node.js version line: {0}", line);
            return null;
        }

    }


}
