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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;

/**
 * @author Ritesh Adval
 */
public class SQLJoinViewImpl extends AbstractSQLObject implements SQLJoinView {

    private static transient final Logger mLogger = Logger.getLogger(SQLJoinViewImpl.class.getName());
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
    public static final String TAG_JOINVIEW = "join-view";
    public static final String TAG_TABLES = "tables";
    private static final String ATTR_ALIAS_NAME = "aliasName";
    private static final String LOG_CATEGORY = SQLJoinViewImpl.class.getName();
    /* GUI state info */
    private GUIInfo guiInfo = new GUIInfo();
    private ArrayList objectList = new ArrayList();
    private transient List secondPassList = new ArrayList();
    private SQLGroupBy groupBy;

    /** Creates a new instance of SQLJoinContainerImpl */
    public SQLJoinViewImpl() {
        this.type = SQLConstants.JOIN_VIEW;
        this.setDisplayName("JoinView");
    }

    public SQLJoinViewImpl(SQLJoinView src) throws BaseException {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null SQLJoinView instance for src param.");
        }

        try {
            copyFrom(src);
        } catch (Exception ex) {
            throw new BaseException("can not create SQLJoinViewImpl using copy constructor", ex);
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
        if (newObject.getObjectType() == SQLConstants.JOIN_TABLE || newObject.getObjectType() == SQLConstants.JOIN) {
            add(newObject);
        } else {
            throw new BaseException("Addition of sqlobject of type " + TagParserUtility.getStringType(newObject.getObjectType()) + " is not supported.");
        }
    }

    /**
     * Adds SQLObject to list of object references to be resolved in a second pass.
     *
     * @param sqlObj to be added
     * @param element DOM Element of SQLObject to be resolved later
     */
    public void addSecondPassSQLObject(SQLObject sqlObj, Element element) {
        secondPassList.add(new SecondParseObjectInfo(sqlObj, element));
    }

