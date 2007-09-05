/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * WebServiceHandler.java
 *
 * Created on October 2, 2006, 11:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;

/**
 *
 * @author gpatil
 */
public class WebServiceHandler implements AnnotationHandler{
    private List<Endpoint> endPoints = new ArrayList<Endpoint>();
    private static Logger logger = Logger.getLogger(WebServiceHandler.class.getName());
    
    // Constants    
    public WebServiceHandler() {
    }
    
    public String getAnnotationClassConstant(){
        return ANNO_WEB_SERVICE;
    }
    
    public void handle(JarClassFileLoader cl, ClassFile theClass) {
        ClassName cnWs = ClassName.getClassName(ANNO_WEB_SERVICE);
        Annotation annoWs = theClass.getAnnotation(cnWs);
        Properties prop = new Properties();
        String tns = null;
        String svcName = null;
        String portName = null;
        String portTypeName = null;
        String qClassName = null;
        String className = null;
        String packageName = null;
        boolean usedDefaultName = false;
        QName service = null;
        QName portType = null;
        
        if (annoWs != null){
            if (!( (theClass.getAccess() & Access.INTERFACE) == Access.INTERFACE)){
                NBAnnonationWrapper ws = new NBAnnonationWrapper(annoWs);
                
                qClassName = theClass.getName().getExternalName();
                int index = qClassName.lastIndexOf('.');
                if (index > -1){
                    packageName = qClassName.substring(0, index) ;
                    className = qClassName.substring(index + 1) ;
                } else {
                    packageName = "default"; // NoI18N
                    className = qClassName;
                }
                
                // tns
                tns = ws.getStringValue(PROP_TNS);
                // Incase user created Webservice w/o WSDL/Contract first model
                if ((tns == null) || "".equals(tns)){
                    usedDefaultName = true;
                    tns = "http://" + convertToTNS(packageName) + "/"; // NoI18N
                }
                
                // svcName
                svcName = ws.getStringValue(PROP_SVC_NAME) ;
                // Incase user created Webservice w/o WSDL/Contract first model
                if ((svcName == null) || "".equals(svcName)){
                    usedDefaultName = true;
                    svcName = className + "Service"; // NoI18N
                }
                service = new QName(tns, svcName);

                portName = ws.getStringValue(PROP_PORT_NAME);

                portTypeName = ws.getStringValue(PROP_NAME);
                if ((portTypeName == null) || ("".equals(portTypeName))){
                    Collection<ClassName> interfaces = theClass.getInterfaces();
                    if ((interfaces != null) && (interfaces.size() > 0)){
                        interfaces.remove(cnWs);
                        portType = getPortType(cl, interfaces);
                    }
                    
                    if (portType == null){
                        usedDefaultName = true;
                        portType = new QName(tns, className);
                        
                        // PROP_PORT_NAME                        
                        if ((portName == null) || "".equals(portName)){
                            usedDefaultName = true;
                            portName = className + "Port" ; // No I18N
                        }
                    } else {
                        // PROP_PORT_NAME
                        if ((portName == null) || "".equals(portName)){
                            usedDefaultName = true;
                            portName = className + "Port" ; // No I18N
                        }                    
                    }
                } else {
                    portType = new QName(tns, portTypeName);
                    // Default PROP_PORT_NAME when portType is not default for 
                    // 'PortType' is 'PortTypePort'
                    if ((portName == null) || "".equals(portName)){
                        usedDefaultName = true;
                        portName = portTypeName + "Port" ; // No I18N
                    }                    
                }

                Endpoint ep = new Endpoint(Endpoint.EndPointType.Provider, portName, portType, service );
                ep.isUsingDefaultNames(Boolean.valueOf(usedDefaultName));
                endPoints.add(ep);
            }
        }
    }
    
    
    private String convertToTNS(String packageName){
        StringBuffer sb = new StringBuffer();
        Stack stk = new Stack<String>();
        StringTokenizer st = new StringTokenizer(packageName, ".");
        
        while (st.hasMoreTokens()){
            stk.push(st.nextToken());
        }
        
        boolean first = true;
        while (!stk.empty()){
            if (!first){
                sb.append(".");
            }
            sb.append(stk.pop());
            first = false;
        }
        
        return sb.toString();
    }
    
    private QName getPortType(JarClassFileLoader cl, Collection<ClassName> interfaces){
        QName ret = null;
        for (ClassName intfc: interfaces){
            try {
                ClassFile cf = cl.getClassFile(intfc);
                Annotation annoWs = cf.getAnnotation(ClassName.getClassName(ANNO_WEB_SERVICE));
                if ( annoWs != null){
                    NBAnnonationWrapper ws = new NBAnnonationWrapper(annoWs);
                    ret = new QName(ws.getStringValue(PROP_TNS), ws.getStringValue(PROP_NAME)) ;
                    break;
                }
            } catch (Exception ex){
                logger.warning("Error loading class:" + intfc.getExternalName(true) + ":" + intfc.getInternalName()+ ":"+ ex.getMessage());
            }
        }
        return ret;
    }
    
    public List<Endpoint> getEndPoints(){
        return this.endPoints;
    }
    
    public void resetEndPoints(){
        this.endPoints.clear();
    }
}
