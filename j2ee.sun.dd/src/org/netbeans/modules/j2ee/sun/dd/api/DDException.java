/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.dd.api;

public class DDException extends org.netbeans.modules.schema2beans.Schema2BeansException {
    private java.lang.String errorMsg;
    
    /**
     * Constructor DDException
     * 
     * @param beanName name of the DD element (CommonDDBean object)
     * @param keyProperty name of the property that should be unique
     * @param keyValue value of the keyProperty that causes the duplicity
     */
    public DDException (String message) {
        super(message);
        this.errorMsg = message;
    }
    
    /**
     * Returns the localized message
     * 
     * @return localized message describing the problem.
     */
    public String getMessage() {
        return this.errorMsg;
    }
}
