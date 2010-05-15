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

import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageValue;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageVariable;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementValue;
import org.netbeans.modules.bpel.debugger.BreakPosition;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.SchemaViolationException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.WSDLMessage;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XpathExpressionException;
import org.w3c.dom.Node;

/**
 *
 * @author Alexander Zgursky
 */
public class WsdlMessageVariableImpl extends VariableSupport implements WsdlMessageVariable {
    private Value myValue;
    private boolean myValueIsInitialized;
    
    /** Creates a new instance of WsdlMessageVariableImpl */
    public WsdlMessageVariableImpl(String name, BreakPosition breakPosition, BPELVariable engineVariable) {
        super(name, breakPosition, engineVariable);
        assert engineVariable.isWSDLMessage() : "Provided engine variable should be a WSDL Message Variable";
    }
    
    public Value getValue() {
        if (!myValueIsInitialized) {
            myValueIsInitialized = true;
        
            WSDLMessage engineWsdlMessage = getEngineVariable().getWSDLMessage();
            if (engineWsdlMessage != null) {
                myValue = new WsdlMessageValueImpl(getBreakPosition(), this);
            }
        }
        return myValue;
    }

    public void setPartNodeValue(WsdlMessageValue.Part part, Node node, String newValue) {
        //System.out.println("Trying to set part node value for " + this);
        
        DebuggableEngine varContext = getBreakPosition().getFrame().getDebuggableEngine();
        String xpath = XmlElementValue.Helper.xpath(node);
        try {
            varContext.changeVariableMessageTypeValue(getName(), part.getName(), xpath, newValue);
            
            myValueIsInitialized = false;
        } catch (XpathExpressionException ex) {
            //TODO:handle this
            ex.printStackTrace();
        } catch (SchemaViolationException ex) {
            //TODO:handle this
            ex.printStackTrace();
        } catch (Exception e) {
            // almost anything can be thrown..
            e.printStackTrace();
        }
    }
    
    public void setPartSimpleValue(WsdlMessageValue.Part part, String newValue) {
        DebuggableEngine varContext = getBreakPosition().getFrame().getDebuggableEngine();
        
        try {
            varContext.changeVariableMessageTypeValue(getName(), part.getName(), newValue);
        } catch (SchemaViolationException ex) {
            //TODO:handle this
            ex.printStackTrace();
        }
    }
}
