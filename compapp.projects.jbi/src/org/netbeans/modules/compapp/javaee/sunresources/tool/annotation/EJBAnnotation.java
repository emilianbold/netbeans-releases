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
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.MDBNode;

/**
 * this class is responsible for processing annotations in the javax.ejb.* package
 * 
 * @author echou
 *
 */
public class EJBAnnotation {

    private ResourceAggregator resAggregator;
    
    public EJBAnnotation(ResourceAggregator resAggregator) {
        this.resAggregator = resAggregator;
    }
    
    /*
    public EJBAnnotation(SunResourcesDDJaxbHandler sunResourcesDD) {
        this.sunResourcesDD = sunResourcesDD;
    }
     **/
    
    /*
    public void fillWebServiceNode(EJBNode node, Class<?> cls) {
        WebService ws = (WebService) cls.getAnnotation(WebService.class);
        Properties p = node.getProps();
        p.setProperty("endpointInterface", ws.endpointInterface());
        p.setProperty("name", ws.name());
        p.setProperty("portName", ws.portName());
        p.setProperty("serviceName", ws.serviceName());
        p.setProperty("targetNamespace", ws.targetNamespace());
        p.setProperty("wsdlLocation", ws.wsdlLocation());
    }
     **/
    
    public void fillWebServiceNode(EJBNode node, ClassFile cls) {
        WebServiceAnnoWrapper ws = new WebServiceAnnoWrapper(cls.getAnnotation(
                ClassName.getClassName(JavaEEAnnotationProcessor.WEBSERVICE_CLASSTYPE)));
        Properties p = node.getProps();
        p.setProperty("endpointInterface", ws.endpointInterface()); // NOI18N
        p.setProperty("name", ws.name()); // NOI18N
        p.setProperty("portName", ws.portName()); // NOI18N
        p.setProperty("serviceName", ws.serviceName()); // NOI18N
        p.setProperty("targetNamespace", ws.targetNamespace()); // NOI18N
        p.setProperty("wsdlLocation", ws.wsdlLocation()); // NOI18N
    }
    
    /*
    public void fillStatelessNode(EJBNode node, Class<?> cls) {
        Stateless sl = (Stateless) cls.getAnnotation(Stateless.class);
        Properties p = node.getProps();
        p.setProperty("description", sl.description());
        p.setProperty("mappedName", sl.mappedName());
        p.setProperty("name", sl.name());
    }
     **/
    
    public void fillStatelessNode(EJBNode node, ClassFile cls) {
        StatelessAnnoWrapper sl = new StatelessAnnoWrapper(cls.getAnnotation(
                ClassName.getClassName(JavaEEAnnotationProcessor.STATELESS_CLASSTYPE)));
        Properties p = node.getProps();
        p.setProperty("description", sl.description()); // NOI18N
        p.setProperty("mappedName", sl.mappedName()); // NOI18N
        p.setProperty("name", sl.name()); // NOI18N
    }
    
    /*
    public void fillStatefulNode(EJBNode node, Class<?> cls) {
        Stateful sf = (Stateful) cls.getAnnotation(Stateful.class);
        Properties p = node.getProps();
        p.setProperty("description", sf.description());
        p.setProperty("mappedName", sf.mappedName());
        p.setProperty("name", sf.name());
    }
     **/
    
    public void fillStatefulNode(EJBNode node, ClassFile cls) {
        StatefulAnnoWrapper sf = new StatefulAnnoWrapper(cls.getAnnotation(
                ClassName.getClassName(JavaEEAnnotationProcessor.STATEFUL_CLASSTYPE)));
        Properties p = node.getProps();
        p.setProperty("description", sf.description()); // NOI18N
        p.setProperty("mappedName", sf.mappedName()); // NOI18N
        p.setProperty("name", sf.name()); // NOI18N
    }

    /*
    public void fillMessageDrivenNode(MDBNode node, Class<?> cls) {
        MessageDriven md = (MessageDriven) cls.getAnnotation(MessageDriven.class);
        Properties p = node.getProps();
        p.setProperty("description", md.description());
        p.setProperty("mappedName", md.mappedName());
        p.setProperty("name", md.name());
        
        // fill in mappedName
        node.setMappedName(md.mappedName());
        
        // fill in messageListenerInterface
        ArrayList<Class> intfs = 
            filterExcludedEJBIntf(cls.getInterfaces());
        if (intfs.size() == 1) {
            node.setMsgListenerIntClassName(intfs.get(0).getName());
        } else {
            node.setMsgListenerIntClassName(md.messageListenerInterface().getName());
        }
        
        // fill in ActivationConfig
        Properties nodeAC = node.getActivationConfig();
        ActivationConfigProperty[] acProps = md.activationConfig();
        for (int i = 0; i < acProps.length; i++) {
            nodeAC.setProperty(acProps[i].propertyName(), 
                    acProps[i].propertyValue());
        }
        
        // add to server resources jmsra
        if (!sunResourcesDD.containsResource(md.mappedName())) {
            String resType = null;
            for (int i = 0; i < acProps.length; i++) {
                if (acProps[i].propertyName().equals("destinationType")) {
                    resType = acProps[i].propertyValue();
                }
            }
            Properties adminObjProps = new Properties();
            adminObjProps.setProperty("Name", cls.getSimpleName());
            sunResourcesDD.addAdminObjectResource(md.mappedName(), resType, "jmsra", adminObjProps);
        }
    }
     **/
    
