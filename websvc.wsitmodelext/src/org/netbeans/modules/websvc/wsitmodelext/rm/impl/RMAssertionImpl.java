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

package org.netbeans.modules.websvc.wsitmodelext.rm.impl;

import org.netbeans.modules.websvc.wsitmodelext.rm.AcknowledgementInterval;
import org.netbeans.modules.websvc.wsitmodelext.rm.BaseRetransmissionInterval;
import org.netbeans.modules.websvc.wsitmodelext.rm.ExponentialBackoff;
import org.netbeans.modules.websvc.wsitmodelext.rm.InactivityTimeout;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

import java.util.Collections;

/**
 *
 * @author Martin Grebac
 */
public class RMAssertionImpl extends RMComponentImpl implements RMAssertion {
    
    /**
     * Creates a new instance of RMAssertionImpl
     */
    public RMAssertionImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public RMAssertionImpl(WSDLModel model){
        this(model, createPrefixedElement(RMQName.RMASSERTION.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setBaseRetransmissionInterval(BaseRetransmissionInterval baseRetransmissionInterval) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(BaseRetransmissionInterval.class, BASE_RETRANSMISSION_INTERVAL_PROPERTY, baseRetransmissionInterval, classes);
    }

    public BaseRetransmissionInterval getBaseRetransmissionInterval() {
        return getChild(BaseRetransmissionInterval.class);
    }

    public void removeBaseRetransmissionInterval(BaseRetransmissionInterval baseRetransmissionInterval) {
        removeChild(BASE_RETRANSMISSION_INTERVAL_PROPERTY, baseRetransmissionInterval);
    }

    public void setInactivityTimeout(InactivityTimeout inactivityTimeout) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(InactivityTimeout.class, INACTIVITY_TIMEOUT_PROPERTY, inactivityTimeout, classes);
    }

    public InactivityTimeout getInactivityTimeout() {
        return getChild(InactivityTimeout.class);
    }

    public void removeInactivityTimeout(InactivityTimeout inactivityTimeout) {
        removeChild(INACTIVITY_TIMEOUT_PROPERTY, inactivityTimeout);
    }

    public void setAcknowledgementInterval(AcknowledgementInterval acknowledgementInterval) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AcknowledgementInterval.class, ACKNOWLEDGEMENT_INTERVAL_PROPERTY, acknowledgementInterval, classes);
    }

    public AcknowledgementInterval getAcknowledgementInterval() {
        return getChild(AcknowledgementInterval.class);
    }

    public void removeAcknowledgementInterval(AcknowledgementInterval acknowledgementInterval) {
        removeChild(ACKNOWLEDGEMENT_INTERVAL_PROPERTY, acknowledgementInterval);
    }

    public void setExponentialBackoff(ExponentialBackoff exponentialBackoff) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ExponentialBackoff.class, EXPONENTIAL_BACKOFF_PROPERTY, exponentialBackoff, classes);
    }

    public ExponentialBackoff getExponentialBackoff() {
        return getChild(ExponentialBackoff.class);
    }

    public void removeExponentialBackoff(ExponentialBackoff exponentialBackoff) {
        removeChild(EXPONENTIAL_BACKOFF_PROPERTY, exponentialBackoff);
    }
    
}
