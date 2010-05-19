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

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.XslModelReference;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;
import org.w3c.dom.Element;

/**
 * @author ads
 *
 */
abstract class HrefSpecImpl extends XslComponentImpl implements XslModelReference {

    HrefSpecImpl( XslModelImpl model, Element e ) {
        super(model, e);
    }
    
    HrefSpecImpl( XslModelImpl model, XslElements type ) {
        super(model, type);
    }

    public String getHref() {
        return getAttribute( XslAttributes.HREF );
    }

    public void setHref( String href ) {
        setAttribute( XslAttributes.HREF, href );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslModelReference#resolveReferencedModel()
     */
    public XslModel resolveReferencedModel() throws CatalogModelException {
        ModelSource ms = resolveModel( getHref() );
        return XslModelFactory.XslModelFactoryAccess.getFactory().getModel( ms );
    }

}