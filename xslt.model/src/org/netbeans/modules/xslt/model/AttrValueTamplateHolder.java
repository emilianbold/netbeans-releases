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
 * This interface represent entity that has "name" attrubute 
 * with type AttributeValueTemplate.
 *  
 * @author ads
 *
 */
public interface AttrValueTamplateHolder extends XslComponent {

    String NAME = QualifiedNameable.NAME;
    
    /**
     * @return QName value for attribute "name"
     */
    AttributeValueTemplate getName();
   
    /**
     * Sets QName value for attribute "name".
     * @param name new QName value. 
     */
    void setName( AttributeValueTemplate name );
    
    /**
     * Creates attribute value template via <code>qName</code>
     * as input value.
     * @param qName original QName for wrap
     * @return instantiated object
     */
    AttributeValueTemplate createTemplate( QName qName );
    
    /**
     * Creates attribute value template via its string representation.
     * 
     * @param value string value for template
     * @return instantiated object
     */
    AttributeValueTemplate createTemplate( String value  );
}
