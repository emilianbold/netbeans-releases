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

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 */
public class SQLJoinTableImpl extends AbstractSQLObject implements SQLJoinTable {

    private static final String LOG_CATEGORY = SQLJoinTableImpl.class.getName();

    private GUIInfo guiInfo = new GUIInfo();
    private SourceTable table;

    /**
     * Creates a new default instance of SQLJoinTable.
     */
    public SQLJoinTableImpl() {
        this.type = SQLConstants.JOIN_TABLE;
    }

    /**
     * Creates a new instance of SQLJoinTable.
     * 
     * @param tbl underlying SourceTable to associate with this instance.
     */
    public SQLJoinTableImpl(SourceTable tbl) {
        this();
        this.table = tbl;
    }

    /**
     * Creates a new instance of SQLJoinTableImpl
     * 
     * @param src SQLJoinTable from which to copy attributes, etc.
     * @throws BaseException if error occurs while copying from src
     */
    public SQLJoinTableImpl(SQLJoinTable src) throws BaseException {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null SQLJoinTable instance for src param.");
        }

        try {
            copyFrom(src);
        } catch (Exception ex) {
            throw new BaseException("can not create SQLJoinTableImpl using copy constructor", ex);
        }
    }

    /**
     * Overrides default implementation.
     * 
     * @return cloned instance of this object
     * @throws CloneNotSupportedException if this cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        SQLJoinTable cond = null;
        try {
            cond = new SQLJoinTableImpl(this);
        } catch (Exception ex) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "clone", "Error while cloniing SQLJoinTableImpl", ex);

            throw new CloneNotSupportedException("can not create clone of " + this.toString());
        }
        return cond;
    }

    /**
     * All SQL objects are cloneable.
     * 
     * @return cloned instance of this object
     * @throws CloneNotSupportedException if this cannot be cloned.
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Gets display name of this object.
     * 
     * @return String representing display name.
     */
    public String getDisplayName() {
        return table.getDisplayName();
    }

    /**
     * Gets GUI-related attributes for this instance in the form of a GuiInfo instance.
     * 
     * @return associated GuiInfo instance
     * @see GUIInfo
     */
    public GUIInfo getGUIInfo() {
        return this.guiInfo;
    }

    /**
     * Gets name of this object.
     * 
     * @return String representing object name.
     */
    public String getName() {
        return table.getName();
    }

    /**
     * Gets source table associated with this instance.
     * 
     * @return SourceTable to associate with this instance.
     */
    public SourceTable getSourceTable() {
        return table;
    }

    /**
     * Populates the member variables and collections of this SQLObject instance, parsing
     * the given DOM Element as the source for reconstituting its contents.
     * 
     * @param columnElement DOM element containing XML marshalled version of this
     *        SQLObject instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);

        NodeList childNodeList = xmlElement.getChildNodes();
        if (childNodeList != null && childNodeList.getLength() != 0) {
            for (int i = 0; i < childNodeList.getLength(); i++) {
                if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNodeList.item(i);
                    String tagName = childElement.getTagName();

                    if (TagParserUtility.TAG_OBJECTREF.equals(tagName)) {
                        secondPassParse(childElement);
                    } else if (GUIInfo.TAG_GUIINFO.equals(tagName)) {
                        this.guiInfo = new GUIInfo(childElement);
                    }
                }
            }
        }
    }

    /**
     * Parses elements which require a second round of parsing to resolve their
     * references.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws BaseException {
        SQLJoinView jView = (SQLJoinView) this.getParentObject();
        SQLDefinition definition = TagParserUtility.getAncestralSQLDefinition(jView);

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // If obj is null it may not be parsed yet so do a second parse...
        // it registers this TargetColumn instance to be parsed a second time
        // to resolve the value reference
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        } else {
            setSourceTable((SourceTable) obj);
        }
    }

    /**
     * Sets source table for this object to the given instance.
     * 
     * @param sTable SourceTable to associate with this instance
     */
    public void setSourceTable(SourceTable sTable) {
        this.table = sTable;
    }

    /**
     * Generates XML document representing this object's content, using the given String
     * as a prefix for each line.
     * 
     * @param prefix String to be prepended to each line of the generated XML document
     * @return String containing XML representation
     * @exception BaseException - exception
     * @see SQLObject#toXMLString(java.lang.String)
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder buffer = new StringBuilder(500);
        if (prefix == null) {
            prefix = "";
        }

        final String childPrefix = prefix + "\t";

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));
        buffer.append(TagParserUtility.toXMLObjectRefTag(this.getSourceTable(), childPrefix));

        buffer.append(this.guiInfo.toXMLString(childPrefix));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    private void copyFrom(SQLJoinTable src) throws BaseException {
        super.copyFromSource(src);

        // copy gui info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

        // copy source table as it no cloning as it is referenced object
        this.table = src.getSourceTable();
    }
}

