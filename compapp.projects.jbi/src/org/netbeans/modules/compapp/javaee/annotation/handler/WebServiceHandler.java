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
    
    public void handle(ClassFileLoader cl, ClassFile theClass) {
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
    
    private QName getPortType(ClassFileLoader cl, Collection<ClassName> interfaces){
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
