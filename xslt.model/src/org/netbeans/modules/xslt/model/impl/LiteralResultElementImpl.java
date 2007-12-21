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

import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class LiteralResultElementImpl extends SequenceElementConstructorImpl implements
        LiteralResultElement
{

    LiteralResultElementImpl( XslModelImpl model, Element e ) {
        super(model, e);
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
        return LiteralResultElement.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.LiteralResultElement#getAttribute(java.lang.String)
     */
    public String getAttribute( String attribute ) {
        return getAttribute(new StringAttribute(attribute));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.LiteralResultElement#setAttribute(java.lang.String, java.lang.String)
     */
    public void setAttribute( String attribute, String value ) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    static class StringAttribute implements Attribute {

        StringAttribute( String name ) {
            myName = name;
        }

        public Class getType() {
            return String.class;
        }

        public String getName() {
            return myName;
        }

        public Class getMemberType() {
            return null;
        }
        
        private String myName;
    }

}
