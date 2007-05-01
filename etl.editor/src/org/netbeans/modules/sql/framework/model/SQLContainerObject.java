/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    public Collection getAllObjects();

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
     * @throws BaseException if error occurs while setting parent object
     */
    public void setParent(Object newParent);

    /**
     * Returns the XML representation of collabSegment.
     * 
     * @param prefix the xml.
     * @return Returns the XML representation of colabSegment.
     */
    public String toXMLString(String prefix) throws BaseException;
}

