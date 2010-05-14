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
 *
 * Represents the address element under the wsdl port for FTP binding
 * @author jim.fu@sun.com
*/
public interface FTPAddress extends FTPComponent {
    public static final String FTP_URL_PROPERTY = "url";
    public static final String FTP_DIRLSTSTYLE_PROPERTY = "dirListStyle";
    public static final String FTP_USE_UD_HEURISTICS_PROPERTY = "useUserDefinedHeuristics";
    public static final String FTP_UD_DIRLSTSTYLE_PROPERTY = "userDefDirListStyle";
    public static final String FTP_UD_HEURISTICS_PROPERTY = "userDefDirListHeuristics";
    public static final String FTP_TRANSMODE_PROPERTY = "mode";
    public static final String FTP_CMD_CH_TIMEOUT_PROPERTY = "cmdChannelTimeout";
    public static final String FTP_DATA_CH_TIMEOUT_PROPERTY = "dataChannelTimeout";
    public static final String FTP_CNTRL_CH_ENCODING_PROPERTY = "controlChannelEncoding";
    // added for clustering-awareness of FTPBC SU
    public static final String FTP_PERSIST_BASE_LOC_PROPERTY = "baseLocation";

    // new attrs for FTP/TLS 
    public static final String FTP_SEC_TYPE_PROPERTY = "securedFTP";
    public static final String FTP_ENABLE_CCC_PROPERTY = "enableCCC";
    public static final String FTP_KSTOR_PROPERTY = "keyStore";
    public static final String FTP_KSTOR_PASSWD_PROPERTY = "keyStorePassword";
    public static final String FTP_KEY_ALIAS_PROPERTY = "keyAlias";
    public static final String FTP_KEY_PASSWD_PROPERTY = "keyPassword";
    public static final String FTP_TSTOR_PROPERTY = "trustStore";
    public static final String FTP_TSTOR_PASSWD_PROPERTY = "trustStorePassword";
    
    // for url user and password overwrite
    public static final String FTP_LOGIN_PROPERTY = "user";
    public static final String FTP_LOGIN_PASSWORD_PROPERTY = "password";
    
    public String getFTPURL();
    public void setFTPURL(String url);
    public String getDirListStyle();
    public void setDirListStyle(String style);
    //useUserDefinedHeuristics="indicate that user defined listing style will be used"
    public boolean getUseUserDefinedHeuristics();
    public void setUseUserDefinedHeuristics(boolean useUserDefined);
    //userDefDirListStyle="specify user defined listing style name (optional)"
    public String getUserDefDirListStyle();
    public void setUserDefDirListStyle(String style);
    //userDefDirListHeuristics="specify location of heuristics configuration file containing the 'userDefDirListingStyle'"
    public String getUserDefDirListHeuristics();
    public void setUserDefDirListHeuristics(String heuristicsLoc);
    //mode="BINARY"
    public String getTransferMode();
    public void setTransferMode(String mode);

    public String getCmdChannelTimeout();
    public void setCmdChannelTimeout(String s);
    public String getDataChannelTimeout();
    public void setDataChannelTimeout(String s);

    public String getControlChannelEncoding();
    public void setControlChannelEncoding(String s);

    // added for clustering support - a file system or NFS based file locking mechanism
    // used to synchronizing clustered BC service units servicing the same endpoint
    // e.g. same inbound directory for polling inbound messages
    public String getPersistenceBaseDir();
    public void setPersistenceBaseDir(String s);
    
    // new setters/getters for FTP/TLS
    public String getSecureFTPType();
    public void setSecureFTPType(String s);
    public boolean getEnableCCC();
    public void setEnableCCC(boolean b);
    public String getKeyStore();
    public void setKeyStore(String s);
    public String getKeyStorePassword();
    public void setKeyStorePassword(String s);
    public String getKeyAlias();
    public void setKeyAlias(String s);
    public String getKeyPassword();
    public void setKeyPassword(String s);
    public String getTrustStore();
    public void setTrustStore(String s);
    public String getTrustStorePassword();
    public void setTrustStorePassword(String s);
    public String getFTPLogin();
    public void setFTPLogin(String s);
    public String getFTPLoginPassword();
    public void setFTPLoginPassword(String s);
}
