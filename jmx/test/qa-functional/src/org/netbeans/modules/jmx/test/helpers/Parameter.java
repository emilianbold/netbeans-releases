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

/**
 *
 * @author an156382
 */
public class Parameter {
    
    private String paramName = "";
    private String paramType = "";
    private String paramComment = "";
    
    /** Creates a new instance of Parameter */
    public Parameter(String paramName, String paramType, String paramComment) {
        
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramComment = paramComment;
    }
    
    /**
     * Method which returns the name of the parameter
     * @return paramName the name of the parameter
     *
     */
    public String getParamName() {
        return paramName;
    }
    
    /**
     * Method which returns the type of the parameter
     * @return paramType the type of the parameter
     *
     */
    public String getParamType() {
        return paramType;
    }
    
    /**
     * Method which returns the comment of the parameter
     * @return paramComment the comment of the parameter
     *
     */
    public String getParamComment() {
        return paramComment;
    }
    
}
