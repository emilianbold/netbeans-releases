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
    public String getTrnasferMode();
    public void setTransferMode(String mode);

    public String getCmdChannelTimeout();
    public void setCmdChannelTimeout(String s);
    public String getDataChannelTimeout();
    public void setDataChannelTimeout(String s);
}
