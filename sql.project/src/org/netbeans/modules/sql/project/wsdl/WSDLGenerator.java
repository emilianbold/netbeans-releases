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

import org.netbeans.modules.sql.project.dbmodel.DBMetaData;
import org.netbeans.modules.sql.project.dbmodel.PrepStmt;
import org.netbeans.modules.sql.project.dbmodel.ResultSetColumn;
import org.netbeans.modules.sql.project.dbmodel.ResultSetColumns;
import org.netbeans.modules.sql.project.dbmodel.Parameter;
import org.netbeans.modules.sql.project.dbmodel.Procedure;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.queries.FileEncodingQuery;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import java.net.URL;
import java.net.URI;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.Types;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Port;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Operation;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOutput;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.openide.util.NbBundle;


public class WSDLGenerator {

    public static HashMap builtInTypes = new HashMap();
    private static Logger logger = Logger.getLogger(WSDLGenerator.class.getName());
    private static WSDLFactory factory;
    private static DocumentBuilderFactory docBuilderfactory;

    private Definition def;
    private DBMetaData dbmeta;
    private String sqlFileName;
    private List sqlFileList = null;
    private String schema;
    private String pcatalog;
    private String wsdlFileLocation;
    private String engineFileName;
    private Document doc;
    private String STATEMENT_TYPE = null;
    private String wsdlFileName = null;
    private boolean wsdlFileExsits = false;
    private Connection conn;
    private DatabaseConnection dbConn;
    private JFrame frame;
    private static final String SELECT_STATEMENT = "SELECT";
    private static final String INSERT_STATEMENT = "INSERT";
    private static final String UPDATE_STATEMENT = "UPDATE";
    private static final String DELETE_STATEMENT = "DELETE";
    private static final String DDL_STATEMENT_CREATE = "CREATE";
    private static final String DDL_STATEMENT_ALTER = "ALTER";
    private static final String DDL_STATEMENT_DROP = "DROP";
    private static final String TRUNCATE_STATEMENT = "TRUNCATE";
    private static final String PROC_STATEMENT = "EXEC";

    static {
        builtInTypes.put(
                "byte[]",
                "xsd:base64Binary");
        builtInTypes.put(
                "boolean",
                "xsd:boolean");
        builtInTypes.put(
                "byte",
                "xsd:byte");
        builtInTypes.put(
                "java.util.Calendar",
                "xsd:dateTime");
        builtInTypes.put(
                "java.math.BigDecimal",
                "xsd:decimal");
        builtInTypes.put(
                "double",
                "xsd:double");
        builtInTypes.put(
                "float",
                "xsd:float");
        builtInTypes.put(
                "byte[]",
                "xsd:hexBinary");
        builtInTypes.put(
                "int",
                "xsd:int");
        builtInTypes.put(
                "java.math.BigInteger",
                "xsd:integer");
        builtInTypes.put(
                "long",
                "xsd:long");
        builtInTypes.put(
                "javax.xml.namespace.QName",
                "xsd:QName");
        builtInTypes.put(
                "short",
                "xsd:short");
        builtInTypes.put(
                "java.lang.String",
                "xsd:string");
        builtInTypes.put(
                "java.sql.Time",
                "xsd:string");
        builtInTypes.put(
                "java.sql.Timestamp",
                "xsd:string");
        builtInTypes.put(
                "java.sql.Date",
                "xsd:string");

        // temporary for demo
        builtInTypes.put(
                "java.sql.Blob",
                "xsd:string");
        builtInTypes.put(
                "java.sql.Clob",
                "xsd:string");
        //added by abey for Procedure with parameter of type RefCursor
        builtInTypes.put(
                "java.sql.ResultSet",
                "xsd:ResultSet");


    }

    static {
        initFactory();
    }

    /**
     * Constructor
     * @param dbmeta
     * @param sqlFile
     * @param wsdlFileLocation
     */
    public WSDLGenerator(DBMetaData dbmeta, String sqlFile, String wsdlFileLocation, String engineFileName) {
        this.dbmeta = dbmeta;
        this.sqlFileName = sqlFile;
        this.wsdlFileLocation = wsdlFileLocation;
        this.engineFileName = engineFileName.substring(0, engineFileName.indexOf(".xml"));
    }

