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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.policy;

import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.Ordered;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss11;

/**
 *
 * @author Martin Grebac
 */
public interface All extends NestedPolicyAllowed {
    public static final String ALL_PROPERTY = "ALL";     //NOI18N
    public static final String EXACTLYONE_PROPERTY = "EXACTLYONE";     //NOI18N
    public static final String POLICYREFERENCE_PROPERTY = "POLICYREFERENCE";     //NOI18N
    
    All getAll();
    void setAll(All all);
    void removeAll(All all);

    ExactlyOne getExactlyOne();
    void setExactlyOne(ExactlyOne exactlyOne);
    void removeExactlyOne(ExactlyOne exactlyOne);
    
    PolicyReference getPolicyReference();
    void setPolicyReference(PolicyReference policyReference);
    void removePolicyReference(PolicyReference policyReference);

    RMAssertion getRMAssertion();
    void setRMAssertion(RMAssertion rmAssertion);
    void removeRMAssertion(RMAssertion rmAssertion);

    Wss11 getWss11();
    void setWss11(Wss11 wss11);
    void removeWss11(Wss11 wss11);

    Wss10 getWss10();
    void setWss10(Wss10 wss10);
    void removeWss10(Wss10 wss10);

    FlowControl getFlowControl();
    void setFlowControl(FlowControl flowControl);
    void removeFlowControl(FlowControl flowControl);

    Ordered getOrdered();
    void setOrdered(Ordered ordered);
    void removeOrdered(Ordered ordered);
}
