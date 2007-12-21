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
package org.netbeans.modules.xslt.model.impl;

import java.util.List;

import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class AttributeSetImpl extends QNameableImpl implements AttributeSet {


    AttributeSetImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    AttributeSetImpl( XslModelImpl model ){
        super( model , XslElements.ATTRIBUTE_SET );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept( XslVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return AttributeSet.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeSet#addAttribute(org.netbeans.modules.xslt.model.Attribute, int)
     */
    public void addAttribute( Attribute attr, int position ) {
        insertAtIndex( ATTRIBUTE_PROPERTY, attr, position,
                Attribute.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeSet#appendAttribute(org.netbeans.modules.xslt.model.Attribute)
     */
    public void appendAttribute( Attribute attr ) {
        appendChild( ATTRIBUTE_PROPERTY , attr );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeSet#getAttributes()
     */
    public List<Attribute> getAttributes() {
        return getChildren( Attribute.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeSet#removeAttribute(org.netbeans.modules.xslt.model.Attribute)
     */
    public void removeAttribute( Attribute attr ) {
        removeChild( ATTRIBUTE_PROPERTY, attr);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseAttributesSetsSpec#getUseAttributeSets()
     */
    public List<XslReference<AttributeSet>> getUseAttributeSets() {
        return resolveGlobalReferenceList( AttributeSet.class , 
                XslAttributes.USE_ATTRIBUTE_SETS);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseAttributesSetsSpec#setUseAttributeSets(java.util.Collection)
     */
    public void setUseAttributeSets( List<XslReference<AttributeSet>> collection ) {
        setAttributeList( XslAttributes.USE_ATTRIBUTE_SETS, collection);
    }

}
