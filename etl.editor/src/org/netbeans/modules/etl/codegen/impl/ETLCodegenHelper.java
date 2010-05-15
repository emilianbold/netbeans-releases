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
package org.netbeans.modules.etl.codegen.impl;

//import com.sun.jbi.ui.devtool.codegen.framework.model.CodeGenException;
//import com.sun.jbi.ui.devtool.configuration.IConfiguration;
//import com.sun.jbi.ui.devtool.configuration.factory.Factory;
//import com.sun.jbi.ui.devtool.connector.framework.util.ConfigurationHelper;
//import com.sun.jbi.ui.devtool.connector.repository.ExternalSystem;
//import com.sun.jbi.ui.devtool.connector.repository.ExternalSystemComposite;
//import com.sun.jbi.ui.devtool.deployment.repository.ProjectDeployment;
//import org.netbeans.modules.etl.codegen.impl.GenerateEngineFile.LocalMessageManager;
//import org.netbeans.modules.model.database.DBRuntimeEnvAccessor;
//import com.sun.jbi.ui.devtool.eways.services.dbsupport.DBRuntimeEnvAccessorFactory;
//import com.sun.jbi.ui.devtool.model.common.Configurable;
//import com.sun.jbi.ui.devtool.model.common.EnvironmentElement;
//import com.sun.jbi.ui.devtool.model.common.cme.CMLink;
//import com.sun.jbi.ui.devtool.model.common.cme.ConnectorConfiguration;
//import com.sun.jbi.ui.devtool.repository.RepositoryException;
//import com.sun.jbi.ui.devtool.sql.framework.exception.BaseException;
//import com.sun.jbi.ui.devtool.sql.framework.utils.Logger;

/**
 * @author Girish Patil
 * @version $Revision$
 */
