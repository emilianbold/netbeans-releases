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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementValue;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementVariable;
import org.netbeans.modules.bpel.debugger.BreakPosition;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.SchemaViolationException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XpathExpressionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Alexander Zgursky
 */
public class XmlElementVariableImpl extends VariableSupport implements XmlElementVariable {
    private Value myValue;
    private boolean myValueIsInitialized;
    
    /** Creates a new instance of XmlElementVariableImpl */
    public XmlElementVariableImpl(String name, BreakPosition breakPosition, BPELVariable engineVariable) {
        super(name, breakPosition, engineVariable);
    }

    public Value getValue() {
        if (!myValueIsInitialized) {
            myValueIsInitialized = true;
            String valueAsString = getEngineVariable().getXSDData();
            if (valueAsString != null) {
                Element element = Util.parseXmlElement(valueAsString);
                if (element != null) {
                    myValue = new XmlElementValueImpl(element, this);
                } else {
                    //TODO:handle this properly
//                    System.out.println("Was expecting an xml value for variable " +
//                            getName() + ", but couldn't parse the value: " +
//                            valueAsString);
                }
            }
        }
        
        return myValue;
    }

    public void setNodeValue(Node node, String newValue) {
        DebuggableEngine varContext = getBreakPosition().getFrame().getDebuggableEngine();
        String xpath = XmlElementValue.Helper.xpath(node);
        try {
            varContext.changeVariableSchemaTypeValue(getName(), xpath, newValue);
            
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
}
