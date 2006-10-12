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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.jmi.javamodel.Array;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class JaxWsClassesCookieImpl implements JaxWsClassesCookie {
    Service service;
    FileObject implClassFO;
    
    public JaxWsClassesCookieImpl(Service service, FileObject implClassFO) {
        this.service = service;
        this.implClassFO = implClassFO;
    }
    
    private JavaClass getImplBeanClass() {
        String implBean = service.getImplementationClass();
        if(implBean != null) {
            return JMIUtils.findClass(implBean, implClassFO);
        }
        return null;
    }
    
    private String getImplicitValue(Type t) {
        // TODO - should enhance to enum types, annotation types
        String ret = "return ";
        
        if (t instanceof PrimitiveType) {
            
            if (((PrimitiveType)t).getKind().equals(PrimitiveTypeKindEnum.BOOLEAN)) {
                return "false"; //NOI18N
            }
            if (((PrimitiveType)t).getKind().equals(PrimitiveTypeKindEnum.CHAR)) {
                return "''"; //NOI18N
            }
            return "0"; //NOI18N
            
        } else if (t instanceof JavaClass) {
            return "null"; //NOI18N
            
        } else if (t instanceof Array) {
            return "new " + t.getName() + " { }"; //NOI18N
        }
        
        return null;
    }
    
    public void addOperation(Method m) {
        JavaClass implClass = null;
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            m.setJavadocText(NbBundle.getMessage(JaxWsClassesCookieImpl.class, "TXT_WSOperation"));
                    
            implClass = getImplBeanClass();
            
            String body = NbBundle.getMessage(JaxWsClassesCookieImpl.class, "TXT_VoidOperation"); //NOI18N
            
            Type returnType = m.getType();
            boolean voidType = true;
            if (!((returnType instanceof PrimitiveType) && ((PrimitiveType) returnType).getKind().equals(PrimitiveTypeKindEnum.VOID))) {
                voidType=false;
                String retString = getImplicitValue(m.getType());
                if (retString != null) {
                    body += "return " + retString + ";"; //NOI18N
                }
            }

            if (implClass != null) {
                implClass.getContents().add(m);

                // #61178
                JMIUtils.fixImports(implClass);
                //JMIUtils.fixImports(seiClass);

                m.setBodyText(body);

                // add javax.jws.WebMethod annotation
                m.getAnnotations().add(JMIGenerationUtil.createAnnotation(implClass,"javax.jws.WebMethod",new ArrayList())); //NOI18N
                if (voidType && m.getExceptionNames().size()==0) m.getAnnotations().add(JMIGenerationUtil.createAnnotation(implClass,"javax.jws.Oneway",new ArrayList())); //NOI18N
                List parameters = m.getParameters();
                for (int i=0;i<parameters.size();i++) {
                    Parameter param = (Parameter)parameters.get(i);
                    String paramName = param.getName();
                    List attrValues = new ArrayList();
                    attrValues.add(JMIGenerationUtil.createAttributeValue(implClass,"name",paramName));
                    param.getAnnotations().add(JMIGenerationUtil.createAnnotation(implClass,"javax.jws.WebParam",attrValues)); //NOI18N
                } 
                rollback = false;
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        JMIUtils.openInEditor(m);
    }
    
}

