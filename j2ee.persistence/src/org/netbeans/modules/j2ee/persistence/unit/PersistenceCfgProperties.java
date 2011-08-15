/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.unit;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;

/**
 *
 * @author sp153251
 */
public class PersistenceCfgProperties {

    public static final String[] defaultJPA20Keys = new String[]{
        PersistenceUnitProperties.PESSIMISTIC_LOCK_TIMEOUT, 
        PersistenceUnitProperties.QUERY_TIMEOUT, 
        PersistenceUnitProperties.VALIDATION_GROUP_PRE_PERSIST, 
        PersistenceUnitProperties.VALIDATION_GROUP_PRE_UPDATE, 
        PersistenceUnitProperties.VALIDATION_GROUP_PRE_REMOVE};
    public static final String[] eclipselink20Keys = new String[]{
        PersistenceUnitProperties.TEMPORAL_MUTABLE, 
        PersistenceUnitProperties.CACHE_TYPE_DEFAULT, 
        PersistenceUnitProperties.CACHE_SIZE_DEFAULT, 
        PersistenceUnitProperties.CACHE_SHARED_DEFAULT, 
        PersistenceUnitProperties.FLUSH_CLEAR_CACHE, 
        PersistenceUnitProperties.THROW_EXCEPTIONS, 
        PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS, 
        PersistenceUnitProperties.WEAVING, 
        PersistenceUnitProperties.WEAVING_LAZY, 
        PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, 
        PersistenceUnitProperties.WEAVING_FETCHGROUPS, 
        PersistenceUnitProperties.WEAVING_INTERNAL, 
        PersistenceUnitProperties.WEAVING_EAGER, 
        PersistenceUnitProperties.SESSION_CUSTOMIZER, 
        PersistenceUnitProperties.VALIDATION_ONLY_PROPERTY, 
        PersistenceUnitProperties.CLASSLOADER, 
        PersistenceUnitProperties.PROFILER, 
        PersistenceUnitProperties.PERSISTENCE_CONTEXT_REFERENCE_MODE, 
        PersistenceUnitProperties.JDBC_BIND_PARAMETERS, 
        PersistenceUnitProperties.NATIVE_SQL, 
        PersistenceUnitProperties.BATCH_WRITING, 
        PersistenceUnitProperties.BATCH_WRITING_SIZE, 
        PersistenceUnitProperties.CACHE_STATEMENTS, 
        PersistenceUnitProperties.CACHE_STATEMENTS_SIZE, 
        PersistenceUnitProperties.EXCLUSIVE_CONNECTION_IS_LAZY, 
        PersistenceUnitProperties.EXCLUSIVE_CONNECTION_MODE, 
        PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MAX, 
        PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MIN, 
        PersistenceUnitProperties.JDBC_READ_CONNECTIONS_SHARED, 
        PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MAX, 
        PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MIN, 
        PersistenceUnitProperties.LOGGING_LOGGER, 
        PersistenceUnitProperties.LOGGING_LEVEL, 
        PersistenceUnitProperties.LOGGING_TIMESTAMP, 
        PersistenceUnitProperties.LOGGING_THREAD, 
        PersistenceUnitProperties.LOGGING_SESSION, 
        PersistenceUnitProperties.LOGGING_EXCEPTIONS, 
        PersistenceUnitProperties.LOGGING_FILE, 
        PersistenceUnitProperties.SESSION_NAME, 
        PersistenceUnitProperties.SESSIONS_XML, 
        PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, 
        PersistenceUnitProperties.INCLUDE_DESCRIPTOR_QUERIES, 
        PersistenceUnitProperties.TARGET_DATABASE, 
        PersistenceUnitProperties.TARGET_SERVER, 
        PersistenceUnitProperties.APP_LOCATION, 
        PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, 
        PersistenceUnitProperties.DROP_JDBC_DDL_FILE, 
        PersistenceUnitProperties.DDL_GENERATION_MODE, 
        PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, 
        "eclipselink.canonicalmodel.prefix", 
        "eclipselink.canonicalmodel.suffix", 
        "eclipselink.canonicalmodel.subpackage"};//TODO: handle properties {propname.entityname}//NOI18N
    public static final String[] hibernate20Keys = new String[]{
        "hibernate.dialect", 
        "hibernate.show_sql", 
        "hibernate.format_sql", 
        "hibernate.transaction.manager_lookup_class", 
        "hibernate.max_fetch_depth", 
        "hibernate.ejb.cfgfile", 
        "hibernate.archive.autodetection", 
        "hibernate.ejb.interceptor", 
        "hibernate.ejb.interceptor.session_scoped", 
        "hibernate.ejb.naming_strategy", 
        "hibernate.ejb.use_class_enhancer", 
        "hibernate.ejb.discard_pc_on_close", 
        "hibernate.ejb.resource_scanner"};//TODO: handle properties {propname.entityname}//NOI18N
    // String[] for selecting one of the values
    private final static String[] TRUE_FALSE = new String[]{"true", "false"}; // NOI18N
    //eclipselink
    private final static String[] EL_CACHE_TYPES = new String[]{"Full", "Weak", "Soft", "SoftWeak", "HardWeak", "NONE"};//NOI18N
    private final static String[] EL_FLUSH_CLEAR_CACHE = new String[]{"Drop", "DropInvalidate", "Merge"};//NOI18N
    private final static String[] EL_WEAWING = new String[] {"true", "false", "static"};//NOI18N
    private final static String[] EL_PROFILER = new String[]{"PerformanceProfiler", "QueryMonitor", "NoProfiler"};//NOI18N
    private final static String[] EL_CONTEXT_REFMODE = new String[]{"HARD", "WEAK", "FORCE_WEAK"};//NOI18N
    private final static String[] EL_BATCHWRITER = new String[]{"JDBC", "Buffered", "Oracle-JDBC", "None"};//NOI18N
    private final static String[] EL_EXCLUSIVE_CON_MODE = new String[]{"Transactional", "Isolated", "Always"};//NOI18N
    private final static String[] EL_LOGGER = new String[]{"DefaultLogger", "JavaLogger", "ServerLogger"};//NOI18N
    private final static String[] EL_LOGGER_LEVEL = new String[]{"OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL"};//NOI18N
    private final static String[] EL_TARGET_DATABASE = new String[]{"Attunity", "Auto", "Cloudscape", "Database", "DB2", "DB2Mainframe", "DBase", "Derby", "HSQL", "Informix", "JavaDB", "MySQL", "Oracle", "PointBase", "PostgreSQL", "SQLAnywhere", "SQLServer", "Sybase", "TimesTen"};//NOI18N
    private final static String[] EL_TARGET_SERVER = new String[]{"None", "WebLogic", "Note", "WebLogic_9", "WebLogic_10", "OC4J", "SunAS9", "Note", "WebSphere", "WebSphere_6_1", "JBoss", "NetWeaver_7_1"};//NOI18N
    private final static String[] EL_DDL_GEN_MODE = new String[]{"both", "database", "sql-script"};//NOI18N
    
