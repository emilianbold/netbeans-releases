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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
