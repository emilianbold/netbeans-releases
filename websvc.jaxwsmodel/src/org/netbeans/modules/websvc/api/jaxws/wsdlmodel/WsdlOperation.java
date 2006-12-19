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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import java.util.*;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WsdlOperation {
    public static final int TYPE_NORMAL=0;
    public static final int TYPE_ASYNC_POLLING=1;
    public static final int TYPE_ASYNC_CALLBACK=2;
    
    private Operation operation;
    /** Creates a new instance of WsdlOperation */
    public WsdlOperation(Operation operation) {
        this.operation=operation;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Operation*/ getInternalJAXWSOperation() {
        return operation;
    }
    
    public String getName() {
        String operationName = operation.getName().getLocalPart();
        String postfix=null;
        switch (getOperationType()) {
            case TYPE_NORMAL:break;
            case TYPE_ASYNC_POLLING: {
                postfix = NbBundle.getMessage(WsdlOperation.class,"TXT_asyncPolling");
                break;
            }
            case TYPE_ASYNC_CALLBACK: {
                postfix = NbBundle.getMessage(WsdlOperation.class,"TXT_asyncCallback");
                break;
            }
        }
        if (postfix!=null)
            operationName = NbBundle.getMessage(WsdlOperation.class,"TXT_operationName",operationName,postfix);
        return operationName; 
    }
    
    public String getJavaName() {
        return operation.getJavaMethod().getName();
    }
    
    public String getReturnTypeName() {
        return operation.getJavaMethod().getReturnType().getName();
    }
    
    public List<WsdlParameter> getParameters() {
        List<WsdlParameter> wsdlParameters = new ArrayList<WsdlParameter> ();
        if (operation==null) return wsdlParameters;
        List<JavaParameter> parameterList = operation.getJavaMethod().getParametersList();
        for (JavaParameter param: parameterList)
            wsdlParameters.add(new WsdlParameter(param));
        return wsdlParameters;
    }
    
    public Iterator<String> getExceptions() {
        return operation.getJavaMethod().getExceptions();
    }
    
    public int getOperationType() {
        String returnType = getReturnTypeName();
        if (returnType.startsWith("javax.xml.ws.Response")) { //NOI18N
            return TYPE_ASYNC_POLLING;
        } else if (returnType.startsWith("java.util.concurrent.Future")) { //NOI18N
            return TYPE_ASYNC_CALLBACK;
        } else return TYPE_NORMAL;
        
    }
    
}
