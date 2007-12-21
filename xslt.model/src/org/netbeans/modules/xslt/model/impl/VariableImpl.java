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

import javax.xml.namespace.QName;

import org.netbeans.modules.xslt.model.Variable;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class VariableImpl extends AsSeqElementCtorImpl implements Variable {


    VariableImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    VariableImpl( XslModelImpl model ) {
        super( model , XslElements.VARIABLE );
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
        return Variable.class;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.QualifiedNameable#getName()
     */
    public QName getName() {
        return QNameBuilder.createQName( this , XslAttributes.NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.QualifiedNameable#setName(javax.xml.namespace.QName)
     */
    public void setName( QName name ) {
        setAttribute( XslAttributes.NAME, name );
    }

}
