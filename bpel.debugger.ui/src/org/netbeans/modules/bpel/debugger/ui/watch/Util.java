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

package org.netbeans.modules.bpel.debugger.ui.watch;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.variables.SimpleValue;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.Variable;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageValue;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementValue;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.modules.bpel.debugger.ui.util.XmlUtil;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ksorokin
 */
public class Util {
    private BpelDebugger myDebugger;
    private VariablesUtil myHelper;
    
    public Util(
            final BpelDebugger debugger) {
        myDebugger = debugger;
        myHelper = new VariablesUtil(debugger);
    }

    public String toString(
            final Node node) {
        if (node instanceof Element) {
            final Element element = (Element) node;
            final NodeList children = element.getChildNodes();
            
            if (XmlUtil.isTextOnlyNode(element)) {
                return children.item(0).getNodeValue();
            } else {
                return XmlUtil.toString(element);
            }
        } else {
            final String nodeValue = node.getNodeValue();
            
            return nodeValue != null ? nodeValue : "";
        }
    }

    public Object toString(
            final Value value) throws UnknownTypeException {
        if (value instanceof SimpleValue) {
            return ((SimpleValue) value).getValueAsString();
        }
        
        if (value instanceof XmlElementValue) {
            return toString(((XmlElementValue) value).getElement());
        }
        
        return "";
    }
    
    public Variable getVariable(
            final String name) {
        final ProcessInstance processInstance = 
                myDebugger.getCurrentProcessInstance();

        if (processInstance == null) {
            return null;
        }
        
        for (Variable variable: processInstance.getVariables()) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        
        return null;
    }
    
    public Object getValue(
            final String expression) {
        final int dotIndex = expression.indexOf(".");

        final String variableName = dotIndex > -1 ? 
                expression.substring(1, dotIndex) : 
                expression.substring(1);
        final String xpath = dotIndex > -1 ?
                expression.substring(dotIndex + 1) :
                null;
        
        final Variable variable = getVariable(variableName);
        
        if (variable == null) {
            return null;
        }
        
        if (xpath == null) {
            return myHelper.getValue(variable);
        }
        
        final Value object = variable.getValue();
        
        if (object instanceof WsdlMessageValue) {
            final WsdlMessageValue value = (WsdlMessageValue) object;
            final int slashIndex = xpath.indexOf("/");
            
            if (slashIndex == -1) {
                return null;
            }
            
            final String partName = xpath.substring(0, slashIndex);
            final String properPath = xpath.substring(slashIndex + 1);
            
            WsdlMessageValue.Part part = null;
            for (WsdlMessageValue.Part temp: value.getParts()) {
                if (temp.getName().equals(partName)) {
                    part = temp;
                    break;
                }
            }
            
            if (part == null) {
                return null;
            }
            
            if (part.getValue() instanceof XmlElementValue) {
                return getValue(
                        (XmlElementValue) part.getValue(), properPath);
            } else {
                return null;
            }
        }
        
        if (object instanceof XmlElementValue) {
            return getValue((XmlElementValue) object, xpath);
        }
        
        return null;
    }
    
    public Object getValue(
            final XmlElementValue value, 
            final String xpath) {
        final Element element = value.getElement();
        
        try {
            return XPathFactory.
                    newInstance().newXPath().evaluate(xpath, element);
        } catch (XPathExpressionException ex) {
            return null;
        }
    }
    
    public Object[] getChildren(
            final String expression) {
        final int dotIndex = expression.indexOf(".");

        final String variableName = dotIndex > -1 ? 
                expression.substring(1, dotIndex) : 
                expression.substring(1);
        final String xpath = dotIndex > -1 ?
                expression.substring(dotIndex + 1) :
                null;
        
        final Variable variable = getVariable(variableName);
        
        if (variable == null) {
            return new Object[0];
        }
        
        if (xpath == null) {
            return myHelper.getChildren(variable);
        }
        
        final Value object = variable.getValue();
        
        if (object instanceof WsdlMessageValue) {
            final WsdlMessageValue value = (WsdlMessageValue) object;
            final int slashIndex = xpath.indexOf("/");
            
            if (slashIndex == -1) {
                return new Object[0];
            }
            
            final String partName = xpath.substring(0, slashIndex);
            final String properPath = xpath.substring(slashIndex + 1);
            
            WsdlMessageValue.Part part = null;
            for (WsdlMessageValue.Part temp: value.getParts()) {
                if (temp.getName().equals(partName)) {
                    part = temp;
                    break;
                }
            }
            
            if (part == null) {
                return new Object[0];
            }
            
            if (part.getValue() instanceof XmlElementValue) {
                return myHelper.getChildren(getValue(
                        (XmlElementValue) part.getValue(), properPath));
            } else {
                return new Object[0];
            }
        }
        
        if (object instanceof XmlElementValue) {
            return myHelper.getChildren(
                    getValue((XmlElementValue) object, xpath));
        }
        
        return new Object[0];
    }
    
    public Object[] getChildren(
            final XmlElementValue value) {
        return myHelper.getChildren(value.getElement());
    }
    
    public VariablesUtil getVariablesUtil() {
        return myHelper;
    }
}
