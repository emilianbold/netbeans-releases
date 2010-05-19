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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Attribute;
import org.netbeans.modules.etl.logger.Localizer;

/**
 * This class represents the condition set at source table, target table and at each case
 * when condition. This holds predicates used in this condition and columnref used in it
 * 
 * @author Ritesh Adval
 */
public class SQLConditionImpl implements SQLCondition, Cloneable {

    private static transient final Logger mLogger = Logger.getLogger(SQLConditionImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class SecondParseObjectInfo {

        private Element mElm;
        private SQLObject mObj;

        SecondParseObjectInfo(SQLObject obj, Element elm) {
            this.mObj = obj;
            this.mElm = elm;
        }

        public Element getElement() {
            return mElm;
        }

        public SQLObject getSQLObject() {
            return mObj;
        }
    }
    private static final String GUIMODE = "guiMode";
    private static final String LOG_CATEGORY = SQLConditionImpl.class.getName();
    private static final String PARENTHESIS = "parenthesis";
    /**
     * Map of attributes; used by concrete implementations to store class-specific fields
     * without hardcoding them as member variables
     */
    private Map attributes = new HashMap();
    private String conditionText;
    private String dispName;
    private GUIInfo guiInfo = new GUIInfo();
    private ArrayList objectList;
    private Object parent;
    private SQLPredicate root;
    private transient List secondPassList = new ArrayList();

    public SQLConditionImpl(SQLCondition src) throws BaseException {
        this(src.getDisplayName());
        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null SQLCondition instance for src param.");
        }

        try {
            copyFrom(src);
        } catch (Exception ex) {
            throw new BaseException("can not create SQLConditionImpl using copy constructor", ex);
        }
    }

    /** Creates a new instance of SQLCondition */
    public SQLConditionImpl(String displayName) {
        objectList = new ArrayList();
        this.setDisplayName(displayName);
    }

    /**
     * Adds equality predicate to the condition. If a different predicate already exists
     * then it is linked with "AND" predicator. Ex: 1) col = input 2) (T1.EMP_ID =
     * S1.EMP_ID) AND (T1.LAST_NAME = S2.LAST_NAME)
     * 
     * @param input
     * @param col
     */
    public void addEqualityPredicate(SQLObject input, SQLDBColumn col) throws BaseException {
        ColumnRefImpl leftCRef = new ColumnRefImpl(col);
        ColumnRefImpl rightCRef = new ColumnRefImpl((SQLDBColumn) input);

        VisibleSQLPredicate predicate = SQLModelObjectFactory.getInstance().createVisibleSQLPredicate();
        predicate.setOperatorType(SQLConstants.OPERATOR_STR_EQUAL);
        predicate.addInput(SQLPredicate.LEFT, leftCRef);
        predicate.addInput(SQLPredicate.RIGHT, rightCRef);

        if (objectList.size() == 0) {
            add(leftCRef);
            add(rightCRef);
            add(predicate);
        } else {
            SQLObject exPredicate = getMatchingSQLObject(predicate, PARENTHESIS);
            SQLObject exLeftCRef = isObjectExist(leftCRef);
            SQLObject exRightCRef = isObjectExist(rightCRef);

            SQLPredicate rootP = getRootPredicate();

            if (exPredicate == null) {
                addObject(predicate);

                if (exLeftCRef == null) {
                    addObject(leftCRef);
                } else {
                    predicate.removeInputByArgName(SQLPredicate.LEFT, leftCRef);
                    predicate.addInput(SQLPredicate.LEFT, exLeftCRef);
                }

                if (exRightCRef == null) {
                    addObject(rightCRef);
                } else {
                    predicate.removeInputByArgName(SQLPredicate.RIGHT, rightCRef);
                    predicate.addInput(SQLPredicate.RIGHT, exRightCRef);
                }

                // By default create an 'and' predicate and add. This newly created
                // predicate with existing root predicate in SQLCondition
                if (rootP != null) {
                    VisibleSQLPredicate andP = SQLModelObjectFactory.getInstance().createVisibleSQLPredicate();
                    andP.setOperatorType("AND");
                    andP.addInput(SQLPredicate.LEFT, rootP);
                    andP.addInput(SQLPredicate.RIGHT, predicate);
                    addObject(andP);
                }

            }
        }

        // Now get the condition text and set it
        int currentMode = getGuiMode();
        setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
        SQLPredicate pred = getRootPredicate();
        setGuiMode(currentMode);

        if (pred != null) {
            this.setConditionText(pred.toString());
        }
    }

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     * 
     * @param newObject new instance to add
     * @throws BaseException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws BaseException {
        add(newObject);
    }

