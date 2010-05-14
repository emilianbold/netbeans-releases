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

import java.util.List;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControls;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControl;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.w3c.dom.Element;

/**
 * @author Nageswara.Samudrala@Sun.com
 *
 */
public class HL7CommunicationControlsImpl extends HL7ComponentImpl implements HL7CommunicationControls {

	private List<HL7CommunicationControl> commCntrls;

    public HL7CommunicationControlsImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HL7CommunicationControlsImpl(WSDLModel model){
        this(model, createPrefixedElement(HL7QName.COMMUNICATIONCONTROLS.getQName(), model));
    }
    
    public void accept(HL7Component.Visitor visitor) {
        visitor.visit(this);
    }

	public List<HL7CommunicationControl> getHL7CommunicationControls() {
		return commCntrls;
	}

	public void setHL7CommunicationControls(List<HL7CommunicationControl> hl7CommCntrls) {
		commCntrls = hl7CommCntrls;
	}
}