    public WSDLGenerator(Connection conn,
                         List sqlFiles,
                         String wsdlFileName,
                         String wsdlFileLocation,
                         String engineFileName) {
        this.conn = conn;
        this.sqlFileList = sqlFiles;
        this.wsdlFileLocation = wsdlFileLocation;
        this.wsdlFileName = wsdlFileName;
        this.engineFileName = engineFileName.substring(0, engineFileName.indexOf(".xml"));
    }

    /**
     * initialize the WSDLFactory
     */
    private static void initFactory() {
        if (factory == null) {
            try {
                factory = WSDLFactory.newInstance();
                docBuilderfactory = DocumentBuilderFactory.newInstance();
            } catch (WSDLException wsdle) {
                logger.log(Level.WARNING, wsdle.getMessage(), wsdle);
            }
        }
    }

    /**
     * reads an sqlpro wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition getWsdlTemplate() throws WSDLException, ParserConfigurationException, SAXException, IOException {
        Definition def = null;
        try {
            URL u = WSDLGenerator.class.getResource("sqlpro.wsdl.template");
            if (u != null) {
                String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
                def = readWSDLFile(wsdlURI);

            } else {
                logger.log(Level.WARNING, "Unable to locate the wsdl template");
            }
        } catch (WSDLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return def;
    }

    private Definition readWSDLFile(String wsdlURI) throws SAXException, IOException, ParserConfigurationException, WSDLException {
        WSDLReader reader = factory.newWSDLReader();
        Definition def;
        docBuilderfactory.setNamespaceAware(true);
        docBuilderfactory.setValidating(false);
        doc = docBuilderfactory.newDocumentBuilder().parse(wsdlURI);
        def = reader.readWSDL(wsdlURI, doc);
        return def;
    }

    public Definition generateWSDL() throws Exception {
        try {
            //wsdlFileName = wsdlFileLocation + "/" + sqlFileName + ".wsdl";
        	File f1 = new File(wsdlFileLocation + File.separator + wsdlFileName + ".wsdl");
        	File f2 = new File(wsdlFileLocation + File.separator + wsdlFileName + ".wsdl_old");
            if (f1.exists()) {
            	try{
            	      InputStream in = new FileInputStream(f1);
            	      OutputStream out = new FileOutputStream(f2);
            	      byte[] buf = new byte[1024];
            	      int len;
            	      while ((len = in.read(buf)) > 0){
            	        out.write(buf, 0, len);
            	      }
            	      in.close();
            	      out.close();
            	      System.out.println("File copied.");
            	    }
            	    catch(FileNotFoundException ex){
            	    	logger.log(Level.WARNING, ex.getMessage(), ex);
            	    }
            	    catch(IOException e){
            	    	logger.log(Level.WARNING, e.getMessage(), e);
            	    }
            }
            this.def = getWsdlTemplate();
            modifyWSDL();
            writeWsdl();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }        
        return def;
    }

    private Definition loadExistingWSDLFile(String wsdlFileName) {
        try {
            URI f = new File(wsdlFileLocation + File.separator + wsdlFileName + ".wsdl").toURI();
            Definition def = readWSDLFile(f.getPath());
            return def;
        } catch (Exception mfe) {
            logger.log(Level.SEVERE, "Unable to load existing wsdl file " + wsdlFileName +
                    " Reason: " + mfe.getLocalizedMessage());
        }
        return null;
    }

    /**
     * modify the wsdl template
     */
    private void modifyWSDL() throws Exception {
        try {
        modifyName();

        if (sqlFileList != null) {            
            for (int i = 0; i < sqlFileList.size(); i++) {
                File f = (File) sqlFileList.get(i);
                sqlFileName = f.getName().trim().substring(0, f.getName().indexOf(".sql"));
                modifyMessageTypes(f);
                createMessages();
                PortType pt = createPortType();
                Binding binding = createBinding(pt);
                modifyServices(binding);
            }
            modifyPartnerLink();
        }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }

    private void modifyName() {
        QName q = def.getQName();
        q = new QName(q.getNamespaceURI(), wsdlFileName);
        def.setQName(q);
    }

    private void modifyMessageTypes(File sqlFile) throws Exception {
        try {
            Element currRequest = null;
            Element currResponse = null;
            Element selectRecord = null;
            
            //Read sql file.
            BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
            String line = null;
            StringBuffer sqlText = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (sqlText.length() != 0) {
                    sqlText.append("\n");
                }
                sqlText.append(line);
            }
            dbmeta = new DBMetaData(conn, sqlText.toString());
            logger.log(Level.INFO, "SQL Statement is:[" + dbmeta.getSQLText() + "]");
            parseSQLStatement(sqlText.toString());
            Types types = def.getTypes();

            if (null != types) {
                List schemaList = types.getExtensibilityElements();
                for (Iterator iterator1 = schemaList.iterator(); iterator1.hasNext();) {
                    Object o = iterator1.next();
                    if (o instanceof Schema) {
                        Schema schemaType = (Schema) o;
                        Element e = schemaType.getElement();
                        currRequest = getElementByName(e, sqlFileName + "Request");
                        if (currRequest == null) {
                            currRequest = createElementWithComplexType(sqlFileName + "Request");
                        }
                        if (STATEMENT_TYPE != null) {
                            if (STATEMENT_TYPE.equalsIgnoreCase(SELECT_STATEMENT)) {
                                currResponse = getElementByName(e, sqlFileName + "Response");
                                if (currResponse == null) {
                                    currResponse = createElementWithComplexTypeSelect(sqlFileName + "Response");
                                    selectRecord = createElementWithComplexTypeRecord(sqlFileName + "_Record"); //113494
                                }
                                generateSelectSchemaElements(currRequest, selectRecord);
                            } else if (STATEMENT_TYPE.equalsIgnoreCase(INSERT_STATEMENT)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateInsertSchemaElements(currRequest, currResponse);
                            } else if (STATEMENT_TYPE.equalsIgnoreCase(UPDATE_STATEMENT)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateInsertSchemaElements(currRequest, currResponse);
                            } else if (STATEMENT_TYPE.equalsIgnoreCase(DELETE_STATEMENT)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateInsertSchemaElements(currRequest, currResponse);
                            }else if (STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_CREATE)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateCreateSchemaElements(currRequest, currResponse);
                            }else if (STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_ALTER)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateAlterSchemaElements(currRequest, currResponse);
                            }else if (STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_DROP)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateDropSchemaElements(currRequest, currResponse);
                            }else if (STATEMENT_TYPE.equalsIgnoreCase(TRUNCATE_STATEMENT)) {
                            	currResponse = getElementByName(e, sqlFileName + "Response");
                            	removeSchemaElements(currRequest, currResponse);
                                if (currResponse == null) {
                                	currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                            	currResponse = getElementByName(e, "numRowsEffected");
                                if (currResponse == null) {
                                    currResponse = createElement("numRowsEffected", "xsd:int");
                                }
                                generateTruncateSchemaElements(currRequest, currResponse);
                            } else if (STATEMENT_TYPE.equalsIgnoreCase(PROC_STATEMENT)) {
                                currResponse = getElementByName(e, sqlFileName + "Response");
                                if (currResponse == null) {
                                    currResponse = createElementWithComplexType(sqlFileName + "Response");
                                }
                                generateProcSchemaElements(currRequest, currResponse);
                            }
                            e.appendChild(currRequest);
                            e.appendChild(currResponse);
                            if(selectRecord != null || STATEMENT_TYPE.equalsIgnoreCase(SELECT_STATEMENT)){
                            	e.appendChild(selectRecord);
                            }
                        } else {
                            throw new Exception("Unsupported SQL Statement entered: " + sqlText);
                        }

                    } else if (o instanceof UnknownExtensibilityElement) {
                        //
                    }
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            JOptionPane.showMessageDialog(frame,
                    NbBundle.getMessage(WSDLGenerator.class,"LBL_MSG"),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            throw e;
        }

    }

    private void createMessages() {
        QName responseQName = null;
        QName partQName = null;
        Message m = def.createMessage();
        QName qname = new QName(def.getTargetNamespace(), sqlFileName + "Request");
        Part p = def.createPart();
        p.setName(sqlFileName + "RequestPart");
        p.setElementName(qname);
        m.addPart(p);
        m.setQName(qname);
        m.setUndefined(false);
        def.addMessage(m);
        Message mResponse = def.createMessage();
        Part p2 = def.createPart();
        
        if (STATEMENT_TYPE.equalsIgnoreCase(SELECT_STATEMENT) ||
        		STATEMENT_TYPE.equalsIgnoreCase(PROC_STATEMENT) ) {
            partQName = new QName(def.getTargetNamespace(), sqlFileName + "Response");
        } else if (STATEMENT_TYPE.equalsIgnoreCase(INSERT_STATEMENT) ||
                STATEMENT_TYPE.equalsIgnoreCase(UPDATE_STATEMENT) ||
                STATEMENT_TYPE.equalsIgnoreCase(DELETE_STATEMENT) ||
                STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_CREATE) ||
                STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_ALTER) ||
                STATEMENT_TYPE.equalsIgnoreCase(DDL_STATEMENT_DROP) ||
                STATEMENT_TYPE.equalsIgnoreCase(TRUNCATE_STATEMENT)) {
            partQName = new QName(def.getTargetNamespace(), "numRowsEffected");
        }
        p2.setName(sqlFileName + "ResponsePart");
        p2.setElementName(partQName);
        mResponse.addPart(p2);
        responseQName = new QName(def.getTargetNamespace(), sqlFileName + "Response");
        mResponse.setQName(responseQName);
        mResponse.setUndefined(false);
        def.addMessage(mResponse);

    }

    /**
     * this sets the portType name according to the given engine file
     */
    private PortType createPortType() {
        QName qn = new QName(def.getTargetNamespace(), wsdlFileName + "_sqlsePortType");
        PortType pt = def.getPortType(qn);

        if (pt == null) {
            pt = def.createPortType();
            pt.setQName(qn);
            pt.setUndefined(false);
            def.addPortType(pt);
        }
        createPortTypeOperations(pt);
        return pt;
    }

    private void createPortTypeOperations(PortType pt) {
        Operation op = pt.getOperation(sqlFileName, sqlFileName + "Request", sqlFileName + "Response");
        if (op == null) {
            op = def.createOperation();
            op.setName(sqlFileName);
            Input input = def.createInput();
            Output output = def.createOutput();
            input.setMessage(getMessageByName(sqlFileName + "Request"));
            input.setName(sqlFileName + "Request");
            output.setMessage(getMessageByName(sqlFileName + "Response"));
            output.setName(sqlFileName + "Response");
            op.setInput(input);
            op.setOutput(output);
            op.setUndefined(false);
            pt.addOperation(op);
        }
    }

    private Binding createBinding(PortType pt) {
        QName qname = new QName(def.getTargetNamespace(), wsdlFileName + "Binding");
        Binding binding = def.getBinding(qname);
        if (binding == null) {
            binding = def.createBinding();
            binding.setQName(qname);
            binding.setUndefined(false);
            binding.setPortType(pt);
            def.addBinding(binding);
        } else {
            PortType pt2 = binding.getPortType();
            if (pt2.equals(pt)) {
                BindingOperation bindingOp = binding.getBindingOperation(sqlFileName, sqlFileName + "Request", sqlFileName + "Response");
                if (bindingOp != null) {
                    return binding;
                }
            } else {
                binding.setPortType(pt2);
                pt = pt2;
            }
        }

        BindingOperation bo = def.createBindingOperation();
        bo.setName(sqlFileName);
        if (pt != null) {
            Operation op = pt.getOperation(sqlFileName, sqlFileName + "Request", sqlFileName + "Response");
            if (op != null) {
                bo.setOperation(op);
                BindingInput binput = def.createBindingInput();
                binput.setName(sqlFileName + "Request");
                bo.setBindingInput(binput);
                BindingOutput boutput = def.createBindingOutput();
                boutput.setName(sqlFileName + "Response");
                bo.setBindingOutput(boutput);
                binding.addBindingOperation(bo);
            }
        }
        return binding;
    }

    private Message getMessageByName(String messageName) {
        QName qname = new QName("http://com.sun.jbi/sqlse/sqlseengine", messageName);
        return def.getMessage(qname);
    }

    private void modifyMessageElementName(Message message, String partName, String newElementName) {
        Part part = message.getPart(partName);
        if (part != null) {
            QName qname = new QName("http://com.sun.jbi/sqlse/sqlseengine", newElementName);
            part.setElementName(qname);
        }
    }

    private void modifyServices(Binding binding) {
        Port p = null;
        QName qname = new QName(def.getTargetNamespace(), wsdlFileName + "_sqlseService");
        Service service = def.getService(qname);
        if (service == null) {
            service = def.createService();
            service.setQName(qname);
            p = def.createPort();
            p.setBinding(binding);
            p.setName(wsdlFileName + "_sqlsePort");
            service.addPort(p);
            def.addService(service);
        }
    }

    private void modifyPartnerLink() {
        if (!wsdlFileExsits) {
            List l = def.getExtensibilityElements();
            UnknownExtensibilityElement plinkType = (UnknownExtensibilityElement) l.get(0);		
            //set plinkType name
            plinkType.getElement();
            String plinkName = plinkType.getElement().getAttribute("name");
            plinkType.getElement().setAttribute("name", wsdlFileName + "_" + plinkName);		
        
            //set plink:role name and portType
            NodeList nl = plinkType.getElement().getChildNodes();
            Element plinkRole = (Element) nl.item(1);
            plinkRole.setAttribute("name", wsdlFileName + "_" + plinkRole.getAttribute("name"));

            String temp = plinkRole.getAttribute("portType").substring("tns:".length());
            plinkRole.setAttribute("portType", "tns:" + wsdlFileName + "_" + temp);
        }

    }


    private void generateDeleteSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
        	PrepStmt prep = dbmeta.getPrepStmtMetaData();
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
    
    private void generateCreateSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
        	PrepStmt prep = dbmeta.getPrepStmtMetaData();
        	if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
        	if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void generateAlterSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
        	PrepStmt prep = dbmeta.getPrepStmtMetaData();
        	if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }

    private void generateDropSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
        	PrepStmt prep = dbmeta.getPrepStmtMetaData();
        	if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
    
    private void generateTruncateSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
        	PrepStmt prep = dbmeta.getPrepStmtMetaData();
        	if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
    
    private void generateInsertSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
			PrepStmt prep = dbmeta.getPrepStmtMetaData();
			if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }

    /**
     * Adds a whereClause to the request element and resultset to the result element.
     * @param requestElement
     * @param responseElement
     * @throws Exception 
     */
    private void generateSelectSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
			PrepStmt prep = dbmeta.getPrepStmtMetaData();				
			if(dbmeta.getErrPrepStmtMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}

            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prep.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prep, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addResultSetColumnsToElement(prep, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
    
    /**
     * Adds Input parameters to the request element and resultset to the result element.
     * @param requestElement
     * @param responseElement
     * @throws Exception 
     */
    private void generateProcSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
            String catalog = conn.getCatalog();
            schema= dbConn.getSchema();
            String procName = getProcName();
            if (catalog == null && pcatalog == null) {
        		catalog = "";
        	} else {
        		catalog = pcatalog;
        	}
			Procedure proc = dbmeta.getProcedureMetaData(catalog, schema, procName,"Procedure");
			if(dbmeta.getErrProcMetaData()) {
				JOptionPane.showMessageDialog(frame,
                    "Problem in generating the message types for WSDL.Update the generated WSDL if needed.Please see the log for more details.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
			}
        	
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (proc.getNumParameters() > 0) {
                        addProcedureParametersToElement(proc, sequenceElement);
                    } else {
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                        }


                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                Element colElem2 = getElementByName(responseElement, "xsd:sequence");
                if (colElem2 != null) {
                    addProcedureResultSetColumnsToElement(proc, colElem2);
                    //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
    
    /**
     * Given a xml Element and a Prepstmnt object, adds the resultset columns
     * and their types as sub elements.
     * @param prep
     * @param sequenceElement
     */
    private void addResultSetColumnsToElement(PrepStmt prep, Element sequenceElement) 
    throws WSDLException{
        String colType = null;
        if (sequenceElement != null) {
            NodeList list = sequenceElement.getChildNodes();
            
			if (list != null) {
                for (int j = list.getLength() - 1; j >= 0; j--) {
                    sequenceElement.removeChild(list.item(j));
                }
            }
        }

		if (prep != null) {
            ResultSetColumn[] rs = prep.getResultSetColumns();
            if (rs != null && rs.length > 0) {
                for (int i = 0; i < rs.length; i++) {
                    try {
                        colType = rs[i].getJavaType();
                        Element elem = null;
                        if (isBuiltInType(colType)) {
                            elem = createElement(rs[i].getName(), (String) WSDLGenerator.builtInTypes.get(colType));
                        } else {
                            throw new WSDLException(WSDLException.INVALID_WSDL, "Invalid datatype encountered");
                        }
                        sequenceElement.appendChild(elem);
                    } catch (WSDLException e) {
                        logger.log(Level.SEVERE, e.getLocalizedMessage());
                        throw new WSDLException(WSDLException.INVALID_WSDL, "Check if the sql entered is valid");
                    }
                }
            }			
        }
    }

/**
     * Given a xml Element and a Procedure object, adds the resultset columns
     * and their types as sub elements.
     * @param proc
     * @param sequenceElement
     */
    private void addProcedureResultSetColumnsToElement(Procedure proc, Element sequenceElement) 
    throws WSDLException{
        String colType = null;
        if (sequenceElement != null) {
            NodeList list = sequenceElement.getChildNodes();
            if (list != null) {
                for (int j = list.getLength() - 1; j >= 0; j--) {
                    sequenceElement.removeChild(list.item(j));
                }
            }
        }
        if (proc != null) {
            ResultSetColumns[] rss = proc.getResultSetColumnsArray();
            for(int j=0;j<rss.length;j++) {
            int numColumns= rss[j].getNumColumns();
            for(int i = 0;i < numColumns; i++){
            ResultSetColumn rs  =rss[j].get(i);
            if (rs != null) {
                try {
                        colType = rs.getJavaType();
                        Element elem = null;
                        if (isBuiltInType(colType)) {
                            elem = createElement(rs.getName(), (String) WSDLGenerator.builtInTypes.get(colType));
                        } else {
                            throw new WSDLException(WSDLException.INVALID_WSDL, "Invalid datatype encountered");
                        }
                        sequenceElement.appendChild(elem);
                    } catch (WSDLException e) {
                        logger.log(Level.SEVERE, e.getLocalizedMessage());
                        throw new WSDLException(WSDLException.INVALID_WSDL, "Check if the sql entered is valid");
                    }
                    }
                }
            }
        
        }
    }

    /**
     * Adds prepared statement parameters to the element.
     * @param prep
     * @param sequenceElement
     */
    private void addPreparedStmtParametersToElement(PrepStmt prep, Element sequenceElement) {
        if (prep.getNumParameters() > 0) {
            if (sequenceElement != null) {
                NodeList list = sequenceElement.getChildNodes();
                if (list != null) {
                    for (int j = list.getLength() - 1; j >= 0; j--) {
                        sequenceElement.removeChild(list.item(j));
                    }
                }
                Parameter[] params = prep.getParameters();
                for (int i = 0; i < prep.getNumParameters(); i++) {
                    Element elem2 = createElement(params[i].getName(), (String) WSDLGenerator.builtInTypes.get(params[i].getJavaType()));
                    sequenceElement.appendChild(elem2);
                }

            }
        }
    }

    /**
     * Adds Procedure parameters to the element.
     * @param prep
     * @param sequenceElement
     */
    private void addProcedureParametersToElement(Procedure prep, Element sequenceElement) {
        if (prep.getNumParameters() > 0) {
            if (sequenceElement != null) {
                NodeList list = sequenceElement.getChildNodes();
                if (list != null) {
                    for (int j = list.getLength() - 1; j >= 0; j--) {
                        sequenceElement.removeChild(list.item(j));
                    }
                }
                Parameter[] params = prep.getParameters();
                for (int i = 0; i < prep.getNumParameters(); i++) {
                    Element elem2 = createElement(params[i].getName(), (String) WSDLGenerator.builtInTypes.get(params[i].getJavaType()));
                    sequenceElement.appendChild(elem2);
                }

            }
        }
    }

    
    
    
    /**
     * Helper method to return the Element with the name elementName from a 
     * top level element e. The method recursively looks thru sub elements and 
     * returns it once it is found. or a null.
     * @param e
     * @param elementName
     * @return
     */
    private Element getElementByName(Element e, String elementName) {
        if (e.getAttribute("name").equalsIgnoreCase(elementName)) {
            return e;
        }
        NodeList list = e.getChildNodes();
        Element el = null;
        Element e2 = null;
        for (int i = 0; i < list.getLength(); i++) {
            if (e2 == null) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    el = (Element) n;
                    if ((el.getAttribute("name").equalsIgnoreCase(elementName)) ||
                            el.getTagName().equalsIgnoreCase(elementName) ||
                            ((el.getLocalName() != null) && el.getLocalName().equalsIgnoreCase(elementName))) {
                        e2 = el;
                        break;
                    } else {
                        e2 = getElementByName(el, elementName);
                        if ((e2 != null) && (e2.getAttribute("name").equalsIgnoreCase(elementName))) {
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
     * Helper method which sets the sql statement type from the sql text.
     * @param sqlText
     */
    private void parseSQLStatement(String sqlText) {
        sqlText = sqlText.trim();
        String modSQLText = sqlText.toUpperCase();
        if (modSQLText.startsWith(SELECT_STATEMENT)) {
            STATEMENT_TYPE = SELECT_STATEMENT;
        } else if (modSQLText.startsWith(INSERT_STATEMENT)) {
            STATEMENT_TYPE = INSERT_STATEMENT;
        } else if (modSQLText.startsWith(UPDATE_STATEMENT)) {
            STATEMENT_TYPE = UPDATE_STATEMENT;
        } else if (modSQLText.startsWith(DELETE_STATEMENT)) {
            STATEMENT_TYPE = DELETE_STATEMENT;
        } else if (modSQLText.startsWith(TRUNCATE_STATEMENT)) {
            STATEMENT_TYPE = TRUNCATE_STATEMENT;
        }else if (modSQLText.startsWith(DDL_STATEMENT_CREATE)) {
            STATEMENT_TYPE = DDL_STATEMENT_CREATE;
        }else if (modSQLText.startsWith(DDL_STATEMENT_ALTER)) {
            STATEMENT_TYPE = DDL_STATEMENT_ALTER;
        } else if (modSQLText.startsWith(DDL_STATEMENT_DROP)) {
            STATEMENT_TYPE = DDL_STATEMENT_DROP;
        } else {
        	STATEMENT_TYPE = PROC_STATEMENT;
        }

    }

    /**
     * persist the wsdl file to disk
     *
     * @throws WSDLException
     */
    private void writeWsdl() throws WSDLException {
        try {
            final WSDLWriter writer = WSDLGenerator.factory.newWSDLWriter();
            final String outputFileName = this.wsdlFileLocation + File.separator + this.wsdlFileName + ".wsdl";
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
            final Writer sink = new java.io.OutputStreamWriter(fos, FileEncodingQuery.getDefaultEncoding());
            writer.writeWSDL(this.def, sink);
            WSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                " using the file encoding:"+ FileEncodingQuery.getDefaultEncoding());
        } catch (final Exception e) {
           if(e instanceof FileNotFoundException){
                WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof IOException){
                WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else if(e instanceof WSDLException){ 
            if((((WSDLException)e).getMessage()).indexOf("Unsupported Java encoding for writing wsdl file") != -1){
                try{ 
                   final WSDLWriter writer = WSDLGenerator.factory.newWSDLWriter();
                   final String outputFileName = this.wsdlFileLocation + File.separator + this.wsdlFileName + ".wsdl";
                   java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName);
                   final Writer sink = new java.io.OutputStreamWriter(fos,"UTF-8");
                   writer.writeWSDL(this.def, sink);
                   WSDLGenerator.logger.log(Level.INFO, "Successfully generated wsdl file :" + outputFileName +
                        " using the file encoding: UTF-8");
                   }catch(Exception ex){
                       WSDLGenerator.logger.log(Level.SEVERE, ex.getMessage());
                   }
                }else WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
            }else WSDLGenerator.logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void indentWSDLFile(Writer writer) {
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
            //transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N
            // indent the output to make it more legible... 
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // NOI18N
            } catch (Exception e) {
                ; // the JAXP implementation doesn't support indentation, no big deal
            }
            transformer.transform(source, result);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to indent wsdl file " + e.getLocalizedMessage());
        }

    }

    /**
     * Check if the column type is a xsd built in type.
     * @param type
     * @return
     */
    public static boolean isBuiltInType(String type) {
        return (builtInTypes.get(type) != null);
    }

    private Element createElementWithComplexTypeSelect(String name) {
        Element elem = doc.createElement("xsd:element");
        elem.setAttribute("name", name);
        Element complexType = doc.createElement("xsd:complexType");
        Element sequence = doc.createElement("xsd:sequence");
        Element record = doc.createElement("xsd:element");
        record.setAttribute("ref", sqlFileName + "_Record"); //113494
        record.setAttribute("maxOccurs", "unbounded");
        sequence.appendChild(record);
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
        return elem;
    }
    
    private Element createElementWithComplexTypeRecord(String name) {
        Element elem = doc.createElement("xsd:element");
        elem.setAttribute("name", name);
        Element complexType = doc.createElement("xsd:complexType");
        Element sequence = doc.createElement("xsd:sequence");
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
        return elem;
    }
    
    private Element createElementWithComplexType(String name) {
        Element elem = doc.createElement("xsd:element");
        elem.setAttribute("name", name);
        Element complexType = doc.createElement("xsd:complexType");
        Element sequence = doc.createElement("xsd:sequence");
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
        return elem;
    }

    private Element createElement(String name, String type) {
        Element elem = doc.createElementNS("http://www.w3.org/2001/XMLSchema", "xsd:element");
        elem.setAttribute("name", name);
        elem.setAttribute("type", type);
        return elem;
    }
    
    private void removeSchemaElements(Element requestElement, Element responseElement) {
        try {
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    
                        //remove elements under the current requestItem.             
                        NodeList list = sequenceElement.getChildNodes();
                        if (list != null) {
                            for (int j = list.getLength() - 1; j >= 0; j--) {
                                sequenceElement.removeChild(list.item(j));
                            }
                    }
                    //sequenceElement.removeChild(colElem1);
                }
            }
            if (responseElement != null) {
                if (responseElement != null) {
                    NodeList list = responseElement.getChildNodes();
                    if (list != null) {
                        for (int j = list.getLength() - 1; j >= 0; j--) {
                            responseElement.removeChild(list.item(j));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }
    
    
    public void setDBConnection(DatabaseConnection con) {
    	this.dbConn = con;
    	this.schema=schema;
    }
    
    private String getProcName() {
    	String proc_name = "";
    	String schema = "";
    	final StringTokenizer tok = new StringTokenizer(dbmeta.getSQLText(), " ");
		
        while (tok.hasMoreElements()) {
            String column = (String) tok.nextElement();
            int cnt = 0;
            column=column.toLowerCase();
            if(column.endsWith("call")){
            	cnt++;
            	proc_name=(String)tok.nextElement();
            	if(proc_name.contains(".")){
            		final StringTokenizer tok1 = new StringTokenizer(proc_name, ".");
            		pcatalog=tok1.nextToken();
            		proc_name=tok1.nextToken();
            	}
            	if(proc_name.contains("(")){
            		int i = proc_name.indexOf("(");
            		proc_name=proc_name.substring(0, i);
            	}
            	if(proc_name.contains("}")){
            		int i = proc_name.indexOf("}");
            		proc_name=proc_name.substring(0, i);
            	}
            }
            if(cnt>0)
            	break;
        }
        return proc_name;
    }
}
