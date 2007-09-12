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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.javaee.sunresources.tool.annotation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode.ResourceType;

/**
 * @author echou
 *
 */
public class DependencyAnnotation {
    
    private ResourceAggregator resAggregator;
    
    public DependencyAnnotation(ResourceAggregator resAggregator) {
        this.resAggregator = resAggregator;
    }
    
    /**
     * handle dependency injection of EJB and Resource type
     * 
     * @param node
     * @param cls
     */
    /*
    public void fillDependencyInfo(CMapNode node, Class cls) {
        // first check the class level annotations
        handleEJBDep(node, cls);
        handleResourceDep(node, cls);
        handleWebServiceRef(node, cls);
        
        // check for field level annotations
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            handleEJBDep(node, fields[i]);
            handleResourceDep(node, fields[i]);
            handleWebServiceRef(node, fields[i]);
        }
        
        // check for method level annotations
        Method[] methods = cls.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            handleEJBDep(node, methods[i]);
            handleResourceDep(node, methods[i]);
            handleWebServiceRef(node, methods[i]);
        }
    }
     **/
    
    /*
     * fill dependency info using NetBeans ClassFile API
     */
    public void fillDependencyInfo(CMapNode node, ClassFile cls) {
        // first check the class level annotations
        handleEJBDep(node, cls);
        handleResourceDep(node, cls);
        handleWebServiceRef(node, cls);
        
        // check for field level annotations
        Collection<org.netbeans.modules.classfile.Variable> variables = 
                (Collection<org.netbeans.modules.classfile.Variable>) cls.getVariables();
        for (Iterator<org.netbeans.modules.classfile.Variable> iter = variables.iterator();
            iter.hasNext(); ) {
            org.netbeans.modules.classfile.Variable variable = iter.next();
            handleEJBDep(node, variable);
            handleResourceDep(node, variable);
            handleWebServiceRef(node, variable);
        }
        
        // check for method level annotations
        Collection<org.netbeans.modules.classfile.Method> methods = 
                (Collection<org.netbeans.modules.classfile.Method>) cls.getMethods();
        for (Iterator<org.netbeans.modules.classfile.Method> iter = methods.iterator();
            iter.hasNext(); ) {
            org.netbeans.modules.classfile.Method method = iter.next();
            handleEJBDep(node, method);
            handleResourceDep(node, method);
            handleWebServiceRef(node, method);
        }
    }
    
    /*
    private void handleEJBDep(CMapNode node, AnnotatedElement ae) {
        // check for EJB or EJBs annotation
        if (ae.isAnnotationPresent(EJB.class)) {
            processEJBDep(node, (EJB) ae.getAnnotation(EJB.class), ae);
        } else if (ae.isAnnotationPresent(EJBs.class)) {
            EJBs ejbs = (EJBs) ae.getAnnotation(EJBs.class);
            EJB[] ejbArr = ejbs.value();
            for (int i = 0; i < ejbArr.length; i++) {
                processEJBDep(node, ejbArr[i], ae);
            }
        }
    }
     **/
    
    private void handleEJBDep(CMapNode node, ClassFile cls) {
        // check for EJB or EJBs annotation
        ClassName ejbCN = ClassName.getClassName(JavaEEAnnotationProcessor.EJB_CLASSTYPE);
        ClassName ejbsCN = ClassName.getClassName(JavaEEAnnotationProcessor.EJBS_CLASSTYPE);
        if (cls.isAnnotationPresent(ejbCN)) {
            processEJBDep(node, new EJBAnnoWrapper(cls.getAnnotation(ejbCN)), cls);
        } else if (cls.isAnnotationPresent(ejbsCN)) {
            EJBsAnnoWrapper ejbs = new EJBsAnnoWrapper(cls.getAnnotation(ejbsCN));
            EJBAnnoWrapper[] ejbArr = ejbs.value();
            for (int i = 0; i < ejbArr.length; i++) {
                processEJBDep(node, ejbArr[i], cls);
            }
        }
    }
    
