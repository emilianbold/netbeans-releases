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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeImports {
    
    private static final String ERROR = "<error>";
    
    /** Creates a new instance of JavaFixAllImports */
    public ComputeImports() {
    }
    
    private boolean cancelled;
    
    public synchronized void cancel() {
        cancelled = true;
        
        if (visitor != null)
            visitor.cancel();
    }
    
    private synchronized boolean isCancelled() {
        return cancelled;
    }
    
    public Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> computeCandidates(CompilationInfo info) {
        return computeCandidates(info, Collections.<String>emptySet());
    }
    
    private TreeVisitorImpl visitor;
    
    private synchronized void setVisitor(TreeVisitorImpl visitor) {
        this.visitor = visitor;
    }
    
    Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> computeCandidates(CompilationInfo info, Set<String> forcedUnresolved) {
        Map<String, List<TypeElement>> candidates = new HashMap<String, List<TypeElement>>();
        Map<String, List<TypeElement>> notFilteredCandidates = new HashMap<String, List<TypeElement>>();
        TreeVisitorImpl v = new TreeVisitorImpl(info);

        setVisitor(v);
        
        v.scan(info.getCompilationUnit(), new HashMap<String, Object>());
        
        setVisitor(null);
        
        Set<String> unresolvedNames = new HashSet<String>(v.unresolved);
        
        unresolvedNames.addAll(forcedUnresolved);
        
        for (String unresolved : unresolvedNames) {
            if (isCancelled())
                return new Pair(Collections.emptyMap(), Collections.emptyMap());
            
            List<TypeElement> classes = new ArrayList<TypeElement>();
            
            for (ElementHandle<TypeElement> typeNames : info.getJavaSource().getClasspathInfo().getClassIndex().getDeclaredTypes(unresolved, NameKind.SIMPLE_NAME,EnumSet.allOf(ClassIndex.SearchScope.class))) {
                TypeElement te = info.getElements().getTypeElement(typeNames.getQualifiedName());
                
                if (te == null) {
                    Logger.getLogger(ComputeImports.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeNames + "\".");
                    continue;
                }
                
                classes.add(te);
            }
            
            candidates.put(unresolved, new ArrayList(classes));
            notFilteredCandidates.put(unresolved, classes);
        }
        
        boolean wasChanged = true;
        
        while (wasChanged) {
            if (isCancelled())
                return new Pair(Collections.emptyMap(), Collections.emptyMap());
            
            wasChanged = false;
            
            for (Hint hint: v.hints) {
                wasChanged |= hint.filter(info, notFilteredCandidates, candidates);
            }
        }
            
        return new Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>>(candidates, notFilteredCandidates);
    }
    
    private static boolean filter(Types types, List<TypeElement> left, List<TypeElement> right, boolean leftReadOnly, boolean rightReadOnly) {
        boolean changed = false;
        Map<TypeElement, List<TypeElement>> validPairs = new HashMap<TypeElement, List<TypeElement>>();
        
        for (TypeElement l : left) {
            List<TypeElement> valid = new ArrayList<TypeElement>();
            
            for (TypeElement r : right) {
                TypeMirror t1 = types.erasure(l.asType());
                TypeMirror t2 = types.erasure(r.asType());
                
//                System.err.println("t2 = " + t2);
//                System.err.println("t1 = " + t1);
//                System.err.println("types= " + types.getClass());
//                System.err.println("types.isAssignable(t2, t1) = " + types.isAssignable(t2, t1));
//                System.err.println("types.isSubtype(t2, t1) = " + types.isSubtype(t2, t1));
//                System.err.println("types.isAssignable(t1,t2) = " + types.isAssignable(t1,t2));
//                System.err.println("types.isSubtype(t1, t2) = " + types.isSubtype(t1, t2));
                if (types.isAssignable(t2, t1))
                    valid.add(r);
            }
            
//            System.err.println("l = " + l );
//            System.err.println("valid = " + valid );
            validPairs.put(l, valid);
        }
        
        Set<TypeElement> validRights = new HashSet<TypeElement>();
        
        for (TypeElement l : validPairs.keySet()) {
            List<TypeElement> valid = validPairs.get(l);
            
            if (valid.isEmpty() && !leftReadOnly) {
                //invalid left:
                left.remove(l);
                changed = true;
            }
            
            validRights.addAll(valid);
        }
        
        if (!rightReadOnly)
            changed = right.retainAll(validRights) | changed;
        
        return changed;
    }
    
    private static EnumSet<TypeKind> INVALID_TYPES = EnumSet.of(TypeKind.NULL, TypeKind.NONE, TypeKind.OTHER, TypeKind.ERROR);
    
    private static class TreeVisitorImpl extends CancellableTreePathScanner<Void, Map<String, Object>> {
        
        private CompilationInfo info;
        private Set<String> unresolved;
        
        private List<Hint> hints;
        
        public TreeVisitorImpl(CompilationInfo info) {
            this.info = info;
            unresolved = new HashSet<String>();
            hints = new ArrayList<Hint>();
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, Map<String, Object> p) {
            if (tree.getExpression().getKind() == Kind.IDENTIFIER) {
                p.put("request", null);
            }
            
            scan(tree.getExpression(), p);
            
            Union2<String, DeclaredType> leftSide = (Union2<String, DeclaredType>) p.remove("result");
            
            p.remove("request");
            
            if (leftSide != null && leftSide.hasFirst()) {
                String rightSide = tree.getIdentifier().toString();
                
                if (ERROR.equals(rightSide))
                    rightSide = "";
                
                boolean isMethodInvocation = getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION;
                
                //Ignore .class (which will not help us much):
                if (!"class".equals(rightSide))
                    hints.add(new EnclosedHint(leftSide.first(), rightSide, !isMethodInvocation));
            }
            
            return null;
        }
        
        @Override
        public Void visitVariable(VariableTree tree, Map<String, Object> p) {
            scan(tree.getModifiers(), p);
            
            if (tree.getType().getKind() == Kind.IDENTIFIER) {
                p.put("request", null);
            }
            
            scan(tree.getType(), p);
            
            Union2<String, DeclaredType> leftSide = (Union2<String, DeclaredType>) p.remove("result");
            
            p.remove("request");
            
            Union2<String, DeclaredType> rightSide = null;
            
            if (leftSide != null && tree.getInitializer() != null) {
                Element el = info.getTrees().getElement(new TreePath(getCurrentPath(),tree.getInitializer()));
                TypeMirror rightType = el != null ? el.asType() : null;
                
//                System.err.println("rightType = " + rightType );
//                System.err.println("tree.getInitializer()=" + tree.getInitializer());
//                System.err.println("rightType.getKind()=" + rightType.getKind());
//                System.err.println("INVALID_TYPES.contains(rightType.getKind())=" + INVALID_TYPES.contains(rightType.getKind()));
                if (rightType != null && rightType.getKind() == TypeKind.DECLARED) {
                    rightSide = Union2.<String, DeclaredType>createSecond((DeclaredType) rightType);
                } else {
                    if (tree.getInitializer().getKind() == Kind.NEW_CLASS || tree.getInitializer().getKind() == Kind.NEW_ARRAY) {
                        p.put("request", null);
                    }
                }
            }
            
            scan(tree.getInitializer(), p);
            
            rightSide = rightSide == null ? (Union2<String, DeclaredType>) p.remove("result") : rightSide;
            
            p.remove("result");
            
//            System.err.println("rightSide = " + rightSide );
            
            p.remove("request");
            
            if (leftSide != null && rightSide != null) {
                if (!(leftSide instanceof TypeMirror) || !(rightSide instanceof TypeMirror)) {
                    hints.add(new TypeHint(leftSide, rightSide));
                }
            }
            
            return null;
        }
        
        @Override
        public Void visitIdentifier(IdentifierTree tree, Map<String, Object> p) {
            super.visitIdentifier(tree, p);
            
//            System.err.println("tree=" + tree);
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null && (el.getKind().isClass() || el.getKind().isInterface() || el.getKind() == ElementKind.PACKAGE)) {
                TypeMirror type = el.asType();
                String simpleName = null;
                
                if (type.getKind() == TypeKind.ERROR) {
                    simpleName = el.getSimpleName().toString();
                }
                
                if (type.getKind() == TypeKind.PACKAGE) {
                    //does the package really exists?
                    String s = ((PackageElement) el).getQualifiedName().toString();
                    if (info.getElements().getPackageElement(s) == null) {
                        //probably situation like:
                        //Map.Entry e;
                        //where Map is not imported
                        simpleName = el.getSimpleName().toString();
                    }
                }
                
                if (ERROR.equals(simpleName)) {
                    simpleName = null;
                }
                
                if (simpleName != null) {
                    unresolved.add(simpleName);
                    
                    Scope currentScope = info.getTrees().getScope(getCurrentPath());
                    
                    hints.add(new AccessibleHint(simpleName, currentScope));
                    
                    if (p.containsKey("request")) {
                        p.put("result", Union2.<String, DeclaredType>createFirst(simpleName));
                    }
                } else {
                    if (p.containsKey("request") && type.getKind() == TypeKind.DECLARED) {
                        p.put("result", Union2.<String, DeclaredType>createSecond((DeclaredType) type));
                    }
                }
            }
            
            p.remove("request");
            
            return null;
        }
        
    }
    
    public static interface Hint {
        
        public abstract boolean filter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates);
        
    }
    
    public static final class TypeHint implements Hint {
        
        private Union2<String, DeclaredType> left;
        private Union2<String, DeclaredType> right;
        
        public TypeHint(Union2<String, DeclaredType> left, Union2<String, DeclaredType> right) {
            this.left = left;
            this.right = right;
        }
        
        public boolean filter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates) {
            List<TypeElement> left = null;
            List<TypeElement> right = null;
            boolean leftReadOnly = false;
            boolean rightReadOnly = false;
            
            if (this.left.hasSecond()) {
                Element el = this.left.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    left = Collections.singletonList((TypeElement) el);
                    leftReadOnly = true;
                }
            } else {
                left = candidates.get(this.left.first());
            }
            
            if (this.right.hasSecond()) {
                Element el = this.right.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    right = Collections.singletonList((TypeElement) el);
                    rightReadOnly = true;
                }
            } else {
                right = candidates.get(this.right.first());
            }
            
            if (left != null && right != null && !left.isEmpty() && !right.isEmpty()) {
                return ComputeImports.filter(info.getTypes(), left, right, leftReadOnly, rightReadOnly);
            }
            
            return false;
        }
        
    }
    
    public static final class EnclosedHint implements Hint {
        
        private String simpleName;
        private String methodName;
        private boolean allowPrefix;
        
        public EnclosedHint(String simpleName, String methodName, boolean allowPrefix) {
            this.simpleName = simpleName;
            this.methodName = methodName;
            this.allowPrefix = allowPrefix;
        }
        
        public boolean filter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates) {
            List<TypeElement> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : cands) {
                boolean found = false;
                
                for (Element e : te.getEnclosedElements()) {
                    String simpleName = e.getSimpleName().toString();
                    
                    if (methodName.contentEquals(simpleName)) {
                        found = true;
                        break;
                    }
                    
                    if (allowPrefix && simpleName.startsWith(methodName)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    toRemove.add(te);
                }
            }
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static final class AccessibleHint implements Hint {
        
        private String simpleName;
        private Scope scope;
        
        public AccessibleHint(String simpleName, Scope scope) {
            this.simpleName = simpleName;
            this.scope = scope;
        }
        
        public boolean filter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates) {
            List<TypeElement> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : cands) {
                if (!info.getTrees().isAccessible(scope, te)) {
                    toRemove.add(te);
                }
            }
            
            //remove it from the raw candidates too:
            rawCandidates.get(simpleName).removeAll(toRemove);
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static class Pair<A, B> {
        
        public A a;
        public B b;
        
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
    
}
