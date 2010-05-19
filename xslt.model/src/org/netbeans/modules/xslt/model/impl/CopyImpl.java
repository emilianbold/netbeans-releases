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

import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.Copy;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class CopyImpl extends ValidationCopyNsSpecImpl implements Copy {

    CopyImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    CopyImpl( XslModelImpl model ){
        super( model , XslElements.COPY );
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
        return Copy.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseAttributesSetsSpec#getUseAttributeSets()
     */
    public List<XslReference<AttributeSet>> getUseAttributeSets() {
        return resolveGlobalReferenceList( AttributeSet.class, 
                XslAttributes.USE_ATTRIBUTE_SETS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseAttributesSetsSpec#setUseAttributeSets(java.util.Collection)
     */
    public void setUseAttributeSets( List<XslReference<AttributeSet>> collection ) {
        setAttributeList( XslAttributes.USE_ATTRIBUTE_SETS , collection );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#addSequenceChild(org.netbeans.modules.xslt.model.SequenceElement, int)
     */
    public void addSequenceChild( SequenceElement element, int position ) {
        insertAtIndex( SEQUENCE_ELEMENT, element , position );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#appendSequenceChild(org.netbeans.modules.xslt.model.SequenceElement)
     */
    public void appendSequenceChild( SequenceElement element ) {
        appendChild( SEQUENCE_ELEMENT, element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#getSequenceChildren()
     */
    public List<SequenceElement> getSequenceChildren() {
        return getChildren( SequenceElement.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#removeSequenceChild(org.netbeans.modules.xslt.model.SequenceElement)
     */
    public void removeSequenceChild( SequenceElement element ) {
        removeChild( SEQUENCE_ELEMENT, element );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.InheritNamespacesSpec#getInheritNamespaces()
     */
    public TBoolean getInheritNamespaces() {
        return TBoolean.forString( getAttribute( 
                XslAttributes.INHERIT_NAMESPACES ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.InheritNamespacesSpec#setInheritNamespaces(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setInheritNamespaces( TBoolean value ) {
        setAttribute( XslAttributes.INHERIT_NAMESPACES, value );
    }

}
