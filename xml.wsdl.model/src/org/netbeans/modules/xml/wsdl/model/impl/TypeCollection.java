/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author nn136682
 */
public enum TypeCollection {
    ALL(createAll()),
    DOCUMENTATION(createDocumentation()),
    FOR_IMPORT(createListForImport()),
    FOR_TYPES(createListForTypes()),
    FOR_MESSAGE(createListForMessage()),
    FOR_PORTTYPE(createListForPortType()),
    FOR_BINDING(createListForBinding()),
    FOR_SERVICE(createListForService()),
    DOCUMENTATION_OUTPUT(createDocumentationOutputList()),
    DOCUMENTATION_INPUT(createDocumentationInputList()),
    DOCUMENTATION_EXTENSIBILITY_BINDINDINPUT(createListForBindingOnput());
    
    private Collection<Class<? extends WSDLComponent>> types;
    TypeCollection(Collection<Class<? extends WSDLComponent>> types) {
        this.types = types;
    }
    public Collection<Class<? extends WSDLComponent>> types() { return types; }
    
    static Collection <Class<? extends WSDLComponent>> createAll() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(WSDLComponent.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createDocumentation() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        return c;
    }
    
    static Collection<Class<? extends WSDLComponent>> createListForImport() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForTypes() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForMessage() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForPortType() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForBinding() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        c.add(PortType.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForService() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Import.class);
        c.add(Types.class);
        c.add(Message.class);
        c.add(PortType.class);
        c.add(Binding.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createDocumentationOutputList() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Output.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createDocumentationInputList() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(Input.class);
        return c;
    }

    static Collection<Class<? extends WSDLComponent>> createListForBindingOnput() {
        Collection<Class<? extends WSDLComponent>> c = new ArrayList<Class<? extends WSDLComponent>>();
        c.add(Documentation.class);
        c.add(ExtensibilityElement.class);
        c.add(BindingInput.class);
        return c;
    }

}
