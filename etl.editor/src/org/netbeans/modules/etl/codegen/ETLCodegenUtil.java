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
package org.netbeans.modules.etl.codegen;

import java.util.UUID;

import org.netbeans.modules.etl.utils.ETLDeploymentConstants;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;

import com.sun.sql.framework.exception.BaseException;

/**
 * Utility methods for generating engine file, Please note the class modifiers should be
 * scoped to default to restrict their access to members of this package only.
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
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
            String repOID = UUID.randomUUID().toString() ;
            
            // Use "/" for maximum portability as codegen OS may be different than  OS on 
            // which service is deployed.
            monitorDBFolder = workspaceDir + "/" + ETL_FOLDER + "/m/" + repOID + "/" + deployable  + "/" ;
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
        String engineInstanceDBFolder = null;
        // Use "/" as codegen OS may be different than OS on which deployed.
        engineInstanceDBFolder = appWorkspaceDirectory + "/"
                               + ETL_FOLDER + "/i/"
                               + ETLDeploymentConstants.PARAM_INSTANCE_DB_NAME+ "/";
        return engineInstanceDBFolder;
    }
    
    public static FlatfileDefinition getOtd(DBTable table) {
        if(table.getParent().getSource() != null) {
            if (table.getParent().getSource() instanceof FlatfileDefinition) {
            	return (FlatfileDefinition) table.getParent().getSource() ;
            }
        }
        return null;
    }

    public static FlatfileDefinition getStcdbObjectTypeDefinition(DBTable table) {
        if(table.getParent().getSource() != null) {
            Object obj = table.getParent().getSource();
            if (obj instanceof FlatfileDefinition){
            	return (FlatfileDefinition)obj;
            }
        }
        return (FlatfileDefinition)null;
    }

    public static String getQualifiedOtdOid(SQLDBModel dbModel) {
        String key = null;

        try {
            key = dbModel.getSource().getObjectId();
            if (dbModel.getObjectType() == SQLConstants.TARGET_DBMODEL) {
                key =  createTargetPortNameFromOID(key);
            }else {
                key =  createSourcePortNameFromOID(key);
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
            }else {
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
