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

package org.netbeans.modules.wsdlextensions.ims.model.impl;

import java.util.Collection;

import org.netbeans.modules.xml.xam.dom.Attribute;

public enum IMSAttribute implements Attribute {
    IMS_SERVER_LOCATION("imsServerLocation"),
    IRM_LEN("irmLen"),
	IRM_ID("irmId"),
	IRM_TIMER("irmTimer"),
	IRM_SOCKET("irmSocket"),
	IRM_CLIENT_ID("irmClientId"),
	IRM_MOD("irmMod"),
	IRM_COMMIT_MODE("irmCommitMode"),
	IRM_SYNC_LEVEL("irmSyncLevel"),
	IRM_ACK("irmAck"),
	IRM_FLOW("irmFlow"),
	IRM_TRAN_CODE("irmTranCode"),
	IRM_TRAN_CODE_SRC("irmTranCodeSrc"),
	IRM_DEST_ID("irmDestId"),
	IRM_LTERM("irmLterm"),
	IRM_RACF_GRP_NAME("irmRacfGrpName"),
	IRM_RACF_USER_ID("irmRacfUserId"),
	IRM_RACF_PASS("irmRacfPwd"),
	IRM_HEADER_ENCODING("irmHeaderEncod"),
	SEND_DATA_ENCODING("sendDataEncod"),
	REPLY_DATA_ENCODING("replyDataEncod"),
    IMS_USE("use"),
    IMS_ENCODING_STYLE("encodingStyle"),
    IMS_PART("part");

    private String name;
    private Class type;
    private Class subtype;
    
    IMSAttribute(String name) {
        this(name, String.class);
    }
    
    IMSAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    IMSAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() {
		return name; 
	}
    
    public Class getType() {
		return type;
    }
    
    public String getName() { 
		return name; 
	}
    
    public Class getMemberType() { 
		return subtype; 
	}
}
