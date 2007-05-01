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
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * This class is part of When. An addendum to SQLCase and always used with SQLCase
 * 
 * @author Sudhendra Seshachala
 * @version $Revision$
 */
public class SQLWhenImpl extends SQLConnectableObjectImpl implements SQLWhen {

    /** Key constant: condition input */
    public static final String CONDITION = "condition";

    /** Key constant: return output */
    public static final String RETURN = "return";

    private SQLPredicate oldPredicate;

    private SQLCondition whenCondition;

    /** Creates a new default instance of SQLWhenImpl. */
    public SQLWhenImpl() {
        super();

        type = SQLConstants.WHEN;
        whenCondition = SQLModelObjectFactory.getInstance().createSQLCondition(WHEN_CONDITION);
        whenCondition.setParent(this);
        whenCondition.setConditionText("");

        SQLInputObject inputObject = new SQLInputObjectImpl(RETURN, RETURN, null);
        this.inputMap.put(RETURN, inputObject);
    }

    /**
     * Creates a new instance of SQLWhen with the given display name
     * 
     * @param newDisplayName display name for the new instance
     */
    public SQLWhenImpl(String newDisplayName) {
        this();
        setDisplayName(newDisplayName);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#addInput
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        if (CONDITION.equals(argName)) {
            oldPredicate = (SQLPredicate) newInput;
        } else {
            super.addInput(argName, newInput);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLWhen#getCondition()
     */
    public SQLCondition getCondition() {
        return whenCondition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#getInput(java.lang.String)
     */
    public SQLInputObject getInput(String argName) {
        if (CONDITION.equals(argName)) {
            return new SQLInputObjectImpl(CONDITION, CONDITION, oldPredicate);
        } else {
            return super.getInput(argName);
        }
    }

    /**
     * Overrides default implementation to return JDBC type of the associated return
     * input, if any.
     * 
     * @return JDBC type of return input, or default value if no return input is currently
     *         linked.
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractSQLObject#getJdbcType
     */
    public int getJdbcType() {
        SQLObject value = this.getSQLObject(RETURN);

        // Return either the associated return object's type, or the default
        // type as defined in AbstractSQLObject.
        return (value != null) ? value.getJdbcType() : super.getJdbcType();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#isInputValid
     */
    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null || argName == null) {
            return false;
        }

        switch (input.getObjectType()) {
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:            	
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.LITERAL:
            case SQLConstants.VISIBLE_LITERAL:
            case SQLConstants.CASE:
            case SQLConstants.SOURCE_COLUMN:
                return RETURN.equals(argName.trim());

            default:
                return false;
        }
    }

    /**
     * Populates the member variables and collections of this SQLWhen instance, parsing
     * the given DOM Element as the source for reconstituting its contents.
     * 
     * @param xmlElement DOM element containing XML marshaled version of this SQLWhen
     *        instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);
        this.objectType = xmlElement.getAttribute(SQLObject.OBJECT_TYPE);

        NodeList conditionNodeList = xmlElement.getElementsByTagName(SQLCondition.TAG_CONDITION);
        if (conditionNodeList != null && conditionNodeList.getLength() != 0) {
            Element elem = (Element) conditionNodeList.item(0);
            whenCondition = SQLModelObjectFactory.getInstance().createSQLCondition(WHEN_CONDITION);
            whenCondition.setParent(this);
            whenCondition.parseXML(elem);
        }

        NodeList list = xmlElement.getChildNodes();
        if (list != null && list.getLength() != 0) {
            TagParserUtility.parseInputChildNodes(this, list);
        }
    }

    /**
     * Resolves object reference contained in given DOM element; called in second pass of
     * SQLDefinition parsing process.
     * 
     * @param element to be parsed
     * @exception BaseException thrown while parsing
     */
    public void secondPassParse(Element element) throws BaseException {
        TagParserUtility.parseInputTag(this, element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.model.SQLWhen#setCondition(org.netbeans.modules.sql.framework.model.SQLCondition)
     */
    public void setCondition(SQLCondition cond) {
        whenCondition = cond;
    }

    /**
     * Overrides parent implementation to append when condition information.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     * @throws BaseException if error occurs during XML creation
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuffer buffer = new StringBuffer();
        if (prefix == null) {
            prefix = "";
        }

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));

        if (whenCondition != null) {
            buffer.append(whenCondition.toXMLString(prefix + "\t"));
        }

        buffer.append(TagParserUtility.toXMLInputTag(prefix + "\t", inputMap));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }
}
