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
package org.netbeans.modules.sql.framework.common.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLContainerObject;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;

/**
 * Utility class for reading and writing some mundane objects like SQLObjects
 * 
 * @author Ritesh Adval
 * @author Sudhi Seshachala
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class TagParserUtility {

    public static final String ATTR_REFID = "refId";
    public static final String ATTR_TYPE = "type";
    public static final String TAG_INPUT = "input";
    public static final String TAG_OBJECTREF = "objectRef";

    /**
     * Gets ancestral SQLDefinition instance for the given SQLExpresionObject.
     * 
     * @param sqlObj SQLConnectableObject whose ancestor SQLDefinition is sought
     * @return SQLDefinition instance
     * @throws BaseException if ancestral SQLDefinition could not be located
     */
    public static SQLDefinition getAncestralSQLDefinition(SQLObject sqlObj) {
        return SQLObjectUtil.getAncestralSQLDefinition(sqlObj);
    }

    /**
     * Gets the displayable representation of SQLObject type for given int type.
     * 
     * @param type int representation of SQLObject type
     * @return String representation of type
     * @throws BaseException thrown while getting the type
     */
    public static String getDisplayStringFor(int type) throws BaseException {
        switch (type) {
            case SQLConstants.GENERIC_OPERATOR:
                return SQLConstants.DISPLAY_STR_GENERIC_OPERATOR;

            case SQLConstants.CAST_OPERATOR:
                return SQLConstants.DISPLAY_STR_CAST_OPERATOR;

            case SQLConstants.DATE_DIFF_OPERATOR:
                return SQLConstants.DISPLAY_STR_DATEDIFF_OPERATOR;

            case SQLConstants.DATE_ADD_OPERATOR:
                return SQLConstants.DISPLAY_STR_DATEADD_OPERATOR;

            case SQLConstants.RUNTIME_ARGS:
                return SQLConstants.DISPLAY_RUNTIME_ARGS;

            case SQLConstants.JOIN:
                return SQLConstants.DISPLAY_STR_JOIN_OPERATOR;

            case SQLConstants.LITERAL:
            case SQLConstants.VISIBLE_LITERAL:
                return SQLConstants.DISPLAY_STR_LITERAL_OPERATOR;

            case SQLConstants.CASE:
                return SQLConstants.DISPLAY_STR_CASE_OPERATOR;

            case SQLConstants.PREDICATE:
            case SQLConstants.VISIBLE_PREDICATE:
                return SQLConstants.DISPLAY_STR_PREDICATE;

            case SQLConstants.WHEN:
                return SQLConstants.DISPLAY_STR_WHEN;

            case SQLConstants.SOURCE_COLUMN:
                return SQLConstants.DISPLAY_STR_SOURCE_COLUMN;

            case SQLConstants.TARGET_COLUMN:
                return SQLConstants.DISPLAY_STR_TARGET_COLUMN;

            case SQLConstants.TARGET_DBMODEL:
                return SQLConstants.DISPLAY_STR_TARGET_DBMODEL;

            case SQLConstants.SOURCE_DBMODEL:
                return SQLConstants.DISPLAY_STR_SOURCE_DBMODEL;

            case SQLConstants.SOURCE_TABLE:
                return SQLConstants.DISPLAY_STR_SOURCE_TABLE;

            case SQLConstants.TARGET_TABLE:
                return SQLConstants.DISPLAY_STR_TARGET_TABLE;

            case SQLConstants.FILTER:
                return SQLConstants.DISPLAY_STR_FILTER;

            case SQLConstants.RUNTIME_DBMODEL:
                return SQLConstants.DISPLAY_STR_RUNTIME_DBMODEL;

            case SQLConstants.RUNTIME_INPUT:
                return SQLConstants.DISPLAY_STR_RUNTIME_INPUT;

            case SQLConstants.RUNTIME_OUTPUT:
                return SQLConstants.DISPLAY_STR_RUNTIME_OUTPUT;

            case SQLConstants.COLUMN_REF:
                return SQLConstants.DISPLAY_STR_COLUMN_REF;

            case SQLConstants.JOIN_VIEW:
                return SQLConstants.DISPLAY_STR_JOIN_VIEW;

            case SQLConstants.JOIN_TABLE:
                return SQLConstants.DISPLAY_STR_JOIN_TABLE;

            case SQLConstants.JOIN_TABLE_COLUMN:
                return SQLConstants.DISPLAY_STR_JOIN_TABLE_COLUMN;

            case SQLConstants.CUSTOM_OPERATOR:
                return SQLConstants.DISPLAY_STR_USER_FUNCTION;

            default:
                throw new BaseException("Undefined SQLObject type: " + type);
        }
    }

    /**
     * Gets the int representation for given SQLObject string type.
     * 
     * @param type String representation of SQLObject type
     * @return int representation of type
     * @throws BaseException thrown while getting the type
     */
    public static int getIntType(String type) throws BaseException {
        if (type.equals(SQLConstants.STR_GENERIC_OPERATOR) || type.equals(SQLConstants.STR_SCALAR_OPERATOR)) {
            return SQLConstants.GENERIC_OPERATOR;
        } else if (type.equals(SQLConstants.STR_CAST_OPERATOR)) {
            return SQLConstants.CAST_OPERATOR;
        } else if (type.equals(SQLConstants.STR_DATEDIFF_OPERATOR)) {
            return SQLConstants.DATE_DIFF_OPERATOR;
        } else if (type.equals(SQLConstants.STR_DATEADD_OPERATOR)) {
            return SQLConstants.DATE_ADD_OPERATOR;
        } else if (type.equals(SQLConstants.STR_RUNTIME_ARGS)) {
            return SQLConstants.RUNTIME_ARGS;
        } else if (type.equals(SQLConstants.STR_JOIN_OPERATOR)) {
            return SQLConstants.JOIN;
        } else if (type.equals(SQLConstants.STR_CASE_OPERATOR)) {
            return SQLConstants.CASE;
        } else if (type.equals(SQLConstants.STR_LITERAL_OPERATOR)) {
            return SQLConstants.LITERAL;
        } else if (type.equals(SQLConstants.STR_PREDICATE)) {
            return SQLConstants.PREDICATE;
        } else if (type.equals(SQLConstants.STR_WHEN)) {
            return SQLConstants.WHEN;
        } else if (type.equals(SQLConstants.STR_SOURCE_TABLE)) {
            return SQLConstants.SOURCE_TABLE;
        } else if (type.equals(SQLConstants.STR_TARGET_TABLE)) {
            return SQLConstants.TARGET_TABLE;
        } else if (type.equals(SQLConstants.STR_SOURCE_COLUMN)) {
            return SQLConstants.SOURCE_COLUMN;
        } else if (type.equals(SQLConstants.STR_TARGET_COLUMN)) {
            return SQLConstants.TARGET_COLUMN;
        } else if (type.equals(SQLConstants.STR_VISIBLE_PREDICATE)) {
            return SQLConstants.VISIBLE_PREDICATE;
        } else if (type.equals(SQLConstants.STR_FILTER)) {
            return SQLConstants.FILTER;
        } else if (type.equals(SQLConstants.STR_VISIBLE_LITERAL)) {
            return SQLConstants.VISIBLE_LITERAL;
        } else if (type.equals(SQLConstants.STR_RUNTIME_DBMODEL)) {
            return SQLConstants.RUNTIME_DBMODEL;
        } else if (type.equals(SQLConstants.STR_RUNTIME_INPUT)) {
            return SQLConstants.RUNTIME_INPUT;
        } else if (type.equals(SQLConstants.STR_RUNTIME_OUTPUT)) {
            return SQLConstants.RUNTIME_OUTPUT;
        } else if (type.equals(SQLConstants.STR_COLUMN_REF)) {
            return SQLConstants.COLUMN_REF;
        } else if (type.equals(SQLConstants.STR_JOIN_VIEW)) {
            return SQLConstants.JOIN_VIEW;
        } else if (type.equals(SQLConstants.STR_JOIN_TABLE)) {
            return SQLConstants.JOIN_TABLE;
        } else if (type.equals(SQLConstants.STR_JOIN_TABLE_COLUMN)) {
            return SQLConstants.JOIN_TABLE_COLUMN;
        } else if (type.equals(SQLConstants.STR_CUSTOM_OPERATOR)) {
            return SQLConstants.CUSTOM_OPERATOR;
        } else {
            throw new BaseException("Failed to get Int type for '" + type + "'");
        }
    }

    /**
     * Returns all the attributes name value of Node in a Map.
     * 
     * @param node
     * @return map containing name/value of all Node attributes.
     */
    public static Map getNodeAttributes(Node node) {
        Map ret = new HashMap();
        NamedNodeMap nnm = node.getAttributes();
        Node attNode = null;
        int length = nnm.getLength();

        for (int i = 0; i < length; i++) {
            attNode = nnm.item(i);
            ret.put(attNode.getNodeName(), attNode.getNodeValue());
        }

        return ret;
    }

    /**
     * Returns node attribute value
     * 
     * @param node
     * @param attName
     * @return value of the attribute
     */
    public static String getNodeAttributeValue(Node node, String attName) {
        String ret = null;
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node att = nnm.getNamedItem(attName);
            if (att != null) {
                ret = att.getNodeValue();
            }
        }
        return ret;
    }

    /**
     * Gets the String representation of SQLObject type for given int type.
     * 
     * @param type int representation of SQLObject type
     * @return String representation of type
     * @throws BaseException thrown while getting the type
     */
    public static String getStringType(int type) throws BaseException {
        switch (type) {
            case SQLConstants.GENERIC_OPERATOR:
                return SQLConstants.STR_GENERIC_OPERATOR;

            case SQLConstants.CAST_OPERATOR:
                return SQLConstants.STR_CAST_OPERATOR;

            case SQLConstants.DATE_DIFF_OPERATOR:
                return SQLConstants.STR_DATEDIFF_OPERATOR;

            case SQLConstants.DATE_ADD_OPERATOR:
                return SQLConstants.STR_DATEADD_OPERATOR;

            case SQLConstants.RUNTIME_ARGS:
                return SQLConstants.STR_RUNTIME_ARGS;

            case SQLConstants.JOIN:
                return SQLConstants.STR_JOIN_OPERATOR;

            case SQLConstants.LITERAL:
                return SQLConstants.STR_LITERAL_OPERATOR;

            case SQLConstants.CASE:
                return SQLConstants.STR_CASE_OPERATOR;

            case SQLConstants.PREDICATE:
                return SQLConstants.STR_PREDICATE;

            case SQLConstants.WHEN:
                return SQLConstants.STR_WHEN;

            case SQLConstants.SOURCE_COLUMN:
                return SQLConstants.STR_SOURCE_COLUMN;

            case SQLConstants.TARGET_COLUMN:
                return SQLConstants.STR_TARGET_COLUMN;

            case SQLConstants.TARGET_DBMODEL:
                return SQLConstants.STR_TARGET_DBMODEL;

            case SQLConstants.SOURCE_DBMODEL:
                return SQLConstants.STR_SOURCE_DBMODEL;

            case SQLConstants.SOURCE_TABLE:
                return SQLConstants.STR_SOURCE_TABLE;

            case SQLConstants.TARGET_TABLE:
                return SQLConstants.STR_TARGET_TABLE;

            case SQLConstants.VISIBLE_PREDICATE:
                return SQLConstants.STR_VISIBLE_PREDICATE;

            case SQLConstants.FILTER:
                return SQLConstants.STR_FILTER;

            case SQLConstants.VISIBLE_LITERAL:
                return SQLConstants.STR_VISIBLE_LITERAL;

            case SQLConstants.RUNTIME_DBMODEL:
                return SQLConstants.STR_RUNTIME_DBMODEL;

            case SQLConstants.RUNTIME_INPUT:
                return SQLConstants.STR_RUNTIME_INPUT;

            case SQLConstants.RUNTIME_OUTPUT:
                return SQLConstants.STR_RUNTIME_OUTPUT;

            case SQLConstants.COLUMN_REF:
                return SQLConstants.STR_COLUMN_REF;

            case SQLConstants.JOIN_VIEW:
                return SQLConstants.STR_JOIN_VIEW;

            case SQLConstants.JOIN_TABLE:
                return SQLConstants.STR_JOIN_TABLE;

            case SQLConstants.JOIN_TABLE_COLUMN:
                return SQLConstants.STR_JOIN_TABLE_COLUMN;

            case SQLConstants.CUSTOM_OPERATOR:
                return SQLConstants.STR_CUSTOM_OPERATOR;

            default:
                throw new BaseException("Undefined Operator Type" + type);
        }
    }

    public static Attribute parseAttribute(Element elem) throws BaseException {
        if (elem == null) {
            throw new IllegalArgumentException("Attribute element can not be null.");
        }

        if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
            Attribute attr = new Attribute();
            attr.parseXMLString(elem);
            return attr;
        }
        throw new IllegalArgumentException("Element is not an attribute element.");
    }

    public static void parseAttributeList(Map attributes, NodeList list) throws BaseException {
        if (attributes == null) {
            throw new IllegalArgumentException("Attribute map can not be null");
        }

        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) list.item(i);
                if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
                    Attribute attr = new Attribute();
                    attr.parseXMLString(elem);
                    attributes.put(attr.getAttributeName(), attr);
                }
            }
        }
    }

    /**
     * Parses list of Nodes possibly containing input elements.
     * 
     * @param sqlObj SQLConnectableObject to contain the parsed input objects
     * @param inputArgList NodeList with possible input elements
     * @throws BaseException if error occurs while parsing
     */
    public static void parseInputChildNodes(SQLConnectableObject sqlObj, NodeList inputArgList) throws BaseException {
        for (int i = 0; i < inputArgList.getLength(); i++) {
            if (inputArgList.item(i).getNodeType() == Node.ELEMENT_NODE && inputArgList.item(i).getNodeName().equals(TAG_INPUT)) {
                Element elem = (Element) inputArgList.item(i);
                parseInputTag(sqlObj, elem);
            }
        }
    }

    /**
     * Parser the <input>tag . This should be called from any SQLObject which has input
     * tag
     * 
     * @param sqlObj to be parsed
     * @param elem Element
     * @throws BaseException if error occurs while parsing
     */
    public static void parseInputTag(SQLConnectableObject sqlObj, Element elem) throws BaseException {
        if (elem != null) {
            String argName = elem.getAttribute(SQLInputObject.ATTR_ARGNAME);

            // Recurse ancestor list to obtain parent SQLDefinition.
            SQLContainerObject defn = null;
            SQLObject loopObj = sqlObj;
            do {
                Object parentObject = loopObj.getParentObject();
                if (parentObject instanceof SQLContainerObject) {
                    defn = (SQLContainerObject) parentObject;
                    break;
                } else if (parentObject == null) {
                    throw new BaseException("Could not locate root SQLDefinition instance!");
                }
                loopObj = (SQLObject) parentObject;
            } while (true);

            NodeList objRefList = elem.getElementsByTagName(TAG_OBJECTREF);

            // Only one Element
            if (objRefList != null && objRefList.getLength() != 0) {
                Element objRefElement = (Element) objRefList.item(0);
                if (objRefElement != null) {
                    SQLObject refObj = parseXMLObjectRefTag(defn, objRefElement);

                    // If input is null it may not be parsed yet so do a second parse...
                    // This will take for any second parse for SQL objects
                    if (refObj == null) {
                        // QAI#: 85425 to maintain order of the argument of var-argument
                        // operators, create a placeholder for this argument in the
                        // inputmap.
                        if (sqlObj.getInputObjectMap().get(argName) == null) {
                            sqlObj.getInputObjectMap().put(argName, null);
                        }
                        defn.addSecondPassSQLObject(sqlObj, elem);
                    } else {
                        sqlObj.addInput(argName, refObj);
                    }
                }
            } else {
                // If input does not have an object ref then it must be a part of object
                NodeList objList = elem.getElementsByTagName(SQLObject.TAG_SQLOBJECT);
                if (objList != null && objList.getLength() != 0) {
                    Element objElement = (Element) objList.item(0);
                    if (objElement != null) {
                        SQLObject partObj = SQLObjectFactory.createSQLObjectForElement(sqlObj, objElement);

                        if (partObj != null) {
                            sqlObj.addInput(argName, partObj);
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the <input>tag . This should be called from any SQLObject which has input
     * tag
     * 
     * @param sqlObj to be parsed
     * @param inputArgList NodeList
     * @throws BaseException if error occurs while parsing
     */
    public static void parseInputTagList(SQLConnectableObject sqlObj, NodeList inputArgList) throws BaseException {
        for (int i = 0; i < inputArgList.getLength(); i++) {
            Element elem = (Element) inputArgList.item(i);
            parseInputTag(sqlObj, elem);
        }
    }

    /**
     * Reads in and resolves, if possible, from the given SQLDefinition instance the
     * reference to an SQLObject contained in the given XML Element.
     * 
     * @param definition definition from which to attempt to resolve the SQLObject
     *        reference
     * @param xmlElement Element representing the SQLObject reference
     * @return SQLObject referenced by xmlElement; null if object has not yet been parsed
     *         and registered with definition.
     * @throws BaseException if errors occur during parsing
     */
    public static SQLObject parseXMLObjectRefTag(SQLContainerObject definition, Element xmlElement) throws BaseException {
        if (definition == null) {
            throw new BaseException("Must supply non-null SQLDefinition ref for param 'definition'.");
        }

        if (xmlElement == null) {
            throw new BaseException("Must supply non-null Element ref for param 'xmlElement'.");
        }

        String refIdValue = xmlElement.getAttribute(ATTR_REFID);
        String type = xmlElement.getAttribute(ATTR_TYPE);

        // Now get SQLObject for this refId
        return definition.getObject(refIdValue, TagParserUtility.getIntType(type));
    }

    /**
     * Generates XML elements representing this object's associated attributes.
     * 
     * @param prefix Prefix string to be prepended to each element
     * @return String containing XML representation of attributes
     */
    public static String toXMLAttributeTags(Map attributes, String prefix) {
        StringBuilder buf = new StringBuilder(100);

        Iterator iter = attributes.values().iterator();
        while (iter.hasNext()) {
            Attribute attr = (Attribute) iter.next();
            if (attr.getAttributeValue() != null) {
                buf.append(attr.toXMLString(prefix + "\t"));
            }
        }

        return buf.toString();
    }

    /**
     * Writer for <input>tag. This should be called from any SQLObject which needs to
     * write out input child elements from a List.
     * 
     * @param prefix Prefix string to be appended to each line of the generated XML
     *        document
     * @param inputs List of SQLInputObject instances to be written out
     * @return XML element containing input reference information
     */
    public static String toXMLInputTag(String prefix, List inputs) throws BaseException {
        Iterator it = inputs.iterator();
        StringBuilder buffer = new StringBuilder();

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            buffer.append(obj.toXMLString(prefix));
        }

        return buffer.toString();
    }

    /**
     * Writer for <input>tag. This should be called from any SQLObject which needs to
     * write out input child elements from a Map.
     * 
     * @param prefix Prefix string to be appended to each line of the generated XML
     *        document
     * @param inputs Map of SQLInputObject instances to be written out
     * @return XML element containing references to SQLObjects as contained in inputs
     */
    public static String toXMLInputTag(String prefix, Map inputs) {
        Iterator it = inputs.values().iterator();
        StringBuilder buffer = new StringBuilder();

        while (it.hasNext()) {
            SQLInputObject obj = (SQLInputObject) it.next();
            if (obj != null && obj.getSQLObject() != null) {
                buffer.append(obj.toXMLString(prefix));
            }
        }

        return buffer.toString();
    }

    /**
     * Writes out an XML document element representing a reference to the given SQLObject,
     * using the given string as a prefix for each output line.
     * 
     * @param object SQLObject to be referenced in the XML element
     * @param prefix String to prepend to each new line in the String
     * @return XML document element referencing object
     * @throws BaseException if object is null or errors occur during writing
     */
    public static String toXMLObjectRefTag(SQLObject object, String prefix) throws BaseException {
        if (object == null) {
            throw new BaseException("Must supply non-null SQLObject ref for param 'object'.");
        }

        StringBuilder xml = new StringBuilder(prefix);

        xml.append("<" + TAG_OBJECTREF + " ");
        xml.append(ATTR_REFID + "=\"" + object.getId() + "\" ");
        xml.append(ATTR_TYPE + "=\"");

        xml.append(TagParserUtility.getStringType(object.getObjectType())).append("\" />\n");

        return xml.toString();
    }

    /*
     * Creates a new instance of TagParserUtility. No-arg and private as this is a static
     * utility class.
     */
    private TagParserUtility() {
    }
}

