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
package org.netbeans.modules.sql.framework.model;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.model.utils.OperatorUtil;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;
import org.netbeans.modules.etl.logger.Localizer;


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
    private static transient final Localizer mLoc = Localizer.get();
    private static final String OBJECT_TYPE_CLASS = "class";
    private static final String OBJECT_TYPE_CLASS_TAG = "objectTypeClass";
    private static final String OBJECT_TYPE_MAP_FILE = "org/netbeans/modules/sql/framework/model/impl/objectTypeMap.xml";
    private static final String OBJECT_TYPE_NAME = "name";
    private static Map objectTagToTypeMap = new HashMap();
    private static final String USER_FUNCTION_ID = "userFx";
    

    static {
        init();
    }

    /**
     * Creates a new SQLObject instance of the type represented by the given tag name.
     * Does not add the the vended SQLObject to a SQLDefinitionImpl instance. To correctly
     * associate the returned SQLObject instance with a SQLDefinitionImpl instance, the
     * calling method should call SQLDefinitionImpl.addSQLObject(SQLObject).
     * 
     * @param objTag objTag of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see org.netbeans.modules.sql.framework.model#addSQLObject(SQLObject)
     */
    public static SQLObject createObjectForTag(String objTag) throws BaseException {
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
    public static SQLOperator createOperatorFromParsedList(String dbSpName, List args) throws BaseException {
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
        throw new BaseException("Cannot locate definition for operator " + opName);
    }

    /**
     * Creates a new SQLObject instance of the given type. Does not add the the vended
     * SQLObject to a SQLDefinitionImpl instance. To correctly associate the returned
     * SQLObject instance with a SQLDefinitionImpl instance, the calling method should
     * call SQLDefinitionImpl.addSQLObject(SQLObject).
     * 
     * @param className className of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see org.netbeans.modules.sql.framework.model#addSQLObject(SQLObject)
     */
    public static SQLObject createSQLObject(String className) throws BaseException {
        SQLObject obj = null;

        if (className != null) {
            try {
                Class sqlObject = Class.forName(className);
                obj = (SQLObject) sqlObject.newInstance();
            } catch (ClassNotFoundException e) {
                throw new BaseException("Cannot create object of class " + className);
            } catch (ClassCastException e1) {
                throw new BaseException("Cannot create object of class " + className);
            } catch (InstantiationException e2) {
                throw new BaseException("Cannot create object of class " + className);
            } catch (IllegalAccessException e3) {
                throw new BaseException("Cannot create object of class " + className);
            }
        }

        return obj;
    }

    public static SQLObject createSQLObjectForElement(Object parent, Element element) throws BaseException {
        SQLObject sqlObj = null;
        String objType = element.getAttribute(SQLObject.OBJECT_TYPE);
        if (objType == null) {
            throw new BaseException(SQLObject.OBJECT_TYPE + " attribute not found for element: " + element.getNodeName());
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

    /**
     * Creates a new SQLObject representing a SQLOperator implementation, using the given
     * Element for initialization. Does not add the the vended SQLObject to a
     * SQLDefinitionImpl instance. To correctly associate the returned SQLObject instance
     * with a SQLDefinitionImpl instance, the calling method should call
     * SQLDefinition.addSQLObject(SQLObject).
     * 
     * @param element XML Element containing initialization information for the new
     *        function
     * @return new SQLObject instance representing a SQLOperator implementation
     * @throws BaseException if error occurs during creation
     * @see org.netbeans.modules.sql.framework.model#addSQLObject(SQLObject)
     */
    private static SQLObject createSQLOperator(Element element) throws BaseException {
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
            throw new BaseException(SQLOperator.ATTR_SCRIPTREF + " attribute not found for element: " + element.getNodeName());
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
            mLogger.errorNoloc(mLoc.t("EDIT136: Failed to load the Object Map{0}", LOG_CATEGORY), e);
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
    private static void replaceVisibleLiteralsForStaticFields(List operatorArgs, IOperatorXmlInfo operatorXml) throws BaseException {
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

