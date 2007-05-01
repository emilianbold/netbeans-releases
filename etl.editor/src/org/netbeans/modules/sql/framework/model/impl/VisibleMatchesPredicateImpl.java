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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;

/**
 * @author Ritesh Adval
 */
public class VisibleMatchesPredicateImpl implements VisibleSQLPredicate {
    private VisibleSQLPredicate delegate = null;

    public VisibleMatchesPredicateImpl() {
        delegate = new VisibleSQLPredicateImpl();
    }

    public VisibleMatchesPredicateImpl(VisibleMatchesPredicateImpl src) throws BaseException {
        this();
        if (src == null) {
            throw new IllegalArgumentException("can not create VisibleSQLPredicate using copy constructor, src is null");
        }
        try {
            delegate = (VisibleSQLPredicate) ((VisibleSQLPredicateImpl) src.delegate).clone();
        } catch (CloneNotSupportedException ex) {
            throw new BaseException(ex);
        }
    }

    public void addInput(String argName, SQLObject newInput) throws BaseException {
        delegate.addInput(argName, newInput);
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            VisibleMatchesPredicateImpl vmPredicate = new VisibleMatchesPredicateImpl(this);
            return vmPredicate;
        } catch (BaseException ex) {
            throw new CloneNotSupportedException("can not create clone of " + this.getOperatorType());
        }
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

    public SQLPredicate getRoot() {
        return delegate.getRoot();
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

    public boolean isCustomOperator() {
        return delegate.isCustomOperator();
    }

    public int isInputCompatible(String argName, SQLObject input) {
        return delegate.isInputCompatible(argName, input);
    }

    public boolean isInputStatic(String inputName) {
        return delegate.isInputStatic(inputName);
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
        if (val instanceof VisibleSQLLiteralImpl) {
            VisibleSQLLiteralImpl visLiteral = (VisibleSQLLiteralImpl) val;
            SQLLiteral literal = new SQLLiteralImpl(visLiteral);
            delegate.setArgument(argName, literal);
        } else {
            delegate.setArgument(argName, val);
        }
    }

    public void setArguments(List args) throws BaseException {
        SQLOperatorDefinition operatorDefinition = delegate.getOperatorDefinition();
        if (operatorDefinition == null) {
            throw new BaseException("Operator Definition is null.");
        }

        if (args != null) {
            // now add inputs from the arg list
            Iterator it = args.iterator();
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

    public void setRoot(SQLPredicate pred) {
        delegate.setRoot(pred);
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
