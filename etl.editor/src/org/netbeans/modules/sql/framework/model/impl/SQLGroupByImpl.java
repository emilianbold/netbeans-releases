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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLGroupByValidationVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.StringUtil;

/**
 * @author Ritesh Adval
 */
public class SQLGroupByImpl extends AbstractSQLObject implements SQLGroupBy, Cloneable {

    /**
     * Attribute name used in XML representation of column sequence.
     */
    public static final String ATTR_SEQUENCE = "sequence";

    /**
     * Name used for root element of the XML representation for this class.
     */
    public static final String ELEMENT_TAG = "groupBy";

    /** having condition tag */
    public static final String HAVING_CONDITION = "havingCondition";

    private List colSequence = new ArrayList();
    private Map columns = new HashMap();
    private SQLCondition havingCondition;

    /**
     * Creates a new default instance of SQLGroupByImpl.
     */
    public SQLGroupByImpl() {
        super();

        havingCondition = SQLModelObjectFactory.getInstance().createSQLCondition(HAVING_CONDITION);
        setHavingCondition(havingCondition);
    }

    /**
     * Creates a new instance of SQLGroupByImpl
     * 
     * @param myColumnList List of columns associated with this group-by instance
     * @param parent parent object of this group-by instance
     */
    public SQLGroupByImpl(List myColumnList, Object parent) {
        this();

        setColumns(myColumnList);
        setParentObject(parent);
    }
    
    public SQLGroupByImpl(SQLGroupBy source) {
        this();
        copyFrom(source);
    }
    
    public void copyFrom(SQLGroupBy source) {
        super.copyFromSource((SQLObject)source);
        setColumns(source.getColumns());
        setParentObject(source.getParentObject());
        setHavingCondition(source.getHavingCondition());
    }

    /**
     * Indicates whether if given object is equal to this one.
     * 
     * @param obj Object against which to compare this
     * @return true if obj equals this; false otherwise.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SQLGroupBy)) {
            return false;
        } else if (obj == null) {
            return false;
        }

        SQLGroupBy src = (SQLGroupBy) obj;
        boolean response = super.equals(obj);

        if (!response) {
            return response;
        }

        response &= src.getColumns().equals(this.getColumns());

        return response;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLGroupBy#getColumns
     */
    public List getColumns() {
        List colList = new ArrayList(columns.values().size());

        Iterator iter = colSequence.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            colList.add(columns.get(key));
        }

