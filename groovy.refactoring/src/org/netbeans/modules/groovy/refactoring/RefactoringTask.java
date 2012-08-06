/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring;

import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.api.parser.SourceUtils;
import org.netbeans.modules.groovy.refactoring.ui.WhereUsedQueryUI;
import org.netbeans.modules.groovy.refactoring.utils.ElementUtils;
import org.netbeans.modules.groovy.refactoring.utils.FindTypeUtils;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * Abstract groovy refactoring task. In the current state it is always either
 * TextComponent task (which means refactoring called from the editor) or
 * NodeToElementTask (refactoring called on the concrete node)
 *
 * @author Martin Janicek
 */
public abstract class RefactoringTask extends UserTask implements Runnable {

    private RefactoringTask() {
    }

    public static RefactoringTask createRefactoringTask(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        FileObject fileObject = lookup.lookup(FileObject.class);

        if (GroovyProjectUtil.isFromEditor(ec)) {
            return new TextComponentTask(ec, fileObject);
        } else {
            return new NodeToElementTask(lookup.lookupAll(Node.class), fileObject);
        }
    }


    private static class TextComponentTask extends RefactoringTask {

        private final FileObject fileObject;
        private JTextComponent textC;
        private RefactoringUI ui;

        
        private TextComponentTask(EditorCookie ec, FileObject fileObject) {
            this.textC = ec.getOpenedPanes()[0];
            this.fileObject = fileObject;

            assert textC != null;
            assert textC.getCaretPosition() != -1;
            assert textC.getSelectionStart() != -1;
            assert textC.getSelectionEnd() != -1;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult parserResult = AstUtilities.getParseResult(resultIterator.getParserResult());
            ASTNode root = AstUtilities.getRoot(parserResult);
            if (root == null) {
                return;
            }

            int caret = textC.getCaretPosition();
            int start = textC.getSelectionStart();
            int end = textC.getSelectionEnd();

            BaseDocument doc = GroovyProjectUtil.getDocument(parserResult, fileObject);
            AstPath path = new AstPath(root, caret, doc);
            ASTNode findingNode = FindTypeUtils.findCurrentNode(path, doc, fileObject, caret);;
            ElementKind kind = ElementUtils.getKind(path, fileObject, doc, caret);

            GroovyRefactoringElement element = new GroovyRefactoringElement(parserResult, (ModuleNode) root, findingNode, fileObject, kind);
            if (element != null && element.getName() != null) {
                ui = createRefactoringUI(element, start, end, parserResult);
            }
        }

        @Override
        public final void run() {
            try {
                SourceUtils.runUserActionTask(fileObject, this);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            UI.openRefactoringUI(ui, TopComponent.getRegistry().getActivated());
        }

        protected RefactoringUI createRefactoringUI(GroovyRefactoringElement selectedElement,int startOffset,int endOffset, GroovyParserResult info) {
            return new WhereUsedQueryUI(selectedElement);
        }
    }

    private static class NodeToElementTask extends RefactoringTask {

        private final FileObject fileObject;
        private RefactoringUI ui;


        private NodeToElementTask(Collection<? extends Node> nodes, FileObject fileObject) {
            assert nodes.size() == 1;
            this.fileObject = fileObject;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult parserResult = AstUtilities.getParseResult(resultIterator.getParserResult());
            ASTNode root = AstUtilities.getRoot(parserResult);
            if (root == null) {
                return;
            }
            
            GroovyRefactoringElement element = new GroovyRefactoringElement(parserResult, (ModuleNode) root, root, fileObject, ElementKind.CLASS);
            if (element != null && element.getName() != null) {
                ui = createRefactoringUI(element, parserResult);
            }
        }

        @Override
        public final void run() {
            try {
                SourceUtils.runUserActionTask(fileObject, this);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            UI.openRefactoringUI(ui);
        }

        protected RefactoringUI createRefactoringUI(GroovyRefactoringElement selectedElement, GroovyParserResult info) {
            return new WhereUsedQueryUI(selectedElement);
        }
    }
}
