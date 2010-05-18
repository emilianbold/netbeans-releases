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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class MessageExchangeContainerImpl extends ExtensibleElementsImpl implements
        MessageExchangeContainer , AfterImport, AfterSources
{

    MessageExchangeContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    MessageExchangeContainerImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.MESSAGE_EXCHAGES.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#getMessageExchanges()
     */
    public MessageExchange[] getMessageExchanges() {
        readLock();
        try {
            List<MessageExchange> list = getChildren( MessageExchange.class );
            return list.toArray( new MessageExchange[ list.size() ]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#getMessageExchange(int)
     */
    public MessageExchange getMessageExchange( int i ) {
        return getChild( MessageExchange.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#setMessageExchange(org.netbeans.modules.bpel.model.api.MessageExchange, int)
     */
    public void setMessageExchange( MessageExchange exchange, int i ) {
        setChildAtIndex( exchange , MessageExchange.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#insertMessageExchange(org.netbeans.modules.bpel.model.api.MessageExchange, int)
     */
    public void insertMessageExchange( MessageExchange exchange, int i ) {
        insertAtIndex( exchange , MessageExchange.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#removeMessageExchange(int)
     */
    public void removeMessageExchange( int i ) {
        removeChild( MessageExchange.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#addMessageExchange(org.netbeans.modules.bpel.model.api.MessageExchange)
     */
    public void addMessageExchange( MessageExchange exchange ) {
        addChildBefore( exchange , MessageExchange.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#setMessageExchanges(org.netbeans.modules.bpel.model.api.MessageExchange[])
     */
    public void setMessageExchanges( MessageExchange[] exchanges ) {
        setArrayBefore( exchanges , MessageExchange.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.MessageExchangeContainer#sizeOfMessageExchanges()
     */
    public int sizeOfMessageExchanges() {
        readLock();
        try {
            return getChildren( MessageExchange.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return MessageExchangeContainer.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#accept(org.netbeans.modules.bpel.model.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.MESSAGE_EXCHAGE.getName().equals( element.getLocalName())){
            return new MessageExchangeImpl( getModel() , element );
        }
        return super.create(element);
    }
}
