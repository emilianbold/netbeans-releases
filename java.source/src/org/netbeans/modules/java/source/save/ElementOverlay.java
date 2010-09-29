/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.save;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.QualIdentTree;

/**
 *
 * @author lahvac
 */
public class ElementOverlay {

    private final Map<String, List<String>> class2Enclosed = new HashMap<String, List<String>>();
    private final Map<String, List<Tree>> class2SuperElementTrees = new HashMap<String, List<Tree>>();
    private final Set<String> packages = new HashSet<String>();
    private final Set<String> classes = new HashSet<String>();
    private final Map<String, Element> elementCache = new HashMap<String, Element>();

    public List<Element> getEnclosedElements(ASTService ast, Elements elements, String parent) {
        List<Element> result = new LinkedList<Element>();
        Element parentEl = resolve(ast, elements, parent);

        if (parentEl == null) throw new IllegalStateException(parent);
        if (!(parentEl instanceof FakeTypeElement)) {
            result.addAll(parentEl.getEnclosedElements());
        }

        List<String> enclosed = class2Enclosed.get(parent);

        if (enclosed != null) {
            for (String enc : enclosed) {
                Element el = createElement(ast, elements, enc);

                if (el != null) {
                    result.add(el);
                }
            }
        }

        return result;
    }

    private Element createElement(ASTService ast, Elements elements, String name) {
        Element el = elementCache.get(name);

        if (el == null) {
            int lastDot = name.lastIndexOf('.');
            Name simpleName = elements.getName(name.substring(lastDot + 1));
            Name fqnName = elements.getName(name);

            if (classes.contains(name)) {
                Element parent = lastDot > 0 ? resolve(ast, elements, name.substring(0, lastDot)) : elements.getPackageElement("");

                elementCache.put(name, el = new FakeTypeElement(ast, elements, simpleName, fqnName, name, parent));
            } else if (packages.contains(name)) {
                elementCache.put(name, el = new FakePackageElement(ast, elements, fqnName, name, simpleName));
            } else {
                return null;//XXX: handling of this null in callers!
            }
        }

        return el;
    }

    public Element resolve(ASTService ast, Elements elements, String what) {
        Element result = null;
        
        if (classes.contains(what)) {
            result = createElement(ast, elements, what);
        }

        if (result == null) {
            result = elements.getTypeElement(what);
        }

        if (result == null) {
            result = elements.getPackageElement(what);
        }

        if (result == null) {
            result = createElement(ast, elements, what);
        }

        return result;
    }

    public void registerClass(String parent, String clazz, Tree ext, Collection<? extends Tree> impl) {
        classes.add(clazz);
        
        List<String> c = class2Enclosed.get(parent);

        if (c == null) {
            class2Enclosed.put(parent, c = new LinkedList<String>());
        }

        c.add(clazz);

        List<Tree> superTree = new LinkedList<Tree>();

        if (ext != null) superTree.add(ext);

        superTree.addAll(impl);

        class2SuperElementTrees.put(clazz, superTree);
    }

    public void registerPackage(String currentPackage) {
        packages.add(currentPackage);
    }

    public Iterable<? extends Element> getAllSuperElements(ASTService ast, Elements elements, Element forElement) {
        List<Element> result = new LinkedList<Element>();
        if (forElement instanceof FakeTypeElement) {

            for (Tree t : class2SuperElementTrees.get(((FakeTypeElement) forElement).fqnString)) {
                Element el = ast.getElement(t);

                if (el != null) {
                    result.add(el);
                } else if (t instanceof QualIdentTree) {
                    result.add(resolve(ast, elements, ((QualIdentTree) t).getFQN()));
                } else {
                    Logger.getLogger(ElementOverlay.class.getName()).log(Level.SEVERE, "No element and no QualIdent");
                }
            }
        } else if (forElement.getKind().isClass() || forElement.getKind().isInterface()) {
            addElement(((TypeElement) forElement).getSuperclass(), result);
            for (TypeMirror i : ((TypeElement) forElement).getInterfaces()) {
                addElement(i, result);
            }
        }

        return result;
    }

    private void addElement(TypeMirror tm, List<Element> result) {
        if (tm == null || tm.getKind() != TypeKind.DECLARED) {
            return;
        }

        result.add(((DeclaredType) tm).asElement());
    }

    public void clearElementsCache() {
        elementCache.clear();;
    }

    private final class FakeTypeElement implements TypeElement {

        private final ASTService ast;
        private final Elements elements;
        private final Name simpleName;
        private final Name fqn;
        private final String fqnString;
        private final Element parent;

        public FakeTypeElement(ASTService ast, Elements elements, Name simpleName, Name fqn, String fqnString, Element parent) {
            this.ast = ast;
            this.elements = elements;
            this.simpleName = simpleName;
            this.fqn = fqn;
            this.fqnString = fqnString;
            this.parent = parent;
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            return ElementOverlay.this.getEnclosedElements(ast, elements, fqnString);
        }

        @Override
        public NestingKind getNestingKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Name getQualifiedName() {
            return fqn;
        }

        @Override
        public TypeMirror getSuperclass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends TypeMirror> getInterfaces() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends TypeParameterElement> getTypeParameters() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Name getSimpleName() {
            return simpleName;
        }

        @Override
        public Element getEnclosingElement() {
            return parent;
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private class FakePackageElement implements PackageElement {

        private final ASTService ast;
        private final Elements elements;
        private final Name fqn;
        private final String fqnString;
        private final Name simpleName;

        public FakePackageElement(ASTService ast, Elements elements, Name fqn, String fqnString, Name simpleName) {
            this.ast = ast;
            this.elements = elements;
            this.fqn = fqn;
            this.fqnString = fqnString;
            this.simpleName = simpleName;
        }

        @Override
        public Name getQualifiedName() {
            return fqn;
        }

        @Override
        public boolean isUnnamed() {
            return false;
        }

        @Override
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Name getSimpleName() {
            return simpleName;
        }

        @Override
        public Element getEnclosingElement() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            //should delegate to pre-existing PackageElement, if available:
            return ElementOverlay.this.getEnclosedElements(ast, elements, fqnString);
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    public static class FQNComputer {
        private final StringBuilder fqn = new StringBuilder();
        public void setCompilationUnit(CompilationUnitTree cut) {
            setPackageNameTree(cut.getPackageName());
        }
        public void enterClass(ClassTree ct) {
            if (fqn.length() > 0) fqn.append('.');
            fqn.append(ct.getSimpleName());
        }
        public void leaveClass() {
            int dot = Math.max(0, fqn.lastIndexOf("."));
            
            fqn.delete(dot, fqn.length());
        }
        public String getFQN() {
            return fqn.toString();
        }

        public void setPackageNameTree(ExpressionTree packageNameTree) {
            fqn.delete(0, fqn.length());
            if (packageNameTree != null) {
                fqn.append(packageNameTree.toString()); //XXX: should not use toString
            }
        }
    }
}
