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
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLGenericOperator;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLLiteral;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.model.SQLOperatorArg;
import org.netbeans.modules.edm.model.SQLOperatorDefinition;
import org.netbeans.modules.edm.editor.utils.GeneratorUtil;
import org.netbeans.modules.edm.editor.utils.OperatorUtil;
import org.netbeans.modules.edm.model.visitors.SQLVisitor;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorField;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.openide.util.NbBundle;

/**
 * Model for operators supported by SQLBuilder
 * 
 * @author Ritesh Adval, Sudhi Seshahcala
 * @version $Revision$
 */
public class SQLGenericOperatorImpl extends SQLConnectableObjectImpl implements SQLGenericOperator {

    /* Log4J category name */
    static final String LOG_CATEGORY = SQLGenericOperator.class.getName();

    /* script of this operator. */
    protected SQLOperatorDefinition operatorDefinition;

    /* GUI state info */
    private GUIInfo guiInfo = new GUIInfo();

    /* flag indicating if operator can take variable arguments. */
    protected boolean hasVariableArgs = false;

    protected IOperatorXmlInfo operatorXmlInfo;

    /** Creates a new default instance of SQLGenericOperator */
    public SQLGenericOperatorImpl() {
        super();
        this.type = SQLConstants.GENERIC_OPERATOR;
    }

    public SQLGenericOperatorImpl(SQLGenericOperator src) throws EDMException {
        this();
        copyFrom(src);
    }

