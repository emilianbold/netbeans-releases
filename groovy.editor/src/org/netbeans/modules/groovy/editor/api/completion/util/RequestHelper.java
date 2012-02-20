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

package org.netbeans.modules.groovy.editor.api.completion.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyTypeAnalyzer;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 * Utility class which provides various methods related to CompletionRequest.
 *
 * @author Martin Janicek
 */
public final class RequestHelper {

    protected static final Logger LOG = Logger.getLogger(RequestHelper.class.getName());

    private RequestHelper() {
    }

    
    /**
     * Returns the next enclosing ClassNode for the given request
     * 
     * @param request completion request which includes position information
     * @return the next surrounding ClassNode
     */
    public static ClassNode getSurroundingClassNode(CompletionRequest request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ClassNode) {
                ClassNode classNode = (ClassNode) current;
                LOG.log(Level.FINEST, "Found surrounding Class: {0}", classNode.getName()); // NOI18N
                return classNode;
            }
        }
        return null;
    }

    /**
     * Returns all declared <code>ClassNode</code>'s for the given request
     *
     * @param request completion request
     * @return list of declared <code>ClassNode</code>'s
     */
    public static List<ClassNode> getDeclaredClasses(CompletionRequest request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();

            if (current instanceof ModuleNode) {
                return ((ModuleNode) current).getClasses();
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns the next enclosing MethodNode for the given request
     * 
     * @param request completion request which includes position information
     * @return the next surrounding MethodNode
     */
    public static ASTNode getSurroundingMethodOrClosure(CompletionRequest request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "getSurroundingMethodOrClosure() ----------------------------------------");
        LOG.log(Level.FINEST, "Path : {0}", request.path);

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof MethodNode) {
                MethodNode mn = (MethodNode) current;
                LOG.log(Level.FINEST, "Found Method: {0}", mn.getName()); // NOI18N
                return mn;
            } else if (current instanceof FieldNode) {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable()) {
                    LOG.log(Level.FINEST, "Found Closure(Field): {0}", fn.getName()); // NOI18N
                    return fn;
                }
            } else if (current instanceof ClosureExpression) {
                LOG.log(Level.FINEST, "Found Closure(Expr.): {0}", ((ClosureExpression) current).getText()); // NOI18N
                return current;
            }
        }
        return null;
    }

    /**
     * Get the ClassNode for the before-dot expression. This is important for
     * field and method completion.
     * <p>
     * If the <code>request.declaringClass</code> is not <code>null</code>
     * this value is immediately returned.
     * <p>
     * Returned value is stored to <code>request.declaringClass</code> too.
     *
     * Here are some sample paths:
     *
     * new String().
     * [ModuleNode:ConstructorCallExpression:ExpressionStatement:ConstructorCallExpression:]
     *
     * new String().[caret] something_unrelated
     * [ModuleNode:ClassNode:MethodCallExpression]
     * for this case we have to go for object expression of the method call
     *
     * s.
     * [ModuleNode:VariableExpression:ExpressionStatement:VariableExpression:]
     *
     * s.spli
     * [ModuleNode:PropertyExpression:ConstantExpression:ExpressionStatement:PropertyExpression:ConstantExpression:]
     *
     * l.
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:VariableExpression:]
     *
     * l.ab
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression:ConstantExpression:]
     *
     * l.M
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression:VariableExpression:ConstantExpression:]
     *
     * @param request
     * @return a valid ASTNode or null
     */
    public static ClassNode getBeforeDotDeclaringClass(CompletionRequest request) {

        assert request.isBehindDot() || request.ctx.before1 == null;

        if (request.declaringClass != null && request.declaringClass instanceof ClassNode) {
            LOG.log(Level.FINEST, "returning declaringClass from request."); // NOI18N
            return request.declaringClass;
        }

        // FIXME move this up
        DotCompletionContext dotCompletionContext = getDotCompletionContext(request);

        // FIXME static/script context...
        if (!request.isBehindDot() && request.ctx.before1 == null
                && (request.location == CaretLocation.INSIDE_CLOSURE || request.location == CaretLocation.INSIDE_METHOD)) {
            request.declaringClass = getSurroundingClassNode(request);
            return request.declaringClass;
        }

        if (dotCompletionContext == null || dotCompletionContext.getAstPath() == null
                || dotCompletionContext.getAstPath().leaf() == null) {
            return null;
        }

        request.beforeDotPath = dotCompletionContext.getAstPath();

        ClassNode declClass = null;

        // experimental type inference
        GroovyTypeAnalyzer typeAnalyzer = new GroovyTypeAnalyzer(request.doc);
        Set<ClassNode> infered = typeAnalyzer.getTypes(dotCompletionContext.getAstPath(),
                dotCompletionContext.getAstOffset());
        // FIXME multiple types
        // FIXME is there any test (?)
        if (!infered.isEmpty()) {
            return infered.iterator().next();
        }

        // type inferred
        if (declClass != null) {
            request.declaringClass = declClass;
            return request.declaringClass;
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof VariableExpression) {
            VariableExpression variable = (VariableExpression) dotCompletionContext.getAstPath().leaf();
            if ("this".equals(variable.getName())) { // NOI18N
                request.declaringClass = getSurroundingClassNode(request);
                return request.declaringClass;
            }
            if ("super".equals(variable.getName())) { // NOI18N
                ClassNode thisClass = getSurroundingClassNode(request);
                request.declaringClass = thisClass.getSuperClass();
                if (request.declaringClass == null) {
                    return new ClassNode("java.lang.Object", ClassNode.ACC_PUBLIC, null);
                }
                return request.declaringClass;
            }
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof Expression) {
            Expression expression = (Expression) dotCompletionContext.getAstPath().leaf();

            // see http://jira.codehaus.org/browse/GROOVY-3050
            if (expression instanceof RangeExpression
                    && "java.lang.Object".equals(expression.getType().getName())) { // NOI18N
                try {
                    expression.setType(
                            new ClassNode(Class.forName("groovy.lang.Range"))); // NOI18N
                } catch (ClassNotFoundException ex) {
                    expression.setType(
                            new ClassNode("groovy.lang.Range", ClassNode.ACC_PUBLIC | ClassNode.ACC_INTERFACE, null)); // NOI18N
                }
            // FIXME report issue
            } else if (expression instanceof ConstantExpression) {
                ConstantExpression constantExpression = (ConstantExpression) expression;
                if (!constantExpression.isNullExpression()) {
                    constantExpression.setType(new ClassNode(constantExpression.getValue().getClass()));
                }
            }
            request.declaringClass = expression.getType();
        }

        return request.declaringClass;
    }

    /**
     * Figure-out, where we are in the code (comment, CU, class, method, etc.).
     * 
     * @param request compilation request
     * @return concrete caret location type
     */
    public static CaretLocation getCaretLocationFromRequest(final CompletionRequest request) {


        int position = request.lexOffset;
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);

        // are we living inside a comment?

        ts.move(position);

        if (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();

            if (t.id() == GroovyTokenId.LINE_COMMENT || t.id() == GroovyTokenId.BLOCK_COMMENT) {
                return CaretLocation.INSIDE_COMMENT;
            }

            if (t.id() == GroovyTokenId.STRING_LITERAL) {
                return CaretLocation.INSIDE_STRING;
            }
            // This is a special case. If we have a NLS right behind a LINE_COMMENT it
            // should be treated as a CaretLocation.INSIDE_COMMENT. Therefore we have to rewind.

            if (t.id() == GroovyTokenId.NLS) {
                if ((ts.isValid() && ts.movePrevious() && ts.offset() >= 0)) {
                    Token<? extends GroovyTokenId> tparent = (Token<? extends GroovyTokenId>) ts.token();
                    if (tparent.id() == GroovyTokenId.LINE_COMMENT) {
                        return CaretLocation.INSIDE_COMMENT;
                    }
                }
            }
        }


        // Are we above the package statement?
        // We try to figure this out by moving down the lexer Stream

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();

            if (t.id() == GroovyTokenId.LITERAL_package) {
                return CaretLocation.ABOVE_PACKAGE;
            }
        }

        // Are we before the first class or interface statement?
        // now were heading to the beginning to the document ...

        boolean classDefBeforePosition = false;

        ts.move(position);

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_interface) {
                classDefBeforePosition = true;
                break;
            }
        }


        boolean classDefAfterPosition = false;

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_interface) {
                classDefAfterPosition = true;
                break;
            }
        }

        if (request.path != null) {
            ASTNode node = request.path.root();
            if (node instanceof ModuleNode) {
                ModuleNode module = (ModuleNode) node;
                String name = null;
                for (Iterator it = module.getClasses().iterator(); it.hasNext();) {
                    ClassNode clazz = (ClassNode) it.next();
                    if (clazz.isScript()) {
                        name = clazz.getName();
                        request.scriptMode = true;
                        break;
                    }
                }

                // we have a script class - lets see if there is another
                // non-script class with same name that would mean we are just
                // broken class, not a script
                if (name != null) {
                    for (Iterator it = module.getClasses().iterator(); it.hasNext();) {
                        ClassNode clazz = (ClassNode) it.next();
                        if (!clazz.isScript() && name.equals(clazz.getName())) {
                            request.scriptMode = false;
                            break;
                        }
                    }
                }
            }
        }

        if (!request.scriptMode && !classDefBeforePosition && classDefAfterPosition) {
            return CaretLocation.ABOVE_FIRST_CLASS;
        }

        // If there's *no* class definition in the file we are running in a
        // script with synthetic wrapper class and wrapper method: run().
        // See GINA, ch. 7

        if (!classDefBeforePosition && request.scriptMode) {
            return CaretLocation.INSIDE_METHOD;
        }


        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }



        /* here we loop from the tail of the path (innermost element)
        up to the root to figure out where we are. Some of the trails are:

        In main method:
        Path(4)=[ModuleNode:ClassNode:MethodNode:ConstantExpression:]

        In closure, which sits in a method:
        Path(7)=[ModuleNode:ClassNode:MethodNode:DeclarationExpression:DeclarationExpression:VariableExpression:ClosureExpression:]

        In closure directly attached to class:
        Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]

        In a class, outside method, right behind field declaration:
        Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]

        Right after a class declaration:
        Path(2)=[ModuleNode:ClassNode:]

        Inbetween two classes:
        [ModuleNode:ConstantExpression:]

        Outside of any class:
        Path(1)=[ModuleNode:]

        Start of Parameter-list:
        Path(4)=[ModuleNode:ClassNode:MethodNode:Parameter:]

         */

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ClosureExpression) {
                return CaretLocation.INSIDE_CLOSURE;
            } else if (current instanceof FieldNode) {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable()) {
                    return CaretLocation.INSIDE_CLOSURE;
                }
            } else if (current instanceof MethodNode) {
                return CaretLocation.INSIDE_METHOD;
            } else if (current instanceof ClassNode) {
                return CaretLocation.INSIDE_CLASS;
            } else if (current instanceof ModuleNode) {
                return CaretLocation.OUTSIDE_CLASSES;
            } else if (current instanceof Parameter) {
                return CaretLocation.INSIDE_PARAMETERS;
            }
        }
        return CaretLocation.UNDEFINED;
    }

    /**
     * Here we test, whether the provided CompletionContext is likely to become
     * a variable definition. At this point in time we can not check whether we
     * live in a "DeclarationExpression" since this is not yet created.
     *
     * We have basically three cases:
     *
     * 1.) "def" - keyword in front, then it's a definition but we can not propose a varname
     * 2.) "int, char, long, ..." primitive type. It's a definition and we propose a single char
     * 3.) Lexer token IDENTIFIER: Then we have to decide wheter it's a type or a method:
     *     For example it could be:
     *     println variable
     *     StringBuilder variable
     *
     * We have to check for:
     *
     * a) Methods
     * b) closures
     *
     * todo: figuring out whether the IDENTIFIER is a method or a type.
     * @param ctx
     * @return true if we are on variable definition line, false otherwise
     */
    public static boolean isVariableDefinitionLine(CompletionRequest request) {
        LOG.log(Level.FINEST, "checkForVariableDefinition()"); //NOI18N
        CompletionContext ctx = request.ctx;

        if (ctx == null || ctx.before1 == null) {
            return false;
        }

        GroovyTokenId id = ctx.before1.id();

        switch (id) {
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
            case LITERAL_def:
                LOG.log(Level.FINEST, "LITERAL_* discovered"); //NOI18N
                return true;
            case IDENTIFIER:
                // now comes the tricky part, i have to figure out
                // whether I'm dealing with a ClassExpression here.
                // Otherwise it's a call which will or won't succeed.
                // But this could only be figured at runtime.
                ASTNode node = getASTNodeForToken(ctx.before1, request);
                LOG.log(Level.FINEST, "getASTNodeForToken(ASTNode) : {0}", node); //NOI18N

                if (node != null && (node instanceof ClassExpression || node instanceof DeclarationExpression)) {
                    LOG.log(Level.FINEST, "ClassExpression or DeclarationExpression discovered"); //NOI18N
                    return true;
                }

                return false;
            default:
                LOG.log(Level.FINEST, "default:"); //NOI18N
                return false;
        }
    }

    public static boolean isFieldDefinitionLine(CompletionRequest request) {
        LOG.log(Level.FINEST, "isFieldDefinitionLine()"); //NOI18N
        CompletionContext ctx = request.ctx;

        if (ctx == null || ctx.before1 == null) {
            return false;
        }

        ASTNode node = getASTNodeForToken(ctx.before1, request);
        if (node != null && (node instanceof PropertyNode || node instanceof ClassNode)) {
            return true;
        }
        return false;
    }

    private static ASTNode getASTNodeForToken(Token<? extends GroovyTokenId> tid, CompletionRequest request) {
        LOG.log(Level.FINEST, "getASTNodeForToken()"); //NOI18N
        TokenHierarchy<Document> th = TokenHierarchy.get((Document) request.doc);
        int position = tid.offset(th);

        ModuleNode rootNode = AstUtilities.getRoot(request.info);
        if (rootNode == null) {
            return null;
        }
        int astOffset = AstUtilities.getAstOffset(request.info, position);
        if (astOffset == -1) {
            return null;
        }

        BaseDocument document = (BaseDocument) request.info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return null;
        }

        final AstPath path = new AstPath(rootNode, astOffset, document);
        final ASTNode node = path.leaf();

        LOG.log(Level.FINEST, "path = {0}", path); //NOI18N
        LOG.log(Level.FINEST, "node: {0}", node); //NOI18N

        return node;
    }

    /**
     * Check whether this completion request was issued behind an import statement.
     * 
     * @param request completion request
     * @return true if it's right behind and import statement, false otherwise
     */
    public static boolean checkForRequestBehindImportStatement(final CompletionRequest request) {

        int rowStart = 0;
        int nonWhite = 0;

        try {
            rowStart = org.netbeans.editor.Utilities.getRowStart(request.doc, request.lexOffset);
            nonWhite = org.netbeans.editor.Utilities.getFirstNonWhiteFwd(request.doc, rowStart);

        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "Trouble doing getRowStart() or getFirstNonWhiteFwd(): {0}", ex.getMessage());
        }

        Token<? extends GroovyTokenId> importToken = LexUtilities.getToken(request.doc, nonWhite);

        if (importToken != null && importToken.id() == GroovyTokenId.LITERAL_import) {
            LOG.log(Level.FINEST, "Right behind an import statement");
            return true;
        }

        return false;
    }

    /**
     * Computes an CompletionContext which surrounds the request.
     * Three tokens in front and three after the request.
     *
     * @param request completion request
     * @return completion context
     */
    public static CompletionContext getCompletionContext(final CompletionRequest request) {
        int position = request.lexOffset;

        Token<? extends GroovyTokenId> beforeLiteral = null;
        Token<? extends GroovyTokenId> before2 = null;
        Token<? extends GroovyTokenId> before1 = null;
        Token<? extends GroovyTokenId> active = null;
        Token<? extends GroovyTokenId> after1 = null;
        Token<? extends GroovyTokenId> after2 = null;
        Token<? extends GroovyTokenId> afterLiteral = null;

        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);

        int difference = ts.move(position);

        // get the active token:

        if (ts.isValid() && ts.moveNext() && ts.offset() >= 0) {
            active = (Token<? extends GroovyTokenId>) ts.token();
        }

        // if we are right at the end of a line, a separator or a whitespace we gotta rewind.

        // 1.) NO  str.^<NLS>
        // 2.) NO  str.^subString
        // 3.) NO  str.sub^String
        // 4.) YES str.subString^<WHITESPACE-HERE>
        // 5.) YES str.subString^<NLS>
        // 6.) YES str.subString^()


        if (active != null) {
            if ((active.id() == GroovyTokenId.WHITESPACE && difference == 0)
                    /*|| active.id().primaryCategory().equals("separator")*/) {
                LOG.log(Level.FINEST, "ts.movePrevious() - 1");
                ts.movePrevious();
            } else if (active.id() == GroovyTokenId.NLS ) {
                ts.movePrevious();
                if(((Token<? extends GroovyTokenId>) ts.token()).id() == GroovyTokenId.DOT) {
                    ts.moveNext();
                } else {
                    LOG.log(Level.FINEST, "ts.movePrevious() - 2");
                }
            }
        }


        // Travel to the beginning to get before2 and before1

        int stopAt = 0;

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.NLS) {
                break;
            } else if (t.id() != GroovyTokenId.WHITESPACE) {
                if (stopAt == 0) {
                    before1 = t;
                } else if (stopAt == 1) {
                    before2 = t;
                } else if (stopAt == 2) {
                    break;
                }

                stopAt++;
            }
        }

        // Move to the beginning (again) to get the next left-hand-sight literal

        ts.move(position);

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.NLS ||
                t.id() == GroovyTokenId.LBRACE) {
                break;
            } else if (t.id().primaryCategory().equals("keyword")) {
                beforeLiteral = t;
                break;
            }
        }

        // now looking for the next right-hand-sight literal in the opposite direction

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.NLS ||
                t.id() == GroovyTokenId.RBRACE) {
                break;
            } else if (t.id().primaryCategory().equals("keyword")) {
                afterLiteral = t;
                break;
            }
        }


        // Now we're heading to the end of that stream

        ts.move(position);
        stopAt = 0;

        while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();

            if (t.id() == GroovyTokenId.NLS) {
                break;
            } else if (t.id() != GroovyTokenId.WHITESPACE) {
                if (stopAt == 0) {
                    after1 = t;
                } else if (stopAt == 1) {
                    after2 = t;
                } else if (stopAt == 2) {
                    break;
                }
                stopAt++;
            }
        }

        if (false) {
            // Display the line where completion was invoked to ease debugging

            String line = "";
            String marker = "";

            try {
                int lineStart = org.netbeans.editor.Utilities.getRowStart(request.doc, request.lexOffset);
                int lineStop = org.netbeans.editor.Utilities.getRowEnd(request.doc, request.lexOffset);
                int lineLength = request.lexOffset - lineStart;

                line = request.doc.getText(lineStart, lineStop - lineStart);

                StringBuilder sb = new StringBuilder();

                while (lineLength > 0) {
                    sb.append(" ");
                    lineLength--;
                }

                sb.append("|");

                marker = sb.toString();


            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            LOG.log(Level.FINEST, "---------------------------------------------------------------");
            LOG.log(Level.FINEST, "Prefix : {0}", request.prefix);
            LOG.log(Level.FINEST, "Line   : {0}", marker);
            LOG.log(Level.FINEST, "Line   : {0}", line);
        }

        LOG.log(Level.FINEST, "---------------------------------------------------------------");
        LOG.log(Level.FINEST, "move() diff   : {0}", difference);
        LOG.log(Level.FINEST, "beforeLiteral : {0}", beforeLiteral);
        LOG.log(Level.FINEST, "before2       : {0}", before2);
        LOG.log(Level.FINEST, "before1       : {0}", before1);
        LOG.log(Level.FINEST, "active        : {0}", active);
        LOG.log(Level.FINEST, "after1        : {0}", after1);
        LOG.log(Level.FINEST, "after2        : {0}", after2);
        LOG.log(Level.FINEST, "afterLiteral  : {0}", afterLiteral);

        return new CompletionContext(beforeLiteral, before2, before1, active, after1, after2, afterLiteral, ts);
    }

    public static DotCompletionContext getDotCompletionContext(final CompletionRequest request) {
        if (request.dotContext != null) {
            return request.dotContext;
        }

        int position = request.lexOffset;

        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);

        int difference = ts.move(position);

        // get the active token:
        Token<? extends GroovyTokenId> active = null;
        if (ts.isValid() && ts.moveNext() && ts.offset() >= 0) {
            active = (Token<? extends GroovyTokenId>) ts.token();
        }

        if (LOG.isLoggable(Level.FINE)) {
            if (ts.isValid() && active != null) {
                LOG.log(Level.FINE, "Current token text {0}", active.text());
            }
        }

        // this should move us to dot or whitespace or NLS or prefix
        if (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();


            if (t.id() != GroovyTokenId.DOT && t.id() != GroovyTokenId.OPTIONAL_DOT && t.id() != GroovyTokenId.MEMBER_POINTER
                    && t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                // is it prefix
                // keyword check is here because of issue #150862
                if (t.id() != GroovyTokenId.IDENTIFIER && !t.id().primaryCategory().equals("keyword")) {
                    return null;
                } else {
                    ts.movePrevious();
                }
            }
        }

        // now we should be on dot or in whitespace or NLS after the dot
        boolean remainingTokens = true;
        if (ts.token().id() != GroovyTokenId.DOT && ts.token().id() != GroovyTokenId.OPTIONAL_DOT && ts.token().id() != GroovyTokenId.MEMBER_POINTER) {

            // travel back on the token string till the token is neither a
            // WHITESPACE nor NLS
            while (ts.isValid() && (remainingTokens = ts.movePrevious()) && ts.offset() >= 0) {
                Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
                if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                    break;
                }
            }
        }

        if ((ts.token().id() != GroovyTokenId.DOT && ts.token().id() != GroovyTokenId.OPTIONAL_DOT && ts.token().id() != GroovyTokenId.MEMBER_POINTER)
                || !remainingTokens) {
            // no astpath
            return null;
        }

        boolean methodsOnly = false;
        if (ts.token().id() == GroovyTokenId.MEMBER_POINTER) {
            methodsOnly = true;
        }

        // travel back on the token string till the token is neither a
        // WHITESPACE nor NLS
        Token<? extends GroovyTokenId> t = null;
        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                break;
            }
        }

        int lexOffset = ts.offset();
        int astOffset = AstUtilities.getAstOffset(request.info, lexOffset);
        AstPath realPath = getPath(request.info, request.doc, astOffset);

        return new DotCompletionContext(lexOffset, astOffset, realPath, methodsOnly);
    }

    private static AstPath getPath(ParserResult info, BaseDocument doc, int astOffset) {
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(info);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317
        if (root == null) {
            LOG.log(Level.FINEST, "AstUtilities.getRoot(request.info) returned null."); // NOI18N
            LOG.log(Level.FINEST, "request.info   = {0}", info); // NOI18N

            return null;
        }
        return new AstPath(root, astOffset, doc);
    }

    /**
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     *
     * @param request
     * @return a freshly created AstPath object for the offset given in the request
     */
    public static AstPath getPathFromRequest(final CompletionRequest request) {
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(request.info);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317
        if (root == null) {
            LOG.log(Level.FINEST, "AstUtilities.getRoot(request.info) returned null."); // NOI18N
            LOG.log(Level.FINEST, "request.info   = {0}", request.info); // NOI18N
            LOG.log(Level.FINEST, "request.prefix = {0}", request.prefix); // NOI18N

            return null;
        }

        return new AstPath(root, request.astOffset, request.doc);
    }
}
