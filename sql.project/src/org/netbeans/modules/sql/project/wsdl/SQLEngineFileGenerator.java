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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.wsdl;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileOutputStream;

/**
 * Generates an xml file containing list of sql files and their associated
 * connection parameters. This file should ideally be generated at design time
 * as the user creates new sql files and associates connections to the sql file.  
 */
public class SQLEngineFileGenerator {
    private Map sqlDefinitionMap = new HashMap();
    private String engineFileName = null;
    private String projectName = null;

    private static Logger logger = Logger.getLogger(SQLEngineFileGenerator.class.getName());

    public SQLEngineFileGenerator(String engineFileName, String projectName) {
        this.engineFileName = engineFileName;
        this.projectName = projectName;
    }

    public void addSQLDefinition(String sqlFileName, DatabaseConnection dbConn) {
        sqlDefinitionMap.put(sqlFileName, dbConn);
    }

    public void persistEngineFile(String jndi_name,String transactionRequired) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<sqlengine name=\"" + projectName + "\">");
        Iterator iter = sqlDefinitionMap.keySet().iterator();
        while (iter.hasNext()) {
            String sqlFileName = (String) iter.next();
            DatabaseConnection dbConn = (DatabaseConnection) sqlDefinitionMap.get(sqlFileName);
            sb.append("<sqldef>" + "\n");
            sb.append("<sqlfile name=\"" + sqlFileName + "\"/>" + "\n");
            sb.append("<connectiondef name=\"" + dbConn.getName() + "\"" + "\t");
            sb.append("driverClass=\"" + dbConn.getDriverClass() + "\"" + "\t");
            sb.append("dbURL=\"" + dbConn.getDatabaseURL() + "\"" + "\t");
            sb.append("databaseName=\"" + dbConn.getSchema() + "\"" + "\t");
            sb.append("user=\"" + dbConn.getUser() + "\"" + "\t");
			sb.append("password=\"" + dbConn.getPassword() + "\"" + "\t");
//TODO HOT FIX            sb.append("password=\"" + org.netbeans.modules.sql.project.security.Base64Impl.getInstance().encode(dbConn.getPassword()) + "\"" + "\t");
            sb.append("jndi_name=\"" + jndi_name + "\"" + "\t");
            sb.append("transactionRequired=\"" + transactionRequired + "\"" + "\t");
            sb.append(">\n</connectiondef>");
            sb.append("\n</sqldef>");
        }
        sb.append("\n</sqlengine>");
        try {
            //Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
            //Writer fileWriter = new FileWriter(engineFileName);
            //indentWSDLFile(fileWriter, doc);
            FileOutputStream fos = new FileOutputStream(engineFileName);
            FileUtil.copy(sb.toString().getBytes("UTF-8"), fos);
            
        } catch (Exception e) {
        }
    }
    private void indentWSDLFile(Writer writer, Document doc) {
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            PrintWriter pw = new PrintWriter(writer); //USE PRINTWRITER
            StreamResult result = new StreamResult(pw);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");   // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
            // indent the output to make it more legible... 
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // NOI18N
            } catch (Exception e) {
                ; // the JAXP implementation doesn't support indentation, no big deal
            }
            transformer.transform(source, result);
        } catch (Exception e) {
        }
    }
}
