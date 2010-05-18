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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.model;

import javax.xml.namespace.QName;


/**
 * "elements" attribute holder.
 * Type for attribute value here is QName.
 * In reality by XSL spec it should be NameTest :
 * 
 * NameTest ::= QName | Wildcard
 * Wildcard ::= "*"
 *               | (NCName ":" "*")
 *               | ("*" ":" NCName)
 *               
 * So this type has the same structure as QName ( firstPart:secondPart ),
 * so QName is appropriate type here.
 *
 * @author ads
 *
 */
public interface ElementsSpec {

    String ELEMENTS = "elements";       // NOI18N
    
    /**
     * @return "elements" attribute value
     */
    QName getElements();
    
    /**
     * Set "elements" attribute value. 
     * @param value new value
     */
    void setElements( QName value );
}
