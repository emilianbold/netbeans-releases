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
 * WebServiceClientHanlder.java
 *
 * Created on November 21, 2006, 11:43 AM
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;

import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;

/**
 *
 * @author gpatil
 */
public class WebServiceClientHanlder  implements AnnotationHandler {
    private List<Endpoint> endPoints = new ArrayList<Endpoint>();
    private static Logger logger = Logger.getLogger(WebserviceRefHandler.class.getName());
        
    public WebServiceClientHanlder() {
    }
    
    public String getAnnotationClassConstant(){
        return ANNO_WEBSERVICE_CLIENT;
    }
    
    public void handle(JarClassFileLoader cl, ClassFile theClass) {
        Annotation annoWsc = theClass.getAnnotation(ClassName.getClassName(ANNO_WEBSERVICE_CLIENT));
        Annotation annoWep = null;
        NBAnnonationWrapper wsc = null;
        NBAnnonationWrapper wep = null;
        Properties prop = new Properties();
        if (annoWsc != null){
            wsc = new NBAnnonationWrapper(annoWsc);            
            //WebEndpoint
            Collection<org.netbeans.modules.classfile.Method> methods = theClass.getMethods();
            if ((methods != null) && (methods.size() > 0)){
                for(org.netbeans.modules.classfile.Method method : methods){
                    annoWep = method.getAnnotation(ClassName.getClassName(ANNO_WEB_ENDPOINT));
                    if (annoWep != null){
                        wep = new NBAnnonationWrapper(annoWep);                                    
                        QName serviceName = new QName(wsc.getStringValue(PROP_TNS), wsc.getStringValue(PROP_NAME)); 
                        String returnType = method.getReturnType();
                        ClassFile returnClass = cl.getClassFileFromInternalName(returnType);
                        QName portType = getPortType(returnClass);
                        Endpoint ep = new Endpoint(Endpoint.EndPointType.Consumer, wep.getStringValue(PROP_NAME),
                                (portType == null) ? null : portType, serviceName);
                        this.endPoints.add(ep);
                    }
                }
            }
        }
    }
    
    private QName getPortType(ClassFile cls){
        QName ret = null;
        Annotation anno = cls.getAnnotation(ClassName.getClassName(ANNO_WEB_SERVICE));        
        if ( anno != null){
            NBAnnonationWrapper ws = new NBAnnonationWrapper(anno);            
            ret = new QName(ws.getStringValue(PROP_TNS), ws.getStringValue(PROP_NAME)) ;
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
