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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.deprecated.PhpProgram;
import org.netbeans.modules.php.project.deprecated.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

@Deprecated
public class DefaultScriptProvider implements DebugScript.Provider {
    protected static final ExecutionDescriptor.LineConvertorFactory PHP_LINE_CONVERTOR_FACTORY = new PhpLineConvertorFactory();

    // for debugger, let's treat all the files withour project under one dbg session
    private static final Project PROJECT = new DummyProject();

    protected final PhpProgram program;
    protected final File file;


    public DefaultScriptProvider(File file) {
        assert file != null;
        this.file = file;

        PhpProgram prg = null;
        try {
            prg = getPhpProgram();
            assert prg.isValid() : String.format("php program %s must be valid", prg);
        } catch (InvalidPhpProgramException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
        }

        program = prg;
    }

    @Override
    public ExecutionDescriptor getDescriptor() throws IOException {
        RunScript.InOutPostRedirector redirector = new RunScript.InOutPostRedirector(file);
        return PhpProgram.getExecutionDescriptor()
                .frontWindow(PhpOptions.getInstance().isOpenResultInOutputWindow())
                .optionsPath(UiUtils.OPTIONS_PATH)
                .outConvertorFactory(PHP_LINE_CONVERTOR_FACTORY)
                .outProcessorFactory(redirector)
                .postExecution(redirector)
                .charset(FileEncodingQuery.getDefaultEncoding());
    }

    @Override
    public ExternalProcessBuilder getProcessBuilder() {
        assert isValid();

        ExternalProcessBuilder builder = program.getProcessBuilder().addArgument(file.getAbsolutePath());
        builder = builder.workingDirectory(file.getParentFile());
        return builder;
    }

    @Override
    public String getOutputTabTitle() {
        assert isValid();

        return String.format("%s - %s", program.getProgram(), file.getName()); // NOI18N
    }

    @Override
    public boolean isValid() {
        return program != null && program.isValid();
    }

    protected PhpProgram getPhpProgram() throws PhpProgram.InvalidPhpProgramException {
        return null;//PhpInterpreter.getDefault();
    }

    @Override
    public Project getProject() {
        return PROJECT;
    }

    @Override
    public FileObject getStartFile() {
        return FileUtil.toFileObject(file);
    }

    @Override
    public List<Pair<String, String>> getDebugPathMapping() {
        return Collections.emptyList();
    }

    @Override
    public Pair<String, Integer> getDebugProxy() {
        return null;
    }

    private static final class PhpLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        @Override
        public LineConvertor newLineConvertor() {
            LineConvertor[] lineConvertors = new LineConvertor[PhpInterpreter.LINE_PATTERNS.length];
            int i = 0;
            for (Pattern linePattern : PhpInterpreter.LINE_PATTERNS) {
                lineConvertors[i++] = LineConvertors.filePattern(null, linePattern, null, 1, 2);
            }
            return LineConvertors.proxy(lineConvertors);
        }
    }

    // needed for php debugger, used as a key in session map
    private static final class DummyProject implements Project {

        @Override
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
