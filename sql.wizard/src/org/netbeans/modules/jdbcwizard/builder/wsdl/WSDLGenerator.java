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


public class WSDLGenerator {

    public static HashMap builtInTypes = new HashMap();
    private static final Logger logger = Logger.getLogger(WSDLGenerator.class.getName());
    private static WSDLFactory factory;
    private static DocumentBuilderFactory docBuilderfactory;
    
    public WSDLReader reader = null;

    private Definition def;
    private String mWSDLFileName;
    private String wsdlFileLocation;
    private Document doc;
    private static final String IMPORT_ELEMENT = "xsd:import";
    private static final String NAMESPACE_ATTR = "namespace";
    private static final String SCHEMALOCATION_ATTR = "schemaLocation";
    private static final String TARGET_NS = "targetNamespace";
    private static final String TARGET_NS_PREFIX_STRING = "http://j2ee.netbeans.org/wsdl/";
    private static final String TNS_STRING = "xmlns:tns";
    private static final String NAME = "name";
     
    private static final String PART_ELEMENT = "part";
    private static final String NAME_ATTR = "name";
    private static final String ELEMENT_ATTR = "element";
    private static final String TARGETNAMESPACE = "http://j2ee.netbeans.org/xsd/tableSchema";
    private static final String INPUTMESSAGE_NAME = "inputMsg";
    private static final String OUTPUTMESSAGE_NAME = "outputMsg";
    private static final String XMLSCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    private static final String INSERT_QUERY = "insertQuery";
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
        
    private DBTable mTable;
    private String xsdTopEleName;
    private String xsdName;
    private static final String XSD_EXT = ".xsd";
    private String mDBType;
    private String mJNDIName;
    private String mTableName = null;

    private DBConnectionDefinition dbinfo;
    
    private DBQueryModel dbDataAccessObject = null;
    static {
        WSDLGenerator.initFactory();
    }

    /**
     * Constructor
     * @param dbmeta
     * @param sqlFile
     * @param wsdlFileLocation
     */
    public WSDLGenerator(final DBTable table, final String wsdlFileName, final String wsdlFileLocation, final String dbtype, final String jndiName) {
        this.mWSDLFileName = wsdlFileName;
        this.wsdlFileLocation = wsdlFileLocation;
        this.mTable = table;
        this.mDBType = dbtype;
        this.mJNDIName = jndiName;
      // this.setTopEleName();
      //  this.setXSDName();
    }

    /**
     * initialize the WSDLFactory
     */
    private static void initFactory() {
        if (WSDLGenerator.factory == null) {
            try {
                WSDLGenerator.factory = WSDLFactory.newInstance();
                WSDLGenerator.docBuilderfactory = DocumentBuilderFactory.newInstance();
            } catch (final WSDLException wsdle) {
                WSDLGenerator.logger.log(Level.WARNING, wsdle.getMessage(), wsdle);
            }
        }
    }
    /**
     * 
     *
     */
    public void setTopEleName(){
        this.xsdTopEleName = "ns:" +  XMLCharUtil.makeValidNCName(this.mTable.getName());
    }
    /**
     * 
     *
     */
    public void setXSDName(){
        this.xsdName = XMLCharUtil.makeValidNCName(this.mTable.getName()) + WSDLGenerator.XSD_EXT;
    }
    