    private void handleEJBDep(CMapNode node, org.netbeans.modules.classfile.Field field) {
        // check for EJB or EJBs annotation
        ClassName ejbCN = ClassName.getClassName(JavaEEAnnotationProcessor.EJB_CLASSTYPE);
        ClassName ejbsCN = ClassName.getClassName(JavaEEAnnotationProcessor.EJBS_CLASSTYPE);
        if (field.isAnnotationPresent(ejbCN)) {
            processEJBDep(node, new EJBAnnoWrapper(field.getAnnotation(ejbCN)), field);
        } else if (field.isAnnotationPresent(ejbsCN)) {
            EJBsAnnoWrapper ejbs = new EJBsAnnoWrapper(field.getAnnotation(ejbsCN));
            EJBAnnoWrapper[] ejbArr = ejbs.value();
            for (int i = 0; i < ejbArr.length; i++) {
                processEJBDep(node, ejbArr[i], field);
            }
        }
    }
    
    /*
    private void processEJBDep(CMapNode node, EJB ejbAnno, 
            AnnotatedElement ae) {
        EJBDepend ejbDepend = new EJBDepend(node);
        Class beanIntf = ejbAnno.beanInterface();
        if (beanIntf != Object.class) {
            ejbDepend.setTargetIntfName(beanIntf.getName());
        } else {
            if (ae instanceof Field) {
                ejbDepend.setTargetIntfName( ((Field) ae).getType().getName());
            } else if (ae instanceof Method) {
                // verify if this is a setter method, and print out warning if it isn't
                Method m = (Method) ae;
                if (!isSetter(m)) {
                    System.out.println("found @EJB annotation declared on a non-setter method");
                    return;
                }
                ejbDepend.setTargetIntfName(m.getParameterTypes()[0].getName());
            }
        }
        node.getEjbDepends().add(ejbDepend);
    }
     **/

    private void processEJBDep(CMapNode node, EJBAnnoWrapper ejbAnno, ClassFile cls) {
        EJBDepend ejbDepend = new EJBDepend(node);
        String beanIntf = ejbAnno.beanInterface();
        if (!beanIntf.equals("java.lang.Object")) { // NOI18N
            ejbDepend.setTargetIntfName(beanIntf);
        }
        node.getEjbDepends().add(ejbDepend);
    }
    
    private void processEJBDep(CMapNode node, EJBAnnoWrapper ejbAnno, 
            org.netbeans.modules.classfile.Field field) {
        EJBDepend ejbDepend = new EJBDepend(node);
        String beanIntf = ejbAnno.beanInterface();
        if (!beanIntf.equals("java.lang.Object")) { // NOI18N
            ejbDepend.setTargetIntfName(beanIntf);
        } else {
            if (field instanceof org.netbeans.modules.classfile.Variable) {
                ejbDepend.setTargetIntfName(
                        ClassName.getClassName(field.getDescriptor()).getExternalName());
            } else if (field instanceof org.netbeans.modules.classfile.Method) {
                // verify if this is a setter method, and print out warning if it isn't
                org.netbeans.modules.classfile.Method m = 
                        (org.netbeans.modules.classfile.Method) field;
                if (!isSetter(m)) {
                    System.out.println("found @EJB annotation declared on a non-setter method");
                    return;
                }
                ejbDepend.setTargetIntfName(
                    ClassName.getClassName(
                        ((org.netbeans.modules.classfile.Parameter) m.getParameters().get(0)).getDescriptor()).getExternalName());
            }
        }
        node.getEjbDepends().add(ejbDepend);
    }

    /*
    private void handleResourceDep(CMapNode node, AnnotatedElement ae) {
        // check for Resource or Resources annotation
        if (ae.isAnnotationPresent(Resource.class)) {
            processResourceDep(node, (Resource) ae.getAnnotation(Resource.class), ae);
        } else if (ae.isAnnotationPresent(Resources.class)) {
            Resources resources = (Resources) ae.getAnnotation(Resources.class);
            Resource[] resourceArr = resources.value();
            for (int i = 0; i < resourceArr.length; i++) {
                processResourceDep(node, resourceArr[i], ae);
            }
        }
    }
     **/
    
