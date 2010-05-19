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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.Choose;
import org.netbeans.modules.xslt.model.Otherwise;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ChooseImpl extends SequenceElementImpl implements Choose {

    ChooseImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    ChooseImpl( XslModelImpl model ){
        super( model , XslElements.CHOOSE );
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
        return Choose.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#addWhen(org.netbeans.modules.xslt.model.When, int)
     */
    public void addWhen( When when, int position ) {
        insertAtIndex( WHEN_PROPERTY, when, position,
                Attribute.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#appendWhen(org.netbeans.modules.xslt.model.When)
     */
    public void appendWhen( When when ) {
        addBefore( WHEN_PROPERTY, when, OTHERWISE_COLLECTION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#getOtherwise()
     */
    public Otherwise getOtherwise() {
        return getChild( Otherwise.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#getWhens()
     */
    public List<When> getWhens() {
        return getChildren( When.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#removeWhen(org.netbeans.modules.xslt.model.When)
     */
    public void removeWhen( When when ) {
        removeChild( WHEN_PROPERTY , when);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Choose#setOtherwise(org.netbeans.modules.xslt.model.Otherwise)
     */
    public void setOtherwise( Otherwise otherwise ) {
        setChildBefore( Otherwise.class , OTHERWISE_PROPERTY , otherwise , EMPTY );
    }

    private static final Collection<Class<? extends XslComponent>> 
        OTHERWISE_COLLECTION = new ArrayList<Class<? extends XslComponent>>(1);
    
    static {
        OTHERWISE_COLLECTION.add( Otherwise.class );
    }
}
