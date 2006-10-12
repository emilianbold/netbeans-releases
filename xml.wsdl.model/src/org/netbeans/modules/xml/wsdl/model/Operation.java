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
import java.util.List;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author rico
 * Represents a WSDL operation
 */
public interface Operation extends Nameable<WSDLComponent>, ReferenceableWSDLComponent, WSDLComponent {
    public static final String FAULT_PROPERTY = "fault";
    public static final String INPUT_PROPERTY = "input";
    public static final String OUTPUT_PROPERTY = "output";
    public static final String PARAMETER_ORDER_PROPERTY = "parameterOrder";
    
    Input getInput();
    void setInput(Input input);

    Output getOutput();
    void setOutput(Output output);
    
    void addFault(Fault fault);
    void removeFault(Fault fault);
    Collection<Fault> getFaults();
    
    //needed only in RPC type operations-shd we ignore this?
    void setParameterOrder(List<String> parameterOrder);
    List<String> getParameterOrder();
}
