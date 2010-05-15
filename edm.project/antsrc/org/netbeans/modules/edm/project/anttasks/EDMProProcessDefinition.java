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
package org.netbeans.modules.edm.project.anttasks;

import java.io.File;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.edm.editor.utils.DBConnectionParameters;
import org.netbeans.modules.edm.editor.utils.RuntimeAttribute;
import org.netbeans.modules.edm.editor.utils.SQLDBConnectionDefinition;
import org.netbeans.modules.edm.editor.utils.SQLPart;


/**
 * 
 * This class is responsible for representing the mashup queries, 
 * basically picks up the mashup engine file and sucks in the sqls in the 
 * order. Basically, creates the db links, creates the external tables
 * and the view query sqls. 
 *
 *  <MashupEngine>
 *  <connectiondef name="IDB_CONN_DEF" driverName="org.axiondb.jdbc.AxionDriver" dbName="Internal" dbUrl="jdbc:axiondb:testdb:C:/test/" userName="sa" password="02C820">
 *  </connectiondef>
 *   <init>
 *        <dbLinks>
 *             <dbLink></dbLink>
 *       </dbLinks>
 *       <VTables>
 *             <VTable name="test" type="WebTable"><dropsql/><createsql/></VTable>
 *       </VTables>
 *   </init>
 *   <process>
 *      <DataMashup name="">
 **          <mashupsql/>
 *       </DataMashup>
 *   </process>
 *  </MashupEngine>
 * 
 * @author Srinivasan Rengarajan
 * 
 */
public class EDMProProcessDefinition {

    protected DBConnectionParameters connectionDef = null;
    private List<SQLPart> initSQLs = new LinkedList<SQLPart>();
    private Map<String, RuntimeAttribute> attributeMap = new HashMap<String, RuntimeAttribute>();
    private SQLPart mashupQuery = null;
    private String mashupResponse = "";
    private static final String MASHUP_SQL = "mashupsql";
    private static final String MASHUP = "DataMashup";
    private static final String MASHUP_ENGINE = "MashupEngine";
    private static final String NL = "\n";
    private static final String INIT = "init";
    private static final String PROCESS = "process";
    private static final String RTINPUT = "runtimeInputs";
    private static final String RTATTR = "RuntimeAttr";
    public static final String RESPONSETYPE = "resptype";
    public String respValue = null;

    public EDMProProcessDefinition() {
    }

    public void setAttributeMap(Map attributeMap) {
        this.attributeMap = attributeMap;
    }

    public Map getAttributeMap() {
        return attributeMap;
    }

    public void setInitStatements(List<SQLPart> stmts) {
        this.initSQLs.clear();
        this.initSQLs = stmts;
    }

    public SQLPart getDataMashupQuery() {
        return this.mashupQuery;
    }

    public void setDataMashupQuery(SQLPart mashupQuery) {
        this.mashupQuery = mashupQuery;
    }

    public String getMashupResponse() {
        return this.mashupResponse;
    }

    public void setMashupResponse(String mashupResponse) {
        this.mashupResponse = mashupResponse;
    }

    public DBConnectionParameters getDBConnectionParams() {
        return this.connectionDef;
    }

    public String generateEngineFile() {
        StringBuffer buffer = new StringBuffer(1000);
        buffer.append(getStartElement(MASHUP_ENGINE));
        buffer.append(getStartElement(INIT));
        sqlPartsToXML(buffer, initSQLs);
        buffer.append(getEndElement(INIT));
        rtAttributesToXML(buffer, attributeMap);
        processToXML(buffer);
        buffer.append(getEndElement(MASHUP_ENGINE));

        return buffer.toString();
    }

    private void processToXML(StringBuffer buffer) {

        /*
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Element element = df.newDocumentBuilder().parse(engineFile).getDocumentElement();
        ETLDefinitionImpl defnImpl = new ETLDefinitionImpl(element, null);
        SQLDefinition sqlDefn = defnImpl.getSQLDefinition();
        
         */

        buffer.append(getStartElement(PROCESS));


        respValue = this.getMashupResponse().toUpperCase();

        buffer.append(getStartElementWithAtrribute(MASHUP, RESPONSETYPE, respValue));
        buffer.append(getStartElement(MASHUP_SQL));
        //buffer.append(XmlUtil.escapeXML(this.mashupQuery.toXMLString()));
        buffer.append(this.mashupQuery.toXMLString());
        buffer.append(getEndElement(MASHUP_SQL));
        buffer.append(getEndElement(MASHUP));
        buffer.append(getEndElement(PROCESS));
    }

    private void sqlPartsToXML(StringBuffer buffer, List<SQLPart> parts) {
        Iterator partsIter = parts.listIterator();
        while (partsIter.hasNext()) {
            buffer.append(((SQLPart) partsIter.next()).toXMLString());
        }
    }

    private void rtAttributesToXML(StringBuffer buffer, Map<String, RuntimeAttribute> attributeMap) {
        Iterator iter = attributeMap.keySet().iterator();
        buffer.append(getStartElement(RTINPUT));
        while (iter.hasNext()) {
            String name = (String) iter.next();
            RuntimeAttribute ra = attributeMap.get(name);
            buffer.append("<").append(RTATTR);
            buffer.append(" ").append("name").append("=\"").append(ra.getAttributeName()).append("\"");
            buffer.append(" ").append("value").append("=\"").append(ra.getAttributeValue()).append("\"");
            buffer.append(" ").append("type").append("=\"").append(ra.getJdbcType()).append("\"");
            buffer.append("/>").append(NL);
        }
        buffer.append(getEndElement(RTINPUT));
    }

    private String getStartElementWithAtrribute(String elementName, String attributeName, String attributeValue) {
        StringBuffer newTag = new StringBuffer(1000);
        newTag.append("<").append(elementName);
        newTag.append(" ").append(attributeName).append("=\"").append(attributeValue).append("\"");
        newTag.append(">").append(NL);
        return newTag.toString();
    }

    private String getStartElement(String elementName) {
        return "<" + elementName + ">" + NL;
    }

    private String getEndElement(String elementName) {
        return "</" + elementName + ">" + NL;
    }

    public String dumpQueries() {
        StringBuffer output = new StringBuffer(1000);
        output.append(connectionDef.toString());
        for (int i = 0; i < initSQLs.size(); i++) {
            output.append(initSQLs.get(i));
        }
        output.append(mashupQuery);
        return output.toString();
    }

    public void setDBConnectionParameters(SQLDBConnectionDefinition connDef) {
        if (connectionDef == null) {
            connectionDef = new DBConnectionParameters();
        }
        this.connectionDef.setDBType(connDef.getDBType());
        this.connectionDef.setConnectionURL(connDef.getConnectionURL());
        this.connectionDef.setDescription(connDef.getDescription());
        this.connectionDef.setDriverClass(connDef.getDriverClass());
        this.connectionDef.setName(connDef.getName());
        this.connectionDef.setUserName(connDef.getUserName());
        this.connectionDef.setPassword(connDef.getPassword());
    }

    public static void main(String[] args) {
        File f = new File(args[0]);
        EDMProProcessDefinition processDef = new EDMProProcessDefinition();
    }
}
