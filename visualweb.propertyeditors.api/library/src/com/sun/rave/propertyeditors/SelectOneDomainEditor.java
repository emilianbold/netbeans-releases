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

import com.sun.rave.propertyeditors.domains.Domain;


import java.beans.PropertyEditor;

/**
 * An editor for properties, the value of which must be selected from one of
 * a range of values. The domain of legal values is supplied to the editor as an
 * instance of {@link com.sun.rave.propertyeditors.domains.Domain}. The domain class
 * is supplied, indirectly, as a value for the property descriptor key 
 * {@link DomainPropertyEditor#DOMAIN_CLASS}. For example, to configure a property
 * the value of which should represent an ISO language code:
 *
 * <pre>
 *     propertyDescriptor.setEditorClass(SelectOneDomainEditor.class);
 *     propertyDescriptor.setValue(DomainPropertyEditor.DOMAIN_CLASS, LanguagesDomain);
 * </pre>
 */
public interface SelectOneDomainEditor extends DomainPropertyEditor {

}
