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
package org.netbeans.modules.spring.beans.editor;

import org.netbeans.modules.spring.beans.utils.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 * Finds the actual class which is implementing the specified bean. 
 * 
 * Uses a recursive logic as follows
 * 
 * <pre>
 * findImplementationClass(bean) :
 *   type = null;
 * 
 *   if(bean has parent) {
 *      getMergedAttributes(parent); // walk the ancestor chain and find all attributes
 *   }
 * 
 *   if(bean has factory-bean attribute defined) {
 *      type = findImplementationClass(factory-bean);
 *   } else if(bean has class attribute defined) {
 *      type = class attrib value;
 *   } else if(bean has parent attribute) {
 *      type = findImplementationClass(parent);
 *   }
 * 
 *   if(bean has factory-method) {
 *      type = findFactoryMethodReturnType(type, factory-method-name);
 *   }
 * 
 *   return type;
 * </pre>
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeanClassFinder {

    private static final String ID_ATTRIB = "id"; // NOI18N
    private static final String NAME_ATTRIB = "name"; // NOI18N
    
    private Node beanNode;
    private FileObject fileObject;
    private Set<String> walkedBeanNames;  
    private Document document;
    private String startBeanName;
    private SpringBean startBean;

    public BeanClassFinder(Node beanNode, Document document) {
        this.beanNode = beanNode;
        this.document = document;
        this.fileObject = NbEditorUtilities.getFileObject(document);
        this.walkedBeanNames = new HashSet<String>();
        this.startBean = SpringXMLConfigEditorUtils.getMergedBean(beanNode, document);
        startBeanName = getBeanIdOrName(beanNode);
    }

    public String findImplementationClass() {
        walkedBeanNames.add(startBeanName);
        return findImplementationClass(startBean);
    }

    private String findImplementationClass(SpringBean logicalBean) {
        String implClass = null;
        boolean staticFlag = false;
        
        if (StringUtils.hasText(logicalBean.getFactoryBean())) {
            implClass = findImplementationClass(logicalBean.getFactoryBean());
            staticFlag = false;
        } else if (StringUtils.hasText(logicalBean.getClassName())) {
            implClass = logicalBean.getClassName();
            staticFlag = true;
        }

        if(logicalBean.getFactoryMethod() != null) {
            implClass = getFactoryMethodReturnTypeName(implClass, logicalBean.getFactoryMethod(), staticFlag);
        }
        
        return implClass;
    }

    private String findImplementationClass(final String beanName) {
        if(walkedBeanNames.contains(beanName)) {
            // possible circular dep - bail out
            return null;
        }
        
        final String[] clazz = {null};
        try {
            SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
            if (model == null) {
                return null;
            }

            model.runReadAction(new Action<SpringBeans>() {

                public void run(SpringBeans springBeans) {
                    SpringBean bean = springBeans.findBean(beanName);
                    bean = SpringXMLConfigEditorUtils.getMergedBean(bean, document);
                    if(bean == null) {
                        return;
                    }
                    
                    String beanName = getBeanIdOrName(bean);
                    walkedBeanNames.add(beanName);
                    clazz[0] = findImplementationClass(bean);
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return clazz[0];
    }

    /**
     * Tries to search for a factory method with a specified name on the class.
     * Due to current limitations of the model, if more than one factory method
     * is found, we are not able to disambiguate based on the parameter types of
     * the factory method. 
     * 
     * Hence we return a null in such a scenario.
     * 
     */
    private String getFactoryMethodReturnTypeName(final String implClass, final String factoryMethodName, 
            final boolean staticFlag) {
        final String[] retVal = {null};

        if(!StringUtils.hasText(factoryMethodName)) {
            return null;
        }
        
        try {
            JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(document);
            if (js == null) {
                return null;
            }

            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    TypeElement te = SpringXMLConfigEditorUtils.findClassElementByBinaryName(implClass, cc);
                    if (te == null) {
                        return;
                    }

                    FactoryMethodFinder factoryMethodFinder = new FactoryMethodFinder(te, factoryMethodName, staticFlag, cc.getElementUtilities());
                    List<ExecutableElement> methods = factoryMethodFinder.findMethods();
                    if (methods.size() != 1) {
                        return;
                    }

                    ExecutableElement method = methods.get(0);
                    if (method.getReturnType().getKind() != TypeKind.DECLARED) {
                        return;
                    }

                    DeclaredType dt = (DeclaredType) method.getReturnType();
                    retVal[0] = ElementUtilities.getBinaryName((TypeElement) dt.asElement());
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return retVal[0];
    }

    private static class FactoryMethodFinder {
        private TypeElement te;
        private String factoryMethodName;
        private boolean staticFlag;
        private ElementUtilities eu;

        public FactoryMethodFinder(TypeElement te, String factoryMethodName, boolean staticFlag, ElementUtilities eu) {
            this.te = te;
            this.factoryMethodName = factoryMethodName;
            this.staticFlag = staticFlag;
            this.eu = eu;
        }
        
        public List<ExecutableElement> findMethods() {
            Iterable<? extends Element>  list = eu.getMembers(te.asType(), new ElementUtilities.ElementAcceptor() {

                public boolean accept(Element e, TypeMirror type) {
                    if (e.getKind() == ElementKind.METHOD) {
                        TypeElement te = (TypeElement) e.getEnclosingElement();
                        if (te.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                            return false;
                        }

                        // match name
                        if (!e.getSimpleName().toString().equals(factoryMethodName)) {
                            return false;
                        }

                        ExecutableElement method = (ExecutableElement) e;
                        // match static
                        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                        if (isStatic != staticFlag) {
                            return false;
                        }
                        return true;
                    }
                    
                    return false;
                }
            });
            
            List<ExecutableElement> retList = new ArrayList<ExecutableElement>();
            for(Element e : list) {
                ExecutableElement ee = (ExecutableElement) e;
                retList.add(ee);
            }
            
            return retList;
        }
    }
    
    private String getBeanIdOrName(SpringBean bean) {
        if(bean.getId() != null) {
            return bean.getId();
        }
        
        if(bean.getNames().size() > 0) {
            return bean.getNames().get(0);
        }
        
        return null;
    }
    
    private String getBeanIdOrName(Node beanNode) {
        if(SpringXMLConfigEditorUtils.hasAttribute(beanNode, ID_ATTRIB)) {
            return SpringXMLConfigEditorUtils.getAttribute(beanNode, ID_ATTRIB);
        }
        
        if(SpringXMLConfigEditorUtils.hasAttribute(beanNode, NAME_ATTRIB)) {
            String names = SpringXMLConfigEditorUtils.getAttribute(beanNode, NAME_ATTRIB);
            return StringUtils.tokenize(names, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS).get(0);
        }
        
        return null;
    }
}
