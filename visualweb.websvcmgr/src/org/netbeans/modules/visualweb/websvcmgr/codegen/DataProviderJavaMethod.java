/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.websvcmgr.codegen;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;

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
        this.returnType = ManagerUtil.typeToString(method.getGenericReturnType());
        if (returnType == null) {
            returnType = method.getReturnType().getCanonicalName();
        }
        
        this.parameters = new ArrayList<DataProviderParameter>();
        Type[] methodParameters = method.getGenericParameterTypes();
        Class[] methodClassParameters = method.getParameterTypes();
        for (int i = 0; i < methodParameters.length; i++) {
            String nextParamType = ManagerUtil.typeToString(methodParameters[i]);
            if (nextParamType == null) {
                nextParamType = methodClassParameters[i].getCanonicalName();
            }
            
            parameters.add(new DataProviderParameter(nextParamType, "arg" + i));
        }
        
        this.exceptions = new ArrayList<String>();
        Type[] methodExceptions = method.getGenericExceptionTypes();
        Class[] methodClassExceptions = method.getExceptionTypes();
        for (int i = 0; i < methodExceptions.length; i++) {
            String nextException = ManagerUtil.typeToString(methodExceptions[i]);
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
