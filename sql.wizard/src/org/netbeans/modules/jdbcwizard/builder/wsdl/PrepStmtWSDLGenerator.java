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
package org.netbeans.modules.jdbcwizard.builder.wsdl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.Definition;

import javax.wsdl.WSDLException;
import javax.wsdl.Types;

import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.model.DBQueryModel;
import org.netbeans.modules.jdbcwizard.builder.model.DerbyQueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.model.OracleQueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.model.DB2QueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.model.SQLServerQueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.model.JdbcQueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.model.MySQLQueryGenerator;
import org.netbeans.modules.jdbcwizard.builder.util.XMLCharUtil;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


public class PrepStmtWSDLGenerator {

    public static HashMap builtInTypes = new HashMap();
    private static final Logger logger = Logger.getLogger(PrepStmtWSDLGenerator.class.getName());
    private static WSDLFactory factory;
    private static DocumentBuilderFactory docBuilderfactory;
    
    public WSDLReader reader = null;

    private Definition def;
    private String mWSDLFileName;
    private String wsdlFileLocation;
    private Document doc;
    private Document wsdlDocumentation;
    private static final String IMPORT_ELEMENT = "xsd:import";
    private static final String NAMESPACE_ATTR = "namespace";
    private static final String SCHEMALOCATION_ATTR = "schemaLocation";
    private static final String TARGET_NS = "targetNamespace";
    private static final String TARGET_NS_PREFIX_STRING = "http://j2ee.netbeans.org/wsdl/";
    private static final String TNS_STRING = "xmlns:tns";
    private static final String NS_STRING = "ns:";						
    private static final String NAME = "name";
     
    private static final String PART_ELEMENT = "part";
    private static final String NAME_ATTR = "name";
    private static final String ELEMENT_ATTR = "element";
    private static final String TARGETNAMESPACE = "http://j2ee.netbeans.org/xsd/tableSchema";
    private static final String INPUTMESSAGE_NAME = "inputMsg";
    private static final String OUTPUTMESSAGE_NAME = "outputMsg";
    private static final String XMLSCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    private static final String QUERY = "sqlQuery";
    private static final String UPDATE_QUERY = "updateQuery";
    private static final String DELETE_QUERY = "deleteQuery";
    private static final String FIND_QUERY = "findQuery";
    private static final String POLL_QUERY = "pollQuery";
    
//  private static final String JDBC_SQL_ELEMENT = "jdbc:sql";
    private static final String JDBC_SQL_ELEMENT = "jdbc:input";
    private static final String SQL_ELEMENT = "sql";
    private static final String PARAM_ORDER = "paramOrder";
    private static final String JDBC_NAMESPACE = "http://schemas.sun.com/jbi/wsdl-extensions/jdbc/";

    private static final String SCHEMA_ELE="xsd:schema";
    private static final String PRIMARYKEY_ATTR = "PKName"; 
    private static final String TRANSACTION  = "Transaction";   
    private static final String MARK_COLUMN_VALUE = "MarkColumnValue";  
    private static final String MARK_COLUMN_NAME = "MarkColumnName";    
    private static final String POLL_POST_PROCESS = "PollingPostProcessing";    
    private static final String TABLE_NAME = "TableName";

    private static final String NUMNBER_OF_RECORDS = "numberOfRecords";
    private static final String MOVEROW_TABLE_NAME ="MoveRowToTableName";
    private static final String POLLMILLI_SECONDS = "PollMilliSeconds";
    private static final String OPERATION_TYPE="operationType";
    
 // private static final String FLAGCOL_ATTR = "FlagColumn";    
    private static final String JNDI_NAME = "jndiname";
    private static final String JNDI_ADD_ELE = "jdbc:address";
    
    private static final String SERVICE_NAME = "Service";
    private static final String PORT_NAME = "Port";
    private static final String PORT_TYPE = "jdbcPortType";
            
    private DBTable mTable;
    private String xsdTopEleName;
    private String xsdName;
    private static final String XSD_EXT = ".xsd";
    private String mDBType;
    private String mJNDIName;
    private String mTableName = null;
    private String sqlText;
    private boolean isSelectStmt = false;
    
    private DBConnectionDefinition dbinfo;
    
    private DBQueryModel dbDataAccessObject = null;
    static {
        PrepStmtWSDLGenerator.initFactory();
    }

