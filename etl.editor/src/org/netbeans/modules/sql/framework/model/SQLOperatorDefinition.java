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
package org.netbeans.modules.sql.framework.model;

import java.util.List;

import org.w3c.dom.Element;

import com.sun.etl.exception.BaseException;

/**
 * Defines methods for obtaining metadata of an SQL operator.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SQLOperatorDefinition extends Cloneable {

    public static final String ATTR_AGGREGATE_FUNCTION = "aggregate_function";

    public static final String ATTR_SHOWPARENTHESIS = "showparenthesis";

    /**
     * get a list of argument to use while calculating precision and scale
     */
    public List getArg2Use();

    /**
     * Gets the number of input arguments defined in the script. If the operator script
     * defines an operator with variable number of arguments then we allow a maximum of
     * 100 input arguments.
     * 
     * @return maximum number of arguments this operator can support.
     */
    public int getArgCount();

    /**
     * Indicates if this operator can take a variable number of input arguments.
     * 
     * @return flag indicating weather this operator takes variable number of input
     *         arguments.
     */
    public int getArgCountType();

    /**
     * Gets index of argument associated with given name, if any.
     * 
     * @param arg Argument name
     * @return Argument index if successful, -1 if failed.
     */
    public int getArgIndex(String arg);

    /**
     * Gets the argument name, if any, associated with the given index.
     * 
     * @param i index of argument.
     * @return JDBC type if successful, JDBCSQL_TYPE_UNDEFINED if failed.
     * @see SQLConstants#JDBCSQL_TYPE_UNDEFINED
     */
    public int getArgJdbcSQLType(int i);

    /**
     * Gets the JDBC sql type associated with the given argument name.
     * 
     * @param argName name of argument
     * @return JDBC type if successful, JDBCSQL_TYPE_UNDEFINED if failed.
     * @see SQLConstants#JDBCSQL_TYPE_UNDEFINED
     */
    public int getArgJdbcSQLType(String argName);

    /**
     * Get List of current arguments.
     * 
     * @return Argument list.
     */
    public List getArgList();

    /**
     * Set argument list
     *
     * @param args List
     */
    public void setArgList(List<SQLOperatorArg> args);
    /**
     * Gets the SQL type of the argument, if any, associated with the given index.
     * 
     * @param i index of argument
     * @return SQL type of argument indexed by i, null if no argument exists at i.
     */
    public String getArgType(int i);

    /**
     * @see SQLObject#getAttributeObject
     */
    public Object getAttributeValue(String attrName);

    /**
     * get the database specfic name of the operator
     */
    public String getDbSpecficName();

    /**
     * get the gui representation of operator
     */
    public String getGuiName();

    /**
     * Gets Operator Arg for the given index
     * 
     * @param index for OperatorArg
     * @return SQLOperatorArg
     */
    public SQLOperatorArg getOperatorArg(int index);

    /**
     * get the category type of the operator (numeric of string)
     * 
     * @return operator category type
     */
    public String getOperatorCategoryType();

    /**
     * Gets the canonical name of this operator.
     * 
     * @return canonical name of operator
     */
    public String getOperatorName();

    /**
     * Gets JDBC SQL type of this operator's output
     * 
     * @return JDBC type if successful, -65535 if failed.
     */
    public int getOutputJdbcSQLType();

    /**
     * sets the jdbc type of this operators return type
     * @param int jdbcType sql constant for the type
     */
    public void setOutputJdbcSQLType(int jdbcType);
    
    /**
     * This method is another convenience method to compute
     * the sql constant for the given string and set the jdbc type
     * sets the jdbc type of this operators return type
     * @param String jdbcType
     */
    public void setOutputJdbcSQLType(String jdbcType);
    /**
     * Gets the range based on argname
     * 
     * @param argName for which range is returned
     * @return String for range
     */
    public String getRange(String argName);

    /**
     * Gets the script associated with this operator.
     * 
     * @return SQL text representation of this operator, if any.
     */
    public String getScript();

    /**
     * Gets base name of argument, if any, used in generating arguments for operators that
     * can accept multiple inputs of the same type ("var" operators, e.g., "varconcat",
     * "varadd").
     * 
     * @return name of var argument
     */
    public String getVarOperatorArgName();

    /**
     * Reads this operator's script and configuration information from the given DOM
     * element.
     * 
     * @param element DOM element containing operator configuration information
     */
    public void parseXML(Element element) throws BaseException;

    /**
     * Clone itself. Operator Definitions are immutable, except UserFunction definition.
     */
    /**
     * @param operatorArgs List of SQLOperatorArg
     * @return
     */
    public Object clone(List operatorArgs);
}
