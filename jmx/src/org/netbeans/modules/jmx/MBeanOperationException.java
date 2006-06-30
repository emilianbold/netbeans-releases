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
