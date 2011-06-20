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
package org.netbeans.modules.profiler.nbimpl.providers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource.ClassInfo;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource.MethodInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.spi.java.AbstractJavaProfilerSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
@MimeRegistration(mimeType = "text/x-java", service = AbstractJavaProfilerSource.class)
public class JavaProfilerSourceImpl implements AbstractJavaProfilerSource {
    private static class ClassInfoImpl implements ClassInfo {
        final private String simpleName, qualifiedName, vmName;
        
        private ClassInfoImpl(String simpleName, String qualifiedName, String vmName) {
            this.simpleName = simpleName;
            this.qualifiedName = qualifiedName;
            this.vmName = vmName;
        }
        
        @Override
        public String getQualifiedName() {
            return qualifiedName;
        }

        @Override
        public String getSimpleName() {
            return simpleName;
        }

        @Override
        public String getVMName() {
            return vmName;
        }
        
    }
    private static class MethodInfoImpl implements MethodInfo {
        private String className, name, signature, vmName;
        private boolean execFlag;

        private MethodInfoImpl(String className, String name, String signature, String vmName, boolean execFlag) {
            this.className = className;
            this.name = name;
            this.signature = signature;
            this.vmName = vmName;
            this.execFlag = execFlag;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getClassName() {
            return className;
        }        

        @Override
        public String getSignature() {
            return signature; 
        }

        @Override
        public String getVMName() {
            return vmName;
        }
        
        @Override
        public boolean isExecutable() {
            return execFlag;
        }        
    }
    
    private static final String[] APPLET_CLASSES = new String[]{"java.applet.Applet", "javax.swing.JApplet"}; // NOI18N
    private static final String[] TEST_CLASSES = new String[]{"junit.framework.TestCase", "junit.framework.TestSuite"}; // NOI18N
    private static final String[] TEST_ANNOTATIONS = new String[]{"org.junit.Test", "org.testng.annotations.Test"}; // NOI18N
    
    @Override
    public ClassInfo getEnclosingClass(FileObject fo, final int position) {
        final ClassInfo[] result = new ClassInfo[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController controller)
                        throws Exception {
                    if (controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                        return;
                    }

                    TypeElement parentClass = controller.getTreeUtilities().scopeFor(position).getEnclosingClass();

                    if (parentClass != null) {
                        result[0] = new ClassInfoImpl(parentClass.getSimpleName().toString(), parentClass.getQualifiedName().toString(), ElementUtilities.getBinaryName(parentClass));
                    }
                }
            }, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result[0];
    }

