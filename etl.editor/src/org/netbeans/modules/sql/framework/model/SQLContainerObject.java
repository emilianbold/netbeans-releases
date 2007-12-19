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

package org.netbeans.modules.sql.framework.model;

import java.util.Collection;
import org.w3c.dom.Element;
import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public interface SQLContainerObject {

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     *
     * @param newObject new instance to add
     * @throws BaseException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws BaseException;

    /**
     * Adds SQLObject to list of object references to be resolved in a second pass.
     *
     * @param sqlObj to be added
     * @param element DOM Element of SQLObject to be resolved later
     */
    public void addSecondPassSQLObject(SQLObject sqlObj, Element element);

    /**
     * all sql objects are cloneable
     * @return cloned sql object
     * @throws java.lang.CloneNotSupportedException
     */
    public Object cloneSQLObject() throws CloneNotSupportedException;

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
    public SQLObject createObject(String objTag) throws BaseException;

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
    public SQLObject createSQLObject(String className) throws BaseException;

    /**
     * Gets the Collection of active SQLObjects.
     *
     * @return Collection of current SQLObjects in this SQLDefinition instance.
     */
    public Collection<SQLObject> getAllObjects();

    /**
     * Gets associated SQLObject instance, if any, with the given object ID.
     *
     * @param objectId ID of SQLObject instance to be retrieved
     * @param type type of object to retrieve
     * @return associated SQLObject instance, or null if no such instance exists
     */
    public SQLObject getObject(String objectId, int type);

    /**
     * Gets a Collection of SQLObjects, if any, with the given type
     *
     * @param type SQLObject type to retrieve
     * @return Collection (possibly empty) of SQLObjects with the given type
     */
    public Collection getObjectsOfType(int type);

    /**
     * Gets parent object, if any, that owns this SQLDefinition instance.
     *
     * @return parent object
     */
    public Object getParent();

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     *
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @throws BaseException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws BaseException;

    /**
     * Remove all objects from this container
     */
    public void removeAllObjects();

    /**
     * Removes the given object from SQLDefinition
     *
     * @param sqlObj to be removed
     * @throws BaseException while removing
     */
    public void removeObject(SQLObject sqlObj) throws BaseException;

    /**
     * Removes SQLObjects passed.
     *
     * @param sqlObjs Collection of SQLObjects to be removed
     * @throws BaseException while removing
     */
    public void removeObjects(Collection sqlObjs) throws BaseException;

    /**
     * Sets parent object, if any, that owns this SQLDefinition instance.
     *
     * @param newParent new parent object
     */
    public void setParent(Object newParent);

    /**
     * Returns the XML representation of collabSegment.
     *
     * @param prefix the xml.
     * @return Returns the XML representation of colabSegment.
     * @throws com.sun.sql.framework.exception.BaseException
     */
    public String toXMLString(String prefix) throws BaseException;
}