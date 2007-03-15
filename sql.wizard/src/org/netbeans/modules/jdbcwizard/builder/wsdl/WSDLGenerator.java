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

package org.netbeans.modules.jdbcwizard.builder.wsdl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;


import java.net.URL;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

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
    	this.xsdTopEleName = "ns:" + this.mTable.getName();
    }
    /**
     * 
     *
     */
    public void setXSDName(){
    	this.xsdName = this.mTable.getName() + WSDLGenerator.XSD_EXT;
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
        this.modifyMessageTypes();
        this.modifyBindings();
        this.modifyServiceAndPortNames();
        this.modifyJNDIAddress();
        //modifyPortTypes();
        //modifyServices();
        //modifyPartnerLink();
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
    private void modifyMessageTypes() throws WSDLException, Exception{
        try {
            final Types types = this.def.getTypes();
             
            if (null != types) {
                final List schemaList = types.getExtensibilityElements();
                for (final Iterator iterator1 = schemaList.iterator(); iterator1.hasNext();) {
                    final Object o = iterator1.next();
                    if (o instanceof Schema) {
                        final Schema schemaType = (Schema) o;
                        final Element schEle = schemaType.getElement();
                        //Modify and rewrite the schema replacing default template schema
                        this.refineSchema(schEle);
                		
                		
                    } else if (o instanceof UnknownExtensibilityElement) {
                        //
                    }
                }
            }

        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR, "Could not generate the WSDL");
        }

    }
    /**
     * Modifies the default template schema
     * @param schEle
     * @throws WSDLException
     */
	private void refineSchema(final Element schEle) throws WSDLException {
		try {
			Element remEle = null;
			final NodeList childNodes = schEle.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node child = childNodes.item(i);
				if (child instanceof Element) {
					remEle = (Element) child;
					break;
				}
			}
			//modfiy the targetnamespace attribute of xsd:schema element. Set it to wsdlname
			Attr attr = schEle.getAttributeNode(TARGET_NS);
			attr.setNodeValue(TARGET_NS_PREFIX_STRING + this.mWSDLFileName);
			final Element importElem = this.doc.createElementNS(WSDLGenerator.XMLSCHEMA_NAMESPACE,
					WSDLGenerator.IMPORT_ELEMENT);
			importElem.setAttribute(WSDLGenerator.NAMESPACE_ATTR, WSDLGenerator.TARGETNAMESPACE);// get
																		// the
			// xsd
			// targetnamespace
			importElem.setAttribute(WSDLGenerator.SCHEMALOCATION_ATTR, this.xsdName);// get
																		// the
			// xsd name
			schEle.appendChild(importElem);
			schEle.removeChild(remEle);

			final Element rootEle = this.doc.getDocumentElement();
			// Change Input message
			final Element inputMsgEle = this.getElementByName(rootEle, WSDLGenerator.INPUTMESSAGE_NAME);
			final NodeList partNodes = inputMsgEle.getChildNodes();
			Element remPartEle = null;
			for (int i = 0; i < partNodes.getLength(); i++) {
				final Node child = partNodes.item(i);
				if (child instanceof Element) {
					remPartEle = (Element) child;
					break;
				}
			}
			final Element addPartEle = this.doc.createElementNS(this.def.getTargetNamespace(),
					WSDLGenerator.PART_ELEMENT);
			addPartEle.setAttribute(WSDLGenerator.NAME_ATTR, WSDLGenerator.PART_ELEMENT);// get the xsd
			// targetnamespace
			addPartEle.setAttribute(WSDLGenerator.ELEMENT_ATTR, this.xsdTopEleName);// get the top
			// element Name
			inputMsgEle.appendChild(addPartEle);
			inputMsgEle.removeChild(remPartEle);

			// Change Output message
			final Element outputMsgEle = this.getElementByName(rootEle, WSDLGenerator.OUTPUTMESSAGE_NAME);
			final NodeList outPartNodes = outputMsgEle.getChildNodes();
			Element remOutPartEle = null;
			for (int i = 0; i < outPartNodes.getLength(); i++) {
				final Node child = outPartNodes.item(i);
				if (child instanceof Element) {
					remOutPartEle = (Element) child;
					break;
				}
			}
			final Element addOutPartEle = this.doc.createElementNS(this.def
					.getTargetNamespace(), WSDLGenerator.PART_ELEMENT);
			addOutPartEle.setAttribute(WSDLGenerator.NAME_ATTR, WSDLGenerator.PART_ELEMENT);// get the xsd
			// targetnamespace
			addOutPartEle.setAttribute(WSDLGenerator.ELEMENT_ATTR, this.xsdTopEleName);// get the
																	// xsd
			// name
			outputMsgEle.appendChild(addOutPartEle);
			outputMsgEle.removeChild(remOutPartEle);

			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
		} catch (final Exception e) {
			throw new WSDLException(WSDLException.OTHER_ERROR,
					"Could not generate the WSDL");
		}
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

			this.mTableName = this.mTable.getSchema()+"."+this.mTable.getName();

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
	public void modifyInsert(final DBQueryModel dao, final String query) throws WSDLException, Exception {
		try {
			String primaryKey = null;
			final Element rootEle = this.doc.getDocumentElement();

			final Element insSqlEle = this.getElementBySQL(rootEle, WSDLGenerator.INSERT_QUERY);
			final Element insParentNode = (Element) insSqlEle.getParentNode();
			primaryKey = dao.getPrimaryKey();


			// Create sql element to replace
			final Element sqlEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JDBC_SQL_ELEMENT);
			sqlEle.setAttribute(WSDLGenerator.OPERATION_TYPE,"insert");
			sqlEle.setAttribute(WSDLGenerator.PARAM_ORDER, dao.getParamOrder(WSDLGenerator.INSERT_QUERY));
			sqlEle.setAttribute(WSDLGenerator.SQL_ELEMENT, query);
			sqlEle.setAttribute(WSDLGenerator.PRIMARYKEY_ATTR,primaryKey);
			sqlEle.setAttribute(WSDLGenerator.TRANSACTION,"NOTransaction");	
			sqlEle.setAttribute(WSDLGenerator.TABLE_NAME,this.mTableName);
	
			sqlEle.setAttribute(WSDLGenerator.NUMNBER_OF_RECORDS,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_VALUE,"");
			sqlEle.setAttribute(WSDLGenerator.MOVEROW_TABLE_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLLMILLI_SECONDS,"5000");

			insParentNode.appendChild(sqlEle);
			insParentNode.removeChild(insSqlEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
			final Element insParentNode = (Element) updateSqlEle.getParentNode();
			primaryKey = dao.getPrimaryKey();


			// Create sql element to replace
			final Element sqlEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JDBC_SQL_ELEMENT);
			sqlEle.setAttribute(WSDLGenerator.OPERATION_TYPE,"update");
			sqlEle.setAttribute(WSDLGenerator.PARAM_ORDER, dao.getParamOrder(WSDLGenerator.UPDATE_QUERY));
			sqlEle.setAttribute(WSDLGenerator.SQL_ELEMENT, query);
			sqlEle.setAttribute(WSDLGenerator.PRIMARYKEY_ATTR, primaryKey);
			sqlEle.setAttribute(WSDLGenerator.TRANSACTION,"NOTransaction");	
			sqlEle.setAttribute(WSDLGenerator.TABLE_NAME,this.mTableName);
			
			sqlEle.setAttribute(WSDLGenerator.NUMNBER_OF_RECORDS,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_VALUE,"");
			sqlEle.setAttribute(WSDLGenerator.MOVEROW_TABLE_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLLMILLI_SECONDS,"5000");

			insParentNode.appendChild(sqlEle);
			insParentNode.removeChild(updateSqlEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
			final Element insParentNode = (Element) delSqlEle.getParentNode();
			primaryKey = dao.getPrimaryKey();
	
			// Create sql element to replace
			final Element sqlEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JDBC_SQL_ELEMENT);
			sqlEle.setAttribute(WSDLGenerator.OPERATION_TYPE,"delete");
			sqlEle.setAttribute(WSDLGenerator.PARAM_ORDER, "");
			sqlEle.setAttribute(WSDLGenerator.SQL_ELEMENT, query);
			sqlEle.setAttribute(WSDLGenerator.PRIMARYKEY_ATTR, primaryKey);
			sqlEle.setAttribute(WSDLGenerator.TRANSACTION,"NOTransaction");
			sqlEle.setAttribute(WSDLGenerator.TABLE_NAME,this.mTableName);
			
			sqlEle.setAttribute(WSDLGenerator.NUMNBER_OF_RECORDS,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_VALUE,"");
			sqlEle.setAttribute(WSDLGenerator.MOVEROW_TABLE_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLLMILLI_SECONDS,"5000");

			insParentNode.appendChild(sqlEle);
			insParentNode.removeChild(delSqlEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
			final Element insParentNode = (Element) findSqlEle.getParentNode();
			primaryKey = dao.getPrimaryKey();

			// Create sql element to replace
			final Element sqlEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JDBC_SQL_ELEMENT);
			sqlEle.setAttribute(WSDLGenerator.OPERATION_TYPE,"find");
			sqlEle.setAttribute(WSDLGenerator.PARAM_ORDER, "");
			sqlEle.setAttribute(WSDLGenerator.SQL_ELEMENT, query);
			sqlEle.setAttribute(WSDLGenerator.PRIMARYKEY_ATTR,primaryKey);
			sqlEle.setAttribute(WSDLGenerator.TRANSACTION,"NOTransaction");
			sqlEle.setAttribute(WSDLGenerator.TABLE_NAME,this.mTableName);
			
			sqlEle.setAttribute(WSDLGenerator.NUMNBER_OF_RECORDS,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_VALUE,"");
			sqlEle.setAttribute(WSDLGenerator.MOVEROW_TABLE_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLLMILLI_SECONDS,"5000");

			insParentNode.appendChild(sqlEle);
			insParentNode.removeChild(findSqlEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
			final Element insParentNode = (Element) poolSqlEle.getParentNode();

			// Create sql element to replace
			primaryKey = dao.getPrimaryKey();
			final Element sqlEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JDBC_SQL_ELEMENT);
			sqlEle.setAttribute(WSDLGenerator.OPERATION_TYPE,"poll");
			sqlEle.setAttribute(WSDLGenerator.NUMNBER_OF_RECORDS,"");
			sqlEle.setAttribute(WSDLGenerator.PARAM_ORDER, "");
			sqlEle.setAttribute(WSDLGenerator.SQL_ELEMENT, query);
			sqlEle.setAttribute(WSDLGenerator.PRIMARYKEY_ATTR, primaryKey);
			sqlEle.setAttribute(WSDLGenerator.TRANSACTION,"NOTransaction");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_VALUE,"");
			sqlEle.setAttribute(WSDLGenerator.MARK_COLUMN_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLL_POST_PROCESS,"Delete");
			sqlEle.setAttribute(WSDLGenerator.TABLE_NAME,this.mTableName);
			sqlEle.setAttribute(WSDLGenerator.MOVEROW_TABLE_NAME,"");
			sqlEle.setAttribute(WSDLGenerator.POLLMILLI_SECONDS,"5000");

			//sqlEle.setAttribute(FLAGCOL_ATTR, "");
			insParentNode.appendChild(sqlEle);
			insParentNode.removeChild(poolSqlEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
            //final String projectName = getProjectName(this.wsdlFileLocation);
            final NodeList list = rootEle.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                final Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (n.getLocalName().equalsIgnoreCase(SERVICE_NAME)) {
                        Element serEle = (Element) n;
                        serEle.removeAttribute("name");
                        serEle.setAttribute("name", this.mWSDLFileName + SERVICE_NAME);
                        final NodeList childList = n.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            final Node childNode = childList.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (childNode.getLocalName().equalsIgnoreCase(PORT_NAME)) {
                                    Element portEle = (Element) childNode;
                                    portEle.removeAttribute("name");
                                    portEle.setAttribute("name", this.mWSDLFileName + PORT_NAME);
                                }
                            }
                        }
                    }
                }
            }
            this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
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
			final Element jndiRemEle = this.getElementByAddress(rootEle, WSDLGenerator.JNDI_NAME);
			final Element jndiParentNode = (Element) jndiRemEle.getParentNode();

			final Element jndiEle = this.doc.createElementNS(WSDLGenerator.JDBC_NAMESPACE,
					WSDLGenerator.JNDI_ADD_ELE);
			jndiEle.setAttribute("jndiName", this.mJNDIName);
			// commenting out the attribute creation which is required to create data source
			// at runtime
		  /*jndiEle.setAttribute("dbURL", this.dbinfo.getConnectionURL());
            jndiEle.setAttribute("driverClassName", this.dbinfo.getDriverClass());
            jndiEle.setAttribute("userName",  this.dbinfo.getUserName());
            jndiEle.setAttribute("password", this.dbinfo.getPassword());
           */
			jndiParentNode.appendChild(jndiEle);
			jndiParentNode.removeChild(jndiRemEle);
			// Change WSDL template with generated queries
			this.def = this.reader.readWSDL(this.wsdlFileLocation, rootEle);
		} catch (final Exception e) {
			throw new WSDLException(WSDLException.OTHER_ERROR,
					"Could not generate the WSDL");
		}
	}
    
    /**
     * get the project name from the wsdl file location
     * @param wsdlLocation
     * @return
     */
    private String getProjectName(String wsdlLocation) {
        String projName = "";
        if(wsdlLocation != null && !wsdlLocation.equals("")){
            projName  = wsdlLocation.substring (0 , wsdlLocation.lastIndexOf(File.separator));
            projName = projName.substring(projName.lastIndexOf(File.separator)+1 , projName.length());
        }
        return projName;
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
            final String outputFileName = this.wsdlFileLocation + "/" + this.mWSDLFileName + ".wsdl";
            final Writer sink = new FileWriter(outputFileName);
            writer.writeWSDL(this.def, sink);
            WSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName);
        } catch (final Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR, e.getMessage());
        }

    }
}
