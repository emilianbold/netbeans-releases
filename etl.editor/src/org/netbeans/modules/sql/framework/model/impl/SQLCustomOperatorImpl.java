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
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.codegen.OperatorInstance;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.CustomOperatorNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLUtils;

/**
 * @author Girish Patil
 * @version $Revision$
 */

public class SQLCustomOperatorImpl extends SQLGenericOperatorImpl {
    private static final String OPT_ARGS_TAG = "args";
    private static final String USER_FUNCTION_ID = "userFx";
    private static final String PREFIX_ARG = "arg";
    private static final String NAME_TAG = "name";
    private static final String JDBC_TYPE_TAG = "jdbc-type";
    private static final String OUTPUT_TAG = "output";

    public SQLCustomOperatorImpl() throws BaseException {
        super();
        this.type = SQLConstants.CUSTOM_OPERATOR;
        this.setDbSpecificOperator(USER_FUNCTION_ID);
        this.setAttribute(ATTR_CUSTOM_OPERATOR, Boolean.TRUE);
    }

    public SQLCustomOperatorImpl(SQLGenericOperator src) throws BaseException {
        this.type = SQLConstants.CUSTOM_OPERATOR;
        super.copyFrom(src);
        if (this.operatorDefinition == null){
             this.setDbSpecificOperator(USER_FUNCTION_ID);
        }
    }
    
    public void setDbSpecificOperator(String operatorName) {
    	this.operatorDefinition =  new OperatorInstance(operatorName); 
    }
    
    public void setOperatorXmlInfo(IOperatorXmlInfo opInfo) throws BaseException {
        super.operatorXmlInfo = opInfo;
        String aType = opInfo.getName();
        setAttribute(SQLOperator.ATTR_SCRIPTREF, aType);
        this.hasVariableArgs = false;
                
        
        List outputFields = opInfo.getOutputFields();
        Iterator iter = outputFields.iterator();
        if(iter.hasNext()) {
        	IOperatorField field = (IOperatorField) iter.next();
        	String outputType = (String) field.getAttributeValue("retTypeStr");
        	this.operatorDefinition.setOutputJdbcSQLType(outputType);
        }
    }
    
    public int getOutputJdbcType() {
        String outputType = null;
        List outputFields = operatorXmlInfo.getOutputFields();
        Iterator iter = outputFields.iterator();
        if (iter.hasNext()) {
            IOperatorField field = (IOperatorField) iter.next();
            outputType = (String) field.getAttributeValue("retTypeStr");

        }
        return SQLUtils.getStdJdbcType(outputType);
    }
    
   

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#addInput
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        if (argName == null) {
            throw new BaseException("Input arguments not specified");
        }

        if (newInput != null) {
	        int newType = newInput.getObjectType();
	        String objType = TagParserUtility.getDisplayStringFor(newType);

	        if (!isInputValid(argName, newInput)) {
	            throw new BaseException("Cannot link " + objType + " '" + newInput.getDisplayName() + "' as input to '" + argName + "' in "
	                + TagParserUtility.getDisplayStringFor(type) + " '" + this.getDisplayName() + "'");
	        }
        }

	    SQLInputObject inputObject = (SQLInputObject) this.inputMap.get(argName);

