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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

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
        try {
            final FileObject fo = getFileObject(doc);
            
            if (fo == null)
                return null;
            
            JavaSource js = JavaSource.forFileObject(fo);
            final String[] result = new String[1];
            
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                        return;
                    
                    Token<JavaTokenId>[] token = new Token[1];
                    int[] span = getIdentifierSpan(doc, offset, token);
                    
                    if (span == null) {
                        Toolkit.getDefaultToolkit().beep();
                        return ;
                    }
                    
                    int exactOffset = controller.getPositionConverter().getJavaSourcePosition(span[0] + 1);
                    
                    TreePath path = controller.getTreeUtilities().pathFor(exactOffset);
                    TreePath parent = path.getParentPath();
                    
                    if (parent != null) {
                        Tree parentLeaf = parent.getLeaf();

                        if (parentLeaf.getKind() == Kind.NEW_CLASS && ((NewClassTree) parentLeaf).getIdentifier() == path.getLeaf()) {
                            if (!isError(controller.getTrees().getElement(path.getParentPath()))) {
                                path = path.getParentPath();
                            }
                        } else {
                            if (   parentLeaf.getKind() == Kind.PARAMETERIZED_TYPE
                                && parent.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS
                                && ((ParameterizedTypeTree) parentLeaf).getType() == path.getLeaf()) {
                                if (!isError(controller.getTrees().getElement(parent.getParentPath()))) {
                                    path = parent.getParentPath();
                                }
                            }
                        }
                    } else {
                        if (!tooltip)
                            CALLER.beep();
                        else
                            result[0] = null;
                        return;
                    }
                    
                    Element el = controller.getTrees().getElement(path);
                    
                    if (isError(el)) {
                        if (!tooltip)
                            CALLER.beep();
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (goToSource) {
                        TypeMirror type = null;
                        
                        if (el instanceof VariableElement)
                            type = el.asType();
                        
                        if (type != null && type.getKind() == TypeKind.DECLARED) {
                            el = ((DeclaredType)type).asElement();
                        }
                    }
                    
                    if (isError(el)) {
                        if (!tooltip)
                            CALLER.beep();
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
                            CALLER.beep();
                        else
                            result[0] = null;
                        return;
                    }
                    
                    if (el.getKind() != ElementKind.CONSTRUCTOR && (token[0].id() == JavaTokenId.SUPER || token[0].id() == JavaTokenId.THIS)) {
                        if (!tooltip)
                            CALLER.beep();
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
                            CALLER.beep ();
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
                            long endPos   = controller.getTrees().getSourcePositions().getEndPosition(controller.getCompilationUnit(), tree);
                            
                            if (startPos != (-1)) {
                                //check if the caret is inside the declaration itself, as jump in this case is not very usefull:
                                if (startPos <= offset && offset <= endPos) {
                                    CALLER.beep();
                                } else {
                                    //#71272: it is necessary to translate the offset:
                                    int targetOffset = controller.getPositionConverter().getOriginalPosition((int) startPos);
                                    
                                    if (targetOffset >= 0) {
                                        CALLER.open(fo, targetOffset);
                                    } else {
                                        CALLER.beep();
                                    }
                                }
                            } else {
                                CALLER.beep();
                            }
                        } else {
                            CALLER.open(controller.getClasspathInfo(), el);
                        }
                    }
                }
            },true);
            
            return result[0];
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
    
    public static void goTo(Document doc, int offset, boolean goToSource) {
        performGoTo(doc, offset, goToSource, false, false);
    }
    
    public static void goToJavadoc(Document doc, int offset) {
        performGoTo(doc, offset, false, false, true);
    }
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = new HashSet<JavaTokenId>(Arrays.asList(JavaTokenId.IDENTIFIER, JavaTokenId.THIS, JavaTokenId.SUPER));
    
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
        
        if (!USABLE_TOKEN_IDS.contains(t.id())) {
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
                result.append(e.getQualifiedName());
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
            
            result.append(Utilities.getTypeName(e.asType(), true));
            
            result.append(' ');
            
            boldStartCheck(highlightName);

            result.append(e.getSimpleName());
            
            boldStopCheck(highlightName);
            
            if (highlightName) {
                if (e.getConstantValue() != null) {
                    result.append(" = ");
                    result.append(e.getConstantValue().toString());
                }
                
                result.append(" in ");
                
                Element enclosing = e.getEnclosingElement();
                
                if (!(enclosing.getKind() == ElementKind.PARAMETER || enclosing.getKind() == ElementKind.LOCAL_VARIABLE)) {
                    //short typename:
                    result.append(Utilities.getTypeName(enclosing.asType(), true));
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
                    result.append(e.getSimpleName());
                    boldStopCheck(highlightName);
                    dumpArguments(e.getParameters());
                    dumpThrows(e.getThrownTypes());
                    break;
                case METHOD:
                    modifier(e.getModifiers());
                    dumpTypeArguments(e.getTypeParameters());
                    result.append(Utilities.getTypeName(e.getReturnType(), true));
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
            
            result.append('<');
            
            for (TypeParameterElement e : list) {
                if (addSpace) {
                    result.append(", ");
                }
                
                result.append(Utilities.getTypeName(e.asType(), true));
                
                addSpace = true;
            }
                
            result.append('>');
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
                
                result.append(Utilities.getTypeName(t, true));
                
                addSpace = true;
            }
        }
            
    }
    
    static UiUtilsCaller CALLER = new UiUtilsCaller() {
        public void open(FileObject fo, int pos) {
            UiUtils.open(fo, pos);
        }
        public void beep() {
            Toolkit.getDefaultToolkit().beep();
        }
        public void open(ClasspathInfo info, Element el) {
            ElementOpen.open(info, el);
        }
    };
    
    interface UiUtilsCaller {
        public void open(FileObject fo, int pos);
        public void beep();
        public void open(ClasspathInfo info, Element el);
    }
}
