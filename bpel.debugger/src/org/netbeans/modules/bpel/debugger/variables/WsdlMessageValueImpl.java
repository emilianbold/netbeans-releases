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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.variables;

import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageValue;
import org.netbeans.modules.bpel.debugger.BreakPosition;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.WSDLMessage;
import org.w3c.dom.Element;

/**
 *
 * @author Alexander Zgursky
 */

public class WsdlMessageValueImpl implements WsdlMessageValue {
    private BreakPosition myBreakPosition;
    private WsdlMessageVariableImpl myVariable;
    private WsdlMessageValue.Part[] myParts;
    //Map<String, Value> myPartValues = new HashMap<String, Value>();
    
    /** Creates a new instance of WsdlMessageValueImpl */
    public WsdlMessageValueImpl(BreakPosition breakPosition, WsdlMessageVariableImpl variable) {
        myBreakPosition = breakPosition;
        myVariable = variable;
    }

    public WsdlMessageValue.Part[] getParts() {
        if (myParts == null) {
            BPELVariable engineVariable = myVariable.getEngineVariable();
            WSDLMessage engineWsdlMessage = engineVariable.getWSDLMessage();
            String[] partNames = engineWsdlMessage.getParts();
            myParts = new WsdlMessageValue.Part[partNames.length];
            for (int i=0; i<partNames.length; i++) {
                String partName = partNames[i];
                myParts[i] = new PartImpl(partName);
            }
        }
        return myParts;
    }

    public NamedValueHost getValueHost() {
        return myVariable;
    }
    
    public class PartImpl implements WsdlMessageValue.Part {
        private String myName;
        private Value myValue;
        private boolean myValueIsInitialized;
        
        public PartImpl(String name) {
            myName = name;
        }

        public String getName() {
            return myName;
        }
        
        public Value getValue() {
            if (!myValueIsInitialized) {
                myValueIsInitialized = true;
                BPELVariable engineVariable = myVariable.getEngineVariable();
                WSDLMessage engineWsdlMessage = engineVariable.getWSDLMessage();

                String valueAsString = engineWsdlMessage.getPart(myName);
                if (valueAsString != null) {
                    Element element = Util.parseXmlElement(valueAsString);
                    if (element != null) {
                        myValue = new XmlElementValueImpl(element, this);
                    } else {
                        myValue = new SimpleValueImpl(valueAsString, this);
                    }
                }
            }

            return myValue;
        }

        public WsdlMessageValue getMessage() {
            return WsdlMessageValueImpl.this;
        }
    }
}