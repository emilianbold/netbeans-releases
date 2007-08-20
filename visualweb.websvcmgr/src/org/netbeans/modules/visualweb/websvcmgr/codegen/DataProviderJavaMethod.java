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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.visualweb.websvcmgr.util.Util;

/**
 *
 * @author quynguyen
 */
public class DataProviderJavaMethod implements DataProviderMethod {
    private String name;
    private String returnType;
    private List<DataProviderParameter> parameters;
    private List<String> exceptions;
    
    public DataProviderJavaMethod(Method method) {
        this.name = method.getName();
        this.returnType = Util.typeToString(method.getGenericReturnType());
        if (returnType == null) {
            returnType = method.getReturnType().getCanonicalName();
        }
        
        this.parameters = new ArrayList<DataProviderParameter>();
        Type[] methodParameters = method.getGenericParameterTypes();
        Class[] methodClassParameters = method.getParameterTypes();
        for (int i = 0; i < methodParameters.length; i++) {
            String nextParamType = Util.typeToString(methodParameters[i]);
            if (nextParamType == null) {
                nextParamType = methodClassParameters[i].getCanonicalName();
            }
            
            parameters.add(new DataProviderParameter(nextParamType, "arg" + i));
        }
        
        this.exceptions = new ArrayList<String>();
        Type[] methodExceptions = method.getGenericExceptionTypes();
        Class[] methodClassExceptions = method.getExceptionTypes();
        for (int i = 0; i < methodExceptions.length; i++) {
            String nextException = Util.typeToString(methodExceptions[i]);
            if (nextException == null) {
                nextException = methodClassExceptions[i].getCanonicalName();
            }
            
            exceptions.add(nextException);
        }
    }

    public String getMethodName() {
        return name;
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
}
