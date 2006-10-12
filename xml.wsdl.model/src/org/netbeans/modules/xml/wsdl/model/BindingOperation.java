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

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author rico
 * Represents an operation binding in the WSDL document. This is the
 * operation that is contained in the binding element
 */
public interface BindingOperation extends Nameable<WSDLComponent>, WSDLComponent {
    public static final String BINDING_OPERATION_PROPERTY = NAME_PROPERTY;
    public static final String BINDING_INPUT_PROPERTY = "input";
    public static final String BINDING_OUTPUT_PROPERTY = "output";
    public static final String BINDING_FAULT_PROPERTY = "fault";
    
    void setBindingInput(BindingInput bindingInput);
    BindingInput getBindingInput();
    void setBindingOutput(BindingOutput bindingOutput);
    BindingOutput getBindingOutput();
    
    /**
     * Set corresponding portType operationusing the given reference.
     * @param operation reference.
     */
    void setOperation(Reference<Operation> operation);
    
    /**
     * @return reference to the corresponding operation.
     */
    Reference<Operation> getOperation();
    
    void addBindingFault(BindingFault bindingFault);
    void removeBindingFault(BindingFault bindingFault);
    Collection<BindingFault> getBindingFaults();
}
