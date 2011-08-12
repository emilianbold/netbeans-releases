/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.javac;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavacClassInfo extends SourceClassInfo {
    private ElementHandle<TypeElement> handle;
    private FileObject src;
    private ClasspathInfo cpInfo;
    private JavaSource source;

    private JavacClassInfo(ElementHandle<TypeElement> eh) {
        super(getSimpleName(eh.getBinaryName()), eh.getBinaryName(), eh.getBinaryName().replace('.', '/')); // NOI18N
        handle = eh;
    }
    
    public JavacClassInfo(ElementHandle<TypeElement> eh, ClasspathInfo cpInfo) {
        this(eh);
        
        this.cpInfo = cpInfo;
    }
    
    public JavacClassInfo(ElementHandle<TypeElement> eh, CompilationController cc) {
        this(eh);
        
        this.cpInfo = cc.getClasspathInfo();
        source = cc.getJavaSource();
    }

    @Override
    public Set<SourceMethodInfo> getMethods(final boolean all) {
        final Set<SourceMethodInfo>[] rslt = new Set[1];
        if (handle != null) {
            try {
                getSource(false).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED) == JavaSource.Phase.ELEMENTS_RESOLVED) {
                            rslt[0] = getMethods(cc, all);
                        }
                    }
                }, true);
            } catch (IllegalArgumentException e) {
            } catch (IOException e) {
            }
        }
        return rslt[0] != null ? rslt[0] : Collections.EMPTY_SET;
    }

    @Override
    public Set<SourceClassInfo> getSubclasses() {
        final Set<SourceClassInfo>[] rslt = new Set[]{Collections.EMPTY_SET};
        if (handle != null) {
            try {
                getSource(true).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        rslt[0] = getSubclasses(cc);
                    }
                }, true);
            } catch (IllegalArgumentException e) {
            } catch (IOException e) {
            }
        }
        return rslt[0] != null ? rslt[0] : Collections.EMPTY_SET;
    }
    
    @Override
    public synchronized FileObject getFile() {
        if (src == null) {
            src = SourceUtils.getFile(handle, cpInfo);
            if (src == null) {
                String resName = handle.getBinaryName().replace('.', '/').concat(".class"); // NOI18N
                src = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT).findResource(resName);
                if (src == null) {
                    src = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE).findResource(resName);
                    if (src == null) {
                        src = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resName);
                    }
                }
            }
        }
        return src;
    }

    @Override
    public Set<SourceMethodInfo> getConstructors() {
        final Set<SourceMethodInfo> infos = new HashSet<SourceMethodInfo>();
        if (handle != null) {
            try {
                getSource(false).runUserActionTask(new Task<CompilationController>() {
                    
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED) == JavaSource.Phase.ELEMENTS_RESOLVED) {
                            TypeElement type = handle.resolve(cc);
                            for (ExecutableElement method : ElementFilter.constructorsIn(type.getEnclosedElements())) {
                                infos.add(new JavacMethodInfo(method, cc));
                            }
                        }
                    }
                }, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return infos;
    }

    @Override
    public Set<SourceClassInfo> getInnerClases() {
        final Set<SourceClassInfo> innerClasses = new HashSet<SourceClassInfo>();

        if (handle != null) {
            try {
                getSource(false).runUserActionTask(new Task<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController cc)
                            throws Exception {
                        if (cc.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                            return;
                        }

                        TypeElement type = handle.resolve(cc);
                        List<TypeElement> elements = ElementFilter.typesIn(type.getEnclosedElements());

                        for (TypeElement element : elements) {
                            innerClasses.add(new JavacClassInfo(ElementHandle.create(element), cc));
                        }

                        addAnonymousInnerClasses(cc, innerClasses);
                    }
                }, true);
            } catch (IllegalArgumentException ex) {
                // TODO
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return innerClasses;
    }

    @Override
    public Set<SourceClassInfo> getInterfaces() {
        final Set<SourceClassInfo> ifcs = new HashSet<SourceClassInfo>();
        if (handle != null) {
            try {
                getSource(true).runUserActionTask(new Task<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement te = handle.resolve(cc);
                        
                        Types t = cc.getTypes();
                        for(TypeMirror ifc : te.getInterfaces()) {
                            TypeElement ife = (TypeElement)t.asElement(ifc);
                            ifcs.add(new JavacClassInfo(ElementHandle.create(ife), cc));
                        }
                    }
                }, true);
            } catch (IOException e) {
            }    
        }
        return ifcs;
    }

    @Override
    public SourceClassInfo getSuperType() {
        final SourceClassInfo[] rslt = new SourceClassInfo[1];
        
        if (handle != null) {
            try {
                getSource(false).runUserActionTask(new Task<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController cc) throws Exception {
                        if (cc.toPhase(JavaSource.Phase.RESOLVED) == JavaSource.Phase.RESOLVED) {
                            TypeElement te = handle.resolve(cc);

                            TypeMirror superTm = te.getSuperclass();
                            if (superTm != null) {
                                TypeElement superType = (TypeElement)cc.getTypes().asElement(superTm);

                                rslt[0] = new JavacClassInfo(ElementHandle.create(superType), cc);
                            }
                        }
                    }
                }, true);
            } catch (IOException e) {
            }    
        }
        return rslt[0];
    }
    
    final Set<SourceMethodInfo> getMethods(final CompilationController cc, final boolean all) {
        final Set<SourceMethodInfo> mis = new HashSet<SourceMethodInfo>();
        TypeElement te = handle.resolve(cc);
        if (te != null) {
            Set<ExecutableElement> methods = new HashSet<ExecutableElement>(ElementFilter.methodsIn(te.getEnclosedElements()));
            for (ExecutableElement method : ElementFilter.methodsIn(cc.getElements().getAllMembers(te))) {
                String parent = ElementUtilities.getBinaryName((TypeElement) method.getEnclosingElement());
                if (parent.equals(getQualifiedName()) || 
                    (all && 
                     !containsAny(method.getModifiers(), EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)) &&
                     !parent.equals(Object.class.getName()))) {
                    methods.add(method);
                }
            }
            for(ExecutableElement method : methods) {
                mis.add(new JavacMethodInfo(method, cc));
            }
        }
        return mis;
    }
    
    final Set<SourceClassInfo> getSubclasses(final CompilationController cc) {
        final Set<SourceClassInfo> subs = new HashSet<SourceClassInfo>();
        TypeElement te = handle.resolve(cc);
        if (te != null) {
            for(ElementHandle<TypeElement> eh : findImplementors(cc.getClasspathInfo(), handle)) {
                subs.add(new JavacClassInfo(eh, cc));
            }
        }
        return subs;
    }
    
    final Set<SourceClassInfo> getSuperclasses(final CompilationController cc) {
        final Set<SourceClassInfo> sups = new HashSet<SourceClassInfo>();
        TypeElement te = handle.resolve(cc);
        if (te != null) {
            collectSuperclass(cc, te, sups);
        }
        return sups;
    }
    
    private void collectSuperclass(final CompilationController cc, final TypeElement te, Set<SourceClassInfo> superClasses) {
        TypeElement sType = (TypeElement)cc.getTypes().asElement(te.getSuperclass());
        if (sType != null) {//
            superClasses.add(new JavacClassInfo(ElementHandle.create(sType), cc));
            collectSuperclass(cc, sType, superClasses);
        }
    }
    
    private static String getSimpleName(String qualName) {
        String name = qualName;
        int lastDot = name.lastIndexOf(".");
        if (lastDot > -1) {
            name = name.substring(lastDot + 1);
        }
        return name;
    }
    
    private static Set<ElementHandle<TypeElement>> findImplementors(ClasspathInfo cpInfo, final ElementHandle<TypeElement> baseType) {
        final Set<ClassIndex.SearchKind> kind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
        final Set<ClassIndex.SearchScope> scope = EnumSet.allOf(ClassIndex.SearchScope.class);
        
        Set<ElementHandle<TypeElement>> allImplementors = new HashSet<ElementHandle<TypeElement>>();
        Set<ElementHandle<TypeElement>> implementors = cpInfo.getClassIndex().getElements(baseType, kind, scope);

        do {
            Set<ElementHandle<TypeElement>> tmpImplementors = new HashSet<ElementHandle<TypeElement>>();
            allImplementors.addAll(implementors);

            for (ElementHandle<TypeElement> element : implementors) {
                tmpImplementors.addAll(cpInfo.getClassIndex().getElements(element, kind, scope));
            }

            implementors = tmpImplementors;
        } while (!implementors.isEmpty());
        
        return allImplementors;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavacClassInfo other = (JavacClassInfo) obj;
        if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
            return false;
        }
        if (this.src != null) {
            if (this.src != other.src && !this.src.equals(other.src)){
                return false;
            }
        } 
        if (this.cpInfo != null) {
            if (this.cpInfo != other.cpInfo && !this.cpInfo.equals(other.cpInfo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.handle != null ? this.handle.hashCode() : 0);
        hash = 89 * hash + (this.src != null ? this.src.hashCode() : 0);
        hash = 89 * hash + (this.cpInfo != null ? this.cpInfo.hashCode() : 0);
        return hash;
    }   

    private void addAnonymousInnerClasses(final CompilationController cc, final Set<SourceClassInfo> innerClasses)
            throws IOException {
        final int parentClassNameLength = getQualifiedName().length();

        cc.toPhase(JavaSource.Phase.RESOLVED);
        
        TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

            @Override
            public Void visitClass(ClassTree node, Void v) {
                Element classElement = cc.getTrees().getElement(getCurrentPath());

                if ((classElement != null) && (classElement.getKind() == ElementKind.CLASS)) {
                    TypeElement innerClassElement = (TypeElement) classElement;
                    String className = ElementUtilities.getBinaryName(innerClassElement);

                    if (className.length() <= parentClassNameLength) {
                        className = "";
                    } else {
                        className = className.substring(parentClassNameLength);
                    }

                    if (isAnonymous(className)) {
                        innerClasses.add(new JavacClassInfo(ElementHandle.create(innerClassElement), cc));
                    }
                }

                super.visitClass(node, v);

                return null;
            }
        };

        scanner.scan(cc.getCompilationUnit(), null);
    }
    
    private static <T> boolean containsAny(Set<T> superSet, Set<T> subSet) {
        Set<T> set = new HashSet<T>(superSet);
        
        return set.removeAll(subSet);
    }
    
    private synchronized JavaSource getSource(boolean allowSourceLess) {
        JavaSource jSrc = source;
        if (jSrc == null || (!allowSourceLess && jSrc.getFileObjects().isEmpty())) {
            FileObject f = getFile();
            if (f.getExt().toLowerCase().equals("java")) { // NOI18N
                jSrc = cpInfo != null ? JavaSource.create(cpInfo, getFile()) : JavaSource.forFileObject(getFile());
            } else if (cpInfo != null) {
                jSrc = JavaSource.create(cpInfo);
            }
            source = jSrc;
        }
        return source;
    }
}
