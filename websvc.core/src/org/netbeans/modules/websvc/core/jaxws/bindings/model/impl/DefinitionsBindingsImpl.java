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

import java.util.Collections;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponent;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.DefinitionsBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class DefinitionsBindingsImpl extends BindingsComponentImpl 
             implements DefinitionsBindings{
    
    /** Creates a new instance of DefinitionsBindingsImpl */
    public DefinitionsBindingsImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public DefinitionsBindingsImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.BINDINGS.getQName(), model));
    }

    public void setNode(String node) {
        setAttribute(NODE_PROPERTY, BindingsAttribute.NODE, node);
    }

    public void setHandlerChains(BindingsHandlerChains handlerChains) {
        java.util.List<Class<? extends BindingsComponent>> classes = Collections.emptyList();
        setChild(BindingsHandlerChains.class,
                HANDLER_CHAINS_PROPERTY, handlerChains, classes);
    }

    public String getNode() {
        return getAttribute(BindingsAttribute.NODE);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAXWS_NS_URI;
    }

    public BindingsHandlerChains getHandlerChains() {
        return getChild(BindingsHandlerChains.class);
    }
    
}
