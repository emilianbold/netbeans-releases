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

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyTypeAnalyzer;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 * Carry completion context around since this logic is split across lots of methods
 * and I don't want to pass dozens of parameters from method to method; just pass
 * a request context with supporting info needed by the various completion helpers
 *
 * @author Martin Janicek
 */
public class CompletionRequest {

    private static final Logger LOG = Logger.getLogger(CompletionRequest.class.getName());

    public int lexOffset;
    public int astOffset;
    public ParserResult info;
    public BaseDocument doc;
    public String prefix = "";
    
    public boolean scriptMode;
    public boolean behindImport;
    public CaretLocation location;
    public CompletionContext ctx;
    public AstPath path;
    public AstPath beforeDotPath;
    public ClassNode declaringClass;
    public DotCompletionContext dotContext;

    
    public CompletionRequest(int lexOffset, int astOffset, ParserResult info, BaseDocument doc, String prefix) {
        this.lexOffset = lexOffset;
        this.astOffset = astOffset;
        this.info = info;
        this.doc = doc;
        this.prefix = prefix;
        this.scriptMode = false;
    }

    /**
     * Try to initiate the rest of CompilationRequest attributes.
     *
     * This method returns false if it find out that there is nothing that could
     * be proposed by code completion (for example when we are above package
     * declaration or inside a comment), true otherwise
     *
     * @return false if nothing could be proposed, true otherwise
     */
    public boolean initContextAttributes() {
        path = getPathFromRequest();
        LOG.log(Level.FINEST, "complete(...), path        : {0}", path);

        location = getCaretLocationFromRequest();
        LOG.log(Level.FINEST, "I am here in sourcecode: {0}", location); // NOI18N

        // if we are above a package statement or inside a comment there's no completion at all.
        if (location == CaretLocation.ABOVE_PACKAGE || location == CaretLocation.INSIDE_COMMENT) {
            return false;
        }

        // now let's figure whether we are in some sort of definition line
        ctx = getCompletionContext();

        // Are we invoked right behind a dot? This is information is used later on in
        // a couple of completions.
        dotContext = getDotCompletionContext();

        declaringClass = getBeforeDotDeclaringClass();

        // are we're right behind an import statement?
        behindImport = checkForRequestBehindImportStatement();

        return true;
    }

    public boolean isBehindDot() {
        return dotContext != null;
    }

    /**
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     *
     * @return a freshly created AstPath object for the offset given in the request
     */
    private AstPath getPathFromRequest() {
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(info);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317
        if (root == null) {
            LOG.log(Level.FINEST, "AstUtilities.getRoot(request.info) returned null."); // NOI18N
            LOG.log(Level.FINEST, "request.info   = {0}", info); // NOI18N
            LOG.log(Level.FINEST, "request.prefix = {0}", prefix); // NOI18N

            return null;
        }

        return new AstPath(root, astOffset, doc);
    }

    /**
     * Figure-out, where we are in the code (comment, CU, class, method, etc.).
     *
     * @return concrete caret location type
     */
    private CaretLocation getCaretLocationFromRequest() {
        int position = lexOffset;
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(doc, position);

        // are we living inside a comment?

        ts.move(position);

        if (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
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

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
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

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_interface) {
                classDefAfterPosition = true;
                break;
            }
        }

