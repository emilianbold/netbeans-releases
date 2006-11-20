/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.*;
import org.openide.filesystems.FileObject;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Registry;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionProvider implements CompletionProvider {
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (".".equals(typedText)) {
            if (Utilities.isJavaContext(component, component.getSelectionStart() - 1))
                return COMPLETION_QUERY_TYPE;
        }
        return 0;
    }
    
    public CompletionTask createTask(int type, JTextComponent component) {
        if ((type & COMPLETION_QUERY_TYPE) != 0 || type == TOOLTIP_QUERY_TYPE || type == DOCUMENTATION_QUERY_TYPE)
            return new AsyncCompletionTask(new JavaCompletionQuery(type, component.getSelectionStart()), component);
        return null;
    }
    
    static CompletionTask createDocTask(ElementHandle element) {
        JavaCompletionQuery query = new JavaCompletionQuery(DOCUMENTATION_QUERY_TYPE, -1);
        query.element = element;
        return new AsyncCompletionTask(query, Registry.getMostActiveComponent());
    }
    
    public static List<? extends CompletionItem> query(JavaSource source, int queryType, int offset, int substitutionOffset) throws IOException {
        assert source != null;
        assert (queryType & COMPLETION_QUERY_TYPE) != 0;
        JavaCompletionQuery query = new JavaCompletionQuery(queryType, offset);
        source.runUserActionTask(query, false);
        if (offset != substitutionOffset) {
            for (JavaCompletionItem jci : query.results) {
                jci.substitutionOffset += (substitutionOffset - offset);
            }
        }
        return query.results;
    }
    
    static final class JavaCompletionQuery extends AsyncCompletionQuery implements CancellableTask<CompilationController> {
        
        private static final String ERROR = "<error>"; //NOI18N
        private static final String INIT = "<init>"; //NOI18N
        private static final String SPACE = " "; //NOI18N
        private static final String COLON = ":"; //NOI18N
        private static final String SEMI = ";"; //NOI18N
        
        private static final String ABSTRACT_KEYWORD = "abstract"; //NOI18N
        private static final String ASSERT_KEYWORD = "assert"; //NOI18N
        private static final String BOOLEAN_KEYWORD = "boolean"; //NOI18N
        private static final String BREAK_KEYWORD = "break"; //NOI18N
        private static final String BYTE_KEYWORD = "byte"; //NOI18N
        private static final String CASE_KEYWORD = "case"; //NOI18N
        private static final String CATCH_KEYWORD = "catch"; //NOI18N
        private static final String CHAR_KEYWORD = "char"; //NOI18N
        private static final String CLASS_KEYWORD = "class"; //NOI18N
        private static final String CONTINUE_KEYWORD = "continue"; //NOI18N
        private static final String DEFAULT_KEYWORD = "default"; //NOI18N
        private static final String DOUBLE_KEYWORD = "double"; //NOI18N
        private static final String ENUM_KEYWORD = "enum"; //NOI18N
        private static final String EXTENDS_KEYWORD = "extends"; //NOI18N
        private static final String FALSE_KEYWORD = "false"; //NOI18N
        private static final String FINAL_KEYWORD = "final"; //NOI18N
        private static final String FINALLY_KEYWORD = "finally"; //NOI18N
        private static final String FLOAT_KEYWORD = "float"; //NOI18N
        private static final String FOR_KEYWORD = "for"; //NOI18N
        private static final String IF_KEYWORD = "if"; //NOI18N
        private static final String IMPLEMENTS_KEYWORD = "implements"; //NOI18N
        private static final String IMPORT_KEYWORD = "import"; //NOI18N
        private static final String INSTANCEOF_KEYWORD = "instanceof"; //NOI18N
        private static final String INT_KEYWORD = "int"; //NOI18N
        private static final String INTERFACE_KEYWORD = "interface"; //NOI18N
        private static final String LONG_KEYWORD = "long"; //NOI18N
        private static final String NATIVE_KEYWORD = "native"; //NOI18N
        private static final String NEW_KEYWORD = "new"; //NOI18N
        private static final String NULL_KEYWORD = "null"; //NOI18N
        private static final String PACKAGE_KEYWORD = "package"; //NOI18N
        private static final String PRIVATE_KEYWORD = "private"; //NOI18N
        private static final String PROTECTED_KEYWORD = "protected"; //NOI18N
        private static final String PUBLIC_KEYWORD = "public"; //NOI18N
        private static final String RETURN_KEYWORD = "return"; //NOI18N
        private static final String SHORT_KEYWORD = "short"; //NOI18N
        private static final String STATIC_KEYWORD = "static"; //NOI18N
        private static final String STRICT_KEYWORD = "strictfp"; //NOI18N
        private static final String SUPER_KEYWORD = "super"; //NOI18N
        private static final String SWITCH_KEYWORD = "switch"; //NOI18N
        private static final String SYNCHRONIZED_KEYWORD = "synchronized"; //NOI18N
        private static final String THIS_KEYWORD = "this"; //NOI18N
        private static final String THROW_KEYWORD = "throw"; //NOI18N
        private static final String THROWS_KEYWORD = "throws"; //NOI18N
        private static final String TRANSIENT_KEYWORD = "transient"; //NOI18N
        private static final String TRUE_KEYWORD = "true"; //NOI18N
        private static final String TRY_KEYWORD = "try"; //NOI18N
        private static final String VOID_KEYWORD = "void"; //NOI18N
        private static final String VOLATILE_KEYWORD = "volatile"; //NOI18N
        private static final String WHILE_KEYWORD = "while"; //NOI18N
        
        private static final String[] PRIM_KEYWORDS = new String[] {
            BOOLEAN_KEYWORD, BYTE_KEYWORD, CHAR_KEYWORD, DOUBLE_KEYWORD,
            FLOAT_KEYWORD, INT_KEYWORD, LONG_KEYWORD, SHORT_KEYWORD
        };
        
        private static final String[] VALUE_KEYWORDS = new String[] {
            FALSE_KEYWORD, NULL_KEYWORD, TRUE_KEYWORD
        };

        private static final String[] STATEMENT_KEYWORDS = new String[] {
            FOR_KEYWORD, SWITCH_KEYWORD, SYNCHRONIZED_KEYWORD, TRY_KEYWORD,
            VOID_KEYWORD, WHILE_KEYWORD
        };
        
        private static final String[] STATEMENT_SPACE_KEYWORDS = new String[] {
            ASSERT_KEYWORD, NEW_KEYWORD, THROW_KEYWORD
        };
        
        private static final String[] BLOCK_KEYWORDS = new String[] {
            ASSERT_KEYWORD, CLASS_KEYWORD, FINAL_KEYWORD, NEW_KEYWORD,
            THROW_KEYWORD
        };

        private static final String[] CLASS_BODY_KEYWORDS = new String[] {
            ABSTRACT_KEYWORD, CLASS_KEYWORD, ENUM_KEYWORD, FINAL_KEYWORD,
            INTERFACE_KEYWORD, NATIVE_KEYWORD, PRIVATE_KEYWORD, PROTECTED_KEYWORD,
            PUBLIC_KEYWORD, STATIC_KEYWORD, STRICT_KEYWORD, SYNCHRONIZED_KEYWORD,
            TRANSIENT_KEYWORD, VOID_KEYWORD, VOLATILE_KEYWORD
        };
        
        private List<JavaCompletionItem> results;
        private JToolTip toolTip;
        private CompletionDocumentation documentation;
        private int anchorOffset;

        private JTextComponent component;

        private int queryType;
        private int caretOffset;
        private String filterPrefix;
        
        private ElementHandle element;
        
        private JavaCompletionQuery(int queryType, int caretOffset) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int newCaretOffset = component.getSelectionStart();
            if (newCaretOffset >= caretOffset) {
                try {
                    if (isJavaIdentifierPart(component.getDocument().getText(caretOffset, newCaretOffset - caretOffset)))
                        return;
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }
        
        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                this.caretOffset = caretOffset;
                if (queryType == TOOLTIP_QUERY_TYPE || Utilities.isJavaContext(component, caretOffset)) {
                    results = null;
                    documentation = null;
                    toolTip = null;
                    anchorOffset = -1;
                    JavaSource js = JavaSource.forDocument(doc);
                    if (queryType == DOCUMENTATION_QUERY_TYPE && element != null) {
                        FileObject fo = SourceUtils.getFile(element, js.getClasspathInfo());
                        if (fo != null)
                            js = JavaSource.forFileObject(fo);
                    }
                    js.runUserActionTask(this, (queryType & COMPLETION_QUERY_TYPE) == 0);
                    if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                        if (results != null)
                            resultSet.addAllItems(results);
                    } else if (queryType == TOOLTIP_QUERY_TYPE) {
                        if (toolTip != null)
                            resultSet.setToolTip(toolTip);
                    } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                        if (documentation != null)
                            resultSet.setDocumentation(documentation);
                    }
                    if (anchorOffset > -1)
                        resultSet.setAnchorOffset(anchorOffset);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                resultSet.finish();
            }
        }
        
        @Override
        protected boolean canFilter(JTextComponent component) {
            filterPrefix = null;
            int newOffset = component.getSelectionStart();
            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                if (newOffset >= caretOffset) {
                    if (anchorOffset > -1) {
                        try {
                            String prefix = component.getDocument().getText(anchorOffset, newOffset - anchorOffset);
                            if (isJavaIdentifierPart(prefix))
                                filterPrefix = prefix;
                        } catch (BadLocationException e) {}
                    }
                }
                return filterPrefix != null;
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                try {
                    if (newOffset - caretOffset > 0)
                        filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
                    else if (newOffset - caretOffset < 0)
                        filterPrefix = component.getDocument().getText(newOffset, caretOffset - newOffset);
                } catch (BadLocationException ex) {}
                return (filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1); // NOI18N
            }
            return false;
        }
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null)
                        resultSet.addAllItems(getFilteredData(results, filterPrefix));
                } else if (queryType == TOOLTIP_QUERY_TYPE) {
                    resultSet.setToolTip(toolTip);
                }
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }
        
        public void run(CompilationController controller) throws Exception {
            if ((queryType & COMPLETION_QUERY_TYPE) != 0)
                resolveCompletion(controller);
            else if (queryType == TOOLTIP_QUERY_TYPE)
                resolveToolTip(controller);
            else if (queryType == DOCUMENTATION_QUERY_TYPE)
                resolveDocumentation(controller);
        }
        
        public void cancel() {
        }
        
        private void resolveToolTip(final CompilationController controller) throws IOException {
            Env env = getCompletionEnvironment(controller, false);
            Tree lastTree = null;
            int offset = env.getOffset();
            TreePath path = env.getPath();
            while (path != null) {
                Tree tree = path.getLeaf();
                if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mi = (MethodInvocationTree)tree;
                    CompilationUnitTree root = env.getRoot();
                    SourcePositions sourcePositions = env.getSourcePositions();
                    int startPos = lastTree != null ? (int)sourcePositions.getStartPosition(root, lastTree) : offset;
                    List<Tree> argTypes = getArgumentsUpToPos(env, mi.getArguments(), (int)sourcePositions.getEndPosition(root, mi.getMethodSelect()), startPos);
                    if (argTypes != null) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeMirror[] types = new TypeMirror[argTypes.size()];
                        int j = 0;
                        for (Tree t : argTypes)
                            types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                        List<List<String>> params = null;
                        Tree mid = mi.getMethodSelect();
                        path = new TreePath(path, mid);
                        switch (mid.getKind()) {
                            case MEMBER_SELECT: {
                                ExpressionTree exp = ((MemberSelectTree)mid).getExpression();
                                path = new TreePath(path, exp);
                                Trees trees = controller.getTrees();
                                final TypeMirror type = trees.getTypeMirror(path);
                                final Element element = trees.getElement(path);
                                final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface());
                                final Scope scope = env.getScope();
                                final TreeUtilities tu = controller.getTreeUtilities();
                                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                    public boolean accept(Element e, TypeMirror t) {
                                        return (!isStatic || e.getModifiers().contains(STATIC) || e.getKind() == CONSTRUCTOR) && tu.isAccessible(scope, e, t);
                                    }
                                };
                                params = getMatchingParams(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree)mid).getIdentifier().toString(), types, controller.getTypes());
                                break;
                            }
                            case IDENTIFIER: {
                                final Scope scope = env.getScope();
                                final TreeUtilities tu = controller.getTreeUtilities();
                                final TypeElement enclClass = scope.getEnclosingClass();
                                final boolean isStatic = enclClass != null ? tu.isStaticContext(scope) : false;
                                final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
                                final ExecutableElement method = scope.getEnclosingMethod();
                                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                    public boolean accept(Element e, TypeMirror t) {
                                        switch (e.getKind()) {
                                            case LOCAL_VARIABLE:
                                            case EXCEPTION_PARAMETER:
                                            case PARAMETER:
                                                return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                                        !illegalForwardRefs.contains(e);
                                            case FIELD:
                                                if (illegalForwardRefs.contains(e))
                                                    return false;
                                                if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                                                    return !isStatic;
                                            default:
                                                return (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                                        }
                                    }
                                };
                                String name = ((IdentifierTree)mid).getName().toString();
                                if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                                    TypeMirror superclass = enclClass.getSuperclass();
                                    params = getMatchingParams(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types, controller.getTypes());
                                } else {
                                    params = getMatchingParams(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), THIS_KEYWORD.equals(name) ? INIT : name, types, controller.getTypes());
                                }
                                break;
                            }
                        }
                        if (params != null)
                            toolTip = new MethodParamsTipPaintComponent(params, types.length);
                        startPos = (int)sourcePositions.getEndPosition(env.getRoot(), mi.getMethodSelect());
                        anchorOffset = controller.getText().indexOf('(', startPos); //NOI18N
                        return;
                    }
                } else if (tree.getKind() == Tree.Kind.NEW_CLASS) {
                    NewClassTree nc = (NewClassTree)tree;
                    CompilationUnitTree root = env.getRoot();
                    SourcePositions sourcePositions = env.getSourcePositions();
                    int startPos = lastTree != null ? (int)sourcePositions.getStartPosition(root, lastTree) : offset;
                    List<Tree> argTypes = getArgumentsUpToPos(env, nc.getArguments(), (int)sourcePositions.getEndPosition(root, nc.getIdentifier()), startPos);
                    if (argTypes != null) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeMirror[] types = new TypeMirror[argTypes.size()];
                        int j = 0;
                        for (Tree t : argTypes)
                            types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                        path = new TreePath(path, nc.getIdentifier());
                        Trees trees = controller.getTrees();
                        final TypeMirror type = trees.getTypeMirror(path);
                        final Scope scope = env.getScope();
                        final TreeUtilities tu = controller.getTreeUtilities();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            public boolean accept(Element e, TypeMirror t) {
                                return e.getKind() == CONSTRUCTOR && tu.isAccessible(scope, e, t);
                            }
                        };
                        List<List<String>> params = getMatchingParams(type, controller.getElementUtilities().getMembers(type, acceptor), INIT, types, controller.getTypes());
                        if (params != null)
                            toolTip = new MethodParamsTipPaintComponent(params, types.length);
                        startPos = (int)sourcePositions.getEndPosition(env.getRoot(), nc.getIdentifier());
                        anchorOffset = controller.getText().indexOf('(', startPos); //NOI18N
                        return;
                    }
                }
                lastTree = tree;
                path = path.getParentPath();
            }
        }
        
        private void resolveDocumentation(CompilationController controller) throws IOException {            
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            Element el = element != null ? element.resolve(controller) : controller.getTrees().getElement(getCompletionEnvironment(controller, false).getPath());
            if (el != null) {
                documentation = JavaCompletionDoc.create(controller.getElementUtilities().javaDocFor(el), controller);
            }
        }
        
        private void resolveCompletion(CompilationController controller) throws IOException {
            Env env = getCompletionEnvironment(controller, true);
            results = new ArrayList<JavaCompletionItem>();
            anchorOffset = env.getOffset();
            TreePath path = env.getPath();
            switch(path.getLeaf().getKind()) {
                case COMPILATION_UNIT:
                    insideCompilationUnit(env);
                    break;
                case IMPORT:
                    insideImport(env);
                    break;
                case CLASS:
                    insideClass(env);
                    break;
                case VARIABLE:
                    insideVariable(env);
                    break;
                case METHOD:
                    insideMethod(env);
                    break;
                case MODIFIERS:
                    insideModifiers(env, path);
                    break;
                case ANNOTATION:
                    insideAnnotation(env);
                    break;
                case TYPE_PARAMETER:
                    insideTypeParameter(env);
                    break;
                case PARAMETERIZED_TYPE:
                    insideParameterizedType(env, path);
                    break;
                case UNBOUNDED_WILDCARD:
                case EXTENDS_WILDCARD:
                case SUPER_WILDCARD:
                    TreePath parentPath = path.getParentPath();
                    if (parentPath.getLeaf().getKind() == Tree.Kind.PARAMETERIZED_TYPE)
                        insideParameterizedType(env, parentPath);
                    break;
                case BLOCK:
                    insideBlock(env);
                    break;
                case MEMBER_SELECT:
                    insideMemberSelect(env);
                    break;
                case METHOD_INVOCATION:
                    insideMethodInvocation(env);
                    break;
                case NEW_CLASS:
                    insideNewClass(env);
                    break;
                case ASSERT:
                case RETURN:
                case THROW:                    
                    localResult(env);
                    addValueKeywords(env);
                    break;
                case CATCH:
                    insideCatch(env);
                    break;
                case IF:
                    insideIf(env);
                    break;
                case WHILE_LOOP:
                    insideWhile(env);
                    break;
                case FOR_LOOP:
                    insideFor(env);
                    break;
                case ENHANCED_FOR_LOOP:
                    insideForEach(env);
                    break;
                case SWITCH:
                    insideSwitch(env);
                    break;
                case CASE:
                    insideCase(env);
                    break;
                case PARENTHESIZED:
                    insideParens(env);
                    break;
                case TYPE_CAST:
                    insideExpression(env, path);
                    break;
                case INSTANCE_OF:
                    insideTypeCheck(env);
                    break;
                case ARRAY_ACCESS:
                    insideArrayAccess(env);
                    break;
                case NEW_ARRAY:
                    insideNewArray(env);
                    break;
                case ASSIGNMENT:
                    insideAssignment(env);
                    break;
                case MULTIPLY_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                case REMAINDER_ASSIGNMENT:
                case PLUS_ASSIGNMENT:
                case MINUS_ASSIGNMENT:
                case LEFT_SHIFT_ASSIGNMENT:
                case RIGHT_SHIFT_ASSIGNMENT:
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                case AND_ASSIGNMENT:
                case XOR_ASSIGNMENT:
                case OR_ASSIGNMENT:
                    insideCompoundAssignment(env);
                    break;
                case PREFIX_INCREMENT:
                case PREFIX_DECREMENT:
                case UNARY_PLUS:
                case UNARY_MINUS:
                case BITWISE_COMPLEMENT:
                case LOGICAL_COMPLEMENT:
                    localResult(env);
                    break;
                case AND:
                case CONDITIONAL_AND:
                case CONDITIONAL_OR:
                case DIVIDE:
                case EQUAL_TO:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case LEFT_SHIFT:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case MINUS:
                case MULTIPLY:
                case NOT_EQUAL_TO:
                case OR:
                case PLUS:
                case REMAINDER:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case XOR:
                    insideBinaryTree(env);
                    break;
                case CONDITIONAL_EXPRESSION:
                    insideConditionalExpression(env);
                    break;
                case EXPRESSION_STATEMENT:
                    insideExpressionStatement(env);
                    break;
            }
        }
        
        private void insideCompilationUnit(Env env) throws IOException {
            int offset = env.getOffset();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            Tree pkg = root.getPackageName();
            if (pkg == null || offset <= sourcePositions.getStartPosition(root, root)) {
                addKeywordsForCU(env);
                return;
            }
            if (offset <= sourcePositions.getStartPosition(root, pkg)) {
                addPackages(env, env.getPrefix());
            } else if (env.getController().getText().substring((int)sourcePositions.getEndPosition(root, pkg), offset).trim().startsWith(SEMI)) {
                addKeywordsForCU(env);
            }
        }
        
        private void insideImport(Env env) {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            ImportTree im = (ImportTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            if (offset <= sourcePositions.getStartPosition(root, im.getQualifiedIdentifier())) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, im, offset);
                if (last.token().id() == JavaTokenId.IMPORT && Utilities.startsWith(STATIC_KEYWORD, prefix))
                    addKeyword(env, STATIC_KEYWORD, SPACE);
                addPackages(env, prefix);
            }            
        }
        
        private void insideClass(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            ClassTree cls = (ClassTree)path.getLeaf();
            CompilationController controller = env.getController();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int startPos = (int)sourcePositions.getEndPosition(root, cls.getModifiers()) + 1;
            if (startPos <= 0)
                startPos = (int)sourcePositions.getStartPosition(root, cls);
            String headerText = controller.getText().substring(startPos, offset);
            int idx = headerText.indexOf('{'); //NOI18N
            if (idx >= 0) {
                addKeywordsForClassBody(env);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                return;
            }
            TreeUtilities tu = controller.getTreeUtilities();
            Tree lastImpl = null;
            for (Tree impl : cls.getImplementsClause()) {
                int implPos = (int)sourcePositions.getEndPosition(root, impl);
                if (implPos == Diagnostic.NOPOS || offset <= implPos)
                    break;
                lastImpl = impl;
                startPos = implPos;
            }
            if (lastImpl != null) {
                if (controller.getText().substring(startPos, offset).trim().equals(",")) { //NOI18N
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    addTypes(env, EnumSet.of(INTERFACE), null, controller.getTrees().getElement(path));
                }
                return;
            }
            Tree ext = cls.getExtendsClause();
            if (ext != null) {
                int extPos = (int)sourcePositions.getEndPosition(root, ext);
                if (extPos != Diagnostic.NOPOS && offset > extPos) {
                    headerText = controller.getText().substring(extPos + 1, offset).trim();
                    if (IMPLEMENTS_KEYWORD.equals(headerText)) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, EnumSet.of(INTERFACE), null, controller.getTrees().getElement(path));
                    } else if (Utilities.startsWith(IMPLEMENTS_KEYWORD, prefix)) {
                        addKeyword(env, IMPLEMENTS_KEYWORD, SPACE);
                    }
                    return;
                }
            }
            TypeParameterTree lastTypeParam = null;
            for (TypeParameterTree tp : cls.getTypeParameters()) {
                int tpPos = (int)sourcePositions.getEndPosition(root, tp);
                if (tpPos == Diagnostic.NOPOS || offset <= tpPos)
                    break;
                lastTypeParam = tp;
                startPos = tpPos;
            }
            if (lastTypeParam != null) {
                headerText = controller.getText().substring(startPos, offset);
                idx = headerText.indexOf('>'); //NOI18N
                if (idx > -1) { //NOI18N
                    headerText = headerText.substring(idx + 1).trim();
                    if (EXTENDS_KEYWORD.equals(headerText)) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, tu.isInterface(cls) ? EnumSet.of(INTERFACE) : EnumSet.of(CLASS), null, controller.getTrees().getElement(path));
                    } else if (IMPLEMENTS_KEYWORD.equals(headerText)) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, EnumSet.of(INTERFACE), null, controller.getTrees().getElement(path));
                    } else {
                        if (!tu.isAnnotation(cls)) {
                            if (!tu.isEnum(cls) && Utilities.startsWith(EXTENDS_KEYWORD, prefix))
                                addKeyword(env, EXTENDS_KEYWORD, SPACE);
                            if (!tu.isInterface(cls) && Utilities.startsWith(IMPLEMENTS_KEYWORD, prefix))
                                addKeyword(env, IMPLEMENTS_KEYWORD, SPACE);
                        }
                    }
                } else {
                    if (lastTypeParam.getBounds().isEmpty()) {
                        if (Utilities.startsWith(EXTENDS_KEYWORD, prefix))
                            addKeyword(env, EXTENDS_KEYWORD, SPACE);
                    }
                }
                return;
            }            
            TokenSequence<JavaTokenId> lastNonWhitespaceToken = findLastNonWhitespaceToken(env, startPos, offset);
            if (lastNonWhitespaceToken != null) {
                switch (lastNonWhitespaceToken.token().id()) {
                    case EXTENDS:
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, tu.isInterface(cls) ? EnumSet.of(INTERFACE) : EnumSet.of(CLASS), null, controller.getTrees().getElement(path));
                        break;
                    case IMPLEMENTS:
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, EnumSet.of(INTERFACE), null, controller.getTrees().getElement(path));
                        break;
                    case IDENTIFIER:
                        if (!tu.isAnnotation(cls)) {
                            if (!tu.isEnum(cls) && Utilities.startsWith(EXTENDS_KEYWORD, prefix))
                                addKeyword(env, EXTENDS_KEYWORD, SPACE);
                            if (!tu.isInterface(cls) && Utilities.startsWith(IMPLEMENTS_KEYWORD, prefix))
                                addKeyword(env, IMPLEMENTS_KEYWORD, SPACE);
                        }
                        break;
                }
                return;
            }
            lastNonWhitespaceToken = findLastNonWhitespaceToken(env, (int)sourcePositions.getStartPosition(root, cls), offset);
            if (lastNonWhitespaceToken != null && lastNonWhitespaceToken.token().id() == JavaTokenId.AT) {
                if (Utilities.startsWith(INTERFACE_KEYWORD, env.getPrefix()))
                    addKeyword(env, INTERFACE_KEYWORD, SPACE);
                addTypes(env, EnumSet.of(ANNOTATION_TYPE), null, null);
            } else if (path.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                addClassModifiers(env, cls.getModifiers().getFlags());
            } else {
                addMemberModifiers(env, cls.getModifiers().getFlags(), false);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
            }
        }
        
        private void insideVariable(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            VariableTree var = (VariableTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            boolean isLocal = path.getParentPath().getLeaf().getKind() != Tree.Kind.CLASS;
            Tree type = var.getType();
            int typePos = type.getKind() == Tree.Kind.ERRONEOUS && ((ErroneousTree)type).getErrorTrees().isEmpty() ?
                (int)sourcePositions.getEndPosition(root, type) : (int)sourcePositions.getStartPosition(root, type);            
            if (offset <= typePos) {
                addMemberModifiers(env, var.getModifiers().getFlags(), isLocal);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                return;
            }
            Tree init = unwrapErrTree(var.getInitializer());
            if ((init == null || offset <= sourcePositions.getStartPosition(root, init))) {
                String text = env.getController().getText().substring((int)sourcePositions.getEndPosition(root, type), offset).trim();
                if (text.length() == 0) {
                    insideExpression(env, new TreePath(path, type));
                } else if (text.endsWith("=")) { //NOI18N
                    localResult(env);
                    addValueKeywords(env);                    
                }
            }
        }
        
        private void insideMethod(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            MethodTree mth = (MethodTree)path.getLeaf();
            CompilationController controller = env.getController();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int startPos = (int)sourcePositions.getStartPosition(root, mth);
            Tree retType = mth.getReturnType();
            if (retType == null) {
                int modPos = (int)sourcePositions.getEndPosition(root, mth.getModifiers());
                if (modPos > startPos)
                    startPos = modPos;
                if (controller.getText().substring(startPos, offset).trim().length() == 0) {
                    addMemberModifiers(env, mth.getModifiers().getFlags(), false);
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                    return;
                }
            } else {
                if (offset <= sourcePositions.getStartPosition(root, retType)) {
                    addMemberModifiers(env, mth.getModifiers().getFlags(), false);
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                    return;
                }
                startPos = (int)sourcePositions.getEndPosition(root, retType) + 1;
            }
            Tree lastThr = null;
            for (Tree thr: mth.getThrows()) {
                int thrPos = (int)sourcePositions.getEndPosition(root, thr);
                if (thrPos == Diagnostic.NOPOS || offset <= thrPos)
                    break;
                lastThr = thr;
                startPos = thrPos;
            }
            if (lastThr != null) {
                if (controller.getText().substring(startPos, offset).trim().equals(",")) { //NOI18N
                    if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                        controller.toPhase(Phase.RESOLVED);
                        Set<TypeMirror> exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, mth.getBody()));
                        Trees trees = controller.getTrees();
                        Types types = controller.getTypes();
                        for (ExpressionTree thr : mth.getThrows()) {
                            TypeMirror t = trees.getTypeMirror(new TreePath(path, thr));
                            for (Iterator<TypeMirror> it = exs.iterator(); it.hasNext();)
                                if (types.isSubtype(it.next(), t))
                                    it.remove();
                            if (thr == lastThr)
                                break;
                        }
                        Elements elements = controller.getElements();
                        for (TypeMirror ex : exs)
                            if (ex.getKind() == TypeKind.DECLARED)
                                results.add(JavaCompletionItem.createTypeItem((TypeElement)((DeclaredType)ex).asElement(), (DeclaredType)ex, offset, true, elements.isDeprecated(((DeclaredType)ex).asElement())));
                    } else {
                        addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")), null); //NOI18N
                    }
                }
                return;
            }
            String headerText = controller.getText().substring(startPos, offset);
            int parStart = headerText.indexOf('('); //NOI18N
            if (parStart >= 0) {
                int parEnd = headerText.indexOf(')', parStart); //NOI18N
                if (parEnd > parStart) {
                    headerText = headerText.substring(parEnd + 1).trim();
                    if (THROWS_KEYWORD.equals(headerText)) {
                        if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                            controller.toPhase(Phase.RESOLVED);
                            Set<TypeMirror> exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, mth.getBody()));
                            Elements elements = controller.getElements();
                            for (TypeMirror ex : exs)
                                if (ex.getKind() == TypeKind.DECLARED)
                                    results.add(JavaCompletionItem.createTypeItem((TypeElement)((DeclaredType)ex).asElement(), (DeclaredType)ex, offset, true, elements.isDeprecated(((DeclaredType)ex).asElement())));
                        } else {
                            addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")), null); //NOI18N
                        }
                    } else {
                        Tree mthParent = path.getParentPath().getLeaf();
                        if (mthParent.getKind() == Tree.Kind.CLASS && controller.getTreeUtilities().isAnnotation((ClassTree)mthParent)) {
                            if (Utilities.startsWith(DEFAULT_KEYWORD, prefix))
                                addKeyword(env, DEFAULT_KEYWORD, SPACE);
                        } else {
                            if (Utilities.startsWith(THROWS_KEYWORD, prefix))
                                addKeyword(env, THROWS_KEYWORD, SPACE);
                        }
                    }
                } else {
                    for (VariableTree param : mth.getParameters()) {
                        int parPos = (int)sourcePositions.getEndPosition(root, param);
                        if (parPos == Diagnostic.NOPOS || offset <= parPos)
                            break;
                        parStart = parPos - startPos;
                    }
                    headerText = headerText.substring(parStart).trim();
                    if ("(".equals(headerText) || ",".equals(headerText)) { //NOI18N
                        addMemberModifiers(env, Collections.<Modifier>emptySet(), true);
                        addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                    }
                }
            }
        }
        
        private void insideModifiers(Env env, TreePath modPath) throws IOException {
            int offset = env.getOffset();
            ModifiersTree mods = (ModifiersTree)modPath.getLeaf();
            Set<Modifier> m = EnumSet.noneOf(Modifier.class);
            TokenSequence<JavaTokenId> ts = env.getController().getTreeUtilities().tokensFor(mods, env.getSourcePositions());
            JavaTokenId lastNonWhitespaceTokenId = null;
            while(ts.moveNext() && ts.offset() < offset) {
                lastNonWhitespaceTokenId = ts.token().id();
                switch (lastNonWhitespaceTokenId) {
                    case PUBLIC:
                        m.add(PUBLIC);
                        break;
                    case PROTECTED:
                        m.add(PROTECTED);
                        break;
                    case PRIVATE:
                        m.add(PRIVATE);
                        break;
                    case STATIC:
                        m.add(STATIC);
                        break;
                    case ABSTRACT:
                        m.add(ABSTRACT);
                        break;
                    case FINAL:
                        m.add(FINAL);
                        break;
                    case SYNCHRONIZED:
                        m.add(SYNCHRONIZED);
                        break;
                    case NATIVE:
                        m.add(NATIVE);
                        break;
                    case STRICTFP:
                        m.add(STRICTFP);
                        break;
                    case TRANSIENT:
                        m.add(TRANSIENT);
                        break;
                    case VOLATILE:
                        m.add(VOLATILE);
                        break;
                }                
            };            
            if (lastNonWhitespaceTokenId == JavaTokenId.AT) {
                if (Utilities.startsWith(INTERFACE_KEYWORD, env.getPrefix()))
                    addKeyword(env, INTERFACE_KEYWORD, SPACE);
                addTypes(env, EnumSet.of(ANNOTATION_TYPE), null, null);
                return;
            }            
            TreePath parentPath = modPath.getParentPath();
            Tree parent = parentPath.getLeaf();
            TreePath grandParentPath = parentPath.getParentPath();
            Tree grandParent = grandParentPath != null ? grandParentPath.getLeaf() : null;
            if (isTopLevelClass(parent, env.getRoot())) {
                addClassModifiers(env, m);
            } else if (parent.getKind() != Tree.Kind.VARIABLE || grandParent == null || grandParent.getKind() == Tree.Kind.CLASS) {
                addMemberModifiers(env, m, false);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
            } else if (parent.getKind() == Tree.Kind.VARIABLE && grandParent.getKind() == Tree.Kind.METHOD) {
                addMemberModifiers(env, m, true);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
            } else {
                localResult(env);
                addKeywordsForBlock(env);
            }
        }
        
        private void insideAnnotation(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            AnnotationTree ann = (AnnotationTree)path.getLeaf();
            CompilationController controller = env.getController();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int typeEndPos = (int)sourcePositions.getEndPosition(root, ann.getAnnotationType());
            if (offset <= typeEndPos) {
                if (Utilities.startsWith(INTERFACE_KEYWORD, prefix))
                    addKeyword(env, INTERFACE_KEYWORD, SPACE);
                addTypes(env, EnumSet.of(ANNOTATION_TYPE), null, null);
                return;
            }
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, ann, offset);
            if (ts == null || (ts.token().id() != JavaTokenId.LPAREN && ts.token().id() != JavaTokenId.COMMA))
                return;
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            Element annTypeElement = controller.getTrees().getElement(new TreePath(path, ann.getAnnotationType()));
            if (annTypeElement != null && annTypeElement.getKind() == ANNOTATION_TYPE) {
                HashSet<String> names = new HashSet<String>();
                for(ExpressionTree arg : ann.getArguments()) {
                    if (arg.getKind() == Tree.Kind.ASSIGNMENT && sourcePositions.getEndPosition(root, arg) < offset) {
                        ExpressionTree var = ((AssignmentTree)arg).getVariable();
                        if (var.getKind() == Tree.Kind.IDENTIFIER)
                            names.add(((IdentifierTree)var).getName().toString());
                    }
                }
                int hasOnlyValue = 0;
                Elements elements = controller.getElements();
                for(Element e : ((TypeElement)annTypeElement).getEnclosedElements()) {
                    if (e.getKind() == METHOD) {
                        String name = e.getSimpleName().toString();
                        if (hasOnlyValue < 2)
                            hasOnlyValue += "value".equals(name) ? 1 : 2; //NOI18N
                        if (queryType != COMPLETION_SMART_QUERY_TYPE && !names.contains(name) && Utilities.startsWith(name, prefix))
                            results.add(JavaCompletionItem.createAttributeItem((ExecutableElement)e, (ExecutableType)e.asType(), offset, elements.isDeprecated(e)));
                    }
                }
                if (hasOnlyValue == 1 && names.size() == 0)
                    addLocalConstantsAndTypes(env);
            }
        }
        
        private void insideTypeParameter(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            TypeParameterTree tp = (TypeParameterTree)path.getLeaf();
            CompilationController controller = env.getController();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, tp, offset);
            if (ts != null) {
                switch(ts.token().id()) {
                    case EXTENDS:
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, EnumSet.of(CLASS, INTERFACE), null, controller.getTrees().getElement(path.getParentPath()));
                        break;
                    case AMP:
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        addTypes(env, EnumSet.of(INTERFACE), null, controller.getTrees().getElement(path.getParentPath()));
                        break;
                    case IDENTIFIER:
                        if (Utilities.startsWith(EXTENDS_KEYWORD, prefix) && ts.offset() == env.getSourcePositions().getStartPosition(env.getRoot(), tp))
                            addKeyword(env, EXTENDS_KEYWORD, SPACE);
                        break;
                }
            }
        }
        
        private void insideParameterizedType(Env env, TreePath ptPath) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            ParameterizedTypeTree ta = (ParameterizedTypeTree)ptPath.getLeaf();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, ta, offset);
            if (ts != null) {
                switch (ts.token().id()) {
                    case EXTENDS:
                    case SUPER:
                    case LT:
                    case COMMA:
                        addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                        break;
                    case QUESTION:
                        if (Utilities.startsWith(EXTENDS_KEYWORD, prefix))
                            addKeyword(env, EXTENDS_KEYWORD, SPACE);
                        if (Utilities.startsWith(SUPER_KEYWORD, prefix))
                            addKeyword(env, SUPER_KEYWORD, SPACE);
                        break;
                }
            }
        }
        
        private void insideBlock(Env env) throws IOException {
            int offset = env.getOffset();
            BlockTree bl = (BlockTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int blockPos = (int)sourcePositions.getStartPosition(root, bl);
            String text = env.getController().getText().substring(blockPos, offset);
            if (text.indexOf('{') < 0) { //NOI18N
                addMemberModifiers(env, Collections.singleton(STATIC), false);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                return;
            }
            StatementTree last = null;
            for(StatementTree stat : bl.getStatements()) {
                int pos = (int)sourcePositions.getStartPosition(root, stat);
                if (pos == Diagnostic.NOPOS || offset <= pos)
                    break;
                last = stat;
            }
            if (last != null && last.getKind() == Tree.Kind.TRY) {
                if (((TryTree)last).getFinallyBlock() == null) {
                    addKeyword(env, CATCH_KEYWORD, null);
                    addKeyword(env, FINALLY_KEYWORD, null);
                }
                if (((TryTree)last).getCatches().size() == 0)
                    return;
            }
            localResult(env);
            addKeywordsForBlock(env);
        }
        
        private void insideMemberSelect(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            MemberSelectTree fa = (MemberSelectTree)path.getLeaf();
            CompilationController controller = env.getController();
            CompilationUnitTree root = env.getRoot();
            SourcePositions sourcePositions = env.getSourcePositions();
            int expEndPos = (int)sourcePositions.getEndPosition(root, fa.getExpression());
            boolean afterDot = false;
            boolean afterLt = false;
            int openLtNum = 0;
            JavaTokenId lastNonWhitespaceTokenId = null;
            TokenSequence<JavaTokenId> ts = controller.getTokenHiearchy().tokenSequence(JavaTokenId.language());
            if (ts.move(expEndPos) > 0)
                ts.moveNext();
            do {
                if (ts.offset() >= offset) {
                    break;
                }
                switch (ts.token().id()) {
                    case DOUBLE_LITERAL:
                        if (ts.offset() != expEndPos || ts.token().text().charAt(0) != '.')
                            break;
                    case DOT:
                        afterDot = true;
                        break;
                    case LT:
                        afterLt = true;
                        openLtNum++;
                        break;
                    case GT:
                        openLtNum--;
                        break;
                    case GTGT:
                        openLtNum -= 2;
                        break;
                    case GTGTGT:
                        openLtNum -= 3;
                        break;
                }
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                        break;
                    default:
                        lastNonWhitespaceTokenId = ts.token().id();
                }
            } while (ts.moveNext());
            if (!afterDot) {
                if (expEndPos <= offset)
                    insideExpression(env, new TreePath(path, fa.getExpression()));
                return;
            }
            if (openLtNum > 0) {
                switch (lastNonWhitespaceTokenId) {
                    case QUESTION:
                        if (Utilities.startsWith(EXTENDS_KEYWORD, prefix))
                            addKeyword(env, EXTENDS_KEYWORD, SPACE);
                        if (Utilities.startsWith(SUPER_KEYWORD, prefix))
                            addKeyword(env, SUPER_KEYWORD, SPACE);
                        break;
                    case LT:
                    case COLON:
                    case EXTENDS:
                    case SUPER:
                        addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                        break;
                }
            } else if (lastNonWhitespaceTokenId != JavaTokenId.STAR) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TreePath parentPath = path.getParentPath();
                Tree parent = parentPath != null ? parentPath.getLeaf() : null;
                TreePath grandParentPath = parentPath != null ? parentPath.getParentPath() : null;
                Tree grandParent = grandParentPath != null ? grandParentPath.getLeaf() : null;
                ExpressionTree exp = fa.getExpression();
                TreePath expPath = new TreePath(path, exp);
                TypeMirror type = controller.getTrees().getTypeMirror(expPath);
                if (type != null) {
                    EnumSet<ElementKind> kinds;
                    DeclaredType baseType = null;
                    Set<TypeMirror> exs = null;
                    boolean inImport = false;
                    if (parent.getKind() == Tree.Kind.CLASS && ((ClassTree)parent).getExtendsClause() == fa) {
                        kinds = EnumSet.of(CLASS);
                    } else if (parent.getKind() == Tree.Kind.CLASS && ((ClassTree)parent).getImplementsClause().contains(fa)) {
                        kinds = EnumSet.of(INTERFACE);
                    } else if (parent.getKind() == Tree.Kind.IMPORT) {
                        inImport = true;
                        kinds = ((ImportTree)parent).isStatic() ? EnumSet.of(CLASS, ENUM, INTERFACE, ANNOTATION_TYPE, FIELD, METHOD, ENUM_CONSTANT) : EnumSet.of(CLASS, ANNOTATION_TYPE, ENUM, INTERFACE);
                    } else if (parent.getKind() == Tree.Kind.NEW_CLASS && ((NewClassTree)parent).getIdentifier() == fa) {
                        kinds = EnumSet.of(CLASS, INTERFACE);
                        if (grandParent.getKind() == Tree.Kind.THROW)
                            baseType = controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")); //NOI18N
                    } else if (parent.getKind() == Tree.Kind.PARAMETERIZED_TYPE && ((ParameterizedTypeTree)parent).getTypeArguments().contains(fa)) {
                        kinds = EnumSet.of(CLASS, ENUM, INTERFACE);
                    } else if (parent.getKind() == Tree.Kind.ANNOTATION) {
                        if (((AnnotationTree)parent).getAnnotationType() == fa) {
                            kinds = EnumSet.of(ANNOTATION_TYPE);
                        } else {
                            Iterator<? extends ExpressionTree> it = ((AnnotationTree)parent).getArguments().iterator();
                            if (it.hasNext()) {
                                ExpressionTree et = it.next();
                                if (et == fa || (et.getKind() == Tree.Kind.ASSIGNMENT && ((AssignmentTree)et).getExpression() == fa)) {
                                    Element el = controller.getTrees().getElement(expPath);
                                    if (el instanceof PackageElement)
                                        addPackageContent(env, (PackageElement)el, EnumSet.of(CLASS, ENUM, INTERFACE), null);
                                    else if (type.getKind() == TypeKind.DECLARED)
                                        addMemberConstantsAndTypes(env, (DeclaredType)type, el);
                                    return;
                                }
                            }
                            kinds = EnumSet.of(CLASS, ENUM, INTERFACE, FIELD, METHOD, ENUM_CONSTANT);
                        }
                    } else if (parent.getKind() == Tree.Kind.ASSIGNMENT && ((AssignmentTree)parent).getExpression() == fa && grandParent != null && grandParent.getKind() == Tree.Kind.ANNOTATION) {
                        Element el = controller.getTrees().getElement(expPath);
                        if (el instanceof PackageElement)
                            addPackageContent(env, (PackageElement)el, EnumSet.of(CLASS, ENUM, INTERFACE), null);
                        else if (type.getKind() == TypeKind.DECLARED)
                            addMemberConstantsAndTypes(env, (DeclaredType)type, el);
                        return;
                    } else if (parent.getKind() == Tree.Kind.VARIABLE && ((VariableTree)parent).getType() == fa && grandParent.getKind() == Tree.Kind.CATCH) {
                        if (queryType == COMPLETION_SMART_QUERY_TYPE)
                            exs = controller.getTreeUtilities().getUncaughtExceptions(grandParentPath.getParentPath());
                        kinds = EnumSet.of(CLASS, INTERFACE);
                        baseType = controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")); //NOI18N
                    } else if (parent.getKind() == Tree.Kind.METHOD && ((MethodTree)parent).getThrows().contains(fa)) {
                        Types types = controller.getTypes();
                        if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                            controller.toPhase(Phase.RESOLVED);
                            exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, ((MethodTree)parent).getBody()));
                            Trees trees = controller.getTrees();
                            for (ExpressionTree thr : ((MethodTree)parent).getThrows()) {
                                if (sourcePositions.getEndPosition(root, thr) >= offset)
                                    break;
                                TypeMirror t = trees.getTypeMirror(new TreePath(path, thr));
                                for (Iterator<TypeMirror> it = exs.iterator(); it.hasNext();)
                                    if (types.isSubtype(it.next(), t))
                                        it.remove();
                            }
                        }
                        kinds = EnumSet.of(CLASS, INTERFACE);
                        baseType = controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")); //NOI18N
                    } else if (afterLt) {
                        kinds = EnumSet.of(METHOD);
                    } else {
                        kinds = EnumSet.of(CLASS, ENUM, INTERFACE, FIELD, METHOD, ENUM_CONSTANT);
                    }
                    switch (type.getKind()) {
                        case TYPEVAR:
                            type = ((TypeVariable)type).getUpperBound();
                            if (type == null)
                                return;
                            type = controller.getTypes().capture(type);
                        case ARRAY:
                        case DECLARED:
                        case BOOLEAN:
                        case BYTE:
                        case CHAR:
                        case DOUBLE:
                        case FLOAT:
                        case INT:
                        case LONG:
                        case SHORT:
                        case VOID:
                            boolean b = exp.getKind() == Tree.Kind.PARENTHESIZED || exp.getKind() == Tree.Kind.TYPE_CAST;
                            while(b) {
                                if (exp.getKind() == Tree.Kind.PARENTHESIZED) {
                                    exp = ((ParenthesizedTree)exp).getExpression();
                                    expPath = new TreePath(expPath, exp);
                                } else if (exp.getKind() == Tree.Kind.TYPE_CAST) {
                                    exp = ((TypeCastTree)exp).getExpression();
                                    expPath = new TreePath(expPath, exp);
                                } else {
                                    b = false;
                                }
                            }
                            Element el = controller.getTrees().getElement(expPath);
                            if (exs != null) {
                                Elements elements = controller.getElements();
                                for (TypeMirror ex : exs)
                                    if (ex.getKind() == TypeKind.DECLARED) {
                                        Element e = ((DeclaredType)ex).asElement();
                                        if (e.getEnclosingElement() == el && Utilities.startsWith(e.getSimpleName().toString(), prefix))
                                            results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)ex, offset, true, elements.isDeprecated(e)));
                                    }
                            } else {
                                if (el == null && exp.getKind() == Tree.Kind.PRIMITIVE_TYPE)
                                    el = controller.getTypes().boxedClass((PrimitiveType)type);
                                addMembers(env, type, el, kinds, baseType, inImport);
                            }
                            break;
                        default:
                            el = controller.getTrees().getElement(expPath);
                            if (el != null && el.getKind() == PACKAGE) {
                                if (queryType == COMPLETION_QUERY_TYPE) {
                                    if (parent.getKind() == Tree.Kind.NEW_CLASS && ((NewClassTree)parent).getIdentifier() == fa && prefix != null) {
                                        String typeName = Utilities.getElementName(el, true) + "." + prefix; //NOI18N
                                        TypeMirror tm = controller.getTreeUtilities().parseType(typeName, env.getScope().getEnclosingClass());
                                        if (tm != null && tm.getKind() == TypeKind.DECLARED)
                                            addMembers(env, tm, ((DeclaredType)tm).asElement(), EnumSet.of(CONSTRUCTOR), null, inImport);
                                    }
                                }
                                if (exs != null) {
                                    Elements elements = controller.getElements();
                                    for (TypeMirror ex : exs)
                                        if (ex.getKind() == TypeKind.DECLARED) {
                                            Element e = ((DeclaredType)ex).asElement();
                                            if (e.getEnclosingElement() == el && Utilities.startsWith(e.getSimpleName().toString(), prefix))
                                                results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)ex, offset, true, elements.isDeprecated(e)));
                                        }
                                } else {
                                    addPackageContent(env, (PackageElement)el, kinds, baseType);
                                }
                            }
                    }
                }
            }
        }
        
        private void insideMethodInvocation(Env env) throws IOException {
            MethodInvocationTree mi = (MethodInvocationTree)env.getPath().getLeaf();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, mi, env.getOffset());
            if (ts == null || (ts.token().id() != JavaTokenId.LPAREN && ts.token().id() != JavaTokenId.COMMA))
                return;
            addLocalMembersAndVars(env);
            addValueKeywords(env);
            if (queryType != COMPLETION_SMART_QUERY_TYPE) {
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                addPrimitiveTypeKeywords(env);
            }
        }
        
        private void insideNewClass(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            NewClassTree nc = (NewClassTree)path.getLeaf();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, nc, offset);
            if (ts != null) {
                switch(ts.token().id()) {
                    case NEW:
                        if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                            Set<TypeMirror> smarts = getSmartTypes(env);
                            if (smarts != null)
                                for (TypeMirror smart : smarts)
                                    if (smart.getKind() == TypeKind.DECLARED)
                                        addSubtypesOf(env, (DeclaredType)smart);
                        } else {
                            String prefix = env.getPrefix();
                            CompilationController controller = env.getController();
                            DeclaredType base = path.getParentPath().getLeaf().getKind() == Tree.Kind.THROW ?
                                controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")) : null; //NOI18N
                            if (queryType == COMPLETION_QUERY_TYPE && nc.getIdentifier().getKind() == Tree.Kind.IDENTIFIER && prefix != null) {
                                TypeMirror tm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                                if (tm != null && tm.getKind() == TypeKind.DECLARED)
                                    addMembers(env, tm, ((DeclaredType)tm).asElement(), EnumSet.of(CONSTRUCTOR), base, false);
                            }
                            addTypes(env, EnumSet.of(CLASS, INTERFACE), base, null);
                        }
                        break;
                    case LPAREN:
                    case COMMA:
                        addLocalMembersAndVars(env);
                        addValueKeywords(env);
                        if (queryType != COMPLETION_SMART_QUERY_TYPE) {
                            addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                            addPrimitiveTypeKeywords(env);
                        }
                        break;
                    case GT:
                        CompilationController controller = env.getController();
                        TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(path, nc.getIdentifier()));
                        addMembers(env, tm, ((DeclaredType)tm).asElement(), EnumSet.of(CONSTRUCTOR), null, false);
                        break;
                }
            }
        }
        
        private void insideCatch(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            CatchTree ct = (CatchTree)path.getLeaf();
            CompilationController controller = env.getController();
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, ct, offset);
            if (last != null && last.token().id() == JavaTokenId.LPAREN) {
                if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                    Set<TypeMirror> exs = controller.getTreeUtilities().getUncaughtExceptions(path.getParentPath());
                    Elements elements = controller.getElements();
                    for (TypeMirror ex : exs)
                        if (ex.getKind() == TypeKind.DECLARED)
                            results.add(JavaCompletionItem.createTypeItem((TypeElement)((DeclaredType)ex).asElement(), (DeclaredType)ex, offset, true, elements.isDeprecated(((DeclaredType)ex).asElement())));
                } else {
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(controller.getElements().getTypeElement("java.lang.Throwable")), null); //NOI18N
                }
            }
        }        
        
        private void insideIf(Env env) throws IOException {
            IfTree iff = (IfTree)env.getPath().getLeaf();
            if (env.getSourcePositions().getEndPosition(env.getRoot(), iff.getCondition()) <= env.getOffset()) {
                localResult(env);
                addKeywordsForStatement(env);
            }
        }
        
        private void insideWhile(Env env) throws IOException {
            WhileLoopTree wlt = (WhileLoopTree)env.getPath().getLeaf();
            if (env.getSourcePositions().getEndPosition(env.getRoot(), wlt.getCondition()) <= env.getOffset()) {
                localResult(env);
                addKeywordsForStatement(env);
            }
        }
        
        private void insideFor(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            ForLoopTree fl = (ForLoopTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            Tree lastTree = null;
            int lastTreePos = offset;
            for (Tree update : fl.getUpdate()) {
                int pos = (int)sourcePositions.getEndPosition(root, update);
                if (pos == Diagnostic.NOPOS || offset <= pos)
                    break;
                lastTree = update;
                lastTreePos = pos;
            }
            if (lastTree == null) {
                int pos = (int)sourcePositions.getEndPosition(root, fl.getCondition());
                if (pos != Diagnostic.NOPOS && pos < offset) {
                    lastTree = fl.getCondition();
                    lastTreePos = pos;
                }
            }
            if (lastTree == null) {
                for (Tree init : fl.getInitializer()) {
                    int pos = (int)sourcePositions.getEndPosition(root, init);
                    if (pos == Diagnostic.NOPOS || offset <= pos)
                        break;
                    lastTree = init;
                    lastTreePos = pos;
                }
            }
            if (lastTree == null) {
                addLocalFieldsAndVars(env);
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
                addPrimitiveTypeKeywords(env);
            } else {
                String text = env.getController().getText().substring(lastTreePos, offset).trim();
                if (";".equals(text)) { //NOI18N
                    localResult(env);
                    addValueKeywords(env);
                } else if (text.endsWith(")")) { //NOI18N
                    localResult(env);
                    addKeywordsForStatement(env);
                } else {
                    switch (lastTree.getKind()) {
                        case VARIABLE:
                            Tree var = ((VariableTree)lastTree).getInitializer();
                            if (var != null)
                                insideExpression(env, new TreePath(new TreePath(path, lastTree), var));
                            break;
                        case EXPRESSION_STATEMENT:
                            Tree exp = unwrapErrTree(((ExpressionStatementTree)lastTree).getExpression());
                            if (exp != null)
                                insideExpression(env, new TreePath(new TreePath(path, lastTree), exp));
                            break;
                        default:
                            insideExpression(env, new TreePath(path, lastTree));
                    }
                }
            }
        }

        private void insideForEach(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            EnhancedForLoopTree efl = (EnhancedForLoopTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            CompilationController controller = env.getController();
            if (sourcePositions.getStartPosition(root, efl.getExpression()) >= offset) {
                if (":".equals(controller.getText().substring((int)sourcePositions.getEndPosition(root, efl.getVariable()), offset).trim())) //NOI18N
                    localResult(env);
                return;
            }
            localResult(env);
            if (controller.getText().substring((int)sourcePositions.getEndPosition(root, efl.getExpression()), offset).trim().endsWith(")")) //NOI18N
                addKeywordsForStatement(env);            
        }
        
        private void insideSwitch(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            TreePath path = env.getPath();
            SwitchTree st = (SwitchTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            if (sourcePositions.getStartPosition(root, st.getExpression()) < offset) {
                CaseTree lastCase = null;
                for (CaseTree t : st.getCases()) {
                    int pos = (int)sourcePositions.getStartPosition(root, t);
                    if (pos == Diagnostic.NOPOS || offset <= pos)
                        break;
                    lastCase = t;
                }
                if (lastCase != null) {
                    localResult(env);
                    addKeywordsForBlock(env);
                } else {
                    TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, st, offset);
                    if (ts != null && ts.token().id() == JavaTokenId.LBRACE) {
                        if (Utilities.startsWith(CASE_KEYWORD, prefix))
                            addKeyword(env, CASE_KEYWORD, SPACE);
                        if (Utilities.startsWith(DEFAULT_KEYWORD, prefix))
                            addKeyword(env, DEFAULT_KEYWORD, COLON);
                    }
                }
            }
        }
        
        private void insideCase(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            CaseTree cst = (CaseTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            CompilationController controller = env.getController();
            if (cst.getExpression() != null && ((sourcePositions.getStartPosition(root, cst.getExpression()) >= offset) ||
                    (cst.getExpression().getKind() == Tree.Kind.ERRONEOUS && ((ErroneousTree)cst.getExpression()).getErrorTrees().isEmpty() && sourcePositions.getEndPosition(root, cst.getExpression()) >= offset))) {
                TreePath path1 = path.getParentPath();
                if (path1.getLeaf().getKind() == Tree.Kind.SWITCH) {
                    TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(path1, ((SwitchTree)path1.getLeaf()).getExpression()));
                    if (tm.getKind() == TypeKind.DECLARED && ((DeclaredType)tm).asElement().getKind() == ENUM) {
                        addEnumConstants(env, (TypeElement)((DeclaredType)tm).asElement());
                    }
                }
            } else {
                TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, cst, offset);
                if (ts != null && ts.token().id() == JavaTokenId.COLON) {
                    localResult(env);
                    addKeywordsForBlock(env);
                }
            }
        }
        
        private void insideParens(Env env) throws IOException {
            TreePath path = env.getPath();
            ParenthesizedTree pa = (ParenthesizedTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            Tree exp = unwrapErrTree(pa.getExpression());
            if (exp == null || env.getOffset() <= sourcePositions.getStartPosition(root, exp)) {
                localResult(env);
                addValueKeywords(env);
            } else {
                insideExpression(env, new TreePath(path, exp));
            }
        }
        
        private void insideTypeCheck(Env env) throws IOException {
            InstanceOfTree iot = (InstanceOfTree)env.getPath().getLeaf();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, iot, env.getOffset());
            if (ts != null && ts.token().id() == JavaTokenId.INSTANCEOF)
                addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
        }
        
        private void insideArrayAccess(Env env) throws IOException {
            int offset = env.getOffset();
            ArrayAccessTree aat = (ArrayAccessTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int aaTextStart = (int)sourcePositions.getEndPosition(root, aat.getExpression());
            if (aaTextStart != Diagnostic.NOPOS) {
                Tree expr = unwrapErrTree(aat.getIndex());
                if (expr == null || offset <= (int)sourcePositions.getStartPosition(root, expr)) {
                    String aatText = env.getController().getText().substring(aaTextStart, offset);
                    int bPos = aatText.indexOf('['); //NOI18N
                    if (bPos > -1) {
                        localResult(env);
                        addValueKeywords(env);
                    }
                }
            }
        }
        
        private void insideNewArray(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            NewArrayTree nat = (NewArrayTree)path.getLeaf();
            if (nat.getInitializers() != null) { // UFFF!!!!
                SourcePositions sourcePositions = env.getSourcePositions();
                CompilationUnitTree root = env.getRoot();
                Tree last = null;
                int lastPos = offset;
                for (Tree init : nat.getInitializers()) {
                    int pos = (int)sourcePositions.getEndPosition(root, init);
                    if (pos == Diagnostic.NOPOS || offset <= pos)
                        break;
                    last = init;
                    lastPos = pos;
                }
                if (last != null) {
                    String text = env.getController().getText().substring(lastPos, offset).trim();
                    if (",".equals(text)) { //NOI18N
                        localResult(env);
                        addValueKeywords(env);
                    }
                    return;
                }
            }
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, nat, offset);
            switch (ts.token().id()) {
                case LBRACKET:
                case LBRACE:
                    localResult(env);
                    addValueKeywords(env);
                    break;
                case RBRACKET:
                    if (nat.getDimensions().size() > 0)
                        insideExpression(env, path);
                    break;
            }
        }
        
        private void insideAssignment(Env env) throws IOException {
            int offset = env.getOffset();
            TreePath path = env.getPath();
            AssignmentTree as = (AssignmentTree)path.getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int asTextStart = (int)sourcePositions.getEndPosition(root, as.getVariable());
            if (asTextStart != Diagnostic.NOPOS) {
                Tree expr = unwrapErrTree(as.getExpression());
                if (expr == null || offset <= (int)sourcePositions.getStartPosition(root, expr)) {
                    String asText = env.getController().getText().substring(asTextStart, offset);
                    int eqPos = asText.indexOf('='); //NOI18N
                    if (eqPos > -1) {
                        if (path.getParentPath().getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                            addLocalConstantsAndTypes(env);
                        } else {
                            localResult(env);
                            addValueKeywords(env);
                        }
                    }
                }
            }
        }
        
        private void insideCompoundAssignment(Env env) throws IOException {
            int offset = env.getOffset();
            CompoundAssignmentTree cat = (CompoundAssignmentTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int catTextStart = (int)sourcePositions.getEndPosition(root, cat.getVariable());
            if (catTextStart != Diagnostic.NOPOS) {
                Tree expr = unwrapErrTree(cat.getExpression());
                if (expr == null || offset <= (int)sourcePositions.getStartPosition(root, expr)) {
                    String catText = env.getController().getText().substring(catTextStart, offset);
                    int eqPos = catText.indexOf('='); //NOI18N
                    if (eqPos > -1) {
                        localResult(env);
                        addValueKeywords(env);
                    }
                }
            }
        }
        
        private void insideBinaryTree(Env env) throws IOException {
            int offset = env.getOffset();
            BinaryTree bi = (BinaryTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int pos = (int)sourcePositions.getEndPosition(root, bi.getRightOperand());
            if (pos != Diagnostic.NOPOS && pos < offset)
                return;
            pos = (int)sourcePositions.getEndPosition(root, bi.getLeftOperand());
            if (pos != Diagnostic.NOPOS) {
                String biText = env.getController().getText().substring(pos, offset).trim();
                if (biText.length() > 0) {
                    localResult(env);
                    addValueKeywords(env);
                }
            }
        }

        private void insideConditionalExpression(Env env) throws IOException {
            ConditionalExpressionTree co = (ConditionalExpressionTree)env.getPath().getLeaf();
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int coTextStart = (int)sourcePositions.getStartPosition(root, co);
            if (coTextStart != Diagnostic.NOPOS) {
                String coText = env.getController().getText().substring(coTextStart, env.getOffset()).trim();
                if (coText.endsWith("?") || coText.endsWith(":")) { //NOI18N
                    localResult(env);
                    addValueKeywords(env);
                }
            }
        }
        
        private void insideExpressionStatement(Env env) throws IOException {
            TreePath path = env.getPath();
            ExpressionStatementTree est = (ExpressionStatementTree)path.getLeaf();
            CompilationController controller = env.getController();
            Tree t = est.getExpression();
            if (t.getKind() == Tree.Kind.ERRONEOUS) {
                Iterator<? extends Tree> it = ((ErroneousTree)t).getErrorTrees().iterator();
                if (it.hasNext()) {
                    t = it.next();
                } else {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHiearchy().tokenSequence(JavaTokenId.language());
                    ts.move((int)env.getSourcePositions().getStartPosition(env.getRoot(), est));
                    ts.movePrevious();
                    switch (ts.token().id()) {
                        case FOR:
                        case IF:
                        case SWITCH:
                        case WHILE:
                            return;
                    }
                    localResult(env);
                    Tree parentTree = path.getParentPath().getLeaf();
                    switch (parentTree.getKind()) {
                        case FOR_LOOP:
                            if (((ForLoopTree)parentTree).getStatement() == est)
                                addKeywordsForStatement(env);
                            else
                                addValueKeywords(env);
                            break;
                        case ENHANCED_FOR_LOOP:
                            if (((EnhancedForLoopTree)parentTree).getStatement() == est)
                                addKeywordsForStatement(env);
                            else
                                addValueKeywords(env);
                            break;
                        case VARIABLE:
                            addValueKeywords(env);
                            break;
                        default:
                            addKeywordsForStatement(env);
                            break;
                    }
                    return;
                }
            }
            TreePath tPath = new TreePath(path, t);
            if (t.getKind() == Tree.Kind.MODIFIERS) {
                insideModifiers(env, tPath);
            } else if (t.getKind() == Tree.Kind.MEMBER_SELECT && ERROR.contentEquals(((MemberSelectTree)t).getIdentifier())) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TreePath expPath = new TreePath(tPath, ((MemberSelectTree)t).getExpression());
                TypeMirror type = controller.getTrees().getTypeMirror(expPath);
                switch (type.getKind()) {
                    case TYPEVAR:
                        type = ((TypeVariable)type).getUpperBound();
                        if (type == null)
                            return;
                        type = controller.getTypes().capture(type);
                    case ARRAY:
                    case DECLARED:
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case DOUBLE:
                    case FLOAT:
                    case INT:
                    case LONG:
                    case SHORT:
                    case VOID:
                        addMembers(env, type, controller.getTrees().getElement(expPath), EnumSet.of(CLASS, ENUM, INTERFACE, FIELD, METHOD, ENUM_CONSTANT), null, false);
                        break;
                    default:
                        Element el = controller.getTrees().getElement(expPath);
                        if (el instanceof PackageElement) {
                            addPackageContent(env, (PackageElement)el, EnumSet.of(CLASS, ENUM, INTERFACE, FIELD, METHOD, ENUM_CONSTANT), null);
                        }
                }
            } else {
                insideExpression(env, tPath);
            }
            
        }
        
        private void insideExpression(Env env, TreePath exPath) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            Tree et = exPath.getLeaf();
            Tree parent = exPath.getParentPath().getLeaf();
            CompilationController controller = env.getController();
            int endPos = (int)env.getSourcePositions().getEndPosition(env.getRoot(), et);
            if (endPos != Diagnostic.NOPOS && endPos < offset) {
                if (controller.getText().substring(endPos, offset).trim().length() > 0)
                    return;
            }
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            boolean isConst = parent.getKind() == Tree.Kind.VARIABLE && ((VariableTree)parent).getModifiers().getFlags().containsAll(EnumSet.of(FINAL, STATIC));
            if ((parent == null || parent.getKind() != Tree.Kind.PARENTHESIZED) &&
                    (et.getKind() == Tree.Kind.PRIMITIVE_TYPE || et.getKind() == Tree.Kind.ARRAY_TYPE || et.getKind() == Tree.Kind.PARAMETERIZED_TYPE)) {
                TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
                final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
                Scope scope = env.getScope();
                final ExecutableElement method = scope.getEnclosingMethod();
                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror t) {
                        return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                !illegalForwardRefs.contains(e);
                    }
                };
                for (String name : Utilities.varNamesSuggestions(tm, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalVars(scope, acceptor), isConst))
                    results.add(JavaCompletionItem.createVariableItem(name, offset));
                return;
            }
            if (et.getKind() == Tree.Kind.IDENTIFIER) {
                Element e = controller.getTrees().getElement(exPath);
                if (e == null)
                    return;
                TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
                switch (e.getKind()) {
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case PACKAGE:
                        if (parent == null || parent.getKind() != Tree.Kind.PARENTHESIZED) {
                            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
                            Scope scope = env.getScope();
                            final ExecutableElement method = scope.getEnclosingMethod();
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                public boolean accept(Element e, TypeMirror t) {
                                    return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                            !illegalForwardRefs.contains(e);
                                }
                            };
                            for (String name : Utilities.varNamesSuggestions(tm, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalVars(scope, acceptor), isConst))
                                results.add(JavaCompletionItem.createVariableItem(name, offset));
                        }
                        VariableElement ve = getFieldOrVar(env, e.getSimpleName().toString());
                        if (ve != null) {
                            if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                        }
                        break;
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case FIELD:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR) {
                            if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                        }
                        TypeElement te = getTypeElement(env, e.getSimpleName().toString());
                        if (te != null) {
                            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
                            Scope scope = env.getScope();
                            final ExecutableElement method = scope.getEnclosingMethod();
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                public boolean accept(Element e, TypeMirror t) {
                                    return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                            !illegalForwardRefs.contains(e);
                                }
                            };
                            for (String name : Utilities.varNamesSuggestions(controller.getTypes().getDeclaredType(te), prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalVars(scope, acceptor), isConst))
                                results.add(JavaCompletionItem.createVariableItem(name, offset));
                        }
                        break;
                }
                return;
            }
            Tree exp = null;
            if (et.getKind() == Tree.Kind.PARENTHESIZED) {
                exp = ((ParenthesizedTree)et).getExpression();
            } else if (et.getKind() == Tree.Kind.TYPE_CAST) {
                if(env.getSourcePositions().getEndPosition(env.getRoot(), et) > offset ||
                    ((TypeCastTree)et).getExpression().getKind() == Tree.Kind.IDENTIFIER)
                    exp = ((TypeCastTree)et).getType();
            }
            if (exp != null) {
                exPath = new TreePath(exPath, exp);
                if (exp.getKind() == Tree.Kind.PRIMITIVE_TYPE || et.getKind() == Tree.Kind.ARRAY_TYPE || et.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                    localResult(env);
                    addValueKeywords(env);
                    return;
                }
                Element e = controller.getTrees().getElement(exPath);
                if (e == null) {
                    if (exp.getKind() == Tree.Kind.TYPE_CAST && Utilities.startsWith(INSTANCEOF_KEYWORD, prefix)) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                    }
                    return;
                }
                TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
                switch (e.getKind()) {
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case PACKAGE:
                        if (exp.getKind() == Tree.Kind.IDENTIFIER) {
                            VariableElement ve = getFieldOrVar(env, e.getSimpleName().toString());
                            if (ve != null) {
                                if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                    addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                            }
                            if (ve == null || tm.getKind() != TypeKind.ERROR) {
                                localResult(env);
                                addValueKeywords(env);
                            }
                        } else if (exp.getKind() == Tree.Kind.MEMBER_SELECT) {
                            if (tm.getKind() == TypeKind.ERROR || tm.getKind() == TypeKind.PACKAGE) {
                                if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                    addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                            }
                            localResult(env);
                            addValueKeywords(env);
                        } else if (exp.getKind() == Tree.Kind.PARENTHESIZED && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY)) {
                            if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                        }
                        break;
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case FIELD:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR) {
                            if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                        }
                        TypeElement te = getTypeElement(env, e.getSimpleName().toString());
                        if (te != null || exp.getKind() == Tree.Kind.MEMBER_SELECT) {
                            localResult(env);
                            addValueKeywords(env);
                        }
                        break;
                    case CONSTRUCTOR:
                    case METHOD:
                        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR) {
                            if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                                addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                        }
                }
                return;
            }
            Element e = controller.getTrees().getElement(exPath);
            TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
            if (e == null) {
                if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR) {
                    if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                }
                return;
            }
            switch (e.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case PACKAGE:
                    final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
                    Scope scope = env.getScope();
                    final ExecutableElement method = scope.getEnclosingMethod();
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                       public boolean accept(Element e, TypeMirror t) {
                            return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                    !illegalForwardRefs.contains(e);
                        }
                    };
                    for (String name : Utilities.varNamesSuggestions(tm, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalVars(scope, acceptor), isConst))
                        results.add(JavaCompletionItem.createVariableItem(name, offset));
                    if (et.getKind() == Tree.Kind.MEMBER_SELECT && tm.getKind() == TypeKind.ERROR) {
                        if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                            addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                    }
                    break;
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case FIELD:
                case LOCAL_VARIABLE:
                case PARAMETER:
                case CONSTRUCTOR:
                case METHOD:
                    if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR) {
                        if (Utilities.startsWith(INSTANCEOF_KEYWORD, prefix))
                            addKeyword(env, INSTANCEOF_KEYWORD, SPACE);
                    }
            }
        }
        
        private void localResult(Env env) throws IOException {
            addLocalMembersAndVars(env);
            addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
            addPrimitiveTypeKeywords(env);
        }
        
        private void addLocalConstantsAndTypes(Env env) throws IOException {
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            final Scope scope = env.getScope();
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
                for (TypeMirror st : smartTypes) {
                    if (st.getKind().isPrimitive())
                        st = types.boxedClass((PrimitiveType)st).asType();
                    if (st.getKind() == TypeKind.DECLARED) {
                        final DeclaredType type = (DeclaredType)st;
                        final TypeElement element = (TypeElement)type.asElement();
                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            public boolean accept(Element e, TypeMirror t) {
                                return (e.getKind() == ENUM_CONSTANT || e.getKind() == FIELD && ((VariableElement)e).getConstantValue() != null) &&
                                        (!isStatic || e.getModifiers().contains(STATIC)) &&
                                        Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                        tu.isAccessible(scope, e, t) &&
                                        types.isAssignable(((VariableElement)e).asType(), type);
                            }
                        };
                        boolean csm = false;
                        for (Element ee : controller.getElementUtilities().getMembers(type, acceptor)) {
                            results.add(JavaCompletionItem.createStaticMemberItem(type, ee, types.asMemberOf(type, ee), offset, elements.isDeprecated(ee)));
                            csm = true;
                        }
                        if (csm) {
                            String name = element.getSimpleName().toString();
                            if (Utilities.startsWith(name, prefix))
                                results.add(JavaCompletionItem.createTypeItem(element, type, offset, false, elements.isDeprecated(element)));
                        }
                    }
                }
            }
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
            final ExecutableElement method = scope.getEnclosingMethod();
            final Set<TypeMirror> finalSmartTypes = smartTypes;
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    switch (e.getKind()) {
                        case LOCAL_VARIABLE:
                        case EXCEPTION_PARAMETER:
                        case PARAMETER:
                            return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL) ||
                                    (method == null && (e.getEnclosingElement().getKind() == INSTANCE_INIT ||
                                    e.getEnclosingElement().getKind() == STATIC_INIT))) &&
                                    !illegalForwardRefs.contains(e) &&
                                    ((VariableElement)e).getConstantValue() != null;
                        case FIELD:
                            if (illegalForwardRefs.contains(e) || ((VariableElement)e).getConstantValue() == null)
                                return false;
                        case ENUM_CONSTANT:
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                    (!isStatic || e.getModifiers().contains(STATIC)) &&
                                    tu.isAccessible(scope, e, t) &&
                                    isOfSmartType(e.asType(), finalSmartTypes, types);
                    }
                    return false;
                }
            };
            for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor))
                if (e.getKind() == FIELD)
                    results.add(JavaCompletionItem.createVariableItem((VariableElement)e, asMemberOf(e, enclClass != null ? enclClass.asType() : null, types), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                else
                    results.add(JavaCompletionItem.createVariableItem((VariableElement)e, e.asType(), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
            addTypes(env, EnumSet.of(CLASS, INTERFACE, ENUM, TYPE_PARAMETER), null, null);
        }
        
        private void addLocalMembersAndVars(Env env) throws IOException {
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            final Scope scope = env.getScope();
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
                for (TypeMirror st : smartTypes) {
                    if (st.getKind().isPrimitive())
                        st = types.boxedClass((PrimitiveType)st).asType();
                    if (st.getKind() == TypeKind.DECLARED) {
                        final DeclaredType type = (DeclaredType)st;
                        final TypeElement element = (TypeElement)type.asElement();
                        if (withinScope(env, element))
                            continue;
                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            public boolean accept(Element e, TypeMirror t) {
                                return (!isStatic || e.getModifiers().contains(STATIC)) &&
                                        Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                        tu.isAccessible(scope, e, t) &&
                                        (e.getKind().isField() && types.isAssignable(((VariableElement)e).asType(), type) || e.getKind() == METHOD && types.isAssignable(((ExecutableElement)e).getReturnType(), type));
                            }
                        };
                        boolean csm = false;
                        for (Element ee : controller.getElementUtilities().getMembers(type, acceptor)) {
                            results.add(JavaCompletionItem.createStaticMemberItem(type, ee, types.asMemberOf(type, ee), offset, elements.isDeprecated(ee)));
                            csm = true;
                        }
                        if (csm) {
                            String name = element.getSimpleName().toString();
                            if (Utilities.startsWith(name, prefix))
                                results.add(JavaCompletionItem.createTypeItem(element, type, offset, false, elements.isDeprecated(element)));
                        }
                    }
                }
            }
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
            final ExecutableElement method = scope.getEnclosingMethod();
            final Set<TypeMirror> finalSmartTypes = smartTypes;
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    switch (e.getKind()) {
                        case CONSTRUCTOR:
                            return false;
                        case LOCAL_VARIABLE:
                        case EXCEPTION_PARAMETER:
                        case PARAMETER:
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                    (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL) ||
                                    (method == null && (e.getEnclosingElement().getKind() == INSTANCE_INIT ||
                                    e.getEnclosingElement().getKind() == STATIC_INIT))) &&
                                    !illegalForwardRefs.contains(e) &&
                                    isOfSmartType(e.asType(), finalSmartTypes, types);
                        case FIELD:
                            if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                                return !isStatic && isOfSmartType(e.asType(), finalSmartTypes, types) && 
                                        Utilities.startsWith(e.getSimpleName().toString(), prefix);
                        case ENUM_CONSTANT:
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                    !illegalForwardRefs.contains(e) &&
                                    (!isStatic || e.getModifiers().contains(STATIC)) &&
                                    tu.isAccessible(scope, e, t) &&
                                    isOfSmartType(e.asType(), finalSmartTypes, types);
                        case METHOD:
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                    (!isStatic || e.getModifiers().contains(STATIC)) &&
                                    tu.isAccessible(scope, e, t) &&
                                    isOfSmartType(((ExecutableElement)e).getReturnType(), finalSmartTypes, types);
                    }
                    return false;
                }
            };
            for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
                switch (e.getKind()) {
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                        results.add(JavaCompletionItem.createVariableItem((VariableElement)e, e.asType(), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                    case FIELD:
                        String name = e.getSimpleName().toString();
                        if (THIS_KEYWORD.equals(name) || SUPER_KEYWORD.equals(name))
                            addKeyword(env, name, null);
                        else
                            results.add(JavaCompletionItem.createVariableItem((VariableElement)e, asMemberOf(e, enclClass != null ? enclClass.asType() : null, types), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                    case METHOD:
                        results.add(JavaCompletionItem.createExecutableItem((ExecutableElement)e, (ExecutableType)asMemberOf(e, enclClass != null ? enclClass.asType() : null, types), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), false));
                        break;
                }
            }
        }

        private void addLocalFieldsAndVars(Env env) throws IOException {
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            final Scope scope = env.getScope();
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
            }
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
            final ExecutableElement method = scope.getEnclosingMethod();
            final Set<TypeMirror> finalSmartTypes = smartTypes;
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    switch (e.getKind()) {
                        case LOCAL_VARIABLE:
                        case EXCEPTION_PARAMETER:
                        case PARAMETER:
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                                    (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL) ||
                                    (method == null && (e.getEnclosingElement().getKind() == INSTANCE_INIT ||
                                    e.getEnclosingElement().getKind() == STATIC_INIT))) &&
                                    !illegalForwardRefs.contains(e) &&
                                    isOfSmartType(e.asType(), finalSmartTypes, types);
                        case FIELD:
                            return !e.getSimpleName().contentEquals(THIS_KEYWORD) && !e.getSimpleName().contentEquals(SUPER_KEYWORD) &&
                                    !isStatic && isOfSmartType(e.asType(), finalSmartTypes, types) && Utilities.startsWith(e.getSimpleName().toString(), prefix);
                    }
                    return false;
                }
            };            
            for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
                switch (e.getKind()) {
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                        results.add(JavaCompletionItem.createVariableItem((VariableElement)e, e.asType(), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                    case FIELD:
                        results.add(JavaCompletionItem.createVariableItem((VariableElement)e, asMemberOf(e, enclClass != null ? enclClass.asType() : null, types), offset, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                }
            }
        }
        
        private void addMemberConstantsAndTypes(Env env, final TypeMirror type, final Element elem) throws IOException {
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
            }
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement)((DeclaredType)type).asElement() : null;
            final boolean isStatic = elem != null && (elem.getKind().isClass() || elem.getKind().isInterface());
            final Scope scope = env.getScope();
            final Set<TypeMirror> finalSmartTypes = smartTypes;
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    if (!Utilities.startsWith(e.getSimpleName().toString(), prefix) ||
                            (isStatic && !e.getModifiers().contains(STATIC)))
                        return false;
                    switch (e.getKind()) {
                        case FIELD:
                            if (((VariableElement)e).getConstantValue() == null)
                                return false;
                        case ENUM_CONSTANT:
                            return tu.isAccessible(scope, e, t) &&
                                    isOfSmartType(e.asType(), finalSmartTypes, types);
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                            return (finalSmartTypes == null || finalSmartTypes.isEmpty()) &&
                                    tu.isAccessible(scope, e, t);
                    }
                    return false;
                }
            };
            for(Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
                switch (e.getKind()) {
                    case FIELD:
                    case ENUM_CONSTANT:
                        results.add(JavaCompletionItem.createVariableItem((VariableElement)e, type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType)type, e) : e.asType(), offset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), offset, false, elements.isDeprecated(e)));
                        break;
                }
            }
        }
        
        private void addMembers(Env env, final TypeMirror type, final Element elem, final EnumSet<ElementKind> kinds, final DeclaredType baseType, final boolean inImport) throws IOException {
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
            }
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Trees trees = controller.getTrees();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement)((DeclaredType)type).asElement() : null;
            final boolean isStatic = elem != null && (elem.getKind().isClass() || elem.getKind().isInterface() || elem.getKind() == TYPE_PARAMETER);
            final Scope scope = env.getScope();
            final Set<TypeMirror> finalSmartTypes = smartTypes;
            final boolean[] ctorSeen = {false};
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    switch (e.getKind()) {
                        case FIELD:
                            if (!Utilities.startsWith(e.getSimpleName().toString(), prefix) ||
                                    !isOfKindAndType(e, kinds, baseType, scope, trees, types) ||
                                    !tu.isAccessible(scope, e, t) || 
                                    (isStatic && !e.getModifiers().contains(STATIC)) ||
                                    (!isStatic && e.getSimpleName().contentEquals(THIS_KEYWORD)) ||
                                    ((!isStatic || inImport) && e.getSimpleName().contentEquals(CLASS_KEYWORD)))
                                return false;
                        case ENUM_CONSTANT:
                        case EXCEPTION_PARAMETER:
                        case LOCAL_VARIABLE:
                        case PARAMETER:
                            if (!Utilities.startsWith(e.getSimpleName().toString(), prefix) ||
                                    !isOfKindAndType(e, kinds, baseType, scope, trees, types) ||
                                    !tu.isAccessible(scope, e, t))
                                return false;
                            return isOfSmartType(e.asType(), finalSmartTypes, types);
                        case METHOD:
                            if (!Utilities.startsWith(e.getSimpleName().toString(), prefix) ||
                                    !isOfKindAndType(e, kinds, baseType, scope, trees, types) ||
                                    !tu.isAccessible(scope, e, t))
                                return false;
                            return (!isStatic || e.getModifiers().contains(STATIC)) && isOfSmartType(((ExecutableElement)e).getReturnType(), finalSmartTypes, types);
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                            if (!Utilities.startsWith(e.getSimpleName().toString(), prefix) ||
                                    !isOfKindAndType(e, kinds, baseType, scope, trees, types) ||
                                    !tu.isAccessible(scope, e, t))
                                return false;
                            return isStatic && (finalSmartTypes == null || finalSmartTypes.isEmpty());
                        case CONSTRUCTOR:
                            ctorSeen[0] = true;
                            if (!Utilities.startsWith(e.getEnclosingElement().getSimpleName().toString(), prefix) ||
                                    !isOfKindAndType(e, kinds, baseType, scope, trees, types) ||
                                    (!tu.isAccessible(scope, e, t) && (!elem.getModifiers().contains(ABSTRACT) || e.getModifiers().contains(PRIVATE))))
                                return false;
                            return isStatic && (finalSmartTypes == null || finalSmartTypes.isEmpty());
                    }
                    return false;
                }
            };
            for(Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
                switch (e.getKind()) {
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case FIELD:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                        String name = e.getSimpleName().toString();
                        if (THIS_KEYWORD.equals(name) || CLASS_KEYWORD.equals(name))
                            addKeyword(env, name, null);
                        else
                            results.add(JavaCompletionItem.createVariableItem((VariableElement)e, type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType)type, e) : e.asType(), offset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e)));
                        break;
                    case CONSTRUCTOR:
                    case METHOD:
                        results.add(JavaCompletionItem.createExecutableItem((ExecutableElement)e, (ExecutableType)(type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType)type, e) : e.asType()), offset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), inImport));
                        break;
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                        results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), offset, false, elements.isDeprecated(e)));
                        break;
                }
            }
            if (!ctorSeen[0] && kinds.contains(CONSTRUCTOR) && elem.getKind().isInterface()) {
                results.add(JavaCompletionItem.createDefaultConstructorItem((TypeElement)elem, offset));
            }
        }
        
        private void addEnumConstants(Env env, TypeElement elem) {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            Elements elements = env.getController().getElements();
            for(Element e : elem.getEnclosedElements()) {
                if (e.getKind() == ENUM_CONSTANT) {
                    String name = e.getSimpleName().toString();
                    if (Utilities.startsWith(name, prefix))
                        results.add(JavaCompletionItem.createVariableItem((VariableElement)e, e.asType(), offset, false, elements.isDeprecated(e)));
                }
            }
        }
        
        private void addPackageContent(Env env, PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType) throws IOException {
            Set<TypeMirror> smartTypes = null;
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                smartTypes = getSmartTypes(env);
                if (smartTypes == null || smartTypes.isEmpty())
                    return;
            }
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            CompilationController controller = env.getController();
            Elements elements = controller.getElements();
            Types types = controller.getTypes();
            Trees trees = controller.getTrees();
            Scope scope = env.getScope();
            for(Element e : pe.getEnclosedElements()) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    String name = e.getSimpleName().toString();
                        if (Utilities.startsWith(name, prefix) && trees.isAccessible(scope, (TypeElement)e) && isOfKindAndType(e, kinds, baseType, scope, trees, types) && isOfSmartType(e.asType(), smartTypes, types)) {
                            results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), offset, false, elements.isDeprecated(e)));
                    }
                }
            }
            String pkgName = pe.getQualifiedName() + "."; //NOI18N
            if (prefix != null && prefix.length() > 0)
                pkgName += prefix;
            addPackages(env, pkgName);
        }
        
        private void addPackages(Env env, String fqnPrefix) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            if (fqnPrefix == null)
                fqnPrefix = ""; //NOI18N
            for (String pkgName : env.getController().getClasspathInfo().getClassIndex().getPackageNames(fqnPrefix, true,EnumSet.allOf(ClassIndex.SearchScope.class)))
                if (pkgName.length() > 0)
                    results.add(JavaCompletionItem.createPackageItem(pkgName, offset, false));
        }
        
        private void addTypes(Env env, EnumSet<ElementKind> kinds, DeclaredType baseType, Element toExclude) throws IOException {
            if (queryType == COMPLETION_QUERY_TYPE) {
                addLocalAndImportedTypes(env, kinds, baseType, toExclude);
            } else if (queryType == COMPLETION_ALL_QUERY_TYPE) {
                if (baseType == null)
                    addAllTypes(env, kinds);
                else
                    addSubtypesOf(env, baseType);
            }
            addPackages(env, env.getPrefix());
        }
        
        private void addLocalAndImportedTypes(Env env, final EnumSet<ElementKind> kinds, final DeclaredType baseType, final Element toExclude) throws IOException {
            int offset = env.getOffset();
            final String prefix = env.getPrefix();
            final CompilationController controller = env.getController();
            final Trees trees = controller.getTrees();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final TreeUtilities tu = controller.getTreeUtilities();
            final Scope scope = env.getScope();
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    if (e.getKind().isClass() || e.getKind().isInterface() || e.getKind() == TYPE_PARAMETER) {
                        String name = e.getSimpleName().toString();
                        return name.length() > 0 && !Character.isDigit(name.charAt(0)) && Utilities.startsWith(name, prefix) &&
                                (!isStatic || e.getModifiers().contains(STATIC)) && isOfKindAndType(e, kinds, baseType, scope, trees, types);
                    }
                    return false;
                }
            };
            for(Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
                switch (e.getKind()) {
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                        results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), offset, false, elements.isDeprecated(e)));
                        break;
                    case TYPE_PARAMETER:
                        results.add(JavaCompletionItem.createTypeParameterItem((TypeParameterElement)e, offset));
                        break;                        
                }                
            }
            acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    return toExclude != e && Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                            e.getEnclosingElement().getKind() == PACKAGE &&
                            trees.isAccessible(scope, (TypeElement)e) &&
                            isOfKindAndType(e, kinds, baseType, scope, trees, types);
                }
            };
            for (TypeElement e : controller.getElementUtilities().getGlobalTypes(acceptor)) {
                results.add(JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), offset, false, elements.isDeprecated(e)));
            }
        }
        
        private void addAllTypes(Env env, EnumSet<ElementKind> kinds) {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            CompilationController controller = env.getController();
            for(ElementHandle<TypeElement> name : controller.getJavaSource().getClasspathInfo().getClassIndex().getDeclaredTypes(prefix != null ? prefix : "", Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, EnumSet.allOf(ClassIndex.SearchScope.class))) { //NOI18N
                results.add(LazyTypeCompletionItem.create(name.getQualifiedName(), kinds, offset, controller));
            }
        }
        
        private void addSubtypesOf(Env env, DeclaredType baseType) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            CompilationController controller = env.getController();
            Elements elements = controller.getElements();
            Types types = controller.getTypes();
            Trees trees = controller.getTrees();
            Scope scope = env.getScope();
            HashSet<TypeElement> elems = new HashSet<TypeElement>();
            LinkedList<DeclaredType> bases = new LinkedList<DeclaredType>();
            bases.add(baseType);
            ClassIndex index = controller.getJavaSource().getClasspathInfo().getClassIndex();
            while(!bases.isEmpty()) {
                DeclaredType head = bases.remove();
                TypeElement elem = (TypeElement)head.asElement();
                if (!elems.add(elem))
                    continue;
                if (Utilities.startsWith(elem.getSimpleName().toString(), prefix) && trees.isAccessible(scope, elem)) {
                    results.add(JavaCompletionItem.createTypeItem(elem, head, offset, true, elements.isDeprecated(elem)));
                }
                List<? extends TypeMirror> tas = head.getTypeArguments();
                boolean isRaw = !tas.iterator().hasNext();
                subtypes:
                for (ElementHandle<TypeElement> eh : index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    TypeElement e = eh.resolve(controller);
                    if (e != null) {
                        if (trees.isAccessible(scope, e)) {
                            if (isRaw) {
                                DeclaredType dt = types.getDeclaredType(e);
                                bases.add(dt);
                            } else {
                                HashMap<Element, TypeMirror> map = new HashMap<Element, TypeMirror>();
                                TypeMirror sup = e.getSuperclass();
                                if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType)sup).asElement() == elem) {
                                    DeclaredType dt = (DeclaredType)sup;
                                    Iterator<? extends TypeMirror> ittas = tas.iterator();
                                    Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                    while(it.hasNext() && ittas.hasNext()) {
                                        TypeMirror basetm = ittas.next();
                                        TypeMirror stm = it.next();
                                        if (basetm != stm) {
                                            if (stm.getKind() == TypeKind.TYPEVAR) {
                                                map.put(((TypeVariable)stm).asElement(), basetm);
                                            } else {
                                                continue subtypes;
                                            }
                                        }
                                    }
                                    if (it.hasNext() != ittas.hasNext()) {
                                        continue subtypes;
                                    }
                                } else {
                                    for (TypeMirror tm : e.getInterfaces()) {
                                        if (((DeclaredType)tm).asElement() == elem) {
                                            DeclaredType dt = (DeclaredType)tm;
                                            Iterator<? extends TypeMirror> ittas = tas.iterator();
                                            Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                            while(it.hasNext() && ittas.hasNext()) {
                                                TypeMirror basetm = ittas.next();
                                                TypeMirror stm = it.next();
                                                if (basetm != stm) {
                                                    if (stm.getKind() == TypeKind.TYPEVAR) {
                                                        map.put(((TypeVariable)stm).asElement(), basetm);
                                                    } else {
                                                        continue subtypes;
                                                    }
                                                }
                                            }
                                            if (it.hasNext() != ittas.hasNext()) {
                                                continue subtypes;
                                            }
                                            break;
                                        }
                                    }
                                }
                                List<? extends TypeParameterElement> tpes = e.getTypeParameters();
                                TypeMirror[] targs = new TypeMirror[tpes.size()];
                                int i = 0;
                                for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
                                    TypeParameterElement tpe = it.next();
                                    TypeMirror t = map.get(tpe);
                                    targs[i++] = t != null ? t : tpe.asType();
                                }
                                DeclaredType dt = types.getDeclaredType(e, targs);
                                bases.add(dt);
                            }
                        }
                    } else {
                        Logger.getLogger("global").log(Level.FINE, String.format("Cannot resolve: %s on bootpath: %s classpath: %s sourcepath: %s\n", eh.toString(),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE)));
                    }
                }
            }
        }
        
        private void addKeyword(Env env, String kw, String postfix) {
            if (queryType != COMPLETION_SMART_QUERY_TYPE)
                results.add(JavaCompletionItem.createKeywordItem(kw, postfix, env.getOffset()));
        }
        
        private void addKeywordsForCU(Env env) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            List<String> kws = new ArrayList<String>();
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            CompilationUnitTree cu = env.getRoot();
            SourcePositions sourcePositions = env.getSourcePositions();
            kws.add(ABSTRACT_KEYWORD);
            kws.add(CLASS_KEYWORD);
            kws.add(ENUM_KEYWORD);
            kws.add(FINAL_KEYWORD);
            kws.add(INTERFACE_KEYWORD);
            boolean beforeAnyClass = true;
            boolean beforePublicClass = true;
            for(Tree t : cu.getTypeDecls()) {
                if (t.getKind() == Tree.Kind.CLASS) {
                    int pos = (int)sourcePositions.getEndPosition(cu, t);
                    if (pos != Diagnostic.NOPOS && offset >= pos) {
                        beforeAnyClass = false;
                        if (((ClassTree)t).getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                            beforePublicClass = false;
                            break;
                        }
                    }
                }
            }
            if (beforePublicClass)
                kws.add(PUBLIC_KEYWORD);
            if (beforeAnyClass) {
                kws.add(IMPORT_KEYWORD);
                Tree firstImport = null;
                for(Tree t : cu.getImports()) {
                    firstImport = t;
                    break;
                }
                Tree pd = cu.getPackageName();
                if ((pd != null && offset <= sourcePositions.getStartPosition(cu, cu)) ||
                        (pd == null && (firstImport == null || sourcePositions.getStartPosition(cu, firstImport) >= offset)))
                    kws.add(PACKAGE_KEYWORD);
            }
            for (String kw : kws) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
        }
        
        private void addKeywordsForClassBody(Env env) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            for (String kw : CLASS_BODY_KEYWORDS)
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            addPrimitiveTypeKeywords(env);
        }
        
        private void addKeywordsForBlock(Env env) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            for (String kw : STATEMENT_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, null, offset));
            }
            for (String kw : BLOCK_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
            if (Utilities.startsWith(RETURN_KEYWORD, prefix)) {
                TreePath mth = Utilities.getPathElementOfKind(Tree.Kind.METHOD, env.getPath());
                String postfix = SPACE;
                if (mth != null) {
                    Tree rt = ((MethodTree)mth.getLeaf()).getReturnType();
                    if (rt == null || (rt.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)rt).getPrimitiveTypeKind() == TypeKind.VOID))
                        postfix = SEMI;
                }
                results.add(JavaCompletionItem.createKeywordItem(RETURN_KEYWORD, postfix, offset));
            }
            TreePath tp = env.getPath();
            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case SWITCH:
                        CaseTree lastCase = null;
                        CompilationUnitTree root = env.getRoot();
                        SourcePositions sourcePositions = env.getSourcePositions();
                        for (CaseTree t : ((SwitchTree)tp.getLeaf()).getCases()) {
                            if (sourcePositions.getStartPosition(root, t) >= offset)
                                break;
                            lastCase = t;
                        }
                        if (lastCase == null || lastCase.getExpression() != null) {
                            if (Utilities.startsWith(CASE_KEYWORD, prefix))
                                results.add(JavaCompletionItem.createKeywordItem(CASE_KEYWORD, SPACE, offset));
                            if (Utilities.startsWith(DEFAULT_KEYWORD, prefix))
                                results.add(JavaCompletionItem.createKeywordItem(DEFAULT_KEYWORD, COLON, offset));
                        }
                        if (Utilities.startsWith(BREAK_KEYWORD, prefix))
                            results.add(JavaCompletionItem.createKeywordItem(BREAK_KEYWORD, SEMI, offset));
                        break;
                    case DO_WHILE_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case FOR_LOOP:
                    case WHILE_LOOP:
                        if (Utilities.startsWith(BREAK_KEYWORD, prefix))
                            results.add(JavaCompletionItem.createKeywordItem(BREAK_KEYWORD, SEMI, offset));
                        if (Utilities.startsWith(CONTINUE_KEYWORD, prefix))
                            results.add(JavaCompletionItem.createKeywordItem(CONTINUE_KEYWORD, SEMI, offset));
                        break;
                }
                tp = tp.getParentPath();
            }
        }
        
        private void addKeywordsForStatement(Env env) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            for (String kw : STATEMENT_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, null, offset));
            }
            for (String kw : STATEMENT_SPACE_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
            if (Utilities.startsWith(RETURN_KEYWORD, prefix)) {
                TreePath mth = Utilities.getPathElementOfKind(Tree.Kind.METHOD, env.getPath());
                String postfix = SPACE;
                if (mth != null) {
                    Tree rt = ((MethodTree)mth.getLeaf()).getReturnType();
                    if (rt.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)rt).getPrimitiveTypeKind() == TypeKind.VOID)
                        postfix = SEMI;
                }
                results.add(JavaCompletionItem.createKeywordItem(RETURN_KEYWORD, postfix, offset));
            }
            TreePath tp = env.getPath();
            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case DO_WHILE_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case FOR_LOOP:
                    case WHILE_LOOP:
                        if (Utilities.startsWith(CONTINUE_KEYWORD, prefix))
                            results.add(JavaCompletionItem.createKeywordItem(CONTINUE_KEYWORD, SEMI, offset));
                    case SWITCH:
                        if (Utilities.startsWith(BREAK_KEYWORD, prefix))
                            results.add(JavaCompletionItem.createKeywordItem(BREAK_KEYWORD, SEMI, offset));
                        break;
                }
                tp = tp.getParentPath();
            }
        }
        
        private void addValueKeywords(Env env) throws IOException {
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            if (queryType == COMPLETION_SMART_QUERY_TYPE) {
                Set<TypeMirror> smartTypes = getSmartTypes(env);
                if (smartTypes != null && !smartTypes.isEmpty()) {
                    Set<String> kws = new HashSet<String>();
                    for (TypeMirror st : smartTypes) {
                        if (st.getKind() == TypeKind.BOOLEAN) {
                            if (Utilities.startsWith(FALSE_KEYWORD, prefix))
                                results.add(JavaCompletionItem.createKeywordItem(FALSE_KEYWORD, null, offset));
                            if (Utilities.startsWith(TRUE_KEYWORD, prefix))
                                results.add(JavaCompletionItem.createKeywordItem(TRUE_KEYWORD, null, offset));
                        }
                    }
                }
                return;
            }
            for (String kw : VALUE_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, null, offset));
            }
            if (Utilities.startsWith(NEW_KEYWORD, prefix))
                results.add(JavaCompletionItem.createKeywordItem(NEW_KEYWORD, SPACE, offset));
        }

        private void addPrimitiveTypeKeywords(Env env) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            for (String kw : PRIM_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, null, offset));
            }
        }
        
        private void addClassModifiers(Env env, Set<Modifier> modifiers) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            List<String> kws = new ArrayList<String>();
            if (!modifiers.contains(PUBLIC) && !modifiers.contains(PRIVATE)) {
                kws.add(PUBLIC_KEYWORD);
            }
            if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
                kws.add(ABSTRACT_KEYWORD);
                kws.add(FINAL_KEYWORD);
            }
            kws.add(CLASS_KEYWORD);
            kws.add(INTERFACE_KEYWORD);
            kws.add(ENUM_KEYWORD);
            for (String kw : kws) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
        }
        
        private void addMemberModifiers(Env env, Set<Modifier> modifiers, boolean isLocal) {
            if (queryType == COMPLETION_SMART_QUERY_TYPE)
                return;
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            List<String> kws = new ArrayList<String>();
            if (isLocal) {
                if (!modifiers.contains(FINAL)) {
                    kws.add(FINAL_KEYWORD);
                }
            } else {
                if (!modifiers.contains(PUBLIC) && !modifiers.contains(PROTECTED) && !modifiers.contains(PRIVATE)) {
                    kws.add(PUBLIC_KEYWORD);
                    kws.add(PROTECTED_KEYWORD);
                    kws.add(PRIVATE_KEYWORD);
                }
                if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
                    kws.add(ABSTRACT_KEYWORD);
                    kws.add(FINAL_KEYWORD);
                }
                if (!modifiers.contains(STATIC)) {
                    kws.add(STATIC_KEYWORD);
                }
                kws.add(CLASS_KEYWORD);
                kws.add(INTERFACE_KEYWORD);
                kws.add(ENUM_KEYWORD);
                kws.add(NATIVE_KEYWORD);
                kws.add(STRICT_KEYWORD);
                kws.add(SYNCHRONIZED_KEYWORD);
                kws.add(TRANSIENT_KEYWORD);
                kws.add(VOID_KEYWORD);
                kws.add(VOLATILE_KEYWORD);
            }
            for (String kw : kws) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
            for (String kw : PRIM_KEYWORDS) {
                if (Utilities.startsWith(kw, prefix))
                    results.add(JavaCompletionItem.createKeywordItem(kw, SPACE, offset));
            }
        }
        
        private TypeElement getTypeElement(Env env, final String simpleName) throws IOException {
            if (simpleName == null || simpleName.length() == 0)
                return null;
            final CompilationController controller = env.getController();
            final TreeUtilities tu = controller.getTreeUtilities();
            final Scope scope = env.getScope();
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    return (e.getKind().isClass() || e.getKind().isInterface()) &&
                            e.getSimpleName().contentEquals(simpleName) &&
                            (!isStatic || e.getModifiers().contains(STATIC)) &&
                            tu.isAccessible(scope, e, t);
                }
            };
            for(Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor))
                return (TypeElement)e;
            final Trees trees = controller.getTrees();
            acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    return e.getSimpleName().contentEquals(simpleName) && e.getEnclosingElement().getKind() == PACKAGE &&
                            trees.isAccessible(scope, (TypeElement)e);
                }
            };
            for (TypeElement e : controller.getElementUtilities().getGlobalTypes(acceptor))
                if (simpleName.contentEquals(e.getSimpleName()))
                    return e;
            return null;
        }
        
        private VariableElement getFieldOrVar(Env env, final String simpleName) throws IOException {
            if (simpleName == null || simpleName.length() == 0)
                return null;
            final CompilationController controller = env.getController();
            final Scope scope = env.getScope();
            final TypeElement enclClass = scope.getEnclosingClass();
            final boolean isStatic = enclClass == null ? false :
                (controller.getTreeUtilities().isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
            final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
            final ExecutableElement method = scope.getEnclosingMethod();
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                public boolean accept(Element e, TypeMirror t) {
                    if (!e.getSimpleName().contentEquals(simpleName))
                        return false;
                    switch (e.getKind()) {
                        case LOCAL_VARIABLE:
                            if (isStatic && (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)))
                                return false;
                        case EXCEPTION_PARAMETER:
                        case PARAMETER:
                            return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)) &&
                                    !illegalForwardRefs.contains(e);
                        case FIELD:
                            if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                                return !isStatic;
                        case ENUM_CONSTANT:
                            return !illegalForwardRefs.contains(e);
                    }
                    return false;
                }
            };
            for(Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor))
                return (VariableElement)e;
            return null;
        }
        
        private boolean isOfSmartType(TypeMirror type, Set<TypeMirror> smartTypes, Types types) {
            if (smartTypes == null || smartTypes.isEmpty())
                return true;
            for (TypeMirror smartType : smartTypes)
                if (types.isAssignable(type, smartType))
                    return true;
            return false;
        }
        
        private boolean isTopLevelClass(Tree tree, CompilationUnitTree root) {
            if (tree.getKind() == Tree.Kind.CLASS || (tree.getKind() == Tree.Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree)tree).getExpression().getKind() == Tree.Kind.ERRONEOUS)) {
                for (Tree t : root.getTypeDecls())
                    if (tree == t)
                        return true;
            }
            return false;
        }
        private boolean isJavaIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection getFilteredData(Collection<JavaCompletionItem> data, String prefix) {
            if (prefix.length() == 0)
                return data;
            List ret = new ArrayList();
            for (Iterator<JavaCompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();
                if (Utilities.startsWith(itm.getInsertPrefix().toString(), prefix))
                    ret.add(itm);
            }
            return ret;
        }
        
        private boolean isOfKindAndType(Element e, EnumSet<ElementKind> kinds, TypeMirror base, Scope scope, Trees trees, Types types) {
            if (kinds.contains(e.getKind())) {
                if (base == null)
                    return true;
                TypeMirror type;
                switch(e.getKind()) {
                    case CONSTRUCTOR:
                        type = e.getEnclosingElement().asType();
                        break;
                    case METHOD:
                        type = ((ExecutableType)e.asType()).getReturnType();
                        break;
                    default:
                        type = e.asType();
                }
                if (types.isSubtype(type, base))
                    return true;
            }
            if (e.getKind().isClass() || e.getKind().isInterface()) {
                DeclaredType type = (DeclaredType)e.asType();
                for (Element ee : e.getEnclosedElements())
                    if (trees.isAccessible(scope, ee, type) && isOfKindAndType(ee, kinds, base, scope, trees, types))
                        return true;
            }
            return false;
        }
        
        private Set<TypeMirror> getSmartTypes(Env env) throws IOException {
            int offset = env.getOffset();
            final CompilationController controller = env.getController();
            TreePath path = env.getPath();
            Tree lastTree = null;
            while(path != null) {
                Tree tree = path.getLeaf();
                switch(tree.getKind()) {
                    case VARIABLE:
                        return Collections.singleton(controller.getTrees().getTypeMirror(new TreePath(path, ((VariableTree)tree).getType())));
                    case ASSIGNMENT:
                        TypeMirror type = controller.getTrees().getTypeMirror(new TreePath(path, ((AssignmentTree)tree).getVariable()));
                        TreePath parentPath = path.getParentPath();
                        if (parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION && type.getKind() == TypeKind.EXECUTABLE)
                            type = ((ExecutableType)type).getReturnType();
                        return Collections.singleton(type);
                    case RETURN:
                        TreePath methodPath = Utilities.getPathElementOfKind(Tree.Kind.METHOD, path);
                        return methodPath != null ? Collections.singleton(controller.getTrees().getTypeMirror(new TreePath(methodPath, ((MethodTree)methodPath.getLeaf()).getReturnType()))) : null;
                    case THROW:
                        methodPath = Utilities.getPathElementOfKind(Tree.Kind.METHOD, path);
                        if (methodPath == null)
                            return null;
                        HashSet<TypeMirror> ret = new HashSet<TypeMirror>();
                        Trees trees = controller.getTrees();
                        for (ExpressionTree thr : ((MethodTree)methodPath.getLeaf()).getThrows())
                            ret.add(trees.getTypeMirror(new TreePath(methodPath, thr)));
                        return ret;
                    case IF:
                        IfTree iff = (IfTree)tree;
                        return iff.getCondition() == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                    case WHILE_LOOP:
                        WhileLoopTree wl = (WhileLoopTree)tree;
                        return wl.getCondition() == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                    case FOR_LOOP:
                        ForLoopTree fl = (ForLoopTree)tree;
                        Tree cond = fl.getCondition();
                        if (lastTree != null) {
                            if (cond instanceof ErroneousTree) {
                                Iterator<? extends Tree> itt =((ErroneousTree)cond).getErrorTrees().iterator();
                                if (itt.hasNext())
                                    cond = itt.next();
                            }
                            return cond == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                        }
                        SourcePositions sourcePositions = env.getSourcePositions();
                        CompilationUnitTree root = env.getRoot();
                        if (cond != null && sourcePositions.getEndPosition(root, cond) < offset)
                            return null;
                        Tree lastInit = null;
                        for (Tree init : fl.getInitializer()) {
                            if (sourcePositions.getEndPosition(root, init) >= offset)
                                return null;
                            lastInit = init;
                        }
                        String text = null;
                        if (lastInit == null) {
                            text = controller.getText().substring((int)sourcePositions.getStartPosition(root, fl), offset).trim();
                            int idx = text.indexOf('('); //NOI18N
                            if (idx >= 0)
                                text = text.substring(idx + 1);
                        } else {
                            text = controller.getText().substring((int)sourcePositions.getEndPosition(root, lastInit), offset).trim();
                        }
                        return ";".equals(text) ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null; //NOI18N
                    case ENHANCED_FOR_LOOP:
                        EnhancedForLoopTree efl = (EnhancedForLoopTree)tree;
                        Tree expr = efl.getExpression();
                        if (lastTree != null) {
                            if (expr instanceof ErroneousTree) {
                                Iterator<? extends Tree> itt =((ErroneousTree)expr).getErrorTrees().iterator();
                                if (itt.hasNext())
                                    expr = itt.next();
                            }
                            if(expr != lastTree)
                                return null;
                        } else {
                            sourcePositions = env.getSourcePositions();
                            root = env.getRoot();
                            text = null;
                            if (efl.getVariable() == null) {
                                text = controller.getText().substring((int)sourcePositions.getStartPosition(root, efl), offset).trim();
                                int idx = text.indexOf('('); //NOI18N
                                if (idx >= 0)
                                    text = text.substring(idx + 1);
                            } else {
                                text = controller.getText().substring((int)sourcePositions.getEndPosition(root, efl.getVariable()), offset).trim();
                            }
                            if (!":".equals(text))
                                return null;
                        }
                        Elements elements = controller.getElements();
                        TypeElement iter = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                        TypeMirror var = efl.getVariable() != null ? controller.getTrees().getTypeMirror(new TreePath(path, efl.getVariable())) : null;
                        ret = new HashSet<TypeMirror>();
                        Types types = controller.getTypes();
                        if (var != null) {
                            ret.add(types.getDeclaredType(iter, types.getWildcardType(var, null)));
                            ret.add(types.getArrayType(var));
                        } else {
                            ret.add(types.getDeclaredType(iter));
                            ret.add(types.getArrayType(elements.getTypeElement("java.lang.Object").asType())); //NOI18N
                        }
                        return ret;
                    case SWITCH:
                        SwitchTree sw = (SwitchTree)tree;
                        if (sw.getExpression() != lastTree)
                            return null;
                        ret = new HashSet<TypeMirror>();
                        types = controller.getTypes();
                        ret.add(controller.getTypes().getPrimitiveType(TypeKind.INT));
                        ret.add(types.getDeclaredType(controller.getElements().getTypeElement("java.lang.Enum")));
                        return ret;
                    case METHOD_INVOCATION:
                        MethodInvocationTree mi = (MethodInvocationTree)tree;
                        sourcePositions = env.getSourcePositions();
                        root = env.getRoot();
                        List<Tree> argTypes = getArgumentsUpToPos(env, mi.getArguments(), (int)sourcePositions.getEndPosition(root, mi.getMethodSelect()), lastTree != null ? (int)sourcePositions.getStartPosition(root, lastTree) : offset);
                        if (argTypes != null) {
                            TypeMirror[] args = new TypeMirror[argTypes.size()];
                            int j = 0;
                            for (Tree t : argTypes)
                                args[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                            Tree mid = mi.getMethodSelect();
                            path = new TreePath(path, mid);
                            switch (mid.getKind()) {
                                case MEMBER_SELECT: {
                                    String name = ((MemberSelectTree)mid).getIdentifier().toString();
                                    ExpressionTree exp = ((MemberSelectTree)mid).getExpression();
                                    path = new TreePath(path, exp);
                                    final TypeMirror tm = controller.getTrees().getTypeMirror(path);
                                    final Element el = controller.getTrees().getElement(path);
                                    final TreeUtilities tu = controller.getTreeUtilities();
                                    if (el != null && tm.getKind() == TypeKind.DECLARED) {
                                        final boolean isStatic = el.getKind().isClass() || el.getKind().isInterface();
                                        final Scope scope = env.getScope();
                                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                            public boolean accept(Element e, TypeMirror t) {
                                                return e.getKind() == METHOD && (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                                            }
                                        };
                                        return getMatchingArgumentTypes(tm, controller.getElementUtilities().getMembers(tm, acceptor), name, args, controller.getTypes());
                                    }
                                    return null;
                                }
                                case IDENTIFIER: {
                                    String name = ((IdentifierTree)mid).getName().toString();
                                    final Scope scope = env.getScope();
                                    final TreeUtilities tu = controller.getTreeUtilities();
                                    final TypeElement enclClass = scope.getEnclosingClass();
                                    final boolean isStatic = enclClass != null ? tu.isStaticContext(scope) : false;
                                    if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                            public boolean accept(Element e, TypeMirror t) {
                                                return e.getKind() == CONSTRUCTOR && tu.isAccessible(scope, e, t);
                                            }
                                        };
                                        TypeMirror superclass = enclClass.getSuperclass();
                                        return getMatchingArgumentTypes(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, args, controller.getTypes());
                                    }
                                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                        public boolean accept(Element e, TypeMirror t) {
                                            return e.getKind() == METHOD && (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                                        }
                                    };
                                    return getMatchingArgumentTypes(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), THIS_KEYWORD.equals(name) ? INIT : name, args, controller.getTypes());
                                }
                            }
                        }
                        return null;
                    case NEW_CLASS:
                        NewClassTree nc = (NewClassTree)tree;
                        sourcePositions = env.getSourcePositions();
                        root = env.getRoot();
                        int idEndPos = (int)sourcePositions.getEndPosition(root, nc.getIdentifier());
                        if (controller.getText().substring(idEndPos, offset).indexOf('(') < 0)
                            break;
                        argTypes = getArgumentsUpToPos(env, nc.getArguments(), idEndPos, lastTree != null ? (int)sourcePositions.getStartPosition(root, lastTree) : offset);
                        if (argTypes != null) {
                            TypeMirror[] args = new TypeMirror[argTypes.size()];
                            int j = 0;
                            for (Tree t : argTypes)
                                args[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                            Tree mid = nc.getIdentifier();
                            path = new TreePath(path, mid);
                            final TypeMirror tm = controller.getTrees().getTypeMirror(path);
                            final Element el = controller.getTrees().getElement(path);
                            final TreeUtilities tu = controller.getTreeUtilities();
                            if (el != null && tm.getKind() == TypeKind.DECLARED) {
                                final Scope scope = env.getScope();
                                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                    public boolean accept(Element e, TypeMirror t) {
                                        return e.getKind() == CONSTRUCTOR && tu.isAccessible(scope, e, t);
                                    }
                                };
                                return getMatchingArgumentTypes(tm, controller.getElementUtilities().getMembers(tm, acceptor), INIT, args, controller.getTypes());
                            }
                            return null;
                        }
                        return null;
                    case ANNOTATION:
                        AnnotationTree ann = (AnnotationTree)tree;
                        if (ann.getArguments().isEmpty()) {
                            root = env.getRoot();
                            text = controller.getText().substring((int)env.getSourcePositions().getEndPosition(root, ann.getAnnotationType()), offset).trim();
                            if ("(".equals(text)) { //NOI18N
                                TypeElement el = (TypeElement)controller.getTrees().getElement(new TreePath(path, ann.getAnnotationType()));
                                for (Element ee : el.getEnclosedElements()) {
                                    if (ee.getKind() == METHOD && "value".contentEquals(ee.getSimpleName()))
                                        return Collections.singleton(((ExecutableElement)ee).getReturnType());
                                }
                            }
                        }
                        return null;
                }
                lastTree = tree;
                path = path.getParentPath();
            }
            return null;
        }
        
        private TokenSequence<JavaTokenId> findLastNonWhitespaceToken(Env env, Tree tree, int position) {
            int startPos = (int)env.getSourcePositions().getStartPosition(env.getRoot(), tree);
            return findLastNonWhitespaceToken(env, startPos, position);
        }
        
        private TokenSequence<JavaTokenId> findLastNonWhitespaceToken(Env env, int startPos, int endPos) {
            TokenSequence<JavaTokenId> ts = env.getController().getTokenHiearchy().tokenSequence(JavaTokenId.language());
            if (ts.move(endPos) == 0)
                ts.movePrevious();
            do {
                int offset = ts.offset();
                if (offset < startPos)
                    return null;
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                        break;
                    default:
                        return ts;
                }
            } while (ts.movePrevious());
            return null;
        }
        
        private List<Tree> getArgumentsUpToPos(Env env, Iterable<? extends ExpressionTree> args, int startPos, int position) {
            List<Tree> ret = new ArrayList<Tree>();
            CompilationUnitTree root = env.getRoot();
            SourcePositions sourcePositions = env.getSourcePositions();
            for (ExpressionTree e : args) {
                int pos = (int)sourcePositions.getEndPosition(root, e);
                if (pos != Diagnostic.NOPOS && position > pos) {
                    startPos = pos;
                    ret.add(e);
                }
            }
            if (position > startPos) {
                String text = env.getController().getText().substring(startPos, position).trim();
                if ("(".equals(text) || ",".equals(text)) //NOI18N
                    return ret;
            }
            return null;
        }
        
        private List<List<String>> getMatchingParams(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
            List<List<String>> ret = new ArrayList<List<String>>();
            for (Element e : elements) {
                if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                    int i = 0;
                    List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                    if (params.size() < argTypes.length) {
                        continue;
                    }
                    if (params.size() == 0) {
                        ret.add(Collections.<String>singletonList(NbBundle.getMessage(JavaCompletionProvider.class, "JCP-no-parameters")));
                    } else {
                        ExecutableType eType = (ExecutableType)asMemberOf(e, type, types);
                        for (TypeMirror param : eType.getParameterTypes()) {
                            if (i == argTypes.length) {
                                List<String> paramStrings = new ArrayList<String>(params.size());
                                Iterator<? extends TypeMirror> tIt = eType.getParameterTypes().iterator();
                                for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                                    VariableElement ve = it.next();
                                    StringBuffer sb = new StringBuffer();
                                    sb.append(Utilities.getTypeName(tIt.next(), false));
                                    CharSequence veName = ve.getSimpleName();
                                    if (veName != null && veName.length() > 0) {
                                        sb.append(" "); // NOI18N
                                        sb.append(veName);
                                    }
                                    if (it.hasNext()) {
                                        sb.append(", "); // NOI18N
                                    }
                                    paramStrings.add(sb.toString());
                                }
                                ret.add(paramStrings);
                                break;
                            }
                            if (!types.isAssignable(argTypes[i++], param))
                                break;
                        }
                    }
                }
            }
            return ret.isEmpty() ? null : ret;
        }
        
        private Set<TypeMirror> getMatchingArgumentTypes(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
            Set<TypeMirror> ret = new HashSet<TypeMirror>();
            for (Element e : elements) {
                if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                    int i = 0;
                    Collection<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                    if (params.size() <= argTypes.length)
                        continue;
                    for (TypeMirror param : ((ExecutableType)asMemberOf(e, type, types)).getParameterTypes()) {
                        if (i == argTypes.length) {
                            ret.add(param);
                            break;
                        }
                        if (!types.isAssignable(argTypes[i++], param))
                            break;
                    }
                }
            }
            return ret.isEmpty() ? null : ret;
        }
        
        private TypeMirror asMemberOf(Element element, TypeMirror type, Types types) {
            TypeMirror ret = element.asType();
            TypeMirror enclType = types.erasure(element.getEnclosingElement().asType());
            while(type != null && type.getKind() == TypeKind.DECLARED) {
                if (types.isSubtype(type, enclType)) {
                    ret = types.asMemberOf((DeclaredType)type, element);
                    break;
                }
                type = ((DeclaredType)type).getEnclosingType();
            }
            return ret;
        }       
        
        private Tree unwrapErrTree(Tree tree) {
            if (tree != null && tree.getKind() == Tree.Kind.ERRONEOUS) {
                Iterator<? extends Tree> it = ((ErroneousTree)tree).getErrorTrees().iterator();
                tree = it.hasNext() ? it.next() : null;
            }
            return tree;
        }
        
        private boolean withinScope(Env env, TypeElement e) throws IOException {
            for (Element encl = env.getScope().getEnclosingClass(); encl != null; encl = encl.getEnclosingElement()) {
                if (e == encl)
                    return true;
            }
            return false;
        }
        
        private Env getCompletionEnvironment(CompilationController controller, boolean upToOffset) throws IOException {
            int offset = caretOffset;
            String prefix = null;
            if (upToOffset) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHiearchy().tokenSequence(JavaTokenId.language());
                if (ts.moveNext()) { //check for empty file
                    if (ts.move(offset) == 0)
                        ts.movePrevious();
                    if (ts.offset() < offset && (ts.token().id() == JavaTokenId.IDENTIFIER || "keyword".equals(ts.token().id().primaryCategory()))) { //TODO: Use isKeyword(...) when available
                        prefix = ts.token().toString().substring(0, offset - ts.offset());
                        offset = ts.offset();
                    }
                }
            }
            controller.toPhase(Phase.PARSED);
            TreeUtilities tu = controller.getTreeUtilities();
            TreePath path = tu.pathFor(offset);
            SourcePositions sourcePositions = controller.getTrees().getSourcePositions();
            if (!upToOffset && Phase.RESOLVED.compareTo(controller.getPhase()) <= 0)
                return new Env(offset, prefix, controller, path, sourcePositions, null);
            CompilationUnitTree root = controller.getCompilationUnit();
            Scope scope = null;
            LinkedList<TreePath> reversePath = new LinkedList<TreePath>();
            TreePath treePath = path;
            while (treePath != null) {
                reversePath.addFirst(treePath);
                treePath = treePath.getParentPath();
            }
            for (TreePath tp : reversePath) {
                Tree tree = tp.getLeaf();
                Tree parent = tp.getParentPath() == null ? null : tp.getParentPath().getLeaf();
                if (tree.getKind() == Tree.Kind.BLOCK) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    int blockPos = (int)sourcePositions.getStartPosition(root, tree);
                    String blockText = controller.getText().substring(blockPos, upToOffset ? offset : (int)sourcePositions.getEndPosition(root, tree));
                    final SourcePositions[] sp = new SourcePositions[1];
                    final BlockTree block = (BlockTree)(((BlockTree)tree).isStatic() ? tu.parseStaticBlock(blockText, sp) : tu.parseStatement(blockText, sp));
                    if (block == null)
                        break;
                    sourcePositions = new SourcePositionsImpl(block, sourcePositions, sp[0], blockPos, upToOffset ? offset : -1);
                    path = tu.pathFor(new TreePath(tp.getParentPath(), block), offset, sourcePositions);
                    scope = controller.getTrees().getScope(tp);
                    if (upToOffset) {
                        Tree last = path.getLeaf();
                        List<? extends StatementTree> stmts = null;
                        switch (path.getLeaf().getKind()) {
                            case BLOCK:
                                stmts = ((BlockTree)path.getLeaf()).getStatements();
                                break;
                            case FOR_LOOP:
                                stmts = ((ForLoopTree)path.getLeaf()).getInitializer();
                                break;
                            case ENHANCED_FOR_LOOP:
                                stmts = Collections.singletonList(((EnhancedForLoopTree)path.getLeaf()).getStatement());
                                break;
                            case METHOD:
                                stmts = ((MethodTree)path.getLeaf()).getParameters();
                                break;
                        }
                        if (stmts != null) {
                            for (StatementTree st : stmts) {
                                if (sourcePositions.getEndPosition(root, st) <= offset)
                                    last = st;
                            }
                        }
                        scope = tu.attributeTreeTo(block, scope, last);
                    } else {
                        tu.attributeTree(block, scope);
                    }
                    break;
                } else if (parent != null && parent.getKind() == Tree.Kind.VARIABLE && unwrapErrTree(((VariableTree)parent).getInitializer()) == tree) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    final int initPos = (int)sourcePositions.getStartPosition(root, tree);
                    String initText = controller.getText().substring(initPos, upToOffset ? offset : (int)sourcePositions.getEndPosition(root, tree));
                    final SourcePositions[] sp = new SourcePositions[1];
                    final ExpressionTree init = tu.parseVariableInitializer(initText, sp);
                    final ExpressionStatementTree fake = new ExpressionStatementTree() {
                        public Object accept(TreeVisitor v, Object p) {
                            return v.visitExpressionStatement(this, p);
                        }
                        public ExpressionTree getExpression() {
                            return init;
                        }
                        public Kind getKind() {
                            return Tree.Kind.EXPRESSION_STATEMENT;
                        }
                    };
                    sourcePositions = new SourcePositionsImpl(fake, sourcePositions, sp[0], initPos, upToOffset ? offset : -1);
                    path = tu.pathFor(new TreePath(tp.getParentPath(), fake), offset, sourcePositions);
                    scope = controller.getTrees().getScope(tp);
                    if (upToOffset && sp[0].getEndPosition(root, init) + initPos > offset) {
                        scope = tu.attributeTreeTo(init, scope, path.getLeaf());
                    } else {
                        tu.attributeTree(init, scope);
                    }
                    break;
                } else if (tree.getKind() == Tree.Kind.VARIABLE && ((VariableTree)tree).getInitializer() != null && tp == path && sourcePositions.getStartPosition(root, ((VariableTree)tree).getInitializer()) <= offset) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    tree = ((VariableTree)tree).getInitializer();
                    final int initPos = (int)sourcePositions.getStartPosition(root, tree);
                    String initText = controller.getText().substring(initPos, offset);
                    final SourcePositions[] sp = new SourcePositions[1];
                    final ExpressionTree init = tu.parseVariableInitializer(initText, sp);
                    scope = controller.getTrees().getScope(new TreePath(tp, tree));
                    final ExpressionStatementTree fake = new ExpressionStatementTree() {
                        public Object accept(TreeVisitor v, Object p) {
                            return v.visitExpressionStatement(this, p);
                        }
                        public ExpressionTree getExpression() {
                            return init;
                        }
                        public Kind getKind() {
                            return Tree.Kind.EXPRESSION_STATEMENT;
                        }
                    };
                    sourcePositions = new SourcePositionsImpl(fake, sourcePositions, sp[0], initPos, offset);
                    path = tu.pathFor(new TreePath(tp, fake), offset, sourcePositions);
                    tu.attributeTree(init, scope);
                    break;
                }
            }
            return new Env(offset, prefix, controller, path, sourcePositions, scope);
        }
        
        private class SourcePositionsImpl extends TreeScanner<Void, Tree> implements SourcePositions {
            
            private Tree root;
            private SourcePositions original;
            private SourcePositions modified;
            private int startOffset;
            private int endOffset;
            
            private boolean found;
            
            private SourcePositionsImpl(Tree root, SourcePositions original, SourcePositions modified, int startOffset, int endOffset) {
                this.root = root;
                this.original = original;
                this.modified = modified;
                this.startOffset = startOffset;
                this.endOffset = endOffset;
            }
            
            public long getStartPosition(CompilationUnitTree compilationUnitTree, Tree tree) {
                if (tree == root)
                    return startOffset;
                found = false;
                scan(root, tree);
                return found ? modified.getStartPosition(compilationUnitTree, tree) + startOffset : original.getStartPosition(compilationUnitTree, tree);
            }

            public long getEndPosition(CompilationUnitTree compilationUnitTree, Tree tree) {
                if (endOffset >= 0 && (tree == root))
                    return endOffset;
                found = false;
                scan(root, tree);
                return found ? modified.getEndPosition(compilationUnitTree, tree) + startOffset : original.getEndPosition(compilationUnitTree, tree);
            }

            public Void scan(Tree node, Tree p) {
                if (node == p)
                    found = true;
                else
                    super.scan(node, p);
                return null;
            }
        }
                
        private class Env {
            private int offset;
            private String prefix;
            private CompilationController controller;
            private TreePath path;
            private SourcePositions sourcePositions;
            private Scope scope;
            private Collection<? extends Element> refs = null;
            
            private Env(int offset, String prefix, CompilationController controller, TreePath path, SourcePositions sourcePositions, Scope scope) {
                this.offset = offset;
                this.prefix = prefix;
                this.controller = controller;
                this.path = path;
                this.sourcePositions = sourcePositions;
                this.scope = scope;
            }
            
            public int getOffset() {
                return offset;
            }
            
            public String getPrefix() {
                return prefix;
            }
            
            public CompilationController getController() {
                return controller;
            }
            
            public CompilationUnitTree getRoot() {
                return path.getCompilationUnit();
            }
            
            public TreePath getPath() {
                return path;
            }
            
            public SourcePositions getSourcePositions() {
                return sourcePositions;
            }
            
            public Scope getScope() throws IOException {
                if (scope == null) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    scope = controller.getTreeUtilities().scopeFor(offset);
                }
                return scope;
            }

            public Collection<? extends Element> getForwardReferences() {
                if (refs == null)
                    refs = Utilities.getForwardReferences(path, offset, sourcePositions, controller.getTrees());
                return refs;
            }
        }
    }
}
