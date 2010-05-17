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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