    private void handleResourceDep(CMapNode node, ClassFile cls) {
        // check for Resource or Resources annotation
        ClassName resourceCN = ClassName.getClassName(JavaEEAnnotationProcessor.RESOURCE_CLASSTYPE);
        ClassName resourcesCN = ClassName.getClassName(JavaEEAnnotationProcessor.RESOURCES_CLASSTYPE);
        if (cls.isAnnotationPresent(resourceCN)) {
            processResourceDep(node, new ResourceAnnoWrapper(cls.getAnnotation(resourceCN)), cls);
        } else if (cls.isAnnotationPresent(resourcesCN)) {
            ResourcesAnnoWrapper resources = new ResourcesAnnoWrapper(cls.getAnnotation(resourcesCN));
            ResourceAnnoWrapper[] resourceArr = resources.value();
            for (int i = 0; i < resourceArr.length; i++) {
                processResourceDep(node, resourceArr[i], cls);
            }
        }
    }

    private void handleResourceDep(CMapNode node, org.netbeans.modules.classfile.Field field) {
        // check for Resource or Resources annotation
        ClassName resourceCN = ClassName.getClassName(JavaEEAnnotationProcessor.RESOURCE_CLASSTYPE);
        ClassName resourcesCN = ClassName.getClassName(JavaEEAnnotationProcessor.RESOURCES_CLASSTYPE);
        if (field.isAnnotationPresent(resourceCN)) {
            processResourceDep(node, new ResourceAnnoWrapper(field.getAnnotation(resourceCN)), field);
        } else if (field.isAnnotationPresent(resourcesCN)) {
            ResourcesAnnoWrapper resources = new ResourcesAnnoWrapper(field.getAnnotation(resourcesCN));
            ResourceAnnoWrapper[] resourceArr = resources.value();
            for (int i = 0; i < resourceArr.length; i++) {
                processResourceDep(node, resourceArr[i], field);
            }
        }
    }
    
    /*
    private void processResourceDep(CMapNode node, Resource resAnno, 
            AnnotatedElement ae) {
        Class resType = resAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        
        // process resType
        if (resType != Object.class) {
            resDepend.setTargetResType(resType.getName());
        } else {
            if (ae instanceof Field) {
                resDepend.setTargetResType( ((Field) ae).getType().getName());
            } else if (ae instanceof Method) {
                // verify if this is a setter method, and print out warning if it isn't
                Method m = (Method) ae;
                if (!isSetter(m)) {
                    System.out.println("found @Resource annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResType(m.getParameterTypes()[0].getName());
            }
        }
        
        if (isSimpleEnvEntry(resDepend.getTargetResType())) {
            // not the type of Resource we are looking for
            return; 
        }
        if (isJmsResource(resDepend.getTargetResType())) { 
            resDepend.setType(ResourceType.JMS);
        } else if (isOtherResource(resDepend.getTargetResType())) {
            resDepend.setType(ResourceType.OTHER);
        } else {
            // encountered an unknown resource type, skip it
            return;
        }
        
        // process redJndiName
        if (!resAnno.name().equals("")) {
            resDepend.setTargetResJndiName(resAnno.name());
        } else {
            if (ae instanceof Field) {
                resDepend.setTargetResJndiName( ((Field) ae).getName() );
            } else if (ae instanceof Method) {
                Method m = (Method) ae;
                if (!isSetter(m)) {
                    System.out.println("found @Resource annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResJndiName(getJavaBeanPropName(m));
            }
        }
        
        // process mappedName
        resDepend.setMappedName(resAnno.mappedName());
        
        node.getResDepends().add(resDepend);
    }
     **/
    
    private void processResourceDep(CMapNode node, ResourceAnnoWrapper resAnno, 
            ClassFile cls) {
        String resType = resAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        
        // process resType
        if (!resType.equals("java.lang.Object")) { // NOI18N
            resDepend.setTargetResType(resType);
        }
        
        if (isSimpleEnvEntry(resDepend.getTargetResType())) {
            // not the type of Resource we are looking for
            return; 
        }
        
        // process redJndiName
        if (!resAnno.name().equals("")) { // NOI18N
            resDepend.setTargetResJndiName(resAnno.name());
        }
        
        // process mappedName
        resDepend.setMappedName(resAnno.mappedName());
        
        node.getResDepends().add(resDepend);
        
        // handle server resource
        String sourceName = cls.getName().getPackage().replace('.', '/') + "/" + cls.getSourceFileName(); // NOI18N
        handleServerResource(resDepend, sourceName);
    }
    
