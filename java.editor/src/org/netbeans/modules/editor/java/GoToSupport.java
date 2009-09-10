/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class GoToSupport {
    
    /** Creates a new instance of GoToSupport */
    public GoToSupport() {
    }
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        return od != null ? od.getPrimaryFile() : null;
    }
    
    public static String getGoToElementTooltip(Document doc, final int offset, final boolean goToSource) {
        return performGoTo(doc, offset, goToSource, true, false);
    }
    
    private static boolean isError(Element el) {
        return el == null || el.asType() == null || el.asType().getKind() == TypeKind.ERROR;
    }
    
    private static String performGoTo(final Document doc, final int offset, final boolean goToSource, final boolean tooltip, final boolean javadoc) {
        if (!tooltip && !javadoc) {
            final AtomicBoolean cancel = new AtomicBoolean();
            
            RunOffAWT.runOffAWT(new Runnable() {
                public void run() {
                    performGoToImpl(doc, offset, goToSource, tooltip, javadoc, cancel);
                }
            }, NbBundle.getMessage(GoToSupport.class, goToSource ? "LBL_GoToSource" : "LBL_GoToDeclaration"), cancel);
            
            return null;
        } else {
            return performGoToImpl(doc, offset, goToSource, tooltip, javadoc, null);
        }
    }

    private static String performGoToImpl (final Document doc, final int offset, final boolean goToSource, final boolean tooltip, final boolean javadoc, final AtomicBoolean cancel) {
        try {
            final FileObject fo = getFileObject(doc);
            
            if (fo == null)
                return null;
            
            final String[] result = new String[1];
            final int[] offsetToOpen = new int[] {-1};
            final ElementHandle[] elementToOpen = new ElementHandle[1];
            final String[] displayNameForError = new String[1];
            final boolean[] tryToOpen = new boolean[1];
            final ClasspathInfo[] cpInfo = new ClasspathInfo[1];
            
            ParserManager.parse(Collections.singleton (Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result res = resultIterator.getParserResult (offset);
                    if (cancel != null && cancel.get())
                        return ;
                    CompilationController controller = CompilationController.get(res);
                    if (controller == null || controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                        return;
                    cpInfo[0] = controller.getClasspathInfo();
                    Token<JavaTokenId>[] token = new Token[1];
                    int[] span = getIdentifierSpan(doc, offset, token);
                    
                    if (span == null) {
                        CALLER.beep(goToSource, javadoc);
                        return ;
                    }
                    
                    int exactOffset = controller.getPositionConverter().getJavaSourcePosition(span[0] + 1);
                    
                    Element el = null;
                    boolean insideImportStmt = false;
                    TreePath path = controller.getTreeUtilities().pathFor(exactOffset);
                    
                    if (token[0] != null && token[0].id() == JavaTokenId.JAVADOC_COMMENT) {
                        el = JavadocImports.findReferencedElement(controller, offset);
                    } else {
                        TreePath parent = path.getParentPath();

                        if (parent != null) {
                            Tree parentLeaf = parent.getLeaf();

                            if (parentLeaf.getKind() == Kind.NEW_CLASS && ((NewClassTree) parentLeaf).getIdentifier() == path.getLeaf()) {
                                if (!isError(controller.getTrees().getElement(path.getParentPath()))) {
                                    path = path.getParentPath();
                                }
                            } else if (parentLeaf.getKind() == Kind.IMPORT && ((ImportTree) parentLeaf).isStatic()) {
                                el = handleStaticImport(controller, (ImportTree) parentLeaf);
                                insideImportStmt = true;
                            } else {
                                if (   parentLeaf.getKind() == Kind.PARAMETERIZED_TYPE
                                    && parent.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS
                                    && ((ParameterizedTypeTree) parentLeaf).getType() == path.getLeaf()) {
                                    if (!isError(controller.getTrees().getElement(parent.getParentPath()))) {
                                        path = parent.getParentPath();
                                    }
                                }
                            }

                            if (el == null) {
                                el = controller.getTrees().getElement(path);

                                if (parentLeaf.getKind() == Kind.METHOD_INVOCATION && isError(el)) {
                                    ExecutableElement ee = Utilities.fuzzyResolveMethodInvocation(controller, path.getParentPath(), new TypeMirror[1], new int[1]);

                                    if (ee != null) {
                                        el = ee;
                                    } else {
                                        ExpressionTree select = ((MethodInvocationTree)parentLeaf).getMethodSelect();
                                        Name methodName = null;
                                        switch (select.getKind()) {
                                            case IDENTIFIER:
                                                Scope s = controller.getTrees().getScope(path);
                                                el = s.getEnclosingClass();
                                                methodName = ((IdentifierTree)select).getName();
                                                break;
                                            case MEMBER_SELECT:
                                                el = controller.getTrees().getElement(new TreePath(path, ((MemberSelectTree)select).getExpression()));
                                                methodName = ((MemberSelectTree)select).getIdentifier();
                                                break;
                                        }
                                        if (el != null) {
                                            for (ExecutableElement m : ElementFilter.methodsIn(el.getEnclosedElements())) {
                                                if (m.getSimpleName() == methodName) {
                                                    el = m;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (!tooltip)
                                CALLER.beep(goToSource, javadoc);
                            else
                                result[0] = null;
                            return;
                        }
                    }
                    
                    if (isError(el)) {
                        if (!tooltip)
                            CALLER.beep(goToSource, javadoc);
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (goToSource && !insideImportStmt) {
                        TypeMirror type = null;
                        
                        if (el instanceof VariableElement)
                            type = el.asType();
                        
                        if (type != null && type.getKind() == TypeKind.DECLARED) {
                            el = ((DeclaredType)type).asElement();
                        }
                    }
                    
                    if (isError(el)) {
                        if (!tooltip)
                            CALLER.beep(goToSource, javadoc);
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (controller.getElementUtilities().isSynthetic(el) && el.getKind() == ElementKind.CONSTRUCTOR) {
                        //check for annonymous innerclasses:
                        el = handlePossibleAnonymousInnerClass(controller, el);
                    }
                    
                    if (isError(el)) {
                        if (!tooltip)
                            CALLER.beep(goToSource, javadoc);
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (el.getKind() != ElementKind.CONSTRUCTOR && (token[0].id() == JavaTokenId.SUPER || token[0].id() == JavaTokenId.THIS)) {
                        if (!tooltip)
                            CALLER.beep(goToSource, javadoc);
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (tooltip) {
                        DisplayNameElementVisitor v = new DisplayNameElementVisitor();
                        
                        v.visit(el, true);
                        
                        result[0] = "<html><body>" + v.result.toString();
                    } else if (javadoc) {
                        result[0] = null;
                        URL url = SourceUtils.getJavadoc(el, controller.getClasspathInfo());
                        if (url != null) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } else {
                            CALLER.beep(goToSource, javadoc);
                        }
                    } else {
                        TreePath elpath = controller.getTrees().getPath(el);
                        Tree tree = elpath != null && path.getCompilationUnit() == elpath.getCompilationUnit()? elpath.getLeaf(): null;
                        
                        if (tree == null && (el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE)) {
                            while (path.getLeaf().getKind() != Kind.METHOD && path.getLeaf().getKind() != Kind.CLASS) {
                                path = path.getParentPath();
                            }
                            
                            FindVariableDeclarationVisitor v = new FindVariableDeclarationVisitor();
                            
                            v.info = controller;
                            v.scan(path, el);
                            
                            tree = v.found;
                        }
                        
                        if (tree != null) {
                            long startPos = controller.getTrees().getSourcePositions().getStartPosition(controller.getCompilationUnit(), tree);
                            
                            if (startPos != (-1)) {
                                //check if the caret is inside the declaration itself, as jump in this case is not very usefull:
                                if (isCaretInsideDeclarationName(controller, tree, elpath, offset)) {
                                    CALLER.beep(goToSource, javadoc);
                                } else {
                                    //#71272: it is necessary to translate the offset:
                                    offsetToOpen[0] = controller.getPositionConverter().getOriginalPosition((int) startPos);
                                    displayNameForError[0] = Utilities.getElementName(el, false).toString();
                                    tryToOpen[0] = true;
                                }
                            } else {
                                CALLER.beep(goToSource, javadoc);
                            }
                        } else {
                            elementToOpen[0] = ElementHandle.create(el);
                            displayNameForError[0] = Utilities.getElementName(el, false).toString();
                            tryToOpen[0] = true;
                        }
                    }
                }
            });
            
            if (tryToOpen[0]) {
                assert result[0] == null;

                boolean openSucceeded = false;

                if (cancel.get()) return null;

                if (offsetToOpen[0] >= 0) {
                    openSucceeded = CALLER.open(fo, offsetToOpen[0]);
                } else {
                    if (elementToOpen[0] != null) {
                        openSucceeded = CALLER.open(cpInfo[0], elementToOpen[0]);
                    }
                }
                if (!openSucceeded) {
                    CALLER.warnCannotOpen(displayNameForError[0]);
                }
            }
            
            return result[0];
        } catch (ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public static void goTo(final Document doc, final int offset, final boolean goToSource) {
        performGoTo(doc, offset, goToSource, false, false);
    }
    
    public static void goToJavadoc(Document doc, int offset) {
        performGoTo(doc, offset, false, false, true);
    }
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.IDENTIFIER, JavaTokenId.THIS, JavaTokenId.SUPER);
    
    public static int[] getIdentifierSpan(Document doc, int offset, Token<JavaTokenId>[] token) {
        if (getFileObject(doc) == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        
        if (JavaTokenId.JAVADOC_COMMENT == t.id()) {
            // javadoc hyperlinking (references + param names)
            TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
            if (JavadocImports.isInsideReference(jdts, offset) || JavadocImports.isInsideParamName(jdts, offset)) {
                jdts.move(offset);
                jdts.moveNext();
                if (token != null) {
                    token[0] = t;
                }
                return new int [] {jdts.offset(), jdts.offset() + jdts.token().length()};
            }
            return null;
        } else if (!USABLE_TOKEN_IDS.contains(t.id())) {
            ts.move(offset - 1);
            if (!ts.moveNext())
                return null;
            t = ts.token();
            if (!USABLE_TOKEN_IDS.contains(t.id()))
                return null;
        }
        
        if (token != null)
            token[0] = t;
        
        return new int [] {ts.offset(), ts.offset() + t.length()};
    }
    
    private static Element handlePossibleAnonymousInnerClass(CompilationInfo info, final Element el) {
        Element encl = el.getEnclosingElement();
        Element doubleEncl = encl != null ? encl.getEnclosingElement() : null;
        
        if (   doubleEncl != null
            && !doubleEncl.getKind().isClass()
            && !doubleEncl.getKind().isInterface()
            && doubleEncl.getKind() != ElementKind.PACKAGE
            && encl.getKind() == ElementKind.CLASS) {
            TreePath enclTreePath = info.getTrees().getPath(encl);
            Tree enclTree = enclTreePath != null ? enclTreePath.getLeaf() : null;
            
            if (enclTree != null && enclTree.getKind() == Tree.Kind.CLASS && enclTreePath.getParentPath().getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
                NewClassTree nct = (NewClassTree) enclTreePath.getParentPath().getLeaf();
                
                if (nct.getClassBody() != null) {
                    Element parentElement = info.getTrees().getElement(new TreePath(enclTreePath, nct.getIdentifier()));
                    
                    if (parentElement == null || parentElement.getKind().isInterface()) {
                        return parentElement;
                    } else {
                        //annonymous innerclass extending a class. Find out which constructor is used:
                        TreePath superConstructorCall = new FindSuperConstructorCall().scan(enclTreePath, null);
                        
                        if (superConstructorCall != null) {
                            return info.getTrees().getElement(superConstructorCall);
                        }
                    }
                }
            }
            
            return null;//prevent jumps to incorrect positions
        } else {
            if (encl != null)
                return encl;
            else
                return el;
        }
    }
    
    /**
     * Tries to guess element referenced by static import. It may not be deterministic
     * as in <code>import static java.awt.Color.getColor</code>.
     */
    private static Element handleStaticImport(CompilationInfo javac, ImportTree impt) {
        Tree impIdent = impt.getQualifiedIdentifier();
        if (!impt.isStatic() || impIdent == null || impIdent.getKind() != Kind.MEMBER_SELECT) {
            return null;
        }
        
        // resolve type element containing imported element
        Trees trees = javac.getTrees();
        MemberSelectTree select = (MemberSelectTree) impIdent;
        Name mName = select.getIdentifier();
        TreePath cutPath = new TreePath(javac.getCompilationUnit());
        TreePath selectPath = new TreePath(new TreePath(cutPath, impt), select.getExpression());
        Element selectElm = trees.getElement(selectPath);
        if (isError(selectElm)) {
            return null;
        }
        
        // resolve class to determine scope
        TypeMirror clazzMir = null;
        TreePath clazzPath = null;
        List<? extends Tree> decls = javac.getCompilationUnit().getTypeDecls();
        if (!decls.isEmpty()) {
            Tree clazz = decls.get(0);
            if (clazz.getKind() == Kind.CLASS) {
                clazzPath = new TreePath(cutPath, clazz);
                Element clazzElm = trees.getElement(clazzPath);
                if (isError(clazzElm)) {
                    return null;
                }
                clazzMir = clazzElm.asType();
            }
        }
        if (clazzMir == null) {
            return null;
        }
        
        Scope clazzScope = trees.getScope(clazzPath);
        
        // choose the first acceptable member
        for (Element member : selectElm.getEnclosedElements()) {
            if (member.getModifiers().contains(Modifier.STATIC)
                    && mName.contentEquals(member.getSimpleName())
                    && javac.getTreeUtilities().isAccessible(clazzScope, member, clazzMir)) {
                return member;
            }
        }
        return null;
    }
    
    private static boolean isCaretInsideDeclarationName(CompilationInfo info, Tree t, TreePath path, int caret) {
        try {
            switch (t.getKind()) {
                case CLASS:
                case METHOD:
                case VARIABLE:
                    int[] span = org.netbeans.modules.java.editor.semantic.Utilities.findIdentifierSpan(path, info, info.getDocument());

                    if (span == null || span[0] == (-1) || span[1] == (-1)) {
                        return false;
                    }

                    return span[0] <= caret && caret <= span[1];
                default:
                    return false;
            }

        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
            return false;
        }
    }
    
    private static final class FindVariableDeclarationVisitor extends TreePathScanner<Void, Element> {
        
        private CompilationInfo info;
        private Tree found;
        
        public @Override Void visitClass(ClassTree node, Element p) {
            //do not dive into the innerclasses:
            return null;
        }
        
        public @Override Void visitVariable(VariableTree node, Element p) {
            Element resolved = info.getTrees().getElement(getCurrentPath());
            
            if (resolved == p) {
                found = node;
            }
            
            return null;
        }
        
    }
    
    private static final class FindSuperConstructorCall extends TreePathScanner<TreePath, Void> {
        
        @Override
        public TreePath visitMethodInvocation(MethodInvocationTree tree, Void v) {
            if (tree.getMethodSelect().getKind() == Kind.IDENTIFIER && "super".equals(((IdentifierTree) tree.getMethodSelect()).getName().toString())) {
                return getCurrentPath();
            }
            
            return null;
        }
        
        @Override
        public TreePath reduce(TreePath first, TreePath second) {
            if (first == null) {
                return second;
            } else {
                return first;
            }
        }
        
    }
    
    private static final class DisplayNameElementVisitor extends AbstractElementVisitor6<Void, Boolean> {
        
        private StringBuffer result        = new StringBuffer();
        
        private void boldStartCheck(boolean highlightName) {
            if (highlightName) {
                result.append("<b>");
            }
        }
        
        private void boldStopCheck(boolean highlightName) {
            if (highlightName) {
                result.append("</b>");
            }
        }
        
        public Void visitPackage(PackageElement e, Boolean highlightName) {
            boldStartCheck(highlightName);
            
            result.append(e.getQualifiedName());
            
            boldStopCheck(highlightName);
            
            return null;
        }

        public Void visitType(TypeElement e, Boolean highlightName) {
            modifier(e.getModifiers());
            switch (e.getKind()) {
                case CLASS:
                    result.append("class ");
                    break;
                case INTERFACE:
                    result.append("interface ");
                    break;
                case ENUM:
                    result.append("enum ");
                    break;
                case ANNOTATION_TYPE:
                    result.append("@interface ");
                    break;
            }
            Element enclosing = e.getEnclosingElement();
            
            if (enclosing == SourceUtils.getEnclosingTypeElement(e)) {
                result.append(((TypeElement) enclosing).getQualifiedName());
                result.append('.');
                boldStartCheck(highlightName);
                result.append(e.getSimpleName());
                boldStopCheck(highlightName);
            } else {
                result.append(e.getQualifiedName());
            }
            
            return null;
        }

        public Void visitVariable(VariableElement e, Boolean highlightName) {
            modifier(e.getModifiers());
            
            result.append(getTypeName(e.asType(), true));
            
            result.append(' ');
            
            boldStartCheck(highlightName);

            result.append(e.getSimpleName());
            
            boldStopCheck(highlightName);
            
            if (highlightName) {
                if (e.getConstantValue() != null) {
                    result.append(" = ");
                    result.append(e.getConstantValue().toString());
                }
                
                Element enclosing = e.getEnclosingElement();
                
                if (e.getKind() != ElementKind.PARAMETER && e.getKind() != ElementKind.LOCAL_VARIABLE && e.getKind() != ElementKind.EXCEPTION_PARAMETER) {
                    result.append(" in ");

                    //short typename:
                    result.append(getTypeName(enclosing.asType(), true));
                }
            }
            
            return null;
        }

        public Void visitExecutable(ExecutableElement e, Boolean highlightName) {
            switch (e.getKind()) {
                case CONSTRUCTOR:
                    modifier(e.getModifiers());
                    dumpTypeArguments(e.getTypeParameters());
                    result.append(' ');
                    boldStartCheck(highlightName);
                    result.append(e.getEnclosingElement().getSimpleName());
                    boldStopCheck(highlightName);
                    dumpArguments(e.getParameters());
                    dumpThrows(e.getThrownTypes());
                    break;
                case METHOD:
                    modifier(e.getModifiers());
                    dumpTypeArguments(e.getTypeParameters());
                    result.append(getTypeName(e.getReturnType(), true));
                    result.append(' ');
                    boldStartCheck(highlightName);
                    result.append(e.getSimpleName());
                    boldStopCheck(highlightName);
                    dumpArguments(e.getParameters());
                    dumpThrows(e.getThrownTypes());
                    break;
                case INSTANCE_INIT:
                case STATIC_INIT:
                    //these two cannot be referenced anyway...
            }
            return null;
        }

        public Void visitTypeParameter(TypeParameterElement e, Boolean highlightName) {
            return null;
        }
        
        private void modifier(Set<Modifier> modifiers) {
            boolean addSpace = false;
            
            for (Modifier m : modifiers) {
                if (addSpace) {
                    result.append(' ');
                }
                addSpace = true;
                result.append(m.toString());
            }
            
            if (addSpace) {
                result.append(' ');
            }
        }
        
//        private void throwsDump()

        private void dumpTypeArguments(List<? extends TypeParameterElement> list) {
            if (list.isEmpty())
                return ;
            
            boolean addSpace = false;
            
            result.append("&lt;");
            
            for (TypeParameterElement e : list) {
                if (addSpace) {
                    result.append(", ");
                }
                
                result.append(getTypeName(e.asType(), true));
                
                addSpace = true;
            }
                
            result.append("&gt;");
        }

        private void dumpArguments(List<? extends VariableElement> list) {
            boolean addSpace = false;
            
            result.append('(');
            
            for (VariableElement e : list) {
                if (addSpace) {
                    result.append(", ");
                }
                
                visit(e, false);
                
                addSpace = true;
            }
                
            result.append(')');
        }

        private void dumpThrows(List<? extends TypeMirror> list) {
            if (list.isEmpty())
                return ;
            
            boolean addSpace = false;
            
            result.append(" throws ");
            
            for (TypeMirror t : list) {
                if (addSpace) {
                    result.append(", ");
                }
                
                result.append(getTypeName(t, true));
                
                addSpace = true;
            }
        }
            
    }
    
    private static String getTypeName(TypeMirror t, boolean fqn) {
        return translate(Utilities.getTypeName(t, fqn).toString());
    }
    
    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private static String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }
    
    static UiUtilsCaller CALLER = new UiUtilsCaller() {
        public boolean open(FileObject fo, int pos) {
            return UiUtils.open(fo, pos);
        }
        public void beep(boolean goToSource, boolean goToJavadoc) {
            Toolkit.getDefaultToolkit().beep();
            int value = goToSource ? 1 : goToJavadoc ? 2 : 0;
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToSupport.class, "WARN_CannotGoToGeneric", value));
        }
        public boolean open(ClasspathInfo info, ElementHandle<?> el) {
            return ElementOpen.open(info, el);
        }
        public void warnCannotOpen(String displayName) {
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToSupport.class, "WARN_CannotGoTo", displayName));
        }
    };
    
    interface UiUtilsCaller {
        public boolean open(FileObject fo, int pos);
        public void beep(boolean goToSource, boolean goToJavadoc);
        public boolean open(ClasspathInfo info, ElementHandle<?> el);
        public void warnCannotOpen(String displayName);
    }
}
