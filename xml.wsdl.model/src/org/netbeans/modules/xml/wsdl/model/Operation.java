/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author rico
 * Represents a WSDL operation
 */
public interface Operation extends Named<WSDLComponent>, WSDLComponent {
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
