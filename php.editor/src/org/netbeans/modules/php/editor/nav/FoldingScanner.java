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
package org.netbeans.modules.php.editor.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class FoldingScanner {

    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N
    private static final String FOLD_CLASS = "inner-classes"; //NOI18N
    private static final String FOLD_PHPDOC = "comments"; //NOI18N
    private static final String FOLD_COMMENT = "initial-comment"; //NOI18N
    private static final String FOLD_OTHER_CODE_BLOCKS = "othercodeblocks"; //NOI18N
    private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY"; //NOI18N

    public static FoldingScanner create() {
        return new FoldingScanner();
    }

    private FoldingScanner() {
    }

    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        Program program = Utils.getRoot(info);
        if (program != null) {
            if (program.getStatements().size() == 1) {
                // check whether the ast is broken.
                if (program.getStatements().get(0) instanceof ASTError) {
                    final Document document = info.getSnapshot().getSource().getDocument(false);
                    @SuppressWarnings("unchecked") //NOI18N
                    Map<String, List<OffsetRange>> lastCorrect = document != null
                            ? ((Map<String, List<OffsetRange>>) document.getProperty(LAST_CORRECT_FOLDING_PROPERTY))
                            : null;
                    if (lastCorrect != null) {
                        return lastCorrect;
                    } else {
                        return Collections.emptyMap();
                    }
                }
            }
            List<Comment> comments = program.getComments();
            if (comments != null) {
                for (Comment comment : comments) {
                    if (comment.getCommentType() == Comment.Type.TYPE_PHPDOC) {
                        getRanges(folds, FOLD_PHPDOC).add(createOffsetRange(comment));
                    } else {
                        if (comment.getCommentType() == Comment.Type.TYPE_MULTILINE) {
                            getRanges(folds, FOLD_COMMENT).add(createOffsetRange(comment));
                        }
                    }
                }
            }
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel(Model.Type.COMMON);
            FileScope fileScope = model.getFileScope();
            List<Scope> scopes = getEmbededScopes(fileScope, null);
            for (Scope scope : scopes) {
                OffsetRange offsetRange = scope.getBlockRange();
                if (offsetRange == null) {
                    continue;
                }
                if (scope instanceof TypeScope) {
                    getRanges(folds, FOLD_CLASS).add(offsetRange);
                } else {
                    if (scope instanceof FunctionScope || scope instanceof MethodScope) {
                        getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
                    }
                }
            }
            program.accept(new FoldingVisitor(folds));
            Source source = info.getSnapshot().getSource();
            assert source != null : "source was null";
            Document doc = source.getDocument(false);

            if (doc != null) {
                doc.putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
            }
            return folds;
        }
        return Collections.emptyMap();
    }

    private OffsetRange createOffsetRange(ASTNode node) {
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    private List<Scope>  getEmbededScopes(Scope scope, List<Scope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new ArrayList<Scope>();
        }
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof Scope) {
                collectedScopes.add((Scope) element);
                getEmbededScopes((Scope) element, collectedScopes);
            }
        }
        return collectedScopes;
    }

    private class FoldingVisitor extends DefaultVisitor {
        private final Map<String, List<OffsetRange>> folds;

        public FoldingVisitor(final Map<String, List<OffsetRange>> folds) {
            this.folds = folds;
        }

        @Override
        public void visit(IfStatement node) {
            super.visit(node);
            if (node.getTrueStatement() != null) {
                addFold(node.getTrueStatement());
            }
            if (node.getFalseStatement() != null && !(node.getFalseStatement() instanceof IfStatement)) {
                addFold(node.getFalseStatement());
            }
        }

        @Override
        public void visit(UseTraitStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(ForEachStatement node) {
            super.visit(node);
            if (node.getStatement() != null) {
                addFold(node.getStatement());
            }
        }

        @Override
        public void visit(ForStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(WhileStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(DoStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(SwitchStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(SwitchCase node) {
            super.visit(node);
            List<Statement> actions = node.getActions();
            if (!actions.isEmpty()) {
                OffsetRange offsetRange = null;
                if (node.isDefault()) {
                    offsetRange = new OffsetRange(node.getStartOffset() + "default:".length(), actions.get(actions.size() - 1).getEndOffset()); //NOI18N
                } else {
                    Expression value = node.getValue();
                    if (value != null) {
                        offsetRange = new OffsetRange(value.getEndOffset() + ":".length(), actions.get(actions.size() - 1).getEndOffset()); //NOI18N
                    }
                }
                addFold(offsetRange);
            }
        }

        @Override
        public void visit(TryStatement node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(CatchClause node) {
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        private void addFold(final ASTNode node) {
            addFold(createOffsetRange(node));
        }

        private void addFold(final OffsetRange offsetRange) {
            getRanges(folds, FOLD_OTHER_CODE_BLOCKS).add(offsetRange);
        }

    }

}
