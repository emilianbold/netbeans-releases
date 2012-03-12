/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.jackpot.spi.PatternConvertor;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public abstract class JackpotBasedRefactoring implements RefactoringPlugin {

    protected final AtomicBoolean cancel = new AtomicBoolean();

    private final AbstractRefactoring refactoring;

    protected JackpotBasedRefactoring(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public final void cancelRequest() {
        cancel.set(true);
    }

    public final Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        FileObject file = tph.getFileObject();
        ClassPath source = ClassPath.getClassPath(file, ClassPath.SOURCE);
        FileObject sourceRoot = source.findOwnerRoot(file);
        
        Collection<MessageImpl> problems = new LinkedList<MessageImpl>();
        Context result = new Context();
        prepareAndConstructRule(result);
        Collection<ModificationResult> modifications = new LinkedList<ModificationResult>(result.modifications);
        Collection<HintDescription> hints = new LinkedList<HintDescription>();
        Map<FileObject, List<Difference>> scriptDifferences = new HashMap<FileObject, List<Difference>>();
        for (ScriptDescription s : result.scripts) {
            if (s.options.contains(ScriptOptions.RUN)) {
                for (HintDescription c : PatternConvertor.create(s.code)) {
                    hints.add(c);
                }
            }
            if (s.options.contains(ScriptOptions.STORE)) {
                createDifference(sourceRoot, file, s, scriptDifferences);
            }
        }
        if (!scriptDifferences.isEmpty()) {
            modifications.add(JavaSourceAccessor.getINSTANCE().createModificationResult(scriptDifferences, Collections.<Object, int[]>emptyMap()));
        }
        BatchResult batchResult = BatchSearch.findOccurrences(hints, Scopes.allOpenedProjectsScope());
        modifications.addAll(BatchUtilities.applyFixes(batchResult, new ProgressHandleWrapper(1, 1), cancel, problems));

        createAndAddElements(refactoringElements, modifications);
        
        return null;//XXX
    }

    protected abstract void prepareAndConstructRule(Context result);

    private void createAndAddElements(RefactoringElementsBag elements, Collection<ModificationResult> results) {
        elements.registerTransaction(JavaRefactoringPlugin.createTransaction(results));
        for (ModificationResult result:results) {
            for (FileObject jfo : result.getModifiedFileObjects()) {
                for (Difference diff: result.getDifferences(jfo)) {
                    elements.add(refactoring,DiffElement.create(diff, jfo, result));
                }
            }
        }
    }

    private static void createDifference(FileObject sourceRoot, FileObject origFile, ScriptDescription sd, Map<FileObject, List<Difference>> scriptDifferences) {
        String relPath = "META-INF/upgrade/" + sd.name + ".hint";
        FileObject hintFile = sourceRoot.getFileObject(relPath);
        List<Difference> diffs = scriptDifferences.get(hintFile != null ? hintFile : origFile);

        if (diffs == null) {
            scriptDifferences.put(hintFile != null ? hintFile : origFile, diffs = new LinkedList<Difference>());
        }

        if (hintFile != null) {
            //append:
            try {
                CloneableEditorSupport ces = DataObject.find(hintFile).getLookup().lookup(CloneableEditorSupport.class);
                Document doc = ces.openDocument();
                PositionRef pos = ces.createPositionRef(doc.getLength(), Bias.Forward);

                diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(Difference.Kind.INSERT, pos, pos, "", sd.code, null));
            } catch (IOException ex) {
                //XXX:
                Exceptions.printStackTrace(ex);
            }
        } else {
            File root = FileUtil.toFile(sourceRoot);
            JavaFileObject jfo = FileObjects.fileFileObject(new File(root, relPath), root, null, FileEncodingQuery.getEncoding(sourceRoot));
            
            diffs.add(JavaSourceAccessor.getINSTANCE().createNewFileDifference(jfo, sd.code));
        }
    }

    public static final class Context {
        private final Collection<ModificationResult> modifications = new LinkedList<ModificationResult>();
        private final Collection<ScriptDescription> scripts = new LinkedList<ScriptDescription>();

        public void addModificationResult(ModificationResult r) {
            modifications.add(r);
        }

        public void addScript(String name, String code, ScriptOptions... options) {
            Set<ScriptOptions> optionsSet = EnumSet.noneOf(ScriptOptions.class);
            optionsSet.addAll(Arrays.asList(options));
            scripts.add(new ScriptDescription(name, code, optionsSet));
        }
    }

    private static final class ScriptDescription {
        private final String name;
        private final String code;
        private final Set<ScriptOptions> options;

        public ScriptDescription(String name, String code, Set<ScriptOptions> run) {
            this.name = name;
            this.code = code;
            this.options = run;
        }

    }

    public enum ScriptOptions {
        STORE,
        RUN;
    }
}
