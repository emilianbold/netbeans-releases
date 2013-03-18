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
package org.netbeans.modules.php.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.analyzer.CodeSnifferCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public class CodeSnifferAnalyzerImpl implements Analyzer {

    private static final String ANALYZER_PREFIX = "codeSniffer:"; // NOI18N
    private static final LazyFixList EMPTY_LAZY_FIX_LIST = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());

    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();


    CodeSnifferAnalyzerImpl(Context context) {
        this.context = context;
    }

    @CheckForNull
    static CodeSniffer getCodeSniffer() {
        try {
            return CodeSniffer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getMessage(), AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();

        CodeSniffer codeSniffer = getCodeSniffer();
        assert codeSniffer != null;

        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);

        // XXX how to get it from customizer?
        String codeSnifferStandard = AnalysisOptions.getInstance().getCodeSnifferStandard();
        int progress = 0;
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<Result> results = codeSniffer.analyze(codeSnifferStandard, root);
            // XXX inform about error?
            if (results != null) {
                errors.addAll(map(results));
            }
            progress += fileCount.get(root);
            context.progress(progress);
        }

        for (FileObject file : scope.getFiles()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<Result> results = codeSniffer.analyze(codeSnifferStandard, file);
            // XXX inform about error?
            if (results != null) {
                errors.addAll(map(results));
            }
            progress += fileCount.get(file);
            context.progress(progress);
        }

        for (NonRecursiveFolder nonRecursiveFolder : scope.getFolders()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            FileObject folder = nonRecursiveFolder.getFolder();
            List<Result> results = codeSniffer.analyze(codeSnifferStandard, folder, true);
            // XXX inform about error?
            if (results != null) {
                errors.addAll(map(results));
            }
            progress += fileCount.get(folder);
            context.progress(progress);
        }

        context.finish();

        return errors;
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        // XXX cancel code sniffer?
        return true;
    }

    //~ Mappers

    private Collection<? extends ErrorDescription> map(List<Result> results) {
        List<ErrorDescription> errorDescriptions = new ArrayList<ErrorDescription>(results.size());
        FileObject file = null;
        String filePath = null;
        int[] lineMap = null;
        for (Result result : results) {
            String currentFilePath = result.getFilePath();
            if (!currentFilePath.equals(filePath)) {
                filePath = currentFilePath;
                file = FileUtil.toFileObject(new File(currentFilePath));
                assert file != null : "File object not found for " + currentFilePath;
                lineMap = AnalysisUtils.computeLineMap(file, FileEncodingQuery.getEncoding(file));
            }
            assert file != null;
            assert filePath != null;
            assert lineMap != null;
            errorDescriptions.add(map(result, file, lineMap));
        }
        return errorDescriptions;
    }

    private ErrorDescription map(Result result, FileObject file, int[] lineMap) {
        int line = 2 * (Math.min(result.getLine(), lineMap.length / 2) - 1);
        // XXX i18n for category
        // XXX categories in Inspector view
        // XXX which severity?
        // XXX fixes? show only warning without any hint...
        return ErrorDescriptionFactory.createErrorDescription(ANALYZER_PREFIX + result.getCategory(), Severity.VERIFIER, result.getCategory(),
                result.getDescription(), EMPTY_LAZY_FIX_LIST, file, lineMap[line], lineMap[line + 1]);
    }

    //~ Inner classes

    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class CodeSnifferAnalyzerFactory extends AnalyzerFactory {

        // XXX
        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/warning.gif"; // NOI18N


        @NbBundle.Messages("CodeSnifferAnalyzerFactory.displayName=Code Sniffer")
        public CodeSnifferAnalyzerFactory() {
            super("PhpCodeSniffer", Bundle.CodeSnifferAnalyzerFactory_displayName(), ICON_PATH);
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            // XXX
            // check code sniffer is set correctly
            getCodeSniffer();
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, CodeSnifferCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, CodeSnifferCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }
                @Override
                public CodeSnifferCustomizerPanel createComponent(CustomizerContext<Void, CodeSnifferCustomizerPanel> context) {
                    // XXX whatif any error in the customizer?
                    // how to get values from customizer?
                    return new CodeSnifferCustomizerPanel();
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new CodeSnifferAnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            // XXX
        }

    }

}