    /**
     * Adds SQLObject to list of object references to be resolved in a second pass.
     * 
     * @param sqlObj to be added
     * @param element DOM Element of SQLObject to be resolved later
     */
    public void addSecondPassSQLObject(SQLObject sqlObj, Element element) {
        // this is a fix for problem with equal and hashcode method of sql object
        // equal method usually use some object which are not yet resolved when
        // this method is called during parsing. so different object become
        // equal breaking the code because of hash map get and put method
        // may return a different object or overwrite existing object
        secondPassList.add(new SecondParseObjectInfo(sqlObj, element));
    }

    public Object clone() throws CloneNotSupportedException {
        SQLCondition cond = null;
        try {
            cond = new SQLConditionImpl(this);
        } catch (Exception ex) {
            throw new CloneNotSupportedException("can not create clone of " + this.toString());
        }

        return cond;
    }

    /**
     * all sql objects are cloneable
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Creates a new SQLObject instance of the given type with the given display name -
     * does not associated the vended SQLObject with this instance. To associate the
     * returned SQLObject instance with this instance, the calling method should call
     * addSQLObject(SQLObject) which will ensure the parent-child relationship is
     * preserved.
     * 
     * @param objTag objTag of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see #addObject(SQLObject)
     */
    public SQLObject createObject(String objTag) throws BaseException {
        return SQLObjectFactory.createObjectForTag(objTag);
    }

    /**
     * Creates a new SQLObject instance of the given type with the given display name -
     * does not associated the vended SQLObject with this instance. To associate the
     * returned SQLObject instance with this instance, the calling method should call
     * addSQLObject(SQLObject) which will ensure the parent-child relationship is
     * preserved.
     * 
     * @param className className of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see #addObject(SQLObject)
     */
    public SQLObject createSQLObject(String className) throws BaseException {
        return SQLObjectFactory.createSQLObject(className);
    }

    /**
     * check if two objects are equal
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SQLCondition)) {
            return false;
        } else if (obj == null) {
            return false;
        }

        SQLCondition cond = (SQLCondition) obj;
        // check for display name
        boolean matched = (this.getDisplayName() != null) ? this.getDisplayName().equals(cond.getDisplayName()) : (cond.getDisplayName() == null);

        // check for condition text
        matched &= (this.getConditionText() != null) ? this.getConditionText().equals(cond.getConditionText()) : (cond.getConditionText() == null);

        // check for object collection
        if (matched) {
            Collection objList = cond.getAllObjects();
            if (this.objectList.size() == objList.size()) {
                Iterator it = this.objectList.iterator();
                while (it.hasNext()) {
                    SQLObject sqlObj = (SQLObject) it.next();
                    if (!objList.contains(sqlObj)) {
                        matched = false;
                        break;
                    }
                }
            } else {
                matched = false;
            }
        }

        if (matched) {
            if (this.getGuiMode() != cond.getGuiMode()) {
                matched = false;
            }
        }

        return matched;
    }

    /**
     * Gets the Collection of active SQLObjects.
     * 
     * @return Collection of current SQLObjects in this SQLDefinition instance.
     */
    public Collection getAllObjects() {
        return objectList;
    }

    /**
     * Gets an attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName) {
        return (Attribute) attributes.get(attrName);
    }

    /**
     * @see SQLObject#getAttributeNames
     */
    public Collection getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * @see SQLObject#getAttributeObject
     */
    public Object getAttributeValue(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    public String getConditionText() {
        return this.conditionText;
    }

    public String getConditionText(boolean constructIfEmpty) {
        if (constructIfEmpty) {
            if (conditionText == null || conditionText.trim().equals("")) {
                constructSqlText();
            }
        }
        return this.conditionText;
    }

    /**
     * Gets display name.
     * 
     * @return current display name
     */
    public String getDisplayName() {
        return dispName;
    }

    public GUIInfo getGUIInfo() {
        return this.guiInfo;
    }

    public int getGuiMode() {
        Integer mode = (Integer) this.guiInfo.getAttributeValue(GUIMODE);
        if (mode != null) {
            return mode.intValue();
        }

        return GUIMODE_SQLCODE;
    }

    public Collection getInputCanvasObjectsNotIn(List gg) {
        Collection inputObjects = getAllInputSQLCanvasObjects(this.root);
        inputObjects.removeAll(gg);
        return inputObjects;
    }

    /**
     * Gets associated SQLObject instance, if any, with the given object ID.
     * 
     * @param objectId ID of SQLObject instance to be retrieved
     * @param type type of object to retrieve
     * @return associated SQLObject instance, or null if no such instance exists
     */
    public SQLObject getObject(String objectId, int type) {
        SQLObject sqlObj = null;
        sqlObj = getSQLObject(objectId);

        return sqlObj;
    }

    /**
     * Gets a Collection of SQLObjects, if any, with the given type
     * 
     * @param type SQLObject type to retrieve
     * @return Collection (possibly empty) of SQLObjects with the given type
     */
    public Collection getObjectsOfType(int type) {
        ArrayList list = new ArrayList();

        Iterator it = objectList.iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            if (sqlObject.getObjectType() == type) {
                list.add(sqlObject);
            }
        }

        return list;
    }

