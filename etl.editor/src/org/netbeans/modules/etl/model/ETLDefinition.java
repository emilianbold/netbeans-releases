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

package org.netbeans.modules.etl.model;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectListener;
import org.w3c.dom.Element;
import com.sun.etl.exception.BaseException;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.ValidationInfo;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 *
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
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of target DatabaseModels
     */
    public List<SQLDBModel> getTargetDatabaseModels();

    /**
     * Gets the List of TargetTables
     *
     * @return List, possibly empty, of TargetTables
     */
    public List<DBTable> getTargetTables();

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
    public List<ValidationInfo> validate();
    
   /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> badgeValidate();
}