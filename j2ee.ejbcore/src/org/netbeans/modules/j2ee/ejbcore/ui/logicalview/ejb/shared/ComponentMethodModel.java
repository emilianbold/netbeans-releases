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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.openide.util.Exceptions;

// TODO: RETOUCHE listening on source
public abstract class ComponentMethodModel extends Children.Keys<MethodModel> /*implements MDRChangeListener*/ {
    
    private final JavaSource javaSource;
    private final String homeInterface;
    private Collection interfaces;
    private String implBean;
    
    public ComponentMethodModel(JavaSource javaSource, String implBean, Collection interfaces, String homeInterface) {
        this.javaSource = javaSource;
        this.homeInterface = homeInterface;
        this.implBean = implBean;
        this.interfaces = interfaces;
    }
    
    private void updateKeys() {
        final ComponentMethodViewStrategy viewStrategy = createViewStrategy();
        final List<MethodModel> keys = new ArrayList<MethodModel>();
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(final CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    final ElementUtilities elementUtilities = controller.getElementUtilities();
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (String className : getInterfaces()) {
                        TypeElement intf = elements.getTypeElement(className);
                        
                        if (intf != null) {
                            // from home interface we want only direct methods
                            if (className.equals(homeInterface)) {
                                for (ExecutableElement executableElement : ElementFilter.methodsIn(intf.getEnclosedElements())) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, executableElement);
                                    keys.add(methodModel);
                                }
                            } else {
                                Iterable<? extends Element> methods = elementUtilities.getMembers(intf.asType(), new ElementAcceptor() {
                                    public boolean accept(Element e, TypeMirror type) {
                                        TypeElement parent = elementUtilities.enclosingTypeElement(e);
                                        boolean isInInterface = ElementKind.INTERFACE == parent.getKind();
                                        boolean isFromJavaxEjb = parent.getQualifiedName().toString().startsWith("javax.ejb."); // NOI18N
                                        return isInInterface && !isFromJavaxEjb && ElementKind.METHOD == e.getKind();
                                    }
                                });
                                for (Element method : methods) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, (ExecutableElement) method);
                                    keys.add(methodModel);
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
            public void run() {
                setKeys(keys);
            }
        });
    }
    
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
//        Iterator iterator = interfaces.iterator();
//        while(iterator.hasNext()){
//            JavaClass intf = (JavaClass)iterator.next();
//            if (intf != null) {
//                ((MDRChangeSource) intf).addListener(this);  
//            }
//        }
    }
    
    private void removeListeners() {
//        if (interfaces == null)
//            return;
//        Iterator iterator = interfaces.iterator();
//        while(iterator.hasNext()){
//            JavaClass intf = (JavaClass)iterator.next();
//            ((MDRChangeSource) intf).removeListener(this);  
//        }
    }
    
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
        return null;
    }
    
    /*
     * Subclasses have to override this if no-arg constructor is used
     */
    protected String getImplBean(){
       return null; 
    }

//    public void change(MDRChangeEvent evt) {
//        // TODO: how to make following old code working?
////        if (ElementProperties.PROP_VALID.equals(evt.getPropertyName())) {
////            Object value = evt.getNewValue();
////            if ((value instanceof Boolean) && !((Boolean) value).booleanValue()) {
////                removeListeners();
////                interfaces = getInterfaces();
////                registerListeners();
////            }
////        }
//        updateKeys();
//    }
    
    public abstract ComponentMethodViewStrategy  createViewStrategy();
    
    protected Node[] createNodes(MethodModel key) {
        ComponentMethodViewStrategy cmvs = createViewStrategy();
        return new Node[] { new MethodNode(javaSource, key, implBean, getInterfaces(), cmvs) };
    }

}
