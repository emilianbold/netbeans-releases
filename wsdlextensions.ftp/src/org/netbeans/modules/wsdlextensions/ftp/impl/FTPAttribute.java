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

package org.netbeans.modules.wsdlextensions.ftp.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author jim.fu@sun.com
 *
 */
public enum FTPAttribute implements Attribute {
    FTP_URL_PROPERTY("url"),
    FTP_CNTRL_CH_ENCODING_PROPERTY("controlChannelEncoding"),
    FTP_CMD_CH_TIMEOUT_PROPERTY("cmdChannelTimeout"),
    FTP_DATA_CH_TIMEOUT_PROPERTY("dataChannelTimeout"),
    FTP_ENCODINGSTYLE_PROPERTY("encodingStyle"),
    FTP_SENDER_USEPROXY_PROPERTY("senderUseProxy"),
    FTP_SENDER_PROXY_PROPERTY("senderProxy"),
    /* FTP_SENDER_USEPASSIVE_PROPERTY("senderUsePassive"),*/
    FTP_RECEIVER_USEPROXY_PROPERTY("receiverUseProxy"),
    FTP_RECEIVER_PROXY_PROPERTY("receiverProxy"),
    /* FTP_RECEIVER_USEPASSIVE_PROPERTY("receiverUsePassive"), */
    FTP_USE_PROPERTY("use"),
    FTP_PART_PROPERTY("part"),
    FTP_POLLINTERVAL_PROPERTY("pollIntervalMillis"),
    FTP_SENDTO_PROPERTY("sendTo"),
    FTP_APPEND_PROPERTY("append"),
    FTP_SENDTO_HAS_PATTS_PROPERTY("sendToHasPatterns"),
    FTP_RECEIVEFROM_PROPERTY("receiveFrom"),
    FTP_RECEIVEFROM_HAS_PATTS_PROPERTY("receiveFromHasRegexs"),
    FTP_DIRLSTSTYLE_PROPERTY("dirListStyle"),
    FTP_USE_UD_HEURISTICS_PROPERTY("useUserDefinedHeuristics"),
    FTP_UD_DIRLSTSTYLE_PROPERTY("userDefDirListStyle"),
    FTP_UD_HEURISTICS_PROPERTY("userDefDirListHeuristics"),
    FTP_TRANSMODE_PROPERTY("mode"),
    FTP_PRE_SEND_CMD_PROPERTY("preSendCommand"),
    FTP_PRE_SEND_LOC_PROPERTY("preSendLocation"),
    FTP_PRE_SEND_LOC_HAS_PATTS_PROPERTY("preSendLocationHasPatterns"),
    FTP_PRE_RECEIVE_CMD_PROPERTY("preReceiveCommand"),
    FTP_PRE_RECEIVE_LOC_PROPERTY("preReceiveLocation"),
    FTP_PRE_RECEIVE_LOC_HAS_PATTS_PROPERTY("preReceiveLocationHasPatterns"),
    FTP_POST_SEND_CMD_PROPERTY("postSendCommand"),
    FTP_POST_SEND_LOC_PROPERTY("postSendLocation"),
    FTP_POST_SEND_LOC_HAS_PATTS_PROPERTY("postSendLocationHasPatterns"),
    FTP_POST_RECEIVE_CMD_PROPERTY("postReceiveCommand"),
    FTP_POST_RECEIVE_LOC_PROPERTY("postReceiveLocation"),
    FTP_POST_RECEIVE_LOC_HAS_PATTS_PROPERTY("postReceiveLocationHasPatterns"),
    FTP_SYMETRIC_MSG_REPO_PROPERTY("messageRepository"), // for symetrical wsdl
    FTP_SYMETRIC_MSG_NAME_PROPERTY("messageName"), // for symetrical wsdl
    FTP_SYMETRIC_MSG_NAME_PREFIX_IB_PROPERTY("messageNamePrefixIB"), // for symetrical wsdl
    FTP_SYMETRIC_MSG_NAME_PREFIX_OB_PROPERTY("messageNamePrefixOB"), // for symetrical wsdl
    /* FTP_CONSUMER_USEPASSIVE_PROPERTY("consumerUsePassive"),
    FTP_PROVIDER_USEPASSIVE_PROPERTY("providerUsePassive"),*/
    FTP_PROTECT_ENABLED_PROPERTY("protect"),
    FTP_ARCHIVE_ENABLED_PROPERTY("archive"),
    FTP_STAGING_ENABLED_PROPERTY("stage"),
    FTP_MSG_CORRELATE_PROPERTY("messageCorrelate"),
    // for FTP/TLS
    FTP_SEC_TYPE_PROPERTY("securedFTP"),
    FTP_ENABLE_CCC_PROPERTY("enableCCC"),
    FTP_KSTOR_PROPERTY("keyStore"),
    FTP_KSTOR_PASSWD_PROPERTY("keyStorePassword"),
    FTP_KEY_ALIAS_PROPERTY("keyAlias"),
    FTP_KEY_PASSWD_PROPERTY("keyPassword"),
    FTP_TSTOR_PROPERTY("trustStore"),
    FTP_TSTOR_PASSWD_PROPERTY("trustStorePassword"),
    // user and password in URL overwrite
    FTP_LOGIN_PROPERTY("user"),
    FTP_LOGIN_PASSWORD_PROPERTY("password"),
    FTP_CHAR_ENCODE_PROPERTY("characterEncoding"),
    FTP_FILETYPE_PROPERTY("fileType"),
    // added to make FTPBC clustering-aware by employing a
    // file system or NFS based file locking so that 
    // concurrent service units servicing the same endpoint - i.e. - inbound SU polling
    // the same ftp directory for the same set of files
    // can be effectively synchronized
    FTP_PERSIST_BASE_LOC_PROPERTY("baseLocation"),
    FTP_FWDATTACH_PROPERTY("forwardAsAttachment");
    
    private String name;
    private Class type;
    private Class subtype;
    
    FTPAttribute(String name) {
        this(name, String.class);
    }
    
    FTPAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    FTPAttribute(String name, Class type, Class subtype) {
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