    /**
     * Constructor
     * @param wsdl File name
     * @param wsdl filelocation
     * @param dbtype
     * @param jndiName
     */
    public PrepStmtWSDLGenerator(final String wsdlFileName, final String wsdlFileLocation, final String dbtype, final String jndiName) {
        this.mWSDLFileName = wsdlFileName;
        this.wsdlFileLocation = wsdlFileLocation;
        this.mDBType = dbtype;
        this.mJNDIName = jndiName;
        this.setTopEleName();
        this.setXSDName();
    }
    
    /**
     * initialize the WSDLFactory
     */
    private static void initFactory() {
        if (PrepStmtWSDLGenerator.factory == null) {
            try {
                PrepStmtWSDLGenerator.factory = WSDLFactory.newInstance();
                PrepStmtWSDLGenerator.docBuilderfactory = DocumentBuilderFactory.newInstance();
            } catch (final WSDLException wsdle) {
                PrepStmtWSDLGenerator.logger.log(Level.WARNING, wsdle.getMessage(), wsdle);
            }
        }
    }
    /**
     * 
     *
     */
    public void setTopEleName(){
    	this.xsdTopEleName = this.mWSDLFileName;
    }
    /**
     * 
     *
     */
    public void setXSDName(){
    	this.xsdName = this.mWSDLFileName + PrepStmtWSDLGenerator.XSD_EXT;	
    }
    
    public void setDBInfo(DBConnectionDefinition dbinfo){
        this.dbinfo = dbinfo;
    }
    
    public void setSql(String sql){
    	this.sqlText = sql;    	
    }
    
   /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate() throws WSDLException, ParserConfigurationException, SAXException, IOException {
        Definition def = null;
        this.reader = PrepStmtWSDLGenerator.factory.newWSDLReader();

        try {
            final URL u = PrepStmtWSDLGenerator.class.getResource("jdbc.wsdl.template.prep");
            if (u != null) {
                final String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
                PrepStmtWSDLGenerator.docBuilderfactory.setNamespaceAware(true);
                PrepStmtWSDLGenerator.docBuilderfactory.setValidating(false);
                this.doc = PrepStmtWSDLGenerator.docBuilderfactory.newDocumentBuilder().parse(wsdlURI);
                def = this.reader.readWSDL(wsdlURI, this.doc);

            } else {
                PrepStmtWSDLGenerator.logger.log(Level.WARNING, "Unable to locate the wsdl template");
            }
        } catch (final WSDLException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final ParserConfigurationException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final SAXException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final IOException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return def;
    }
    
   /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate(WSDLComponent wsdlComponent) throws WSDLException, ParserConfigurationException, SAXException, IOException {
        Definition def = null;
        this.reader = PrepStmtWSDLGenerator.factory.newWSDLReader();

        try {
            final URL u = PrepStmtWSDLGenerator.class.getResource("jdbc.wsdl.template.prep");
            if (u != null) {
                final String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
                PrepStmtWSDLGenerator.docBuilderfactory.setNamespaceAware(true);
                PrepStmtWSDLGenerator.docBuilderfactory.setValidating(false);
                this.doc = PrepStmtWSDLGenerator.docBuilderfactory.newDocumentBuilder().parse(wsdlURI);
                if (wsdlComponent != null) {
                    this.wsdlDocumentation = wsdlComponent.getModel().getDocument();
                }
                def = this.reader.readWSDL(wsdlURI, this.doc);

            } else {
                PrepStmtWSDLGenerator.logger.log(Level.WARNING, "Unable to locate the wsdl template");
            }
        } catch (final WSDLException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final ParserConfigurationException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final SAXException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final IOException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return def;
    }
    
    /**
     * Generates the WSDL
     * @return
     */
    public Definition generatePrepStmtWSDL() {
        try {
            this.def = this.getWsdlTemplate();
            this.modifyPrepStmtWSDL();
            this.writeWsdl();
        } catch (final WSDLException wsdle) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, wsdle.getMessage(), wsdle);
        } catch (final ParserConfigurationException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final SAXException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final IOException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }catch(final Exception e){
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return this.def;
    }
    
    /**
     * Generates the WSDL
     * @return
     */
    public WSDLModel generatePrepStmtWSDL(WSDLComponent wsdlComponent) {
        WSDLModel wsdlModel = null;
        try {
            if (wsdlComponent == null) {
                // original code
                this.def = this.getWsdlTemplate();
                this.modifyPrepStmtWSDL();
                this.writeWsdl();
            } else {
                this.def = this.getWsdlTemplate(wsdlComponent);
                this.modifyPrepStmtWSDL();
                File file = this.writeWsdl(wsdlComponent);      
                wsdlModel = prepareModelFromFile(file, null, null);               
            }
        } catch (final WSDLException wsdle) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, wsdle.getMessage(), wsdle);
        } catch (final ParserConfigurationException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final SAXException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final IOException e) {
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }catch(final Exception e){
            PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return wsdlModel;
    }
    
