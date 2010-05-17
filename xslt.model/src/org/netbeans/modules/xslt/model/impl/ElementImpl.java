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
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.netbeans.modules.xslt.model.enums.Validation;


/**
 * @author ads
 *
 */
class ElementImpl extends TypeableNameableSeqElCtor implements Element {

    ElementImpl( XslModelImpl model, org.w3c.dom.Element element ) {
        super( model , element );
    }
    
    
    ElementImpl( XslModelImpl model ){
        super( model , XslElements.ELEMENT );
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
        return Element.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ValidationSpec#getValidation()
     */
    public Validation getValidation() {
        return Validation.forString( getAttribute( XslAttributes.VALIDATION ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ValidationSpec#setValidation(org.netbeans.modules.xslt.model.enums.Validation)
     */
    public void setValidation( Validation validation ) {
        setAttribute( XslAttributes.VALIDATION, validation );
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