    @Override
    public MethodInfo getEnclosingMethod(FileObject fo, final int position) {
        final MethodInfo[] result = new MethodInfo[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController cc)
                        throws Exception {
                    if (cc.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                        return;
                    }

                    ExecutableElement parentMethod = cc.getTreeUtilities().scopeFor(position).getEnclosingMethod();

                    if (parentMethod != null) {
                        result[0] = new MethodInfoImpl(ElementUtilities.getBinaryName((TypeElement)parentMethod.getEnclosingElement()), parentMethod.getSimpleName().toString(), ElementUtilitiesEx.getBinaryName(parentMethod, cc), getVMMethodName(parentMethod), isExecutable(parentMethod));
                    }
                }
            }, true);
        } catch (IOException ex) {
            ProfilerLogger.log(ex);
        }

        return result[0];
    }

    @Override
    public Set<ClassInfo> getMainClasses(final FileObject fo) {
        final Set<ClassInfo> mainClasses = new HashSet<ClassInfo>();
        
        for(ElementHandle<TypeElement> handle : SourceUtils.getMainClasses(fo)) {
            mainClasses.add(new ClassInfoImpl(getSimpleClassName(handle.getQualifiedName()), handle.getQualifiedName(), handle.getBinaryName()));
        }        
        
        return mainClasses;
    }

    @Override
    public Set<MethodInfo> getConstructors(FileObject fo) {
        Set<MethodInfo> constructors = new HashSet<MethodInfo>();
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController controller)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                        public Void visitMethod(MethodTree node, Void p) {
                            Void retValue;
                            ExecutableElement method = (ExecutableElement) controller.getTrees().getElement(getCurrentPath());
                            retValue = super.visitMethod(node, p);

                            return retValue;
                        }
                    };

                    scanner.scan(controller.getCompilationUnit(), null);
                }
            }, true);
        } catch (IOException e) {
            ProfilerLogger.log(e);
        }

        return constructors;
    }

    @Override
    public ClassInfo getTopLevelClass(FileObject fo) {
        final ClassInfo[] result = new ClassInfo[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController controller)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePathScanner<ClassInfo, Void> scanner = new TreePathScanner<ClassInfo, Void>() {

                        public ClassInfo visitClass(ClassTree node, Void p) {
                            try {
                                TypeElement te = (TypeElement)controller.getTrees().getElement(getCurrentPath());
                                return new ClassInfoImpl(te.getSimpleName().toString(), te.getQualifiedName().toString(), ElementUtilities.getBinaryName(te));
                            } catch (NullPointerException e) {
                                ProfilerLogger.log(e);
                                return null;
                            }
                        }
                    };

                    result[0] = scanner.scan(controller.getCompilationUnit(), null);
                }
            }, true);
        } catch (IOException ex) {
            ProfilerLogger.log(ex);
        }

        return result[0];
    }

    @Override
    public boolean hasAnnotation(FileObject fo, final String[] annotationNames, boolean allRequired) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);

        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return false;
        }
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                    // do nothing
                }

                public void run(final CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);

                    TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                        @Override
                        public Void visitAnnotation(AnnotationTree annTree, Void p) {
                            if (result.get()) {
                                return null;
                            }

                            TypeMirror tm = controller.getTrees().getTypeMirror(getCurrentPath());
                            if (tm != null) {
                                TypeElement annType = (TypeElement) controller.getTypes().asElement(tm);
                                if (annType != null) {
                                    boolean res = result.get();
                                    if (!res) {
                                        for (String ann : annotationNames) {
                                            if (ann.equals(ElementUtilities.getBinaryName(annType))) {
                                                res = true;
                                                break;
                                            }
                                        }
                                    }
                                    result.set(res);
                                }
                            }
                            return null;
                        }
                    };
                    scanner.scan(controller.getCompilationUnit(), null);

                    latch.countDown();
                }
            }, true);
            latch.await();
            return result.get();
        } catch (IOException e) {
            ProfilerLogger.log(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean hasAnnotation(FileObject fo, String annotation) {
        return hasAnnotation(fo, new String[]{annotation}, true);
    }

    @Override
    public boolean isApplet(FileObject fo) {
        return isInstanceOf(fo, APPLET_CLASSES, false); // NOI18N
    }

    @Override
    public boolean isInstanceOf(FileObject fo, final String[] classNames, final boolean allRequired) {
        final boolean[] result = new boolean[]{false};

        // get javasource for the java file
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return false; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController controller)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (controller.toPhase(Phase.ELEMENTS_RESOLVED).compareTo(Phase.ELEMENTS_RESOLVED) < 0) {
                        return;
                    }

                    Elements elements = controller.getElements();
                    Trees trees = controller.getTrees();
                    Types types = controller.getTypes();

                    Collection<TypeElement> classElements = new ArrayList<TypeElement>();

                    for (String className : classNames) {
                        TypeElement resolvedElement = elements.getTypeElement(className);

                        if (resolvedElement != null) {
                            classElements.add(resolvedElement);
                        }
                    }

                    if (classElements.isEmpty()) {
                        result[0] = false;

                        return;
                    }

                    CompilationUnitTree cu = controller.getCompilationUnit();
                    List<? extends Tree> topLevels = cu.getTypeDecls();

                    for (Tree topLevel : topLevels) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(topLevel.getKind())) {
                            TypeElement type = (TypeElement) trees.getElement(TreePath.getPath(cu, topLevel));

                            if (type != null) {
                                Set<Modifier> modifiers = type.getModifiers();

                                if (modifiers.contains(Modifier.PUBLIC) && (classElements != null)) {
                                    boolean rslt = allRequired;

                                    for (TypeElement classElement : classElements) {
                                        if (classElement == null) {
                                            continue;
                                        }

                                        if (allRequired) {
                                            rslt = rslt && types.isSubtype(type.asType(), classElement.asType());

                                            if (!rslt) {
                                                break;
                                            }
                                        } else {
                                            rslt = rslt || types.isSubtype(type.asType(), classElement.asType());

                                            if (rslt) {
                                                break;
                                            }
                                        }
                                    }

                                    result[0] = rslt;

                                    if (rslt) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException e) {
            ProfilerLogger.log(e);
        }

        return result[0];
    }

    @Override
    public boolean isInstanceOf(FileObject fo, String className) {
        return isInstanceOf(fo, new String[]{className}, true);
    }

    @Override
    public boolean isOffsetValid(FileObject fo, final int offset) {
        final Boolean[] validated = new Boolean[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js != null) {
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                            throws Exception {
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        validated[0] = false; // non-validated default

                        Scope sc = controller.getTreeUtilities().scopeFor(offset);

                        if (sc.getEnclosingClass() != null) {
                            validated[0] = true;
                        }

                    }
                }, true);
            } catch (IOException ex) {
                ProfilerLogger.log(ex);
            }

        }

        return validated[0];
    }

    @Override
    public boolean isTest(FileObject fo) {
        return (hasAnnotation(fo, TEST_ANNOTATIONS, false) || isInstanceOf(fo, TEST_CLASSES, false)); // NOI18N
    }

    @Override
    public MethodInfo resolveMethodAtPosition(FileObject fo, final int position) {
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        // Final holder of resolved method
        final MethodInfo[] resolvedMethod = new MethodInfo[1];

        // Resolve the method
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController ci)
                        throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePath path = ci.getTreeUtilities().pathFor(position);

                    if (path == null) {
                        return;
                    }

