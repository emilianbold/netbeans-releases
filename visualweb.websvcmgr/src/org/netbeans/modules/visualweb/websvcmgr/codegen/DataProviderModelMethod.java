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
