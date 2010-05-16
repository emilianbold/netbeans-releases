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

package org.netbeans.modules.wsdlextensions.hl7;

/**
 * @author Nageswara.Samudrala@Sun.com
 *
 */
public interface HL7CommunicationControl extends HL7Component {
	//attributes
	public static final String HL7_NAME_PROPERTY = "name";
    public static final String HL7_VALUE_PROPERTY = "value";
    public static final String HL7_ENABLED_PROPERTY = "enabled";
	public static final String HL7_RECOURSEACTION_PROPERTY = "recourseAction";
	//communication controls
	public static final String TIME_TO_WAIT_FOR_A_RESPONSE = "TIME_TO_WAIT_FOR_A_RESPONSE";
	public static final String NAK_RECEIVED = "NAK_RECEIVED";
	public static final String MAX_NO_RESPONSE = "MAX_NO_RESPONSE";
	public static final String MAX_NAK_RECEIVED = "MAX_NAK_RECEIVED";
	public static final String MAX_NAK_SENT = "MAX_NAK_SENT";
	public static final String MAX_CANNED_NAK_SENT = "MAX_CANNED_NAK_SENT";
	public static final String MAX_CONNECT_RETRIES = "MAX_CONNECT_RETRIES";

    //<hl7:communicationcontrol name="MAX_NAK_SENT"
    public String getName();
    public void setName(String val);
    //<hl7:communicationcontrol value="3"
    public Long getValue() throws NumberFormatException;
    public void setValue(Long val);
    public String getValueAsString();
    public void setValueAsString(String val);
    //<hl7:communicationcontrol enabled="true"
    public Boolean getEnabled();
    public void setEnabled(Boolean val);
	 //<hl7:communicationcontrol recourseAction="SUSPEND"
    public String getRecourseAction();
    public void setRecourseAction(String val);
	public void setCommunicationControlsElement(HL7CommunicationControls commCntrls);
	public HL7CommunicationControls getCommunicationControlsElement();

}
