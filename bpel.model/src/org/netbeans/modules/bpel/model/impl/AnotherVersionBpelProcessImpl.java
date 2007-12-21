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
package org.netbeans.modules.bpel.model.impl;

import java.util.List;

import org.netbeans.modules.bpel.model.api.AnotherVersionBpelComponent;
import org.netbeans.modules.bpel.model.api.AnotherVersionBpelProcess;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class AnotherVersionBpelProcessImpl extends 
    AbstractDocumentComponent<AnotherVersionBpelComponent> 
    implements AnotherVersionBpelProcess
{


    AnotherVersionBpelProcessImpl( AbstractDocumentModel model, Element element ) {
        super(model, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute, java.lang.String)
     */
    @Override
    protected Object getAttributeValueOf( Attribute attr, String stringValue )
    {
        // this method should not be called at all
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#populateChildren(java.util.List)
     */
    @Override
    protected void populateChildren( List<AnotherVersionBpelComponent> children )
    {
        // we don't provide children for this component
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.AnotherVersionBpelComponent#getNamespaceUri()
     */
    public String getNamespaceUri() {
        Element element = getPeer();
        if ( element == null ) {
            return null;
        }
        else {
            return element.getNamespaceURI();
        }
    }


}
