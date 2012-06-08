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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;

/**
 *
 * @author lahvac
 */
public class UnusedImports {
    private static final Object KEY_CACHE = new Object();
    
    public static Collection<TreePath> process(CompilationInfo info, AtomicBoolean cancel) {
        Collection<TreePath> result = (Collection<TreePath>) info.getCachedValue(KEY_CACHE);
        
        if (result != null) return result;
        
        DetectorVisitor v = new DetectorVisitor(info, cancel);
        
        CompilationUnitTree cu = info.getCompilationUnit();
        
        v.scan(cu, null);
        
        if (cancel.get())
            return null;
        
        List<TreePath> allUnusedImports = new ArrayList<TreePath>();

        for (TreePath tree : v.import2Highlight.values()) {
            if (cancel.get()) {
                return null;
            }

            allUnusedImports.add(tree);
        }
        
        allUnusedImports = Collections.unmodifiableList(allUnusedImports);
        
        info.putCachedValue(KEY_CACHE, allUnusedImports, CacheClearPolicy.ON_CHANGE);
        
        return allUnusedImports;
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, Void> {
        
        private final CompilationInfo info;
        
        private final Map<Element, ImportTree> element2Import = new HashMap<Element, ImportTree>();
        private final Set<Element> importedBySingleImport = new HashSet<Element>();
        private final Map<String, ImportTree> method2Import = new HashMap<String, ImportTree>();
        private final Map<String, Collection<ImportTree>> simpleName2UnresolvableImports = new HashMap<String, Collection<ImportTree>>();
        private final Set<ImportTree> unresolvablePackageImports = new HashSet<ImportTree>();
        private final Map<ImportTree, TreePath/*ImportTree*/> import2Highlight = new HashMap<ImportTree, TreePath>();
        
        private DetectorVisitor(CompilationInfo info, AtomicBoolean cancel) {
            super(cancel);
            
            this.info = info;
        }
        
        private void handleJavadoc(TreePath classMember) {
            if (classMember == null) {
                return;
            }
            for (Element el : JavadocImports.computeReferencedElements(info, classMember)) {
                typeUsed(el, null, false);
            }
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            handleJavadoc(getCurrentPath());
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            handleJavadoc(getCurrentPath());
            return super.visitMethod(node, p);
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e != null && e.getKind().isField()) {
                handleJavadoc(getCurrentPath());
            }
            
            return super.visitVariable(node, p);
        }
        
        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, Void d) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
	    scan(tree.getImports(), d);
	    scan(tree.getPackageAnnotations(), d);
	    scan(tree.getTypeDecls(), d);
	    return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void d) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;
            
            typeUsed(info.getTrees().getElement(getCurrentPath()), getCurrentPath(), getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION);
            
            return super.visitIdentifier(tree, null);
        }
        
        private boolean isStar(ImportTree tree) {
            Tree qualIdent = tree.getQualifiedIdentifier();
            
            if (qualIdent == null || qualIdent.getKind() == Kind.IDENTIFIER) {
                return false;
            }
            
            return ((MemberSelectTree) qualIdent).getIdentifier().contentEquals("*");
        }

        private boolean parseErrorInImport(ImportTree imp) {
            if (isStar(imp)) return false;
            final StringBuilder fqn = new StringBuilder();
            new TreeScanner<Void, Void>() {
                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void p) {
                    super.visitMemberSelect(node, p);
                    fqn.append('.');
                    fqn.append(node.getIdentifier());
                    return null;
                }
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    fqn.append(node.getName());
                    return null;
                }
            }.scan(imp.getQualifiedIdentifier(), null);

            return !SourceVersion.isName(fqn);
        }
        
        @Override
        public Void visitImport(ImportTree tree, Void d) {
            if (parseErrorInImport(tree)) {
                return super.visitImport(tree, null);
            }
            if (!tree.isStatic()) {
                if (isStar(tree)) {
                    MemberSelectTree qualIdent = (MemberSelectTree) tree.getQualifiedIdentifier();
                    Element decl = info.getTrees().getElement(new TreePath(new TreePath(getCurrentPath(), qualIdent), qualIdent.getExpression()));
                    
                    if (decl != null && decl.getKind() == ElementKind.PACKAGE) {
                        if (!ElementFilter.typesIn(decl.getEnclosedElements()).isEmpty()) {
                            for (TypeElement te : ElementFilter.typesIn(decl.getEnclosedElements())) {
                                if (!importedBySingleImport.contains(te)) {
                                    element2Import.put(te, tree);
                                }
                            }
                            import2Highlight.put(tree, getCurrentPath());
                        } else {
                            unresolvablePackageImports.add(tree);
                            import2Highlight.put(tree, getCurrentPath());
                        }
                    }
                } else {
                    Element decl = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getQualifiedIdentifier()));

                    if (decl != null) {
                        if (decl.asType().getKind() != TypeKind.ERROR) {
                            element2Import.put(decl, tree);
                            importedBySingleImport.add(decl);
                            import2Highlight.put(tree, getCurrentPath());
                        } else {
                            if (tree.getQualifiedIdentifier().getKind() == Kind.MEMBER_SELECT) {
                                addUnresolvableImport(((MemberSelectTree) tree.getQualifiedIdentifier()).getIdentifier(), tree);
                                import2Highlight.put(tree, getCurrentPath());
                            }
                        }
                    }
                }
            } else {
                if (tree.getQualifiedIdentifier() != null && tree.getQualifiedIdentifier().getKind() == Kind.MEMBER_SELECT) {
                    MemberSelectTree qualIdent = (MemberSelectTree) tree.getQualifiedIdentifier();
                    Element decl = info.getTrees().getElement(new TreePath(new TreePath(getCurrentPath(), qualIdent), qualIdent.getExpression()));

                    if (   decl != null
                        && (decl.getKind().isClass() || decl.getKind().isInterface())) {
                        if (decl.asType().getKind() != TypeKind.ERROR) {
                            Name simpleName = isStar(tree) ? null : qualIdent.getIdentifier();
                            boolean assign = false;

                            for (Element e : info.getElements().getAllMembers((TypeElement) decl)) {
                                if (!e.getModifiers().contains(Modifier.STATIC)) continue;
                                if (simpleName != null && !e.getSimpleName().equals(simpleName)) {
                                    continue;
                                }
                                if (e.getKind() == ElementKind.METHOD) {
                                    method2Import.put(e.getSimpleName().toString() + e.asType().toString(), tree);
                                    assign = true;
                                    continue;
                                }

                                if (e.getKind().isField()) {
                                    element2Import.put(e, tree);
                                    assign = true;
                                    continue;
                                }

                                if (e.getKind().isClass() || e.getKind().isInterface()) {
                                    element2Import.put(e, tree);
                                    assign = true;
                                    continue;
                                }
                            }

                            if (!assign) {
                                addUnresolvableImport(qualIdent.getIdentifier(), tree);
                            }
                            import2Highlight.put(tree, getCurrentPath());
                        } else {
                            if (!isStar(tree)) {
                                addUnresolvableImport(qualIdent.getIdentifier(), tree);
                            } else {
                                unresolvablePackageImports.add(tree);
                            }
                            import2Highlight.put(tree, getCurrentPath());
                        }
                    }
                }
            }
            super.visitImport(tree, null);
            return null;
        }

        private void addUnresolvableImport(Name name, ImportTree imp) {
            String key = name.toString();

            Collection<ImportTree> l = simpleName2UnresolvableImports.get(key);

            if (l == null) {
                simpleName2UnresolvableImports.put(key, l = new LinkedList<ImportTree>());
            }

            l.add(imp);
        }
        
        private void typeUsed(Element decl, TreePath expr, boolean methodInvocation) {
            if (decl != null && (expr == null || expr.getLeaf().getKind() == Kind.IDENTIFIER || expr.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE)) {
                if (decl.asType() != null && decl.asType().getKind() != TypeKind.ERROR) {
                    ImportTree imp = decl.getKind() != ElementKind.METHOD ? element2Import.remove(decl) : method2Import.remove(decl.getSimpleName().toString() + decl.asType().toString());

                    if (imp != null) {
                        if (isStar(imp)) {
                            //TODO: explain
                            handleUnresolvableImports(decl, methodInvocation, false);
                        }
                        import2Highlight.remove(imp);
                    }
                } else {
                    handleUnresolvableImports(decl, methodInvocation, true);
                }
            }
        }

        private void handleUnresolvableImports(Element decl,
                boolean methodInvocation, boolean removeStarImports) {
            Name simpleName = decl.getSimpleName();
            if (simpleName != null) {
                Collection<ImportTree> imps = simpleName2UnresolvableImports.get(simpleName.toString());

                if (imps != null) {
                    for (ImportTree imp : imps) {
                        if (!methodInvocation || imp.isStatic()) {
                            import2Highlight.remove(imp);
                        }
                    }
                } else {
                    if (removeStarImports) {
                        //TODO: explain
                        for (ImportTree unresolvable : unresolvablePackageImports) {
                            if (!methodInvocation || unresolvable.isStatic()) {
                                import2Highlight.remove(unresolvable);
                            }
                        }
                    }
                }
            }
        }
    }
    
}
