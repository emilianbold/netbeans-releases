/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.refactoring;

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Position.Bias;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.api.parser.SourceUtils;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesVisitor;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Adamek, Martin Janicek
 */
public class GroovyWhereUsed extends ProgressProviderAdapter implements RefactoringPlugin {
    
    private final WhereUsedQuery whereUsedQuery;
    private final FileObject fileObject;
    private final String fqn;

    
    public GroovyWhereUsed(FileObject fileObject, String fqn, WhereUsedQuery whereUsedQuery) {
        this.fqn = fqn;
        this.fileObject = fileObject;
        this.whereUsedQuery = whereUsedQuery;
    }
    
    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        Set<FileObject> relevantFiles = getRelevantFiles();
        for (final FileObject fo : relevantFiles) {
            try {
                SourceUtils.runUserActionTask(fo, new AddRefactoringElements(refactoringElements, fo));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        final Set<FileObject> set = new HashSet<FileObject>();
        // XXX be more selective here
        set.addAll(GroovyProjectUtil.getGroovyFilesInProject(fileObject));
        return set;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
    }

    private class AddRefactoringElements extends UserTask {

        private final RefactoringElementsBag refactoringElements;
        private final FileObject fo;


        public AddRefactoringElements(RefactoringElementsBag refactoringElements, FileObject fo) {
            this.refactoringElements = refactoringElements;
            this.fo = fo;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult result = AstUtilities.getParseResult(resultIterator.getParserResult());
            ModuleNode moduleNode = result.getRootElement().getModuleNode();
            Set<ASTNode> usages = new FindUsagesVisitor(moduleNode, fqn).findUsages();
            BaseDocument doc = GroovyProjectUtil.getDocument(result, fo);
            for (ASTNode node : usages) {
                refactoringElements.add(whereUsedQuery, new WhereUsedElement(new GroovyRefactoringElement(result, moduleNode, node, fo), doc));
            }
        }
    }
    
    private static class WhereUsedElement extends SimpleRefactoringElementImplementation{

        private final GroovyRefactoringElement element;
        private final BaseDocument doc;

        public WhereUsedElement(GroovyRefactoringElement element, BaseDocument doc) {
            this.element = element;
            this.doc = doc;
        }

        @Override
        public String getText() {
            return element.getName() + " -";
        }

        @Override
        public String getDisplayText() {
            Line line = GroovyProjectUtil.getLine(element.getFileObject(), element.getNode().getLineNumber() - 1);
            return line.getText().trim();
        }

        @Override
        public void performChange() {
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return element.getFileObject();
        }

        @Override
        public PositionBounds getPosition() {
            OffsetRange range = AstUtilities.getRange(element.getNode(), doc);
            if (range == OffsetRange.NONE) {
                return null;
            }

            CloneableEditorSupport ces = GroovyProjectUtil.findCloneableEditorSupport(element.getFileObject());
            PositionRef ref1 = ces.createPositionRef(range.getStart(), Bias.Forward);
            PositionRef ref2 = ces.createPositionRef(range.getEnd(), Bias.Forward);
            return new PositionBounds(ref1, ref2);
        }
    }
}