        if (path != null) {
            ASTNode node = path.root();
            if (node instanceof ModuleNode) {
                ModuleNode module = (ModuleNode) node;
                String name = null;
                for (Iterator it = module.getClasses().iterator(); it.hasNext();) {
                    ClassNode clazz = (ClassNode) it.next();
                    if (clazz.isScript()) {
                        name = clazz.getName();
                        scriptMode = true;
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
                            scriptMode = false;
                            break;
                        }
                    }
                }
            }
        }

        if (!scriptMode && !classDefBeforePosition && classDefAfterPosition) {
            return CaretLocation.ABOVE_FIRST_CLASS;
        }

        // If there's *no* class definition in the file we are running in a
        // script with synthetic wrapper class and wrapper method: run().
        // See GINA, ch. 7

        if (!classDefBeforePosition && scriptMode) {
            return CaretLocation.INSIDE_METHOD;
        }


        if (path == null) {
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

        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
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
     * Computes an CompletionContext which surrounds the request.
     * Three tokens in front and three after the request.
     *
     * @return completion context
     */
    private CompletionContext getCompletionContext() {
        int position = lexOffset;

        Token<? extends GroovyTokenId> beforeLiteral = null;
        Token<? extends GroovyTokenId> before2 = null;
        Token<? extends GroovyTokenId> before1 = null;
        Token<? extends GroovyTokenId> active = null;
        Token<? extends GroovyTokenId> after1 = null;
        Token<? extends GroovyTokenId> after2 = null;
        Token<? extends GroovyTokenId> afterLiteral = null;

        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(doc, position);

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

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
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

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
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
                int lineStart = org.netbeans.editor.Utilities.getRowStart(doc, lexOffset);
                int lineStop = org.netbeans.editor.Utilities.getRowEnd(doc, lexOffset);
                int lineLength = lexOffset - lineStart;

                line = doc.getText(lineStart, lineStop - lineStart);

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
            LOG.log(Level.FINEST, "Prefix : {0}", prefix);
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

    private DotCompletionContext getDotCompletionContext() {
        if (dotContext != null) {
            return dotContext;
        }

        int position = lexOffset;

        TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, position);

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

            if (ts.token().id() != GroovyTokenId.DOT &&
                ts.token().id() != GroovyTokenId.NLS &&
                ts.token().id() != GroovyTokenId.WHITESPACE &&
                ts.token().id() != GroovyTokenId.OPTIONAL_DOT &&
                ts.token().id() != GroovyTokenId.MEMBER_POINTER) {

                // is it prefix
                // keyword check is here because of issue #150862
                if (ts.token().id() != GroovyTokenId.IDENTIFIER && !ts.token().id().primaryCategory().equals("keyword")) {
                    return null;
                } else {
                    ts.movePrevious();
                }
            }
        }

        // now we should be on dot or in whitespace or NLS after the dot
        boolean remainingTokens = true;
        if (ts.token().id() != GroovyTokenId.DOT &&
            ts.token().id() != GroovyTokenId.OPTIONAL_DOT &&
            ts.token().id() != GroovyTokenId.MEMBER_POINTER) {

            // travel back on the token string till the token is neither a
            // WHITESPACE nor NLS
            while (ts.isValid() && (remainingTokens = ts.movePrevious()) && ts.offset() >= 0) {
                Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
                if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                    break;
                }
            }
        }

        if ((ts.token().id() != GroovyTokenId.DOT &&
             ts.token().id() != GroovyTokenId.OPTIONAL_DOT &&
             ts.token().id() != GroovyTokenId.MEMBER_POINTER)
            || !remainingTokens) {

            return null; // no astpath
        }

        boolean methodsOnly = false;
        if (ts.token().id() == GroovyTokenId.MEMBER_POINTER) {
            methodsOnly = true;
        }

        // travel back on the token string till the token is neither a
        // WHITESPACE nor NLS
        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                break;
            }
        }

        int lexOffset = ts.offset();
        int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        AstPath realPath = getPath(info, doc, astOffset);

        return new DotCompletionContext(lexOffset, astOffset, realPath, methodsOnly);
    }

    private AstPath getPath(ParserResult info, BaseDocument doc, int astOffset) {
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
     * @return a valid ASTNode or null
     */
    public ClassNode getBeforeDotDeclaringClass() {
       // assert isBehindDot() || ctx.before1 == null;

        if (declaringClass != null && declaringClass instanceof ClassNode) {
            LOG.log(Level.FINEST, "returning declaringClass from request."); // NOI18N
            return declaringClass;
        }

        // FIXME move this up
        DotCompletionContext dotCompletionContext = getDotCompletionContext();

        // FIXME static/script context...
        if (!isBehindDot() && ctx.before1 == null
                && (location == CaretLocation.INSIDE_CLOSURE || location == CaretLocation.INSIDE_METHOD)) {
            declaringClass = ContextHelper.getSurroundingClassNode(this);
            return declaringClass;
        }

        if (dotCompletionContext == null || dotCompletionContext.getAstPath() == null
                || dotCompletionContext.getAstPath().leaf() == null) {
            return null;
        }

        beforeDotPath = dotCompletionContext.getAstPath();

        ClassNode declClass = null;

        // experimental type inference
        GroovyTypeAnalyzer typeAnalyzer = new GroovyTypeAnalyzer(doc);
        Set<ClassNode> infered = typeAnalyzer.getTypes(dotCompletionContext.getAstPath(),
                dotCompletionContext.getAstOffset());
        // FIXME multiple types
        // FIXME is there any test (?)
        if (!infered.isEmpty()) {
            return infered.iterator().next();
        }

        // type inferred
        if (declClass != null) {
            declaringClass = declClass;
            return declaringClass;
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof VariableExpression) {
            VariableExpression variable = (VariableExpression) dotCompletionContext.getAstPath().leaf();
            if ("this".equals(variable.getName())) { // NOI18N
                declaringClass = ContextHelper.getSurroundingClassNode(this);
                return declaringClass;
            }
            if ("super".equals(variable.getName())) { // NOI18N
                ClassNode thisClass = ContextHelper.getSurroundingClassNode(this);
                declaringClass = thisClass.getSuperClass();
                if (declaringClass == null) {
                    return new ClassNode("java.lang.Object", ClassNode.ACC_PUBLIC, null);
                }
                return declaringClass;
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
            declaringClass = expression.getType();
        }

        return declaringClass;
    }

    /**
     * Check whether this completion request was issued behind an import statement.
     *
     * @return true if it's right behind and import statement, false otherwise
     */
    private boolean checkForRequestBehindImportStatement() {
        int rowStart = 0;
        int nonWhite = 0;

        try {
            rowStart = Utilities.getRowStart(doc, lexOffset);
            nonWhite = Utilities.getFirstNonWhiteFwd(doc, rowStart);

        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "Trouble doing getRowStart() or getFirstNonWhiteFwd(): {0}", ex.getMessage());
        }

        Token<? extends GroovyTokenId> importToken = LexUtilities.getToken(doc, nonWhite);

        if (importToken != null && importToken.id() == GroovyTokenId.LITERAL_import) {
            LOG.log(Level.FINEST, "Right behind an import statement");
            return true;
        }

        return false;
    }
}
