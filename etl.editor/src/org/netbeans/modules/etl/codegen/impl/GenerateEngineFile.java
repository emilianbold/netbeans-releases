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
//import com.sun.jbi.ui.devtool.configuration.IParameter;
//import com.sun.jbi.ui.devtool.configuration.ISection;
//import com.sun.jbi.ui.devtool.deployment.repository.ProjectDeployment;
//import org.netbeans.modules.etl.codegen.ETLProcessFlowGenerator;
//import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
//import org.netbeans.modules.etl.engine.ETLEngine;
//import org.netbeans.modules.etl.model.ETLDefinition;
//import org.netbeans.modules.etl.model.ETLDefinitionProjectElement;
//import org.netbeans.modules.etl.utils.ETLDeploymentConstants;
//import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
//import com.sun.jbi.ui.devtool.model.common.cme.CMLink;
//import com.sun.jbi.ui.devtool.model.common.cme.CMNode;
//import com.sun.jbi.ui.devtool.model.common.cme.Port;
//import com.sun.jbi.ui.devtool.model.common.cme.PortType;
//import com.sun.jbi.ui.devtool.model.common.cme.ProcessingDefinition;
//import org.netbeans.modules.model.database.DBRuntimeEnvAccessor;
//import com.sun.jbi.ui.devtool.repository.RepositoryException;
//import com.sun.jbi.ui.devtool.sql.framework.exception.BaseException;
//import com.sun.jbi.ui.devtool.sql.framework.jdbc.SQLDBConnectionDefinition;
//import org.netbeans.modules.sql.framework.model.SQLDefinition;
//import com.sun.jbi.ui.devtool.sql.framework.utils.DBConnectionDefinitionTemplate;
//import com.sun.jbi.ui.devtool.sql.framework.utils.Logger;
//import com.sun.jbi.ui.devtool.sql.framework.utils.StringUtil;

/**
 * @author Ahimanikya Satapathy
 * @author Jonathan Giron
 * @version $Revision$
 */
