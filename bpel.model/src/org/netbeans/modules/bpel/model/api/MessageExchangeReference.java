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
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.references.BpelReference;

/**
 * @author ads
 */
public interface MessageExchangeReference {

    String MESSAGE_EXCHANGE = "messageExchange";        // NOI18N

    /**
     * Getter for "messageExchange" attribute value.
     *
     * @return Reference to messageExchange element.
     */
    BpelReference<MessageExchange> getMessageExchange();

    /**
     * Setter for "messageExchange" attribute value.
     *
     * @param ref
     *            New "messageExchange" reference.
     */
    void setMessageExchange( BpelReference<MessageExchange> ref ) ;

    /**
     * Removes "messageExchange" attribute.
     */
    void removeMessageExchange();
}
