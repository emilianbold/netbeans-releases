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

package org.netbeans.modules.jmx;

/**
 * Class which gives the structure of an MBean operation parameter
 * 
 */
public class MBeanOperationParameter {
    
    private String paramName        = "";
    private String paramType        = "";
    private String paramDescription = "";
    
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
