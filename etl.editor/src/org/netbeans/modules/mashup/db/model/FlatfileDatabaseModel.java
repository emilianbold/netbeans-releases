/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mashup.db.model;

import java.util.Map;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.JDBCConnectionProvider;
import org.netbeans.modules.sql.framework.model.SQLDBModel;


/**
 * Root interface to be implemented by ETL-compatible flatfile data sources that provide
 * information in a row-and-column addressable format. Extends DatabaseModel to support
 * collection of flatfiles as analogues for a database instance of tables. This model
 * represents a collection of one or more flatfiles in a single directory.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDatabaseModel extends SQLDBModel, JDBCConnectionProvider {

    /**
     * Adds new SourceTable to the model.
     * 
     * @param table new DBTable to add
     */
    void addTable(FlatfileDBTable table);
    
    /**
     * Clones this object.
     * 
     * @return shallow copy of this ETLDataSource
     */
    Object clone();

    /**
     * Copies member values from those contained in the given FlatfileDatabaseModel
     * instance.
     * 
     * @param src DatabaseModel whose contents are to be copied into this instance
     */
    void copyFrom(FlatfileDatabaseModel src);

    /**
     * @see java.lang.Object#equals
     */
    boolean equals(Object refObj);

    /**
     * Gets name of DBConnectionDefinition associated with this database model.
     * 
     * @return name of associated DBConnectionDefinition instance
     */
    String getConnectionName();
    
    /**
     * Gets Flatfile instance, if any, whose file name matches the given String
     * 
     * @param aName file name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    FlatfileDBTable getFileMatchingFileName(String aName);

    /**
     * Gets Flatfile instance, if any, whose table name matches the given String.
     * 
     * @param tableName table name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    FlatfileDBTable getFileMatchingTableName(String tableName);

    DBConnectionDefinition getFlatfileDBConnectionDefinition(boolean download);

    Map getFlatfileTablePropertyMap(String flatfileName);

    Map getFlatfileTablePropertyMaps();

    int getMajorVersion();

    int getMicroVersion();

    int getMinorVersion();

    String getVersionString();

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    int hashCode();

    /**
     * Setter for FlatfileDBConnectionDefinition
     * 
     * @param theConnectionDefinition to be set
     */
    void setConnectionDefinition(DBConnectionDefinition theConnectionDefinition);

    /**
     * Sets the Connection Name associated with connection name
     * 
     * @param theConName associated with this DataSource
     */
    void setConnectionName(String theConName);

    /**
     * Sets repository object, if any, providing underlying data for this DatabaseModel
     * implementation.
     * 
     * @param obj FlatfileDefinition hosting this object's metadata, or null if data are not
     *        held by a StcdbObjectTypeDefinition.
     */
    void setSource(FlatfileDefinition obj);


    /**
     * Setter for tables
     * 
     * @param theTables to be part of Model
     */
    void setTables(Map theTables);

    /**
     * Overrides default implementation to return name of this DatabaseModel.
     * 
     * @return model name.
     */
    String toString();

}

