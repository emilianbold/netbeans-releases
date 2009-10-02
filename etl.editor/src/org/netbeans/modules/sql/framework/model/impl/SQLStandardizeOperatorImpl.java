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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;

/**
 * @author Ritesh Adval
 */
public class SQLStandardizeOperatorImpl implements SQLGenericOperator {

    public static final String ARG_LOCALE = "locale";
    public static final String ARG_PART = "part";
    public static final String ARG_STR = "str";

    private SQLGenericOperator delegate = null;

    public SQLStandardizeOperatorImpl() {
        delegate = new SQLGenericOperatorImpl();
    }

    public SQLStandardizeOperatorImpl(SQLStandardizeOperatorImpl src) throws CloneNotSupportedException {
        this();
        if (src == null) {
            throw new IllegalArgumentException("can not create VisibleSQLPredicate using copy constructor, src is null");
        }
        delegate = (SQLGenericOperator) src.delegate.cloneSQLObject();

    }

    public void addInput(String argName, SQLObject newInput) throws BaseException {
        delegate.addInput(argName, newInput);
    }

    public Object clone() throws CloneNotSupportedException {
        return new SQLStandardizeOperatorImpl(this);
    }

    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    public Object getArgumentValue(String argName) throws BaseException {
        return delegate.getArgumentValue(argName);
    }

    public Attribute getAttribute(String attrName) {
        return delegate.getAttribute(attrName);
    }

    public Collection getAttributeNames() {
        return delegate.getAttributeNames();
    }

    public Object getAttributeObject(String attrName) {
        return delegate.getAttributeObject(attrName);
    }

    public List getChildSQLObjects() {
        return delegate.getChildSQLObjects();
    }

    public String getCustomOperatorName() {
        return delegate.getCustomOperatorName();
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public String getFooter() {
        return delegate.getFooter();
    }

    public GUIInfo getGUIInfo() {
        return delegate.getGUIInfo();
    }

    public String getHeader() {
        return delegate.getHeader();
    }

    public String getId() {
        return delegate.getId();
    }

    public SQLInputObject getInput(String argName) {
        return delegate.getInput(argName);
    }

    public Map getInputObjectMap() {
        return delegate.getInputObjectMap();
    }

    public int getJdbcType() {
        return delegate.getJdbcType();
    }

    public int getObjectType() {
        return delegate.getObjectType();
    }

    public SQLOperatorDefinition getOperatorDefinition() {
        return delegate.getOperatorDefinition();
    }

    public String getOperatorType() {
        return delegate.getOperatorType();
    }

    public IOperatorXmlInfo getOperatorXmlInfo() {
        return delegate.getOperatorXmlInfo();
    }

    public SQLObject getOutput(String argName) throws BaseException {
        return this;
    }

    public Object getParentObject() {
        return delegate.getParentObject();
    }

    public List getSourceColumnsUsed() {
        return delegate.getSourceColumnsUsed();
    }

    public SQLObject getSQLObject(String argName) {
        return delegate.getSQLObject(argName);
    }

    public Map getSQLObjectMap() {
        return delegate.getSQLObjectMap();
    }

    public List getTargetColumnsUsed() {
        return delegate.getTargetColumnsUsed();
    }

    public boolean hasSourceColumn() {
        return delegate.hasSourceColumn();
    }

    public boolean hasTargetColumn() {
        return delegate.hasTargetColumn();
    }

    public boolean hasVariableArgs() {
        return delegate.hasVariableArgs();
    }

    public boolean isAggregateFunction() {
        return delegate.isAggregateFunction();
    }

    public int isCastable(String argName, SQLObject input) {
        return delegate.isCastable(argName, input);
    }

    public boolean isCustomOperator() {
        return delegate.isCustomOperator();
    }

    public int isInputCompatible(String argName, SQLObject input) {
        return delegate.isInputCompatible(argName, input);
    }

    public boolean isInputStatic(String argName) {
        return delegate.isInputStatic(argName);
    }

    public boolean isInputValid(String argName, SQLObject input) {
        return delegate.isInputValid(argName, input);
    }

    public boolean isShowParenthesis() {
        return delegate.isShowParenthesis();
    }

    public void parseXML(Element element) throws BaseException {
        delegate.parseXML(element);
    }

    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException {
        return delegate.removeInputByArgName(argName, sqlObj);
    }

    public void reset() {
        delegate.reset();
    }

    public void secondPassParse(Element element) throws BaseException {
        delegate.secondPassParse(element);
    }

    public void setArgument(String argName, Object val) throws BaseException {
        if (ARG_PART.equals(argName) || ARG_LOCALE.equals(argName)) {
            if (val instanceof VisibleSQLLiteralImpl) {
                VisibleSQLLiteralImpl visLiteral = (VisibleSQLLiteralImpl) val;
                SQLLiteral literal = new SQLLiteralImpl(visLiteral);
                delegate.setArgument(argName, literal);
            } else {
                delegate.setArgument(argName, val);
            }
        } else {
            delegate.setArgument(argName, val);
        }
    }

    public void setArguments(List opArgs) throws BaseException {
        SQLOperatorDefinition operatorDefinition = delegate.getOperatorDefinition();
        if (operatorDefinition == null) {
            throw new BaseException("Operator Definition is null.");
        }

        if (opArgs != null) {
            // now add inputs from the arg list
            Iterator it = opArgs.iterator();
            int i = 0;
            while (it.hasNext()) {
                SQLObject argValue = (SQLObject) it.next();
                if (i == 0 && argValue instanceof SQLLiteral) {
                    SQLLiteral literal = (SQLLiteral) argValue;
                    String val = literal.getValue();
                    if ((val).equals("Address") || val.equals("BusinessName")) {
                        continue;
                    }
                }
                SQLOperatorArg arg = operatorDefinition.getOperatorArg(i);
                setArgument(arg.getArgName(), argValue);
                i++;
            }
        }
    }

    public void setAttribute(String attrName, Object val) {
        delegate.setAttribute(attrName, val);
    }

    public void setCustomOperator(boolean userFx) {
        delegate.setCustomOperator(userFx);
    }

    public void setCustomOperatorName(String userFxName) {
        delegate.setCustomOperatorName(userFxName);
    }

    public void setDbSpecificOperator(String dbName) throws BaseException {
        delegate.setDbSpecificOperator(dbName);
    }

    public void setDisplayName(String newName) {
        delegate.setDisplayName(newName);
    }

    public void setId(String newId) throws BaseException {
        delegate.setId(newId);
    }

    public void setJdbcType(int newType) {
        delegate.setJdbcType(newType);
    }

    public void setOperatorType(String newOperator) throws BaseException {
        delegate.setOperatorType(newOperator);
    }

    public void setOperatorXmlInfo(IOperatorXmlInfo opInfo) throws BaseException {
        delegate.setOperatorXmlInfo(opInfo);
    }

    public void setParentObject(Object newParent) throws BaseException {
        delegate.setParentObject(newParent);
    }

    public void setShowParenthesis(boolean show) {
        delegate.setShowParenthesis(show);
    }

    public String toString() {
        return delegate.toString();
    }

    public String toXMLString(String prefix) throws BaseException {
        return delegate.toXMLString(prefix);
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

}

