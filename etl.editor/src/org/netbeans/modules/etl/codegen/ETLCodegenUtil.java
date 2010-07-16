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
package org.netbeans.modules.etl.codegen;

import org.netbeans.modules.etl.utils.ETLDeploymentConstants;

import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import com.sun.etl.exception.BaseException;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.impl.SQLDBModelImpl;

/**
 * Utility methods for generating engine file, Please note the class modifiers should be
 * scoped to default to restrict their access to members of this package only.
 *
 * @author Ahimanikya Satapathy
 */
public class ETLCodegenUtil {

    private static final String ETL_FOLDER = "eTL";

    public static String getMonitorDBDir(String deployable, String workspaceDir) throws BaseException {
        String monitorDBFolder = null;
        try {
            String repOID = String.valueOf(System.currentTimeMillis());

            // Use "/" for maximum portability as codegen OS may be different than  OS on
            // which service is deployed.
            monitorDBFolder = workspaceDir + "/" + ETL_FOLDER + "/m/" + repOID + "/" + deployable + "/";
        } catch (Exception ex) {
            throw new BaseException("Cannot create eTL monitor log folder.", ex);
        }

        return monitorDBFolder;
    }

    public static String getEngineInstanceWorkingFolder() {
        return getEngineInstanceWorkingFolder(ETLDeploymentConstants.PARAM_APP_DATAROOT);
    }

    public static String getEngineInstanceWorkingFolder(String appWorkspaceDirectory) {
        String repOID = String.valueOf(System.currentTimeMillis());
        String engineInstanceDBFolder = null;
        // Use "/" as codegen OS may be different than OS on which deployed.
        engineInstanceDBFolder = appWorkspaceDirectory + "/" + ETL_FOLDER + "/i/" + repOID + "/" + ETLDeploymentConstants.INSTANCE_DB + "/";
        return engineInstanceDBFolder;
    }

    public static String getUniqueEngineInstanceWorkingFolder(String appWorkspaceDirectory, String dbInstanceName) {
        String engineInstanceDBFolder = null;
        String repoID = String.valueOf(System.currentTimeMillis());

        // Use "/" as codegen OS may be different than OS on which deployed.
        engineInstanceDBFolder = ETLDeploymentConstants.PARAM_APP_DATAROOT + "/" + ETL_FOLDER + "/i/" + repoID + "/" + dbInstanceName + "/";
        return engineInstanceDBFolder;
    }

    public static VirtualDBDefinition getFFDefinition(DBTable table) {
        if (table.getParent().getSource() == null) {
                DatabaseModel model = table.getParent();
                DBConnectionDefinition condef = model.getConnectionDefinition();
                try {
                    Class.forName("org.axiondb.jdbc.AxionDriver");
                } catch (ClassNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();

                }

                if (condef.getDriverClass().equals("org.axiondb.jdbc.AxionDriver")) {

                    VirtualDBDefinition fd = new VirtualDBDefinition(model.getModelName());
                    VirtualDBConnectionDefinition ffdbConndef = new VirtualDBConnectionDefinition(condef.getName(), condef.getDriverClass(),
                            condef.getConnectionURL(), condef.getUserName(), condef.getPassword());

                    VirtualDatabaseModel fdm = new VirtualDatabaseModel(model.getModelName(),
                            ffdbConndef);
                    Iterator iterator = model.getTables().iterator();
                    HashMap fftables = new HashMap();
                    while (iterator.hasNext()) {
                        SQLDBTable element = (SQLDBTable) iterator.next();
                        VirtualDBTable ft = new VirtualDBTable();
                        Iterator iter = element.getColumnList().iterator();

                        while (iter.hasNext()) {
                            SQLDBColumn col = (SQLDBColumn)iter.next();
                            ft.addColumn(new VirtualDBColumn(col.getName(), col.getJdbcType(), col.getPrecision(), col.getScale(), col.isPrimaryKey(), col.isForeignKey(), col.isIndexed(), col.isNullable()));
                        }
                        HashMap map = ((SQLDBModelImpl) model).getTableMetaData(condef, element);
                        ft.setProperties(map);
                        fftables.put(element.getName(), ft);
                    }
                    fdm.setTables(fftables);
                    fd.setVirtualDatabaseModel(fdm);
                    return fd;
                }
            }
        
        return null;
    }

    public static VirtualDBDefinition getStcdbObjectTypeDefinition(DBTable table) {
        if (table.getParent().getSource() != null) {
            Object obj = table.getParent().getSource();
            if (obj instanceof VirtualDBDefinition) {
                return (VirtualDBDefinition) obj;
            }
        } else {
            return getFFDefinition(table);
        }
        return (VirtualDBDefinition) null;
    }

    public static String getQualifiedObjectId(SQLDBModel dbModel) {
        String key = null;

        try {
            key = dbModel.getSource().getObjectId();
            if (dbModel.getObjectType() == SQLConstants.TARGET_DBMODEL) {
                key = createTargetPortNameFromOID(key);
            } else {
                key = createSourcePortNameFromOID(key);
            }
        } catch (Exception ex) {
            key = null;
        }

        return key;
    }

    public static String getQualifiedConnectionDefinitionName(SQLDBModel dbModel, String name) {
        String qName = name;
        try {
            if (dbModel.getObjectType() == SQLConstants.TARGET_DBMODEL) {
                qName = "T_" + qName;
            } else {
                qName = "S_" + qName;
            }
        } catch (Exception ex) {
            qName = null;
        }

        return qName;
    }

    public static String createSourcePortNameFromOID(String oid) {
        return oid + SQLConstants.SOURCE_DB_MODEL_NAME_SUFFIX;
    }

    public static String createTargetPortNameFromOID(String oid) {
        return oid + SQLConstants.TARGET_DB_MODEL_NAME_SUFFIX;
    }
}
