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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by
 * @author ads
 */
public class BPELComponentFactory {
    private WSDLModel model;
    
    /** Creates a new instance of BPELComponentFactory */
    public BPELComponentFactory(WSDLModel model) {
        this.model = model;
    }
    
    public CorrelationProperty createCorrelationProperty( WSDLComponent context )
    {
        return (CorrelationProperty) model.getFactory().create(context,
                BPELQName.PROPERTY.getQName());
    }
    
    public PropertyAlias createPropertyAlias( WSDLComponent context ) {
        return (PropertyAlias) model.getFactory().create(context,
                BPELQName.PROPERTY_ALIAS.getQName());
    }

    public PartnerLinkType createPartnerLinkType( WSDLComponent context ) {
        return (PartnerLinkType) model.getFactory().create(context,
                BPELQName.PARTNER_LINK_TYPE.getQName());
    }

    public Role createRole( WSDLComponent context ) {
        return (Role) model.getFactory().create(context,
                BPELQName.ROLE.getQName());
    }
    
    public Query createQuery( WSDLComponent context ){
        return (Query) model.getFactory().create(context,
                BPELQName.QUERY.getQName());
    }
    
    public Documentation createDocumentation(WSDLComponent context) {
        QName qName = null;
        if ( context instanceof AbstractDocumentComponent ) {
            qName = ((AbstractDocumentComponent)context).getQName();
        }
        else {
            throw new IllegalStateException("Couldn't create child " +   // NOI18N
                    "documentation for unknown implementation parent");  // NOI18N
        }
        assert qName != null;
        if ( BPELQName.VARPROP_NS.equals( qName.getNamespaceURI() )) {
            return (Documentation) model.getFactory().create(context,
                    BPELQName.DOCUMENTATION_VARPROP.getQName());
        }
        else if ( BPELQName.PLNK_NS.equals( qName.getNamespaceURI() )) {
            return (Documentation) model.getFactory().create(context,
                    BPELQName.DOCUMENTATION_PLNK.getQName());
        }
        else {
            assert false;
            return null;
        }
    }
}