    /**
     * Modify the WSDL Template
     * @throws WSDLException
     * @throws Exception
     */
    private void modifyPrepStmtWSDL() throws WSDLException, Exception {
        this.modifyName();
        this.modifyTargetNamespace();
        this.modifySchEle();
        this.modifyMessageEles();
        this.modifyPortTypes();
        this.modifyPrepStmtBindings();
        this.modifyServiceAndPortNames();
        this.modifyJNDIAddress();
    }
    
    /**
     * 
     * @throws WSDLException
     */
    public void modifyPrepStmtBindings() throws Exception {
        try {
        	Element rootEle = this.doc.getDocumentElement();
	        Element binding = null;
	        Element operation = null;
	        final NodeList childNodes = rootEle.getElementsByTagName("binding");
	        for (int i = 0; i < childNodes.getLength(); i++) {
	            final Node child = childNodes.item(i);
	            if (child instanceof Element) {
	            	binding = (Element) child;
	                break;
	            }
	        }
	        
	        final NodeList childNodes1 = binding.getElementsByTagName("operation");
	        for (int i = 0; i < childNodes1.getLength(); i++) {
	            final Node child = childNodes1.item(i);
	            if (child instanceof Element) {
	            	operation = (Element) child;
	                break;
	            }
	        }
	        Attr attr = operation.getAttributeNode("name");
	        attr.setNodeValue(this.mWSDLFileName + "_Operation");
            
            this.modifyPrepStmtFind();
            
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Could not generate the WSDL");
        }
    }
    
    public void modifyPortTypes() throws WSDLException, Exception {
        try {
            Element rootEle = this.doc.getDocumentElement();
	        Element portTypes = null;
	        Element operation = null;
	        final NodeList childNodes = rootEle.getElementsByTagName("portType");
	        for (int i = 0; i < childNodes.getLength(); i++) {
	            final Node child = childNodes.item(i);
	            if (child instanceof Element) {
	            	portTypes = (Element) child;
	                break;
	            }
	        }
	        
	        final NodeList childNodes1 = portTypes.getElementsByTagName("operation");
	        for (int i = 0; i < childNodes1.getLength(); i++) {
	            final Node child = childNodes1.item(i);
	            if (child instanceof Element) {
	            	operation = (Element) child;
	                break;
	            }
	        }
	        Attr attr = operation.getAttributeNode("name");
	        attr.setNodeValue(this.mWSDLFileName + "_Operation");
	            
	        }catch (final Exception e) {
	            throw new WSDLException(WSDLException.OTHER_ERROR,
	                    "Could not generate the WSDL");
	        }
    }
    
    
    /**
     * 
     * @param dao
     * @param query
     * @throws WSDLException
     * @throws Exception
     */
    public void modifyPrepStmtFind() throws WSDLException, Exception {
        try {
            String primaryKey = null;
            final Element rootEle = this.doc.getDocumentElement();
            final Element findSqlEle = this.getElementBySQL(rootEle, PrepStmtWSDLGenerator.QUERY);
            primaryKey = "";

            Attr optType = findSqlEle
            .getAttributeNode(PrepStmtWSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue(this.mWSDLFileName + "_Operation");
        
            Attr attrParamOrder = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue("");

            Attr attrSqlEle = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(this.sqlText);
        
            Attr attrPrmKey = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);
        
            Attr attrTrans = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");
        
            Attr attrTableName = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mWSDLFileName);
        
            Attr attrNumOfRecs = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");
        
            Attr attrMarkColVal = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");
        
            Attr attrMovRowname = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");
        
            Attr attrMarkColName = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");
        
            Attr attrPollMilli = findSqlEle
                    .getAttributeNode(PrepStmtWSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Could not generate the WSDL");
        }
    }
    
    
    /**
     * Modify the WSDL name
     *
     */
    private void modifyName() {
        /* QName q = this.def.getQName();
        q = new QName(q.getNamespaceURI(), this.mWSDLFileName);
        this.def.setQName(q); */
        Element rootEle = this.doc.getDocumentElement();
        Attr attr = rootEle.getAttributeNode(NAME);
        attr.setNodeValue(this.mWSDLFileName);
    }
    
