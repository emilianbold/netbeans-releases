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

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * Concrete implementation of DBColumn describing column metadata for target columns.
 * 
 * @author Sudhendra Seshachala, Jonathan Giron
 * @version $Revision$
 */
public class TargetColumnImpl extends AbstractDBColumn implements TargetColumn {

    /** SQLObject supplying input value for this target column */
    private SQLObject inputValue;

    /**
     * Constructs a default instance of TargetColumnImpl.
     */
    public TargetColumnImpl() {
        super();
        init();
    }

    /**
     * Creates a new instance of TargetColumn, cloning the contents of the given DBColumn
     * implementation instance.
     * 
     * @param src DBColumn instance to be cloned
     */
    public TargetColumnImpl(DBColumn src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBColumn instance for src.");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of TargetColumn using the given parameters and assuming
     * that the column is not part of a foreign key or primary key, and that it accepts
     * null values.
     * 
     * @param colName name of this column
     * @param sqlJdbcType JDBC type of this column
     * @param colScale scale of this column
     * @param colPrecision precision of this column
     * @param isNullable true if nullable, false otherwise
     * @see java.sql.Types
     */
    public TargetColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isNullable);
        init();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn
     */
    public TargetColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
        init();
    }

    /**
     * Clone a copy of DBColumn.
     * 
     * @return a copy of DBColumn.
     */
    public Object clone() {
        try {
            TargetColumnImpl column = (TargetColumnImpl) super.clone();
            column.parentObject = parentObject;
            column.inputValue = inputValue;

            return column;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Sets the various member variables and collections using the given DBColumn instance
     * as a source object.
     * 
     * @param source DBColumn from which to obtain values for member variables and
     *        collections
     */
    public void copyFrom(DBColumn source) {
        super.copyFrom(source);

        if (source instanceof TargetColumn) {
            inputValue = ((TargetColumn) source).getValue();
        }
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof TargetColumn)) {
            return false;
        } else if (this == refObj) {
            return true;
        }

        TargetColumn refMeta = (TargetColumn) refObj;

        boolean result = super.equals(refObj);
        result &= (inputValue != null) ? inputValue.equals(refMeta.getValue()) : (refMeta.getValue() == null);

        return result;
    }

    /**
     * @see SQLObject#getOutput(java.lang.String)
     */
    public SQLObject getOutput(String argName) throws BaseException {
        throw new BaseException("TargetColumnImpl cannot be an output SQLObject.");
    }

    /**
     * Gets associated input SQLObject, if any, for this column.
     * 
     * @return input SQLObject, or null if none was set
     */
    public SQLObject getValue() {
        return inputValue;
    }

    /**
     * Returns the hashCode for this object.
     * 
     * @return the hashCode of this object.
     */
    public int hashCode() {
        return super.hashCode() + ((inputValue != null) ? inputValue.hashCode() : 0);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#parseXML(org.w3c.dom.Element)
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
                    }
                }
            }
        }
    }

    /**
     * Parses elements which require a second pass to resolve their values.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws BaseException {
        TargetTable parentTable = (TargetTable) parentObject;
        SQLDBModel parentDbModel = (SQLDBModel) parentTable.getParentObject();
        SQLDefinition definition = (SQLDefinition) parentDbModel.getParentObject();

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // If obj is null it may not be parsed yet so do a second parse...
        // it registers this TargetColumn instance to be parsed a second time
        // to resolve the value reference
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        } else {
            setValue(obj);
        }
    }

    /**
     * Sets associated SQLObject as input to this column.
     * 
     * @param newInput new input SQLObject; may be null
     */
    public void setValue(SQLObject newInput) {
        inputValue = newInput;
    }

    /**
     * Overrides default implementation to return evaluated column name.
     * 
     * @return evaluated column name.
     */
    public String toString() {
        return super.toString();
    }

    /**
     * @see SQLObject#toXMLString
     */
    public String toXMLString(String prefix) {
        StringBuilder xml = new StringBuilder(50);
        xml.append(prefix).append("<").append(ELEMENT_TAG);

        // Allow superclass to write its attributes out first.
        appendXMLAttributes(xml);

        xml.append(" >\n");

        // write out attributes
        xml.append(super.toXMLAttributeTags(prefix));

        if (inputValue != null) {
            try {
                String refXml = TagParserUtility.toXMLObjectRefTag(inputValue, prefix + "\t");
                xml.append(refXml);
            } catch (BaseException e) {
                // TODO log this exception
            }
        }

        xml.append(prefix).append("</").append(ELEMENT_TAG).append(">\n");
        return xml.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#getElementTagName
     */
    protected String getElementTagName() {
        return ELEMENT_TAG;
    }

    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.TARGET_COLUMN;
    }

}

