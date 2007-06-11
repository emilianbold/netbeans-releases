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

package org.netbeans.modules.wsdlextensions.ftp;

/**
* @author jim.fu@sun.com
*/
public interface FTPTransfer extends FTPComponentEncodable {
    public static final String FTP_POLLINTERVAL_PROPERTY = "pollIntervalMillis";
    public static final String FTP_SENDTO_PROPERTY = "sendTo";
    public static final String FTP_SENDTO_HAS_PATTS_PROPERTY = "sendToHasPatterns";
    public static final String FTP_APPEND_PROPERTY = "append";
    public static final String FTP_RECEIVEFROM_PROPERTY = "receiveFrom";
    public static final String FTP_RECEIVEFROM_HAS_PATTS_PROPERTY = "receiveFromHasRegexs";
    public static final String FTP_PRE_SEND_CMD_PROPERTY = "preSendCommand";
    public static final String FTP_PRE_SEND_LOC_PROPERTY = "preSendLocation";
    public static final String FTP_PRE_SEND_LOC_HAS_PATTS_PROPERTY = "preSendLocationHasPatterns";
    public static final String FTP_PRE_RECEIVE_CMD_PROPERTY = "preReceiveCommand";
    public static final String FTP_PRE_RECEIVE_LOC_PROPERTY = "preReceiveLocation";
    public static final String FTP_PRE_RECEIVE_LOC_HAS_PATTS_PROPERTY = "preReceiveLocationHasPatterns";
    public static final String FTP_POST_SEND_CMD_PROPERTY = "postSendCommand";
    public static final String FTP_POST_SEND_LOC_PROPERTY = "postSendLocation";
    public static final String FTP_POST_SEND_LOC_HAS_PATTS_PROPERTY = "postSendLocationHasPatterns";
    public static final String FTP_POST_RECEIVE_CMD_PROPERTY = "postReceiveCommand";
    public static final String FTP_POST_RECEIVE_LOC_PROPERTY = "postReceiveLocation";
    public static final String FTP_POST_RECEIVE_LOC_HAS_PATTS_PROPERTY = "postReceiveLocationHasPatterns";
    public static final String FTP_SENDER_USEPROXY_PROPERTY = "senderUseProxy";
    public static final String FTP_SENDER_PROXY_PROPERTY = "senderProxy";
    public static final String FTP_SENDER_USEPASSIVE_PROPERTY = "senderUsePassive";
    public static final String FTP_RECEIVER_USEPROXY_PROPERTY = "receiverUseProxy";
    public static final String FTP_RECEIVER_PROXY_PROPERTY = "receiverProxy";
    public static final String FTP_RECEIVER_USEPASSIVE_PROPERTY = "receiverUsePassive";
    public static final String FTP_MSG_CORRELATE_PROPERTY = "messageCorrelate";
    
    public String getPollInterval();
    public void setPollInterval(String s);

    public String getSendTo();
    public void setSendTo(String s);
    
    public boolean getSendToHasPatterns();
    public void setSendToHasPatterns(boolean b);
    
    public boolean getAppend();
    public void setAppend(boolean b);

    public String getReceiveFrom();
    public void setReceiveFrom(String file);
    
    public boolean getReceiveFromHasPatterns();
    public void setReceiveFromHasPatterns(boolean b);
    
    public String getPreSendCommand();
    public void setPreSendCommand(String s);
    
    public String getPreSendLocation();
    public void setPreSendLocation(String s);
    
    public boolean getPreSendLocationHasPatterns();
    public void setPreSendLocationHasPatterns(boolean b);
    
    public String getPreReceiveCommand();
    public void setPreReceiveCommand(String s);
    
    public String getPreReceiveLocation();
    public void setPreReceiveLocation(String s);

    public boolean getPreReceiveLocationHasPatterns();
    public void setPreReceiveLocationHasPatterns(boolean b);
    
    public String getPostSendCommand();
    public void setPostSendCommand(String s);
    
    public String getPostSendLocation();
    public void setPostSendLocation(String s);
    
    public boolean getPostSendLocationHasPatterns();
    public void setPostSendLocationHasPatterns(boolean b);
    
    public String getPostReceiveCommand();
    public void setPostReceiveCommand(String s);

    public String getPostReceiveLocation();
    public void setPostReceiveLocation(String s);
    
    public boolean getPostReceiveLocationHasPatterns();
    public void setPostReceiveLocationHasPatterns(boolean b);

    public boolean getSenderUsePassive();
    public void setSenderUsePassive(boolean b);
    /* proxy config moved to MBean config of the BC
    public boolean getSenderUseProxy();
    public void setSenderUseProxy(boolean b);
    public String getSenderProxyURL();
    public void setSenderProxyURL(String s);
    */
    public boolean getReceiverUsePassive();
    public void setReceiverUsePassive(boolean b);
    public boolean getReceiverUseProxy();
    /* proxy config moved to MBean config of the BC
    public void setReceiverUseProxy(boolean b);
    public String getReceiverProxyURL();
    public void setReceiverProxyURL(String s);
    */
    public boolean getMessageCorrelateEnabled();
    public void setMessageCorrelateEnabled(boolean b);
}

