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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.propertyeditors;

import java.beans.PropertyEditor;

/**
 * A base property editor for properties whose range of legal values are represented
 * by an instance of {@link com.sun.rave.propertyeditors.domains.Domain}. The domain
 * class may be supplied directly as an argument to the constructor, or indirectly, as
 * a value for the property descriptor key {@link DomainPropertyEditor#DOMAIN_CLASS}.
 */
public interface DomainPropertyEditor extends PropertyEditor {


    /**
     * Key used to specify a domain class within a property descriptor.
     */
    public final static String DOMAIN_CLASS =
            "com.sun.rave.propertyeditors.DOMAIN_CLASS"; //NOI18N

    
}