    public void fillMessageDrivenNode(MDBNode node, ClassFile cls) {
        MessageDrivenAnnoWrapper md = new MessageDrivenAnnoWrapper(
                cls.getAnnotation(ClassName.getClassName(JavaEEAnnotationProcessor.MDB_CLASSTYPE)));
        Properties p = node.getProps();
        p.setProperty("description", md.description()); // NOI18N
        p.setProperty("mappedName", md.mappedName()); // NOI18N
        p.setProperty("name", md.name()); // NOI18N
        
        // fill in mappedName
        node.setMappedName(md.mappedName());
        
        // fill in messageListenerInterface
        ArrayList<String> intfs = 
            filterExcludedEJBIntf(cls.getInterfaces());
        if (intfs.size() == 1) {
            node.setMsgListenerIntClassName(intfs.get(0));
        } else {
            node.setMsgListenerIntClassName(md.messageListenerInterface());
        }
        
        // fill in ActivationConfig
        Properties nodeAC = node.getActivationConfig();
        ActivationConfigPropertyAnnoWrapper[] acProps = md.activationConfig();
        for (int i = 0; i < acProps.length; i++) {
            nodeAC.setProperty(acProps[i].propertyName(), 
                    acProps[i].propertyValue());
        }
        
        // add to server resources jmsra
        ResourceAggregator.ResourceEntry resourceEntry = 
                resAggregator.getAdminObjectResourceEntry(md.mappedName());
        if (resourceEntry == null) {
            String resType = null;
            for (int i = 0; i < acProps.length; i++) {
                if (acProps[i].propertyName().equals("destinationType")) { // NOI18N
                    resType = acProps[i].propertyValue();
                }
            }
            Properties adminObjProps = new Properties();
            adminObjProps.setProperty("Name", cls.getName().getSimpleName()); // NOI18N
            resourceEntry = resAggregator.addAdminObjectResourceEntry(
                    md.mappedName(), resType, "jmsra", adminObjProps); // NOI18N
        } else {
            resourceEntry.obsolete = false;
            resourceEntry.orphanStatus = ResourceAggregator.OrphanStatus.BOTH;
        }
        String sourceName = cls.getName().getPackage().replace('.', '/') + "/" + cls.getSourceFileName(); // NOI18N
        resourceEntry.addResourceUsage(sourceName, null);
    }
    
    
    /* EJB 3.0 Simplied API, 10.1.3
     * The messageListenerInterface element specifies the message listener 
     * interface of the bean. It must be specified if the bean class does 
     * not implement its message listener interface or implements more than 
     * one interface other than java.io.Serializable, java.io.Externalizable, 
     * or any of the interfaces defined by the javax.ejb package.
     */
    /*
    public static ArrayList<Class> filterExcludedEJBIntf(Class[] intfs) {
        ArrayList<Class> aList = new ArrayList<Class> ();
        for (int i = 0; i < intfs.length; i++) {
            if (intfs[i] == java.io.Serializable.class ||
                    intfs[i] == java.io.Externalizable.class ||
                    intfs[i].getName().startsWith("javax.ejb.")) {
                continue;
            } else {
                aList.add(intfs[i]);
            }
        }
        return aList;
    }
     **/
    
    // using NetBeans ClassFile API, input is Collection of String
    public static ArrayList<String> filterExcludedEJBIntf(Collection<ClassName> intfs) {
        ArrayList<String> aList = new ArrayList<String> ();
        for (Iterator<ClassName> iter = intfs.iterator(); iter.hasNext(); ) {
            ClassName intfCN = iter.next();
            String intf = intfCN.getExternalName();
            if (intf.equals("java.io.Serializable") || // NOI18N
                    intf.equals("java.io.Externalizable") || // NOI18N
                    intf.startsWith("javax.ejb.")) { // NOI18N
                continue; 
            } else {
                aList.add(intf);
            }
        }
        return aList;
    }
    
}