public class ETLCodegenHelper {
//    private static final String LOG_CATEGORY = ETLCodegenHelper.class.getName();
//    private static final LocalMessageManager MESSAGE_MGR = new LocalMessageManager();
//
//    public static byte[] getConfigurationData(Configurable configurable) throws Exception {
//        String configDataStr = (configurable != null) ? configurable.getConfigurationData() : null;
//        return (configDataStr != null) ? configDataStr.getBytes() : null;
//    }
//
//    public static byte[] getConfigurationData(ConnectorConfiguration config) throws Exception {
//        String configDataStr = config.getConfigurationData();
//        return (configDataStr != null) ? configDataStr.getBytes() : null;
//    }
//
//    public static byte[] getConfigurationTemplateContent(Configurable configurable) throws Exception {
//        String tmp = (configurable != null) ? configurable.getConfigurationTemplateContent() : null;
//        return (tmp == null) ? null : tmp.getBytes();
//    }
//
//    public static byte[] getConfigurationTemplateContent(ConnectorConfiguration config) throws Exception {
//        String tmp = (config != null) ? config.getConfigurationTemplateContent() : null;
//        return (tmp == null) ? null : tmp.getBytes();
//    }
//
//    public static DBRuntimeEnvAccessor getDBRuntimeEnvAccessor(String externalSystemType, IConfiguration extSubSysCfg, IConfiguration extSysCommonCfg) throws BaseException {
//		DBRuntimeEnvAccessor runtimeEnv = null;
//		try {
//			IConfiguration mergedCfg = ConfigurationHelper.mergeConfiguration(extSubSysCfg, extSysCommonCfg, false);
//			runtimeEnv = DBRuntimeEnvAccessorFactory.createDBRuntimeEnvAccessor(externalSystemType, mergedCfg);
//		}catch(Exception ex){
//			throw new BaseException(ex);
//		}
//		return runtimeEnv;
//	}   
//    
//    public static IConfiguration getExternalSystemCommonConfiguration(CMLink cmLink, ProjectDeployment dp) throws BaseException, CodeGenException {
//		IConfiguration config = null;
//		try {
//			EnvironmentElement envElement = dp.getDeployedElement(cmLink, dp.getEnvironment());
//			if (envElement instanceof ExternalSystemComposite) {
//				ExternalSystemComposite extSysComposite = (ExternalSystemComposite) envElement;
//				String configData = extSysComposite.getCommonConfigurationInstance();
//				String configTemplate = extSysComposite.getCommonConfigurationTemplate();
//
//				if ((configData != null) && (configTemplate != null)) {
//					config = getIConfiguration(configTemplate.getBytes("UTF-8"), configData.getBytes("UTF-8"));
//				}
//			}
//		} catch (Exception ex) {
//			if (ex instanceof CodeGenException) {
//				throw ((CodeGenException) ex);
//			} else {
//				throw new BaseException(ex);
//			}
//		}
//
//		return config;
//    }
//    
//    public static IConfiguration getExternalSystemConfiguration(CMLink link, ProjectDeployment deploy, String extSysTypeName) throws BaseException , CodeGenException {
//        String extAppName = MESSAGE_MGR.getString("LBL_UNKNOWN");
//        String extSystemName = null;
//        byte[] configData = null;
//        byte[] configTemplate = null;
//        IConfiguration config = null;
//        	
//        try {
//            extAppName = link.getDestinationNode().getName();
//        } catch (RepositoryException ignore) {
//            // Ignore.
//        }
//        try {
//            EnvironmentElement envElement = deploy.getDeployedElement(link, deploy.getEnvironment());
//            extSystemName = envElement.getName();
//
//            if (envElement.getEnvironmentElementType().indexOf("ExternalSystem") > 0) {
//                ExternalSystem extSystem = null;
//
//                if (envElement instanceof ExternalSystemComposite) {
//                    ExternalSystemComposite extSysComposite = (ExternalSystemComposite) envElement;
//                    extSystem = extSysComposite.getExternalSystem(extSysTypeName);
//                    if (extSystem != null) {
//                        Configurable envCfg = extSystem.getConfiguration();
//                        configData = getConfigurationData(envCfg);
//                        if (configData == null || configData.length == 0) {
//                            throw new CodeGenException(MESSAGE_MGR.getString("ERR_EXTSYS_DATA_MISSING", extSystemName, extSysTypeName));
//                        }
//
//                        configTemplate = getConfigurationTemplateContent(envCfg);
//                        if (configTemplate == null || configTemplate.length == 0) {
//                            throw new CodeGenException(MESSAGE_MGR.getString("ERR_EXTSYS_TEMPLATE_MISSING", extSystemName, extSysTypeName));
//                        }
//
//                        config =  getIConfiguration(configTemplate, configData);
//                    }                    
//                }else{
//                	// TODO use eways ExternalSystem 
//	                if (envElement instanceof org.netbeans.modules.mashup.db.model.ExternalSystem){
//	                	org.netbeans.modules.mashup.db.model.ExternalSystem flatfileXSystem = (org.netbeans.modules.mashup.db.model.ExternalSystem) envElement;
//	                	if (flatfileXSystem != null){
//		                	configData = getConfigurationData(flatfileXSystem.getConfiguration());
//	                        if (configData == null || configData.length == 0) {
//	                            throw new CodeGenException(MESSAGE_MGR.getString("ERR_EXTSYS_DATA_MISSING", extSystemName, extSysTypeName));
//	                        }
//		                	
//		                	configTemplate = getConfigurationTemplateContent(flatfileXSystem.getConfiguration());
//	                        if (configTemplate == null || configTemplate.length == 0) {
//	                            throw new CodeGenException(MESSAGE_MGR.getString("ERR_EXTSYS_TEMPLATE_MISSING", extSystemName, extSysTypeName));
//	                        }
//		                	
//		                	config = getIConfiguration(configTemplate, configData);
//	                	}
//	                }
//                }                
//            }
//        } catch (Exception e) {
//            if (e instanceof CodeGenException) {
//                throw ((CodeGenException) e);
//            }
//
//            StringBuffer msg = new StringBuffer();
//            msg.append("Error occurred while retrieving connection parameters from ");
//            if (extSysTypeName != null) {
//                msg.append(" section " + extSysTypeName + " of ");
//            }
//
//            if (extSystemName != null) {
//                msg.append("External System \"" + extSystemName + "\".");
//            } else {
//                msg.append("External System associated with External Application \"" + extAppName + "\"");
//            }
//
//            throw new BaseException(msg.toString(), e);
//        }
//        return config;
//    }
//
//    public static IConfiguration getIConfiguration(byte[] template, byte[] data) throws Exception {
//        Factory factory = new Factory();
//        return factory.getConfiguration(template, data);
//    }
//
//    public static IConfiguration getIConfiguration(Configurable configurable) throws Exception {
//        IConfiguration ret = null;
//        byte[] configData = null;
//        byte[] configTemplate = null;
//        configData = getConfigurationData(configurable);
//        configTemplate = getConfigurationTemplateContent(configurable);
//        ret = getIConfiguration(configTemplate, configData);
//        return ret;
//    }
//
//    public static IConfiguration getIConfiguration(ConnectorConfiguration connectorConfiguration, String linkName) throws CodeGenException {
//		IConfiguration iconfiguration = null;
//		if (connectorConfiguration != null) {
//			try {
//				// TODO Conform message to match instructions in eGate docs for CME.
//				byte[] configData = getConfigurationData(connectorConfiguration);
//				if (configData == null || configData.length == 0) {
//					throw new CodeGenException(MESSAGE_MGR.getString(
//							"ERR_ICONFIGURATION_DATA_MISSING", linkName));
//				}
//
//				byte[] configTemplate = getConfigurationTemplateContent(connectorConfiguration);
//				if (configTemplate == null || configTemplate.length == 0) {
//					throw new CodeGenException(MESSAGE_MGR.getString(
//							"ERR_ICONFIGURATION_TEMPLATE_MISSING", linkName));
//				}
//
//				iconfiguration = getIConfiguration(configTemplate, configData);
//			} catch (CodeGenException e) {
//				throw e;
//			} catch (Exception e) {
//				Logger.print(Logger.ERROR, LOG_CATEGORY,
//						"Failed to get configuration data", e);
//				throw new CodeGenException(e);
//			}
//		}
//		return iconfiguration;
//	}

}
