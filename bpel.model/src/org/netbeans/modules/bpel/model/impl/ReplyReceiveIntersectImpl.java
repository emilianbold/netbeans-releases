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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeReference;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public abstract class ReplyReceiveIntersectImpl extends InvokeReceiveReplyCommonImpl
        implements VariableReference, MessageExchangeReference
{

    ReplyReceiveIntersectImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ReplyReceiveIntersectImpl( BpelBuilderImpl builder, String tagName )
    {
        super(builder, tagName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#getVariable()
     */
    public BpelReference<VariableDeclaration> getVariable() {
        return getBpelReference( BpelAttributes.VARIABLE_REF , 
                VariableDeclaration.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#removeVariable()
     */
    public void removeVariable() {
        removeReference( BpelAttributes.VARIABLE_REF );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableSpec#setVariable(org.netbeans.modules.soa.model.bpel20.references.VariableReference)
     */
    public void setVariable( BpelReference<VariableDeclaration> value ) {
        setBpelReference( BpelAttributes.VARIABLE_REF ,value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#getMessageExchange()
     */
    public BpelReference<MessageExchange> getMessageExchange() {
        return getBpelReference( BpelAttributes.MESSAGE_EXCHANGE , 
                MessageExchange.class);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#setMessageExchange(java.lang.String)
     */
    public void setMessageExchange( BpelReference<MessageExchange> ref ) {
        setBpelReference( BpelAttributes.MESSAGE_EXCHANGE , ref );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.MessageExchangeSpec#removeMessageExchange()
     */
    public void removeMessageExchange() {
        removeReference( BpelAttributes.MESSAGE_EXCHANGE );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences(){
        Reference[] refs = super.getReferences();
        Reference[] ret = new Reference[ refs.length +2 ];
        System.arraycopy( refs, 0 , ret , 2 , refs.length );
        ret[0] = getVariable();
        ret[1] = getMessageExchange();
        return ret;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.VARIABLE_REF;
            ret[ 1 ] = BpelAttributes.MESSAGE_EXCHANGE; // Fix for #81457
            myAttributes.compareAndSet( null,  ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
