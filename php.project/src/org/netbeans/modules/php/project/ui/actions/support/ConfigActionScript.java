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
import java.util.Arrays;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProgram;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
public class ConfigActionScript extends ConfigAction {

    @Override
    public boolean isRunProjectEnabled(PhpProject project) {
        return isRunProjectEnabled();
    }

    @Override
    public boolean isDebugProjectEnabled(PhpProject project) {
        return isDebugProjectEnabled();
    }

    @Override
    public boolean isRunFileEnabled(PhpProject project, Lookup context) {
        FileObject rootFolder = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, rootFolder);
        return file != null && CommandUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(PhpProject project, Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(project, context);
    }

    @Override
    public void runProject(PhpProject project) {
        new RunScript(new ScriptProvider(project, null)).run();
    }

    @Override
    public void debugProject(PhpProject project) {
        new DebugScript(new ScriptProvider(project, null)).run();
    }

    @Override
    public void runFile(PhpProject project, Lookup context) {
        new RunScript(new ScriptProvider(project, context)).run();
    }

    @Override
    public void debugFile(PhpProject project, Lookup context) {
        new DebugScript(new ScriptProvider(project, context)).run();
    }

    private final class ScriptProvider implements DebugScript.Provider {
        private final PhpProject project;
        private final PhpProgram program;
        private final File startFile;

        public ScriptProvider(PhpProject project, Lookup context) {
            assert project != null;

            this.project = project;
            program = ProjectPropertiesSupport.getPhpInterpreter(project);
            startFile = getStartFile(context);
        }

        public Project getProject() {
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
                    .optionsPath(PHPOptionsCategory.PATH_IN_LAYER)
                    .outProcessorFactory(redirector)
                    .postExecution(redirector);

        }

        public ExternalProcessBuilder getProcessBuilder() {
            assert startFile != null;
            ExternalProcessBuilder builder = new ExternalProcessBuilder(program.getProgram());
            for (String param : program.getParameters()) {
                builder = builder.addArgument(param);
            }
            builder = builder.addArgument(startFile.getName());
            String argProperty = ProjectPropertiesSupport.getArguments(project);
            if (PhpProjectUtils.hasText(argProperty)) {
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
            FileObject sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
            if (context == null) {
                file = CommandUtils.fileForProject(project, sourceRoot);
            } else {
                file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
            }
            assert file != null : "Start file must be found";
            return FileUtil.toFile(file);
        }
    }
}
