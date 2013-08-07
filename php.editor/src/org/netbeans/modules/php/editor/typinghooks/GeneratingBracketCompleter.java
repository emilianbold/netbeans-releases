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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.typinghooks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public final class GeneratingBracketCompleter {

    private static final RequestProcessor RP = new RequestProcessor("Generating Bracket Completer"); //NOI18N

    static final String TYPE_PLACEHOLDER = "type";

    private GeneratingBracketCompleter() {
    }

    static void generateDocTags(final BaseDocument doc, final int offset, final int indent) {
        Runnable docTagsGenerator = new DocTagsGenerator(doc, offset, indent);
        RP.post(docTagsGenerator);
    }

    private static void generateFunctionDoc(BaseDocument doc, int offset, int indent, ParserResult info, FunctionDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        ScannerImpl i = new ScannerImpl(info, decl);

        i.scan(decl);

        addVariables(doc, toAdd, "@global", indent, i.globals);
        addVariables(doc, toAdd, "@staticvar", indent, i.staticvars);
        addVariables(doc, toAdd, "@param", indent, i.params);

        if (i.hasReturn) {
            generateDocEntry(doc, toAdd, "@return", indent, null, i.returnType);
        }

        addVariables(doc, toAdd, "@throws", indent, i.throwsExceptions);

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static void addVariables(BaseDocument doc, StringBuilder toAdd, String text, int indent, List<Pair<String, String>> vars) {
        for (Pair<String, String> p : vars) {
            generateDocEntry(doc, toAdd, text, indent, p.getA(), p.getB());
        }
    }

    private static void generateDocEntry(BaseDocument doc, StringBuilder toAdd, String text, int indent, String name, String type) {
        toAdd.append("\n");
        toAdd.append(IndentUtils.createIndentString(doc, indent));

        toAdd.append(" * ");
        toAdd.append(text);
        if (type != null && !type.isEmpty()) {
            toAdd.append(" ");
            toAdd.append(type);
        } else {
            toAdd.append(" ");
            toAdd.append(TYPE_PLACEHOLDER);
        }
        if (name != null) {
            toAdd.append(" ");
            toAdd.append(name);
        }
    }

    private static void generateGlobalVariableDoc(BaseDocument doc, int offset, int indent, String indexName, String type) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        generateDocEntry(doc, toAdd, "@global", indent, "$GLOBALS['" + indexName + "']", type);
        toAdd.append("\n").append(IndentUtils.createIndentString(doc, indent));
        toAdd.append(" * ").append("@name $").append(indexName);

        doc.insertString(offset - 1, toAdd.toString(), null);
    }

    private static void generateFieldDoc(BaseDocument doc, int offset, int indent, ParserResult info, FieldsDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        generateDocEntry(doc, toAdd, "@var", indent, null, null);

        doc.insertString(offset - 1, toAdd.toString(), null);
    }

    private static class ScannerImpl extends DefaultVisitor {
        private List<Pair<String, String>> globals = new LinkedList<>();
        private List<Pair<String, String>> staticvars = new LinkedList<>();
        private List<Pair<String, String>> params = new LinkedList<>();
        private List<Pair<String, String>> throwsExceptions = new LinkedList<>();
        private List<String> usedThrows = new LinkedList<>();
        final Set<VariableName> declaredVariables = new HashSet<>();
        private boolean hasReturn;
        private String returnType;
        private final FunctionDeclaration decl;
        private final FunctionScope fnc;

        public ScannerImpl(ParserResult info, FunctionDeclaration decl) {
            if (info instanceof PHPParseResult) {
                PHPParseResult parseResult = (PHPParseResult) info;
                Model model = parseResult.getModel();
                final VariableScope variableScope = model.getVariableScope(decl.getEndOffset() - 1);
                if (variableScope instanceof FunctionScope) {
                    fnc = (FunctionScope) variableScope;
                    declaredVariables.addAll(fnc.getDeclaredVariables());
                } else {
                    fnc = null;
                }
            } else {
                fnc = null;
            }
            this.decl = decl;
        }

        @Override
        public void scan(ASTNode node) {
            if (fnc != null) {
                super.scan(node);
            }
        }

        @Override
        public void visit(final FormalParameter p) {
            String name = "";
            Expression expr = p.getParameterName();
            Variable var = null;
            if (expr instanceof Variable) {
                var = (Variable) expr;
            }
            if (expr instanceof Reference) {
                Reference ref = (Reference) expr;
                if (ref.getExpression() instanceof Variable) {
                    var = (Variable) ref.getExpression();
                }
            }
            if (var != null && var.getName() instanceof Identifier) {
                name = ((Identifier) var.getName()).getName();
            }
            if (name != null) {
                for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                    final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                    String type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                    if (VariousUtils.isSemiType(type)) {
                        type = null;
                    }
                    params.add(new Pair<>(variable.getName(), type));
                }
            }
            super.visit(p);
        }



        @Override
        public void visit(GlobalStatement node) {
            for (Variable v : node.getVariables()) {
                final String name = CodeUtils.extractVariableName(v);
                if (name != null) {
                    for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                        final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                        String type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                        if (VariousUtils.isSemiType(type)) {
                            type = null;
                        }
                        globals.add(new Pair<>(variable.getName(), type));
                    }
                }
            }

            super.visit(node);
        }

        @Override
        public void visit(ReturnStatement node) {
            hasReturn = true;
            Collection<? extends String> typeNames = fnc.getReturnTypeNames();
            StringBuilder type = new StringBuilder();
            String item;
            for (Iterator<String> i = (Iterator<String>) typeNames.iterator(); i.hasNext();) {
                item = i.next();
                if (VariousUtils.isSemiType(item)) {
                    break;
                }
                type = type.toString().isEmpty() ? type.append(item) : type.append("|").append(item); //NOI18N
            }
            returnType = type.toString();
        }

        @Override
        public void visit(StaticStatement node) {
            for (Variable v : node.getVariables()) {
                final String name = CodeUtils.extractVariableName(v);
                if (name != null) {
                    for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                        final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                        String type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                        if (VariousUtils.isSemiType(type)) {
                            type = null;
                        }
                        staticvars.add(new Pair<>(variable.getName(), type));
                    }
                }
            }

            super.visit(node);
        }

        @Override
        public void visit(ThrowStatement node) {
            String type = getTypeFromThrowStatement(node);
            if (!usedThrows.contains(type)) {
                usedThrows.add(type);
                throwsExceptions.add(new Pair<String, String>(null, type));
            }
            super.visit(node);
        }

        private String getTypeFromThrowStatement(ThrowStatement throwStatement) {
            String type = null;
            Expression expression = throwStatement.getExpression();
            if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
                Expression name = classInstanceCreation.getClassName().getName();
                if (name instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) name;
                    type = getTypeFromNamespaceName(namespaceName);
                }
            } else if (expression instanceof Variable) {
                Variable v = (Variable) expression;
                final String name = CodeUtils.extractVariableName(v);
                for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                    final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                    type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                }
            }
            return type;
        }

        private String getTypeFromNamespaceName(NamespaceName namespaceName) {
            StringBuilder sbType = new StringBuilder();
            if (namespaceName.isGlobal()) {
                sbType.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
            }
            List<Identifier> segments = namespaceName.getSegments();
            for (Iterator<Identifier> iter =  segments.iterator(); iter.hasNext();) {
                sbType.append(iter.next().getName());
                if (iter.hasNext()) {
                    sbType.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
                }
            }
            return sbType.toString();
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (node == decl) {
                super.visit(node);
            }
        }

        @Override
        public void visit(ClassDeclaration node) {
        }

    }

    private static class DocTagsGenerator implements Runnable {
        private final BaseDocument doc;
        private final int offset;
        private final int indent;

        public DocTagsGenerator(final BaseDocument doc, final int offset, final int indent) {
            this.doc = doc;
            this.offset = offset;
            this.indent = indent;
        }

        @Override
        public void run() {
            FileObject file = NavUtils.getFile(doc);
            if (file == null) {
                return;
            }
            try {
                ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        final ParserResult parserResult = (ParserResult) resultIterator.getParserResult();
                        if (parserResult != null) {
                            //find coresponding ASTNode:
                            //TODO: slow and ugly:
                            class Result extends Error {

                                private ASTNode node;

                                public Result(ASTNode node) {
                                    this.node = node;
                                }
                            }
                            ASTNode n = null;
                            try {
                                DefaultVisitor visitor = new DefaultVisitor() {

                                    @Override
                                    public void scan(ASTNode node) {
                                        if (node != null) {
                                            Comment c = Utils.getCommentForNode(Utils.getRoot(parserResult), node);

                                            if (c != null && c.getStartOffset() <= offset && offset <= c.getEndOffset()) {
                                                //found:
                                                throw new Result(node);
                                            }
                                        }
                                        super.scan(node);
                                    }
                                };
                                visitor.scan(Utils.getRoot(parserResult));
                            } catch (Result r) {
                                n = r.node;
                            }

                            if (n == null) {
                                //no found
                                return;
                            }

                            if (n instanceof FunctionDeclaration) {
                                generateFunctionDoc(doc, offset, indent, parserResult, (FunctionDeclaration) n);
                            }

                            if (n instanceof MethodDeclaration) {
                                generateFunctionDoc(doc, offset, indent, parserResult, ((MethodDeclaration) n).getFunction());
                            }

                            if (n instanceof ExpressionStatement) {
                                if (((ExpressionStatement) n).getExpression() instanceof Assignment) {
                                    Assignment assignment = (Assignment) ((ExpressionStatement) n).getExpression();
                                    if (assignment.getLeftHandSide() instanceof ArrayAccess) {
                                        ArrayAccess arrayAccess = (ArrayAccess) assignment.getLeftHandSide();
                                        if (arrayAccess.getName() instanceof Variable) {
                                            Variable variable = (Variable) arrayAccess.getName();
                                            if (variable.isDollared()
                                                    && variable.getName() instanceof Identifier
                                                    && "GLOBALS".equals(((Identifier) variable.getName()).getName())
                                                    && arrayAccess.getDimension().getIndex() instanceof Scalar) {
                                                String index = ((Scalar) arrayAccess.getDimension().getIndex()).getStringValue().trim();
                                                if (index.length() > 0
                                                        && (index.charAt(0) == '\'' || index.charAt(0) == '"')) {
                                                    index = index.substring(1, index.length() - 1);
                                                }
                                                String type = null;
                                                if (assignment.getRightHandSide() instanceof Scalar) {
                                                    switch (((Scalar) assignment.getRightHandSide()).getScalarType()) {
                                                        case INT:
                                                            type = Type.INTEGER;
                                                            break;
                                                        case REAL:
                                                            type = Type.FLOAT;
                                                            break;
                                                        case STRING:
                                                            type = Type.STRING;
                                                            break;
                                                        default:
                                                            //no-op
                                                    }
                                                }
                                                generateGlobalVariableDoc(doc, offset, indent, index, type);
                                            }
                                        }
                                    }
                                }
                            }

                            if (n instanceof FieldsDeclaration) {
                                generateFieldDoc(doc, offset, indent, parserResult, (FieldsDeclaration) n);
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private static final class Pair<A, B> {
        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

    }

}
