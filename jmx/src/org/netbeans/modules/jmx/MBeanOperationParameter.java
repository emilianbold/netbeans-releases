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

package org.netbeans.modules.jmx;

/**
 * Class which gives the structure of an MBean operation parameter
 *
 */
public class MBeanOperationParameter {

    private String paramName        = "";// NOI18N
    private String paramType        = "";// NOI18N
    private String paramDescription = "";// NOI18N

    /**
     * Constructor
     * @param paramName the parameter name
     * @param paramType the parameter type
     * @param paramDescription the parameter description
     */
    public MBeanOperationParameter(String paramName, String paramType, 
            String paramDescription) {
        
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramDescription = paramDescription;
    }
    
    /**
     * Sets the parameter name
     * @param name the name to set for this parameter
     */
    public void setParamName(String name) {
        this.paramName = name;
    }
    
    /**
     * Method which returns the name of the parameter
     * @return String the name of the parameter
     *
     */
    public String getParamName() {
        return paramName;
    }
    
    /**
     * Sets the parameter type
     * @param type the parameter type to set
     */
    public void setParamType(String type) {
        this.paramType = type;
    }
    
    /**
     * Method which returns the type of the parameter
     * @return String the type of the parameter
     *
     */
    public String getParamType() {
        return paramType;
    }
    
    /**
     * Sets the parameter description
     * @param descr the parameter description to set
     */
    public void setParamDescription(String descr) {
        this.paramDescription = descr;
    }
    
    /**
     * Method which returns the description of the parameter
     * @return String the description of the parameter
     *
     */
    public String getParamDescription() {
        return paramDescription;
    }
    
}
