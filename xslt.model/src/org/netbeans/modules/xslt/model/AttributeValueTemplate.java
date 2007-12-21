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
 * This interface represent attribute value template.
 * It could represent just simple QName and also it could
 * be a expression {$var_name}.
 * 
 * <pre>
 *   &lt;xs:simpleType name="avt">
 *          &lt;xs:annotation>
 *              &lt;xs:documentation>
 *                  This type is used for all attributes that allow an attribute value template.
 *                  The general rules for the syntax of attribute value templates, and the specific
 *                  rules for each such attribute, are described in the XSLT 2.0 Recommendation.
 *   
 *              &lt;/xs:documentation>
 *          &lt;/xs:annotation>
 *          &lt;xs:restriction base="xs:string"/>
 *   &lt;/xs:simpleType>
 * 
 * </pre>
 * @author ads
 *
 */
public interface AttributeValueTemplate {

    /**
     * @return QName representation of value
     */
    public QName getQName();
    
    /**
     * @return true if this value is template ( not just clear QName )
     */
    public boolean isTemplate();
    
    /**
     * @return original value representation.
     */
    public String toString();
}
