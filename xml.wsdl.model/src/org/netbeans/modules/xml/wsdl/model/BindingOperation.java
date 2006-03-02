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
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author rico
 * Represents an operation binding in the WSDL document. This is the
 * operation that is contained in the binding element
 */
public interface BindingOperation extends Named<WSDLComponent>, WSDLComponent {
    public static final String BINDING_OPERATION_PROPERTY = NAME_PROPERTY;
    public static final String BINDING_INPUT_PROPERTY = "input";
    public static final String BINDING_OUTPUT_PROPERTY = "output";
    public static final String BINDING_FAULT_PROPERTY = "fault";
    
    void setBindingInput(BindingInput bindingInput);
    BindingInput getBindingInput();
    void setBindingOutput(BindingOutput bindingOutput);
    BindingOutput getBindingOutput();
    void setOperation(Operation operation);
    Operation getOperation();
    void addBindingFault(BindingFault bindingFault);
    void removeBindingFault(BindingFault bindingFault);
    Collection<BindingFault> getBindingFaults();
}
