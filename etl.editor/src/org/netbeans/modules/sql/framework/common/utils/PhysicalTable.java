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
package org.netbeans.modules.sql.framework.common.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.DatabaseModel;

import com.sun.sql.framework.utils.Logger;

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
    private static boolean SKIP_OTD_NAME = true;

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
                pt.setOtdName(dbModel.getModelName());
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
    public static List getPhysicalTableList(List dbTableList) {
        List ptList = null;
        PhysicalTable pt = null;
        DBTable sqlTable;
        if (dbTableList != null) {
            Iterator itr = dbTableList.iterator();
            ptList = new ArrayList();

            while (itr.hasNext()) {
                sqlTable = (DBTable) itr.next();

                pt = new PhysicalTable();
                pt.setCatalog(sqlTable.getCatalog());
                pt.setName(sqlTable.getName());
                pt.setSchema(sqlTable.getSchema());

                DatabaseModel dbModel = sqlTable.getParent();
                if (dbModel != null) {
                    pt.setOtdName(dbModel.getModelName());
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

    private String mOtdName;
    private String mSchema;

    public boolean equals(Object other) {
        boolean eql = true;
        PhysicalTable otherTable = null;
        String blank = "";

        if (!(other instanceof PhysicalTable)) {
            eql = false;
            Logger.print(Logger.DEBUG, PhysicalTable.class.getName(), "Class cast request for:" + other);
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

        //Check OtdName
        if (eql && (!SKIP_OTD_NAME)) {
            if ((this.mOtdName == null) || blank.equals(mOtdName)) {
                if (!((otherTable.getOtdName() == null) || blank.equals(otherTable.getOtdName()))) {
                    eql = false;
                }
            } else {
                if (!this.mOtdName.equals(otherTable.getOtdName())) {
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
     * @return Returns the otdName.
     */
    public String getOtdName() {
        return mOtdName;
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
     * @param otdName The otdName to set.
     */
    public void setOtdName(String otdName) {
        this.mOtdName = otdName;
    }

    /**
     * @param schema The schema to set.
     */
    public void setSchema(String schema) {
        this.mSchema = schema;
    }

}

