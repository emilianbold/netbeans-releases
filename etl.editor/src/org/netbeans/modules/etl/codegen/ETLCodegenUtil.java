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

import java.util.UUID;
import org.netbeans.modules.etl.utils.ETLDeploymentConstants;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import com.sun.sql.framework.exception.BaseException;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBConnectionDefinitionImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
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
    //private static final String LOG_CATEGORY = ETLCodegenUtil.class.getName();

    //private static final MessageManager MESSAGE_MGR = MessageManager.getManager(ETLCodelet.class);
    //private static final String IS_SECTION = "IS Configuration" ;
    //private static final String IS_WORKSPACE_DIR = "WORKSPACEDIR" ;
//    /**
//     * Returns Folder for Monitor DB. Specific to each Collaboration/CMap service.
//     * @param deployable
//     * @return Parameterized Monitor DB path.
//     * @throws BaseException
//     */
//    public static String getMonitorDBDir(Deployable deployable) throws BaseException {
//        return getMonitorDBDir(deployable, ETLDeploymentConstants.PARAM_APP_DATAROOT);
//    }
//
//
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
//    public static String getRSWorkspaceDir (CodeGenFramework framework, ErrorBundle errorBundle,
//                                            ProjectDeployment projDeployment, Deployable deployable) {
//            String errorMessage = null;
//            String svcName = "eTL Service" ;
//            IConfiguration iConfig = null;
//            String tRSWorkspaceDir = null;
//            String runtimeServerWorkspaceDir = null;
//            ETLDefinitionProjectElement etlDef = null;
//            int i = 1;
//
//            if (errorBundle == null) {
//                errorBundle = framework.createErrorBundle();
//            }
//
//            Collection tc = errorBundle.getErrors() ;
//            if (tc != null) {
//                i = (i <= tc.size())?(tc.size()+1): i;
//            }
//
//            try {
//                etlDef = (ETLDefinitionProjectElement) ((ProcessingNode) deployable).getProcessingDefinition();
//                EnvironmentElement ee =projDeployment.getDeployedElement(deployable, projDeployment.getEnvironment());
//                svcName = deployable.getName();
//
//                if ((ee != null) && (ee instanceof IntegrationServer)){
//                    IntegrationServer is = (IntegrationServer) ee;
//
//                    iConfig = ETLCodegenHelper.getIConfiguration(is.getConfiguration());
//
//                    if ((iConfig != null) && (iConfig.getSection(IS_SECTION) != null)) {
//                        ISection section = iConfig.getSection(IS_SECTION) ;
//                        IParameter param = section.getParameter(IS_WORKSPACE_DIR);
//                        if ((param != null) && (param.getValue() != null)) {
//                            tRSWorkspaceDir = param.getValue().toString();
//                        }
//
//                        if ((tRSWorkspaceDir == null) || ("".equals(tRSWorkspaceDir.trim()))){
//                            errorMessage = MESSAGE_MGR.getString("ERR_RS_WORKSPACE_DIR_NOT_PRESENT", is.getName() );
//                            errorBundle.addError(framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//                            runtimeServerWorkspaceDir = null;
//                        }else {
//                            runtimeServerWorkspaceDir = tRSWorkspaceDir;
//                        }
//
//                    }else {
//                        errorMessage = MESSAGE_MGR.getString("ERR_GETTING_RS_CONFIG", svcName );
//                        errorBundle.addError(framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//                    }
//                }else {
//                    errorMessage = MESSAGE_MGR.getString("ERR_COLLAB_SVC_NOT_DEPLOYED", svcName);
//                    errorBundle.addError(framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//                }
//            }catch (RepositoryException re) {
//                errorMessage = MESSAGE_MGR.getString("ERR_GETTING_RS_CONFIG", svcName );
//                errorBundle.addError(framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//
//            }catch (Exception ex) {
//                errorMessage = MESSAGE_MGR.getString("ERR_GETTING_RS_CONFIG", svcName);
//                errorBundle.addError(framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//
//            }
//
//            return runtimeServerWorkspaceDir;
//    }
    /**
     * Returns Folder for Monitor DB. Specific to each Collaboration/CMap service.
     * @return Parameterized engine instance DB path.
     * @throws BaseException
     */
    public static String getEngineInstanceWorkingFolder() {
        return getEngineInstanceWorkingFolder(ETLDeploymentConstants.PARAM_APP_DATAROOT);
    }

    /**
     * Returns Folder for Monitor DB. Specific to each Collaboration/CMap service.
     * @return Parameterized engine instance DB path.
     * @throws BaseException
     */
    public static String getEngineInstanceWorkingFolder(String appWorkspaceDirectory) {
        String repOID = String.valueOf(System.currentTimeMillis());
        String engineInstanceDBFolder = null;
        // Use "/" as codegen OS may be different than OS on which deployed.
        engineInstanceDBFolder = appWorkspaceDirectory + "/" + ETL_FOLDER + "/i/" + repOID + "/" + ETLDeploymentConstants.INSTANCE_DB + "/";
        return engineInstanceDBFolder;
    }

    /**
     * Returns Folder for Monitor DB. Specific to each Collaboration/CMap service.
     * @return Parameterized engine instance DB path.
     * @throws BaseException
     */
    public static String getUniqueEngineInstanceWorkingFolder(String appWorkspaceDirectory, String dbInstanceName) {
        String engineInstanceDBFolder = null;
        String repoID = String.valueOf(System.currentTimeMillis());

        // Use "/" as codegen OS may be different than OS on which deployed.
        engineInstanceDBFolder = ETLDeploymentConstants.PARAM_APP_DATAROOT + "/" + ETL_FOLDER + "/i/" + repoID + "/" + dbInstanceName + "/";
        return engineInstanceDBFolder;
    }

    public static FlatfileDefinition getFFDefinition(DBTable table) {
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

                    FlatfileDefinition fd = new FlatfileDefinition(model.getModelName());
                    FlatfileDBConnectionDefinitionImpl ffdbConndef = new FlatfileDBConnectionDefinitionImpl(condef.getName(), condef.getDriverClass(),
                            condef.getConnectionURL(), condef.getUserName(), condef.getPassword(),
                            condef.getDescription());

                    FlatfileDatabaseModel fdm = new FlatfileDatabaseModelImpl(model.getModelName(),
                            ffdbConndef);
                    Iterator iterator = model.getTables().iterator();
                    HashMap fftables = new HashMap();
                    while (iterator.hasNext()) {
                        SQLDBTable element = (SQLDBTable) iterator.next();
                        element.getClass();
                        FlatfileDBTableImpl ft = new FlatfileDBTableImpl(element);
                        HashMap map = ((SQLDBModelImpl) model).getTableMetaData(condef, element);
                        ft.setProperties(map);
                        fftables.put(element.getName(), ft);
                    }
                    fdm.setTables(fftables);
                    fd.setFlatfileDatabaseModel(fdm);
//                return (FlatfileDefinition) table.getParent().getSource();
                    return fd;
                }
            }
        
        return null;
    }

    public static FlatfileDefinition getStcdbObjectTypeDefinition(DBTable table) {
        if (table.getParent().getSource() != null) {
            Object obj = table.getParent().getSource();
            if (obj instanceof FlatfileDefinition) {
                return (FlatfileDefinition) obj;
            }
        } else {
            return getFFDefinition(table);
        }
        return (FlatfileDefinition) null;
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
