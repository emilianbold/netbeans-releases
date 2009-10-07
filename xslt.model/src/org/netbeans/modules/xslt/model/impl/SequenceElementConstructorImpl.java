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

import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.w3c.dom.Element;

/**
 * @author ads
 *
 */
abstract class SequenceElementConstructorImpl extends
        SequenceElementImpl
{

    SequenceElementConstructorImpl( XslModelImpl model, Element e ) {
        super(model, e);
    }
    
    SequenceElementConstructorImpl( XslModelImpl model, XslElements type ) {
        super(model, type );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#addSequenceChild(org.netbeans.modules.xslt.model.SequenceElement, int)
     */
    public void addSequenceChild( SequenceElement element, int position ) {
        insertAtIndex( SequenceConstructor.SEQUENCE_ELEMENT, element , position );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceConstructor#appendSequenceChild(org.netbeans.modules.xslt.model.SequenceElement)
     */
    public void appendSequenceChild( SequenceElement element ) {
        appendChild( SequenceConstructor.SEQUENCE_ELEMENT, element );
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
        removeChild( SequenceConstructor.SEQUENCE_ELEMENT, element );
    }

}
