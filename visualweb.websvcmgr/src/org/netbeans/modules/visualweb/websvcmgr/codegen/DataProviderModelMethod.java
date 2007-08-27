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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.websvcmgr.codegen;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author quynguyen
 */
public class DataProviderModelMethod implements DataProviderMethod {
    private String methodName;
    private String returnType;
    private List<DataProviderParameter> parameters;
    private List<String> exceptions;
    private JavaMethod javaMethod;
    
    public DataProviderModelMethod(JavaMethod method) {
        this.methodName = method.getName();
        this.returnType = toString(method.getReturnType());
        this.javaMethod = method;
        
        List<JavaParameter> params = method.getParametersList();
        parameters = new ArrayList<DataProviderParameter>();
        for (JavaParameter param : params) {
            String paramType = toString(param.getType());
            if (param.isHolder()) {
                paramType = param.getHolderName() + "<" + paramType + ">"; // NOI18N
            }
            parameters.add(new DataProviderParameter(paramType, param.getName()));
        }
        
        exceptions = new ArrayList<String>();
        Iterator<String> exceptionIter = method.getExceptions();
        
        while (exceptionIter.hasNext()) {
            exceptions.add(exceptionIter.next());
        }
    }

    public JavaMethod getJavaMethod() {
        return javaMethod;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public String getMethodReturnType() {
        return returnType;
    }

    public List<DataProviderParameter> getParameters() {
        return parameters;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    private String toString(JavaType type) {
        if (type.isHolder()) {
            return "javax.xml.ws.Holder<" + type.getHolderName() + ">"; // NOI18N
        }else {
            return type.getRealName();
        }
    }
}
