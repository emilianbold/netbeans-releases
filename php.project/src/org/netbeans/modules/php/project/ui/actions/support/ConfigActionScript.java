/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.project.deprecated.PhpProgram;
import org.netbeans.modules.php.project.deprecated.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigScriptValidator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
class ConfigActionScript extends ConfigAction {
    private final FileObject sourceRoot;

    protected ConfigActionScript(PhpProject project) {
        super(project);
        sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
        assert sourceRoot != null;
    }

    @Override
    public boolean isProjectValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), true) == null);
    }

    @Override
    public boolean isFileValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), false) == null);
    }

    private boolean isValid(boolean valid) {
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        return file != null && FileUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        new RunScript(new ScriptProvider(getStartFile(null))).run();
    }

    @Override
    public void debugProject() {
        new DebugScript(new ScriptProvider(getStartFile(null))).run();
    }

    @Override
    public void runFile(Lookup context) {
        new RunScript(new ScriptProvider(getStartFile(context))).run();
    }

    @Override
    public void debugFile(Lookup context) {
        new DebugScript(new ScriptProvider(getStartFile(context))).run();
    }

    private File getStartFile(Lookup context) {
        FileObject file;
        if (context == null) {
            file = FileUtil.toFileObject(RunConfigScript.forProject(project).getIndexFile());
        } else {
            file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        }
        assert file != null : "Start file must be found";
        return FileUtil.toFile(file);
    }

    private final class ScriptProvider extends DefaultScriptProvider implements DebugScript.Provider {

        public ScriptProvider(File file) {
            super(file);
        }

        @Override
        public PhpProject getProject() {
            return project;
        }

        @Override
        public FileObject getStartFile() {
            assert file != null;
            return FileUtil.toFileObject(file);
        }

        @Override
        public ExecutionDescriptor getDescriptor() throws IOException {
            assert file != null;
            return super.getDescriptor()
                    .charset(Charset.forName(ProjectPropertiesSupport.getEncoding(project)));
        }

        @Override
        public ExternalProcessBuilder getProcessBuilder() {
            assert file != null;
            RunConfigScript runConfig = RunConfigScript.forProject(project);
            ExternalProcessBuilder builder = program.getProcessBuilder();
            String phpArgs = runConfig.getOptions();
            if (StringUtils.hasText(phpArgs)) {
                for (String phpArg : Utilities.parseParameters(phpArgs)) {
                    builder = builder.addArgument(phpArg);
                }
            }
            builder = builder.addArgument(file.getAbsolutePath());
            String args = runConfig.getArguments();
            if (StringUtils.hasText(args)) {
                for (String arg : Utilities.parseParameters(args)) {
                    builder = builder.addArgument(arg);
                }
            }

            String workDir = runConfig.getWorkDir();
            if (StringUtils.hasText(workDir)) {
                builder = builder.workingDirectory(new File(workDir));
            } else {
                builder = builder.workingDirectory(file.getParentFile());
            }
            return builder;
        }

        @Override
        public boolean isValid() {
            return super.isValid() && file != null;
        }

        @Override
        protected PhpProgram getPhpProgram() throws InvalidPhpProgramException {
            return ProjectPropertiesSupport.getValidPhpInterpreter(project);
        }

        @Override
        public List<Pair<String, String>> getDebugPathMapping() {
            // XXX run config
            return ProjectPropertiesSupport.getDebugPathMapping(project);
        }

        @Override
        public Pair<String, Integer> getDebugProxy() {
            // XXX run config
            return ProjectPropertiesSupport.getDebugProxy(project);
        }
    }
}
