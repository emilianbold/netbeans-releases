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
package org.netbeans.modules.php.analysis.commands;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.CodeSnifferReportParser;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CodeSniffer {

    public static final String NAME = "phpcs"; // NOI18N
    public static final String LONG_NAME = NAME + FileUtils.getScriptExtension(true);

    private static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpcs-log.xml"); // NOI18N

    // XXX standard
    private static final String STANDARD_PARAM = "--standard=PSR2"; // NOI18N
    private static final String REPORT_PARAM = "--report=xml"; // NOI18N
    private static final String REPORT_FILE_PARAM = "--report-file=" + XML_LOG.getAbsolutePath();
    // XXX how to get all php extensions?
    private static final String EXTENSIONS_PARAM = "--extensions=php"; // NOI18N
    private static final String ENCODING_PARAM = "--encoding=%s"; // NOI18N
    private static final String NO_RECURSION_PARAM = "-l"; // NOI18N

    private final String codeSnifferPath;


    private CodeSniffer(String codeSnifferPath) {
        this.codeSnifferPath = codeSnifferPath;
    }

    /**
     * Get the default, <b>valid only</b> Code Sniffer.
     * @return the default, <b>valid only</b> Code Sniffer.
     * @throws InvalidPhpExecutableException if Code Sniffer is not valid.
     */
    public static CodeSniffer getDefault() throws InvalidPhpExecutableException {
        String codeSnifferPath = AnalysisOptions.getInstance().getCodeSnifferPath();
        String error = validate(codeSnifferPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new CodeSniffer(codeSnifferPath);
    }

    @NbBundle.Messages("CodeSniffer.script.label=Code sniffer")
    public static String validate(String codeSnifferPath) {
        return PhpExecutableValidator.validateCommand(codeSnifferPath, Bundle.CodeSniffer_script_label());
    }

    @CheckForNull
    public List<Result> analyze(FileObject file) {
        return analyze(file, false);
    }

    @NbBundle.Messages("CodeSniffer.analyze=Code Sniffer (analyze)")
    @CheckForNull
    public List<Result> analyze(FileObject file, boolean noRecursion) {
        assert file.isValid() : "Invalid file given: " + file;
        try {
            Integer result = getExecutable(Bundle.CodeSniffer_analyze())
                    .additionalParameters(getParameters(file, noRecursion))
                    .runAndWait(getDescriptor(), "Running code sniffer..."); // NOI18N
            if (result == null) {
                return null;
            }
            return CodeSnifferReportParser.parse(XML_LOG);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    private PhpExecutable getExecutable(String title) {
        return new PhpExecutable(codeSnifferPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor() {
        // XXX no reset but custom IO is needed
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH)
                .inputVisible(false);
        return descriptor;
    }

    private List<String> getParameters(FileObject file, boolean noRecursion) {
        Charset encoding = FileEncodingQuery.getEncoding(file);
        List<String> params = new ArrayList<String>();
        params.add(STANDARD_PARAM);
        params.add(REPORT_PARAM);
        params.add(REPORT_FILE_PARAM);
        params.add(EXTENSIONS_PARAM);
        params.add(String.format(ENCODING_PARAM, encoding.name()));
        if (noRecursion) {
            params.add(NO_RECURSION_PARAM);
        }
        params.add(FileUtil.toFile(file).getAbsolutePath());
        return params;
    }

}
