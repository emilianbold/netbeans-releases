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

package org.netbeans.modules.jdbcwizard.builder.xsd;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBColumnImpl;
import org.netbeans.modules.jdbcwizard.builder.util.XMLCharUtil;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author This class generates an XML schema for every created OTD.
 */
public class XSDGenerator {
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

    private static final String NAME_ATTR = "name";

    private static final String TYPE_ATTR = "type";

    private static final String MAX_OCCURS_ATTR = "maxOccurs";

    private static final String TARGETNAMESPACE = "http://j2ee.netbeans.org/xsd/tableSchema";

    private Document mDoc;

    // private Document mRepeatStructDoc;
    private Element mRoot;

    private Element mCurrentNode;

    private String mFileName = "";

    private String mRepeatStructFileName = "";

    // private String mOtdName = "";
    // private List mTables = null;
    // private List mDbObjects = null;
    private DBTable ltbl = null;

    public XSDGenerator() throws Exception {// Need to revisit the object type
    }

    public void generate(final String aPrjPath, final String aFileName, final DBTable tbl) throws Exception {
        this.mFileName = aFileName;
        // mOtdName = aOtdName;
        if (this.mFileName != null && !this.mFileName.trim().equals("")) {
            final File aFile = new File(this.mFileName);
            if (!aFile.getName().endsWith(".xsd") && !aFile.getName().endsWith(".XSD")) {
                throw new Exception("Found invalid XSD file name: " + aFile.getName()
                        + ". Please make sure that the XSD file name has \".xsd\" suffix.");
            }
            final String newFileName = aFile.getName().substring(0, aFile.getName().lastIndexOf(".xsd")) + "_s.xsd";
            this.mRepeatStructFileName = aFile.getParent() + File.separator + newFileName;
        }
        this.ltbl = tbl;
        this.generateSimpleXsd();
        this.resetElements();
        // generateRepeatXsd();

    }

    public void generateSimpleXsd() throws Exception {
        this.generateXSDHeaders();
        final Element aNode = this.addElementNode( XMLCharUtil.makeValidNCName(this.ltbl.getName()));
        this.mCurrentNode = aNode;
        this.createComplexTypesForTables(this.ltbl);
        this.serialize(this.mFileName);
    }

    public void generateRepeatXsd() throws Exception {
        this.generateXSDHeaders();

        final String tableName = XMLCharUtil.makeValidNCName( this.ltbl.getName());
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
        final String tableName =  XMLCharUtil.makeValidNCName(aTable.getName());

        aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_COMPLEX_TYPE);
        aNode.setAttribute(XSDGenerator.NAME_ATTR, tableName);
        this.mRoot.appendChild(aNode);
        this.mCurrentNode = aNode;
        
        bNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        bNode.setAttribute(XSDGenerator.XSD_REF, "record");
        bNode.setAttribute(XSDGenerator.MAX_OCCURS_ATTR, "unbounded");
        
        if (cols.size() > 0) {
            aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_SEQUENCE);
            //aNode.setAttribute(XSDGenerator.MAX_OCCURS_ATTR, "unbounded");
            aNode.appendChild(bNode);
            this.mCurrentNode.appendChild(aNode);
            this.mCurrentNode = aNode;
        }

        this.createColumnElements(cols);
        this.mCurrentNode = this.mRoot;
    }

    private void createColumnElements(final List cols) throws Exception {
        Element aNode = null;
        Element bNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
        bNode.setAttribute(XSDGenerator.NAME_ATTR, "record");
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

            aNode = this.mDoc.createElement(XSDGenerator.XSD_PREFIX + XSDGenerator.XSD_ELEMENT);
            aNode.setAttribute(XSDGenerator.NAME_ATTR, colName);
            aNode.setAttribute(XSDGenerator.TYPE_ATTR, colType);// defaulted for time being
            // aNode.setAttribute(MIN_OCCURS_ATTR, "0");
            // aNode.setAttribute(MAX_OCCURS_ATTR, "unbounded");
            dNode.appendChild(aNode);
        }
        cNode.appendChild(dNode);
        bNode.appendChild(cNode);
        this.mRoot.appendChild(bNode);
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
            final File lFile = new File(this.mFileName);
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
}