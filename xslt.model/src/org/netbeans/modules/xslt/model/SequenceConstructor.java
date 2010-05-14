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

import java.util.List;


/**
 * This is not equivalent of sequence constructor term in XSLT text spec.
 * Sequence constructor XSLT spec term is child construct for some 
 * XSLT instruction. But in this model instruction itself is considered
 * as sequence constructor. This confirm with XSLT schema type.
 * <pre>
 * &lt;xs:complexType name="sequence-constructor">
 *      &lt;xs:complexContent mixed="true">
 *          &lt;xs:extension base="xsl:versioned-element-type">    
 *              &lt;xs:group ref="xsl:sequence-constructor-group" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;/xs:extension>
 *      &lt;/xs:complexContent>
 * &lt;/xs:complexType>
 * </pre>
 * 
 * @author ads
 *
 */
public interface SequenceConstructor extends ContentElement {
    
    String SEQUENCE_ELEMENT = "sequence_element";       // NOI18N

    List<SequenceElement> getSequenceChildren();
    
    /**
     * Add new child <code>element</code> element at <code>position</code>. 
     * @param element new child element.
     * @param position position for new element.
     */
    void addSequenceChild(SequenceElement element, int position);
    
    /**
     * Append new child <code>element</code>.
     * @param element new child element for appending.
     */
    void appendSequenceChild(SequenceElement element);
    
    /**
     * Removes existing child <code>element</code>.
     * @param element child element.
     */
    void removeSequenceChild(SequenceElement element);
    
}
