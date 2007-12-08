/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.utils.GeneratorUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class ColumnRefImpl extends AbstractSQLObject implements ColumnRef {

    /** Constant for column metadata name tag. */
    static final String ELEMENT_TAG = "dbColumnRef"; // NOI18N

    /** String to use in prefixing each line of a generated XML document */
    protected static final String INDENT = "\t";

    private SQLDBColumn columnRef;

    /** Contains UI state information */
    private GUIInfo guiInfo = new GUIInfo();

    /** Creates a new instance of ColumnRefImpl */
    public ColumnRefImpl() {
        guiInfo = new GUIInfo();
        init();
    }

    /**
     * New instance
     * 
     * @param src - source
     */
    public ColumnRefImpl(ColumnRef src) {
        this();
        if (src == null) {
            throw new IllegalArgumentException("can not create ColumnRefImpl using copy constructor, src is null");
        }

        copyFrom(src);
    }

    /**
     * New instance
     * 
     * @param aColumnRef SQLDBColumn to be referenced by this instance
     */
    public ColumnRefImpl(SQLDBColumn aColumnRef) {
        this();
        this.columnRef = aColumnRef;
    }

    /**
     * Clone
     * 
     * @return cloned object
     */
    public Object clone() {
        return new ColumnRefImpl(this);
    }

    /**
     * all SQL objects are cloneable
     * 
     * @exception CloneNotSupportedException - exception
     * @return cloned object
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Copies contents of given ConditionColumn instance to this object.
     * 
     * @param src ConditionColumn from which to copy contents
     */
    public void copyFrom(ColumnRef src) {
        super.copyFromSource(src);

        GUIInfo gInfo = src.getGUIInfo();

        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;
        SQLDBColumn sCol = (SQLDBColumn) src.getColumn();
        try {
            this.columnRef = sCol != null ? (SQLDBColumn) sCol.cloneSQLObject() : null;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }

        this.parentObject = src.getParentObject();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#equals(java.lang.Object)
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof ColumnRef)) {
            return false;
        }

        ColumnRef refMeta = (ColumnRef) refObj;
        boolean result = super.equals(refMeta);

        result &= (this.getColumn() != null) ? this.getColumn().equals(refMeta.getColumn()) : (refMeta.getColumn() == null);

        return result;
    }

    /**
     * Get column
     * 
     * @return SQLObject
     */
    public SQLObject getColumn() {
        return columnRef;
    }

    /**
     * Get display name
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (columnRef != null) {
            return columnRef.getDisplayName();
        }

        return "Undefined";
    }

    /**
     * Gets GUI-related attributes for this instance in the form of a GuiInfo instance.
     * 
     * @return associated GuiInfo instance
     * @see GUIInfo
     */
    public GUIInfo getGUIInfo() {
        return guiInfo;
    }

    /**
     * Gets JDBC type of output, if any.
     * 
     * @return JDBC type of output, or SQLConstants.JDBCSQL_TYPE_UNDEFINED if output is
     *         undefined for this instance
     */
    public int getJdbcType() {
        return columnRef.getJdbcType();
    }

    /**
     * Gets reference to SQLObject corresponding to given argument name that can be linked
     * to an SQLConnectableObject.
     * 
     * @param argName argument name of linkable SQLObject
     * @return linkable SQLObject corresponding to argName
     * @throws BaseException if object cannot be linked to an SQLConnectableObject
     */
    public SQLObject getOutput(String argName) throws BaseException {
        return this;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#hashCode
     */
    public int hashCode() {
        return super.hashCode() + ((this.getColumn() != null) ? this.getColumn().hashCode() : 0);
    }

    /**
     * Populates the member variables and collections of this SQLObject instance, parsing
     * the given DOM Element as the source for reconstituting its contents.
     * 
     * @param columnElement DOM element containing XML marshalled version of this
     *        SQLObject instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void parseXML(Element columnElement) throws BaseException {
        super.parseXML(columnElement);

        NodeList childNodeList = columnElement.getChildNodes();
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
        SQLCondition sqlCond = (SQLCondition) this.getParentObject();
        SQLDefinition definition = TagParserUtility.getAncestralSQLDefinition((SQLObject) sqlCond.getParentObject());

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // If obj is null it may not be parsed yet so do a second parse...
        // it registers this TargetColumn instance to be parsed a second time
        // to resolve the value reference
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        } else {
            setColumn(obj);
        }
    }

    /**
     * Set column
     * 
     * @param column - column
     */
    public void setColumn(SQLObject column) {
        this.columnRef = (SQLDBColumn) column;
    }

    /**
     * Set display name
     * 
     * @param dispName - display name
     */
    public void setDisplayName(String dispName) {
    }

    /**
     * toString
     * 
     * @return String
     */
    public String toString() {
        if (columnRef != null) {
            String cName = columnRef.getName();
            try {
                GeneratorUtil eval = GeneratorUtil.getInstance();
                eval.setTableAliasUsed(true);
                cName = eval.getEvaluatedString(this);
                eval.setTableAliasUsed(false);

                return cName;
            } catch (BaseException ignore) {
                // ignore - should we log this?
            }
        }
        return "Undefined";
    }

    /**
     * Gets XML representation of this SQLObject, appending the given String to the
     * beginning of each new line.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    public String toXMLString(String prefix) {
        StringBuilder buf = new StringBuilder(200);

        buf.append(prefix).append(getHeader());
        buf.append(toXMLAttributeTags(prefix));

        if (this.getColumn() != null) {
            try {
                String refXml = TagParserUtility.toXMLObjectRefTag(this.getColumn(), prefix + "\t");
                buf.append(refXml);
            } catch (BaseException e) {
                // @TODO log this exception
            }

            if (guiInfo != null) {
                buf.append(guiInfo.toXMLString(prefix + INDENT));
            }
        }

        buf.append(prefix).append(getFooter());
        return buf.toString();
    }

    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.COLUMN_REF;
    }
}