        return colList;
    }

    public SQLCondition getHavingCondition() {
        return havingCondition;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLGroupBy#getParent
     */
    public Object getParentObject() {
        return parentObject;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hCode = super.hashCode();

        hCode += this.getColumns().hashCode();
        return hCode;
    }

    public boolean isValid() {
        TargetTable targetTable = (TargetTable) this.getParentObject();
        SQLGroupByValidationVisitor groupByVisitor = new SQLGroupByValidationVisitor(null, getColumns());
        groupByVisitor.visit(targetTable.getColumns().values());
        if (groupByVisitor.getValidationInfoList().size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parses the XML content, if any, represented by the given DOM element.
     * 
     * @param groupByElement DOM element to be parsed for orderBy content
     * @exception BaseException thrown while parsing XML, or if orderByElement is null
     */
    public void parseXML(Element groupByElement) throws BaseException {
        String seqList = groupByElement.getAttribute(ATTR_SEQUENCE);
        if (seqList == null || seqList.trim().length() == 0) {
            throw new BaseException("Invalid or missing sequence attribute.");
        }
        colSequence = StringUtil.createStringListFrom(seqList);

        NodeList childNodeList = groupByElement.getChildNodes();
        if (childNodeList != null && childNodeList.getLength() != 0) {
            for (int i = 0; i < childNodeList.getLength(); i++) {
                if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNodeList.item(i);
                    String tagName = childElement.getTagName();

                    if (TagParserUtility.TAG_OBJECTREF.equals(tagName)) {
                        secondPassParse(childElement);
                    } else if (SQLCondition.TAG_CONDITION.equals(tagName)) {
                        String conditionName = childElement.getAttribute(SQLCondition.DISPLAY_NAME);
                        if (conditionName != null && conditionName.equals(HAVING_CONDITION)) {
                            SQLCondition cond1 = SQLModelObjectFactory.getInstance().createSQLCondition(HAVING_CONDITION);
                            cond1.setParent(this);
                            cond1.parseXML(childElement);
                            this.setHavingCondition(cond1);
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLOrderBy#removeExpression
     */
    public void removeColumn(SQLObject obj) {
        if (obj != null) {
            String objId = obj.getId();
            if (!StringUtil.isNullString(objId)) {
                colSequence.remove(objId);
                columns.remove(objId);
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
        SQLDefinition definition = SQLObjectUtil.getAncestralSQLDefinition((SQLObject) parentObject);

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // If obj is null it may not be parsed yet so do a second parse...
        // it registers this TargetColumn instance to be parsed a second time
        // to resolve the value reference
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        } else {
            columns.put(obj.getId(), obj);
        }
    }

    /**
     * Sets collection of columns associated with this instance.
     * 
     * @param newColumnList list of columns involved in this group by
     */
    public void setColumns(List newColumnList) {
        columns.clear();
        colSequence.clear();

        Iterator iter = newColumnList.iterator();
        while (iter.hasNext()) {
            SQLObject repObj = (SQLObject) iter.next();
            colSequence.add(repObj.getId());
            columns.put(repObj.getId(), repObj);
        }
    }

    public void setHavingCondition(SQLCondition having) {
        this.havingCondition = having;
        if (this.havingCondition != null) {
            this.havingCondition.setParent(this);
            this.havingCondition.setDisplayName(HAVING_CONDITION);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLGroupBy#setParent
     */
    public void setParentObject(Object obj) {
        parentObject = obj;
    }

    public String toString() {
        StringBuilder strBuf = new StringBuilder(40);
        Iterator it = this.getColumns().iterator();
        while (it.hasNext()) {
            Object colObj = it.next();
            if (colObj instanceof SourceColumn) {
                SourceColumn column = (SourceColumn) colObj;
                strBuf.append(column.getName());
            } else if (colObj instanceof TargetColumn) {
                TargetColumn column = (TargetColumn) colObj;
                strBuf.append(column.getName());
                SQLObject obj = column.getValue();
                strBuf.append("->(");
                if (obj != null) {
                    strBuf.append(obj.toString());
                }
                strBuf.append(")");
            }

            if (it.hasNext()) {
                strBuf.append(",");
            }
        }
        return strBuf.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLGroupBy#toXMLString(String)
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder xml = new StringBuilder(500);
        final String indent = prefix + "\t";

        if (columns == null || columns.isEmpty()) {
            return "";
        }

        xml.append(prefix).append("<").append(ELEMENT_TAG).append(" ");
        xml.append(ATTR_SEQUENCE).append("=\"");

        removeDanglingExpressionIds();
        xml.append(StringUtil.createDelimitedStringFrom(colSequence));
        xml.append("\">\n");

        Iterator iter = columns.values().iterator();
        while (iter.hasNext()) {
            SQLObject expr = (SQLObject) iter.next();
            try {
                String refXml = TagParserUtility.toXMLObjectRefTag(expr, indent);
                xml.append(refXml);
            } catch (BaseException e) {
                // TODO log this exception
            }
        }

        if (havingCondition != null) {
            xml.append(havingCondition.toXMLString(prefix + indent));
        }

        xml.append(prefix).append("</").append(ELEMENT_TAG).append(">\n");
        return xml.toString();
    }

    private void removeDanglingExpressionIds() {
        ListIterator iter = colSequence.listIterator();
        while (iter.hasNext()) {
            String anId = (String) iter.next();
            if (!columns.containsKey(anId)) {
                iter.remove();
            }
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return new SQLGroupByImpl(this);
    }
}

