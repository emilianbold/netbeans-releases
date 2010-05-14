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
package org.netbeans.modules.wsdlextensions.jdbc.builder.wsdl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.HashMap;

import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.IOException;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.Definition;

import javax.wsdl.WSDLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.wsdlextensions.jdbc.builder.Procedure;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.DBQueryModel;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.DerbyQueryGenerator;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.OracleQueryGenerator;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.DB2QueryGenerator;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.SQLServerQueryGenerator;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.JdbcQueryGenerator;
import org.netbeans.modules.wsdlextensions.jdbc.builder.model.MySQLQueryGenerator;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class StoredProcWSDLGenerator {

    public static HashMap builtInTypes = new HashMap();
    private static final Logger logger = Logger.getLogger(StoredProcWSDLGenerator.class.getName());
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
//  private static final String JDBC_SQL_ELEMENT = "jdbc:sql";
    private static final String JDBC_SQL_ELEMENT = "jdbc:input";
    private static final String SQL_ELEMENT = "sql";
    private static final String PARAM_ORDER = "paramOrder";
    private static final String JDBC_NAMESPACE = "http://schemas.sun.com/jbi/wsdl-extensions/jdbc/";
    private static final String SCHEMA_ELE = "xsd:schema";
    private static final String PRIMARYKEY_ATTR = "PKName";
    private static final String TRANSACTION = "Transaction";
    private static final String MARK_COLUMN_VALUE = "MarkColumnValue";
    private static final String MARK_COLUMN_NAME = "MarkColumnName";
    private static final String POLL_POST_PROCESS = "PollingPostProcessing";
    private static final String PROCEDURE_NAME = "ProcedureName";
    private static final String NUMNBER_OF_RECORDS = "numberOfRecords";
    private static final String MOVEROW_TABLE_NAME = "MoveRowToTableName";
    private static final String POLLMILLI_SECONDS = "PollMilliSeconds";
    private static final String OPERATION_TYPE = "operationType";
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
    private String mWSDLTargetNamespace;
    private Procedure mStoredProc;
    private DBConnectionDefinition dbinfo;
    private DBQueryModel dbDataAccessObject = null;
    

    static {
        StoredProcWSDLGenerator.initFactory();
    }

    /**
     * Constructor
     * @param wsdl File name
     * @param wsdl filelocation
     * @param dbtype
     * @param jndiName
     */
    public StoredProcWSDLGenerator(final String wsdlFileName, final String wsdlFileLocation, final String dbtype, final String jndiName, final String wsdlTargetNamespace) {
        this.mWSDLFileName = wsdlFileName;
        this.wsdlFileLocation = wsdlFileLocation;
        this.mDBType = dbtype;
        this.mJNDIName = jndiName;
        this.mWSDLTargetNamespace = wsdlTargetNamespace;
        this.setTopEleName();
        this.setXSDName();
    }

    /**
     * initialize the WSDLFactory
     */
    private static void initFactory() {
        if (StoredProcWSDLGenerator.factory == null) {
            try {
                StoredProcWSDLGenerator.factory = WSDLFactory.newInstance();
                StoredProcWSDLGenerator.docBuilderfactory = DocumentBuilderFactory.newInstance();
            } catch (final WSDLException wsdle) {
                StoredProcWSDLGenerator.logger.log(Level.WARNING, wsdle.getMessage(), wsdle);
            }
        }
    }

    /**
     * 
     *
     */
    public void setTopEleName() {
        this.xsdTopEleName = this.mWSDLFileName;
    }

    /**
     * 
     *
     */
    public void setXSDName() {
        this.xsdName = this.mWSDLFileName + StoredProcWSDLGenerator.XSD_EXT;
    }

    public void setDBInfo(DBConnectionDefinition dbinfo) {
        this.dbinfo = dbinfo;
    }

    void setStoredProcedure(Procedure mProcedure) {
        this.mStoredProc = mProcedure;
    }

    /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate() throws WSDLException, ParserConfigurationException, SAXException, IOException {
        return getWsdlTemplate(null);
    }

    /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate(WSDLComponent wsdlComponent) throws WSDLException, ParserConfigurationException, SAXException, IOException {
        Definition def = null;
        this.reader = StoredProcWSDLGenerator.factory.newWSDLReader();

        try {
            final URL u = StoredProcWSDLGenerator.class.getResource("jdbc.wsdl.template.proc");
            if (u != null) {
                final String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
                StoredProcWSDLGenerator.docBuilderfactory.setNamespaceAware(true);
                StoredProcWSDLGenerator.docBuilderfactory.setValidating(false);
                this.doc = StoredProcWSDLGenerator.docBuilderfactory.newDocumentBuilder().parse(wsdlURI);
                if (wsdlComponent != null) {
                    this.wsdlDocumentation = wsdlComponent.getModel().getDocument();
                }   
                
                def = this.reader.readWSDL(wsdlURI, this.doc);

            } else {
                StoredProcWSDLGenerator.logger.log(Level.WARNING, "Unable to locate the wsdl template");
            }
        } catch (final WSDLException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final ParserConfigurationException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final SAXException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (final IOException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return def;
    }
    
    /**
     * Generates the WSDL
     * @return
     */
    public Definition generateProcWSDL() {
        try {
            this.def = this.getWsdlTemplate();
            this.modifyProcWSDL();
            this.writeWsdl();
        } catch (final WSDLException wsdle) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, wsdle.getMessage(), wsdle);
        } catch (final ParserConfigurationException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final SAXException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final IOException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final Exception e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return this.def;
    }

    /**
     * Generates the WSDL
     * @return
     */
    public WSDLModel generateProcWSDL(WSDLComponent wsdlComponent) {
        WSDLModel wsdlModel = null;
        try {
            if (wsdlComponent == null) {
                // original code
                this.def = this.getWsdlTemplate();
                this.modifyProcWSDL();
                this.writeWsdl();
            } else {
                this.def = this.getWsdlTemplate(wsdlComponent);
                this.modifyProcWSDL();
                File file = this.writeWsdl(wsdlComponent);      
                wsdlModel = prepareModelFromFile(file, null, null);
                deleteWsdl();                
            }
        } catch (final WSDLException wsdle) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, wsdle.getMessage(), wsdle);
        } catch (final ParserConfigurationException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final SAXException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final IOException e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (final Exception e) {
            StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return wsdlModel;
    }    

    /**
     * Modify the WSDL Template
     * @throws WSDLException
     * @throws Exception
     */
    private void modifyProcWSDL() throws WSDLException, Exception {
        this.modifyName();
        this.modifyTargetNamespace();
        this.modifySchEle();
        this.modifyMessageEles();
        this.modifyPortTypes();
        this.modifyProcBindings();
        this.modifyServiceAndPortNames();
        this.modifyJNDIAddress();
    }

    /**
     * 
     * @throws WSDLException
     */
    public void modifyProcBindings() throws Exception {
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
            attr.setNodeValue(this.mWSDLFileName + "_execute");

            this.modifyExecuteOperation(operation);

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
            attr.setNodeValue(this.mStoredProc.getName() + "_execute");

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
    public void modifyExecuteOperation(Element bindingOp) throws WSDLException, Exception {
        try {
            final Element procedureNameElem = getElementByAttribute(bindingOp, "ExecutionString", "call");

            Attr optType = procedureNameElem.getAttributeNode("ExecutionString");
            optType.setNodeValue(this.mStoredProc.getCallableStmtString());



            Attr attrTrans = procedureNameElem.getAttributeNode(StoredProcWSDLGenerator.TRANSACTION);
            attrTrans.setNodeValue("NOTransaction");

            Attr attrTableName = procedureNameElem.getAttributeNode(StoredProcWSDLGenerator.PROCEDURE_NAME);
            attrTableName.setNodeValue(this.mStoredProc.getName());

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
        attr.setNodeValue(this.mWSDLTargetNamespace);
        attr = rootEle.getAttributeNode(TNS_STRING);
        attr.setNodeValue(this.mWSDLTargetNamespace);
    }

    /**
     * 
     * @throws WSDLException
     * @throws Exception
     */
    public void modifySchEle() throws WSDLException, Exception {
        Element rootEle = this.doc.getDocumentElement();
        Element scheEle = null;
        final NodeList childNodes = rootEle.getElementsByTagName(StoredProcWSDLGenerator.SCHEMA_ELE);
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child instanceof Element) {
                scheEle = (Element) child;
                break;
            }
        }
        //Change schema element targetnamespace
        Attr attr = scheEle.getAttributeNode(StoredProcWSDLGenerator.TARGET_NS);
        attr.setNodeValue(StoredProcWSDLGenerator.JDBC_NAMESPACE);

        Element importEle = null;
        final NodeList childNodesTmp = rootEle.getElementsByTagName(StoredProcWSDLGenerator.IMPORT_ELEMENT);
        for (int i = 0; i < childNodesTmp.getLength(); i++) {
            final Node childTmp = childNodesTmp.item(i);
            if (childTmp instanceof Element) {
                importEle = (Element) childTmp;
                break;
            }
        }
        //Change import element namespace and schemalocation
        Attr attrimp = importEle.getAttributeNode(StoredProcWSDLGenerator.NAMESPACE_ATTR);
        attrimp.setNodeValue(StoredProcWSDLGenerator.TARGETNAMESPACE);
        Attr schloc = importEle.getAttributeNode(StoredProcWSDLGenerator.SCHEMALOCATION_ATTR);
        schloc.setNodeValue(this.xsdName);
    }

    public void modifyMessageEles() throws WSDLException, Exception {
        Element rootEle = this.doc.getDocumentElement();
        final Element inputMsgEle = this.getElementByName(rootEle, StoredProcWSDLGenerator.INPUTMESSAGE_NAME);
        final NodeList partNodes = inputMsgEle.getChildNodes();
        Element partEle = null;
        for (int i = 0; i < partNodes.getLength(); i++) {
            final Node child = partNodes.item(i);
            if (child instanceof Element) {
                partEle = (Element) child;
                break;
            }
        }
        Attr attrInput = partEle.getAttributeNode(StoredProcWSDLGenerator.ELEMENT_ATTR);
        attrInput.setNodeValue(StoredProcWSDLGenerator.NS_STRING + this.mStoredProc.getName() + "_Request");

        Element partOutEle = null;
        final Element outputMsgEle = this.getElementByName(rootEle, StoredProcWSDLGenerator.OUTPUTMESSAGE_NAME);
        final NodeList outPartNodes = outputMsgEle.getChildNodes();
        for (int i = 0; i < outPartNodes.getLength(); i++) {
            final Node child = outPartNodes.item(i);
            if (child instanceof Element) {
                partOutEle = (Element) child;
                break;
            }
        }
        Attr attroutput = partOutEle.getAttributeNode(StoredProcWSDLGenerator.ELEMENT_ATTR);
        attroutput.setNodeValue(StoredProcWSDLGenerator.NS_STRING + this.mStoredProc.getName() + "_Response");
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
                    if (n.getLocalName().equalsIgnoreCase(StoredProcWSDLGenerator.SERVICE_NAME)) {
                        Element serEle = (Element) n;
                        Attr attrSer = serEle.getAttributeNode("name");
                        attrSer.setNodeValue(this.mWSDLFileName + StoredProcWSDLGenerator.SERVICE_NAME);

                        final NodeList childList = n.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            final Node childNode = childList.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (childNode.getLocalName().equalsIgnoreCase(PORT_NAME)) {
                                    Element portEle = (Element) childNode;
                                    Attr attrPort = portEle.getAttributeNode("name");
                                    attrPort.setNodeValue(this.mWSDLFileName + StoredProcWSDLGenerator.PORT_NAME);
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
            final Element jndiEle = this.getElementByAddress(rootEle, StoredProcWSDLGenerator.JNDI_NAME);

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
    public DBQueryModel getQueryGenerator() {
        DBQueryModel objDataAccess = null;
        if (this.mDBType.equalsIgnoreCase("DERBY")) {
            objDataAccess = DerbyQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("ORACLE")) {
            objDataAccess = OracleQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("DB2")) {
            objDataAccess = DB2QueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("SQLServer")) {
            objDataAccess = SQLServerQueryGenerator.getInstance();
        } else if (this.mDBType.equalsIgnoreCase("MYSQL")) {
            objDataAccess = MySQLQueryGenerator.getInstance();
        } else {
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
     * Helper method to return the Element with the name elementName from a 
     * top level element e. The method recursively looks thru sub elements and 
     * returns it once it is found. or a null.
     * @param e
     * @param elementName
     * @return
     */
    private Element getElementByAttribute(final Element e, final String attrToFind, final String attrValue) {
        if (e.getAttribute(attrToFind).equalsIgnoreCase(attrValue)) {
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
                    if (el.getAttribute(attrToFind).equalsIgnoreCase(attrValue)) {
                        e2 = el;
                        break;
                    } else {
                        e2 = this.getElementByAttribute(el, attrToFind, attrValue);
                        if (e2 != null && e2.getAttribute(attrToFind).equalsIgnoreCase(attrValue)) {
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
            final WSDLWriter writer = StoredProcWSDLGenerator.factory.newWSDLWriter();
            String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
            if ((wsdlComponent != null) && (wsdlFile != null)) {
                outputFileName = wsdlFile.getAbsolutePath();
            }
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
            final Writer sink = new java.io.OutputStreamWriter(fos, FileEncodingQuery.getDefaultEncoding());
            writer.writeWSDL(this.def, sink);
            StoredProcWSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                    " using the file encoding:" + FileEncodingQuery.getDefaultEncoding());
        } catch (final Exception e) {
            if (e instanceof FileNotFoundException) {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            } else if (e instanceof IOException) {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            } else if (e instanceof WSDLException) {
                if ((((WSDLException) e).getMessage()).indexOf("Unsupported Java encoding for writing wsdl file") != -1) {
                    try {
                        final WSDLWriter writer = StoredProcWSDLGenerator.factory.newWSDLWriter();
                        String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl";
                        if (wsdlComponent != null) {
                            outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + "_";
                        }
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
                        final Writer sink = new java.io.OutputStreamWriter(fos, "UTF-8");
                        writer.writeWSDL(this.def, sink);
                        StoredProcWSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                                " using the file encoding: UTF-8");
                        if (wsdlComponent != null) {
                            wsdlFile = new File(outputFileName);
                            wsdlFile.deleteOnExit();
                        }                        
                    } catch (Exception ex) {
                        StoredProcWSDLGenerator.logger.log(Level.SEVERE, ex.getMessage());
                    }
                } else {
                    StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
                }
            } else {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }
        }
        return wsdlFile;
    }
    
   /**
     * persist the wsdl file to disk
     *
     * @throws WSDLException
     */
    private File deleteWsdl() throws WSDLException {
        File file = null;
        try {
            String outputFileName = this.wsdlFileLocation + File.separator + this.mWSDLFileName + ".wsdl.bak";
            file = new File(outputFileName);
            if ((file != null) && (file.exists())) {
                file.delete();
            }
        } catch (final Exception e) {
            if (e instanceof FileNotFoundException) {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            } else if (e instanceof IOException) {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            } else {
                StoredProcWSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }
        }
        return file;
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
