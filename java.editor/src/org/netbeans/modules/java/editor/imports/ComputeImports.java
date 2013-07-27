/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.Symbols;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.editor.java.Utilities;
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
    
    private static final Object IMPORT_CANDIDATES_KEY = new Object();
    
    public Pair<Map<String, List<Element>>, Map<String, List<Element>>> computeCandidates(CompilationInfo info) {
        Pair<Map<String, List<Element>>, Map<String, List<Element>>> result = (Pair<Map<String, List<Element>>, Map<String, List<Element>>>) info.getCachedValue(IMPORT_CANDIDATES_KEY);
        
        if (result != null) {
            return result;
        }
        
        result = computeCandidates(info, Collections.<String>emptySet());
        
        if (!isCancelled() && result != null) {
            info.putCachedValue(IMPORT_CANDIDATES_KEY, result, CacheClearPolicy.ON_CHANGE);
        }
        
        return result;
    }
    
    private TreeVisitorImpl visitor;
    
    private synchronized void setVisitor(TreeVisitorImpl visitor) {
        this.visitor = visitor;
    }
    
    Pair<Map<String, List<Element>>, Map<String, List<Element>>> computeCandidates(CompilationInfo info, Set<String> forcedUnresolved) {
        Map<String, List<Element>> candidates = new HashMap<String, List<Element>>();
        Map<String, List<Element>> notFilteredCandidates = new HashMap<String, List<Element>>();
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
            
            List<Element> classes = new ArrayList<Element>();
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
                if (info.getElements().getPackageOf(te).getQualifiedName().length() != 0 &&
                        !Utilities.isExcluded(te.getQualifiedName())) {
                    classes.add(te);
                }
            }
            
            Iterable<Symbols> simpleNames = info.getClasspathInfo().getClassIndex().getDeclaredSymbols(unresolved, NameKind.SIMPLE_NAME,EnumSet.allOf(ClassIndex.SearchScope.class));

            if (simpleNames == null) {
                //Canceled:
                return null;
            }
            
            for (final Symbols p : simpleNames) {
                final TypeElement te = p.getEnclosingType().resolve(info);
                final Set<String> idents = p.getSymbols();
                if (te != null) {
                    for (Element ne : te.getEnclosedElements()) {
                        if (!ne.getModifiers().contains(Modifier.STATIC)) continue;
                        if (idents.contains(getSimpleName(ne, te))) {
                            classes.add(ne);
                        }
                    }
                }
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
            
        return new Pair<Map<String, List<Element>>, Map<String, List<Element>>>(candidates, notFilteredCandidates);
    }
    
    public static String displayNameForImport(@NonNull CompilationInfo info, @NonNull Element element) {
        if (element.getKind().isClass() || element.getKind().isInterface()) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        
        StringBuilder fqnSB = new StringBuilder();

        fqnSB.append(((TypeElement) element.getEnclosingElement()).getQualifiedName());
        fqnSB.append('.');
        fqnSB.append(element.getSimpleName());

        if (element.getKind() == ElementKind.METHOD) {
            fqnSB.append('(');
            boolean first = true;
            for (VariableElement var : ((ExecutableElement) element).getParameters()) {
                if (!first) {
                    fqnSB.append(", ");
                }
                fqnSB.append(info.getTypeUtilities().getTypeName(info.getTypes().erasure(var.asType())));
                first = false;
            }
            fqnSB.append(')');
        }
        
        return fqnSB.toString();
    }

    private static final String INIT = "<init>"; //NOI18N
    private String getSimpleName (
            @NonNull final Element element,
            @NullAllowed final Element enclosingElement) {
        String result = element.getSimpleName().toString();
        if (enclosingElement != null && INIT.equals(result)) {
            result = enclosingElement.getSimpleName().toString();
        }
        return result;
    }
    
    private static boolean filter(Types types, List<Element> left, List<Element> right, boolean leftReadOnly, boolean rightReadOnly) {
        boolean changed = false;
        Map<TypeElement, List<TypeElement>> validPairs = new HashMap<TypeElement, List<TypeElement>>();
        
        for (TypeElement l : ElementFilter.typesIn(left)) {
            List<TypeElement> valid = new ArrayList<TypeElement>();
            
            for (TypeElement r : ElementFilter.typesIn(right)) {
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
        
        private final CompilationInfo info;
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
            
            if (tree.getType() != null && tree.getType().getKind() == Kind.IDENTIFIER) {
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
            
            boolean methodInvocation = getCurrentPath().getParentPath() != null && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION;
            
            if (methodInvocation) {
                MethodInvocationTree mit = (MethodInvocationTree) getCurrentPath().getParentPath().getLeaf();
                
                if (mit.getMethodSelect() == tree) {
                    List<TypeMirror> params = new ArrayList<TypeMirror>();
                    for (ExpressionTree realParam : mit.getArguments()) {
                        params.add(info.getTrees().getTypeMirror(new TreePath(getCurrentPath().getParentPath(), realParam)));
                    }
                    this.hints.add(new MethodParamsHint(tree.getName().toString(), params));
                }
            }
            
//            System.err.println("tree=" + tree);
            final Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null && (el.getKind().isClass() || el.getKind().isInterface() || el.getKind() == ElementKind.PACKAGE)) {
                TypeMirror type = el.asType();
                String simpleName = null;
                
                if (type != null) {
                    if (type.getKind() == TypeKind.ERROR) {
                        boolean allowImport = true;

                        if (getCurrentPath().getParentPath() != null && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ASSIGNMENT) {
                            AssignmentTree at = (AssignmentTree) getCurrentPath().getParentPath().getLeaf();

                            allowImport = at.getVariable() != tree;
                        }
                        
                        if (methodInvocation) {
                            Scope s = info.getTrees().getScope(getCurrentPath());

                            while (s != null) {
                                allowImport &= !info.getElementUtilities().getLocalMembersAndVars(s, new ElementAcceptor() {
                                    @Override public boolean accept(Element e, TypeMirror type) {
                                        return e.getSimpleName().contentEquals(el.getSimpleName());
                                    }
                                }).iterator().hasNext();
                                s = s.getEnclosingScope();
                            }
                        }

                        if (allowImport) {
                            simpleName = el.getSimpleName().toString();
                        }
                    }

                    if (type != null && type.getKind() == TypeKind.PACKAGE) {
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
            }
            
            p.remove("request");
            
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree node, Map<String, Object> p) {
            filterByNotAcceptedKind(node.getIdentifier(), ElementKind.ENUM);
            scan(node.getEnclosingExpression(), new HashMap<String, Object>());
            scan(node.getIdentifier(), p);
            scan(node.getTypeArguments(), new HashMap<String, Object>());
            scan(node.getArguments(), new HashMap<String, Object>());
            scan(node.getClassBody(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Map<String, Object> p) {
            scan(node.getTypeArguments(), new HashMap<String, Object>());
            scan(node.getMethodSelect(), p);
            scan(node.getArguments(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitNewArray(NewArrayTree node, Map<String, Object> p) {
            scan(node.getType(), p);
            scan(node.getDimensions(), new HashMap<String, Object>());
            scan(node.getInitializers(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, Map<String, Object> p) {
            scan(node.getType(), p);
            scan(node.getTypeArguments(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Map<String, Object> p) {
            if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
                filterByAcceptedKind(node.getExtendsClause(), ElementKind.CLASS);
                for (Tree intf : node.getImplementsClause()) {
                    filterByAcceptedKind(intf, ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE);
                }
            }
            return super.visitClass(node, p);
        }

        @Override
        public Void visitAnnotation(AnnotationTree node, Map<String, Object> p) {
            filterByAcceptedKind(node.getAnnotationType(), ElementKind.ANNOTATION_TYPE);
            return super.visitAnnotation(node, p);
        }
        
        private Scope topLevelScope;
        
        private Scope getScope() {
            if (topLevelScope == null) {
                topLevelScope = info.getTrees().getScope(new TreePath(getCurrentPath().getCompilationUnit()));
            }
            return topLevelScope;
        }
        
        private void filterByAcceptedKind(Tree toFilter, ElementKind acceptedKind, ElementKind... otherAcceptedKinds) {
            filterByKind(toFilter, EnumSet.of(acceptedKind, otherAcceptedKinds), EnumSet.noneOf(ElementKind.class));
        }
        
        private void filterByNotAcceptedKind(Tree toFilter, ElementKind notAcceptedKind, ElementKind... otherNotAcceptedKinds) {
            filterByKind(toFilter, EnumSet.noneOf(ElementKind.class), EnumSet.of(notAcceptedKind, otherNotAcceptedKinds));
        }
        
        private void filterByKind(Tree toFilter, Set<ElementKind> acceptedKinds, Set<ElementKind> notAcceptedKinds) {
            if (toFilter == null) return;
            switch (toFilter.getKind()) {
                case IDENTIFIER:
                    hints.add(new KindHint(((IdentifierTree) toFilter).getName().toString(), acceptedKinds, notAcceptedKinds));
                    break;
                case PARAMETERIZED_TYPE:
                    filterByKind(((ParameterizedTypeTree) toFilter).getType(), acceptedKinds, notAcceptedKinds);
                    break;
            }
        }
    }
    
    public static interface Hint {
        
        public abstract boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates);
        
    }
    
    public static final class TypeHint implements Hint {
        
        private Union2<String, DeclaredType> left;
        private Union2<String, DeclaredType> right;
        
        public TypeHint(Union2<String, DeclaredType> left, Union2<String, DeclaredType> right) {
            this.left = left;
            this.right = right;
        }
        
        public boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates) {
            List<Element> left = null;
            List<Element> right = null;
            boolean leftReadOnly = false;
            boolean rightReadOnly = false;
            
            if (this.left.hasSecond()) {
                Element el = this.left.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    left = Collections.singletonList(el);
                    leftReadOnly = true;
                }
            } else {
                left = candidates.get(this.left.first());
            }
            
            if (this.right.hasSecond()) {
                Element el = this.right.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    right = Collections.singletonList(el);
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
        
        public boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates) {
            List<Element> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : ElementFilter.typesIn(cands)) {
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
    
    public static final class KindHint implements Hint {
        
        private String simpleName;
        private Set<ElementKind> acceptedKinds;
        private Set<ElementKind> notAcceptedKinds;

        public KindHint(String simpleName, Set<ElementKind> acceptedKinds, Set<ElementKind> notAcceptedKinds) {
            this.simpleName = simpleName;
            this.acceptedKinds = acceptedKinds;
            this.notAcceptedKinds = notAcceptedKinds;
        }
        
        public boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates) {
            List<Element> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : ElementFilter.typesIn(cands)) {
                if (!acceptedKinds.isEmpty() && !acceptedKinds.contains(te.getKind())) {
                    toRemove.add(te);
                    continue;
                }
                if (!notAcceptedKinds.isEmpty() && notAcceptedKinds.contains(te.getKind())) {
                    toRemove.add(te);
                    continue;
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
        
        public boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates) {
            List<Element> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<Element> toRemove = new ArrayList<Element>();
            
            for (Element te : cands) {
                if (te.getKind().isClass() || te.getKind().isInterface() ? !info.getTrees().isAccessible(scope, (TypeElement) te)
                                                                         : !info.getTrees().isAccessible(scope, te, (DeclaredType) te.getEnclosingElement().asType())) {
                    toRemove.add(te);
                }
            }
            
            //remove it from the raw candidates too:
            rawCandidates.get(simpleName).removeAll(toRemove);
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static final class MethodParamsHint implements Hint {
        
        private final String simpleName;
        private final List<TypeMirror> paramTypes;

        public MethodParamsHint(String simpleName, List<TypeMirror> paramTypes) {
            this.simpleName = simpleName;
            this.paramTypes = paramTypes;
        }

        public boolean filter(CompilationInfo info, Map<String, List<Element>> rawCandidates, Map<String, List<Element>> candidates) {
            List<Element> rawCands = rawCandidates.get(simpleName);
            List<Element> cands = candidates.get(simpleName);
            
            if (rawCands == null || cands == null) {
                return false;
            }

            boolean modified = false;
            
            for (Element c : new ArrayList<Element>(rawCands)) {
                if (c.getKind() != ElementKind.METHOD) {
                    rawCands.remove(c);
                    cands.remove(c);
                    modified |= true;
                } else {
                    //XXX: varargs
                    Iterator<? extends TypeMirror> real = paramTypes.iterator();
                    Iterator<? extends TypeMirror> formal = ((ExecutableType) c.asType()).getParameterTypes().iterator();
                    boolean matches = true;
                    boolean inVarArgs = false;
                    TypeMirror currentFormal = null;
                    
                    while (real.hasNext() && (formal.hasNext() || inVarArgs)) {
                        TypeMirror currentReal = real.next();
                        
                        if (!inVarArgs)
                            currentFormal = formal.next();
                        
                        if (!info.getTypes().isAssignable(info.getTypes().erasure(currentReal), info.getTypes().erasure(currentFormal))) {
                            if (((ExecutableElement) c).isVarArgs() && !formal.hasNext()) {
                                currentFormal = ((ArrayType) currentFormal).getComponentType();
                                
                                if (!info.getTypes().isAssignable(info.getTypes().erasure(currentReal), info.getTypes().erasure(currentFormal))) {
                                    matches = false;
                                    break;
                                }
                                
                                inVarArgs = true;
                            } else {
                                matches = false;
                                break;
                            }
                        }
                    }
                    
                    matches &= real.hasNext() == formal.hasNext();

                    if (!matches) {
                        rawCands.remove(c);
                        cands.remove(c);
                        modified |= true;
                    }
                }
            }
            
            return modified;
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
