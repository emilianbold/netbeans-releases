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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;

/**
 *
 * @author sp153251
 */
public class PersistenceCfgProperties {

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
    
    private static final Map<Provider, Map<String, String[]>> possiblePropertyValues = new HashMap<Provider, Map<String, String[]>>();

    static {
        //general 2.0
        possiblePropertyValues.put(null, new HashMap<String, String[]>());//it's for default
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.PESSIMISTIC_LOCK_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.QUERY_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_PERSIST, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_UPDATE, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_REMOVE, null);
        //eclipselink 2.0
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER, new HashMap<String, String[]>());
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
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.PARTITIONING, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(PersistenceUnitProperties.PARTITIONING_CALLBACK, null);
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
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.prefix", null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.suffix", null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put("eclipselink.canonicalmodel.subpackage", null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(ProviderUtil.ECLIPSELINK_PROVIDER.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(ProviderUtil.ECLIPSELINK_PROVIDER.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(ProviderUtil.ECLIPSELINK_PROVIDER.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(ProviderUtil.ECLIPSELINK_PROVIDER.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER).put(ProviderUtil.ECLIPSELINK_PROVIDER.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.ECLIPSELINK_PROVIDER.getTableGenerationCreateValue(),ProviderUtil.ECLIPSELINK_PROVIDER.getTableGenerationDropCreateValue(), PersistenceUnitProperties.NONE });
        //hibernate //TODO? reuse hibernate module?
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER2_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationPropertyName(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.dialect",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.show_sql",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.format_sql",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.transaction.manager_lookup_class",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.max_fetch_depth",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.cfgfile",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.archive.autodetection",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.interceptor",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.interceptor.session_scoped",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.naming_strategy",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.use_class_enhancer",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.discard_pc_on_close",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.resource_scanner",     null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationCreateValue(),ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationDropCreateValue(), "validate", "update" });//NOI18N
        //hibernate jpa 1.0
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER).put(ProviderUtil.HIBERNATE_PROVIDER.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER).put(ProviderUtil.HIBERNATE_PROVIDER.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER).put(ProviderUtil.HIBERNATE_PROVIDER.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER).put(ProviderUtil.HIBERNATE_PROVIDER.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER).put(ProviderUtil.HIBERNATE_PROVIDER.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.HIBERNATE_PROVIDER.getTableGenerationCreateValue(),ProviderUtil.HIBERNATE_PROVIDER.getTableGenerationDropCreateValue(), "validate", "update"  });
        //eclipselink jpa 1.0
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationDropCreateValue(), PersistenceUnitProperties.NONE });
        //openjpa 1.0
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER).put(ProviderUtil.OPENJPA_PROVIDER.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER).put(ProviderUtil.OPENJPA_PROVIDER.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER).put(ProviderUtil.OPENJPA_PROVIDER.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER).put(ProviderUtil.OPENJPA_PROVIDER.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER).put(ProviderUtil.OPENJPA_PROVIDER.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.OPENJPA_PROVIDER.getTableGenerationCreateValue(),ProviderUtil.OPENJPA_PROVIDER.getTableGenerationDropCreateValue() });
        //toplink 1.0
        possiblePropertyValues.put(ProviderUtil.TOPLINK_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcUrl(),null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcDriver(),null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcPassword(),null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcUsername(),null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationDropCreateValue() });
    }
    
    
    public static Object  getPossiblePropertyValue( Provider provider, String propName ) {
        if(provider == null) {
            provider = ProviderUtil.ECLIPSELINK_PROVIDER;
        }//TODO, some logic to add, either search for all providers or some other
        Map<String, String[]> firstMap = possiblePropertyValues.get(provider);
        return firstMap != null ? firstMap.get(propName) : null;
    }
    
    /**
     * return list of pu properties for a provider including default properties
     * return default 2.0 if provider is null
     * @param provider
     * @return 
     */
    public static List<String> getKeys(Provider provider){
        //TODO: cache lists?
        ArrayList<String> ret = new ArrayList<String>();
        if(provider == null || Persistence.VERSION_2_0.equals(ProviderUtil.getVersion(provider))) {
            ret.addAll(possiblePropertyValues.get(null).keySet());
        }
        if(provider !=null ) {
            ret.addAll(possiblePropertyValues.get(provider).keySet());
        }
        return ret;
    }
    
     public static Map<Provider, Map<String, String[]>> getAllKeyAndValues(){
        return possiblePropertyValues;
    }   
    /**
     * return list of supported(by this class) providers with some known properties
     * @return 
     */
    public static List<Provider> getProviders(){
        ArrayList<Provider> ret = new ArrayList<Provider>();
        ret.add(ProviderUtil.ECLIPSELINK_PROVIDER);
        ret.add(ProviderUtil.HIBERNATE_PROVIDER2_0);
        return ret;
    }
}
