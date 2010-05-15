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

import org.netbeans.modules.xslt.model.Param;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ParamImpl extends QNameableSequenceConstructor implements Param {

    ParamImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }

    ParamImpl( XslModelImpl model ) {
        super( model , XslElements.PARAM );
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
        return Param.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Param#getRequired()
     */
    public TBoolean getRequired() {
        return TBoolean.forString( getAttribute( XslAttributes.REQUIRED ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Param#getTunnel()
     */
    public TBoolean getTunnel() {
        return TBoolean.forString( getAttribute( XslAttributes.TUNNEL));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Param#setRequired(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setRequired( TBoolean required ) {
        setAttribute( XslAttributes.REQUIRED, required );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Param#setTunnel(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setTunnel( TBoolean tunnel ) {
        setAttribute( XslAttributes.TUNNEL, tunnel );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AsSpec#getAs()
     */
    public String getAs() {
        return getAttribute( XslAttributes.AS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AsSpec#setAs(java.lang.String)
     */
    public void setAs( String value ) {
        setAttribute( XslAttributes.AS, value );
    }

}
