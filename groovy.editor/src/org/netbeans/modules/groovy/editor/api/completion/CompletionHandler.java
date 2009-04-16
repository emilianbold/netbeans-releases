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
package org.netbeans.modules.groovy.editor.api.completion;

import java.beans.PropertyChangeEvent;
import javax.lang.model.element.ElementKind;
import groovy.lang.MetaMethod;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.GroovyTypeAnalyzer;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.completion.CompleteElementHandler;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public class CompletionHandler implements CodeCompletionHandler {

    private static final Logger LOG = Logger.getLogger(CompletionHandler.class.getName());

    private final List<String> defaultImports = new ArrayList<String>();

    private final PropertyChangeListener docListener;

    private int anchor;

    private String jdkJavaDocBase = null;

    private String groovyJavaDocBase = null;

    private String groovyApiDocBase = null;

    // FIXME this should be local variable
    private Set<GroovyKeyword> keywords;

    public CompletionHandler() {
        JavaPlatformManager platformMan = JavaPlatformManager.getDefault();
        JavaPlatform platform = platformMan.getDefaultPlatform();
        List<URL> docfolder = platform.getJavadocFolders();

        for (URL url : docfolder) {
            LOG.log(Level.FINEST, "JDK Doc path: {0}", url.toString()); // NOI18N
            jdkJavaDocBase = url.toString();
        }

        GroovySettings groovySettings = GroovySettings.getInstance();
        docListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (CompletionHandler.this) {
                    groovyJavaDocBase = null;
                    groovyApiDocBase = null;
                }
            }
        };
        groovySettings.addPropertyChangeListener(WeakListeners.propertyChange(docListener, this));

        Collections.addAll(defaultImports, "java.io", "java.lang", "java.net", // NOI18N
                "java.util", "groovy.util", "groovy.lang"); // NOI18N
    }

    private String getGroovyJavadocBase() {
        synchronized (this) {
            if (groovyJavaDocBase == null) {
                String docroot = GroovySettings.getInstance().getGroovyDoc() + "/"; // NOI18N
                groovyJavaDocBase = directoryNameToUrl(docroot + "groovy-jdk/"); // NOI18N
            }
            return groovyJavaDocBase;
        }
    }

    private String getGroovyApiDocBase() {
        synchronized (this) {
            if (groovyApiDocBase == null) {
                String docroot = GroovySettings.getInstance().getGroovyDoc() + "/"; // NOI18N
                groovyApiDocBase = directoryNameToUrl(docroot + "gapi/"); // NOI18N
            }
            return groovyApiDocBase;
        }
    }

    private static String directoryNameToUrl(String dirname) {
        if (dirname == null) {
            return "";
        }

        // FIXME use FileObject (?)
        File dirFile = new File(dirname);

        if (dirFile != null && dirFile.exists() && dirFile.isDirectory()) {
            String fileURL = "";
            if (Utilities.isWindows()) {
                dirname = dirname.replace("\\", "/");
                fileURL = "file:/"; // NOI18N
            } else {
                fileURL = "file://"; // NOI18N
            }
            return fileURL + dirname;
        } else {
            return "";
        }
    }

    private static void printASTNodeInformation(String description, ASTNode node) {

        LOG.log(Level.FINEST, "--------------------------------------------------------");
        LOG.log(Level.FINEST, "{0}", description);

        if (node == null) {
            LOG.log(Level.FINEST, "node == null");
        } else {
            LOG.log(Level.FINEST, "Node.getText()  : " + node.getText());
            LOG.log(Level.FINEST, "Node.toString() : " + node.toString());
            LOG.log(Level.FINEST, "Node.getClass() : " + node.getClass());
            LOG.log(Level.FINEST, "Node.hashCode() : " + node.hashCode());


            if (node instanceof ModuleNode) {
                LOG.log(Level.FINEST, "ModuleNode.getClasses() : " + ((ModuleNode) node).getClasses());
                LOG.log(Level.FINEST, "SourceUnit.getName() : " + ((ModuleNode) node).getContext().getName());
            }
        }

        LOG.log(Level.FINEST, "--------------------------------------------------------");
    }

    private static void printMethod(MetaMethod mm) {

        LOG.log(Level.FINEST, "--------------------------------------------------");
        LOG.log(Level.FINEST, "getName()           : " + mm.getName());
        LOG.log(Level.FINEST, "toString()          : " + mm.toString());
        LOG.log(Level.FINEST, "getDescriptor()     : " + mm.getDescriptor());
        LOG.log(Level.FINEST, "getSignature()      : " + mm.getSignature());
        // LOG.log(Level.FINEST, "getParamTypes()     : " + mm.getParameterTypes());
        LOG.log(Level.FINEST, "getDeclaringClass() : " + mm.getDeclaringClass());
    }

    /**
     * Holder class for the context of a given completion.
     * This means the two surrounding Lexer-tokens before and after
     * the completion point.
     */
    class CompletionContext {
        // b2    b1      |       a1        a2
        // class MyClass extends BaseClass {

        Token<? extends GroovyTokenId> beforeLiteral;
        Token<? extends GroovyTokenId> before2;
        Token<? extends GroovyTokenId> before1;
        Token<? extends GroovyTokenId> active;
        Token<? extends GroovyTokenId> after1;
        Token<? extends GroovyTokenId> after2;
        Token<? extends GroovyTokenId> afterLiteral;
        TokenSequence<?> ts; // we keep the sequence with us.

        public CompletionContext(
            Token<? extends GroovyTokenId> beforeLiteral,
            Token<? extends GroovyTokenId> before2,
            Token<? extends GroovyTokenId> before1,
            Token<? extends GroovyTokenId> active,
            Token<? extends GroovyTokenId> after1,
            Token<? extends GroovyTokenId> after2,
            Token<? extends GroovyTokenId> afterLiteral,
            TokenSequence<?> ts) {

            this.beforeLiteral = beforeLiteral;
            this.before2 = before2;
            this.before1 = before1;
            this.active = active;
            this.after1 = after1;
            this.after2 = after2;
            this.afterLiteral = afterLiteral;
            this.ts = ts;
        }
    }

    /**
     * Computes an CompletionContext which surrounds the request.
     * Three tokens in front and three after the request.
     *
     * @param request
     * @return
     */
    CompletionContext getCompletionContext(final CompletionRequest request) {
        int position = request.lexOffset;

        Token<? extends GroovyTokenId> beforeLiteral = null;
        Token<? extends GroovyTokenId> before2 = null;
        Token<? extends GroovyTokenId> before1 = null;
        Token<? extends GroovyTokenId> active = null;
        Token<? extends GroovyTokenId> after1 = null;
        Token<? extends GroovyTokenId> after2 = null;
        Token<? extends GroovyTokenId> afterLiteral = null;

        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);

        int difference = 0;
        difference = ts.move(position);

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

    boolean checkForPackageStatement(final CompletionRequest request) {
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, 1);

        if (ts != null) {
            ts.move(1);

            while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
                Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();

                if (t.id() == GroovyTokenId.LITERAL_package) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Figure-out, where we are in the code (comment, CU, class, method, etc.)
     * @param request
     * @return
     */
    private CaretLocation getCaretLocationFromRequest(final CompletionRequest request) {


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

        if (!classDefBeforePosition && classDefAfterPosition) {
            return CaretLocation.ABOVE_FIRST_CLASS;
        }

        // If there's *no* class definition in the file we are running in a
        // script with synthetic wrapper class and wrapper method: run().
        // See GINA, ch. 7

        if (!classDefBeforePosition && !classDefAfterPosition) {
            request.scriptMode = true;
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

    private ArgumentListExpression getSurroundingArgumentList(AstPath path) {
        if (path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "AEL, Path : {0}", path);

        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ArgumentListExpression) {

                return (ArgumentListExpression) current;
            }
        }
        return null;

    }

    /**
     * returns the next enclosing MethodNode for the given request
     * @param request completion request which includes position information
     * @return the next surrouning MethodNode
     */
    private ASTNode getSurroundingMethodOrClosure(CompletionRequest request) {
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
     * returns the next enclosing ClassNode for the given request
     * @param request completion request which includes position information
     * @return the next surrouning ClassNode
     */
    private ClassNode getSurroundingClassNode(CompletionRequest request) {
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
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     *
     * @param request
     * @return a freshly created AstPath object for the offset given in the request
     */
    private AstPath getPathFromRequest(final CompletionRequest request) {
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

    private AstPath getPathFromInfo(final int caretOffset, final ParserResult info) {
        assert info != null;

        ASTNode root = AstUtilities.getRoot(info);

        // If we don't get a valid root-node from a valid CompilationInfo,
        // there's not much we can do. cf. # 150929

        if (root == null) {
            return null;
        }

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        return new AstPath(root, caretOffset, doc);

    }

    /**
     * Complete Groovy or Java Keywords.
     *
     * @see GroovyKeyword for matrix of capabilities, scope and allowed usage.
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeKeywords(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeKeywords"); // NOI18N
        String prefix = request.prefix;

        if (request.location == CaretLocation.INSIDE_PARAMETERS) {
            LOG.log(Level.FINEST, "no keywords completion inside of parameters"); // NOI18N
            return false;
        }

        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N
            return false;
        }

        // Is there already a "package"-statement in the sourcecode?
        boolean havePackage = checkForPackageStatement(request);

        keywords = EnumSet.allOf(GroovyKeyword.class);

        // filter-out keywords in a step-by-step approach

        filterPackageStatement(havePackage);
        filterPrefix(prefix);
        filterLocation(request.location);
        filterClassInterfaceOrdering(request.ctx);
        filterMethodDefinitions(request.ctx);
        filterKeywordsNextToEachOther(request.ctx);

        // add the remaining keywords to the result

        for (GroovyKeyword groovyKeyword : keywords) {
            LOG.log(Level.FINEST, "Adding keyword proposal : {0}", groovyKeyword.name); // NOI18N
            proposals.add(new CompletionItem.KeywordItem(groovyKeyword.name, null, anchor, request.info, groovyKeyword.isGroovy));
        }

        return true;
    }

    // filter-out package-statemen, if there's already one
    void filterPackageStatement(boolean havePackage) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (groovyKeyword.name.equals("package") && havePackage) {
                // LOG.log(Level.FINEST, "filterPackageStatement - removing : {0}", groovyKeyword.name);
                keywords.remove(groovyKeyword);
            }
        }
    }

    // Filter prefix
    void filterPrefix(String prefix) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (!groovyKeyword.name.startsWith(prefix)) {
                // LOG.log(Level.FINEST, "filterPrefix - removing : {0}", groovyKeyword.name);
                keywords.remove(groovyKeyword);
            }
        }
    }

    // Filter Location
    void filterLocation(CaretLocation location) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (!checkKeywordAllowance(groovyKeyword, location)) {
                // LOG.log(Level.FINEST, "filterLocation - removing : {0}", groovyKeyword.name);
                keywords.remove(groovyKeyword);
            }
        }
    }

    // Filter right Keyword ordering
    void filterClassInterfaceOrdering(CompletionContext ctx) {

        if (ctx == null || ctx.beforeLiteral == null) {
            return;
        }

        if (ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_interface) {
            keywords.clear();
            keywords.add(GroovyKeyword.KEYWORD_extends);
        } else if (ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            keywords.clear();
            keywords.add(GroovyKeyword.KEYWORD_extends);
            keywords.add(GroovyKeyword.KEYWORD_implements);
        }

    }

    // Filter-out modifier/datatype ordering in method definitions
    void filterMethodDefinitions(CompletionContext ctx) {

        if (ctx == null || ctx.afterLiteral == null) {
            return;
        }


        if (ctx.afterLiteral.id() == GroovyTokenId.LITERAL_void ||
            ctx.afterLiteral.id() == GroovyTokenId.IDENTIFIER ||
            ctx.afterLiteral.id().primaryCategory().equals("number")) {

            // we have to filter-out the primitive types

            for (GroovyKeyword groovyKeyword : keywords) {
                if (groovyKeyword.category == KeywordCategory.PRIMITIVE) {
                    LOG.log(Level.FINEST, "filterMethodDefinitions - removing : {0}", groovyKeyword.name);
                    keywords.remove(groovyKeyword);
                }
            }
        }
    }

    // Filter-out keywords, if we are surrounded by others.
    // This can only be an approximation.
    void filterKeywordsNextToEachOther(CompletionContext ctx) {

        if (ctx == null) {
            return;
        }

        boolean filter = false;

        if (ctx.after1 != null && ctx.after1.id().primaryCategory().equals("keyword")) {
            filter = true;
        }

        if (ctx.before1 != null && ctx.before1.id().primaryCategory().equals("keyword")) {
            filter = true;
        }

        if (filter) {
            for (GroovyKeyword groovyKeyword : keywords) {
                if (groovyKeyword.category == KeywordCategory.KEYWORD) {
                    LOG.log(Level.FINEST, "filterMethodDefinitions - removing : {0}", groovyKeyword.name);
                    keywords.remove(groovyKeyword);
                }
            }
        }

    }

    boolean checkKeywordAllowance(GroovyKeyword groovyKeyword, CaretLocation location) {

        if (location == null) {
            return false;
        }

        switch (location) {
            case ABOVE_FIRST_CLASS:
                if (groovyKeyword.aboveFistClass) {
                    return true;
                }
                break;
            case OUTSIDE_CLASSES:
                if (groovyKeyword.outsideClasses) {
                    return true;
                }
                break;
            case INSIDE_CLASS:
                if (groovyKeyword.insideClass) {
                    return true;
                }
                break;
            case INSIDE_METHOD: // intentionally fall-through
            case INSIDE_CLOSURE:
                if (groovyKeyword.insideCode) {
                    return true;
                }
                break;
        }

        return false;
    }

    private boolean completeNewVars(List<CompletionProposal> proposals, CompletionRequest request, List<String> newVars) {
        LOG.log(Level.FINEST, "-> completeNewVars"); // NOI18N

        if (request.location == CaretLocation.OUTSIDE_CLASSES) {
            LOG.log(Level.FINEST, "outside of any class, bail out."); // NOI18N
            return false;
        }

        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N
            return false;
        }

        if (newVars == null) {
            LOG.log(Level.FINEST, "Can not propose with newVars == null"); // NOI18N
            return false;
        }

        boolean stuffAdded = false;

        for (String var : newVars) {
            LOG.log(Level.FINEST, "Variable candidate: {0}", var); // NOI18N
            if (var.startsWith(request.prefix)) {
                proposals.add(new CompletionItem.NewVarItem(var, anchor));
                stuffAdded = true;
            }
        }
        return stuffAdded;
    }

    /**
     * Complete the fields for a class. There are two principal completions for fields:
     *
     * 1.) We are invoked right behind a dot. Then we have to retrieve the type in front of this dot.
     * 2.) We are located inside a type. Then we gotta get the fields for this class.
     *
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeFields(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeFields"); // NOI18N

        if (request.location == CaretLocation.INSIDE_PARAMETERS && request.isBehindDot() == false) {
            LOG.log(Level.FINEST, "no fields completion inside of parameters-list"); // NOI18N
            return false;
        }

        if (request.dotContext != null && request.dotContext.isMethodsOnly()) {
            return false;
        }

        ClassNode declaringClass;

        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N

            PackageCompletionRequest packageRequest = getPackageRequest(request);

            if (packageRequest.basePackage.length() > 0) {
                ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);

                if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                    LOG.log(Level.FINEST, "The string before the dot seems to be a valid package"); // NOI18N
                    return false;
                }
            }

            declaringClass = getBeforeDotDeclaringClass(request);

            if (declaringClass == null) {
                LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
                return false;
            }
        } else {
            declaringClass = getSurroundingClassNode(request);

            if (declaringClass == null) {
                LOG.log(Level.FINEST, "No surrounding class found, bail out ..."); // NOI18N
                return false;
            }
        }

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        int anchorShift = 0;
        String fieldName = request.prefix;

        if (request.prefix.startsWith("$")) {
            fieldName = request.prefix.substring(1);
            anchorShift = 1;
        }

        Map<FieldSignature, ? extends CompletionItem> result = CompleteElementHandler
                .forCompilationInfo(request.info)
                    .getFields(getSurroundingClassNode(request), declaringClass, fieldName, anchor + anchorShift);

        proposals.addAll(result.values());

        return true;
    }

    private boolean completeLocalVars(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeLocalVars"); // NOI18N

        if (!(request.location == CaretLocation.INSIDE_CLOSURE || request.location == CaretLocation.INSIDE_METHOD)) {
            LOG.log(Level.FINEST, "not inside method or closure, bail out."); // NOI18N
            return false;
        }

        // If we are right behind a dot, there's no local-vars completion.

        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N
            return false;
        }

        ASTNode scope = getSurroundingMethodOrClosure(request);

        if (request.scriptMode) {
            LOG.log(Level.FINEST, "We are running in script-mode."); // NOI18N
            if (scope == null) {
                scope = AstUtilities.getRoot(request.info);
            }
        }

        if (scope == null) {
            LOG.log(Level.FINEST, "scope == null"); // NOI18N
            return false;
        }

        Map<String, ASTNode> result = new HashMap<String, ASTNode>();
        getLocalVars(scope, result, request);

        if (!result.isEmpty()) {
            for (ASTNode node : result.values()) {
                String varName = ((Variable) node).getName();
                LOG.log(Level.FINEST, "Node found: {0}", varName); // NOI18N

                if (request.prefix.length() < 1) {
                    proposals.add(new CompletionItem.LocalVarItem((Variable) node, anchor));
                } else {
                    if (varName.compareTo(request.prefix) != 0 && varName.startsWith(request.prefix)) {
                        proposals.add(new CompletionItem.LocalVarItem((Variable) node, anchor));
                    }
                }

            }
        }

        return true;
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
     * @return
     */
    private boolean checkForVariableDefinition(CompletionRequest request) {
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

    private ASTNode getASTNodeForToken(Token<? extends GroovyTokenId> tid, CompletionRequest request) {
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
     * This is a minimal version of Utilities.varNamesForType() to suggest variable names.
     *
     * See:
     * java.editor/src/org/netbeans/modules/editor/java/JavaCompletionProvider.java
     * java.editor/src/org/netbeans/modules/editor/java/Utilities.varNamesSuggestions()
     * how to do this right.
     *
     * todo: recurse to look at arrays. For example: Long [] gives longs
     *
     * @param ctx
     * @return
     */
    private List<String> getNewVarNameSuggestion(CompletionContext ctx) {
        LOG.log(Level.FINEST, "getNewVarNameSuggestion()"); // NOI18N

        List<String> result = new ArrayList<String>();

        if (ctx == null || ctx.before1 == null) {
            return result;
        }

        // Check for primitive types first:
        // int long char byte double float short boolean

        if (ctx.before1.id() == GroovyTokenId.LITERAL_boolean) {
            result.add("b");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_byte) {
            result.add("b");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_char) {
            result.add("c");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_double) {
            result.add("d");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_float) {
            result.add("f");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_int) {
            result.add("i");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_long) {
            result.add("l");
        } else if (ctx.before1.id() == GroovyTokenId.LITERAL_short) {
            result.add("s");
        }

        // now we propose variable names based on the type

        if (ctx.before1.id() == GroovyTokenId.IDENTIFIER) {

            String typeName = ctx.before1.text().toString();

            if (typeName != null) {
                // Only First char, lowercase
                addIfNotIn(result, typeName.substring(0, 1).toLowerCase(Locale.ENGLISH));
                // name lowercase
                addIfNotIn(result, typeName.toLowerCase(Locale.ENGLISH));
                // camelcase hunches put together
                addIfNotIn(result, camelCaseHunch(typeName));
                // first char switched to lowercase
                addIfNotIn(result, typeName.substring(0, 1).toLowerCase(Locale.ENGLISH) + typeName.substring(1));
            }
        }
        return result;
    }

    void addIfNotIn(List<String> result, String name) {
        if (name.length() > 0) {
            if (!result.contains(name)) {
                LOG.log(Level.FINEST, "Adding new-var suggestion : {0}", name); // NOI18N
                result.add(name);
            }
        }
    }

    // this was: Utilities.nextName()
    private static String camelCaseHunch(CharSequence name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                char lc = Character.toLowerCase(c);
                sb.append(lc);
            }
        }
        return sb.toString();
    }

    /**
     * This method returns the Local Variables of a method or a closure.
     * @param node
     * @param result
     * @param request
     */
    private void getLocalVars(ASTNode node, Map<String, ASTNode> result, CompletionRequest request) {
        // if we are dealing with a closure, we retrieve the local vars differently

        if (node instanceof ClosureExpression) {
            ClosureExpression closure = (ClosureExpression) node;

            if (closure.isParameterSpecified()) {
                LOG.log(Level.FINEST, "We do have Parameters...");
                Parameter[] params = closure.getParameters();

                for (int i = 0; i < params.length; i++) {
                    Parameter parameter = params[i];
                    LOG.log(Level.FINEST, "Parameter: {0}", parameter.getName());
                    // FIXME is the text right choice ?
                    result.put(parameter.getName(), parameter);
                }


            } else {
                LOG.log(Level.FINEST, "Closure without parameters, have to put it in list");
                // FIXME is the text right choice ?
                result.put("it", new VariableExpression("it"));
            }
        } else {
            if (node instanceof Variable) {
                String name = ((Variable) node).getName();
                if (!result.containsKey(name) && name.length() > 0) {
                    result.put(name, node);
                }
            }

            List<ASTNode> list = AstUtilities.children(node);
            for (ASTNode child : list) {
                // if we are running in script-mode, which means starting at the root
                // we neighter recurse into MethodNodes nor closures.
                if (request.scriptMode && (child instanceof MethodNode || child instanceof ClosureExpression)) {
                    continue;
                }

                getLocalVars(child, result, request);
            }
        }
    }

    /**
     * Check whether this completion request was issued behind an import statement.
     * @param request
     * @return
     */
    boolean checkForRequestBehindImportStatement(final CompletionRequest request) {

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

    boolean checkBehindDot(final CompletionRequest request) {
        boolean behindDot = false;

        if (request == null || request.ctx == null || request.ctx.before1 == null) {
            behindDot = false;
        } else {
            if (CharSequenceUtilities.textEquals(request.ctx.before1.text(), ".") // NOI18N
                    || (request.ctx.before1.text().toString().equals(request.prefix)
                        && request.ctx.before2 != null
                        && CharSequenceUtilities.textEquals(request.ctx.before2.text(), "."))) { // NOI18N
                behindDot = true;
            }
        }

        return behindDot;
    }

    PackageCompletionRequest getPackageRequest(final CompletionRequest request) {
        int position = request.lexOffset;
        PackageCompletionRequest result = new PackageCompletionRequest();

        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);
        ts.move(position);

        // travel back on the token string till the token is neither a
        // DOT nor an IDENTIFIER

        Token<? extends GroovyTokenId> token = null;
        boolean remainingTokens = true;
        while (ts.isValid() && (remainingTokens = ts.movePrevious()) && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            // LOG.log(Level.FINEST, "LexerToken(back): {0}", t.text().toString());
            if (!(t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER)) {
                break;
            } else {
                token = t;
            }
        }

        // now we are travelling in the opposite direction to construct
        // the result

        StringBuffer buf = new StringBuffer();
        Token<? extends GroovyTokenId> lastToken = null;

        // if we reached the beginning in the previous iteration we have to get
        // the first token too (without call to moveNext())
        if (!remainingTokens && token != null && ts.isValid()) {
            buf.append(token.text().toString());
            lastToken = token;
        }

        // iterate the rest of the sequence
        while (ts.isValid() && ts.moveNext() && ts.offset() < position) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();

            // LOG.log(Level.FINEST, "LexerToken(fwd): {0}", t.text().toString());
            if (t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER) {
                buf.append(t.text().toString());
                lastToken = t;
            } else {
                break;
            }
        }

        // construct the return value. These are the combinations:
        // string           basePackage prefix
        // ""               ""          ""
        // "java"           ""          "java"
        // "java."          "java"      ""
        // "java.lan"       "java"      "lan"
        // "java.lang"      "java"      "lang"
        // "java.lang."     "java.lang" ""

        result.fullString = buf.toString();

        if (buf.length() == 0) {
            result.basePackage = "";
            result.prefix = "";
        } else if (lastToken != null && lastToken.id() == GroovyTokenId.DOT) {
            String pkgString = buf.toString();
            result.basePackage = pkgString.substring(0, pkgString.length() - 1);
            result.prefix = "";
        } else if (lastToken != null && lastToken.id() == GroovyTokenId.IDENTIFIER) {
            String pkgString = buf.toString();
            result.prefix = lastToken.text().toString();

            result.basePackage = pkgString.substring(0, pkgString.length() - result.prefix.length());

            if (result.basePackage.endsWith(".")) {
                result.basePackage = result.basePackage.substring(0, result.basePackage.length() - 1);
            }
        }

        LOG.log(Level.FINEST, "-- fullString : >{0}<", result.fullString);
        LOG.log(Level.FINEST, "-- basePackage: >{0}<", result.basePackage);
        LOG.log(Level.FINEST, "-- prefix:      >{0}<", result.prefix);

        return result;
    }

    class PackageCompletionRequest {

        String fullString = "";
        String basePackage = "";
        String prefix = "";
    }

    private DotCompletionContext getDotCompletionContext(final CompletionRequest request) {
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
                if (t.id() != GroovyTokenId.IDENTIFIER) {
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

    private static class DotCompletionContext {

        private final int lexOffset;

        private final int astOffset;

        private final AstPath astPath;

        private final boolean methodsOnly;

        public DotCompletionContext(int lexOffset, int astOffset,
                AstPath astPath, boolean methodsOnly) {
            this.lexOffset = lexOffset;
            this.astOffset = astOffset;
            this.astPath = astPath;
            this.methodsOnly = methodsOnly;
        }

        public int getLexOffset() {
            return lexOffset;
        }

        public int getAstOffset() {
            return astOffset;
        }

        public AstPath getAstPath() {
            return astPath;
        }

        public boolean isMethodsOnly() {
            return methodsOnly;
        }
        
    }

    /**
     *
     * @param request
     * @return
     */
    private ClasspathInfo getClasspathInfoFromRequest(final CompletionRequest request) {
        FileObject fileObject = request.info.getSnapshot().getSource().getFileObject();

        if (fileObject != null) {
            return ClasspathInfo.create(fileObject);
        }

        return null;
    }

    private JavaSource getJavaSourceFromRequest(final CompletionRequest request) {

        ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);
        assert pathInfo != null;

        // get the JavaSource for our file.

        JavaSource javaSource = JavaSource.create(pathInfo);

        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }

        return javaSource;
    }

    /**
     * Here we complete package-names like java.lan to java.lang ...
     *
     * @param proposals the CompletionPropasal we should populate
     * @param request wrapper object for this specific request ( position etc.)
     * @return true if we found something suitable
     */
    private boolean completePackages(final List<CompletionProposal> proposals, final CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completePackages"); // NOI18N

        // this can happen for ?. or similar constructs
        PackageCompletionRequest packageRequest = getPackageRequest(request);
        if (request.isBehindDot() && packageRequest.basePackage.length() <= 0) {
            return false;
        }


        LOG.log(Level.FINEST, "Token fullString = >{0}<", packageRequest.fullString);

        ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);

        assert pathInfo != null : "Can not get ClasspathInfo";

        if (request.ctx.before1 != null
                && CharSequenceUtilities.textEquals(request.ctx.before1.text(), "*")
                && request.behindImport) {
            return false;
        }

        // try to find suitable packages ...

        Set<String> pkgSet;

        pkgSet = pathInfo.getClassIndex().getPackageNames(packageRequest.fullString, true, EnumSet.allOf(ClassIndex.SearchScope.class));

        for (String singlePackage : pkgSet) {
            LOG.log(Level.FINEST, "PKG set item: {0}", singlePackage);

            if (packageRequest.prefix.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.fullString.length());
            } else if (!packageRequest.basePackage.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.basePackage.length() + 1);
            }

            if (singlePackage.startsWith(packageRequest.prefix) && singlePackage.length() > 0) {
                CompletionItem.PackageItem item = new CompletionItem.PackageItem(singlePackage, anchor, request.info);

                if (request.behindImport) {
                    item.setSmart(true);
                }

                proposals.add(item);
            }

        }

        return false;
    }

    private boolean isValidPackage(ClasspathInfo pathInfo, String pkg) {
        assert pathInfo != null : "ClasspathInfo can not be null";

        // the following statement gives us all the packages *starting* with the
        // first parameter. We have to check for exact matches ourselves. See # 142372

        Set<String> pkgSet = pathInfo.getClassIndex().getPackageNames(pkg, true, EnumSet.allOf(ClassIndex.SearchScope.class));

        if (pkgSet.size() > 0) {
            LOG.log(Level.FINEST, "Packages with prefix : {0}", pkg);
            LOG.log(Level.FINEST, "               found : {0}", pkgSet);

            for (String singlePkg : pkgSet) {
                if(singlePkg.equals(pkg)){
                    LOG.log(Level.FINEST, "Exact match found.");
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    /**
     * Complete the Groovy and Java types available at this position.
     *
     * This could be either:
     *
     * 1.) Completing all available Types in a given package. This is used for:
     *
     * 1.1) import statements completion
     * 1.2) If you simply want to give the fq-name for something.
     *
     * 2.) Complete the types which are available without having to give a fqn:
     *
     * 2.1.) Types defined in the Groovy File where the completion is invoked. (INDEX)
     * 2.2.) Types located in the same package (source or binary). (INDEX)
     * 2.3.) Types manually imported via the "import" statement. (AST)
     * 2.4.) The Default imports for Groovy, which are a super-set of Java. (NB JavaSource)
     *
     * These are the Groovy default imports:
     *
     * java.io.*
     * java.lang.*
     * java.math.BigDecimal
     * java.math.BigInteger
     * java.net.*
     * java.util.*
     * groovy.lang.*
     * groovy.util.*
     *
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeTypes(final List<CompletionProposal> proposals, final CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeTypes"); // NOI18N
        final PackageCompletionRequest packageRequest = getPackageRequest(request);

        // todo: we don't handle single dots in the source. In that case we should
        // find the class we are living in. Disable it for now.

        if (packageRequest.basePackage.length() == 0
                && packageRequest.prefix.length() == 0
                && packageRequest.fullString.equals(".")) {
            return false;
        }

        // this is a new Something()| request for a constructor, which is handled in completeMethods.

        if (request.ctx.before1 != null
                && request.ctx.before1.text().toString().equals("new") // NOI18N
                && request.prefix.length() > 0) {
            return false;
        }

        // are we dealing with a class xyz implements | {
        // kind of completion?

        boolean onlyInterfaces = false;

        if (request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) {
            LOG.log(Level.FINEST, "Completing only interfaces after implements keyword.");
            onlyInterfaces = true;
        }

        // This ModuleNode is used to retrieve the types defined here
        // and the package name.

        ModuleNode mn = null;
        AstPath path = request.path;
        if (path != null) {
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
                ASTNode current = it.next();
                if (current instanceof ModuleNode) {
                    LOG.log(Level.FINEST, "Found ModuleNode");
                    mn = (ModuleNode) current;
                }
            }
        }

        // Get current package
        String currentPackage = null;
        if (mn != null) {
            currentPackage = mn.getPackageName();
        } else {
            ClassNode node = getSurroundingClassNode(request);
            if (node != null) {
                currentPackage = node.getPackageName();
            }
        }

        Set<TypeHolder> addedTypes = new HashSet<TypeHolder>();

        // get the JavaSource for our file.
        final JavaSource javaSource = getJavaSourceFromRequest(request);

        // if we are dealing with a basepackage we simply complete all the packages given in the basePackage

        if (packageRequest.basePackage.length() > 0 || request.behindImport) {
            if (!(request.behindImport && packageRequest.basePackage.length() == 0)) {

                List<TypeHolder> stringTypelist =
                        getElementListForPackageAsTypeHolder(javaSource, packageRequest.basePackage, currentPackage);

                if (stringTypelist == null) {
                    LOG.log(Level.FINEST, "Typelist is null for package : {0}", packageRequest.basePackage);
                    return false;
                }

                LOG.log(Level.FINEST, "Number of types found:  {0}", stringTypelist.size());

                for (TypeHolder singleType : stringTypelist) {
                    addToProposalUsingFilter(addedTypes, proposals, request, singleType, onlyInterfaces);
                }
            }

            return true;

        }
        // already exited if package completion

        // dont want types for objectExpression.something
        if (request.isBehindDot()) {
            return false;
        }

        // Retrieve the package we are living in from AST and then
        // all classes from that package using the Groovy Index.

        if (mn != null) {
            LOG.log(Level.FINEST, "We are living in package : {0} ", currentPackage);

            // FIXME parsing API
            GroovyIndex index = null;
            FileObject fo = request.info.getSnapshot().getSource().getFileObject();
            if (fo != null) {
                index = GroovyIndex.get(QuerySupport.findRoots(fo,
                        Collections.singleton(ClassPath.SOURCE),
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList()));
            }

            if (index != null) {
                Set<IndexedClass> classes = index.getClasses(request.prefix, QuerySupport.Kind.CASE_INSENSITIVE_PREFIX,
                        true, false, false);

                if (classes.size() == 0) {
                    LOG.log(Level.FINEST, "Nothing found in GroovyIndex");
                } else {
                    LOG.log(Level.FINEST, "Found this number of classes : {0} ", classes.size());

                    Set<TypeHolder> typelist = new HashSet<TypeHolder>();

                    for (IndexedClass indexedClass : classes) {
                        LOG.log(Level.FINEST, "FQN classname from index : {0} ", indexedClass.getFqn());

                        ElementKind ek;
                        if (indexedClass.getKind() == org.netbeans.modules.csl.api.ElementKind.CLASS) {
                            ek = ElementKind.CLASS;
                        } else {
                            ek = ElementKind.INTERFACE;
                        }

                        typelist.add(new TypeHolder(indexedClass.getFqn(), ek));
                    }

                    for (TypeHolder type : typelist) {
                        addToProposalUsingFilter(addedTypes, proposals, request, type, onlyInterfaces);
                    }
                }
            }
        }

        List<String> localDefaultImports = new ArrayList<String>();

        // Are there any manually imported types?

        if (mn != null) {

            // this gets the list of full-qualified names of imports.
            List<ImportNode> imports = mn.getImports();

            if (imports != null) {
                for (ImportNode importNode : imports) {
                    LOG.log(Level.FINEST, "From getImports() : {0} ", importNode.getClassName());

                    ElementKind ek;

                    if (importNode.getClass().isInterface()) {
                        ek = ElementKind.INTERFACE;
                    } else {
                        ek = ElementKind.CLASS;
                    }

                    addToProposalUsingFilter(addedTypes, proposals, request,
                            new TypeHolder(importNode.getClassName(), ek), onlyInterfaces);
                }
            }

            // this returns a list of String's of wildcard-like included types.
            List<String> importsPkg = mn.getImportPackages();

            for (String wildcardImport : importsPkg) {
                LOG.log(Level.FINEST, "From getImportPackages() : {0} ", wildcardImport);
                if (wildcardImport.endsWith(".")) {
                    wildcardImport = wildcardImport.substring(0, wildcardImport.length() - 1);
                }

                localDefaultImports.add(wildcardImport);

            }

        }


        // Now we compute the type-proposals for the default imports.
        // First, create a list of default JDK packages. These are reused,
        // so they are defined elsewhere.

        localDefaultImports.addAll(defaultImports);

        // adding types from default import, optionally filtered by
        // prefix

        for (String singlePackage : localDefaultImports) {
            List<TypeHolder> typeList;

            typeList = getElementListForPackageAsTypeHolder(javaSource, singlePackage, currentPackage);

            if (typeList == null) {
                LOG.log(Level.FINEST, "Typelist is null for package : {0}", singlePackage);
                continue;
            }

            LOG.log(Level.FINEST, "Number of types found:  {0}", typeList.size());

            for (TypeHolder element : typeList) {
                addToProposalUsingFilter(addedTypes, proposals, request, element, onlyInterfaces);
            }
        }

        // Adding two single classes per hand
        addToProposalUsingFilter(addedTypes, proposals, request,
                new TypeHolder("java.math.BigDecimal", ElementKind.CLASS), onlyInterfaces);
        addToProposalUsingFilter(addedTypes, proposals, request,
                new TypeHolder("java.math.BigInteger", ElementKind.CLASS), onlyInterfaces);
        return true;
    }

    /**
     * Adds the type given in fqn with its simple name to the proposals, filtered by
     * the prefix and the package name.
     *
     * @param proposals
     * @param request
     * @param fqn
     */
    void addToProposalUsingFilter(Set<TypeHolder> alreadyPresent, List<CompletionProposal> proposals,
            CompletionRequest request, TypeHolder type, boolean onlyInterfaces) {

        if ((onlyInterfaces && (type.getKind() != ElementKind.INTERFACE)) || alreadyPresent.contains(type)) {
            return;
        }

        String typeName = NbUtilities.stripPackage(type.getName());

        if (typeName.toUpperCase(Locale.ENGLISH).startsWith(request.prefix.toUpperCase(Locale.ENGLISH))) {
            alreadyPresent.add(type);
            proposals.add(new CompletionItem.TypeItem(typeName, anchor, type.getKind()));
        }

        return;
    }

    /**
     *
     * @param javaSource
     * @param pkg
     * @return
     */
    List<? extends javax.lang.model.element.Element> getElementListForPackage(Elements elements, JavaSource javaSource, final String pkg) {
        LOG.log(Level.FINEST, "getElementListForPackage(), Package :  {0}", pkg);

        List<? extends javax.lang.model.element.Element> typelist = null;

        if (elements != null && pkg != null) {
            LOG.log(Level.FINEST, "TypeSearcherHelper.run(), elements retrieved");
            PackageElement packageElement = elements.getPackageElement(pkg);

            if (packageElement == null) {
                LOG.log(Level.FINEST, "packageElement is null");
            } else {
                typelist = packageElement.getEnclosedElements();
            }

        }

        LOG.log(Level.FINEST, "Returning Typlist");
        return typelist;

    }

    List<TypeHolder> getElementListForPackageAsTypeHolder(final JavaSource javaSource, final String pkg, final String currentPackage) {
        LOG.log(Level.FINEST, "getElementListForPackageAsString(), Package :  {0}", pkg);

        final List<TypeHolder> result = new ArrayList<TypeHolder>();

        if (javaSource != null) {

            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController info) {

                        List<? extends javax.lang.model.element.Element> typelist = null;

                        Elements elements = info.getElements();

                        if (elements != null && pkg != null) {
                            LOG.log(Level.FINEST, "TypeSearcherHelper.run(), elements retrieved");
                            PackageElement packageElement = elements.getPackageElement(pkg);

                            if (packageElement == null) {
                                LOG.log(Level.FINEST, "packageElement is null");
                            } else {
                                typelist = packageElement.getEnclosedElements();
                                boolean samePackage = pkg.equals(currentPackage);

                                for (Element element : typelist) {
                                    Set<Modifier> modifiers = element.getModifiers();
                                    if (modifiers.contains(Modifier.PUBLIC)
                                            || samePackage && (modifiers.contains(Modifier.PROTECTED)
                                                || (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PRIVATE)))) {
                                        result.add(new TypeHolder(element.toString(), element.getKind()));
                                    }
                                }
                            }

                        }
                    }
                }, true);
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "IOException : {0}", ex.getMessage());
            }

        }

        return result;

    }

    boolean isPackageAlreadyProposed(Set<String> pkgSet, String prefix) {
        for (String singlePackage : pkgSet) {
            if (prefix.startsWith(singlePackage)) {
                return true;
            }
        }
        return false;
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
    private ClassNode getBeforeDotDeclaringClass(CompletionRequest request) {

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
            }
            request.declaringClass = expression.getType();
        }

        return request.declaringClass;
    }

    /**
     * Get the list of parameters of this executable as a List of ParamDesc's
     * To be used in insert templates and pretty-printers.
     * @param exe
     * @return
     */
    private static List<CompletionItem.ParameterDescriptor> getParameterList(ExecutableElement exe) {

        List<CompletionItem.ParameterDescriptor> paramList = new ArrayList<CompletionItem.ParameterDescriptor>();

        if (exe != null) {
            // generate a list of parameters
            // unfortunately, we have to work around # 139695 in an ugly fashion

            List<? extends VariableElement> params = null;

            try {
                params = exe.getParameters(); // this can cause NPE's
                int i = 1;

                for (VariableElement variableElement : params) {
                    TypeMirror tm = variableElement.asType();

                    String fullName = tm.toString();
                    String name = fullName;

                    if (tm.getKind() == javax.lang.model.type.TypeKind.DECLARED) {
                        name = NbUtilities.stripPackage(fullName);
                    }

                    // todo: this needs to be replaced with values from the JavaDoc
                    String varName = "param" + String.valueOf(i);

                    paramList.add(new CompletionItem.ParameterDescriptor(fullName, name, varName));

                    i++;
                }
            } catch (NullPointerException e) {
                // simply do nothing.
            }

        }

        return paramList;
    }

    /**
     * Get the parameter-list of this executable as String
     * @param exe
     * @return
     */
    public static String getParameterListForMethod(ExecutableElement exe) {
        StringBuffer sb = new StringBuffer();

        if (exe != null) {
            // generate a list of parameters
            // unfortunately, we have to work around # 139695 in an ugly fashion

            List<? extends VariableElement> params = null;

            try {
                params = exe.getParameters(); // this can cause NPE's

                for (VariableElement variableElement : params) {
                    TypeMirror tm = variableElement.asType();

                    if (sb.length() > 0) {
                        sb.append(", ");
                    }

                    if (tm.getKind() == javax.lang.model.type.TypeKind.DECLARED
                            || tm.getKind() == javax.lang.model.type.TypeKind.ARRAY) {
                        sb.append(NbUtilities.stripPackage(tm.toString()));
                    } else {
                        sb.append(tm.toString());
                    }
                }
            } catch (NullPointerException e) {
                // simply do nothing.
            }

        }

        return sb.toString();
    }

    /**
     * Complete the methods invokable on a class.
     * @param proposals the CompletionProposal List we populate (return value)
     * @param request location information used as input
     * @return true if we found something usable
     */
    private boolean completeMethods(final List<CompletionProposal> proposals, final CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeMethods"); // NOI18N

        if (request.location == CaretLocation.INSIDE_PARAMETERS) {
            LOG.log(Level.FINEST, "no method completion inside of parameters"); // NOI18N
            return false;
        }

        if (request == null || request.ctx == null/* || request.ctx.before1 == null*/) {
            return false;
        }

        // check whether we are either:
        //
        // 1.) This is a constructor-call like: String s = new String|
        // 2.) right behind a dot. Then we look for:
        //     2.1  method on collection type: List, Map or Range
        //     2.2  static/instance method on class or object
        //     2.3  Get apropriate groovy-methods from index.
        //     2.4  dynamic, mixin method on Groovy-object like getXbyY()


        // 1.) Test if this is a Constructor-call?
        // FIXME move this to separate method completeConstructors()
        if (request.ctx.before1 != null && request.ctx.before1.text().toString().equals("new") && request.prefix.length() > 0) {
            LOG.log(Level.FINEST, "This looks like a constructor ...");
            // look for all imported types starting with prefix, which have public constructors
            final List<String> localDefaultImports = new ArrayList<String>();

            localDefaultImports.addAll(defaultImports);

            final JavaSource javaSource = getJavaSourceFromRequest(request);

            if (javaSource != null) {

                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController info) {

                            for (String singlePackage : localDefaultImports) {
                                List<? extends javax.lang.model.element.Element> typelist;

                                typelist = getElementListForPackage(info.getElements(), javaSource, singlePackage);

                                if (typelist == null) {
                                    LOG.log(Level.FINEST, "Typelist is null for package : {0}", singlePackage);
                                    continue;
                                }

                                LOG.log(Level.FINEST, "Number of types found:  {0}", typelist.size());

                                for (Element element : typelist) {
                                    // only look for classes rather than enums or interfaces
                                    if (element.getKind() == ElementKind.CLASS) {
                                        javax.lang.model.element.TypeElement te = (javax.lang.model.element.TypeElement) element;

                                        List<? extends javax.lang.model.element.Element> enclosed = te.getEnclosedElements();

                                        // we gotta get the constructors name from the type itself, since
                                        // all the constructors are named <init>.

                                        String constructorName = te.getSimpleName().toString();

                                        for (Element encl : enclosed) {
                                            if (encl.getKind() == ElementKind.CONSTRUCTOR) {

                                                if (constructorName.toUpperCase(Locale.ENGLISH).startsWith(request.prefix.toUpperCase(Locale.ENGLISH))) {

                                                    LOG.log(Level.FINEST, "Constructor call candidate added : {0}", constructorName);

                                                    String paramListString = getParameterListForMethod((ExecutableElement)encl);
                                                    List<CompletionItem.ParameterDescriptor> paramList = getParameterList((ExecutableElement)encl);

                                                    proposals.add(new CompletionItem.ConstructorItem(constructorName, paramListString, paramList, anchor, false));
                                                }
                                            }
                                        }

                                    }
                                }
                            }

                        }
                    }, true);
                } catch (IOException ex) {
                    LOG.log(Level.FINEST, "IOException : {0}", ex.getMessage());
                }

            }

            return !proposals.isEmpty();
        }

        // 2.2  static/instance method on class or object

        if (!request.isBehindDot() && request.ctx.before1 != null) {
            LOG.log(Level.FINEST, "I'm not invoked behind a dot."); // NOI18N
            return false;
        }

        ClassNode declaringClass = getBeforeDotDeclaringClass(request);

        if (declaringClass == null) {
            LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
            return false;
        }


        /*
            Here we need to figure out, whether we want to complete a variable:

            s.|

            where we want to complete fields and methodes *OR* a package prefix like:

            java.|

            To achive this we only complete methods if there is no basePackage, which is a valid
            package.
         */

        PackageCompletionRequest packageRequest = getPackageRequest(request);

        if (packageRequest.basePackage.length() > 0) {
            ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);

            if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                LOG.log(Level.FINEST, "The string before the dot seems to be a valid package"); // NOI18N
                return false;
            }
        }

        Map<MethodSignature, ? extends CompletionItem> result = CompleteElementHandler
                .forCompilationInfo(request.info)
                    .getMethods(getSurroundingClassNode(request), declaringClass, request.prefix, anchor,
                    request.dotContext != null && request.dotContext.isMethodsOnly());
        proposals.addAll(result.values());

        return true;
    }

    /**
     * This should complete CamelCaseTypes. Simply type SB for StringBuilder.
     * a) New Constructors for exising classes
     * b) imported, or in the CP available Types
     *
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeCamelCase(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeCamelCase"); // NOI18N

        // variant a) adding constructors.

        if (!(request.location == CaretLocation.INSIDE_CLASS)) {
            LOG.log(Level.FINEST, "Not inside a class"); // NOI18N
            return false;
        }


        String prefix = request.prefix;

        // Are we dealing with an all-uppercase prefix?
        if (prefix != null && prefix.length() > 0 && prefix.equals(prefix.toUpperCase())) {

            ClassNode requestedClass = getSurroundingClassNode(request);

            if (requestedClass == null) {
                LOG.log(Level.FINEST, "No surrounding class found, bail out ..."); // NOI18N
                return false;
            }

            String camelCaseSignature = computeCamelCaseSignature(requestedClass.getName());

            LOG.log(Level.FINEST, "Class name          : {0}", requestedClass.getName()); // NOI18N
            LOG.log(Level.FINEST, "CamelCase signature : {0}", camelCaseSignature); // NOI18N

            if (camelCaseSignature.startsWith(prefix)) {
                LOG.log(Level.FINEST, "Prefix matches Class's CamelCase signature. Adding."); // NOI18N
                proposals.add(new CompletionItem.ConstructorItem(requestedClass.getName(), null, null, anchor, true));
                return true;
            }

        }

        // todo: variant b) needs to have the CamelCase signatures in the index.

        return false;
    }

    private String computeCamelCaseSignature(String name) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                sb.append(name.charAt(i));
            }
        }

        return sb.toString();
    }

    public CodeCompletionResult complete(CodeCompletionContext context) {
        ParserResult info = context.getParserResult();
        String prefix = context.getPrefix();

        final int lexOffset = context.getCaretOffset();
        final int astOffset = AstUtilities.getAstOffset(info, lexOffset);

        LOG.log(Level.FINEST, "complete(...), prefix      : {0}", prefix); // NOI18N
        LOG.log(Level.FINEST, "complete(...), lexOffset   : {0}", lexOffset); // NOI18N
        LOG.log(Level.FINEST, "complete(...), astOffset   : {0}", astOffset); // NOI18N


        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        anchor = lexOffset - prefix.length();

        final Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return CodeCompletionResult.NONE;
        }

        final BaseDocument doc = (BaseDocument) document;

        doc.readLock(); // Read-lock due to Token hierarchy use

        try {
            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.doc = doc;
            request.info = info;
            request.prefix = prefix;
            request.scriptMode = false;
            request.path = getPathFromRequest(request);

            LOG.log(Level.FINEST, "complete(...), path        : {0}", request.path);


            // here we figure out once for all completions, where we are inside the source
            // (in method, in class, ouside class etc)

            request.location = getCaretLocationFromRequest(request);
            LOG.log(Level.FINEST, "I am here in sourcecode: {0}", request.location); // NOI18N

            // if we are above a package statement or inside a comment there's no completion at all.
            if (request.location == CaretLocation.ABOVE_PACKAGE || request.location == CaretLocation.INSIDE_COMMENT) {
                return new DefaultCompletionResult(proposals, false);
            }

            // now let's figure whether we are in some sort of definition line

            request.ctx = getCompletionContext(request);

            // Are we invoked right behind a dot? This is information is used later on in
            // a couple of completions.

            assert request.ctx != null;

            request.dotContext = getDotCompletionContext(request);

            if (request.isBehindDot()) {
                request.declaringClass = getBeforeDotDeclaringClass(request);
            }

            boolean definitionLine = checkForVariableDefinition(request);

            // are we're right behind an import statement?
            request.behindImport = checkForRequestBehindImportStatement(request);

            List<String> newVars = null;

            if (definitionLine) {
                newVars = getNewVarNameSuggestion(request.ctx);
            } else {

                if (!(request.location == CaretLocation.OUTSIDE_CLASSES
                        || request.location == CaretLocation.INSIDE_STRING)) {
                    // complete packages
                    completePackages(proposals, request);

                    // complete classes, interfaces and enums

                    completeTypes(proposals, request);

                }

                if (!request.behindImport) {

                    if (request.location != CaretLocation.INSIDE_STRING) {
                        // complete keywords
                        completeKeywords(proposals, request);

                        // complete methods
                        completeMethods(proposals, request);
                    }

                    // complete fields
                    completeFields(proposals, request);

                    // complete local variables
                    completeLocalVars(proposals, request);

                }
            }

            // proposals for new vars
            completeNewVars(proposals, request, newVars);

            // CamelCase completion
            completeCamelCase(proposals, request);

            return new DefaultCompletionResult(proposals, false);
        } finally {
            doc.readUnlock();
        }
    //return proposals;
    }

    /**
     * create the signature-string of this method usable as a
     * Javadoc URL suffix (behind the # )
     *
     * This was needed, since from groovy 1.5.4 to
     * 1.5.5 the MetaMethod.getSignature() changed from
     * human-readable to Class.getName() output.
     *
     * To make matters worse, we have some subtle
     * differences between JDK and GDK MetaMethods
     *
     * method.getSignature for the JDK gives the return-
     * value right behind the method and encodes like Class.getName():
     *
     * codePointCount(II)I
     *
     * GDK-methods look like this:
     * java.lang.String center(java.lang.Number, java.lang.String)
     *
     * TODO: if groovy folks ever change this (again), we're falling
     * flat on our face.
     *
     */
    public static String getMethodSignature(MetaMethod method, boolean forURL, boolean isGDK) {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK) {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1) {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL) {
                methodSignature = methodSignature.replaceAll(", ", ",%20");
            }

            return methodSignature;

        } else {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) {
                return "";
            }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName()
     */
    static String decodeTypes(final String encodedType, boolean forURL) {


        String DELIMITER = ",";

        if (forURL) {
            DELIMITER = DELIMITER + "%20";
        } else {
            DELIMITER = DELIMITER + " ";
        }

        StringBuffer sb = new StringBuffer("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++) {
            char c = encodedType.charAt(i);

            if (c == '[') {
                nextIsAnArray = true;
                continue;
            } else if (c == 'Z') {
                sb.append("boolean");
            } else if (c == 'B') {
                sb.append("byte");
            } else if (c == 'C') {
                sb.append("char");
            } else if (c == 'D') {
                sb.append("double");
            } else if (c == 'F') {
                sb.append("float");
            } else if (c == 'I') {
                sb.append("int");
            } else if (c == 'J') {
                sb.append("long");
            } else if (c == 'S') {
                sb.append("short");
            } else if (c == 'L') { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                if (forURL) {
                    sb.append(typeName);
                } else {
                    sb.append(NbUtilities.stripPackage(typeName));
                }

                i = semicolon;
            }

            if (nextIsAnArray) {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1) {
                sb.append(DELIMITER);
            }

        }

        return sb.toString();
    }

    public String document(ParserResult info, ElementHandle element) {
        LOG.log(Level.FINEST, "document(), ElementHandle : {0}", element);

        String error = NbBundle.getMessage(CompletionHandler.class, "GroovyCompletion_NoJavaDocFound");
        String doctext = null;

        if (element instanceof AstMethodElement) {
            AstMethodElement ame = (AstMethodElement) element;

            String base = "";

            String javadoc = getGroovyJavadocBase();
            if (jdkJavaDocBase != null && ame.isGDK() == false) {
                base = jdkJavaDocBase;
            } else if (javadoc != null && ame.isGDK() == true) {
                base = javadoc;
            } else {
                LOG.log(Level.FINEST, "Neither JDK nor GDK or error locating: {0}", ame.isGDK());
                return error;
            }

            MetaMethod mm = ame.getMethod();

            // enable this to troubleshoot subtle differences in JDK/GDK signatures
            printMethod(mm);

            // figure out who originally defined this method

            String className;

            if (ame.isGDK()) {
                className = mm.getDeclaringClass()/*.getCachedClass()*/.getName();
            } else {

                String declName = null;

                if (mm != null) {
                    CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        declName = cc.getName();
                    }
                    /*CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        Class clz = cc.getCachedClass();
                        if (clz != null) {
                            declName = clz.getName();
                        }
                    }*/
                }

                if (declName != null) {
                    className = declName;
                } else {
                    className = ame.getClz().getName();
                }
            }

            // create path from fq java package name:
            // java.lang.String -> java/lang/String.html
            String classNamePath = className.replace(".", "/");
            classNamePath = classNamePath + ".html"; // NOI18N

            // if the file can be located in the GAPI folder prefer it
            // over the JDK
            if (!ame.isGDK()) {

                URL url;
                File testFile;

                String apiDoc = getGroovyApiDocBase();
                try {
                    url = new URL(apiDoc + classNamePath);
                    testFile = new File(url.toURI());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINEST, "MalformedURLException: {0}", ex);
                    return error;
                } catch (URISyntaxException uriEx) {
                    LOG.log(Level.FINEST, "URISyntaxException: {0}", uriEx);
                    return error;
                }

                if (testFile != null && testFile.exists()) {
                    base = apiDoc;
                }
            }

            // create the signature-string of the method
            String sig = getMethodSignature(ame.getMethod(), true, ame.isGDK());
            String printSig = getMethodSignature(ame.getMethod(), false, ame.isGDK());

            String urlName = base + classNamePath + "#" + sig;

            try {
                LOG.log(Level.FINEST, "Trying to load URL = {0}", urlName); // NOI18N
                doctext = HTMLJavadocParser.getJavadocText(
                    new URL(urlName),
                    false,
                    ame.isGDK());
            } catch (MalformedURLException ex) {
                LOG.log(Level.FINEST, "document(), URL trouble: {0}", ex); // NOI18N
                return error;
            }

            // If we could not find a suitable JavaDoc for the method - say so.
            if (doctext == null) {
                return error;
            }

            doctext = "<h3>" + className + "." + printSig + "</h3><BR>" + doctext;
        }
        return doctext;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        // pass the original handle back. That's better than to throw an unsupported-exception.
        return originalHandle;
    }

    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '.') {
            return QueryType.COMPLETION;
        }

        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        LOG.log(Level.FINEST, "parameters(), caretOffset = {0}", caretOffset); // NOI18N

        // here we need to calculate the list of parameters for the methods under the caret.
        // proposal seems to be null all the time.

        List<String> paramList = new ArrayList<String>();

        AstPath path = getPathFromInfo(caretOffset, info);

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        if (path != null) {

            ArgumentListExpression ael = getSurroundingArgumentList(path);

            if (ael != null) {

                List<ASTNode> children = AstUtilities.children(ael);

                // populate list with *all* parameters, but let index and offset
                // point to a specific parameter.

                int idx = 1;
                int index = -1;
                int offset = -1;

                for (ASTNode node : children) {
                    OffsetRange range = AstUtilities.getRange(node, doc);
                    paramList.add(node.getText());

                    if (range.containsInclusive(caretOffset)) {
                        offset = range.getStart();
                        index = idx;
                    }

                    idx++;
                }

                // calculate the parameter we are dealing with

                if (paramList != null && !paramList.isEmpty()) {
                    return new ParameterInfo(paramList, index, offset);
                }
            } else {
                LOG.log(Level.FINEST, "ArgumentListExpression ==  null"); // NOI18N
                return ParameterInfo.NONE;
            }

        } else {
            LOG.log(Level.FINEST, "path ==  null"); // NOI18N
            return ParameterInfo.NONE;
        }

        return ParameterInfo.NONE;

    }

    // FIXME make it ordinary class and/or split it
    static class CompletionRequest {

        ParserResult info;
        int lexOffset;
        int astOffset;
        BaseDocument doc;
        String prefix = "";
        CaretLocation location;
        boolean scriptMode;
        boolean behindImport;
        CompletionContext ctx;
        AstPath path;
        AstPath beforeDotPath;
        ClassNode declaringClass;
        DotCompletionContext dotContext;

        public boolean isBehindDot() {
            return dotContext != null;
        }

    }

    private static class TypeHolder {

        private final String name;

        private final ElementKind kind;

        public TypeHolder(String name, ElementKind kind) {
            this.name = name;
            this.kind = kind;
        }

        public ElementKind getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeHolder other = (TypeHolder) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

    }

}
