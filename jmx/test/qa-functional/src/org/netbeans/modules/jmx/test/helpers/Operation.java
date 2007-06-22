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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

import java.util.ArrayList;

/**
 * Used to check MBean operation wizard values.
 */

public class Operation {
    
    private String operationName = "";
    private String operationReturnType = "";
    private ArrayList<Parameter> operationParameters = null;
    private ArrayList<Exception> operationExceptions = null;
    private String operationDescription = "";
    
    public Operation(String operationName, String operationReturnType,
            ArrayList<Parameter> operationParameters,
            ArrayList<Exception> operationExceptions,
            String operationDescription) {
        this.operationName = operationName;
        this.operationReturnType = operationReturnType;
        this.operationParameters = operationParameters;
        this.operationExceptions = operationExceptions;
        this.operationDescription = operationDescription;
    }
    
    public Operation(String operationName, String operationReturnType,
            String operationDescription) {
        this.operationName = operationName;
        this.operationReturnType = operationReturnType;
        this.operationDescription = operationDescription;
    }
    
    /**
     * Method which returns the name of the operation
     * @return operationName the name of the operation
     *
     */
    public String getName() {
        return operationName;
    }
    
    /**
     * Method which sets the name of the operation
     * @param operationName the name of the operation
     *
     */
    public void setName(String operationName) {
        this.operationName = operationName;
    }
    
    /**
     * Method which returns the return the type of the operation
     * @return operationReturnType the return type of the operation
     *
     */
    public String getReturnType() {
        return operationReturnType;
    }
    
    /**
     * Method which sets the return the type of the operation
     * @param operationReturnType the return type of the operation
     *
     */
    public void setReturnType(String operationReturnType) {
        this.operationReturnType = operationReturnType;
    }
    
    /**
     * Method which returns an operation description
     * @return operationDescription the operation description
     *
     */
    public String getDescription() {
        return operationDescription;
    }
    
    /**
     * Method which sets an operation description
     * @return operationDescription the operation description
     *
     */
    public void setDescription(String operationDescription) {
        this.operationDescription = operationDescription;
    }
    
    /**
     * Method which returns the operation parameters
     */
    public ArrayList<Parameter> getParameters() {
        return operationParameters;
    }
    
    /**
     * Method which returns an operation parameter
     * @param index the index of the operation parameter
     * @return operationParameters the parameter at index
     *
     */
    public Parameter getParameter(int index) {
        return operationParameters.get(index);
    }
    
    /**
     * Method which returns the operation exceptions
     */
    public ArrayList<Exception> getExceptions() {
        return operationExceptions;
    }
    
    /**
     * Method which returns an operation exception
     * @param index the index of the operation exception
     * @return operationExceptions the exception at index
     *
     */
    public Exception getException(int index) {
        return operationExceptions.get(index);
    }
    
    /**
     * Method which returns the number of parameters
     * @return operationParameters the number of parameters
     *
     */
    public int getParameterSize() {
        return operationParameters.size();
    }
    
    /**
     * Method which returns the number of exceptions
     * @return operationExceptions the number of exceptions
     *
     */
    public int getExceptionSize() {
        return operationExceptions.size();
    }
}