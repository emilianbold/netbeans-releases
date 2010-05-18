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


/**
 * This is xsl:text element.
 * Please note that this is not the same as TEXT DOM node .
 * TEXT nodes in Xsl OM is maped to following sibling element if any.   
 *  
 * This is real xslt instraction element. 
 * <pre>
 * &lt;xs:element name="text" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:simpleContent>
 *              &lt;xs:extension base="xsl:text-element-base-type">
 *                  &lt;xs:attribute name="disable-output-escaping" type="xsl:yes-or-no" default="no"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:simpleContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 *
 * &lt;xs:complexType name="text-element-base-type">
 *      &lt;xs:simpleContent>
 *          &lt;xs:restriction base="xsl:versioned-element-type">
 *              &lt;xs:simpleType>
 *                  &lt;xs:restriction base="xs:string"/>
 *              &lt;/xs:simpleType>
 *              &lt;xs:anyAttribute namespace="##other" processContents="lax"/>
 *          &lt;/xs:restriction>
 *      &lt;/xs:simpleContent>
 * &lt;/xs:complexType>
 * </pre>
 * 
 * @author ads
 *
 */
public interface Text extends Instruction, DisableOutputExcapingSpec,
    ContentElement 
{

}
