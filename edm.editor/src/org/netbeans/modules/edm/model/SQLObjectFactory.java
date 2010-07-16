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
package org.netbeans.modules.edm.model;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.codegen.SQLOperatorFactory;
import org.netbeans.modules.edm.editor.utils.OperatorUtil;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorField;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.openide.util.NbBundle;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.netbeans.modules.edm.editor.utils.XmlUtil;


/**
 * Singleton object factory for SQL objects.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLObjectFactory {

    /* Log4J category string */
    private static final String LOG_CATEGORY = SQLObjectFactory.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLObjectFactory.class.getName());
    private static final String OBJECT_TYPE_CLASS = "class";
    private static final String OBJECT_TYPE_CLASS_TAG = "objectTypeClass";
    private static final String OBJECT_TYPE_MAP_FILE = "org/netbeans/modules/edm/model/impl/objectTypeMap.xml";
    private static final String OBJECT_TYPE_NAME = "name";
    private static Map objectTagToTypeMap = new HashMap();
    private static final String USER_FUNCTION_ID = "userFx";
    

    static {
        init();
    }


    public static SQLObject createObjectForTag(String objTag) throws EDMException {
        String className = (String) objectTagToTypeMap.get(objTag);
        SQLObject obj = createSQLObject(className);
        return obj;
    }

    /**
     * Creates a SQLOperator instance of the given operator name and using the give List
     * of SQLObjects as argument inputs. This method should only be called from
     * SQLConditionParser.
     * 
     * @param dbSpName name of parsed DB operator to be created
     * @param args List of SQLObjects representing parsed argument inputs
     */
    public static SQLOperator createOperatorFromParsedList(String dbSpName, List args) throws EDMException {
        boolean userFunction = false;
        String userFunctionName = dbSpName;
        if ("STANDARDIZE".equalsIgnoreCase(dbSpName)) {
            SQLLiteral literal = (SQLLiteral) args.get(0);
            String arg0 = literal.getValue();
            dbSpName = dbSpName + "_" + arg0;
        }

        // first try all lower case
        String cdbSpName = dbSpName.toLowerCase();
        SQLOperatorDefinition operatorDefinition = SQLOperatorFactory.getDefault().getDbSpecficOperatorDefinition(cdbSpName);
        if (operatorDefinition == null) {
            // now try upper case
            cdbSpName = dbSpName.toUpperCase();
            operatorDefinition = SQLOperatorFactory.getDefault().getDbSpecficOperatorDefinition(cdbSpName);
            // if it is still null then throw exception
            if (operatorDefinition == null) {
                operatorDefinition = SQLOperatorFactory.getDefault().getDbSpecficOperatorDefinition(dbSpName);
                userFunction = true;
            }
        }

        // set IOperatorXmlInfo
        String opName = userFunction ? USER_FUNCTION_ID : operatorDefinition.getOperatorName();
        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(opName);
        if (operatorXml != null) {
            SQLOperator operator = (SQLOperator) createSQLObject(operatorXml.getObjectClassName());
            operator.setDisplayName(opName);
            operator.setDbSpecificOperator(userFunction ? USER_FUNCTION_ID : operatorDefinition.getDbSpecficName());
            if (userFunction) {
                operator.setCustomOperatorName(userFunctionName);
                operator.setOperatorXmlInfo(operatorXml);
            }

            replaceVisibleLiteralsForStaticFields(args, operatorXml);
            operator.setArguments(args);
            return operator;
        }
        throw new EDMException(NbBundle.getMessage(SQLObjectFactory.class, "ERROR_Cannot_locate_definition") + opName);
    }


    public static SQLObject createSQLObject(String className) throws EDMException {
        SQLObject obj = null;

        if (className != null) {
            try {
                Class sqlObject = Class.forName(className);
                obj = (SQLObject) sqlObject.newInstance();
            } catch (ClassNotFoundException e) {
                throw new EDMException(NbBundle.getMessage(SQLObjectFactory.class, "ERROR_Cannot_create_object_of_class") + className);
            } catch (ClassCastException e1) {
                throw new EDMException(NbBundle.getMessage(SQLObjectFactory.class, "ERROR_Cannot_create_object_of_class") + className);
            } catch (InstantiationException e2) {
                throw new EDMException(NbBundle.getMessage(SQLObjectFactory.class, "ERROR_Cannot_create_object_of_class") + className);
            } catch (IllegalAccessException e3) {
                throw new EDMException(NbBundle.getMessage(SQLObjectFactory.class, "ERROR_Cannot_create_object_of_class") + className);
            }
        }

        return obj;
    }

    public static SQLObject createSQLObjectForElement(Object parent, Element element) throws EDMException {
        SQLObject sqlObj = null;
        String objType = element.getAttribute(SQLObject.OBJECT_TYPE);
        if (objType == null) {
            throw new EDMException(SQLObject.OBJECT_TYPE + NbBundle.getMessage(SQLObjectFactory.class, "ERROR_attribute_not_found") + element.getNodeName());
        }

        int objectType = TagParserUtility.getIntType(objType);
        if ((SQLConstants.GENERIC_OPERATOR == objectType) || (SQLConstants.VISIBLE_PREDICATE == objectType) || (SQLConstants.CUSTOM_OPERATOR == objectType)) {
            sqlObj = createSQLOperator(element);
        } else {
            sqlObj = createObjectForTag(objType);
        }

        if (sqlObj != null) {
            sqlObj.setParentObject(parent);
            sqlObj.parseXML(element);
        }
        return sqlObj;
    }


    private static SQLObject createSQLOperator(Element element) throws EDMException {
        NodeList list = element.getChildNodes();
        String script = null;
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (!Attribute.TAG_ATTR.equals(node.getNodeName())) {
                continue;
            }

            Element attrElem = (Element) node;
            Attribute attr = TagParserUtility.parseAttribute(attrElem);

            if (SQLOperator.ATTR_SCRIPTREF.equals(attr.getAttributeName())) {
                script = (String) attr.getAttributeValue();
                break;
            }
        }

        if (script == null) {
            throw new EDMException(SQLOperator.ATTR_SCRIPTREF + NbBundle.getMessage(SQLObjectFactory.class, "ERROR_attribute_not_found") + element.getNodeName());
        }

        IOperatorXmlInfo operatorXml = OperatorUtil.findOperatorXmlInfo(script);
        SQLOperator operator = null;
        if (operatorXml != null) {
            operator = (SQLOperator) createSQLObject(operatorXml.getObjectClassName());
        }

        return operator;
    }

    private static void init() {
        try {
            Element elem = XmlUtil.loadXMLFile(OBJECT_TYPE_MAP_FILE);
            NodeList objMap = elem.getElementsByTagName(OBJECT_TYPE_CLASS_TAG);
            parseObjectMap(objMap);
        } catch (Exception e) {
            mLogger.log(Level.INFO,NbBundle.getMessage(SQLObjectFactory.class, "LOG.INFO_Failed_to_load_the_Object_Map",new Object[] {LOG_CATEGORY}),e);
        }
    }

    private static void parseObjectMap(NodeList objMap) {
        for (int i = 0; i < objMap.getLength(); i++) {
            Element elem = (Element) objMap.item(i);
            String objType = elem.getAttribute(OBJECT_TYPE_NAME);
            String objClass = elem.getAttribute(OBJECT_TYPE_CLASS);

            if (objType != null && objClass != null) {
                objectTagToTypeMap.put(objType, objClass);
            }
        }
    }

    /**
     * Substitutes SQLLiteralImpl instances for VisibleSQLLiteralImpl instances in the
     * given List of SQLObjects, based on whether a field is declared as static in the
     * given IOperatorXmlInfo class. (Static fields do not accept inputs from other canvas
     * objects; their values are set via Swing controls such as combo boxes and/or text
     * fields which are embedded in the canvas object.)
     * 
     * @param operatorArgs List containing SQLObjects which represent operator inputs to
     *        be evaluated and replaced if necessary
     * @param operatorXml IOperatorXmlInfo instance
     */
    private static void replaceVisibleLiteralsForStaticFields(List operatorArgs, IOperatorXmlInfo operatorXml) throws EDMException {
        List inputFields = operatorXml.getInputFields();
        if (operatorArgs != null && inputFields != null && operatorArgs.size() == inputFields.size()) {
            ListIterator it = inputFields.listIterator();

            while (it.hasNext()) {
                IOperatorField fieldInfo = (IOperatorField) it.next();
                int argIdx = it.previousIndex();

                SQLObject argValue = (SQLObject) operatorArgs.get(argIdx);
                if (fieldInfo.isStatic() && argValue instanceof VisibleSQLLiteral) {
                    VisibleSQLLiteral vlit = (VisibleSQLLiteral) argValue;
                    SQLLiteral replacement = SQLModelObjectFactory.getInstance().createSQLLiteral(vlit.getDisplayName(), vlit.getValue(),
                            vlit.getJdbcType());
                    operatorArgs.set(argIdx, replacement);
                }
            }
        }
    }

    private SQLObjectFactory() {
    }
}