    /**
     * Gets parent object, if any, that owns this SQLDefinition instance.
     * 
     * @return parent object
     */
    public Object getParent() {
        return this.parent;
    }

    public Object getParentObject() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.model.SQLCondition#getParticipatingColumns()
     */
    public List getParticipatingColumns() {
        List columns = new ArrayList();
        SQLConnectableObject expression = this.root;

        if (expression != null) {
            columns.addAll(getColumnsInExpression(expression));
        }

        return columns;
    }

    public SQLPredicate getRootPredicate() {
        // return predicate based on where user specified condition
        if (this.getGuiMode() == SQLCondition.GUIMODE_GRAPHICAL) {
            this.root = findRootPredicate();
            // check if there are any dangling objects even though we have a valid root
            // predicate
            if (this.root != null && isDanglingObjectsExist()) {
                this.root = null;
            }
        } else {
            populateObjectsFromConditionText();
        }

        return this.root;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hCode = super.hashCode();

        if (this.getDisplayName() != null) {
            hCode += this.getDisplayName().hashCode();
        }

        if (this.getConditionText() != null) {
            hCode += this.getConditionText().hashCode();
        }

        Iterator it = this.objectList.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            hCode += sqlObj.hashCode();
        }

        return hCode;
    }

    public boolean isConditionDefined() {
        if (this.getAllObjects().size() == 0 && ((this.getConditionText() == null) || (this.getConditionText() != null && this.getConditionText().trim().equals("")))) {
            return false;
        }

        return true;
    }

    /**
     * Check if a java operator is used in the model.
     * 
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators() {
        Boolean containsJavaOperators = (Boolean) this.getAttributeValue(ATTR_CONTAINS_JAVA_OPERATORS);
        if (containsJavaOperators != null) {
            return containsJavaOperators.booleanValue();
        }
        return false;
    }

    /**
     * check if the object already exist
     * 
     * @return true if object already exist
     */
    public SQLObject isObjectExist(SQLObject obj) {
        Collection objs = this.getAllObjects();
        Iterator it = objs.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj.equals(obj)) {
                return sqlObj;
            }
        }

        return null;
    }

    /**
     * is this condition a valid condition
     * 
     * @return true if condition is valid
     */
    public boolean isValid() {
        if (isConditionDefined()) {
            if (this.getRootPredicate() != null) {
                return true;
            }

            return false;
        }
        // by default condition is valid if condition is not
        // yet defined in any way
        return true;

    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @exception BaseException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws BaseException {
        NodeList list = null;

        if (xmlElement == null) {
            throw new BaseException("xmlElement is null");
        }

        // parse sql code
        list = xmlElement.getElementsByTagName(TAG_SQLCODE);
        if (list != null) {
            // we will have first child as TEXT_NODE where we have
            // sql text.
            if (list.getLength() > 0) {
                Node sqlCodeNode = list.item(0);
                Node textNode = sqlCodeNode.getFirstChild();
                if (textNode != null) {
                    final short nodeType = textNode.getNodeType();
                    if (nodeType == Node.CDATA_SECTION_NODE) {
                        String sql = ((CDATASection) textNode).getData();
                        this.setConditionText(sql);
                    } else if (nodeType == Node.TEXT_NODE) {
                        String sql = ((Text) textNode).getData();
                        this.setConditionText(sql);
                    }
                }
            }
        }

        // parse the attributes
        list = xmlElement.getChildNodes();
        TagParserUtility.parseAttributeList(this.attributes, list);

        list = xmlElement.getChildNodes();
        parseXML(list);

        // parse gui info
        NodeList guiInfoList = xmlElement.getChildNodes();
        for (int i = 0; i < guiInfoList.getLength(); i++) {
            Node gNode = guiInfoList.item(i);
            if (gNode.getNodeName().equals(GUIInfo.TAG_GUIINFO)) {
                this.guiInfo = new GUIInfo((Element) gNode);
            }
        }

        doSecondPassParse();

    }

    /**
     * Remove all objects from this container
     */
    public void removeAllObjects() {
        this.objectList.clear();
    }

    public void removeDanglingColumnRef(ColumnRefImpl col) {
        Collection allObj = this.getAllObjects();
        Iterator it = allObj.iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SQLConnectableObject) {
                SQLConnectableObject expObj = (SQLConnectableObject) obj;

                Map inputObjMap = expObj.getInputObjectMap();
                Iterator itIn = inputObjMap.keySet().iterator();

                while (itIn.hasNext()) {
                    String name = (String) itIn.next();
                    SQLInputObject inputObj = (SQLInputObject) inputObjMap.get(name);
                    SQLObject sqlObj = inputObj.getSQLObject();
                    // null out the dangling condition column ref
                    if (sqlObj != null && sqlObj.equals(col)) {
                        inputObj.setSQLObject(null);
                    }
                }
            }
        }
    }

    /**
     * when a table is removed whose column are refered in this condition rhen we need to
     * remove the column references
     */
    public void removeDanglingColumnRef(SQLObject column) throws BaseException {
        // get all ColumnRefImpl and check if the ref to given passed column
        // if so then remove the reference to this ConditionCoulmnImpl from
        // other expression objects within this condition and remove ColumnRefImpl
        // also
        Collection columnRefs = this.getObjectsOfType(SQLConstants.COLUMN_REF);
        Iterator it = columnRefs.iterator();

        while (it.hasNext()) {
            ColumnRefImpl colRef = (ColumnRefImpl) it.next();
            SQLObject columnRef = colRef.getColumn();
            // check if column contained in the ColumnRefImpl ref to the passed
            // coulmn
            if (columnRef != null && columnRef.equals(column)) {
                removeDanglingColumnRef(colRef);
                this.removeObject(colRef);
            }
        }
    }

    /**
     * Removes equality operator "col = value" from the condition and any reference to it
     * using "AND" operator.
     * 
     * @param col
     * @param value
     * @throws BaseException
     */
    public void removeEqualsPredicate(SQLDBColumn col, SQLObject value) throws BaseException {
        ColumnRefImpl leftCRef = new ColumnRefImpl(col);
        ColumnRefImpl rightCRef = new ColumnRefImpl((SQLDBColumn) value);

        VisibleSQLPredicate predicate = SQLModelObjectFactory.getInstance().createVisibleSQLPredicate();
        predicate.setOperatorType(SQLConstants.OPERATOR_STR_EQUAL);
        predicate.addInput(SQLPredicate.LEFT, leftCRef);
        predicate.addInput(SQLPredicate.RIGHT, rightCRef);

        SQLObject exPredicate = getMatchingSQLObject(predicate, PARENTHESIS);
        SQLObject exLeftCRef = isObjectExist(leftCRef);
        SQLObject exRightCRef = isObjectExist(rightCRef);
        SQLPredicate rootP = getRootPredicate();

        boolean foundExistingAnd = false;
        VisibleSQLPredicate exAndP = null;

        if ((exPredicate != null) && (rootP != null)) {
            VisibleSQLPredicate andP = SQLModelObjectFactory.getInstance().createVisibleSQLPredicate();
            andP.setOperatorType("AND");

            Collection objColl = getAllObjects();
            if (objColl != null) {
                Iterator itr = objColl.iterator();
                SQLObject sqlObject = null;
                SQLObject leftOperand = null;
                SQLObject rightOperand = null;
                SQLInputObject input = null;
                while (itr.hasNext()) {
                    sqlObject = (SQLObject) itr.next();
                    if ((sqlObject != null) && (sqlObject.getObjectType() == andP.getObjectType())) {
                        exAndP = (VisibleSQLPredicate) sqlObject;
                        input = exAndP.getInput(SQLPredicate.LEFT);
                        leftOperand = input.getSQLObject();

                        if (predicate.equals(leftOperand)) {
                            foundExistingAnd = true;
                            break;
                        }

                        input = exAndP.getInput(SQLPredicate.RIGHT);
                        if (input != null) {
                            rightOperand = input.getSQLObject();
                            if (predicate.equals(rightOperand)) {
                                foundExistingAnd = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (foundExistingAnd) {
            if (exAndP != null) {
                removeObject(exAndP);
                Map inputMap = exAndP.getInputObjectMap();
                if (inputMap != null) {
                    Collection inputs = inputMap.values();
                    Iterator itr = inputs.iterator();
                    VisibleSQLPredicate inputPredicate = null;
                    SQLInputObject inputObject = null;
                    while (itr.hasNext()) {
                        inputObject = (SQLInputObject) itr.next();
                        if (inputObject.getSQLObject() instanceof VisibleSQLPredicate) {
                            inputPredicate = (VisibleSQLPredicate) inputObject.getSQLObject();
                            inputPredicate.setRoot(exAndP.getRoot());
                        }
                    }
                }
            }
        }

        if (exPredicate != null) {
            removeObject(exPredicate);

            if (exLeftCRef != null) {
                removeObject(exLeftCRef);
            }

            if (exRightCRef != null) {
                removeObject(exRightCRef);
            }
        }
    }

    /**
     * Removes the given object from SQLDefinition
     * 
     * @param sqlObj to be removed
     * @throws BaseException while removing
     */
    public void removeObject(SQLObject sqlObj) throws BaseException {
        // chec if it is a table object
        if (sqlObj == null) {
            throw new BaseException("Can not delete null object");
        }

        objectList.remove(sqlObj);
    }

    /**
     * Removes the given list of objects from SQLCondition collection.
     * 
     * @param sqlObjs collection of SQLObjects to be removed
     * @throws BaseException while removing
     */
    public void removeObjects(Collection sqlObjs) throws BaseException {
        if (sqlObjs == null) {
            throw new BaseException("Can not delete null object");
        }
        objectList.removeAll(sqlObjs);
    }

    /**
     * Removes the "TargetColumn IS NULL" expression from the target condition. Used when
     * target table is outer joined with source/table view and SQL being generated for
     * ANSI satndard FROM clause.
     * 
     * @param cond
     * @throws BaseException
     */
    public void replaceTargetColumnIsNullPredicate() throws BaseException {
        if (objectList != null) {
            Iterator itr = objectList.iterator();

            SQLObject condObject = null;
            SQLObject operand = null;
            SQLObject targetColumnInput = null;
            SQLInputObject input = null;
            ColumnRef colref = null;
            TargetColumn tc = null;
            VisibleSQLPredicate isNullPredicate = null;
            SQLPredicate isNullParent = null;
            boolean resetText = false;
            Collection objectsTobeRemoved = new ArrayList();
            Collection objectsTobeAdded = new ArrayList();

            while (itr.hasNext()) {
                condObject = (SQLObject) itr.next();
                if (condObject instanceof VisibleSQLPredicate) {
                    isNullPredicate = (VisibleSQLPredicate) condObject;
                    if ("IS".equalsIgnoreCase(isNullPredicate.getOperatorType())) {
                        input = isNullPredicate.getInput(SQLPredicate.LEFT);
                        colref = (ColumnRef) input.getSQLObject();
                        operand = colref.getColumn();

                        if (operand instanceof TargetColumn) {
                            objectsTobeRemoved.add(isNullPredicate);

                            isNullParent = isNullPredicate.getRoot();

                            tc = (TargetColumn) operand;
                            targetColumnInput = tc.getValue();
                            // If source column is mapped to a target column,
                            // replacewith tcol = sCol.
                            if (targetColumnInput instanceof SourceColumn) {
                                ColumnRef rightCRef = new ColumnRefImpl((SQLDBColumn) targetColumnInput);
                                VisibleSQLPredicate eqPredicate = SQLModelObjectFactory.getInstance().createVisibleSQLPredicate();
                                eqPredicate.setOperatorType(SQLConstants.OPERATOR_STR_EQUAL);
                                eqPredicate.addInput(SQLPredicate.LEFT, colref);
                                eqPredicate.addInput(SQLPredicate.RIGHT, rightCRef);
                                eqPredicate.setRoot(isNullParent);
                                objectsTobeAdded.add(eqPredicate);
                                objectsTobeAdded.add(rightCRef);

                                if (isNullParent != null) {
                                    if (isNullPredicate.equals(isNullParent.getInput(SQLPredicate.LEFT).getSQLObject())) {
                                        isNullParent.addInput(SQLPredicate.LEFT, eqPredicate);
                                    } else {
                                        isNullParent.addInput(SQLPredicate.RIGHT, eqPredicate);
                                    }
                                }
                            } else {
                                objectsTobeRemoved.add(colref);
                                if (isNullParent != null) {
                                    preparePredicateRemoval(isNullParent);
                                    objectsTobeRemoved.add(isNullParent);
                                }
                            }
                        }
                    }
                }
            }

            if (objectsTobeRemoved.size() > 0) {
                removeObjects(objectsTobeRemoved);
                resetText = true;
            }

            if (objectsTobeAdded.size() > 0) {
                itr = objectsTobeAdded.iterator();
                while (itr.hasNext()) {
                    addObject((SQLObject) itr.next());
                }
                resetText = true;
            }

            // Now get the condition text and set it
            int currentMode = getGuiMode();
            // As SQLCondition mode may be text or graphical depending on whether this
            // method is invoked from main eTL canvas or Condition editor canvas.
            setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
            SQLPredicate pred = getRootPredicate();
            setGuiMode(currentMode);

            if ((pred != null) && (resetText)) {
                setConditionText(pred.toString());
            }
        }
    }

    /**
     * @see SQLObject#setAttribute
     */
    public void setAttribute(String attrName, Object val) {
        Attribute attr = getAttribute(attrName);
        if (attr != null) {
            attr.setAttributeValue(val);
        } else {
            attr = new Attribute(attrName, val);
            attributes.put(attrName, attr);
        }
    }

    public void setConditionText(String text) {
        this.conditionText = text;
    }

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    public void setContainsJavaOperators(boolean javaOp) {
        this.setAttribute(ATTR_CONTAINS_JAVA_OPERATORS, new Boolean(javaOp));
    }

    /**
     * Sets display name to given value.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        this.dispName = newName;
    }

    public void setGuiMode(int mode) {
        this.guiInfo.setAttribute(GUIMODE, new Integer(mode));
    }

    /**
     * Sets parent object, if any, that owns this SQLDefinition instance.
     * 
     * @param newParent new parent object
     */
    public void setParent(Object newParent) {
        this.parent = newParent;
    }

    public void setParentObject(Object myParent) {
        this.parent = myParent;
    }

    /**
     * Returns the XML representation of collabSegment.
     * 
     * @param prefix the xml.
     * @return Returns the XML representation of collabSegment.
     */
    public String toXMLString(String prefix) throws BaseException {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder xml = new StringBuilder(500);

        xml.append(prefix + "<" + TAG_CONDITION);

        xml.append(" " + DISPLAY_NAME + "=\"");
        if (this.getDisplayName() != null) {
            xml.append(this.getDisplayName().trim());
        }
        xml.append("\">\n");

        // write out attributes
        xml.append(TagParserUtility.toXMLAttributeTags(this.attributes, prefix));

        String nestedPrefix = prefix + "\t";

        if (getConditionText() != null) {
            xml.append(nestedPrefix + "<" + TAG_SQLCODE + ">");
            xml.append("<![CDATA[").append(getConditionText()).append("]]>");
            xml.append("</" + TAG_SQLCODE + ">\n");
        }

        xml.append(toXMLString(nestedPrefix, objectList));

        // write out gui info
        xml.append(this.guiInfo.toXMLString(nestedPrefix));

        xml.append(prefix).append("</").append(TAG_CONDITION).append(">\n");

        return xml.toString();
    }

    public List validate() {
        SQLValidationVisitor vVisitor = new SQLValidationVisitor();
        vVisitor.visit(this);
        return vVisitor.getValidationInfoList();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    String generateId() {
        int cnt = 0;

        String id = "sqlObject" + "_" + cnt;
        while (isIdExists(id)) {
            cnt++;
            id = "sqlObject" + "_" + cnt;
        }

        return id;
    }

    SQLObject getSQLObject(String id) {
        if (id == null) {
            return null;
        }

        Iterator it = objectList.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (id.equals(sqlObj.getId())) {
                return sqlObj;
            }
        }

        return null;
    }

    boolean isIdExists(String id) {
        if (id == null) {
            return false;
        }

        if (getSQLObject(id) != null) {
            return true;
        }

        return false;
    }

    private void add(SQLObject newObject) throws BaseException {
        // sql definition make sure an object added has unique id
        // first check if id exists if yes then generate a unique id
        // then add the object
        if (newObject.getId() == null) {
            newObject.setId(generateId());
        }

        newObject.setParentObject(this);
        objectList.add(newObject);
    }

    private void constructSqlText() {
        SQLPredicate theroot = this.getRootPredicate();
        if (theroot != null) {
            this.setConditionText(theroot.toString());
        }
    }

    private void copyFrom(SQLCondition src) throws BaseException {
        // we need to set the parent otherwise this clone
        // condition which is used in condition builder will throw
        // null pointer exception as there is no parent set
        this.setParent(src.getParent());
        this.setDisplayName(src.getDisplayName());
        this.setConditionText(src.getConditionText());

        // clone attributes
        Collection attrNames = src.getAttributeNames();
        Iterator it = attrNames.iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            Attribute attr = src.getAttribute(name);
            if (attr != null) {
                try {
                    Attribute copiedAttr = (Attribute) attr.clone();
                    this.attributes.put(name, copiedAttr);
                } catch (CloneNotSupportedException ex) {
                    mLogger.errorNoloc(mLoc.t("EDIT108: Failed to copy source objects attributes{0}", LOG_CATEGORY), ex);
                }
            }
        }

        // copy gui info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

        // map of original to cloned objects
        // this is so that we can set links properly
        HashMap origToCloneMap = new HashMap();

        // now copy all container object
        Collection children = src.getAllObjects();
        it = children.iterator();

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            try {
                SQLObject clonedObj = (SQLObject) obj.cloneSQLObject();
                this.addObject(clonedObj);
                origToCloneMap.put(obj, clonedObj);
            } catch (CloneNotSupportedException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT108: Failed to copy source objects attributes{0}", LOG_CATEGORY), ex);
            }
        }

        setLinks(origToCloneMap);
        origToCloneMap.clear();
    }

    private void doSecondPassParse() throws BaseException {
        Iterator it = secondPassList.iterator();
        while (it.hasNext()) {
            SecondParseObjectInfo objInfo = (SecondParseObjectInfo) it.next();
            objInfo.getSQLObject().secondPassParse(objInfo.getElement());
        }

        secondPassList.clear();
    }

    private SQLPredicate findRootPredicate() {
        ArrayList rootPredicateList = new ArrayList();
        ArrayList oneTopRootPredicateList = new ArrayList();

        Collection objects = this.objectList;
        Iterator it = objects.iterator();

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            if (obj instanceof VisibleSQLPredicate) {
                SQLPredicate predicate = (SQLPredicate) obj;
                SQLValidationVisitor vVisitor = new SQLValidationVisitor();
                predicate.visit(vVisitor);
                List vInfos = vVisitor.getValidationInfoList();
                vInfos = ConditionBuilderUtil.filterValidations(vInfos);
                if ((vInfos.size() != 0) && hasError(vInfos)) {
                    return null;
                }

                SQLPredicate rootP = predicate.getRoot();

                if (rootP != null) {
                    // add to root predicate list
                    rootPredicateList.add(predicate);
                } else {
                    oneTopRootPredicateList.add(predicate);
                }
            }
        }

        // now there should be only one predicate which will have root predicate
        // is null and all other predicate should have root predicate
        // if this is the case we have one and only one root predicate so we return that'
        if (oneTopRootPredicateList.size() == 1) {
            return (SQLPredicate) oneTopRootPredicateList.get(0);
        }

        return null;
    }

    private Collection getAllInputSQLCanvasObjects(SQLConnectableObject expObj) {
        Set inputObjs = new HashSet();
        inputObjs.add(expObj);

        Collection inputs = expObj.getInputObjectMap().values();
        Iterator it = inputs.iterator();

        while (it.hasNext()) {
            SQLInputObject inObj = (SQLInputObject) it.next();
            SQLObject sqlObj = inObj.getSQLObject();
            // if sqlObj is a SQLCanvasObject then only we need to check.
            // This sqlObj may not be a SQLCanvasObject if it is
            // a part of object like SQLLiteral for literal values
            if (sqlObj != null && sqlObj instanceof SQLCanvasObject) {
                if (sqlObj instanceof SQLConnectableObject) {
                    inputObjs.addAll(getAllInputSQLCanvasObjects((SQLConnectableObject) sqlObj));
                } else {
                    inputObjs.add(sqlObj);
                }
            }
        }
        return inputObjs;
    }

    /**
     * @param expression
     * @return List of source or target table columns participating in the expression.
     */
    private List getColumnsInExpression(SQLConnectableObject expression) {
        List columns = new ArrayList();
        Collection inputs = expression.getInputObjectMap().values();
        Iterator itr = inputs.iterator();

        while (itr.hasNext()) {
            SQLInputObject inObj = (SQLInputObject) itr.next();
            SQLObject sqlObj = inObj.getSQLObject();
            if (sqlObj != null) {
                if (sqlObj instanceof SQLConnectableObject) {
                    columns.addAll(getColumnsInExpression((SQLConnectableObject) sqlObj));
                } else {
                    if (sqlObj instanceof ColumnRef) {
                        SQLObject sObj = ((ColumnRef) sqlObj).getColumn();
                        if ((sObj instanceof SourceColumn) || (sObj instanceof TargetColumn)) {
                            columns.add(sObj);
                        }
                    }
                }
            }
        }
        return columns;
    }

    /**
     * Gets SQLObject participating in the condition, if one exists. While checking for
     * equality ingnores the attribute passed.
     * 
     * @param sqlObject
     * @param ignoreAttributeName
     * @return matching SQLObject else null
     */
    private SQLObject getMatchingSQLObject(SQLObject sqlObject, String ignoreAttributeName) {
        SQLObject matchingObject = null;
        matchingObject = isObjectExist(sqlObject);

        if (matchingObject == null) {
            sqlObject.setAttribute(ignoreAttributeName, Boolean.FALSE);
            matchingObject = isObjectExist(sqlObject);
        }

        if (matchingObject == null) {
            sqlObject.setAttribute(ignoreAttributeName, Boolean.TRUE);
            matchingObject = isObjectExist(sqlObject);
        }

        return matchingObject;
    }

    /**
     * @param valInfoList
     * @return
     */
    private boolean hasError(List valInfoList) {
        boolean ret = false;

        if (valInfoList != null) {
            Iterator itr = valInfoList.iterator();
            ValidationInfo vInfo = null;

            while (itr.hasNext()) {
                vInfo = (ValidationInfo) itr.next();
                if (vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    // check for dangling condition columns in graph mode
    // there may be a valid predicate but if there are other dangling
    // objects then we warn user. The dangling objects does not harm
    // since we still get root predicate , but still we need to check for it to be
    // consistent
    private boolean isDanglingObjectsExist() {
        boolean response = false;
        // so we have a valid root predicate, this can be used for condition
        // but now check for dangling condition columns by going through all inputs of
        // root in a recursive fashion
        if (this.root != null) {
            Collection usedObjects = getAllInputSQLCanvasObjects(this.root);
            if (usedObjects.size() != this.objectList.size()) {
                response = true;
            }
        }

        return response;
    }

    private void parseXML(NodeList list) throws BaseException {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (!node.getNodeName().equals(SQLObject.TAG_SQLOBJECT)) {
                continue;
            }
            Element opeElem = (Element) node;

            SQLObject sqlObj = SQLObjectFactory.createSQLObjectForElement(this, opeElem);
            if (sqlObj != null) {
                this.addObject(sqlObj);
            } else {
                throw new BaseException("Failed to parse " + opeElem);
            }
        }
    }

    private void populateObjectsFromConditionText() {
        try {
            this.root = null;
            SQLObject obj = ConditionUtil.parseCondition(this.conditionText, SQLObjectUtil.getAncestralSQLDefinition((SQLObject) this.getParent()));
            this.removeAllObjects();
            ConditionUtil.populateCondition(this, obj);
            this.root = findRootPredicate();
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT110: Error finding root predicate from text condition{0}in {1}", this.conditionText, LOG_CATEGORY), ex);
        }
    }

    /**
     * Assign current predicate's parent as its childs parent.
     * 
     * @param cond
     * @param predicate
     * @throws BaseException
     */
    private void preparePredicateRemoval(SQLPredicate predicate) {
        Map inputMap = predicate.getInputObjectMap();
        if (inputMap != null) {
            Collection inputs = inputMap.values();
            Iterator itr = inputs.iterator();
            VisibleSQLPredicate inputPredicate = null;
            SQLInputObject inputObject = null;
            while (itr.hasNext()) {
                inputObject = (SQLInputObject) itr.next();
                if (inputObject.getSQLObject() instanceof VisibleSQLPredicate) {
                    inputPredicate = (VisibleSQLPredicate) inputObject.getSQLObject();
                    inputPredicate.setRoot(predicate.getRoot());
                }
            }
        }
    }

    private void setLinks(HashMap origToCloneMap) throws BaseException {
        setLinks(origToCloneMap, origToCloneMap.keySet());
    }

    private void setLinks(HashMap origToCloneMap, Collection expObjs) throws BaseException {
        Iterator it = expObjs.iterator();
        while (it.hasNext()) {
            SQLObject origObj = (SQLObject) it.next();
            if (origObj instanceof SQLConnectableObject) {
                setLinks(origToCloneMap, (SQLConnectableObject) origObj);
            }
        }
    }

    private void setLinks(HashMap origToCloneMap, SQLConnectableObject origObj) throws BaseException {
        SQLConnectableObject clonedObj = (SQLConnectableObject) origToCloneMap.get(origObj);

        if (origObj instanceof SQLPredicate) {
            SQLPredicate myRoot = ((SQLPredicate) origObj).getRoot();
            if (myRoot != null) {
                ((SQLPredicate) clonedObj).setRoot((SQLPredicate) origToCloneMap.get(myRoot));
            }
        }

        Map inputObjMap = origObj.getInputObjectMap();
        Iterator it = inputObjMap.keySet().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            SQLInputObject inObj = (SQLInputObject) inputObjMap.get(name);

            SQLObject sqlObj = inObj.getSQLObject();
            if (sqlObj != null) {
                SQLObject clonedSQLObj = (SQLObject) origToCloneMap.get(sqlObj);
                if (clonedSQLObj != null) {
                    clonedObj.addInput(name, clonedSQLObj);
                }
            }
        }

        List children = origObj.getChildSQLObjects();
        setLinks(origToCloneMap, children);
    }

    private String toXMLString(String prefix, Collection sqlObjects) throws BaseException {
        Iterator it = sqlObjects.iterator();
        StringBuilder xml = new StringBuilder(" ");

        int i = 0;
        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            if (obj != null) {
                // Add newline between each element, starting after first one.
                if (i++ != 0) {
                    xml.append("\n");
                }
                xml.append(obj.toXMLString(prefix));
            }
        }

        return xml.toString();
    }
}
