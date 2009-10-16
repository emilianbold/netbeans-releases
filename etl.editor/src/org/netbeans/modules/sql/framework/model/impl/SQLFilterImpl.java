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

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.utils.GeneratorUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * Represents boolean conditional expression for a source table filter.
 * 
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public class SQLFilterImpl extends SQLConnectableObjectImpl implements SQLFilter {

    /**
     * Overrides base class to implement unary predicates, i.e., predicates with only one
     * input, such as IS NULL and IS NOT NULL.
     * 
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class LeftUnary extends SQLFilterImpl {
        /**
         * Overrides parent implementation to silently discard inputs other than LEFT.
         * 
         * @param argName name of argument to add
         * @param newInput SQLObject to associate with argName as an input
         * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#addInput(java.lang.String,
         *      org.netbeans.modules.sql.framework.model.SQLObject)
         */
        public void addInput(String argName, SQLObject newInput) throws BaseException {
            if (LEFT.equals(argName)) {
                super.addInput(argName, newInput);
            }
        }

        /**
         * Overrides base implementation to reflect that only the LEFT input should be
         * tested for existence.
         * 
         * @return true if this filter is valid; false otherwise
         * @see org.netbeans.modules.sql.framework.model.SQLFilter#isValid()
         */
        public boolean isValid() {
            return (this.getOperator() != null && this.getSQLObject(SQLFilter.LEFT) != null);
        }

        /**
         * Overrides base implementation to return an instance of SQLFilter.Unary.
         * 
         * @param oldFilter filter to be cloned
         * @return new instance of SQLFilter.Unary with same characteristics as
         *         <code>oldFilter</code>
         * @see org.netbeans.modules.sql.framework.model.impl.SQLFilterImpl#createFilter(org.netbeans.modules.sql.framework.model.SQLFilter)
         */
        protected SQLFilter createFilter(SQLFilter oldFilter) {
            // XXX Should we only accept SQLFilterImpl.LeftUnary instances for cloning?
            SQLFilter filter = new SQLFilterImpl.LeftUnary();

            // set prefix
            if (oldFilter.getPrefix() != null) {
                filter.setPrefix(oldFilter.getPrefix());
            }

            // set operator
            if (oldFilter.getOperator() != null) {
                filter.setOperator(oldFilter.getOperator());
            }

            // set display name
            if (oldFilter.getDisplayName() != null) {
                filter.setDisplayName(oldFilter.getDisplayName());
            }

            try {
                if (oldFilter.getSQLObject(SQLFilter.LEFT) != null) {
                    filter.addInput(SQLFilter.LEFT, oldFilter.getSQLObject(SQLFilter.LEFT));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return filter;
        }

        /**
         * Overrides base implementation to initialize only a single LEFT input.
         * 
         * @see org.netbeans.modules.sql.framework.model.impl.SQLFilterImpl#initInputs()
         */
        protected void initInputs() {
            SQLInputObject inputObject = new SQLInputObjectImpl(LEFT, "", null);
            this.inputMap.put(LEFT, inputObject);
        }
    }

    public static class RightUnary extends SQLFilterImpl {
        public void addInput(String argName, SQLObject newInput) throws BaseException {
            if (RIGHT.equals(argName)) {
                super.addInput(argName, newInput);
            }
        }

        /**
         * Overrides base implementation to reflect that only the RIGHT input should be
         * tested for existence.
         * 
         * @return true if this filter is valid; false otherwise
         * @see org.netbeans.modules.sql.framework.model.SQLFilter#isValid()
         */
        public boolean isValid() {
            return (this.getOperator() != null && this.getSQLObject(SQLFilter.RIGHT) != null);
        }

        /**
         * Overrides base implementation to return an instance of SQLFilter.Unary.
         * 
         * @param oldFilter filter to be cloned
         * @return new instance of SQLFilter.Unary with same characteristics as
         *         <code>oldFilter</code>
         * @see org.netbeans.modules.sql.framework.model.impl.SQLFilterImpl#createFilter(org.netbeans.modules.sql.framework.model.SQLFilter)
         */
        protected SQLFilter createFilter(SQLFilter oldFilter) {
            // XXX Should we only accept SQLFilterImpl.LeftUnary instances for cloning?
            SQLFilter filter = new SQLFilterImpl.RightUnary();

            // set prefix
            if (oldFilter.getPrefix() != null) {
                filter.setPrefix(oldFilter.getPrefix());
            }

            // set operator
            if (oldFilter.getOperator() != null) {
                filter.setOperator(oldFilter.getOperator());
            }

            // set display name
            if (oldFilter.getDisplayName() != null) {
                filter.setDisplayName(oldFilter.getDisplayName());
            }

            try {
                if (oldFilter.getSQLObject(SQLFilter.RIGHT) != null) {
                    filter.addInput(SQLFilter.RIGHT, oldFilter.getSQLObject(SQLFilter.RIGHT));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return filter;
        }

        /**
         * Overrides base implementation to initialize only a single RIGHT input.
         * 
         * @see org.netbeans.modules.sql.framework.model.impl.SQLFilterImpl#initInputs()
         */
        protected void initInputs() {
            SQLInputObject inputObject = new SQLInputObjectImpl(RIGHT, "", null);
            this.inputMap.put(RIGHT, inputObject);
        }
    }

    /* Reference to next filter predicate, if any (for composite predicates) */
    private SQLFilter next = null;

    /** Creates a new default instance of SQLFilter */
    public SQLFilterImpl() {
        super();
        type = SQLConstants.FILTER;
        this.setFilterType(NORMAL);

        initInputs();
    }

    /**
     * Creates a new instance of SQLFilter, parsing the given DOM element to retrieve the
     * contents of the new object.
     * 
     * @param filterElement DOM element containing filter information
     * @throws BaseException if error occurs while parsing
     */
    public SQLFilterImpl(Element filterElement) throws BaseException {
        this();
        parseXML(filterElement);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        SQLFilter filter = createFilter(this);

        SQLFilter tFilter = filter;
        SQLFilter nFilter = this.getNextFilter();
        while (nFilter != null) {
            SQLFilter f = createFilter(nFilter);
            tFilter.setNextFilter(f);
            tFilter = f;
            nFilter = nFilter.getNextFilter();
        }

        return filter;
    }

    /**
     * @see java.lang.Object#equals
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof SQLFilter)) {
            return false;
        }

        SQLFilter filter = (SQLFilter) refObj;

        // check if predicate has same operator
        String myOp = getOperator();
        String refOp = filter.getOperator();
        boolean response = (myOp != null) ? (myOp.equals(refOp)) : (refOp == null);

        if (!response) {
            return response;
        }

        String myPrefix = getPrefix();
        String refPrefix = filter.getPrefix();
        response &= (myPrefix != null) ? (myPrefix.equals(refPrefix)) : (refPrefix == null);

        if (!response) {
            return response;
        }

        // check next
        SQLFilter nFilter = this.getNextFilter();
        SQLFilter refNFilter = filter.getNextFilter();

        response &= (nFilter != null) ? nFilter.equals(refNFilter) : (refNFilter == null);

        if (!response) {
            return response;
        }

        return response && super.equals(refObj);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLFilter#getFilterType()
     */
    public int getFilterType() {
        Integer fType = (Integer) this.getAttributeObject(ATTR_FILTER_TYPE);
        if (fType != null) {
            return fType.intValue();
        }

        return NORMAL;
    }

    /**
     * Gets chained instance of SQLFilter, if any.
     * 
     * @return next SQLFilter, or null if no chained instance exists.
     */
    public SQLFilter getNextFilter() {
        return next;
    }

    /**
     * Gets current operator.
     * 
     * @return operator String
     */
    public String getOperator() {
        return (String) getAttributeObject(ATTR_OPERATOR);
    }

    /**
     * Gets (optional) prefix associated with this filter.
     * 
     * @return prefix string, possibly null
     */
    public String getPrefix() {
        return (String) getAttributeObject(PREFIX);
    }

    /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (getOperator() != null) ? getOperator().hashCode() : 0;
        myHash += (getPrefix() != null) ? getPrefix().hashCode() : 0;

        return myHash;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#isInputValid
     */
    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null) {
            return false;
        }

        switch (input.getObjectType()) {
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:            	
            case SQLConstants.LITERAL:
            case SQLConstants.VISIBLE_LITERAL:
            case SQLConstants.PREDICATE:
            case SQLConstants.CASE:
            case SQLConstants.SOURCE_COLUMN:
            case SQLConstants.TARGET_COLUMN:
            case SQLConstants.COLUMN_REF:
                return true;

            default:
                return false;
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLFilter#isValid()
     */
    public boolean isValid() {
        return (this.getOperator() != null && this.getSQLObject(SQLFilter.LEFT) != null && this.getSQLObject(SQLFilter.RIGHT) != null);
    }

    /**
     * Parses the given XML element to populate content of this instance.
     * 
     * @param xmlElement Element to be parsed
     * @exception BaseException thropwn while parsing
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);

        NodeList list = xmlElement.getElementsByTagName(SQLObject.TAG_INPUT);
        TagParserUtility.parseInputTagList(this, list);

        list = xmlElement.getElementsByTagName(TAG_NEXT);
        parseNextFilterList(list);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.SQLObject#secondPassParse
     */
    public void secondPassParse(Element element) throws BaseException {
        String nodeName = element.getNodeName();
        if (TagParserUtility.TAG_INPUT.equals(nodeName)) {
            TagParserUtility.parseInputTag(this, element);
        } else if (TagParserUtility.TAG_OBJECTREF.equals(nodeName)) {
            parseNextFilter(element);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLFilter#setFilterType(int)
     */
    public void setFilterType(int fType) {
        this.setAttribute(ATTR_FILTER_TYPE, new Integer(fType));
    }

    /**
     * Sets next filter to the given SQLFilter instance.
     * 
     * @param newNext new chained instance of SQLFilter, possibly null
     */
    public void setNextFilter(SQLFilter newNext) {
        next = newNext;
    }

    /**
     * Sets operator to given String.
     * 
     * @param newOperator new operator String
     */
    public void setOperator(String newOperator) {
        setAttribute(ATTR_OPERATOR, newOperator);
    }

    /**
     * Sets (optional) prefix associated with this filter.
     * 
     * @param newPrefix new prefix string, possibly null
     */
    public void setPrefix(String newPrefix) {
        setAttribute(PREFIX, newPrefix);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return GeneratorUtil.getInstance().getEvaluatedString(this);
        } catch (BaseException ignore) {
            return "Unknown";
        }
    }

    /**
     * Overrides parent implementation to append XML elements for any input objects
     * associated with this expression.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    public String toXMLString(String prefix) {
        StringBuilder buf = new StringBuilder();
        if (prefix == null) {
            prefix = "";
        }

        buf.append(prefix).append(getHeader());
        buf.append(super.toXMLAttributeTags(prefix));
        buf.append(TagParserUtility.toXMLInputTag(prefix + "\t", inputMap));

        if (next != null) {
            try {
                String nextRef = TagParserUtility.toXMLObjectRefTag(next, prefix + "\t\t");
                buf.append(prefix + "\t<" + TAG_NEXT + ">\n");
                buf.append(nextRef);
                buf.append(prefix + "\t</" + TAG_NEXT + ">\n");
            } catch (BaseException e) {
                // TODO log this exception
            }
        }

        buf.append(prefix).append(getFooter());

        return buf.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject#visit(org.netbeans.modules.sql.framework.model.visitors.SQLVisitor)
     */
    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a new instance of SQLFilter, cloning the relevant elements of the given
     * SQLFilter. References to SQLObjects that are inputs are copied, but the inputs
     * themselves are not cloned. May be overridden by subclass to control the specific
     * implementation of SQLFilter to be returned or accepted as a template.
     * 
     * @param oldFilter SQLFilter to be cloned
     * @return clone of <code>oldFilter</code>
     */
    protected SQLFilter createFilter(SQLFilter oldFilter) {
        SQLFilter filter = new SQLFilterImpl();

        // set prefix
        if (oldFilter.getPrefix() != null) {
            filter.setPrefix(oldFilter.getPrefix());
        }

        // set operator
        if (oldFilter.getOperator() != null) {
            filter.setOperator(oldFilter.getOperator());
        }

        // set display name
        if (oldFilter.getDisplayName() != null) {
            filter.setDisplayName(oldFilter.getDisplayName());
        }

        try {
            if (oldFilter.getSQLObject(SQLFilter.LEFT) != null) {
                filter.addInput(SQLFilter.LEFT, oldFilter.getSQLObject(SQLFilter.LEFT));
            }

            if (oldFilter.getSQLObject(SQLFilter.RIGHT) != null) {
                filter.addInput(SQLFilter.RIGHT, oldFilter.getSQLObject(SQLFilter.RIGHT));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return filter;
    }

    /**
     * Initialize inputs to this filter. May be overridden by subclasses to alter the
     * initialization behavior.
     */
    protected void initInputs() {
        SQLInputObject inputObject = new SQLInputObjectImpl(LEFT, "", null);
        this.inputMap.put(LEFT, inputObject);

        inputObject = new SQLInputObjectImpl(RIGHT, "", null);
        this.inputMap.put(RIGHT, inputObject);
    }

    /**
     * Reconstitutes filter associated with the given Element and associates it with this
     * instance.
     * 
     * @param elem Element representing associated filter
     * @throws BaseException if error occurs during parsing and reconstitution.
     */
    private void parseNextFilter(Element elem) throws BaseException {
        SQLDefinition defn = TagParserUtility.getAncestralSQLDefinition(this);

        // Only one Element
        if (elem != null) {
            SQLObject refObj = TagParserUtility.parseXMLObjectRefTag(defn, elem);

            // if input is null it may not be parsed yet so
            // do a second parse... This will take for any second parse for
            // SQL objects
            if (refObj == null) {
                defn.addSecondPassSQLObject(this, elem);
            } else {
                try {
                    next = (SQLFilter) refObj;
                } catch (ClassCastException e) {
                    throw new BaseException("Caught invalid object type in lieu of SQLFilter.", e);
                }
            }
        }
    }

    /**
     * Parses and reconstitutes filters associated with this filter, as defined in the
     * given NodeList.
     * 
     * @param list NodeList of elements representing filters that are associated with this
     *        filter.
     * @throws BaseException if error occurs during parsing and reconstitution
     */
    private void parseNextFilterList(NodeList list) throws BaseException {
        for (int i = 0; i < list.getLength(); i++) {
            Node aNode = list.item(i);
            if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) aNode;
                if (TAG_NEXT.equals(aNode.getNodeName())) {
                    NodeList objRefList = elem.getElementsByTagName(TagParserUtility.TAG_OBJECTREF);
                    if (objRefList != null && objRefList.getLength() != 0) {
                        Element objRefElement = (Element) objRefList.item(0);
                        parseNextFilter(objRefElement);
                    }
                    break; // Only one next tag allowed.
                }
            }
        }
    }
}