    public void setDBInfo(DBConnectionDefinition dbinfo){
        this.dbinfo = dbinfo;
    }
   /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate() throws WSDLException, ParserConfigurationException, SAXException, IOException {
        Definition def = null;
        this.reader = WSDLGenerator.factory.newWSDLReader();

        try {
            final URL u = WSDLGenerator.class.getResource("jdbc.wsdl.template");
            if (u != null) {
                final String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
                WSDLGenerator.docBuilderfactory.setNamespaceAware(true);
                WSDLGenerator.docBuilderfactory.setValidating(false);
                this.doc = WSDLGenerator.docBuilderfactory.newDocumentBuilder().parse(wsdlURI);
                def = this.reader.readWSDL(wsdlURI, this.doc);

            } else {
                WSDLGenerator.logger.log(Level.WARNING, "Unable to locate the wsdl template");
            }
        } catch (final WSDLException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final ParserConfigurationException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final SAXException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final IOException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return def;
    }
    /**
     * Generates the WSDL
     * @return
     */
    public Definition generateWSDL() {
        try {
            this.def = this.getWsdlTemplate();
            this.modifyWSDL();
            this.writeWsdl();
        } catch (final WSDLException wsdle) {
            WSDLGenerator.logger.log(Level.SEVERE, wsdle.getMessage(), wsdle);
        } catch (final ParserConfigurationException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final SAXException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final IOException e) {
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }catch(final Exception e){
            WSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return this.def;
    }

    /**
     * Modify the WSDL Template
     * @throws WSDLException
     * @throws Exception
     */
    private void modifyWSDL() throws WSDLException, Exception {
        this.modifyName();
        this.modifyTargetNamespace();
        this.modifySchEle();
        this.modiyMessageEles();    
        this.modifyBindings();
        this.modifyServiceAndPortNames();
        this.modifyJNDIAddress();
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
        final NodeList childNodes = rootEle.getElementsByTagName(WSDLGenerator.SCHEMA_ELE);
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child instanceof Element) {
                scheEle = (Element) child;
                break;
            }
        }
        //Change schema element targetnamespace
        Attr attr = scheEle.getAttributeNode(WSDLGenerator.TARGET_NS);
        attr.setNodeValue(WSDLGenerator.JDBC_NAMESPACE);
        
        Element importEle = null;
        final NodeList childNodesTmp = rootEle.getElementsByTagName(WSDLGenerator.IMPORT_ELEMENT);
        for (int i = 0; i < childNodesTmp.getLength(); i++) {
            final Node childTmp = childNodesTmp.item(i);
            if (childTmp instanceof Element) {
                importEle = (Element) childTmp;
                break;
            }
        }
        //Change import element namespace and schemalocation
        Attr attrimp = importEle.getAttributeNode(WSDLGenerator.NAMESPACE_ATTR);
        attrimp.setNodeValue(WSDLGenerator.TARGETNAMESPACE);
        Attr schloc = importEle.getAttributeNode(WSDLGenerator.SCHEMALOCATION_ATTR);
        schloc.setNodeValue(this.xsdName);
    }
   public void modiyMessageEles()throws WSDLException, Exception{
       Element rootEle = this.doc.getDocumentElement();
       final Element inputMsgEle = this.getElementByName(rootEle, WSDLGenerator.INPUTMESSAGE_NAME);
       final NodeList partNodes = inputMsgEle.getChildNodes();
       Element partEle = null;
       for (int i = 0; i < partNodes.getLength(); i++) {
            final Node child = partNodes.item(i);
            if (child instanceof Element) {
                partEle = (Element) child;
                break;
            }
        }
        Attr attrInput = partEle.getAttributeNode(WSDLGenerator.ELEMENT_ATTR);
        attrInput.setNodeValue(this.xsdTopEleName);
        
        Element partOutEle = null;
        final Element outputMsgEle = this.getElementByName(rootEle, WSDLGenerator.OUTPUTMESSAGE_NAME);
        final NodeList outPartNodes = outputMsgEle.getChildNodes();
        for (int i = 0; i < outPartNodes.getLength(); i++) {
            final Node child = outPartNodes.item(i);
            if (child instanceof Element) {
                partOutEle = (Element) child;
                break;
            }
        }
        Attr attroutput = partOutEle.getAttributeNode(WSDLGenerator.ELEMENT_ATTR);
        attroutput.setNodeValue(this.xsdTopEleName);
        this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
   }
    /**
     * 
     * @throws WSDLException
     */
    public void modifyBindings() throws Exception {
        try {
            String insertQuery = null;
            String updateQuery = null;
            String deleteQuery = null;
            String findQuery = null;
            String poolQuery = null;

            this.dbDataAccessObject = this.getQueryGenerator();
            this.dbDataAccessObject.init(this.mTable);

            //this.mTableName = this.mTable.getSchema()+"."+this.mTable.getName();
            this.mTableName = this.mTable.getName();
            // Generate Queries
            insertQuery = this.dbDataAccessObject.createInsertQuery();
            updateQuery = this.dbDataAccessObject.createUpdateQuery();
            deleteQuery = this.dbDataAccessObject.createDeleteQuery();
            findQuery = this.dbDataAccessObject.createFindQuery();
            poolQuery = this.dbDataAccessObject.createPoolQuery();

            this.modifyInsert(this.dbDataAccessObject, insertQuery);
            this.modifyUpdate(this.dbDataAccessObject, updateQuery);
            this.modifyDelete(this.dbDataAccessObject, deleteQuery);
            this.modifyFind(this.dbDataAccessObject, findQuery);
            this.modifyPoll(this.dbDataAccessObject, poolQuery);
        } catch (final Exception e) {
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
    public void modifyInsert(final DBQueryModel dao, final String query)
            throws WSDLException, Exception {
        try {
            String primaryKey = null;
            final Element rootEle = this.doc.getDocumentElement();

            final Element insSqlEle = this.getElementBySQL(rootEle,
                    WSDLGenerator.INSERT_QUERY);
            primaryKey = dao.getPrimaryKey();

            Attr optType = insSqlEle
                    .getAttributeNode(WSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue("insert");

            Attr attrParamOrder = insSqlEle
                    .getAttributeNode(WSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue(dao
                    .getParamOrder(WSDLGenerator.INSERT_QUERY));

            Attr attrSqlEle = insSqlEle
                    .getAttributeNode(WSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(query);

            Attr attrPrmKey = insSqlEle
                    .getAttributeNode(WSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);

            Attr attrTrans = insSqlEle
                    .getAttributeNode(WSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");

            Attr attrTableName = insSqlEle
                    .getAttributeNode(WSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mTableName);

            Attr attrNumOfRecs = insSqlEle
                    .getAttributeNode(WSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");

            Attr attrMarkColVal = insSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");

            Attr attrMovRowname = insSqlEle
                    .getAttributeNode(WSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");

            Attr attrMarkColName = insSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");

            Attr attrPollMilli = insSqlEle
                    .getAttributeNode(WSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
        } catch (final Exception e) {
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
    public void modifyUpdate(final DBQueryModel dao, final String query) throws WSDLException, Exception {
        try {
            String primaryKey = null;
            final Element rootEle = this.doc.getDocumentElement();

            final Element updateSqlEle = this.getElementBySQL(rootEle, WSDLGenerator.UPDATE_QUERY);
            primaryKey = dao.getPrimaryKey();
            
            Attr optType = updateSqlEle
            .getAttributeNode(WSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue("update");
        
            Attr attrParamOrder = updateSqlEle
                    .getAttributeNode(WSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue(dao.getParamOrder(WSDLGenerator.UPDATE_QUERY));

            Attr attrSqlEle = updateSqlEle
                    .getAttributeNode(WSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(query);
        
            Attr attrPrmKey = updateSqlEle
                    .getAttributeNode(WSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);
        
            Attr attrTrans = updateSqlEle
                    .getAttributeNode(WSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");
        
            Attr attrTableName = updateSqlEle
                    .getAttributeNode(WSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mTableName);
        
            Attr attrNumOfRecs = updateSqlEle
                    .getAttributeNode(WSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");
        
            Attr attrMarkColVal = updateSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");
        
            Attr attrMovRowname = updateSqlEle
                    .getAttributeNode(WSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");
        
            Attr attrMarkColName = updateSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");
        
            Attr attrPollMilli = updateSqlEle
                    .getAttributeNode(WSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
        } catch (final Exception e) {
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
    public void modifyDelete(final DBQueryModel dao, final String query) throws WSDLException, Exception {
        try {
            String primaryKey = null;
            final Element rootEle = this.doc.getDocumentElement();

            final Element delSqlEle = this.getElementBySQL(rootEle, WSDLGenerator.DELETE_QUERY);
            primaryKey = dao.getPrimaryKey();
            
            Attr optType = delSqlEle
            .getAttributeNode(WSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue("delete");
        
            Attr attrParamOrder = delSqlEle
                    .getAttributeNode(WSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue("");

            Attr attrSqlEle = delSqlEle
                    .getAttributeNode(WSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(query);
        
            Attr attrPrmKey = delSqlEle
                    .getAttributeNode(WSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);
        
            Attr attrTrans = delSqlEle
                    .getAttributeNode(WSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");
        
            Attr attrTableName = delSqlEle
                    .getAttributeNode(WSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mTableName);
        
            Attr attrNumOfRecs = delSqlEle
                    .getAttributeNode(WSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");
        
            Attr attrMarkColVal = delSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");
        
            Attr attrMovRowname = delSqlEle
                    .getAttributeNode(WSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");
        
            Attr attrMarkColName = delSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");
        
            Attr attrPollMilli = delSqlEle
                    .getAttributeNode(WSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
        } catch (final Exception e) {
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
    public void modifyFind(final DBQueryModel dao, final String query) throws WSDLException, Exception {
        try {
            String primaryKey = null;
            final Element rootEle = this.doc.getDocumentElement();
            final Element findSqlEle = this.getElementBySQL(rootEle, WSDLGenerator.FIND_QUERY);
            primaryKey = dao.getPrimaryKey();

            Attr optType = findSqlEle
            .getAttributeNode(WSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue("find");
        
            Attr attrParamOrder = findSqlEle
                    .getAttributeNode(WSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue("");

            Attr attrSqlEle = findSqlEle
                    .getAttributeNode(WSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(query);
        
            Attr attrPrmKey = findSqlEle
                    .getAttributeNode(WSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);
        
            Attr attrTrans = findSqlEle
                    .getAttributeNode(WSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");
        
            Attr attrTableName = findSqlEle
                    .getAttributeNode(WSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mTableName);
        
            Attr attrNumOfRecs = findSqlEle
                    .getAttributeNode(WSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");
        
            Attr attrMarkColVal = findSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");
        
            Attr attrMovRowname = findSqlEle
                    .getAttributeNode(WSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");
        
            Attr attrMarkColName = findSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");
        
            Attr attrPollMilli = findSqlEle
                    .getAttributeNode(WSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
        } catch (final Exception e) {
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
    public void modifyPoll(final DBQueryModel dao, final String query) throws WSDLException, Exception {
        try {
            final Element rootEle = this.doc.getDocumentElement();
            String primaryKey = null;
            final Element poolSqlEle = this.getElementBySQL(rootEle, WSDLGenerator.POLL_QUERY);
            
            // Create sql element to replace
            primaryKey = dao.getPrimaryKey();
            
            Attr optType = poolSqlEle
            .getAttributeNode(WSDLGenerator.OPERATION_TYPE);
            optType.setNodeValue("poll");
        
            Attr attrParamOrder = poolSqlEle
                    .getAttributeNode(WSDLGenerator.PARAM_ORDER);
            attrParamOrder.setNodeValue("");

            Attr attrSqlEle = poolSqlEle
                    .getAttributeNode(WSDLGenerator.SQL_ELEMENT);
            attrSqlEle.setNodeValue(query);
        
            Attr attrPrmKey = poolSqlEle
                    .getAttributeNode(WSDLGenerator.PRIMARYKEY_ATTR);
            attrPrmKey.setNodeValue(primaryKey);
        
            Attr attrTrans = poolSqlEle
                    .getAttributeNode(WSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");
        
            Attr attrTableName = poolSqlEle
                    .getAttributeNode(WSDLGenerator.TABLE_NAME);
            attrTableName.setNodeValue(this.mTableName);
        
            Attr attrNumOfRecs = poolSqlEle
                    .getAttributeNode(WSDLGenerator.NUMNBER_OF_RECORDS);
            attrNumOfRecs.setNodeValue("");
        
            Attr attrMarkColVal = poolSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_VALUE);
            attrMarkColVal.setNodeValue("");
        
            Attr attrMovRowname = poolSqlEle
                    .getAttributeNode(WSDLGenerator.MOVEROW_TABLE_NAME);
            attrMovRowname.setNodeValue("");
        
            Attr attrMarkColName = poolSqlEle
                    .getAttributeNode(WSDLGenerator.MARK_COLUMN_NAME);
            attrMarkColName.setNodeValue("");
            
            Attr attrPollPost = poolSqlEle.getAttributeNode(WSDLGenerator.POLL_POST_PROCESS);
            attrPollPost.setNodeValue("Delete");
    
            Attr attrPollMilli = poolSqlEle
                    .getAttributeNode(WSDLGenerator.POLLMILLI_SECONDS);
            attrPollMilli.setNodeValue("5000");
            
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Could not generate the WSDL");
        }
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
                    if (n.getLocalName().equalsIgnoreCase(WSDLGenerator.SERVICE_NAME)) {
                        Element serEle = (Element) n;
                        Attr attrSer = serEle.getAttributeNode("name");
                        attrSer.setNodeValue(this.mWSDLFileName + WSDLGenerator.SERVICE_NAME);
                        
                        final NodeList childList = n.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            final Node childNode = childList.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (childNode.getLocalName().equalsIgnoreCase(PORT_NAME)) {
                                    Element portEle = (Element) childNode;
                                    Attr attrPort = portEle.getAttributeNode("name");
                                    attrPort.setNodeValue(this.mWSDLFileName + WSDLGenerator.PORT_NAME);
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
            final Element jndiEle = this.getElementByAddress(rootEle, WSDLGenerator.JNDI_NAME);
            
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
    private void writeWsdl() throws WSDLException {
        try {
            final WSDLWriter writer = WSDLGenerator.factory.newWSDLWriter();
            final String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
            final Writer sink = new java.io.OutputStreamWriter(fos);
            writer.writeWSDL(this.def, sink);
            WSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName);
        } catch (final Exception e) {
           if(e instanceof FileNotFoundException){
               WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof IOException){
               WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof WSDLException){ 
            if((((WSDLException)e).getMessage()).indexOf("Unsupported Java encoding for writing wsdl file") != -1){
                try{ 
                   final WSDLWriter writer = WSDLGenerator.factory.newWSDLWriter();
                   final String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
                   java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
                   final Writer sink = new java.io.OutputStreamWriter(fos,"UTF-8");
                   writer.writeWSDL(this.def, sink);
                   WSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName);
                   }catch(Exception ex){
                       WSDLGenerator.logger.log(Level.SEVERE, ex.getMessage());
                   }
                }else WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
        }
    }
    
}
