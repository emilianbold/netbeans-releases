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

/**
 * Used to check MBean operation exception wizard values.
 */
public class Exception {

    private String excepClass = "";
    private String excepDescription = "";

    public Exception(String excepClass, String excepDescription) {
        this.excepClass = excepClass;
        this.excepDescription = excepDescription;
    }

    /**
     * Returns the class of the exception
     * @return excepClass the class of the exception
     */
    public String getClassName() {
        return excepClass;
    }

    /**
     * Sets the class of the exception
     * @param excepClass the class of the exception
     */
    public void setClassName(String excepClass) {
        this.excepClass = excepClass;
    }

    /**
     * Returns the description of the exception
     * @return excepDescription the description of the exception
     */
    public String getDescription() {
        return excepDescription;
    }
    
    /**
     * Sets the description of the exception
     * @param excepDescription the description of the exception
     */
    public void setDescription(String excepDescription) {
        this.excepDescription = excepDescription;
    }
}
