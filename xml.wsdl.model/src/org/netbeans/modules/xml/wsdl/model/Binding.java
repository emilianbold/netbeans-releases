/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author rico
 * Represents a binding in the WSDL document
 */
public interface Binding extends ReferenceableWSDLComponent {
    public static final String BINDING_OPERATION_PROPERTY = "operation";
    public static final String TYPE_PROPERTY = "type";
    
    void setType(GlobalReference<PortType> portType);
    GlobalReference<PortType> getType();
    
    void addBindingOperation(BindingOperation bindingOperation);
    void removeBindingOperation(BindingOperation bindingOperation);
    Collection<BindingOperation> getBindingOperations();
}
