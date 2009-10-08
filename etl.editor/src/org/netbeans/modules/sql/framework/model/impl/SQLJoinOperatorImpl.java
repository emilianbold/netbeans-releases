/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.List;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Defines joins on tables.
 *
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public class SQLJoinOperatorImpl extends SQLConnectableObjectImpl implements SQLJoinOperator {

    private static final String LOG_CATEGORY = SQLJoinOperatorImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLJoinOperatorImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /** GUI state information */
    private transient GUIInfo guiInfo = new GUIInfo();
    private SQLCondition jCondition;
    // ref to root join ie join whose one input is this join.
    private SQLJoinOperator rootJoin;

    /** Creates a new default instance of SQLJoin */
    public SQLJoinOperatorImpl() {
        super();

        type = SQLConstants.JOIN;
        setJoinType(SQLConstants.INNER_JOIN);

        SQLInputObjectImpl inputObject = new SQLInputObjectImpl(LEFT, LEFT, null);
        inputMap.put(LEFT, inputObject);

        inputObject = new SQLInputObjectImpl(RIGHT, RIGHT, null);
        inputMap.put(RIGHT, inputObject);

        this.jCondition = SQLModelObjectFactory.getInstance().createSQLCondition(JOIN_CONDITION);
        this.jCondition.setParent(this);
        // set condition to empty string, if it is not set then it is null
        // but when we get a SQLCondition from condition builder this is set to empty
        // string though user has not modified it so to make sure equal method works
        // properly we need to set it to empty string
        this.jCondition.setConditionText("");
    }

    /**
     * Creates a new instance of SQLJoinOperatorImpl using information from the given
     * SQLJoinOperator object.
     *
     * @param src SQLJoinOperator from which to obtain attributes, etc., for this new
     *        instance
     * @throws BaseException if error occurs during instantiation
     */
    public SQLJoinOperatorImpl(SQLJoinOperator src) throws BaseException {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null SQLJoinOperator instance for src param.");
        }

        try {
            copyFrom(src);
        } catch (Exception ex) {
            throw new BaseException("can not create SQLJoinOperatorImpl " + "using copy constructor", ex);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#addInput
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        if (argName == null || newInput == null) {
            throw new BaseException("Input arguments not specified");
        }

        int newType = newInput.getObjectType();
        String objType = TagParserUtility.getDisplayStringFor(newType);

        switch (newType) {
            case SQLConstants.JOIN_TABLE:
                if (!(LEFT.equals(argName) || RIGHT.equals(argName))) {
                    throw new BaseException(objType + " is valid only for LEFT and RIGHT input fields.");
                }
                break;

            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.TARGET_TABLE:
                if (!(LEFT.equals(argName) || RIGHT.equals(argName))) {
                    throw new BaseException(objType + " is valid only for LEFT and RIGHT input fields.");
                }
                break;

            case SQLConstants.JOIN:
                if (!(LEFT.equals(argName) || RIGHT.equals(argName))) {
                    throw new BaseException(objType + " is valid only for LEFT and RIGHT input fields.");
                }
                // join which is added to this join is no longer a root join
                // add the this object as parent to newInput join
                ((SQLJoinOperator) newInput).setRoot(this);
                break;
            default:
                // Redundant, as isInputValid should have caught any
                // unrecognized types...but left in as a backstop.
                throw new BaseException("Cannot link " + objType + " '" + newInput.getDisplayName() + "' as input to '" + argName + "' in " + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'");
        }

        SQLInputObject inputObject = (SQLInputObject) this.inputMap.get(argName);
        if (inputObject != null) {
            inputObject.setSQLObject(newInput);
        } else {
            throw new BaseException("Input with argName '" + argName + "' does not exist.");
        }
    }

    /**
     * Overrides default implementation.
     *
     * @return clone of this instance
     * @throws CloneNotSupportedException if error occurs during cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        SQLJoinOperatorImpl join = null;
        try {
            join = new SQLJoinOperatorImpl(this);
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT115: can not create clone of{0}", this.toString()), ex);
            throw new CloneNotSupportedException("can not create clone of " + this.toString());
        }

        return join;
    }

    /**
     * All SQL objects are cloneable.
     *
     * @return clone of this instance
     * @throws CloneNotSupportedException if error occurs during cloning.
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Overrides parent implementation to use special rules for determining equality of
     * two SQLJoinOperators.
     *
     * @param o Object to compare against this for equality
     * @return true if this equals o, false otherwise
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof SQLJoinOperator)) {
            return false;
        }

        SQLJoinOperatorImpl target = (SQLJoinOperatorImpl) o;

        // Must re-implement selected portions of equals(Object) methods in the
        // AbstractSQLObject hierarchy, rather than calling super.equals(),
        // as join is a special case.
        boolean response = (type == target.type);
        // check for display name
        response &= (this.getDisplayName() != null) ? this.getDisplayName().equals(target.getDisplayName()) : (target.getDisplayName() == null);

        response &= (attributes != null) ? attributes.equals(target.attributes) : (target.attributes == null);

        response &= (this.getJoinType() == target.getJoinType());
        response &= (this.isRoot() == target.isRoot());

        SQLInputObject leftIn = getInput(LEFT);
        SQLObject left = (leftIn != null) ? leftIn.getSQLObject() : null;
        SQLInputObject rightIn = getInput(RIGHT);
        SQLObject right = (rightIn != null) ? rightIn.getSQLObject() : null;

        SQLInputObject targetLeftIn = target.getInput(LEFT);
        SQLObject targetLeft = (targetLeftIn != null) ? targetLeftIn.getSQLObject() : null;
        SQLInputObject targetRightIn = target.getInput(RIGHT);
        SQLObject targetRight = (targetRightIn != null) ? targetRightIn.getSQLObject() : null;

        boolean leftEqualsTargetLeft = (left != null) ? left.equals(targetLeft) : (targetLeft == null);
        boolean rightEqualsTargetRight = (right != null) ? right.equals(targetRight) : (targetRight == null);
        boolean leftEqualsTargetRight = (left != null) ? left.equals(targetRight) : (targetRight == null);
        boolean rightEqualsTargetLeft = (right != null) ? right.equals(targetLeft) : (targetLeft == null);

        switch (getJoinType()) {
            case SQLConstants.INNER_JOIN:
                response &= (leftEqualsTargetLeft && rightEqualsTargetRight) || (rightEqualsTargetLeft && leftEqualsTargetRight);
                break;

            default:
                response &= leftEqualsTargetLeft && rightEqualsTargetRight;
                break;
        }

        response &= (target.getJoinCondition() != null) ? target.getJoinCondition().equals(this.getJoinCondition()) : (this.getJoinCondition() == null);
        return response;
    }

    /**
     * get a list of all tables which are used in this join or any of its input join. This
     * method recursively goes through LEFT and RIGHT inputs if they are join operator and
     * finds out all the SourceTables
     *
     * @return list of all participating SourceTables for this join
     */
    public List getAllSourceTables() {
        return getAllSourceTables(this);
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
     * get join condition
     *
     * @return join condition
     */
    public SQLCondition getJoinCondition() {
        return jCondition;
    }

    /**
     * get the type for join condition it will be one of following
     * SYSTEM_DEFINED_CONDITION USER_DEFINED_CONDITION NO_CONDITION
     *
     * @return join condition type
     */
    public int getJoinConditionType() {
        Integer jConditionType = (Integer) this.getAttributeObject(ATTR_JOINCONDITION_TYPE);
        if (jConditionType != null) {
            return jConditionType.intValue();
        }

        return NO_CONDITION;
    }

    /**
     * Get type of join (inner, left outer, right outer, full outer)
     *
     * @return type of join.
     * @see SQLConstants
     */
    public int getJoinType() {
        return ((Integer) getAttributeObject(ATTR_JOINTYPE)).intValue();
    }

    public String getJoinTypeString() {
        String joinType = "";
        switch (getJoinType()) {
            case SQLConstants.INNER_JOIN:
                joinType = "INNER JOIN";
                break;
            case SQLConstants.RIGHT_OUTER_JOIN:
                joinType = "RIGHT OUTER JOIN";
                break;
            case SQLConstants.LEFT_OUTER_JOIN:
                joinType = "LEFT OUTER JOIN";
                break;
            case SQLConstants.FULL_OUTER_JOIN:
                joinType = "FULL OUTER JOIN";
        }
        return joinType;
    }

    public void setJoinType(String joinType) {
        if (joinType.equals("INNER JOIN")) {
            setJoinType(SQLConstants.INNER_JOIN);
        } else if (joinType.equals("RIGHT OUTER JOIN")) {
            setJoinType(SQLConstants.RIGHT_OUTER_JOIN);
        } else if (joinType.equals("LEFT OUTER JOIN")) {
            setJoinType(SQLConstants.LEFT_OUTER_JOIN);
        } else if (joinType.equals("FULL OUTER JOIN")) {
            setJoinType(SQLConstants.FULL_OUTER_JOIN);
        }
    }

    /**
     * Overrides default implementation to compute hashcode based on values of
     * non-transient member variables.
     *
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hashCode = 0;

        hashCode = super.hashCode();

        if (this.getJoinCondition() != null) {
            hashCode += this.getJoinCondition().hashCode();
        }

        hashCode += this.getJoinConditionType();

        hashCode += ((this.isRoot()) ? 1 : 0);
        return hashCode;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#isInputValid
     */
    public int isInputCompatible(String argName, SQLObject input) {
        return super.isInputCompatible(argName, input);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#isInputValid
     */
    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null) {
            return false;
        }

        switch (input.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.TARGET_TABLE:
            case SQLConstants.JOIN:
                return (LEFT.equals(argName) || RIGHT.equals(argName));

            default:
                return false;
        }
    }

    /**
     * method isRoot returns true if the root is set.
     *
     * @return boolean true if root is set.
     */
    public boolean isRoot() {
        return rootJoin == null ? true : false;
    }

    /**
     * Parses the given Element
     *
     * @param xmlElement to be parsed
     * @exception BaseException while parsing
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);

        NodeList conditionNodeList = xmlElement.getElementsByTagName(SQLCondition.TAG_CONDITION);
        if (conditionNodeList != null && conditionNodeList.getLength() != 0) {
            Element elem = (Element) conditionNodeList.item(0);
            this.jCondition = SQLModelObjectFactory.getInstance().createSQLCondition(JOIN_CONDITION);
            this.jCondition.setParent(this);
            this.jCondition.parseXML(elem);
        }

        NodeList inputNodeList = xmlElement.getChildNodes();

        if (inputNodeList != null && inputNodeList.getLength() != 0) {
            for (int i = 0; i < inputNodeList.getLength(); i++) {
                Node node = inputNodeList.item(i);
                if (node.getNodeName().equals(SQLObject.TAG_INPUT)) {
                    TagParserUtility.parseInputTag(this, (Element) node);
                }
            }
        }

        NodeList guiInfoList = xmlElement.getElementsByTagName(GUIInfo.TAG_GUIINFO);
        if (guiInfoList != null && guiInfoList.getLength() != 0) {
            Element elem = (Element) guiInfoList.item(0);
            guiInfo = new GUIInfo(elem);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#removeInputByArgName
     */
    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException {
        SQLObject retObj = super.removeInputByArgName(argName, sqlObj);
        if (sqlObj != null && sqlObj.getObjectType() == SQLConstants.JOIN) {
            // join which is removed again becomes a root join
            // remove the this object as parent to newInput join
            ((SQLJoinOperator) sqlObj).setRoot(null);
        }
        return retObj;
    }

    /**
     * Second parse, being called, if not found in first pass
     *
     * @param element to be parsed
     * @exception BaseException thrown while secondparsing
     */
    public void secondPassParse(Element element) throws BaseException {
        TagParserUtility.parseInputTag(this, element);
    }

    /**
     * set the join condition
     *
     * @param condition join condition
     */
    public void setJoinCondition(SQLCondition condition) {
        this.jCondition = condition;
        if (this.jCondition != null) {
            this.jCondition.setDisplayName(JOIN_CONDITION);
            this.jCondition.setParent(this);
        }
    }

    /**
     * Sets join condition type to the given value.
     *
     * @param jConditionType new value representing join condition type.
     */
    public void setJoinConditionType(int jConditionType) {
        this.setAttribute(ATTR_JOINCONDITION_TYPE, new Integer(jConditionType));
    }

    /**
     * Sets the join type to the given value
     *
     * @param newType new join type
     */
    public void setJoinType(int newType) {
        setAttribute(ATTR_JOINTYPE, new Integer(newType));
    }

    /**
     * Sets root join operator to which this join operator is attached.
     *
     * @param rJoin root join operator; null if this operator is root
     */
    public void setRoot(SQLJoinOperator rJoin) {
        this.rootJoin = rJoin;
    }

    /**
     * Overrides parent implementation to append GUIInfo information.
     *
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     * @throws BaseException if error occurs during XML creation
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder buffer = new StringBuilder();

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));

        if (this.jCondition != null) {
            buffer.append(this.jCondition.toXMLString(prefix + "\t"));
        }

        buffer.append(TagParserUtility.toXMLInputTag(prefix + "\t", this.inputMap));
        buffer.append(this.guiInfo.toXMLString(prefix + "\t"));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    private void copyFrom(SQLJoinOperator src) throws BaseException {
        super.copyFromSource(src);
        try {
            // copy gui info
            GUIInfo gInfo = src.getGUIInfo();
            this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

            // copy join type
            // copy join condition
            SQLCondition srcCondition = src.getJoinCondition();
            SQLCondition joinCond = srcCondition != null ? (SQLCondition) srcCondition.cloneSQLObject() : null;
            // this will set the parent object as this join on condition
            this.setJoinCondition(joinCond);
        } catch (Exception ex) {
            throw new BaseException("exception occured while cloning SQLJoinOperator", ex);
        }
    }

    private ArrayList getAllSourceTables(SQLJoinOperator join) {
        ArrayList sTables = new ArrayList();

        SQLInputObject leftInObj = join.getInput(SQLJoinOperator.LEFT);
        SQLInputObject rightInObj = join.getInput(SQLJoinOperator.RIGHT);

        SQLObject leftObj = leftInObj.getSQLObject();
        SQLObject rightObj = rightInObj.getSQLObject();

        if (leftObj != null) {
            if (leftObj.getObjectType() == SQLConstants.JOIN) {
                sTables.addAll(getAllSourceTables((SQLJoinOperator) leftObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) leftObj;
                SourceTable sTable = jTable.getSourceTable();
                sTables.add(sTable);
            }
        }

        if (rightObj != null) {
            if (rightObj.getObjectType() == SQLConstants.JOIN) {
                sTables.addAll(getAllSourceTables((SQLJoinOperator) rightObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) rightObj;
                SourceTable sTable = jTable.getSourceTable();
                sTables.add(sTable);
            }
        }

        return sTables;
    }
}

