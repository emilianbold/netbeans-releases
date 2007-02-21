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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaConnectionImpl extends CasaComponentImpl implements CasaConnection {
    
    /** Creates a new instance of CasaConnectionImpl */
    public CasaConnectionImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaConnectionImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.CONNECTION));
    }

    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
        
    public String getState() {
        return getAttribute(CasaAttribute.STATE);
    }

    public void setState(String state) {
        setAttribute(STATE_PROPERTY, CasaAttribute.STATE, state);
    }
    
    public NamedComponentReference<CasaEndpoint> getProvider() {
        NamedComponentReference<CasaEndpoint> ret =
                resolveGlobalReference(CasaEndpoint.class, CasaAttribute.PROVIDER);
        return ret;
    }

    public void setProvider(NamedComponentReference<CasaEndpoint> endpoint) {
        setAttribute(PROVIDER_PROPERTY, CasaAttribute.PROVIDER, endpoint);
    }
    
    public NamedComponentReference<CasaEndpoint> getConsumer() {
        NamedComponentReference<CasaEndpoint> ret =
                resolveGlobalReference(CasaEndpoint.class, CasaAttribute.CONSUMER);
        return ret;
    }

    public void setConsumer(NamedComponentReference<CasaEndpoint> endpoint) {
        setAttribute(CONSUMER_PROPERTY, CasaAttribute.CONSUMER, endpoint);
    }
}
