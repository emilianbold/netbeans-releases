/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.common;

import org.openide.util.NbBundle;
/**
 * Exception for cases when adding a new element should be prevented for avoiding duplicity.<br>
 * See : {@link CreateCapability#addBean} method.
 *
 * @author  Milan Kuchtiak
 */
public class NameAlreadyUsedException extends java.lang.Exception {
    private java.lang.String keyProperty, keyValue, beanName;
    
    /**
     * Constructor NameAlreadyUsedException
     * 
     * @param beanName name of the DD element (CommonDDBean object)
     * @param keyProperty name of the property that should be unique
     * @param keyValue value of the keyProperty that causes the duplicity
     */
    public NameAlreadyUsedException (String beanName, String keyProperty, String keyValue) {
        super();
        this.beanName=beanName;
        this.keyProperty=keyProperty;
        this.keyValue=keyValue;
    }
    
    /**
     * Returns the localized message
     * 
     * @return localized message describing the problem.
     */
    public String getMessage() {
        return NbBundle.getMessage(NameAlreadyUsedException.class,"MSG_nameAlreadyUsed",beanName,keyProperty,keyValue);
    }
}
