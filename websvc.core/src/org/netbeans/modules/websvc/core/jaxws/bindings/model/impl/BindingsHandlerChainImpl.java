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
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerChainImpl extends 
        BindingsComponentImpl implements BindingsHandlerChain{
    
    /**
     * Creates a new instance of BindingsHandlerChainImpl
     */
    public BindingsHandlerChainImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public BindingsHandlerChainImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.HANDLER_CHAIN.getQName(), model));
    }

    public void removeHandler(BindingsHandler handler) {
        removeChild(HANDLER_PROPERTY, handler);
    }

    public void addHandler(BindingsHandler handler) {
        appendChild(HANDLER_PROPERTY, handler);
    }

    public Collection<BindingsHandler> getHandlers() {
        return getChildren(BindingsHandler.class);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }
}
