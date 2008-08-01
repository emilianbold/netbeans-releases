/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.parser.SourceUtils;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
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
 * @author Martin Adamek
 */
public class GroovyWhereUsed extends ProgressProviderAdapter implements GroovyRefactoring {
    
    private final WhereUsedQuery whereUsedQuery;
    private final FileObject fileObject;
    private final String fqn;
    
    public GroovyWhereUsed(FileObject fileObject, String fqn, WhereUsedQuery whereUsedQuery) {
        this.fqn = fqn;
        this.fileObject = fileObject;
        this.whereUsedQuery = whereUsedQuery;
    }
    
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        Set<FileObject> relevantFiles = getRelevantFiles();
        for (final FileObject fo : relevantFiles) {
            try {
                SourceUtils.runUserActionTask(fo, new CancellableTask<GroovyParserResult>() {
                    public void run(GroovyParserResult result) throws Exception {
                        ModuleNode moduleNode = result.getRootElement().getModuleNode();
                        Set<ASTNode> usages = new UsagesVisitor(moduleNode, fqn).findUsages();
                        BaseDocument doc = Utils.getDocument(result.getInfo(), fo);
                        for (ASTNode node : usages) {
                            refactoringElements.add(whereUsedQuery, new WhereUsedElement(new GroovyRefactoringElement(moduleNode, node, fo), doc));
                        }
                    }
                    public void cancel() {}
                });
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        return null;
    }

    public Problem preCheck() {
        return null;
    }
    
    private Set<FileObject> getRelevantFiles() {
        final Set<FileObject> set = new HashSet<FileObject>();
        // XXX be more selective here
        set.addAll(Utils.getGroovyFilesInProject(fileObject));
        return set;
    }
    
    private static class WhereUsedElement extends SimpleRefactoringElementImplementation{

        private final GroovyRefactoringElement element;
        private final BaseDocument doc;

        public WhereUsedElement(GroovyRefactoringElement element, BaseDocument doc) {
            this.element = element;
            this.doc = doc;
        }

        public String getText() {
            return element.getName() + " -";
        }

        public String getDisplayText() {
            Line line = Utils.getLine(element.getFileObject(), element.getNode().getLineNumber() - 1);
            return line.getText().trim();
        }

        public void performChange() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return element.getFileObject();
        }

        public PositionBounds getPosition() {

            OffsetRange range = AstUtilities.getRange(element.getNode(), doc);
            if (range == OffsetRange.NONE) {
                return null;
            }

            CloneableEditorSupport ces = Utils.findCloneableEditorSupport(element.getFileObject());
            PositionRef ref1 = ces.createPositionRef(range.getStart(), Bias.Forward);
            PositionRef ref2 = ces.createPositionRef(range.getEnd(), Bias.Forward);
            return new PositionBounds(ref1, ref2);
        }
        
    }

    private static class UsagesVisitor extends ClassCodeVisitorSupport {

        private final ModuleNode moduleNode;
        private final String fqn;
        private final Set<ASTNode> usages = new HashSet<ASTNode>();

        public UsagesVisitor(ModuleNode moduleNode, String fqn) {
            this.moduleNode = moduleNode;
            this.fqn = fqn;
        }

        public Set<ASTNode> findUsages() {
            for (Object object : moduleNode.getClasses()) {
                visitClass((ClassNode) object);
            }
            return usages;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return moduleNode.getContext();
        }

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            VariableExpression variable = expression.getVariableExpression();
            ClassNode classNode = variable.getType();
            if (fqn.equals(classNode.getName())) {
                usages.add(variable);
            }
            super.visitDeclarationExpression(expression);
        }

        @Override
        public void visitField(FieldNode node) {
            if (fqn.equals(node.getType().getName())) {
                usages.add(node);
            }
            super.visitField(node);
        }

    }

}
