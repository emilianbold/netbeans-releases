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
package org.netbeans.modules.edm.model;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.Attribute;

/**
 * Root interface for all objects in the UI Object Model. Classes which implement this
 * interface are considered to be valid inputs to any instance of a class implementing
 * SQLConnectableObject.
 * 
 * @author Jonathan Giron
 */
public interface SQLObject extends Cloneable {

    /** XML attribute name for argument name */
    public static final String ATTR_ARGNAME = "argName";

    // xml tag constants
    /** XML attribute name for display name */
    public static final String DISPLAY_NAME = "displayName";

    /** XML attribute name for ID */
    public static final String ID = "id";

    /** XML attribute name for object type */
    public static final String OBJECT_TYPE = "objectType";

    /** XML tag name for input */
    public static final String TAG_INPUT = "input";

    /** XML tag name for SQLObject (root element) */
    public static final String TAG_SQLOBJECT = "sql-object";

    /** XML attribute name for type */
    public static final String TYPE = "type";

    /**
     * all sql objects are cloneable
     */
    public Object cloneSQLObject() throws CloneNotSupportedException;

    /**
     * Gets an Attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName);

    /**
     * Gets Collection of active attribute names.
     * 
     * @return Collection of attribute names
     */
    public Collection getAttributeNames();

    /**
     * Gets the object referenced by a named Attribute, if it exists.
     * 
     * @param attrName attribute Name
     * @return Object referenced by Attributed with name attrName, or null if none exists
     */
    public Object getAttributeObject(String attrName);

    /**
     * Gets List of child SQLObjects belonging to this instance.
     * 
     * @return List of child SQLObjects
     */
    public List getChildSQLObjects();

    /**
     * Gets display name of this SQLObject instance.
     * 
     * @return display name
     */
    public String getDisplayName();

    /**
     * Gets the XML footer string for this instance; called by subclasses while generating
     * XML output.
     * 
     * @return String footer
     */
    public String getFooter();

    /**
     * Gets the XML header string for this instance; called by subclasses while generating
     * XML output.
     * 
     * @return String header
     */
    public String getHeader();

    /**
     * Gets unique ID for this instance of SQLObject.
     * 
     * @return String representing unique instance ID
     */
    public String getId();

    /**
     * Gets JDBC type of output, if any.
     * 
     * @return JDBC type of output, or SQLConstants.JDBCSQL_TYPE_UNDEFINED if output is
     *         undefined for this instance
     */
    public int getJdbcType();

    /**
     * Gets specific type of SQLObject (as an enumerated int value) that this instance
     * represents.
     * 
     * @return int value representing specific object type
     */
    public int getObjectType();

    /**
     * Gets reference to SQLObject corresponding to given argument name that can be linked
     * to an SQLConnectableObject.
     * 
     * @param argName argument name of linkable SQLObject
     * @return linkable SQLObject corresponding to argName
     * @throws EDMException if object cannot be linked to an SQLConnectableObject
     */
    public SQLObject getOutput(String argName) throws EDMException;

    /**
     * Gets parent object for this SQLObject instance.
     * 
     * @return reference to parent object
     */
    public Object getParentObject();

    /**
     * Populates the member variables and collections of this SQLObject instance, parsing
     * the given DOM Element as the source for reconstituting its contents.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws EDMException if element is null or error occurs during parsing
     */
    public void parseXML(Element element) throws EDMException;

    /**
     * Clear id and parent object
     */
    public void reset();

    /**
     * Parses elements which require a second round of parsing to resolve their
     * references.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws EDMException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws EDMException;

    /**
     * Sets an attribute name-value pair. The name of the Attribute should be one of the
     * String constants defined in this class.
     * 
     * @param attrName attribute Name
     * @param val value of the attribute
     */
    public void setAttribute(String attrName, Object val);

    /**
     * Sets display name of this SQLObject instance.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets ID for this instance of SQLObject; must be unique in any collection of
     * SQLObjects within which this instance is a part.
     * 
     * @param newId new instance ID for this SQLObject; must be a unique value
     * @throws EDMException if newID is null or invalid, or if error occurs while setting
     *         ID value
     */
    public void setId(String newId) throws EDMException;

    /**
     * Sets JDBC type of output, if any.
     * 
     * @param newType new JDBC type of output; ignored if output is undefined for this
     *        instance
     */
    public void setJdbcType(int newType);

    /**
     * Sets parent object for this SQLObject instance.
     * 
     * @param newParent reference to new pagetObjectTyperent object.
     * @throws EDMException if newParent reference is null or error occurs while setting
     *         parent reference
     */
    public void setParentObject(Object newParent) throws EDMException;

    /**
     * Gets XML representation of this SQLObject, appending the given String to the
     * beginning of each new line.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */

    public String toXMLString(String prefix) throws EDMException;

}

