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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.EDMParentObject;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLObjectListener;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.netbeans.modules.edm.model.MashupDefinition;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.DatabaseModel;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.ValidationInfo;

/**
 * Root container for holding ETL model objects.
 *
 * @author Jonathan Giron
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class MashupDefinitionImpl implements MashupDefinition, Serializable {

    /** Attribute name: displayName */
    public static final String ATTR_DISPLAYNAME = "displayName";

    /** Document version */
    public static final String DOC_VERSION = "6.0.1";
    /** XML formatting constant: indent prefix */
    public static final String INDENT = "    ";
    /** TAG_DEFINITION is the tag for an ETL definition */
    public static final String TAG_DEFINITION = "mashupDefinition";
    private static final String ATTR_VERSION = "version";
    private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
    private SQLDefinition sqlDefinition;

    /**
     * Creates a new default instance of MashupDefinitionImpl.
     */
    public MashupDefinitionImpl() {
        init();
        this.setVersion(DOC_VERSION);
    }

    public MashupDefinitionImpl(Element xmlElement, EDMParentObject parent) throws EDMException {
        this(xmlElement, parent, false);
    }

    public MashupDefinitionImpl(Element xmlElement, EDMParentObject parent, boolean preserveVersion) throws EDMException {
        init();
        sqlDefinition.setEDMParentObject(parent);
        parseXML(xmlElement); // parseXML checks for old version
        if (!preserveVersion) {
            this.setVersion(DOC_VERSION); // after parsing set the version
        }
    }

    /**
     * Creates a new instance of ETLDefinitionImpl with the given display name.
     *
     * @param displayName for this
     */
    public MashupDefinitionImpl(String displayName) {
        this();
        this.setDisplayName(displayName);
        
    }

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     *
     * @param newObject new instance to add
     * @throws EDMException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws EDMException {
        this.sqlDefinition.addObject(newObject);
    }

    /**
     * Adds an SQL object listener
     *
     * @param listener SQL object listener
     */
    public void addSQLObjectListener(SQLObjectListener listener) {
        this.sqlDefinition.addSQLObjectListener(listener);
    }

    /**
     * Gets Collection of all SQLObjects in this model.
     *
     * @return Collection, possibly empty, of all SQLObjects
     */
    public Collection<SQLObject> getAllObjects() {
        return this.sqlDefinition.getAllObjects();
    }

    /**
     * Gets the List of Databases
     *
     * @return java.util.List for this
     */
    public List<SQLDBModel> getAllDatabases() {
        return this.sqlDefinition.getAllDatabases();
    }

    /**
     * Gets an attribute based on its name
     *
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName) {
        return attributes.get(attrName);
    }

    /**
     * @see SQLObject#getAttributeNames
     */
    public Collection<String> getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * @see SQLObject#getAttributeObject
     */
    public Object getAttributeValue(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    /**
     * Getter for DatabaseModel
     *
     * @param modelName to be retrieved
     * @return DatabaseModel for given Model Name
     */
    public DatabaseModel getDatabaseModel(String modelName) {
        java.util.List list = sqlDefinition.getAllDatabases();
        java.util.Iterator it = list.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            int type = sqlObj.getObjectType();
            if (type == SQLConstants.SOURCE_DBMODEL) {
                DatabaseModel dbModel = (DatabaseModel) sqlObj;
                if (dbModel != null && dbModel.getModelName().equals(modelName)) {
                    return dbModel;
                }
            }
        }
        return null;
    }

    /**
     * Gets display name.
     *
     * @return current display name
     */
    public String getDisplayName() {
        return (String) this.getAttributeValue(ATTR_DISPLAYNAME);
    }

    /**
     * get the parent repository object
     *
     * @return parent repository object
     */
    public Object getParent() {
        return this.sqlDefinition.getParent();
    }

    /**
     * get runtime db model
     *
     * @return runtime db model
     */
    public RuntimeDatabaseModel getRuntimeDbModel() {
        return this.sqlDefinition.getRuntimeDbModel();
    }

    /**
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of source DatabaseModels
     */
    public List<SQLDBModel> getSourceDatabaseModels() {
        return this.sqlDefinition.getSourceDatabaseModels();
    }

    /**
     * Gets the List of SourceTables
     *
     * @return List, possibly empty, of SourceTables
     */
    public List<DBTable> getSourceTables() {
        return this.sqlDefinition.getSourceTables();
    }

    /**
     * Gets the SQL definition
     *
     * @return SQL definition
     */
    public SQLDefinition getSQLDefinition() {
        return this.sqlDefinition;
    }

    /**
     * get the tag name for this ETLDefinitionImpl override at subclass level to return a
     * different tag name
     *
     * @return tag name to be used in xml representation of this object
     */
    public String getTagName() {
        return MashupDefinitionImpl.TAG_DEFINITION;
    }

    public String getVersion() {
        return (String) this.getAttributeValue(ATTR_VERSION);
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     *
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @throws EDMException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws EDMException {
        NodeList list;

        if (xmlElement == null) {
            throw new EDMException("xmlElement is null");
        }

        list = xmlElement.getChildNodes();
        TagParserUtility.parseAttributeList(attributes, list);
        // check if we have a version less than 5.02
        // for version less than 5.02 it is null
        String version = getVersion();
        if (version == null) {
            sqlDefinition.parseXML(xmlElement);
        } else {
            list = xmlElement.getElementsByTagName(sqlDefinition.getTagName());
            if (list != null && list.getLength() > 0) {
                // first element will be SQLDefinition
                sqlDefinition.parseXML((Element) list.item(0));
            }
        }
    }

    /**
     * Removes given SQLObject instance from this SQLDefinition.
     *
     * @param sqlObj instance to remove
     * @throws EDMException if error occurs during removal
     */
    public void removeObject(SQLObject sqlObj) throws EDMException {
        this.sqlDefinition.removeObject(sqlObj);
    }

    /**
     * Removes SQL object listener
     *
     * @param listener SQL object listener
     */
    public void removeSQLObjectListener(SQLObjectListener listener) {
        this.sqlDefinition.removeSQLObjectListener(listener);
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

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        this.setAttribute(ATTR_DISPLAYNAME, newName);
        sqlDefinition.setDisplayName(newName);
    }

    /**
     * set the parent repository object
     *
     * @param parent parent repository object
     */
    public void setParent(Object parent) {
        this.sqlDefinition.setParent(parent);
    }

    public void setVersion(String ver) {
        this.setAttribute(ATTR_VERSION, ver);
        sqlDefinition.setVersion(ver);
    }

    /**
     * Returns the XML representation of collabSegment.
     *
     * @param prefix the xml.
     * @return Returns the XML representation of colabSegment.
     */
    public String toXMLString(String prefix) throws EDMException {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder xml = new StringBuilder(500);

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        xml.append(prefix + "<" + getTagName() + ">\n");
        // write out attributes
        xml.append(TagParserUtility.toXMLAttributeTags(attributes, prefix));

        // write out SQL definition
        if (sqlDefinition != null) {
            xml.append(sqlDefinition.toXMLString(prefix + "\t"));
        }

        xml.append("</" + getTagName() + ">\n");

        return xml.toString();
    }

    /**
     * validate the definition starting from the target tables.
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> validate() {
        return this.sqlDefinition.validate();
    }

    public List<ValidationInfo> badgeValidate() {
        return this.sqlDefinition.badgeValidate();
    }

    /**
     * Applies whatever rules are appropriate to migrate the current object model to the
     * current version of ETLDefinition as implemented by the concrete class.
     *
     * @throws EDMException if error occurs during migration
     */
//    public void migrateFromOlderVersions() throws EDMException {
//        sqlDefinition.migrateFromOlderVersions();
//    }

    protected void init() {
        sqlDefinition = SQLModelObjectFactory.getInstance().createSQLDefinition();
    }
}
