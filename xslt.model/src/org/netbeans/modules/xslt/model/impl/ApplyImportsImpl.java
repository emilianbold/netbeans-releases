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

import org.netbeans.modules.xslt.model.ApplyImports;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ApplyImportsImpl extends XslComponentImpl implements ApplyImports {

    ApplyImportsImpl( XslModelImpl model, Element e ) {
        super(model, e);
    }
    
    ApplyImportsImpl( XslModelImpl model ){
        this( model , createNewElement( XslElements.APPLY_IMPORTS, model));
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
        return ApplyImports.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.WithParamContainer#addWithParam(org.netbeans.modules.xslt.model.WithParam, int)
     */
    public void addWithParam( WithParam withParam, int position ) {
        insertAtIndex( WITH_PARAM, withParam, position);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.WithParamContainer#appendWithParam(org.netbeans.modules.xslt.model.WithParam)
     */
    public void appendWithParam( WithParam withParam ) {
        appendChild( WITH_PARAM, withParam);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.WithParamContainer#getWithParams()
     */
    public List<WithParam> getWithParams() {
        return getChildren( WithParam.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.WithParamContainer#removeWithParam(org.netbeans.modules.xslt.model.WithParam)
     */
    public void removeWithParam( WithParam withParam ) {
        removeChild( WITH_PARAM, withParam);
    }

}
