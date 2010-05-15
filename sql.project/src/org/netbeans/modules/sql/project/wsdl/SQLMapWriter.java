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

package org.netbeans.modules.sql.project.wsdl;

import javax.wsdl.Definition;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.io.FileOutputStream;
import java.io.File;

public class SQLMapWriter {

    private Definition wsdlDefinition;
    private String sqlmapLocation;
    private List sqlFilesList;
    
    public SQLMapWriter(List sqlFilesList , Definition def, String canonicalPath) {
        this.sqlFilesList = sqlFilesList;
        this.wsdlDefinition = def;
        this.sqlmapLocation = canonicalPath;
    }

    /**
     * 
     * <?xml version="1.0" encoding="UTF-8"?> <sqlmap
     * xmlns:tns="http://com.sun.com/sqlse/sqlengine"
     * targetNamespace="http://com.sun.com/sqlse/sqlengine" > <sql
     * partnerLink="{http://com.sun.com/sqlse/sqlengine}Client2ETELLink"
     * portType="{http://com.sun.com/sqlse/sqlengine}sqlsePortType"
     * operation="execute" file="sqlse-engine.xml" type="requestReplyService"/>
     * 
     */

    public String writeMap() throws Exception {

        StringBuffer sb = new StringBuffer();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<sqlmap  xmlns:tns=\"http://com.sun.jbi/sqlse/sqlengine\" \n");
        sb.append("\t targetNamespace=\"http://com.sun.jbi/sqlse/sqlengine\" > \n");

        if(sqlFilesList != null) {
        Iterator iter = sqlFilesList.iterator();
        while (iter.hasNext()) {

            String sqlFilePath = ((File) iter.next()).getName();
            int beginIndex = sqlFilePath.lastIndexOf(File.separator);
            String sqlFileName = sqlFilePath.substring(beginIndex + 1, sqlFilePath.length());
            sb.append("\t <sql partnerLink=\"" + wsdlDefinition.getQName() + "_sqlsePartnerLink" + "\"\n");
            sb.append("\t \t partnerLinkType=\"" + wsdlDefinition.getQName() + "_sqlsePartnerLinkType" + "\"\n");
            sb.append("\t \t roleName=\"" + wsdlDefinition.getQName() + "_myrole" + "\"\n");
            sb.append("\t \t portType=\"" + wsdlDefinition.getQName() + "_sqlsePortType" + "\"\n");
            sb.append("\t \t operation= \"" + sqlFileName + "\" \n");
            sb.append("\t \t wsdlfile=\"" + wsdlDefinition.getQName().getLocalPart() + ".wsdl" + "\"\n");
            sb.append("\t \t sqlfile=\"" + sqlFilePath + "\"\n");
            sb.append("\t \t type= \"requestReplyService\"  \n");
            sb.append("\t \t displayName=\"" + wsdlDefinition.getQName().getLocalPart() + "\"\n");
            sb.append("\t \t processName=\"" + wsdlDefinition.getQName().getLocalPart() + "\"\n");
            sb.append("\t \t filePath= \"" + sqlFilePath + "\" /> \n");


        }
        } else {
           throw new Exception("No SQL Files found to generate SQL Map"); 
        }
        sb.append("</sqlmap>");

        String content = sb.toString();
        
        try {
            FileOutputStream fos = new FileOutputStream(sqlmapLocation + "/sqlmap.xml");
            FileUtil.copy(content.getBytes("UTF-8"), fos);
        } catch (Exception e) {
            throw new Exception("Unable to generate sqlmap file", e);
        }

     return content;
    }
}