        if (inputObject != null) {
            inputObject.setSQLObject(newInput);
        } else {
            inputObject = new SQLInputObjectImpl(argName, displayName + "_" + argName, null);
            inputObject.setSQLObject(newInput);
            inputMap.put(argName, inputObject);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            SQLCustomOperatorImpl op = new SQLCustomOperatorImpl(this);
            return op;
        } catch (BaseException ex) {
            throw new CloneNotSupportedException("can not create clone of " + this.getOperatorType());
        }
    }

    public String getCustomOperatorName() {
        String custOpName = (String) getAttributeObject(ATTR_CUSTOM_OPERATOR_NAME);
        if (custOpName == null) {
            custOpName = this.displayName;
        }
        return custOpName;
    }
    
    /**
     * returns the default custom operator type userFx
     * @return String 
     */
    public String getOperatorType() {
    	return this.USER_FUNCTION_ID;
    }

    /**
     * Always returns false, as once this object is created, arguments are fixed for this
     * instance.
     */
    public boolean hasVariableArgs() {
        return false;
    }

    public boolean isCustomOperator() {
        Boolean custOp = (Boolean) getAttributeObject(ATTR_CUSTOM_OPERATOR);
        if (custOp == null) {
            custOp = Boolean.FALSE;
        }
        return custOp.booleanValue();
    }

    /**
     * @see SQLObject#getObjectType
     */
    public int getObjectType() {
        return type;
    }
    
    //public int isInputCompatible(String argName, SQLObject input) {
    //    return SQLConstants.TYPE_CHECK_COMPATIBLE;
    //}

    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException {
        Iterator it = inputMap.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            SQLInputObject inputObject = (SQLInputObject) inputMap.get(name);
            if (inputObject != null) {
                SQLObject sqlObject = inputObject.getSQLObject();
                if (sqlObject != null && sqlObject.equals(sqlObj)) {
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

    public void setArgument(String argName, Object val) throws BaseException {
        if (val instanceof String) {
            String strVal = (String) val;
            int argJdbc = this.operatorDefinition.getArgJdbcSQLType(PREFIX_ARG);
            SQLLiteral literal = new SQLLiteralImpl(strVal, strVal, argJdbc);
            this.addInput(argName, literal);
        } else if (val instanceof SQLObject) {
            this.addInput(argName, (SQLObject) val);
        } else {
            throw new BaseException("Can not set argument, object " + val + "is not a valid SQLObject");
        }
    }

    public void setArguments(List opArgs) throws BaseException {
    	this.inputMap.clear();
        if (operatorDefinition == null) {
            throw new BaseException("Operator Definition is null.");
        }

        if (opArgs != null) {
            // now add inputs from the argument list
            Iterator it = opArgs.iterator();
            /** argument names are always arg1, arg2 etc. **/
            String argName = "arg";
            int argIdx = 1;
            while (it.hasNext()) {
                SQLObject argValue = (SQLObject) it.next();
                setArgument(argName + argIdx, argValue);
                argIdx++;
            }
        }
    }

    public void setCustomOperator(boolean custOp) {
        setAttribute(ATTR_CUSTOM_OPERATOR, new Boolean(custOp));
    }

    public void setCustomOperatorName(String custOpName) {
        setAttribute(ATTR_CUSTOM_OPERATOR_NAME, custOpName);
       	setDbSpecificOperator(custOpName);
        this.setDisplayName(custOpName);
    }

    public void initializeInputs(int numOfInputs) throws BaseException{
    	this.inputMap.clear();
    	if (numOfInputs > 0){
    		for (int i=1; i <= numOfInputs; i++){
    			this.addInput(PREFIX_ARG + i, null);
    		}
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
        this.appendOperatorDefinition(buffer, prefix + '\t');
        buffer.append(TagParserUtility.toXMLInputTag(prefix + '\t', this.inputMap));
        buffer.append(this.getGUIInfo().toXMLString(prefix + '\t'));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);
        NodeList nodes = xmlElement.getElementsByTagName(OPT_ARGS_TAG);
        if (nodes.getLength() > 0) {
            List inputArgs = new ArrayList();
            SQLOperatorArg retType = null;
            NodeList args = nodes.item(0).getChildNodes();
            for (int i = 0; i < args.getLength(); i++) {
                Node node = args.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (PREFIX_ARG.equals(element.getTagName())) {
                        String name = element.getAttribute(NAME_TAG);
                        String jdbcTypeStr = element.getAttribute(JDBC_TYPE_TAG);
                        int jdbcType = SQLUtils.getStdJdbcType(jdbcTypeStr);
                        SQLOperatorArg optArg = new SQLOperatorArg(name, jdbcType);
                        inputArgs.add(optArg);
                    } else if (OUTPUT_TAG.equals(element.getTagName())) {
                        String jdbcTypeStr = element.getAttribute(JDBC_TYPE_TAG);
                        int jdbcType = SQLUtils.getStdJdbcType(jdbcTypeStr);
                        retType = new SQLOperatorArg("return", jdbcType);
                    }
                }
            }

            CustomOperatorNode customOptNode = new CustomOperatorNode(this.getOperatorXmlInfo(), inputArgs, retType);
            this.setOperatorXmlInfo(customOptNode);
            this.getOperatorDefinition().setArgList(inputArgs);
        }
    }
    private void appendOperatorDefinition(StringBuilder buffer, String prefix) {
        SQLOperatorDefinition optDef = this.getOperatorDefinition();
        Iterator it = optDef.getArgList().iterator();
        //Write out input arguments
        buffer.append(prefix).append('<').append(OPT_ARGS_TAG).append('>').append('\n');
        while (it.hasNext()) {
            SQLOperatorArg optArg = (SQLOperatorArg)it.next();
            buffer.append(prefix + '\t').append('<').append(PREFIX_ARG);
            buffer.append(' ').append(NAME_TAG).append("=\"").append(optArg.getArgName()).append('"');
            buffer.append(' ').append(JDBC_TYPE_TAG).append("=\"").append(SQLUtils.getStdSqlType(optArg.getJdbcType())).append('"');
            buffer.append("/>\n");
        }
        //Write out return type
        int jdbcType = this.getOutputJdbcType();
        buffer.append(prefix + '\t').append('<').append(OUTPUT_TAG);
        buffer.append(' ').append(JDBC_TYPE_TAG).append("=\"").append(
                SQLUtils.getStdSqlType(jdbcType)).append('"');
        buffer.append("/>\n");
        buffer.append(prefix).append("</").append(OPT_ARGS_TAG).append('>')
        .append('\n');
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
}