    public Object clone() throws CloneNotSupportedException {
        SQLJoinViewImpl cond = null;
        try {
            cond = new SQLJoinViewImpl(this);
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT119: Error while cloniing SQLJoinViewImpl{0}", this.toString()), ex);
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

    public boolean containsSourceTable(SourceTable table) {
        Collection tables = this.getObjectsOfType(SQLConstants.JOIN_TABLE);
        Iterator it = tables.iterator();

        while (it.hasNext()) {
            SQLJoinTable jtable = (SQLJoinTable) it.next();
            SourceTable sTable = jtable.getSourceTable();
            if (sTable.equals(table)) {
                return true;
            }
        }

        return false;
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
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     *
     * @param o Object to test for equality with this
     * @return hashcode for this instance
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof SQLJoinView)) {
            return false;
        }

        SQLJoinView joinView = (SQLJoinView) o;
        Collection objList = joinView.getAllObjects();

        if (this.objectList.size() == objList.size()) {
            Iterator it = this.objectList.iterator();
            while (it.hasNext()) {
                SQLObject sqlObj = (SQLObject) it.next();
                if (!objList.contains(sqlObj)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    /**
     * get the alias name for this join view
     *
     * @return alias name
     */
    public String getAliasName() {
        return (String) this.getAttributeObject(ATTR_ALIAS_NAME);
    }

    /**
     * Gets the Collection of active SQLObjects.
     *
     * @return Collection of current SQLObjects in this SQLDefinition instance.
     */
    public Collection getAllObjects() {
        return this.objectList;
    }

    /**
     * Gets GUI-related attributes for this instance in the form of a GuiInfo instance.
     *
     * @return associated GuiInfo instance
     * @see GUIInfo
     */
    public GUIInfo getGUIInfo() {
        return guiInfo;
    }

    public SQLJoinOperator getJoinofTable(SQLJoinTable jTable) {
        Collection joins = this.getObjectsOfType(SQLConstants.JOIN);

        Iterator it = joins.iterator();

        while (it.hasNext()) {
            SQLJoinOperator join = (SQLJoinOperator) it.next();
            SQLInputObject leftIn = join.getInput(SQLJoinOperator.LEFT);
            SQLInputObject rightIn = join.getInput(SQLJoinOperator.RIGHT);

            SQLObject leftObj = leftIn.getSQLObject();
            SQLObject rightObj = rightIn.getSQLObject();

            if (leftObj != null && leftObj.equals(jTable)) {
                return join;
            }

            if (rightObj != null && rightObj.equals(jTable)) {
                return join;
            }
        }

        return null;
    }

    public SQLJoinTable getJoinTable(SourceTable sTable) {
        Collection tables = this.getObjectsOfType(SQLConstants.JOIN_TABLE);
        Iterator it = tables.iterator();

        while (it.hasNext()) {
            SQLJoinTable jtable = (SQLJoinTable) it.next();
            SourceTable table = jtable.getSourceTable();
            if (sTable.equals(table)) {
                return jtable;
            }
        }

        return null;
    }

    /**
     * Gets associated SQLObject instance, if any, with the given object ID.
     *
     * @param objectId ID of SQLObject instance to be retrieved
     * @param aType type of object to retrieve
     * @return associated SQLObject instance, or null if no such instance exists
     */
    public SQLObject getObject(String objectId, int aType) {
        SQLObject sqlObj = null;
        sqlObj = getSQLObject(objectId);

        return sqlObj;
    }

    /**
     * Gets a Collection of SQLObjects, if any, with the given type
     *
     * @param atype SQLObject type to retrieve
     * @return Collection (possibly empty) of SQLObjects with the given type
     */
    public Collection getObjectsOfType(int atype) {
        ArrayList list = new ArrayList();

        Iterator it = objectList.iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            if (sqlObject.getObjectType() == atype) {
                list.add(sqlObject);
            }
        }

        return list;
    }

    public int getObjectType() {
        return SQLConstants.JOIN_VIEW;
    }

    /**
     * Gets parent object, if any, that owns this SQLDefinition instance.
     *
     * @return parent object
     */
    public Object getParent() {
        return super.getParentObject();
    }

    /**
     * get table qualified name
     *
     * @return qualified table name prefixed with alias
     */
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder(50);
        String aName = this.getAliasName();
        if (aName != null && !aName.trim().equals("")) {
            buf.append("(");
            buf.append(aName);
            buf.append(") ");
        }

        buf.append(this.getDisplayName());

        return buf.toString();
    }

    /**
     * get the root join located in this join view
     *
     * @return root join
     */
    public SQLJoinOperator getRootJoin() {
        Collection joins = this.getObjectsOfType(SQLConstants.JOIN);
        Iterator it = joins.iterator();

        while (it.hasNext()) {
            SQLJoinOperator join = (SQLJoinOperator) it.next();
            if (join.isRoot()) {
                return join;
            }
        }

        return null;
    }

    public List getSourceTables() {
        ArrayList sTables = new ArrayList();

        Collection tables = this.getObjectsOfType(SQLConstants.JOIN_TABLE);
        Iterator it = tables.iterator();

        while (it.hasNext()) {
            SQLJoinTable jtable = (SQLJoinTable) it.next();
            SourceTable sTable = jtable.getSourceTable();
            sTables.add(sTable);
        }

        return sTables;
    }

    public Collection getSQLJoinTables() {
        return this.getObjectsOfType(SQLConstants.JOIN_TABLE);
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     *
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hCode = super.hashCode();
        Iterator it = this.objectList.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            hCode += sqlObj.hashCode();
        }

        return hCode;
    }

    public boolean isSourceColumnVisible(SQLDBColumn column) {
        Collection tables = this.getObjectsOfType(SQLConstants.JOIN_TABLE);
        Iterator it = tables.iterator();
        SourceTable table = (SourceTable) column.getParent();

        while (it.hasNext()) {
            SQLJoinTable jtable = (SQLJoinTable) it.next();
            SourceTable sTable = jtable.getSourceTable();
            if (sTable.getParent().getFullyQualifiedTableName(sTable).equals(table.getParent().getFullyQualifiedTableName(table))) {
                List columns = sTable.getColumnList();
                Iterator cIt = columns.iterator();
                while (cIt.hasNext()) {
                    SQLDBColumn oldColumn = (SQLDBColumn) cIt.next();
                    // check based on name not on object equal
                    if (oldColumn.getName().equals(column.getName())) {
                        return oldColumn.isVisible();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     *
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @throws BaseException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);

        NodeList list = xmlElement.getChildNodes();

        parseXML(list);

        // parse gui info & groupby operator.
        NodeList guiInfoList = xmlElement.getChildNodes();
        for (int i = 0; i < guiInfoList.getLength(); i++) {
            Node gNode = guiInfoList.item(i);
            if (gNode.getNodeName().equals(GUIInfo.TAG_GUIINFO)) {
                this.guiInfo = new GUIInfo((Element) gNode);
            } else if (gNode.getNodeName().equals(SQLGroupByImpl.ELEMENT_TAG)) {
                groupBy = new SQLGroupByImpl();
                groupBy.setParentObject(this);
                groupBy.parseXML((Element) gNode);
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
        switch (sqlObj.getObjectType()) {

            case SQLConstants.JOIN_TABLE:
                SQLJoinTable jTable = (SQLJoinTable) sqlObj;

                Collection joins = this.getObjectsOfType(SQLConstants.JOIN);
                Iterator it = joins.iterator();
                while (it.hasNext()) {
                    SQLJoinOperator join = (SQLJoinOperator) it.next();
                    SQLInputObject leftInObj = join.getInput(SQLJoinOperator.LEFT);
                    SQLInputObject rightInObj = join.getInput(SQLJoinOperator.RIGHT);

                    if (jTable.equals(leftInObj.getSQLObject())) {
                        leftInObj.setSQLObject(null);
                        this.removeObject(join);
                    } else if (jTable.equals(rightInObj.getSQLObject())) {
                        rightInObj.setSQLObject(null);
                        this.removeObject(join);
                    } else {
                        // see if this join's condition has that table column reference
                        SQLCondition joinCondition = join.getJoinCondition();
                        SourceTable sTable = jTable.getSourceTable();
                        Iterator cIt = sTable.getColumnList().iterator();
                        while (cIt.hasNext()) {
                            SQLDBColumn column = (SQLDBColumn) cIt.next();
                            joinCondition.removeDanglingColumnRef(column);
                        }
                    }
                }
                break;
            case SQLConstants.JOIN:
                SQLJoinOperator join = (SQLJoinOperator) sqlObj;
                removeJoinReference(join);
                break;
        }
        objectList.remove(sqlObj);
    }

    /**
     * Removes given SQLObjects from SQLJoinView collection.
     *
     * @param sqlObjects to be removed
     * @throws BaseException while removing
     */
    public void removeObjects(Collection sqlObjs) throws BaseException {
        if (sqlObjs == null) {
            throw new BaseException("Can not delete null object");
        }

        Iterator itr = sqlObjs.iterator();
        while (itr.hasNext()) {
            this.removeObject((SQLObject) itr.next());
        }
    }

    public void removeTablesAndJoins(SourceTable sTable) throws BaseException {
        Collection joins = this.getObjectsOfType(SQLConstants.JOIN);
        Iterator it = joins.iterator();

        while (it.hasNext()) {
            SQLJoinOperator join = (SQLJoinOperator) it.next();

            SQLInputObject leftInObj = join.getInput(SQLJoinOperator.LEFT);
            SQLInputObject rightInObj = join.getInput(SQLJoinOperator.RIGHT);

            SQLObject leftObj = leftInObj.getSQLObject();
            SQLObject rightObj = rightInObj.getSQLObject();

            if (sTable.equals(leftObj) || sTable.equals(rightObj)) {
                this.removeObject(join);
            }
        }

        this.removeObject(sTable);

    }

    /**
     * set the alias name for this join view
     *
     * @param aName alias name
     */
    public void setAliasName(String aName) {
        this.setAttribute(ATTR_ALIAS_NAME, aName);
    }

    /**
     * Sets parent object, if any, that owns this SQLDefinition instance.
     *
     * @param newParent new parent object
     * @throws BaseException if error occurs while setting parent object
     */
    public void setParent(Object newParent) {
        try {
            super.setParentObject(newParent);
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the XML representation of collabSegment.
     *
     * @param prefix the xml.
     * @return Returns the XML representation of colabSegment.
     */
    public String toXMLString(String prefix) throws BaseException {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buffer = new StringBuilder(500);
        if (prefix == null) {
            prefix = "";
        }

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));

        // write out tables
        Iterator it = this.objectList.iterator();
        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            buffer.append(obj.toXMLString(prefix + "\t"));
        }

        if (groupBy != null) {
            buffer.append(groupBy.toXMLString(prefix + "\t"));
        }

        buffer.append(this.guiInfo.toXMLString(prefix + "\t"));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    String generateId() {
        int cnt = 0;

        String anId = "sqlObject" + "_" + cnt;
        while (isIdExists(anId)) {
            cnt++;
            anId = "sqlObject" + "_" + cnt;
        }

        return anId;
    }

    SQLObject getSQLObject(String anId) {
        if (anId == null) {
            return null;
        }

        Iterator it = objectList.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (anId.equals(sqlObj.getId())) {
                return sqlObj;
            }
        }

        return null;
    }

    boolean isIdExists(String anId) {
        if (anId == null) {
            return false;
        }

        if (getSQLObject(anId) != null) {
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

    private void copyFrom(SQLJoinView src) throws BaseException {
        super.copyFromSource(src);

        // copy gui info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

        // map of original to cloned objects
        // this is so that we can set links properly
        HashMap origToCloneMap = new HashMap();

        // now copy all container object
        Collection children = src.getAllObjects();
        Iterator it = children.iterator();

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            try {
                SQLObject clonedObj = (SQLObject) obj.cloneSQLObject();

                this.addObject(clonedObj);
                origToCloneMap.put(obj, clonedObj);

            } catch (CloneNotSupportedException ex) {
                throw new BaseException("Failed to clone " + obj, ex);
            }
        }

        SQLGroupBy grpBy = src.getSQLGroupBy();
        if (grpBy != null) {
            groupBy = new SQLGroupByImpl(grpBy);
        }

        setLinks(origToCloneMap);
        origToCloneMap.clear();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#getSQLGroupBy()
     */
    public SQLGroupBy getSQLGroupBy() {
        return groupBy;
    }

    private void doSecondPassParse() throws BaseException {
        Iterator it = secondPassList.iterator();
        while (it.hasNext()) {
            SecondParseObjectInfo objInfo = (SecondParseObjectInfo) it.next();
            objInfo.getSQLObject().secondPassParse(objInfo.getElement());
        }

        secondPassList.clear();
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

    private void removeJoinReference(SQLJoinOperator join) throws BaseException {
        Collection joins = this.getObjectsOfType(SQLConstants.JOIN);
        Iterator it = joins.iterator();
        while (it.hasNext()) {
            SQLJoinOperator joinEx = (SQLJoinOperator) it.next();
            SQLInputObject leftInObj = joinEx.getInput(SQLJoinOperator.LEFT);
            SQLInputObject rightInObj = joinEx.getInput(SQLJoinOperator.RIGHT);
            if (join.equals(leftInObj.getSQLObject())) {
                leftInObj.setSQLObject(null);
                this.removeObject(joinEx);
            } else if (join.equals(rightInObj.getSQLObject())) {
                rightInObj.setSQLObject(null);
                this.removeObject(joinEx);
            }
        }

        // also remove this joins all input so that root join can be set again
        Map inMap = join.getInputObjectMap();
        Iterator itIn = inMap.keySet().iterator();

        while (itIn.hasNext()) {
            String argName = (String) itIn.next();
            SQLInputObject inObj = (SQLInputObject) inMap.get(argName);
            SQLObject obj = inObj.getSQLObject();
            if (obj != null) {
                join.removeInputByArgName(argName, obj);
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

    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#setSQLGroupBy(org.netbeans.modules.sql.framework.model.SQLGroupBy)
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        this.groupBy = groupBy;
    }
}
