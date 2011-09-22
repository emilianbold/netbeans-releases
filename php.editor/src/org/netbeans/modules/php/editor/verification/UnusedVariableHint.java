/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnusedVariableHint extends AbstractRule {

    private static final String HINT_ID = "Unused.Variable.Hint"; //NOI18N
    private static final List<String> UNCHECKED_VARIABLES = new LinkedList<String>();

    static {
        UNCHECKED_VARIABLES.add("this"); //NOI18N
        UNCHECKED_VARIABLES.add("GLOBALS"); //NOI18N
        UNCHECKED_VARIABLES.add("_SERVER"); //NOI18N
        UNCHECKED_VARIABLES.add("_GET"); //NOI18N
        UNCHECKED_VARIABLES.add("_POST"); //NOI18N
        UNCHECKED_VARIABLES.add("_FILES"); //NOI18N
        UNCHECKED_VARIABLES.add("_COOKIE"); //NOI18N
        UNCHECKED_VARIABLES.add("_SESSION"); //NOI18N
        UNCHECKED_VARIABLES.add("_REQUEST"); //NOI18N
        UNCHECKED_VARIABLES.add("_ENV"); //NOI18N
    }

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        VariablesHeap variablesHeap = new VariablesHeap(phpParseResult.getModel(), context.doc);
        HintsCreator hintsCreator = new HintsCreator(variablesHeap, fileObject, context.doc);

        CheckVisitor checkVisitor = new CheckVisitor(variablesHeap);
        phpParseResult.getProgram().accept(checkVisitor);
        hints.addAll(hintsCreator.getHints());
    }

    private class CheckVisitor extends DefaultTreePathVisitor {

        private static final String FUNC_GET_ARG = "func_get_arg"; //NOI18N
        private final VariablesHeap variablesHeap;

        public CheckVisitor(VariablesHeap variablesHeap) {
            this.variablesHeap = variablesHeap;
        }

        @Override
        public void visit(FormalParameter node) {
            super.visit(node);
            processNode(node);
        }

        @Override
        public void visit(Variable node) {
            super.visit(node);
            processNode(node);
        }

        private void processNode(ASTNode node) {
            Identifier identifier = getIdentifier(node);
            if (identifier != null) {
                variablesHeap.addNodeUsage(node);
            }
        }

        @Override
        public void visit(FunctionInvocation node) {
            super.visit(node);
            if (node.getFunctionName().getName() instanceof NamespaceName) {
                NamespaceName namespaceName = (NamespaceName) node.getFunctionName().getName();
                if (namespaceName.getSegments().size() == 1) {
                    Identifier functionIdentifier = ModelUtils.getFirst(namespaceName.getSegments());
                    if (functionIdentifier.getName().startsWith(FUNC_GET_ARG)) {
                        variablesHeap.setParamUsageInScope(node.getStartOffset());
                    }
                }
            }
        }

    }

    private class VariablesHeap {

        private final Map<VariableScope, Map<String, List<Identifier>>> allVariables = new HashMap<VariableScope, Map<String, List<Identifier>>>();
        private final Set<VariableScope> scopesWithUsedParams = new HashSet<VariableScope>();
        private final Model model;
        private final BaseDocument doc;

        public VariablesHeap(Model model, BaseDocument doc) {
            this.model = model;
            this.doc = doc;
        }

        public void setParamUsageInScope(int inScopeOffset) {
            VariableScope variableScope = model.getVariableScope(inScopeOffset);
            scopesWithUsedParams.add(variableScope);
        }

        public boolean isUsedParamInScope(VariableScope variableScope) {
            return scopesWithUsedParams.contains(variableScope);
        }

        public void addNodeUsage(ASTNode node) {
            Identifier identifier = getIdentifier(node);
            if (identifier != null && !UNCHECKED_VARIABLES.contains(identifier.getName())) {
                int inScopeOffset = resolveInScopeOffset(node);
                VariableScope variableScope = model.getVariableScope(inScopeOffset);
                Map<String, List<Identifier>> scopeVars = getScopeVariables(variableScope);
                List<Identifier> identifiers = getIdentifiersOfName(scopeVars, identifier.getName());
                identifiers.add(identifier);
            }
        }

        private int resolveInScopeOffset(ASTNode node) {
            int retval = 0;
            if (node instanceof FormalParameter) {
                retval = getOffsetAfterBlockCurlyOpen(doc, node.getEndOffset());
            } else if (node instanceof Variable) {
                retval = node.getEndOffset();
            }
            return retval;
        }

        private int getOffsetAfterBlockCurlyOpen(BaseDocument doc, int offset) {
            int retval = offset;
            doc.readLock();
            try {
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
                if (ts != null) {
                    ts.move(retval);
                    while (ts.moveNext()) {
                        Token t = ts.token();
                        if (t.id() == PHPTokenId.PHP_CURLY_OPEN) {
                            ts.moveNext();
                            retval = ts.offset();
                            break;
                        }
                    }
                }
            } finally {
                doc.readUnlock();
            }
            return retval;
        }

        public Map<String, List<Identifier>> getScopeVariables(VariableScope variableScope) {
            Map<String, List<Identifier>> scopeVars = allVariables.get(variableScope);
            if (scopeVars == null) {
                scopeVars = new HashMap<String, List<Identifier>>();
                allVariables.put(variableScope, scopeVars);
            }
            return scopeVars;
        }

        private List<Identifier> getIdentifiersOfName(Map<String, List<Identifier>> scopeVars, String name) {
            List<Identifier> identifiers = scopeVars.get(name);
            if (identifiers == null) {
                identifiers = new LinkedList<Identifier>();
                scopeVars.put(name, identifiers);
            }
            return identifiers;
        }

        public Set<VariableScope> getVariableScopes() {
            return allVariables.keySet();
        }

    }

    private class HintsCreator {

        private final List<Hint> hints = new LinkedList<Hint>();
        private final VariablesHeap variablesHeap;
        private final FileObject fileObject;
        private final BaseDocument doc;

        public HintsCreator(VariablesHeap variablesHeap, FileObject fileObject, BaseDocument doc) {
            this.variablesHeap = variablesHeap;
            this.fileObject = fileObject;
            this.doc = doc;
        }

        public List<Hint> getHints() {
            for (VariableScope variableScope : variablesHeap.getVariableScopes()) {
                checkVariableScope(variableScope);
            }
            return hints;
        }

        private void checkVariableScope(VariableScope variableScope) {
            Collection<? extends VariableName> declaredVariables = variableScope.getDeclaredVariables();
            for (VariableName variableName : declaredVariables) {
                if (!UNCHECKED_VARIABLES.contains(getPureName(variableName)) && !isInPhpComment(variableName.getOffset())) {
                    checkVariableName(variableName, variableScope);
                }
            }
        }

        private boolean isInPhpComment(int offset) {
            Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, offset);
            PHPTokenId id = token.id();
            return id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHPDOC_COMMENT;
        }

        private void checkVariableName(VariableName variableName, VariableScope variableScope) {
            Map<String, List<Identifier>> scopeVars = variablesHeap.getScopeVariables(variableScope);
            if (isParam(variableName) && variablesHeap.isUsedParamInScope(variableScope)) {
                scopeVars.remove(getPureName(variableName));
            } else {
                checkIdentifiers(variableName, scopeVars);
            }
        }

        private boolean isParam(VariableName variableName) {
            boolean retval = false;
            if (variableName.getInScope() instanceof FunctionScope) {
                FunctionScope scope = (FunctionScope) variableName.getInScope();
                if (variableName.getNameRange().getStart() > scope.getOffset()
                        && variableName.getNameRange().getEnd() < scope.getBlockRange().getStart()) {
                    retval = true;
                }
            }
            return retval;
        }

        private void checkIdentifiers(VariableName variableName, Map<String, List<Identifier>> scopeVars) {
            List<Identifier> identifiers = scopeVars.get(getPureName(variableName));
            if (identifiers != null && identifiers.size() > 1) {
                scopeVars.remove(getPureName(variableName));
            } else {
                hints.add(createHint(variableName));
            }
        }

        private String getPureName(VariableName variableName) {
            return variableName.getName().substring(1);
        }

        @Messages("UnusedVariableHintCustom=Variable ${0} does not seem to be used in its scope")
        private Hint createHint(VariableName variableName) {
            String varName = getPureName(variableName);
            int start = variableName.getNameRange().getStart();
            int end = start + varName.length();
            OffsetRange offsetRange = new OffsetRange(start, end);
            Hint hint = new Hint(UnusedVariableHint.this, Bundle.UnusedVariableHintCustom(varName), fileObject, offsetRange, null, 500);
            return hint;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("UnusedVariableHintDesc=Variable does not seem to be used in its scope")
    public String getDescription() {
        return Bundle.UnusedVariableHintDesc();
    }

    @Override
    @Messages("UnusedVariableHintDispName=Variable does not seem to be used in its scope")
    public String getDisplayName() {
        return Bundle.UnusedVariableHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @CheckForNull
    static Identifier getIdentifier(ASTNode node) {
        Variable variable = null;
        Identifier retval = null;
        if (node instanceof FormalParameter) {
            FormalParameter formalParameter = (FormalParameter) node;
            if (formalParameter.getParameterName() instanceof Variable) {
                variable = (Variable) formalParameter.getParameterName();
            } else if (formalParameter.getParameterName() instanceof Reference) {
                Reference reference = (Reference) formalParameter.getParameterName();
                if (reference.getExpression() instanceof Variable) {
                    variable = (Variable) reference.getExpression();
                }
            }
        } else if (node instanceof Variable) {
            variable = (Variable) node;
        }
        if (variable != null && variable.isDollared()) {
            if (variable.getName() instanceof Identifier) {
                retval = (Identifier) variable.getName();
            }
        }
        return retval;
    }

}
