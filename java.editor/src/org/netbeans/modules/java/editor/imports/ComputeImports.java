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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;
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
        
        unresolvedNames.addAll(JavadocImports.computeUnresolvedImports(info));
        
        for (String unresolved : unresolvedNames) {
            if (isCancelled())
                return null;
            
            List<TypeElement> classes = new ArrayList<TypeElement>();
            Set<ElementHandle<TypeElement>> typeNames = info.getClasspathInfo().getClassIndex().getDeclaredTypes(unresolved, NameKind.SIMPLE_NAME,EnumSet.allOf(ClassIndex.SearchScope.class));
            if (typeNames == null) {
                //Canceled
                return null;
            }
            for (ElementHandle<TypeElement> typeName : typeNames) {
                TypeElement te = info.getElements().getTypeElement(typeName.getQualifiedName());
                
                if (te == null) {
                    Logger.getLogger(ComputeImports.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
                    continue;
                }
                
                //#122334: do not propose imports from the default package:
                if (info.getElements().getPackageOf(te).getQualifiedName().length() != 0) {
                    classes.add(te);
                }
            }
            Collections.sort(classes, new Comparator<TypeElement>() {
                public int compare(TypeElement te1, TypeElement te2) {
                    return (te1 == te2) ? 0 : te1.getQualifiedName().toString().compareTo(te2.getQualifiedName().toString());
                }
            });
            
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
            
            if (getCurrentPath().getParentPath() != null && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                MethodInvocationTree mit = (MethodInvocationTree) getCurrentPath().getParentPath().getLeaf();
                
                if (mit.getMethodSelect() == tree) {
                    return null;
                }
            }
            
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
                
                if (simpleName == null || !SourceVersion.isIdentifier(simpleName) || SourceVersion.isKeyword(simpleName)) {
                    simpleName = null;
                }
                    
                if (simpleName != null) {
                    unresolved.add(simpleName);
                    
                    Scope currentScope = getScope();
                    
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
        
        private static final Set<Kind> SAFE_KIND_FOR_SCOPE = EnumSet.of(Kind.COMPILATION_UNIT, Kind.CLASS);
        
        //resolving scope for each unresolved identifier is very slow, and not really necessary for Trees.isAccessible -
        //scope for the nearest class should be OK
        private Scope getScope() {
            TreePath tp = getCurrentPath();
            Kind kind = tp.getLeaf().getKind();
            
            while (!SAFE_KIND_FOR_SCOPE.contains(kind)) {
                tp = tp.getParentPath();
                kind = tp.getLeaf().getKind();
            }
            
            return info.getTrees().getScope(tp);
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
            return false;
        }
        
        //IZ 102613 -- bugous 'discouraged' hints
        public boolean Xfilter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates) {
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
            return false;
        }
        
        //IZ 102613 -- bugous 'discouraged' hints
        private boolean Xfilter(CompilationInfo info, Map<String, List<TypeElement>> rawCandidates, Map<String, List<TypeElement>> candidates) {
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
