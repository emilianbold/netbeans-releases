/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.nav.SemiAttribute;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedType;
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
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratingBracketCompleter {

    static void generateDocTags(final BaseDocument doc, final int offset, final int indent) {
        FileObject file = NavUtils.getFile(doc);
        
        if (file == null) {
            return ;
        }
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final ParserResult parameter = (ParserResult) resultIterator.getParserResult();
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
                        new DefaultVisitor() {

                            @Override
                            public void scan(ASTNode node) {
                                if (node != null) {
                                    Comment c = Utils.getCommentForNode(Utils.getRoot(parameter), node);

                                    if (c != null && c.getStartOffset() <= offset && offset <= c.getEndOffset()) {
                                        //found:
                                        throw new Result(node);
                                    }
                                }
                                super.scan(node);
                            }
                        }.scan(Utils.getRoot(parameter));
                    } catch (Result r) {
                        n = r.node;
                    }

                    if (n == null) {
                        //no found
                        return;
                    }

                    if (n instanceof FunctionDeclaration) {
                        generateFunctionDoc(doc, offset, indent, parameter, (FunctionDeclaration) n);
                    }

                    if (n instanceof MethodDeclaration) {
                        generateFunctionDoc(doc, offset, indent, parameter, ((MethodDeclaration) n).getFunction());
                    }

                    if (n instanceof ExpressionStatement && ((ExpressionStatement) n).getExpression() instanceof Assignment) {
                        Assignment a = (Assignment) ((ExpressionStatement) n).getExpression();

                        if (a.getLeftHandSide() instanceof ArrayAccess) {
                            AttributedElement el = SemiAttribute.semiAttribute(parameter).getElement(
                                    a.getLeftHandSide());

                            if (el != null && el.getKind() == Kind.VARIABLE) {
                                generateVariableDoc(doc, offset, indent, parameter, el);
                            }
                        }
                    }

                    if (n instanceof FieldsDeclaration) {
                        generateFieldDoc(doc, offset, indent, parameter, (FieldsDeclaration) n);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    static final String TYPE_PLACEHOLDER = "<type>";
    
    private static void generateFunctionDoc(BaseDocument doc, int offset, int indent, ParserResult info, FunctionDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        
        ScannerImpl i = new ScannerImpl(info, decl);
        
        i.scan(decl);
        
        addVariables(doc, toAdd, "@global", indent, i.globals);
        addVariables(doc, toAdd, "@staticvar", indent, i.staticvars);
        
        for (final FormalParameter p : decl.getFormalParameters()) {
            String name = "";
            Expression expr = p.getParameterName();
            Variable var = null;
            if (expr instanceof Variable) {
                var = (Variable) expr;
            }
            if (expr instanceof Reference) {
                Reference ref = (Reference)expr;
                if (ref.getExpression() instanceof Variable) {
                    var = (Variable) ref.getExpression();
                }
            }
            if (var != null && var.getName() instanceof Identifier) {
                name = ((Identifier) var.getName()).getName();
            }
            AttributedType type = null;
            if (p.getParameterType() != null) {
                final Expression paramIdentifier = p.getParameterType();
                if (paramIdentifier != null) {
                    type = new AttributedType() {
                        @Override
                        public String getTypeName() {
                            return CodeUtils.extractUnqualifiedTypeName(p);
                        }
                    };
                }
            }
            generateDocEntry(doc, toAdd, "@param", indent, "$" + name, type);
        }
        
        if (i.hasReturn) {
            generateDocEntry(doc, toAdd, "@return", indent, null, i.returnType);
        }
        
        doc.insertString(offset - 1, toAdd.toString(), null);
    }
    
    private static void addVariables(BaseDocument doc, StringBuilder toAdd, String text, int indent, List<Pair<AttributedElement, AttributedType>> vars) {
        for (Pair<AttributedElement, AttributedType> p : vars) {
            generateDocEntry(doc, toAdd, text,indent, "$" + p.getA().getName(), p.getB());
        }
    }
    
    private static final AttributedType PRINT_NO_TYPE = new AttributedType() {
        @Override
        public String getTypeName() {
            return null;
        }
    };
    
    private static void generateDocEntry(BaseDocument doc, StringBuilder toAdd, String text, int indent, String name, AttributedType type) {
        toAdd.append("\n");
        toAdd.append(IndentUtils.createIndentString(doc, indent));

        toAdd.append(" * ");
        toAdd.append(text);
        if (type != null) {
            if (type != PRINT_NO_TYPE) {
                toAdd.append(" ");
                toAdd.append(type.getTypeName());
            }
        } else {
            toAdd.append(" ");
            toAdd.append(TYPE_PLACEHOLDER);
        }
        if (name != null) {
            toAdd.append(" ");
            toAdd.append(name);
        }
    }
    
    private static void generateVariableDoc(BaseDocument doc, int offset, int indent, ParserResult info, AttributedElement el) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        generateDocEntry(doc, toAdd, "@global", indent, "$GLOBALS['" + el.getName() + "']", null);
        generateDocEntry(doc, toAdd, "@name", indent, "$" + el.getName(), PRINT_NO_TYPE);

        doc.insertString(offset - 1, toAdd.toString(), null);
    }
    
    private static void generateFieldDoc(BaseDocument doc, int offset, int indent, ParserResult info, FieldsDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        
        generateDocEntry(doc, toAdd, "@var", indent, null, null);
        
        doc.insertString(offset - 1, toAdd.toString(), null);
    }
    
    private static class ScannerImpl extends DefaultVisitor {
        private List<Pair<AttributedElement, AttributedType>> globals = new LinkedList<Pair<AttributedElement, AttributedType>>();
        private List<Pair<AttributedElement, AttributedType>> staticvars = new LinkedList<Pair<AttributedElement, AttributedType>>();
        private boolean hasReturn;
        private AttributedType returnType;
        private SemiAttribute sa;
        private FunctionDeclaration decl;

        public ScannerImpl(ParserResult info, FunctionDeclaration decl) {
            sa = SemiAttribute.semiAttribute(info);
            this.decl = decl;
        }

        @Override
        public void visit(GlobalStatement node) {
            for (Variable v : node.getVariables()) {
                handleVariable(v, globals);
            }
            
            super.visit(node);
        }

        @Override
        public void visit(ReturnStatement node) {
            hasReturn = true;
            Expression expression = node.getExpression();
            if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation instanceCreation = (ClassInstanceCreation) expression;
                final String clsname = CodeUtils.extractClassName(instanceCreation.getClassName());
                if (clsname != null) {
                    //TODO: resolve type (should be recorded in SemiAttribute)
                    returnType = new AttributedType() {
                        @Override
                        public String getTypeName() {
                            return clsname;
                        }
                    };
                }
            }

        }

        @Override
        public void visit(StaticStatement node) {
            for (Variable v : node.getVariables()) {
                handleVariable(v, staticvars);
            }
            
            super.visit(node);
        }

        private void handleVariable(Variable v, List<Pair<AttributedElement, AttributedType>> vars) {
            AttributedElement e = sa.getElement(v);

            if (e != null) {
                //TODO: types
                vars.add(new Pair<AttributedElement, AttributedType>(e, null));
            }
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
