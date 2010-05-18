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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.selector.java.nodes;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Icon;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.selector.spi.nodes.ClassNode;
import org.netbeans.modules.profiler.selector.spi.nodes.ConstructorsNode;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.spi.nodes.InnerClassesNode;
import org.netbeans.modules.profiler.selector.spi.nodes.MethodNode;
import org.netbeans.modules.profiler.selector.spi.nodes.MethodsNode;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavaClassNode extends ClassNode {
    private ClasspathInfo cpInfo;
    private ElementHandle<TypeElement> typeHandle;
    private SourceCodeSelection signature;

    public JavaClassNode(ClasspathInfo cpInfo, String displayName, boolean isAnonymous, TypeElement type, ContainerNode parent) {
        super(ElementUtilities.getBinaryName(type), displayName, isAnonymous, parent);
        this.cpInfo = cpInfo;
        this.typeHandle = ElementHandle.create(type);

        signature = new SourceCodeSelection(ElementUtilities.getBinaryName(type), "*", ""); // NOI18N
    }

    public JavaClassNode(ClasspathInfo cpInfo, boolean isAnonymous, TypeElement type, ContainerNode parent) {
        this(cpInfo, getSimpleName(ElementUtilities.getBinaryName(type)), isAnonymous, type, parent);
    }

    public JavaClassNode(ClasspathInfo cpInfo, TypeElement type, ContainerNode parent) {
        this(cpInfo, false, type, parent);
    }

    public JavaClassNode(ClasspathInfo cpInfo, String displayName, Icon icon, boolean isAnonymous, TypeElement type, ContainerNode parent) {
        super(ElementUtilities.getBinaryName(type), displayName, icon, isAnonymous, parent);
        this.cpInfo = cpInfo;
        this.typeHandle = ElementHandle.create(type);

        signature = new SourceCodeSelection(ElementUtilities.getBinaryName(type), "*", ""); // NOI18N
    }

    public JavaClassNode(ClasspathInfo cpInfo, Icon icon, boolean isAnonymous, TypeElement type, ContainerNode parent) {
        this(cpInfo, type.getSimpleName().toString(), icon, isAnonymous, type, parent);
        this.cpInfo = cpInfo;
        this.typeHandle = ElementHandle.create(type);

        signature = new SourceCodeSelection(ElementUtilities.getBinaryName(type), "*", ""); // NOI18N
    }

    public JavaClassNode(ClasspathInfo cpInfo, Icon icon, TypeElement type, ContainerNode parent) {
        this(cpInfo, icon, false, type, parent);
    }

    @Override
    public SourceCodeSelection getSignature() {
        return signature;
    }

    protected ConstructorsNode getConstructorsNode() {
        return new ConstructorsNode(this) {

            @Override
            protected List<SelectorNode> getConstructorNodes(final ConstructorsNode parent) {
                final List<SelectorNode> constructorNodes = new ArrayList<SelectorNode>();
                try {
                    JavaSource js = JavaSource.create(cpInfo, new org.openide.filesystems.FileObject[0]);
                    js.runUserActionTask(new CancellableTask<CompilationController>() {

                        public void cancel() {
                        }

                        public void run(CompilationController controller)
                                throws Exception {
                            controller.toPhase(JavaSource.Phase.RESOLVED);
                            TypeElement type = typeHandle.resolve(controller);
                            List<ExecutableElement> methods = ElementFilter.constructorsIn(type.getEnclosedElements());

                            for (ExecutableElement method : methods) {
                                constructorNodes.add(new JavaConstructorNode(cpInfo, method, method.getSimpleName().toString(), parent));
                            }
                        }
                    }, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return constructorNodes;
            }
        };
    }

    protected InnerClassesNode getInnerClassesNode() {
        return new InnerClassesNode(this) {

            private String getInterfaceNames(TypeElement classElement, CompilationInfo ci) {
                String ifcNames = null;
                List<? extends TypeMirror> ifcs = classElement.getInterfaces();

                if (ifcs != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean firstRun = true;

                    for (TypeMirror ifc : ifcs) {
                        Element superclassElement = ci.getTypes().asElement(ifc);

                        if ((superclassElement != null) && (superclassElement.getKind() == ElementKind.INTERFACE)) {
                            if (!firstRun) {
                                sb.append(", ");
                            }

                            sb.append(ElementUtilities.getBinaryName((TypeElement) superclassElement));
                        }
                    }

                    ifcNames = sb.toString();
                }

                return ifcNames;
            }

            private String getSuperTypeClassName(TypeElement classElement, CompilationInfo ci) {
                String superClassName = null;

                TypeMirror superclass = classElement.getSuperclass();

                if (superclass != null) {
                    Element superclassElement = ci.getTypes().asElement(superclass);

                    if ((superclassElement != null) && (superclassElement.getKind() == ElementKind.CLASS)) {
                        String superclassName = ElementUtilities.getBinaryName((TypeElement) superclassElement);

                        if (!superclassName.equals("java.lang.Object")) {
                            superClassName = superclassName;
                        }
                    }
                }

                return superClassName;
            }

            private void addAnonymousInnerClasses(final CompilationController controller, final InnerClassesNode parentClass,
                    final Set<ClassNode> innerClassNodes)
                    throws IOException {
                final Pattern anonymousInnerClassPattern = Pattern.compile("\\$[0-9]*");
                final int parentClassNameLength = typeHandle.getBinaryName().length();
                FileObject fo = org.netbeans.api.java.source.SourceUtils.getFile(typeHandle,
                        controller.getClasspathInfo());
                JavaSource.forFileObject(fo).runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(final CompilationController cc)
                            throws Exception {
                        cc.toPhase(JavaSource.Phase.RESOLVED);

                        TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                            @Override
                            public Void visitClass(ClassTree node, Void v) {
                                Element classElement = controller.getTrees().getElement(getCurrentPath());

                                if ((classElement != null) && (classElement.getKind() == ElementKind.CLASS)) {
                                    TypeElement innerClassElement = (TypeElement) classElement;
                                    String className = ElementUtilities.getBinaryName(innerClassElement);

                                    if (className.length() <= parentClassNameLength) {
                                        className = "";
                                    } else {
                                        className = className.substring(parentClassNameLength);
                                    }

                                    if (anonymousInnerClassPattern.matcher(className).matches()) {
                                        String implementedClassName = getSuperTypeClassName(innerClassElement, cc);

                                        if (implementedClassName == null) {
                                            implementedClassName = getInterfaceNames(innerClassElement, cc);
                                        }

                                        if (implementedClassName != null) {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(className).append("[").append(implementedClassName).append("]"); // NOI18N
                                            className = sb.toString();
                                            innerClassNodes.add(new JavaClassNode(cpInfo, true, innerClassElement, parentClass));
                                        }
                                    }
                                }

                                super.visitClass(node, v);

                                return null;
                            }
//
//                            @Override
//                            public Void visitAssignment(AssignmentTree assTree, Void v) {
//                                return super.visitAssignment(assTree, v);
//                            }
                        };

                        scanner.scan(cc.getCompilationUnit(), null);
                    }
                }, false);
            }

            @Override
            protected Set<ClassNode> getInnerClassNodes(final InnerClassesNode parent) {
                final Set<ClassNode> innerClassNodes = new HashSet<ClassNode>();

                try {
                    JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);
                    js.runUserActionTask(new CancellableTask<CompilationController>() {

                        public void cancel() {
                        }

                        public void run(CompilationController controller)
                                throws Exception {
                            if (controller.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                                return;
                            }

                            TypeElement type = typeHandle.resolve(controller);
                            List<TypeElement> elements = ElementFilter.typesIn(type.getEnclosedElements());

                            for (TypeElement element : elements) {
                                innerClassNodes.add(new JavaClassNode(cpInfo, false, element, parent));
                            }

                            addAnonymousInnerClasses(controller, parent, innerClassNodes);
                        }
                    }, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return innerClassNodes;
            }
        };
    }

    protected MethodsNode getMethodsNode() {
        return new MethodsNode(this) {

            @Override
            protected List<MethodNode> getMethodNodes(final MethodsNode parent) {
                final List<MethodNode> methodNodes = new ArrayList<MethodNode>();
                try {
                    JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);
                    js.runUserActionTask(new CancellableTask<CompilationController>() {

                        public void cancel() {
                        }

                        public void run(CompilationController controller)
                                throws Exception {
                            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                            TypeElement type = typeHandle.resolve(controller);
                            List<ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());

                            for (ExecutableElement method : methods) {
                                MethodNode methodNode = new JavaMethodNode(cpInfo, method, parent);

                                if (methodNode.getSignature() != null) {
                                    methodNodes.add(methodNode);
                                }
                            }
                        }
                    }, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                return methodNodes;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavaClassNode other = (JavaClassNode) obj;
        if (this.typeHandle != other.typeHandle && (this.typeHandle == null || !this.typeHandle.equals(other.typeHandle))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.typeHandle != null ? this.typeHandle.hashCode() : 0);
        return hash;
    }

    private static String getSimpleName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > -1) {
            return qualifiedName.substring(index + 1);
        }
        return qualifiedName;
    }
}
