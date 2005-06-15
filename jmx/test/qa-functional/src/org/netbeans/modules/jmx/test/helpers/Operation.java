/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
    
    /**
     * Method which returns the name of the operation
     * @return operationName the name of the operation
     *
     */
    public String getOperationName() {
        return operationName;
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