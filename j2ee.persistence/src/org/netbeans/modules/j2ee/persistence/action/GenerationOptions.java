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

package org.netbeans.modules.j2ee.persistence.action;

/**
 * This class represents code generation options for invoking
 * <code>javax.persistence.EntityManager</code> .
 *
 * @author Erno Mononen
 */
public final class GenerationOptions {
    
    public enum Operation {
        PERSIST("em.persist({0});"),
        MERGE("em.merge({0});"),
        REMOVE("em.merge({0});\n" + "em.remove({0});"),
        FIND("return ({2}) em.find({2}.class, {0});"),
        FIND_ALL("return em.createQuery(\"select object(o) from {2} as o\").getResultList();");
    
        private String body;
        
        private Operation(String body){
            this.body = body;
        }
        
        public String getBody(){
            return body;
        }
    }
    
    private Operation operation;
    private String methodName;
    private String returnType;
    private String parameterName;
    private String parameterType;
    private String queryAttribute;
    
    /** Creates a new instance of GenerationOptions */
    public GenerationOptions() {
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Operation getOperation() {
        return operation;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getQueryAttribute() {
        return queryAttribute;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    
    public void setQueryAttribute(String queryAttribute) {
        this.queryAttribute = queryAttribute;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
 
}
