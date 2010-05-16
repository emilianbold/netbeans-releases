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
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder.xsd;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl.DBColumnImpl;
import org.netbeans.modules.wsdlextensions.jdbc.builder.util.XMLCharUtil;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.logging.Logger;
import javax.wsdl.WSDLException;
import org.netbeans.modules.wsdlextensions.jdbc.builder.DBMetaData;
import org.netbeans.modules.wsdlextensions.jdbc.builder.Parameter;
import org.netbeans.modules.wsdlextensions.jdbc.builder.Procedure;
import org.netbeans.modules.wsdlextensions.jdbc.builder.PrepStmt;
import org.netbeans.modules.wsdlextensions.jdbc.builder.ResultSetColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.ResultSetColumns;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author This class generates an XML schema for every created OTD.
 */
public class XSDGenerator {

    public static HashMap builtInTypes = new HashMap();
    private static final Logger mLogger = Logger.getLogger("JDBC" + XSDGenerator.class.getName());
    private static final String INDENT = "  ";
    private static final String XSD_PREFIX = "xsd:";
    private static final String XMLNS = "xmlns";
    private static final String NS_XSD = "xmlns:xsd";
    private static final String TARGET_NS = "targetNamespace";
    private static final String ELEMENT_FORM_DEFAULT = "elementFormDefault";
    private static final String XSD_SEQUENCE = "sequence";
    private static final String XSD_REF = "ref";
    private static final String XSD_ELEMENT = "element";
    private static final String XSD_COMPLEX_TYPE = "complexType";
    private static final String XSD_SIMPLE_CONTENT = "simpleContent";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String IS_NULL = "isNull";
    private static final String DEFAULT = "isDefaultColumn";
    private static final String MIN_OCCURS_ATTR = "minOccurs";
    private static final String MAX_OCCURS_ATTR = "maxOccurs";
    private static final String TARGETNAMESPACE = "http://j2ee.netbeans.org/xsd/tableSchema";
    //LOB Support
    private static final String XSD_SIMPLE_TYPE = "simpleType";
    private static final String RESTRICTION = "restriction";
    private static final String VALUE_ATTR = "value";
    private static final String MAX_LENGTH = "maxLength";
    private static final String BASE_ATTR = "base";
    private static final String LOB_SIZE_LIMIT = "1073741824";
    
    private Document mDoc;
    private Document doc;

    // private Document mRepeatStructDoc;
    private Element mRoot;
    private Element mCurrentNode;
    private String mFileName = "";
    private String mRepeatStructFileName = "";

    // private String mOtdName = "";
    // private List mTables = null;
    // private List mDbObjects = null;
    private DBTable ltbl = null;
    private String STATEMENT_TYPE = null;
    private static final String SELECT_STATEMENT = "SELECT";
    private static final String INSERT_STATEMENT = "INSERT";
    private static final String UPDATE_STATEMENT = "UPDATE";
    private static final String DELETE_STATEMENT = "DELETE";
    private PrepStmt prepStmt;
    private Procedure proc = null;

