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


package org.netbeans.modules.wsdlextensions.hl7.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControl;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControls;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.w3c.dom.Element;

/**
 * @author Nageswara.Samudrala@Sun.com
 *
 */
public class HL7CommunicationControlImpl extends HL7ComponentImpl implements HL7CommunicationControl {

	private HL7CommunicationControls commCntrls;

	public HL7CommunicationControlImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HL7CommunicationControlImpl(WSDLModel model){
        this(model, createPrefixedElement(HL7QName.COMMUNICATIONCONTROL.getQName(), model));
    }
    
    public void accept(HL7Component.Visitor visitor) {
        visitor.visit(this);
    }

    public String getName() {
		return getAttribute(HL7Attribute.HL7_COMM_CTRL_NAME);
	}

    public void setName(String val) {
		setAttribute(HL7CommunicationControl.HL7_NAME_PROPERTY, HL7Attribute.HL7_COMM_CTRL_NAME, val);
	}
    
	public Long getValue() throws NumberFormatException {
		String valStr = getAttribute(HL7Attribute.HL7_COMM_CTRL_VALUE);
		Long value = null;
		if(nonEmptyString(valStr)) {
		  try {
			value = new Long(valStr);
		  }
		  catch (NumberFormatException ne){
			throw ne;
		  } 
		}
		return value;
	}
    
	public void setValue(Long val) {
		setAttribute(HL7CommunicationControl.HL7_VALUE_PROPERTY, HL7Attribute.HL7_COMM_CTRL_VALUE, val);
	}
    
        public String getValueAsString(){
            String valStr = getAttribute(HL7Attribute.HL7_COMM_CTRL_VALUE);
            if(valStr != null)
                return valStr.trim();
            else 
                return "";
        }
        
        public void setValueAsString(String val){
            setAttribute(HL7CommunicationControl.HL7_VALUE_PROPERTY, HL7Attribute.HL7_COMM_CTRL_VALUE, val);
        }
        
	public Boolean getEnabled() {
		String enabledStr = getAttribute(HL7Attribute.HL7_COMM_CTRL_ENABLED);
		Boolean enabled = null;
		if(nonEmptyString(enabledStr)){
		  try {
		  	enabled = new Boolean(enabledStr);
		  }
		  catch (Exception e){
			// Just Ignor
		  } 
		  
		}
		return enabled;
	}
    
	public void setEnabled(Boolean val) {
		setAttribute(HL7CommunicationControl.HL7_ENABLED_PROPERTY, HL7Attribute.HL7_COMM_CTRL_ENABLED, val);
	}
    
	public String getRecourseAction() {
		return getAttribute(HL7Attribute.HL7_COMM_CTRL_RECOURSE_ACTION);
	}
    
	public void setRecourseAction(String val) {
		setAttribute(HL7CommunicationControl.HL7_RECOURSEACTION_PROPERTY, HL7Attribute.HL7_COMM_CTRL_RECOURSE_ACTION, val);
	}

	public void setCommunicationControlsElement(HL7CommunicationControls commCntrls) {
		this.commCntrls = commCntrls;
	}

	public HL7CommunicationControls getCommunicationControlsElement() {
		return commCntrls;
	}

	private boolean nonEmptyString(String strToTest) {
        boolean nonEmpty = false;
        if (strToTest != null && strToTest.length() > 0) {
            nonEmpty = true;
        }
        return nonEmpty;
    }

}
