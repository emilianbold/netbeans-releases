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
package org.netbeans.modules.edm.project.anttasks;


public class EDMDeploymentConstants {

   public static String AXION_URL_PREFIX = "jdbc:axiondb:";
   public static String ORACLE_URL_PREFIX = "jdbc:oracle:thin:@";
   public static String SQL_SERVER_URL_PREFIX = "jdbc:SeeBeyond:sqlserver://";
   public static String DB2_URL_PREFIX = "jdbc:Seebeyond:db2://";
   public static String SYBASE_URL_PREFIX = "jdbc:SeeBeyond:sybase://";

   // Used in ETLCodelet for DeploymentProfileGenerator.addEnv() entry datatype
   public static final String STRING = "java.lang.String";

   public static final String STC_NAME_SPACE = "stc/EAR_NAMESPACE";

   public static final String COLLAB_NAME = "COLLAB_NAME";
   public static final String PROJECT_NAME = "PROJECT_NAME";
   public static final String CLASS_NAME = "CLASS_NAME"; // of ETLRuntimeHandler
   public static final String ENGINE_ROOT_DIR = "ENGINE_ROOT_DIR";
   public static final String RUNTIME_SERVER_WORKSPACE_DIR = "RUNTIME_SERVER_WORKSPACE_DIR";

   public static final String ETL_ENGINE_INSTANCE_DB_DIR = "ETL_ENGINE_INSTANCE_DB_DIR";
   public static final String ETL_MONITOR_DB_DIR = "ETL_MONITOR_DB_DIR";

   public static final String PARAM_APP_DATAROOT = "{APP_DATAROOT}";
   public static final String PARAM_MONITOR_DB_NAME = "{MONITOR_DB_NAME}";
   public static final String PARAM_INSTANCE_DB_NAME = "{INSTANCE_DB_NAME}";

   public static final String OPERATION_NAME_TAG = "OPERATION_NAME";
   public static final String OPERATION_NAME = "execute";

   public static final String LH = "LH";
   public static final String IS = "IS";
   public static final String DEPLOYMENT_NAME = "DEPLOYMENT_NAME";
   public static final String ENVIRONMENT_NAME = "ENVIRONMENT_NAME";

   public static final String DIRECTORY = "Directory";
   public static final String ORACLE = "oracle";
   public static final String SQL_SERVER = "sqlserver";
   public static final String DB2 = "db2";
   public static final String SYBASE = "sybase";
   public static final String INTERNAL = "internal";

   public static final String DRIVER_PROPERTIES = "DriverProperties";
   public static final String DATABASE_NAME = "DatabaseName";
   public static final String SERVER_NAME = "ServerName";
   public static final String PORT_NUMBER = "PortNumber";
   public static final String USER = "User";
   public static final String PASSWORD = "Password";
   public static final String LOCATION_NAME = "LocationName";
   public static final String COLLECTION_ID = "CollectionID";
   public static final String PACKAGE_COLLECTION = "PackageCollection";
   public static final String EWAYS_PROP_DELIMITER = "Delimiter";

   public static final String ENV_NAME_PREFIX = "java:comp/env/";
   public static final String ETL_ENGINE_FILE = "ETL_ENGINE_FILE";
   public static final String DEPLOY_PARTNERS = "com.stc.codegen.frameworkImpl.runtime.DeployedServicePartners";

   public static final String STATUS = "STATUS";
   public static final String FAILURE = "Failure";
   public static final String SUCCESS = "Success";
   public static final String ENDTIME = "ENDTIME";
   public static final String STARTTIME = "STARTTIME";

   public static final String WSDL_TARGET_NAMESPACE_PREFIX = "urn:stc:egate:eTL";
   public static final String WSDL_URN_PREFIX = "{" + WSDL_TARGET_NAMESPACE_PREFIX + ":";
   public static final String WSDL_URN_SUFFIX = "}";
   public static final String ETL_GLOBAL_FAULT = "ETLFault";
}