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

package org.netbeans.modules.wsdlextensions.ims.model;

/*
 *
 * @author Sun Microsystems
 */

public interface IMSMessage extends IMSComponent {

	public static final String IRM_LEN = "irmLen";
	
	public static final String IRM_ID = "irmId";
	
	public static final String IRM_TIMER = "irmTimer";
	
	public static final String IRM_SOCKET = "irmSocket";
	
	public static final String IRM_CLIENT_ID = "irmClientId";
	
	public static final String IRM_MOD = "irmMod";
	
	public static final String IRM_COMMIT_MODE = "irmCommitMode";
	
	public static final String IRM_SYNC_LEVEL = "irmSyncLevel";
	
	public static final String IRM_ACK = "irmAck";
	
	public static final String IRM_FLOW = "irmFlow";
	
	public static final String IRM_TRAN_CODE = "irmTranCode";
	
	public static final String IRM_TRAN_CODE_SRC = "irmTranCodeSrc";
	
	public static final String IRM_DEST_ID = "irmDestId";
	
	public static final String IRM_LTERM = "irmLterm";
   
	public static final String IRM_RACF_GRP_NAME = "irmRacfGrpName";
	
	public static final String IRM_RACF_USER_ID = "irmRacfUserId";
	
	public static final String IRM_RACF_PASS = "irmRacfPwd";
	
	public static final String IRM_HEADER_ENCODING = "irmHeaderEncod";
	
	public static final String SEND_DATA_ENCODING = "sendDataEncod";
	
	public static final String REPLY_DATA_ENCODING = "replyDataEncod";
	
	public static final String IMS_USE = "use";
    
	public static final String IMS_ENCODING_STYLE = "encodingStyle";
    
	public static final String IMS_PART = "part";
    
    
    public String getIrmLen(); 

    public void setIrmLen(String len);

	public String getIrmId();

    public void setIrmId(String id);

	public String getIrmTimer(); 

    public void setIrmTimer(String timer); 

	public String getIrmSocket(); 

    public void setIrmSocket(String soct); 

	public String getIrmClientId(); 

    public void setIrmClientId(String clientId); 

	public String getIrmMod(); 

    public void setIrmMod(String mod);

	public String getIrmCommitMode();

    public void setIrmCommitMode(String mode);

	public String getIrmSyncLevel(); 

    public void setIrmSyncLevel(String level); 

	public String getIrmAck(); 

    public void setIrmAck(String ack); 

	public String getIrmFlow(); 

    public void setIrmFlow(String flow);

	public String getIrmTranCode(); 

    public void setIrmTranCode(String tcode);

	public String getIrmTranCodeSrc(); 

    public void setIrmTranCodeSrc(String tcode); 

	public String getIrmDestId(); 

    public void setIrmDestId(String dest); 

	public String getIrmLterm(); 

    public void setIrmLterm(String lterm); 

	public String getIrmRacfGrpName(); 

    public void setIrmRacfGrpName(String grpname); 

	public String getIrmRacfUserId(); 

    public void setIrmRacfUserId(String userid); 

	public String getIrmRacfPwd(); 

    public void setIrmRacfPwd(String pwd); 

	public String getIrmHeaderEncod(); 

    public void setIrmHeaderEncod(String hdr); 

	public String getSendDataEncod(); 

    public void setSendDataEncod(String data); 

	public String getReplyDataEncod(); 

    public void setReplyDataEncod(String data); 

	//<msmq:message ="use:literal"
    public String getUse();
    
	public void setUse(String use);
    
	//<msmq:message encodingStyle="encoded"
    public String getEncodingStyle();
    
	public void setEncodingStyle(String style);
    
	//<msmq:message part="part1"
    public String getMessagePart();
    
	public void setMessagePart(String part);


}

