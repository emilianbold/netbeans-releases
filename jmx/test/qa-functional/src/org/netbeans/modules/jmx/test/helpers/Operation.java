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
 *
 * @author an156382
 */

public class Operation {

    private String operationName = "";
    private String operationReturnType = "";
    private ArrayList<Parameter> operationParameters = null;
    private ArrayList<Exception> operationExceptions = null;
    private String operationComment = "";

    public Operation(String operationName, String operationReturnType,
            ArrayList<Parameter> operationParameters,
            ArrayList<Exception> operationExceptions,
            String operationComment) {
        
        this.operationName = operationName;
        this.operationReturnType = operationReturnType;
        this.operationParameters = operationParameters;
        this.operationExceptions = operationExceptions;
        this.operationComment = operationComment;
    }
    
    public Operation(String operationName, String operationReturnType,
            String operationComment) {
        
        this.operationName = operationName;
        this.operationReturnType = operationReturnType;
        this.operationParameters = null;
        this.operationExceptions = null;
        this.operationComment = operationComment;
    }
    
    /**
     * Method which returns the name of the operation
     * @return operationName the name of the operation
     *
     */
    public String getOperationName() {
        return operationName;
    }
    
    /**
     * Method which sets the name of the operation
     * @param operationName the name of the operation
     *
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
    /**
     * Method which returns the return the type of the operation
     * @return operationReturnType the return type of the operation
     *
     */
    public String getOperationReturnType() {
        return operationReturnType;
    }
    
    /**
     * Method which returns an operation parameter
     * @param index the index of the operation parameter
     * @return operationParameters the parameter at index
     *
     */
    public Parameter getOperationParameter(int index) {
        return operationParameters.get(index);
    }
    
    /**
     * Method which returns an operation exception
     * @param index the index of the operation exception
     * @return operationExceptions the exception at index
     *
     */
    public Exception getOperationException(int index) {
        return operationExceptions.get(index);
    }
    
    /**
     * Method which returns an operation comment
     * @return operationComment the operation comment
     *
     */
    public String getOperationComment() {
        return operationComment;
    }
    
    /**
     * Method which returns the number of parameters
     * @return operationParameters the number of parameters
     *
     */
    public int getOperationParameterSize() {
        if (operationParameters != null)
            return operationParameters.size();
        else
            return -1;
    }
    
    /**
     * Method which returns the number of exceptions
     * @return operationExceptions the number of exceptions
     *
     */
    public int getOperationExceptionSize() {
        if (operationExceptions != null)
            return operationExceptions.size();
        else
            return -1;
    }
}