//                    Use the following code to enable javac hierarchy traversal
//                    Element element = null;
//                    while (path != null) {
//                        element = ci.getTrees().getElement(path);
//                        if (element != null && ((element.getKind() == ElementKind.METHOD) || (element.getKind() == ElementKind.CONSTRUCTOR) || (element.getKind() == ElementKind.STATIC_INIT))) {
//                            break;
//                        }
//                        path = path.getParentPath();
//                    }

                    Element element = ci.getTrees().getElement(path);

                    if ((element != null) && ((element.getKind() == ElementKind.METHOD) || (element.getKind() == ElementKind.CONSTRUCTOR) || (element.getKind() == ElementKind.STATIC_INIT))) {
                        ExecutableElement method = (ExecutableElement) element;
                        String vmClassName = ElementUtilities.getBinaryName((TypeElement) method.getEnclosingElement());
                        String vmMethodName = getVMMethodName(method);
                        String vmMethodSignature = ElementUtilitiesEx.getBinaryName(method, ci);
                        resolvedMethod[0] = new MethodInfoImpl(vmClassName, method.getSimpleName().toString(), vmMethodName, vmMethodSignature, isExecutable(method));
                    }

                }
            }, true);
        } catch (IOException ioex) {
            ProfilerLogger.log(ioex);
            return null;
        }

        return resolvedMethod[0];
    }

    @Override
    public ClassInfo resolveClassAtPosition(FileObject fo, final int position, final boolean resolveField) {
        // Get JavaSource for given FileObject
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        // Final holder of resolved method
        final ClassInfo[] resolvedClass = new ClassInfo[1];

        // Resolve the method
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController ci)
                        throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePath path = ci.getTreeUtilities().pathFor(position);

                    if (path == null) {
                        return;
                    }

                    Element element = ci.getTrees().getElement(path);

                    if (element == null) {
                        return;
                    }

                    // resolve class/enum at cursor
                    if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.ENUM)) {
                        TypeElement jclass = (TypeElement) element;
                        String vmClassName = ElementUtilities.getBinaryName(jclass);
                        resolvedClass[0] = new ClassInfoImpl(jclass.getSimpleName().toString(), jclass.getQualifiedName().toString(), vmClassName);

                        return;

                    }

                    // resolve field at cursor

                    if (resolveField && ((element.getKind() == ElementKind.FIELD) || (element.getKind() == ElementKind.LOCAL_VARIABLE)) && (element.asType().getKind() == TypeKind.DECLARED)) {
                        TypeMirror jclassMirror = ci.getTypes().erasure(element.asType());
                        TypeElement jclass = (TypeElement)ci.getTypes().asElement(jclassMirror);
                        String vmClassName = ElementUtilities.getBinaryName(jclass);
                        resolvedClass[0] = new ClassInfoImpl(jclass.getSimpleName().toString(), jclass.getQualifiedName().toString(), vmClassName);
                        return;

                    }


                }
            }, true);
        } catch (IOException ioex) {
            ProfilerLogger.log(ioex);
            ioex.printStackTrace();

            return null;
        }

        return resolvedClass[0];
    }

    private static String getVMMethodName(ExecutableElement method) {
        // Constructor returns <init>
        // Static initializer returns <clinit>
        // Method returns its simple name
        return method.getSimpleName().toString();
    }
    
    private static String getSimpleClassName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf(".");
        if (lastDot == -1) {
            return qualifiedName;
        }
        String simple = qualifiedName.substring(lastDot + 1);
        simple = simple.replace("$", ".");
        return simple;
    }
    
    private static boolean isExecutable(ExecutableElement method) {
        if (method == null) {
            return false;
        }

        Set<Modifier> modifiers = method.getModifiers();

        if (modifiers.contains(Modifier.ABSTRACT) || modifiers.contains(Modifier.NATIVE)) {
            return false;
        }

        return true;
    }
}
