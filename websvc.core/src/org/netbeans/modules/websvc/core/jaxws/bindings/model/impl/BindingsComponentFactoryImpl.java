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

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponent;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponentFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerClass;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.DefinitionsBindings;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsComponentFactoryImpl 
          implements BindingsComponentFactory{
    
    private BindingsModelImpl model;
    /** Creates a new instance of BindingsComponentFactoryImpl */
    public BindingsComponentFactoryImpl(BindingsModel model) {
        if (model instanceof BindingsModelImpl) {
            this.model = (BindingsModelImpl) model;
        } else {
            throw new IllegalArgumentException("Excpect BindingsModelImpl");
        }
    }

    public BindingsComponent create(Element e, BindingsComponent parent) {
        //TODO implement Visitor to get rid of this humongous if-else block
        QName childQName = new QName(e.getNamespaceURI(), e.getLocalName());
        if(childQName.equals(BindingsQName.BINDINGS.getQName())){
            if(parent instanceof GlobalBindings){
                return new DefinitionsBindingsImpl(model, e);
            }
            else{
                return new GlobalBindingsImpl(model, e);
            }
        }
        if(childQName.equals(BindingsQName.HANDLER_CHAINS.getQName())){
            return new BindingsHandlerChainsImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CHAIN.getQName())){
            return new BindingsHandlerChainImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER.getQName())){
            return new BindingsHandlerImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CLASS.getQName())){
            return new BindingsHandlerClassImpl(model, e);
        }
        return null;
    }

    public BindingsHandlerClass createHandlerClass() {
        return new BindingsHandlerClassImpl(model);
    }

    public BindingsHandlerChains createHandlerChains() {
        return new BindingsHandlerChainsImpl(model);
    }

    public BindingsHandlerChain createHandlerChain() {
        return new BindingsHandlerChainImpl(model);
    }

    public GlobalBindings createGlobalBindings() {
        return new GlobalBindingsImpl(model);
    }

    public DefinitionsBindings createDefinitionsBindings() {
        return new DefinitionsBindingsImpl(model);
    }

    public BindingsHandler createHandler() {
        return new BindingsHandlerImpl(model);
    }
    
}
