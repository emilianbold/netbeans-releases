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

import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author raghunadh.teegavarapu@sun.com
 *
 */
public enum HL7Attribute implements Attribute {
    HL7_ENCODINGSTYLE_PROPERTY("encodingStyle"),
    HL7_USE_PROPERTY("use"),
    HL7_PART_PROPERTY("part"),
    HL7_MESSAGETYPE_PROPERTY("messageType"),
    HL7_SVR_LOCATIONURL("location"),
	HL7_TRANS_PROTOCOL_NAME("transportProtocolName"),
	HL7_LLP_TYPE("llpType"),
	HL7_ACK_MODE("acknowledgmentMode"),
    HL7_START_BLOCK_CHARACTER("startBlockCharacter"),
    HL7_END_DATA_CHARACTER("endDataCharacter"),
    HL7_END_BLOCK_CHARACTER("endBlockCharacter"),
    HL7_HLLP_CHECKSUM_ENABLED("hllpChecksumEnabled"),
	HL7_SEQNUM_ENABLED("seqNumEnabled"),
	HL7_VALIDATE_MSH("validateMSH"),
    HL7_PROCESSING_ID("processingID"),
    HL7_VERSION_ID("versionID"),
	HL7_FIELD_SEPARATOR("fieldSeparator"),
	HL7_ENCODING_CHARACTERS("encodingCharacters"),
    HL7_SENDING_APPLICATION("sendingApplication"),
    HL7_SENDING_FACILITY("sendingFacility"),
    HL7_ENABLED_SFT("enabledSFT"),
    HL7_SOFTWARE_VENDOR_ORGANIZATION("softwareVendorOrganization"),
    HL7_SOFTWARE_CERTIFIED_VERSION("softwareCertifiedVersionOrReleaseNumber"),
    HL7_SOFTWARE_PRODUCT_NAME("softwareProductName"),
    HL7_SOFTWARE_BINARY_ID("softwareBinaryID"),
    HL7_SOFTWARE_PRODUCT_INFORMATION("softwareProductInformation"),
    HL7_SOFTWARE_INSTALL_DATE("softwareInstallDate"),
	HL7_MLLPV2_RETRIES_COUNT_ON_NAK("mllpv2RetriesCountOnNak"),
	HL7_MLLPV2_RETRY_INTERVAL("mllpv2RetryInterval"),
	HL7_MLLPV2_TIME_TO_WAIT_FOR_ACK_NAK("mllpv2TimeToWaitForAckNak"),
	HL7_COMM_CTRL_NAME("name"),
	HL7_COMM_CTRL_VALUE("value"),
	HL7_COMM_CTRL_ENABLED("enabled"),
	HL7_COMM_CTRL_RECOURSE_ACTION("recourseAction"),
	HL7_JOURNALLING_ENABLED("journallingEnabled"),
	HL7_PERSISTENCE_ENABLED("persistenceEnabled");

    private String name;
    private Class type;
    private Class subtype;
    
    HL7Attribute(String name) {
        this(name, String.class);
    }
    
    HL7Attribute(String name, Class type) {
        this(name, type, null);
    }
    
    HL7Attribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() {
        return type;
    }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
