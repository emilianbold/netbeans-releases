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
package org.netbeans.modules.bpel.model.impl.services;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Link;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 * This service checks for unique name definition in the same scope.
 */
public class UniqueNameCheck extends InnerEventDispatcherAdapter {

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent) {
            PropertyUpdateEvent ev = (PropertyUpdateEvent) event;
            if ( event.getParent().getBpelModel().inSync() ){
                return false;
            }
            String attributeName = ev.getName();
            if ( ((PropertyUpdateEvent)event).getNewValue() ==  null ){
                return false;
            }
            return attributeName.equals( NamedElement.NAME )
                && ( event.getParent() instanceof Variable || 
                        event.getParent() instanceof CorrelationSet ||
                        event.getParent() instanceof PartnerLink ||
                        event.getParent() instanceof MessageExchange ||
                        event.getParent() instanceof Link ) ;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) throws VetoException {
        BpelContainer parent = event.getParent().getParent();
        if  ( parent != null ) { // parent can be null for element that is not in tree.
            
            for ( BpelEntity child : parent.getChildren( 
                    event.getParent().getElementType())) 
            {
                if ( !(child instanceof NamedElement) || 
                        child.equals( event.getParent()) )
                {
                    continue;
                }
                NamedElement kid = (NamedElement) child;    
                if ( ((PropertyUpdateEvent)event).getNewValue().equals( 
                        kid.getName() ))
                {
                    String str = Utils.getResourceString( 
                            getError( child.getElementType()) , kid.getName() );
                    throw new VetoException( str , event );
                }
            }
        }
    }
    
    private String getError( Class<? extends BpelEntity> clazz ){
        return LazyInit.ERROR_BUILDERS.get( clazz ).getError(); 
    }

    private static class LazyInit {
        private static final Map<Class<? extends BpelEntity>,
            ErrorMessageBuilder> ERROR_BUILDERS = new HashMap<
                Class<? extends BpelEntity>,ErrorMessageBuilder>();
        
        static {
            ERROR_BUILDERS.put( Variable.class , new VariableErrorBuilder() );
            ERROR_BUILDERS.put( CorrelationSet.class , 
                    new CorrelationSetErrorBuilder());
            ERROR_BUILDERS.put( PartnerLink.class , new PartnerLinkErrorBuilder());
            ERROR_BUILDERS.put( MessageExchange.class, 
                    new MessageExchangeErrorBuilder());
            ERROR_BUILDERS.put( Link.class, new LinkErrorBuilder());
        }
    }
}

interface ErrorMessageBuilder {
    String getError( );
}

class VariableErrorBuilder implements ErrorMessageBuilder {
    
    public String getError( ) {
        return Utils.BAD_VARIABLE_NAME;
    }
}

class CorrelationSetErrorBuilder implements ErrorMessageBuilder {
    
    public String getError( ) {
        return Utils.BAD_CORRELATION_SET_NAME;
    }
}

class PartnerLinkErrorBuilder implements ErrorMessageBuilder {
    
    public String getError( ) {
        return Utils.BAD_PARTNER_LINK_NAME;
    }
}

class MessageExchangeErrorBuilder implements ErrorMessageBuilder {
    
    public String getError( ) {
        return Utils.BAD_MESSAGE_EXCHANGE_NAME;
    }
}

class LinkErrorBuilder implements ErrorMessageBuilder {
    
    public String getError( ) {
        return Utils.BAD_LINK_NAME;
    }
}
