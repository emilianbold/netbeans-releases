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

package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.support.TBoolean;

public interface BaseScope extends ActivityHolder, JoinFailureSuppressor ,
    VariableDeclarationScope
{

    /**
     * exitOnStandardFault attribute name.
     */
    String EXIT_ON_STANDART_FAULT = "exitOnStandardFault"; // NOI18N

    /**
     * Gets the value of the variables property.
     *
     * @return possible object is {@link VariableContainer }
     */
    VariableContainer getVariableContainer();

    /**
     * Sets the value of the variables property.
     * 
     * @param value
     *            allowed object is {@link VariableContainer }
     */
    void setVariableContainer( VariableContainer value );

    /**
     * Gets the value of the correlationSets property.
     * 
     * @return possible object is {@link CorrelationSetContainer }
     */
    CorrelationSetContainer getCorrelationSetContainer();

    /**
     * Sets the value of the correlationSets property.
     * 
     * @param value
     *            allowed object is {@link CorrelationSetContainer }
     */
    void setCorrelationSetContainer( CorrelationSetContainer value );

    /**
     * Gets the value of the faultHandlers property.
     * 
     * @return possible object is {@link FaultHandlers }
     */
    FaultHandlers getFaultHandlers();

    /**
     * Sets the value of the faultHandlers property.
     * 
     * @param value
     *            allowed object is {@link FaultHandlers }
     */
    void setFaultHandlers( FaultHandlers value );

    /**
     * Gets the value of the eventHandlers property.
     * 
     * @return possible object is {@link EventHandlers }
     */
    EventHandlers getEventHandlers();

    /**
     * Sets the value of the eventHandlers property.
     * 
     * @param value
     *            allowed object is {@link EventHandlers }
     */
    void setEventHandlers( EventHandlers value );

    /**
     * Removes VariableContainer element from this entity.
     */
    void removeVariableContainer();

    /**
     * Removes FaultHandlers element from this entity.
     */
    void removeFaultHandlers();

    /**
     * Removes EventHandlers element from this entity.
     */
    void removeEventHandlers();

    /**
     * Removes CorrelationSetContainer element from this entity.
     */
    void removeCorrelationSetContainer();

    /**
     * Setter for attribute exitOnStandardFault.
     * 
     * @param value
     *            TBoolean value for attribute.
     */
    void setExitOnStandardFault( TBoolean value );

    /**
     * Getter for attribute exitOnStandardFault.
     * 
     * @return "exitOnStandardFault" attribute value.
     */
    TBoolean getExitOnStandardFault();

    /**
     * Removes attribute exitOnStandardFault.
     */
    void removeExitOnStandardFault();

    /**
     * Gets the value of the partnerLinks property.
     * 
     * @return possible object is {@link PartnerLinkContainer }
     */
    PartnerLinkContainer getPartnerLinkContainer();

    /**
     * Sets the value of the partnerLinks property.
     * 
     * @param value
     *            allowed object is {@link PartnerLinkContainer }
     */
    void setPartnerLinkContainer( PartnerLinkContainer value );

    /**
     * Removes partnerLinks.
     */
    void removePartnerLinkContainer();
    
    /**
     * Set the new MessageExchangeContainer child.
     * @param container New MessageExchangeContainer child.
     */
    void setMessageExchangeContainer( MessageExchangeContainer container );
    
    /**
     * @return MessageExchangeContainer child.
     */
    MessageExchangeContainer getMessageExchangeContainer();
    
    /**
     * Removes MessageExchangeContainer child.
     */
    void removeMessageExchangeContainer();

}