    public SQLGenericOperatorImpl(String newName, String aType) throws EDMException {
        this();
        this.displayName = newName;
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(aType);
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Cannot_locate_definition") + aType);
        }
    }

    public void addInput(String argName, SQLObject newInput) throws EDMException {
        if (argName == null || newInput == null) {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Input_arguments_not_specified"));
        }

        int newType = newInput.getObjectType();
        String objType = TagParserUtility.getDisplayStringFor(newType);

        if (!isInputValid(argName, newInput)) {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Cannot_link") + objType + " '" + newInput.getDisplayName() + NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_as_input") + argName + "' in "
                + TagParserUtility.getDisplayStringFor(type) + " '" + this.getDisplayName() + "'");
        }

        // If operator is not of type variable arguments then we know its number
        // of inputs
        if (!hasVariableArgs) {
            SQLInputObject inputObject = (SQLInputObject) this.inputMap.get(argName);
            if (inputObject != null) {
                inputObject.setSQLObject(newInput);
            } else {
                throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Could_not_resolve_link.") + argName + "' in " + TagParserUtility.getDisplayStringFor(type)
                    + " '" + this.getDisplayName() + "'");

            }
        } else {
            // we need to do this check, bcos at reload time argument name is already set
            // only at link time argument name will not be set
            SQLOperatorArg operatorArg = operatorDefinition.getOperatorArg(0);
            String name = operatorArg.getArgName();

            if (argName.equals(name)) {
                argName = generateVarOperatorArgName(argName);
            }

            SQLInputObject inputObject = new SQLInputObjectImpl(argName, displayName, null);
            inputObject.setSQLObject(newInput);
            inputMap.put(argName, inputObject);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            SQLGenericOperator op = new SQLGenericOperatorImpl(this);
            return op;
        } catch (EDMException ex) {
            throw new CloneNotSupportedException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_can_not_create_clone") + this.getOperatorType());
        }
    }

    // We need to add more for equals ???
    /**
     * @param refObj 
     * @see java.lang.Object#equals
     */
    public boolean equals(Object refObj) {
        return super.equals(refObj);
    }

    public Object getArgumentValue(String argName) throws EDMException {
        return this.getSQLObject(argName);
    }

    public String getCustomOperatorName() {
        String ufName = (String) getAttributeObject(ATTR_CUSTOM_OPERATOR_NAME);
        if (ufName == null) {
            ufName = this.displayName;
        }

        return ufName;
    }

    public String getDisplayName() {
        String dName = super.getDisplayName();
        if (dName == null) {
            return this.getOperatorType();
        }

        return dName;
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
     * Overrides default implementation to return JDBC type as specified by the operator
     * script.
     * 
     * @return operator JDBC type
     */
    public int getJdbcType() {
        return (operatorDefinition != null) ? operatorDefinition.getOutputJdbcSQLType() : SQLConstants.JDBCSQL_TYPE_UNDEFINED;
    }

    /**
     * Get the script of this operator.
     * 
     * @return Return script of this operator.
     */
    public SQLOperatorDefinition getOperatorDefinition() {
        return operatorDefinition;
    }

    /**
     * Gets canonical operator type, e.g., "concat", "tolowercase", etc..
     * 
     * @return canonical operator name
     */
    public String getOperatorType() {
        if (operatorDefinition != null) {
            return operatorDefinition.getOperatorName();
        }

        Attribute attr = getAttribute(SQLOperator.ATTR_SCRIPTREF);
        return (attr != null) ? attr.getAttributeValue().toString() : null;
    }

    public IOperatorXmlInfo getOperatorXmlInfo() {
        return this.operatorXmlInfo;
    }

    /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Indicates weather this operator has variable number of arguments.
     * 
     * @return true if operator has variable number of arguments; else, false.
     */
    public boolean hasVariableArgs() {
        return (this.hasVariableArgs);
    }

    /**
     * check if operator is an aggregate function
     * 
     * @return bool
     */
    public boolean isAggregateFunction() {
        SQLOperatorDefinition opdef = this.getOperatorDefinition();

        if (opdef != null) {
            Boolean aggFunc = (Boolean) opdef.getAttributeValue(SQLOperatorDefinition.ATTR_AGGREGATE_FUNCTION);
            if (aggFunc != null) {
                return aggFunc.booleanValue();
            }
        }
        return false;
    }

    /**
     * Determines if input referenced by the given argument name can received a link from
     * the given SQLObject without breaking type casting rules.
     * 
     * @param argName name of the operator input to which the source operator is being
     *        connected.
     * @param input SQLObject to which input argument is being connected.
     * @return true if 'argName' can be connected to input, false otherwise
     */
    public int isCastable(String argName, SQLObject input) {
        int srcType = SQLConstants.JDBCSQL_TYPE_UNDEFINED;

        if (hasVariableArgs) {
            argName = operatorDefinition.getVarOperatorArgName();
        }

        int destType = operatorDefinition.getArgJdbcSQLType(argName);

        // XXX Refactor to completely reflect SQL syntax rules for sql operators.
        switch (input.getObjectType()) {
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:            	
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.SOURCE_COLUMN:
            case SQLConstants.VISIBLE_LITERAL:
            case SQLConstants.COLUMN_REF:
            case SQLConstants.CASE:
                srcType = input.getJdbcType();
                break;

            case SQLConstants.LITERAL:
                return SQLConstants.TYPE_CHECK_SAME;
            default:
                srcType = SQLConstants.JDBCSQL_TYPE_UNDEFINED;
        }

        return SQLOperatorFactory.getDefault().getCastingRuleFor(srcType, destType);
    }

    public boolean isCustomOperator() {
        Boolean uf = (Boolean) getAttributeObject(ATTR_CUSTOM_OPERATOR);
        if (uf == null) {
            uf = Boolean.FALSE;
        }
        return uf.booleanValue();
    }

    public int isInputCompatible(String argName, SQLObject input) {
        return isCastable(argName, input);
    }

    public boolean isInputStatic(String argName) {
        IOperatorField field = getOperatorXmlInfo().getInputField(argName);
        if (field != null) {
            return getOperatorXmlInfo().getInputField(argName).isStatic();
        }
        return false;
    }

    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null) {
            return false;
        }

        // XXX Refactor to completely reflect SQL syntax rules for generic operators.
        switch (input.getObjectType()) {
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.DATE_ARITHMETIC_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.SOURCE_COLUMN:
            case SQLConstants.LITERAL:
            case SQLConstants.COLUMN_REF:
            case SQLConstants.CASE:
                return true;

            case SQLConstants.VISIBLE_LITERAL:
                return this.checkRange(argName, input);

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
        SQLOperatorDefinition opdef = this.getOperatorDefinition();

        if (paran != null) {
            return paran.booleanValue();
        } else if (opdef != null) {
            Boolean showParan = (Boolean) opdef.getAttributeValue(SQLOperatorDefinition.ATTR_SHOWPARENTHESIS);
            if (showParan != null) {
                return showParan.booleanValue();
            }
        }

        return false;
    }


    public void parseXML(Element xmlElement) throws EDMException {
        super.parseXML(xmlElement);

        String opName = (String) getAttributeObject(SQLOperator.ATTR_SCRIPTREF);
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(opName);
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Cannot_locate_definition") + opName);
        }

        NodeList inputArgList = xmlElement.getElementsByTagName(TAG_INPUT);
        if (inputArgList != null && inputArgList.getLength() != 0) {
            TagParserUtility.parseInputTagList(this, inputArgList);
        }

        NodeList guiInfoList = xmlElement.getElementsByTagName(GUIInfo.TAG_GUIINFO);
        if (guiInfoList != null && guiInfoList.getLength() != 0) {
            Element elem = (Element) guiInfoList.item(0);
            guiInfo = new GUIInfo(elem);
        }
    }

    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws EDMException {
        // if operator is not of variable argument then call super class's
        // removeInputByArgName method
        if (!hasVariableArgs) {
            return super.removeInputByArgName(argName, sqlObj);
        }

        // we need to handle deletion of variable argument operator seperately here
        Iterator it = inputMap.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            SQLInputObject inputObject = (SQLInputObject) inputMap.get(name);
            if (inputObject != null) {
                SQLObject sqlObject = inputObject.getSQLObject();
                if (sqlObject != null && sqlObject.equals(sqlObj)) {
                    // (Bug #6795) For variable arguments, don't dissociate the
                    // source object...rather, delete the SQLInputObject itself.
                    SQLInputObject obj = (SQLInputObject) inputMap.remove(name);
                    if (obj != null) {
                        obj.setSQLObject(null);
                    }
                    return sqlObject;
                }
            }
        }
        return null;
    }

    /**
     * Second call parse
     * 
     * @param element to be parsed
     * @exception EDMException thrown while parsing
     */
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
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Can_not_set_argument") + val + NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_not_valid_SQLObject"));
        }
    }

    public void setArguments(List opArgs) throws EDMException {
        if (operatorDefinition == null) {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_null_Operator_Definition"));
        }

        int argCount = operatorDefinition.getArgCount();

        if (opArgs != null) {
            if (!this.hasVariableArgs && opArgs.size() != argCount) {
                throw new EDMException("expected " + argCount + " argument for operator " + this.getDisplayName() + ", but found " + opArgs
                    + " arguments.");
            }

            // now add inputs from the argument list
            Iterator it = opArgs.iterator();
            int argIdx = 0;
            while (it.hasNext()) {
                SQLObject argValue = (SQLObject) it.next();

                SQLOperatorArg operatorArg = operatorDefinition.getOperatorArg(argIdx);
                String argName = operatorArg.getArgName();
                setArgument(argName, argValue);

                if (!hasVariableArgs) {
                    argIdx++;
                }
            }
        }
    }

    public void setCustomOperator(boolean userFx) {
        throw new UnsupportedOperationException("Not a User function");
    }

    public void setCustomOperatorName(String userFxName) {
        throw new UnsupportedOperationException("Not a User function");
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
                throw new EDMException(dbSpName + " is not a recognized operator.");
            }
        }

        // set IOperatorXmlInfo
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(operatorDefinition.getOperatorName());
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Cannot_locate_definition") + operatorDefinition.getOperatorName());
        }
    }

    /**
     * sets canonical operator type, e.g., "concat", "tolowercase", etc..
     * 
     * @param opName canonical operator name
     */
    public void setOperatorType(String opName) throws EDMException {
        // set IOperatorXmlInfo
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(opName);
        if (operatorXml != null) {
            this.setOperatorXmlInfo(operatorXml);
        } else {
            throw new EDMException(NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_Cannot_locate_definition") + opName);
        }
    }

    public void setOperatorXmlInfo(IOperatorXmlInfo opInfo) throws EDMException {
        this.operatorXmlInfo = opInfo;
        String aType = opInfo.getName();
        setAttribute(SQLOperator.ATTR_SCRIPTREF, aType);
        operatorDefinition = SQLOperatorFactory.getDefault().getSQLOperatorDefinition(aType.toLowerCase());
        if (operatorDefinition == null) {
            throw new EDMException(aType + NbBundle.getMessage(SQLGenericOperatorImpl.class, "ERROR_not_a_recognized_operator"));
        }
        int argCount = operatorDefinition.getArgCount();
        this.hasVariableArgs = (operatorDefinition.getArgCountType() == SQLConstants.OPERATOR_ARGS_VARIABLE);

        if (!hasVariableArgs) {
            init(argCount);
        }
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
        try {
            return GeneratorUtil.getInstance().getEvaluatedString(this);
        } catch (EDMException ignore) {
            return "Unknown";
        }
    }

    /**
     * Overrides parent implementation to append GUIInfo information.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    public String toXMLString(String prefix) {
        StringBuilder buffer = new StringBuilder(500);
        if (prefix == null) {
            prefix = "";
        }

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));
        buffer.append(TagParserUtility.toXMLInputTag(prefix + "\t", this.inputMap));
        buffer.append(this.guiInfo.toXMLString(prefix + "\t"));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    protected void copyFrom(SQLGenericOperator op) throws EDMException {
        this.hasVariableArgs = op.hasVariableArgs();
        this.operatorDefinition = op.getOperatorDefinition();
        this.setOperatorXmlInfo(op.getOperatorXmlInfo());
        // then call super.copyFromSource so that inputs are copied
        super.copyFromSource(op);

        GUIInfo gInfo = op.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;
    }

    private boolean checkRange(String argName, SQLObject obj) {
        SQLLiteral lit = (SQLLiteral) obj;
        String range = operatorDefinition.getRange(argName);

        if (!StringUtil.isNullString(range)) {
            if (range.equals("unsigned") && obj.getJdbcType() == java.sql.Types.INTEGER) {
                String val = lit.getValue();
                try {
                    int intVal = Integer.parseInt(val);
                    return (intVal >= 0);
                } catch (NumberFormatException ne) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    private String generateVarOperatorArgName(String argName) {
        int cnt = 0;
        String aName = argName + "_" + cnt;
        while (isVarOperatorArgNameExist(aName)) {
            cnt++;
            aName = argName + "_" + cnt;
        }

        return aName;
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

    private boolean isVarOperatorArgNameExist(String argName) {
        Iterator it = inputMap.keySet().iterator();
        while (it.hasNext()) {
            String aName = (String) it.next();
            if (aName.equals(argName)) {
                return true;
            }
        }

        return false;
    }
}

