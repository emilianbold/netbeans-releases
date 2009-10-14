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
package org.netbeans.modules.sql.framework.common.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;

/**
 * Class representing Physical table such that for any two instances t1 and t2 if
 * (t1.equals(t2)) then both instances represents the same physical table.
 *
 * @author Girish Patil
 * @version $Revision$
 */
public class PhysicalTable {

    private static boolean SKIP_CONNECTION_URL = false;
    //Preferably one of the two should be true to assert two tables are equal.
    private static boolean SKIP_DB_NAME = true;
    private static transient final Logger mLogger = Logger.getLogger(PhysicalTable.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Creates new instance of PhysicalTable for given DBTable.
     *
     * @param sqlTable
     * @return
     */
    public static PhysicalTable getPhysicalTable(DBTable sqlTable) {
        PhysicalTable pt = null;
        if (sqlTable != null) {
            pt = new PhysicalTable();
            pt.setCatalog(sqlTable.getCatalog());
            pt.setName(sqlTable.getName());
            pt.setSchema(sqlTable.getSchema());

            DatabaseModel dbModel = sqlTable.getParent();
            if (dbModel != null) {
                pt.setDbName(dbModel.getModelName());
                DBConnectionDefinition dbConnDef = dbModel.getConnectionDefinition();
                if (dbConnDef != null) {
                    pt.setConnectionUrl(dbConnDef.getConnectionURL());
                }
            }
        }

        return pt;
    }

    /**
     * Returns list of PhysicalTable given given list of DBTable.
     *
     * @param sqlTable
     * @return
     */
    public static List<PhysicalTable> getPhysicalTableList(List<DBTable> dbTableList) {
        List<PhysicalTable> ptList = null;
        PhysicalTable pt = null;
        DBTable sqlTable;
        if (dbTableList != null) {
            ptList = new ArrayList<PhysicalTable>();
            for (Iterator<DBTable> itr = dbTableList.iterator(); itr.hasNext();) {
                sqlTable = itr.next();

                pt = new PhysicalTable();
                pt.setCatalog(sqlTable.getCatalog());
                pt.setName(sqlTable.getName());
                pt.setSchema(sqlTable.getSchema());

                DatabaseModel dbModel = sqlTable.getParent();
                if (dbModel != null) {
                    pt.setDbName(dbModel.getModelName());
                    DBConnectionDefinition dbConnDef = dbModel.getConnectionDefinition();
                    if (dbConnDef != null) {
                        pt.setConnectionUrl(dbConnDef.getConnectionURL());
                    }
                }
                ptList.add(pt);
            }
        }

        return ptList;
    }
    private String mCatalog;
    private String mConnectionUrl;
    private String mName;
    private String mDbName;
    private String mSchema;

    @Override
    public boolean equals(Object other) {
        boolean eql = true;
        PhysicalTable otherTable = null;
        String blank = "";

        if (!(other instanceof PhysicalTable)) {
            eql = false;
            mLogger.infoNoloc(mLoc.t("EDIT101: Class cast request for:{0}", other));
        } else {
            otherTable = (PhysicalTable) other;
        }

        //Check Catalog
        if (eql) {
            if ((this.mCatalog == null) || blank.equals(mCatalog)) {
                if (!((otherTable.getCatalog() == null) || blank.equals(otherTable.getCatalog()))) {
                    eql = false;
                }
            } else {
                if (!this.mCatalog.equals(otherTable.getCatalog())) {
                    eql = false;
                }
            }
        }

        //Check Schema
        if (eql) {
            if ((this.mSchema == null) || blank.equals(mSchema)) {
                if (!((otherTable.getSchema() == null) || blank.equals(otherTable.getSchema()))) {
                    eql = false;
                }
            } else {
                if (!this.mSchema.equals(otherTable.getSchema())) {
                    eql = false;
                }
            }
        }

        //Check Name
        if (eql) {
            if ((this.mName == null) || blank.equals(mName)) {
                if (!((otherTable.getName() == null) || blank.equals(otherTable.getName()))) {
                    eql = false;
                }
            } else {
                if (!this.mName.equals(otherTable.getName())) {
                    eql = false;
                }
            }
        }

        //Check DbName
        if (eql && (!SKIP_DB_NAME)) {
            if ((this.mDbName == null) || blank.equals(mDbName)) {
                if (!((otherTable.getDbName() == null) || blank.equals(otherTable.getDbName()))) {
                    eql = false;
                }
            } else {
                if (!this.mDbName.equals(otherTable.getDbName())) {
                    eql = false;
                }
            }
        }

        //Check ConnectionUrl
        if (eql && (!SKIP_CONNECTION_URL)) {
            if ((this.mConnectionUrl == null) || blank.equals(mConnectionUrl)) {
                if (!((otherTable.getConnectionUrl() == null) || blank.equals(otherTable.getConnectionUrl()))) {
                    eql = false;
                }
            } else {
                if (!this.mConnectionUrl.equals(otherTable.getConnectionUrl())) {
                    eql = false;
                }
            }
        }

        return eql;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.mCatalog != null ? this.mCatalog.hashCode() : 0);
        hash = 53 * hash + (this.mConnectionUrl != null ? this.mConnectionUrl.hashCode() : 0);
        hash = 53 * hash + (this.mName != null ? this.mName.hashCode() : 0);
        hash = 53 * hash + (this.mDbName != null ? this.mDbName.hashCode() : 0);
        hash = 53 * hash + (this.mSchema != null ? this.mSchema.hashCode() : 0);
        return hash;
    }

    /**
     * @return Returns the catalog.
     */
    public String getCatalog() {
        return mCatalog;
    }

    /**
     * @return Returns the connectionUrl.
     */
    public String getConnectionUrl() {
        return mConnectionUrl;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return mName;
    }

    /**
     * @return Returns the dbName.
     */
    public String getDbName() {
        return mDbName;
    }

    /**
     * @return Returns the schema.
     */
    public String getSchema() {
        return mSchema;
    }

    /**
     * @param catalog The catalog to set.
     */
    public void setCatalog(String catalog) {
        this.mCatalog = catalog;
    }

    /**
     * @param schema The schema to set.
     */
    public void setConnectionUrl(String cUrl) {
        this.mConnectionUrl = cUrl;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * @param dbName The dbName to set.
     */
    public void setDbName(String dbName) {
        this.mDbName = dbName;
    }

    /**
     * @param schema The schema to set.
     */
    public void setSchema(String schema) {
        this.mSchema = schema;
    }
}
