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
public interface FTPMessageActivePassive extends FTPComponentEncodable {
    public static final String FTP_POLLINTERVAL_PROPERTY = "pollIntervalMillis";
    public static final String FTP_SYMETRIC_MSG_REPO_PROPERTY = "messageRepository";
    public static final String FTP_SYMETRIC_MSG_NAME_PROPERTY = "messageName";
    public static final String FTP_SYMETRIC_MSG_NAME_PREFIX_IB_PROPERTY = "messageNamePrefixIB";
    public static final String FTP_SYMETRIC_MSG_NAME_PREFIX_OB_PROPERTY = "messageNamePrefixOB";
    public static final String FTP_PROTECT_ENABLED_PROPERTY = "protect";
    public static final String FTP_ARCHIVE_ENABLED_PROPERTY = "archive";
    public static final String FTP_STAGING_ENABLED_PROPERTY = "stage";
    public static final String FTP_CONSUMER_USEPASSIVE_PROPERTY = "consumerUsePassive";
    public static final String FTP_PROVIDER_USEPASSIVE_PROPERTY = "providerUsePassive";
    public static final String FTP_MSG_CORRELATE_PROPERTY = "messageCorrelate";
    
    public String getPollInterval();
    public void setPollInterval(String s);

    public String getMessageRepository();
    public void setMessageRepository(String s);

    public String getMessageName();
    public void setMessageName(String s);
    
    public String getMessageNamePrefixIB();
    public void setMessageNamePrefixIB(String s);
    
    public String getMessageNamePrefixOB();
    public void setMessageNamePrefixOB(String s);

    public boolean getArchiveEnabled();
    public void setArchiveEnabled(boolean b);
    
    public boolean getProtectEnabled();
    public void setProtectEnabled(boolean b);

    public boolean getStagingEnabled();
    public void setStagingEnabled(boolean b);

    public boolean getConsumerUsePassive();
    public void setConsumerUsePassive(boolean b);
    
    public boolean getProviderUsePassive();
    public void setProviderUsePassive(boolean b);

    public boolean getMessageCorrelateEnabled();
    public void setMessageCorrelateEnabled(boolean b);
}