    private static final Map<Provider, Map<String, Object>> possiblePropertyValues = new HashMap<Provider, Map<String, Object>>();

    static {
        //general 2.0
        possiblePropertyValues.put(null, new HashMap<String, Object>());//it's for default
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.PESSIMISTIC_LOCK_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.QUERY_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_PERSIST, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_UPDATE, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_REMOVE, null);
        //eclipselink 2.0
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER, new HashMap<String, Object>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.TEMPORAL_MUTABLE, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CACHE_TYPE_DEFAULT, EL_CACHE_TYPES);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CACHE_SIZE_DEFAULT, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.FLUSH_CLEAR_CACHE, EL_FLUSH_CLEAR_CACHE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.THROW_EXCEPTIONS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING, EL_WEAWING);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_LAZY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_FETCHGROUPS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_INTERNAL, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_EAGER, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.SESSION_CUSTOMIZER, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.VALIDATION_ONLY_PROPERTY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CLASSLOADER, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.PROFILER, EL_PROFILER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_REFERENCE_MODE, EL_CONTEXT_REFMODE);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_BIND_PARAMETERS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.NATIVE_SQL, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.BATCH_WRITING, EL_BATCHWRITER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.BATCH_WRITING_SIZE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CACHE_STATEMENTS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CACHE_STATEMENTS_SIZE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.EXCLUSIVE_CONNECTION_IS_LAZY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.EXCLUSIVE_CONNECTION_MODE, EL_EXCLUSIVE_CON_MODE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MAX, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MIN, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_READ_CONNECTIONS_SHARED, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MAX, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MIN, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_LOGGER, EL_LOGGER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_LEVEL, EL_LOGGER_LEVEL);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_TIMESTAMP, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_THREAD, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_SESSION, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_EXCEPTIONS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.LOGGING_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.SESSION_NAME, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.SESSIONS_XML, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.INCLUDE_DESCRIPTOR_QUERIES, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.TARGET_DATABASE, EL_TARGET_DATABASE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.TARGET_SERVER, EL_TARGET_SERVER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.APP_LOCATION, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.DROP_JDBC_DDL_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.DDL_GENERATION_MODE, EL_DDL_GEN_MODE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.prefix", null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.suffix", null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.subpackage", null);
        //hibernate //TODO? reuse hibernate module?
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER2_0, new HashMap<String, Object>());
        //
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER, new HashMap<String, Object>());
    }
    
    
    public static Object  getPossiblePropertyValue( Provider provider, String propName ) {
        Map<String, Object> firstMap = possiblePropertyValues.get(provider);
        return firstMap != null ? firstMap.get(propName) : null;
    }
}