    public XSDGenerator() throws Exception {// Need to revisit the object type
    }
    

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
                "xsd:base64Binary");
        builtInTypes.put(
                "java.sql.Clob",
                "xsd:string");
        //added by abey for Procedure with parameter of type RefCursor
        builtInTypes.put(
                "java.sql.ResultSet",
                "xsd:ResultSet");


    }
    public String generateFileName(String aFileName) throws Exception {
        String baseFileName = aFileName;
        String retFileName = aFileName;
        if (retFileName == null && retFileName.trim().equals("")) {
            throw new Exception("Found invalid XSD file name: " +retFileName+".");
        }
        
        File aFile = new File(retFileName);
        if (!aFile.getName().endsWith(".xsd") && !aFile.getName().endsWith(".XSD")) {
            throw new Exception("Found invalid XSD file name: " + aFile.getName() + ". Please make sure that the XSD file name has \".xsd\" suffix.");
        }
        
        int suffix = 1;
        while (aFile.exists()) {
            retFileName = baseFileName.substring(0, baseFileName.lastIndexOf(".xsd")) + suffix + ".xsd";
            aFile = new File(retFileName);
            suffix++;
        }
        
        final String newFileName = aFile.getName().substring(0, aFile.getName().lastIndexOf(".xsd")) + "_s.xsd";
        this.mRepeatStructFileName = aFile.getParent() + File.separator + newFileName;

        return aFile.getAbsolutePath();
    }
    
    public void generate(final String aPrjPath, final String aFileName, final DBTable tbl) throws Exception {
        this.mFileName = aFileName;
        // mOtdName = aOtdName;
        if (this.mFileName != null && !this.mFileName.trim().equals("")) {
            final File aFile = new File(this.mFileName);
            if (!aFile.getName().endsWith(".xsd") && !aFile.getName().endsWith(".XSD")) {
                throw new Exception("Found invalid XSD file name: " + aFile.getName() + ". Please make sure that the XSD file name has \".xsd\" suffix.");
            }
            final String newFileName = aFile.getName().substring(0, aFile.getName().lastIndexOf(".xsd")) + "_s.xsd";
            this.mRepeatStructFileName = aFile.getParent() + File.separator + newFileName;
        }
        this.ltbl = tbl;
        this.generateSimpleXsd(this.ltbl);
        this.resetElements();
    // generateRepeatXsd();

    }

    public void generateProcXSD(final String aPrjPath, final String aFileName, final Procedure proc) throws Exception {
        this.mFileName = aFileName;
        // mOtdName = aOtdName;
        if (this.mFileName != null && !this.mFileName.trim().equals("")) {
            final File aFile = new File(this.mFileName);
            if (!aFile.getName().endsWith(".xsd") && !aFile.getName().endsWith(".XSD")) {
                throw new Exception("Found invalid XSD file name: " + aFile.getName() + ". Please make sure that the XSD file name has \".xsd\" suffix.");
            }
            final String newFileName = aFile.getName().substring(0, aFile.getName().lastIndexOf(".xsd")) + "_s.xsd";
            this.mRepeatStructFileName = aFile.getParent() + File.separator + newFileName;
        }
        this.proc = proc;
        this.generateXSDHeaders();
        this.modifyProcMessageTypes(proc, aFileName);
        this.serialize(aPrjPath);
        this.resetElements();
    // generateRepeatXsd();

    }

    public void generateSimpleXsd(DBTable table) throws Exception {
        this.generateXSDHeaders();
        final Element aNode = this.addElementNode(XMLCharUtil.makeValidNCName(this.ltbl.getName()));
        this.mCurrentNode = aNode;
        this.createComplexTypesForTables(table);
        this.serialize(this.mFileName);
    }

    public void generateRepeatXsd() throws Exception {
        this.generateXSDHeaders();

        final String tableName = XMLCharUtil.makeValidNCName(this.ltbl.getName());
        final String complexTypeName = tableName + "List";
        final Element aNode = this.addElementNode(tableName + "_List", complexTypeName);
        this.createComplexTypeRepeatElement(complexTypeName, tableName);
        this.createComplexTypesForTables(this.ltbl);
        this.serialize(this.mRepeatStructFileName);
    }

    private void generateXSDHeaders() throws Exception {
        this.createDocuments();
        this.mRoot = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + "schema");
        this.mRoot.setAttribute(XSDGenerator.NS_XSD, "http://www.w3.org/2001/XMLSchema");
        this.mRoot.setAttribute(XSDGenerator.TARGET_NS, XSDGenerator.TARGETNAMESPACE);
        this.mRoot.setAttribute(XSDGenerator.ELEMENT_FORM_DEFAULT, "qualified");
        this.mRoot.setAttribute(XSDGenerator.XMLNS, XSDGenerator.TARGETNAMESPACE);
        this.mDoc.appendChild(this.mRoot);
        this.mCurrentNode = this.mRoot;
    }

    private Element addElementNode(final String name) {
        final Element aElement = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        aElement.setAttribute(XSDGenerator.NAME_ATTR, name);
        aElement.setAttribute(XSDGenerator.TYPE_ATTR, name);
        this.mCurrentNode.appendChild(aElement);

        return aElement;
    }

    private Element addElementNode(final String name, final String type) {
        final Element aElement = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        aElement.setAttribute(XSDGenerator.NAME_ATTR, name);
        aElement.setAttribute(XSDGenerator.TYPE_ATTR, type);
        this.mCurrentNode.appendChild(aElement);

        return aElement;
    }

    private void createComplexTypesForTables(final DBTable aTable) throws Exception {
        final List cols = aTable.getColumnList();
        Element aNode = null;
        Element bNode = null;
        final String tableName = XMLCharUtil.makeValidNCName(aTable.getName());

        aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_COMPLEX_TYPE);
        aNode.setAttribute(XSDGenerator.NAME_ATTR, tableName);
        this.mRoot.appendChild(aNode);
        this.mCurrentNode = aNode;

        bNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        bNode.setAttribute(XSDGenerator.XSD_REF, tableName + "_Record");
        bNode.setAttribute(XSDGenerator.MAX_OCCURS_ATTR, "unbounded");

        if (cols.size() > 0) {
            aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_SEQUENCE);
            //aNode.setAttribute(XSDGenerator.MAX_OCCURS_ATTR, "unbounded");
            aNode.appendChild(bNode);
            this.mCurrentNode.appendChild(aNode);
            this.mCurrentNode = aNode;
        }

        this.createColumnElements(cols, tableName);
        this.mCurrentNode = this.mRoot;
    }

    private void createColumnElements(final List cols, String tableName) throws Exception {
        Element aNode = null;
        boolean lobType = true;
        Element bNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        bNode.setAttribute(XSDGenerator.NAME_ATTR, tableName + "_Record");
        Element cNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_COMPLEX_TYPE);
        Element dNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_SEQUENCE);
        
        for (int ii = 0; ii < cols.size(); ii++) {
            final DBColumn iCol = (DBColumn) cols.get(ii);
            final DBColumnImpl colDesc = iCol instanceof DBColumnImpl ? (DBColumnImpl) iCol : new DBColumnImpl();
            colDesc.copyFrom(iCol);
            final String colName = colDesc.getJavaName();
            final int javaType = colDesc.getJdbcType();
            String colType = (String) TypeUtil.SQLTOJAVATYPES.get(TypeUtil.getSQLTypeDescription(javaType));

            if (!TypeUtil.isBuiltInType(colType)) {
                XSDGenerator.mLogger.severe("Encountered invalid data type of [" + colType + "]");
            // throw new Exception("Encountered invalid data type of [" + colType + "]");
            } else {
                colType = (String) TypeUtil.builtInTypes.get(colType);
            }
            if(lobType){
            if(TypeUtil.getSQLTypeDescription(javaType).equals("CLOB") || TypeUtil.getSQLTypeDescription(javaType).equals("BLOB")
				|| TypeUtil.getSQLTypeDescription(javaType).equals("LONGVARCHAR") || TypeUtil.getSQLTypeDescription(javaType).equals("LONGVARBINARY") ){
                colType = TypeUtil.getSQLTypeDescription(javaType);
                createSimpleTypeForLob(colType);
	                lobType=false;
            }
            }

            aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
            aNode.setAttribute(XSDGenerator.NAME_ATTR, colName);
            //aNode.setAttribute(XSDGenerator.TYPE_ATTR, colType);// defaulted for time being
            
            //aNode.setAttribute(MIN_OCCURS_ATTR, "0");
            //aNode.setAttribute(MAX_OCCURS_ATTR, "unbounded");
            aNode.appendChild(createIsNullAttribute(colType));
            dNode.appendChild(aNode);
        }
        cNode.appendChild(dNode);
        bNode.appendChild(cNode);
        this.mRoot.appendChild(bNode);
    }

    private void createSimpleTypeForLob(String colType) {
        Element simpleTypeNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX
                + XSDGenerator.XSD_SIMPLE_TYPE);
        simpleTypeNode.setAttribute(XSDGenerator.NAME_ATTR, colType);
        Element restrictionNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.RESTRICTION);
        restrictionNode.setAttribute(XSDGenerator.BASE_ATTR, (String) TypeUtil.builtInTypes
                .get((String) TypeUtil.SQLTOJAVATYPES.get(colType)));
        Element maxLengthNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.MAX_LENGTH);
        maxLengthNode.setAttribute(XSDGenerator.VALUE_ATTR, XSDGenerator.LOB_SIZE_LIMIT);
        restrictionNode.appendChild(maxLengthNode);
        simpleTypeNode.appendChild(restrictionNode);
        this.mRoot.appendChild(simpleTypeNode);
    }
        
    private void createComplexTypeRepeatElement(final String typeName, final String elemName) {
        Element aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_COMPLEX_TYPE);
        aNode.setAttribute(XSDGenerator.NAME_ATTR, typeName);
        this.mRoot.appendChild(aNode);
        this.mCurrentNode = aNode;
        aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_SEQUENCE);
        aNode.setAttribute(XSDGenerator.MAX_OCCURS_ATTR, "unbounded");
        this.mCurrentNode.appendChild(aNode);
        this.mCurrentNode = aNode;
        aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        aNode.setAttribute(XSDGenerator.NAME_ATTR, elemName);
        aNode.setAttribute(XSDGenerator.TYPE_ATTR, elemName);
        // aNode.setAttribute(MIN_OCCURS_ATTR, "0");
        // aNode.setAttribute(MAX_OCCURS_ATTR, "unbounded");
        this.mCurrentNode.appendChild(aNode);
    }

    private void resetElements() {
        this.mDoc = null;
        this.mRoot = null;
        this.mCurrentNode = null;
    }

    public void serialize(final String filename) throws Exception {
        try {
            final File lFile = new File(filename);
            final File lParent = lFile.getParentFile();
            if (!lParent.exists()) {
                lParent.mkdirs();
            }
            /**
             * Original design was to prompt the user for a directory name to create an XSD file
             * with a name that matches the OTD names and a file extension of .xsd Because of the
             * difficulties with the GUI design, we will settle for a user-entered file name for
             * now.
             */
            final PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename)));
            final DOMWriter dw = new DOMWriter(pw, false);
            dw.print(XSDGenerator.INDENT, this.mDoc, true);
        } catch (final Exception e) {
            XSDGenerator.mLogger.severe("Failed to serialize XSD document: " + e.getMessage());
            throw new Exception("Failed to serialize XSD document: " + e.getMessage());
        }
    }

    private void createDocuments() throws Exception {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            synchronized (dbf) {
                dbf.setNamespaceAware(true);
                this.mDoc = dbf.newDocumentBuilder().newDocument();
                if (this.mDoc == null) {
                    XSDGenerator.mLogger.severe("Failed to create Document object");
                    throw new Exception("Failed to create Document object.");
                }
            }
        } catch (final Exception e) {
            XSDGenerator.mLogger.severe("Failed to create XSD Document: " + e.getMessage());
            throw new Exception("Failed to create XSD Document: " + e.getMessage());
        }
    }

    public void generatePrepStmtXSD(final String aPrjPath, final String aFileName, final String sql, PrepStmt prepStmt) throws Exception {
        this.mFileName = aFileName;
        this.prepStmt = prepStmt;
		// mOtdName = aOtdName;
        if (this.mFileName != null && !this.mFileName.trim().equals("")) {
            final File aFile = new File(this.mFileName);
            if (!aFile.getName().endsWith(".xsd") && !aFile.getName().endsWith(".XSD")) {
                throw new Exception("Found invalid XSD file name: " + aFile.getName() + ". Please make sure that the XSD file name has \".xsd\" suffix.");
            }
            final String newFileName = aFile.getName().substring(0, aFile.getName().lastIndexOf(".xsd")) + "_s.xsd";
            this.mRepeatStructFileName = aFile.getParent() + File.separator + newFileName;
        }
        this.generateXSDHeaders();
        this.modifyMessageTypes(sql, aFileName);
        this.serialize(aPrjPath);

    }

    private void modifyMessageTypes(String sqlText, String sqlFileName) throws Exception {
        try {
            Element currRequest = null;
            Element currResponse = null;
            Element selectRecord = null;
            Element e = this.mCurrentNode;
            //Read sql file.
            parseSQLStatement(sqlText);
            currRequest = getElementByName(this.mRoot, sqlFileName.substring(0, sqlFileName.lastIndexOf(".xsd")) + "_Request");
            if (currRequest == null) {
                currRequest = createElementWithComplexType(sqlFileName.substring(0, sqlFileName.lastIndexOf(".xsd")) + "_Request");
            }
            if (STATEMENT_TYPE != null) {
                if (STATEMENT_TYPE.equalsIgnoreCase(SELECT_STATEMENT)) {
                    currResponse = createElementWithComplexTypeSelect(sqlFileName.substring(0, sqlFileName.lastIndexOf(".xsd")));
                    selectRecord = createElementWithComplexTypeRecord(sqlFileName.substring(0, sqlFileName.lastIndexOf(".xsd")) + "_Record");
                    generateSelectSchemaElements(currRequest, selectRecord);
                } else if (STATEMENT_TYPE.equalsIgnoreCase(INSERT_STATEMENT)) {
                    removeSchemaElements(currRequest, currResponse);
                    currResponse = createElement("numRowsEffected", "xsd:int");
                    generateInsertSchemaElements(currRequest, currResponse);
                } else if (STATEMENT_TYPE.equalsIgnoreCase(UPDATE_STATEMENT)) {
                    removeSchemaElements(currRequest, currResponse);
                    currResponse = createElement("numRowsEffected", "xsd:int");
                    generateInsertSchemaElements(currRequest, currResponse);
                } else if (STATEMENT_TYPE.equalsIgnoreCase(DELETE_STATEMENT)) {
                    removeSchemaElements(currRequest, currResponse);
                    currResponse = createElement("numRowsEffected", "xsd:int");
                    generateInsertSchemaElements(currRequest, currResponse);
                }
            }
            e.appendChild(currRequest);
            e.appendChild(currResponse);
            if (selectRecord != null || STATEMENT_TYPE.equalsIgnoreCase(SELECT_STATEMENT)) {
                e.appendChild(selectRecord);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void modifyProcMessageTypes(Procedure proc, String xsdFileName) throws Exception {
        try {
            Element currRequest = null;
            Element currResponse = null;
            Element e = this.mCurrentNode;
            currRequest = getElementByName(e, proc.getName() + "_Request");
            if (currRequest == null) {
                currRequest = createElementWithComplexType(proc.getName() + "_Request");
            }
            currResponse = getElementByName(e, proc.getName() + "_Response");
            if (currResponse == null) {
                currResponse = createElementWithComplexType(proc.getName() + "_Response");
            }
            generateProcSchemaElements(currRequest, currResponse);

            e.appendChild(currRequest);
            e.appendChild(currResponse);
        } catch (Exception e) {
            throw e;
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

    private Element createElementWithComplexTypeSelect(String name) {
        Element elem = mDoc.createElement("xsd:element");
        elem.setAttribute("name", name + "_Response");
        Element complexType = mDoc.createElement("xsd:complexType");
        Element sequence = mDoc.createElement("xsd:sequence");
        Element record = mDoc.createElement("xsd:element");
        record.setAttribute("ref", name + "_Record");
        record.setAttribute("maxOccurs", "unbounded");
        sequence.appendChild(record);
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
        return elem;
    }

    private Element createElementWithComplexTypeRecord(String name) {
        Element elem = mDoc.createElement("xsd:element");
        elem.setAttribute("name", name);
        Element complexType = mDoc.createElement("xsd:complexType");
        Element sequence = mDoc.createElement("xsd:sequence");
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
        return elem;
    }

    private Element createElementWithComplexType(String name) {
        Element elem = mDoc.createElement("xsd:element");
        elem.setAttribute("name", name);
        Element complexType = mDoc.createElement("xsd:complexType");
        Element sequence = mDoc.createElement("xsd:sequence");
        complexType.appendChild(sequence);
        elem.appendChild(complexType);
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
        }
    }

    private void generateInsertSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prepStmt.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prepStmt, sequenceElement);
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
                    addResultSetColumnsToElement(prepStmt, colElem2);
                //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private Element createElement(String name, String type) {
        Element elem = mDoc.createElementNS("http://www.w3.org/2001/XMLSchema", "xsd:element");
        elem.setAttribute("name", name);
        elem.setAttribute("type", type);
        return elem;
    }
    
    private Element createElement(String name) {
        Element elem = mDoc.createElementNS("http://www.w3.org/2001/XMLSchema", "xsd:element");
        elem.setAttribute("name", name);
        return elem;
    }

    /**
     * Adds a whereClause to the request element and resultset to the result element.
     * @param requestElement
     * @param responseElement
     * @throws Exception 
     */
    private void generateSelectSchemaElements(Element requestElement, Element responseElement) throws Exception {
        try {
            if (requestElement != null) {
                Element sequenceElement = getElementByName(requestElement, "xsd:sequence");
                if (sequenceElement != null) {
                    if (prepStmt.getNumParameters() > 0) {
                        addPreparedStmtParametersToElement(prepStmt, sequenceElement);
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
                    addResultSetColumnsToElement(prepStmt, colElem2);
                //colElem2.getParentNode().removeChild(colElem2);
                }
            }
        } catch (Exception e) {
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
                ResultSetColumns[] rsArray = proc.getResultSetColumnsArray();
                Element responseSequence = getElementByName(responseElement, "xsd:sequence");
                addProcOutParametersToResponse(proc, responseSequence);

                if ((rsArray != null) && rsArray.length > 0) {
                    
                    for (ResultSetColumns rs : rsArray) {
                        Element complexType = mDoc.createElement("xsd:complexType");
                        Element sequence = mDoc.createElement("xsd:sequence");
                        complexType.appendChild(sequence);
                        //responseElement.appendChild(complexType);
                        addProcedureResultSetColumnsToElement(rs, complexType);
                        this.mCurrentNode.appendChild(complexType);
                        Element elem = createElement(rs.getName() + "_Resultset", rs.getName());
                        elem.setAttribute("minOccurs", "0");
                        elem.setAttribute("maxOccurs", "unbounded");
                        responseSequence.appendChild(elem);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Given a xml Element and a Procedure object, adds the resultset columns
     * and their types as sub elements.
     * @param proc
     * @param sequenceElement
     */
    private void addProcedureResultSetColumnsToElement(ResultSetColumns rss, Element complexTypeElement)
            throws WSDLException {
        
        if (complexTypeElement != null) {
            complexTypeElement.setAttribute("name", rss.getName());
            Element sequenceElement = getElementByName(complexTypeElement, "xsd:sequence");
            String colType = null;
            if (sequenceElement != null) {
                NodeList list = sequenceElement.getChildNodes();
                if (list != null) {
                    for (int j = list.getLength() - 1; j >= 0; j--) {
                        sequenceElement.removeChild(list.item(j));
                    }
                }
            }
            if (rss != null) {
                int numColumns = rss.getNumColumns();
                for (int i = 0; i < numColumns; i++) {
                    ResultSetColumn rs = rss.get(i);
                    if (rs != null) {
                        try {
                            colType = rs.getJavaType();
                            Element elem = null;
                            if (isBuiltInType(colType)) {
                                elem = createElement(rs.getName());
                                elem.appendChild(createIsNullAttribute((String) builtInTypes.get(colType)));
                            } else {
                                throw new WSDLException(WSDLException.INVALID_WSDL, "Invalid datatype encountered");
                            }
                            sequenceElement.appendChild(elem);
                        } catch (WSDLException e) {
                            throw new WSDLException(WSDLException.INVALID_WSDL, "Check if the sql entered is valid");
                        }
                    }
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
                    if(params[i].getParamType().equalsIgnoreCase(Procedure.IN) ||
                        params[i].getParamType().equalsIgnoreCase(Procedure.INOUT)) {                        
                        Element elem2 = createElement(params[i].getName());
                        elem2.appendChild(createIsNullAttribute((String) builtInTypes.get(params[i].getJavaType())));
                        sequenceElement.appendChild(elem2);
                    }
                }

            }
        }
    }
    private void addProcOutParametersToResponse(Procedure prep, Element sequenceElement) {
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
                    if(params[i].getParamType().equalsIgnoreCase(Procedure.OUT) ||
                        params[i].getParamType().equalsIgnoreCase(Procedure.INOUT) ||
                        params[i].getParamType().equalsIgnoreCase(Procedure.RETURN) ) {
                            if(params[i].getJavaType().equalsIgnoreCase("java.sql.ResultSet")) {
                                //ignore resultsets.
                            } else {
                                Element elem2 = createElement(params[i].getName());                                
                                elem2.appendChild(createIsNullAttribute((String) builtInTypes.get(params[i].getJavaType())));
                                sequenceElement.appendChild(elem2);
                            }
                    }
                }

            }
        }
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
                    Element elem2 = createElement(params[i].getName());
                    elem2.appendChild(createIsNullAttribute((String) builtInTypes.get(params[i].getJavaType())));
                    sequenceElement.appendChild(elem2);
                }

            }
        }
    }

    /**
     * Given a xml Element and a Prepstmnt object, adds the resultset columns
     * and their types as sub elements.
     * @param prep
     * @param sequenceElement
     */
    private void addResultSetColumnsToElement(PrepStmt prep, Element sequenceElement)
            throws WSDLException {
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
            ResultSetColumn[] rs = prepStmt.getResultSetColumns();
            if (rs != null && rs.length > 0) {
                for (int i = 0; i < rs.length; i++) {
                    try {
                        colType = rs[i].getJavaType();
                        Element elem = null;
                        if (isBuiltInType(colType)) {
                            elem = createElement(rs[i].getName());
                            elem.appendChild(createIsNullAttribute((String) builtInTypes.get(colType)));                            
                        } else {
                            throw new WSDLException(WSDLException.INVALID_WSDL, "Invalid datatype encountered");
                        }
                        sequenceElement.appendChild(elem);
                    } catch (WSDLException e) {

                        throw new WSDLException("Exception:", "Check if the sql entered is valid");
                    }
                }
            }
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
    
    /**
     * creates elements to add isNull attribute to Elements.
     * @param 
     * @return Element
     */
    private Element createIsNullAttribute(String colType){
    	Element eNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_COMPLEX_TYPE);
        Element fNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_SIMPLE_CONTENT);
        Element gNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + "extension");
        Element hNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + "attribute");
        Element iNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + "attribute");
        gNode.setAttribute(XSDGenerator.BASE_ATTR, colType);
        hNode.setAttribute(XSDGenerator.NAME_ATTR, XSDGenerator.IS_NULL);
        hNode.setAttribute(XSDGenerator.TYPE_ATTR, "xsd:boolean");
        iNode.setAttribute(XSDGenerator.NAME_ATTR, XSDGenerator.DEFAULT);
        iNode.setAttribute(XSDGenerator.TYPE_ATTR, "xsd:boolean");
        gNode.appendChild(hNode);
        gNode.appendChild(iNode);
        fNode.appendChild(gNode);
        eNode.appendChild(fNode);
        return eNode;
    }
}
