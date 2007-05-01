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

package org.netbeans.modules.etl.model;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectListener;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 * 
 * TODO: Sync with external metadata change. 
 */
public interface ETLDefinition {

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     *
     * @param newObject new instance to add
     * @throws BaseException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws BaseException;

    /**
     * add an sql object listener
     *
     * @param listener sql object listener
     */
    public void addSQLObjectListener(SQLObjectListener listener);

    /**
     * Gets Collection of all SQLObjects in this model.
     *
     * @return Collection, possibly empty, of all SQLObjects
     */
    public Collection getAllObjects();

    /**
     * Gets the List of OTDs
     *
     * @return java.util.List for this
     */
    public List getAllOTDs();

    /**
     * Getter for DatabaseModel
     *
     * @param modelName to be retrieved
     * @return DatabaseModel for given Model Name
     */
    public DatabaseModel getDatabaseModel(String modelName);

    /**
     * Gets display name.
     *
     * @return current display name
     */
    public String getDisplayName();

    /**
     * Gets execution stratergy code set for this collaboration.
     * @return execution stratergy code
     */
    public Integer getExecutionStrategyCode();

    /**
     * get the parent repository object
     *
     * @return parent repository object
     */
    public Object getParent();

    /**
     * get runtime db model
     *
     * @return runtime dbmodel
     */
    public RuntimeDatabaseModel getRuntimeDbModel();

    /**
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of source DatabaseModels
     */
    public List getSourceDatabaseModels();

    /**
     * Gets the List of SourceTables
     *
     * @return List, possibly empty, of SourceTables
     */
    public List getSourceTables();

    /**
     * get the sql definition
     *
     * @return sqldefinition
     */
    public SQLDefinition getSQLDefinition();

    /**
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of target DatabaseModels
     */
    public List getTargetDatabaseModels();

    /**
     * Gets the List of TargetTables
     *
     * @return List, possibly empty, of TargetTables
     */
    public List getTargetTables();

    /**
     * get the version
     *
     * @return version
     */
    public String getVersion();

    /**
     * Check if a java operator is used in the model.
     *
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators();

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
     * remove sql object listener
     *
     * @param listener sql object listener
     */
    public void removeSQLObjectListener(SQLObjectListener listener);

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets execution stratergy codefor this collaboration.
     * @param code execution stratergy code
     */
    public void setExecutionStrategyCode(Integer code);

    /**
     * set the parent repository object
     *
     * @param parent parent repository object
     */
    public void setParent(Object parent);

    /**
     * Returns the XML representation of collabSegment.
     *
     * @param prefix the xml.
     * @return Returns the XML representation of colabSegment.
     */
    public String toXMLString(String prefix) throws BaseException;

    /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List validate();
}