public class GenerateEngineFile {
//    private static final String LOCAL_PATTERN_DELIMITER = "#";
//    private static final String LOG_CATEGORY = GenerateEngineFile.class.getName();
//    private static final LocalMessageManager MESSAGE_MGR = new LocalMessageManager();
//    private static final String SETPARAM_PATTERN = "set([^#]*?)#([^#]*?)##";
//    private static final String SETURL_PATTERN = "setURL#([^#]*?)##";
//	
//    private final ETLCodelet codelet;
//    private DBConnectionDefinitionTemplate connectionDefnTemplate;
//    private ETLEngine engine = null;
//    private ETLDefinitionProjectElement etlDefnPE = null;
//    
//    // Holds a map of unique IDs (DB OID + port type [either "-Source" or "-Target"]) to
//    // InternalDBMetadata instances which hold CME-level parameters such as directory
//    // location of flatfiles and whether dynamic file name resolution is in effect.
//    private Map internalDBConfigParams = new HashMap();
//
//    // Holds a map of DB OIDs to corresponding connection pool names.
//    private Map dbNamePoolNameMap = new HashMap(2);
//
//
//    /**
//     * Local implementation of a resource bundle manager. This class is necessary because
//     * neither the eTL nor the SQLFramework flavors of MessageManager can be used within
//     * the codegen thread - neither is able to resolve the LocalStrings.properties file
//     * with the classloader associated with the codegen thread.
//      */
//    static class LocalMessageManager {
//        ResourceBundle rb;
//
//        public LocalMessageManager() {
//            rb = ResourceBundle.getBundle(GenerateEngineFile.class.getPackage().getName().concat(".LocalStrings"), Locale.getDefault(),
//                GenerateEngineFile.class.getClassLoader());
//        }
//
//        /**
//         * Get a string from the underlying resource bundle.
//         *
//         * @param key for which msg needs to retrieved from HashMap
//         * @return String The MessageFormat for given Key
//         */
//
//        public synchronized String getString(String key) {
//            if (key == null) {
//                String msg = "key is null";
//                throw new NullPointerException(msg);
//            }
//
//            if (rb == null) {
//                String message = "Could not load resource bundle.";
//                throw new NullPointerException(message);
//            }
//
//            String str;
//
//            try {
//                str = rb.getString(key);
//            } catch (MissingResourceException mre) {
//                str = "Cannot find message associated with key '" + key + "'";
//            }
//
//            return str;
//        }
//
//        /**
//         * Get a string from the underlying resource bundle and format it with the given
//         * object argument. This argument can of course be a String object.
//         *
//         * @param key For which String needs to be displayed
//         * @param arg That is the result for Info, ERR or Exception
//         * @return String MessageString for given Key
//         */
//
//        public synchronized String getString(String key, Object arg) {
//            Object[] args = new Object[] { arg};
//            return getString(key, args);
//        }
//
//        /**
//         * Get a string from the underlying resource bundle and format it with the given
//         * object arguments. These arguments can of course be String objects.
//         *
//         * @param key For which Message needs to be constructed
//         * @param arg1 used for message construction
//         * @param arg2 used for message construction
//         * @return String Message String for the given key
//         */
//
//        public String getString(String key, Object arg1, Object arg2) {
//            Object[] args = new Object[] { arg1, arg2};
//            return getString(key, args);
//        }
//
//        /**
//         * Get a string from the underlying resource bundle and format it with the given
//         * set of arguments.
//         *
//         * @param key For which MessageFormat needs to be picked for a key
//         * @param args This contains the arguments
//         * @return String Message Format for given key and arguments
//         */
//        public String getString(String key, Object[] args) {
//            String iString;
//            String value = getString(key);
//
//            try {
//                Object nonNullArgs[] = args;
//                for (int i = 0; i < args.length; i++) {
//                    if (args[i] == null) {
//                        if (nonNullArgs == args) {
//                            nonNullArgs = (Object[]) args.clone();
//                        }
//                        nonNullArgs[i] = "null";
//                    }
//                }
//
//                iString = MessageFormat.format(value, nonNullArgs);
//            } catch (IllegalArgumentException iae) {
//                StringBuffer buf = new StringBuffer();
//                buf.append(value);
//                for (int i = 0; i < args.length; i++) {
//                    buf.append(" arg[" + i + "]=" + args[i]);
//                }
//                iString = buf.toString();
//            }
//
//            return iString;
//        }
//    }
//
//    static String convertDriverPropertiesToURL(ISection section, boolean includeParams) throws CodeGenException {
//        String driverProps = getStringParameter(section, ETLDeploymentConstants.DRIVER_PROPERTIES, false);
//        String delimiter = getStringParameter(section, ETLDeploymentConstants.EWAYS_PROP_DELIMITER, driverProps != null);
//
//        StringBuffer buf = new StringBuffer(50);
//        extractURLFrom(driverProps, buf, delimiter);
//
//        return (buf.length() != 0) ? buf.toString() : "";
//    }
//
//
//    static String getOptionalDriverProperties(ISection section) throws CodeGenException {
//        String driverProps = getStringParameter(section, ETLDeploymentConstants.DRIVER_PROPERTIES, false);
//        String delimiter = getStringParameter(section, ETLDeploymentConstants.EWAYS_PROP_DELIMITER, driverProps != null);
//
//        StringBuffer buf = new StringBuffer(50);
//        int ct = extractPropertiesFrom(driverProps, buf, delimiter);
//        return (ct != 0) ? buf.toString() : "";
//    }
//
//    /**
//     * Parses property key-value pairs from substring of eWays DriverProperties value (assuming
//     * the setURL property has been removed), appending them in canonical "key=value[;]" form
//     * into the given StringBuffer.
//     *
//     * @param propsString String to be parsed for driver parameters
//     * @param buf StringBuffer which will accept property key-value pairs
//     * @param delimiter delimiter specified by user in eWays configuration dialog
//     * @return count of parameters written to <code>buf</code>
//     */
//    static int extractPropertiesFrom(String propsString, StringBuffer buf, String delimiter) {
//        if (StringUtil.isNullString(propsString)) {
//            return 0;
//        }
//
//        Pattern pattern = Pattern.compile(SETPARAM_PATTERN.replaceAll(LOCAL_PATTERN_DELIMITER,
//            StringUtil.escapeJavaRegexpChars(delimiter)));
//        Matcher match = pattern.matcher(propsString);
//        int i = 0;
//
//        while (match.find()) {
//            String key = match.group(1);
//
//            // Skip the setURL group, if any.
//            if ("URL".equals(key.toUpperCase())) {
//                continue;
//            }
//
//            //
//            // Handle SpyAttributes property - for now, ignore it.
//            //
//            // If we support the use of the DataDirect Spy driver, then we will need to modify
//            // convertDriverPropertiesToURL(...) to wrap the generated URL in "jdbc:spy:{[URL goes here]}"
//            // and append the value of the SpyAttributes property to the wrapped URL.
//            //
//            // See http://media.datadirect.com/download/docs/jdbc/jdbcref/usejdbc.html#wp1014509
//            // and eWays team for details on how/whether the various eWays support the Spy driver.
//            //
//            if ("SPYATTRIBUTES".equals(key.toUpperCase())) {
//                continue;
//            }
//
//            if (i++ != 0) {
//                buf.append(";");
//            }
//            buf.append(key).append("=").append(match.group(2));
//        }
//
//        return i;
//    }
//
//    /**
//     * Parses URL information, if any, from an eWays DriverProperties value and appends it into the
//     * given StringBuffer.
//     *
//     * @param driverPropStr String to be parsed for URL information
//     * @param buf StringBuffer which will accept URL information
//     * @param delimiter delimiter specified by user in eWays configuration dialog
//     * @return position (in driverPropStr) of next character immediately following the setURL
//     * parameter group, or 0 if no setURL parameter group was found.
//     */
//    static int extractURLFrom(String driverPropStr, StringBuffer buf, String delimiter) {
//        if (StringUtil.isNullString(driverPropStr) || StringUtil.isNullString(delimiter)) {
//            return 0;
//        }
//
//        Pattern pattern = Pattern.compile(SETURL_PATTERN.replaceAll(LOCAL_PATTERN_DELIMITER,
//            StringUtil.escapeJavaRegexpChars(delimiter)));
//        Matcher matcher = pattern.matcher(driverPropStr);
//        if (matcher.find()) {
//            buf.append(matcher.group(1));
//            return matcher.end();
//        }
//        return 0;
//    }
//
//    static IParameter getIParameter(ISection section, String parameterName) throws Exception {
//        return section.getParameter(parameterName);
//    }
//
//    static String getStringParameter(ISection section, String parameterName) throws CodeGenException {
//        return getStringParameter(section, parameterName, true);
//    }
//
//    static String getStringParameter(ISection section, String parameterName, boolean failIfMissing) throws CodeGenException {
//        String sParameter = null;
//        try {
//            IParameter param = getIParameter(section, parameterName);
//            if (param != null) {
//                Object value = param.getValue();
//                if (value != null) {
//                    sParameter = value.toString();
//                } else if (failIfMissing) {
//                    String msg = MESSAGE_MGR.getString("ERR_EXTSYS_MISSING_PARAMETER", parameterName);
//                    Logger.print(Logger.ERROR, LOG_CATEGORY, msg);
//                    throw new CodeGenException(msg);
//                } else {
//                    Logger.print(Logger.WARN, LOG_CATEGORY, "Could not obtain parameter value for " + parameterName + "; continuing.");
//                }
//            }
//        } catch (Exception e) {
//            Logger.print(Logger.ERROR, LOG_CATEGORY, "Caught exception while getting parameter value for " + parameterName, e);
//            throw new CodeGenException(e);
//        }
//        return sParameter;
//    }
//
//    static boolean isAnyParamNull(String dbName, String host, String portNumber) {
//        return StringUtil.isNullString(dbName) || StringUtil.isNullString(host) || StringUtil.isNullString(portNumber);
//    }
//
//    static boolean isDynamicFilePath(ISection section, String parameter) throws Exception {
//        Object obj = getIParameter(section, parameter).getValue();
//        return ((Boolean) obj).booleanValue();
//    }
//
//    public GenerateEngineFile(ETLCodelet codelet, Collection pnLinks, ProcessingDefinition processDefn, ProjectDeployment deploy)
//            										throws RepositoryException, BaseException, CodeGenException {
//        Iterator it = pnLinks.iterator();
//        String qualdbId = null;
//        String externalSystemType = null;
//        String externalSystemSubType = null;                        
//        IConfiguration iExtSubSysCfg = null;
//        IConfiguration iExtSysCommonCfg = null;
//        Map connDefs = new HashMap(); // Connection pool names --> resolved
//        Map dbCatalogOverrideMapMap = new HashMap();  
//    	Map dbSchemaOverrideMapMap = new HashMap();
//        
//        this.codelet = codelet;
//        this.connectionDefnTemplate = new DBConnectionDefinitionTemplate();
//        // DBConnectionDefinitions
//        this.etlDefnPE = (ETLDefinitionProjectElement) processDefn;
//        ETLDefinition etldefn = this.codelet.getETLDefinition(this.etlDefnPE);
//
//        while (it.hasNext()) {
//            Object objRef = it.next();
//            if (objRef != null && objRef instanceof CMLink) {
//                CMLink link = (CMLink) objRef;
//                String portName = link.getSourcePort();
//                String suffix = null;
//                Port port = null;
//                if (portName != null) {
//                    int index = portName.lastIndexOf("-");
//                    suffix = portName.substring(index, portName.length());
//                    port = processDefn.getDestination(portName);
//                }
//                if (port != null) {
//                    PortType portType = port.getPortType();
//
//                    String poolName = this.codelet.getDeployedName().getLocalPart() + "_" + link.getDestinationNode().getName();
//
//                    if (portType != null) {
//                    	qualdbId = portType.getOID() + suffix ;
//                        this.dbNamePoolNameMap.put(qualdbId, poolName);
//                        externalSystemType = this.codelet.getExternalSystemType(link);
//                        externalSystemSubType = this.codelet.getExternalSystemTypeName(link); 
//                        iExtSubSysCfg = ETLCodegenHelper.getExternalSystemConfiguration(link, deploy, externalSystemSubType);
//                        populateConnectionDefinitions(link, iExtSubSysCfg, connDefs, poolName, portType, suffix);
//                        iExtSysCommonCfg = ETLCodegenHelper.getExternalSystemCommonConfiguration(link, deploy);
//                        DBRuntimeEnvAccessor runtimeEnv = ETLCodegenHelper.getDBRuntimeEnvAccessor(externalSystemType, iExtSubSysCfg, iExtSysCommonCfg);
//                        if (runtimeEnv != null){
//                        	if (runtimeEnv.isCatalogNameOverwritten()){
//                        		Map map = new TreeMap(String.CASE_INSENSITIVE_ORDER);
//                        		map.putAll(runtimeEnv.getOverwrittenCatalogNameMap());
//                        		
//                        		dbCatalogOverrideMapMap.put(qualdbId, map);
//                        	}
//                        	
//                        	if (runtimeEnv.isSchemaNameOverwritten()){
//                        		Map map = new TreeMap(String.CASE_INSENSITIVE_ORDER);
//                        		map.putAll(runtimeEnv.getOverwrittenSchemaNameMap());
//                        		dbSchemaOverrideMapMap.put(qualdbId, map);
//                        	}                        	
//                        }
//                    }
//                }
//            }
//        }
//        
//        SQLDefinition sqlDefinition = etldefn.getSQLDefinition();
//        
//        try {
//	        sqlDefinition.overrideCatalogNamesForDb(dbCatalogOverrideMapMap);
//	        sqlDefinition.overrideSchemaNamesForDb(dbSchemaOverrideMapMap);
//	        ETLProcessFlowGenerator flowgen = ETLProcessFlowGeneratorFactory.getCollabFlowGenerator(sqlDefinition, true );
//	        flowgen.setWorkingFolder(ETLDeploymentConstants.PARAM_APP_DATAROOT);
//	        flowgen.setInstanceDBName(ETLDeploymentConstants.PARAM_INSTANCE_DB_NAME);
//	        flowgen.setInstanceDBFolder(codelet.getEngineInstanceDBDirectory());
//	        flowgen.setMonitorDBName(codelet.getDeployable().getName());
//	        flowgen.setMonitorDBFolder(codelet.getEngineMonitorDBDirectory());
//	        flowgen.applyConnectionDefinitions(connDefs, this.dbNamePoolNameMap, internalDBConfigParams);
//	        this.engine = flowgen.getScript();
//        }finally{
//        	sqlDefinition.clearOverride(true, true);
//        }
//    }
//
//    public String getEngineTaskNodes() {
//        return engine.toXMLString();
//    }
//
//    private String createURLForAllOtherDBs(ISection section, String connectionURL, String extAppName, String extSysTypeName)
//            throws CodeGenException, BaseException {
//        StringBuffer url = new StringBuffer();
//
//        url.append(convertDriverPropertiesToURL(section, false));
//        if (StringUtil.isNullString(url.toString())) {
//            url = new StringBuffer();
//
//            String dbName = getStringParameter(section, ETLDeploymentConstants.DATABASE_NAME, false);
//            String host = getStringParameter(section, ETLDeploymentConstants.SERVER_NAME, false);
//            String portNumber = getStringParameter(section, ETLDeploymentConstants.PORT_NUMBER, false);
//
//            if (isAnyParamNull(dbName, host, portNumber)) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_CONN_PARAMS_NOTSET", extAppName, extSysTypeName));
//            } else {
//                Map connectionParams = new HashMap();
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, dbName);
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_NAME, host);
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_PORT, portNumber);
//                url.append(StringUtil.replace(connectionURL, connectionParams));
//            }
//        }
//
//        String optionalParams = GenerateEngineFile.getOptionalDriverProperties(section);
//        if (!StringUtil.isNullString(optionalParams)) {
//            if (!url.toString().endsWith(";") && !optionalParams.startsWith(";")) {
//                url.append(";");
//            }
//            url.append(optionalParams);
//        }
//
//        return url.toString();
//    }
//
//    private String createURLForDB2Connect(ISection section, final String urlTemplate,
//            String extAppName, String extSysTypeName) throws CodeGenException, BaseException {
//        StringBuffer url = new StringBuffer();
//
//        Map connectionParams = new HashMap();
//        url.append(convertDriverPropertiesToURL(section, false));
//        if (StringUtil.isNullString(url.toString())) {
//            url = new StringBuffer();
//
//            String databaseName = getStringParameter(section, ETLDeploymentConstants.DATABASE_NAME, false);
//            if (StringUtil.isNullString(databaseName)) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_CONN_PARAMS_NOTSET", extAppName, extSysTypeName));
//            } else {
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, databaseName);
//                url.append(StringUtil.replace(urlTemplate, connectionParams));
//            }
//        }
//
//        String optionalParams = GenerateEngineFile.getOptionalDriverProperties(section);
//        if (!StringUtil.isNullString(optionalParams)) {
//            if (!url.toString().endsWith(":") && !optionalParams.startsWith(":")) {
//                url.append(":");
//            }
//
//            url.append(optionalParams);
//            // DB2 Connect driver expects every key-value property to be terminated by a semi-colon.
//            if (!url.toString().endsWith(";")) {
//                url.append(";");
//            }
//        }
//
//        return url.toString();
//    }
//
//    private String createURLForDB2ConnectType4(ISection section, final String urlTemplate,
//            String extAppName, String extSysTypeName) throws CodeGenException, BaseException {
//        StringBuffer url = new StringBuffer();
//
//        url.append(convertDriverPropertiesToURL(section, false));
//        if (StringUtil.isNullString(url.toString())) {
//            url = new StringBuffer();
//
//            String dbName = getStringParameter(section, ETLDeploymentConstants.DATABASE_NAME, false);
//            String host = getStringParameter(section, ETLDeploymentConstants.SERVER_NAME, false);
//            String portNumber = getStringParameter(section, ETLDeploymentConstants.PORT_NUMBER, false);
//
//            if (isAnyParamNull(dbName, host, portNumber)) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_CONN_PARAMS_NOTSET", extAppName, extSysTypeName));
//            } else {
//                Map connectionParams = new HashMap();
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_NAME, host);
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_PORT, portNumber);
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, dbName);
//
//                url.append(StringUtil.replace(urlTemplate, connectionParams));
//            }
//        }
//
//        String optionalParams = GenerateEngineFile.getOptionalDriverProperties(section);
//        if (!StringUtil.isNullString(optionalParams)) {
//            if (!url.toString().endsWith(":") && !optionalParams.startsWith(":")) {
//                url.append(":");
//            }
//
//            url.append(optionalParams);
//            // DB2 Connect driver expects every key-value property to be terminated by a semi-colon.
//            if (!url.toString().endsWith(";")) {
//                url.append(";");
//            }
//        }
//
//        return url.toString();
//    }
//
//    private String createURLForDB2DataDirect(ISection section, final String urlTemplate,
//            String extAppName, String extSysTypeName) throws CodeGenException, BaseException {
//        StringBuffer url = new StringBuffer();
//
//        url.append(convertDriverPropertiesToURL(section, false));
//        if (StringUtil.isNullString(url.toString())) {
//            url = new StringBuffer();
//            Map connectionParams = new HashMap();
//
//            String dbName = getStringParameter(section, ETLDeploymentConstants.DATABASE_NAME, false);
//            String host = getStringParameter(section, ETLDeploymentConstants.SERVER_NAME, false);
//            String portNumber = getStringParameter(section, ETLDeploymentConstants.PORT_NUMBER, false);
//
//            // z/OS and/or AS/400 does not define DatabaseName; instead, LocationName and
//            // PackageCollection are supplied.
//            StringBuffer paramListBuf = new StringBuffer(30);
//            if (StringUtil.isNullString(dbName)) {
//                String locationName = getStringParameter(section, ETLDeploymentConstants.LOCATION_NAME, false);
//                if (locationName != null) {
//                    paramListBuf.append("LocationName=").append(locationName);
//                }
//
//                String collectionID = getStringParameter(section, ETLDeploymentConstants.COLLECTION_ID, false);
//                if (collectionID != null ) {
//                    paramListBuf.append(locationName != null ? ";" : "");
//                    paramListBuf.append("collectionId=").append(collectionID);
//                }
//
//                String packageCollection = getStringParameter(section, ETLDeploymentConstants.PACKAGE_COLLECTION, false);
//                if ((packageCollection != null ) &&(! "".equals(packageCollection.trim()))){
//                    paramListBuf.append(((locationName != null) || (collectionID != null)) ? ";" : "");
//                    paramListBuf.append("packageCollection=").append(packageCollection);
//                }
//            } else {
//                paramListBuf.append("DatabaseName=").append(dbName);
//            }
//
//            if (isAnyParamNull(paramListBuf.toString(), host, portNumber)) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_CONN_PARAMS_NOTSET", extAppName, extSysTypeName));
//            }
//
//            connectionParams.put(DBConnectionDefinitionTemplate.KEY_PARAM_LIST, paramListBuf.toString());
//            connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_NAME, host);
//            connectionParams.put(DBConnectionDefinitionTemplate.KEY_HOST_PORT, portNumber);
//            url.append(StringUtil.replace(urlTemplate, connectionParams));
//        }
//
//        String optionalParams = GenerateEngineFile.getOptionalDriverProperties(section);
//        if (!StringUtil.isNullString(optionalParams)) {
//            if (!url.toString().endsWith(";") && !optionalParams.startsWith(";")) {
//                url.append(";");
//            }
//
//            url.append(optionalParams);
//        }
//
//        return url.toString();
//    }
//
//    private SQLDBConnectionDefinition extractConnectionDefinitionParameters(CMLink link, IConfiguration iExtSubSysCfg, PortType portType, String portName) 
//    											throws BaseException, CodeGenException, RepositoryException {
//        SQLDBConnectionDefinition connDef = null;
//
//        String extAppName = MESSAGE_MGR.getString("LBL_UNKNOWN");
//        try {
//            CMNode destNode = link.getDestinationNode();
//            if (destNode != null) {
//                extAppName = destNode.getName();
//            }
//        } catch (RepositoryException ignore) {
//            // Ignore.
//        }
//
//        try {
//            final String extSystemAdapterType = this.codelet.getExternalSystemType(link);
//            final String extSysSubType = this.codelet.getExternalSystemTypeName(link);
//            
//            if (iExtSubSysCfg == null) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_CONN_PARAMS_MISSING", extAppName, extSysSubType));
//            }
//            
//            SQLDBConnectionDefinition conndefTemplate = this.connectionDefnTemplate.getDBConnectionDefinition(extSystemAdapterType);
//            connDef = (SQLDBConnectionDefinition) conndefTemplate.cloneObject();
//
//            
//            if (ETLCodelet.EXTERNAL_APPLICATION_TYPE_FLATFILEDB.equalsIgnoreCase(extSystemAdapterType)){
//                Map sectionMap = iExtSubSysCfg.getSections();            	
//                ISection section = (ISection) sectionMap.get("parameter-settings");
//                Logger.print(Logger.DEBUG, LOG_CATEGORY, "Section: " + section);
//                String metadataDir = codelet.getEngineInstanceDBDirectory();
//
//                String directory = getStringParameter(section, ETLDeploymentConstants.DIRECTORY);
//                Logger.print(Logger.DEBUG, LOG_CATEGORY, "Directory for flatfile lookup: " + directory);
//                if (this.isObjectTypeFlatfileObjectTypeDefinition(portType)) {
//                    InternalDBMetadata dbMetadata = new InternalDBMetadata(directory, isDynamicFilePath(section, "DynamicFilePath"),
//                        connDef.getName());
//                    internalDBConfigParams.put(portType.getOID() + portName, dbMetadata);
//                }
//
//                String collabName = codelet.getDeployable().getName();
//
//                Map connectionParams = new HashMap();
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, collabName);
//                connectionParams.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, metadataDir);
//
//                connDef.setConnectionURL(StringUtil.replace(connDef.getConnectionURL(), connectionParams));
//            }else{
//                Map dbMap = iExtSubSysCfg.getSections();
//                ISection section = (ISection) dbMap.get("JDBCConnectorSettings");
//                String url = "";
//
//                if ("DB2ADAPTER".equals(extSystemAdapterType)) {
//                    url = createURLForDB2DataDirect(section, connDef.getConnectionURL(), extAppName, extSysSubType);
//                } else if ("DB2CONNECTADAPTER".equals(extSystemAdapterType)) {
//                    // NOTE: Ugly hack, but there isn't any other way to know what driver type
//                    // (and URL form) to use.
//                    if (extSysSubType.toUpperCase().indexOf("TYPE 4") != -1) {
//                        conndefTemplate = this.connectionDefnTemplate.getDBConnectionDefinition(extSystemAdapterType + "_TYPE4");
//                        connDef = (SQLDBConnectionDefinition) conndefTemplate.cloneObject();
//                        url = createURLForDB2ConnectType4(section, connDef.getConnectionURL(), extAppName, extSysSubType);
//                    } else {
//                        url = createURLForDB2Connect(section, connDef.getConnectionURL(), extAppName, extSysSubType);
//                    }
//                } else {
//                    url = createURLForAllOtherDBs(section, connDef.getConnectionURL(), extAppName, extSysSubType);
//                }
//
//                connDef.setUserName(getStringParameter(section, ETLDeploymentConstants.USER));
//                connDef.setPassword(getStringParameter(section, ETLDeploymentConstants.PASSWORD));
//                connDef.setConnectionURL(url);
//            } 
//        } catch (Exception e) {
//            // Don't wrap exception if it's a BaseException or CodeGenException.
//            if (e instanceof BaseException) {
//                throw (BaseException) e;
//            } else if (e instanceof CodeGenException) {
//                throw (CodeGenException) e;
//            }
//            throw new BaseException(MESSAGE_MGR.getString("ERR_CONN_CONFIG_WRAPPED_EXCEPTION", extAppName));
//        }
//
//        return connDef;
//    }
//
//    private boolean isObjectTypeFlatfileObjectTypeDefinition(PortType type) {
//        return (type instanceof FlatfileDefinition);
//    }
//
//    private void populateConnectionDefinitions(CMLink link, IConfiguration extSysCfg, Map connDefs, String poolName, PortType portType, String portName) throws CodeGenException {
//		String extAppName = MESSAGE_MGR.getString("LBL_UNKNOWN");
//		try {
//			CMNode destNode = link.getDestinationNode();
//			if (destNode != null) {
//				extAppName = destNode.getName();
//			}
//		} catch (RepositoryException ignore) {
//			// Ignore.
//		}
//
//		try {
//			SQLDBConnectionDefinition conndef = extractConnectionDefinitionParameters(link, extSysCfg, portType, portName);
//			conndef.setName(poolName);
//			connDefs.put(poolName, conndef);
//		} catch (CodeGenException e) {
//			throw e;
//		} catch (Exception e) {
//			Logger.print(Logger.ERROR, LOG_CATEGORY, "Failed to configure" + " DBConnectionDefinition for runtime DataSourceConnection", e);
//			String msg = e.getMessage();
//			if (msg == null) {
//				msg = MESSAGE_MGR.getString("ERR_EXTSYS_CONN_PARAMS_NOTSET", extAppName);
//			}
//			throw new CodeGenException(msg, e);
//		}
//	}
}
