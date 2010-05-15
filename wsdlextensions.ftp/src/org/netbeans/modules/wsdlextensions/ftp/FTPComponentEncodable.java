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
 *
 */
public interface FTPComponentEncodable extends FTPComponent {

    public static final String FTP_USE_PROPERTY = "use";
    public static final String FTP_ENCODINGSTYLE_PROPERTY = "encodingStyle";
    public static final String FTP_PART_PROPERTY = "part";
    
    public static final String FTP_CHAR_ENCODE_PROPERTY = "characterEncoding";
    public static final String FTP_FILE_TYPE_PROPERTY = "fileType";
    public static final String FTP_FWD_ATTACH_PROPERTY = "forwardAsAttachment";
    
    public String getUse();
    public void setUse(String use);

    public String getEncodingStyle();
    public void setEncodingStyle(String encodingStyle);

    public String getPart();
    public void setPart(String use);

    public String getCharacterEncoding();
    public void setCharacterEncoding(String s);

    public String getFileType();
    public void setFileType(String s);

    public boolean getForwardAsAttachment();
    public void setForwardAsAttachment(boolean b);
}
