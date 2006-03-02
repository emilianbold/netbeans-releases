/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
