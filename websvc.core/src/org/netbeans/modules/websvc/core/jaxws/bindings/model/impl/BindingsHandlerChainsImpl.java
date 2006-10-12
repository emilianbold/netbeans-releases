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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.core.jaxws.bindings.model.impl;

import java.util.Collection;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerChainsImpl extends BindingsComponentImpl implements
       BindingsHandlerChains{
    
    /**
     * Creates a new instance of BindingsHandlerChainsImpl
     */
    public BindingsHandlerChainsImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public BindingsHandlerChainsImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.HANDLER_CHAINS.getQName(), model));
    }

    public void removeHandlerChain(BindingsHandlerChain chain) {
        removeChild(HANDLER_CHAIN_PROPERTY, chain);
    }

    public void addHandlerChain(BindingsHandlerChain chain) {
        appendChild(HANDLER_CHAIN_PROPERTY, chain);
    }

    public Collection<BindingsHandlerChain> getHandlerChains() {
        return getChildren(BindingsHandlerChain.class);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }
    
}
