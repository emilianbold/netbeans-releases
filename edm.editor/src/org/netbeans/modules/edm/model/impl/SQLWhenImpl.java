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

import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.SQLWhen;
import org.netbeans.modules.edm.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;

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

    public void addInput(String argName, SQLObject newInput) throws EDMException {
        if (CONDITION.equals(argName)) {
            oldPredicate = (SQLPredicate) newInput;
        } else {
            super.addInput(argName, newInput);
        }
    }

    public SQLCondition getCondition() {
        return whenCondition;
    }

    public SQLInputObject getInput(String argName) {
        if (CONDITION.equals(argName)) {
            return new SQLInputObjectImpl(CONDITION, CONDITION, oldPredicate);
        } else {
            return super.getInput(argName);
        }
    }

    public int getJdbcType() {
        SQLObject value = this.getSQLObject(RETURN);

        // Return either the associated return object's type, or the default
        // type as defined in AbstractSQLObject.
        return (value != null) ? value.getJdbcType() : super.getJdbcType();
    }

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
     * @throws EDMException if element is null or error occurs during parsing
     */
    public void parseXML(Element xmlElement) throws EDMException {
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
     * @exception EDMException thrown while parsing
     */
    public void secondPassParse(Element element) throws EDMException {
        TagParserUtility.parseInputTag(this, element);
    }

    public void setCondition(SQLCondition cond) {
        whenCondition = cond;
    }

    /**
     * Overrides parent implementation to append when condition information.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     * @throws EDMException if error occurs during XML creation
     */
    public String toXMLString(String prefix) throws EDMException {
        StringBuilder buffer = new StringBuilder();
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
