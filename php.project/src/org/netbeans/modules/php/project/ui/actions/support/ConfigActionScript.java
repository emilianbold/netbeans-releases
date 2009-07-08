/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.Arrays;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import org.netbeans.modules.php.api.util.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
class ConfigActionScript extends ConfigAction {
    static final ExecutionDescriptor.LineConvertorFactory PHP_LINE_CONVERTOR_FACTORY = new PhpLineConvertorFactory();
    private final FileObject sourceRoot;

    protected ConfigActionScript(PhpProject project) {
        super(project);
        sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
        assert sourceRoot != null;
    }

    @Override
    public boolean isValid(boolean indexFileNeeded) {
        boolean valid = true;
        if (indexFileNeeded && !isIndexFileValid(sourceRoot)) {
            valid = false;
        } else if (RunAsValidator.validateScriptFields(
                ProjectPropertiesSupport.getPhpInterpreter(project).getProgram(),
                FileUtil.toFile(sourceRoot),
                null,
                ProjectPropertiesSupport.getArguments(project)) != null) {
            valid = false;
        }
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        return file != null && CommandUtils.isPhpFile(file);
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
        new RunScript(new ScriptProvider()).run();
    }

    @Override
    public void debugProject() {
        new DebugScript(new ScriptProvider()).run();
    }

    @Override
    public void runFile(Lookup context) {
        new RunScript(new ScriptProvider(context)).run();
    }

    @Override
    public void debugFile(Lookup context) {
        new DebugScript(new ScriptProvider(context)).run();
    }

    private final class ScriptProvider implements DebugScript.Provider {
        private final PhpProgram program;
        private final File startFile;

        public ScriptProvider() {
            this(null);
        }

        public ScriptProvider(Lookup context) {
            program = ProjectPropertiesSupport.getPhpInterpreter(project);
            startFile = getStartFile(context);
        }

        public PhpProject getProject() {
            return project;
        }

        public FileObject getStartFile() {
            assert startFile != null;
            return FileUtil.toFileObject(startFile);
        }

        public ExecutionDescriptor getDescriptor() throws IOException {
            assert startFile != null;
            RunScript.InOutPostRedirector redirector = new RunScript.InOutPostRedirector(startFile);
            return new ExecutionDescriptor()
                    .frontWindow(PhpOptions.getInstance().isOpenResultInOutputWindow())
                    .inputVisible(true)
                    .showProgress(true)
                    .optionsPath(UiUtils.OPTIONS_PATH)
                    .outConvertorFactory(PHP_LINE_CONVERTOR_FACTORY)
                    .outProcessorFactory(redirector)
                    .postExecution(redirector)
                    .charset(Charset.forName(ProjectPropertiesSupport.getEncoding(project)));

        }

        public ExternalProcessBuilder getProcessBuilder() {
            assert startFile != null;
            ExternalProcessBuilder builder = new ExternalProcessBuilder(program.getProgram());
            for (String param : program.getParameters()) {
                builder = builder.addArgument(param);
            }
            builder = builder.addArgument(startFile.getName());
            String argProperty = ProjectPropertiesSupport.getArguments(project);
            if (StringUtils.hasText(argProperty)) {
                for (String argument : Arrays.asList(argProperty.split(" "))) { // NOI18N
                    builder = builder.addArgument(argument);
                }
            }
            builder = builder.workingDirectory(startFile.getParentFile());
            return builder;
        }

        public String getOutputTabTitle() {
            assert startFile != null;
            return String.format("%s - %s", program.getProgram(), startFile.getName());
        }

        public boolean isValid() {
            return program.isValid() && startFile != null;
        }

        private File getStartFile(Lookup context) {
            FileObject file = null;
            if (context == null) {
                file = CommandUtils.fileForProject(project, sourceRoot);
            } else {
                file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
            }
            assert file != null : "Start file must be found";
            return FileUtil.toFile(file);
        }
    }

    static final class PhpLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        public LineConvertor newLineConvertor() {
            LineConvertor[] lineConvertors = new LineConvertor[PhpInterpreter.LINE_PATTERNS.length];
            int i = 0;
            for (Pattern linePattern : PhpInterpreter.LINE_PATTERNS) {
                lineConvertors[i++] = LineConvertors.filePattern(null, linePattern, null, 1, 2);
            }
            return LineConvertors.proxy(lineConvertors);
        }
    }
}
