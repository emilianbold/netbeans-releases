/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.codegen.SQLOperatorFactory;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLLiteral;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.model.SQLOperatorArg;
import org.netbeans.modules.edm.model.SQLOperatorDefinition;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.editor.utils.GeneratorUtil;
import org.netbeans.modules.edm.editor.utils.OperatorUtil;
import org.netbeans.modules.edm.model.visitors.SQLVisitor;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorField;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.openide.util.NbBundle;

/**
 * Represents boolean conditional expressions for join, case, etc.,
 * 
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public class SQLPredicateImpl extends SQLConnectableObjectImpl implements SQLPredicate {

    /** Reference to left-most predicate, if any (for composite predicates) */
    protected SQLPredicate leftMost = null;

    protected SQLOperatorDefinition operatorDefinition;

    protected IOperatorXmlInfo operatorXmlInfo;

    /** Parent of this predicate (can be null or another predicate) */
    protected SQLPredicate root = null;

    /** Creates a new instance of SQLPredicate */
    public SQLPredicateImpl() {
        super();
        type = SQLConstants.PREDICATE;
    }

    public SQLPredicateImpl(SQLPredicate src) throws EDMException {
        this();
        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_can_not_create_SQLPredicate"));
        }

        copyFrom(src);
    }

    public void addInput(String argName, SQLObject newInput) throws EDMException {
        if (argName == null || newInput == null) {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Input_arguments_not_specified"));
        }

        int newType = newInput.getObjectType();
        String objType = TagParserUtility.getDisplayStringFor(newType);

        if (isInputCompatible(argName, newInput) == SQLConstants.TYPE_CHECK_INCOMPATIBLE) {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Input_type") + objType + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_is_incompatible") + argName + "'.");
        }

        if (isInputValid(argName, newInput)) {
            switch (newType) {
                case SQLConstants.PREDICATE:
                case SQLConstants.VISIBLE_PREDICATE:
                    ((SQLPredicate) newInput).setRoot(this);
                // Fall through to next group.

                case SQLConstants.GENERIC_OPERATOR:
                case SQLConstants.CUSTOM_OPERATOR:
                case SQLConstants.LITERAL:
                case SQLConstants.VISIBLE_LITERAL:
                case SQLConstants.CASE:
                case SQLConstants.SOURCE_COLUMN:
                case SQLConstants.CAST_OPERATOR:
                case SQLConstants.DATE_DIFF_OPERATOR:
                case SQLConstants.DATE_ADD_OPERATOR:
                case SQLConstants.COLUMN_REF:
                    SQLInputObject inputObject = (SQLInputObject) this.inputMap.get(argName);
                    if (inputObject != null) {
                        inputObject.setSQLObject(newInput);
                    } else {
                        throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Input_with_argName") + argName + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_does_not_exist"));
                    }

                    break;

                default:
                    throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Cannot_link") + objType + " '" + newInput.getDisplayName() + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_as_input") + argName + "' in "
                        + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'");
            }
        } else {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Cannot_link") + objType + " '" + newInput.getDisplayName() + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_as_input") + argName + "' in "
                + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            SQLPredicateImpl predicate = new SQLPredicateImpl(this);
            return predicate;
        } catch (EDMException ex) {
            throw new CloneNotSupportedException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_can_not_create_clone") + this.getOperatorType());
        }
    }

    public void copyFrom(SQLPredicate src) throws EDMException {
        // Must establish input objects via OperatorXmlInfo before we can call
        // super.copyFromSource() to populate them with cloned SQLObject inputs.
        this.operatorDefinition = src.getOperatorDefinition();
        this.setOperatorXmlInfo(src.getOperatorXmlInfo());

        super.copyFromSource(src);

        // we do not clone root as it must be set outside to build this tree
        if (src.getRoot() != null) {
            this.root = src.getRoot();
        }
    }

    /**
     * @see java.lang.Object#equals
     */
    public boolean equals(Object refObj) {
        // TODO: Need to refactor class and Interface hierarchy, such that we should be
        // using Interface more then actual implemetation classes in referring classes.
        if (!(refObj instanceof SQLPredicateImpl)) {
            return false;
        }

        SQLPredicateImpl predicate = (SQLPredicateImpl) refObj;

        // check if predicate has same operator
        String myOp = getOperatorType();
        String refOp = predicate.getOperatorType();
        boolean response = (myOp != null) ? (myOp.equals(refOp)) : (refOp == null);

        SQLPredicate thisPred = this.getLeftMostPredicate();
        SQLPredicate refPred = predicate.getLeftMostPredicate();

        if ((thisPred != this) && (refPred != predicate)) {
            response &= (thisPred != null) ? (thisPred.equals(refPred)) : (refPred == null);
        }

        return response && super.equals(refObj);
    }

    public Object getArgumentValue(String argName) throws EDMException {
        return this.getSQLObject(argName);
    }

    public String getCustomOperatorName() {
        return this.displayName;
    }

    public String getDisplayName() {
        String dName = super.getDisplayName();
        if (dName == null) {
            return this.getOperatorType();
        }

        return dName;
    }

    public SQLOperatorDefinition getOperatorDefinition() {
        return operatorDefinition;
    }

    public String getOperatorType() {
        if (operatorDefinition != null) {
            return operatorDefinition.getOperatorName();
        }

        Attribute attr = getAttribute(SQLOperator.ATTR_SCRIPTREF);
        return (attr != null) ? attr.getAttributeValue().toString() : null;
    }

    public IOperatorXmlInfo getOperatorXmlInfo() {
        return operatorXmlInfo;
    }

    /**
     * method getRoot gets the root SQLPredicate.
     * 
     * @return SQLPredicate of the root.
     */
    public SQLPredicate getRoot() {
        return (this.root);
    }

    /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = super.hashCode();
        myHash += (getOperatorType() != null) ? getOperatorType().hashCode() : 0;

        // Avoid infinite recursion if this predicate (erroneously) references
        // itself.
        Object left = this.getLeftMostPredicate();
        if (left != null && this != left) {
            myHash += left.hashCode();
        }
        return myHash;
    }

    public boolean isCustomOperator() {
        return false;
    }


    public int isInputCompatible(String argName, SQLObject input) {
        String operator = this.getOperatorType();
        // if operator is not AND or OR and we are trying to add another
        // predicate which is not AND or OR (which may be one of < , > etc)
        // then it is not compatible
        // ex - to disallow link a < to a < predicate
        if (!isGroupPredicate(operator) && (input instanceof SQLPredicate)) {
            // if input is a predicate then disallow
            return SQLConstants.TYPE_CHECK_INCOMPATIBLE;

        } else if (isGroupPredicate(operator) && !(input instanceof SQLPredicate)) {
            // if this is a group predicate then we should not allow non comparison
            // inputs
            return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
        }

        return SQLConstants.TYPE_CHECK_COMPATIBLE;
    }

    public boolean isInputStatic(String argName) {
        IOperatorXmlInfo xmlInfo = getOperatorXmlInfo();
        if (xmlInfo != null && argName != null) {
            IOperatorField field = xmlInfo.getInputField(argName);
            if (field != null) {
                return field.isStatic();
            }
        }
        return false;
    }


    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null) {
            return false;
        }

        switch (input.getObjectType()) {
            case SQLConstants.PREDICATE:
            case SQLConstants.VISIBLE_PREDICATE:
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:            	
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.VISIBLE_LITERAL:
            case SQLConstants.CASE:
            case SQLConstants.SOURCE_COLUMN:
            case SQLConstants.LITERAL:
            case SQLConstants.COLUMN_REF:
                return true;

            default:
                return false;
        }
    }

    /**
     * check if open and close parenthesis should be used
     * 
     * @return bool
     */
    public boolean isShowParenthesis() {
        Boolean paran = (Boolean) getAttributeObject(ATTR_PARENTHESIS);

        if (paran != null) {
            return paran.booleanValue();
        }

        return true;
    }

    /**
     * Parses the given XML element to populate content of this instance.
     * 
     * @param xmlElement Element to be parsed
     * @exception EDMException thropwn while parsing
     */
    public void parseXML(Element xmlElement) throws EDMException {
        parseCommonFields(xmlElement);
        NodeList inputArgList = xmlElement.getElementsByTagName(SQLObject.TAG_INPUT);
        TagParserUtility.parseInputTagList(this, inputArgList);
    }

    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws EDMException {
        // make sure root predicate is null out for input
        if (sqlObj instanceof SQLPredicate) {
            ((SQLPredicate) sqlObj).setRoot(null);
        }

        return super.removeInputByArgName(argName, sqlObj);
    }

    public void secondPassParse(Element element) throws EDMException {
        TagParserUtility.parseInputTag(this, element);
    }

    public void setArgument(String argName, Object val) throws EDMException {
        if (val instanceof String) {
            String strVal = (String) val;
            int argJdbc = this.operatorDefinition.getArgJdbcSQLType(argName);
            SQLLiteral literal = new SQLLiteralImpl(strVal, strVal, argJdbc);
            this.addInput(argName, literal);
        } else if (val instanceof SQLObject) {
            this.addInput(argName, (SQLObject) val);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Can_not_set_argument") + val + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_not_valid_SQLObject"));
        }

    }

    public void setArguments(List opArgs) throws EDMException {
        if (operatorDefinition == null) {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_null_Operator_Definition"));
        }

        if (opArgs != null) {
            // now add inputs from the argument list
            Iterator it = opArgs.iterator();
            int argIdx = 0;
            while (it.hasNext()) {
                SQLObject argValue = (SQLObject) it.next();
                SQLOperatorArg operatorArg = operatorDefinition.getOperatorArg(argIdx);
                String argName = operatorArg.getArgName();
                setArgument(argName, argValue);
                argIdx++;
            }
        }

    }

    public void setCustomOperator(boolean userFx) {
        throw new UnsupportedOperationException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Not_a_user_specific_function"));
    }

    public void setCustomOperatorName(String userFxName) {
        throw new UnsupportedOperationException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Not_a_user_specific_function"));
    }

    public void setDbSpecificOperator(String dbSpName) throws EDMException {
        // first try all lower case
        String cdbSpName = dbSpName.toLowerCase();
        operatorDefinition = SQLOperatorFactory.getDefault().getDbSpecficOperatorDefinition(dbSpName);
        if (operatorDefinition == null) {
            // now try upper case
            cdbSpName = dbSpName.toUpperCase();
            operatorDefinition = SQLOperatorFactory.getDefault().getDbSpecficOperatorDefinition(cdbSpName);
            // if it is still null then throw exception
            if (operatorDefinition == null) {
                throw new EDMException(dbSpName + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_not_a_recognized_operator"));
            }
        }

        // set IOperatorXmlInfo
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(operatorDefinition.getOperatorName());
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Cannot_locate_definition") + operatorDefinition.getOperatorName());
        }
    }

    public void setOperatorType(String opName) throws EDMException {
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(opName);
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Cannot_locate_definition") + opName);
        }
    }

    public void setOperatorXmlInfo(IOperatorXmlInfo opInfo) throws EDMException {
        this.operatorXmlInfo = opInfo;
        String aType = opInfo.getName();
        setAttribute(SQLOperator.ATTR_SCRIPTREF, aType);
        operatorDefinition = SQLOperatorFactory.getDefault().getSQLOperatorDefinition(aType);
        if (operatorDefinition == null) {
            throw new EDMException(aType + NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_not_a_recognized_operator"));
        }

        int argCount = operatorDefinition.getArgCount();
        init(argCount);
    }

    /**
     * method setRoot sets the root SQLPredicate.
     * 
     * @param pred is the SQLPredicate of the new root.
     */
    public void setRoot(SQLPredicate pred) {
        this.root = pred;
    }

    /**
     * set to true if parenthesis needs to be appended
     * 
     * @param show bool
     */
    public void setShowParenthesis(boolean show) {
        setAttribute(ATTR_PARENTHESIS, new Boolean(show));
    }

    public String toString() {
        String str = super.toString();

        try {
            GeneratorUtil eval = GeneratorUtil.getInstance();
            eval.setTableAliasUsed(true);
            str = eval.getEvaluatedString(this);
            eval.setTableAliasUsed(false);
        } catch (EDMException ignore) {
            // ignore
        }
        return str;
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets leftmost predicate, if any, in composite predicate hierarchy of which this
     * instance is a part.
     * 
     * @return leftmost SQLPredicate instance
     */
    protected SQLPredicate getLeftMostPredicate() {
        SQLPredicate ret = getLeftMostPredicate(this);
        if (ret == null) {
            return (this);
        }
        return (ret);
    }

    /**
     * Gets leftmost predicate of either (1) this instance or (2) the given predicate
     * instance.
     * 
     * @param pred SQLPredicate which may be searched for its leftmost predicate if this
     *        instance has no leftmost predicate.
     * @return leftmost predicate of either this instance or pred
     */
    protected SQLPredicate getLeftMostPredicate(SQLPredicate pred) {
        SQLPredicate newReturnPred = null;

        if (pred == null) {
            return null;
        }

        SQLInputObject inObj = getInput(LEFT);
        SQLObject leftObj = null;

        if (inObj != null) {
            leftObj = inObj.getSQLObject();
        }

        if (leftObj != null && (leftObj instanceof SQLPredicate)) {
            SQLObject sqlObj = null;
            if (pred.getInput(LEFT) != null) {
                sqlObj = pred.getInput(LEFT).getSQLObject();
            }

            if (sqlObj instanceof SQLPredicate) {
                newReturnPred = this.getLeftMostPredicate((SQLPredicate) sqlObj);
            }

            // if there are no left most predicate then see if pred has left as predicate
            if (newReturnPred == null) {
                SQLInputObject inObj1 = pred.getInput(LEFT);
                if (inObj1 != null) {
                    sqlObj = inObj1.getSQLObject();
                    if (sqlObj instanceof SQLPredicate) {
                        return (SQLPredicate) sqlObj;
                    }
                }
            } else {
                return (newReturnPred);
            }
        }
        return null;
    }

    protected void parseCommonFields(Element xmlElement) throws EDMException {
        super.parseXML(xmlElement);
        String opName = (String) getAttributeObject(SQLOperator.ATTR_SCRIPTREF);
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(opName);
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLPredicateImpl.class, "ERROR_Cannot_locate_definition") + opName);
        }
    }

    private void init(int argCount) {
        for (int i = 0; i < argCount; i++) {
            SQLOperatorArg operatorArg = operatorDefinition.getOperatorArg(i);
            String argName = operatorArg.getArgName();
            IOperatorField field = null;
            if (operatorXmlInfo != null) {
                field = operatorXmlInfo.getInputField(argName);
            }

            SQLInputObject inputObject = new SQLInputObjectImpl(argName, (field != null ? field.getDisplayName() : argName), null);
            this.inputMap.put(argName, inputObject);
        }
    }

    // a group predicate is AND or OR which groups two comparison predicates
    private boolean isGroupPredicate(String op) {
        if (op != null && (op.equalsIgnoreCase("AND") || op.equalsIgnoreCase("OR") || op.equalsIgnoreCase("NOT"))) {
            return true;
        }
        return false;
    }
}

