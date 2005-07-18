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
 * Class which describes the structure of an MBean operation exception
 * 
 */
public class MBeanOperationException {
    
    private String excepClass = "";// NOI18N
    private String excepDescription = "";// NOI18N
    
    
    /**
     * Default constructor
     */
    public MBeanOperationException() {
        
    }
    
    /**
     * Constructor
     * @param excepClass the exception java class
     * @param excepDescription the exception description
     */
    public MBeanOperationException(String excepClass, 
            String excepDescription) {
        
        this.excepClass = excepClass;
        this.excepDescription = excepDescription;
    }
    
    /**
     * Sets the exception class
     * @param excepClass the exception class
     */
    public void setExceptionClass(String excepClass) {
        this.excepClass = excepClass;
    }
    
    /**
     * Returns the class of the exception
     * @return String the class of the exception
     */
    public String getExceptionClass() {
        return excepClass;
    }
    
    /**
     * Sets the exception description
     * @param excepDescription the exception description to set
     */
    public void setExceptionDescription(String excepDescription) {
        this.excepDescription = excepDescription;
    }
    
    /**
     * Returns the comment of the exception
     * @return String the comment of the exception
     */
    public String getExceptionDescription() {
        return excepDescription;
    }
    
}
