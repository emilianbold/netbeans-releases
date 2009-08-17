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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMap;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBInterface;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.MDBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode.CMapNodeType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBInterface.EJBInterfaceType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ServletNode;

/**
 * @author echou
 *
 */
public class JavaEEAnnotationProcessor {

    public static final String LOCAL_CLASSTYPE = "javax/ejb/Local"; // NOI18N
    public static final String REMOTE_CLASSTYPE = "javax/ejb/Remote"; // NOI18N
    public static final String STATEFUL_CLASSTYPE = "javax/ejb/Stateful"; // NOI18N
    public static final String STATELESS_CLASSTYPE = "javax/ejb/Stateless"; // NOI18N
    public static final String MDB_CLASSTYPE = "javax/ejb/MessageDriven"; // NOI18N
    public static final String WEBSERVICE_CLASSTYPE = "javax/jws/WebService"; // NOI18N
    public static final String EJB_CLASSTYPE = "javax/ejb/EJB"; // NOI18N
    public static final String EJBS_CLASSTYPE = "javax/ejb/EJBs"; // NOI18N
    public static final String RESOURCE_CLASSTYPE = "javax/annotation/Resource"; // NOI18N
    public static final String RESOURCES_CLASSTYPE = "javax/annotation/Resources"; // NOI18N
    public static final String WEBSERVICEREF_CLASSTYPE = "javax/xml/ws/WebServiceRef"; // NOI18N
    public static final String WEBSERVICEREFS_CLASSTYPE = "javax/xml/ws/WebServiceRefs"; // NOI18N
    public static final String SERVLET_CLASSTYPE = "javax/servlet/Servlet"; // NOI18N
    public static final String GENERIC_SERVLET_CLASSTYPE = "javax/servlet/GenericServlet"; // NOI18N
    public static final String HTTP_SERVLET_CLASSTYPE = "javax/servlet/http/HttpServlet"; // NOI18N
    
    private EJBAnnotation ejbAnnotation;
    private DependencyAnnotation dependencyAnnotation;
    private CMap cmap;
    
    // keep track of Local/Remote interfaces
    private Hashtable<String, EJBInterfaceType> intfTable = 
        new Hashtable<String, EJBInterfaceType> ();
    
    public JavaEEAnnotationProcessor(CMap cmap, ResourceAggregator resAggregator) {
        this.ejbAnnotation = new EJBAnnotation(resAggregator);
        this.dependencyAnnotation = new DependencyAnnotation(resAggregator);
        this.cmap = cmap;
    }
    
    /**
     * 
     * @param cls declaring class of annotations to process
     * @throws Exception
     */
    /*
    public void process(Class<?> cls) throws Exception {
        if (cls.isInterface()) {  // process Interface
            if (cls.isAnnotationPresent(Local.class)) {
                intfTable.put(cls.getName(), EJBInterfaceType.LOCAL);
            } else if (cls.isAnnotationPresent(Remote.class)) {
                intfTable.put(cls.getName(), EJBInterfaceType.REMOTE);
            }
        } else {  // process Class
            if (cls.isAnnotationPresent(WebService.class) &&
                    cls.isAnnotationPresent(Stateless.class)) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATELESS);
                node.setIsWebService(true);
                ejbAnnotation.fillWebServiceNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName(), node);
            } else if (cls.isAnnotationPresent(Stateless.class)) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATELESS);
                ejbAnnotation.fillStatelessNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName(), node);
            } else if (cls.isAnnotationPresent(Stateful.class)) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATEFUL);
                ejbAnnotation.fillStatefulNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName(), node);
            } else if (cls.isAnnotationPresent(MessageDriven.class)) {
                MDBNode node = new MDBNode(cls, CMapNodeType.MDB);
                ejbAnnotation.fillMessageDrivenNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName(), node);
            }
        }
    }
     **/
    

    /*
     * process annotation using NetBeans ClassFile API
     */
    public void process(ClassFile cls) throws Exception {
        if ((cls.getAccess() & Access.INTERFACE) == Access.INTERFACE) {  // process Interface
            if (cls.isAnnotationPresent(ClassName.getClassName(LOCAL_CLASSTYPE))) {
                intfTable.put(cls.getName().getExternalName(), EJBInterfaceType.LOCAL);
            } else if (cls.isAnnotationPresent(ClassName.getClassName(REMOTE_CLASSTYPE))) {
                intfTable.put(cls.getName().getExternalName(), EJBInterfaceType.REMOTE);
            }
        } else {  // process Class
            if (cls.isAnnotationPresent(ClassName.getClassName(WEBSERVICE_CLASSTYPE)) &&
                    cls.isAnnotationPresent(ClassName.getClassName(STATELESS_CLASSTYPE))) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATELESS);
                node.setIsWebService(true);
                ejbAnnotation.fillWebServiceNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName().getExternalName(), node);
            } else if (cls.isAnnotationPresent(ClassName.getClassName(STATELESS_CLASSTYPE))) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATELESS);
                ejbAnnotation.fillStatelessNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName().getExternalName(), node);
            } else if (cls.isAnnotationPresent(ClassName.getClassName(STATEFUL_CLASSTYPE))) {
                EJBNode node = new EJBNode(cls, CMapNodeType.STATEFUL);
                ejbAnnotation.fillStatefulNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName().getExternalName(), node);
            } else if (cls.isAnnotationPresent(ClassName.getClassName(MDB_CLASSTYPE))) {
                MDBNode node = new MDBNode(cls, CMapNodeType.MDB);
                ejbAnnotation.fillMessageDrivenNode(node, cls);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName().getExternalName(), node);
            } else if (isServlet(cls)) {
                ServletNode node = new ServletNode(cls, CMapNodeType.SERVLET);
                dependencyAnnotation.fillDependencyInfo(node, cls);
                cmap.addNode(cls.getName().getExternalName(), node);
            }
        }
    }

    /*
     * post processing
     * need to find out if EJB interface is Local or Remote
     */
    public void postProcess() throws Exception {
        // check global interface table to see if need to set Local/Remote type
        // on the ejb bean implemented interfaces
        for (Iterator<CMapNode> iter = cmap.getNodes(); iter.hasNext(); ) {
            CMapNode node = iter.next();
            if (node instanceof EJBNode) {
                EJBNode ejbNode = (EJBNode) node;
                ArrayList<EJBInterface> ejbIntfs = ejbNode.getImplementedIntfs();
                for (int i = 0; i < ejbIntfs.size(); i++) {
                    EJBInterface ejbIntf = ejbIntfs.get(i);
                    EJBInterfaceType type = intfTable.get(ejbIntf.getIntf());
                    if (type != null && type != ejbIntf.getIntfType()) {
                        ejbIntf.setIntfType(type);
                    }
                }
            }
        }
        
        cmap.postProcess();
    }
    
    private boolean isServlet(ClassFile cls) {
        for (Iterator iter = cls.getInterfaces().iterator(); iter.hasNext(); ) {
            String s = (String) iter.next();
            if (SERVLET_CLASSTYPE.equals(s)) {
                return true;
            }
        }
        // check superclass
        if (cls.getSuperClass().equals(ClassName.getClassName(GENERIC_SERVLET_CLASSTYPE)) ||
                cls.getSuperClass().equals(ClassName.getClassName(HTTP_SERVLET_CLASSTYPE))) {
            return true;
        }
        return false;
    }
}
