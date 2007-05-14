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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * Runtime DbModel is one instance per sql definition and it contains only one instance of
 * RuntimeInputImpl and only one instance of RuntimeOutputImpl
 * 
 * @author Ritesh Adval
 */
public class RuntimeDatabaseModelImpl extends SQLDBModelImpl implements RuntimeDatabaseModel {

    /** Creates a new instance of RuntimeDatabaseModelImpl */
    public RuntimeDatabaseModelImpl() {
        super();
        type = SQLConstants.RUNTIME_DBMODEL;
        this.setModelName("RuntimeDbModel");
        this.setDisplayName("Runtime DbModel");
    }

    /**
     * Get runtime input
     * 
     * @return RuntimeInput
     */
    public RuntimeInput getRuntimeInput() {
        List tbls = this.getTables();
        Iterator it = tbls.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                return (RuntimeInput) sqlObj;
            }
        }

        return null;
    }

    /**
     * Get runtime output
     * 
     * @return RuntimeOutput
     */
    public RuntimeOutput getRuntimeOutput() {
        List tbls = this.getTables();
        Iterator it = tbls.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj.getObjectType() == SQLConstants.RUNTIME_OUTPUT) {
                return (RuntimeOutput) sqlObj;
            }
        }

        return null;
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param dbElement DOM element containing XML marshalled version of a
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    public void parseXML(Element dbElement) throws BaseException {
        if (dbElement == null) {
            throw new BaseException("Must supply non-null org.w3c.dom.Element ref for element.No <" + RUNTIME_MODEL_TAG + "> element found.");
        }

        if (!RUNTIME_MODEL_TAG.equals(dbElement.getNodeName())) {
            throw new BaseException("Invalid root element; expected " + RUNTIME_MODEL_TAG + ", got " + dbElement.getNodeName());
        }

        super.parseCommonAttributesAndTags(dbElement);
        name = dbElement.getAttribute(NAME);
        String typeStr = dbElement.getAttribute(TYPE);

        NodeList childNodeList = null;

        childNodeList = dbElement.getChildNodes();

        if (STRTYPE_RUNTIME.equals(typeStr)) {
            type = SQLConstants.RUNTIME_DBMODEL;
            childNodeList = dbElement.getElementsByTagName(RuntimeInput.TAG_RUNTIME_INPUT);
            parseRuntimeInput(childNodeList);
            childNodeList = dbElement.getElementsByTagName(RuntimeOutput.TAG_RUNTIME_OUTPUT);
            parseRuntimeOutput(childNodeList);
        } else {
            throw new BaseException("Missing or invalid modelType attribute: " + typeStr);
        }
    }

    /**
     * Gets xml representation of this DatabaseModel instance.
     * 
     * @param prefix for this xml.
     * @return Return the xml representation of data source metadata.
     * @exception BaseException - exception
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder xml = new StringBuilder(INIT_XMLBUF_SIZE);
        if (prefix == null) {
            prefix = "";
        }

        xml.append(prefix).append('<').append(RUNTIME_MODEL_TAG).append(" ").append(NAME).append("=\"").append(name.trim()).append("\"");

        if (id != null && id.trim().length() != 0) {
            xml.append(" ").append(ID).append("=\"").append(id.trim()).append("\"");
        }

        if (displayName != null && displayName.trim().length() != 0) {
            xml.append(" ").append(DISPLAY_NAME).append("=\"").append(displayName.trim()).append('"');
        }

        if (type == SQLConstants.RUNTIME_DBMODEL) {
            xml.append(' ').append(TYPE).append("=\"").append(STRTYPE_RUNTIME).append('"');
        }

        xml.append(">\n");

        // write out tables
        writeTables(prefix, xml);

        xml.append(prefix).append("</").append(RUNTIME_MODEL_TAG).append(">\n");
        return xml.toString();
    }

    /**
     * Extracts SourceTable instances from the given NodeList.
     * 
     * @param tableNodeList Nodes to be unmarshalled
     * @throws BaseException if error occurs while parsing
     */
    protected void parseRuntimeInput(NodeList tableNodeList) throws BaseException {
        for (int i = 0; i < tableNodeList.getLength(); i++) {
            if (tableNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tableElement = (Element) tableNodeList.item(i);

                RuntimeInput runtime = SQLModelObjectFactory.getInstance().createRuntimeInput();
                runtime.setParentObject(this);

                runtime.parseXML(tableElement);
                addTable(runtime);
            }
        }
    }

    /**
     * Extracts TargetTable instances from the given NodeList.
     * 
     * @param tableNodeList Nodes to be unmarshalled
     * @throws BaseException if error occurs while parsing
     */
    protected void parseRuntimeOutput(NodeList tableNodeList) throws BaseException {
        for (int i = 0; i < tableNodeList.getLength(); i++) {
            if (tableNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tableElement = (Element) tableNodeList.item(i);

                RuntimeOutput runtime = SQLModelObjectFactory.getInstance().createRuntimeOutput();
                runtime.setParentObject(this);

                runtime.parseXML(tableElement);
                this.addTable(runtime);
            }
        }
    }
}