    private void processResourceDep(CMapNode node, ResourceAnnoWrapper resAnno, 
            org.netbeans.modules.classfile.Field field) {
        String resType = resAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        
        // process resType
        if (!resType.equals("java.lang.Object")) { // NOI18N
            resDepend.setTargetResType(resType);
        } else {
            if (field instanceof org.netbeans.modules.classfile.Variable) {
                resDepend.setTargetResType(
                        ClassName.getClassName(field.getDescriptor()).getExternalName());
            } else if (field instanceof org.netbeans.modules.classfile.Method) {
                // verify if this is a setter method, and print out warning if it isn't
                org.netbeans.modules.classfile.Method m = 
                        (org.netbeans.modules.classfile.Method) field;
                if (!isSetter(m)) {
                    System.out.println("found @Resource annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResType(
                    ClassName.getClassName(
                        ((org.netbeans.modules.classfile.Parameter) m.getParameters().get(0)).getDescriptor()).getExternalName());
            }
        }
        
        if (isSimpleEnvEntry(resDepend.getTargetResType())) {
            // not the type of Resource we are looking for
            return; 
        }
        
        // process redJndiName
        if (!resAnno.name().equals("")) { // NOI18N
            resDepend.setTargetResJndiName(resAnno.name());
        } else {
            if (field instanceof org.netbeans.modules.classfile.Variable) {
                resDepend.setTargetResJndiName(field.getName());
            } else if (field instanceof org.netbeans.modules.classfile.Method) {
                org.netbeans.modules.classfile.Method m = 
                        (org.netbeans.modules.classfile.Method) field;
                if (!isSetter(m)) {
                    System.out.println("found @Resource annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResJndiName(getJavaBeanPropName(m));
            }
        }
        
        // process mappedName
        resDepend.setMappedName(resAnno.mappedName());
        
        node.getResDepends().add(resDepend);
        
        // handle server resource
        String sourceName = field.getClassFile().getName().getPackage().replace('.', '/') + "/" + field.getClassFile().getSourceFileName(); // NOI18N
        handleServerResource(resDepend, sourceName);
    }
    
    private void handleServerResource(ResourceDepend resDepend, String sourceName) {
        if (isAdminObjectResource(resDepend.getTargetResType())) {
            // only for JMS destination type resources
            resDepend.setType(ResourceType.JMS);
                    
            // add to server resources jmsra
            ResourceAggregator.ResourceEntry resourceEntry = 
                    resAggregator.getAdminObjectResourceEntry(resDepend.getMappedName());
            if (resourceEntry == null) {
                Properties adminObjProps = new Properties();
                adminObjProps.setProperty("Name", resDepend.getTargetResJndiName()); // NOI18N
                resourceEntry = resAggregator.addAdminObjectResourceEntry(resDepend.getMappedName(), resDepend.getTargetResType(), "jmsra", adminObjProps); // NOI18N
            } else {
                resourceEntry.obsolete = false;
                if (resourceEntry.orphanStatus == ResourceAggregator.OrphanStatus.FILE_ONLY) {
                    resourceEntry.orphanStatus = ResourceAggregator.OrphanStatus.BOTH;
                }
            }
            
            resourceEntry.addResourceUsage(sourceName, null);
            
        } else if (isConnectorResource(resDepend.getTargetResType())) {
            resDepend.setType(ResourceType.OTHER);
            
            // add to server resources jmsra
            ResourceAggregator.ResourceEntry resourceEntry =
                    resAggregator.getConnectorResourceResourceEntry(resDepend.getMappedName());
            if (resourceEntry == null) {
                resourceEntry = resAggregator.addConnectorResourceEntry(resDepend.getMappedName(), resDepend.getTargetResType(), "jmsra"); // NOI18N
            } else {
                resourceEntry.obsolete = false;
                if (resourceEntry.orphanStatus == ResourceAggregator.OrphanStatus.FILE_ONLY) {
                    resourceEntry.orphanStatus = ResourceAggregator.OrphanStatus.BOTH;
                }
            }
            
            resourceEntry.addResourceUsage(sourceName, null);
            
        } else if (isJdbcResource(resDepend.getTargetResType())) {
            resDepend.setType(ResourceType.OTHER);
            
            // add to server resources jmsra
            ResourceAggregator.ResourceEntry resourceEntry =
                    resAggregator.getJdbcResourceResourceEntry(resDepend.getTargetResJndiName());
            if (resourceEntry == null) {
                resourceEntry = resAggregator.addJdbcResourceEntry(
                        resDepend.getTargetResJndiName(), 
                        resDepend.getTargetResType(), 
                        "org.apache.derby.jdbc.ClientDataSource"); // NOI18N
            } else {
                resourceEntry.obsolete = false;
                if (resourceEntry.orphanStatus == ResourceAggregator.OrphanStatus.FILE_ONLY) {
                    resourceEntry.orphanStatus = ResourceAggregator.OrphanStatus.BOTH;
                }
            }
            
            resourceEntry.addResourceUsage(sourceName, null);
            
        } else if (isOtherResource(resDepend.getTargetResType())) {
            resDepend.setType(ResourceType.OTHER);
        } else {
            // encountered an unknown resource type, skip it
            
        }
    }
    
    /*
    private void handleWebServiceRef(CMapNode node, AnnotatedElement ae) {
        // check for WebServiceRef or WebServiceRefs annotation
        if (ae.isAnnotationPresent(WebServiceRef.class)) {
            processWebServiceRef(node, (WebServiceRef) ae.getAnnotation(WebServiceRef.class), ae);
        } else if (ae.isAnnotationPresent(WebServiceRefs.class)) {
            WebServiceRefs wsRefs = (WebServiceRefs) ae.getAnnotation(WebServiceRefs.class);
            WebServiceRef[] wsRefArr = wsRefs.value();
            for (int i = 0; i < wsRefArr.length; i++) {
                processWebServiceRef(node, wsRefArr[i], ae);
            }
        }
    }
     **/
    
    private void handleWebServiceRef(CMapNode node, ClassFile cls) {
        // check for WebServiceRef or WebServiceRefs annotation
        ClassName webserviceRefCN = ClassName.getClassName(JavaEEAnnotationProcessor.WEBSERVICEREF_CLASSTYPE);
        ClassName webserviceRefsCN = ClassName.getClassName(JavaEEAnnotationProcessor.WEBSERVICEREFS_CLASSTYPE);
        if (cls.isAnnotationPresent(webserviceRefCN)) {
            processWebServiceRef(node, new WebServiceRefAnnoWrapper(cls.getAnnotation(webserviceRefCN)), cls);
        } else if (cls.isAnnotationPresent(webserviceRefsCN)) {
            WebServiceRefsAnnoWrapper wsRefs = new WebServiceRefsAnnoWrapper(cls.getAnnotation(webserviceRefsCN));
            WebServiceRefAnnoWrapper[] wsRefArr = wsRefs.value();
            for (int i = 0; i < wsRefArr.length; i++) {
                processWebServiceRef(node, wsRefArr[i], cls);
            }
        }
    }
    
    private void handleWebServiceRef(CMapNode node, org.netbeans.modules.classfile.Field field) {
        // check for WebServiceRef or WebServiceRefs annotation
        ClassName webserviceRefCN = ClassName.getClassName(JavaEEAnnotationProcessor.WEBSERVICEREF_CLASSTYPE);
        ClassName webserviceRefsCN = ClassName.getClassName(JavaEEAnnotationProcessor.WEBSERVICEREFS_CLASSTYPE);
        if (field.isAnnotationPresent(webserviceRefCN)) {
            processWebServiceRef(node, new WebServiceRefAnnoWrapper(field.getAnnotation(webserviceRefCN)), field);
        } else if (field.isAnnotationPresent(webserviceRefsCN)) {
            WebServiceRefsAnnoWrapper wsRefs = new WebServiceRefsAnnoWrapper(field.getAnnotation(webserviceRefsCN));
            WebServiceRefAnnoWrapper[] wsRefArr = wsRefs.value();
            for (int i = 0; i < wsRefArr.length; i++) {
                processWebServiceRef(node, wsRefArr[i], field);
            }
        }
    }
    
    /*
    private void processWebServiceRef(CMapNode node, WebServiceRef wsRefAnno, 
            AnnotatedElement ae) {
        Class wsRefType = wsRefAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        resDepend.setType(ResourceType.WEBSERVICE);
        
        // process wsRefType
        String simpleName = "";
        if (wsRefType != Object.class) {
            resDepend.setTargetResType(wsRefType.getName());
            simpleName = wsRefType.getSimpleName();
        } else {
            if (ae instanceof Field) {
                resDepend.setTargetResType( ((Field) ae).getType().getName());
                simpleName = ((Field) ae).getType().getSimpleName();
            } else if (ae instanceof Method) {
                // verify if this is a setter method, and print out warning if it isn't
                Method m = (Method) ae;
                if (!isSetter(m)) {
                    System.out.println("found @WebServiceRef annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResType(m.getParameterTypes()[0].getName());
                simpleName = m.getParameterTypes()[0].getSimpleName();
            }
        }
        
        // process wsRefJndiName
        if (!wsRefAnno.name().equals("")) {
            resDepend.setTargetResJndiName(wsRefAnno.name());
        } else {
            if (ae instanceof Field) {
                resDepend.setTargetResJndiName( ((Field) ae).getName() );
            } else if (ae instanceof Method) {
                Method m = (Method) ae;
                if (!isSetter(m)) {
                    System.out.println("found @WebServiceRef annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResJndiName(getJavaBeanPropName(m));
            }
        }
        
        // process mappedName, if mappedName is not present, then we set
        // it as the simple name of webserviceref type
        String mappedName = wsRefAnno.mappedName();
        if (mappedName.equals("")) {
            resDepend.setMappedName(simpleName);
        } else {
            resDepend.setMappedName(mappedName);
        }
        // process wsdlLocation
        resDepend.getProps().setProperty("wsdlLocation", wsRefAnno.wsdlLocation());
        
        node.getResDepends().add(resDepend);
    }
     **/
    
    private void processWebServiceRef(CMapNode node, WebServiceRefAnnoWrapper wsRefAnno, 
            ClassFile cls) {
        String wsRefType = wsRefAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        resDepend.setType(ResourceType.WEBSERVICE);
        
        // process wsRefType
        String simpleName = "";
        if (!wsRefType.equals("java.lang.Object")) { // NOI18N
            resDepend.setTargetResType(wsRefType);
            simpleName = wsRefType.substring(wsRefType.lastIndexOf(".") + 1); // NOI18N
        }
        
        // process wsRefJndiName
        if (!wsRefAnno.name().equals("")) { // NOI18N
            resDepend.setTargetResJndiName(wsRefAnno.name());
        }
        
        // process mappedName, if mappedName is not present, then we set
        // it as the simple name of webserviceref type
        String mappedName = wsRefAnno.mappedName();
        if (mappedName.equals("")) { // NOI18N
            resDepend.setMappedName(simpleName);
        } else {
            resDepend.setMappedName(mappedName);
        }
        // process wsdlLocation
        resDepend.getProps().setProperty("wsdlLocation", wsRefAnno.wsdlLocation()); // NOI18N
        
        node.getResDepends().add(resDepend);
    }
    
    private void processWebServiceRef(CMapNode node, WebServiceRefAnnoWrapper wsRefAnno, 
            org.netbeans.modules.classfile.Field field) {
        String wsRefType = wsRefAnno.type();
        ResourceDepend resDepend = new ResourceDepend(node);
        resDepend.setType(ResourceType.WEBSERVICE);
        
        // process wsRefType
        String simpleName = ""; // NOI18N
        if (!wsRefType.equals("java.lang.Object")) { // NOI18N
            resDepend.setTargetResType(wsRefType);
            simpleName = wsRefType.substring(wsRefType.lastIndexOf(".") + 1); // NOI18N
        } else {
            if (field instanceof org.netbeans.modules.classfile.Variable) {
                resDepend.setTargetResType(
                        ClassName.getClassName(field.getDescriptor()).getExternalName());
                simpleName = ClassName.getClassName(field.getDescriptor()).getSimpleName();
            } else if (field instanceof org.netbeans.modules.classfile.Method) {
                // verify if this is a setter method, and print out warning if it isn't
                org.netbeans.modules.classfile.Method m = 
                        (org.netbeans.modules.classfile.Method) field;
                if (!isSetter(m)) {
                    System.out.println("found @WebServiceRef annotation declared on a non-setter method");
                    return;
                }
                ClassName paramType = ClassName.getClassName(
                    ((org.netbeans.modules.classfile.Parameter) m.getParameters().get(0)).getDescriptor());
                resDepend.setTargetResType(paramType.getExternalName());
                simpleName = paramType.getSimpleName();
            }
        }
        
        // process wsRefJndiName
        if (!wsRefAnno.name().equals("")) { // NOI18N
            resDepend.setTargetResJndiName(wsRefAnno.name());
        } else {
            if (field instanceof org.netbeans.modules.classfile.Variable) {
                resDepend.setTargetResJndiName(field.getName());
            } else if (field instanceof org.netbeans.modules.classfile.Method) {
                org.netbeans.modules.classfile.Method m = 
                        (org.netbeans.modules.classfile.Method) field;
                if (!isSetter(m)) {
                    System.out.println("found @WebServiceRef annotation declared on a non-setter method");
                    return;
                }
                resDepend.setTargetResJndiName(getJavaBeanPropName(m));
            }
        }
        
        // process mappedName, if mappedName is not present, then we set
        // it as the simple name of webserviceref type
        String mappedName = wsRefAnno.mappedName();
        if (mappedName.equals("")) { // NOI18N
            resDepend.setMappedName(simpleName);
        } else {
            resDepend.setMappedName(mappedName);
        }
        // process wsdlLocation
        resDepend.getProps().setProperty("wsdlLocation", wsRefAnno.wsdlLocation()); // NOI18N
        
        node.getResDepends().add(resDepend);
    }
    
    // check to see if resource type is one of environment entry simple type 
    private boolean isSimpleEnvEntry(String resType) {
        if (resType.equals(String.class.getName()) || 
            resType.equals(Character.class.getName()) ||
            resType.equals(Integer.class.getName()) || 
            resType.equals(Boolean.class.getName()) ||
            resType.equals(Double.class.getName()) || 
            resType.equals(Byte.class.getName()) ||
            resType.equals(Short.class.getName()) || 
            resType.equals(Long.class.getName()) ||
            resType.equals(Float.class.getName())) {
            return true;
        } else {
            return false;
        }
    }
    
    // check to see if resource type is of JMS type
    private boolean isAdminObjectResource(String resType) {
        if (resType.equals("javax.jms.Queue") || // NOI18N
            resType.equals("javax.jms.Topic") || // NOI18N
            resType.equals("javax.jms.Destination") // NOI18N
            ) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isConnectorResource(String resType) {
        if (resType.equals("javax.jms.ConnectionFactory") || // NOI18N
            resType.equals("javax.jms.QueueConnectionFactory") || // NOI18N
            resType.equals("javax.jms.TopicConnectionFactory") || // NOI18N
            resType.equals("javax.jms.XAQueueConnectionFactory") || // NOI18N
            resType.equals("javax.jms.XATopicConnectionFactory") // NOI18N
            ){
            return true;
        } else {
            return false;
        }
    }

    private boolean isJdbcResource(String resType) {
        if (resType.equals("javax.sql.DataSource") || // NOI18N
            resType.equals("javax.sql.XADataSource") || // NOI18N
            resType.equals("javax.sql.ConnectionPoolDataSource") // NOI18N
            ){
            return true;
        } else {
            return false;
        }
    }

    // check to see if resource type is of Other known types
    // TODO: register all valid resource types in here for now
    private boolean isOtherResource(String resType) {
        return true;
    }
    
    /*
    private boolean isSetter(Method m) {
        return m.getName().startsWith("set") && m.getParameterTypes().length == 1;
    }
     **/

    private boolean isSetter(org.netbeans.modules.classfile.Method m) {
        return m.getName().startsWith("set") && m.getParameters().size() == 1; // NOI18N
    }
        
    /*
     * Method m must be a setter method
     */
    /*
    private String getJavaBeanPropName(Method m) {
        String c = m.getName().substring(3, 4).toLowerCase();
        String s = m.getName().substring(4);
        return c + s;
    }
     **/
    
    private String getJavaBeanPropName(org.netbeans.modules.classfile.Method m) {
        String c = m.getName().substring(3, 4).toLowerCase();
        String s = m.getName().substring(4);
        return c + s;
    }
}
