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
import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLObjectListener;
import org.w3c.dom.Element;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.DatabaseModel;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.ValidationInfo;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 *
 */
public interface MashupDefinition {

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     *
     * @param newObject new instance to add
     * @throws EDMException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws EDMException;

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
    public Collection<SQLObject> getAllObjects();

    /**
     * Gets the List of Databases
     *
     * @return java.util.List for this
     */
    public List<SQLDBModel> getAllDatabases();

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
    public List<SQLDBModel> getSourceDatabaseModels();

    /**
     * Gets the List of SourceTables
     *
     * @return List, possibly empty, of SourceTables
     */
    public List<DBTable> getSourceTables();

    /**
     * get the sql definition
     *
     * @return sqldefinition
     */
    public SQLDefinition getSQLDefinition();

     /**
     * get the version
     *
     * @return version
     */
    public String getVersion();

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     *
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @throws EDMException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws EDMException;

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
    public String toXMLString(String prefix) throws EDMException;

    /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> validate();
    
   /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> badgeValidate();
}