    /**
     * Modify the WSDL TargetNamespace
     *
     */
    private void modifyTargetNamespace() {
        Element rootEle = this.doc.getDocumentElement();
        Attr attr = rootEle.getAttributeNode(TARGET_NS);
        attr.setNodeValue(TARGET_NS_PREFIX_STRING + this.mWSDLFileName);
        attr = rootEle.getAttributeNode(TNS_STRING);
        attr.setNodeValue(TARGET_NS_PREFIX_STRING + this.mWSDLFileName);
    }
    
    /**
     * 
     * @throws WSDLException
     * @throws Exception
     */
    public void modifySchEle() throws WSDLException, Exception{
        Element rootEle = this.doc.getDocumentElement();
        Element scheEle = null;
        final NodeList childNodes = rootEle.getElementsByTagName(PrepStmtWSDLGenerator.SCHEMA_ELE);
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child instanceof Element) {
                scheEle = (Element) child;
                break;
            }
        }
        //Change schema element targetnamespace
        Attr attr = scheEle.getAttributeNode(PrepStmtWSDLGenerator.TARGET_NS);
        attr.setNodeValue(PrepStmtWSDLGenerator.JDBC_NAMESPACE);
        
        Element importEle = null;
        final NodeList childNodesTmp = rootEle.getElementsByTagName(PrepStmtWSDLGenerator.IMPORT_ELEMENT);
        for (int i = 0; i < childNodesTmp.getLength(); i++) {
            final Node childTmp = childNodesTmp.item(i);
            if (childTmp instanceof Element) {
                importEle = (Element) childTmp;
                break;
            }
        }
        //Change import element namespace and schemalocation
        Attr attrimp = importEle.getAttributeNode(PrepStmtWSDLGenerator.NAMESPACE_ATTR);
        attrimp.setNodeValue(PrepStmtWSDLGenerator.TARGETNAMESPACE);
        Attr schloc = importEle.getAttributeNode(PrepStmtWSDLGenerator.SCHEMALOCATION_ATTR);
        schloc.setNodeValue(this.xsdName);
    }
   public void modifyMessageEles()throws WSDLException, Exception{
       Element rootEle = this.doc.getDocumentElement();
       final Element inputMsgEle = this.getElementByName(rootEle, PrepStmtWSDLGenerator.INPUTMESSAGE_NAME);
       final NodeList partNodes = inputMsgEle.getChildNodes();
       Element partEle = null;
       for (int i = 0; i < partNodes.getLength(); i++) {
            final Node child = partNodes.item(i);
            if (child instanceof Element) {
                partEle = (Element) child;
                break;
            }
        }
        Attr attrInput = partEle.getAttributeNode(PrepStmtWSDLGenerator.ELEMENT_ATTR);
        attrInput.setNodeValue(PrepStmtWSDLGenerator.NS_STRING + this.xsdTopEleName + "_Request");
        
        Element partOutEle = null;
        final Element outputMsgEle = this.getElementByName(rootEle, PrepStmtWSDLGenerator.OUTPUTMESSAGE_NAME);
        final NodeList outPartNodes = outputMsgEle.getChildNodes();
        for (int i = 0; i < outPartNodes.getLength(); i++) {
            final Node child = outPartNodes.item(i);
            if (child instanceof Element) {
                partOutEle = (Element) child;
                break;
            }
        }
        Attr attroutput = partOutEle.getAttributeNode(PrepStmtWSDLGenerator.ELEMENT_ATTR);
        if(isSelectStmt){
        	attroutput.setNodeValue(PrepStmtWSDLGenerator.NS_STRING + this.xsdTopEleName + "_Response");
        } else{
        	attroutput.setNodeValue(PrepStmtWSDLGenerator.NS_STRING + "numRowsEffected");
        }
        this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
   }
    
    
    /**
     * @throws WSDLException
     * @throws Exception
     */
    public void modifyServiceAndPortNames() throws WSDLException, Exception {
        try {
            final Element rootEle = this.doc.getDocumentElement();
            final NodeList list = rootEle.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                final Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (n.getLocalName().equalsIgnoreCase(PrepStmtWSDLGenerator.SERVICE_NAME)) {
                        Element serEle = (Element) n;
                        Attr attrSer = serEle.getAttributeNode("name");
                        attrSer.setNodeValue(this.mWSDLFileName + PrepStmtWSDLGenerator.SERVICE_NAME);
                        
                        final NodeList childList = n.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            final Node childNode = childList.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (childNode.getLocalName().equalsIgnoreCase(PORT_NAME)) {
                                    Element portEle = (Element) childNode;
                                    Attr attrPort = portEle.getAttributeNode("name");
                                    attrPort.setNodeValue(this.mWSDLFileName + PrepStmtWSDLGenerator.PORT_NAME);
                                }
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR, "Could not generate the WSDL");
        }
    }
    
    /**
     * 
     * @throws WSDLException
     * @throws Exception
     */
    public void modifyJNDIAddress() throws WSDLException, Exception {
        try {
            final Element rootEle = this.doc.getDocumentElement();
            final Element jndiEle = this.getElementByAddress(rootEle, PrepStmtWSDLGenerator.JNDI_NAME);
            
            Attr attrJndi = jndiEle.getAttributeNode("jndiName");
            attrJndi.setNodeValue(this.mJNDIName);
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Could not generate the WSDL");
        }
    }
    
   
    /**
     * 
     * @return
     */
    public DBQueryModel getQueryGenerator(){
        DBQueryModel objDataAccess = null;
        if(this.mDBType.equalsIgnoreCase("DERBY")){
            objDataAccess = DerbyQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("ORACLE")) {
            objDataAccess = OracleQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("DB2")) {
            objDataAccess = DB2QueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("SQLServer")) {
            objDataAccess = SQLServerQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("MYSQL")) {
            objDataAccess = MySQLQueryGenerator.getInstance();
        }else {
            objDataAccess = JdbcQueryGenerator.getInstance();
        }
        
        return objDataAccess;
    }
  
    /**
     * Helper method to return the Element with the name elementName from a 
     * top level element e. The method recursively looks thru sub elements and 
     * returns it once it is found. or a null.
     * @param e
     * @param elementName
     * @return
     */
      private Element getElementByName(final Element e, final String elementName) {
            if (e.getAttribute("name").equalsIgnoreCase(elementName)) {
                return e;
            }
            final NodeList list = e.getChildNodes();
            Element el = null;
            Element e2 = null;
            for (int i = 0; i < list.getLength(); i++) {
                if (e2 == null) {
                    final Node n = list.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        el = (Element) n;
                        if (el.getAttribute("name").equalsIgnoreCase(elementName)) {
                            e2 = el;
                            break;
                        } else {
                            e2 = this.getElementByName(el, elementName);
                            if (e2 != null && e2.getAttribute("name").equalsIgnoreCase(elementName)) {
                                return e2;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            return e2;
        }
    /**
     * 
     * @param e
     * @param elementName
     * @return
     */
    private Element getElementBySQL(final Element e, final String elementName) {
        if (e.getAttribute("sql").equalsIgnoreCase(elementName)) {
            return e;
        }
        final NodeList list = e.getChildNodes();
        Element el = null;
        Element e2 = null;
        for (int i = 0; i < list.getLength(); i++) {
            if (e2 == null) {
                final Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    el = (Element) n;
                    if (el.getAttribute("input").equalsIgnoreCase(elementName)) {
                        e2 = el;
                        break;
                    } else {
                        e2 = this.getElementBySQL(el, elementName);
                        if (e2 != null && e2.getAttribute("sql").equalsIgnoreCase(elementName)) {
                            return e2;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return e2;
    }
    /**
     * 
     * @param e
     * @param elementName
     * @return
     */
    private Element getElementByAddress(final Element e, final String elementName) {
        if (e.getAttribute("jndiName").equalsIgnoreCase(elementName)) {
            return e;
        }
        final NodeList list = e.getChildNodes();
        Element el = null;
        Element e2 = null;
        for (int i = 0; i < list.getLength(); i++) {
            if (e2 == null) {
                final Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    el = (Element) n;
                    if (el.getAttribute("jndiName").equalsIgnoreCase(elementName)) {
                        e2 = el;
                        break;
                    } else {
                        e2 = this.getElementByAddress(el, elementName);
                        if (e2 != null && e2.getAttribute("jndiName").equalsIgnoreCase(elementName)) {
                            return e2;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return e2;
    }

   /**
     * persist the wsdl file to disk
     *
     * @throws WSDLException
     */
    private File writeWsdl() throws WSDLException {
        return writeWsdl(null);
    }    

   /**
     * persist the wsdl file to disk
     *
     * @throws WSDLException
     */
    private File writeWsdl(WSDLComponent wsdlComponent) throws WSDLException {
        File wsdlFile = null;
        try {
            if (wsdlComponent != null) {
                wsdlFile = File.createTempFile(mWSDLFileName + "JDBC", ".wsdl");
                wsdlFile.deleteOnExit();
            }            
            final WSDLWriter writer = PrepStmtWSDLGenerator.factory.newWSDLWriter();
            String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
            if ((wsdlComponent != null) && (wsdlFile != null)) {
                outputFileName = wsdlFile.getAbsolutePath();
            }
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
            final Writer sink = new java.io.OutputStreamWriter(fos, FileEncodingQuery.getDefaultEncoding());
            writer.writeWSDL(this.def, sink);
            PrepStmtWSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                " using the file encoding:"+ FileEncodingQuery.getDefaultEncoding());
        } catch (final Exception e) {
           if(e instanceof FileNotFoundException){
               PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof IOException){
               PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof WSDLException){ 
            if((((WSDLException)e).getMessage()).indexOf("Unsupported Java encoding for writing wsdl file") != -1){
                try{ 
                   final WSDLWriter writer = PrepStmtWSDLGenerator.factory.newWSDLWriter();
                   String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
                   if (wsdlComponent != null) {
                       outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + "_";
                   }
                   java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
                   final Writer sink = new java.io.OutputStreamWriter(fos,"UTF-8");
                   writer.writeWSDL(this.def, sink);
                   PrepStmtWSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                        " using the file encoding: UTF-8");                
                   }catch(Exception ex){
                       PrepStmtWSDLGenerator.logger.log(Level.SEVERE, ex.getMessage());
                   }
                }else PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else PrepStmtWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
        }
        return wsdlFile;
    }
    
    /**
     * Helper method which sets the sql statement type from the sql text.
     * @param sqlText
     */
    public void parseSQLStatement() {
        sqlText = sqlText.trim();
        String modSQLText = sqlText.toUpperCase();
        if (modSQLText.startsWith("SELECT")) {
            isSelectStmt=true;
        } else {
        	isSelectStmt=false;
        }

    }       
    
    /**
     * Load and initialize the WSDL model from the given file, which should
     * already have a minimal WSDL definition. The preparation includes
     * setting the definition name, adding a namespace and prefix, and
     * adding the types component.
     *
     * @param  file  the file with a minimal WSDL definition.
     * @return  the model.
     */
    public static WSDLModel prepareModelFromFile(File file, String definitionName,
            String targetNameSpace) {
        File f = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(f);
        ModelSource modelSource = org.netbeans.modules.xml.retriever.
                catalog.Utilities.getModelSource(fobj, fobj.canWrite());
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model.getState() == WSDLModel.State.VALID) {
            model.startTransaction();
            if (definitionName != null) {
                model.getDefinitions().setName(definitionName);
            }
            if (targetNameSpace != null) {
                model.getDefinitions().setTargetNamespace(targetNameSpace);
                ((AbstractDocumentComponent) model.getDefinitions()).
                        addPrefix("tns", targetNameSpace);
            }
            if (model.getDefinitions().getTypes() == null) {
                model.getDefinitions().setTypes(model.getFactory().createTypes());
            }
            model.endTransaction();
        } else {
            assert false : "Model is invalid, correct the template if any";
        }
        return model;
    }    
    
}
