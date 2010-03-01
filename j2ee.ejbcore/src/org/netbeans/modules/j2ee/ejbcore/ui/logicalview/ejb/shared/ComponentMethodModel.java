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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypesEvent;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.openide.util.Exceptions;

public abstract class ComponentMethodModel extends Children.Keys<MethodModel> {
    
    private final ClasspathInfo cpInfo;
    private final String homeInterface;
    private Collection<String> interfaces;
    private String implBean;
    private final ClassIndexListener classIndexListener;
    
    public ComponentMethodModel(ClasspathInfo cpInfo, String implBean, Collection<String> interfaces, String homeInterface) {
        this.cpInfo = cpInfo;
        this.homeInterface = homeInterface;
        this.implBean = implBean;
        this.interfaces = interfaces;
        this.classIndexListener = new ClassIndexListenerImpl();
    }
    
    private void updateKeys() {
        final ComponentMethodViewStrategy viewStrategy = createViewStrategy();
        final List<MethodModel> keys = new ArrayList<MethodModel>();
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(final CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    final ElementUtilities elementUtilities = controller.getElementUtilities();
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (String className : getInterfaces()) {
                        final TypeElement intf = elements.getTypeElement(className);

                        if (intf != null) {
                            // from home interface we want only direct methods
                            if (className.equals(homeInterface)) {
                                for (ExecutableElement executableElement : ElementFilter.methodsIn(intf.getEnclosedElements())) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, executableElement);
                                    if (methodModel != null){
                                        keys.add(methodModel);
                                    }
                                }
                            } else if(intf.getKind() == ElementKind.CLASS) {
                                Iterable<? extends Element> methods = elementUtilities.getMembers(intf.asType(), new ElementAcceptor() {
                                    @Override
                                    public boolean accept(Element e, TypeMirror type) {
                                        TypeElement parent = elementUtilities.enclosingTypeElement(e);
                                        return ElementKind.METHOD == e.getKind() && 
                                               e.getEnclosingElement().equals(intf) &&
                                               e.getModifiers().contains(Modifier.PUBLIC);
                                    }
                                });
                                for (Element method : methods) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, (ExecutableElement) method);
                                    if (methodModel != null && !keys.contains(methodModel)){
                                        keys.add(methodModel);
                                    }
                                }
                            } else {
                                Iterable<? extends Element> methods = elementUtilities.getMembers(intf.asType(), new ElementAcceptor() {
                                    @Override
                                    public boolean accept(Element e, TypeMirror type) {
                                        TypeElement parent = elementUtilities.enclosingTypeElement(e);
                                        boolean isInInterface = ElementKind.INTERFACE == parent.getKind();
                                        boolean isFromJavaxEjb = parent.getQualifiedName().toString().startsWith("javax.ejb."); // NOI18N
                                        return isInInterface && !isFromJavaxEjb && ElementKind.METHOD == e.getKind();
                                    }
                                });
                                for (Element method : methods) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, (ExecutableElement) method);
                                    if (methodModel != null){
                                        keys.add(methodModel);
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setKeys(keys);
            }
        });
    }
    
    @Override
    protected void addNotify() {
        if(interfaces == null){
            interfaces = getInterfaces();
        }
        assert(interfaces != null);
        if(implBean == null){
            implBean = getImplBean();
        }
        super.addNotify();
        registerListeners();
        updateKeys();
    }
    
    private void registerListeners() {
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassIndex classIndex = controller.getClasspathInfo().getClassIndex();
                    classIndex.addClassIndexListener(classIndexListener);
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void removeListeners() {
        if (interfaces == null) {
            return;
        }
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassIndex classIndex = controller.getClasspathInfo().getClassIndex();
                    classIndex.removeClassIndexListener(classIndexListener);
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    @Override
    protected void removeNotify() {
        if (interfaces == null)
            return;
        setKeys(Collections.<MethodModel>emptySet());
        removeListeners();
        super.removeNotify();
    }
    
    /*
     * Subclasses have to override this if no-arg constructor is used
     */
    protected Collection<String> getInterfaces(){
        return interfaces;
    }
    
    /*
     * Subclasses have to override this if no-arg constructor is used
     */
    protected String getImplBean(){
       return null; 
    }

    public abstract ComponentMethodViewStrategy  createViewStrategy();
    
    protected Node[] createNodes(MethodModel key) {
        ComponentMethodViewStrategy cmvs = createViewStrategy();
        return new Node[] { new MethodNode(cpInfo, key, implBean, getInterfaces(), cmvs) };
    }

    private class ClassIndexListenerImpl implements ClassIndexListener {

        @Override
        public void typesAdded(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void typesRemoved(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void typesChanged(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void rootsAdded(RootsEvent event) {
            // ignore
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
            // ignore
        }

        private void handleTypes(TypesEvent event) {
            for (ElementHandle<TypeElement> elementHandle : event.getTypes()) {
                if (interfaces.contains(elementHandle.getQualifiedName())) {
                    updateKeys();
                    return;
                }
            }
        }
        
    }
    
}
