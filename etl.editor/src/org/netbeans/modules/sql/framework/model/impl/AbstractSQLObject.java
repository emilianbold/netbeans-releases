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
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Attribute;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * This basic class provides sql framework functionality to all SQLObjects
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public abstract class AbstractSQLObject implements SQLObject {
    /* Log4J category string */

    private static final String LOG_CATEGORY = AbstractSQLObject.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(AbstractSQLObject.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Map of attributes; used by concrete implementations to store class-specific fields
     * without hardcoding them as member variables
     */
    protected Map attributes = new HashMap();
    /** User-modifiable display name for this object */
    protected transient String displayName;
    /** Unique handle for SQLObject-related referencing purposes */
    protected transient String id;
    /** String representation of object type */
    protected transient String objectType;
    /** Object instance that "owns" this AbstractSQLObject instance. */
    protected transient Object parentObject;
    /** int representation of object type */
    protected int type;
    private transient boolean isIdSet = false;

    /** Creates a new instance of SQLBasicObject */
    public AbstractSQLObject() {
    }

    /**
     * all sql objects are cloneable
     * 
     * @return cloned SQL object
     * @throws CloneNotSupportedException - exception
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Copy
     * 
     * @param source - source
     */
    public void copyFromSource(SQLObject source) {
        if (source == null) {
            throw new java.lang.IllegalArgumentException("Require non-null SQLObject instance:" + source);
        }
        this.displayName = source.getDisplayName();
        // id is set only once for an object
        if (this.getId() == null) {
            this.id = source.getId();
        }

        this.type = source.getObjectType();
        this.parentObject = source.getParentObject();

        // clone attributes
        Collection attrNames = source.getAttributeNames();
        Iterator it = attrNames.iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            Attribute attr = source.getAttribute(name);
            if (attr != null) {
                try {
                    Attribute copiedAttr = (Attribute) attr.clone();
                    this.attributes.put(name, copiedAttr);
                } catch (CloneNotSupportedException ex) {
                    // log me
                    mLogger.errorNoloc(mLoc.t("EDIT105: Failed to copy source objects{0}", LOG_CATEGORY), ex);
                }
            }
        }
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
        }

        boolean response = false;

        if (o instanceof AbstractSQLObject) {
            AbstractSQLObject target = (AbstractSQLObject) o;

            // check for type
            response = (type == target.type);

            // check for display name
            response &= (this.getDisplayName() != null) ? this.getDisplayName().equals(target.getDisplayName()) : (target.getDisplayName() == null);

            // check for attributes
            response &= (attributes != null) ? attributes.equals(target.attributes) : (target.attributes == null);

            // check for id
            // FOR NOW we check if both ids are avialable then only do equal
            // comparison, TODO: in future we should always do id comparison
            // we need to make sure that id is always available for that.
            if (this.id != null && target.id != null) {
                response &= (this.id != null) ? this.id.equals(target.getId()) : (target.getId() == null);
            }
        }

        return response;
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
    public Object getAttributeObject(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    /**
     * Gets List of child SQLObjects belonging to this instance.
     * 
     * @return List of child SQLObjects
     */
    public List getChildSQLObjects() {
        return new ArrayList(1);
    }

    /**
     * @see SQLObject#getDisplayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @see SQLObject#getFooter
     */
    public String getFooter() {
        StringBuilder buf = new StringBuilder();
        buf.append("</").append(TAG_SQLOBJECT).append(">\n");
        return buf.toString();
    }

    /**
     * @see SQLObject#getHeader
     */
    public String getHeader() {
        String strType = "";
        try {
            strType = TagParserUtility.getStringType(this.getObjectType());
        } catch (BaseException e) {
            mLogger.infoNoloc(mLoc.t("EDIT106: Failed to get type attr.{0}", LOG_CATEGORY));
            strType = "UNKNOWN_TYPE";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("<" + TAG_SQLOBJECT + " " + OBJECT_TYPE + "=\"");
        if (strType != null) {
            buffer.append(strType);
        }

        buffer.append("\" " + ID + "=\"");
        if (id != null) {
            buffer.append(id.trim());
        }

        buffer.append("\" " + DISPLAY_NAME + "=\"");
        if (this.getDisplayName() != null) {
            buffer.append(XmlUtil.escapeXML(this.getDisplayName().trim()));
        }
        buffer.append("\">\n");

        return buffer.toString();
    }

    /**
     * @see SQLObject#getId
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see SQLObject#getJdbcType
     */
    public int getJdbcType() {
        return SQLConstants.JDBCSQL_TYPE_UNDEFINED;
    }

    /**
     * @see SQLObject#getObjectType
     */
    public int getObjectType() {
        return type;
    }

    /**
     * @see SQLObject#getOutput(java.lang.String)
     */
    public SQLObject getOutput(String argName) throws BaseException {
        return this;
    }

    /**
     * @see SQLObject#getParentObject
     */
    public Object getParentObject() {
        return this.parentObject;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        return type + ((id != null) ? id.hashCode() : 0) + ((attributes != null) ? attributes.hashCode() : 0);
    }

    /**
     * @see SQLObject#parseXML
     */
    public void parseXML(Element xmlElement) throws BaseException {
        parseCommonAttributesAndTags(xmlElement);
    }

    /**
     * Clear id and parent object
     */
    public void reset() {
        this.id = null;
        this.isIdSet = false;
        this.parentObject = null;
    }

    /**
     * @see SQLObject#secondPassParse
     */
    public void secondPassParse(Element element) throws BaseException {
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
     * @see SQLObject#setDisplayName
     */
    public void setDisplayName(String newName) {
        displayName = (newName != null) ? newName.trim() : "";
    }

    /**
     * @see SQLObject#setId
     */
    public void setId(String newId) throws BaseException {
        // ID will be set only once in this object's lifetime.
        if (isIdSet) {
            return;
        }

        if (newId == null) {
            throw new BaseException("Must supply non-null String ref for newId.");
        }

        isIdSet = true;
        this.id = newId;
    }

    /**
     * @see SQLObject#setJdbcType(int)
     */
    public void setJdbcType(int newType) {
        // Ignore for default implementation.
    }

    /**
     * @see SQLObject#setParentObject
     */
    public void setParentObject(Object newParent) throws BaseException {
        if (newParent == null) {
            throw new BaseException("Must supply non-null Object ref for newParent.");
        }
        this.parentObject = newParent;
    }

    /**
     * Generates XML document representing this object's content, using the given String
     * as a prefix for each line.
     * 
     * @param prefix String to be prepended to each line of the generated XML document
     * @return String containing XML representation
     * @exception BaseException - exception
     * @see SQLObject#toXMLString(java.lang.String)
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder buf = new StringBuilder(200);

        buf.append(prefix).append(getHeader());
        buf.append(toXMLAttributeTags(prefix));
        buf.append(prefix).append(getFooter());

        return buf.toString();
    }

    /**
     * Parse common attributes
     * 
     * @param xmlElement - element
     * @throws BaseException - exception
     */
    protected void parseCommonAttributesAndTags(Element xmlElement) throws BaseException {
        // Some sql framework parsing elements can be in AbstractSQLObject.
        if (xmlElement == null) {
            throw new BaseException("xmlElement is null");
        }
        if (this.parentObject == null) {
            throw new BaseException("ParentObject is null");
        }

        setDisplayName(xmlElement.getAttribute(SQLObject.DISPLAY_NAME));
        setId(xmlElement.getAttribute(SQLObject.ID));

        NodeList list = xmlElement.getChildNodes();
        parseAttributeList(list);
    }

    /**
     * Generates XML elements representing this object's associated attributes.
     * 
     * @param prefix Prefix string to be prepended to each element
     * @return String containing XML representation of attributes
     */
    protected String toXMLAttributeTags(String prefix) {
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

    private void parseAttributeList(NodeList list) throws BaseException {
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) list.item(i);
                if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
                    Attribute attr = new Attribute();
                    attr.parseXMLString(elem);
                    this.attributes.put(attr.getAttributeName(), attr);
                }
            }
        }
    }
}

